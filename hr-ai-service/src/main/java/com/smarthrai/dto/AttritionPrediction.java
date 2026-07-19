package com.smarthrai.dto;

import java.util.List;

public class AttritionPrediction {
    private String employeeId;
    private String employeeFullName;
    private String riskLevel;       // BAS, MOYEN, ELEVE
    private double probability;     // Valeur de probabilité entre 0.0 et 1.0
    private List<String> influencingFactors;
    
    // Vraies caractéristiques calculées pour l'employé
    private int age;
    private double monthlyIncome;
    private int yearsAtCompany;
    private int jobSatisfaction;
    private int workLifeBalance;
    private int overtime;
    private int numPromotions;

    public AttritionPrediction() {}

    public AttritionPrediction(String employeeId, String employeeFullName, String riskLevel, double probability, List<String> influencingFactors) {
        this.employeeId = employeeId;
        this.employeeFullName = employeeFullName;
        this.riskLevel = riskLevel;
        this.probability = probability;
        this.influencingFactors = influencingFactors;
    }

    public AttritionPrediction(String employeeId, String employeeFullName, String riskLevel, double probability, List<String> influencingFactors,
                               int age, double monthlyIncome, int yearsAtCompany, int jobSatisfaction, int workLifeBalance, int overtime, int numPromotions) {
        this.employeeId = employeeId;
        this.employeeFullName = employeeFullName;
        this.riskLevel = riskLevel;
        this.probability = probability;
        this.influencingFactors = influencingFactors;
        this.age = age;
        this.monthlyIncome = monthlyIncome;
        this.yearsAtCompany = yearsAtCompany;
        this.jobSatisfaction = jobSatisfaction;
        this.workLifeBalance = workLifeBalance;
        this.overtime = overtime;
        this.numPromotions = numPromotions;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeFullName() {
        return employeeFullName;
    }

    public void setEmployeeFullName(String employeeFullName) {
        this.employeeFullName = employeeFullName;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public List<String> getInfluencingFactors() {
        return influencingFactors;
    }

    public void setInfluencingFactors(List<String> influencingFactors) {
        this.influencingFactors = influencingFactors;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public int getYearsAtCompany() {
        return yearsAtCompany;
    }

    public void setYearsAtCompany(int yearsAtCompany) {
        this.yearsAtCompany = yearsAtCompany;
    }

    public int getJobSatisfaction() {
        return jobSatisfaction;
    }

    public void setJobSatisfaction(int jobSatisfaction) {
        this.jobSatisfaction = jobSatisfaction;
    }

    public int getWorkLifeBalance() {
        return workLifeBalance;
    }

    public void setWorkLifeBalance(int workLifeBalance) {
        this.workLifeBalance = workLifeBalance;
    }

    public int getOvertime() {
        return overtime;
    }

    public void setOvertime(int overtime) {
        this.overtime = overtime;
    }

    public int getNumPromotions() {
        return numPromotions;
    }

    public void setNumPromotions(int numPromotions) {
        this.numPromotions = numPromotions;
    }
}
