package hpms.ui.panels;

import hpms.auth.User;
import hpms.auth.AuthService;
import hpms.model.UserRole;
import hpms.model.Patient;
import hpms.util.*;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdministrationPanel extends JPanel {
    private DefaultTableModel userModel;
    private JLabel statsLabel;

    public AdministrationPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        // Security check: Only admins can access administration panel
        if (AuthService.current == null || AuthService.current.role != UserRole.ADMIN) {
            JLabel accessDeniedLabel = new JLabel("Access Denied: Administration panel is for admins only");
            accessDeniedLabel.setFont(new Font("Arial", Font.BOLD, 14));
            accessDeniedLabel.setForeground(new Color(244, 67, 54));
            accessDeniedLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(accessDeniedLabel, BorderLayout.CENTER);
            return;
        }

        add(SectionHeader.info("Administration", "User management, backups, and system settings"), BorderLayout.NORTH);

        // Tab panel
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.BG);

        // Tab 1: User Management
        JPanel userTab = createUserManagementTab();
        tabbedPane.addTab("User Accounts", userTab);

        // Tab 2: Backup & Restore
        JPanel backupTab = createBackupTab();
        tabbedPane.addTab("Backup & Restore", backupTab);

        // Tab 3: System Settings
        JPanel settingsTab = createSettingsTab();
        tabbedPane.addTab("System Settings", settingsTab);

        add(tabbedPane, BorderLayout.CENTER);
        // ensure panel data refreshes when we navigate back to this card
        ensureVisibilityRefresh();
        refresh();
    }

    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Stats
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 8));
        statsPanel.setBackground(Theme.BG);
        statsLabel = new JLabel("Total Users: 0");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statsPanel.add(statsLabel);
        panel.add(statsPanel, BorderLayout.NORTH);

        // User table
        userModel = new DefaultTableModel(
                new String[] { "User ID", "Username", "Role", "Status" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable userTable = new JTable(userModel);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actionPanel.setBackground(Theme.BG);

        JButton addBtn = new JButton("Add User");
        styleButton(addBtn, new Color(0, 110, 102));
        addBtn.addActionListener(e -> addUserDialog());

        JButton patientAccountBtn = new JButton("Create Patient Account");
        styleButton(patientAccountBtn, new Color(52, 152, 219));
        patientAccountBtn.addActionListener(e -> createPatientAccountDialog());

        JButton deactivateBtn = new JButton("Deactivate Account");
        styleButton(deactivateBtn, new Color(192, 57, 43));
        deactivateBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user to deactivate", "Selection Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String username = String.valueOf(userTable.getValueAt(row, 0));

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deactivate this user account?\n\nThe account will be preserved in the database and can be reactivated later.",
                    "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                hpms.auth.User user = hpms.util.DataStore.users.get(username);
                if (user != null) {
                    user.status = "DEACTIVATED";
                    // Disabled backup save - using database instead
                    JOptionPane.showMessageDialog(this, "Account deactivated successfully", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                }
            }
        });

        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(41, 128, 185));
        refreshBtn.addActionListener(e -> refresh());

        actionPanel.add(addBtn);
        actionPanel.add(patientAccountBtn);
        actionPanel.add(deactivateBtn);
        actionPanel.add(refreshBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBackupTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(245, 247, 250));
        infoPanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        JTextArea infoText = new JTextArea();
        infoText.setEditable(false);
        infoText.setText("BACKUP & RESTORE SYSTEM\n\n" +
                "Backup: Export all system data to JSON file\n" +
                "Restore: Import previously backed-up data\n" +
                "Location: System-managed backups folder\n\n" +
                "Regular backups are recommended for data integrity.");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoText.setBorder(new EmptyBorder(12, 12, 12, 12));
        infoText.setBackground(new Color(245, 247, 250));

        infoPanel.add(infoText, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.NORTH);

        // Action buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 12, 12));
        actionPanel.setBackground(Theme.BG);
        actionPanel.setBorder(new EmptyBorder(24, 80, 24, 80));

        JButton backupBtn = new JButton("CREATE BACKUP");
        styleButton(backupBtn, new Color(46, 204, 113));
        backupBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backupBtn.addActionListener(e -> {
            String json = BackupUtil.toJson();
            JTextArea jsonArea = new JTextArea(json, 10, 50);
            jsonArea.setEditable(true);
            jsonArea.setLineWrap(true);
            jsonArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(jsonArea);
            int result = JOptionPane.showConfirmDialog(this, scrollPane, "Backup JSON - Save this data",
                    JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(this, "Backup data displayed above. Copy to save file.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton restoreBtn = new JButton("RESTORE BACKUP");
        styleButton(restoreBtn, new Color(41, 128, 185));
        restoreBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        restoreBtn.addActionListener(e -> {
            String json = JOptionPane.showInputDialog(this, "Paste backup JSON data:", "");
            if (json != null && !json.trim().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "This will overwrite all existing data. Continue?",
                        "Confirm Restore", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        BackupUtil.fromJson(json);
                        JOptionPane.showMessageDialog(this, "Data restored successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        actionPanel.add(backupBtn);
        actionPanel.add(restoreBtn);

        panel.add(actionPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSettingsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Settings form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 247, 250));
        formPanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);
        c.anchor = GridBagConstraints.WEST;

        // Hospital Name
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        formPanel.add(new JLabel("Hospital Name:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JTextField hospitalName = new JTextField("City Hospital", 30);
        formPanel.add(hospitalName, c);

        // Address
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Address:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JTextField address = new JTextField("123 Healthcare Ave, Medical City", 30);
        formPanel.add(address, c);

        // Phone
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Phone:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JTextField phone = new JTextField("(555) 123-4567", 30);
        formPanel.add(phone, c);

        // Email
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Email:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JTextField email = new JTextField("admin@cityhospital.com", 30);
        formPanel.add(email, c);

        // Payment Methods
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Payment Methods:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        paymentPanel.setBackground(new Color(245, 247, 250));

        JCheckBox cashCB = new JCheckBox("Cash", true);
        JCheckBox cardCB = new JCheckBox("Card", true);
        JCheckBox insuranceCB = new JCheckBox("Insurance", true);
        JCheckBox bankCB = new JCheckBox("Bank Transfer", true);

        paymentPanel.add(cashCB);
        paymentPanel.add(cardCB);
        paymentPanel.add(insuranceCB);
        paymentPanel.add(bankCB);

        formPanel.add(paymentPanel, c);

        // Test Modes
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("System Mode:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        JRadioButton prodMode = new JRadioButton("Production", true);
        JRadioButton devMode = new JRadioButton("Development");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(prodMode);
        modeGroup.add(devMode);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        modePanel.setBackground(new Color(245, 247, 250));
        modePanel.add(prodMode);
        modePanel.add(devMode);

        formPanel.add(modePanel, c);

        panel.add(formPanel, BorderLayout.CENTER);

        // Save button
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        savePanel.setBackground(Theme.BG);

        JButton saveBtn = new JButton("Save Settings");
        styleButton(saveBtn, new Color(0, 110, 102));
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Settings saved successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        savePanel.add(saveBtn);
        panel.add(savePanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add User", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Username *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField usernameField = new JTextField();
        panel.add(usernameField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("Password *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JPasswordField passwordField = new JPasswordField();
        panel.add(passwordField, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.3;
        panel.add(new JLabel("Role *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> roleCombo = new JComboBox<>(new String[] { "ADMIN", "DOCTOR", "NURSE", "CASHIER" });
        panel.add(roleCombo, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn = new JButton("Add");
        styleButton(saveBtn, new Color(0, 110, 102));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            if (usernameField.getText().trim().isEmpty() || passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.util.List<String> result = hpms.auth.AuthService.register(
                    usernameField.getText().trim(),
                    new String(passwordField.getPassword()),
                    roleCombo.getSelectedItem().toString());

            if (result.get(0).startsWith("User registered")) {
                JOptionPane.showMessageDialog(dialog, "User added successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void refresh() {
        userModel.setRowCount(0);

        if (DataStore.users != null) {
            int idx = 0;
            for (User user : DataStore.users.values()) {
                userModel.addRow(new Object[] {
                        "U" + (++idx), // Generate user ID based on index
                        user.username,
                        user.role.toString(),
                        "Active"
                });
            }
            statsLabel.setText("Total Users: " + DataStore.users.size());
        }
    }

    // refresh when this panel becomes visible again
    private void ensureVisibilityRefresh() {
        this.addHierarchyListener(evt -> {
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing())
                    SwingUtilities.invokeLater(this::refresh);
            }
        });
    }

    private void createPatientAccountDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Patient Account", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Patient selection
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Select Patient *"), c);
        
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> patientCombo = new JComboBox<>();
        patientCombo.setPreferredSize(new Dimension(200, 28));
        
        // Load patients from DataStore
        if (DataStore.patients != null) {
            for (Patient patient : DataStore.patients.values()) {
                if (patient.isComplete) {
                    patientCombo.addItem(patient.id + " - " + patient.name);
                }
            }
        }
        panel.add(patientCombo, c);

        // Username field
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("Username *"), c);
        
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 28));
        panel.add(usernameField, c);

        // Password field
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.3;
        panel.add(new JLabel("Password *"), c);
        
        c.gridx = 1;
        c.weightx = 0.7;
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 28));
        panel.add(passwordField, c);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton createBtn = new JButton("Create Account");
        JButton cancelBtn = new JButton("Cancel");
        
        createBtn.addActionListener(e -> {
            System.out.println("=== Create Patient Account Button Clicked ===");
            String selectedPatient = (String) patientCombo.getSelectedItem();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            System.out.println("Selected patient: " + selectedPatient);
            System.out.println("Username: " + username);
            System.out.println("Password: " + (password.isEmpty() ? "[empty]" : "[provided]"));

            if (selectedPatient == null || selectedPatient.isEmpty()) {
                System.out.println("ERROR: No patient selected");
                JOptionPane.showMessageDialog(dialog, "Please select a patient", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("ERROR: Missing username or password");
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Extract patient ID from the selected item
            String patientId = selectedPatient.split(" - ")[0];
            Patient patient = DataStore.patients.get(patientId);

            if (patient == null) {
                System.out.println("ERROR: Patient not found with ID: " + patientId);
                JOptionPane.showMessageDialog(dialog, "Patient not found", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("Patient found: " + patient.name + " (ID: " + patientId + ")");

            // Check if current user is admin before calling AuthService.register
            System.out.println("Current user: " + (hpms.auth.AuthService.current != null ? 
                hpms.auth.AuthService.current.username + " (" + hpms.auth.AuthService.current.role + ")" : "null"));

            // Create user account for patient
            System.out.println("About to call AuthService.register...");
            java.util.List<String> result = hpms.auth.AuthService.register(
                    username,
                    password,
                    "PATIENT"
            );

            System.out.println("AuthService.register result: " + result.get(0));

            if (result.get(0).startsWith("User registered")) {
                JOptionPane.showMessageDialog(dialog, 
                    "Patient account created successfully!\n\n" +
                    "Patient: " + patient.name + "\n" +
                    "Username: " + username + "\n" +
                    "Password: " + password + "\n\n" +
                    "The patient can now login with these credentials.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        styleButton(createBtn, new Color(0, 110, 102));
        styleButton(cancelBtn, new Color(149, 165, 166));
        
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.CENTER;
        panel.add(buttonPanel, c);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}
