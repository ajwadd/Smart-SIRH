package com.smarthrai.service.impl;

import com.smarthrai.dto.AttritionPrediction;
import com.smarthrai.dto.ExpenseFraudPrediction;
import com.smarthrai.service.MlPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlPredictionServiceImpl implements MlPredictionService {

    private final RestTemplate restTemplate;

    @Value("${ml.service.url}")
    private String mlBaseUrl;

    @Override
    public AttritionPrediction predictAttrition(
            String employeeId,
            String employeeFullName,
            int age,
            double monthlyIncome,
            int yearsAtCompany,
            int jobSatisfaction,
            int workLifeBalance,
            int overtime,
            int numPromotions
    ) {
        log.info("Appel de la prédiction d'attrition pour {} (id: {}) via le service ML", employeeFullName, employeeId);
        try {
            String url = mlBaseUrl + "/predict";
            Map<String, Object> requestPayload = Map.of(
                    "age", age,
                    "monthly_income", monthlyIncome,
                    "years_at_company", yearsAtCompany,
                    "job_satisfaction", jobSatisfaction,
                    "work_life_balance", workLifeBalance,
                    "overtime", overtime,
                    "years_since_last_promotion", numPromotions,
                    "num_promotions", numPromotions
            );

            log.debug("Payload envoyé à {}: {}", url, requestPayload);
            Map<?, ?> response = restTemplate.postForObject(url, requestPayload, Map.class);
            if (response == null) {
                throw new RuntimeException("Réponse vide du service ML d'attrition");
            }

            int churnRisk = response.containsKey("churn_risk") ? ((Number) response.get("churn_risk")).intValue() : 0;
            double probability = response.containsKey("probability") ? ((Number) response.get("probability")).doubleValue() : 0.0;
            String riskLevel = response.containsKey("risk_level") ? (String) response.get("risk_level") : "INCONNU";
            List<String> factors = response.containsKey("factors") ? (List<String>) response.get("factors") : List.of();

            return new AttritionPrediction(employeeId, employeeFullName, riskLevel, probability, factors,
                    age, monthlyIncome, yearsAtCompany, jobSatisfaction, workLifeBalance, overtime, numPromotions);
        } catch (Exception e) {
            log.error("Erreur lors de l'appel au service ML d'attrition : {}", e.getMessage(), e);
            return new AttritionPrediction(
                    employeeId,
                    employeeFullName,
                    "ERREUR",
                    0.0,
                    List.of("Impossible de contacter le service ML: " + e.getMessage()),
                    age, monthlyIncome, yearsAtCompany, jobSatisfaction, workLifeBalance, overtime, numPromotions
            );
        }
    }

    @Override
    public ExpenseFraudPrediction detectExpenseFraud(double amount, String category, String dayOfWeek) {
        log.info("Appel de la détection de fraude de note de frais via le service ML (Montant: {}, Catégorie: {}, Jour: {})", 
                amount, category, dayOfWeek);
        try {
            String url = mlBaseUrl + "/predict/anomaly";
            Map<String, Object> requestPayload = Map.of(
                    "amount", amount,
                    "category", category,
                    "day_of_week", dayOfWeek
            );

            log.debug("Payload envoyé à {}: {}", url, requestPayload);
            Map<?, ?> response = restTemplate.postForObject(url, requestPayload, Map.class);
            if (response == null) {
                throw new RuntimeException("Réponse vide du service ML de détection de fraude");
            }

            boolean isAnomaly = response.containsKey("is_anomaly") && (Boolean) response.get("is_anomaly");
            double anomalyScore = response.containsKey("anomaly_score") ? ((Number) response.get("anomaly_score")).doubleValue() : 0.0;
            String riskLevel = response.containsKey("risk_level") ? (String) response.get("risk_level") : "INCONNU";
            List<String> reasons = response.containsKey("reasons") ? (List<String>) response.get("reasons") : List.of();

            return new ExpenseFraudPrediction(amount, category, dayOfWeek, isAnomaly, anomalyScore, riskLevel, reasons);
        } catch (Exception e) {
            log.error("Erreur lors de l'appel au service ML de détection de fraude : {}", e.getMessage(), e);
            return new ExpenseFraudPrediction(
                    amount,
                    category,
                    dayOfWeek,
                    true,
                    1.0,
                    "CRITIQUE",
                    List.of("Erreur de connexion avec le service de détection de fraude ML : " + e.getMessage())
            );
        }
    }
}
