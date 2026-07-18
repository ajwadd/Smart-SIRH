package com.smarthr.mapper;

import com.smarthr.dto.PayrollDTO;
import com.smarthr.entity.Payroll;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {

    public PayrollDTO toDTO(Payroll payroll) {
        if (payroll == null) {
            return null;
        }

        PayrollDTO dto = new PayrollDTO();
        dto.setId(payroll.getId());
        dto.setPayMonth(payroll.getPayMonth());
        dto.setGrossSalary(payroll.getGrossSalary());
        dto.setNetSalary(payroll.getNetSalary());
        dto.setBaseSalary(payroll.getBaseSalary());
        dto.setBonus(payroll.getBonus());
        dto.setOvertimePay(payroll.getOvertimePay());
        dto.setOvertimeHours(payroll.getOvertimeHours());
        dto.setDeductions(payroll.getDeductions());
        dto.setCnssContribution(payroll.getCnssContribution());
        dto.setMutualInsurance(payroll.getMutualInsurance());
        dto.setTax(payroll.getTax());
        dto.setInsurance(payroll.getInsurance());
        dto.setDocumentPath(payroll.getDocumentPath());

        if (payroll.getEmployee() != null) {
            dto.setEmployeeId(payroll.getEmployee().getId());
            dto.setEmployeeFullName(payroll.getEmployee().getFullName());
            dto.setEmployeeNumber(payroll.getEmployee().getEmployeeNumber());
        }

        return dto;
    }
}
