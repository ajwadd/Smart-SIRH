package com.smarthr.service;

import com.smarthr.dto.AttendanceDTO;
import com.smarthr.dto.AttendanceSaveRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    AttendanceDTO checkIn(UUID employeeId, String notes);
    AttendanceDTO checkOut(UUID employeeId, String notes);
    AttendanceDTO startBreak(UUID employeeId);
    AttendanceDTO endBreak(UUID employeeId);
    List<AttendanceDTO> getAttendancesByEmployeeId(UUID employeeId);
    List<AttendanceDTO> getAttendancesByDate(LocalDate date);
    AttendanceDTO updateAttendance(UUID id, AttendanceSaveRequest request);
}
