package com.smarthr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "payrolls", indexes = {
    @Index(name = "idx_payroll_employee", columnList = "employee_id"),
    @Index(name = "idx_payroll_month", columnList = "pay_month")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll extends BaseEntity {

    @Column(name = "pay_month", nullable = false)
    private LocalDate payMonth;

    @Column(name = "gross_salary", nullable = false)
    private Double grossSalary;

    @Column(name = "net_salary", nullable = false)
    private Double netSalary;

    @Column(name = "base_salary")
    private Double baseSalary;

    @Column(name = "bonus")
    @Builder.Default
    private Double bonus = 0.0;

    @Column(name = "overtime_pay")
    @Builder.Default
    private Double overtimePay = 0.0;

    @Column(name = "overtime_hours")
    @Builder.Default
    private Double overtimeHours = 0.0;

    @Column(name = "deductions")
    @Builder.Default
    private Double deductions = 0.0;

    @Column(name = "cnss_contribution")
    @Builder.Default
    private Double cnssContribution = 0.0;

    @Column(name = "mutual_insurance")
    @Builder.Default
    private Double mutualInsurance = 0.0;

    @Column(name = "tax")
    @Builder.Default
    private Double tax = 0.0;

    @Column(name = "insurance")
    @Builder.Default
    private Double insurance = 0.0;

    @Column(name = "document_path")
    private String documentPath;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
