package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.RescueTeamProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RescueTeamProfileRepository extends JpaRepository<RescueTeamProfile, Long> {
    Optional<RescueTeamProfile> findByTeamCode(String teamCode);
    Optional<RescueTeamProfile> findByOfficialEmail(String officialEmail);
}