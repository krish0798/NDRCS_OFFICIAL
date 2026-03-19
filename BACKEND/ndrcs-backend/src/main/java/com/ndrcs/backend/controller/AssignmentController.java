package com.ndrcs.backend.controller;

import com.ndrcs.backend.dto.AssignIncidentRequest;
import com.ndrcs.backend.model.IncidentAssignment;
import com.ndrcs.backend.repository.IncidentAssignmentRepository;
import com.ndrcs.backend.service.AssignmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final IncidentAssignmentRepository incidentAssignmentRepository;

    public AssignmentController(AssignmentService assignmentService,
                                IncidentAssignmentRepository incidentAssignmentRepository) {
        this.assignmentService = assignmentService;
        this.incidentAssignmentRepository = incidentAssignmentRepository;
    }

    @PostMapping
    public String assignIncident(@RequestBody AssignIncidentRequest request) {
        return assignmentService.assignIncident(request);
    }

    @GetMapping
    public List<IncidentAssignment> getAllAssignments() {
        return incidentAssignmentRepository.findAll();
    }

    @PostMapping("/complete")
    public String completeAssignment(@RequestBody java.util.Map<String, String> request) {
        return assignmentService.completeAssignment(request.get("userId"));
    }
}