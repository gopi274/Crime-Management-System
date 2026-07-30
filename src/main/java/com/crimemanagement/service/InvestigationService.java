package com.crimemanagement.service;

import com.crimemanagement.dao.CaseDAO;
import com.crimemanagement.entity.Case;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class InvestigationService {
    private final CaseDAO caseDAO;
    private final com.crimemanagement.dao.EvidenceDAO evidenceDAO;

    @Autowired
    public InvestigationService(CaseDAO caseDAO) {
        this.caseDAO = caseDAO;
        this.evidenceDAO = new com.crimemanagement.dao.EvidenceDAO();
    }

    /**
     * No-args constructor for backward compatibility with legacy Swing UI
     */
    public InvestigationService() {
        this.caseDAO = new CaseDAO();
        this.evidenceDAO = new com.crimemanagement.dao.EvidenceDAO();
    }

    // Existing methods
    public List<Case> getAssignedCases(Integer officerId) throws SQLException {
        return caseDAO.findByAssignedOfficer(officerId);
    }

    public Case updateCaseStatus(Integer caseId, String status) throws SQLException {
        Case c = caseDAO.findById(caseId);
        if (c == null)
            return null;
        c.setStatus(status);
        return caseDAO.update(c);
    }

    // New methods for CRUD
    public List<Case> getAllCases(Integer stationId, Integer courtId) throws SQLException {
        return caseDAO.findAll(stationId, courtId);
    }

    public List<Case> getAllCases(Integer stationId) throws SQLException {
        return getAllCases(stationId, null);
    }

    public List<Case> getAllCases() throws SQLException {
        return getAllCases(null, null);
    }

    public Case createCase(Case c) throws SQLException {
        return caseDAO.create(c);
    }

    public boolean deleteCase(Integer caseId) throws SQLException {
        return caseDAO.deleteById(caseId);
    }

    public Case getCaseById(Integer caseId) throws SQLException {
        return caseDAO.findById(caseId);
    }

    public Case updateCase(Case c) throws SQLException {
        return caseDAO.update(c);
    }

    public List<com.crimemanagement.entity.Evidence> getEvidenceForCase(Integer caseId) throws SQLException {
        Case c = caseDAO.findById(caseId);
        if (c == null) return java.util.Collections.emptyList();
        return evidenceDAO.findByFirId(c.getFirId());
    }
}
