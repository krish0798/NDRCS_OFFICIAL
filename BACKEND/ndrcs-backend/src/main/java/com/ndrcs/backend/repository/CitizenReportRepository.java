package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.CitizenReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitizenReportRepository extends JpaRepository<CitizenReport, Long> {
    List<CitizenReport> findByCitizenId(Long citizenId);
}