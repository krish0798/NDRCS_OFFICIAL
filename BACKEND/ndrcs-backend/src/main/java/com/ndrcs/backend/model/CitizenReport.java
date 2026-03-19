package com.ndrcs.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_reports")
public class CitizenReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private CitizenProfile citizen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "report_time", nullable = false)
    private LocalDateTime reportTime = LocalDateTime.now();

    public CitizenReport() {
    }

    public Long getId() {
        return id;
    }

    public CitizenProfile getCitizen() {
        return citizen;
    }

    public void setCitizen(CitizenProfile citizen) {
        this.citizen = citizen;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }
}