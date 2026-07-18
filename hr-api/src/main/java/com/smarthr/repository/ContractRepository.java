package com.smarthr.repository;

import com.smarthr.entity.Contract;
import com.smarthr.enums.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findByEmployeeId(UUID employeeId);
    List<Contract> findByContractType(ContractType contractType);
}
