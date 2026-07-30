package com.crimemanagement.dao;

import com.crimemanagement.entity.Court;
import com.crimemanagement.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CourtDAO {

    public List<Court> findAll() throws SQLException {
        String sql = "SELECT * FROM courts ORDER BY name";
        List<Court> courts = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courts.add(new Court(
                    rs.getInt("court_id"),
                    rs.getString("name"),
                    rs.getString("city"),
                    rs.getString("state"),
                    rs.getString("jurisdiction_level")
                ));
            }
        }
        return courts;
    }

    public Court findById(int id) throws SQLException {
        String sql = "SELECT * FROM courts WHERE court_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Court(
                        rs.getInt("court_id"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("jurisdiction_level")
                    );
                }
            }
        }
        return null;
    }
}
