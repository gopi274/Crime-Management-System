package com.crimemanagement.entity;

public class UserAccount {
    private Integer userId;
    private String username;
    private String passwordHash;
    private String role; // ADMIN, OFFICER, COURT, JAILER
    private Integer officerId;
    private Integer jailId;
    private Integer courtId;
    private Integer scientistId;

    public Integer getScientistId() { return scientistId; }
    public void setScientistId(Integer scientistId) { this.scientistId = scientistId; }

    public Integer getCourtId() { return courtId; }
    public void setCourtId(Integer courtId) { this.courtId = courtId; }

    public Integer getJailId() { return jailId; }
    public void setJailId(Integer jailId) { this.jailId = jailId; }

    public Integer getOfficerId() { return officerId; }
    public void setOfficerId(Integer officerId) { this.officerId = officerId; }

    private Integer citizenId;
    public Integer getCitizenId() { return citizenId; }
    public void setCitizenId(Integer citizenId) { this.citizenId = citizenId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}


