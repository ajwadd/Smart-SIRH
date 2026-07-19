package com.smarthr.service;

import com.smarthr.dto.LeaveBalanceDTO;
import com.smarthr.dto.LeaveDTO;
import com.smarthr.dto.LeaveSaveRequest;

import java.util.List;
import java.util.UUID;

public interface LeaveService {
    LeaveDTO requestLeave(LeaveSaveRequest request);
    LeaveDTO approveLeave(UUID id, String approvedBy);
    LeaveDTO rejectLeave(UUID id, String approvedBy, String rejectionReason);
    List<LeaveDTO> getLeavesByEmployeeId(UUID employeeId);
    List<LeaveDTO> getAllPendingLeaves();
    LeaveBalanceDTO getLeaveBalance(UUID employeeId);
    java.util.List<LeaveDTO> getAllLeaves();
}
