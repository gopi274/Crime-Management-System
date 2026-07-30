package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.service.AuditService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditLogPanel extends JPanel {
    private final AuditService auditService = new AuditService();
    private final JTable logTable;
    private final DefaultTableModel model;

    public AuditLogPanel() {
        super(new BorderLayout());
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = DesignSystem.createHeader("Security Audit Trail");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        String[] cols = { "Timestamp", "Actor", "Action", "Details" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        logTable = new JTable(model);
        UIUtils.applyTableDefaults(logTable);

        // Custom column widths
        logTable.getColumnModel().getColumn(0).setPreferredWidth(160); // Timestamp
        logTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Actor
        logTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Action
        logTable.getColumnModel().getColumn(3).setPreferredWidth(450); // Details

        JScrollPane scroll = new JScrollPane(logTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 100)));
        add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        JButton refresh = DesignSystem.createPrimaryButton("Refresh Logs");
        refresh.addActionListener(e -> loadLogs());
        south.add(refresh);
        add(south, BorderLayout.SOUTH);

        loadLogs();
    }

    private void loadLogs() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return auditService.getRecentLogs(100);
            }

            @Override
            protected void done() {
                try {
                    List<String> logs = get();
                    model.setRowCount(0);
                    for (String line : logs) {
                        try {
                            // Expected format: [TIMESTAMP] ACTOR: ACTION | DETAILS
                            // Example: [2026-03-27 11:42:00] Admin: LOGIN | User admin logged in
                            String timestamp = "-";
                            String actor = "-";
                            String action = "-";
                            String details = line; // Default to showing full line in details if parsing fails

                            if (line.startsWith("[")) {
                                int tsEnd = line.indexOf("]");
                                if (tsEnd > 0) {
                                    timestamp = line.substring(1, tsEnd);
                                    int actorEnd = line.indexOf(":", tsEnd);
                                    if (actorEnd > 0) {
                                        actor = line.substring(tsEnd + 2, actorEnd).trim();
                                        int actionEnd = line.indexOf("|", actorEnd);
                                        if (actionEnd > 0) {
                                            action = line.substring(actorEnd + 1, actionEnd).trim();
                                            details = line.substring(actionEnd + 1).trim();
                                        } else {
                                             // No pipe found, treat everything after colon as details
                                             action = "General";
                                             details = line.substring(actorEnd + 1).trim();
                                        }
                                    }
                                }
                            }
                            model.addRow(new Object[] { timestamp, actor, action, details });
                        } catch (Exception e) {
                            // Fallback if parsing fails completely
                            model.addRow(new Object[] { "-", "-", "-", line });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
