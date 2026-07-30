package com.crimemanagement.dao;

import com.crimemanagement.entity.LeaveRequest;
import com.crimemanagement.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestDAO {
    
    public LeaveRequest create(LeaveRequest lr) throws SQLException {
        String sql = "INSERT INTO leave_requests (officer_id, leave_type, start_date, end_date, reason, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lr.getOfficerId());
            ps.setString(2, lr.getLeaveType());
            ps.setDate(3, Date.valueOf(lr.getStartDate()));
            ps.setDate(4, Date.valueOf(lr.getEndDate()));
            ps.setString(5, lr.getReason());
            ps.setString(6, lr.getStatus() != null ? lr.getStatus() : "PENDING");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) lr.setLeaveId(rs.getInt(1));
            }
            return lr;
        }
    }

    public void updateStatus(int leaveId, String status) throws SQLException {
        String sql = "UPDATE leave_requests SET status = ? WHERE leave_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, leaveId);
            ps.executeUpdate();
        }
    }

    public List<LeaveRequest> findAllJoined() throws SQLException {
        String sql = "SELECT l.*, o.first_name, o.last_name, o.rank FROM leave_requests l " +
                     "JOIN officers o ON l.officer_id = o.officer_id ORDER BY l.created_at DESC";
        return executeQuery(sql, null);
    }
    
    public List<LeaveRequest> findByOfficerId(int officerId) throws SQLException {
        String sql = "SELECT l.*, o.first_name, o.last_name, o.rank FROM leave_requests l " +
                     "JOIN officers o ON l.officer_id = o.officer_id WHERE l.officer_id = ? ORDER BY l.created_at DESC";
        return executeQuery(sql, officerId);
    }

    private List<LeaveRequest> executeQuery(String sql, Integer officerIdParam) throws SQLException {
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (officerIdParam != null) {
                ps.setInt(1, officerIdParam);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest();
                    lr.setLeaveId(rs.getInt("leave_id"));
                    lr.setOfficerId(rs.getInt("officer_id"));
                    lr.setLeaveType(rs.getString("leave_type"));
                    lr.setStartDate(rs.getDate("start_date").toLocalDate());
                    lr.setEndDate(rs.getDate("end_date").toLocalDate());
                    lr.setReason(rs.getString("reason"));
                    lr.setStatus(rs.getString("status"));
                    
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) lr.setCreatedAt(created.toLocalDateTime());
                    
                    lr.setOfficerName(rs.getString("first_name") + " " + rs.getString("last_name"));
                    lr.setRank(rs.getString("rank"));
                    list.add(lr);
                }
            }
        }
        return list;
    }
}
