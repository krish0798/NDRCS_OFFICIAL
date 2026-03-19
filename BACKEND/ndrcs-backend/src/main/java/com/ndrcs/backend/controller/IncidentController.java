package com.ndrcs.backend.controller;

import com.ndrcs.backend.dto.ReportIncidentRequest;
import com.ndrcs.backend.model.Incident;
import com.ndrcs.backend.repository.IncidentRepository;
import com.ndrcs.backend.service.IncidentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "*")
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentRepository incidentRepository;

    public IncidentController(IncidentService incidentService,
                              IncidentRepository incidentRepository) {
        this.incidentService = incidentService;
        this.incidentRepository = incidentRepository;
    }

    @PostMapping("/report")
    public String reportIncident(@RequestBody ReportIncidentRequest request) {
        return incidentService.reportIncident(request);
    }

    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAllByOrderByReportedAtDesc();
    }
}