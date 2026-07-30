package com.crimemanagement.util;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class ResetAdminPassword {
    public static void main(String[] args) {
        System.out.println("Resetting admin password to 'admin'...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET password_hash = ? WHERE username = 'admin'";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String newHash = BCrypt.hashpw("admin", BCrypt.gensalt());
                ps.setString(1, newHash);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Successfully reset password for 'admin'.");
                } else {
                    System.out.println("ERROR: No 'admin' user found to reset.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
