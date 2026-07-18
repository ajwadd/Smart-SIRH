from flask import Flask, request, jsonify
import joblib
import numpy as np
import pandas as pd
import os
from sklearn.ensemble import IsolationForest

app = Flask(__name__)

# Charger le modèle globalement
MODEL_PATH = os.path.join(os.path.dirname(__file__), 'attrition_model.joblib')
model = None

try:
    model = joblib.load(MODEL_PATH)
    print(f"✅ Modèle d'attrition chargé depuis {MODEL_PATH}")
except Exception as e:
    print(f"❌ Impossible de charger le modèle : {e}")

# Générer des données factices réalistes pour entraîner l'Isolation Forest de détection de fraudes
def train_anomaly_model():
    np.random.seed(42)
    n_samples = 1000
    
    # Categories: MEAL=0, TRAVEL=1, LODGING=2, OTHER=3
    categories = np.random.choice([0, 1, 2, 3], size=n_samples, p=[0.4, 0.3, 0.2, 0.1])
    amounts = []
    for cat in categories:
        if cat == 0: # MEAL
            amounts.append(np.random.normal(150, 40))
        elif cat == 1: # TRAVEL
            amounts.append(np.random.normal(300, 80))
        elif cat == 2: # LODGING
            amounts.append(np.random.normal(700, 150))
        else: # OTHER
            amounts.append(np.random.normal(200, 60))
    amounts = np.array(amounts)
    amounts = np.clip(amounts, 10, None)
    
    # Jours (Lundi-Dimanche : 0-6)
    days = np.random.choice([0, 1, 2, 3, 4, 5, 6], size=n_samples, p=[0.18, 0.18, 0.18, 0.18, 0.18, 0.05, 0.05])
    
    df_train = pd.DataFrame({
        'amount': amounts,
        'category': categories,
        'day_of_week': days
    })
    
    iso_forest = IsolationForest(contamination=0.04, random_state=42)
    iso_forest.fit(df_train)
    return iso_forest

anomaly_model = train_anomaly_model()
print("✅ Modèle d'Isolation Forest pour la détection d'anomalies budgétaires entraîné avec succès !")

@app.route('/predict', methods=['POST'])
def predict():
    if model is None:
        return jsonify({"error": "Le modèle de prédiction n'est pas disponible"}), 500
        
    data = request.get_json()
    if not data:
        return jsonify({"error": "Requête vide ou format JSON invalide"}), 400
        
    # Validation et valeurs par défaut
    try:
        age = int(data.get('age', 30))
        monthly_income = float(data.get('monthly_income', 8000.0))
        years_at_company = int(data.get('years_at_company', 3))
        job_satisfaction = int(data.get('job_satisfaction', 3))
        work_life_balance = int(data.get('work_life_balance', 3))
        overtime = int(data.get('overtime', 0))
        num_promotions = int(data.get('num_promotions', 0))
    except (ValueError, TypeError) as e:
        return jsonify({"error": f"Types de données invalides : {str(e)}"}), 400

    # Création du DataFrame d'entrée
    input_data = pd.DataFrame([{
        'age': age,
        'monthly_income': monthly_income,
        'years_at_company': years_at_company,
        'job_satisfaction': job_satisfaction,
        'work_life_balance': work_life_balance,
        'overtime': overtime,
        'num_promotions': num_promotions
    }])

    # Prédiction
    try:
        prediction = int(model.predict(input_data)[0])
        probability = float(model.predict_proba(input_data)[0][1])
    except Exception as e:
        return jsonify({"error": f"Erreur lors de l'exécution de la prédiction : {str(e)}"}), 500

    # Explications qualitatives des facteurs de risque
    factors = []
    if job_satisfaction <= 2:
        factors.append(f"Faible satisfaction au travail ({job_satisfaction}/4)")
    if work_life_balance <= 2:
        factors.append(f"Mauvais équilibre vie pro/perso ({work_life_balance}/4)")
    if overtime == 1:
        factors.append("Heures supplémentaires régulières effectuées")
    if monthly_income < 7000.0:
        factors.append(f"Niveau de salaire en dessous de la moyenne ({monthly_income:.0f} MAD)")
    if num_promotions == 0 and years_at_company > 3:
        factors.append(f"Absence de promotion depuis {years_at_company} ans")

    if not factors:
        factors.append("Aucun facteur de risque critique identifié")

    return jsonify({
        "churn_risk": prediction,
        "probability": probability,
        "risk_level": "ÉLEVÉ" if probability >= 0.70 else "MOYEN" if probability >= 0.40 else "BAS",
        "factors": factors
    })

