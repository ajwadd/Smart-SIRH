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

        LeaveStatus status = LeaveStatus.PENDING;
        String validationNotes = null;
        String approvedBy = null;

        if (Boolean.TRUE.equals(request.getAutoValidate())) {
            StringBuilder notesBuilder = new StringBuilder();
            boolean eligible = true;

            // 1. Type check
            if (request.getLeaveType() != com.smarthr.enums.LeaveType.ANNUAL && request.getLeaveType() != com.smarthr.enums.LeaveType.COMPENSATORY) {
                eligible = false;
                notesBuilder.append("- Type de congé non éligible pour l'auto-validation (uniquement ANNUAL ou COMPENSATORY).\n");
            }

            // 2. Duration check
            if (daysCount > 3) {
                eligible = false;
                notesBuilder.append("- La durée dépasse la limite de 3 jours ouvrés (demandé: ").append(daysCount).append(" jours).\n");
            }

            // 3. Notice period check
            long noticeDays = ChronoUnit.DAYS.between(LocalDate.now(), request.getStartDate());
            if (noticeDays < 2) {
                eligible = false;
                notesBuilder.append("- Délai de prévenance insuffisant (minimum 48h avant le début du congé, demandé: ").append(noticeDays).append(" jours).\n");
            }

            // 4. Team Overlap check
            if (employee.getDepartment() != null) {
                UUID deptId = employee.getDepartment().getId();
                long totalDept = employeeRepository.countActiveByDepartmentId(deptId);
                if (totalDept > 0) {
                    long overlappingDept = leaveRepository.countActiveLeavesInDepartmentDuringPeriod(deptId, request.getStartDate(), request.getEndDate(), employee.getId());
                    double presenceRate = ((double) (totalDept - overlappingDept - 1) / totalDept) * 100.0;
                    if (presenceRate < 70.0) {
                        eligible = false;
                        notesBuilder.append("- Taux de présence du département insuffisant (requis: >=70%, avec votre congé il serait de ")
                                    .append(String.format("%.1f", presenceRate)).append("%).\n");
                    }
                }
            }

            // 5. Blackout period check
            boolean fallsInBlackout = false;
            LocalDate start = request.getStartDate();
            LocalDate end = request.getEndDate();
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (d.getMonthValue() == 12 && d.getDayOfMonth() >= 20) {
                    fallsInBlackout = true;
                    break;
                }
            }
            if (fallsInBlackout) {
                eligible = false;
                notesBuilder.append("- Demande de congé pendant la période bloquée de fin d'année (20 décembre au 31 décembre).\n");
            }

            if (eligible) {
                status = LeaveStatus.APPROVED;
                approvedBy = "AI_AGENT";
                validationNotes = "Approuvé automatiquement par l'agent IA de SmartHR. Tous les critères ont été respectés.";
            } else {
                status = LeaveStatus.PENDING;
                validationNotes = "Soumis à la validation du manager pour les raisons suivantes :\n" + notesBuilder.toString();
            }
        } else {
            validationNotes = "Demande standard soumise pour validation manager.";
        }

        leave.setStatus(status);
        leave.setApprovedBy(approvedBy);
        leave.setValidationNotes(validationNotes);

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

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(leaveMapper::toDTO)
                .collect(Collectors.toList());
    }
}
