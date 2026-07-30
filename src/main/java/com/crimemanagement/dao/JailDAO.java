package com.crimemanagement.dao;

import com.crimemanagement.entity.Jail;
import com.crimemanagement.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JailDAO {
    
    public List<Jail> findAll() throws SQLException {
        String sql = "SELECT * FROM jails ORDER BY type, name";
        List<Jail> jails = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                jails.add(mapResultSetToJail(rs));
            }
        }
        return jails;
    }

    public Jail findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM jails WHERE jail_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToJail(rs);
            }
        }
        return null;
    }

    private Jail mapResultSetToJail(ResultSet rs) throws SQLException {
        Jail jail = new Jail();
        jail.setJailId(rs.getInt("jail_id"));
        jail.setName(rs.getString("name"));
        jail.setType(Jail.JailType.valueOf(rs.getString("type")));
        jail.setLocation(rs.getString("location"));
        jail.setCapacity(rs.getInt("capacity"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) jail.setCreatedAt(ts.toLocalDateTime());
        return jail;
    }
}
