package com.ndrcs.backend.config;

import com.ndrcs.backend.model.AuthUser;
import com.ndrcs.backend.model.ControlRoomProfile;
import com.ndrcs.backend.model.RescueTeamProfile;
import com.ndrcs.backend.repository.AuthUserRepository;
import com.ndrcs.backend.repository.ControlRoomProfileRepository;
import com.ndrcs.backend.repository.RescueTeamProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AuthUserRepository authUserRepository;
    private final ControlRoomProfileRepository controlRoomProfileRepository;
    private final RescueTeamProfileRepository rescueTeamProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AuthUserRepository authUserRepository,
                      ControlRoomProfileRepository controlRoomProfileRepository,
                      RescueTeamProfileRepository rescueTeamProfileRepository,
                      PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.controlRoomProfileRepository = controlRoomProfileRepository;
        this.rescueTeamProfileRepository = rescueTeamProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedControlRoomAccounts();
        seedRescueTeamAccounts();
    }

    private void seedControlRoomAccounts() {
        createControlRoomIfNotExists(
                "ctrl001", "ctrl001@ndrcs.gov", "control123",
                "Aarav Mehta", "Chief Control Officer", "EMP-CTRL-001"
        );
        createControlRoomIfNotExists(
                "ctrl002", "ctrl002@ndrcs.gov", "control123",
                "Riya Sharma", "Control Room Supervisor", "EMP-CTRL-002"
        );
        createControlRoomIfNotExists(
                "ctrl003", "ctrl003@ndrcs.gov", "control123",
                "Vikram Rao", "Operations Coordinator", "EMP-CTRL-003"
        );
        createControlRoomIfNotExists(
                "ctrl004", "ctrl004@ndrcs.gov", "control123",
                "Sneha Kulkarni", "Emergency Desk Officer", "EMP-CTRL-004"
        );
    }

    private void seedRescueTeamAccounts() {
        createRescueTeamIfNotExists(
                "rescue001", "rescue001@ndrcs.gov", "rescue123",
                "RT-001", "Rescue Team Alpha", "MH01AB1234", "Dadar Base"
        );
        createRescueTeamIfNotExists(
                "rescue002", "rescue002@ndrcs.gov", "rescue123",
                "RT-002", "Rescue Team Bravo", "MH01AB2234", "Kurla Base"
        );
        createRescueTeamIfNotExists(
                "rescue003", "rescue003@ndrcs.gov", "rescue123",
                "RT-003", "Rescue Team Charlie", "MH01AB3234", "Andheri Base"
        );
        createRescueTeamIfNotExists(
                "rescue004", "rescue004@ndrcs.gov", "rescue123",
                "RT-004", "Rescue Team Delta", "MH01AB4234", "Navi Mumbai Base"
        );
    }

    private void createControlRoomIfNotExists(String userId,
                                               String email,
                                               String password,
                                               String fullName,
                                               String designation,
                                               String employeeCode) {
        if (authUserRepository.findByUserId(userId).isPresent()) {
            return;
        }

        AuthUser authUser = new AuthUser();
        authUser.setUserId(userId);
        authUser.setPasswordHash(passwordEncoder.encode(password));
        authUser.setRole(AuthUser.Role.CONTROL_ROOM);
        authUser.setAccountStatus(AuthUser.AccountStatus.ACTIVE);
        authUser.setEmailVerified(true);

        AuthUser savedAuthUser = authUserRepository.save(authUser);

        ControlRoomProfile profile = new ControlRoomProfile();
        profile.setAuthUser(savedAuthUser);
        profile.setFullName(fullName);
        profile.setOfficialEmail(email);
        profile.setDesignation(designation);
        profile.setEmployeeCode(employeeCode);

        controlRoomProfileRepository.save(profile);
    }

    private void createRescueTeamIfNotExists(String userId,
                                             String email,
                                             String password,
                                             String teamCode,
                                             String teamName,
                                             String vehicleNumber,
                                             String baseLocation) {
        if (authUserRepository.findByUserId(userId).isPresent()) {
            return;
        }

        AuthUser authUser = new AuthUser();
        authUser.setUserId(userId);
        authUser.setPasswordHash(passwordEncoder.encode(password));
        authUser.setRole(AuthUser.Role.RESCUE_TEAM);
        authUser.setAccountStatus(AuthUser.AccountStatus.ACTIVE);
        authUser.setEmailVerified(true);

        AuthUser savedAuthUser = authUserRepository.save(authUser);

        RescueTeamProfile profile = new RescueTeamProfile();
        profile.setAuthUser(savedAuthUser);
        profile.setTeamCode(teamCode);
        profile.setTeamName(teamName);
        profile.setVehicleNumber(vehicleNumber);
        profile.setOfficialEmail(email);
        profile.setBaseLocation(baseLocation);
        profile.setCurrentStatus(RescueTeamProfile.TeamStatus.AVAILABLE);

        rescueTeamProfileRepository.save(profile);
    }
}