package hpms.ui.doctor;

import hpms.auth.AuthSession;
import hpms.model.*;
import hpms.util.DataStore;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.*;

/**
 * Doctor Dashboard - displays today's appointments, pending requests, and assigned patients
 */
public class DoctorDashboardPanel extends JPanel {
    private AuthSession session;
    private JTable appointmentsTable;
    private JTable upcomingTable;
    private JTable requestsTable;
    private JTable patientsTable;

    public DoctorDashboardPanel(AuthSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Doctor Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(31, 41, 55));
        JLabel subtitle = new JLabel("Welcome, " + session.fullName);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(new Color(107, 114, 128));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Tabbed interface
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", createDashboardContent());
        tabs.addTab("My Profile", createProfileTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createDashboardContent() {
        // Content - 4 sections
        JPanel content = new JPanel(new GridLayout(4, 1, 12, 12));
        content.setOpaque(false);

        content.add(createTodayAppointmentsSection());
        content.add(createUpcomingAppointmentsSection());
        content.add(createPendingRequestsSection());
        content.add(createAssignedPatientsSection());

        return content;
    }

    private JPanel createTodayAppointmentsSection() {
        JPanel section = new JPanel(new BorderLayout(8, 8));
        section.setBackground(new Color(248, 249, 250));
        section.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Today's Appointments");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(new Color(47, 111, 237));
        section.add(title, BorderLayout.NORTH);

        // Table
        appointmentsTable = new JTable();
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.setRowHeight(28);
        refreshAppointmentsTable();

        JScrollPane scroll = new JScrollPane(appointmentsTable);
        scroll.setBorder(null);
        section.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton viewAttachmentsBtn = new JButton("View Attachments");
        viewAttachmentsBtn.setBackground(new Color(59, 130, 246));
        viewAttachmentsBtn.setForeground(Color.WHITE);
        viewAttachmentsBtn.setFocusPainted(false);
        viewAttachmentsBtn.addActionListener(e -> viewPatientAttachments());
        
        JButton confirmBtn = new JButton("Confirm Appointment");
        confirmBtn.setBackground(new Color(34, 197, 94));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.addActionListener(e -> confirmSelectedAppointment());
        
        buttonPanel.add(viewAttachmentsBtn);
        buttonPanel.add(confirmBtn);
        section.add(buttonPanel, BorderLayout.SOUTH);

        return section;
    }

    private JPanel createPendingRequestsSection() {
        JPanel section = new JPanel(new BorderLayout(8, 8));
        section.setBackground(new Color(248, 249, 250));
        section.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Pending Appointment Requests");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(new Color(47, 111, 237));
        section.add(title, BorderLayout.NORTH);

        requestsTable = new JTable();
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.setRowHeight(28);
        refreshRequestsTable();

        JScrollPane scroll = new JScrollPane(requestsTable);
        scroll.setBorder(null);
        section.add(scroll, BorderLayout.CENTER);

        return section;
    }

    private JPanel createAssignedPatientsSection() {
        JPanel section = new JPanel(new BorderLayout(8, 8));
        section.setBackground(new Color(248, 249, 250));
        section.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Assigned Patients");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(new Color(47, 111, 237));
        section.add(title, BorderLayout.NORTH);

        patientsTable = new JTable();
        patientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientsTable.setRowHeight(28);
        System.out.println("DEBUG: Calling refreshPatientsTable() from createAssignedPatientsSection");
        refreshPatientsTable();

        JScrollPane scroll = new JScrollPane(patientsTable);
        scroll.setBorder(null);
        section.add(scroll, BorderLayout.CENTER);

        return section;
    }

    private void refreshAppointmentsTable() {
        LocalDate today = LocalDate.now();
        java.util.List<Appointment> todayAppts = new ArrayList<>();
        
        for (Appointment a : DataStore.appointments.values()) {
            if (session.userId.equals(a.staffId) && a.dateTime.toLocalDate().equals(today)) {
                todayAppts.add(a);
            }
        }

        todayAppts.sort((a, b) -> a.dateTime.compareTo(b.dateTime));

        String[] columns = {"Time", "Patient ID", "Patient Name"};
        Object[][] data = new Object[todayAppts.size()][3];

        for (int i = 0; i < todayAppts.size(); i++) {
            Appointment a = todayAppts.get(i);
            Patient p = DataStore.patients.get(a.patientId);
            data[i][0] = a.dateTime.toLocalTime().toString();
            data[i][1] = a.patientId;
            data[i][2] = p != null ? p.name : "Unknown";
            // Department removed per requirements
        }

        appointmentsTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
    }

    private JPanel createUpcomingAppointmentsSection() {
        JPanel section = new JPanel(new BorderLayout(8, 8));
        section.setBackground(new Color(248, 249, 250));
        section.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Upcoming Week");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(new Color(47, 111, 237));
        section.add(title, BorderLayout.NORTH);

        upcomingTable = new JTable();
        upcomingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        upcomingTable.setRowHeight(28);
        refreshUpcomingTable();

        JScrollPane scroll = new JScrollPane(upcomingTable);
        scroll.setBorder(null);
        section.add(scroll, BorderLayout.CENTER);

        return section;
    }

    private void refreshUpcomingTable() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        java.util.List<Appointment> upcoming = new ArrayList<>();

        for (Appointment a : DataStore.appointments.values()) {
            LocalDate d = a.dateTime.toLocalDate();
            if (session.userId.equals(a.staffId) && d.isAfter(today) && d.isBefore(nextWeek)) {
                upcoming.add(a);
            }
        }

        upcoming.sort((a, b) -> a.dateTime.compareTo(b.dateTime));

        String[] columns = {"Date", "Time", "Patient ID", "Patient Name"};
        Object[][] data = new Object[upcoming.size()][4];

        for (int i = 0; i < upcoming.size(); i++) {
            Appointment a = upcoming.get(i);
            Patient p = DataStore.patients.get(a.patientId);
            data[i][0] = a.dateTime.toLocalDate().toString();
            data[i][1] = a.dateTime.toLocalTime().toString();
            data[i][2] = a.patientId;
            data[i][3] = p != null ? p.name : "Unknown";
            // Department removed per requirements
        }

        upcomingTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
    }

