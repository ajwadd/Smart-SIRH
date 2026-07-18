package com.smarthr.dto;

import com.smarthr.enums.EmployeeStatus;
import com.smarthr.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeSaveRequest {

    private String employeeNumber;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String cin;
    private String nationality;
    private String photo;
    private String address;
    private String city;
    private String country;
    private String zip;

    @NotNull(message = "La date d'embauche est obligatoire")
    private LocalDate hireDate;

    private EmployeeStatus status;
    private String rib;
    private String iban;
    private String bank;
    private String cnss;
    private String maritalStatus;
    private Integer numberOfChildren;
    private String emergencyContact;
    private UUID departmentId;
    private UUID positionId;
    private UUID managerId;
}
