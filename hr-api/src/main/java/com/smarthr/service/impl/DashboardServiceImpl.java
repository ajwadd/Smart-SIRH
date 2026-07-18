package com.smarthr.service.impl;

import com.smarthr.dto.DashboardChartsDTO;
import com.smarthr.dto.DashboardSummaryDTO;
import com.smarthr.entity.Contract;
import com.smarthr.entity.Employee;
import com.smarthr.entity.Leave;
import com.smarthr.entity.Payroll;
import com.smarthr.enums.AttendanceStatus;
import com.smarthr.enums.EmployeeStatus;
import com.smarthr.enums.LeaveStatus;
import com.smarthr.repository.*;
import com.smarthr.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollRepository payrollRepository;
    private final AttendanceRepository attendanceRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummaryStats() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1).withDayOfMonth(1);

        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        long totalDepartments = departmentRepository.count();
        long totalPositions = positionRepository.count();

        // Congés
        long pendingLeaves = leaveRepository.findByStatus(LeaveStatus.PENDING).size();
        long activeLeavesToday = leaveRepository.countActiveLeavesOnDate(today);

        // Salaires (du mois dernier ou du mois en cours)
        LocalDate targetMonth = today.withDayOfMonth(1);
        Double totalGross = payrollRepository.findTotalGrossSalaryByMonth(targetMonth);
        Double avgNet = payrollRepository.findAverageNetSalaryByMonth(targetMonth);

        // Si aucun bulletin de paie généré pour le mois en cours, regarder le mois d'avant
        if (totalGross == null || totalGross == 0.0) {
            totalGross = payrollRepository.findTotalGrossSalaryByMonth(lastMonth);
            avgNet = payrollRepository.findAverageNetSalaryByMonth(lastMonth);
        }

        // Présences d'aujourd'hui
        long presentCount = attendanceRepository.countByStatusAndDate(AttendanceStatus.PRESENT, today);
        long remoteCount = attendanceRepository.countByStatusAndDate(AttendanceStatus.REMOTE, today);
        long lateCount = attendanceRepository.countByStatusAndDate(AttendanceStatus.LATE, today);

        long totalPresentsToday = presentCount + remoteCount + lateCount;
        double attendanceRate = 100.0;
        if (activeEmployees > 0) {
            attendanceRate = (totalPresentsToday / (double) activeEmployees) * 100.0;
            // Cap à 100%
            if (attendanceRate > 100.0) {
                attendanceRate = 100.0;
            }
        }

        return DashboardSummaryDTO.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .totalDepartments(totalDepartments)
                .totalPositions(totalPositions)
                .pendingLeaves(pendingLeaves)
                .activeLeavesToday(activeLeavesToday)
                .monthlyPayrollMass(totalGross != null ? totalGross : 0.0)
                .averageSalary(avgNet != null ? avgNet : 0.0)
                .todayAttendanceRate(Math.round(attendanceRate * 10.0) / 10.0) // 1 chiffre après la virgule
                .todayLates(lateCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardChartsDTO getChartStats() {
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(6).withDayOfMonth(1);

        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        List<Contract> contracts = contractRepository.findAll();
        List<Payroll> payrolls = payrollRepository.findAll();

        // 1. Répartition par département
        Map<String, Long> deptDist = activeEmployees.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(e -> e.getDepartment().getName(), Collectors.counting()));

        // 2. Répartition par contrat
        Map<String, Long> contractDist = contracts.stream()
                .collect(Collectors.groupingBy(c -> c.getContractType().name(), Collectors.counting()));

        // 3. Répartition par genre
        Map<String, Long> genderDist = activeEmployees.stream()
                .filter(e -> e.getGender() != null)
                .collect(Collectors.groupingBy(e -> e.getGender().name(), Collectors.counting()));

        // 4. Évolution de la masse salariale (6 derniers mois)
        Map<String, Double> evolution = payrolls.stream()
                .filter(p -> p.getPayMonth().isAfter(sixMonthsAgo.minusDays(1)))
                .collect(Collectors.groupingBy(
                        p -> p.getPayMonth().toString().substring(0, 7), // Format "yyyy-MM"
                        Collectors.summingDouble(Payroll::getGrossSalary)
                ));

        return DashboardChartsDTO.builder()
                .departmentDistribution(deptDist)
                .contractDistribution(contractDist)
                .genderDistribution(genderDist)
                .payrollEvolution(evolution)
                .build();
    }
}
