package com.smarthrai.dto;

import java.util.List;

public class ExpenseFraudPrediction {
    private double amount;
    private String category;
    private String dayOfWeek;
    private boolean isAnomaly;
    private double anomalyScore;
    private String riskLevel; // NORMAL, SUSPECT, CRITIQUE
    private List<String> reasons;

    public ExpenseFraudPrediction() {}

    public ExpenseFraudPrediction(double amount, String category, String dayOfWeek, boolean isAnomaly, double anomalyScore, String riskLevel, List<String> reasons) {
        this.amount = amount;
        this.category = category;
        this.dayOfWeek = dayOfWeek;
        this.isAnomaly = isAnomaly;
        this.anomalyScore = anomalyScore;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isAnomaly() {
        return isAnomaly;
    }

    public void setAnomaly(boolean anomaly) {
        isAnomaly = anomaly;
    }

    public double getAnomalyScore() {
        return anomalyScore;
    }

    public void setAnomalyScore(double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}
