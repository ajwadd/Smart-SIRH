package com.smarthr.repository;

import com.smarthr.entity.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, UUID> {
    List<JobOffer> findByActive(Boolean active);
    List<JobOffer> findByDepartmentId(UUID departmentId);
}
