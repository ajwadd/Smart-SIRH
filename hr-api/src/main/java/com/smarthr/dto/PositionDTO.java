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
public class PositionDTO {
    private UUID id;
    private String title;
    private String description;
    private String skills;
    private Double minSalary;
    private Double maxSalary;
}
