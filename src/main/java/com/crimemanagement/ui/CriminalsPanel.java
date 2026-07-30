package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.entity.Criminal;
import com.crimemanagement.service.CriminalService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CriminalsPanel extends JPanel {
    private final CriminalService service = new CriminalService();
    private DefaultTableModel model;
    private JTable table;
    private final LoginDialog.Role userRole;
    private java.util.Map<Integer, Criminal> criminalMap = new java.util.HashMap<>(); // Store criminals by ID

    public CriminalsPanel(LoginDialog.Role role) {
        this.userRole = role;
        setup();
    }

    /**
     * Constructor for backward compatibility.
     * Defaults to ADMIN role for full access.
     */
    public CriminalsPanel() {
        this.userRole = LoginDialog.Role.ADMIN;
        setup();
    }

    private void setup() {
        super.setLayout(new BorderLayout());
        model = new DefaultTableModel(new Object[] {
                "ID", "First", "Last", "Alias", "Date of Birth", "Age", "Gender",
                "Address", "Phone Number", "Criminal Record" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        UIUtils.applyTableDefaults(table);

        // Set minimum column widths for truncated columns to ensure ellipsis is visible
        table.getColumnModel().getColumn(7).setMinWidth(120); // Address
        table.getColumnModel().getColumn(9).setMinWidth(150); // Criminal Record

        // Add mouse click listener to show full text for truncated cells
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int viewRow = table.rowAtPoint(e.getPoint());
                    int viewCol = table.columnAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewCol >= 0) {
                        // Convert view row to model row (handles sorting)
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        int modelCol = table.convertColumnIndexToModel(viewCol);
                        Object value = model.getValueAt(modelRow, modelCol);
                        if (value != null) {
                            String text = value.toString();
                            // Check if text is truncated (contains "...") - even if only one dot is visible
                            if (text.contains("...")) {
                                // Get the Criminal ID from the first column
                                Integer criminalId = (Integer) model.getValueAt(modelRow, 0);
                                Criminal criminal = criminalMap.get(criminalId);
                                if (criminal != null) {
                                    String columnName = model.getColumnName(modelCol);
                                    String fullText = "";

                                    // Get full text based on column
                                    if (columnName.equals("Address")) {
                                        fullText = criminal.getAddress() != null ? criminal.getAddress() : "N/A";
                                    } else if (columnName.equals("Criminal Record")) {
                                        fullText = criminal.getCriminalRecord() != null ? criminal.getCriminalRecord()
                                                : "N/A";
                                    }

                                    // Only show dialog if full text is different from displayed text (truncated)
                                    if (!fullText.isEmpty() && !fullText.equals("N/A") && !fullText.equals(text)) {
                                        // Show full text in a dialog
                                        JTextArea textArea = new JTextArea(fullText);
                                        textArea.setWrapStyleWord(true);
                                        textArea.setLineWrap(true);
                                        textArea.setEditable(false);
                                        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                                        JScrollPane scrollPane = new JScrollPane(textArea);
                                        scrollPane.setPreferredSize(new Dimension(500, 200));
                                        JOptionPane.showMessageDialog(CriminalsPanel.this, scrollPane,
                                                "Full " + columnName, JOptionPane.INFORMATION_MESSAGE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        JTextField search = new JTextField(18);
        UIUtils.bindSearch(table, search);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());
        JButton addBtn = new JButton("Add Criminal");
        addBtn.addActionListener(e -> addCriminal());

        JButton editBtn = new JButton("Edit Criminal");
        editBtn.addActionListener(e -> editCriminal());

        JButton removeBtn = new JButton("Remove Criminal");
        removeBtn.addActionListener(e -> removeCriminal());

        // Disable add/edit/delete for COURT role (read-only)
        boolean isReadOnly = userRole == LoginDialog.Role.COURT;
        addBtn.setEnabled(!isReadOnly);
        editBtn.setEnabled(!isReadOnly);
        removeBtn.setEnabled(!isReadOnly);

        JButton posterBtn = new JButton("Detailed Poster");
        posterBtn.addActionListener(e -> generatePoster());

        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(search);
        top.add(refresh);
        top.add(addBtn);
        top.add(editBtn);
        top.add(removeBtn);
        top.add(posterBtn);
        add(top, BorderLayout.NORTH);
        load();
    }

    private void addCriminal() {
        JTextField firstField = new JTextField(15);
        JTextField lastField = new JTextField(15);
        JTextField aliasField = new JTextField(15);
        JComboBox<String> genderBox = new JComboBox<>(new String[] { "M", "F", "Other" });
        JTextField dobField = new JTextField(15);
        JTextField addressField = new JTextField(20);
        JTextField phoneField = new JTextField(15);
        JTextField recordField = new JTextField(20);

        JPanel panel = DesignSystem.createFormPanel();
        DesignSystem.addField(panel, "First Name", firstField, 0);
        DesignSystem.addField(panel, "Last Name", lastField, 1);
        DesignSystem.addField(panel, "Alias", aliasField, 2);
        DesignSystem.addField(panel, "Gender", genderBox, 3);
        DesignSystem.addField(panel, "DOB (YYYY-MM-DD)", dobField, 4);
        DesignSystem.addField(panel, "Address", addressField, 5);
        DesignSystem.addField(panel, "Phone Number", phoneField, 6);
        DesignSystem.addField(panel, "Criminal Record", recordField, 7);

        int option = JOptionPane.showConfirmDialog(this, panel, "Add New Criminal", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            // Validate required fields
            if (firstField.getText().trim().isEmpty() || lastField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "First Name and Last Name are required.", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Criminal c = new Criminal();
            c.setFirstName(firstField.getText().trim());
            c.setLastName(lastField.getText().trim());

            String alias = aliasField.getText().trim();
            if (!alias.isEmpty()) {
                c.setAlias(alias);
            }

            c.setGender((String) genderBox.getSelectedItem());

            // Set date of birth if provided
            String dobStr = dobField.getText().trim();
            if (!dobStr.isEmpty()) {
                try {
                    c.setDateOfBirth(java.time.LocalDate.parse(dobStr));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD format.", "Date Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Set address if provided
            String address = addressField.getText().trim();
            if (!address.isEmpty()) {
                c.setAddress(address);
            }

            // Set phone number if provided (must be exactly 10 digits)
            String phone = phoneField.getText().trim();
            if (!phone.isEmpty()) {
                // Remove any non-digit characters for validation
                String digitsOnly = phone.replaceAll("[^0-9]", "");
                if (digitsOnly.length() != 10) {
                    JOptionPane.showMessageDialog(this, "Phone number must contain exactly 10 digits.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                c.setPhoneNumber(digitsOnly);
            }

            // Set criminal record if provided
            String record = recordField.getText().trim();
            if (!record.isEmpty()) {
                c.setCriminalRecord(record);
            }

            try {
                service.createCriminal(c);
                load();
                JOptionPane.showMessageDialog(this, "Criminal added successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding criminal: " + e.getMessage());
            }
        }
    }

    private void editCriminal() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a criminal to edit.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int criminalId = (int) model.getValueAt(selectedRow, 0);

        SwingWorker<Criminal, Void> loadWorker = new SwingWorker<>() {
            @Override
            protected Criminal doInBackground() throws Exception {
                return service.getCriminalById(criminalId);
            }

            @Override
            protected void done() {
                try {
                    Criminal c = get();
                    if (c == null) {
                        JOptionPane.showMessageDialog(CriminalsPanel.this, "Criminal not found.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    JTextField firstField = new JTextField(c.getFirstName(), 15);
                    JTextField lastField = new JTextField(c.getLastName(), 15);
                    JTextField aliasField = new JTextField(c.getAlias() != null ? c.getAlias() : "", 15);
                    JComboBox<String> genderBox = new JComboBox<>(new String[] { "M", "F", "Other" });
                    genderBox.setSelectedItem(c.getGender());
                    JTextField dobField = new JTextField(
                            c.getDateOfBirth() != null ? c.getDateOfBirth().toString() : "", 15);
                    JTextField addressField = new JTextField(c.getAddress() != null ? c.getAddress() : "", 20);
                    JTextField phoneField = new JTextField(c.getPhoneNumber() != null ? c.getPhoneNumber() : "", 15);
                    JTextField recordField = new JTextField(c.getCriminalRecord() != null ? c.getCriminalRecord() : "",
                            20);

                    JPanel panel = DesignSystem.createFormPanel();
                    DesignSystem.addField(panel, "First Name", firstField, 0);
                    DesignSystem.addField(panel, "Last Name", lastField, 1);
                    DesignSystem.addField(panel, "Alias", aliasField, 2);
                    DesignSystem.addField(panel, "Gender", genderBox, 3);
                    DesignSystem.addField(panel, "DOB (YYYY-MM-DD)", dobField, 4);
                    DesignSystem.addField(panel, "Address", addressField, 5);
                    DesignSystem.addField(panel, "Phone Number", phoneField, 6);
                    DesignSystem.addField(panel, "Criminal Record", recordField, 7);

                    int option = JOptionPane.showConfirmDialog(CriminalsPanel.this, panel, "Edit Criminal",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (option == JOptionPane.OK_OPTION) {
                        // Validate required fields
                        if (firstField.getText().trim().isEmpty() || lastField.getText().trim().isEmpty()) {
                            JOptionPane.showMessageDialog(CriminalsPanel.this, "First Name and Last Name are required.",
                                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        c.setFirstName(firstField.getText().trim());
                        c.setLastName(lastField.getText().trim());

                        String alias = aliasField.getText().trim();
                        if (!alias.isEmpty()) {
                            c.setAlias(alias);
                        } else {
                            c.setAlias(null);
                        }

                        c.setGender((String) genderBox.getSelectedItem());

                        // Set date of birth if provided
                        String dobStr = dobField.getText().trim();
                        if (!dobStr.isEmpty()) {
                            try {
                                c.setDateOfBirth(java.time.LocalDate.parse(dobStr));
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(CriminalsPanel.this,
                                        "Invalid date format. Use YYYY-MM-DD format.", "Date Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } else {
                            c.setDateOfBirth(null);
                        }

                        // Set address if provided
                        String address = addressField.getText().trim();
                        if (!address.isEmpty()) {
                            c.setAddress(address);
                        } else {
                            c.setAddress(null);
                        }

                        // Set phone number if provided (with validation)
                        String phone = phoneField.getText().trim();
                        if (!phone.isEmpty()) {
                            String digitsOnly = phone.replaceAll("[^0-9]", "");
                            if (digitsOnly.length() != 10) {
                                JOptionPane.showMessageDialog(CriminalsPanel.this,
                                        "Phone number must contain exactly 10 digits.", "Validation Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            c.setPhoneNumber(digitsOnly);
                        } else {
                            c.setPhoneNumber(null);
                        }

                        // Set criminal record if provided
                        String record = recordField.getText().trim();
                        if (!record.isEmpty()) {
                            c.setCriminalRecord(record);
                        } else {
                            c.setCriminalRecord(null);
                        }

                        SwingWorker<Void, Void> updateWorker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                service.updateCriminal(c);
                                return null;
                            }

                            @Override
                            protected void done() {
                                try {
                                    get();
                                    load();
                                    JOptionPane.showMessageDialog(CriminalsPanel.this, "Criminal updated successfully!",
                                            "Success", JOptionPane.INFORMATION_MESSAGE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(CriminalsPanel.this,
                                            "Error updating criminal: " + e.getMessage(), "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        };
                        updateWorker.execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(CriminalsPanel.this, "Error loading criminal: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        loadWorker.execute();
    }

    private void removeCriminal() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Remove selected criminal?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    service.deleteCriminal(id); // <-- matches your CriminalService
                    load();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error removing criminal: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a criminal to remove");
        }
    }

    private void load() {
        SwingWorker<List<Criminal>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Criminal> doInBackground() throws Exception {
                try {
                    return service.getAllCriminals();
                } catch (Exception ex) {
                    return java.util.Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Criminal> list = get();
                    model.setRowCount(0);
                    criminalMap.clear(); // Clear previous data
                    for (Criminal c : list) {
                        // Store criminal in map for later retrieval
                        criminalMap.put(c.getCriminalId(), c);
                        // Format date of birth
                        String dob = c.getDateOfBirth() != null
                                ? c.getDateOfBirth().toString()
                                : "N/A";

                        // Calculate age
                        int age = c.getAge();
                        String ageDisplay = age > 0 ? String.valueOf(age) : "N/A";

                        // Truncate address if too long (make ellipsis more visible)
                        String address = c.getAddress();
                        if (address != null && address.length() > 20) {
                            address = address.substring(0, 17) + "...";
                        } else if (address == null || address.trim().isEmpty()) {
                            address = "N/A";
                        }

                        // Truncate criminal record if too long (make ellipsis more visible)
                        String record = c.getCriminalRecord();
                        if (record != null && record.length() > 40) {
                            record = record.substring(0, 37) + "...";
                        } else if (record == null || record.trim().isEmpty()) {
                            record = "N/A";
                        }

                        // Display phone number or "N/A"
                        String phone = c.getPhoneNumber() != null && !c.getPhoneNumber().trim().isEmpty()
                                ? c.getPhoneNumber()
                                : "N/A";

                        model.addRow(new Object[] {
                                c.getCriminalId(),
                                c.getFirstName(),
                                c.getLastName(),
                                c.getAlias() != null && !c.getAlias().trim().isEmpty() ? c.getAlias() : "N/A",
                                dob,
                                ageDisplay,
                                c.getGender(),
                                address,
                                phone,
                                record
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        w.execute();
    }

    private void generatePoster() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a criminal first");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("wanted_poster_" + id + ".pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Criminal c = service.getCriminalById(id);
                new com.crimemanagement.service.PDFService().generateWantedPoster(c,
                        fc.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Poster generated successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}
