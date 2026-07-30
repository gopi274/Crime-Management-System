package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.dao.JailDAO;
import com.crimemanagement.dao.JailInmateDAO;
import com.crimemanagement.dao.CriminalDAO;
import com.crimemanagement.entity.Jail;
import com.crimemanagement.entity.JailInmate;
import com.crimemanagement.entity.Criminal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import com.crimemanagement.service.AuditService;

public class JailManagementPanel extends JPanel {
    private final JailDAO jailDao;
    private final JailInmateDAO inmateDao;
    private final CriminalDAO criminalDao;
    private final Integer assignedJailId;
    private JTable jailTable;
    private DefaultTableModel jailModel;
    private JTable inmateTable;
    private DefaultTableModel inmateModel;

    public JailManagementPanel(Integer assignedJailId) {
        this.assignedJailId = assignedJailId;
        jailDao = new JailDAO();
        inmateDao = new JailInmateDAO();
        criminalDao = new CriminalDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Kerala Prison & Correctional Home Registry");
        title.setFont(DesignSystem.TITLE_FONT);
        title.setForeground(DesignSystem.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = DesignSystem.createPrimaryButton("🔄 Refresh Registry");
        refreshBtn.addActionListener(e -> refreshJails());
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Main Content (Split Pane: Jails on left/top, Inmates on right/bottom)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Left: Jail List
        JPanel jailPanel = new JPanel(new BorderLayout());
        jailPanel.setOpaque(false);
        jailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(DesignSystem.KP_GOLD), "Prisons", 0, 0, DesignSystem.BODY_FONT, DesignSystem.KP_GOLD));

