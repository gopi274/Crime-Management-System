package com.crimemanagement.service;

import com.crimemanagement.dao.UserAccountDAO;
import com.crimemanagement.entity.UserAccount;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class AdminService {
    private final UserAccountDAO userAccountDAO = new UserAccountDAO();
    private final com.crimemanagement.dao.OfficerDAO officerDAO = new com.crimemanagement.dao.OfficerDAO();

    public List<UserAccount> getAllUsers() throws SQLException {
        return userAccountDAO.findAll();
    }

    public UserAccount createUser(UserAccount user) throws SQLException {
        // Validate username uniqueness
        UserAccount existing = userAccountDAO.findByUsername(user.getUsername());
        if (existing != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Hash password if not already hashed
        String hash = user.getPasswordHash();
        if (hash != null && !(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))) {
            user.setPasswordHash(BCrypt.hashpw(hash, BCrypt.gensalt()));
        }


        return userAccountDAO.create(user);
    }

    public UserAccount createOfficerUser(UserAccount user, com.crimemanagement.entity.Officer officer) throws SQLException {
        // Enforce personnel capacity checks
        if (officer.getStationId() != null) {
            com.crimemanagement.entity.PoliceStation station = new com.crimemanagement.dao.PoliceStationDAO().findById(officer.getStationId());
            if (station != null) {
                java.util.List<com.crimemanagement.entity.Officer> members = officerDAO.findByStationId(officer.getStationId());
                if (members.size() >= station.getMaxTotalStrength()) {
                    throw new IllegalArgumentException("this particular station is of full strength");
                }
                if ("INSPECTOR".equals(officer.getRank())) {
                    long inspectorCount = members.stream().filter(m -> "INSPECTOR".equals(m.getRank())).count();
                    if (inspectorCount >= station.getMaxInspectors()) {
                        throw new IllegalArgumentException("this particular station already has maximum sanctioned Inspectors");
                    }
                }
            }
        }

        // Create officer first to get ID
        com.crimemanagement.entity.Officer createdOfficer = officerDAO.create(officer);
        user.setOfficerId(createdOfficer.getOfficerId());
        
        // Hash password
        String hash = user.getPasswordHash();
        if (hash != null && !(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))) {
            user.setPasswordHash(BCrypt.hashpw(hash, BCrypt.gensalt()));
        }
        
        return userAccountDAO.create(user);
    }

    public UserAccount updateUser(UserAccount user) throws SQLException {
        // Validate user exists
        UserAccount existing = userAccountDAO.findById(user.getUserId());
        if (existing == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Validate username uniqueness if changed
        if (!existing.getUsername().equals(user.getUsername())) {
            UserAccount usernameExists = userAccountDAO.findByUsername(user.getUsername());
            if (usernameExists != null) {
                throw new IllegalArgumentException("Username already exists");
            }
        }

        // Hash password if provided and not already hashed
        String hash = user.getPasswordHash();
        if (hash != null && !hash.isEmpty()
                && !(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))) {
            user.setPasswordHash(BCrypt.hashpw(hash, BCrypt.gensalt()));
        } else if (hash == null || hash.isEmpty()) {
            // Keep existing password if not provided
            user.setPasswordHash(existing.getPasswordHash());
        }

        return userAccountDAO.update(user);
    }

    public boolean deleteUser(Integer userId) throws SQLException {
        // Prevent deleting the last admin
        UserAccount user = userAccountDAO.findById(userId);
        if (user != null && "ADMIN".equals(user.getRole())) {
            List<UserAccount> admins = userAccountDAO.findAll().stream()
                    .filter(u -> "ADMIN".equals(u.getRole()))
                    .toList();
            if (admins.size() <= 1) {
                throw new IllegalArgumentException("Cannot delete the last admin user");
            }
        }
        return userAccountDAO.deleteById(userId);
    }

    public UserAccount getUserById(Integer userId) throws SQLException {
        return userAccountDAO.findById(userId);
    }
}
