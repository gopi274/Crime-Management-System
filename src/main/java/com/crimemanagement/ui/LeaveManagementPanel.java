package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.dao.LeaveRequestDAO;
import com.crimemanagement.entity.LeaveRequest;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LeaveManagementPanel extends JPanel {

    private final LeaveRequestDAO dao = new LeaveRequestDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private LoginDialog.Role userRole;
    private Integer officerId;
    private String activeOfficerRank;

    private static final java.util.List<String> RANKS = java.util.Arrays.asList(
        "CONSTABLE", "SCPO", "ASI", "SI", "INSPECTOR", "ACP", "DYSP", "DCP", "SP", "ADDL_CP", "JOINT_CP", "DIG", "IG", "ADGP", "DGP", "COMMISSIONER"
    );

    private int getRankLevel(String rank) {
        if (rank == null) return -1;
        int idx = RANKS.indexOf(rank.toUpperCase());
        return idx != -1 ? idx : RANKS.indexOf(rank); // Try case-sensitive if not found
    }

    public LeaveManagementPanel(LoginDialog.Role role, Integer officerId) {
        this.userRole = role;
        this.officerId = officerId;
        setLayout(new BorderLayout(15, 15));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Leave Management Portal");
        title.setFont(DesignSystem.TITLE_FONT);
        title.setForeground(DesignSystem.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        // Fetch current officer rank for hierarchy logic
        activeOfficerRank = "CONSTABLE";
        if (officerId != null) {
            try {
                com.crimemanagement.entity.Officer o = new com.crimemanagement.dao.OfficerDAO().findById(officerId);
                if (o != null) activeOfficerRank = o.getRank();
            } catch (Exception ex) {}
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        
        JButton applyBtn = new JButton("Apply for Leave");
        DesignSystem.applyButtonStyle(applyBtn, true);
        applyBtn.addActionListener(e -> applyForLeave());
        if (officerId == null) applyBtn.setVisible(false);
        actions.add(applyBtn);

        JButton approveBtn = new JButton("Sanction Subordinate Leave");
        DesignSystem.applyButtonStyle(approveBtn, true);
        approveBtn.addActionListener(e -> updateStatus(activeOfficerRank));
        if (role != LoginDialog.Role.ADMIN && role != LoginDialog.Role.OFFICER) {
            approveBtn.setVisible(false);
        }
        actions.add(approveBtn);

        JButton refreshBtn = new JButton("Refresh");
        DesignSystem.applyButtonStyle(refreshBtn, false);
        refreshBtn.addActionListener(e -> refreshData(activeOfficerRank));
        actions.add(refreshBtn);

        header.add(actions, BorderLayout.NORTH);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Officer Name", "Rank", "Type", "Start Date", "End Date", "Reason", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.applyTableDefaults(table);
        table.setRowHeight(35);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(DesignSystem.BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private void refreshData(String myRank) {
        new SwingWorker<List<LeaveRequest>, Void>() {
            @Override
            protected List<LeaveRequest> doInBackground() throws Exception {
                List<LeaveRequest> all = dao.findAllJoined();
                if (userRole == LoginDialog.Role.ADMIN) return all;
                
                int myLevel = getRankLevel(myRank);
                return all.stream()
                    .filter(lr -> lr.getOfficerId() == officerId || getRankLevel(lr.getRank()) < myLevel)
                    .toList();
            }
            @Override
            protected void done() {
                try {
                    List<LeaveRequest> list = get();
                    tableModel.setRowCount(0);
                    for (LeaveRequest lr : list) {
                        tableModel.addRow(new Object[]{
                                lr.getLeaveId(),
                                lr.getOfficerName(),
                                lr.getRank(),
                                lr.getLeaveType(),
                                lr.getStartDate(),
                                lr.getEndDate(),
                                lr.getReason(),
                                lr.getStatus()
                        });
                    }
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void applyForLeave() {
        if (officerId == null) return;
        JComboBox<String> typeField = new JComboBox<>(new String[]{"Casual", "Sick", "Earned", "Maternity/Paternity", "Special"});
        JComponent startField = DesignSystem.createDatePicker();
        JComponent endField = DesignSystem.createDatePicker();
        JTextField reasonField = new JTextField(20);

        JPanel panel = DesignSystem.createFormPanel();
        DesignSystem.addField(panel, "Leave Type", typeField, 0);
        DesignSystem.addField(panel, "Start Date", startField, 1);
        DesignSystem.addField(panel, "End Date", endField, 2);
        DesignSystem.addField(panel, "Reason", reasonField, 3);

        if (JOptionPane.showConfirmDialog(this, panel, "Apply for Leave", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            LeaveRequest lr = new LeaveRequest();
            lr.setOfficerId(officerId);
            lr.setLeaveType((String) typeField.getSelectedItem());
            lr.setReason(reasonField.getText().trim());
            lr.setStatus("PENDING");
            try {
                lr.setStartDate(java.time.LocalDate.parse(((JSpinner.DateEditor)((JSpinner)startField).getEditor()).getTextField().getText()));
                lr.setEndDate(java.time.LocalDate.parse(((JSpinner.DateEditor)((JSpinner)endField).getEditor()).getTextField().getText()));
                dao.create(lr);
                refreshData();
                JOptionPane.showMessageDialog(this, "Leave application submitted.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error applying for leave: " + ex.getMessage());
            }
        }
    }

    private void updateStatus(String myRank) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a leave request first.");
            return;
        }

        String applicantRank = (String) tableModel.getValueAt(row, 2);
        int applicantId = -1;
        try {
            int selLeaveId = (int) tableModel.getValueAt(row, 0);
            LeaveRequest selLr = dao.findAllJoined().stream()
                .filter(r -> r.getLeaveId() == selLeaveId)
                .findFirst()
                .orElse(null);
            if (selLr != null) applicantId = selLr.getOfficerId();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error identifying applicant: " + ex.getMessage());
            return;
        }
        
        if (userRole != LoginDialog.Role.ADMIN && getRankLevel(myRank) <= getRankLevel(applicantRank)) {
            JOptionPane.showMessageDialog(this, "You can only approve leave for subordinate officers.");
            return;
        }
        
        if (userRole != LoginDialog.Role.ADMIN && officerId != null && applicantId == officerId) {
            JOptionPane.showMessageDialog(this, "You cannot approve your own leave.");
            return;
        }

        int leaveId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 7);

        String[] options = {"PENDING", "APPROVED", "REJECTED"};
        String choice = (String) JOptionPane.showInputDialog(this, "Set Leave Status:", "Update",
                JOptionPane.QUESTION_MESSAGE, null, options, currentStatus);

        if (choice != null && !choice.equals(currentStatus)) {
            try {
                dao.updateStatus(leaveId, choice);
                
                // Deduction Business Logic: If status becomes APPROVED, deduct from balance
                if ("APPROVED".equals(choice)) {
                    int selLeaveId = (int) tableModel.getValueAt(row, 0);
                    LeaveRequest lr = dao.findAllJoined().stream().filter(r -> r.getLeaveId() == selLeaveId).findFirst().orElse(null);
                    if (lr != null) {
                        com.crimemanagement.dao.OfficerDAO oDao = new com.crimemanagement.dao.OfficerDAO();
                        com.crimemanagement.entity.Officer applicant = oDao.findById(lr.getOfficerId());
                        if (applicant != null) {
                            long days = java.time.temporal.ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1;
                            int newBalance = (applicant.getLeaveBalance() != null ? applicant.getLeaveBalance() : 30) - (int)days;
                            applicant.setLeaveBalance(newBalance);
                            oDao.update(applicant);
                            JOptionPane.showMessageDialog(this, "Leave sanctioned. Officer's new leave balance: " + newBalance);
                        }
                    }
                }
                
                refreshData(myRank);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
            }
        }
    }
    
    private void refreshData() {
        refreshData(activeOfficerRank != null ? activeOfficerRank : "CONSTABLE");
    }
}
