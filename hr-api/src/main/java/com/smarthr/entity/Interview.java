package com.smarthr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview extends BaseEntity {

    @Column(name = "interview_date", nullable = false)
    private LocalDate interviewDate;

    @Column(name = "interview_time")
    private LocalTime interviewTime;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "type", length = 30)
    private String type; // PHONE, VIDEO, IN_PERSON

    @Column(name = "status", length = 20)
    private String status; // SCHEDULED, COMPLETED, CANCELLED

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "score")
    private Integer score;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id")
    private Employee interviewer;
}
