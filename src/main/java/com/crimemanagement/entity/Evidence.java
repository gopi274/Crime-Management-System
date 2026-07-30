package com.crimemanagement.entity;

import java.time.LocalDateTime;

public class Evidence {
    public enum EvidenceStatus {
        COLLECTED, ANALYZED, IN_COURT, RETURNED, DESTROYED, LAB_PROCESS, SFSL_ESCALATION, COURT_SUBMITTED
    }

    private Integer evidenceId;
    private Integer firId;
    private String evidenceType;
    private String description;
    private String locationFound;
    private Integer collectedBy; // officer_id
    private LocalDateTime collectedDate;
    private String storageLocation;
    private Integer labId;
    private Integer scientistId;
    private EvidenceStatus status;
    private String mediaType; // PHOTO, VIDEO, AUDIO, DOCUMENT
    private String mediaUrl;  // Path or URL to file
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Evidence() {}

    public Integer getLabId() { return labId; }
    public void setLabId(Integer labId) { this.labId = labId; }

    public Integer getScientistId() { return scientistId; }
    public void setScientistId(Integer scientistId) { this.scientistId = scientistId; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public Integer getEvidenceId() { return evidenceId; }
    public void setEvidenceId(Integer evidenceId) { this.evidenceId = evidenceId; }

    public Integer getFirId() { return firId; }
    public void setFirId(Integer firId) { this.firId = firId; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationFound() { return locationFound; }
    public void setLocationFound(String locationFound) { this.locationFound = locationFound; }

    public Integer getCollectedBy() { return collectedBy; }
    public void setCollectedBy(Integer collectedBy) { this.collectedBy = collectedBy; }

    public LocalDateTime getCollectedDate() { return collectedDate; }
    public void setCollectedDate(LocalDateTime collectedDate) { this.collectedDate = collectedDate; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public EvidenceStatus getStatus() { return status; }
    public void setStatus(EvidenceStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
