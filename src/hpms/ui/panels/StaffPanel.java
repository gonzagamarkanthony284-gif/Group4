package hpms.ui.panels;

import hpms.auth.AuthService;
import hpms.model.*;
import hpms.service.*;
import hpms.util.*;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;
import hpms.ui.components.DoctorFilterPanel;

import javax.swing.*;
import hpms.ui.staff.StaffRegistrationForm;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StaffPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private DefaultTableModel doctorModel, nurseModel, cashierModel;
    private JTable doctorTable, nurseTable, cashierTable;
    private JLabel statsLabel;
    private DoctorFilterPanel doctorFilterPanel;
    private JCheckBox showDeactivatedCheck;
    private JButton deactivateBtn;

    public StaffPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(SectionHeader.info("Staff Management", "Manage staff members organized by role"), BorderLayout.NORTH);

        // Stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.BEFORE_FIRST_LINE);

        // Create tabbed pane for each role
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(Theme.BG);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Doctor Tab
        tabbedPane.addTab("Doctors", createRolePanel("DOCTOR"));

        // Nurse Tab
        tabbedPane.addTab("Nurses", createRolePanel("NURSE"));

        // Cashier Tab
        tabbedPane.addTab("Cashiers", createRolePanel("CASHIER"));

        // Admin tab removed — admin management lives in Administration section

        add(tabbedPane, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);

        refresh();

        // Ensure panel refreshes when it becomes visible again
        this.addHierarchyListener(evt -> {
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing())
                    SwingUtilities.invokeLater(this::refresh);
            }
        });
    }

    private JPanel createRolePanel(String role) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);

        // Add filter panel for doctors only
        if ("DOCTOR".equals(role)) {
            doctorFilterPanel = new DoctorFilterPanel();
            doctorFilterPanel.setFilterChangeListener((dept, specialty) -> {
                applyDoctorFilters(dept, specialty);
            });
            panel.add(doctorFilterPanel, BorderLayout.NORTH);
        }

        // Create table model for this role
        String[] columnNames;
        if ("DOCTOR".equals(role)) {
            columnNames = new String[] { "Staff ID", "Name", "Department", "Expertise", "Details", "Status",
                    "Joined Date" };
        } else if ("NURSE".equals(role)) {
            // Nurses do not have departments
            columnNames = new String[] { "Staff ID", "Name", "Details", "Status", "Joined Date" };
        } else {
            columnNames = new String[] { "Staff ID", "Name", "Department", "Details", "Status", "Joined Date" };
        }

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        // Store model reference
        if ("DOCTOR".equals(role)) {
            doctorModel = model;
        } else if ("NURSE".equals(role)) {
            nurseModel = model;
        } else if ("CASHIER".equals(role)) {
            cashierModel = model;
        }

        // Create table
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);

        if ("DOCTOR".equals(role)) {
            // Doctors: ID, Name, Department, Expertise, Details, Status, Joined Date
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);
            table.getColumnModel().getColumn(4).setPreferredWidth(130);
            table.getColumnModel().getColumn(5).setPreferredWidth(80);
            table.getColumnModel().getColumn(6).setPreferredWidth(120);
        } else if ("NURSE".equals(role)) {
            // Nurses: ID, Name, Details, Status, Joined Date (no Department)
            table.getColumnModel().getColumn(2).setPreferredWidth(180);
            table.getColumnModel().getColumn(3).setPreferredWidth(80);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
        } else {
            // Cashiers/Others: ID, Name, Department, Details, Status, Joined Date
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(3).setPreferredWidth(150);
            table.getColumnModel().getColumn(4).setPreferredWidth(80);
            table.getColumnModel().getColumn(5).setPreferredWidth(120);
        }

        // Store table reference
        if ("DOCTOR".equals(role)) {
            doctorTable = table;
            table.getSelectionModel().addListSelectionListener(evt -> {
                updateDeactivateButton();
            });
        } else if ("NURSE".equals(role)) {
            nurseTable = table;
            table.getSelectionModel().addListSelectionListener(evt -> {
                updateDeactivateButton();
            });
        } else if ("CASHIER".equals(role)) {
            cashierTable = table;
            table.getSelectionModel().addListSelectionListener(evt -> {
                updateDeactivateButton();
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        statsLabel = new JLabel("Total Staff: 0 | Doctors: 0 | Nurses: 0 | Cashiers: 0");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statsLabel.setForeground(Theme.PRIMARY);

        showDeactivatedCheck = new JCheckBox("Show Deactivated");
        showDeactivatedCheck.setOpaque(false);
        showDeactivatedCheck.addActionListener(e -> refresh());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(showDeactivatedCheck);

        panel.add(statsLabel, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(8, 12, 12, 12));

        JButton addBtn = new JButton("Add Staff");
        styleButton(addBtn, new Color(0, 110, 102));

        JButton editBtn = new JButton("Edit");
        styleButton(editBtn, new Color(41, 128, 185));

        deactivateBtn = new JButton("Deactivate");
        styleButton(deactivateBtn, new Color(192, 57, 43));

        JButton regUserBtn = new JButton("Register User");
        styleButton(regUserBtn, new Color(155, 89, 182));

        JButton viewBtn = new JButton("View Details");
        styleButton(viewBtn, new Color(127, 140, 141));

        addBtn.addActionListener(e -> addStaffDialog());
        editBtn.addActionListener(e -> editStaff());
        deactivateBtn.addActionListener(e -> deleteStaff());
        regUserBtn.addActionListener(e -> registerUserDialog());
        viewBtn.addActionListener(e -> viewStaffDetails());

        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deactivateBtn);

        if (AuthService.current != null && AuthService.current.role == UserRole.ADMIN) {
            panel.add(regUserBtn);
        }

        panel.add(viewBtn);

        return panel;
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addStaffDialog() {
        StaffRegistrationForm form = new StaffRegistrationForm();
        form.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                refresh();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                refresh();
            }
        });
        form.setVisible(true);
    }

    private JTable getSelectedRoleTable() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == 0)
            return doctorTable;
        if (selectedIndex == 1)
            return nurseTable;
        if (selectedIndex == 2)
            return cashierTable;
        return null;
    }

    private void editStaff() {
        JTable table = getSelectedRoleTable();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "Please select a staff member", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to edit", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String staffId = model.getValueAt(row, 0).toString();
        Staff staff = DataStore.staff.get(staffId);

        if (staff == null)
            return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Staff", true);
        dialog.setSize(520, 520);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Name"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField nameField = new JTextField(staff.name);
        panel.add(nameField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        @SuppressWarnings("unchecked")
        final JComboBox<String>[] deptComboHolder = new JComboBox[1];
        deptComboHolder[0] = null;

        // Department field only for non-nurse roles
        if (staff.role != StaffRole.NURSE) {
            panel.add(new JLabel("Department"), c);
            c.gridx = 1;
            c.weightx = 0.7;
            deptComboHolder[0] = new JComboBox<>(DataStore.departments.toArray(new String[0]));
            deptComboHolder[0].setSelectedItem(staff.department);
            panel.add(deptComboHolder[0], c);
            c.gridy++;
        }

        c.gridx = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Specialization"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField specField = new JTextField(staff.specialty == null ? "" : staff.specialty);
        // Specialization is permanent for doctors and cannot be edited
        if (staff.role == StaffRole.DOCTOR) {
            specField.setEditable(false);
            specField.setBackground(new Color(240, 240, 240));
            specField.setToolTipText("Doctor specialization cannot be changed after account creation");
        }
        panel.add(specField, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.3;
        panel.add(new JLabel("Phone"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField phoneField = new JTextField(staff.phone == null ? "" : staff.phone);
        panel.add(phoneField, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.3;
        panel.add(new JLabel("Email"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField emailField = new JTextField(staff.email == null ? "" : staff.email);
        panel.add(emailField, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.3;
        panel.add(new JLabel("License #"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField licenseField = new JTextField(staff.licenseNumber == null ? "" : staff.licenseNumber);
        panel.add(licenseField, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.3;
        panel.add(new JLabel("Qualifications"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JTextArea qualsArea = new JTextArea(3, 30);
        qualsArea.setLineWrap(true);
        qualsArea.setWrapStyleWord(true);
        qualsArea.setText(staff.qualifications == null ? "" : staff.qualifications);
        panel.add(new JScrollPane(qualsArea), c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn = new JButton("Update");
        styleButton(saveBtn, new Color(41, 128, 185));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            String nm = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String lic = licenseField.getText().trim();

            if (nm.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name cannot be empty", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                JOptionPane.showMessageDialog(dialog, "Please provide a valid email address.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!phone.isEmpty() && !phone.matches("^[0-9+()\\-\\s]{7,25}$")) {
                JOptionPane.showMessageDialog(dialog, "Please provide a valid phone number.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (Staff other : DataStore.staff.values()) {
                if (other.id.equals(staff.id))
                    continue;
                if (!email.isEmpty() && email.equalsIgnoreCase(other.email)) {
                    JOptionPane.showMessageDialog(dialog, "Another staff member already uses that email.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!lic.isEmpty() && lic.equalsIgnoreCase(other.licenseNumber)) {
                    JOptionPane.showMessageDialog(dialog, "Another staff member already uses that license number.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            staff.name = nm;
            // Only update department for non-nurse roles
            if (staff.role != StaffRole.NURSE && deptComboHolder[0] != null) {
                staff.department = deptComboHolder[0].getSelectedItem().toString();
            }
            // Only update specialty for non-doctor roles (doctor specialization is
            // permanent)
            if (staff.role != StaffRole.DOCTOR) {
                staff.specialty = specField.getText().trim();
            }
            staff.phone = phoneField.getText().trim();
            staff.email = emailField.getText().trim();
            staff.licenseNumber = licenseField.getText().trim();
            staff.qualifications = qualsArea.getText().trim();
            JOptionPane.showMessageDialog(dialog, "Staff updated successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            refresh();
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteStaff() {
        JTable table = getSelectedRoleTable();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "Please select a staff member", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a staff member", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String staffId = model.getValueAt(row, 0).toString();
        Staff staff = DataStore.staff.get(staffId);
        if (staff == null) {
            JOptionPane.showMessageDialog(this, "Staff not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (staff.isActive) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deactivate this account?\nThe record will be preserved and hidden from active lists.",
                    "Confirm Deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                java.util.List<String> result = StaffService.deactivate(staffId);
                JOptionPane.showMessageDialog(this, result.get(0), result.get(0).startsWith("Error") ? "Error"
                        : "Success",
                        result.get(0).startsWith("Error") ? JOptionPane.ERROR_MESSAGE
                                : JOptionPane.INFORMATION_MESSAGE);
                refresh();
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, "Reactivate this account?", "Confirm Reactivation",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                java.util.List<String> result = StaffService.reactivate(staffId);
                JOptionPane.showMessageDialog(this, result.get(0), result.get(0).startsWith("Error") ? "Error"
                        : "Success",
                        result.get(0).startsWith("Error") ? JOptionPane.ERROR_MESSAGE
                                : JOptionPane.INFORMATION_MESSAGE);
                refresh();
            }
        }
    }

    private void registerUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register User Account", true);
        dialog.setSize(450, 300);
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
        JComboBox<String> roleCombo = new JComboBox<>(new String[] { "DOCTOR", "NURSE", "CASHIER" });
        panel.add(roleCombo, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn = new JButton("Register");
        styleButton(saveBtn, new Color(155, 89, 182));
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

            java.util.List<String> result = AuthService.register(
                    usernameField.getText().trim(),
                    new String(passwordField.getPassword()),
                    roleCombo.getSelectedItem().toString());

            if (result.get(0).startsWith("User registered")) {
                JOptionPane.showMessageDialog(dialog, "User registered successfully!", "Success",
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

    private void viewStaffDetails() {
        JTable table = getSelectedRoleTable();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "Please select a staff member", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a staff member", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String staffId = model.getValueAt(row, 0).toString();
        Staff staff = DataStore.staff.get(staffId);

        if (staff == null)
            return;

        // Create detailed information panel
        JPanel detailPanel = new JPanel();

        if (staff.role == StaffRole.DOCTOR) {
            // Enhanced detail view for doctors with photo
            detailPanel = createDoctorDetailPanel(staff);
        } else {
            // Standard detail view for other staff
            String details;
            if (staff.role == StaffRole.NURSE) {
                // Nurses do not have departments
                details = String.format(
                        "Staff ID: %s\n\n" +
                                "Name: %s\n" +
                                "Role: %s\n" +
                                "Phone: %s\n" +
                                "Email: %s\n" +
                                "License #: %s\n" +
                                "Status: Active",
                        staff.id, staff.name, staff.role,
                        staff.phone == null ? "" : staff.phone,
                        staff.email == null ? "" : staff.email,
                        staff.licenseNumber == null ? "" : staff.licenseNumber);
            } else {
                details = String.format(
                        "Staff ID: %s\n\n" +
                                "Name: %s\n" +
                                "Role: %s\n" +
                                "Department: %s\n" +
                                "Phone: %s\n" +
                                "Email: %s\n" +
                                "License #: %s\n" +
                                "Status: Active",
                        staff.id, staff.name, staff.role, staff.department,
                        staff.phone == null ? "" : staff.phone,
                        staff.email == null ? "" : staff.email,
                        staff.licenseNumber == null ? "" : staff.licenseNumber);
            }

            detailPanel.setLayout(new BorderLayout());
            detailPanel.add(new JLabel(details), BorderLayout.CENTER);
        }

        JOptionPane.showMessageDialog(this, detailPanel, "Staff Details - " + staffId, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Create enhanced detail panel for doctors with photo, expertise, and
     * credentials
     */
    private JPanel createDoctorDetailPanel(Staff doctor) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        // Left side: Photo
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBackground(Color.WHITE);

        JLabel photoLabel = new JLabel();
        if (doctor.photoPath != null && !doctor.photoPath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(doctor.photoPath);
                if (icon.getIconWidth() > 0) {
                    // Scale image to 150x150
                    Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    photoLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    photoLabel.setText("No Photo");
                    photoLabel.setHorizontalAlignment(JLabel.CENTER);
                }
            } catch (Exception e) {
                photoLabel.setText("Photo Error");
                photoLabel.setHorizontalAlignment(JLabel.CENTER);
            }
        } else {
            photoLabel.setText("No Photo Available");
            photoLabel.setHorizontalAlignment(JLabel.CENTER);
            photoLabel.setPreferredSize(new Dimension(150, 150));
            photoLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            photoLabel.setBackground(new Color(240, 240, 240));
            photoLabel.setOpaque(true);
        }
        photoPanel.add(photoLabel, BorderLayout.CENTER);
        photoPanel.setPreferredSize(new Dimension(170, 170));
        panel.add(photoPanel, BorderLayout.WEST);

        // Right side: Information
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        // Name and title
        JLabel nameLabel = new JLabel(doctor.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        infoPanel.add(nameLabel);

        if (doctor.specialty != null && !doctor.specialty.isEmpty()) {
            JLabel specialtyLabel = new JLabel(doctor.specialty);
            specialtyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            specialtyLabel.setForeground(new Color(100, 100, 100));
            infoPanel.add(specialtyLabel);
        }

        infoPanel.add(Box.createVerticalStrut(10));

        // Details
        String details = String.format(
                "Staff ID: %s\n" +
                        "Department: %s\n" +
                        "License #: %s\n" +
                        "Phone: %s\n" +
                        "Email: %s",
                doctor.id,
                doctor.department == null ? "" : doctor.department,
                doctor.licenseNumber == null ? "N/A" : doctor.licenseNumber,
                doctor.phone == null ? "" : doctor.phone,
                doctor.email == null ? "" : doctor.email);

        JLabel detailsLabel = new JLabel("<html>" + details.replaceAll("\n", "<br>") + "</html>");
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(detailsLabel);

        // Credentials section
        if ((doctor.qualifications != null && !doctor.qualifications.isEmpty()) ||
                (doctor.certifications != null && !doctor.certifications.isEmpty())) {
            infoPanel.add(Box.createVerticalStrut(10));
            JLabel credentialsHeaderLabel = new JLabel("Credentials:");
            credentialsHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            infoPanel.add(credentialsHeaderLabel);

            if (doctor.qualifications != null && !doctor.qualifications.isEmpty()) {
                JLabel qualLabel = new JLabel("• " + doctor.qualifications);
                qualLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                infoPanel.add(qualLabel);
            }

            if (doctor.certifications != null && !doctor.certifications.isEmpty()) {
                JLabel certLabel = new JLabel("• " + doctor.certifications);
                certLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                infoPanel.add(certLabel);
            }
        }

        infoPanel.add(Box.createVerticalGlue());
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    public void refresh() {
        // Check and clear expired clinic schedules before displaying staff
        StaffService.checkAndClearExpiredSchedules();

        boolean showDeactivated = showDeactivatedCheck != null && showDeactivatedCheck.isSelected();

        doctorModel.setRowCount(0);
        nurseModel.setRowCount(0);
        cashierModel.setRowCount(0);

        int doctorCount = 0;
        int nurseCount = 0;
        int cashierCount = 0;
        int deactivatedCount = 0;

        for (Staff staff : DataStore.staff.values()) {
            // Skip deactivated unless checkbox is checked
            if (!staff.isActive) {
                deactivatedCount++;
                if (!showDeactivated)
                    continue;
            }

            String details = getStaffDetails(staff);

            if (staff.role == StaffRole.DOCTOR) {
                // Doctors have 7 columns: ID, Name, Department, Expertise, Details, Status,
                // Joined Date
                String status = staff.isActive ? "Active" : "Deactivated";
                if (staff.isActive && staff.isScheduleExpired()) {
                    status = "Schedule Expired";
                } else if (staff.isActive && staff.scheduleEndDate != null) {
                    // Check if schedule expires within 7 days
                    long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(
                            java.time.LocalDateTime.now(),
                            staff.scheduleEndDate);
                    if (daysUntilExpiry <= 7 && daysUntilExpiry > 0) {
                        status = "Expiring Soon (" + daysUntilExpiry + "d)";
                    }
                }
                Object[] row = new Object[] {
                        staff.id,
                        staff.name,
                        staff.department,
                        staff.specialty == null ? "" : staff.specialty, // Expertise column
                        details,
                        status,
                        staff.createdAt == null ? "" : staff.createdAt.toLocalDate().toString()
                };
                doctorModel.addRow(row);
                doctorCount++;
            } else if (staff.role == StaffRole.NURSE) {
                // Nurses have 5 columns: ID, Name, Details, Status, Joined Date (no Department)
                String status = staff.isActive ? "Active" : "Deactivated";
                if (staff.isActive && staff.isScheduleExpired()) {
                    status = "Schedule Expired";
                } else if (staff.isActive && staff.scheduleEndDate != null) {
                    // Check if schedule expires within 7 days
                    long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(
                            java.time.LocalDateTime.now(),
                            staff.scheduleEndDate);
                    if (daysUntilExpiry <= 7 && daysUntilExpiry > 0) {
                        status = "Expiring Soon (" + daysUntilExpiry + "d)";
                    }
                }
                Object[] row = new Object[] {
                        staff.id,
                        staff.name,
                        details,
                        status,
                        staff.createdAt == null ? "" : staff.createdAt.toLocalDate().toString()
                };
                nurseModel.addRow(row);
                nurseCount++;
            } else if (staff.role == StaffRole.CASHIER) {
                // Cashiers have 6 columns: ID, Name, Department, Details, Status, Joined Date
                String status = staff.isActive ? "Active" : "Deactivated";
                if (staff.isActive && staff.isScheduleExpired()) {
                    status = "Schedule Expired";
                } else if (staff.isActive && staff.scheduleEndDate != null) {
                    // Check if schedule expires within 7 days
                    long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(
                            java.time.LocalDateTime.now(),
                            staff.scheduleEndDate);
                    if (daysUntilExpiry <= 7 && daysUntilExpiry > 0) {
                        status = "Expiring Soon (" + daysUntilExpiry + "d)";
                    }
                }
                Object[] row = new Object[] {
                        staff.id,
                        staff.name,
                        staff.department,
                        details,
                        status,
                        staff.createdAt == null ? "" : staff.createdAt.toLocalDate().toString()
                };
                cashierModel.addRow(row);
                cashierCount++;
            }
        }

        int totalActive = doctorCount + nurseCount + cashierCount;
        String stats = String.format("Active Staff: %d | Doctors: %d | Nurses: %d | Cashiers: %d",
                totalActive, doctorCount, nurseCount, cashierCount);
        if (deactivatedCount > 0 && !showDeactivated)
            stats += " | Deactivated: " + deactivatedCount + " (hidden)";
        statsLabel.setText(stats);

        // Update button label based on selection
        updateDeactivateButton();
    }

    private void updateDeactivateButton() {
        if (deactivateBtn == null)
            return;
        JTable table = getSelectedRoleTable();
        if (table == null || table.getSelectedRow() < 0) {
            deactivateBtn.setText("Deactivate");
            return;
        }
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String staffId = model.getValueAt(table.getSelectedRow(), 0).toString();
        Staff staff = DataStore.staff.get(staffId);
        if (staff != null && !staff.isActive)
            deactivateBtn.setText("Reactivate");
        else
            deactivateBtn.setText("Deactivate");
    }

    private String getStaffDetails(Staff staff) {
        if (staff.role == StaffRole.DOCTOR) {
            return (staff.specialty == null ? "" : staff.specialty) + " | License: "
                    + (staff.licenseNumber == null ? "N/A" : staff.licenseNumber);
        } else if (staff.role == StaffRole.NURSE) {
            return "License: " + (staff.licenseNumber == null ? "N/A" : staff.licenseNumber);
        } else if (staff.role == StaffRole.CASHIER) {
            return "Billing & Payment Processing";
        } else {
            return "Administrator";
        }
    }

    /**
     * Apply filters to doctor list based on selected department and specialty
     * 
     * @param department Selected department or null for all
     * @param specialty  Selected specialty or null for all
     */
    private void applyDoctorFilters(String department, String specialty) {
        doctorModel.setRowCount(0);

        for (Staff staff : DataStore.staff.values()) {
            if (staff.role != StaffRole.DOCTOR)
                continue;

            // Apply department filter
            if (department != null && (staff.department == null || !staff.department.equals(department))) {
                continue;
            }

            // Apply specialty filter
            if (specialty != null && (staff.specialty == null || !staff.specialty.equals(specialty))) {
                continue;
            }

            // If passed all filters, add to table
            String details = getStaffDetails(staff);
            Object[] row = new Object[] {
                    staff.id,
                    staff.name,
                    staff.department,
                    staff.specialty == null ? "" : staff.specialty, // Expertise column
                    details,
                    "Active",
                    staff.createdAt == null ? "" : staff.createdAt.toLocalDate().toString()
            };
            doctorModel.addRow(row);
        }
    }
}
