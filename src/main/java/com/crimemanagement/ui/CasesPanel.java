package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.entity.Case;
import com.crimemanagement.service.InvestigationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CasesPanel extends JPanel {

    private final InvestigationService investigationService = new InvestigationService();
    private DefaultTableModel model;
    private JTable table;
    private final LoginDialog.Role userRole;
    private final Integer stationId;
    private final Integer courtId;
    private final Integer officerId;
    private final com.crimemanagement.dao.OfficerDAO officerDAO = new com.crimemanagement.dao.OfficerDAO();

    public CasesPanel(LoginDialog.Role role, Integer stationId, Integer courtId, Integer officerId) {
        this.userRole = role;
        this.stationId = stationId;
        this.courtId = courtId;
        this.officerId = officerId;
        setup();
    }

    private void setup() {
        super.setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[] {
                "ID", "Case Number", "FIR ID", "Title", "Priority", "Status", "Assigned Officer", "Court ID",
                "Target Date", "Next Trial", "Delay Expl.", "Witness Summons" }, 0) {
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

        JButton addBtn = new JButton("Add Case");
        addBtn.addActionListener(e -> showAddDialog());

        JButton editBtn = new JButton("Edit Case");
        editBtn.addActionListener(e -> showEditDialog());

        JButton removeBtn = new JButton("Remove Case");
        removeBtn.addActionListener(e -> removeSelectedCase());

        JButton viewEvidenceBtn = new JButton("🔍 View Evidence");
        viewEvidenceBtn.addActionListener(e -> showEvidenceDialog());

        JButton prisonBtn = new JButton("⛓️ Finalize Placement");
        prisonBtn.setBackground(DesignSystem.DANGER);
        prisonBtn.setForeground(Color.WHITE);
        prisonBtn.addActionListener(e -> showPrisonPlacementDialog());

        if (userRole != LoginDialog.Role.COURT && userRole != LoginDialog.Role.ADMIN && userRole != LoginDialog.Role.OFFICER) {
            addBtn.setVisible(false);
            editBtn.setVisible(false);
            removeBtn.setVisible(false);
            prisonBtn.setVisible(false);
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(search);
        top.add(refresh);
        top.add(addBtn);
        top.add(editBtn);
        top.add(removeBtn);
        top.add(viewEvidenceBtn);
        top.add(prisonBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        load();
    }

    private void showPrisonPlacementDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a solved case first.");
            return;
        }
        
        String status = (String) model.getValueAt(row, 5);
        if (!"Solved".equals(status)) {
            JOptionPane.showMessageDialog(this, "Prison placement only allowed for 'Solved' cases (Guilty Verdict).");
            return;
        }

        int caseId = (int) model.getValueAt(row, 0);
        
        try {
            // 1. Find Primary Suspect
            com.crimemanagement.dao.SuspectDAO suspectDao = new com.crimemanagement.dao.SuspectDAO();
            com.crimemanagement.entity.Criminal criminal = suspectDao.findPrimarySuspectForCase(caseId);
            
            if (criminal == null) {
                JOptionPane.showMessageDialog(this, "No primary suspect identified for this case.");
                return;
            }

            // 2. Load Jails
            com.crimemanagement.dao.JailDAO jailDao = new com.crimemanagement.dao.JailDAO();
            List<com.crimemanagement.entity.Jail> jails = jailDao.findAll();
            
            JComboBox<com.crimemanagement.entity.Jail> jailBox = new JComboBox<>();
            for (com.crimemanagement.entity.Jail j : jails) jailBox.addItem(j);
            
            JTextField durationField = new JTextField("2 Years");
            JComponent entryDate = DesignSystem.createDatePicker();

            JPanel panel = DesignSystem.createFormPanel();
            DesignSystem.addField(panel, "Prisoner", new JLabel(criminal.getFirstName() + " " + criminal.getLastName()), 0);
            DesignSystem.addField(panel, "Select Jail", jailBox, 1);
            DesignSystem.addField(panel, "Sentence", durationField, 2);
            DesignSystem.addField(panel, "Entry Date", entryDate, 3);

            if (JOptionPane.showConfirmDialog(this, panel, "Prison Referral - Finalize Placement", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                com.crimemanagement.entity.JailInmate inmate = new com.crimemanagement.entity.JailInmate();
                inmate.setCriminalId(criminal.getCriminalId());
                inmate.setJailId(((com.crimemanagement.entity.Jail) jailBox.getSelectedItem()).getJailId());
                inmate.setCaseId(caseId);
                inmate.setSentenceDuration(durationField.getText());
                
                String strD = ((JSpinner.DateEditor)((JSpinner)entryDate).getEditor()).getTextField().getText();
                inmate.setEntryDate(java.time.LocalDate.parse(strD));
                inmate.setStatus(com.crimemanagement.entity.JailInmate.InmateStatus.ACTIVE);

                new com.crimemanagement.dao.JailInmateDAO().insert(inmate);
                JOptionPane.showMessageDialog(this, "Criminal successfully transferred to " + jailBox.getSelectedItem());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void load() {
        SwingWorker<List<Case>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Case> doInBackground() {
                try {
                    // Apply Multi-tenancy: Station or Court filtering
                    return investigationService.getAllCases(stationId, courtId);
                } catch (SQLException e) {
                    return List.of();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Case> list = get();
                    model.setRowCount(0);
                    for (Case inv : list) {
                        model.addRow(new Object[] {
                                inv.getCaseId(),
                                inv.getCaseNumber(),
                                inv.getFirId(),
                                inv.getCaseTitle(),
                                inv.getPriority(),
                                inv.getStatus(),
                                inv.getAssignedOfficerId(),
                                inv.getCourtId(),
                                inv.getTargetDate(),
                                inv.getNextTrialDate(),
                                inv.getDelayExplanation(),
                                inv.getWitnessSummons()
                        });
                    }
                } catch (Exception ex) {}
            }
        };
        worker.execute();
    }

    private void showAddDialog() {
        JTextField caseNumberField = new JTextField(15);
        JTextField titleField = new JTextField(20);
        String[] priorities = { "Low", "Medium", "High", "Critical" };
        JComboBox<String> priorityBox = new JComboBox<>(priorities);
        String[] statuses = { "Open", "Under Investigation", "In Court", "Closed", "Solved" };
        JComboBox<String> statusBox = new JComboBox<>(statuses);
        JTextField delayExplField = new JTextField(15);
        JComponent targetDateField = DesignSystem.createDatePicker();
        JComponent nextTrialField = DesignSystem.createDatePicker();
        JTextField summonsField = new JTextField(20);

        JComboBox<com.crimemanagement.entity.FIR> firBox = new JComboBox<>();
        JComboBox<com.crimemanagement.entity.Court> courtBox = new JComboBox<>();
        
        try {
            List<com.crimemanagement.entity.FIR> firs = new com.crimemanagement.dao.FIRDAO().findAll(stationId);
            for (com.crimemanagement.entity.FIR f : firs) firBox.addItem(f);
            
            List<com.crimemanagement.entity.Court> courts = new com.crimemanagement.dao.CourtDAO().findAll();
            for (com.crimemanagement.entity.Court c : courts) {
                courtBox.addItem(c);
                // Pre-select if user is a COURT role
                if (courtId != null && c.getCourtId() == courtId) courtBox.setSelectedItem(c);
            }
        } catch (SQLException ex) {}

        JPanel panel = DesignSystem.createFormPanel();
        DesignSystem.addField(panel, "Case Number", caseNumberField, 0);
        DesignSystem.addField(panel, "Link to FIR", firBox, 1);
        DesignSystem.addField(panel, "Case Title", titleField, 2);
        DesignSystem.addField(panel, "Priority", priorityBox, 3);
        DesignSystem.addField(panel, "Status", statusBox, 4);
        DesignSystem.addField(panel, "Assigned Court", courtBox, 5);
        
        // Lock court selection for COURT users
        if (userRole == LoginDialog.Role.COURT) courtBox.setEnabled(false);

        DesignSystem.addField(panel, "Target Date", targetDateField, 6);
        DesignSystem.addField(panel, "Next Trial Date", nextTrialField, 7);
        DesignSystem.addField(panel, "Delay Explanation", delayExplField, 8);
        DesignSystem.addField(panel, "Witness Summons", summonsField, 9);

        if (JOptionPane.showConfirmDialog(this, panel, "V5.2 Justice - Add New Case", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Case inv = new Case();
            inv.setCaseNumber(caseNumberField.getText().trim());
            
            com.crimemanagement.entity.FIR selFir = (com.crimemanagement.entity.FIR) firBox.getSelectedItem();
            if (selFir != null) inv.setFirId(selFir.getFirId());
            
            inv.setCaseTitle(titleField.getText().trim());
            inv.setPriority((String) priorityBox.getSelectedItem());
            inv.setStatus((String) statusBox.getSelectedItem());
            
            com.crimemanagement.entity.Court selCourt = (com.crimemanagement.entity.Court) courtBox.getSelectedItem();
            if (selCourt != null) inv.setCourtId(selCourt.getCourtId());
            
            try { 
                String strT = ((JSpinner.DateEditor)((JSpinner)targetDateField).getEditor()).getTextField().getText();
                inv.setTargetDate(java.time.LocalDate.parse(strT)); 
            } catch (Exception e) {}
            try { 
                String strPl = ((JSpinner.DateEditor)((JSpinner)nextTrialField).getEditor()).getTextField().getText();
                inv.setNextTrialDate(java.time.LocalDate.parse(strPl)); 
            } catch (Exception e) {}
            inv.setDelayExplanation(delayExplField.getText().trim());
            inv.setWitnessSummons(summonsField.getText().trim());

            try {
                investigationService.createCase(inv);
                load();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int caseId = (int) model.getValueAt(row, 0);

        new SwingWorker<Case, Void>() {
            @Override
            protected Case doInBackground() throws SQLException { return investigationService.getCaseById(caseId); }
            @Override
            protected void done() {
                try {
                    Case inv = get();
                    if (inv == null) return;
                    
                    JTextField caseNumberField = new JTextField(inv.getCaseNumber());
                    JTextField titleField = new JTextField(inv.getCaseTitle());
                    JComboBox<String> priorityBox = new JComboBox<>(new String[]{ "Low", "Medium", "High", "Critical" });
                    priorityBox.setSelectedItem(inv.getPriority());
                    JComboBox<String> statusBox = new JComboBox<>(new String[]{ "Open", "Under Investigation", "In Court", "Closed", "Solved" });
                    statusBox.setSelectedItem(inv.getStatus());
                    
                    JComboBox<com.crimemanagement.entity.FIR> firBox = new JComboBox<>();
                    JComboBox<com.crimemanagement.entity.Court> courtBox = new JComboBox<>();
                    
                    try {
                        List<com.crimemanagement.entity.FIR> firs = new com.crimemanagement.dao.FIRDAO().findAll(stationId);
                        for (com.crimemanagement.entity.FIR f : firs) {
                            firBox.addItem(f);
                            if (f.getFirId() == inv.getFirId()) firBox.setSelectedItem(f);
                        }
                        
                        List<com.crimemanagement.entity.Court> courts = new com.crimemanagement.dao.CourtDAO().findAll();
                        for (com.crimemanagement.entity.Court c : courts) {
                            courtBox.addItem(c);
                            if (inv.getCourtId() != null && c.getCourtId() == inv.getCourtId()) courtBox.setSelectedItem(c);
                        }
                    } catch (SQLException ex) {}

                    JComponent targetDateField = DesignSystem.createDatePicker();
                    if (inv.getTargetDate() != null) {
                        try {
                            ((javax.swing.JSpinner.DateEditor)((javax.swing.JSpinner)targetDateField).getEditor()).getTextField().setText(inv.getTargetDate().toString());
                        } catch (Exception e) {}
                    }
                    JComponent nextTrialField = DesignSystem.createDatePicker();
                    if (inv.getNextTrialDate() != null) {
                        try {
                            ((javax.swing.JSpinner.DateEditor)((javax.swing.JSpinner)nextTrialField).getEditor()).getTextField().setText(inv.getNextTrialDate().toString());
                        } catch (Exception e) {}
                    }
                    JTextField delayExplField = new JTextField(inv.getDelayExplanation() != null ? inv.getDelayExplanation() : "");
                    JTextField summonsField = new JTextField(inv.getWitnessSummons() != null ? inv.getWitnessSummons() : "");
                    
                    JTextArea articleArea = new JTextArea(inv.getArticleText() != null ? inv.getArticleText() : "", 8, 30);
                    articleArea.setLineWrap(true);
                    articleArea.setWrapStyleWord(true);
                    JScrollPane articleScroll = new JScrollPane(articleArea);

                    JCheckBox verifiedCheck = new JCheckBox("Court Verified (Forward to Citizen Portal)");
                    verifiedCheck.setSelected(inv.isVerifiedByCourt());
                    
                    // Only Court role can verify and publish articles
                    if (userRole != LoginDialog.Role.COURT && userRole != LoginDialog.Role.ADMIN) {
                        verifiedCheck.setEnabled(false);
                    }
                    
                    JComboBox<com.crimemanagement.entity.Officer> officerBox = new JComboBox<>();
                    try {
                        java.util.List<com.crimemanagement.entity.Officer> candidates;
                        if (officerId != null) {
                            com.crimemanagement.entity.Officer me = officerDAO.findById(officerId);
                            candidates = officerDAO.findByJurisdiction(me);
                        } else {
                            candidates = officerDAO.findAll();
                        }
                        for (com.crimemanagement.entity.Officer o : candidates) {
                            officerBox.addItem(o);
                            if (inv.getAssignedOfficerId() != null && o.getOfficerId() == inv.getAssignedOfficerId()) {
                                officerBox.setSelectedItem(o);
                            }
                        }
                    } catch (Exception ex) {}

                    JTextArea verdictArea = new JTextArea(inv.getVerdictText() != null ? inv.getVerdictText() : "", 4, 20);
                    verdictArea.setLineWrap(true);
                    verdictArea.setWrapStyleWord(true);
                    JScrollPane verdictScroll = new JScrollPane(verdictArea);

                    JPanel panel = DesignSystem.createFormPanel();
                    DesignSystem.addField(panel, "Case Number", caseNumberField, 0);
                    DesignSystem.addField(panel, "Link to FIR", firBox, 1);
                    DesignSystem.addField(panel, "Case Title", titleField, 2);
                    DesignSystem.addField(panel, "Priority", priorityBox, 3);
                    DesignSystem.addField(panel, "Status", statusBox, 4);
                    DesignSystem.addField(panel, "Assigned Court", courtBox, 5);
                    DesignSystem.addField(panel, "Assign to Officer", officerBox, 6);
                    
                    DesignSystem.addField(panel, "Target Date", targetDateField, 7);
                    DesignSystem.addField(panel, "Next Trial", nextTrialField, 8);
                    DesignSystem.addField(panel, "Delay Explanation", delayExplField, 9);
                    DesignSystem.addField(panel, "Witness Summons", summonsField, 10);
                    DesignSystem.addField(panel, "Judicial Article (for Portal)", articleScroll, 11);
                    DesignSystem.addField(panel, "Court Status", verifiedCheck, 12);
                    DesignSystem.addField(panel, "Draft Verdict", verdictScroll, 13);

                    if (JOptionPane.showConfirmDialog(CasesPanel.this, panel, "V5.2 Justice - Edit Case", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

                    inv.setCaseNumber(caseNumberField.getText().trim());
                    com.crimemanagement.entity.FIR selFir = (com.crimemanagement.entity.FIR) firBox.getSelectedItem();
                    if (selFir != null) inv.setFirId(selFir.getFirId());
                    inv.setCaseTitle(titleField.getText().trim());
                    inv.setPriority((String) priorityBox.getSelectedItem());
                    inv.setStatus((String) statusBox.getSelectedItem());
                    com.crimemanagement.entity.Officer selOff = (com.crimemanagement.entity.Officer) officerBox.getSelectedItem();
                    if (selOff != null) inv.setAssignedOfficerId(selOff.getOfficerId());
                    com.crimemanagement.entity.Court selCourt = (com.crimemanagement.entity.Court) courtBox.getSelectedItem();
                    if (selCourt != null) inv.setCourtId(selCourt.getCourtId());
                    try { 
                        String strT = ((javax.swing.JSpinner.DateEditor)((javax.swing.JSpinner)targetDateField).getEditor()).getTextField().getText();
                        inv.setTargetDate(strT.isEmpty() ? null : java.time.LocalDate.parse(strT.split(" ")[0])); 
                    } catch (Exception e) {}
                    try { 
                        String strP = ((javax.swing.JSpinner.DateEditor)((javax.swing.JSpinner)nextTrialField).getEditor()).getTextField().getText();
                        inv.setNextTrialDate(strP.isEmpty() ? null : java.time.LocalDate.parse(strP.split(" ")[0])); 
                    } catch (Exception e) {}
                    inv.setDelayExplanation(delayExplField.getText().trim());
                    inv.setWitnessSummons(summonsField.getText().trim());
                    inv.setVerdictText(verdictArea.getText().trim());
                    inv.setArticleText(articleArea.getText().trim());
                    inv.setIsVerifiedByCourt(verifiedCheck.isSelected());

                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() throws SQLException { investigationService.updateCase(inv); return null; }
                        @Override protected void done() { load(); }
                    }.execute();
                } catch (Exception e) {}
            }
        }.execute();
    }

    private void removeSelectedCase() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;
        int caseId = (int) model.getValueAt(selectedRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete case?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                investigationService.deleteCase(caseId);
                load();
            } catch (SQLException e) {}
        }
    }

    private void showEvidenceDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a case first.");
            return;
        }
        int caseId = (int) model.getValueAt(row, 0);
        String caseNum = (String) model.getValueAt(row, 1);

        try {
            List<com.crimemanagement.entity.Evidence> evidenceList = investigationService.getEvidenceForCase(caseId);
            if (evidenceList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No evidence registered for Case " + caseNum);
                return;
            }

            String[] cols = {"Type", "Description", "Status", "Date Collected"};
            DefaultTableModel evModel = new DefaultTableModel(cols, 0);
            for (com.crimemanagement.entity.Evidence e : evidenceList) {
                evModel.addRow(new Object[]{e.getEvidenceType(), e.getDescription(), e.getStatus(), e.getCollectedDate()});
            }

            JTable evTable = new JTable(evModel);
            UIUtils.applyTableDefaults(evTable);
            
            JScrollPane scrollPane = new JScrollPane(evTable);
            scrollPane.setPreferredSize(new Dimension(600, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Forensic Evidence - Case " + caseNum, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
