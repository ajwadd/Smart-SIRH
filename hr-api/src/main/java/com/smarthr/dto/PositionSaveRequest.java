package com.smarthr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PositionSaveRequest {

    @NotBlank(message = "Le titre du poste est obligatoire")
    private String title;

    private String description;
    private String skills;
    private Double minSalary;
    private Double maxSalary;
}
