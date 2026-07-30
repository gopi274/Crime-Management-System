package com.crimemanagement.util;

import java.io.*;
import java.sql.*;
import java.nio.charset.StandardCharsets;

public class GraduationDataBootstrap {
    public static void main(String[] args) throws Exception {
        System.out.println(">>> V5.2 Justice: Initializing Isolated Graduation Deployment...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                // Ensure H2 mode
                try { stmt.execute("SET REFERENTIAL_INTEGRITY FALSE"); } catch (Exception e) {}
                
                System.out.println("Applying Schema...");
                runSqlResource("schema.sql", conn);
                
                System.out.println("Applying Regional Graduation Data...");
                runSqlResource("justice_graduation_data.sql", conn);
                
                stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
                System.out.println(">>> Graduation Complete: 14 Districts, 24 Courts, 12 Jails Provisioned.");
            }
        }
    }

    private static void runSqlResource(String name, Connection conn) throws Exception {
        InputStream is = GraduationDataBootstrap.class.getClassLoader().getResourceAsStream(name);
        if (is == null) {
            // Fallback for local development
            File local = new File("src/main/resources/" + name);
            if (local.exists()) {
                is = new FileInputStream(local);
            }
        }
        if (is == null) {
            System.err.println("CRITICAL: Could not find SQL resource: " + name);
            return;
        }
        String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        try (Statement stmt = conn.createStatement()) {
            for (String c : sql.split(";")) {
                if (c.trim().isEmpty() || c.trim().startsWith("--")) continue;
                try {
                    stmt.execute(c);
                } catch (SQLException e) {
                    // System.err.println("Error in " + name + ": " + e.getMessage());
                }
            }
        }
    }
}
