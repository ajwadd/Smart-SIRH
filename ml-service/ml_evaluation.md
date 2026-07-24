# Rapport d'Évaluation Quantitative & Rigoureuse — XGBoost + SMOTE imblearn

Ce document présente l'évaluation scientifique sans fuite de données (*Zero Data Leakage*) du modèle d'attrition des employés.

## 1. Description de la Pipeline imblearn
*   **Jeu de Données** : IBM HR Employee Attrition & Performance (1470 lignes)
*   **Pipeline** : `imblearn.pipeline.Pipeline([('smote', SMOTE()), ('model', XGBClassifier())])`
*   **Intégrité Scientifique** : SMOTE est exécuté exclusivement à l'intérieur de chaque pli de la validation croisée.
*   **Hyperparamètres XGBoost** : `n_estimators=120`, `max_depth=4`, `learning_rate=0.06`, `subsample=0.8`
*   **Version du Modèle** : `v2.0`

## 2. Validation Croisée Scientifique (5-Fold Stratified CV sans Fuite)
*   **Scores ROC-AUC par Fold** : `[0.7442, 0.7346, 0.7761, 0.7812, 0.7063]`
*   **Moyenne ROC-AUC en CV** : **74.85%** (+/- 5.53%)

## 3. Métriques de Performance sur le Test Set (20% Holdout)

| Métrique | Valeur Modèle (Imblearn XGBoost) | Baseline (Majoritaire) |
| :--- | :--- | :--- |
| **Accuracy** | **79.25%** | 83.88% |
| **Précision** | **38.33%** | 0.00% |
| **Rappel (Recall)** | **48.94%** | 0.00% |
| **F1-Score** | **42.99%** | 0.00% |
| **ROC AUC** | **76.66%** | 50.00% |

### Matrice de Confusion
```text
Vrais Négatifs (TN): 210 | Faux Positifs (FP): 37
Faux Négatifs (FN): 24 | Vrais Positifs (TP): 23
```

### Rapport de Classification Complet :
```text
              precision    recall  f1-score   support

           0       0.90      0.85      0.87       247
           1       0.38      0.49      0.43        47

    accuracy                           0.79       294
   macro avg       0.64      0.67      0.65       294
weighted avg       0.82      0.79      0.80       294

```

## 4. Feature Importances
1. **stock_option_level** : 24.84%
2. **job_satisfaction** : 10.51%
3. **work_life_balance** : 8.90%
4. **environment_satisfaction** : 7.43%
5. **job_level** : 6.13%
6. **training_times_last_year** : 5.54%
7. **monthly_income** : 5.19%
8. **years_in_current_role** : 4.84%
9. **age** : 4.35%
10. **years_at_company** : 4.27%
11. **total_working_years** : 3.79%
12. **distance_from_home** : 3.78%
13. **overtime** : 3.54%
14. **years_since_last_promotion** : 3.50%
15. **num_companies_worked** : 3.40%
