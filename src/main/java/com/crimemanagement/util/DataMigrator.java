package com.crimemanagement.util;

import java.sql.*;

public class DataMigrator {
    private static final String H2_URL = "jdbc:h2:./database/crime_management";
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/crime_management";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "root";

    public static void main(String[] args) {
        try {
            Class.forName("org.h2.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection h2Conn = DriverManager.getConnection(H2_URL, "sa", "");
                    Connection mysqlConn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS)) {

                System.out.println("Starting migration...");

                // Disable FK checks in MySQL
                try (Statement stmt = mysqlConn.createStatement()) {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                }

                // Evidence removed from tables list
                String[] tables = { "officers", "criminals", "firs", "cases", "suspects", "audit_logs" };

                for (String table : tables) {
                    System.out.println("Cleaning table: " + table);
                    try (Statement stmt = mysqlConn.createStatement()) {
                        stmt.execute("TRUNCATE TABLE " + table);
                    } catch (Exception e) {
                        System.out.println("  Skip truncate: " + e.getMessage());
                    }
                    migrateTable(h2Conn, mysqlConn, table);
                }

                // Re-enable FK checks
                try (Statement stmt = mysqlConn.createStatement()) {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                }

                System.out.println("Migration completed successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void migrateTable(Connection source, Connection target, String tableName) throws SQLException {
        System.out.println("Migrating table: " + tableName);

        try (Statement sourceStmt = source.createStatement();
                ResultSet rs = sourceStmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
            StringBuilder values = new StringBuilder("VALUES (");

            for (int i = 1; i <= columnCount; i++) {
                sql.append("`").append(meta.getColumnName(i)).append("`");
                values.append("?");
                if (i < columnCount) {
                    sql.append(", ");
                    values.append(", ");
                }
            }
            sql.append(") ").append(values).append(")");

            try (PreparedStatement targetStmt = target.prepareStatement(sql.toString())) {
                int count = 0;
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        targetStmt.setObject(i, rs.getObject(i));
                    }
                    targetStmt.executeUpdate();
                    count++;
                }
                System.out.println("  Copied " + count + " rows.");
            }
        } catch (SQLException e) {
            System.err.println("  Error migrating table " + tableName + ": " + e.getMessage());
        }
    }
}
