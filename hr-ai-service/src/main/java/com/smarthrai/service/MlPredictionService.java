package com.smarthrai.service;

import com.smarthrai.dto.AttritionPrediction;
import com.smarthrai.dto.ExpenseFraudPrediction;

public interface MlPredictionService {
    
    AttritionPrediction predictAttrition(
            String employeeId,
            String employeeFullName,
            int age,
            double monthlyIncome,
            int yearsAtCompany,
            int jobSatisfaction,
            int workLifeBalance,
            int overtime,
            int numPromotions
    );

    ExpenseFraudPrediction detectExpenseFraud(
            double amount,
            String category,
            String dayOfWeek
    );
}
