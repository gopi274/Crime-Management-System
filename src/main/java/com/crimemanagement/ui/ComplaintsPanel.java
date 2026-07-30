package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.dao.ComplaintDAO;
import com.crimemanagement.entity.Complaint;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ComplaintsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private ComplaintDAO dao;
    private Integer stationId;

    public ComplaintsPanel(LoginDialog.Role userRole, Integer stationId) {
        this.stationId = stationId;
        setLayout(new BorderLayout(15, 15));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dao = new ComplaintDAO();

        JLabel title = new JLabel("Citizen e-Complaints Queue");
        title.setFont(DesignSystem.TITLE_FONT);
        title.setForeground(DesignSystem.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        DesignSystem.applyButtonStyle(refreshBtn, false);
        refreshBtn.addActionListener(e -> refreshData());

        JButton updateBtn = new JButton("Update Status");
        DesignSystem.applyButtonStyle(updateBtn, true);

        updateBtn.addActionListener(e -> updateStatusAction());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(refreshBtn);
        actions.add(updateBtn);
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Tracking ID", "Location", "Description", "Status", "Date Reported"};
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

    private void refreshData() {
        tableModel.setRowCount(0);
        try {
            List<Complaint> entries = dao.getAll(stationId);
            for (Complaint c : entries) {
                tableModel.addRow(new Object[]{
                    c.getComplaintId(),
                    c.getTrackingId(),
                    c.getLocation(),
                    c.getDescription(),
                    c.getStatus(),
                    c.getCreatedAt()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load complaints: " + e.getMessage());
        }
    }

    private void updateStatusAction() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a complaint first.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 4);

        String[] options = {"PENDING", "VERIFIED", "REJECTED"};
        String choice = (String) JOptionPane.showInputDialog(this, "Select new status:", "Update Status",
                JOptionPane.QUESTION_MESSAGE, null, options, currentStatus);

        if (choice != null && choice.equals("VERIFIED")) {
            // Assignment logic
            try {
                java.util.List<com.crimemanagement.entity.PoliceStation> stations = new com.crimemanagement.dao.PoliceStationDAO().findAll();
                JComboBox<String> stationBox = new JComboBox<>();
                for(com.crimemanagement.entity.PoliceStation s : stations) stationBox.addItem(s.getName());
                
                JComboBox<String> officerBox = new JComboBox<>();
                
                stationBox.addActionListener(e -> {
                    String selSt = (String) stationBox.getSelectedItem();
                    officerBox.removeAllItems();
                    try {
                        com.crimemanagement.entity.PoliceStation st = stations.stream().filter(s -> s.getName().equals(selSt)).findFirst().orElse(null);
                        if (st != null) {
                            java.util.List<com.crimemanagement.entity.Officer> officers = new com.crimemanagement.dao.OfficerDAO().findAll().stream()
                                .filter(o -> o.getStationId() != null && o.getStationId().equals(st.getStationId()))
                                .toList();
                            for (com.crimemanagement.entity.Officer o : officers) {
                                officerBox.addItem(o.getOfficerId() + " - " + o.getRank() + " " + o.getFirstName());
                            }
                        }
                    } catch (Exception ex) {}
                });
                
                // Trigger initial load
                stationBox.setSelectedIndex(0);

                JPanel assignPanel = DesignSystem.createFormPanel();
                DesignSystem.addField(assignPanel, "Target Police Station", stationBox, 0);
                DesignSystem.addField(assignPanel, "Assigning Officer", officerBox, 1);
                
                if (JOptionPane.showConfirmDialog(this, assignPanel, "Identify Jurisdictional Authority", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    String selectedStation = (String) stationBox.getSelectedItem();
                    com.crimemanagement.entity.PoliceStation st = stations.stream().filter(s -> s.getName().equals(selectedStation)).findFirst().orElse(null);
                    
                    String selectedOfficer = (String) officerBox.getSelectedItem();
                    Integer officerId = selectedOfficer == null ? null : Integer.parseInt(selectedOfficer.split(" - ")[0]);
                    
                    if (st != null) {
                        try (java.sql.Connection conn = com.crimemanagement.util.DatabaseConnection.getConnection();
                             java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE complaints SET status='VERIFIED', matched_station_id=?, assigned_officer_id=? WHERE complaint_id=?")) {
                            ps.setInt(1, st.getStationId());
                            if (officerId != null) ps.setInt(2, officerId); else ps.setNull(2, java.sql.Types.INTEGER);
                            ps.setInt(3, id);
                            ps.executeUpdate();
                            
                            com.crimemanagement.service.AuditService.log("ADMIN", "COMPLAINT_VERIFIED", "Complaint ID " + id + " routed to " + selectedStation);
                            refreshData();
                            JOptionPane.showMessageDialog(this, "Complaint verified & successfully routed to " + selectedStation);
                        }
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }
            return;
        }

        if (choice != null && !choice.equals(currentStatus)) {
            try {
                dao.updateStatus(id, choice);
                refreshData();
                JOptionPane.showMessageDialog(this, "Status updated to " + choice);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
            }
        }
    }
}
