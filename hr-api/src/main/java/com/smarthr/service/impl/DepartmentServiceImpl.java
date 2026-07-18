package com.smarthr.service.impl;

import com.smarthr.dto.DepartmentDTO;
import com.smarthr.dto.DepartmentSaveRequest;
import com.smarthr.entity.Department;
import com.smarthr.entity.Employee;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.mapper.DepartmentMapper;
import com.smarthr.repository.DepartmentRepository;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.DEPARTMENT_NOT_FOUND, 
                        "Département introuvable avec l'ID: " + id));
        return departmentMapper.toDTO(department);
    }

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentSaveRequest request) {
        Department department = new Department();
        departmentMapper.updateEntity(request, department);
        
        setRelations(department, request);

        Department saved = departmentRepository.save(department);
        return departmentMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public DepartmentDTO updateDepartment(UUID id, DepartmentSaveRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.DEPARTMENT_NOT_FOUND, 
                        "Département introuvable avec l'ID: " + id));

        departmentMapper.updateEntity(request, department);
        setRelations(department, request);

        Department updated = departmentRepository.save(department);
        return departmentMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteDepartment(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.DEPARTMENT_NOT_FOUND, 
                        "Département introuvable avec l'ID: " + id));
        departmentRepository.delete(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getSubDepartments(UUID parentId) {
        return departmentRepository.findByParentDepartmentId(parentId).stream()
                .map(departmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void setRelations(Department department, DepartmentSaveRequest request) {
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                            "Employé manager introuvable avec l'ID: " + request.getManagerId()));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.DEPARTMENT_NOT_FOUND, 
                            "Département parent introuvable avec l'ID: " + request.getParentDepartmentId()));
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }
    }
}
