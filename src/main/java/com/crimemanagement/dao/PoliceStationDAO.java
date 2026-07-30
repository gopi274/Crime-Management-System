package com.crimemanagement.dao;

import com.crimemanagement.entity.PoliceStation;
import com.crimemanagement.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoliceStationDAO {
    
    public List<PoliceStation> findAll() throws SQLException {
        String sql = "SELECT * FROM police_stations ORDER BY name";
        List<PoliceStation> stations = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stations.add(mapResultSetToStation(rs));
            }
        }
        return stations;
    }

    public PoliceStation findById(int id) throws SQLException {
        String sql = "SELECT * FROM police_stations WHERE station_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(con.nativeSQL(sql))) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToStation(rs);
            }
        }
        return null;
    }

    private PoliceStation mapResultSetToStation(ResultSet rs) throws SQLException {
        PoliceStation s = new PoliceStation();
        s.setStationId(rs.getInt("station_id"));
        s.setName(rs.getString("name"));
        s.setCity(rs.getString("city"));
        s.setState(rs.getString("state"));
        s.setMaxInspectors(rs.getInt("max_inspectors"));
        s.setMaxTotalStrength(rs.getInt("max_total_strength"));
        return s;
    }
}
