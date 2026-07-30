package com.crimemanagement.service;

import com.crimemanagement.dao.OfficerDAO;
import com.crimemanagement.entity.Officer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Officer business logic
 */
@Service
public class OfficerService {
    private final OfficerDAO officerDAO;

    @Autowired
    public OfficerService(OfficerDAO officerDAO) {
        this.officerDAO = officerDAO;
    }

    /**
     * No-args constructor for backward compatibility with legacy Swing UI
     */
    public OfficerService() {
        this.officerDAO = new OfficerDAO();
    }

    /**
     * Create a new officer
     * 
     * @param officer The officer to create
     * @return The created officer
     * @throws SQLException if database error occurs
     */
    public Officer createOfficer(Officer officer) throws SQLException {
        // Validate required fields
        if (officer.getBadgeNumber() == null || officer.getBadgeNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Badge number is required");
        }
        if (officer.getFirstName() == null || officer.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (officer.getLastName() == null || officer.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (officer.getRank() == null || officer.getRank().trim().isEmpty()) {
            throw new IllegalArgumentException("Rank is required");
        }
        if (officer.getStationId() == null) {
            throw new IllegalArgumentException("Station ID is required");
        }

        // Check if badge number already exists
        Officer existingOfficer = officerDAO.findByBadgeNumber(officer.getBadgeNumber());
        if (existingOfficer != null) {
            throw new IllegalArgumentException(
                    "Officer with badge number " + officer.getBadgeNumber() + " already exists");
        }

        return officerDAO.create(officer);
    }

    /**
     * Get officer by ID
     * 
     * @param id The officer ID
     * @return The officer if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public Officer getOfficerById(Integer id) throws SQLException {
        return officerDAO.findById(id);
    }

    /**
     * Get officer by badge number
     * 
     * @param badgeNumber The badge number
     * @return The officer if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public Officer getOfficerByBadgeNumber(String badgeNumber) throws SQLException {
        return officerDAO.findByBadgeNumber(badgeNumber);
    }

    /**
     * Get all officers
     * 
     * @return List of all officers
     * @throws SQLException if database error occurs
     */
    public List<Officer> getAllOfficers() throws SQLException {
        return officerDAO.findAll();
    }

    /**
     * Get officers by department
     * 
     * @param department The department name
     * @return List of officers in the department
     * @throws SQLException if database error occurs
     */
    public List<Officer> getOfficersByStation(Integer stationId) throws SQLException {
        return officerDAO.findByStationId(stationId);
    }

    /**
     * Update an officer
     * 
     * @param officer The officer to update
     * @return The updated officer
     * @throws SQLException if database error occurs
     */
    public Officer updateOfficer(Officer officer) throws SQLException {
        // Validate required fields
        if (officer.getOfficerId() <= 0) {
            throw new IllegalArgumentException("Valid officer ID is required");
        }
        if (officer.getBadgeNumber() == null || officer.getBadgeNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Badge number is required");
        }
        if (officer.getFirstName() == null || officer.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (officer.getLastName() == null || officer.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (officer.getRank() == null || officer.getRank().trim().isEmpty()) {
            throw new IllegalArgumentException("Rank is required");
        }
        if (officer.getStationId() == null) {
            throw new IllegalArgumentException("Station ID is required");
        }

        // Check if officer exists
        Officer existingOfficer = officerDAO.findById(officer.getOfficerId());
        if (existingOfficer == null) {
            throw new IllegalArgumentException("Officer with ID " + officer.getOfficerId() + " not found");
        }

        // Check if badge number is being changed and if it conflicts with another
        // officer
        if (!existingOfficer.getBadgeNumber().equals(officer.getBadgeNumber())) {
            Officer officerWithBadge = officerDAO.findByBadgeNumber(officer.getBadgeNumber());
            if (officerWithBadge != null && officerWithBadge.getOfficerId() != officer.getOfficerId()) {
                throw new IllegalArgumentException(
                        "Badge number " + officer.getBadgeNumber() + " is already assigned to another officer");
            }
        }

        return officerDAO.update(officer);
    }

    /**
     * Delete an officer
     * 
     * @param id The officer ID
     * @return true if deleted, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteOfficer(Integer id) throws SQLException {
        // Check if officer exists
        Officer existingOfficer = officerDAO.findById(id);
        if (existingOfficer == null) {
            throw new IllegalArgumentException("Officer with ID " + id + " not found");
        }

        return officerDAO.deleteById(id);
    }

    /**
     * Check if officer exists
     * 
     * @param id The officer ID
     * @return true if exists, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean officerExists(Integer id) throws SQLException {
        return officerDAO.existsById(id);
    }

    /**
     * Get total count of officers
     * 
     * @return Total count
     * @throws SQLException if database error occurs
     */
    public long getOfficerCount() throws SQLException {
        return officerDAO.count();
    }

    public long getOnDutyCount() throws SQLException {
        return officerDAO.countOnDuty();
    }

    /**
     * Get officers by rank
     * 
     * @param rank The rank
     * @return List of officers with the specified rank
     * @throws SQLException if database error occurs
     */
    public List<Officer> getOfficersByRank(String rank) throws SQLException {
        List<Officer> allOfficers = officerDAO.findAll();
        return allOfficers.stream()
                .filter(officer -> officer.getRank().equalsIgnoreCase(rank))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Search officers by name
     * 
     * @param name The name to search for
     * @return List of officers matching the name
     * @throws SQLException if database error occurs
     */
    public List<Officer> searchOfficersByName(String name) throws SQLException {
        List<Officer> allOfficers = officerDAO.findAll();
        return allOfficers.stream()
                .filter(officer -> officer.getFirstName().toLowerCase().contains(name.toLowerCase()) ||
                        officer.getLastName().toLowerCase().contains(name.toLowerCase()) ||
                        officer.getFullName().toLowerCase().contains(name.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }
}
