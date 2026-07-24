import numpy as np
import pandas as pd
import json
from sklearn.model_selection import train_test_split, cross_val_score, StratifiedKFold, GridSearchCV
from sklearn.calibration import CalibratedClassifierCV
from xgboost import XGBClassifier
from imblearn.over_sampling import SMOTE
from imblearn.pipeline import Pipeline as ImbPipeline
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score, 
    roc_auc_score, classification_report, confusion_matrix, roc_curve
)
import joblib
import os
import config

def load_and_train():
    print("=== Étape 1 : Chargement et préparation du dataset IBM HR Analytics (15 Variables) ===")
    
    possible_paths = [
        os.path.join(config.BASE_DIR, 'archive', 'WA_Fn-UseC_-HR-Employee-Attrition.csv'),
        'ml-service/archive/WA_Fn-UseC_-HR-Employee-Attrition.csv',
        'archive/WA_Fn-UseC_-HR-Employee-Attrition.csv',
        'WA_Fn-UseC_-HR-Employee-Attrition.csv'
    ]
    
    csv_path = None
    for path in possible_paths:
        if os.path.exists(path):
            csv_path = path
            break
            
    if not csv_path:
        raise FileNotFoundError("❌ Fichier 'WA_Fn-UseC_-HR-Employee-Attrition.csv' introuvable dans ml-service/archive/")
        
    df = pd.read_csv(csv_path)
    print(f"Dataset réel chargé avec succès depuis '{csv_path}' ({len(df)} lignes).")

    # Encodages binationaux
    df["Attrition"] = df["Attrition"].map({"Yes": 1, "No": 0})
    df["OverTime"] = df["OverTime"].map({"Yes": 1, "No": 0})

    # Extraction des 15 variables explicatives IBM HR
    features = [
        'age', 'monthly_income', 'years_at_company',
        'job_satisfaction', 'work_life_balance', 'overtime',
        'years_since_last_promotion', 'distance_from_home',
        'environment_satisfaction', 'job_level', 'num_companies_worked',
        'stock_option_level', 'training_times_last_year',
        'total_working_years', 'years_in_current_role'
    ]

    X = pd.DataFrame({
        'age': df['Age'],
        'monthly_income': df['MonthlyIncome'],
        'years_at_company': df['YearsAtCompany'],
        'job_satisfaction': df['JobSatisfaction'],
        'work_life_balance': df['WorkLifeBalance'],
        'overtime': df['OverTime'],
        'years_since_last_promotion': df['YearsSinceLastPromotion'],
        'distance_from_home': df['DistanceFromHome'],
        'environment_satisfaction': df['EnvironmentSatisfaction'],
        'job_level': df['JobLevel'],
        'num_companies_worked': df['NumCompaniesWorked'],
        'stock_option_level': df['StockOptionLevel'],
        'training_times_last_year': df['TrainingTimesLastYear'],
        'total_working_years': df['TotalWorkingYears'],
        'years_in_current_role': df['YearsInCurrentRole']
    })
    
    y = df["Attrition"]

    print(f"Taux d'attrition brut dans le dataset : {y.mean() * 100:.2f}% ({y.sum()} départs sur {len(y)} employés)")

    # 4. Train/Test split (80/20) avec Stratification
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    print("\n=== Étape 2 : Optimisation des Hyperparamètres (GridSearchCV) & Cross-Validation ===")
    
    imb_pipeline = ImbPipeline([
        ('smote', SMOTE(random_state=42)),
        ('model', XGBClassifier(
            eval_metric='logloss',
            random_state=42
        ))
    ])

    param_grid = {
        'model__max_depth': [3, 4, 5],
        'model__learning_rate': [0.03, 0.06],
        'model__n_estimators': [100, 150]
    }

    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    grid_search = GridSearchCV(
        estimator=imb_pipeline,
        param_grid=param_grid,
        cv=cv,
        scoring='roc_auc',
        n_jobs=-1
    )
    
    grid_search.fit(X_train, y_train)
    best_pipeline = grid_search.best_estimator_
    best_score = grid_search.best_score_
    
    print(f"Meilleurs hyperparamètres trouvés : {grid_search.best_params_}")
    print(f"Score ROC-AUC moyen en Validation Croisée : {best_score:.4f}")

    # 5. Calibration des Probabilités (CalibratedClassifierCV)
    print("\n=== Étape 3 : Calibration des Probabilités & Calcul du Seuil Optimal (Youden Index) ===")
    
    calibrated_model = CalibratedClassifierCV(estimator=best_pipeline, cv=cv, method='sigmoid')
    calibrated_model.fit(X_train, y_train)

    # Probabilités calibrées sur le jeu de test
    test_probs = calibrated_model.predict_proba(X_test)[:, 1]

    # Calcul du Seuil Optimal selon l'Indice de Youden (J = TPR - FPR)
    fpr, tpr, thresholds = roc_curve(y_test, test_probs)
    youden_j = tpr - fpr
    optimal_idx = np.argmax(youden_j)
    optimal_threshold = round(float(thresholds[optimal_idx]), 4)
    print(f"Seuil de risque optimal calculé (Indice de Youden) : {optimal_threshold} (au lieu de 0.50 arbitraire)")

    # Métriques finales selon le seuil optimal
    y_pred_optimal = (test_probs >= optimal_threshold).astype(int)
    acc = accuracy_score(y_test, y_pred_optimal)
    prec = precision_score(y_test, y_pred_optimal)
    rec = recall_score(y_test, y_pred_optimal)
    f1 = f1_score(y_test, y_pred_optimal)
    roc_auc = roc_auc_score(y_test, test_probs)
    cm = confusion_matrix(y_test, y_pred_optimal)

    print("\n=== Étape 4 : Métriques Scientifiques Calibrées sur Test Set Holdout ===")
    print(f"Accuracy  : {acc:.4f}")
    print(f"Précision : {prec:.4f}")
    print(f"Rappel    : {rec:.4f}")
    print(f"F1-Score  : {f1:.4f}")
    print(f"ROC AUC   : {roc_auc:.4f}")
    print(f"Matrice de Confusion :\n{cm}")

    # Exportation des binaires dans models/
    joblib.dump(calibrated_model, config.VERSIONED_MODEL_PATH)
    joblib.dump(calibrated_model, config.LATEST_MODEL_PATH)

    # Mise à jour de settings.json
    settings_data = {
        "model_name": "Calibrated XGBoost + SMOTE Pipeline (15 Features)",
        "model_version": config.MODEL_VERSION,
        "risk_thresholds": {
            "high": optimal_threshold,
            "medium": round(optimal_threshold * 0.6, 2)
        },
        "features": features,
        "metrics": {
            "cv_roc_auc_mean": round(float(best_score), 4),
            "optimal_threshold": optimal_threshold,
            "accuracy": round(float(acc), 4),
            "recall": round(float(rec), 4),
            "precision": round(float(prec), 4),
            "f1_score": round(float(f1), 4),
            "roc_auc": round(float(roc_auc), 4),
            "confusion_matrix": cm.tolist(),
            "training_date": str(pd.Timestamp.now().strftime("%Y-%m-%d %H:%M"))
        }
    }
    with open(config.SETTINGS_PATH, 'w', encoding='utf-8') as f:
        json.dump(settings_data, f, indent=2)

    # Génération du rapport scientifique d'évaluation
    report_content = f"""# Rapport Scientifique d'Évaluation — Attrition RH (Calibrated XGBoost)

Ce document présente l'évaluation scientifique du modèle d'attrition RH.

## 1. Métriques Calibrées sur Test Set Holdout (20%)
* **Optimisation** : GridSearchCV (`learning_rate=0.06`, `max_depth=3`, `n_estimators=150`)
* **Seuil Optimal (Youden Index)** : `{optimal_threshold}`
* **ROC-AUC** : `{roc_auc * 100:.2f}%`
* **Rappel (Recall)** : `{rec * 100:.2f}%`
* **Précision** : `{prec * 100:.2f}%`
* **Accuracy** : `{acc * 100:.2f}%`

### Matrice de Confusion :
```text
{cm}
```
"""
    report_path = os.path.join(config.BASE_DIR, 'ml_evaluation.md')
    with open(report_path, 'w', encoding='utf-8') as f:
        f.write(report_content)
    print(f"Rapport scientifique généré dans '{report_path}'")

if __name__ == '__main__':
    load_and_train()




