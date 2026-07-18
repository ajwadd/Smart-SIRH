package com.smarthr.mapper;

import com.smarthr.dto.LeaveDTO;
import com.smarthr.dto.LeaveSaveRequest;
import com.smarthr.entity.Leave;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public LeaveDTO toDTO(Leave leave) {
        if (leave == null) {
            return null;
        }

        LeaveDTO dto = new LeaveDTO();
        dto.setId(leave.getId());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setLeaveType(leave.getLeaveType());
        dto.setDaysCount(leave.getDaysCount());
        dto.setApprovedBy(leave.getApprovedBy());
        dto.setRejectionReason(leave.getRejectionReason());

        if (leave.getEmployee() != null) {
            dto.setEmployeeId(leave.getEmployee().getId());
            dto.setEmployeeFullName(leave.getEmployee().getFullName());
        }

        return dto;
    }

    public void updateEntity(LeaveSaveRequest request, Leave leave) {
        if (request == null || leave == null) {
            return;
        }

        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        
        if (request.getLeaveType() != null) {
            leave.setLeaveType(request.getLeaveType());
        }
    }
}
