package com.smarthr.controller;

import com.smarthr.dto.LeaveBalanceDTO;
import com.smarthr.dto.LeaveDTO;
import com.smarthr.dto.LeaveSaveRequest;
import com.smarthr.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<LeaveDTO> requestLeave(@Valid @RequestBody LeaveSaveRequest request) {
        LeaveDTO created = leaveService.requestLeave(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    public ResponseEntity<LeaveDTO> approveLeave(@PathVariable UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(leaveService.approveLeave(id, username));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    public ResponseEntity<LeaveDTO> rejectLeave(
            @PathVariable UUID id,
            @RequestParam("reason") String reason) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(leaveService.rejectLeave(id, username, reason));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<LeaveDTO>> getLeavesByEmployeeId(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployeeId(employeeId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    public ResponseEntity<List<LeaveDTO>> getAllPendingLeaves() {
        return ResponseEntity.ok(leaveService.getAllPendingLeaves());
    }

    @GetMapping("/employee/{employeeId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<LeaveBalanceDTO> getLeaveBalance(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId));
    }
}
