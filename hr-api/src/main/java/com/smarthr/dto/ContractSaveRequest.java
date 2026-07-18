package com.smarthr.dto;

import com.smarthr.enums.ContractType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ContractSaveRequest {

    @NotNull(message = "Le type de contrat est obligatoire")
    private ContractType contractType;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;
    private Boolean renewable;
    private Double salary;
    private String description;
    private String documentPath;

    @NotNull(message = "L'employé associé est obligatoire")
    private UUID employeeId;
}
