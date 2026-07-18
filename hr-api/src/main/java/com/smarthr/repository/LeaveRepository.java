package com.smarthr.repository;

import com.smarthr.entity.Leave;
import com.smarthr.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, UUID> {
    List<Leave> findByEmployeeId(UUID employeeId);
    List<Leave> findByStatus(LeaveStatus status);
    List<Leave> findByEmployeeIdAndStatus(UUID employeeId, LeaveStatus status);

    @Query("SELECT l FROM Leave l WHERE l.startDate <= :end AND l.endDate >= :start")
    List<Leave> findOverlapping(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId AND l.status != 'REJECTED' AND l.startDate <= :end AND l.endDate >= :start")
    List<Leave> findOverlappingForEmployee(@Param("employeeId") UUID employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(l.daysCount), 0) FROM Leave l WHERE l.employee.id = :employeeId AND l.status = 'APPROVED' AND YEAR(l.startDate) = :year")
    int countApprovedDaysByEmployeeAndYear(@Param("employeeId") UUID employeeId, @Param("year") int year);

    @Query("SELECT COUNT(l) FROM Leave l WHERE l.status = 'APPROVED' AND :date >= l.startDate AND :date <= l.endDate")
    long countActiveLeavesOnDate(@Param("date") LocalDate date);
}
