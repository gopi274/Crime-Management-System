package com.crimemanagement.dao;

import com.crimemanagement.entity.FIR;
import com.crimemanagement.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FIRDAO implements BaseDAO<FIR, Integer> {
    @Override
    public FIR create(FIR fir) throws SQLException {
        String sql = "INSERT INTO firs (fir_number, crime_type, crime_description, location, date_of_incident, date_reported, citizen_id, status, assigned_officer_id, station_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, fir.getFirNumber());
            statement.setString(2, fir.getCrimeType());
            statement.setString(3, fir.getCrimeDescription());
            statement.setString(4, fir.getLocation());
            statement.setTimestamp(5, Timestamp.valueOf(fir.getDateOfIncident()));
            statement.setTimestamp(6, Timestamp.valueOf(fir.getDateReported()));
            statement.setObject(7, fir.getCitizenId());
            statement.setString(8, fir.getStatus());
            statement.setObject(9, fir.getAssignedOfficerId());
            statement.setObject(10, fir.getStationId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating FIR failed.");

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) fir.setFirId(generatedKeys.getInt(1));
            }
            return fir;
        }
    }

    @Override
    public FIR findById(Integer id) throws SQLException {
        String sql = "SELECT f.*, c.name as complainant_name, o.rank as orank, o.first_name as ofn, o.last_name as oln FROM firs f " +
                     "LEFT JOIN citizens c ON f.citizen_id = c.citizen_id " +
                     "LEFT JOIN officers o ON f.assigned_officer_id = o.officer_id " +
                     "WHERE f.fir_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapResultSetToFIR(resultSet);
            }
        }
        return null;
    }

    @Override
    public List<FIR> findAll() throws SQLException {
        return findAll(null);
    }

    public List<FIR> findAll(Integer stationId) throws SQLException {
        String sql = "SELECT f.*, c.name as complainant_name, o.rank as orank, o.first_name as ofn, o.last_name as oln FROM firs f " +
                     "LEFT JOIN citizens c ON f.citizen_id = c.citizen_id " +
                     "LEFT JOIN officers o ON f.assigned_officer_id = o.officer_id";
        if (stationId != null) {
            sql += " WHERE f.station_id = ?";
        }
        sql += " ORDER BY f.date_of_incident DESC";
        
        List<FIR> firs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (stationId != null) {
                statement.setInt(1, stationId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) firs.add(mapResultSetToFIR(resultSet));
            }
        }
        return firs;
    }

    @Override
    public FIR update(FIR fir) throws SQLException {
        String sql = "UPDATE firs SET fir_number = ?, crime_type = ?, crime_description = ?, location = ?, date_of_incident = ?, date_reported = ?, citizen_id = ?, status = ?, assigned_officer_id = ?, station_id = ? WHERE fir_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fir.getFirNumber());
            statement.setString(2, fir.getCrimeType());
            statement.setString(3, fir.getCrimeDescription());
            statement.setString(4, fir.getLocation());
            statement.setTimestamp(5, Timestamp.valueOf(fir.getDateOfIncident()));
            statement.setTimestamp(6, Timestamp.valueOf(fir.getDateReported()));
            statement.setObject(7, fir.getCitizenId());
            statement.setString(8, fir.getStatus());
            statement.setObject(9, fir.getAssignedOfficerId());
            statement.setObject(10, fir.getStationId());
            statement.setInt(11, fir.getFirId());

            statement.executeUpdate();
            return fir;
        }
    }

    @Override
    public boolean deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM firs WHERE fir_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM firs WHERE fir_id = ?";
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
        String sql = "SELECT COUNT(*) FROM firs";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) return resultSet.getLong(1);
            return 0;
        }
    }

    public FIR findByFirNumber(String firNumber) throws SQLException {
        String sql = "SELECT f.*, c.name as complainant_name FROM firs f " +
                     "LEFT JOIN citizens c ON f.citizen_id = c.citizen_id " +
                     "WHERE f.fir_number = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapResultSetToFIR(resultSet);
            }
        }
        return null;
    }

    public List<FIR> findByCrimeType(String crimeType) throws SQLException {
        String sql = "SELECT f.*, c.name as complainant_name FROM firs f " +
                     "LEFT JOIN citizens c ON f.citizen_id = c.citizen_id " +
                     "WHERE f.crime_type = ? ORDER BY f.date_of_incident DESC";
        List<FIR> firs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, crimeType);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) firs.add(mapResultSetToFIR(resultSet));
            }
        }
        return firs;
    }

    public List<FIR> findByStatus(String status) throws SQLException {
        String sql = "SELECT f.*, c.name as complainant_name FROM firs f " +
                     "LEFT JOIN citizens c ON f.citizen_id = c.citizen_id " +
                     "WHERE f.status = ? ORDER BY f.date_of_incident DESC";
        List<FIR> firs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) firs.add(mapResultSetToFIR(resultSet));
            }
        }
        return firs;
    }

    public List<FIR> findByAssignedOfficer(Integer officerId) throws SQLException {
        String sql = "SELECT f.*, c.name as complainant_name FROM firs f " +
                     "LEFT JOIN citizens c ON f.citizen_id = c.citizen_id " +
                     "WHERE f.assigned_officer_id = ? ORDER BY f.date_of_incident DESC";
        List<FIR> firs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, officerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) firs.add(mapResultSetToFIR(resultSet));
            }
        }
        return firs;
    }

    public List<FIR> searchByKeyword(String keyword) throws SQLException {
        String sql = "SELECT f.*, c.name as complainant_name FROM firs f " +
                     "LEFT JOIN citizens c ON f.citizen_id = c.citizen_id " +
                     "WHERE f.crime_description LIKE ? OR f.location LIKE ? OR f.crime_type LIKE ? ORDER BY f.date_of_incident DESC";
        List<FIR> firs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) firs.add(mapResultSetToFIR(resultSet));
            }
        }
        return firs;
    }

    private FIR mapResultSetToFIR(ResultSet resultSet) throws SQLException {
        FIR fir = new FIR();
        fir.setFirId(resultSet.getInt("fir_id"));
        fir.setFirNumber(resultSet.getString("fir_number"));
        fir.setCrimeType(resultSet.getString("crime_type"));
        fir.setCrimeDescription(resultSet.getString("crime_description"));
        fir.setLocation(resultSet.getString("location"));

        Timestamp dateOfIncident = resultSet.getTimestamp("date_of_incident");
        if (dateOfIncident != null) fir.setDateOfIncident(dateOfIncident.toLocalDateTime());

        Timestamp dateReported = resultSet.getTimestamp("date_reported");
        if (dateReported != null) fir.setDateReported(dateReported.toLocalDateTime());

        fir.setCitizenId((Integer) resultSet.getObject("citizen_id"));
        try {
            fir.setComplainantName(resultSet.getString("complainant_name"));
        } catch (SQLException e) {
            // Column might not be in result set if query wasn't joined
        }
        fir.setStatus(resultSet.getString("status"));
        fir.setAssignedOfficerId((Integer) resultSet.getObject("assigned_officer_id"));
        try {
            String r = resultSet.getString("orank");
            String f = resultSet.getString("ofn");
            String l = resultSet.getString("oln");
            if (r != null) fir.setAssignedOfficerName(r + " " + f + " " + l);
        } catch (Exception e) {}
        fir.setStationId((Integer) resultSet.getObject("station_id"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) fir.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) fir.setUpdatedAt(updatedAt.toLocalDateTime());

        return fir;
    }

    public boolean addFIR(FIR fir) throws SQLException {
        FIR created = create(fir);
        return created != null && created.getFirId() > 0;
    }

    public boolean removeFIR(int firId) throws SQLException {
        return deleteById(firId);
    }

    public String getHotspotLocation(Integer stationId) throws SQLException {
        String sql = "SELECT location, COUNT(*) as case_count FROM firs " +
                     (stationId != null ? "WHERE station_id = ? " : "") +
                     "GROUP BY location ORDER BY case_count DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (stationId != null) {
                statement.setInt(1, stationId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("location") + " (" + resultSet.getInt("case_count") + " cases)";
                }
            }
        }
        return "No Data Available";
    }

    public List<FIR> findByCourt(Integer courtId) throws SQLException {
        if (courtId == null) return findAll();
        String sql = "SELECT f.*, c.name as complainant_name FROM firs f " +
                     "LEFT JOIN citizens c ON f.citizen_id = c.citizen_id " +
                     "JOIN cases cs ON f.fir_id = cs.fir_id " +
                     "WHERE cs.court_id = ? ORDER BY f.date_of_incident DESC";
        List<FIR> firs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, courtId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) firs.add(mapResultSetToFIR(resultSet));
            }
        }
        return firs;
    }
}
