package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findAllByOrderByReportedAtDesc();
}