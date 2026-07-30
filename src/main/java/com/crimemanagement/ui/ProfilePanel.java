package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.dao.OfficerDAO;
import com.crimemanagement.entity.Officer;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ProfilePanel extends JPanel {
    private final int officerId;
    private final OfficerDAO dao = new OfficerDAO();

    public ProfilePanel(int officerId) {
        this.officerId = officerId;
        setup();
    }

    private void setup() {
        setLayout(new BorderLayout(20, 20));
        setBackground(DesignSystem.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        try {
            Officer o = dao.findById(officerId);
            if (o == null) {
                add(new JLabel("Profile not found."), BorderLayout.CENTER);
                return;
            }

            // Header Section
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel nameLbl = new JLabel(o.getRank() + " " + o.getFullName());
            nameLbl.setFont(DesignSystem.TITLE_FONT);
            nameLbl.setForeground(DesignSystem.KP_GOLD);
            header.add(nameLbl, BorderLayout.WEST);

            JPanel statsGrid = new JPanel(new GridLayout(1, 4, 20, 0));
            statsGrid.setOpaque(false);
            statsGrid.add(UIUtils.createStatCard("Badge", o.getBadgeNumber(), DesignSystem.ACCENT));
            statsGrid.add(UIUtils.createStatCard("Leave Balance", String.valueOf(o.getLeaveBalance()), DesignSystem.SUCCESS));
            statsGrid.add(UIUtils.createStatCard("Base Salary", "₹" + String.format("%.2f", o.getSalary()), DesignSystem.KP_GOLD));
            
            double da = o.getSalary() * 0.07;
            double hra = o.getSalary() * 0.10;
            double gross = o.getSalary() + da + hra + 2000;
            statsGrid.add(UIUtils.createStatCard("Gross Pay", "₹" + String.format("%.2f", gross), DesignSystem.ACCENT));

            JPanel center = new JPanel(new BorderLayout(0, 30));
            center.setOpaque(false);
            center.add(statsGrid, BorderLayout.NORTH);

            // Detailed Card
            JPanel details = new JPanel(new GridLayout(0, 2, 20, 20));
            details.setOpaque(false);
            details.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(DesignSystem.DIVIDER), "Employee Details", 0, 0, DesignSystem.BODY_FONT, DesignSystem.TEXT_SECONDARY));

            UIUtils.addDetailRow(details, "Email", o.getEmail());
            UIUtils.addDetailRow(details, "Phone", o.getPhoneNumber());
            UIUtils.addDetailRow(details, "Jurisdiction", o.getJurisdictionLevel());
            UIUtils.addDetailRow(details, "Duty Status", o.getIsOnDuty() ? "ON DUTY" : "OFF DUTY");
            
            center.add(details, BorderLayout.CENTER);

            // Actions
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            actions.setOpaque(false);
            
            JButton applyLeave = DesignSystem.createPrimaryButton("✉️ Apply for Leave");
            applyLeave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Leave application submitted to District Command."));
            
            JButton printSlip = new JButton("📄 Print Graduation Payslip");
            DesignSystem.applyButtonStyle(printSlip, false);
            printSlip.addActionListener(e -> printPayslip(o, da, hra, gross));
            
            actions.add(applyLeave);
            actions.add(printSlip);

            add(header, BorderLayout.NORTH);
            add(center, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printPayslip(Officer o, double da, double hra, double gross) {
        String slip = String.format("""
            ==========================================
                KERALA STATE POLICE - PAY ADVICE
            ==========================================
            Emp Name: %s %s
            Badge:    %s
            Month:    APRIL 2026
            ------------------------------------------
            Earnings:
              Basic Pay:        ₹%.2f
              Dearness (DA):    ₹%.2f
              House Rent (HRA): ₹%.2f
              Travel (TA):      ₹2000.00
            ------------------------------------------
            GROSS SALARY:       ₹%.2f
            ==========================================
            Status: CREDITED TO TREASURY ACCOUNT
            """, o.getRank(), o.getFullName(), o.getBadgeNumber(), o.getSalary(), da, hra, gross);
        
        JTextArea ta = new JTextArea(12, 40);
        ta.setText(slip);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Kochi-Justice Payslip Registry", JOptionPane.INFORMATION_MESSAGE);
    }
}
