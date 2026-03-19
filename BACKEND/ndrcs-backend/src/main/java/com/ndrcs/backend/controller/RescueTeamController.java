package com.ndrcs.backend.controller;

import com.ndrcs.backend.dto.UpdateRescueLocationRequest;
import com.ndrcs.backend.model.RescueTeamLiveStatus;
import com.ndrcs.backend.repository.RescueTeamLiveStatusRepository;
import com.ndrcs.backend.service.RescueTeamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rescue")
@CrossOrigin(origins = "*")
public class RescueTeamController {

    private final RescueTeamService rescueTeamService;
    private final RescueTeamLiveStatusRepository rescueTeamLiveStatusRepository;

    public RescueTeamController(RescueTeamService rescueTeamService,
                                RescueTeamLiveStatusRepository rescueTeamLiveStatusRepository) {
        this.rescueTeamService = rescueTeamService;
        this.rescueTeamLiveStatusRepository = rescueTeamLiveStatusRepository;
    }

    @PostMapping("/update-location")
    public String updateLocation(@RequestBody UpdateRescueLocationRequest request) {
        return rescueTeamService.updateLiveLocation(request);
    }

    @GetMapping("/live-status")
    public List<RescueTeamLiveStatus> getAllLiveStatus() {
        return rescueTeamLiveStatusRepository.findAll();
    }
}