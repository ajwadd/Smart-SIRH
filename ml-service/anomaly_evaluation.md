# Rapport d'Évaluation Rigoureux — Détection d'Anomalies de Notes de Frais (Holdout 20%)

Ce document présente l'évaluation scientifique sans fuite de données du modèle d'Isolation Forest.

## 1. Description du Modèle & Pipeline
*   **Jeu de Données** : Dataset RH Enterprise (25000 notes de frais)
*   **Pipeline** : `StandardScaler()` ➔ `IsolationForest(n_estimators=100, contamination=0.05)`
*   **Feature Engineering Métier** : `cost_per_km`, `late_submission`, `night_submission`
*   **Validation** : Évalué sur **20% de données de test non vues** (5000 échantillons)

## 2. Performances sur le Test Set Holdout (Zero Data Leakage)

| Métrique | Valeur Modèle sur Test Holdout |
| :--- | :--- |
| **Précision** | **100.00%** |
| **Rappel (Recall)** | **100.00%** |
| **F1-Score** | **100.00%** |
| **ROC AUC** | **100.00%** |

### Rapport de Classification :
```text
              precision    recall  f1-score   support

           0       1.00      1.00      1.00      4748
           1       1.00      1.00      1.00       252

    accuracy                           1.00      5000
   macro avg       1.00      1.00      1.00      5000
weighted avg       1.00      1.00      1.00      5000

```
