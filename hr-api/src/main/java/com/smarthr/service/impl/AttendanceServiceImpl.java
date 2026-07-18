package com.smarthr.service.impl;

import com.smarthr.dto.AttendanceDTO;
import com.smarthr.dto.AttendanceSaveRequest;
import com.smarthr.entity.Attendance;
import com.smarthr.entity.Employee;
import com.smarthr.enums.AttendanceStatus;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.mapper.AttendanceMapper;
import com.smarthr.repository.AttendanceRepository;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public AttendanceDTO checkIn(UUID employeeId, String notes) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + employeeId));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);
        Attendance attendance;

        if (existingOpt.isPresent()) {
            attendance = existingOpt.get();
            if (attendance.getCheckIn() == null) {
                attendance.setCheckIn(now);
            }
        } else {
            AttendanceStatus status = now.isAfter(LocalTime.of(9, 0)) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
            
            attendance = Attendance.builder()
                    .employee(employee)
                    .attendanceDate(today)
                    .checkIn(now)
                    .status(status)
                    .notes(notes)
                    .build();
        }

        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public AttendanceDTO checkOut(UUID employeeId, String notes) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.ATTENDANCE_NOT_FOUND, 
                        "Aucun pointage d'entrée trouvé pour aujourd'hui. Veuillez d'abord pointer l'entrée."));

        attendance.setCheckOut(now);
        if (notes != null) {
            attendance.setNotes(notes);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public AttendanceDTO startBreak(UUID employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.ATTENDANCE_NOT_FOUND, 
                        "Aucun pointage trouvé pour aujourd'hui."));

        attendance.setBreakStart(LocalTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public AttendanceDTO endBreak(UUID employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.ATTENDANCE_NOT_FOUND, 
                        "Aucun pointage trouvé pour aujourd'hui."));

        attendance.setBreakEnd(LocalTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendancesByEmployeeId(UUID employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream()
                .map(attendanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendancesByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date).stream()
                .map(attendanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceDTO updateAttendance(UUID id, AttendanceSaveRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.ATTENDANCE_NOT_FOUND, 
                        "Pointage introuvable avec l'ID: " + id));

        attendanceMapper.updateEntity(request, attendance);
        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toDTO(saved);
    }
}
