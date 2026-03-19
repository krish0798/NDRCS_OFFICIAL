package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.RescueTeamLiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RescueTeamLiveStatusRepository extends JpaRepository<RescueTeamLiveStatus, Long> {
    Optional<RescueTeamLiveStatus> findByRescueTeamId(Long rescueTeamId);
}