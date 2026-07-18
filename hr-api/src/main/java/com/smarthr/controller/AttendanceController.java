package com.smarthr.controller;

import com.smarthr.dto.AttendanceDTO;
import com.smarthr.dto.AttendanceSaveRequest;
import com.smarthr.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AttendanceDTO> checkIn(
            @RequestParam("employeeId") UUID employeeId,
            @RequestParam(value = "notes", required = false) String notes) {
        return ResponseEntity.ok(attendanceService.checkIn(employeeId, notes));
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AttendanceDTO> checkOut(
            @RequestParam("employeeId") UUID employeeId,
            @RequestParam(value = "notes", required = false) String notes) {
        return ResponseEntity.ok(attendanceService.checkOut(employeeId, notes));
    }

    @PostMapping("/break-start")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AttendanceDTO> startBreak(@RequestParam("employeeId") UUID employeeId) {
        return ResponseEntity.ok(attendanceService.startBreak(employeeId));
    }

    @PostMapping("/break-end")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AttendanceDTO> endBreak(@RequestParam("employeeId") UUID employeeId) {
        return ResponseEntity.ok(attendanceService.endBreak(employeeId));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AttendanceDTO>> getAttendancesByEmployeeId(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendancesByEmployeeId(employeeId));
    }

    @GetMapping("/date")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    public ResponseEntity<List<AttendanceDTO>> getAttendancesByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendancesByDate(date));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<AttendanceDTO> updateAttendance(
            @PathVariable UUID id, @Valid @RequestBody AttendanceSaveRequest request) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request));
    }
}
