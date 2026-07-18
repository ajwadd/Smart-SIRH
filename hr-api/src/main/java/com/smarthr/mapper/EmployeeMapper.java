package com.smarthr.mapper;

import com.smarthr.dto.EmployeeDTO;
import com.smarthr.dto.EmployeeSaveRequest;
import com.smarthr.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeDTO toDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setGender(employee.getGender());
        dto.setCin(employee.getCin());
        dto.setNationality(employee.getNationality());
        dto.setPhoto(employee.getPhoto());
        dto.setAddress(employee.getAddress());
        dto.setCity(employee.getCity());
        dto.setCountry(employee.getCountry());
        dto.setZip(employee.getZip());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        dto.setRib(employee.getRib());
        dto.setIban(employee.getIban());
        dto.setBank(employee.getBank());
        dto.setCnss(employee.getCnss());
        dto.setMaritalStatus(employee.getMaritalStatus());
        dto.setNumberOfChildren(employee.getNumberOfChildren());
        dto.setEmergencyContact(employee.getEmergencyContact());

        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
            dto.setDepartmentName(employee.getDepartment().getName());
        }

        if (employee.getPosition() != null) {
            dto.setPositionId(employee.getPosition().getId());
            dto.setPositionTitle(employee.getPosition().getTitle());
        }

        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
            dto.setManagerFullName(employee.getManager().getFullName());
        }

        return dto;
    }

    public void updateEntity(EmployeeSaveRequest request, Employee employee) {
        if (request == null || employee == null) {
            return;
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setCin(request.getCin());
        employee.setNationality(request.getNationality());
        employee.setPhoto(request.getPhoto());
        employee.setAddress(request.getAddress());
        employee.setCity(request.getCity());
        employee.setCountry(request.getCountry());
        employee.setZip(request.getZip());
        employee.setHireDate(request.getHireDate());
        
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        
        employee.setRib(request.getRib());
        employee.setIban(request.getIban());
        employee.setBank(request.getBank());
        employee.setCnss(request.getCnss());
        employee.setMaritalStatus(request.getMaritalStatus());
        
        if (request.getNumberOfChildren() != null) {
            employee.setNumberOfChildren(request.getNumberOfChildren());
        }
        
        employee.setEmergencyContact(request.getEmergencyContact());
    }
}
