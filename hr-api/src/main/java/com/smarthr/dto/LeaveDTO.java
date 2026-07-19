package com.smarthr.dto;

import com.smarthr.enums.LeaveStatus;
import com.smarthr.enums.LeaveType;
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
public class LeaveDTO {
    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private LeaveType leaveType;
    private Integer daysCount;
    private String approvedBy;
    private String rejectionReason;
    private UUID employeeId;
    private String employeeFullName;
    private String validationNotes;
}
