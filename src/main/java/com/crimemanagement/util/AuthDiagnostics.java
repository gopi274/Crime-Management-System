package com.crimemanagement.util;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class AuthDiagnostics {
    public static void main(String[] args) {
        System.out.println("--- Database Auth Diagnostics ---");
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Connected to: " + meta.getURL());

            String sql = "SELECT username, password_hash, role FROM users";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                boolean foundAdmin = false;
                while (rs.next()) {
                    String user = rs.getString("username");
                    String hash = rs.getString("password_hash");
                    String role = rs.getString("role");

                    System.out.println("User found: [" + user + "], Role: [" + role + "], Hash: [" + hash + "]");

                    if ("admin".equals(user)) {
                        foundAdmin = true;
                        boolean match = BCrypt.checkpw("admin", hash);
                        System.out.println(
                                "  -> Verification test for 'admin' password: " + (match ? "SUCCESS" : "FAILED"));
                    }
                }

                if (!foundAdmin) {
                    System.out.println("CRITICAL: 'admin' user not found in database!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
