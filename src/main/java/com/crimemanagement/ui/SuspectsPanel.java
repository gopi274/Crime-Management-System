package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.dao.SuspectDAO;
import com.crimemanagement.dao.CaseDAO;
import com.crimemanagement.dao.CriminalDAO;
import com.crimemanagement.entity.Suspect;
import com.crimemanagement.entity.Case;
import com.crimemanagement.entity.Criminal;

import com.crimemanagement.util.BNSConstants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import com.crimemanagement.service.AuditService;

/**
 * Suspects Management Panel for Kerala Police ERP V6.2.0 KOCHI-DIRECTORATE.
 * Features automated judicial acquittal routing and MHA-compliant BNS registry.
 */
public class SuspectsPanel extends JPanel {
    private final SuspectDAO suspectDAO = new SuspectDAO();
    private final CaseDAO caseDAO = new CaseDAO();
    private final CriminalDAO criminalDAO = new CriminalDAO();
    private DefaultTableModel model;
    private JTable table;
    private final LoginDialog.Role userRole;
    private final Integer courtId;

    public SuspectsPanel(LoginDialog.Role role, Integer courtId) {
        this.userRole = role;
        this.courtId = courtId;
        setup();
    }

    public SuspectsPanel(LoginDialog.Role role) {
        this(role, null);
    }

    public SuspectsPanel() {
        this(LoginDialog.Role.ADMIN, null);
    }

