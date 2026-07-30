package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.dao.OfficerDAO;
import com.crimemanagement.entity.Officer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class LiveCommandPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private OfficerDAO dao;

    private final LoginDialog.Role userRole;
    private final Integer stationId;

    public LiveCommandPanel(LoginDialog.Role role, Integer stationId) {
        this.userRole = role;
        this.stationId = stationId;
        
        setLayout(new BorderLayout(15, 15));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dao = new OfficerDAO();

        JLabel title = new JLabel("Live Command & Dispatch (Active Rosters)");
        title.setFont(DesignSystem.TITLE_FONT);
        title.setForeground(DesignSystem.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Status");
        DesignSystem.applyButtonStyle(refreshBtn, true);
        refreshBtn.addActionListener(e -> refreshData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(refreshBtn);
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        String[] cols = {"Badge Number", "Name", "Rank", "Level", "Station ID", "Phone Number", "Assigned Load"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
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

        Timer autoRefresh = new Timer(30000, e -> refreshData()); // 30 sec auto refresh
        autoRefresh.start();
    }

    private void refreshData() {
        tableModel.setRowCount(0);
        try {
            List<Officer> activeOfficers = dao.findAll().stream()
                .filter(o -> o.getIsOnDuty() != null && o.getIsOnDuty())
                .filter(o -> {
                    // Inspectors/Officers only see their own station. Admin/Commissioner see all.
                    if (userRole == LoginDialog.Role.OFFICER && stationId != null) {
                        return stationId.equals(o.getStationId());
                    }
                    return true;
                })
                .collect(Collectors.toList());
                
            for (Officer o : activeOfficers) {
                String assignedCases = dao.getAssignedFIRNumbers(o.getOfficerId());
                
                tableModel.addRow(new Object[]{
                    o.getBadgeNumber(),
                    o.getFirstName() + " " + o.getLastName(),
                    o.getRank(),
                    o.getJurisdictionLevel(),
                    o.getStationId() != null ? o.getStationId() : "HQ",
                    o.getPhoneNumber(),
                    assignedCases
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch live rosters: " + e.getMessage());
        }
    }
}
