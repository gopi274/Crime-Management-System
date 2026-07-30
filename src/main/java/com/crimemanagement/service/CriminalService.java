package com.crimemanagement.service;

import com.crimemanagement.dao.CriminalDAO;
import com.crimemanagement.entity.Criminal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for Criminal business logic
 */
@Service
public class CriminalService {
    private final CriminalDAO criminalDAO;

    @Autowired
    public CriminalService(CriminalDAO criminalDAO) {
        this.criminalDAO = criminalDAO;
    }

    /**
     * No-args constructor for backward compatibility with legacy Swing UI
     */
    public CriminalService() {
        this.criminalDAO = new CriminalDAO();
    }

    /**
     * Create a new criminal
     * 
     * @param criminal The criminal to create
     * @return The created criminal
     * @throws SQLException if database error occurs
     */
    public Criminal createCriminal(Criminal criminal) throws SQLException {
        // Validate required fields
        if (criminal.getFirstName() == null || criminal.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (criminal.getLastName() == null || criminal.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (criminal.getGender() == null || criminal.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender is required");
        }

        // Validate gender
        if (!criminal.getGender().equals("M") && !criminal.getGender().equals("F")
                && !criminal.getGender().equals("Other")) {
            throw new IllegalArgumentException("Gender must be M, F, or Other");
        }

        // Validate date of birth if provided
        if (criminal.getDateOfBirth() != null && criminal.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        return criminalDAO.create(criminal);
    }

    /**
     * Get criminal by ID
     * 
     * @param id The criminal ID
     * @return The criminal if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public Criminal getCriminalById(Integer id) throws SQLException {
        return criminalDAO.findById(id);
    }

    /**
     * Get all criminals
     * 
     * @return List of all criminals
     * @throws SQLException if database error occurs
     */
    public List<Criminal> getAllCriminals() throws SQLException {
        return criminalDAO.findAll();
    }

    /**
     * Get criminals by name
     * 
     * @param name The name to search for
     * @return List of criminals matching the name
     * @throws SQLException if database error occurs
     */
    public List<Criminal> getCriminalsByName(String name) throws SQLException {
        return criminalDAO.findByName(name);
    }

    /**
     * Get criminals by gender
     * 
     * @param gender The gender
     * @return List of criminals with the specified gender
     * @throws SQLException if database error occurs
     */
    public List<Criminal> getCriminalsByGender(String gender) throws SQLException {
        // Validate gender
        if (!gender.equals("M") && !gender.equals("F") && !gender.equals("Other")) {
            throw new IllegalArgumentException("Gender must be M, F, or Other");
        }
        return criminalDAO.findByGender(gender);
    }

    /**
     * Update a criminal
     * 
     * @param criminal The criminal to update
     * @return The updated criminal
     * @throws SQLException if database error occurs
     */
    public Criminal updateCriminal(Criminal criminal) throws SQLException {
        // Validate required fields
        if (criminal.getCriminalId() <= 0) {
            throw new IllegalArgumentException("Valid criminal ID is required");
        }
        if (criminal.getFirstName() == null || criminal.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (criminal.getLastName() == null || criminal.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (criminal.getGender() == null || criminal.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender is required");
        }

        // Validate gender
        if (!criminal.getGender().equals("M") && !criminal.getGender().equals("F")
                && !criminal.getGender().equals("Other")) {
            throw new IllegalArgumentException("Gender must be M, F, or Other");
        }

        // Check if criminal exists
        Criminal existingCriminal = criminalDAO.findById(criminal.getCriminalId());
        if (existingCriminal == null) {
            throw new IllegalArgumentException("Criminal with ID " + criminal.getCriminalId() + " not found");
        }

        // Validate date of birth if provided
        if (criminal.getDateOfBirth() != null && criminal.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        return criminalDAO.update(criminal);
    }

    /**
     * Delete a criminal
     * 
     * @param id The criminal ID
     * @return true if deleted, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteCriminal(Integer id) throws SQLException {
        // Check if criminal exists
        Criminal existingCriminal = criminalDAO.findById(id);
        if (existingCriminal == null) {
            throw new IllegalArgumentException("Criminal with ID " + id + " not found");
        }

        return criminalDAO.deleteById(id);
    }

    /**
     * Check if criminal exists
     * 
     * @param id The criminal ID
     * @return true if exists, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean criminalExists(Integer id) throws SQLException {
        return criminalDAO.existsById(id);
    }

    /**
     * Get total count of criminals
     * 
     * @return Total count
     * @throws SQLException if database error occurs
     */
    public long getCriminalCount() throws SQLException {
        return criminalDAO.count();
    }

    /**
     * Search criminals by alias
     * 
     * @param alias The alias to search for
     * @return List of criminals with the specified alias
     * @throws SQLException if database error occurs
     */
    public List<Criminal> getCriminalsByAlias(String alias) throws SQLException {
        List<Criminal> allCriminals = criminalDAO.findAll();
        return allCriminals.stream()
                .filter(criminal -> criminal.getAlias() != null &&
                        criminal.getAlias().toLowerCase().contains(alias.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get criminals by age range
     * 
     * @param minAge Minimum age
     * @param maxAge Maximum age
     * @return List of criminals within the age range
     * @throws SQLException if database error occurs
     */
    public List<Criminal> getCriminalsByAgeRange(int minAge, int maxAge) throws SQLException {
        List<Criminal> allCriminals = criminalDAO.findAll();
        return allCriminals.stream()
                .filter(criminal -> {
                    int age = criminal.getAge();
                    return age >= minAge && age <= maxAge;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get criminals with previous convictions
     * 
     * @return List of criminals with previous convictions
     * @throws SQLException if database error occurs
     */
    public List<Criminal> getCriminalsWithPreviousConvictions() throws SQLException {
        List<Criminal> allCriminals = criminalDAO.findAll();
        return allCriminals.stream()
                .filter(criminal -> criminal.getCriminalRecord() != null &&
                        !criminal.getCriminalRecord().trim().isEmpty() &&
                        !criminal.getCriminalRecord().equalsIgnoreCase("No previous convictions"))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get criminals without previous convictions
     * 
     * @return List of criminals without previous convictions
     * @throws SQLException if database error occurs
     */
    public List<Criminal> getCriminalsWithoutPreviousConvictions() throws SQLException {
        List<Criminal> allCriminals = criminalDAO.findAll();
        return allCriminals.stream()
                .filter(criminal -> criminal.getCriminalRecord() == null ||
                        criminal.getCriminalRecord().trim().isEmpty() ||
                        criminal.getCriminalRecord().equalsIgnoreCase("No previous convictions"))
                .collect(java.util.stream.Collectors.toList());
    }
}
