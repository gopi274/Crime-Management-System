package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.dao.IntranetMessageDAO;
import com.crimemanagement.entity.IntranetMessage;
import com.crimemanagement.entity.UserAccount;
import com.crimemanagement.dao.UserAccountDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IntranetPanel extends JPanel {
    private final IntranetMessageDAO dao = new IntranetMessageDAO();
    private Integer currentUserId;
    private JPanel feedPanel;

    private LoginDialog.Role userRole;

    public IntranetPanel(String username, LoginDialog.Role role) {
        this.userRole = role;
        setLayout(new BorderLayout(15, 15));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        try {
            UserAccount account = new UserAccountDAO().findByUsername(username);
            if (account != null) this.currentUserId = account.getUserId();
        } catch (Exception e) {}

        JLabel title = new JLabel("Intranet & BOLO Dispatch Area");
        title.setFont(DesignSystem.TITLE_FONT);
        title.setForeground(DesignSystem.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        JButton composeBtn = new JButton("Dispatch Alert");
        DesignSystem.applyButtonStyle(composeBtn, true);
        composeBtn.addActionListener(e -> showComposeDialog());
        // Only officers can dispatch alerts as per user request
        if (userRole != LoginDialog.Role.OFFICER && userRole != LoginDialog.Role.ADMIN) {
            composeBtn.setVisible(false);
        }

        JButton refreshBtn = new JButton("Refresh");
        DesignSystem.applyButtonStyle(refreshBtn, false);
        refreshBtn.addActionListener(e -> loadFeed());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(refreshBtn);
        actions.add(composeBtn);

        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(DesignSystem.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(DesignSystem.BACKGROUND);
        add(scrollPane, BorderLayout.CENTER);

        loadFeed();
        
        Timer t = new Timer(30000, e -> loadFeed());
        t.start();
    }

    private void loadFeed() {
        feedPanel.removeAll();
        try {
            List<IntranetMessage> msgs = dao.getFeed(currentUserId);
            for (IntranetMessage m : msgs) {
                if ("CAUGHT".equalsIgnoreCase(m.getStatus())) continue;
                
                JPanel card = new JPanel(new BorderLayout(10, 5));
                card.setBackground(m.getIsBolo() ? new Color(60, 20, 20) : DesignSystem.PANEL_BACKGROUND);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(m.getIsBolo() ? Color.RED : new Color(60, 70, 100), 1, true),
                        new EmptyBorder(15, 15, 15, 15)
                ));
                card.setMaximumSize(new Dimension(1000, 150));

                String titleStr = (m.getIsBolo() ? "🚨 BOLO ALERT" : "Direct Message") + " from " + m.getSenderRank() + " " + m.getSenderName();
                JLabel titleLbl = new JLabel(titleStr);
                titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                titleLbl.setForeground(m.getIsBolo() ? Color.RED : DesignSystem.PRIMARY);

                JLabel timeLbl = new JLabel(m.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                timeLbl.setForeground(DesignSystem.TEXT_SECONDARY);
                timeLbl.setFont(DesignSystem.CAPTION_FONT);

                JPanel top = new JPanel(new BorderLayout());
                top.setOpaque(false);
                top.add(titleLbl, BorderLayout.WEST);
                top.add(timeLbl, BorderLayout.EAST);

                JTextArea body = new JTextArea(m.getBody());
                body.setLineWrap(true);
                body.setWrapStyleWord(true);
                body.setOpaque(false);
                body.setForeground(DesignSystem.TEXT_PRIMARY);
                body.setFont(DesignSystem.BODY_FONT);
                body.setEditable(false);

                JPanel footer = new JPanel(new BorderLayout());
                footer.setOpaque(false);
                
                String statusStr = m.getStatus() != null ? m.getStatus() : "PENDING";
                JLabel statusLbl = new JLabel("RESULT: " + statusStr);
                statusLbl.setFont(DesignSystem.CAPTION_FONT);
                statusLbl.setForeground(statusStr.equals("CAUGHT") ? DesignSystem.SUCCESS : (statusStr.equals("MISSED") ? DesignSystem.DANGER : Color.ORANGE));
                footer.add(statusLbl, BorderLayout.WEST);

                if (m.getIsBolo() && (userRole == LoginDialog.Role.OFFICER || userRole == LoginDialog.Role.ADMIN)) {
                    JButton updateBtn = new JButton("Update Alert Result");
                    updateBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    updateBtn.addActionListener(e -> {
                        String[] options = {"PENDING", "CAUGHT", "MISSED"};
                        String choice = (String) JOptionPane.showInputDialog(this, "Update Target Status:", "Log Alert Result",
                                JOptionPane.QUESTION_MESSAGE, null, options, m.getStatus());
                        if (choice != null) {
                            try {
                                dao.updateStatus(m.getMsgId(), choice);
                                loadFeed();
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
                            }
                        }
                    });
                    footer.add(updateBtn, BorderLayout.EAST);
                }

                card.add(top, BorderLayout.NORTH);
                card.add(body, BorderLayout.CENTER);
                card.add(footer, BorderLayout.SOUTH);
                
                feedPanel.add(card);
                feedPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        feedPanel.revalidate();
        feedPanel.repaint();
    }

    private void showComposeDialog() {
        if (currentUserId == null) return;
        JCheckBox boloBox = new JCheckBox("Mark as BOLO (Broadcast Statewide)");
        boloBox.setForeground(Color.RED);
        JTextField receiverIdField = new JTextField(10);
        receiverIdField.setToolTipText("Leave blank if BOLO. Enter exact User ID if direct messaging.");
        JTextArea msgBody = new JTextArea(5, 30);
        msgBody.setLineWrap(true);

        JPanel panel = DesignSystem.createFormPanel();
        panel.add(boloBox);
        DesignSystem.addField(panel, "Receiver User ID (Blank if BOLO)", receiverIdField, 1);
        panel.add(new JLabel("Message content:"));
        panel.add(new JScrollPane(msgBody));

        boloBox.addActionListener(e -> receiverIdField.setEnabled(!boloBox.isSelected()));

        if (JOptionPane.showConfirmDialog(this, panel, "Dispatch New Message", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String body = msgBody.getText().trim();
            if (body.isEmpty()) return;

            IntranetMessage m = new IntranetMessage();
            m.setSenderId(currentUserId);
            m.setBody(body);
            m.setIsBolo(boloBox.isSelected());

            if (!boloBox.isSelected()) {
                try {
                    int rid = Integer.parseInt(receiverIdField.getText().trim());
                    m.setReceiverId(rid);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Valid Receiver User ID required for Direct Messages.");
                    return;
                }
            }
            try {
                dao.create(m);
                JOptionPane.showMessageDialog(this, "Message dispatched!");
                loadFeed();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dispatch failed: " + ex.getMessage());
            }
        }
    }
}
