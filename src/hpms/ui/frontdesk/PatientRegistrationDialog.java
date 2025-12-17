package hpms.ui.frontdesk;

import hpms.auth.AuthSession;
import hpms.service.*;
import hpms.ui.components.Theme;
import hpms.util.Validators;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class PatientRegistrationDialog extends JDialog {
    
    private JTextField nameField;
    private JTextField ageField;
    private JTextField birthdayField;
    private JComboBox<String> genderCombo;
    private JTextField contactField;
    private JTextField emailField;
    private JTextArea addressArea;
    private JComboBox<String> patientTypeCombo;
    
    public PatientRegistrationDialog(Frame owner, AuthSession session) {
        super(owner, "New Patient Registration", true);
        initializeComponents();
        layoutComponents();
        setupDialog();
    }
    
    private void initializeComponents() {
        nameField = new JTextField(30);
        ageField = new JTextField(10);
        birthdayField = new JTextField(15);
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "LGBTQ+", "Other"});
        contactField = new JTextField(20);
        emailField = new JTextField(30);
        addressArea = new JTextArea(4, 30);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        patientTypeCombo = new JComboBox<>(new String[]{"INPATIENT", "EMERGENCY", "OUTPATIENT"});
        
        // Style components
        Theme.styleTextField(nameField);
        Theme.styleTextField(ageField);
        Theme.styleTextField(birthdayField);
        Theme.styleTextField(contactField);
        Theme.styleTextField(emailField);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBackground(Theme.SURFACE);
        addressArea.setForeground(Theme.FOREGROUND);
        addressArea.setBorder(nameField.getBorder());
        Theme.styleComboBox(genderCombo);
        Theme.styleComboBox(patientTypeCombo);
    }
    
    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Name *:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(nameField, gbc);
        
        // Age
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Age *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(ageField, gbc);
        
        // Birthday
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Birthday *:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(birthdayField, gbc);
        
        // Gender
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Gender *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(genderCombo, gbc);
        
        // Patient Type
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Patient Type *:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(patientTypeCombo, gbc);
        
        // Contact
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Contact *:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(contactField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(emailField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Address *:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(nameField.getBorder());
        mainPanel.add(addressScroll, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.setOpaque(false);
        
        JButton saveButton = new JButton("Register Patient");
        saveButton.setBackground(Theme.PRIMARY);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(this::onSave);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupDialog() {
        setLayout(new BorderLayout());
        setSize(600, 400);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }
    
    private void onSave(ActionEvent e) {
        String name = nameField.getText().trim();
        String age = ageField.getText().trim();
        String birthday = birthdayField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressArea.getText().trim();
        String patientType = (String) patientTypeCombo.getSelectedItem();
        
        // Validate required fields
        if (Validators.empty(name) || Validators.empty(age) || Validators.empty(birthday) || 
            Validators.empty(gender) || Validators.empty(contact) || Validators.empty(address) || Validators.empty(patientType)) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all required fields (*)",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate email if provided
        if (!Validators.empty(email) && !Validators.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid email address",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Register patient
        List<String> result = PatientService.add(name, age, birthday, gender, contact, address, patientType);
        
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Patient registered successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                String.join("\n", result),
                "Registration Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
