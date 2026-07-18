package com.smarthr.service;

import com.smarthr.dto.ContractDTO;
import com.smarthr.dto.ContractSaveRequest;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    List<ContractDTO> getAllContracts();
    ContractDTO getContractById(UUID id);
    ContractDTO createContract(ContractSaveRequest request);
    ContractDTO updateContract(UUID id, ContractSaveRequest request);
    void deleteContract(UUID id);
    List<ContractDTO> getContractsByEmployeeId(UUID employeeId);
}
