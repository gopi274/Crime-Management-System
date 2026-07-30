package com.crimemanagement.util;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

import java.awt.*;

public class DesignSystem {

    // Color Palette (Premium Dark Mode)
    public static final Color BACKGROUND = new Color(15, 20, 35); // Deep Midnight Blue
    public static final Color PANEL_BACKGROUND = new Color(25, 30, 50, 220); 
    public static final Color PRIMARY = new Color(0, 51, 102); // Navy Blue (Official KP Blue)
    public static final Color SECONDARY = new Color(139, 0, 0); // Deep Maroon (Official KP Red)
    public static final Color ACCENT = new Color(212, 175, 55); // Metallic Gold (Official KP Gold)
    public static final Color TEXT_PRIMARY = new Color(245, 245, 250);
    public static final Color TEXT_SECONDARY = new Color(170, 180, 200);
    public static final Color DANGER = new Color(200, 30, 30);
    public static final Color SUCCESS = new Color(40, 160, 80);
    public static final Color WARNING = new Color(210, 150, 40);
    public static final Color SURFACE = BACKGROUND; // Unified background
    public static final Color SURFACE_LIGHTER = new Color(25, 30, 50);
    public static final Color DIVIDER = new Color(60, 70, 100);

    // Official Kerala Police Branding Colors
    public static final Color KP_RED = new Color(139, 0, 0); // Deep Maroon
    public static final Color KP_GOLD = new Color(212, 175, 55); // Metallic Gold
    public static final Color KP_BLUE = new Color(0, 51, 102); // Navy Blue

    // Fonts
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 24);
    public static final Font SUBHEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font CAPTION_FONT = new Font("SansSerif", Font.PLAIN, 12);

    /**
     * Initializes the global look and feel and design constants.
     */
    public static void setup() {
        try {
            FlatDarkLaf.setup();

            // Override global UI defaults for a cohesive midnight blue look
            UIManager.put("Panel.background", BACKGROUND);
            UIManager.put("OptionPane.background", BACKGROUND);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
            UIManager.put("Dialog.background", BACKGROUND);
            UIManager.put("Label.foreground", TEXT_PRIMARY);
            UIManager.put("CheckBox.background", BACKGROUND);
            UIManager.put("CheckBox.foreground", TEXT_PRIMARY);
            UIManager.put("CheckBox.icon.style", "filled");

            // Button Styling
            UIManager.put("Button.arc", 12);
            UIManager.put("Button.background", new Color(30, 35, 60));
            UIManager.put("Button.foreground", TEXT_PRIMARY);

            // Text Component Styling
            UIManager.put("TextField.arc", 10);
            UIManager.put("Component.borderColor", new Color(60, 70, 100));
            UIManager.put("Component.focusedBorderColor", ACCENT);

            // Table Styling
            UIManager.put("Table.selectionBackground", new Color(50, 100, 255, 60));
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("Table.alternateRowColor", new Color(30, 35, 60)); // Dark zebra stripe
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.gridColor", new Color(50, 60, 90));
            UIManager.put("TableHeader.background", new Color(30, 40, 70));
            UIManager.put("TableHeader.foreground", Color.WHITE);
            UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 15));
            UIManager.put("TableHeader.height", 45);

            // TabbedPane Styling
            UIManager.put("TabbedPane.background", BACKGROUND);
            UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
            UIManager.put("TabbedPane.selectedBackground", new Color(35, 40, 65));
            UIManager.put("TabbedPane.selectedForeground", ACCENT);
            UIManager.put("TabbedPane.contentAreaColor", BACKGROUND);
            UIManager.put("TabbedPane.tabsOverlapBorder", true);
            UIManager.put("TabbedPane.tabHeight", 50); // Shorter tabs
            UIManager.put("TabbedPane.minimumTabWidth", 160); // Narrower tabs
            UIManager.put("TabbedPane.tabInsets", new Insets(8, 12, 8, 12)); // Tighter padding

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Applies premium styling to a table.
     */
    public static void applyTableStyle(JTable table) {
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Custom renderer for cell padding
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                c.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15)); // Padding
                if (isSelected) {
                    c.setBackground(new Color(50, 100, 255, 80));
                } else {
                    c.setBackground(row % 2 == 0 ? BACKGROUND : new Color(30, 35, 60));
                }
                return c;
            }
        });
    }

    /**
     * Creates a styled consistent Main Heading Label.
     */
    public static JLabel createHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADER_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /**
     * Applies the 'Card' styling to an existing JPanel.
     * Including rounded corners and semi-transparent background.
     */
    public static void applyCardStyle(JPanel panel) {
        panel.setBackground(PANEL_BACKGROUND);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 20");
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    /**
     * Creates a glowing primary button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0");
        return btn;
    }

    /**
     * Creates a secondary button with a consistent outline or subtle background.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(60, 70, 100));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0");
        return btn;
    }

    public static void applyDialogStyle(JDialog dialog) {
        dialog.getContentPane().setBackground(BACKGROUND);
    }

    public static void applyButtonStyle(JButton btn, boolean isPrimary) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        if (isPrimary) {
            btn.setBackground(PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0");
        } else {
            btn.setBackground(new Color(60, 70, 100));
            btn.setForeground(TEXT_PRIMARY);
            btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0");
        }
    }

    /**
     * Creates a premium styled form panel with a grid-like layout.
     */
    public static JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        return panel;
    }

    public static void addField(JPanel panel, String labelText, JComponent field, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(12, 0, 0, 20);

        JLabel label = new JLabel(labelText);
        label.setFont(BODY_FONT);
        label.setForeground(TEXT_SECONDARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 0, 0);

        if (field instanceof JTextField) {
            field.setPreferredSize(new Dimension(300, 35));
        }

        panel.add(field, gbc);
    }

    /**
     * Specialized date picker component using a spinner with official styling.
     */
    public static JComponent createDatePicker() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(250, 40)); 
        
        // Match the official form style
        spinner.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 1; focusWidth: 0");
        editor.getTextField().setBackground(new Color(25, 30, 55));
        editor.getTextField().setForeground(TEXT_PRIMARY);
        editor.getTextField().setFont(BODY_FONT);
        editor.getTextField().setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        
        return spinner;
    }

    /**
     * Loads the Kerala Police Logo.
     */
    public static ImageIcon getKeralaPoliceLogo(int width, int height) {
        try {
            java.net.URL url = DesignSystem.class.getResource("/images/logo.png");
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {}
        return new ImageIcon(); // Return empty to prevent NPE
    }

    /**
     * Loads the Kerala Police Login Background.
     */
    public static ImageIcon getLoginBackground() {
        try {
            java.net.URL url = DesignSystem.class.getResource("/images/bg.png");
            if (url != null) return new ImageIcon(url);
        } catch (Exception e) {}
        return new ImageIcon();
    }
}
