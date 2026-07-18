package com.smarthr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "performance_reviews", indexes = {
    @Index(name = "idx_review_employee", columnList = "employee_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview extends BaseEntity {

    @Column(name = "score", nullable = false)
    private Integer score; // 1-10

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements;

    @Column(name = "reviewer_name", length = 100)
    private String reviewerName;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;
}
