package com.crimemanagement.util;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class PasswordFixer {
    public static void main(String[] args) {
        System.out.println("Starting password sanitization...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String selectSql = "SELECT user_id, username, password_hash FROM users";
            String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(selectSql);
                    PreparedStatement ps = conn.prepareStatement(updateSql)) {

                int count = 0;
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String current = rs.getString("password_hash");

                    if (current == null)
                        continue;

                    // BCrypt hashes start with $2a$, $2b$, or $2y$
                    if (!current.startsWith("$2a$") && !current.startsWith("$2b$") && !current.startsWith("$2y$")) {
                        System.out.println("Hashing plaintext password for user: " + username);
                        String newHash = BCrypt.hashpw(current, BCrypt.gensalt());
                        ps.setString(1, newHash);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                        count++;
                    }
                }
                System.out.println("Sanitization complete. Fixed " + count + " passwords.");
            }
        } catch (SQLException e) {
            System.err.println("Error fixing passwords: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
