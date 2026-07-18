package com.smarthr.dto;

import com.smarthr.enums.ContractType;
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
public class ContractDTO {
    private UUID id;
    private ContractType contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean renewable;
    private Double salary;
    private String description;
    private String documentPath;
    private UUID employeeId;
    private String employeeFullName;
}
