package com.smarthr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {
    private UUID employeeId;
    private String employeeFullName;
    private Double totalAcquired;
    private Double totalConsumed;
    private Double totalAvailable;
}
