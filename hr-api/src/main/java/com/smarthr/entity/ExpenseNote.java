package com.smarthr.entity;

import com.smarthr.enums.ExpenseStatus;
import com.smarthr.enums.ExpenseType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "expense_notes", indexes = {
    @Index(name = "idx_expense_employee", columnList = "employee_id"),
    @Index(name = "idx_expense_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseNote extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false, length = 20)
    private ExpenseType expenseType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency", length = 5)
    @Builder.Default
    private String currency = "MAD";

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.PENDING;

    @Column(name = "receipt_path")
    private String receiptPath;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
