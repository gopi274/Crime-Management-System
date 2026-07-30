package com.crimemanagement.dao;

import com.crimemanagement.entity.Suspect;
import com.crimemanagement.entity.Criminal;
import com.crimemanagement.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO implementation for Suspect entity
 */
public class SuspectDAO implements BaseDAO<Suspect, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(SuspectDAO.class);

    @Override
    public Suspect create(Suspect suspect) throws SQLException {
        String sql = "INSERT INTO suspects (case_id, criminal_id, role, charge, disposition, arrest_date, bail_amount, bail_status, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, suspect.getCaseId());
            statement.setInt(2, suspect.getCriminalId());
            statement.setString(3, suspect.getRole());
            statement.setString(4, suspect.getCharge());
            statement.setString(5, suspect.getDisposition());
            statement.setDate(6, suspect.getArrestDate() != null ? Date.valueOf(suspect.getArrestDate()) : null);
            statement.setBigDecimal(7, suspect.getBailAmount());
            statement.setString(8, suspect.getBailStatus());
            statement.setString(9, suspect.getGender());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating suspect failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    suspect.setSuspectId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating suspect failed, no ID obtained.");
                }
            }
            
            logger.info("Suspect created with ID: {}", suspect.getSuspectId());
            return suspect;
        }
    }

    @Override
    public Suspect findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM suspects WHERE suspect_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToSuspect(resultSet);
                }
            }
        }
        return null;
    }

    @Override
    public List<Suspect> findAll() throws SQLException {
        String sql = "SELECT * FROM suspects ORDER BY created_at DESC";
        List<Suspect> suspects = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                suspects.add(mapResultSetToSuspect(resultSet));
            }
        }
        return suspects;
    }

    @Override
    public Suspect update(Suspect suspect) throws SQLException {
        String sql = "UPDATE suspects SET case_id = ?, criminal_id = ?, role = ?, charge = ?, disposition = ?, arrest_date = ?, bail_amount = ?, bail_status = ?, gender = ? WHERE suspect_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, suspect.getCaseId());
            statement.setInt(2, suspect.getCriminalId());
            statement.setString(3, suspect.getRole());
            statement.setString(4, suspect.getCharge());
            statement.setString(5, suspect.getDisposition());
            statement.setDate(6, suspect.getArrestDate() != null ? Date.valueOf(suspect.getArrestDate()) : null);
            statement.setBigDecimal(7, suspect.getBailAmount());
            statement.setString(8, suspect.getBailStatus());
            statement.setString(9, suspect.getGender());
            statement.setInt(10, suspect.getSuspectId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating suspect failed, no rows affected.");
            }
            
            logger.info("Suspect updated with ID: {}", suspect.getSuspectId());
            return suspect;
        }
    }

    @Override
    public boolean deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM suspects WHERE suspect_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Suspect deleted with ID: {}", id);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM suspects WHERE suspect_id = ?";
        
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
        String sql = "SELECT COUNT(*) FROM suspects";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0;
        }
    }

    /**
     * Find suspects by case ID
     * @param caseId The case ID
     * @return List of suspects for the case
     * @throws SQLException if database error occurs
     */
    public List<Suspect> findByCaseId(Integer caseId) throws SQLException {
        String sql = "SELECT * FROM suspects WHERE case_id = ? ORDER BY created_at DESC";
        List<Suspect> suspects = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, caseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suspects.add(mapResultSetToSuspect(resultSet));
                }
            }
        }
        return suspects;
    }

    /**
     * Find suspects by criminal ID
     * @param criminalId The criminal ID
     * @return List of suspects for the criminal
     * @throws SQLException if database error occurs
     */
    public List<Suspect> findByCriminalId(Integer criminalId) throws SQLException {
        String sql = "SELECT * FROM suspects WHERE criminal_id = ? ORDER BY created_at DESC";
        List<Suspect> suspects = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, criminalId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suspects.add(mapResultSetToSuspect(resultSet));
                }
            }
        }
        return suspects;
    }

    /**
     * Find suspects by role
     * @param role The role
     * @return List of suspects with the specified role
     * @throws SQLException if database error occurs
     */
    public List<Suspect> findByRole(String role) throws SQLException {
        String sql = "SELECT * FROM suspects WHERE role = ? ORDER BY created_at DESC";
        List<Suspect> suspects = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, role);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suspects.add(mapResultSetToSuspect(resultSet));
                }
            }
        }
        return suspects;
    }

    /**
     * Find suspects by bail status
     * @param bailStatus The bail status
     * @return List of suspects with the specified bail status
     * @throws SQLException if database error occurs
     */
    public List<Suspect> findByBailStatus(String bailStatus) throws SQLException {
        String sql = "SELECT * FROM suspects WHERE bail_status = ? ORDER BY created_at DESC";
        List<Suspect> suspects = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, bailStatus);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suspects.add(mapResultSetToSuspect(resultSet));
                }
            }
        }
        return suspects;
    }

    /**
     * Check if a criminal is already a suspect in a case
     * @param caseId The case ID
     * @param criminalId The criminal ID
     * @return true if already a suspect, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean isCriminalSuspectInCase(Integer caseId, Integer criminalId) throws SQLException {
        String sql = "SELECT 1 FROM suspects WHERE case_id = ? AND criminal_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, caseId);
            statement.setInt(2, criminalId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Map ResultSet to Suspect object
     * @param resultSet The ResultSet
     * @return Suspect object
     * @throws SQLException if database error occurs
     */
    private Suspect mapResultSetToSuspect(ResultSet resultSet) throws SQLException {
        Suspect suspect = new Suspect();
        suspect.setSuspectId(resultSet.getInt("suspect_id"));
        suspect.setCaseId(resultSet.getInt("case_id"));
        suspect.setCriminalId(resultSet.getInt("criminal_id"));
        suspect.setRole(resultSet.getString("role"));
        suspect.setCharge(resultSet.getString("charge"));
        
        Date arrestDate = resultSet.getDate("arrest_date");
        if (arrestDate != null) {
            suspect.setArrestDate(arrestDate.toLocalDate());
        }
        
        BigDecimal bailAmount = resultSet.getBigDecimal("bail_amount");
        suspect.setBailAmount(bailAmount);
        
        suspect.setBailStatus(resultSet.getString("bail_status"));
        suspect.setDisposition(resultSet.getString("disposition"));
        suspect.setGender(resultSet.getString("gender"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            suspect.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return suspect;
    }
    
    public void addCaseSuspect(Suspect suspect) throws SQLException {
        String sql = "INSERT INTO suspects (case_id, criminal_id, role, arrest_date, bail_amount, bail_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, suspect.getCaseId());
            ps.setInt(2, suspect.getCriminalId());
            ps.setString(3, suspect.getRole());

            // Convert LocalDate to java.sql.Date
            if (suspect.getArrestDate() != null) {
                ps.setDate(4, Date.valueOf(suspect.getArrestDate()));
            } else {
                ps.setDate(4, null);
            }

            // Convert Double to BigDecimal for SQL DECIMAL
            if (suspect.getBailAmount() != null) {
                ps.setBigDecimal(5,suspect.getBailAmount());
            } else {
                ps.setBigDecimal(5, null);
            }

            ps.setString(6, suspect.getBailStatus());

            ps.executeUpdate();
        }
    }

    public void removeCaseSuspect(int suspectId) throws SQLException {
        String sql = "DELETE FROM suspects WHERE suspect_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, suspectId);
            ps.executeUpdate();
        }
    }

    public Criminal findPrimarySuspectForCase(int caseId) throws SQLException {
        String sql = "SELECT c.* FROM criminals c JOIN suspects s ON c.criminal_id = s.criminal_id " +
                     "WHERE s.case_id = ? AND s.role = 'Primary Suspect' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Criminal c = new Criminal();
                    c.setCriminalId(rs.getInt("criminal_id"));
                    c.setFirstName(rs.getString("first_name"));
                    c.setLastName(rs.getString("last_name"));
                    return c;
                }
            }
        }
        return null;
    }

    public void updateDisposition(int suspectId, String disposition) throws SQLException {
        String sql = "UPDATE suspects SET disposition = ? WHERE suspect_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, disposition);
            ps.setInt(2, suspectId);
            ps.executeUpdate();
            
            if ("ACQUITTED".equals(disposition)) {
                // Trigger release logic - find if in prison
                triggerPrisonRelease(suspectId);
            }
        }
    }

    private void triggerPrisonRelease(int suspectId) {
        String sql = "UPDATE jail_inmates SET status = 'RELEASED_BY_COURT' " +
                     "WHERE criminal_id = (SELECT criminal_id FROM suspects WHERE suspect_id = ?) " +
                     "AND status = 'IN_CUSTODY'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, suspectId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("V6.2.0 ACQUITTAL: Inmate release triggered for suspect ID: {}", suspectId);
            }
        } catch (SQLException e) {
            logger.warn("Acquittal release trigger failed for suspect {}: {}", suspectId, e.getMessage());
        }
    }
    public List<Suspect> findByCourt(Integer courtId) throws SQLException {
        if (courtId == null) return findAll();
        String sql = "SELECT s.* FROM suspects s JOIN cases c ON s.case_id = c.case_id WHERE c.court_id = ? ORDER BY s.created_at DESC";
        List<Suspect> suspects = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, courtId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suspects.add(mapResultSetToSuspect(resultSet));
                }
            }
        }
        return suspects;
    }
}
