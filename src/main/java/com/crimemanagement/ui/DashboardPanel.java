package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.service.DashboardService;
import com.crimemanagement.entity.Criminal;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService;

    // UI Components to update
    private JLabel totalCasesLbl;
    private JLabel activeLbl;
    private JLabel criticalLbl;
    private JLabel resolvedLbl;
    private JLabel hotspotLbl;
    private Integer currentStationId;

    // Panels to update
    private JPanel mostWantedList;
    private JPanel activityPanel;
    private TrendChart trendChart;

    public DashboardPanel(Integer officerId) {
        dashboardService = new DashboardService();

        setLayout(new BorderLayout(30, 30));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Top Section (Header + Stats)
        JPanel topSection = new JPanel(new BorderLayout(0, 30));
        topSection.setOpaque(false);

        // Header with Refresh and Reporting Officer Info
        JPanel headerInfoPanel = new JPanel(new BorderLayout());
        headerInfoPanel.setOpaque(false);
        
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);
        
        String stationTitle = "Director General's Control Room";
        if (officerId != null) {
            try {
                com.crimemanagement.entity.Officer o = new com.crimemanagement.dao.OfficerDAO().findById(officerId);
                if (o != null && o.getStationId() != null) {
                    com.crimemanagement.entity.PoliceStation s = new com.crimemanagement.dao.PoliceStationDAO().findById(o.getStationId());
                    if (s != null) {
                        String sName = s.getName();
                        if (sName.toUpperCase().endsWith(" PS")) {
                            sName = sName.substring(0, sName.length() - 3) + " Police Station";
                        }
                        String city = s.getCity() != null ? s.getCity() : "KERALA";
                        stationTitle = sName + " - " + city.toUpperCase();
                        this.currentStationId = o.getStationId();
                    }
                }
            } catch (Exception ex) {}
        }
        
        JLabel mainTitle = new JLabel(stationTitle);
        mainTitle.setFont(new Font("Serif", Font.BOLD, 30));
        mainTitle.setForeground(DesignSystem.KP_GOLD);
        titleContainer.add(mainTitle);
        
        headerInfoPanel.add(titleContainer, BorderLayout.WEST);

        // Logo removed from here as per user request

        if (officerId != null) {
            String reportingBoss = "Reporting Authority: Kerala State Police Registry";
            String jurisdictionInfo = "";
            try {
                com.crimemanagement.dao.OfficerDAO oDao = new com.crimemanagement.dao.OfficerDAO();
                com.crimemanagement.entity.Officer o = oDao.findById(officerId);
                if (o != null) {
                    String r = o.getRank().toUpperCase();
                    
                    // Reporting Authority Hierarchy logic
                    java.util.List<String> lookupRanks = new java.util.ArrayList<>();
                    if (r.contains("CONSTABLE") || r.equals("SCPO") || r.equals("ASI")) { lookupRanks.add("SI"); reportingBoss = "Reporting Authority: Sub-Inspector (SI)"; }
                    else if (r.equals("SI")) { lookupRanks.add("INSPECTOR"); reportingBoss = "Reporting Authority: Inspector / SHO"; }
                    else if (r.contains("INSPECTOR") || r.equals("CI")) { lookupRanks.add("DYSP"); lookupRanks.add("ACP"); reportingBoss = "Reporting Authority: DySP / ACP"; }
                    else if (r.contains("DYSP") || r.contains("ACP")) { lookupRanks.add("SP"); lookupRanks.add("DCP"); reportingBoss = "Reporting Authority: Superintendent (SP)"; }
                    else { reportingBoss = "Reporting Authority: Kerala State Police HQ"; }

                    if (!lookupRanks.isEmpty()) {
                        String placeholders = String.join(",", java.util.Collections.nCopies(lookupRanks.size(), "?"));
                        String sql = "SELECT first_name, last_name, rank FROM officers WHERE rank IN (" + placeholders + ") LIMIT 1";
                        try (java.sql.Connection con = com.crimemanagement.util.DatabaseConnection.getConnection();
                             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
                             for (int k = 0; k < lookupRanks.size(); k++) ps.setString(k + 1, lookupRanks.get(k));
                             try (java.sql.ResultSet rs = ps.executeQuery()) {
                                 if (rs.next()) reportingBoss = "Reporting Authority: " + rs.getString("first_name") + " " + rs.getString("last_name") + " (" + rs.getString("rank") + ")";
                             }
                        }
                    }

                    // Dynamic Jurisdiction logic
                    if (o.getStationId() != null) {
                        this.currentStationId = o.getStationId();
                        com.crimemanagement.entity.PoliceStation s = new com.crimemanagement.dao.PoliceStationDAO().findById(o.getStationId());
                        if (s != null) {
                            String stCity = s.getCity() != null ? s.getCity() : "";
                            
                            // Ranking Logic: We must be careful not to match substrings like "SP" inside "INSPECTOR"
                            boolean isDistrictHead = r.contains("SP") && !r.contains("INSPECTOR") || r.contains("COMMISSIONER");
                            boolean isSubDivHead = r.contains("DYSP") || r.contains("ACP");

                            if (r.equals("DGP") || r.equals("ADGP")) {
                                jurisdictionInfo = "🌍 STATE COMMAND: Kerala State Police HQ (Statewide)";
                            } else if (r.equals("IG")) {
                                jurisdictionInfo = "🔱 ZONE COMMAND: Administrative Zone Authority (State Regions)";
                            } else if (r.equals("DIG")) {
                                jurisdictionInfo = "🛡️ RANGE COMMAND: District-Cluster Range Authority";
                            } else if (isDistrictHead) {
                                if (!stCity.isEmpty()) {
                                    jurisdictionInfo = "🏛️ DISTRICT COMMAND: " + stCity.toUpperCase() + " (State Authority)";
                                }
                            } else if (isSubDivHead) {
                                jurisdictionInfo = "⭕ CIRCLE COMMAND: Local Sub-Division (" + s.getName() + ")";
                            } else {
                                jurisdictionInfo = "📍 STATION COMMAND: " + s.getName();
                            }
                        }
                    }

                    JLabel reportingLbl = new JLabel(reportingBoss);
                    reportingLbl.setFont(DesignSystem.CAPTION_FONT);
                    reportingLbl.setForeground(DesignSystem.SUCCESS);
                    titleContainer.add(Box.createVerticalStrut(5));
                    titleContainer.add(reportingLbl);
                    
                    if (!jurisdictionInfo.isEmpty()) {
                        JLabel jurLbl = new JLabel(jurisdictionInfo);
                        jurLbl.setFont(DesignSystem.CAPTION_FONT);
                        jurLbl.setForeground(DesignSystem.WARNING);
                        titleContainer.add(Box.createVerticalStrut(2));
                        titleContainer.add(jurLbl);
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(headerInfoPanel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        JButton portalBtn = DesignSystem.createPrimaryButton("🌐 Launch Citizen Portal");
        portalBtn.setBackground(DesignSystem.SUCCESS);
        portalBtn.setForeground(Color.BLACK); // High contrast for readability
        portalBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel versionLabel = new JLabel("🔘 V6.0 DIRECTOR ");
        versionLabel.setForeground(DesignSystem.SUCCESS);
        versionLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        buttonPanel.add(versionLabel);
        
        portalBtn.addActionListener(e -> {
            portalBtn.setEnabled(false);
            portalBtn.setText("⏳ Starting Hub...");
            
            new SwingWorker<Boolean, Void>() {
                private String p = System.getProperty("portal.port", "9500");
                
                @Override
                protected Boolean doInBackground() throws Exception {
                    for (int i = 0; i < 40; i++) { // Max stability: 40s wait
                        try {
                            java.net.HttpURLConnection con = (java.net.HttpURLConnection) new java.net.URL("http://localhost:" + p + "/api/public/stats").openConnection();
                            con.setConnectTimeout(1000);
                            if (con.getResponseCode() == 200) return true;
                        } catch (Exception ex) { /* Handshake in progress... */ }
                        Thread.sleep(1000);
                    }
                    return false;
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:" + p));
                            portalBtn.setText("🌐 Launch Citizen Portal");
                        } else {
                            int choice = JOptionPane.showOptionDialog(DashboardPanel.this, 
                                "CRITICAL: V5.0 Tactical Sync Error.\n" +
                                "The background engine is being blocked by your Mac.\n" +
                                "Would you like to view the Rescue Log?", 
                                "V5.0 Rescue System", 
                                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, 
                                new String[]{"View Rescue Log", "Close"}, "View Rescue Log");
                            
                            if (choice == 0) {
                                java.awt.Desktop.getDesktop().open(new java.io.File(System.getProperty("user.home"), ".kerala-erp/logs/portal.log"));
                            }
                            portalBtn.setText("🌐 Retry Launch");
                        }
                        portalBtn.setEnabled(true);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });
        buttonPanel.add(portalBtn);

        JButton refreshBtn = DesignSystem.createPrimaryButton("🔄 Refresh Data");
        refreshBtn.addActionListener(e -> loadDashboardData());
        buttonPanel.add(refreshBtn);

        header.add(buttonPanel, BorderLayout.EAST);

        topSection.add(header, BorderLayout.NORTH);
        topSection.add(createStatsRow(), BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);

        // Center Content (Split into Chart and Sidebar)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 20); 
        centerPanel.add(createChartPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        centerPanel.add(createSidebarPanel(), gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Trigger initial data load
        loadDashboardData();
    }
    private void loadDashboardData() {
        new SwingWorker<Void, Void>() {
            DashboardService.DashboardStats stats;
            List<Criminal> recentCriminals;
            List<String> recentActivity;
            Map<String, Long> trendData;

            @Override
            protected Void doInBackground() throws Exception {
                stats = dashboardService.getStats(currentStationId);
                recentCriminals = dashboardService.getRecentCriminals(5); // Top 5
                recentActivity = dashboardService.getRecentActivity(5); // Top 5
                trendData = dashboardService.getTrendData(); // Last 6 months
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for errors

                    // Update Stats
                    totalCasesLbl.setText(String.format("%,d", stats.totalCases));
                    activeLbl.setText(String.format("%,d", stats.activeInvestigations));
                    criticalLbl.setText(String.format("%,d", stats.criticalAlerts));
                    resolvedLbl.setText(stats.resolvedPercentage);
                    hotspotLbl.setText(stats.hotspotInfo);

                    // Update Most Wanted
                    mostWantedList.removeAll();
                    mostWantedList.add(Box.createVerticalStrut(10));
                    if (recentCriminals.isEmpty()) {
                        mostWantedList.add(createListItem("No recent records", "-", DesignSystem.TEXT_SECONDARY));
                    } else {
                        for (Criminal c : recentCriminals) {
                            String name = c.getFirstName() + " " + c.getLastName();
                            if (c.getAlias() != null && !c.getAlias().isEmpty()) {
                                name += " '" + c.getAlias() + "'";
                            }
                            // Using ID as a proxy for "Risk" since we don't have it, or just "Active"
                            mostWantedList.add(createListItem(name, "Active", DesignSystem.DANGER));
                            mostWantedList.add(Box.createVerticalStrut(10));
                        }
                    }
                    mostWantedList.revalidate();
                    mostWantedList.repaint();

                    // Update Activities
                    activityPanel.removeAll();
                    activityPanel.add(Box.createVerticalStrut(10));
                    if (recentActivity.isEmpty()) {
                        activityPanel.add(createActivityItem("-", "No recent activity"));
                    } else {
                        for (String log : recentActivity) {
                            // Simple parsing of the log format: [TIMESTAMP] ACTOR: ...
                            String time = "Just now";
                            String desc = log;
                            if (log.startsWith("[")) {
                                int end = log.indexOf("]");
                                if (end > 0) {
                                    // Extract only time part HH:mm
                                    String fullTs = log.substring(1, end);
                                    if (fullTs.length() > 11) {
                                        time = fullTs.substring(11, 16); // Extract HH:mm
                                    }
                                    desc = log.substring(end + 1).trim();
                                    // Simplify description
                                    desc = desc.replace("ACTOR: ", "").replace("ACTION: ", "- ").replace("DETAILS: ",
                                            "");
                                }
                            }
                            activityPanel.add(createActivityItem(time, desc));
                            activityPanel.add(Box.createVerticalStrut(10));
                        }
                    }
                    activityPanel.revalidate();
                    activityPanel.repaint();

                    // Update Chart
                    trendChart.setData(trendData);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private JPanel createStatsRow() {
        JPanel container = new JPanel(new BorderLayout(25, 0));
        container.setOpaque(false);

        // Core stats in a smaller grid
        JPanel coreStats = new JPanel(new GridLayout(1, 4, 15, 0));
        coreStats.setOpaque(false);
        coreStats.add(createStatCard("Total Cases", "...", "All Time", DesignSystem.PRIMARY, lbl -> totalCasesLbl = lbl));
        coreStats.add(createStatCard("Active Investigations", "...", "Current", DesignSystem.ACCENT, lbl -> activeLbl = lbl));
        coreStats.add(createStatCard("Critical Alerts", "...", "High Priority", DesignSystem.DANGER, lbl -> criticalLbl = lbl));
        coreStats.add(createStatCard("Resolved", "...", "Completion Rate", DesignSystem.SUCCESS, lbl -> resolvedLbl = lbl));

        // Bigger Hotspot Stat
        JPanel hotspotPanel = createStatCard("Operational Hotspot", "...", "High-Activity Sector Identified", DesignSystem.WARNING, lbl -> hotspotLbl = lbl);
        hotspotPanel.setPreferredSize(new Dimension(500, 160));
        hotspotPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.WARNING, 2),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        container.add(coreStats, BorderLayout.CENTER);
        container.add(hotspotPanel, BorderLayout.EAST);

        return container;
    }

    interface LabelConsumer {
        void accept(JLabel label);
    }

    private JPanel createStatCard(String title, String value, String subtitle, Color accentColor,
            LabelConsumer consumer) {
        JPanel card = new JPanel(new BorderLayout());
        DesignSystem.applyCardStyle(card);

        // Custom border with glow effect color
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, accentColor), // removed left border for cleaner look
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(DesignSystem.CAPTION_FONT);
        titleLbl.setForeground(DesignSystem.TEXT_SECONDARY);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLbl.setForeground(DesignSystem.TEXT_PRIMARY);

        if (consumer != null)
            consumer.accept(valueLbl);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(DesignSystem.CAPTION_FONT);
        subLbl.setForeground(accentColor); // Use accent color for subtitle

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DesignSystem.applyCardStyle(panel);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(DesignSystem.createHeader("Crime Trend Analysis (Last 6 Months)"), BorderLayout.WEST);

        panel.add(header, BorderLayout.NORTH);

        trendChart = new TrendChart();
        panel.add(trendChart, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 20));
        panel.setOpaque(false);

        // Most Wanted
        JPanel mostWanted = new JPanel(new BorderLayout());
        DesignSystem.applyCardStyle(mostWanted);
        mostWanted.add(DesignSystem.createHeader("Most Wanted (Recent)"), BorderLayout.NORTH);

        mostWantedList = new JPanel();
        mostWantedList.setLayout(new BoxLayout(mostWantedList, BoxLayout.Y_AXIS));
        mostWantedList.setOpaque(false);
        mostWantedList.add(new JLabel("Loading..."));

        mostWanted.add(mostWantedList, BorderLayout.CENTER);

        // Recent Activities
        JPanel activities = new JPanel(new BorderLayout());
        DesignSystem.applyCardStyle(activities);
        activities.add(DesignSystem.createHeader("Recent Activity"), BorderLayout.NORTH);

        activityPanel = new JPanel();
        activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
        activityPanel.setOpaque(false);
        activityPanel.add(new JLabel("Loading..."));

        activities.add(activityPanel, BorderLayout.CENTER);

        panel.add(mostWanted);
        panel.add(activities);

        return panel;
    }

    // --- Helper Components for Lists ---

    private JPanel createListItem(String name, String status, Color statusColor) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Constrain height

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(DesignSystem.BODY_FONT);
        nameLbl.setForeground(DesignSystem.TEXT_PRIMARY);

        JLabel statusLbl = new JLabel(status);
        statusLbl.setFont(DesignSystem.CAPTION_FONT);
        statusLbl.setForeground(statusColor);

        item.add(nameLbl, BorderLayout.CENTER);
        item.add(statusLbl, BorderLayout.EAST);
        return item;
    }

    private JPanel createActivityItem(String time, String desc) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(DesignSystem.CAPTION_FONT);
        timeLbl.setForeground(DesignSystem.TEXT_SECONDARY);
        timeLbl.setPreferredSize(new Dimension(50, 20)); // Fixed width for time

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(DesignSystem.BODY_FONT);
        descLbl.setForeground(DesignSystem.TEXT_PRIMARY);

        item.add(timeLbl, BorderLayout.WEST);
        item.add(descLbl, BorderLayout.CENTER);
        return item;
    }

    // --- Custom Chart Component ---

    private static class TrendChart extends JPanel {
        private Map<String, Long> data = Collections.emptyMap();

        public TrendChart() {
            setOpaque(false);
        }

        public void setData(Map<String, Long> newData) {
            this.data = newData != null ? newData : Collections.emptyMap();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int paddingLeft = 40;
            int paddingBottom = 40;
            int paddingTop = 20;
            int paddingRight = 30;

            int chartWidth = w - paddingLeft - paddingRight;
            int chartHeight = h - paddingTop - paddingBottom;

            if (data == null || data.isEmpty()) {
                g2.setColor(DesignSystem.TEXT_SECONDARY);
                g2.drawString("No data available", w / 2 - 50, h / 2);
                return;
            }

            List<String> keys = new ArrayList<>(data.keySet());
            List<Long> values = new ArrayList<>(data.values());

            long maxVal = (long) (values.stream().mapToLong(v -> v).max().orElse(10L) * 1.25);
            maxVal = Math.max(10L, maxVal);

            g2.setFont(DesignSystem.CAPTION_FONT);
            FontMetrics fm = g2.getFontMetrics();

            // Draw Y-Axis Grid Lines and Labels
            g2.setColor(new Color(255, 255, 255, 20));
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i <= 4; i++) {
                int y = paddingTop + chartHeight * i / 4;
                g2.drawLine(paddingLeft, y, w - paddingRight, y);

                long labelVal = Math.round(maxVal - (maxVal * i / 4.0));
                String labelStr = String.valueOf(labelVal);
                g2.setColor(DesignSystem.TEXT_SECONDARY);
                g2.drawString(labelStr, paddingLeft - fm.stringWidth(labelStr) - 10, y + 5);
                g2.setColor(new Color(255, 255, 255, 20));
            }

            int numPoints = keys.size();
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < numPoints; i++) {
                int x = paddingLeft + (chartWidth * i) / Math.max(1, numPoints - 1);
                double normalized = (double) values.get(i) / maxVal;
                int y = (int) (h - paddingBottom - (normalized * chartHeight));
                points.add(new Point(x, y));

                // Draw X-Axis Label
                String labelStr = keys.get(i);
                if (labelStr.length() > 5) labelStr = labelStr.substring(5); // Show MM 
                g2.setColor(DesignSystem.TEXT_SECONDARY);
                g2.drawString(labelStr, x - fm.stringWidth(labelStr) / 2, h - paddingBottom + 20);
            }

            Path2D path = new Path2D.Double();
            path.moveTo(points.get(0).x, points.get(0).y);

            if (points.size() > 1) {
                for (int i = 0; i < points.size() - 1; i++) {
                    Point p1 = points.get(i);
                    Point p2 = points.get(i + 1);
                    int ctrlX = (p1.x + p2.x) / 2;
                    path.curveTo(ctrlX, p1.y, ctrlX, p2.y, p2.x, p2.y);
                }
            } else {
                path.lineTo(points.get(0).x, points.get(0).y);
            }

            g2.setStroke(new BasicStroke(3));
            g2.setColor(DesignSystem.PRIMARY);
            g2.draw(path);

            // Gradient Fill
            path.lineTo(points.get(points.size() - 1).x, h - paddingBottom);
            path.lineTo(points.get(0).x, h - paddingBottom);
            path.closePath();

            GradientPaint gp = new GradientPaint(0, 0, new Color(50, 100, 255, 100),
                    0, h, new Color(50, 100, 255, 0));
            g2.setPaint(gp);
            g2.fill(path);

            g2.setColor(DesignSystem.ACCENT);
            for (Point p : points) {
                g2.fillOval(p.x - 4, p.y - 4, 8, 8);
            }
        }
    }
}
