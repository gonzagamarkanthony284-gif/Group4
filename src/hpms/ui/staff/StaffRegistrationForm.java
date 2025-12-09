package hpms.ui.staff;

import javax.swing.*;
import hpms.ui.components.Theme;
import hpms.service.StaffService;
import hpms.auth.AuthService;
import java.awt.*;

public class StaffRegistrationForm extends JFrame {
    private JTextField nameField, phoneField, emailField, yearsExperienceField;
    private JComboBox<String> roleCombo, deptCombo, statusCombo;
    private JComboBox<String> specCombo, nursingCombo;
    private JTextField subSpec, licenseDoctor, yearsPracticeDoctor, licenseNurse, certsField, yearsExpNurse;
    private JPanel doctorPanel, nursePanel, cashierPanel, adminPanel;
    private JLabel empId;

    public StaffRegistrationForm() {
        setTitle("Staff Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1400, 1700);
        setLocationRelativeTo(null);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);

        // Header with title and action buttons
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BACKGROUND);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(15, 15, 10, 15)));
        JLabel titleLabel = new JLabel("Register New Staff Member");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Theme.FOREGROUND);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Add action buttons to header right side
        JPanel headerButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtonPanel.setBackground(Theme.BACKGROUND);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.setBackground(new Color(155, 155, 155));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Save Staff");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 12));
        saveBtn.setBackground(Theme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveStaff());

        headerButtonPanel.add(cancelBtn);
        headerButtonPanel.add(saveBtn);
        headerPanel.add(headerButtonPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Scrollable content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Theme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Basic Information Card
        contentPanel.add(createBasicInfoCard(), gbc);

        // Role-specific Card
        gbc.gridy++;
        contentPanel.add(createRoleSpecificCard(), gbc);

        // Fill remaining space
        gbc.gridy++;
        gbc.weighty = 1.0;
        contentPanel.add(new JPanel(), gbc);
    }

    private JPanel createBasicInfoCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        JLabel sectionLabel = new JLabel("Basic Information");
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sectionLabel.setForeground(Theme.PRIMARY);
        card.add(sectionLabel, gbc);
        gbc.gridwidth = 1;

        // Name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.1;
        card.add(new JLabel("Name *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        nameField = new JTextField();
        Theme.styleTextField(nameField);
        nameField.setMinimumSize(new Dimension(300, 28));
        card.add(nameField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.1;
        card.add(new JLabel("Role *"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.35;
        // NOTE: ADMIN role is excluded from this registration form
        // Admin accounts can ONLY be created through the AdminGUI by existing admins
        // This ensures proper access control and prevents unauthorized admin creation
        roleCombo = new JComboBox<>(new String[] { "DOCTOR", "NURSE", "CASHIER" });
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        roleCombo.setPreferredSize(new Dimension(180, 28));
        card.add(roleCombo, gbc);

        // Phone & Email
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.15;
        card.add(new JLabel("Phone *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        phoneField = new JTextField();
        Theme.styleTextField(phoneField);
        phoneField.setMinimumSize(new Dimension(300, 28));
        card.add(phoneField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.15;
        card.add(new JLabel("Email *"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        emailField = new JTextField();
        Theme.styleTextField(emailField);
        emailField.setMinimumSize(new Dimension(300, 28));
        card.add(emailField, gbc);

        // Department & Status
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.15;
        card.add(new JLabel("Department *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        deptCombo = new JComboBox<>(new String[] { "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Oncology",
                "ER", "Admin", "Nursing", "Billing" });
        deptCombo.setBackground(Color.WHITE);
        deptCombo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        deptCombo.setPreferredSize(new Dimension(200, 28));
        card.add(deptCombo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.15;
        card.add(new JLabel("Status"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.35;
        statusCombo = new JComboBox<>(new String[] { "Active", "On Leave", "Resigned" });
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        statusCombo.setPreferredSize(new Dimension(180, 28));
        card.add(statusCombo, gbc);

        // Years of Experience
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.15;
        card.add(new JLabel("Years of Experience"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.85;
        yearsExperienceField = new JTextField();
        Theme.styleTextField(yearsExperienceField);
        yearsExperienceField.setPreferredSize(new Dimension(600, 28));
        card.add(yearsExperienceField, gbc);
        gbc.gridwidth = 1;

        return card;
    }

    private JPanel createRoleSpecificCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel sectionLabel = new JLabel("Role-Specific Information");
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sectionLabel.setForeground(Theme.PRIMARY);
        card.add(sectionLabel, BorderLayout.NORTH);

        // Create a container for role panels
        JPanel panelContainer = new JPanel(new GridBagLayout());
        panelContainer.setBackground(Color.WHITE);
        panelContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Doctor Panel
        doctorPanel = createDoctorPanel();
        panelContainer.add(doctorPanel, gbc);

        // Nurse Panel
        gbc.gridy++;
        nursePanel = createNursePanel();
        panelContainer.add(nursePanel, gbc);

        // Cashier Panel
        gbc.gridy++;
        cashierPanel = createCashierPanel();
        panelContainer.add(cashierPanel, gbc);

        // Admin Panel
        gbc.gridy++;
        adminPanel = createAdminPanel();
        panelContainer.add(adminPanel, gbc);

        doctorPanel.setVisible(true);
        nursePanel.setVisible(false);
        cashierPanel.setVisible(false);
        adminPanel.setVisible(false);

        card.add(panelContainer, BorderLayout.CENTER);

        // Role change listener
        roleCombo.addActionListener(evt -> {
            String role = (String) roleCombo.getSelectedItem();
            doctorPanel.setVisible("DOCTOR".equals(role));
            nursePanel.setVisible("NURSE".equals(role));
            cashierPanel.setVisible("CASHIER".equals(role));
            adminPanel.setVisible("ADMIN".equals(role));

            if ("CASHIER".equals(role)) {
                empId.setText("EMP-" + (System.currentTimeMillis() % 100000));
            }

            // Update department list - nurses do not have departments
            if ("NURSE".equals(role)) {
                deptCombo.setEnabled(false);
                deptCombo.setModel(new DefaultComboBoxModel<>(new String[] { "N/A" }));
            } else {
                deptCombo.setEnabled(true);
                String[] depts;
                if ("DOCTOR".equals(role)) {
                    depts = new String[] { "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Oncology", "ER" };
                } else if ("CASHIER".equals(role)) {
                    depts = new String[] { "Billing", "Admin" };
                } else {
                    depts = new String[] { "Admin", "Billing" };
                }
                deptCombo.setModel(new DefaultComboBoxModel<>(depts));
            }
            card.revalidate();
            card.repaint();
        });

        return card;
    }

    private JPanel createDoctorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Specialization & Years of Practice
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Specialization *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        specCombo = new JComboBox<>(new String[] { "Cardiology", "Pediatrics", "General Medicine", "Surgery",
                "Orthopedic", "ENT", "Dermatology", "OB-Gyne" });
        specCombo.setBackground(Color.WHITE);
        specCombo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        specCombo.setMinimumSize(new Dimension(300, 28));
        panel.add(specCombo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Years of Practice *"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        yearsPracticeDoctor = new JTextField();
        Theme.styleTextField(yearsPracticeDoctor);
        yearsPracticeDoctor.setMinimumSize(new Dimension(200, 28));
        panel.add(yearsPracticeDoctor, gbc);

        // Sub-specialty & License
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Sub-Specialty"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        subSpec = new JTextField();
        Theme.styleTextField(subSpec);
        subSpec.setMinimumSize(new Dimension(300, 28));
        panel.add(subSpec, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        panel.add(new JLabel("License Number *"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        licenseDoctor = new JTextField();
        Theme.styleTextField(licenseDoctor);
        licenseDoctor.setMinimumSize(new Dimension(200, 28));
        panel.add(licenseDoctor, gbc);

        return panel;
    }

    private JPanel createNursePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nursing Field & Years Experience
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Nursing Field *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        nursingCombo = new JComboBox<>(new String[] { "ER", "ICU", "Ward", "Pediatric", "OB Ward", "General Nursing" });
        nursingCombo.setBackground(Color.WHITE);
        nursingCombo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        nursingCombo.setPreferredSize(new Dimension(220, 28));
        panel.add(nursingCombo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Years Experience *"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        yearsExpNurse = new JTextField();
        Theme.styleTextField(yearsExpNurse);
        yearsExpNurse.setPreferredSize(new Dimension(100, 28));
        panel.add(yearsExpNurse, gbc);

        // License & Certifications
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        panel.add(new JLabel("License Number *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        licenseNurse = new JTextField();
        Theme.styleTextField(licenseNurse);
        licenseNurse.setPreferredSize(new Dimension(220, 28));
        panel.add(licenseNurse, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Certifications"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        certsField = new JTextField();
        Theme.styleTextField(certsField);
        certsField.setPreferredSize(new Dimension(100, 28));
        panel.add(certsField, gbc);

        return panel;
    }

    private JPanel createCashierPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Employee ID"), gbc);
        gbc.gridx = 1;
        empId = new JLabel("(auto generated)");
        empId.setFont(empId.getFont().deriveFont(Font.ITALIC));
        panel.add(empId, gbc);

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(new JLabel("Admin: No additional medical fields required"), gbc);

        return panel;
    }

    private void saveStaff() {
        String role = (String) roleCombo.getSelectedItem();

        // Defensive check: ADMIN role should never be allowed in this form
        if ("ADMIN".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "Admin accounts cannot be created through this form. " +
                    "Use the Administration panel to create admin accounts.", "Access Denied",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String dept = (String) deptCombo.getSelectedItem();

        // Basic validation
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!phone.matches("^[0-9+()\\-\\s]{7,25}$")) {
            JOptionPane.showMessageDialog(this, "Phone format invalid", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Email format invalid", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Role-specific validation
        if ("DOCTOR".equals(role)) {
            if (specCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Specialization is required", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (licenseDoctor.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "License Number is required", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (yearsPracticeDoctor.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Years of Practice is required", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if ("NURSE".equals(role)) {
            if (nursingCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Nursing Field is required", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (licenseNurse.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "License Number is required", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (yearsExpNurse.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Years Experience is required", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Save to service
        java.util.List<String> result = StaffService.add(
                name,
                role,
                dept,
                specCombo != null && specCombo.getSelectedItem() != null ? specCombo.getSelectedItem().toString() : "",
                phone,
                email,
                licenseDoctor != null ? licenseDoctor.getText() : "",
                "",
                "");
        if (result != null && !result.isEmpty() && result.get(0).toLowerCase().contains("added")) {
            // Extract staff ID from response
            String staffId = result.get(0).replaceAll(".*\\s", ""); // Get the ID from "Staff added SXXX"

            // Automatically create a staff account with a generated password
            String generatedPassword = AuthService.generateRandomPasswordForUI();
            java.util.List<String> accountResult = AuthService.register(staffId, generatedPassword, role);

            if (accountResult != null && !accountResult.isEmpty()
                    && accountResult.get(0).startsWith("User registered")) {
                AuthService.changePasswordNoOld(staffId, generatedPassword);
                JOptionPane.showMessageDialog(this,
                        "Staff registered successfully!\n\n" +
                                "Staff ID: " + staffId + "\n" +
                                "Login Password: " + generatedPassword + "\n\n" +
                                "Please save this password securely.",
                        "Registration Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Staff registered but account creation failed.\n" +
                                "Staff ID: " + staffId,
                        "Partial Success",
                        JOptionPane.WARNING_MESSAGE);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    result != null && !result.isEmpty() ? result.get(0) : "Registration failed", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StaffRegistrationForm().setVisible(true));
    }
}
