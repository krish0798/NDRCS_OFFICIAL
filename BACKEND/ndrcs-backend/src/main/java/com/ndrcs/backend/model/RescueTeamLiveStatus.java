package com.ndrcs.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_team_live_status")
public class RescueTeamLiveStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "rescue_team_id", nullable = false, unique = true)
    private RescueTeamProfile rescueTeam;

    @ManyToOne
    @JoinColumn(name = "assigned_incident_id")
    private Incident assignedIncident;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false)
    private Incident.UrgencyLevel urgencyLevel = Incident.UrgencyLevel.NORMAL;

    @Column(name = "status_message", length = 255)
    private String statusMessage;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public RescueTeamLiveStatus() {
    }

    public Long getId() {
        return id;
    }

    public RescueTeamProfile getRescueTeam() {
        return rescueTeam;
    }

    public void setRescueTeam(RescueTeamProfile rescueTeam) {
        this.rescueTeam = rescueTeam;
    }

    public Incident getAssignedIncident() {
        return assignedIncident;
    }

    public void setAssignedIncident(Incident assignedIncident) {
        this.assignedIncident = assignedIncident;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Incident.UrgencyLevel getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(Incident.UrgencyLevel urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}