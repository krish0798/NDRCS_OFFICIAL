package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.IncidentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentAssignmentRepository extends JpaRepository<IncidentAssignment, Long> {
}