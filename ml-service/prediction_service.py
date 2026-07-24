import os
import time
import uuid
import json
import logging
import pandas as pd
import numpy as np
import joblib
import shap
import config

logger = logging.getLogger("ml_service")

class PredictionService:
    def __init__(self):
        self.attrition_model = None
        self.anomaly_model = None
        self.shap_explainer = None
        self.expense_policy = {}
        self.load_models()
        self.load_policy()

    def load_models(self):
        if os.path.exists(config.LATEST_MODEL_PATH):
            try:
                self.attrition_model = joblib.load(config.LATEST_MODEL_PATH)
                try:
                    xgb_step = self.attrition_model.named_steps['model'] if hasattr(self.attrition_model, 'named_steps') else self.attrition_model
                    self.shap_explainer = shap.TreeExplainer(xgb_step)
                except Exception as se:
                    logger.warning(f"Avertissement SHAP Init : {se}")
            except Exception as e:
                logger.error(f"Erreur chargement modèle attrition : {e}")

        anomaly_path = os.path.join(config.MODELS_DIR, 'expense_anomaly_v1.joblib')
        if os.path.exists(anomaly_path):
            try:
                self.anomaly_model = joblib.load(anomaly_path)
            except Exception as e:
                logger.error(f"Erreur chargement modèle anomalies : {e}")

    def load_policy(self):
        policy_path = os.path.join(config.BASE_DIR, 'expense_policy.json')
        if os.path.exists(policy_path):
            try:
                with open(policy_path, 'r', encoding='utf-8') as f:
                    self.expense_policy = json.load(f)
            except Exception as e:
                logger.error(f"Erreur chargement expense_policy.json : {e}")

    def predict_attrition(self, data: dict) -> dict:
        start_time = time.time()
        if self.attrition_model is None:
            raise RuntimeError("Le modèle d'attrition n'est pas disponible")

        input_df = pd.DataFrame([{
            'age': data['age'],
            'monthly_income': data['monthly_income'],
            'years_at_company': data['years_at_company'],
            'job_satisfaction': data['job_satisfaction'],
            'work_life_balance': data['work_life_balance'],
            'overtime': data['overtime'],
            'years_since_last_promotion': data['years_since_last_promotion'],
            'distance_from_home': data.get('distance_from_home', 5),
            'environment_satisfaction': data.get('environment_satisfaction', 3),
            'job_level': data.get('job_level', 2),
            'num_companies_worked': data.get('num_companies_worked', 1),
            'stock_option_level': data.get('stock_option_level', 0),
            'training_times_last_year': data.get('training_times_last_year', 2),
            'total_working_years': data.get('total_working_years', 5),
            'years_in_current_role': data.get('years_in_current_role', 2)
        }])

        pred = int(self.attrition_model.predict(input_df)[0])
        probs = self.attrition_model.predict_proba(input_df)[0]
        stay_prob, leave_prob = float(probs[0]), float(probs[1])

        shap_explanations, top_3_shap = {}, {}
        if self.shap_explainer is not None:
            try:
                raw_shap = self.shap_explainer.shap_values(input_df)
                sv = raw_shap[1][0] if isinstance(raw_shap, list) else (raw_shap[0, :, 1] if len(raw_shap.shape) == 3 else raw_shap[0])
                for feat, val in zip(config.FEATURES, sv):
                    shap_explanations[feat] = round(float(val), 4)
                top_3_shap = dict(sorted(shap_explanations.items(), key=lambda x: abs(x[1]), reverse=True)[:3])
            except Exception as e:
                logger.warning(f"Erreur SHAP : {e}")

        feature_importances = {}
        if hasattr(self.attrition_model, 'named_steps'):
            m_obj = self.attrition_model.named_steps['model']
            if hasattr(m_obj, 'feature_importances_'):
                for feat, imp in zip(config.FEATURES, m_obj.feature_importances_):
                    feature_importances[feat] = round(float(imp), 4)

        factors = []
        if data['job_satisfaction'] <= 2: factors.append(f"Faible satisfaction au travail ({data['job_satisfaction']}/4)")
        if data['work_life_balance'] <= 2: factors.append(f"Mauvais équilibre vie pro/perso ({data['work_life_balance']}/4)")
        if data['overtime'] == 1: factors.append("Heures supplémentaires régulières effectuées")
        if data['monthly_income'] < 7000.0: factors.append(f"Niveau de salaire en dessous de la moyenne ({data['monthly_income']:.0f} MAD)")
        if data['years_since_last_promotion'] >= 3: factors.append(f"Absence de promotion depuis {data['years_since_last_promotion']} ans")
        if not factors: factors.append("Aucun facteur de risque critique identifié")

        risk_level = "ÉLEVÉ" if leave_prob >= config.RISK_THRESHOLD_HIGH else ("MOYEN" if leave_prob >= config.RISK_THRESHOLD_MEDIUM else "BAS")
        latency_ms = round((time.time() - start_time) * 1000, 2)

        return {
            "model_name": config.SETTINGS.get("model_name", "Calibrated XGBoost + SMOTE Pipeline (15 Features)"),
            "model_version": config.MODEL_VERSION,
            "churn_risk": pred,
            "probability": leave_prob,
            "stay_probability": round(stay_prob, 4),
            "leave_probability": round(leave_prob, 4),
            "risk_level": risk_level,
            "factors": factors,
            "feature_importances": feature_importances,
            "shap_explanations": shap_explanations,
            "top_3_shap": top_3_shap,
            "execution_time_ms": latency_ms
        }

    def predict_anomaly(self, data: dict) -> dict:
        if self.anomaly_model is None:
            raise RuntimeError("Le modèle de détection d'anomalies n'est pas disponible")

        cat_map = {'MEAL': 0, 'TRAVEL': 1, 'LODGING': 2, 'HOTEL': 2, 'FUEL': 3, 'OFFICE_SUPPLIES': 4, 'TRAINING': 5, 'CLIENT_MEETING': 6, 'OTHER': 7}
        cat_val = cat_map.get(data['category'].upper(), 7)

        level_map = {'Junior': 0, 'Senior': 1, 'Manager': 2, 'Director': 3}
        level_val = level_map.get(data['job_level'], 1)

        day_map = {'MONDAY': 0, 'TUESDAY': 1, 'WEDNESDAY': 2, 'THURSDAY': 3, 'FRIDAY': 4, 'SATURDAY': 5, 'SUNDAY': 6}
        day_val = day_map.get(data['day_of_week'].upper(), 0)
        weekend = 1 if day_val >= 5 else 0

        cost_per_km = data['amount'] / (data['travel_distance_km'] + 1.0)
        late_submission = 1 if data['claim_delay_days'] > 7 else 0
        night_submission = 1 if (data['submission_hour'] < 6 or data['submission_hour'] > 22) else 0

        input_df = pd.DataFrame([{
            'amount': data['amount'],
            'category_code': cat_val,
            'job_level_code': level_val,
            'day_index': day_val,
            'weekend': weekend,
            'submission_hour': data['submission_hour'],
            'travel_distance_km': data['travel_distance_km'],
            'receipt_uploaded': data['receipt_uploaded'],
            'claim_delay_days': data['claim_delay_days'],
            'cost_per_km': cost_per_km,
            'late_submission': late_submission,
            'night_submission': night_submission
        }])

        pred = int(self.anomaly_model.predict(input_df)[0])
        is_anomaly = (pred == -1)
        score_raw = float(self.anomaly_model.decision_function(input_df)[0])
        anomaly_score = float(1.0 / (1.0 + np.exp(score_raw * 10.0)))

        limits = self.expense_policy.get('limits', {})
        cat_policy = limits.get(data['category'].upper(), {})
        max_amount = cat_policy.get('max_amount_mad', 300.0)

        reasons = []
        if data['amount'] > max_amount:
            reasons.append(f"Montant pour {data['category']} dépasse le plafond de la politique entreprise ({data['amount']:.0f} MAD > {max_amount:.0f} MAD)")
        if weekend == 1:
            reasons.append(f"Dépense soumise le week-end ({data['day_of_week'].capitalize()})")
        if night_submission == 1:
            reasons.append(f"Dépense soumise à une heure très inhabituelle ({data['submission_hour']}h00)")
        if late_submission == 1:
            reasons.append(f"Dépense soumise avec un retard important ({data['claim_delay_days']} jours)")
        if data['receipt_uploaded'] == 0 and data['amount'] > 100.0:
            reasons.append(f"Reçu de justificatif manquant pour un montant de {data['amount']:.0f} MAD")

        if is_anomaly and not reasons:
            reasons.append("Combinaison de montant, catégorie et contexte de déplacement statistiquement anormale")
        if not is_anomaly:
            reasons.append("Note de frais conforme aux plafonds et comportements habituels de l'entreprise")

        return {
            "is_anomaly": is_anomaly,
            "anomaly_score": round(anomaly_score, 4),
            "risk_level": "CRITIQUE" if anomaly_score >= 0.70 else ("SUSPECT" if is_anomaly else "NORMAL"),
            "reasons": reasons
        }