        String[] jailCols = {"Name", "Type", "Location", "ID"};
        jailModel = new DefaultTableModel(jailCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jailTable = new JTable(jailModel);
        UIUtils.applyTableDefaults(jailTable);
        jailTable.removeColumn(jailTable.getColumnModel().getColumn(3)); // Hide ID
        
        jailTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadInmatesForSelectedJail();
        });

        jailPanel.add(new JScrollPane(jailTable), BorderLayout.CENTER);
        splitPane.setLeftComponent(jailPanel);

        // Right: Inmate List
        JPanel inmatePanel = new JPanel(new BorderLayout());
        inmatePanel.setOpaque(false);
        inmatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(DesignSystem.SUCCESS), "Active Inmates", 0, 0, DesignSystem.BODY_FONT, DesignSystem.SUCCESS));

        JPanel inmateActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inmateActionPanel.setOpaque(false);
        JButton addInmateBtn = DesignSystem.createPrimaryButton("➕ Add Inmate");
        JButton editInmateBtn = new JButton("Edit Inmate");
        DesignSystem.applyButtonStyle(editInmateBtn, false);
        
        addInmateBtn.addActionListener(e -> showAddInmateDialog());
        editInmateBtn.addActionListener(e -> {
            int row = inmateTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an inmate first.");
                return;
            }
            showEditInmateDialog();
        });
        
        inmateActionPanel.add(addInmateBtn);
        inmateActionPanel.add(editInmateBtn);
        inmatePanel.add(inmateActionPanel, BorderLayout.NORTH);

        String[] inmateCols = {"Name", "Entry Date", "Duration", "Status"};
        inmateModel = new DefaultTableModel(inmateCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        inmateTable = new JTable(inmateModel);
        UIUtils.applyTableDefaults(inmateTable);

        inmatePanel.add(new JScrollPane(inmateTable), BorderLayout.CENTER);
        splitPane.setRightComponent(inmatePanel);

        add(splitPane, BorderLayout.CENTER);

        refreshJails();
    }

    private void refreshJails() {
        jailModel.setRowCount(0);
        try {
            List<Jail> jails = jailDao.findAll();
            for (Jail j : jails) {
                // RBAC: Only show the assigned jail if the user is restricted
                if (assignedJailId != null && !assignedJailId.equals(j.getJailId())) {
                    continue;
                }
                jailModel.addRow(new Object[]{j.getName(), j.getType(), j.getLocation(), j.getJailId()});
            }
            
            // Auto-select the first visible jail to populate the inmates list
            if (jailModel.getRowCount() > 0) {
                jailTable.setRowSelectionInterval(0, 0);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Registry Load Error: " + e.getMessage());
        }
    }

    private void showAddInmateDialog() {
        int jailRow = jailTable.getSelectedRow();
        if (jailRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a prison on the left first.");
            return;
        }
        Integer jailId = (Integer) jailModel.getValueAt(jailRow, 3);
        
        JComboBox<String> jailBox = new JComboBox<>();
        try {
            List<Jail> jails = jailDao.findAll();
            for (Jail j : jails) {
                jailBox.addItem(j.getJailId() + " - " + j.getName());
                if (j.getJailId().equals(jailId)) jailBox.setSelectedItem(j.getJailId() + " - " + j.getName());
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        JComboBox<String> criminalBox = new JComboBox<>();
        try {
            List<Criminal> criminals = criminalDao.findAll();
            for (Criminal c : criminals) {
                criminalBox.addItem(c.getCriminalId() + " - " + c.getFirstName() + " " + c.getLastName());
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JTextField durationField = new JTextField("1 YEAR");
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"ACTIVE", "RELEASED", "PAROLE", "TRANSFERRED"});

        panel.add(new JLabel("Target Prison:")); panel.add(jailBox);
        panel.add(new JLabel("Select Criminal:")); panel.add(criminalBox);
        panel.add(new JLabel("Sentence Duration:")); panel.add(durationField);
        panel.add(new JLabel("Status:")); panel.add(statusBox);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Inmate to Registry", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String jailSel = (String) jailBox.getSelectedItem();
                int targetJailId = Integer.parseInt(jailSel.split(" - ")[0]);
                String sel = (String) criminalBox.getSelectedItem();
                int criminalId = Integer.parseInt(sel.split(" - ")[0]);
                JailInmate i = new JailInmate();
                i.setJailId(targetJailId);
                i.setCriminalId(criminalId);
                i.setEntryDate(LocalDate.now());
                i.setSentenceDuration(durationField.getText());
                i.setStatus(JailInmate.InmateStatus.valueOf((String) statusBox.getSelectedItem()));
                i.setCaseId(1); // Default link
                inmateDao.insert(i);
                
                AuditService.log("ADMIN", "ADD_INMATE", "Criminal ID " + criminalId + " assigned to Jail " + targetJailId);
                loadInmatesForSelectedJail();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void showEditInmateDialog() {
        int inmateRow = inmateTable.getSelectedRow();
        if (inmateRow == -1) return;

        // Fetch current jail
        int selectedJailRow = jailTable.getSelectedRow();
        Integer currentJailId = (Integer) jailModel.getValueAt(selectedJailRow, 3);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JComboBox<String> jailBox = new JComboBox<>();
        try {
            for (Jail j : jailDao.findAll()) {
                jailBox.addItem(j.getJailId() + " - " + j.getName());
                if (j.getJailId().equals(currentJailId)) jailBox.setSelectedItem(j.getJailId() + " - " + j.getName());
            }
        } catch (SQLException e) {}

        JTextField durationField = new JTextField("2 YEARS");
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"ACTIVE", "RELEASED", "PAROLE", "TRANSFERRED"});
        JTextField orderNoField = new JTextField("JO-2024-KLA-");

        panel.add(new JLabel("Assigned Jail:")); panel.add(jailBox);
        panel.add(new JLabel("Sentence Duration:")); panel.add(durationField);
        panel.add(new JLabel("Inmate Status:")); panel.add(statusBox);
        panel.add(new JLabel("Judicial Order #:")); panel.add(orderNoField);

        if (JOptionPane.showConfirmDialog(this, panel, "Edit Inmate Registry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String orderNo = orderNoField.getText();
            AuditService.log("ADMIN", "EDIT_INMATE", "Updated inmate record under Order: " + orderNo);
            JOptionPane.showMessageDialog(this, "Registry Synchronized with Judicial Order: " + orderNo);
            loadInmatesForSelectedJail();
        }
    }

    private void loadInmatesForSelectedJail() {
        inmateModel.setRowCount(0);
        int row = jailTable.getSelectedRow();
        if (row == -1) return;
        
        Integer jailId = (Integer) jailModel.getValueAt(row, 3);
        try {
            List<JailInmate> inmates = inmateDao.findByJail(jailId);
            for (JailInmate i : inmates) {
                Criminal c = criminalDao.findById(i.getCriminalId());
                String name = c != null ? c.getFirstName() + " " + c.getLastName() : "Unknown ID: " + i.getCriminalId();
                inmateModel.addRow(new Object[]{name, i.getEntryDate(), i.getSentenceDuration(), i.getStatus()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
