package com.ndrcs.backend.controller;

import com.ndrcs.backend.dto.LoginRequest;
import com.ndrcs.backend.dto.RegisterCitizenRequest;
import com.ndrcs.backend.dto.VerifyEmailOtpRequest;
import com.ndrcs.backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String registerCitizen(@RequestBody RegisterCitizenRequest request) {
        return authService.registerCitizen(request);
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestBody VerifyEmailOtpRequest request) {
        return authService.verifyEmailOtp(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}