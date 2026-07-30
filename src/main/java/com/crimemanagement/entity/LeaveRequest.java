package com.crimemanagement.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequest {
    private Integer leaveId;
    private Integer officerId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    
    // Virtual fields for display
    private String officerName;
    private String rank;

    public Integer getLeaveId() { return leaveId; }
    public void setLeaveId(Integer leaveId) { this.leaveId = leaveId; }
    public Integer getOfficerId() { return officerId; }
    public void setOfficerId(Integer officerId) { this.officerId = officerId; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) { this.officerName = officerName; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
}
