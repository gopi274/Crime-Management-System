package com.crimemanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "courts")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private int courtId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "jurisdiction_level", nullable = false)
    private String jurisdictionLevel;

    public Court() {}

    public Court(int courtId, String name, String city, String state, String jurisdictionLevel) {
        this.courtId = courtId;
        this.name = name;
        this.city = city;
        this.state = state;
        this.jurisdictionLevel = jurisdictionLevel;
    }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getJurisdictionLevel() { return jurisdictionLevel; }
    public void setJurisdictionLevel(String jurisdictionLevel) { this.jurisdictionLevel = jurisdictionLevel; }

    @Override
    public String toString() {
        return name + " (" + city + ")";
    }
}