    private void setup() {
        super.setLayout(new BorderLayout());
        setBackground(DesignSystem.BACKGROUND);

        model = new DefaultTableModel(new Object[] {
                "ID", "Case Number", "Criminal", "Gender", "Charge (BNS)", "Role", "Arrest Date", "Bail Amount", "Bail Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        UIUtils.applyTableDefaults(table);

        JTextField search = new JTextField(18);
        UIUtils.bindSearch(table, search);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());

        JButton addBtn = DesignSystem.createPrimaryButton("➕ Add Suspect");
        addBtn.addActionListener(e -> showAddDialog());

        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> showEditDialog());

        JButton dispositionBtn = DesignSystem.createPrimaryButton("⚖️ Judicial Disposition");
        dispositionBtn.addActionListener(e -> showDispositionDialog());

        JButton removeBtn = new JButton("Remove");
        DesignSystem.applyButtonStyle(removeBtn, false);
        removeBtn.addActionListener(e -> removeSelectedSuspect());

        boolean canModify = userRole == LoginDialog.Role.ADMIN || userRole == LoginDialog.Role.COURT;
        addBtn.setVisible(canModify);
        editBtn.setVisible(canModify);
        dispositionBtn.setVisible(canModify);
        removeBtn.setVisible(userRole == LoginDialog.Role.ADMIN);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Search Registry:"));
        top.add(search);
        top.add(refresh);
        top.add(addBtn);
        top.add(editBtn);
        top.add(dispositionBtn);
        top.add(removeBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        load();
    }

    private void load() {
        SwingWorker<List<Suspect>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Suspect> doInBackground() {
                try {
                    return suspectDAO.findByCourt(courtId);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return List.of();
                }
            }
// ... [rest of the file remains same, excluding the truncated end]

            @Override
            protected void done() {
                try {
                    List<Suspect> list = get();
                    model.setRowCount(0);
                    for (Suspect s : list) {
                        String caseNumber = "N/A";
                        try {
                            Case caseObj = caseDAO.findById(s.getCaseId());
                            if (caseObj != null) caseNumber = caseObj.getCaseNumber();
                        } catch (SQLException e) {}

                        String criminalName = "N/A";
                        try {
                            Criminal criminal = criminalDAO.findById(s.getCriminalId());
                            if (criminal != null) criminalName = criminal.getFirstName() + " " + criminal.getLastName();
                        } catch (SQLException e) {}

                        model.addRow(new Object[] {
                                s.getSuspectId(),
                                caseNumber,
                                criminalName,
                                s.getGender() != null ? s.getGender() : "N/A",
                                s.getCharge() != null ? s.getCharge() : "N/A",
                                s.getRole(),
                                s.getArrestDate() != null ? s.getArrestDate().toString() : "N/A",
                                s.getBailAmount() != null ? s.getBailAmount().toString() : "0.00",
                                s.getBailStatus() != null ? s.getBailStatus() : "N/A"
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private String[] loadChargesFromDatabase() {
        java.util.List<String> bnsList = new java.util.ArrayList<>();
        try (java.sql.Connection conn = com.crimemanagement.util.DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT code, title FROM legal_sections ORDER BY code ASC")) {
            while (rs.next()) {
                bnsList.add(rs.getString("code") + ": " + rs.getString("title"));
            }
        } catch (Exception e) {}
        return bnsList.isEmpty() ? BNSConstants.SECTIONS : bnsList.toArray(new String[0]);
    }

    private void showAddDialog() {
        JPanel form = DesignSystem.createFormPanel();
        JComboBox<CaseItem> caseBox = new JComboBox<>();
        try {
            for (Case c : caseDAO.findAll()) caseBox.addItem(new CaseItem(c.getCaseId(), c.getCaseNumber()));
        } catch (Exception ex) {}

        JComboBox<CriminalItem> criminalBox = new JComboBox<>();
        try {
            for (Criminal c : criminalDAO.findAll()) criminalBox.addItem(new CriminalItem(c.getCriminalId(), c.getFirstName() + " " + c.getLastName()));
        } catch (Exception ex) {}

        JList<String> chargeList = new JList<>(loadChargesFromDatabase());
        chargeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane chargeScroll = new JScrollPane(chargeList);
        chargeScroll.setPreferredSize(new Dimension(300, 150));

        String[] legalRoles = {"PRIMARY_SUSPECT", "ACCOMPLICE", "WITNESS"};
        JComboBox<String> roleBox = new JComboBox<>(legalRoles);
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"M", "F", "Other"});
        JTextField bailField = new JTextField("0");
        JComboBox<String> bailStatusBox = new JComboBox<>(new String[]{"IN_CUSTODY", "ON_BAIL", "PENDING_ARREST", "REMAND"});
        JTextField arrestDateField = new JTextField(java.time.LocalDate.now().toString());

        DesignSystem.addField(form, "Select Case", caseBox, 0);
        DesignSystem.addField(form, "Select Criminal", criminalBox, 1);
        DesignSystem.addField(form, "Charges (BNS)", chargeScroll, 2);
        DesignSystem.addField(form, "Legal Role", roleBox, 3);
        DesignSystem.addField(form, "Gender", genderBox, 4);
        DesignSystem.addField(form, "Arrest Date (YYYY-MM-DD)", arrestDateField, 5);
        DesignSystem.addField(form, "Bail Amount", bailField, 6);
        DesignSystem.addField(form, "Bail Status", bailStatusBox, 7);

        int res = JOptionPane.showConfirmDialog(this, form, "Register Suspect", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Suspect s = new Suspect();
                s.setCaseId(((CaseItem)caseBox.getSelectedItem()).id);
                s.setCriminalId(((CriminalItem)criminalBox.getSelectedItem()).id);
                s.setRole((String) roleBox.getSelectedItem());
                s.setGender((String) genderBox.getSelectedItem());
                
                java.util.List<String> codesOnly = new java.util.ArrayList<>();
                for (String chargeStr : chargeList.getSelectedValuesList()) codesOnly.add(chargeStr.split(":")[0]);
                s.setCharge(String.join(", ", codesOnly));
                
                s.setDisposition("PENDING");
                try { s.setArrestDate(java.time.LocalDate.parse(arrestDateField.getText())); } catch (Exception e) {}
                try { s.setBailAmount(new BigDecimal(bailField.getText())); } catch (Exception e) {}
                s.setBailStatus((String) bailStatusBox.getSelectedItem());

                suspectDAO.create(s);
                AuditService.log("OFFICER", "SUSPECT_REGISTERED", "Registered suspect for case: " + s.getCaseId());
                load();
                JOptionPane.showMessageDialog(this, "V6.2.0: Suspect registered in Judicial Registry.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage());
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a suspect to edit.");
            return;
        }
        int id = (int) model.getValueAt(selectedRow, 0);

        try {
            Suspect s = suspectDAO.findById(id);
            if (s == null) return;

            JPanel form = DesignSystem.createFormPanel();

            JList<String> chargeList = new JList<>(loadChargesFromDatabase());
            chargeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            
            // Pre-select existing charges
            if (s.getCharge() != null) {
                String[] existing = s.getCharge().split(", ");
                java.util.List<Integer> indices = new java.util.ArrayList<>();
                for (String c : existing) {
                    for (int i = 0; i < chargeList.getModel().getSize(); i++) {
                        if (chargeList.getModel().getElementAt(i).startsWith(c)) {
                            indices.add(i);
                        }
                    }
                }
                chargeList.setSelectedIndices(indices.stream().mapToInt(i->i).toArray());
            }
            
            JScrollPane chargeScroll = new JScrollPane(chargeList);
            chargeScroll.setPreferredSize(new Dimension(300, 150));

            String[] legalRoles = {"PRIMARY_SUSPECT", "ACCOMPLICE", "WITNESS"};
            JComboBox<String> roleBox = new JComboBox<>(legalRoles);
            roleBox.setSelectedItem(s.getRole());
            
            JTextField bailField = new JTextField(s.getBailAmount() != null ? s.getBailAmount().toString() : "0");
            JComboBox<String> bailStatusBox = new JComboBox<>(new String[]{"IN_CUSTODY", "ON_BAIL", "PENDING_ARREST", "REMAND"});
            if (s.getBailStatus() != null) bailStatusBox.setSelectedItem(s.getBailStatus());

            JComboBox<String> genderBox = new JComboBox<>(new String[]{"M", "F", "Other"});
            genderBox.setSelectedItem(s.getGender());

            JTextField arrestDateField = new JTextField(s.getArrestDate() != null ? s.getArrestDate().toString() : "");

            DesignSystem.addField(form, "Charges (BNS)", chargeScroll, 0);
            DesignSystem.addField(form, "Legal Role", roleBox, 1);
            DesignSystem.addField(form, "Gender", genderBox, 2);
            DesignSystem.addField(form, "Arrest Date (YYYY-MM-DD)", arrestDateField, 3);
            DesignSystem.addField(form, "Bail Amount", bailField, 4);
            DesignSystem.addField(form, "Bail Status", bailStatusBox, 5);

            int res = JOptionPane.showConfirmDialog(this, form, "Edit Suspect", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                s.setRole((String) roleBox.getSelectedItem());
                s.setGender((String) genderBox.getSelectedItem());
                try { s.setArrestDate(java.time.LocalDate.parse(arrestDateField.getText())); } catch (Exception e) {}
                
                java.util.List<String> codesOnly = new java.util.ArrayList<>();
                for (String chargeStr : chargeList.getSelectedValuesList()) {
                    codesOnly.add(chargeStr.split(":")[0]);
                }
                s.setCharge(String.join(", ", codesOnly));
                
                try { s.setBailAmount(new java.math.BigDecimal(bailField.getText())); } catch (Exception e) {}
                s.setBailStatus((String) bailStatusBox.getSelectedItem());

                suspectDAO.update(s);
                AuditService.log("OFFICER", "SUSPECT_UPDATED", "Updated suspect record: " + s.getSuspectId());
                load();
                JOptionPane.showMessageDialog(this, "V6.5.0: Suspect updated successfully.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to edit: " + ex.getMessage());
        }
    }

    private void showDispositionDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a suspect for judicial disposition.");
            return;
        }
        int id = (int) model.getValueAt(selectedRow, 0);
        String currentDisposition = (String) model.getValueAt(selectedRow, 5);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.add(new JLabel("Current Status: " + currentDisposition));
        JComboBox<String> dispBox = new JComboBox<>(new String[]{"GUILTY", "ACQUITTED", "RELEASED_ON_BAIL", "PENDING"});
        panel.add(new JLabel("Set Judicial Disposition:"));
        panel.add(dispBox);

        int res = JOptionPane.showConfirmDialog(this, panel, "V6.2.0 Court Disposition", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String newDisp = (String) dispBox.getSelectedItem();
                suspectDAO.updateDisposition(id, newDisp);
                JOptionPane.showMessageDialog(this, "Judicial record updated. " + 
                    (newDisp.equals("ACQUITTED") ? "Automated release triggered in Prison module." : ""));
                load();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
            }
        }
    }

    private void removeSelectedSuspect() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;
        int id = (int) model.getValueAt(selectedRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete suspect record? This action is permanent.") == JOptionPane.YES_OPTION) {
            try {
                suspectDAO.deleteById(id);
                load();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage());
            }
        }
    }

    private static class CaseItem {
        int id; String num;
        CaseItem(int id, String num) { this.id = id; this.num = num; }
        @Override public String toString() { return num; }
    }

    private static class CriminalItem {
        int id; String name;
        CriminalItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
