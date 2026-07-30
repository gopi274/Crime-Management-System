package com.crimemanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class representing a Suspect in the crime management system
 */
public class Suspect {
    private int suspectId;
    private int caseId;
    private int criminalId;
    private String role;
    private String charge; // BNS Section
    private String disposition; // GUILTY, ACQUITTED, PENDING
    private LocalDate arrestDate;
    private BigDecimal bailAmount;
    private String bailStatus;
    private String gender;
    private LocalDateTime createdAt;

    // Default constructor
    public Suspect() {}

    // Constructor with all fields
    public Suspect(int suspectId, int caseId, int criminalId, String role, String charge, String disposition,
                   LocalDate arrestDate, BigDecimal bailAmount, String bailStatus, LocalDateTime createdAt) {
        this.suspectId = suspectId;
        this.caseId = caseId;
        this.criminalId = criminalId;
        this.role = role;
        this.charge = charge;
        this.disposition = disposition;
        this.arrestDate = arrestDate;
        this.bailAmount = bailAmount;
        this.bailStatus = bailStatus;
        this.createdAt = createdAt;
    }

    public String getDisposition() { return disposition; }
    public void setDisposition(String disposition) { this.disposition = disposition; }

    public String getCharge() { return charge; }
    public void setCharge(String charge) { this.charge = charge; }

    // Getters and Setters
    public int getSuspectId() {
        return suspectId;
    }

    public void setSuspectId(int suspectId) {
        this.suspectId = suspectId;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public int getCriminalId() {
        return criminalId;
    }

    public void setCriminalId(int criminalId) {
        this.criminalId = criminalId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getArrestDate() {
        return arrestDate;
    }

    public void setArrestDate(LocalDate arrestDate) {
        this.arrestDate = arrestDate;
    }

    public BigDecimal getBailAmount() {
        return bailAmount;
    }

    public void setBailAmount(BigDecimal bailAmount) {
        this.bailAmount = bailAmount;
    }

    public String getBailStatus() { return bailStatus; }
    public void setBailStatus(String bailStatus) { this.bailStatus = bailStatus; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Suspect{" +
                "suspectId=" + suspectId +
                ", caseId=" + caseId +
                ", criminalId=" + criminalId +
                ", role='" + role + '\'' +
                ", charge='" + charge + '\'' +
                ", arrestDate=" + arrestDate +
                ", bailAmount=" + bailAmount +
                ", bailStatus='" + bailStatus + '\'' +
                '}';
    }
}
