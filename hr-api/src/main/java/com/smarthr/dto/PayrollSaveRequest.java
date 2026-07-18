package com.smarthr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class PayrollSaveRequest {

    @NotNull(message = "L'employé associé est obligatoire")
    private UUID employeeId;

    @NotNull(message = "Le mois de paie est obligatoire")
    private LocalDate payMonth;

    private Double bonus;
    private Double overtimeHours;
    private Double deductions;
}
