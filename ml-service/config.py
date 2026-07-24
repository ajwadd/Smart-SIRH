import os
import json

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(BASE_DIR, 'models')
os.makedirs(MODELS_DIR, exist_ok=True)

SETTINGS_PATH = os.path.join(BASE_DIR, 'settings.json')

# Chargement dynamique depuis settings.json
if os.path.exists(SETTINGS_PATH):
    with open(SETTINGS_PATH, 'r', encoding='utf-8') as f:
        SETTINGS = json.load(f)
else:
        SETTINGS = {
            "model_version": "v2.0",
            "risk_thresholds": {"high": 0.60, "medium": 0.35},
            "features": [
                'age', 'monthly_income', 'years_at_company',
                'job_satisfaction', 'work_life_balance', 'overtime',
                'years_since_last_promotion'
            ]
        }

MODEL_VERSION = SETTINGS.get("model_version", "v2.0")
MODEL_FILENAME = f"attrition_{MODEL_VERSION}.joblib"
LATEST_MODEL_PATH = os.path.join(MODELS_DIR, "attrition_latest.joblib")
VERSIONED_MODEL_PATH = os.path.join(MODELS_DIR, MODEL_FILENAME)

try:
    RISK_THRESHOLD_HIGH = float(SETTINGS.get("risk_thresholds", {}).get("high", 0.60))
except Exception:
    RISK_THRESHOLD_HIGH = 0.60

try:
    RISK_THRESHOLD_MEDIUM = float(SETTINGS.get("risk_thresholds", {}).get("medium", 0.35))
except Exception:
    RISK_THRESHOLD_MEDIUM = 0.35

DEFAULT_FEATURES = [
    'age', 'monthly_income', 'years_at_company',
    'job_satisfaction', 'work_life_balance', 'overtime',
    'years_since_last_promotion', 'distance_from_home',
    'environment_satisfaction', 'job_level', 'num_companies_worked',
    'stock_option_level', 'training_times_last_year',
    'total_working_years', 'years_in_current_role'
]

FEATURES = SETTINGS.get("features", DEFAULT_FEATURES)
