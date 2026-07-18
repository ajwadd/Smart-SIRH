package com.smarthr.service;

import com.smarthr.dto.PayrollDTO;
import com.smarthr.dto.PayrollSaveRequest;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PayrollService {
    PayrollDTO generatePayroll(PayrollSaveRequest request);
    List<PayrollDTO> generateBulkMonthlyPayrolls(LocalDate payMonth);
    PayrollDTO getPayrollById(UUID id);
    List<PayrollDTO> getPayrollsByEmployeeId(UUID employeeId);
    ByteArrayInputStream exportPayrollToPdf(UUID payrollId);
}
