package com.smarthr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOffer extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "salary_range", length = 50)
    private String salaryRange;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "contract_type", length = 20)
    private String contractType;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @OneToMany(mappedBy = "jobOffer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Candidate> candidates = new ArrayList<>();
}
