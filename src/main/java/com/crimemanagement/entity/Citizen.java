package com.crimemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizens")
public class Citizen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "citizen_id")
    private int citizenId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "access_key", unique = true)
    private String accessKey;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Citizen() {}

    public Citizen(int citizenId, String name, String phone, String address, Boolean isAnonymous, String accessKey, LocalDateTime createdAt) {
        this.citizenId = citizenId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.isAnonymous = isAnonymous;
        this.accessKey = accessKey;
        this.createdAt = createdAt;
    }

    public int getCitizenId() { return citizenId; }
    public void setCitizenId(int citizenId) { this.citizenId = citizenId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean anonymous) { isAnonymous = anonymous; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
