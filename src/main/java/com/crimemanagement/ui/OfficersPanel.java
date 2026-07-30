package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.entity.Officer;
import com.crimemanagement.service.OfficerService;
import com.crimemanagement.service.AuditService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OfficersPanel extends JPanel {
    private final OfficerService officerService = new OfficerService();
    private DefaultTableModel tableModel;
    private JTable table;
    private final com.crimemanagement.dao.PoliceStationDAO stationDAO = new com.crimemanagement.dao.PoliceStationDAO();

    private static class StationItem {
        final Integer id;
        final String name;
        StationItem(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name + " (ID: " + id + ")"; }
    }

    public OfficersPanel() {
        setup();
    }

    private void setup() {
        super.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(
                new Object[] { "ID", "Badge", "First Name", "Last Name", "Rank", "Station ID", "Jurisdiction", "Phone", "Email", "On Duty" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIUtils.applyTableDefaults(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);
        
        JTextField search = new JTextField(15);
        UIUtils.bindSearch(table, search);
        
        // Rank Filter
        String[] rankFilterOpts = new String[RANKS.size() + 1];
        rankFilterOpts[0] = "ALL RANKS";
        for(int i=0; i<RANKS.size(); i++) rankFilterOpts[i+1] = RANKS.get(i);
        JComboBox<String> rankFilter = new JComboBox<>(rankFilterOpts);
        
        // Station Filter
        JComboBox<String> stationFilter = new JComboBox<>();
        stationFilter.addItem("ALL STATIONS");
        try {
            for (com.crimemanagement.entity.PoliceStation s : stationDAO.findAll()) {
                stationFilter.addItem(s.getName());
            }
        } catch (Exception ex) {}

        JButton refreshBtn = DesignSystem.createPrimaryButton("Refresh");
        JButton addBtn = DesignSystem.createPrimaryButton("Add");
        JButton editBtn = DesignSystem.createPrimaryButton("Edit");
        JButton deleteBtn = DesignSystem.createPrimaryButton("Delete");
        
        top.add(new JLabel("🔍 SEARCH:"));
        top.add(search);
        top.add(new JLabel("RANK:"));
        top.add(rankFilter);
        top.add(new JLabel("STATION:"));
        top.add(stationFilter);
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(editBtn);
        top.add(deleteBtn);

        // Filter Action
        java.awt.event.ActionListener filterAction = e -> {
            String selRank = (String) rankFilter.getSelectedItem();
            String selStation = (String) stationFilter.getSelectedItem();
            applyFilters(selRank, selStation);
        };
        rankFilter.addActionListener(filterAction);
        stationFilter.addActionListener(filterAction);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> loadOfficers());
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());

        loadOfficers();
    }

    private static final java.util.List<String> RANKS = java.util.Arrays.asList(
        "CONSTABLE", "SCPO", "ASI", "SI", "INSPECTOR", "ACP", "DYSP", "DCP", "SP", "ADDL_CP", "JOINT_CP", "DIG", "IG", "ADGP", "DGP", "COMMISSIONER",
        "DIRECTOR", "JOINT_DIRECTOR", "ASSISTANT_DIRECTOR", "SSO", "SO", "SSA"
    );

    // Removed getRankLevel as filtering is now based on name matching

    private java.util.List<Officer> cachedOfficers = new java.util.ArrayList<>();
    
    private void applyFilters(String rank, String station) {
        tableModel.setRowCount(0);
        for (Officer o : cachedOfficers) {
            boolean rankMatch = rank.equals("ALL RANKS") || (o.getRank() != null && o.getRank().equalsIgnoreCase(rank));
            boolean stationMatch = station.equals("ALL STATIONS");
            if (!stationMatch && o.getStationId() != null) {
                try {
                    com.crimemanagement.entity.PoliceStation ps = stationDAO.findById(o.getStationId());
                    if (ps != null && ps.getName().equals(station)) stationMatch = true;
                } catch (Exception ex) {}
            }
            
            if (rankMatch && stationMatch) {
                tableModel.addRow(new Object[] {
                    o.getOfficerId(), o.getBadgeNumber(), o.getFirstName(), o.getLastName(),
                    o.getRank(), o.getStationId(), o.getJurisdictionLevel(), o.getPhoneNumber(), o.getEmail(), o.getIsOnDuty() ? "Yes" : "No"
                });
            }
        }
    }

    private void loadOfficers() {
        new SwingWorker<List<Officer>, Void>() {
            @Override protected List<Officer> doInBackground() throws Exception {
                return officerService.getAllOfficers();
            }
            @Override protected void done() {
                try {
                    cachedOfficers = get();
                    applyFilters("ALL RANKS", "ALL STATIONS");
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void onAdd() {
        JTextField badge = new JTextField();
        JTextField first = new JTextField();
        JTextField last = new JTextField();
        JComboBox<String> rankBox = new JComboBox<>(RANKS.toArray(new String[0]));
        
        JComboBox<StationItem> stationBox = new JComboBox<>();
        try {
            for (com.crimemanagement.entity.PoliceStation s : stationDAO.findAll()) {
                StationItem si = new StationItem(s.getStationId(), s.getName());
                stationBox.addItem(si);
            }
        } catch (Exception ex) {}

        JTextField levelBox = new JTextField();
        stationBox.addActionListener(e -> {
            StationItem sel = (StationItem) stationBox.getSelectedItem();
            if (sel != null) levelBox.setText(sel.name);
        });
        // Initial set
        if (stationBox.getItemCount() > 0) levelBox.setText(((StationItem)stationBox.getItemAt(0)).name);
        
        JTextField phone = new JTextField();
        JTextField email = new JTextField();
        String[] genders = { "M", "F", "Other" };
        JComboBox<String> genderBox = new JComboBox<>(genders);
        JCheckBox onDuty = new JCheckBox("Is On Duty");

        JPanel panel = DesignSystem.createFormPanel();
        DesignSystem.addField(panel, "Badge", badge, 0);
        DesignSystem.addField(panel, "First Name", first, 1);
        DesignSystem.addField(panel, "Last Name", last, 2);
        DesignSystem.addField(panel, "Rank", rankBox, 3);
        DesignSystem.addField(panel, "Gender", genderBox, 4);
        DesignSystem.addField(panel, "Station", stationBox, 5);
        DesignSystem.addField(panel, "Jurisdiction", levelBox, 6);
        DesignSystem.addField(panel, "Phone", phone, 7);
        DesignSystem.addField(panel, "Email", email, 8);
        DesignSystem.addField(panel, "Status", onDuty, 9);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Officer", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        Officer o = new Officer();
        o.setBadgeNumber(badge.getText().trim());
        o.setFirstName(first.getText().trim());
        o.setLastName(last.getText().trim());
        o.setRank((String) rankBox.getSelectedItem());
        
        StationItem selectedStation = (StationItem) stationBox.getSelectedItem();
        if (selectedStation != null) o.setStationId(selectedStation.id);
        
        o.setJurisdictionLevel(levelBox.getText().trim());
        o.setGender((String) genderBox.getSelectedItem());
        o.setPhoneNumber(phone.getText().trim().isEmpty() ? null : phone.getText().trim());
        o.setEmail(email.getText().trim().isEmpty() ? null : email.getText().trim());
        o.setIsOnDuty(onDuty.isSelected());

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                officerService.createOfficer(o);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    AuditService.log("ADMIN", "ADD_OFFICER", "Appointed new officer: " + first.getText() + " " + last.getText() + " (" + rankBox.getSelectedItem() + ")");
                    loadOfficers();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(OfficersPanel.this, "Failed to add officer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Integer id = (Integer) tableModel.getValueAt(modelRow, 0);

        SwingWorker<Officer, Void> loadWorker = new SwingWorker<>() {
            @Override
            protected Officer doInBackground() throws Exception {
                return officerService.getOfficerById(id);
            }
            @Override
            protected void done() {
                try {
                    Officer o = get();
                    if (o == null) return;
                    JTextField badge = new JTextField(o.getBadgeNumber());
                    JTextField first = new JTextField(o.getFirstName());
                    JTextField last = new JTextField(o.getLastName());
                    JComboBox<String> rankBox = new JComboBox<>(RANKS.toArray(new String[0]));
                    rankBox.setSelectedItem(o.getRank());
                    
                    JComboBox<StationItem> stationBox = new JComboBox<>();
                    try {
                        for (com.crimemanagement.entity.PoliceStation s : stationDAO.findAll()) {
                            StationItem si = new StationItem(s.getStationId(), s.getName());
                            stationBox.addItem(si);
                            if (o.getStationId() != null && s.getStationId() == (int)o.getStationId()) stationBox.setSelectedItem(si);
                        }
                    } catch (Exception ex) {}

                    final JTextField levelBox = new JTextField(o.getJurisdictionLevel());
                    stationBox.addActionListener(e -> {
                        StationItem sel = (StationItem) stationBox.getSelectedItem();
                        if (sel != null) levelBox.setText(sel.name);
                    });

                    JTextField phone = new JTextField(o.getPhoneNumber());
                    JTextField email = new JTextField(o.getEmail());
                    String[] genders = { "M", "F", "Other" };
                    JComboBox<String> genderBox = new JComboBox<>(genders);
                    genderBox.setSelectedItem(o.getGender());
                    JCheckBox onDuty = new JCheckBox("Is On Duty", o.getIsOnDuty() != null && o.getIsOnDuty());

                    JPanel panel = DesignSystem.createFormPanel();
                    DesignSystem.addField(panel, "Badge", badge, 0);
                    DesignSystem.addField(panel, "First Name", first, 1);
                    DesignSystem.addField(panel, "Last Name", last, 2);
                    DesignSystem.addField(panel, "Rank", rankBox, 3);
                    DesignSystem.addField(panel, "Gender", genderBox, 4);
                    DesignSystem.addField(panel, "Station", stationBox, 5);
                    DesignSystem.addField(panel, "Jurisdiction", levelBox, 6);
                    DesignSystem.addField(panel, "Phone", phone, 7);
                    DesignSystem.addField(panel, "Email", email, 8);
                    DesignSystem.addField(panel, "Status", onDuty, 9);

                    if (JOptionPane.showConfirmDialog(OfficersPanel.this, panel, "Edit Officer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

                    o.setBadgeNumber(badge.getText().trim());
                    o.setFirstName(first.getText().trim());
                    o.setLastName(last.getText().trim());
                    o.setRank((String) rankBox.getSelectedItem());
                    
                    StationItem selectedStation = (StationItem) stationBox.getSelectedItem();
                    if (selectedStation != null) o.setStationId(selectedStation.id);
                    else o.setStationId(null);

                    o.setJurisdictionLevel(levelBox.getText().trim());
                    o.setGender((String) genderBox.getSelectedItem());
                    o.setPhoneNumber(phone.getText().trim().isEmpty() ? null : phone.getText().trim());
                    o.setEmail(email.getText().trim().isEmpty() ? null : email.getText().trim());
                    o.setIsOnDuty(onDuty.isSelected());

                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() throws Exception { officerService.updateOfficer(o); return null; }
                        @Override protected void done() { 
                            try { 
                                get(); 
                                AuditService.log("ADMIN", "EDIT_OFFICER", "Updated officer record for ID: " + id + " (" + o.getFirstName() + ")");
                                loadOfficers(); 
                            } catch (Exception ex) {} 
                        }
                    }.execute();
                } catch (Exception ex) {}
            }
        };
        loadWorker.execute();
    }

    private void onDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Integer id = (Integer) tableModel.getValueAt(modelRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete officer ID " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception { officerService.deleteOfficer(id); return null; }
                @Override protected void done() { 
                    try { 
                        get(); 
                        AuditService.log("ADMIN", "DELETE_OFFICER", "Decommissioned officer ID: " + id);
                        loadOfficers(); 
                    } catch (Exception ex) {} 
                }
            }.execute();
        }
    }
}
