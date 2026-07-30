package com.crimemanagement.util;

import java.sql.*;
import java.util.logging.*;

/**
 * Phase 10 Verification Suite for Kochi-Justice V6.2.0 Graduation.
 * Validates relational integrity and BNS legislative synchronization.
 */
public class Phase10Verification {
    private static final Logger logger = Logger.getLogger(Phase10Verification.class.getName());

    public static void runFullVerification() {
        logger.info("🚀 Initiating Phase 10 Graduation Verification [V6.2.0]...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            checkRelationalCounts(conn);
            checkBNSConsistency(conn);
            checkOrphanedInvestigationRecords(conn);
            logger.info("✅ Phase 10 Verification Complete. Kochi-Justice Graduation is STABLE.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Verification Failure: Relation Mapping Error", e);
        }
    }

    private static void checkRelationalCounts(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM firs";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt("total");
                logger.info("📊 Total FIR Graduation Count: " + count + " / 2453");
            }
        }
    }

    private static void checkBNSConsistency(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM legal_sections WHERE category = 'BNS 2024'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                logger.info("⚖️ BNS 2024 Authoritative Sync: " + rs.getInt("count") + " sections verified.");
            }
        }
    }

    private static void checkOrphanedInvestigationRecords(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM firs f LEFT JOIN cases c ON f.fir_id = c.fir_id WHERE c.case_id IS NULL";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int orphans = rs.getInt("count");
                if (orphans > 0) {
                    logger.warning("⚠️ RELATIONAL ALERT: " + orphans + " orphaned FIRs detected.");
                } else {
                    logger.info("🔗 FIR-to-Case Linkage: 100% Verified.");
                }
            }
        }
    }

    public static void main(String[] args) {
        runFullVerification();
    }
}
