package com.smarthr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class DepartmentSaveRequest {

    @NotBlank(message = "Le nom du département est obligatoire")
    private String name;

    private String description;
    private Double budget;
    private UUID managerId;
    private UUID parentDepartmentId;
}
