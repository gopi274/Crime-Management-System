package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    public enum Role {
        OFFICER, ADMIN, COURT,
        FORENSIC_EXAMINER, JAILER, CITIZEN
    }

    private JTextField username;
    private JPasswordField password;
    private JComboBox<Role> role;
    private boolean success = false;
    private String loggedInUsername;
    private Integer loggedInOfficerId;
    private Integer loggedInJailId;
    private Integer loggedInCourtId;
    private Integer loggedInScientistId;

    public LoginDialog(Frame owner) {
        super(owner, "KERALA POLICE - Secure Login", true); // true for modal
        DesignSystem.setup(); 
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(owner);
        setUndecorated(false); 
        
        // Final Kochi-Justice Branding: Set Window Icon
        try {
            setIconImage(DesignSystem.getKeralaPoliceLogo(64, 64).getImage());
        } catch (Exception ex) {}

        // Main Layered Pane for Background
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.setBackground(new Color(20, 25, 45));
        layeredPane.setOpaque(true);

        // 1. Background Image
        JLabel background = new JLabel(DesignSystem.getLoginBackground());
        background.setBounds(0, 0, 1000, 700);
        layeredPane.add(background, Integer.valueOf(0));

        // 2. Dark Overlay for Contrast
        JPanel overlay = new JPanel();
        overlay.setBackground(new Color(10, 15, 30, 210));
        overlay.setBounds(0, 0, 1000, 700);
        layeredPane.add(overlay, Integer.valueOf(1));

        // 3. Central Login Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(new Color(25, 30, 50, 240));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DesignSystem.KP_GOLD, 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        
        // Solid dimension to prevent truncation
        card.setBounds(250, 50, 500, 580);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.CENTER;

        // Emblem
        JLabel logo = new JLabel(DesignSystem.getKeralaPoliceLogo(110, 110));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 15, 0);
        card.add(logo, c);

        // Titles
        JLabel title = new JLabel("KERALA POLICE");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(DesignSystem.KP_GOLD);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 5, 0);
        card.add(title, c);

        JLabel subTitle = new JLabel("Login");
        subTitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subTitle.setForeground(DesignSystem.TEXT_SECONDARY);
        subTitle.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 35, 0);
        card.add(subTitle, c);

        // Form Fields - VERTICAL STACK ONLY
        c.insets = new Insets(5, 0, 5, 0);
        
        username = new JTextField();
        username.setPreferredSize(new Dimension(350, 50));
        username.setFont(DesignSystem.BODY_FONT);
        username.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 90, 130)), "Username", 0, 0, DesignSystem.CAPTION_FONT, DesignSystem.KP_GOLD));
        c.gridy = 3;
        card.add(username, c);

        password = new JPasswordField();
        password.setPreferredSize(new Dimension(350, 50));
        password.setFont(DesignSystem.BODY_FONT);
        password.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 90, 130)), "Password", 0, 0, DesignSystem.CAPTION_FONT, DesignSystem.KP_GOLD));
        c.gridy = 4;
        card.add(password, c);

        // Dropdown
        java.util.List<Role> visibleRoles = new java.util.ArrayList<>();
        for (Role r : Role.values()) {
            if (r != Role.CITIZEN) visibleRoles.add(r);
        }
        role = new JComboBox<>(visibleRoles.toArray(new Role[0]));
        role.setPreferredSize(new Dimension(350, 45));
        role.setBackground(new Color(30, 35, 60));
        role.setForeground(Color.WHITE);
        role.setFont(DesignSystem.BODY_FONT);
        c.gridy = 5;
        c.insets = new Insets(15, 0, 20, 0);
        card.add(role, c);

        // Login Button
        JButton loginBtn = DesignSystem.createPrimaryButton("SECURE LOGIN");
        loginBtn.setPreferredSize(new Dimension(350, 50));
        loginBtn.setBackground(new Color(150, 0, 0));
        loginBtn.addActionListener(e -> onLogin());
        c.gridy = 6;
        c.insets = new Insets(10, 0, 10, 0);
        card.add(loginBtn, c);

        // Exit
        JButton closeBtn = new JButton("Exit System");
        closeBtn.setContentAreaFilled(false);
        closeBtn.setForeground(DesignSystem.TEXT_SECONDARY);
        closeBtn.addActionListener(e -> System.exit(0));
        c.gridy = 7;
        c.insets = new Insets(5, 0, 20, 0);
        card.add(closeBtn, c);

        // Settings
        JButton settingsBtn = new JButton("HQ Connection Settings");
        settingsBtn.setFont(DesignSystem.CAPTION_FONT);
        settingsBtn.setForeground(DesignSystem.KP_GOLD);
        settingsBtn.setContentAreaFilled(false);
        settingsBtn.setBorder(null);
        settingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsBtn.addActionListener(e -> showConnectionSettings());
        c.gridy = 8;
        card.add(settingsBtn, c);

        layeredPane.add(card, Integer.valueOf(2));
        setContentPane(layeredPane);
    }

    private void showConnectionSettings() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField urlField = new JTextField("jdbc:mysql://localhost:3306/crime_management");
        JTextField userField = new JTextField("root");
        JPasswordField passField = new JPasswordField("");
        
        // Load existing if possible
        java.io.File cfg = new java.io.File("config/database.properties");
        if (cfg.exists()) {
            try (java.io.InputStream in = new java.io.FileInputStream(cfg)) {
                java.util.Properties p = new java.util.Properties();
                p.load(in);
                urlField.setText(p.getProperty("db.url", urlField.getText()));
                userField.setText(p.getProperty("db.username", userField.getText()));
                passField.setText(p.getProperty("db.password", ""));
            } catch (Exception ignored) {}
        }

        panel.add(new JLabel("Server Connection URL (Use HQ IP):"));
        panel.add(urlField);
        panel.add(new JLabel("DB Username:"));
        panel.add(userField);
        panel.add(new JLabel("DB Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "HQ Database Connection Settings", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                java.io.File dir = new java.io.File("config");
                if (!dir.exists()) dir.mkdirs();
                try (java.io.FileWriter fw = new java.io.FileWriter(cfg)) {
                    fw.write("db.url=" + urlField.getText() + "\n");
                    fw.write("db.username=" + userField.getText() + "\n");
                    fw.write("db.password=" + new String(passField.getPassword()) + "\n");
                    fw.write("db.driver=com.mysql.cj.jdbc.Driver\n");
                }
                JOptionPane.showMessageDialog(this, "Settings saved. Please restart the ERP to apply changes.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onLogin() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword());
        Role selectedRole = (Role) role.getSelectedItem();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedRole == null) {
            JOptionPane.showMessageDialog(this, "Please select a role", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            com.crimemanagement.service.AuthService auth = new com.crimemanagement.service.AuthService();
            auth.ensureDefaultAdmin();

            // Check if any users exist with the selected role
            com.crimemanagement.dao.UserAccountDAO userDAO = new com.crimemanagement.dao.UserAccountDAO();
            java.util.List<com.crimemanagement.entity.UserAccount> usersWithRole = userDAO
                    .findByRole(selectedRole.name());

            if (usersWithRole.isEmpty()) {
                String message = "No users found with " + selectedRole + " role.\n\n" +
                        "Please log in as ADMIN to create a user with this role.\n" +
                        "Go to: Admin tab → Add User";
                JOptionPane.showMessageDialog(this, message, "No Users Found", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Authenticate the user
            com.crimemanagement.entity.UserAccount account = auth.authenticate(user, pass);
            if (account != null) {
                Role authenticatedRole;
                try {
                    authenticatedRole = Role.valueOf(account.getRole());
                } catch (IllegalArgumentException e) {
                    authenticatedRole = Role.OFFICER; // Default fallback
                }

                // Verify role matches
                if (authenticatedRole != selectedRole) {
                    String message = "User '" + user + "' has role '" + authenticatedRole + "', " +
                            "but you selected '" + selectedRole + "'.\n\n" +
                            "Please select the correct role for this user.";
                    JOptionPane.showMessageDialog(this, message, "Role Mismatch", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Success - role matches
                success = true;
                loggedInUsername = account.getUsername();
                loggedInOfficerId = account.getOfficerId();
                loggedInJailId = account.getJailId();
                loggedInCourtId = account.getCourtId();
                loggedInScientistId = account.getScientistId();
                role.setSelectedItem(authenticatedRole);

                // Show success message
                JOptionPane.showMessageDialog(this,
                        "Login successful!\nLogged in as: " + loggedInUsername + "\nRole: " + authenticatedRole,
                        "Login Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public Role getRole() {
        return (Role) role.getSelectedItem();
    }

    public String getUsername() {
        return loggedInUsername;
    }

    public Integer getOfficerId() {
        return loggedInOfficerId;
    }

    public Integer getJailId() {
        return loggedInJailId;
    }

    public Integer getCourtId() {
        return loggedInCourtId;
    }

    public Integer getScientistId() {
        return loggedInScientistId;
    }
}
