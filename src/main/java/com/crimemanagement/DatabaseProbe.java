package com.crimemanagement;
import com.crimemanagement.util.DatabaseConnection;
import java.sql.*;

public class DatabaseProbe {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println(">>> Kochi-Justice Probe: " + conn.getMetaData().getURL());
            
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) System.out.println("USER COUNT: " + rs.getInt(1));
            }
            
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM firs")) {
                if (rs.next()) System.out.println("FIR COUNT: " + rs.getInt(1));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM cases")) {
                if (rs.next()) System.out.println("CASE COUNT: " + rs.getInt(1));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM jails")) {
                if (rs.next()) System.out.println("JAIL COUNT: " + rs.getInt(1));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM courts")) {
                if (rs.next()) System.out.println("COURT COUNT: " + rs.getInt(1));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT role, COUNT(*) FROM users GROUP BY role")) {
                while (rs.next()) System.out.println("ROLE: " + rs.getString(1) + " (Count: " + rs.getInt(2) + ")");
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT name FROM jails WHERE name LIKE '%Poojappura%'")) {
                if (rs.next()) System.out.println("VERIFIED JAIL: " + rs.getString(1));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT name FROM courts WHERE name LIKE '%Vanchiyoor%'")) {
                if (rs.next()) System.out.println("VERIFIED COURT: " + rs.getString(1));
            }
        }
    }
}
