package com.crimemanagement.dao;

import com.crimemanagement.entity.Criminal;
import com.crimemanagement.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.sql.Date;

/**
 * DAO implementation for Criminal entity
 */
@Repository
public class CriminalDAO implements BaseDAO<Criminal, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CriminalDAO.class);

    @Override
    public Criminal create(Criminal criminal) throws SQLException {
        String sql = "INSERT INTO criminals (first_name, last_name, alias, date_of_birth, gender, address, phone_number, criminal_record) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, criminal.getFirstName());
            statement.setString(2, criminal.getLastName());
            statement.setString(3, criminal.getAlias());
            statement.setDate(4, criminal.getDateOfBirth() != null ? Date.valueOf(criminal.getDateOfBirth()) : null);
            statement.setString(5, criminal.getGender());
            statement.setString(6, criminal.getAddress());
            statement.setString(7, criminal.getPhoneNumber());
            statement.setString(8, criminal.getCriminalRecord());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating criminal failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    criminal.setCriminalId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating criminal failed, no ID obtained.");
                }
            }

            logger.info("Criminal created with ID: {}", criminal.getCriminalId());
            return criminal;
        }
    }

    @Override
    public Criminal findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM criminals WHERE criminal_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCriminal(resultSet);
                }
            }
        }
        return null;
    }

    @Override
    public List<Criminal> findAll() throws SQLException {
        String sql = "SELECT * FROM criminals ORDER BY first_name, last_name";
        List<Criminal> criminals = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                criminals.add(mapResultSetToCriminal(resultSet));
            }
        }
        return criminals;
    }

    @Override
    public Criminal update(Criminal criminal) throws SQLException {
        String sql = "UPDATE criminals SET first_name = ?, last_name = ?, alias = ?, date_of_birth = ?, gender = ?, address = ?, phone_number = ?, criminal_record = ? WHERE criminal_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, criminal.getFirstName());
            statement.setString(2, criminal.getLastName());
            statement.setString(3, criminal.getAlias());
            statement.setDate(4, criminal.getDateOfBirth() != null ? Date.valueOf(criminal.getDateOfBirth()) : null);
            statement.setString(5, criminal.getGender());
            statement.setString(6, criminal.getAddress());
            statement.setString(7, criminal.getPhoneNumber());
            statement.setString(8, criminal.getCriminalRecord());
            statement.setInt(9, criminal.getCriminalId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating criminal failed, no rows affected.");
            }

            logger.info("Criminal updated with ID: {}", criminal.getCriminalId());
            return criminal;
        }
    }

    @Override
    public boolean deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM criminals WHERE criminal_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Criminal deleted with ID: {}", id);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM criminals WHERE criminal_id = ?";

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
        String sql = "SELECT COUNT(*) FROM criminals";

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
     * Find criminals by name (first name or last name)
     * 
     * @param name The name to search for
     * @return List of criminals matching the name
     * @throws SQLException if database error occurs
     */
    public List<Criminal> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM criminals WHERE first_name LIKE ? OR last_name LIKE ? OR alias LIKE ? ORDER BY first_name, last_name";
        List<Criminal> criminals = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchPattern = "%" + name + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    criminals.add(mapResultSetToCriminal(resultSet));
                }
            }
        }
        return criminals;
    }

    /**
     * Find criminals by gender
     * 
     * @param gender The gender to search for
     * @return List of criminals with the specified gender
     * @throws SQLException if database error occurs
     */
    public List<Criminal> findByGender(String gender) throws SQLException {
        String sql = "SELECT * FROM criminals WHERE gender = ? ORDER BY first_name, last_name";
        List<Criminal> criminals = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, gender);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    criminals.add(mapResultSetToCriminal(resultSet));
                }
            }
        }
        return criminals;
    }

    /**
     * Map ResultSet to Criminal object
     * 
     * @param resultSet The ResultSet
     * @return Criminal object
     * @throws SQLException if database error occurs
     */
    private Criminal mapResultSetToCriminal(ResultSet resultSet) throws SQLException {

        Criminal criminal = new Criminal();
        criminal.setCriminalId(resultSet.getInt("criminal_id"));
        criminal.setFirstName(resultSet.getString("first_name"));
        criminal.setLastName(resultSet.getString("last_name"));
        criminal.setAlias(resultSet.getString("alias"));

        Date dateOfBirth = resultSet.getDate("date_of_birth");
        if (dateOfBirth != null) {
            criminal.setDateOfBirth(dateOfBirth.toLocalDate());
        }

        criminal.setGender(resultSet.getString("gender"));
        criminal.setAddress(resultSet.getString("address"));
        criminal.setPhoneNumber(resultSet.getString("phone_number"));
        criminal.setCriminalRecord(resultSet.getString("criminal_record"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            criminal.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            criminal.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return criminal;
    }

    public void addCriminal(Criminal criminal) throws SQLException {
        String sql = "INSERT INTO criminals " +
                "(first_name, last_name, alias, date_of_birth, gender, address, phone_number, criminal_record) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, criminal.getFirstName());
            ps.setString(2, criminal.getLastName());
            ps.setString(3, criminal.getAlias());
            ps.setDate(4, Date.valueOf(criminal.getDateOfBirth()));
            ps.setString(5, criminal.getGender());
            ps.setString(6, criminal.getAddress());
            ps.setString(7, criminal.getPhoneNumber());
            ps.setString(8, criminal.getCriminalRecord());

            ps.executeUpdate();
        }
    }

    public boolean removeCriminal(int criminalId) throws SQLException {
        String sql = "DELETE FROM criminals WHERE criminal_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, criminalId);
            return ps.executeUpdate() > 0;
        }
    }

}
