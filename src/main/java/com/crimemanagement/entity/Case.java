package com.crimemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cases")
public class Case {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private int caseId;
    
    @NotBlank(message = "Case number is required")
    @Column(name = "case_number", unique = true, nullable = false)
    private String caseNumber;
    
    @NotNull(message = "FIR ID is required")
    @Column(name = "fir_id", nullable = false)
    private int firId;
    
    @NotBlank(message = "Case title is required")
    @Column(name = "case_title", nullable = false)
    private String caseTitle;
    
    @Column(name = "case_description", columnDefinition = "TEXT")
    private String caseDescription;
    
    @Column(name = "priority", nullable = false)
    private String priority = "Medium";
    
    @Column(name = "status", nullable = false)
    private String status = "Open";
    
    @Column(name = "assigned_officer_id")
    private Integer assignedOfficerId;
    
    @Column(name = "court_id")
    private Integer courtId;
    
    @Column(name = "target_date")
    private LocalDate targetDate;
    
    @Column(name = "delay_explanation", columnDefinition = "TEXT")
    private String delayExplanation;
    
    @Column(name = "next_trial_date")
    private LocalDate nextTrialDate;
    
    @Column(name = "witness_summons", columnDefinition = "TEXT")
    private String witnessSummons;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_public")
    private boolean isPublic;
    
    @Column(name = "verdict_text", columnDefinition = "TEXT")
    private String verdictText;

    @Column(name = "article_text", columnDefinition = "TEXT")
    private String articleText;

    @Column(name = "is_verified_by_court")
    private boolean isVerifiedByCourt;

    public Case() {}

    public Case(int caseId, String caseNumber, int firId, String caseTitle, String caseDescription, 
                String priority, String status, Integer assignedOfficerId, Integer courtId, 
                LocalDate targetDate, String delayExplanation, LocalDate nextTrialDate, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.caseId = caseId;
        this.caseNumber = caseNumber;
        this.firId = firId;
        this.caseTitle = caseTitle;
        this.caseDescription = caseDescription;
        this.priority = priority;
        this.status = status;
        this.assignedOfficerId = assignedOfficerId;
        this.courtId = courtId;
        this.targetDate = targetDate;
        this.delayExplanation = delayExplanation;
        this.nextTrialDate = nextTrialDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public int getFirId() { return firId; }
    public void setFirId(int firId) { this.firId = firId; }
    public String getCaseTitle() { return caseTitle; }
    public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }
    public String getCaseDescription() { return caseDescription; }
    public void setCaseDescription(String caseDescription) { this.caseDescription = caseDescription; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(Integer assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }
    public Integer getCourtId() { return courtId; }
    public void setCourtId(Integer courtId) { this.courtId = courtId; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public String getDelayExplanation() { return delayExplanation; }
    public void setDelayExplanation(String delayExplanation) { this.delayExplanation = delayExplanation; }
    public LocalDate getNextTrialDate() { return nextTrialDate; }
    public void setNextTrialDate(LocalDate nextTrialDate) { this.nextTrialDate = nextTrialDate; }
    public String getWitnessSummons() { return witnessSummons; }
    public void setWitnessSummons(String witnessSummons) { this.witnessSummons = witnessSummons; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isPublic() { return isPublic; }
    public void setIsPublic(boolean isPublic) { this.isPublic = isPublic; }
    public String getVerdictText() { return verdictText; }
    public void setVerdictText(String verdictText) { this.verdictText = verdictText; }
    public String getArticleText() { return articleText; }
    public void setArticleText(String articleText) { this.articleText = articleText; }
    public boolean isVerifiedByCourt() { return isVerifiedByCourt; }
    public void setIsVerifiedByCourt(boolean isVerifiedByCourt) { this.isVerifiedByCourt = isVerifiedByCourt; }

    @Override
    public String toString() {
        return "Case{" +
                "caseId=" + caseId +
                ", caseNumber='" + caseNumber + '\'' +
                ", firId=" + firId +
                ", status='" + status + '\'' +
                ", assignedOfficerId=" + assignedOfficerId +
                ", courtId=" + courtId +
                '}';
    }
}
