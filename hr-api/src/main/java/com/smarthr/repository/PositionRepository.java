package com.smarthr.repository;

import com.smarthr.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
    Optional<Position> findByTitle(String title);
    boolean existsByTitle(String title);
}