    private void refreshRequestsTable() {
        java.util.List<Appointment> pending = new ArrayList<>();
        for (Appointment a : DataStore.appointments.values()) {
            if (session.userId.equals(a.staffId) && a.notes != null && a.notes.toLowerCase().contains("pending")) {
                pending.add(a);
            }
        }
        pending.sort((a,b) -> a.dateTime.compareTo(b.dateTime));
        String[] columns = {"ID", "Patient", "Requested Date", "Department", "Status"};
        Object[][] data = new Object[pending.size()][5];
        for (int i=0;i<pending.size();i++) {
            Appointment a = pending.get(i);
            Patient p = DataStore.patients.get(a.patientId);
            data[i][0] = a.id;
            data[i][1] = (p!=null?p.name:"Unknown") + " (" + a.patientId + ")";
            data[i][2] = a.dateTime.toString();
            data[i][3] = a.department;
            data[i][4] = "Pending";
        }
        requestsTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
    }

    private void refreshPatientsTable() {
        // Get all unique patients from appointments for this doctor
        java.util.Set<String> patientIds = new LinkedHashSet<>();
        
        System.out.println("DEBUG: Refreshing patients table for doctor: " + session.userId);
        System.out.println("DEBUG: Total appointments in system: " + DataStore.appointments.size());
        System.out.println("DEBUG: Total patients in system: " + DataStore.patients.size());
        
        for (Appointment a : DataStore.appointments.values()) {
            if (session.userId.equals(a.staffId)) {
                patientIds.add(a.patientId);
                System.out.println("DEBUG: Found appointment for patient: " + a.patientId);
            }
        }
        
        System.out.println("DEBUG: Found " + patientIds.size() + " patients for this doctor");

        String[] columns = {"Patient ID", "Name", "Age", "Gender", "Contact"};
        Object[][] data = new Object[patientIds.size()][5];

        int i = 0;
        for (String pId : patientIds) {
            Patient p = DataStore.patients.get(pId);
            if (p != null) {
                data[i][0] = p.id;
                data[i][1] = p.name;
                data[i][2] = p.age;
                data[i][3] = p.gender != null ? p.gender.name() : "";
                data[i][4] = p.contact;
                i++;
                System.out.println("DEBUG: Added patient to table: " + p.name + " (ID: " + p.id + ")");
            } else {
                System.out.println("DEBUG: Patient not found for ID: " + pId);
            }
        }

        patientsTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
    }

    private void viewPatientAttachments() {
        // Get selected patient from appointments table
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient from Today's Appointments", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patientId = String.valueOf(appointmentsTable.getValueAt(selectedRow, 0));
        if (patientId == null || patientId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid patient selection", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get patient name for dialog title
        Patient patient = DataStore.patients.get(patientId);
        String patientName = patient != null ? patient.name : "Unknown";

        // Create attachment dialog
        JDialog attachmentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Medical Documents - " + patientName + " (" + patientId + ")", true);
        attachmentDialog.setLayout(new BorderLayout(10, 10));
        attachmentDialog.setSize(900, 600);
        attachmentDialog.setLocationRelativeTo(this);

        // Create medical document folder panel
        hpms.ui.components.MedicalDocumentFolderPanel folderPanel = 
            new hpms.ui.components.MedicalDocumentFolderPanel(patientId);

        // Enable upload functionality for doctors (similar to clinical info)
        // Doctors can upload medical documents, test results, prescriptions, etc.
        folderPanel.getUploadButton().setVisible(true);
        folderPanel.getUploadButton().setText("Upload Medical Document");

        attachmentDialog.add(folderPanel, BorderLayout.CENTER);

        // Close button
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> attachmentDialog.dispose());
        closePanel.add(closeBtn);
        attachmentDialog.add(closePanel, BorderLayout.SOUTH);

        attachmentDialog.setVisible(true);
    }

