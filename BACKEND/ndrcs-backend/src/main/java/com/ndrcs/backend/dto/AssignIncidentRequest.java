package com.ndrcs.backend.dto;

public class AssignIncidentRequest {

    private String controlUserId;
    private String controlPassword;
    private Long incidentId;
    private Long rescueTeamId;

    public AssignIncidentRequest() {
    }

    public String getControlUserId() {
        return controlUserId;
    }

    public void setControlUserId(String controlUserId) {
        this.controlUserId = controlUserId;
    }

    public String getControlPassword() {
        return controlPassword;
    }

    public void setControlPassword(String controlPassword) {
        this.controlPassword = controlPassword;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public Long getRescueTeamId() {
        return rescueTeamId;
    }

    public void setRescueTeamId(Long rescueTeamId) {
        this.rescueTeamId = rescueTeamId;
    }
}