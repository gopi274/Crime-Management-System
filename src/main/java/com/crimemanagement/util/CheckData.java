package com.crimemanagement.util;

import java.sql.*;

public class CheckData {
    public static void main(String[] args) {
        String url = "jdbc:h2:file:/Users/parthip/crime-management-system/database/crime_management_app;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
        String user = "sa";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to H2 successfully.");

            System.out.println("\n--- USERS ---");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT user_id, username, role, is_active, password_hash FROM users")) {
                while (rs.next()) {
                    System.out.printf("ID: %d, User: %s, Role: %s, Active: %b, Hash: %s%n",
                            rs.getInt("user_id"), rs.getString("username"), rs.getString("role"), rs.getBoolean("is_active"), rs.getString("password_hash"));
                }
            } catch (Exception e) {
                System.out.println("Error reading users: " + e.getMessage());
            }

            System.out.println("\n--- ROLES IN SCHEMA ---");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT DISTINCT role FROM users")) {
                while (rs.next()) {
                    System.out.println("Role found: " + rs.getString("role"));
                }
            } catch (Exception e) {
                System.out.println("Error reading roles: " + e.getMessage());
            }

            System.out.println("\n--- ROW COUNTS ---");
            String[] tables = { "firs", "cases", "criminals", "officers", "audit_logs", "police_stations" };
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next())
                        System.out.println(table + ": " + rs.getInt(1) + " rows");
                } catch (Exception e) {
                    System.out.println("Error reading " + table + ": " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
