# Rapport d'Évaluation Quantitative — Modèle Prédictif d'Attrition (ML)

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
| **Accuracy** | 89.33% | 80.00% |
| **F1-Score** | 73.77% | 0.00% |
| **ROC AUC** | 94.81% | 50.00% |

### Rapport de Classification Complet :
```text
              precision    recall  f1-score   support

           0       0.94      0.93      0.93       240
           1       0.73      0.75      0.74        60

    accuracy                           0.89       300
   macro avg       0.83      0.84      0.84       300
weighted avg       0.89      0.89      0.89       300

```

## 3. Importance des Caractéristiques (Feature Importance)
L'importance des variables calculée par la forêt d'arbres décisionnels permet d'expliquer pourquoi un employé est à risque :

1. **job_satisfaction** : 27.77%
2. **work_life_balance** : 18.84%
3. **monthly_income** : 15.41%
4. **overtime** : 13.07%
5. **num_promotions** : 9.46%
6. **age** : 8.72%
7. **years_at_company** : 6.72%
