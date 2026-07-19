package com.smarthrai.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public interface HrEmployeeService {
    String searchEmployeeDocuments(String query, String employeeId);
    com.smarthrai.dto.AttritionPrediction predictEmployeeChurn(String employeeId);
    com.smarthrai.dto.ExpenseFraudPrediction detectExpenseFraud(double amount, String category, String dayOfWeek);
    JsonNode searchEmployees(String keyword);
    JsonNode getEmployeeLeaveBalance(String employeeId);
    JsonNode getEmployeeLeaves(String employeeId);
    JsonNode getEmployeeProfile(String employeeId);
    JsonNode getDashboardStats();
    JsonNode submitLeaveRequest(String startDate, String endDate, String reason, String leaveType, String employeeId);
}
