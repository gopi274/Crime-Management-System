package com.crimemanagement.entity;

import java.time.LocalDateTime;

public class Jail {
    public enum JailType {
        CENTRAL, DISTRICT, SUB_JAIL, SPECIAL, OPEN, WOMEN
    }

    private Integer jailId;
    private String name;
    private JailType type;
    private String location;
    private Integer capacity;
    private LocalDateTime createdAt;

    public Jail() {}

    public Integer getJailId() { return jailId; }
    public void setJailId(Integer jailId) { this.jailId = jailId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public JailType getType() { return type; }
    public void setType(JailType type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return name + " (" + location + ")"; }
}
