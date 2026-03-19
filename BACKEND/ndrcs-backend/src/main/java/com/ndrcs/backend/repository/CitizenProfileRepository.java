package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.CitizenProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CitizenProfileRepository extends JpaRepository<CitizenProfile, Long> {
    Optional<CitizenProfile> findByEmail(String email);
    Optional<CitizenProfile> findByPhoneNumber(String phoneNumber);
}