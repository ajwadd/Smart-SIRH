package com.smarthr.dto;

import com.smarthr.enums.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class AttendanceSaveRequest {
    private UUID employeeId;
    private LocalDate attendanceDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private LocalTime breakStart;
    private LocalTime breakEnd;
    private AttendanceStatus status;
    private String notes;
}
