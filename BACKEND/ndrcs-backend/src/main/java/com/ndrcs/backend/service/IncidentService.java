package com.ndrcs.backend.service;

import com.ndrcs.backend.dto.ReportIncidentRequest;
import com.ndrcs.backend.model.AuthUser;
import com.ndrcs.backend.model.CitizenProfile;
import com.ndrcs.backend.model.CitizenReport;
import com.ndrcs.backend.model.Incident;
import com.ndrcs.backend.repository.AuthUserRepository;
import com.ndrcs.backend.repository.CitizenProfileRepository;
import com.ndrcs.backend.repository.CitizenReportRepository;
import com.ndrcs.backend.repository.IncidentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IncidentService {

    private final AuthUserRepository authUserRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final IncidentRepository incidentRepository;
    private final CitizenReportRepository citizenReportRepository;
    private final PasswordEncoder passwordEncoder;

    public IncidentService(AuthUserRepository authUserRepository,
                           CitizenProfileRepository citizenProfileRepository,
                           IncidentRepository incidentRepository,
                           CitizenReportRepository citizenReportRepository,
                           PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.citizenProfileRepository = citizenProfileRepository;
        this.incidentRepository = incidentRepository;
        this.citizenReportRepository = citizenReportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String reportIncident(ReportIncidentRequest request) {

        Optional<AuthUser> authUserOptional = authUserRepository.findByUserId(request.getUserId());

        if (authUserOptional.isEmpty()) {
            return "Invalid user ID";
        }

        AuthUser authUser = authUserOptional.get();

        if (authUser.getRole() != AuthUser.Role.CITIZEN) {
            return "Only citizen users can report disasters";
        }

        if (!authUser.isEmailVerified()) {
            return "Email not verified";
        }

        if (!passwordEncoder.matches(request.getPassword(), authUser.getPasswordHash())) {
            return "Invalid password";
        }

        CitizenProfile citizen = citizenProfileRepository.findAll()
                .stream()
                .filter(c -> c.getAuthUser().getId().equals(authUser.getId()))
                .findFirst()
                .orElse(null);

        if (citizen == null) {
            return "Citizen profile not found";
        }

        Incident incident = new Incident();
        incident.setCitizen(citizen);
        incident.setIncidentType(request.getIncidentType());
        incident.setDescription(request.getDescription());
        incident.setLatitude(request.getLatitude());
        incident.setLongitude(request.getLongitude());
        incident.setLocationName(request.getLocationName());

        if (request.getUrgencyLevel() != null) {
            try {
                incident.setUrgencyLevel(
                        Incident.UrgencyLevel.valueOf(request.getUrgencyLevel().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                incident.setUrgencyLevel(Incident.UrgencyLevel.NORMAL);
            }
        }

        Incident savedIncident = incidentRepository.save(incident);

        CitizenReport citizenReport = new CitizenReport();
        citizenReport.setCitizen(citizen);
        citizenReport.setIncident(savedIncident);

        citizenReportRepository.save(citizenReport);

        return "Incident reported successfully";
    }
}