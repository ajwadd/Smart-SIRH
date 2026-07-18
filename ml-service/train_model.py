import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report, roc_auc_score, f1_score
import joblib
import os

def generate_and_train():
    print("=== Étape 1 : Génération du dataset RH synthétique ===")
    np.random.seed(42)
    n_samples = 1500

    # Caractéristiques des employés
    age = np.random.randint(22, 60, n_samples)
    monthly_income = np.random.randint(4000, 25000, n_samples) # en MAD
    years_at_company = np.random.randint(1, 15, n_samples)
    job_satisfaction = np.random.randint(1, 5, n_samples) # 1 (bas) à 4 (très haut)
    work_life_balance = np.random.randint(1, 5, n_samples)
    overtime = np.random.choice([0, 1], n_samples, p=[0.7, 0.3])
    num_promotions = np.random.randint(0, 4, n_samples)
    
    # Calcul de la probabilité de départ (churn) basée sur des critères logiques
    # Plus le score est élevé, plus le risque de départ est grand
    churn_score = (
        (5 - job_satisfaction) * 2.0 + 
        (5 - work_life_balance) * 1.5 + 
        (overtime * 3.0) - 
        (monthly_income / 6000.0) - 
        (num_promotions * 1.0) +
        (np.random.normal(0, 1.5, n_samples)) # bruit aléatoire
    )
    
    # Seuil pour définir le churn (0 ou 1)
    # Ajusté pour avoir environ 15-20% de départs (taux d'attrition classique)
    churn = (churn_score > np.percentile(churn_score, 80)).astype(int)

    df = pd.DataFrame({
        'age': age,
        'monthly_income': monthly_income,
        'years_at_company': years_at_company,
        'job_satisfaction': job_satisfaction,
        'work_life_balance': work_life_balance,
        'overtime': overtime,
        'num_promotions': num_promotions,
        'churn': churn
    })

    print(f"Dataset généré avec succès ({n_samples} lignes).")
    print(f"Taux d'attrition global : {df['churn'].mean() * 100:.2f}%")

    # Séparation X / y
    X = df.drop('churn', axis=1)
    y = df['churn']

    # Train/Test split (80/20)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    print("\n=== Étape 2 : Entraînement du modèle RandomForest ===")
    model = RandomForestClassifier(n_estimators=100, max_depth=8, random_state=42, class_weight='balanced')
    model.fit(X_train, y_train)
    print("Modèle entraîné avec succès.")

    # Évaluation
    y_pred = model.predict(X_test)
    y_prob = model.predict_proba(X_test)[:, 1]

    accuracy = accuracy_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)
    roc_auc = roc_auc_score(y_test, y_prob)

    print("\n=== Étape 3 : Métriques d'évaluation du modèle ===")
    print(f"Accuracy : {accuracy:.4f}")
    print(f"F1-Score : {f1:.4f}")
    print(f"ROC AUC  : {roc_auc:.4f}")
    print("\nRapport de classification :")
    print(classification_report(y_test, y_pred))

    # Sauvegarde du modèle et des métadonnées
    os.makedirs('ml-service', exist_ok=True)
    model_path = 'ml-service/attrition_model.joblib'
    joblib.dump(model, model_path)
    print(f"\nModèle exporté vers '{model_path}'")

    # Écriture du rapport d'évaluation markdown
    report_content = f"""# Rapport d'Évaluation Quantitative — Modèle Prédictif d'Attrition (ML)

Ce document présente l'évaluation quantitative du modèle de classification entraîné pour prédire les démissions d'employés (churn).

## 1. Description du Modèle
*   **Algorithme** : Random Forest Classifier (Scikit-Learn)
*   **Hyperparamètres** : `n_estimators=100`, `max_depth=8`, `class_weight='balanced'`
*   **Caractéristiques d'Entrée** :
    1.  `age` (Âge de l'employé)
    2.  `monthly_income` (Salaire brut mensuel en MAD)
    3.  `years_at_company` (Ancienneté en années)
    4.  `job_satisfaction` (Score de satisfaction de 1 à 4)
    5.  `work_life_balance` (Score d'équilibre vie pro/perso de 1 à 4)
    6.  `overtime` (Heures supplémentaires effectuées : 0 ou 1)
    7.  `num_promotions` (Nombre de promotions sur les 5 dernières années)

## 2. Performances du Modèle sur le Jeu de Test (20% de 1500 échantillons)
Le modèle a été évalué par rapport à une baseline représentée par un classifieur majoritaire (ZeroR).

| Métrique | Modèle Random Forest | Baseline (Majoritaire) |
| :--- | :--- | :--- |
| **Accuracy** | {accuracy * 100:.2f}% | 80.00% |
| **F1-Score** | {f1 * 100:.2f}% | 0.00% |
| **ROC AUC** | {roc_auc * 100:.2f}% | 50.00% |

### Rapport de Classification Complet :
```text
{classification_report(y_test, y_pred)}
```

## 3. Importance des Caractéristiques (Feature Importance)
L'importance des variables calculée par la forêt d'arbres décisionnels permet d'expliquer pourquoi un employé est à risque :

"""
    importances = model.feature_importances_
    features = X.columns
    indices = np.argsort(importances)[::-1]
    
    for rank, idx in enumerate(indices):
        report_content += f"{rank + 1}. **{features[idx]}** : {importances[idx] * 100:.2f}%\n"

    # Enregistrer le rapport
    report_path = 'ml-service/ml_evaluation.md'
    with open(report_path, 'w', encoding='utf-8') as f:
        f.write(report_content)
    print(f"Rapport d'évaluation écrit vers '{report_path}'")

if __name__ == '__main__':
    generate_and_train()
