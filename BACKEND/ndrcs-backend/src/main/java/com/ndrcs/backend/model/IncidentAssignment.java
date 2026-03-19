package com.ndrcs.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_assignments")
public class IncidentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rescue_team_id", nullable = false)
    private RescueTeamProfile rescueTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "control_room_id", nullable = false)
    private ControlRoomProfile controlRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false)
    private AssignmentStatus assignmentStatus = AssignmentStatus.ASSIGNED;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum AssignmentStatus {
        ASSIGNED,
        ACCEPTED,
        REACHED,
        COMPLETED,
        CANCELLED
    }

    public IncidentAssignment() {
    }

    public Long getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public RescueTeamProfile getRescueTeam() {
        return rescueTeam;
    }

    public void setRescueTeam(RescueTeamProfile rescueTeam) {
        this.rescueTeam = rescueTeam;
    }

    public ControlRoomProfile getControlRoom() {
        return controlRoom;
    }

    public void setControlRoom(ControlRoomProfile controlRoom) {
        this.controlRoom = controlRoom;
    }

    public AssignmentStatus getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(AssignmentStatus assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}