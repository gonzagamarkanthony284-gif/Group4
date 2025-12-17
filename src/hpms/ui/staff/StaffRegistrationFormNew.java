package hpms.ui.staff;

import hpms.auth.AuthService;
import hpms.model.Staff;
import hpms.model.StaffRole;
import hpms.service.StaffService;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Form for registering new staff members with auto-generated Staff ID
 * Validates license number and email format
 * Status defaults to Active
 */
public class StaffRegistrationFormNew extends JDialog {
    private JTextField nameField, phoneField, emailField;
    private JComboBox<String> roleCombo, deptCombo;
    private JComboBox<String> specCombo, nursingCombo;
    private JTextField licenseField, yearsExperienceField, yearsOfWorkField;
    private JPanel roleSpecificPanel;
    private JLabel roleSpecInfoLabel;
    
    // Clinic schedule components
    private JCheckBox[] dayCheckboxes = new JCheckBox[7];
    private JTextField[] startTimeFields = new JTextField[7];
    private JTextField[] endTimeFields = new JTextField[7];
    private String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public StaffRegistrationFormNew(Window owner) {
        super(owner, "Register New Staff", ModalityType.APPLICATION_MODAL);
        setSize(700, 800);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 250, 255));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 220, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel("Register New Staff Member");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 70, 140));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        int row = 0;

        // ===== BASIC INFORMATION SECTION =====
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel basicInfoLabel = new JLabel("Basic Information");
        basicInfoLabel.setFont(new Font("Arial", Font.BOLD, 13));
        basicInfoLabel.setForeground(new Color(47, 111, 237));
        formPanel.add(basicInfoLabel, gbc);
        gbc.gridwidth = 1;

        // Name
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Name *"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        nameField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(nameField, gbc);

        // Role
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Role *"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"DOCTOR", "NURSE", "CASHIER", "FRONT_DESK"});
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(roleCombo, gbc);
        roleCombo.addActionListener(e -> updateDepartmentList());

        // Phone
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Phone *"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(15);
        phoneField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(phoneField, gbc);

        // Email
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Email *"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(15);
        emailField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(emailField, gbc);

        // Department - Hide for CASHIER role
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel deptLabel = createLabel("Department *");
        formPanel.add(deptLabel, gbc);
        gbc.gridx = 1;
        deptCombo = new JComboBox<>();
        deptCombo.setBackground(Color.WHITE);
        deptCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(deptCombo, gbc);
        // Store reference to department label for hiding later
        deptLabel.putClientProperty("component", "departmentLabel");

        // License Number
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("License Number *"), gbc);
        gbc.gridx = 1;
        licenseField = new JTextField(15);
        licenseField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(licenseField, gbc);

        // Years of Experience
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Years Experience"), gbc);
        gbc.gridx = 1;
        yearsExperienceField = new JTextField(15);
        yearsExperienceField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(yearsExperienceField, gbc);

        // Years at Hospital
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Years at Hospital"), gbc);
        gbc.gridx = 1;
        yearsOfWorkField = new JTextField(15);
        yearsOfWorkField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(yearsOfWorkField, gbc);

        // ===== ROLE-SPECIFIC INFORMATION SECTION =====
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        roleSpecInfoLabel = new JLabel("Role-Specific Information");
        roleSpecInfoLabel.setFont(new Font("Arial", Font.BOLD, 13));
        roleSpecInfoLabel.setForeground(new Color(47, 111, 237));
        formPanel.add(roleSpecInfoLabel, gbc);
        gbc.gridwidth = 1;

        // Role-specific fields
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(createRoleSpecificPanel(), gbc);
        gbc.gridwidth = 1;

        // ===== CLINIC SCHEDULE SECTION =====
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel clinicSchedLabel = new JLabel("Clinic Schedule");
        clinicSchedLabel.setFont(new Font("Arial", Font.BOLD, 13));
        clinicSchedLabel.setForeground(new Color(47, 111, 237));
        formPanel.add(clinicSchedLabel, gbc);
        gbc.gridwidth = 1;

        // Clinic schedule table
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(createClinicSchedulePanel(), gbc);
        gbc.gridwidth = 1;

        // Buttons
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(200, 200, 210));
        cancelBtn.setForeground(new Color(40, 40, 40));
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Register Staff");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 11));
        saveBtn.setBackground(new Color(47, 111, 237));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> registerStaff());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        formPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
        updateDepartmentList();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(60, 80, 120));
        return label;
    }

    private JPanel createRoleSpecificPanel() {
        roleSpecificPanel = new JPanel(new GridBagLayout());
        roleSpecificPanel.setBackground(Color.WHITE);
        roleSpecificPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 230, 245), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // Doctor specialization - only visible for doctors
        gbc.gridy = 0;
        gbc.gridx = 0;
        JLabel docSpecLabel = new JLabel("Specialization (Doctor)");
        docSpecLabel.setFont(new Font("Arial", Font.BOLD, 10));
        docSpecLabel.setForeground(new Color(60, 80, 120));
        roleSpecificPanel.add(docSpecLabel, gbc);

        gbc.gridx = 1;
        specCombo = new JComboBox<>(new String[]{"Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Surgery"});
        specCombo.setBackground(Color.WHITE);
        specCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        roleSpecificPanel.add(specCombo, gbc);

        // Nursing field - hidden since nurses are not in dropdown
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel nurseLabel = new JLabel("Nursing Field");
        nurseLabel.setFont(new Font("Arial", Font.BOLD, 10));
        nurseLabel.setForeground(new Color(60, 80, 120));
        roleSpecificPanel.add(nurseLabel, gbc);

        gbc.gridx = 1;
        nursingCombo = new JComboBox<>(new String[]{"ER", "ICU", "Ward", "Pediatric", "OB Ward"});
        nursingCombo.setBackground(Color.WHITE);
        nursingCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        roleSpecificPanel.add(nursingCombo, gbc);

        // Initially hide all role-specific fields
        docSpecLabel.setVisible(false);
        specCombo.setVisible(false);
        nurseLabel.setVisible(false);
        nursingCombo.setVisible(false);

        return roleSpecificPanel;
    }

    private JPanel createClinicSchedulePanel() {
        JPanel schedulePanel = new JPanel(new BorderLayout(10, 10));
        schedulePanel.setBackground(Color.WHITE);
        schedulePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 230, 245), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Create table-like layout with GridBagLayout
        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header row
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.15;
        JLabel dayHeader = new JLabel("Day");
        dayHeader.setFont(new Font("Arial", Font.BOLD, 11));
        dayHeader.setForeground(new Color(60, 80, 120));
        tablePanel.add(dayHeader, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.15;
        JLabel activeHeader = new JLabel("Active");
        activeHeader.setFont(new Font("Arial", Font.BOLD, 11));
        activeHeader.setForeground(new Color(60, 80, 120));
        tablePanel.add(activeHeader, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.35;
        JLabel startHeader = new JLabel("Start Time");
        startHeader.setFont(new Font("Arial", Font.BOLD, 11));
        startHeader.setForeground(new Color(60, 80, 120));
        tablePanel.add(startHeader, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.35;
        JLabel endHeader = new JLabel("End Time");
        endHeader.setFont(new Font("Arial", Font.BOLD, 11));
        endHeader.setForeground(new Color(60, 80, 120));
        tablePanel.add(endHeader, gbc);

        // Data rows
        for (int i = 0; i < 7; i++) {
            gbc.gridy = i + 1;

            // Day name
            gbc.gridx = 0;
            JLabel dayLabel = new JLabel(daysOfWeek[i]);
            dayLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            tablePanel.add(dayLabel, gbc);

            // Checkbox
            gbc.gridx = 1;
            dayCheckboxes[i] = new JCheckBox();
            dayCheckboxes[i].setBackground(Color.WHITE);
            tablePanel.add(dayCheckboxes[i], gbc);

            // Start time
            gbc.gridx = 2;
            startTimeFields[i] = new JTextField("08:00", 8);
            startTimeFields[i].setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
            startTimeFields[i].setHorizontalAlignment(JTextField.CENTER);
            tablePanel.add(startTimeFields[i], gbc);

            // End time
            gbc.gridx = 3;
            endTimeFields[i] = new JTextField("17:00", 8);
            endTimeFields[i].setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
            endTimeFields[i].setHorizontalAlignment(JTextField.CENTER);
            tablePanel.add(endTimeFields[i], gbc);
        }

        schedulePanel.add(tablePanel, BorderLayout.CENTER);
        return schedulePanel;
    }

    private void updateDepartmentList() {
        String role = (String) roleCombo.getSelectedItem();
        String[] depts;

        if ("DOCTOR".equals(role)) {
            depts = new String[]{"Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Oncology", "ER"};
        } else if ("NURSE".equals(role)) {
            depts = new String[]{"Nursing", "ER", "Pediatrics", "Oncology", "Admin"};
        } else if ("CASHIER".equals(role)) {
            // Hide department field for cashiers and set automatic billing
            depts = new String[]{"Billing"}; // Default department for internal use
            deptCombo.setModel(new DefaultComboBoxModel<>(depts));
            deptCombo.setVisible(false);
            // Hide department label too
            JPanel parentPanel = (JPanel)deptCombo.getParent();
            for (Component comp : parentPanel.getComponents()) {
                if (comp instanceof JLabel && "departmentLabel".equals(((JComponent)comp).getClientProperty("component"))) {
                    comp.setVisible(false);
                    break;
                }
            }
        } else if ("FRONT_DESK".equals(role)) {
            depts = new String[]{"Reception", "Admissions", "Patient Services", "Admin"};
        } else {
            depts = new String[]{"Admin"};
        }

        // Update department combo if not cashier (cashier department is already set above)
        if (!"CASHIER".equals(role)) {
            deptCombo.setModel(new DefaultComboBoxModel<>(depts));
            deptCombo.setVisible(true);
            // Show department label
            JPanel parentPanel2 = (JPanel)deptCombo.getParent();
            for (Component comp : parentPanel2.getComponents()) {
                if (comp instanceof JLabel && "departmentLabel".equals(((JComponent)comp).getClientProperty("component"))) {
                    comp.setVisible(true);
                    break;
                }
            }
        }

        // Update role-specific field visibility
        if (roleSpecificPanel != null && roleSpecInfoLabel != null) {
            // Hide role-specific fields for FRONT_DESK and CASHIER
            if ("FRONT_DESK".equals(role) || "CASHIER".equals(role)) {
                roleSpecificPanel.setVisible(false);
                roleSpecInfoLabel.setVisible(false);
            } else if ("DOCTOR".equals(role)) {
                // Show specialization only for doctors
                roleSpecificPanel.setVisible(true);
                roleSpecInfoLabel.setVisible(true);
                roleSpecInfoLabel.setText("Doctor Information");
                // Show doctor specialization, hide nursing
                for (Component comp : roleSpecificPanel.getComponents()) {
                    if (comp instanceof JLabel) {
                        JLabel label = (JLabel) comp;
                        if (label.getText().contains("Specialization")) {
                            label.setVisible(true);
                        } else if (label.getText().contains("Nursing")) {
                            label.setVisible(false);
                        }
                    } else if (comp instanceof JComboBox) {
                        comp.setVisible(true);
                    }
                }
            } else if ("NURSE".equals(role)) {
                // Show nursing fields only for nurses
                roleSpecificPanel.setVisible(true);
                roleSpecInfoLabel.setVisible(true);
                roleSpecInfoLabel.setText("Nurse Information");
                // Show nursing field, hide doctor specialization
                for (Component comp : roleSpecificPanel.getComponents()) {
                    if (comp instanceof JLabel) {
                        JLabel label = (JLabel) comp;
                        if (label.getText().contains("Specialization")) {
                            label.setVisible(false);
                        } else if (label.getText().contains("Nursing")) {
                            label.setVisible(true);
                        }
                    } else if (comp instanceof JComboBox) {
                        comp.setVisible(true);
                    }
                }
            }
        }
    }

    private void registerStaff() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String license = licenseField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        
        // Get department - for cashiers, set automatically to "Billing"
        String dept;
        if ("CASHIER".equals(role)) {
            dept = "Billing"; // Automatic department for cashiers
        } else {
            dept = (String) deptCombo.getSelectedItem();
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate phone number (10 digits only)
        if (!hpms.util.Validators.isValidPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit phone number", "Phone Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Email format is invalid", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Use proper email validation
        if (!hpms.util.Validators.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address", "Email Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (license.isEmpty()) {
            // License is only required for doctors, not for cashiers/front desk
            if ("DOCTOR".equals(role)) {
                JOptionPane.showMessageDialog(this, "License Number is required for doctors", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Create Staff object without ID (StaffService will generate it)
        Staff staff = new Staff(null, name, StaffRole.valueOf(role), dept, LocalDateTime.now());
        staff.phone = phone;
        staff.email = email;
        staff.licenseNumber = license;
        staff.isAvailable = true;

        // Set years of experience if provided
        String yearsExpText = yearsExperienceField.getText().trim();
        if (!yearsExpText.isEmpty()) {
            try {
                staff.yearsExperience = Integer.parseInt(yearsExpText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Years Experience must be a number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Set years at hospital if provided
        String yearsWorkText = yearsOfWorkField.getText().trim();
        if (!yearsWorkText.isEmpty()) {
            try {
                staff.yearsOfWork = Integer.parseInt(yearsWorkText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Years at Hospital must be a number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Set clinic schedule from table
        for (int i = 0; i < 7; i++) {
            String startTime = startTimeFields[i].getText().trim();
            String endTime = endTimeFields[i].getText().trim();
            boolean isActive = dayCheckboxes[i].isSelected();
            
            // Validate time format (HH:mm)
            if (isActive && (!startTime.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$") || 
                !endTime.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$"))) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:mm (e.g., 08:00)", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            staff.clinicSchedule.put(daysOfWeek[i], new Staff.ScheduleEntry(isActive, startTime, endTime));
        }

        if ("DOCTOR".equals(role) && specCombo.getSelectedItem() != null) {
            staff.specialty = specCombo.getSelectedItem().toString();
        } else if ("NURSE".equals(role) && nursingCombo.getSelectedItem() != null) {
            
        }

        // Add to DataStore using StaffService
        java.util.List<String> staffResult = StaffService.add(
            staff.name,
            staff.role.toString(),
            staff.department,
            staff.specialty,
            staff.phone,
            staff.email,
            staff.licenseNumber,
            staff.qualifications,
            "Added via Quick Registration"
        );
        
        String actualStaffId = null;
        if (!staffResult.isEmpty() && staffResult.get(0).startsWith("Staff added")) {
            // Get the generated ID from StaffService
            actualStaffId = staffResult.get(0).substring("Staff added ".length()).trim();
        }

        if (actualStaffId != null) {
            String code = AuthService.generateRandomPasswordForUI();
            java.util.List<String> result = AuthService.register(actualStaffId, code, role);
            if (result != null && !result.isEmpty() && result.get(0).startsWith("User registered")) {
                AuthService.changePasswordNoOld(actualStaffId, code);
                JOptionPane.showMessageDialog(this,
                        "Staff registered successfully!\n\n" +
                                "Staff ID: " + actualStaffId + "\n" +
                                "Role: " + role + "\n" +
                                "6-Digit Login Code: " + code + "\n\n" +
                                "Please save this information securely.",
                        "Registration Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Staff registered but account creation failed.\n" +
                                "Staff ID: " + actualStaffId,
                        "Partial Success",
                        JOptionPane.WARNING_MESSAGE);
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to register staff. Please try again.",
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
