package com.smarthr.repository;

import com.smarthr.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByName(String name);
    boolean existsByName(String name);
    List<Department> findByParentDepartmentIsNull();
    List<Department> findByParentDepartmentId(UUID parentId);
}
