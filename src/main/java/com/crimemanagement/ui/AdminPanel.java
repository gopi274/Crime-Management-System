package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminPanel extends JPanel {
    private final AdminService adminService = new AdminService();
    private final com.crimemanagement.dao.CaseDAO caseDAO = new com.crimemanagement.dao.CaseDAO();
    private final com.crimemanagement.dao.PoliceStationDAO stationDAO = new com.crimemanagement.dao.PoliceStationDAO();

    private static class StationItem {
        final Integer id;
        final String name;
        StationItem(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name + " (ID: " + id + ")"; }
    }

    public AdminPanel() {
        super(new BorderLayout());
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("User Management", createUserManagementPanel());
        tabs.addTab("Portal Approvals", createPortalApprovalsPanel());
        
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        DefaultTableModel model = new DefaultTableModel(new Object[] {
                "ID", "Username", "Role" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        UIUtils.applyTableDefaults(table);

        JTextField search = new JTextField(18);
        UIUtils.bindSearch(table, search);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadUsers(model));

        JButton addBtn = new JButton("Add User");
        addBtn.addActionListener(e -> showAddDialog(model));

        JButton editBtn = new JButton("Edit User");
        editBtn.addActionListener(e -> showEditDialog(table, model));

        JButton removeBtn = new JButton("Delete User");
        removeBtn.addActionListener(e -> removeSelectedUser(table, model));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refresh);
        top.add(new JLabel("Search:"));
        top.add(search);
        top.add(addBtn);
        top.add(editBtn);
        top.add(removeBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadUsers(model);
        return panel;
    }

    private JPanel createPortalApprovalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel model = new DefaultTableModel(new Object[] {
                "ID", "Case No", "Title", "Verifying Role", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(model);
        UIUtils.applyTableDefaults(table);

        JButton refresh = new JButton("Refresh Pending");
        refresh.addActionListener(e -> loadPendingVerdicts(model));

        JButton approveBtn = new JButton("Approve & Publish");
        DesignSystem.applyButtonStyle(approveBtn, true);
        approveBtn.addActionListener(e -> approveSelectedVerdict(table, model));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(refresh);
        top.add(approveBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadPendingVerdicts(model);
        return panel;
    }

    private void loadUsers(DefaultTableModel model) {
        new SwingWorker<List<com.crimemanagement.entity.UserAccount>, Void>() {
            @Override
            protected List<com.crimemanagement.entity.UserAccount> doInBackground() throws SQLException {
                return adminService.getAllUsers();
            }
            @Override
            protected void done() {
                try {
                    List<com.crimemanagement.entity.UserAccount> list = get();
                    model.setRowCount(0);
                    for (com.crimemanagement.entity.UserAccount user : list) {
                        model.addRow(new Object[] { user.getUserId(), user.getUsername(), user.getRole() });
                    }
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadPendingVerdicts(DefaultTableModel model) {
        new SwingWorker<List<com.crimemanagement.entity.Case>, Void>() {
            @Override
            protected List<com.crimemanagement.entity.Case> doInBackground() throws SQLException {
                return caseDAO.findAll().stream()
                        .filter(c -> !c.isPublic() && 
                               ((c.getVerdictText() != null && !c.getVerdictText().isEmpty()) || 
                                (c.getArticleText() != null && !c.getArticleText().isEmpty())))
                        .toList();
            }
            @Override
            protected void done() {
                try {
                    List<com.crimemanagement.entity.Case> list = get();
                    model.setRowCount(0);
                    for (com.crimemanagement.entity.Case c : list) {
                        model.addRow(new Object[] { c.getCaseId(), c.getCaseNumber(), c.getCaseTitle(), (c.isVerifiedByCourt() ? "COURT (Verified)" : "POLICE (Draft)"), c.getStatus() });
                    }
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void approveSelectedVerdict(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int caseId = (int) model.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(this, "Verify and publish this verdict to the Citizen Portal?", "Portal Approval", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                com.crimemanagement.entity.Case c = caseDAO.findById(caseId);
                if (c != null) {
                    c.setIsPublic(true);
                    caseDAO.update(c);
                    JOptionPane.showMessageDialog(this, "Verdict successfully published!");
                    loadPendingVerdicts(model);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Approval failed: " + ex.getMessage());
            }
        }
    }

    private void showAddDialog(DefaultTableModel model) {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        String[] roles = { "ADMIN", "OFFICER", "COURT", "FORENSIC_EXAMINER", "PROSECUTOR", "JAILER" };
        JComboBox<String> roleBox = new JComboBox<>(roles);

        JPanel userPanel = DesignSystem.createFormPanel();
        DesignSystem.addField(userPanel, "Username", usernameField, 0);
        DesignSystem.addField(userPanel, "Password", passwordField, 1);
        DesignSystem.addField(userPanel, "Role", roleBox, 2);

        if (JOptionPane.showConfirmDialog(this, userPanel, "Step 1: Register User Account", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String role = (String) roleBox.getSelectedItem();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            com.crimemanagement.entity.UserAccount user = new com.crimemanagement.entity.UserAccount();
            user.setUsername(username);
            user.setPasswordHash(password);
            user.setRole(role);

            try {
                if ("OFFICER".equals(role)) {
                    // Step 2: Officer Details
                    JTextField firstNameField = new JTextField(15);
                    JTextField lastNameField = new JTextField(15);
                    JTextField badgeField = new JTextField(15);
                    String[] ranks = { "CONSTABLE", "SCPO", "ASI", "SI", "INSPECTOR", "ACP", "DYSP", "DCP", "SP", "ADDL_CP", "JOINT_CP", "DIG", "IG", "ADGP", "DGP", "COMMISSIONER" };
                    JComboBox<String> rankBox = new JComboBox<>(ranks);
                    
                    JComboBox<StationItem> stationBox = new JComboBox<>();
                    try {
                        for (com.crimemanagement.entity.PoliceStation s : stationDAO.findAll()) {
                            stationBox.addItem(new StationItem(s.getStationId(), s.getName()));
                        }
                    } catch (Exception ex) {}

                    JTextField salaryField = new JTextField("0", 10);
                    JTextField phoneField = new JTextField(15);
                    JTextField emailField = new JTextField(15);

                    String[] levels = { "STATION_OFFICER", "CIRCLE_OFFICER", "DISTRICT_OFFICER", "COMMISSIONER", "DGP" };
                    JComboBox<String> jurisdictionLevelBox = new JComboBox<>(levels);
                    jurisdictionLevelBox.setSelectedItem("STATION_OFFICER");

                    JPanel officerPanel = DesignSystem.createFormPanel();
                    DesignSystem.addField(officerPanel, "First Name", firstNameField, 0);
                    DesignSystem.addField(officerPanel, "Last Name", lastNameField, 1);
                    DesignSystem.addField(officerPanel, "Badge Number", badgeField, 2);
                    DesignSystem.addField(officerPanel, "Rank", rankBox, 3);
                    DesignSystem.addField(officerPanel, "Assigned Station", stationBox, 4);
                    DesignSystem.addField(officerPanel, "Jurisdiction Level", jurisdictionLevelBox, 5);
                    DesignSystem.addField(officerPanel, "Monthly Salary", salaryField, 6);
                    DesignSystem.addField(officerPanel, "Phone Number", phoneField, 7);
                    DesignSystem.addField(officerPanel, "Email Address", emailField, 8);

                    if (JOptionPane.showConfirmDialog(this, officerPanel, "Step 2: Complete Officer Profile", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        com.crimemanagement.entity.Officer officer = new com.crimemanagement.entity.Officer();
                        officer.setFirstName(firstNameField.getText().trim());
                        officer.setLastName(lastNameField.getText().trim());
                        officer.setBadgeNumber(badgeField.getText().trim());
                        officer.setRank((String) rankBox.getSelectedItem());
                        
                        StationItem selectedStation = (StationItem) stationBox.getSelectedItem();
                        if (selectedStation != null) officer.setStationId(selectedStation.id);
                        
                        try { officer.setSalary(Double.parseDouble(salaryField.getText().trim())); } catch (Exception ex) {}
                        officer.setPhoneNumber(phoneField.getText().trim());
                        officer.setEmail(emailField.getText().trim());
                        officer.setIsOnDuty(true);
                        officer.setJurisdictionLevel((String) jurisdictionLevelBox.getSelectedItem());

                        adminService.createOfficerUser(user, officer);
                        JOptionPane.showMessageDialog(this, "Officer account and profile created successfully!");
                    } else {
                        return; // Cancelled at step 2
                    }
                } else if ("COURT".equals(role) || "JAILER".equals(role) || "FORENSIC_EXAMINER".equals(role)) {
                    JOptionPane.showMessageDialog(this, "Master Account Created.\nPhase 2: Complete " + role + " profile integration now.");
                    adminService.createUser(user);
                } else {
                    adminService.createUser(user);
                    JOptionPane.showMessageDialog(this, "System user account created successfully!");
                }
                loadUsers(model);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error creating account: " + e.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        try {
            com.crimemanagement.entity.UserAccount user = adminService.getUserById(id);
            if (user == null) return;
            JTextField userF = new JTextField(user.getUsername());
            JPasswordField passF = new JPasswordField(15); // Label: (Leave blank to keep current)
            JComboBox<String> roleB = new JComboBox<>(new String[]{ "ADMIN", "OFFICER", "COURT", "FORENSIC_EXAMINER", "PROSECUTOR", "JAILER" });
            roleB.setSelectedItem(user.getRole());
            
            JPanel panel = DesignSystem.createFormPanel();
            DesignSystem.addField(panel, "Username", userF, 0);
            DesignSystem.addField(panel, "New Password", passF, 1);
            
            JLabel tip = new JLabel("(Leave blank to keep existing password)");
            tip.setFont(DesignSystem.CAPTION_FONT);
            tip.setForeground(DesignSystem.TEXT_SECONDARY);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
            panel.add(tip, gbc);
            
            DesignSystem.addField(panel, "Role", roleB, 3);
            
            if (JOptionPane.showConfirmDialog(this, panel, "Update User Profile", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                user.setUsername(userF.getText().trim());
                String newPass = new String(passF.getPassword());
                if (!newPass.isEmpty()) {
                    user.setPasswordHash(newPass); // AdminService handles hashing
                }
                user.setRole((String) roleB.getSelectedItem());
                adminService.updateUser(user);
                loadUsers(model);
            }
        } catch (Exception e) {}
    }

    private void removeSelectedUser(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete user?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { adminService.deleteUser(id); loadUsers(model); } catch (Exception e) {}
        }
    }
}
