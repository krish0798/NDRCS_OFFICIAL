package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.RescueTeamLocationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RescueTeamLocationLogRepository extends JpaRepository<RescueTeamLocationLog, Long> {
    List<RescueTeamLocationLog> findByRescueTeamIdOrderByLoggedAtDesc(Long rescueTeamId);
}