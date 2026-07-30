package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.entity.FIR;
import com.crimemanagement.service.FIRService;

import com.crimemanagement.util.BNSConstants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import com.crimemanagement.util.DatabaseConnection;



public class FIRsPanel extends JPanel {
    private final FIRService service = new FIRService();
    private DefaultTableModel model;
    private JTable table;
    private final LoginDialog.Role userRole;
    private Integer stationId;

    public FIRsPanel(LoginDialog.Role role, Integer stationId) {
        this.userRole = role;
        this.stationId = stationId;
        setup();
    }

    public FIRsPanel() {
        this.userRole = LoginDialog.Role.ADMIN;
        setup();
    }

    private void setup() {
        super.setLayout(new BorderLayout());
        model = new DefaultTableModel(new Object[] {
                "ID", "FIR Number", "Crime Type", "Description", "Location", "Incident Date",
                "Reported Date", "Complainant", "Status", "Assigned Officer" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        UIUtils.applyTableDefaults(table);

        add(new JScrollPane(table), BorderLayout.CENTER);
        JTextField search = new JTextField(18);
        UIUtils.bindSearch(table, search);
        JButton refresh = new JButton("Refresh");
        JButton addButton = new JButton("Add FIR");
        JButton editButton = new JButton("Edit FIR");
        JButton removeButton = new JButton("Remove FIR");

        boolean isReadOnly = userRole == LoginDialog.Role.COURT;
        addButton.setEnabled(!isReadOnly);
        editButton.setEnabled(!isReadOnly);
        removeButton.setEnabled(!isReadOnly);

        refresh.addActionListener(e -> load());
        addButton.addActionListener(e -> onAdd());
        editButton.addActionListener(e -> onEdit());
        removeButton.addActionListener(e -> onRemove());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(search);
        top.add(refresh);
        top.add(addButton);
        top.add(editButton);
        top.add(removeButton);
        add(top, BorderLayout.NORTH);
        
        load();
    }

    private void load() {
        SwingWorker<List<FIR>, Void> w = new SwingWorker<>() {
            @Override
            protected List<FIR> doInBackground() throws Exception {
                try {
                    return service.getAllFIRs(stationId);
                } catch (Exception ex) {
                    return java.util.Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<FIR> list = get();
                    model.setRowCount(0);
                    for (FIR f : list) {
                        model.addRow(new Object[] {
                                f.getFirId(),
                                f.getFirNumber(),
                                f.getCrimeType(),
                                f.getCrimeDescription(),
                                f.getLocation(),
                                f.getDateOfIncident() != null ? f.getDateOfIncident().toString().replace('T', ' ') : "N/A",
                                f.getDateReported() != null ? f.getDateReported().toString().replace('T', ' ') : "N/A",
                                f.getComplainantName() != null ? f.getComplainantName() : (f.getCitizenId() != null ? "ID: " + f.getCitizenId() : "N/A"),
                                f.getStatus(),
                                f.getAssignedOfficerName() != null ? f.getAssignedOfficerName() : "N/A"
                        });
                    }
                } catch (Exception ex) {}
            }
        };
        w.execute();
    }

    
    private JComboBox<String> createCitizenDropdown() {
        JComboBox<String> box = new JComboBox<>();
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT citizen_id, name FROM citizens")) {
            while (rs.next()) box.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception e) {}
        return box;
    }
    
    private JComboBox<String> createOfficerDropdown(String stationName) {
        JComboBox<String> box = new JComboBox<>();
        String sql = "SELECT o.officer_id, o.first_name, o.last_name, o.rank FROM officers o " +
                     "JOIN police_stations s ON o.station_id = s.station_id WHERE s.name = ?";
        
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stationName);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) box.addItem(rs.getInt(1) + " - " + rs.getString(4) + " " + rs.getString(2) + " " + rs.getString(3));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return box;
    }

    private JComboBox<String> createLocationDropdown() {
        JComboBox<String> box = new JComboBox<>();
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT name FROM police_stations ORDER BY name")) {
            while (rs.next()) box.addItem(rs.getString(1));
        } catch (Exception e) {}
        return box;
    }

    private void onAdd() {
        JTextField firNumberField = new JTextField();
        JList<String> crimeTypeField = new JList<>(BNSConstants.SECTIONS);
        crimeTypeField.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane crimeScroll = new JScrollPane(crimeTypeField);
        crimeScroll.setPreferredSize(new Dimension(300, 100));

        JTextField crimeDescField = new JTextField();
        JComboBox<String> locationField = createLocationDropdown();
        JComboBox<String> citizenIdField = createCitizenDropdown();
        JComboBox<String> officerIdField = new JComboBox<>();
        
        // Dynamic filtering
        locationField.addActionListener(e -> {
            String loc = (String) locationField.getSelectedItem();
            officerIdField.removeAllItems();
            JComboBox<String> filtered = createOfficerDropdown(loc);
            for(int i=0; i<filtered.getItemCount(); i++) officerIdField.addItem(filtered.getItemAt(i));
        });
        // Init
        if(locationField.getItemCount() > 0) {
            String initialLoc = (String) locationField.getSelectedItem();
            JComboBox<String> filtered = createOfficerDropdown(initialLoc);
            for(int i=0; i<filtered.getItemCount(); i++) officerIdField.addItem(filtered.getItemAt(i));
        }
        JTextField statusField = new JTextField("Open");
        JComponent dateIncidentField = DesignSystem.createDatePicker();
        JComponent dateReportedField = DesignSystem.createDatePicker();

        JPanel panel = DesignSystem.createFormPanel();
        DesignSystem.addField(panel, "FIR Number", firNumberField, 0);
        DesignSystem.addField(panel, "Type (BNS)", crimeScroll, 1);
        DesignSystem.addField(panel, "Description", crimeDescField, 2);
        DesignSystem.addField(panel, "Location", locationField, 3);
        DesignSystem.addField(panel, "Citizen ID", citizenIdField, 4);
        DesignSystem.addField(panel, "Officer ID", officerIdField, 5);
        DesignSystem.addField(panel, "Status", statusField, 6);
        DesignSystem.addField(panel, "Incident Date", dateIncidentField, 7);
        DesignSystem.addField(panel, "Reported Date", dateReportedField, 8);

        if (JOptionPane.showConfirmDialog(this, panel, "Add FIR", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            FIR fir = new FIR();
            fir.setFirNumber(firNumberField.getText().trim());
            
            java.util.List<String> selected = new java.util.ArrayList<>();
            for (String val : crimeTypeField.getSelectedValuesList()) selected.add(val.split(":")[0]);
            fir.setCrimeType(String.join(", ", selected));

            fir.setCrimeDescription(crimeDescField.getText().trim());
            fir.setLocation((String)locationField.getSelectedItem());
            
            try { fir.setCitizenId(citizenIdField.getSelectedItem() == null ? null : Integer.parseInt(((String)citizenIdField.getSelectedItem()).split(" ")[0])); } catch (Exception e) {}
            try { fir.setAssignedOfficerId(officerIdField.getSelectedItem() == null ? null : Integer.parseInt(((String)officerIdField.getSelectedItem()).split(" ")[0])); } catch (Exception e) {}
            
            fir.setStatus(statusField.getText().trim());
            try { 
                String strD = ((JSpinner.DateEditor)((JSpinner)dateIncidentField).getEditor()).getTextField().getText();
                fir.setDateOfIncident(java.time.LocalDate.parse(strD).atStartOfDay()); 
            } catch (Exception e) {}
            try { 
                String strR = ((JSpinner.DateEditor)((JSpinner)dateReportedField).getEditor()).getTextField().getText();
                fir.setDateReported(java.time.LocalDate.parse(strR).atStartOfDay()); 
            } catch (Exception e) {}

            com.crimemanagement.dao.FIRDAO dao = new com.crimemanagement.dao.FIRDAO();
            try {
                if (dao.addFIR(fir)) {
                    JOptionPane.showMessageDialog(this, "FIR added!");
                    load();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int firId = (int) model.getValueAt(row, 0);

        new SwingWorker<FIR, Void>() {
            @Override
            protected FIR doInBackground() throws Exception { return service.getFIRById(firId); }
            @Override
            protected void done() {
                try {
                    FIR fir = get();
                    if (fir == null) return;

                    JTextField firNumberField = new JTextField(fir.getFirNumber());
                    JList<String> crimeTypeField = new JList<>(BNSConstants.SECTIONS);
                    crimeTypeField.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    if (fir.getCrimeType() != null) {
                        String[] current = fir.getCrimeType().split(", ");
                        java.util.List<Integer> indices = new java.util.ArrayList<>();
                        for (String c : current) {
                            for (int i=0; i<crimeTypeField.getModel().getSize(); i++) {
                                if (crimeTypeField.getModel().getElementAt(i).startsWith(c)) indices.add(i);
                            }
                        }
                        crimeTypeField.setSelectedIndices(indices.stream().mapToInt(i->i).toArray());
                    }
                    JScrollPane crimeScroll = new JScrollPane(crimeTypeField);
                    crimeScroll.setPreferredSize(new Dimension(300, 100));

                    JTextField crimeDescField = new JTextField(fir.getCrimeDescription());
                    JComboBox<String> locationField = createLocationDropdown();
                    locationField.setSelectedItem(fir.getLocation());
                    JComboBox<String> citizenIdField = createCitizenDropdown();
                    if (fir.getCitizenId() != null) { for(int i=0;i<citizenIdField.getItemCount();i++) if(citizenIdField.getItemAt(i).startsWith(fir.getCitizenId()+" -")) citizenIdField.setSelectedIndex(i); }
                    JComboBox<String> officerIdField = new JComboBox<>();
                    
                    locationField.addActionListener(e -> {
                        String loc = (String) locationField.getSelectedItem();
                        officerIdField.removeAllItems();
                        JComboBox<String> filtered = createOfficerDropdown(loc);
                        for(int j=0; j<filtered.getItemCount(); j++) officerIdField.addItem(filtered.getItemAt(j));
                    });
                    
                    // Initial load based on FIR location
                    JComboBox<String> currentFiltered = createOfficerDropdown(fir.getLocation());
                    for(int j=0; j<currentFiltered.getItemCount(); j++) officerIdField.addItem(currentFiltered.getItemAt(j));
                    
                    if (fir.getAssignedOfficerId() != null) { 
                        for(int i=0;i<officerIdField.getItemCount();i++) 
                            if(officerIdField.getItemAt(i).startsWith(fir.getAssignedOfficerId()+" -")) officerIdField.setSelectedIndex(i); 
                    }
                    JTextField statusField = new JTextField(fir.getStatus());
                    JComponent dateIncidentField = DesignSystem.createDatePicker();
                    JComponent dateReportedField = DesignSystem.createDatePicker();

                    JPanel panel = DesignSystem.createFormPanel();
                    DesignSystem.addField(panel, "FIR Number", firNumberField, 0);
                    DesignSystem.addField(panel, "Type (BNS)", crimeScroll, 1);
                    DesignSystem.addField(panel, "Description", crimeDescField, 2);
                    DesignSystem.addField(panel, "Location", locationField, 3);
                    DesignSystem.addField(panel, "Citizen ID", citizenIdField, 4);
                    DesignSystem.addField(panel, "Officer ID", officerIdField, 5);
                    DesignSystem.addField(panel, "Status", statusField, 6);
                    DesignSystem.addField(panel, "Incident Date", dateIncidentField, 7);
                    DesignSystem.addField(panel, "Reported Date", dateReportedField, 8);

                    if (JOptionPane.showConfirmDialog(FIRsPanel.this, panel, "Edit FIR", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        fir.setFirNumber(firNumberField.getText().trim());
                        
                        java.util.List<String> selected = new java.util.ArrayList<>();
                        for (String val : crimeTypeField.getSelectedValuesList()) selected.add(val.split(":")[0]);
                        fir.setCrimeType(String.join(", ", selected));

                        fir.setCrimeDescription(crimeDescField.getText().trim());
                        fir.setLocation((String)locationField.getSelectedItem());
                        
                        try { fir.setCitizenId(citizenIdField.getSelectedItem() == null ? null : Integer.parseInt(((String)citizenIdField.getSelectedItem()).split(" ")[0])); } catch (Exception e) {}
                        try { fir.setAssignedOfficerId(officerIdField.getSelectedItem() == null ? null : Integer.parseInt(((String)officerIdField.getSelectedItem()).split(" ")[0])); } catch (Exception e) {}
                        
                        fir.setStatus(statusField.getText().trim());
                        try { String strD = ((JSpinner.DateEditor)((JSpinner)dateIncidentField).getEditor()).getTextField().getText(); fir.setDateOfIncident(java.time.LocalDate.parse(strD).atStartOfDay()); } catch (Exception e) {}
                        try { String strR = ((JSpinner.DateEditor)((JSpinner)dateReportedField).getEditor()).getTextField().getText(); fir.setDateReported(java.time.LocalDate.parse(strR).atStartOfDay()); } catch (Exception e) {}

                        new SwingWorker<Void, Void>() {
                            @Override protected Void doInBackground() throws Exception { service.updateFIR(fir); return null; }
                            @Override protected void done() { load(); }
                        }.execute();
                    }
                } catch (Exception e) {}
            }
        }.execute();
    }

    private void onRemove() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int firId = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete selected FIR?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                service.deleteFIR(firId);
                load();
            } catch (Exception ex) {}
        }
    }
}
