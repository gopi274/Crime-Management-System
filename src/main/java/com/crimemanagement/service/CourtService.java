package com.crimemanagement.service;

import com.crimemanagement.dao.CaseDAO;
import com.crimemanagement.entity.Case;

import java.sql.SQLException;
import java.util.List;

public class CourtService {
    private final CaseDAO caseDAO = new CaseDAO();

    public List<Case> getCases(String status) throws SQLException {
        if (status == null || status.isEmpty()) {
            return caseDAO.findAll();
        }
        return caseDAO.findByStatus(status);
    }

    public Case updateVerdict(Integer caseId, String verdict) throws SQLException {
        Case c = caseDAO.findById(caseId);
        if (c == null) return null;
        c.setStatus(verdict);
        return caseDAO.update(c);
    }
}


