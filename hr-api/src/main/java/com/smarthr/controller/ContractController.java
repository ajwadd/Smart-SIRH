package com.smarthr.controller;

import com.smarthr.dto.ContractDTO;
import com.smarthr.dto.ContractSaveRequest;
import com.smarthr.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    public ResponseEntity<List<ContractDTO>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    public ResponseEntity<ContractDTO> getContractById(@PathVariable UUID id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ContractDTO> createContract(@Valid @RequestBody ContractSaveRequest request) {
        ContractDTO created = contractService.createContract(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ContractDTO> updateContract(
            @PathVariable UUID id, @Valid @RequestBody ContractSaveRequest request) {
        return ResponseEntity.ok(contractService.updateContract(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContract(@PathVariable UUID id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<ContractDTO>> getContractsByEmployeeId(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(contractService.getContractsByEmployeeId(employeeId));
    }
}