    private void confirmSelectedAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to confirm", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = String.valueOf(appointmentsTable.getValueAt(selectedRow, 0));
        if (appointmentId == null || appointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid appointment selection", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm appointment using AppointmentService
        java.util.List<String> result = hpms.service.AppointmentService.confirmAppointment(appointmentId, session.userId);
        
        if (result.get(0).startsWith("Appointment confirmed")) {
            JOptionPane.showMessageDialog(this, "Appointment confirmed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAppointmentsTable();
        } else {
            JOptionPane.showMessageDialog(this, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        refreshAppointmentsTable();
        if (upcomingTable != null) refreshUpcomingTable();
        refreshRequestsTable();
        refreshPatientsTable();
    }

    private JPanel createProfileTab() {
        JPanel profilePanel = new JPanel(new BorderLayout(12, 12));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Get current doctor's staff information
        Staff currentStaff = DataStore.staff.get(session.userId);
        
        if (currentStaff == null) {
            JLabel errorLabel = new JLabel("Profile information not available");
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            errorLabel.setForeground(Color.RED);
            profilePanel.add(errorLabel, BorderLayout.CENTER);
            return profilePanel;
        }
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(31, 41, 55));
        header.add(titleLabel, BorderLayout.WEST);
        
        // Edit/Save buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("Edit Profile");
        JButton saveBtn = new JButton("Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.setVisible(false);
        cancelBtn.setVisible(false);
        
        buttonPanel.add(editBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        header.add(buttonPanel, BorderLayout.EAST);
        
        profilePanel.add(header, BorderLayout.NORTH);
        
        // Profile form
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        
        // Left side - Profile picture
        JPanel picturePanel = new JPanel(new BorderLayout());
        picturePanel.setBackground(Color.WHITE);
        picturePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        
        JLabel pictureLabel = new JLabel();
        pictureLabel.setPreferredSize(new Dimension(150, 150));
        pictureLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        pictureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pictureLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Load existing photo or show placeholder
        if (currentStaff.photoPath != null && !currentStaff.photoPath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(currentStaff.photoPath);
                Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                pictureLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                pictureLabel.setText("No Photo");
                pictureLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                pictureLabel.setForeground(Color.GRAY);
            }
        } else {
            pictureLabel.setText("No Photo");
            pictureLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            pictureLabel.setForeground(Color.GRAY);
        }
        
        JButton uploadPhotoBtn = new JButton("Upload Photo");
        uploadPhotoBtn.setEnabled(false); // Initially disabled
        
        picturePanel.add(pictureLabel, BorderLayout.CENTER);
        picturePanel.add(uploadPhotoBtn, BorderLayout.SOUTH);
        
        // Right side - Form fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Form fields
        JTextField nameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField licenseField = new JTextField(20);
        JTextField specialtyField = new JTextField(20);
        JComboBox<String> deptCombo = new JComboBox<>(new String[]{"Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Oncology", "ER", "Admin", "Nursing", "Billing"});
        JTextField yearsExpField = new JTextField(10);
        
        // Make fields initially non-editable
        nameField.setEditable(false);
        phoneField.setEditable(false);
        emailField.setEditable(false);
        addressField.setEditable(false);
        licenseField.setEditable(false);
        specialtyField.setEditable(false);
        deptCombo.setEnabled(false);
        yearsExpField.setEditable(false);
        
        // Load current data
        nameField.setText(currentStaff.name != null ? currentStaff.name : "");
        phoneField.setText(currentStaff.phone != null ? currentStaff.phone : "");
        emailField.setText(currentStaff.email != null ? currentStaff.email : "");
        addressField.setText(currentStaff.address != null ? currentStaff.address : "");
        licenseField.setText(currentStaff.licenseNumber != null ? currentStaff.licenseNumber : "");
        specialtyField.setText(currentStaff.specialty != null ? currentStaff.specialty : "");
        deptCombo.setSelectedItem(currentStaff.department != null ? currentStaff.department : "Cardiology");
        yearsExpField.setText(currentStaff.yearsExperience != null ? String.valueOf(currentStaff.yearsExperience) : "");
        
        // Add form fields
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Staff ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(new JLabel(currentStaff.id), gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(nameField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(phoneField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(emailField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(addressField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(deptCombo, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("License Number:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(licenseField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Specialty:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(specialtyField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Years Experience:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fieldsPanel.add(yearsExpField, gbc);
        
        // Upload photo button action
        uploadPhotoBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                
                try {
                    // Validate file size (max 5MB)
                    long fileSize = selectedFile.length();
                    if (fileSize > 5 * 1024 * 1024) {
                        JOptionPane.showMessageDialog(this, "File size must be less than 5MB", "File Size Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Load and display image
                    ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                    Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    pictureLabel.setIcon(new ImageIcon(scaledImage));
                    pictureLabel.setText("");
                    
                    // Store the file path temporarily (will be saved on save button)
                    currentStaff.photoPath = selectedFile.getAbsolutePath();
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage(), "Image Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Edit button action
        editBtn.addActionListener(e -> {
            nameField.setEditable(true);
            phoneField.setEditable(true);
            emailField.setEditable(true);
            addressField.setEditable(true);
            licenseField.setEditable(true);
            specialtyField.setEditable(true);
            deptCombo.setEnabled(true);
            yearsExpField.setEditable(true);
            uploadPhotoBtn.setEnabled(true);
            
            editBtn.setVisible(false);
            saveBtn.setVisible(true);
            cancelBtn.setVisible(true);
        });
        
        // Save button action
        saveBtn.addActionListener(e -> {
            // Validate input
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Phone is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!hpms.util.Validators.isValidPhoneNumber(phone)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit phone number", "Phone Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!hpms.util.Validators.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address", "Email Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Update staff object
            currentStaff.name = name;
            currentStaff.phone = phone;
            currentStaff.email = email;
            currentStaff.address = addressField.getText().trim().isEmpty() ? null : addressField.getText().trim();
            currentStaff.department = (String) deptCombo.getSelectedItem();
            currentStaff.licenseNumber = licenseField.getText().trim();
            currentStaff.specialty = specialtyField.getText().trim();
            
            String yearsText = yearsExpField.getText().trim();
            if (!yearsText.isEmpty()) {
                try {
                    currentStaff.yearsExperience = Integer.parseInt(yearsText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Years Experience must be a number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            // Save to database
            hpms.service.StaffService.updateStaff(currentStaff);
            
            // Make fields read-only again
            nameField.setEditable(false);
            phoneField.setEditable(false);
            emailField.setEditable(false);
            addressField.setEditable(false);
            licenseField.setEditable(false);
            specialtyField.setEditable(false);
            deptCombo.setEnabled(false);
            yearsExpField.setEditable(false);
            uploadPhotoBtn.setEnabled(false);
            
            editBtn.setVisible(true);
            saveBtn.setVisible(false);
            cancelBtn.setVisible(false);
            
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Cancel button action
        cancelBtn.addActionListener(e -> {
            // Reload original data
            nameField.setText(currentStaff.name != null ? currentStaff.name : "");
            phoneField.setText(currentStaff.phone != null ? currentStaff.phone : "");
            emailField.setText(currentStaff.email != null ? currentStaff.email : "");
            addressField.setText(currentStaff.address != null ? currentStaff.address : "");
            licenseField.setText(currentStaff.licenseNumber != null ? currentStaff.licenseNumber : "");
            specialtyField.setText(currentStaff.specialty != null ? currentStaff.specialty : "");
            deptCombo.setSelectedItem(currentStaff.department != null ? currentStaff.department : "Cardiology");
            yearsExpField.setText(currentStaff.yearsExperience != null ? String.valueOf(currentStaff.yearsExperience) : "");
            
            // Reload profile picture
            if (currentStaff.photoPath != null && !currentStaff.photoPath.isEmpty()) {
                try {
                    ImageIcon icon = new ImageIcon(currentStaff.photoPath);
                    Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    pictureLabel.setIcon(new ImageIcon(scaledImage));
                    pictureLabel.setText("");
                } catch (Exception ex) {
                    pictureLabel.setIcon(null);
                    pictureLabel.setText("No Photo");
                    pictureLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    pictureLabel.setForeground(Color.GRAY);
                }
            } else {
                pictureLabel.setIcon(null);
                pictureLabel.setText("No Photo");
                pictureLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                pictureLabel.setForeground(Color.GRAY);
            }
            
            // Make fields read-only again
            nameField.setEditable(false);
            phoneField.setEditable(false);
            emailField.setEditable(false);
            addressField.setEditable(false);
            licenseField.setEditable(false);
            specialtyField.setEditable(false);
            deptCombo.setEnabled(false);
            yearsExpField.setEditable(false);
            uploadPhotoBtn.setEnabled(false);
            
            editBtn.setVisible(true);
            saveBtn.setVisible(false);
            cancelBtn.setVisible(false);
        });
        
        // Add panels to main form
        formPanel.add(picturePanel, BorderLayout.WEST);
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        profilePanel.add(formPanel, BorderLayout.CENTER);
        return profilePanel;
    }
}
