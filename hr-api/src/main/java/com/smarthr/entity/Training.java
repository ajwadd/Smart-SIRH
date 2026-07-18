package com.smarthr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Training extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration")
    private Integer duration; // en heures

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "trainer", length = 100)
    private String trainer;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "certificate_available", nullable = false)
    @Builder.Default
    private Boolean certificateAvailable = false;

    // ========================
    // RELATIONS
    // ========================

    @ManyToMany(mappedBy = "trainings", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employee> participants = new ArrayList<>();
}
