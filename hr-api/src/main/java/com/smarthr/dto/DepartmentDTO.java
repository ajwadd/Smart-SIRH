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
public class DepartmentDTO {
    private UUID id;
    private String name;
    private String description;
    private Double budget;
    private UUID managerId;
    private String managerFullName;
    private UUID parentDepartmentId;
    private String parentDepartmentName;
}
