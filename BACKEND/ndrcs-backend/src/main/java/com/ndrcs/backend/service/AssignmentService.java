package com.ndrcs.backend.service;

import com.ndrcs.backend.dto.AssignIncidentRequest;
import com.ndrcs.backend.model.AuthUser;
import com.ndrcs.backend.model.ControlRoomProfile;
import com.ndrcs.backend.model.Incident;
import com.ndrcs.backend.model.IncidentAssignment;
import com.ndrcs.backend.model.RescueTeamLiveStatus;
import com.ndrcs.backend.model.RescueTeamProfile;
import com.ndrcs.backend.repository.AuthUserRepository;
import com.ndrcs.backend.repository.ControlRoomProfileRepository;
import com.ndrcs.backend.repository.IncidentAssignmentRepository;
import com.ndrcs.backend.repository.IncidentRepository;
import com.ndrcs.backend.repository.RescueTeamLiveStatusRepository;
import com.ndrcs.backend.repository.RescueTeamProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AssignmentService {

    private final AuthUserRepository authUserRepository;
    private final ControlRoomProfileRepository controlRoomProfileRepository;
    private final IncidentRepository incidentRepository;
    private final RescueTeamProfileRepository rescueTeamProfileRepository;
    private final IncidentAssignmentRepository incidentAssignmentRepository;
    private final RescueTeamLiveStatusRepository rescueTeamLiveStatusRepository;
    private final PasswordEncoder passwordEncoder;

    public AssignmentService(AuthUserRepository authUserRepository,
                             ControlRoomProfileRepository controlRoomProfileRepository,
                             IncidentRepository incidentRepository,
                             RescueTeamProfileRepository rescueTeamProfileRepository,
                             IncidentAssignmentRepository incidentAssignmentRepository,
                             RescueTeamLiveStatusRepository rescueTeamLiveStatusRepository,
                             PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.controlRoomProfileRepository = controlRoomProfileRepository;
        this.incidentRepository = incidentRepository;
        this.rescueTeamProfileRepository = rescueTeamProfileRepository;
        this.incidentAssignmentRepository = incidentAssignmentRepository;
        this.rescueTeamLiveStatusRepository = rescueTeamLiveStatusRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String assignIncident(AssignIncidentRequest request) {

        AuthUser authUser = authUserRepository.findByUserId(request.getControlUserId()).orElse(null);

        if (authUser == null) {
            return "Invalid control room user ID";
        }

        if (authUser.getRole() != AuthUser.Role.CONTROL_ROOM) {
            return "Only control room members can assign incidents";
        }

        if (request.getControlPassword() != null && !request.getControlPassword().isEmpty()) {
            if (!passwordEncoder.matches(request.getControlPassword(), authUser.getPasswordHash())) {
                return "Invalid password";
            }
        }

        ControlRoomProfile controlRoom = controlRoomProfileRepository.findAll()
                .stream()
                .filter(c -> c.getAuthUser().getId().equals(authUser.getId()))
                .findFirst()
                .orElse(null);

        if (controlRoom == null) {
            return "Control room profile not found";
        }

        Incident incident = incidentRepository.findById(request.getIncidentId()).orElse(null);
        if (incident == null) {
            return "Incident not found";
        }

        RescueTeamProfile rescueTeam = rescueTeamProfileRepository.findById(request.getRescueTeamId()).orElse(null);
        if (rescueTeam == null) {
            return "Rescue team not found";
        }

        IncidentAssignment assignment = new IncidentAssignment();
        assignment.setIncident(incident);
        assignment.setRescueTeam(rescueTeam);
        assignment.setControlRoom(controlRoom);
        assignment.setAssignmentStatus(IncidentAssignment.AssignmentStatus.ASSIGNED);
        assignment.setUpdatedAt(LocalDateTime.now());

        incidentAssignmentRepository.save(assignment);

        incident.setStatus(Incident.IncidentStatus.ASSIGNED);
        incidentRepository.save(incident);

        rescueTeam.setCurrentStatus(RescueTeamProfile.TeamStatus.ASSIGNED);
        rescueTeamProfileRepository.save(rescueTeam);

        RescueTeamLiveStatus liveStatus = rescueTeamLiveStatusRepository
                .findByRescueTeamId(rescueTeam.getId())
                .orElseGet(() -> {
                    RescueTeamLiveStatus newStatus = new RescueTeamLiveStatus();
                    newStatus.setRescueTeam(rescueTeam);
                    newStatus.setLatitude(0.0);
                    newStatus.setLongitude(0.0);
                    return newStatus;
                });

        liveStatus.setAssignedIncident(incident);
        liveStatus.setUrgencyLevel(incident.getUrgencyLevel());
        liveStatus.setStatusMessage("Assigned to incident #" + incident.getId());
        liveStatus.setUpdatedAt(LocalDateTime.now());

        rescueTeamLiveStatusRepository.save(liveStatus);

        return "Incident assigned successfully";
    }

    public String completeAssignment(String rescueUserId) {
        AuthUser authUser = authUserRepository.findByUserId(rescueUserId).orElse(null);
        if (authUser == null || authUser.getRole() != AuthUser.Role.RESCUE_TEAM) {
            return "Invalid rescue operative ID";
        }

        RescueTeamProfile rescueTeam = rescueTeamProfileRepository.findAll()
                .stream()
                .filter(t -> t.getAuthUser().getId().equals(authUser.getId()))
                .findFirst()
                .orElse(null);

        if (rescueTeam == null) {
            return "Rescue team profile not found";
        }

        RescueTeamLiveStatus liveStatus = rescueTeamLiveStatusRepository.findByRescueTeamId(rescueTeam.getId()).orElse(null);
        if (liveStatus == null || liveStatus.getAssignedIncident() == null) {
            return "No active mission to complete";
        }

        Incident incident = liveStatus.getAssignedIncident();

        IncidentAssignment assignment = incidentAssignmentRepository.findAll()
                .stream()
                .filter(a -> a.getRescueTeam().getId().equals(rescueTeam.getId())
                          && a.getIncident().getId().equals(incident.getId())
                          && (a.getAssignmentStatus() == IncidentAssignment.AssignmentStatus.ASSIGNED ||
                              a.getAssignmentStatus() == IncidentAssignment.AssignmentStatus.ACCEPTED ||
                              a.getAssignmentStatus() == IncidentAssignment.AssignmentStatus.REACHED))
                .findFirst()
                .orElse(null);

        if (assignment != null) {
            assignment.setAssignmentStatus(IncidentAssignment.AssignmentStatus.COMPLETED);
            assignment.setUpdatedAt(LocalDateTime.now());
            incidentAssignmentRepository.save(assignment);
        }

        incident.setStatus(Incident.IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        incidentRepository.save(incident);

        rescueTeam.setCurrentStatus(RescueTeamProfile.TeamStatus.AVAILABLE);
        rescueTeamProfileRepository.save(rescueTeam);

        liveStatus.setAssignedIncident(null);
        liveStatus.setStatusMessage("Mission Completed");
        liveStatus.setUrgencyLevel(Incident.UrgencyLevel.NORMAL);
        liveStatus.setUpdatedAt(LocalDateTime.now());
        rescueTeamLiveStatusRepository.save(liveStatus);

        return "Mission successfully completed and logged.";
    }
}