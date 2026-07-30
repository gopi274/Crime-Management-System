package com.crimemanagement.service;

import com.crimemanagement.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to handle audit logging for system integrity.
 * Records all critical actions to the database.
 */
@Service
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    public static void log(String actor, String action, String details) {
        String sql = "INSERT INTO audit_logs (actor, action, details) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, actor);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("CRITICAL: Failed to write audit log to database!", e);
        }
    }

    public List<String> getRecentLogs(int limit) {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT created_at, actor, action, details FROM audit_logs ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String timestamp = rs.getString("created_at");
                    String actor = rs.getString("actor");
                    String action = rs.getString("action");
                    String details = rs.getString("details");

                    logs.add(String.format("[%s] %s: %s | %s",
                            timestamp, actor, action, details));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching audit logs", e);
        }
        return logs;
    }
}
