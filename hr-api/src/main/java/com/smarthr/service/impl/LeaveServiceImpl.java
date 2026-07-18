package com.smarthr.service.impl;

import com.smarthr.dto.LeaveBalanceDTO;
import com.smarthr.dto.LeaveDTO;
import com.smarthr.dto.LeaveSaveRequest;
import com.smarthr.entity.Employee;
import com.smarthr.entity.Leave;
import com.smarthr.enums.LeaveStatus;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.mapper.LeaveMapper;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.repository.LeaveRepository;
import com.smarthr.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;

    @Override
    @Transactional
    public LeaveDTO requestLeave(LeaveSaveRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + request.getEmployeeId()));

        long daysCount = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (daysCount <= 0) {
            throw new IllegalArgumentException(ErrorConstants.INVALID_LEAVE_DATES + ": La date de début doit être antérieure à la date de fin.");
        }

        // Vérification de non-chevauchement des congés
        List<Leave> overlapping = leaveRepository.findOverlappingForEmployee(employee.getId(), request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException(ErrorConstants.LEAVE_OVERLAP + ": Impossible de demander ce congé car il chevauche une autre demande existante.");
        }

        LeaveBalanceDTO balance = getLeaveBalance(employee.getId());
        if (balance.getTotalAvailable() < daysCount) {
            throw new IllegalArgumentException(ErrorConstants.INSUFFICIENT_LEAVE_BALANCE + ": Solde de congés insuffisant. Disponible: " + balance.getTotalAvailable() + " jours, demandé: " + daysCount + " jours.");
        }

        Leave leave = new Leave();
        leaveMapper.updateEntity(request, leave);
        leave.setEmployee(employee);
        leave.setDaysCount((int) daysCount);
        leave.setStatus(LeaveStatus.PENDING);

        Leave saved = leaveRepository.save(leave);
        return leaveMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public LeaveDTO approveLeave(UUID id, String approvedBy) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.LEAVE_NOT_FOUND, 
                        "Demande de congé introuvable avec l'ID: " + id));

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approvedBy);

        Leave saved = leaveRepository.save(leave);
        return leaveMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public LeaveDTO rejectLeave(UUID id, String approvedBy, String rejectionReason) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.LEAVE_NOT_FOUND, 
                        "Demande de congé introuvable avec l'ID: " + id));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(approvedBy);
        leave.setRejectionReason(rejectionReason);

        Leave saved = leaveRepository.save(leave);
        return leaveMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDTO> getLeavesByEmployeeId(UUID employeeId) {
        return leaveRepository.findByEmployeeId(employeeId).stream()
                .map(leaveMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDTO> getAllPendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING).stream()
                .map(leaveMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceDTO getLeaveBalance(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + employeeId));

        long monthsWorked = ChronoUnit.MONTHS.between(employee.getHireDate(), LocalDate.now());
        if (monthsWorked < 0) {
            monthsWorked = 0;
        }
        // Calcul basé sur les 26 jours de congés annuels stipulés au contrat (26 jours / 12 mois = ~2.16 jours acquis par mois)
        double totalAcquired = monthsWorked * (26.0 / 12.0);

        double totalConsumed = leaveRepository.findByEmployeeIdAndStatus(employeeId, LeaveStatus.APPROVED).stream()
                .mapToDouble(Leave::getDaysCount)
                .sum();

        double totalAvailable = totalAcquired - totalConsumed;

        return LeaveBalanceDTO.builder()
                .employeeId(employeeId)
                .employeeFullName(employee.getFullName())
                .totalAcquired(totalAcquired)
                .totalConsumed(totalConsumed)
                .totalAvailable(totalAvailable)
                .build();
    }
}
