package com.ndrcs.backend.controller;

import com.ndrcs.backend.model.Incident;
import com.ndrcs.backend.model.IncidentAssignment;
import com.ndrcs.backend.model.RescueTeamLiveStatus;
import com.ndrcs.backend.repository.IncidentAssignmentRepository;
import com.ndrcs.backend.repository.IncidentRepository;
import com.ndrcs.backend.repository.RescueTeamLiveStatusRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final IncidentRepository incidentRepository;
    private final IncidentAssignmentRepository incidentAssignmentRepository;
    private final RescueTeamLiveStatusRepository rescueTeamLiveStatusRepository;

    public DashboardController(IncidentRepository incidentRepository,
                               IncidentAssignmentRepository incidentAssignmentRepository,
                               RescueTeamLiveStatusRepository rescueTeamLiveStatusRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentAssignmentRepository = incidentAssignmentRepository;
        this.rescueTeamLiveStatusRepository = rescueTeamLiveStatusRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        List<Incident> incidents = incidentRepository.findAll();
        List<IncidentAssignment> assignments = incidentAssignmentRepository.findAll();
        List<RescueTeamLiveStatus> liveStatuses = rescueTeamLiveStatusRepository.findAll();

        long reported = incidents.stream().filter(i -> i.getStatus() == Incident.IncidentStatus.REPORTED).count();
        long assigned = incidents.stream().filter(i -> i.getStatus() == Incident.IncidentStatus.ASSIGNED).count();
        long inProgress = incidents.stream().filter(i -> i.getStatus() == Incident.IncidentStatus.IN_PROGRESS).count();
        long resolved = incidents.stream().filter(i -> i.getStatus() == Incident.IncidentStatus.RESOLVED).count();

        long activeAssignments = assignments.stream()
                .filter(a -> a.getAssignmentStatus() == IncidentAssignment.AssignmentStatus.ASSIGNED ||
                             a.getAssignmentStatus() == IncidentAssignment.AssignmentStatus.ACCEPTED ||
                             a.getAssignmentStatus() == IncidentAssignment.AssignmentStatus.REACHED)
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("totalIncidents", incidents.size());
        response.put("reportedIncidents", reported);
        response.put("assignedIncidents", assigned);
        response.put("inProgressIncidents", inProgress);
        response.put("resolvedIncidents", resolved);
        response.put("totalAssignments", assignments.size());
        response.put("activeAssignments", activeAssignments);
        response.put("liveTrackedRescueTeams", liveStatuses.size());

        return response;
    }
}