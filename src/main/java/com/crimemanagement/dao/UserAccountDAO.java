package com.crimemanagement.dao;

import com.crimemanagement.entity.UserAccount;
import com.crimemanagement.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO implementation for UserAccount entity.
 * Cleaned up unused logger for Kochi-Justice production release.
 */
@Repository
public class UserAccountDAO {

    public UserAccount findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, officer_id, jail_id, court_id, scientist_id FROM users WHERE username = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public UserAccount create(UserAccount user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, officer_id, jail_id, court_id, scientist_id) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            
            if (user.getOfficerId() != null) ps.setInt(4, user.getOfficerId());
            else ps.setNull(4, Types.INTEGER);
            
            if (user.getJailId() != null) ps.setInt(5, user.getJailId());
            else ps.setNull(5, Types.INTEGER);
            
            if (user.getCourtId() != null) ps.setInt(6, user.getCourtId());
            else ps.setNull(6, Types.INTEGER);
            
            if (user.getScientistId() != null) ps.setInt(7, user.getScientistId());
            else ps.setNull(7, Types.INTEGER);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) user.setUserId(rs.getInt(1));
            }
            return user;
        }
    }

    public UserAccount findById(Integer userId) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, officer_id, jail_id, court_id, scientist_id FROM users WHERE user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public List<UserAccount> findAll() throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, officer_id, jail_id, court_id, scientist_id FROM users ORDER BY username";
        List<UserAccount> users = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    public UserAccount update(UserAccount user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password_hash = ?, role = ?, officer_id = ?, jail_id = ?, court_id = ?, scientist_id = ? WHERE user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            
            if (user.getOfficerId() != null) ps.setInt(4, user.getOfficerId());
            else ps.setNull(4, Types.INTEGER);
            
            if (user.getJailId() != null) ps.setInt(5, user.getJailId());
            else ps.setNull(5, Types.INTEGER);
            
            if (user.getCourtId() != null) ps.setInt(6, user.getCourtId());
            else ps.setNull(6, Types.INTEGER);
            
            if (user.getScientistId() != null) ps.setInt(7, user.getScientistId());
            else ps.setNull(7, Types.INTEGER);
            
            ps.setInt(8, user.getUserId());
            ps.executeUpdate();
            return user;
        }
    }

    public boolean deleteById(Integer userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<UserAccount> findByRole(String role) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, officer_id, jail_id, court_id, scientist_id FROM users WHERE role = ? ORDER BY username";
        List<UserAccount> users = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    private UserAccount mapResultSetToUser(ResultSet rs) throws SQLException {
        UserAccount u = new UserAccount();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        
        int oid = rs.getInt("officer_id");
        if (!rs.wasNull()) u.setOfficerId(oid);
        
        int jid = rs.getInt("jail_id");
        if (!rs.wasNull()) u.setJailId(jid);
        
        int cid = rs.getInt("court_id");
        if (!rs.wasNull()) u.setCourtId(cid);

        int scid = rs.getInt("scientist_id");
        if (!rs.wasNull()) u.setScientistId(scid);
        
        return u;
    }
}
