package com.smarthr.mapper;

import com.smarthr.dto.AttendanceDTO;
import com.smarthr.dto.AttendanceSaveRequest;
import com.smarthr.entity.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public AttendanceDTO toDTO(Attendance attendance) {
        if (attendance == null) {
            return null;
        }

        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setCheckIn(attendance.getCheckIn());
        dto.setCheckOut(attendance.getCheckOut());
        dto.setBreakStart(attendance.getBreakStart());
        dto.setBreakEnd(attendance.getBreakEnd());
        dto.setStatus(attendance.getStatus());
        dto.setNotes(attendance.getNotes());

        if (attendance.getEmployee() != null) {
            dto.setEmployeeId(attendance.getEmployee().getId());
            dto.setEmployeeFullName(attendance.getEmployee().getFullName());
        }

        return dto;
    }

    public void updateEntity(AttendanceSaveRequest request, Attendance attendance) {
        if (request == null || attendance == null) {
            return;
        }

        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckIn(request.getCheckIn());
        attendance.setCheckOut(request.getCheckOut());
        attendance.setBreakStart(request.getBreakStart());
        attendance.setBreakEnd(request.getBreakEnd());
        
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
        
        attendance.setNotes(request.getNotes());
    }
}
