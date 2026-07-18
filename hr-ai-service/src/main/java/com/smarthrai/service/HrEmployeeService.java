package com.smarthrai.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public interface HrEmployeeService {
    String searchEmployeeDocuments(String query, String employeeId);
    Map<String, Object> predictEmployeeChurn(String employeeId);
    JsonNode searchEmployees(String keyword);
    JsonNode getEmployeeLeaveBalance(String employeeId);
    JsonNode getEmployeeLeaves(String employeeId);
    JsonNode getEmployeeProfile(String employeeId);
    JsonNode getDashboardStats();
}
