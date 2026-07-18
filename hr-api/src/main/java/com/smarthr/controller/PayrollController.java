package com.smarthr.controller;

import com.smarthr.dto.PayrollDTO;
import com.smarthr.dto.PayrollSaveRequest;
import com.smarthr.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<PayrollDTO> generatePayroll(@Valid @RequestBody PayrollSaveRequest request) {
        PayrollDTO created = payrollService.generatePayroll(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<List<PayrollDTO>> generateBulkMonthlyPayrolls(
            @RequestParam("payMonth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate payMonth) {
        return ResponseEntity.ok(payrollService.generateBulkMonthlyPayrolls(payMonth));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    public ResponseEntity<PayrollDTO> getPayrollById(@PathVariable UUID id) {
        return ResponseEntity.ok(payrollService.getPayrollById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<PayrollDTO>> getPayrollsByEmployeeId(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(payrollService.getPayrollsByEmployeeId(employeeId));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InputStreamResource> exportPayrollToPdf(@PathVariable UUID id) {
        ByteArrayInputStream in = payrollService.exportPayrollToPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=payslip.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }
}
