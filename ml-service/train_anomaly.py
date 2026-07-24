import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.ensemble import IsolationForest
from sklearn.metrics import classification_report, precision_score, recall_score, f1_score, roc_auc_score
import joblib
import os
import config

def load_and_train_anomaly():
    print("=== Étape 1 : Chargement et Feature Engineering du Dataset Notes de Frais Enterprise ===")
    
    possible_paths = [
        os.path.join(config.BASE_DIR, 'archive', 'hr_expense_claims_dataset.csv'),
        'ml-service/archive/hr_expense_claims_dataset.csv',
        'archive/hr_expense_claims_dataset.csv',
        'hr_expense_claims_dataset.csv'
    ]
    
    csv_path = None
    for path in possible_paths:
        if os.path.exists(path):
            csv_path = path
            break
            
    if not csv_path:
        raise FileNotFoundError("❌ Fichier 'hr_expense_claims_dataset.csv' introuvable dans ml-service/archive/")
        
    df = pd.read_csv(csv_path)
    print(f"Dataset de notes de frais chargé avec succès ({len(df)} enregistrements).")

    # Mappage des catégories
    cat_map = {
        'MEAL': 0, 'TRAVEL': 1, 'LODGING': 2, 'HOTEL': 2,
        'FUEL': 3, 'OFFICE_SUPPLIES': 4, 'TRAINING': 5,
        'CLIENT_MEETING': 6, 'OTHER': 7
    }
    df['category_code'] = df['category'].str.upper().map(lambda c: cat_map.get(c, 7))

    # Mappage des niveaux de poste
    level_map = {'Junior': 0, 'Senior': 1, 'Manager': 2, 'Director': 3}
    df['job_level_code'] = df['job_level'].map(lambda l: level_map.get(l, 1))

    # Mappage des jours
    day_map = {
        'MONDAY': 0, 'TUESDAY': 1, 'WEDNESDAY': 2, 'THURSDAY': 3, 'FRIDAY': 4,
        'SATURDAY': 5, 'SUNDAY': 6
    }
    df['day_index'] = df['day_of_week'].str.upper().map(lambda d: day_map.get(d, 0))

    # --- FEATURE ENGINEERING RH ---
    # 1. Coût par km pour détecter les remboursements de transport disproportionnés
    df['cost_per_km'] = df['amount'] / (df['travel_distance_km'] + 1.0)
    # 2. Soumission tardive (> 7 jours)
    df['late_submission'] = (df['claim_delay_days'] > 7).astype(int)
    # 3. Soumission nocturne (avant 6h ou après 22h)
    df['night_submission'] = ((df['submission_hour'] < 6) | (df['submission_hour'] > 22)).astype(int)

    # Variables métier sélectionnées
    features = [
        'amount', 'category_code', 'job_level_code', 'day_index', 
        'weekend', 'submission_hour', 'travel_distance_km', 
        'receipt_uploaded', 'claim_delay_days',
        'cost_per_km', 'late_submission', 'night_submission'
    ]
    X = df[features]
    y_true = df['is_anomaly']

    # 2. Séparation Rigoureuse Train / Test Holdout (80/20) - Élimination de la fuite d'évaluation
    X_train, X_test, y_train, y_test = train_test_split(
        X, y_true, test_size=0.2, random_state=42, stratify=y_true
    )

    print(f"\nDonnées d'entraînement : {len(X_train)} | Données de test holdout : {len(X_test)}")

    # 3. Pipeline Scikit-Learn avec StandardScaler + IsolationForest
    print("\n=== Étape 2 : Entraînement de la Pipeline (StandardScaler + IsolationForest) ===")
    pipeline = Pipeline([
        ('scaler', StandardScaler()),
        ('model', IsolationForest(
            n_estimators=100,
            contamination=0.05,
            random_state=42
        ))
    ])
    pipeline.fit(X_train)
    print("Pipeline d'anomalies entraîné avec succès sur le jeu d'entraînement.")

    # 4. Évaluation sur le jeu de TEST HOLDOUT uniquement (Zero Leakage)
    raw_pred = pipeline.predict(X_test)
    y_pred = (raw_pred == -1).astype(int)
    
    # Decision function sur le test set
    scores = -pipeline.decision_function(X_test)

    prec = precision_score(y_test, y_pred)
    rec = recall_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)
    roc_auc = roc_auc_score(y_test, scores)

    print("\n=== Étape 3 : Évaluation Scientifique sur le Test Set Holdout (20%) ===")
    print(f"Précision : {prec:.4f}")
    print(f"Rappel    : {rec:.4f}")
    print(f"F1-Score  : {f1:.4f}")
    print(f"ROC AUC   : {roc_auc:.4f}")
    print("\nRapport de classification complet sur données de TEST :")
    print(classification_report(y_test, y_pred))

    # Sauvegarde de la pipeline entraînée dans models/
    model_path = os.path.join(config.MODELS_DIR, 'expense_anomaly_v1.joblib')
    joblib.dump(pipeline, model_path)

    print(f"\n[OK] Pipeline d'anomalies (StandardScaler + IsolationForest) exportée vers '{model_path}'")

    # Rapport d'évaluation
    report_content = f"""# Rapport d'Évaluation Rigoureux — Détection d'Anomalies de Notes de Frais (Holdout 20%)

Ce document présente l'évaluation scientifique sans fuite de données du modèle d'Isolation Forest.

## 1. Description du Modèle & Pipeline
*   **Jeu de Données** : Dataset RH Enterprise ({len(df)} notes de frais)
*   **Pipeline** : `StandardScaler()` ➔ `IsolationForest(n_estimators=100, contamination=0.05)`
*   **Feature Engineering Métier** : `cost_per_km`, `late_submission`, `night_submission`
*   **Validation** : Évalué sur **20% de données de test non vues** ({len(X_test)} échantillons)

## 2. Performances sur le Test Set Holdout (Zero Data Leakage)

| Métrique | Valeur Modèle sur Test Holdout |
| :--- | :--- |
| **Précision** | **{prec * 100:.2f}%** |
| **Rappel (Recall)** | **{rec * 100:.2f}%** |
| **F1-Score** | **{f1 * 100:.2f}%** |
| **ROC AUC** | **{roc_auc * 100:.2f}%** |

### Rapport de Classification :
```text
{classification_report(y_test, y_pred)}
```
"""
    report_path = os.path.join(config.BASE_DIR, 'anomaly_evaluation.md')
    with open(report_path, 'w', encoding='utf-8') as f:
        f.write(report_content)
    print(f"Rapport d'évaluation généré dans '{report_path}'")

if __name__ == '__main__':
    load_and_train_anomaly()

