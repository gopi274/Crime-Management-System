package com.crimemanagement.dao;

import com.crimemanagement.entity.Complaint;
import com.crimemanagement.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {

    public Complaint insert(Complaint c) throws SQLException {
        String sql = "INSERT INTO complaints (citizen_id, crime_type, description, location, matched_station_id, assigned_officer_id, status, tracking_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (c.getCitizenId() != null) stmt.setInt(1, c.getCitizenId());
            else stmt.setNull(1, Types.INTEGER);
            
            stmt.setString(2, c.getCrimeType());
            stmt.setString(3, c.getDescription());
            stmt.setString(4, c.getLocation());
            
            if (c.getMatchedStationId() != null) stmt.setInt(5, c.getMatchedStationId());
            else stmt.setNull(5, Types.INTEGER);
            
            if (c.getAssignedOfficerId() != null) stmt.setInt(6, c.getAssignedOfficerId());
            else stmt.setNull(6, Types.INTEGER);

            stmt.setString(7, c.getStatus() != null ? c.getStatus() : "PENDING");
            stmt.setString(8, c.getTrackingId());
            
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) c.setComplaintId(rs.getInt(1));
            }
            return c;
        }
    }

    public Complaint findByTrackingId(String trackingId) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE tracking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trackingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Complaint> getAll(Integer stationId) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT * FROM complaints";
        if (stationId != null) {
            sql += " WHERE matched_station_id = ?";
        }
        sql += " ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (stationId != null) {
                stmt.setInt(1, stationId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateStatus(int complaintId, String status) throws SQLException {
        String sql = "UPDATE complaints SET status = ? WHERE complaint_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, complaintId);
            stmt.executeUpdate();
        }
    }

    private Complaint mapRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getInt("complaint_id"));
        
        int citizenId = rs.getInt("citizen_id");
        if (!rs.wasNull()) c.setCitizenId(citizenId);
        
        c.setCrimeType(rs.getString("crime_type"));
        c.setDescription(rs.getString("description"));
        c.setLocation(rs.getString("location"));
        
        int stationId = rs.getInt("matched_station_id");
        if (!rs.wasNull()) c.setMatchedStationId(stationId);
        
        int officerId = rs.getInt("assigned_officer_id");
        if (!rs.wasNull()) c.setAssignedOfficerId(officerId);

        c.setStatus(rs.getString("status"));
        c.setTrackingId(rs.getString("tracking_id"));
        c.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        
        return c;
    }
}
