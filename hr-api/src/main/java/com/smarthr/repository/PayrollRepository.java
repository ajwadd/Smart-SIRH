package com.smarthr.repository;

import com.smarthr.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {
    List<Payroll> findByEmployeeId(UUID employeeId);
    List<Payroll> findByPayMonth(LocalDate payMonth);
    java.util.Optional<Payroll> findByEmployeeIdAndPayMonth(UUID employeeId, LocalDate payMonth);
    List<Payroll> findByEmployeeIdAndPayMonthBetween(UUID employeeId, LocalDate start, LocalDate end);

    @Query("SELECT AVG(p.netSalary) FROM Payroll p WHERE p.payMonth = :month")
    Double findAverageNetSalaryByMonth(@Param("month") LocalDate month);

    @Query("SELECT SUM(p.grossSalary) FROM Payroll p WHERE p.payMonth = :month")
    Double findTotalGrossSalaryByMonth(@Param("month") LocalDate month);
}
