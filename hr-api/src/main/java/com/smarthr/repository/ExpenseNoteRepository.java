package com.smarthr.repository;

import com.smarthr.entity.ExpenseNote;
import com.smarthr.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseNoteRepository extends JpaRepository<ExpenseNote, UUID> {
    List<ExpenseNote> findByEmployeeId(UUID employeeId);
    List<ExpenseNote> findByStatus(ExpenseStatus status);
}
