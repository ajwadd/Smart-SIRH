package com.smarthr.entity;

import com.smarthr.enums.EmployeeStatus;
import com.smarthr.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_number", columnList = "employee_number"),
    @Index(name = "idx_employee_email", columnList = "email"),
    @Index(name = "idx_employee_cin", columnList = "cin"),
    @Index(name = "idx_employee_department", columnList = "department_id"),
    @Index(name = "idx_employee_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Employee extends BaseEntity {

    // ========================
    // INFORMATIONS PERSONNELLES
    // ========================

    @Column(name = "employee_number", nullable = false, unique = true, length = 20)
    private String employeeNumber;

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Email
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "cin", unique = true, length = 20)
    private String cin;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "photo")
    private String photo;

    // ========================
    // ADRESSE
    // ========================

    @Column(name = "address")
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "zip", length = 10)
    private String zip;

    // ========================
    // INFORMATIONS PROFESSIONNELLES
    // ========================

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    // ========================
    // INFORMATIONS BANCAIRES
    // ========================

    @Column(name = "rib", length = 30)
    private String rib;

    @Column(name = "iban", length = 34)
    private String iban;

    @Column(name = "bank", length = 100)
    private String bank;

    @Column(name = "cnss", length = 20)
    private String cnss;

    // ========================
    // SITUATION FAMILIALE
    // ========================

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "number_of_children")
    @Builder.Default
    private Integer numberOfChildren = 0;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @NotAudited
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    @NotAudited
    private Position position;

    /** Manager direct (auto-référence) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @NotAudited
    private Employee manager;

    /** Subordonnés */
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @Builder.Default
    @NotAudited
    private List<Employee> subordinates = new ArrayList<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @NotAudited
    private List<Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @NotAudited
    private List<Payroll> payrolls = new ArrayList<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @NotAudited
    private List<Attendance> attendances = new ArrayList<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @NotAudited
    private List<Leave> leaves = new ArrayList<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @NotAudited
    private List<PerformanceReview> performanceReviews = new ArrayList<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @NotAudited
    private List<Document> documents = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "employee_trainings",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "training_id")
    )
    @Builder.Default
    @NotAudited
    private List<Training> trainings = new ArrayList<>();

    // ========================
    // MÉTHODES UTILITAIRES
    // ========================

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
