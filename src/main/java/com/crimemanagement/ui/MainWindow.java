package com.crimemanagement.ui;

import com.crimemanagement.util.DesignSystem;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private final LoginDialog.Role userRole;
    private final String username;
    private final Integer officerId;
    private final Integer jailId;
    private final Integer courtId;
    private final Integer scientistId;
    private Integer stationId;

    public MainWindow(LoginDialog.Role role, String username, Integer officerId, Integer jailId, Integer courtId, Integer scientistId) {
        super("Crime Record Management System - " + role + " View");
        DesignSystem.setup();
        this.userRole = role;
        this.username = username;
        this.officerId = officerId;
        this.jailId = jailId;
        this.courtId = courtId;
        this.scientistId = scientistId;
        
        if (officerId != null) {
            try {
                com.crimemanagement.dao.OfficerDAO oDao = new com.crimemanagement.dao.OfficerDAO();
                com.crimemanagement.entity.Officer o = oDao.findById(officerId);
                if (o != null) this.stationId = o.getStationId();
            } catch (Exception ex) {}
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);
        
        try {
            setIconImage(DesignSystem.getKeralaPoliceLogo(64, 64).getImage());
        } catch (Exception ex) {}

        add(createTopBar(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        tabs.setOpaque(true);
        tabs.setBackground(DesignSystem.BACKGROUND);

        java.util.function.BiConsumer<String, JPanel> addSafeTab = (title, panel) -> {
            try { tabs.addTab(title, panel); } catch (Exception e) {}
        };

        // Flat Menu Structure as requested by User (V6.5.1 Graduation)
        if (userRole == LoginDialog.Role.ADMIN || userRole == LoginDialog.Role.OFFICER) {
            addSafeTab.accept("Dashboard", new DashboardPanel(officerId));
        }

        if (officerId != null) {
            addSafeTab.accept("My Employee Profile", new ProfilePanel(officerId));
        }

        if (userRole == LoginDialog.Role.ADMIN || userRole == LoginDialog.Role.OFFICER) {
            addSafeTab.accept("e-Complaints", new ComplaintsPanel(userRole, stationId));
        }

        if (userRole == LoginDialog.Role.ADMIN || userRole == LoginDialog.Role.OFFICER) {
            addSafeTab.accept("Officers Registry", new OfficersPanel());
            addSafeTab.accept("Forensic Registry", new ForensicOfficersPanel());
            addSafeTab.accept("Crime Intelligence", new CrimeGraphPanel());
        }

        if (userRole != LoginDialog.Role.JAILER && userRole != LoginDialog.Role.FORENSIC_EXAMINER) {
            addSafeTab.accept("Criminals Database", new CriminalsPanel(userRole));
        }

        addSafeTab.accept("FIRs Registry", new FIRsPanel(userRole, stationId));

        if (userRole != LoginDialog.Role.JAILER && userRole != LoginDialog.Role.FORENSIC_EXAMINER) {
            addSafeTab.accept("Judicial Cases", new CasesPanel(userRole, stationId, this.courtId, this.officerId));
        }

        if (userRole == LoginDialog.Role.ADMIN || userRole == LoginDialog.Role.OFFICER || userRole == LoginDialog.Role.COURT) {
            addSafeTab.accept("Suspects Detail", new SuspectsPanel(userRole, this.courtId));
        }
        
        if (userRole == LoginDialog.Role.ADMIN || userRole == LoginDialog.Role.OFFICER || userRole == LoginDialog.Role.FORENSIC_EXAMINER) {
            addSafeTab.accept("Forensic Evidence", new EvidenceManagementPanel(this.courtId, this.scientistId));
        }
        
        if (userRole == LoginDialog.Role.ADMIN || userRole == LoginDialog.Role.JAILER) {
            addSafeTab.accept("Kerala Prison Registry", new JailManagementPanel(this.jailId));
        }

        if (userRole == LoginDialog.Role.ADMIN || userRole == LoginDialog.Role.OFFICER) {
            addSafeTab.accept("Live Command", new LiveCommandPanel(userRole, stationId));
            addSafeTab.accept("Intranet (HQ)", new IntranetPanel(username, userRole));
        }

        if (userRole == LoginDialog.Role.ADMIN) {
            addSafeTab.accept("User Management", new AdminPanel());
            addSafeTab.accept("System Settings", new SystemSettingsPanel());
            addSafeTab.accept("Security Audit", new AuditLogPanel());
        }

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(DesignSystem.BACKGROUND);
        bar.setPreferredSize(new Dimension(getWidth(), 70));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.KP_GOLD));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        left.setOpaque(false);
        try {
            left.add(new JLabel(DesignSystem.getKeralaPoliceLogo(45, 45)));
        } catch (Exception e) {}
        
        JLabel brand = new JLabel("KERALA POLICE");
        brand.setFont(DesignSystem.TITLE_FONT);
        brand.setForeground(DesignSystem.KP_GOLD);
        left.add(brand);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        right.setOpaque(false);
        
        // User Info with Context (Court/Station/Jail Name)
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel userLbl = new JLabel(username);
        userLbl.setForeground(Color.WHITE);
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        String context = userRole.toString();
        try {
            if (courtId != null) {
                com.crimemanagement.dao.CourtDAO cDao = new com.crimemanagement.dao.CourtDAO();
                com.crimemanagement.entity.Court c = cDao.findById(courtId);
                if (c != null) context += " » " + c.getName();
            } else if (jailId != null) {
                com.crimemanagement.dao.JailDAO jDao = new com.crimemanagement.dao.JailDAO();
                com.crimemanagement.entity.Jail j = jDao.findById(jailId);
                if (j != null) context += " » " + j.getName();
            } else if (stationId != null) {
                com.crimemanagement.dao.PoliceStationDAO sDao = new com.crimemanagement.dao.PoliceStationDAO();
                com.crimemanagement.entity.PoliceStation s = sDao.findById(stationId);
                if (s != null) context += " » " + s.getName();
            }
        } catch (Exception e) {}

        JLabel roleLbl = new JLabel(context);
        roleLbl.setForeground(DesignSystem.KP_GOLD);
        roleLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        info.add(userLbl);
        info.add(roleLbl);
        right.add(info);

        // On-Duty Toggle for Officers
        if (userRole == LoginDialog.Role.OFFICER && officerId != null) {
            JToggleButton dutyToggle = new JToggleButton("ON DUTY");
            dutyToggle.setSelected(true);
            dutyToggle.setFont(new Font("SansSerif", Font.BOLD, 10));
            dutyToggle.setBackground(DesignSystem.SUCCESS);
            dutyToggle.setForeground(Color.BLACK);
            dutyToggle.addActionListener(ev -> {
                boolean isOn = dutyToggle.isSelected();
                dutyToggle.setText(isOn ? "ON DUTY" : "OFF DUTY");
                dutyToggle.setBackground(isOn ? DesignSystem.SUCCESS : DesignSystem.DANGER);
                try {
                    com.crimemanagement.dao.OfficerDAO d = new com.crimemanagement.dao.OfficerDAO();
                    com.crimemanagement.entity.Officer o = d.findById(officerId);
                    if (o != null) {
                        o.setIsOnDuty(isOn);
                        d.update(o);
                        com.crimemanagement.service.AuditService.log(username, "STATUS_CHANGE", "Officer set status to " + (isOn ? "ON_DUTY" : "OFF_DUTY"));
                    }
                } catch (Exception ex) {}
            });
            right.add(dutyToggle);
        }

        JButton logout = new JButton("LOGOUT");
        DesignSystem.applyButtonStyle(logout, false);
        logout.addActionListener(e -> {
            this.dispose();
            com.crimemanagement.DesktopLauncher.startApp(new String[0]);
        });
        right.add(logout);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }
}
