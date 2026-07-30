package com.crimemanagement.util;

import java.sql.*;

public class CheckH2Data {
    public static void main(String[] args) {
        String url = "jdbc:h2:./database/crime_management";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            // Evidence removed from tables list
            String[] tables = { "users", "officers", "firs", "cases", "criminals", "suspects" };
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next()) {
                        System.out.println("Table " + table + ": " + rs.getInt(1) + " rows");
                    }
                } catch (Exception e) {
                    System.out.println("Table " + table + " not found or empty: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
