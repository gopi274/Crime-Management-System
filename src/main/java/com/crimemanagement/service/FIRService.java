package com.crimemanagement.service;

import com.crimemanagement.dao.FIRDAO;
import com.crimemanagement.dao.OfficerDAO;
import com.crimemanagement.entity.FIR;
import com.crimemanagement.entity.Officer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for FIR business logic
 */
@Service
public class FIRService {
    private final FIRDAO firDAO;
    private final OfficerDAO officerDAO;

    @Autowired
    public FIRService(FIRDAO firDAO, OfficerDAO officerDAO) {
        this.firDAO = firDAO;
        this.officerDAO = officerDAO;
    }

    /**
     * No-args constructor for backward compatibility with legacy Swing UI
     */
    public FIRService() {
        this.firDAO = new FIRDAO();
        this.officerDAO = new OfficerDAO();
    }

    /**
     * Create a new FIR
     * 
     * @param fir The FIR to create
     * @return The created FIR
     * @throws SQLException if database error occurs
     */
    public FIR createFIR(FIR fir) throws SQLException {
        // Validate required fields
        if (fir.getFirNumber() == null || fir.getFirNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("FIR number is required");
        }
        if (fir.getCrimeType() == null || fir.getCrimeType().trim().isEmpty()) {
            throw new IllegalArgumentException("Crime type is required");
        }
        if (fir.getCrimeDescription() == null || fir.getCrimeDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Crime description is required");
        }
        if (fir.getLocation() == null || fir.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (fir.getDateOfIncident() == null) {
            throw new IllegalArgumentException("Date of incident is required");
        }
        if (fir.getDateReported() == null) {
            throw new IllegalArgumentException("Date reported is required");
        }
        if (fir.getCitizenId() == null) {
            throw new IllegalArgumentException("Citizen ID is required");
        }

        // Validate dates
        if (fir.getDateOfIncident().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Date of incident cannot be in the future");
        }
        if (fir.getDateReported().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Date reported cannot be in the future");
        }
        if (fir.getDateReported().isBefore(fir.getDateOfIncident())) {
            throw new IllegalArgumentException("Date reported cannot be before date of incident");
        }

        // Check if FIR number already exists
        FIR existingFIR = firDAO.findByFirNumber(fir.getFirNumber());
        if (existingFIR != null) {
            throw new IllegalArgumentException("FIR with number " + fir.getFirNumber() + " already exists");
        }

        // Validate assigned officer if provided
        if (fir.getAssignedOfficerId() != null) {
            Officer officer = officerDAO.findById(fir.getAssignedOfficerId());
            if (officer == null) {
                throw new IllegalArgumentException("Officer with ID " + fir.getAssignedOfficerId() + " not found");
            }
        }

        // Set default status if not provided
        if (fir.getStatus() == null || fir.getStatus().trim().isEmpty()) {
            fir.setStatus("Open");
        }

        return firDAO.create(fir);
    }

    /**
     * Get FIR by ID
     * 
     * @param id The FIR ID
     * @return The FIR if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public FIR getFIRById(Integer id) throws SQLException {
        return firDAO.findById(id);
    }

    /**
     * Get FIR by FIR number
     * 
     * @param firNumber The FIR number
     * @return The FIR if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public FIR getFIRByNumber(String firNumber) throws SQLException {
        return firDAO.findByFirNumber(firNumber);
    }

    /**
     * Get all FIRs
     * 
     * @param stationId Optional station ID for filtering
     * @return List of all (filtered) FIRs
     * @throws SQLException if database error occurs
     */
    public List<FIR> getAllFIRs(Integer stationId) throws SQLException {
        return firDAO.findAll(stationId);
    }

    public List<FIR> getAllFIRs() throws SQLException {
        return getAllFIRs(null);
    }

    /**
     * Get FIRs by crime type
     * 
     * @param crimeType The crime type
     * @return List of FIRs with the specified crime type
     * @throws SQLException if database error occurs
     */
    public List<FIR> getFIRsByCrimeType(String crimeType) throws SQLException {
        return firDAO.findByCrimeType(crimeType);
    }

    /**
     * Get FIRs by status
     * 
     * @param status The status
     * @return List of FIRs with the specified status
     * @throws SQLException if database error occurs
     */
    public List<FIR> getFIRsByStatus(String status) throws SQLException {
        return firDAO.findByStatus(status);
    }

    /**
     * Get FIRs by assigned officer
     * 
     * @param officerId The officer ID
     * @return List of FIRs assigned to the officer
     * @throws SQLException if database error occurs
     */
    public List<FIR> getFIRsByAssignedOfficer(Integer officerId) throws SQLException {
        // Validate officer exists
        Officer officer = officerDAO.findById(officerId);
        if (officer == null) {
            throw new IllegalArgumentException("Officer with ID " + officerId + " not found");
        }
        return firDAO.findByAssignedOfficer(officerId);
    }

    /**
     * Search FIRs by keyword
     * 
     * @param keyword The keyword to search for
     * @return List of FIRs matching the keyword
     * @throws SQLException if database error occurs
     */
    public List<FIR> searchFIRsByKeyword(String keyword) throws SQLException {
        return firDAO.searchByKeyword(keyword);
    }

    /**
     * Update a FIR
     * 
     * @param fir The FIR to update
     * @return The updated FIR
     * @throws SQLException if database error occurs
     */
    public FIR updateFIR(FIR fir) throws SQLException {
        // Validate required fields
        if (fir.getFirId() <= 0) {
            throw new IllegalArgumentException("Valid FIR ID is required");
        }
        if (fir.getFirNumber() == null || fir.getFirNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("FIR number is required");
        }
        if (fir.getCrimeType() == null || fir.getCrimeType().trim().isEmpty()) {
            throw new IllegalArgumentException("Crime type is required");
        }
        if (fir.getCrimeDescription() == null || fir.getCrimeDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Crime description is required");
        }
        if (fir.getLocation() == null || fir.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (fir.getDateOfIncident() == null) {
            throw new IllegalArgumentException("Date of incident is required");
        }
        if (fir.getDateReported() == null) {
            throw new IllegalArgumentException("Date reported is required");
        }
        if (fir.getCitizenId() == null) {
            throw new IllegalArgumentException("Citizen ID is required");
        }

        // Check if FIR exists
        FIR existingFIR = firDAO.findById(fir.getFirId());
        if (existingFIR == null) {
            throw new IllegalArgumentException("FIR with ID " + fir.getFirId() + " not found");
        }

        // Check if FIR number is being changed and if it conflicts with another FIR
        if (!existingFIR.getFirNumber().equals(fir.getFirNumber())) {
            FIR firWithNumber = firDAO.findByFirNumber(fir.getFirNumber());
            if (firWithNumber != null && firWithNumber.getFirId() != fir.getFirId()) {
                throw new IllegalArgumentException(
                        "FIR number " + fir.getFirNumber() + " is already assigned to another FIR");
            }
        }

        // Validate assigned officer if provided
        if (fir.getAssignedOfficerId() != null) {
            Officer officer = officerDAO.findById(fir.getAssignedOfficerId());
            if (officer == null) {
                throw new IllegalArgumentException("Officer with ID " + fir.getAssignedOfficerId() + " not found");
            }
        }

        return firDAO.update(fir);
    }

    /**
     * Delete a FIR
     * 
     * @param id The FIR ID
     * @return true if deleted, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteFIR(Integer id) throws SQLException {
        // Check if FIR exists
        FIR existingFIR = firDAO.findById(id);
        if (existingFIR == null) {
            throw new IllegalArgumentException("FIR with ID " + id + " not found");
        }

        return firDAO.deleteById(id);
    }

    /**
     * Check if FIR exists
     * 
     * @param id The FIR ID
     * @return true if exists, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean firExists(Integer id) throws SQLException {
        return firDAO.existsById(id);
    }

    /**
     * Get total count of FIRs
     * 
     * @return Total count
     * @throws SQLException if database error occurs
     */
    public long getFIRCount() throws SQLException {
        return firDAO.count();
    }

    /**
     * Update FIR status
     * 
     * @param firId  The FIR ID
     * @param status The new status
     * @return The updated FIR
     * @throws SQLException if database error occurs
     */
    public FIR updateFIRStatus(Integer firId, String status) throws SQLException {
        // Validate status
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException(
                    "Invalid status. Must be one of: Open, Under Investigation, Closed, Solved");
        }

        FIR fir = firDAO.findById(firId);
        if (fir == null) {
            throw new IllegalArgumentException("FIR with ID " + firId + " not found");
        }

        fir.setStatus(status);
        return firDAO.update(fir);
    }

    /**
     * Assign officer to FIR
     * 
     * @param firId     The FIR ID
     * @param officerId The officer ID
     * @return The updated FIR
     * @throws SQLException if database error occurs
     */
    public FIR assignOfficerToFIR(Integer firId, Integer officerId) throws SQLException {
        FIR fir = firDAO.findById(firId);
        if (fir == null) {
            throw new IllegalArgumentException("FIR with ID " + firId + " not found");
        }

        Officer officer = officerDAO.findById(officerId);
        if (officer == null) {
            throw new IllegalArgumentException("Officer with ID " + officerId + " not found");
        }

        fir.setAssignedOfficerId(officerId);
        return firDAO.update(fir);
    }

    /**
     * Get FIRs by date range
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return List of FIRs within the date range
     * @throws SQLException if database error occurs
     */
    public List<FIR> getFIRsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<FIR> allFIRs = firDAO.findAll();
        return allFIRs.stream()
                .filter(fir -> fir.getDateOfIncident() != null &&
                        fir.getDateOfIncident().isAfter(startDate) &&
                        fir.getDateOfIncident().isBefore(endDate))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Validate status
     * 
     * @param status The status to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidStatus(String status) {
        return status.equals("Open") || status.equals("Under Investigation") ||
                status.equals("Closed") || status.equals("Solved");
    }
}
