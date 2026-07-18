package com.smarthr.mapper;

import com.smarthr.dto.DepartmentDTO;
import com.smarthr.dto.DepartmentSaveRequest;
import com.smarthr.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDTO toDTO(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setBudget(department.getBudget());

        if (department.getManager() != null) {
            dto.setManagerId(department.getManager().getId());
            dto.setManagerFullName(department.getManager().getFullName());
        }

        if (department.getParentDepartment() != null) {
            dto.setParentDepartmentId(department.getParentDepartment().getId());
            dto.setParentDepartmentName(department.getParentDepartment().getName());
        }

        return dto;
    }

    public void updateEntity(DepartmentSaveRequest request, Department department) {
        if (request == null || department == null) {
            return;
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setBudget(request.getBudget());
    }
}
