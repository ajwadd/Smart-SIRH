package com.smarthr.service;

import com.smarthr.dto.EmployeeDTO;
import com.smarthr.dto.EmployeeSaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    Page<EmployeeDTO> getAllEmployees(String keyword, Pageable pageable);
    EmployeeDTO getEmployeeById(UUID id);
    EmployeeDTO createEmployee(EmployeeSaveRequest request);
    EmployeeDTO updateEmployee(UUID id, EmployeeSaveRequest request);
    void deleteEmployee(UUID id);
    EmployeeDTO archiveEmployee(UUID id);
    List<EmployeeDTO> importEmployeesFromExcel(MultipartFile file);
    ByteArrayInputStream exportEmployeesToExcel();
    ByteArrayInputStream exportEmployeesToPdf();
    List<?> getEmployeeRevisionHistory(UUID id);
}
