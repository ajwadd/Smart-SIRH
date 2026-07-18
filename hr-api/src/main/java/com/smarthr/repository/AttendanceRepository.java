package com.smarthr.repository;

import com.smarthr.entity.Attendance;
import com.smarthr.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByEmployeeId(UUID employeeId);
    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(UUID employeeId, LocalDate start, LocalDate end);
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(UUID employeeId, LocalDate date);
    List<Attendance> findByAttendanceDate(LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.status = :status AND a.attendanceDate = :date")
    long countByStatusAndDate(@Param("status") AttendanceStatus status, @Param("date") LocalDate date);
}