@app.route('/predict/anomaly', methods=['POST'])
def predict_anomaly():
    if anomaly_model is None:
        return jsonify({"error": "Le modèle de détection d'anomalies n'est pas disponible"}), 500
        
    data = request.get_json()
    if not data:
        return jsonify({"error": "Requête vide ou format JSON invalide"}), 400
        
    try:
        amount = float(data.get('amount', 0.0))
        category_str = data.get('category', 'MEAL').upper()
        day_str = data.get('day_of_week', 'MONDAY').upper()
    except (ValueError, TypeError) as e:
        return jsonify({"error": f"Types de données invalides : {str(e)}"}), 400

    cat_map = {'MEAL': 0, 'TRAVEL': 1, 'LODGING': 2, 'OTHER': 3}
    cat_val = cat_map.get(category_str, 3)

    day_map = {
        'MONDAY': 0, 'TUESDAY': 1, 'WEDNESDAY': 2, 'THURSDAY': 3, 'FRIDAY': 4,
        'SATURDAY': 5, 'SUNDAY': 6
    }
    day_val = day_map.get(day_str, 0)

    input_df = pd.DataFrame([{
        'amount': amount,
        'category': cat_val,
        'day_of_week': day_val
    }])
    
    prediction = int(anomaly_model.predict(input_df)[0]) # 1 = normal, -1 = anomaly
    is_anomaly = (prediction == -1)
    
    score_raw = float(anomaly_model.decision_function(input_df)[0])
    anomaly_score = float(1.0 / (1.0 + np.exp(score_raw * 10.0)))

    reasons = []
    if category_str == 'MEAL' and amount > 250:
        reasons.append(f"Montant pour un repas anormalement élevé ({amount:.0f} MAD, moyenne de référence: 150 MAD)")
    elif category_str == 'TRAVEL' and amount > 500:
        reasons.append(f"Frais de transport anormalement élevés ({amount:.0f} MAD, moyenne de référence: 300 MAD)")
    elif category_str == 'LODGING' and amount > 1100:
        reasons.append(f"Frais d'hébergement anormalement élevés ({amount:.0f} MAD, moyenne de référence: 700 MAD)")
    elif category_str == 'OTHER' and amount > 350:
        reasons.append(f"Autres frais anormalement élevés ({amount:.0f} MAD, moyenne de référence: 200 MAD)")
        
    if day_val >= 5:
        reasons.append(f"Dépense soumise un jour non ouvrable ({day_str.capitalize()})")

    if is_anomaly and not reasons:
        reasons.append("Combinaison de montant et de catégorie statistiquement suspecte par rapport au comportement historique")

    if not is_anomaly:
        reasons.append("Note de frais conforme aux plafonds et aux comportements habituels de l'entreprise")

    return jsonify({
        "is_anomaly": is_anomaly,
        "anomaly_score": anomaly_score,
        "risk_level": "CRITIQUE" if anomaly_score >= 0.70 else "SUSPECT" if is_anomaly else "NORMAL",
        "reasons": reasons
    })

@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status": "UP",
        "model_loaded": model is not None,
        "anomaly_model_loaded": anomaly_model is not None
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8083)
