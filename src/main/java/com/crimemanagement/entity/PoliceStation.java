package com.crimemanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "police_stations")
public class PoliceStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private int stationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "max_inspectors")
    private int maxInspectors = 1;

    @Column(name = "max_total_strength")
    private int maxTotalStrength = 50;

    public PoliceStation() {}

    public PoliceStation(int stationId, String name, String city, String state, int maxInspectors, int maxTotalStrength) {
        this.stationId = stationId;
        this.name = name;
        this.city = city;
        this.state = state;
        this.maxInspectors = maxInspectors;
        this.maxTotalStrength = maxTotalStrength;
    }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getMaxInspectors() { return maxInspectors; }
    public void setMaxInspectors(int maxInspectors) { this.maxInspectors = maxInspectors; }

    public int getMaxTotalStrength() { return maxTotalStrength; }
    public void setMaxTotalStrength(int maxTotalStrength) { this.maxTotalStrength = maxTotalStrength; }
}
