package com.ndrcs.backend.controller;

import com.ndrcs.backend.model.CitizenProfile;
import com.ndrcs.backend.model.CitizenReport;
import com.ndrcs.backend.repository.CitizenProfileRepository;
import com.ndrcs.backend.repository.CitizenReportRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citizen")
@CrossOrigin(origins = "*")
public class CitizenController {

    private final CitizenProfileRepository citizenProfileRepository;
    private final CitizenReportRepository citizenReportRepository;

    public CitizenController(CitizenProfileRepository citizenProfileRepository,
                             CitizenReportRepository citizenReportRepository) {
        this.citizenProfileRepository = citizenProfileRepository;
        this.citizenReportRepository = citizenReportRepository;
    }

    @GetMapping("/{citizenId}/profile")
    public Map<String, Object> getCitizenProfile(@PathVariable Long citizenId) {
        CitizenProfile citizen = citizenProfileRepository.findById(citizenId).orElse(null);

        Map<String, Object> response = new HashMap<>();

        if (citizen == null) {
            response.put("message", "Citizen not found");
            return response;
        }

        List<CitizenReport> reports = citizenReportRepository.findByCitizenId(citizenId);

        response.put("citizenId", citizen.getId());
        response.put("userId", citizen.getAuthUser() != null ? citizen.getAuthUser().getUserId() : null);
        response.put("fullName", citizen.getFullName());
        response.put("email", citizen.getEmail());
        response.put("phoneNumber", citizen.getPhoneNumber());
        response.put("address", citizen.getAddress());
        response.put("registeredAt", citizen.getRegisteredAt());
        response.put("totalReports", reports.size());
        response.put("reports", reports);

        return response;
    }

    @PutMapping("/{citizenId}/profile")
    public String updateCitizenProfile(@PathVariable Long citizenId, @RequestBody Map<String, String> request) {
        CitizenProfile citizen = citizenProfileRepository.findById(citizenId).orElse(null);
        if (citizen == null) return "Citizen not found";

        if (request.containsKey("fullName")) citizen.setFullName(request.get("fullName"));
        if (request.containsKey("phoneNumber")) citizen.setPhoneNumber(request.get("phoneNumber"));
        if (request.containsKey("address")) citizen.setAddress(request.get("address"));

        citizenProfileRepository.save(citizen);
        return "Profile updated successfully";
    }
}