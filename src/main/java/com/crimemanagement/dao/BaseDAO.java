package com.crimemanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Base DAO interface providing common database operations
 * @param <T> The entity type
 * @param <K> The primary key type
 */
public interface BaseDAO<T, K> {
    
    /**
     * Create a new record
     * @param entity The entity to create
     * @return The created entity with generated ID
     * @throws SQLException if database error occurs
     */
    T create(T entity) throws SQLException;
    
    /**
     * Find entity by primary key
     * @param id The primary key
     * @return The entity if found, null otherwise
     * @throws SQLException if database error occurs
     */
    T findById(K id) throws SQLException;
    
    /**
     * Find all entities
     * @return List of all entities
     * @throws SQLException if database error occurs
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Update an existing entity
     * @param entity The entity to update
     * @return The updated entity
     * @throws SQLException if database error occurs
     */
    T update(T entity) throws SQLException;
    
    /**
     * Delete entity by primary key
     * @param id The primary key
     * @return true if deleted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean deleteById(K id) throws SQLException;
    
    /**
     * Check if entity exists by primary key
     * @param id The primary key
     * @return true if exists, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean existsById(K id) throws SQLException;
    
    /**
     * Get total count of entities
     * @return Total count
     * @throws SQLException if database error occurs
     */
    long count() throws SQLException;
    
    /**
     * Close resources
     * @param connection The database connection
     * @param statement The prepared statement
     * @param resultSet The result set
     */
    default void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) resultSet.close();
        } catch (SQLException e) {
            // Log error but don't throw
        }
        try {
            if (statement != null) statement.close();
        } catch (SQLException e) {
            // Log error but don't throw
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            // Log error but don't throw
        }
    }
}
