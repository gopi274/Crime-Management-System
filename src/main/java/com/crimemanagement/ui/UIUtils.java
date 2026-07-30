package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;
import java.awt.*;

public class UIUtils {
    public static void applyTableDefaults(JTable table) {
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setBackground(DesignSystem.BACKGROUND);
        DesignSystem.applyTableStyle(table);

        final javax.swing.table.TableCellRenderer originalRenderer = table.getDefaultRenderer(Object.class);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    String text = value != null ? value.toString() : "";
                    if (text.length() > 30 || text.endsWith("...")) {
                        label.setToolTipText(text);
                    } else {
                        label.setToolTipText(null);
                    }
                }
                return c;
            }
        });
    }

    public static void bindSearch(JTable table, JTextField searchField) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>((TableModel) table.getModel());
        table.setRowSorter(sorter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String text = searchField.getText();
                if (text == null || text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });
    }

    public static JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(30, 35, 60));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 60, 100), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        JLabel tLbl = new JLabel(title.toUpperCase());
        tLbl.setFont(DesignSystem.CAPTION_FONT);
        tLbl.setForeground(DesignSystem.TEXT_SECONDARY);
        
        JLabel vLbl = new JLabel(value);
        vLbl.setFont(new Font("Serif", Font.BOLD, 22));
        vLbl.setForeground(color);
        
        card.add(tLbl, BorderLayout.NORTH);
        card.add(vLbl, BorderLayout.CENTER);
        return card;
    }

    public static void addDetailRow(JPanel p, String label, String value) {
        JLabel l = new JLabel(label + ":");
        l.setFont(DesignSystem.BODY_FONT);
        l.setForeground(DesignSystem.TEXT_SECONDARY);
        
        JLabel v = new JLabel(value != null ? value : "N/A");
        v.setFont(DesignSystem.BODY_FONT);
        v.setForeground(Color.WHITE);
        
        p.add(l);
        p.add(v);
    }
}
