package com.crimemanagement.dao;

import com.crimemanagement.entity.Officer;
import com.crimemanagement.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OfficerDAO implements BaseDAO<Officer, Integer> {
    @Override
    public Officer create(Officer officer) throws SQLException {
        String sql = "INSERT INTO officers (badge_number, first_name, last_name, `rank`, station_id, jurisdiction_level, phone_number, email, is_on_duty, salary, leave_balance, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, officer.getBadgeNumber());
            statement.setString(2, officer.getFirstName());
            statement.setString(3, officer.getLastName());
            statement.setString(4, officer.getRank());
            if (officer.getStationId() != null) statement.setInt(5, officer.getStationId());
            else statement.setNull(5, Types.INTEGER);
            statement.setString(6, officer.getJurisdictionLevel());
            statement.setString(7, officer.getPhoneNumber());
            statement.setString(8, officer.getEmail());
            statement.setBoolean(9, officer.getIsOnDuty() != null ? officer.getIsOnDuty() : false);
            statement.setDouble(10, officer.getSalary() != null ? officer.getSalary() : 0.0);
            statement.setInt(11, officer.getLeaveBalance() != null ? officer.getLeaveBalance() : 30);
            statement.setString(12, officer.getGender());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating officer failed, no rows affected.");

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) officer.setOfficerId(generatedKeys.getInt(1));
            }
            return officer;
        }
    }

    @Override
    public Officer findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM officers WHERE officer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapResultSetToOfficer(resultSet);
            }
        }
        return null;
    }

    @Override
    public List<Officer> findAll() throws SQLException {
        String sql = "SELECT * FROM officers ORDER BY first_name, last_name";
        List<Officer> officers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) officers.add(mapResultSetToOfficer(resultSet));
        }
        return officers;
    }

    @Override
    public Officer update(Officer officer) throws SQLException {
        String sql = "UPDATE officers SET badge_number = ?, first_name = ?, last_name = ?, `rank` = ?, station_id = ?, jurisdiction_level = ?, phone_number = ?, email = ?, is_on_duty = ?, salary = ?, leave_balance = ?, gender = ? WHERE officer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, officer.getBadgeNumber());
            statement.setString(2, officer.getFirstName());
            statement.setString(3, officer.getLastName());
            statement.setString(4, officer.getRank());
            if (officer.getStationId() != null) statement.setInt(5, officer.getStationId());
            else statement.setNull(5, Types.INTEGER);
            statement.setString(6, officer.getJurisdictionLevel());
            statement.setString(7, officer.getPhoneNumber());
            statement.setString(8, officer.getEmail());
            statement.setBoolean(9, officer.getIsOnDuty() != null ? officer.getIsOnDuty() : false);
            statement.setDouble(10, officer.getSalary() != null ? officer.getSalary() : 0.0);
            statement.setInt(11, officer.getLeaveBalance() != null ? officer.getLeaveBalance() : 30);
            statement.setString(12, officer.getGender());
            statement.setInt(13, officer.getOfficerId());

            statement.executeUpdate();
            return officer;
        }
    }

    @Override
    public boolean deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM officers WHERE officer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM officers WHERE officer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM officers";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) return resultSet.getLong(1);
            return 0;
        }
    }

    public long countOnDuty() throws SQLException {
        String sql = "SELECT COUNT(*) FROM officers WHERE is_on_duty = TRUE";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) return resultSet.getLong(1);
            return 0;
        }
    }

    public Officer findByBadgeNumber(String badgeNumber) throws SQLException {
        String sql = "SELECT * FROM officers WHERE badge_number = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, badgeNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapResultSetToOfficer(resultSet);
            }
        }
        return null;
    }

    public List<Officer> findByStationId(Integer stationId) throws SQLException {
        String sql = "SELECT * FROM officers WHERE station_id = ?";
        List<Officer> officers = new java.util.ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            if (stationId != null) {
                statement.setInt(1, stationId);
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    officers.add(mapResultSetToOfficer(resultSet));
                }
            }
        }
        return officers;
    }

    public String getAssignedFIRNumbers(int officerId) throws SQLException {
        String sql = "SELECT fir_number FROM firs WHERE assigned_officer_id = ? AND status != 'Closed'";
        List<String> casesArr = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, officerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    casesArr.add(rs.getString("fir_number"));
                }
            }
        }
        return casesArr.isEmpty() ? "None" : String.join(", ", casesArr);
    }

    public List<Officer> findByJurisdiction(Officer requester) throws SQLException {
        if (requester == null || "DGP".equals(requester.getJurisdictionLevel())) {
            return findAll();
        }

        String level = requester.getJurisdictionLevel();
        if ("STATION_OFFICER".equals(level)) {
            return findByStationId(requester.getStationId());
        }

        // For CIRCLE, DISTRICT, COMMISSIONER, filter by City of the requester's station
        String sql = "SELECT o.* FROM officers o " +
                     "JOIN police_stations s ON o.station_id = s.station_id " +
                     "WHERE s.city = (SELECT city FROM police_stations WHERE station_id = ?)";
        
        List<Officer> officers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (requester.getStationId() != null) {
                ps.setInt(1, requester.getStationId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) officers.add(mapResultSetToOfficer(rs));
                }
            }
        }
        return officers;
    }

    private Officer mapResultSetToOfficer(ResultSet resultSet) throws SQLException {
        Officer officer = new Officer();
        officer.setOfficerId(resultSet.getInt("officer_id"));
        officer.setBadgeNumber(resultSet.getString("badge_number"));
        officer.setFirstName(resultSet.getString("first_name"));
        officer.setLastName(resultSet.getString("last_name"));
        officer.setRank(resultSet.getString("rank"));
        officer.setStationId((Integer) resultSet.getObject("station_id"));
        officer.setJurisdictionLevel(resultSet.getString("jurisdiction_level"));
        officer.setPhoneNumber(resultSet.getString("phone_number"));
        officer.setEmail(resultSet.getString("email"));
        officer.setIsOnDuty(resultSet.getBoolean("is_on_duty"));
        officer.setGender(resultSet.getString("gender"));
        officer.setSalary(resultSet.getDouble("salary"));
        officer.setLeaveBalance(resultSet.getInt("leave_balance"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) officer.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) officer.setUpdatedAt(updatedAt.toLocalDateTime());
        return officer;
    }
}
