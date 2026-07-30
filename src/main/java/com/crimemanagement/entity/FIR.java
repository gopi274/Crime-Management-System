package com.crimemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "firs")
public class FIR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fir_id")
    private int firId;
    
    @NotBlank(message = "FIR number is required")
    @Column(name = "fir_number", unique = true, nullable = false)
    private String firNumber;
    
    @NotBlank(message = "Crime type is required")
    @Column(name = "crime_type", nullable = false)
    private String crimeType;
    
    @NotBlank(message = "Crime description is required")
    @Column(name = "crime_description", columnDefinition = "TEXT", nullable = false)
    private String crimeDescription;
    
    @NotBlank(message = "Location is required")
    @Column(name = "location", nullable = false)
    private String location;
    
    @NotNull(message = "Date of incident is required")
    @Column(name = "date_of_incident", nullable = false)
    private LocalDateTime dateOfIncident;
    
    @NotNull(message = "Date reported is required")
    @Column(name = "date_reported", nullable = false)
    private LocalDateTime dateReported;
    
    @Column(name = "citizen_id")
    private Integer citizenId;
    
    @Transient
    private String complainantName;
    
    @Transient
    private String assignedOfficerName;
    
    @Column(name = "status", nullable = false)
    private String status = "Open";
    
    @Column(name = "assigned_officer_id")
    private Integer assignedOfficerId;
    
    @Column(name = "station_id")
    private Integer stationId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FIR() {}

    public FIR(int firId, String firNumber, String crimeType, String crimeDescription, 
               String location, LocalDateTime dateOfIncident, LocalDateTime dateReported, 
               Integer citizenId, String status, Integer assignedOfficerId, Integer stationId, 
               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.firId = firId;
        this.firNumber = firNumber;
        this.crimeType = crimeType;
        this.crimeDescription = crimeDescription;
        this.location = location;
        this.dateOfIncident = dateOfIncident;
        this.dateReported = dateReported;
        this.citizenId = citizenId;
        this.status = status;
        this.assignedOfficerId = assignedOfficerId;
        this.stationId = stationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getFirId() { return firId; }
    public void setFirId(int firId) { this.firId = firId; }
    public String getFirNumber() { return firNumber; }
    public void setFirNumber(String firNumber) { this.firNumber = firNumber; }
    public String getCrimeType() { return crimeType; }
    public void setCrimeType(String crimeType) { this.crimeType = crimeType; }
    public String getCrimeDescription() { return crimeDescription; }
    public void setCrimeDescription(String crimeDescription) { this.crimeDescription = crimeDescription; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getDateOfIncident() { return dateOfIncident; }
    public void setDateOfIncident(LocalDateTime dateOfIncident) { this.dateOfIncident = dateOfIncident; }
    public LocalDateTime getDateReported() { return dateReported; }
    public void setDateReported(LocalDateTime dateReported) { this.dateReported = dateReported; }
    public Integer getCitizenId() { return citizenId; }
    public void setCitizenId(Integer citizenId) { this.citizenId = citizenId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(Integer assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }
    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getComplainantName() { return complainantName; }
    public void setComplainantName(String complainantName) { this.complainantName = complainantName; }

    public String getAssignedOfficerName() { return assignedOfficerName; }
    public void setAssignedOfficerName(String name) { this.assignedOfficerName = name; }

    @Override
    public String toString() {
        return firNumber + " (" + crimeType + ")";
    }
}
