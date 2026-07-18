package com.smarthr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Email
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "cv_path")
    private String cvPath;

    @Column(name = "cover_letter_path")
    private String coverLetterPath;

    @Column(name = "status", length = 20)
    private String status; // APPLIED, INTERVIEW, OFFERED, HIRED, REJECTED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_offer_id")
    private JobOffer jobOffer;
}
