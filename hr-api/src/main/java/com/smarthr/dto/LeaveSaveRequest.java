package com.smarthr.dto;

import com.smarthr.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LeaveSaveRequest {

    @NotNull(message = "L'employé est obligatoire")
    private UUID employeeId;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;

    private String reason;
    private LeaveType leaveType;
    private Boolean autoValidate = false;
}
