package com.smarthr.controller;

import com.smarthr.dto.DashboardChartsDTO;
import com.smarthr.dto.DashboardSummaryDTO;
import com.smarthr.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummaryStats() {
        return ResponseEntity.ok(dashboardService.getSummaryStats());
    }

    @GetMapping("/charts")
    public ResponseEntity<DashboardChartsDTO> getChartStats() {
        return ResponseEntity.ok(dashboardService.getChartStats());
    }
}
