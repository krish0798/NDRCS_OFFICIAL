package com.ndrcs.backend.service;

import com.ndrcs.backend.dto.UpdateRescueLocationRequest;
import com.ndrcs.backend.model.AuthUser;
import com.ndrcs.backend.model.Incident;
import com.ndrcs.backend.model.RescueTeamLiveStatus;
import com.ndrcs.backend.model.RescueTeamLocationLog;
import com.ndrcs.backend.model.RescueTeamProfile;
import com.ndrcs.backend.repository.AuthUserRepository;
import com.ndrcs.backend.repository.RescueTeamLiveStatusRepository;
import com.ndrcs.backend.repository.RescueTeamLocationLogRepository;
import com.ndrcs.backend.repository.RescueTeamProfileRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RescueTeamService {

    private final AuthUserRepository authUserRepository;
    private final RescueTeamProfileRepository rescueTeamProfileRepository;
    private final RescueTeamLiveStatusRepository rescueTeamLiveStatusRepository;
    private final RescueTeamLocationLogRepository rescueTeamLocationLogRepository;

    public RescueTeamService(AuthUserRepository authUserRepository,
                             RescueTeamProfileRepository rescueTeamProfileRepository,
                             RescueTeamLiveStatusRepository rescueTeamLiveStatusRepository,
                             RescueTeamLocationLogRepository rescueTeamLocationLogRepository) {
        this.authUserRepository = authUserRepository;
        this.rescueTeamProfileRepository = rescueTeamProfileRepository;
        this.rescueTeamLiveStatusRepository = rescueTeamLiveStatusRepository;
        this.rescueTeamLocationLogRepository = rescueTeamLocationLogRepository;
    }

    public String updateLiveLocation(UpdateRescueLocationRequest request) {

        AuthUser authUser = authUserRepository.findByUserId(request.getUserId()).orElse(null);

        if (authUser == null) {
            return "Invalid rescue team user ID";
        }

        if (authUser.getRole() != AuthUser.Role.RESCUE_TEAM) {
            return "Only rescue team accounts can update location";
        }

        RescueTeamProfile rescueTeam = rescueTeamProfileRepository.findAll()
                .stream()
                .filter(r -> r.getAuthUser().getId().equals(authUser.getId()))
                .findFirst()
                .orElse(null);

        if (rescueTeam == null) {
            return "Rescue team profile not found";
        }

        RescueTeamLiveStatus liveStatus = rescueTeamLiveStatusRepository
                .findByRescueTeamId(rescueTeam.getId())
                .orElseGet(() -> {
                    RescueTeamLiveStatus newStatus = new RescueTeamLiveStatus();
                    newStatus.setRescueTeam(rescueTeam);
                    newStatus.setLatitude(0.0);
                    newStatus.setLongitude(0.0);
                    return newStatus;
                });

        liveStatus.setLatitude(request.getLatitude());
        liveStatus.setLongitude(request.getLongitude());
        liveStatus.setLocationName(request.getLocationName());
        liveStatus.setStatusMessage(request.getStatusMessage());
        liveStatus.setUpdatedAt(LocalDateTime.now());

        if (request.getUrgencyLevel() != null) {
            try {
                liveStatus.setUrgencyLevel(
                        Incident.UrgencyLevel.valueOf(request.getUrgencyLevel().toUpperCase())
                );
            } catch (IllegalArgumentException ignored) {
            }
        }

        RescueTeamLiveStatus savedLiveStatus = rescueTeamLiveStatusRepository.save(liveStatus);

        RescueTeamLocationLog log = new RescueTeamLocationLog();
        log.setRescueTeam(rescueTeam);
        log.setIncident(savedLiveStatus.getAssignedIncident());
        log.setLatitude(request.getLatitude());
        log.setLongitude(request.getLongitude());
        log.setLocationName(request.getLocationName());

        rescueTeamLocationLogRepository.save(log);

        return "Live location updated successfully";
    }
}