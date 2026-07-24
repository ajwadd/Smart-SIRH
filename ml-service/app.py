import os
import time
import glob
import logging
from flask import Flask, request, jsonify
import config
from prediction_service import PredictionService

logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')
logger = logging.getLogger("ml_service")

app = Flask(__name__)
service = PredictionService()

METRICS_STORE = {
    "total_predictions": 0,
    "total_latency_ms": 0.0,
    "total_errors": 0,
    "last_prediction_time": None
}

@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status": "UP",
        "model_loaded": service.attrition_model is not None,
        "anomaly_model_loaded": service.anomaly_model is not None
    })

@app.route('/model/info', methods=['GET'])
def model_info():
    return jsonify({
        "model_name": config.SETTINGS.get("model_name", "Calibrated XGBoost + SMOTE Pipeline (15 Features)"),
        "model_version": config.MODEL_VERSION,
        "features": config.FEATURES,
        "risk_thresholds": {
            "high": config.RISK_THRESHOLD_HIGH,
            "medium": config.RISK_THRESHOLD_MEDIUM
        },
        "metrics": config.SETTINGS.get("metrics", {})
    })

@app.route('/metrics', methods=['GET'])
def metrics():
    avg_latency = (
        METRICS_STORE["total_latency_ms"] / METRICS_STORE["total_predictions"]
        if METRICS_STORE["total_predictions"] > 0 else 0.0
    )
    return jsonify({
        "total_predictions": METRICS_STORE["total_predictions"],
        "average_latency_ms": round(avg_latency, 2),
        "total_errors": METRICS_STORE["total_errors"],
        "last_prediction_time": METRICS_STORE["last_prediction_time"],
        "status": "HEALTHY"
    })

@app.route('/model/versions', methods=['GET'])
def list_model_versions():
    model_files = glob.glob(os.path.join(config.MODELS_DIR, "*.joblib"))
    versions = [os.path.basename(f) for f in model_files]
    return jsonify({
        "current_version": config.MODEL_VERSION,
        "available_versions": sorted(versions)
    })

@app.route('/predict', methods=['POST'])
def predict():
    start_time = time.time()
    data = request.get_json()
    if not data:
        METRICS_STORE["total_errors"] += 1
        return jsonify({"error": "Requête vide ou format JSON invalide"}), 400
        
    try:
        employee_id = str(data.get('employee_id', data.get('employeeId', 'ANONYMOUS')))
        age = int(data.get('age', 30))
        monthly_income = float(data.get('monthly_income', 8000.0))
    except (ValueError, TypeError) as e:
        METRICS_STORE["total_errors"] += 1
        return jsonify({"error": f"Types de données invalides : {str(e)}"}), 400

    if not (18 <= age <= 80):
        return jsonify({"error": f"Âge invalide ({age}). Doit être entre 18 et 80 ans."}), 400
    if monthly_income < 0:
        return jsonify({"error": f"Salaire mensuel invalide ({monthly_income}). Doit être positif."}), 400

    payload = {
        'age': age,
        'monthly_income': monthly_income,
        'years_at_company': int(data.get('years_at_company', 3)),
        'job_satisfaction': int(data.get('job_satisfaction', 3)),
        'work_life_balance': int(data.get('work_life_balance', 3)),
        'overtime': int(data.get('overtime', 0)),
        'years_since_last_promotion': int(data.get('years_since_last_promotion', data.get('num_promotions', 0))),
        'distance_from_home': int(data.get('distance_from_home', 5)),
        'environment_satisfaction': int(data.get('environment_satisfaction', 3)),
        'job_level': int(data.get('job_level', 2)),
        'num_companies_worked': int(data.get('num_companies_worked', 1)),
        'stock_option_level': int(data.get('stock_option_level', 0)),
        'training_times_last_year': int(data.get('training_times_last_year', 2)),
        'total_working_years': int(data.get('total_working_years', 5)),
        'years_in_current_role': int(data.get('years_in_current_role', 2))
    }

    try:
        res = service.predict_attrition(payload)
    except Exception as e:
        METRICS_STORE["total_errors"] += 1
        return jsonify({"error": f"Erreur lors de la prédiction : {str(e)}"}), 500

    latency_ms = round((time.time() - start_time) * 1000, 2)
    METRICS_STORE["total_predictions"] += 1
    METRICS_STORE["total_latency_ms"] += latency_ms
    timestamp_now = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    METRICS_STORE["last_prediction_time"] = timestamp_now

    logger.info(f"Prédiction exécutée pour '{employee_id}' en {latency_ms}ms | Risque: {res['risk_level']} ({res['leave_probability']:.2%})")

    return jsonify(res)

@app.route('/predict/anomaly', methods=['POST'])
def predict_anomaly():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Requête vide ou format JSON invalide"}), 400
        
    try:
        amount = float(data.get('amount', 0.0))
        submission_hour = int(data.get('submission_hour', 14))
        travel_distance_km = float(data.get('travel_distance_km', data.get('distance', 0.0)))
    except (ValueError, TypeError) as e:
        return jsonify({"error": f"Types de données invalides : {str(e)}"}), 400

    if amount < 0 or amount > 100000:
        return jsonify({"error": f"Montant de dépense hors limites ({amount} MAD). Doit être entre 0 et 100 000 MAD."}), 400
    if not (0 <= submission_hour <= 23):
        return jsonify({"error": f"Heure de soumission invalide ({submission_hour}h). Doit être entre 0h et 23h."}), 400
    if travel_distance_km < 0:
        return jsonify({"error": f"Distance invalide ({travel_distance_km} km). Doit être >= 0."}), 400

    payload = {
        'amount': amount,
        'category': data.get('category', 'MEAL'),
        'job_level': data.get('job_level', 'Senior'),
        'day_of_week': data.get('day_of_week', 'MONDAY'),
        'submission_hour': submission_hour,
        'travel_distance_km': travel_distance_km,
        'receipt_uploaded': int(data.get('receipt_uploaded', 1)),
        'claim_delay_days': int(data.get('claim_delay_days', 2))
    }

    try:
        res = service.predict_anomaly(payload)
        return jsonify(res)
    except Exception as e:
        return jsonify({"error": f"Erreur anomalie : {str(e)}"}), 500

@app.route('/model/retrain', methods=['POST'])
def retrain_model():
    try:
        logger.info("⚡ Déclenchement du ré-entraînement du modèle ML...")
        from train_attrition import load_and_train
        load_and_train()
        service.load_models()
        return jsonify({
            "message": "Ré-entraînement et rechargement réussis",
            "model_version": config.MODEL_VERSION,
            "status": "SUCCESS"
        })
    except Exception as e:
        logger.error(f"Erreur lors du ré-entraînement : {e}")
        return jsonify({"error": f"Échec du ré-entraînement : {str(e)}"}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8083)
