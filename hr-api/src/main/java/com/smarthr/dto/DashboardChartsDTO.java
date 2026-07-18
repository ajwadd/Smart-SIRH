package com.smarthr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardChartsDTO {
    private Map<String, Long> departmentDistribution;
    private Map<String, Long> contractDistribution;
    private Map<String, Long> genderDistribution;
    private Map<String, Double> payrollEvolution;
}
