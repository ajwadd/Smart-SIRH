package com.smarthr.dto;

import com.smarthr.enums.EmployeeStatus;
import com.smarthr.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private UUID id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
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
    private String departmentName;
    private UUID positionId;
    private String positionTitle;
    private UUID managerId;
    private String managerFullName;
}
