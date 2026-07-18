package com.smarthr.mapper;

import com.smarthr.dto.ContractDTO;
import com.smarthr.dto.ContractSaveRequest;
import com.smarthr.entity.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public ContractDTO toDTO(Contract contract) {
        if (contract == null) {
            return null;
        }

        ContractDTO dto = new ContractDTO();
        dto.setId(contract.getId());
        dto.setContractType(contract.getContractType());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setRenewable(contract.getRenewable());
        dto.setSalary(contract.getSalary());
        dto.setDescription(contract.getDescription());
        dto.setDocumentPath(contract.getDocumentPath());

        if (contract.getEmployee() != null) {
            dto.setEmployeeId(contract.getEmployee().getId());
            dto.setEmployeeFullName(contract.getEmployee().getFullName());
        }

        return dto;
    }

    public void updateEntity(ContractSaveRequest request, Contract contract) {
        if (request == null || contract == null) {
            return;
        }

        contract.setContractType(request.getContractType());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        
        if (request.getRenewable() != null) {
            contract.setRenewable(request.getRenewable());
        }
        
        contract.setSalary(request.getSalary());
        contract.setDescription(request.getDescription());
        contract.setDocumentPath(request.getDocumentPath());
    }
}
