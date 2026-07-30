package com.crimemanagement.dao;

import com.crimemanagement.entity.JailInmate;
import com.crimemanagement.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JailInmateDAO {

    public List<JailInmate> findByJail(int jailId) throws SQLException {
        String sql = "SELECT * FROM jail_inmates WHERE jail_id = ? AND status = 'ACTIVE' ORDER BY admission_date DESC";
        List<JailInmate> inmates = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jailId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    inmates.add(mapResultSetToInmate(rs));
                }
            }
        }
        return inmates;
    }

    public void insert(JailInmate inmate) throws SQLException {
        String sql = "INSERT INTO jail_inmates (criminal_id, jail_id, case_id, admission_date, sentence_period_months, release_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inmate.getCriminalId());
            ps.setInt(2, inmate.getJailId());
            ps.setInt(3, inmate.getCaseId());
            ps.setDate(4, Date.valueOf(inmate.getEntryDate()));
            try {
                ps.setInt(5, Integer.parseInt(inmate.getSentenceDuration().split(" ")[0]));
            } catch (Exception e) {
                ps.setInt(5, 12);
            }
            if (inmate.getReleaseDate() != null) ps.setDate(6, Date.valueOf(inmate.getReleaseDate())); else ps.setNull(6, Types.DATE);
            ps.setString(7, inmate.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) inmate.setInmateId(keys.getInt(1));
            }
        }
    }

    private JailInmate mapResultSetToInmate(ResultSet rs) throws SQLException {
        JailInmate i = new JailInmate();
        i.setInmateId(rs.getInt("inmate_id"));
        i.setCriminalId(rs.getInt("criminal_id"));
        i.setJailId(rs.getInt("jail_id"));
        i.setCaseId(rs.getInt("case_id"));
        i.setEntryDate(rs.getDate("admission_date").toLocalDate());
        i.setSentenceDuration(rs.getInt("sentence_period_months") + " months");
        Date relDate = rs.getDate("release_date");
        if (relDate != null) i.setReleaseDate(relDate.toLocalDate());
        i.setStatus(JailInmate.InmateStatus.valueOf(rs.getString("status")));
        return i;
    }
}
