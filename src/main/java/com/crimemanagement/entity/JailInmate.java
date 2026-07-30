package com.crimemanagement.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JailInmate {
    public enum InmateStatus {
        ACTIVE, RELEASED, PAROLE, TRANSFERRED
    }

    private Integer inmateId;
    private Integer criminalId;
    private Integer jailId;
    private Integer caseId;
    private LocalDate entryDate;
    private String sentenceDuration;
    private LocalDate releaseDate;
    private InmateStatus status;
    private LocalDateTime createdAt;

    public JailInmate() {}

    public Integer getInmateId() { return inmateId; }
    public void setInmateId(Integer inmateId) { this.inmateId = inmateId; }

    public Integer getCriminalId() { return criminalId; }
    public void setCriminalId(Integer criminalId) { this.criminalId = criminalId; }

    public Integer getJailId() { return jailId; }
    public void setJailId(Integer jailId) { this.jailId = jailId; }

    public Integer getCaseId() { return caseId; }
    public void setCaseId(Integer caseId) { this.caseId = caseId; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public String getSentenceDuration() { return sentenceDuration; }
    public void setSentenceDuration(String sentenceDuration) { this.sentenceDuration = sentenceDuration; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public InmateStatus getStatus() { return status; }
    public void setStatus(InmateStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
