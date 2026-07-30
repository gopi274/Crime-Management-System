package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Forensic Science Registry Panel V8.0.0
 * Provides a comprehensive view of all Forensic Scientists, Directorates, and Labs.
 */
public class ForensicOfficersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public ForensicOfficersPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = DesignSystem.createHeader("Forensic Science Registry");
        JLabel subtitle = new JLabel("Statewide Directorate of Forensic Science Laboratories (SFSL/RFSL)");
        subtitle.setForeground(DesignSystem.TEXT_SECONDARY);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Search & Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controls.setOpaque(false);
        
        searchField = new JTextField(25);
        searchField.setBackground(DesignSystem.SURFACE_LIGHTER);
        searchField.setForeground(Color.WHITE);
        searchField.putClientProperty("JTextField.placeholderText", "Search by Scientist Name or ID...");
        searchField.setPreferredSize(new Dimension(300, 35));
        
        JButton refreshBtn = DesignSystem.createPrimaryButton("Refresh Registry");
        refreshBtn.addActionListener(e -> loadData());
        
        JButton addBtn = DesignSystem.createPrimaryButton("Add Officer");
        addBtn.addActionListener(e -> onAdd());
        
        JButton editBtn = DesignSystem.createPrimaryButton("Edit Officer");
        editBtn.addActionListener(e -> onEdit());
        
        controls.add(new JLabel("🔍 SEARCH:"));
        controls.add(searchField);
        controls.add(refreshBtn);
        controls.add(addBtn);
        controls.add(editBtn);
        add(controls, BorderLayout.CENTER);

        // Table
        String[] cols = {"ID", "Full Name", "Rank / Designation", "Assigned Lab", "Specialization", "Contact"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        DesignSystem.applyTableStyle(table);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(DesignSystem.DIVIDER));
        
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(controls, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);
        
        add(container, BorderLayout.CENTER);

        loadData();
    }

    private void onAdd() {
        JTextField fn = new JTextField();
        JTextField ln = new JTextField();
        String[] ranks = {"Director", "Joint Director", "Assistant Director", "SSO", "SO", "SSA"};
        JComboBox<String> rankBox = new JComboBox<>(ranks);
        
        JComboBox<String> labBox = new JComboBox<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT lab_id, name FROM forensic_labs")) {
            while (rs.next()) {
                labBox.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        JTextField spec = new JTextField("Forensic Analysis");
        JTextField contact = new JTextField();
        
        JPanel p = DesignSystem.createFormPanel();
        DesignSystem.addField(p, "First Name", fn, 0);
        DesignSystem.addField(p, "Last Name", ln, 1);
        DesignSystem.addField(p, "Rank", rankBox, 2);
        DesignSystem.addField(p, "Assigned Lab", labBox, 3);
        DesignSystem.addField(p, "Specialization", spec, 4);
        DesignSystem.addField(p, "Contact", contact, 5);
        
        if (JOptionPane.showConfirmDialog(this, p, "Add Forensic Scientist", 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String selLab = (String) labBox.getSelectedItem();
            int labId = Integer.parseInt(selLab.split(" - ")[0]);
            String sql = "INSERT INTO forensic_scientists (lab_id, first_name, last_name, rank, specialization, contact_number) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, labId);
                ps.setString(2, fn.getText()); ps.setString(3, ln.getText());
                ps.setString(4, (String)rankBox.getSelectedItem());
                ps.setString(5, spec.getText()); ps.setString(6, contact.getText());
                ps.executeUpdate(); loadData();
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        int sid = (int) model.getValueAt(modelRow, 0);
        
        String[] nameParts = model.getValueAt(modelRow, 1).toString().split(" ");
        JTextField fn = new JTextField(nameParts[0]);
        JTextField ln = new JTextField(nameParts.length > 1 ? nameParts[1] : "");
        
        String[] ranks = {"Director", "Joint Director", "Assistant Director", "SSO", "SO", "SSA"};
        JComboBox<String> rankBox = new JComboBox<>(ranks);
        rankBox.setSelectedItem(model.getValueAt(modelRow, 2));

        JComboBox<String> labBox = new JComboBox<>();
        String currentLabName = model.getValueAt(modelRow, 3).toString();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT lab_id, name FROM forensic_labs")) {
            while (rs.next()) {
                String labEntry = rs.getInt(1) + " - " + rs.getString(2);
                labBox.addItem(labEntry);
                if (rs.getString(2).equals(currentLabName)) labBox.setSelectedItem(labEntry);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        JTextField spec = new JTextField((String)model.getValueAt(modelRow, 4));
        JTextField contact = new JTextField((String)model.getValueAt(modelRow, 5));
        
        JPanel p = DesignSystem.createFormPanel();
        DesignSystem.addField(p, "First Name", fn, 0);
        DesignSystem.addField(p, "Last Name", ln, 1);
        DesignSystem.addField(p, "Rank", rankBox, 2);
        DesignSystem.addField(p, "Assigned Lab", labBox, 3);
        DesignSystem.addField(p, "Specialization", spec, 4);
        DesignSystem.addField(p, "Contact", contact, 5);
        
        if (JOptionPane.showConfirmDialog(this, p, "Edit Forensic Scientist", 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String selLab = (String) labBox.getSelectedItem();
            int labId = Integer.parseInt(selLab.split(" - ")[0]);
            String sql = "UPDATE forensic_scientists SET lab_id=?, first_name=?, last_name=?, rank=?, specialization=?, contact_number=? WHERE scientist_id=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, labId);
                ps.setString(2, fn.getText());
                ps.setString(3, ln.getText());
                ps.setString(4, (String)rankBox.getSelectedItem());
                ps.setString(5, spec.getText());
                ps.setString(6, contact.getText());
                ps.setInt(7, sid);
                ps.executeUpdate(); loadData();
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT s.scientist_id, s.first_name, s.last_name, s.rank, l.name, s.specialization, s.contact_number " +
                     "FROM forensic_scientists s " +
                     "JOIN forensic_labs l ON s.lab_id = l.lab_id " +
                     "ORDER BY s.scientist_id";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2) + " " + rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getString(7)
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading registry: " + ex.getMessage());
        }
    }
}
