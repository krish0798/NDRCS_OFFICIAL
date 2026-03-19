package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.ControlRoomProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControlRoomProfileRepository extends JpaRepository<ControlRoomProfile, Long> {
    Optional<ControlRoomProfile> findByEmployeeCode(String employeeCode);
    Optional<ControlRoomProfile> findByOfficialEmail(String officialEmail);
}