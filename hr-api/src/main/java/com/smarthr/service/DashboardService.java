package com.smarthr.service;

import com.smarthr.dto.DashboardChartsDTO;
import com.smarthr.dto.DashboardSummaryDTO;

public interface DashboardService {
    
    /**
     * Calcule et renvoie les indicateurs clés résumés (KPIs) du jour.
     * 
     * @return DashboardSummaryDTO
     */
    DashboardSummaryDTO getSummaryStats();

    /**
     * Calcule et renvoie les répartitions de données adaptées pour les graphiques.
     * 
     * @return DashboardChartsDTO
     */
    DashboardChartsDTO getChartStats();
}
