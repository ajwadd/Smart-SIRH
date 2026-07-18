package com.smarthr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private Long totalEmployees;
    private Long activeEmployees;
    private Long totalDepartments;
    private Long totalPositions;
    private Long pendingLeaves;
    private Long activeLeavesToday;
    private Double monthlyPayrollMass;
    private Double averageSalary;
    private Double todayAttendanceRate;
    private Long todayLates;
}
