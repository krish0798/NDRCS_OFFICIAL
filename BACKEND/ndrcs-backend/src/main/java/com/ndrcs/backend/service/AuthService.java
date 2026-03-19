package com.ndrcs.backend.service;

import com.ndrcs.backend.dto.LoginRequest;
import com.ndrcs.backend.dto.RegisterCitizenRequest;
import com.ndrcs.backend.dto.VerifyEmailOtpRequest;
import com.ndrcs.backend.model.AuthUser;
import com.ndrcs.backend.model.CitizenProfile;
import com.ndrcs.backend.model.EmailVerification;
import com.ndrcs.backend.repository.AuthUserRepository;
import com.ndrcs.backend.repository.CitizenProfileRepository;
import com.ndrcs.backend.repository.EmailVerificationRepository;
import com.ndrcs.backend.util.OtpGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthUserRepository authUserRepository,
                       CitizenProfileRepository citizenProfileRepository,
                       EmailVerificationRepository emailVerificationRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.citizenProfileRepository = citizenProfileRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerCitizen(RegisterCitizenRequest request) {

        if (authUserRepository.findByUserId(request.getUserId()).isPresent()) {
            return "User ID already exists";
        }

        if (citizenProfileRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already exists";
        }

        if (citizenProfileRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            return "Phone number already exists";
        }

        AuthUser authUser = new AuthUser();
        authUser.setUserId(request.getUserId());
        authUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        authUser.setRole(AuthUser.Role.CITIZEN);
        authUser.setAccountStatus(AuthUser.AccountStatus.ACTIVE);
        authUser.setEmailVerified(false);

        AuthUser savedAuthUser = authUserRepository.save(authUser);

        CitizenProfile citizenProfile = new CitizenProfile();
        citizenProfile.setAuthUser(savedAuthUser);
        citizenProfile.setFullName(request.getFullName());
        citizenProfile.setEmail(request.getEmail());
        citizenProfile.setPhoneNumber(request.getPhoneNumber());
        citizenProfile.setAddress(request.getAddress());

        CitizenProfile savedCitizen = citizenProfileRepository.save(citizenProfile);

        String otp = OtpGenerator.generateOtp();

        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setCitizen(savedCitizen);
        emailVerification.setEmail(savedCitizen.getEmail());
        emailVerification.setOtpCode(otp);
        emailVerification.setStatus(EmailVerification.VerificationStatus.PENDING);
        emailVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        emailVerificationRepository.save(emailVerification);

        emailService.sendOtpEmail(savedCitizen.getEmail(), otp);

        return "Citizen registered successfully. OTP sent to email.";
    }

    public String verifyEmailOtp(VerifyEmailOtpRequest request) {

        Optional<AuthUser> authUserOptional = authUserRepository.findByUserId(request.getUserId());
        if (authUserOptional.isEmpty()) {
            return "Invalid user ID";
        }

        AuthUser authUser = authUserOptional.get();

        CitizenProfile citizen = citizenProfileRepository.findAll()
                .stream()
                .filter(c -> c.getAuthUser().getId().equals(authUser.getId()))
                .findFirst()
                .orElse(null);

        if (citizen == null) {
            return "Citizen profile not found";
        }

        if (!citizen.getEmail().equalsIgnoreCase(request.getEmail())) {
            return "Email does not match registered citizen email";
        }

        Optional<EmailVerification> verificationOptional =
                emailVerificationRepository.findTopByEmailAndOtpCodeOrderByCreatedAtDesc(
                        request.getEmail(),
                        request.getOtpCode()
                );

        if (verificationOptional.isEmpty()) {
            return "Invalid OTP";
        }

        EmailVerification verification = verificationOptional.get();

        if (verification.getStatus() == EmailVerification.VerificationStatus.VERIFIED) {
            return "Email already verified";
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            verification.setStatus(EmailVerification.VerificationStatus.EXPIRED);
            emailVerificationRepository.save(verification);
            return "OTP expired";
        }

        verification.setStatus(EmailVerification.VerificationStatus.VERIFIED);
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        authUser.setEmailVerified(true);
        authUserRepository.save(authUser);

        return "Email verified successfully";
    }

    public String login(LoginRequest request) {
        Optional<AuthUser> userOptional = authUserRepository.findByUserId(request.getUserId());

        if (userOptional.isEmpty()) {
            return "Invalid user ID";
        }

        AuthUser user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return "Invalid password";
        }

        if (user.getRole() == AuthUser.Role.CITIZEN && !user.isEmailVerified()) {
            return "Email not verified";
        }

        user.setLastLogin(LocalDateTime.now());
        authUserRepository.save(user);

        return "Login successful: " + user.getRole().name();
    }
}