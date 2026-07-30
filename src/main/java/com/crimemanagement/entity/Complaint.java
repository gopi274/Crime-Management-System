package com.crimemanagement.entity;

import java.time.LocalDateTime;

public class Complaint {
    private Integer complaintId;
    private Integer citizenId;
    private String crimeType;
    private String description;
    private String location;
    private Integer matchedStationId;
    private Integer assignedOfficerId;
    private String status;
    private LocalDateTime createdAt;
    private String trackingId;

    public Integer getComplaintId() { return complaintId; }
    public void setComplaintId(Integer complaintId) { this.complaintId = complaintId; }

    public Integer getCitizenId() { return citizenId; }
    public void setCitizenId(Integer citizenId) { this.citizenId = citizenId; }

    public String getCrimeType() { return crimeType; }
    public void setCrimeType(String crimeType) { this.crimeType = crimeType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getMatchedStationId() { return matchedStationId; }
    public void setMatchedStationId(Integer matchedStationId) { this.matchedStationId = matchedStationId; }

    public Integer getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(Integer assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
}
