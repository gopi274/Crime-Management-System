package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.dao.FIRDAO;
import com.crimemanagement.dao.EvidenceDAO;
import com.crimemanagement.entity.FIR;
import com.crimemanagement.entity.Evidence;
import com.crimemanagement.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import com.crimemanagement.service.AuditService;

public class EvidenceManagementPanel extends JPanel {
    private final EvidenceDAO evidenceDao;
    private final FIRDAO firDao;
    
    private JComboBox<FIR> firSelector;
    private JTable evidenceTable;
    private DefaultTableModel tableModel;
    private final Integer courtId;
    private final Integer scientistId;

    public EvidenceManagementPanel(Integer courtId, Integer scientistId) {
        this.courtId = courtId;
        this.scientistId = scientistId;
        evidenceDao = new EvidenceDAO();
        firDao = new FIRDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Evidence & Forensic Registry");
        title.setFont(DesignSystem.TITLE_FONT);
        title.setForeground(DesignSystem.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        selectionPanel.setOpaque(false);
        JLabel firLbl = new JLabel("Target FIR: ");
        firLbl.setForeground(DesignSystem.TEXT_SECONDARY);
        selectionPanel.add(firLbl);

        firSelector = new JComboBox<>();
        firSelector.setPreferredSize(new Dimension(300, 30));
        firSelector.addActionListener(e -> loadEvidence());
        selectionPanel.add(firSelector);

        JButton addBtn = DesignSystem.createPrimaryButton("➕ Register Evidence");
        addBtn.addActionListener(e -> showAddEvidenceDialog());
        selectionPanel.add(addBtn);
        
        header.add(selectionPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Type", "Description", "Assigned Lab", "Status", "Timestamp"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        evidenceTable = new JTable(tableModel);
        UIUtils.applyTableDefaults(evidenceTable);
        
        evidenceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && evidenceTable.getSelectedRow() != -1) {
                    int row = evidenceTable.getSelectedRow();
                    String fullDesc = (String) tableModel.getValueAt(evidenceTable.convertRowIndexToModel(row), 1);
                    JTextArea ta = new JTextArea(10, 40);
                    ta.setText(fullDesc);
                    ta.setWrapStyleWord(true);
                    ta.setLineWrap(true);
                    ta.setEditable(false);
                    ta.setBackground(DesignSystem.BACKGROUND);
                    ta.setForeground(DesignSystem.TEXT_PRIMARY);
                    JOptionPane.showMessageDialog(EvidenceManagementPanel.this, new JScrollPane(ta), "Full Forensic Description", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        JButton actionBtn = DesignSystem.createPrimaryButton("⚡ Process / Escalate Evidence");
        actionBtn.addActionListener(e -> showProcessDialog());
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(evidenceTable), BorderLayout.CENTER);
        centerPanel.add(actionBtn, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        loadFIRs();
    }

    private void loadFIRs() {
        try {
            firSelector.removeAllItems();
            FIR allFir = new FIR();
            allFir.setFirId(-1);
            allFir.setFirNumber("< ALL EVIDENCE REGISTRY >");
            firSelector.addItem(allFir);

            List<FIR> firs = courtId != null ? firDao.findByCourt(courtId) : firDao.findAll();
            for (FIR f : firs) firSelector.addItem(f);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadEvidence() {
        tableModel.setRowCount(0);
        FIR selected = (FIR) firSelector.getSelectedItem();
        if (selected == null) return;
        
        try {
            boolean isDirector = false;
            Integer myLabId = null;
            
            if (scientistId != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT rank, lab_id FROM forensic_scientists WHERE scientist_id = ?")) {
                    ps.setInt(1, scientistId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            isDirector = "Director".equalsIgnoreCase(rs.getString("rank"));
                            myLabId = rs.getInt("lab_id");
                        }
                    }
                } catch (SQLException ex) {}
            }

            List<Evidence> evidenceList;
            if (selected.getFirId() == -1) {
                evidenceList = courtId != null ? evidenceDao.findByCourt(courtId) : evidenceDao.findAll();
            } else {
                evidenceList = evidenceDao.findByFirId(selected.getFirId());
            }

            for (Evidence e : evidenceList) {
                // HIERARCHY FILTER: Director sees all. Others see only their lab.
                if (scientistId != null && !isDirector && e.getLabId() != null && !e.getLabId().equals(myLabId)) {
                    continue; 
                }
                String labName = "N/A";
                if (e.getLabId() != null && e.getLabId() > 0) {
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement ps = conn.prepareStatement("SELECT name FROM forensic_labs WHERE lab_id = ?")) {
                        ps.setInt(1, e.getLabId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) labName = rs.getString("name");
                        }
                    } catch (SQLException ex) {}
                }
                tableModel.addRow(new Object[]{
                    e.getEvidenceType(),
                    e.getDescription(),
                    labName,
                    e.getStatus(),
                    e.getCollectedDate()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showAddEvidenceDialog() {
        FIR selected = (FIR) firSelector.getSelectedItem();
        if (selected == null || selected.getFirId() == -1) {
            JOptionPane.showMessageDialog(this, "Select a specific target FIR first.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField typeField = new JTextField();
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        JTextField locField = new JTextField();
        JTextField storageField = new JTextField();
        JComboBox<Evidence.EvidenceStatus> statusBox = new JComboBox<>(Evidence.EvidenceStatus.values());
        JComboBox<String> mediaTypeBox = new JComboBox<>(new String[]{"NONE", "JPEG", "PNG", "MP3", "MOV", "PDF", "DOCUMENT"});
        JTextField mediaUrlField = new JTextField();
        JButton browseBtn = new JButton("📂 Browse...");
        
        JComboBox<String> labBox = new JComboBox<>();
        Integer autoLabId = null;
        try { autoLabId = evidenceDao.getAutoRoutedLabId(selected.getLocation()); } catch (SQLException ex) {}

        String preSelectedLab = null;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT lab_id, name FROM forensic_labs ORDER BY type DESC, name ASC")) {
            while (rs.next()) {
                String name = rs.getString("name");
                labBox.addItem(name);
                if (autoLabId != null && autoLabId == rs.getInt("lab_id")) preSelectedLab = name;
            }
        } catch (SQLException ex) {}

        if (preSelectedLab != null) labBox.setSelectedItem(preSelectedLab);

        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                mediaUrlField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        panel.add(new JLabel("Artifact Type:")); panel.add(typeField);
        panel.add(new JLabel("Description:")); panel.add(new JScrollPane(descArea));
        panel.add(new JLabel("Recovery Location:")); panel.add(locField);
        panel.add(new JLabel("Internal Storage:")); panel.add(storageField);
        panel.add(new JLabel("Initial Status:")); panel.add(statusBox);
        panel.add(new JLabel("Target Lab:")); panel.add(labBox);
        panel.add(new JLabel("Digital Media Type:")); panel.add(mediaTypeBox);
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.add(mediaUrlField, BorderLayout.CENTER);
        filePanel.add(browseBtn, BorderLayout.EAST);
        panel.add(new JLabel("File Path (HQ/Server):")); panel.add(filePanel);

        int res = JOptionPane.showConfirmDialog(this, panel, "Evidence Registration", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Evidence e = new Evidence();
                e.setFirId(selected.getFirId());
                e.setEvidenceType(typeField.getText());
                e.setDescription(descArea.getText());
                e.setLocationFound(locField.getText());
                e.setStorageLocation(storageField.getText());
                e.setStatus((Evidence.EvidenceStatus) statusBox.getSelectedItem());
                e.setMediaType((String) mediaTypeBox.getSelectedItem());
                e.setCollectedDate(LocalDateTime.now());
                e.setCollectedBy(1);
                
                String labName = (String) labBox.getSelectedItem();
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT lab_id FROM forensic_labs WHERE name = ?")) {
                    ps.setString(1, labName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) e.setLabId(rs.getInt("lab_id"));
                    }
                }
                evidenceDao.insert(e);
                AuditService.log("FORENSIC", "EVIDENCE_REGISTERED", "Registered artifact: " + e.getEvidenceType() + " for FIR: " + selected.getFirNumber());
                loadEvidence();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void showProcessDialog() {
        int row = evidenceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an evidence item from the registry.");
            return;
        }
        
        FIR selectedFIR = (FIR) firSelector.getSelectedItem();
        if (selectedFIR == null || selectedFIR.getFirId() == -1) return;

        try {
            List<Evidence> list = evidenceDao.findByFirId(selectedFIR.getFirId());
            Evidence target = list.get(evidenceTable.convertRowIndexToModel(row));

            JPanel panel = DesignSystem.createFormPanel();
            JComboBox<Evidence.EvidenceStatus> statusBox = new JComboBox<>(Evidence.EvidenceStatus.values());
            statusBox.setSelectedItem(target.getStatus());

            JComboBox<String> labBox = new JComboBox<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT lab_id, name FROM forensic_labs ORDER BY type DESC, name ASC")) {
                while (rs.next()) {
                    String name = rs.getInt(1) + " - " + rs.getString(2);
                    labBox.addItem(name);
                    if (target.getLabId() != null && target.getLabId().equals(rs.getInt("lab_id"))) labBox.setSelectedItem(name);
                }
            } catch (SQLException ex) {}

            DesignSystem.addField(panel, "Update Status", statusBox, 0);
            DesignSystem.addField(panel, "Reassign Lab", labBox, 1);
            
            JLabel hint = new JLabel("<html><i>Hint: Use 'SFSL_ESCALATION' for state-level analysis.</i></html>");
            hint.setForeground(DesignSystem.KP_GOLD);
            panel.add(hint);

            if (JOptionPane.showConfirmDialog(this, panel, "Forensic Processing - ID: " + target.getEvidenceId(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                target.setStatus((Evidence.EvidenceStatus) statusBox.getSelectedItem());
                String selLab = (String) labBox.getSelectedItem();
                if (selLab != null) target.setLabId(Integer.parseInt(selLab.split(" - ")[0]));
                
                // Update in DB
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE evidence SET status=?, lab_id=? WHERE evidence_id=?")) {
                    ps.setString(1, target.getStatus().name());
                    if (target.getLabId() != null) ps.setInt(2, target.getLabId()); else ps.setNull(2, java.sql.Types.INTEGER);
                    ps.setInt(3, target.getEvidenceId());
                    ps.executeUpdate();
                    AuditService.log("FORENSIC", "EVIDENCE_PROCESSED", "Updated evidence ID: " + target.getEvidenceId() + " to status: " + target.getStatus());
                    loadEvidence();
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
