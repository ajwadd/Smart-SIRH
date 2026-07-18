package com.smarthr.service.impl;

import com.smarthr.dto.ContractDTO;
import com.smarthr.dto.ContractSaveRequest;
import com.smarthr.entity.Contract;
import com.smarthr.entity.Employee;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.mapper.ContractMapper;
import com.smarthr.repository.ContractRepository;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractMapper contractMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getAllContracts() {
        return contractRepository.findAll().stream()
                .map(contractMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDTO getContractById(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.CONTRACT_NOT_FOUND, 
                        "Contrat introuvable avec l'ID: " + id));
        return contractMapper.toDTO(contract);
    }

    @Override
    @Transactional
    public ContractDTO createContract(ContractSaveRequest request) {
        Contract contract = new Contract();
        contractMapper.updateEntity(request, contract);

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + request.getEmployeeId()));
        contract.setEmployee(employee);

        Contract saved = contractRepository.save(contract);
        return contractMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ContractDTO updateContract(UUID id, ContractSaveRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.CONTRACT_NOT_FOUND, 
                        "Contrat introuvable avec l'ID: " + id));

        contractMapper.updateEntity(request, contract);

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + request.getEmployeeId()));
        contract.setEmployee(employee);

        Contract updated = contractRepository.save(contract);
        return contractMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteContract(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.CONTRACT_NOT_FOUND, 
                        "Contrat introuvable avec l'ID: " + id));
        contractRepository.delete(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getContractsByEmployeeId(UUID employeeId) {
        return contractRepository.findByEmployeeId(employeeId).stream()
                .map(contractMapper::toDTO)
                .collect(Collectors.toList());
    }
}
