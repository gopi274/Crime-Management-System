package com.crimemanagement.service;

import com.crimemanagement.entity.Complaint;
import java.util.Arrays;
import java.util.List;

public class ComplaintAssignmentService {
    
    /**
     * AI Routing Engine Simulator
     * Parses complaints against the 470+ Kerala Police Stations DB and auto-assigns.
     * With 112 Dispatch Override.
     */
    public String parseAndAssign(Complaint complaint) {
        String desc = complaint.getDescription() != null ? complaint.getDescription().toLowerCase() : "";
        List<String> emergencyKeywords = Arrays.asList("gun", "kidnap", "murder", "bomb", "hostage", "kill", "fire", "attack");

        boolean isEmergency = false;
        for (String kw : emergencyKeywords) {
            if (desc.contains(kw)) {
                isEmergency = true;
                break;
            }
        }

        // 112 Dispatch Override execution
        if (isEmergency) {
            // "If the AI detects extreme panic keywords ("Gun", "Kidnap"), it aborts auto-routing 
            // and manually flashes a red alarm to the 24/7 EMERGENCY_DISPATCHER."
            complaint.setStatus("EMERGENCY_DISPATCH_112");
            return "EMERGENCY_DISPATCH_112";
        }

        // AI Location Routing (simulated confidence > 95%)
        String loc = complaint.getLocation() != null ? complaint.getLocation().toLowerCase() : "";
        
        if (loc.contains("trivandrum") || loc.contains("thiruvananthapuram")) {
            complaint.setStatus("AUTO_ASSIGNED_TVM_CITY");
            complaint.setMatchedStationId(1);
            return "AUTO_ASSIGNED_TVM_CITY";
        } else if (loc.contains("kochi") || loc.contains("ernakulam")) {
            complaint.setStatus("AUTO_ASSIGNED_KOCHI_CITY");
            complaint.setMatchedStationId(2);
            return "AUTO_ASSIGNED_KOCHI_CITY";
        } else if (loc.contains("kozhikode") || loc.contains("calicut")) {
            complaint.setStatus("AUTO_ASSIGNED_KOZHIKODE_CITY");
            complaint.setMatchedStationId(3);
            return "AUTO_ASSIGNED_KOZHIKODE_CITY";
        } else if (loc.contains("alappuzha") || loc.contains("alleppey")) {
            complaint.setStatus("AUTO_ASSIGNED_ALAPPUZHA_NORTH");
            complaint.setMatchedStationId(4);
            return "AUTO_ASSIGNED_ALAPPUZHA_NORTH";
        }

        // If below 95% confidence on location
        complaint.setStatus("PENDING_MANUAL_REVIEW");
        return "PENDING_MANUAL_REVIEW";
    }
}
