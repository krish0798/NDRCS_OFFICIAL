package com.ndrcs.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_team_profiles")
public class RescueTeamProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "auth_user_id", nullable = false, unique = true)
    private AuthUser authUser;

    @Column(name = "team_code", nullable = false, unique = true, length = 50)
    private String teamCode;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 50)
    private String vehicleNumber;

    @Column(name = "official_email", nullable = false, unique = true, length = 150)
    private String officialEmail;

    @Column(name = "base_location", length = 150)
    private String baseLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private TeamStatus currentStatus = TeamStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TeamStatus {
        AVAILABLE,
        ASSIGNED,
        IN_PROGRESS,
        OFFLINE
    }

    public RescueTeamProfile() {
    }

    public Long getId() {
        return id;
    }

    public AuthUser getAuthUser() {
        return authUser;
    }

    public void setAuthUser(AuthUser authUser) {
        this.authUser = authUser;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public void setOfficialEmail(String officialEmail) {
        this.officialEmail = officialEmail;
    }

    public String getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    public TeamStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(TeamStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}