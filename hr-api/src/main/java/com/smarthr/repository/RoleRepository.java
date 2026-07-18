package com.smarthr.repository;

import com.smarthr.entity.Role;
import com.smarthr.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleType name);
    boolean existsByName(RoleType name);
}
