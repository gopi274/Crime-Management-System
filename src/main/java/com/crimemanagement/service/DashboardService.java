package com.crimemanagement.service;

import com.crimemanagement.dao.CaseDAO;
import com.crimemanagement.dao.CriminalDAO;
import com.crimemanagement.entity.Criminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to aggregate data for the Dashboard.
 */
@Service
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final CaseDAO caseDAO;
    private final CriminalDAO criminalDAO;
    private final AuditService auditService;

    @Autowired
    public DashboardService(CaseDAO caseDAO, CriminalDAO criminalDAO,
            AuditService auditService) {
        this.caseDAO = caseDAO;
        this.criminalDAO = criminalDAO;
        this.auditService = auditService;
    }

    /**
     * No-args constructor for backward compatibility with legacy Swing UI
     */
    public DashboardService() {
        this.caseDAO = new CaseDAO();
        this.criminalDAO = new CriminalDAO();
        this.auditService = new AuditService();
    }

    public static class DashboardStats {
        public long totalCases;
        public long activeInvestigations;
        public long criticalAlerts;
        public long publicVerdicts;
        public String resolvedPercentage;
        public String hotspotInfo;

        public DashboardStats(long total, long active, long critical, long publicVerdicts, String resolved, String hotspot) {
            this.totalCases = total;
            this.activeInvestigations = active;
            this.criticalAlerts = critical;
            this.publicVerdicts = publicVerdicts;
            this.resolvedPercentage = resolved;
            this.hotspotInfo = hotspot;
        }
    }

    /**
     * Get main statistics for the top row cards
     */
    public DashboardStats getStats() {
        return getStats(null);
    }

    public DashboardStats getStats(Integer stationId) {
        try {
            long totalCases = caseDAO.count();
            long active = caseDAO.countByStatus("Under Investigation") + caseDAO.countByStatus("Open");
            long critical = caseDAO.countByPriority("High") + caseDAO.countByPriority("Critical");
            long closed = caseDAO.countByStatus("Closed") + caseDAO.countByStatus("Solved");
            long publicVerdicts = caseDAO.findAllPublic().size();

            double handled = totalCases > 0 ? ((double) closed / totalCases) * 100 : 0;
            String resolvedStr = String.format("%.0f%%", handled);

            String hotspot = "Not Analyzed";
            try {
                com.crimemanagement.dao.FIRDAO firDAO = new com.crimemanagement.dao.FIRDAO();
                hotspot = firDAO.getHotspotLocation(stationId);
            } catch (Exception e) {
                logger.error("Error fetching hotspot", e);
            }

            return new DashboardStats(totalCases, active, critical, publicVerdicts, resolvedStr, hotspot);
        } catch (SQLException e) {
            logger.error("Error fetching dashboard stats", e);
            return new DashboardStats(0, 0, 0, 0, "0%", "N/A");
        }
    }

    /**
     * Get recent criminals for "Most Wanted" section
     * Since we don't have a risk level, we'll just fetch the most recently added
     * ones.
     */
    public List<Criminal> getRecentCriminals(int limit) {
        try {
            List<Criminal> all = criminalDAO.findAll();
            // Reverse to get newest first (findAll sorts by name usually, but let's just
            // take end of list if it was by ID)
            // Actually CriminalDAO sorts by name. This is suboptimal for "recent", but
            // without a "created_at" sort in DAO,
            // we will just take a sublist.
            // Ideally we should add a method in DAO to findAllOrderByCreatedDesc.
            // For now, let's just shuffle or take the first few as a proxy if we can't sort
            // by date efficiently.

            // Better approach: Sort in memory by ID (proxy for creation time)
            all.sort((c1, c2) -> Integer.compare(c2.getCriminalId(), c1.getCriminalId()));

            return all.stream().limit(limit).collect(Collectors.toList());
        } catch (SQLException e) {
            logger.error("Error fetching criminals", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get recent activity logs
     */
    public List<String> getRecentActivity(int limit) {
        return auditService.getRecentLogs(limit);
    }

    /**
     * Get trend data for the chart (last 6 months)
     */
    public Map<String, Long> getTrendData() {
        try {
            return caseDAO.getMonthlyCaseCounts(6);
        } catch (SQLException e) {
            logger.error("Error fetching trend data", e);
            return Collections.emptyMap();
        }
    }
}
