package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.util.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * System Settings Panel for Kerala Police ERP V6.2.0 DIRECTORATE.
 * Provides administrative controls for global overrides and HQ synchronization.
 */
public class SystemSettingsPanel extends JPanel {

    public SystemSettingsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Directorate General of Police - HQ Settings");
        title.setFont(DesignSystem.TITLE_FONT);
        title.setForeground(DesignSystem.TEXT_PRIMARY);
        header.add(title, BorderLayout.NORTH);
        
        JLabel subtitle = new JLabel("Directorate State Command - Advanced Administrative Terminal (V6.4.2)");
        subtitle.setFont(DesignSystem.SUBHEADER_FONT);
        subtitle.setForeground(DesignSystem.TEXT_SECONDARY);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Grid of Controls
        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 20));
        grid.setOpaque(false);
        
        
        grid.add(createSettingCard("Legal Framework", "Authoritative BNS 2024 Registry (358 Sections)", "BNS Registry Viewer", e -> showBnsRegistry()));

        
        
        

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Footer
        JButton addBnsBtn = DesignSystem.createPrimaryButton("➕ ADD MANUAL BNS OVERRIDE");
        addBnsBtn.addActionListener(e -> showAddBnsDialog());
        add(addBnsBtn, BorderLayout.SOUTH);
    }

    private JPanel createSettingCard(String title, String desc, String btnText, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(DesignSystem.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.ACCENT, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel tLbl = new JLabel(title);
        tLbl.setFont(DesignSystem.SUBHEADER_FONT);
        tLbl.setForeground(DesignSystem.TEXT_PRIMARY);
        card.add(tLbl, BorderLayout.NORTH);
        
        JLabel dLbl = new JLabel("<html>" + desc + "</html>");
        dLbl.setFont(DesignSystem.BODY_FONT);
        dLbl.setForeground(DesignSystem.TEXT_SECONDARY);
        card.add(dLbl, BorderLayout.CENTER);
        
        JButton btn = DesignSystem.createSecondaryButton(btnText);
        btn.addActionListener(action);
        card.add(btn, BorderLayout.SOUTH);
        
        return card;
    }

    private void showBnsRegistry() {
        JDialog dlg = new JDialog((Frame)null, "BNS 2024 Legal Registry", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(800, 500);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(DesignSystem.BACKGROUND);

        String[] cols = {"Code", "Title", "Category", "Description"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT code, title, category, description FROM legal_sections ORDER BY code")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        JTable table = new JTable(model);
        DesignSystem.applyTableStyle(table);
        
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.putClientProperty("JTextField.placeholderText", "Search BNS Section Code or Title...");
        
        javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(new JLabel("🔍 SEARCH REGISTRY:"), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);

        dlg.add(topPanel, BorderLayout.NORTH);
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton addBtn = DesignSystem.createPrimaryButton("➕ Register New BNS Section");
        addBtn.addActionListener(e -> { dlg.dispose(); showAddBnsDialog(); });
        dlg.add(addBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    
private void showAddBnsDialog() {
        JPanel form = DesignSystem.createFormPanel();
        JTextField codeField = new JTextField("SEC ");
        JTextField titleField = new JTextField();
        String[] cats = {"Women/Child", "Human Body", "Property", "State", "Public Peace", "Other"};
        JComboBox<String> catBox = new JComboBox<>(cats);
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        DesignSystem.addField(form, "BNS Section Code", codeField, 0);
        DesignSystem.addField(form, "Section Title", titleField, 1);
        DesignSystem.addField(form, "Category", catBox, 2);
        DesignSystem.addField(form, "Brief Description", new JScrollPane(descArea), 3);

        int res = JOptionPane.showConfirmDialog(this, form, "V6.2.0 Legal framework Update", 
                                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (res == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO legal_sections (code, title, category, description) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, codeField.getText());
                ps.setString(2, titleField.getText());
                ps.setString(3, (String) catBox.getSelectedItem());
                ps.setString(4, descArea.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Success: Registry updated.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}
