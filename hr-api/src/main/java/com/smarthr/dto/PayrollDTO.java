package com.smarthr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDTO {
    private UUID id;
    private LocalDate payMonth;
    private Double grossSalary;
    private Double netSalary;
    private Double baseSalary;
    private Double bonus;
    private Double overtimePay;
    private Double overtimeHours;
    private Double deductions;
    private Double cnssContribution;
    private Double mutualInsurance;
    private Double tax;
    private Double insurance;
    private String documentPath;
    private UUID employeeId;
    private String employeeFullName;
    private String employeeNumber;
}
