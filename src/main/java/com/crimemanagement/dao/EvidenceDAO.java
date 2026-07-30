package com.crimemanagement.dao;

import com.crimemanagement.entity.Evidence;
import com.crimemanagement.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EvidenceDAO {

    /**
     * V6.2.0: Automated District Forensic Science Laboratory (DFSL) Routing
     * Maps Kerala districts to their respective DFSL Lab IDs.
     */
    public Integer getAutoRoutedLabId(String location) throws SQLException {
        if (location == null || location.isEmpty()) return null;
        
        String loc = location.toUpperCase();
        String labName = null;
        
        if (loc.contains("THIRUVANANTHAPURAM") || loc.contains("TRIVANDRUM")) labName = "DFSL TVM";
        else if (loc.contains("KOLLAM") || loc.contains("QUILON")) labName = "DFSL KLM";
        else if (loc.contains("PATHANAMTHITTA")) labName = "DFSL PTA";
        else if (loc.contains("ALAPPUZHA") || loc.contains("ALLEPPEY")) labName = "DFSL ALP";
        else if (loc.contains("KOTTAYAM")) labName = "DFSL KTM";
        else if (loc.contains("IDUKKI")) labName = "DFSL IDK";
        else if (loc.contains("ERNAKULAM") || loc.contains("KOCHI")) labName = "DFSL EKM";
        else if (loc.contains("THRISSUR") || loc.contains("TRICHUR")) labName = "DFSL TCR";
        else if (loc.contains("PALAKKAD") || loc.contains("PALGHAT")) labName = "DFSL PKD";
        else if (loc.contains("MALAPPURAM")) labName = "DFSL MLP";
        else if (loc.contains("KOZHIKODE") || loc.contains("CALICUT")) labName = "DFSL KOZ";
        else if (loc.contains("WAYANAD")) labName = "DFSL WAY";
        else if (loc.contains("KANNUR") || loc.contains("CANANORE")) labName = "DFSL KNR";
        else if (loc.contains("KASARAGOD")) labName = "DFSL KSD";

        if (labName != null) {
            String sql = "SELECT lab_id FROM forensic_labs WHERE name LIKE ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + labName + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("lab_id");
                }
            }
        }
        return null;
    }

    public List<Evidence> findByFirId(int firId) throws SQLException {
        String sql = "SELECT * FROM evidence WHERE fir_id = ? ORDER BY collected_date DESC";
        List<Evidence> evidenceList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, firId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    evidenceList.add(mapResultSetToEvidence(rs));
                }
            }
        }
        return evidenceList;
    }

    public List<Evidence> findAll() throws SQLException {
        String sql = "SELECT * FROM evidence ORDER BY collected_date DESC";
        List<Evidence> evidenceList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                evidenceList.add(mapResultSetToEvidence(rs));
            }
        }
        return evidenceList;
    }

    public void insert(Evidence e) throws SQLException {
        String sql = "INSERT INTO evidence (fir_id, evidence_type, description, location_found, collected_by, collected_date, storage_location, status, media_type, media_url, lab_id, scientist_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getFirId());
            ps.setString(2, e.getEvidenceType());
            ps.setString(3, e.getDescription());
            ps.setString(4, e.getLocationFound());
            if (e.getCollectedBy() != null) ps.setInt(5, e.getCollectedBy()); else ps.setNull(5, Types.INTEGER);
            if (e.getCollectedDate() != null) ps.setTimestamp(6, Timestamp.valueOf(e.getCollectedDate())); else ps.setNull(6, Types.TIMESTAMP);
            ps.setString(7, e.getStorageLocation());
            ps.setString(8, e.getStatus().name());
            ps.setString(9, e.getMediaType());
            ps.setString(10, e.getMediaUrl());
            if (e.getLabId() != null) ps.setInt(11, e.getLabId()); else ps.setNull(11, Types.INTEGER);
            if (e.getScientistId() != null) ps.setInt(12, e.getScientistId()); else ps.setNull(12, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setEvidenceId(keys.getInt(1));
            }
        }
    }

    public void assignToLab(int evidenceId, int labId) throws SQLException {
        String sql = "UPDATE evidence SET lab_id = ?, status = 'LAB_PROCESS' WHERE evidence_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, labId);
            ps.setInt(2, evidenceId);
            ps.executeUpdate();
        }
    }

    public void escalateToHigherFacility(int evidenceId, int currentLabId) throws SQLException {
        // Logic to find parent lab (RFSL for DFSL, SFSL for RFSL)
        String findParentSql = "SELECT parent_lab_id FROM forensic_labs WHERE lab_id = ?";
        String updateSql = "UPDATE evidence SET lab_id = ?, status = 'SFSL_ESCALATION' WHERE evidence_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psFind = conn.prepareStatement(findParentSql)) {
            psFind.setInt(1, currentLabId);
            try (ResultSet rs = psFind.executeQuery()) {
                if (rs.next()) {
                    int parentLabId = rs.getInt("parent_lab_id");
                    if (parentLabId > 0) {
                        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                            psUpdate.setInt(1, parentLabId);
                            psUpdate.setInt(2, evidenceId);
                            psUpdate.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    private Evidence mapResultSetToEvidence(ResultSet rs) throws SQLException {
        Evidence e = new Evidence();
        e.setEvidenceId(rs.getInt("evidence_id"));
        e.setFirId(rs.getInt("fir_id"));
        e.setEvidenceType(rs.getString("evidence_type"));
        e.setDescription(rs.getString("description"));
        e.setLocationFound(rs.getString("location_found"));
        e.setCollectedBy(rs.getInt("collected_by"));
        Timestamp date = rs.getTimestamp("collected_date");
        if (date != null) e.setCollectedDate(date.toLocalDateTime());
        e.setStorageLocation(rs.getString("storage_location"));
        e.setStatus(Evidence.EvidenceStatus.valueOf(rs.getString("status")));
        e.setMediaType(rs.getString("media_type"));
        e.setMediaUrl(rs.getString("media_url"));
        int labId = rs.getInt("lab_id");
        if (!rs.wasNull()) e.setLabId(labId);
        int scientistId = rs.getInt("scientist_id");
        if (!rs.wasNull()) e.setScientistId(scientistId);
        return e;
    }

    public List<Evidence> findByCourt(Integer courtId) throws SQLException {
        if (courtId == null) return findAll();
        String sql = "SELECT e.* FROM evidence e JOIN firs f ON e.fir_id = f.fir_id JOIN cases c ON f.fir_id = c.fir_id WHERE c.court_id = ? ORDER BY e.collected_date DESC";
        List<Evidence> evidenceList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courtId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    evidenceList.add(mapResultSetToEvidence(rs));
                }
            }
        }
        return evidenceList;
    }

    public List<Evidence> searchByKeyword(String keyword) throws SQLException {
        String sql = "SELECT * FROM evidence WHERE evidence_type LIKE ? OR description LIKE ? OR location_found LIKE ? ORDER BY collected_date DESC";
        List<Evidence> evidenceList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    evidenceList.add(mapResultSetToEvidence(rs));
                }
            }
        }
        return evidenceList;
    }
}
