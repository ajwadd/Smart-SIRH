package com.smarthr.entity;

import com.smarthr.enums.ContractType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 15)
    private ContractType contractType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "renewable", nullable = false)
    @Builder.Default
    private Boolean renewable = false;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "document_path")
    private String documentPath;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
