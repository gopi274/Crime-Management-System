package com.crimemanagement.service;

import com.crimemanagement.dao.*;
import com.crimemanagement.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for comprehensive search functionality across all entities
 */
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final FIRDAO firDAO;
    private final CaseDAO caseDAO;
    private final CriminalDAO criminalDAO;
    private final OfficerDAO officerDAO;
    private final SuspectDAO suspectDAO;
    private final EvidenceDAO evidenceDAO;

    public SearchService() {
        this.firDAO = new FIRDAO();
        this.caseDAO = new CaseDAO();
        this.criminalDAO = new CriminalDAO();
        this.officerDAO = new OfficerDAO();
        this.suspectDAO = new SuspectDAO();
        this.evidenceDAO = new EvidenceDAO();
    }

    /**
     * Search results container class
     */
    public static class SearchResults {
        private List<FIR> firs = new ArrayList<>();
        private List<Case> cases = new ArrayList<>();
        private List<Criminal> criminals = new ArrayList<>();
        private List<Officer> officers = new ArrayList<>();
        private List<Suspect> suspects = new ArrayList<>();
        private List<Evidence> evidence = new ArrayList<>();
        private int totalResults = 0;

        // Getters and setters
        public List<FIR> getFirs() {
            return firs;
        }

        public void setFirs(List<FIR> firs) {
            this.firs = firs;
        }

        public List<Case> getCases() {
            return cases;
        }

        public void setCases(List<Case> cases) {
            this.cases = cases;
        }

        public List<Criminal> getCriminals() {
            return criminals;
        }

        public void setCriminals(List<Criminal> criminals) {
            this.criminals = criminals;
        }

        public List<Officer> getOfficers() {
            return officers;
        }

        public void setOfficers(List<Officer> officers) {
            this.officers = officers;
        }

        public List<Suspect> getSuspects() {
            return suspects;
        }

        public void setSuspects(List<Suspect> suspects) {
            this.suspects = suspects;
        }

        public List<Evidence> getEvidence() {
            return evidence;
        }

        public void setEvidence(List<Evidence> evidence) {
            this.evidence = evidence;
        }

        public int getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(int totalResults) {
            this.totalResults = totalResults;
        }
    }

    public SearchResults searchAll(String keyword) {
        SearchResults results = new SearchResults();
        try {
            List<FIR> firs = firDAO.searchByKeyword(keyword);
            results.setFirs(firs);
            List<Case> cases = caseDAO.searchByKeyword(keyword);
            results.setCases(cases);
            List<Criminal> criminals = criminalDAO.findByName(keyword);
            results.setCriminals(criminals);

            List<Officer> officers = new ArrayList<>();
            Officer officer = officerDAO.findByBadgeNumber(keyword);
            if (officer != null) {
                officers.add(officer);
            } else {
                officers = searchOfficersByName(keyword);
            }
            results.setOfficers(officers);

            List<Evidence> evidence = evidenceDAO.searchByKeyword(keyword);
            results.setEvidence(evidence);

            int total = firs.size() + cases.size() + criminals.size() + officers.size() + evidence.size();
            results.setTotalResults(total);
            logger.info("Search completed for keyword '{}' - Found {} total results", keyword, total);
        } catch (SQLException e) {
            logger.error("Error during search for keyword '{}'", keyword, e);
        }
        return results;
    }

    private List<Officer> searchOfficersByName(String name) {
        List<Officer> officers = new ArrayList<>();
        try {
            List<Officer> allOfficers = officerDAO.findAll();
            for (Officer officer : allOfficers) {
                if (officer.getFirstName().toLowerCase().contains(name.toLowerCase()) ||
                        officer.getLastName().toLowerCase().contains(name.toLowerCase()) ||
                        officer.getFullName().toLowerCase().contains(name.toLowerCase())) {
                    officers.add(officer);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching officers by name '{}'", name, e);
        }
        return officers;
    }

    public SearchResults searchByOfficer(Integer officerId) {
        SearchResults results = new SearchResults();
        try {
            List<FIR> firs = firDAO.findByAssignedOfficer(officerId);
            results.setFirs(firs);
            List<Case> cases = caseDAO.findByAssignedOfficer(officerId);
            results.setCases(cases);

            int total = firs.size() + cases.size();
            results.setTotalResults(total);
            logger.info("Officer search completed for ID '{}' - Found {} results", officerId, total);
        } catch (SQLException e) {
            logger.error("Error during officer search for ID '{}'", officerId, e);
        }
        return results;
    }

    public CaseDetails getCaseDetails(Integer caseId) {
        CaseDetails details = new CaseDetails();
        try {
            Case caseEntity = caseDAO.findById(caseId);
            if (caseEntity == null)
                return details;
            details.setCaseEntity(caseEntity);

            FIR fir = firDAO.findById(caseEntity.getFirId());
            details.setFir(fir);

            List<Suspect> suspects = suspectDAO.findByCaseId(caseId);
            details.setSuspects(suspects);

            List<Evidence> evidence = evidenceDAO.findByFirId(caseEntity.getFirId());
            details.setEvidence(evidence);

            if (caseEntity.getAssignedOfficerId() != null) {
                Officer officer = officerDAO.findById(caseEntity.getAssignedOfficerId());
                details.setAssignedOfficer(officer);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving case details for ID '{}'", caseId, e);
        }
        return details;
    }

    public static class CaseDetails {
        private Case caseEntity;
        private FIR fir;
        private List<Suspect> suspects = new ArrayList<>();
        private List<Evidence> evidence = new ArrayList<>();
        private Officer assignedOfficer;

        public Case getCaseEntity() {
            return caseEntity;
        }

        public void setCaseEntity(Case caseEntity) {
            this.caseEntity = caseEntity;
        }

        public FIR getFir() {
            return fir;
        }

        public void setFir(FIR fir) {
            this.fir = fir;
        }

        public List<Suspect> getSuspects() {
            return suspects;
        }

        public void setSuspects(List<Suspect> suspects) {
            this.suspects = suspects;
        }

        public List<Evidence> getEvidence() {
            return evidence;
        }

        public void setEvidence(List<Evidence> evidence) {
            this.evidence = evidence;
        }

        public Officer getAssignedOfficer() {
            return assignedOfficer;
        }

        public void setAssignedOfficer(Officer assignedOfficer) {
            this.assignedOfficer = assignedOfficer;
        }
    }
}
