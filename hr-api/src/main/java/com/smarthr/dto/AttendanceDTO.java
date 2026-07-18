package com.smarthr.dto;

import com.smarthr.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private UUID id;
    private LocalDate attendanceDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private LocalTime breakStart;
    private LocalTime breakEnd;
    private AttendanceStatus status;
    private String notes;
    private UUID employeeId;
    private String employeeFullName;
}
