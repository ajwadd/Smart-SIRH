package com.smarthr.service;

import com.smarthr.dto.DepartmentDTO;
import com.smarthr.dto.DepartmentSaveRequest;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    List<DepartmentDTO> getAllDepartments();
    DepartmentDTO getDepartmentById(UUID id);
    DepartmentDTO createDepartment(DepartmentSaveRequest request);
    DepartmentDTO updateDepartment(UUID id, DepartmentSaveRequest request);
    void deleteDepartment(UUID id);
    List<DepartmentDTO> getSubDepartments(UUID parentId);
}
