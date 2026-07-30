package com.crimemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "officers")
public class Officer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "officer_id")
    private int officerId;
    
    @NotBlank(message = "Badge number is required")
    @Column(name = "badge_number", unique = true, nullable = false)
    private String badgeNumber;
    
    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @NotBlank(message = "Rank is required")
    @Column(name = "rank", nullable = false)
    private String rank;
    
    @Column(name = "station_id")
    private Integer stationId;

    @Column(name = "salary")
    private Double salary = 0.00;

    @Column(name = "leave_balance")
    private Integer leaveBalance = 30;
    
    @Column(name = "jurisdiction_level")
    private String jurisdictionLevel = "STATION_OFFICER";
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    @Column(name = "email")
    private String email;
    
    @Column(name = "is_on_duty")
    private Boolean isOnDuty = false;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Officer() {}

    public Officer(int officerId, String badgeNumber, String firstName, String lastName, 
                   String rank, Integer stationId, String jurisdictionLevel, String phoneNumber, 
                   String email, Boolean isOnDuty, Double salary, Integer leaveBalance,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.officerId = officerId;
        this.badgeNumber = badgeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.rank = rank;
        this.stationId = stationId;
        this.jurisdictionLevel = jurisdictionLevel;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.isOnDuty = isOnDuty;
        this.salary = salary;
        this.leaveBalance = leaveBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getOfficerId() { return officerId; }
    public void setOfficerId(int officerId) { this.officerId = officerId; }
    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }
    public String getJurisdictionLevel() { return jurisdictionLevel; }
    public void setJurisdictionLevel(String jurisdictionLevel) { this.jurisdictionLevel = jurisdictionLevel; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getIsOnDuty() { return isOnDuty; }
    public void setIsOnDuty(Boolean isOnDuty) { this.isOnDuty = isOnDuty; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public Integer getLeaveBalance() { return leaveBalance; }
    public void setLeaveBalance(Integer leaveBalance) { this.leaveBalance = leaveBalance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + rank + ") - Station " + (stationId != null ? stationId : "HQ");
    }
}
