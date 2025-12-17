package hpms.ui.staff;

import hpms.model.Staff;
import hpms.util.DataStore;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal dialog for editing existing staff information
 * All fields except Staff ID are editable
 */
public class StaffEditForm extends JDialog {
    private Staff staff;
    private JTextField nameField, phoneField, emailField, addressField;
    private JComboBox<String> deptCombo, statusCombo;
    private JTextField licenseField;
    private JTextField yearsExperienceField, yearsOfWorkField;
    
    // Clinic schedule components
    private JCheckBox[] dayCheckboxes = new JCheckBox[7];
    private JTextField[] startTimeFields = new JTextField[7];
    private JTextField[] endTimeFields = new JTextField[7];
    private String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public StaffEditForm(Window owner, Staff staff) {
        super((Frame) (owner instanceof Frame ? owner : SwingUtilities.getWindowAncestor((Component) owner)), "Edit Staff - " + staff.id, ModalityType.APPLICATION_MODAL);
        this.staff = staff;
        
        setSize(650, 750);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        loadStaffData();
    }

    private void initComponents() {
        // Main panel
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

        JLabel titleLabel = new JLabel("Edit Staff Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 70, 140));

        JLabel idLabel = new JLabel("Staff ID: " + staff.id + " (Cannot be changed)");
        idLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        idLabel.setForeground(new Color(100, 100, 120));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(idLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        int row = 0;

        // Name
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Name *"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        nameField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(nameField, gbc);

        // Role (read-only)
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Role"), gbc);
        gbc.gridx = 1;
        JLabel roleLabel = new JLabel(staff.role != null ? staff.role.toString() : "N/A");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(80, 80, 80));
        formPanel.add(roleLabel, gbc);

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

        // Address
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Address"), gbc);
        gbc.gridx = 1;
        addressField = new JTextField(15);
        addressField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(addressField, gbc);

        // Department
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Department *"), gbc);
        gbc.gridx = 1;
        deptCombo = new JComboBox<>(new String[]{"Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Oncology", "ER", "Admin", "Nursing", "Billing"});
        deptCombo.setBackground(Color.WHITE);
        deptCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(deptCombo, gbc);

        // Status
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("Status *"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"Active", "On Leave", "Out of Site", "Resigned"});
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(statusCombo, gbc);

        // License Number
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createLabel("License Number"), gbc);
        gbc.gridx = 1;
        licenseField = new JTextField(15);
        licenseField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        formPanel.add(licenseField, gbc);


        // Years Experience
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

        // Clinic Schedule Section
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel scheduleHeader = new JLabel("Clinic Schedule");
        scheduleHeader.setFont(new Font("Arial", Font.BOLD, 12));
        scheduleHeader.setForeground(new Color(47, 111, 237));
        formPanel.add(scheduleHeader, gbc);
        gbc.gridwidth = 1;

        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        formPanel.add(createClinicSchedulePanel(), gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;

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

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 11));
        saveBtn.setBackground(new Color(47, 111, 237));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveChanges());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        formPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(60, 80, 120));
        return label;
    }

    private void loadStaffData() {
        nameField.setText(staff.name);
        phoneField.setText(staff.phone != null ? staff.phone : "");
        emailField.setText(staff.email != null ? staff.email : "");
        addressField.setText(staff.address != null ? staff.address : "");
        deptCombo.setSelectedItem(staff.department);
        licenseField.setText(staff.licenseNumber != null ? staff.licenseNumber : "");
        
        yearsExperienceField.setText(staff.yearsExperience != null ? String.valueOf(staff.yearsExperience) : "");
        yearsOfWorkField.setText(staff.yearsOfWork != null ? String.valueOf(staff.yearsOfWork) : "");

        // Load clinic schedule
        for (int i = 0; i < 7; i++) {
            Staff.ScheduleEntry entry = staff.clinicSchedule.getOrDefault(daysOfWeek[i], 
                new Staff.ScheduleEntry(false, "08:00", "17:00"));
            dayCheckboxes[i].setSelected(entry.active);
            startTimeFields[i].setText(entry.startTime);
            endTimeFields[i].setText(entry.endTime);
        }

        // Set status
        String status = staff.isAvailable ? "Active" : "Inactive";
        statusCombo.setSelectedItem(status);
    }

    private void saveChanges() {
        // Validate required fields
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!phone.matches("^[0-9+()\\-\\s]{7,25}$")) {
            JOptionPane.showMessageDialog(this, "Phone format is invalid", "Validation Error", JOptionPane.WARNING_MESSAGE);
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

        // Update staff object
        staff.name = name;
        staff.phone = phone;
        staff.email = email;
        staff.address = address.isEmpty() ? null : address;
        staff.department = (String) deptCombo.getSelectedItem();
        staff.licenseNumber = licenseField.getText().trim();
        

        String statusText = (String) statusCombo.getSelectedItem();
        boolean prevAvail = staff.isAvailable;
        staff.isAvailable = "Active".equals(statusText);

        String yearsText = yearsExperienceField.getText().trim();
        if (!yearsText.isEmpty()) {
            try {
                staff.yearsExperience = Integer.parseInt(yearsText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Years Experience must be a number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String yearsOfWorkText = yearsOfWorkField.getText().trim();
        if (!yearsOfWorkText.isEmpty()) {
            try {
                staff.yearsOfWork = Integer.parseInt(yearsOfWorkText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Years at Hospital must be a number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Update clinic schedule
        for (int i = 0; i < 7; i++) {
            String startTime = startTimeFields[i].getText().trim();
            String endTime = endTimeFields[i].getText().trim();
            boolean isActive = dayCheckboxes[i].isSelected();

            // Validate time format if active
            if (isActive && (!startTime.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$") || 
                !endTime.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$"))) {
                JOptionPane.showMessageDialog(this, "Invalid time format for " + daysOfWeek[i] + ". Use HH:mm format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            staff.clinicSchedule.put(daysOfWeek[i], new Staff.ScheduleEntry(isActive, startTime, endTime));
        }

        if (prevAvail && !staff.isAvailable && staff.role == hpms.model.StaffRole.DOCTOR) {
            java.time.LocalDate today = java.time.LocalDate.now();
            int affected = hpms.service.AppointmentService.countForDoctorOn(staff.id, today);
            if (affected > 0) {
                for (hpms.model.Appointment a : hpms.util.DataStore.appointments.values()) {
                    if (staff.id.equals(a.staffId) && a.dateTime.toLocalDate().equals(today) && !a.isCompleted) {
                        a.notes = (a.notes==null?"":"[Doctor unavailable] ") + "Doctor marked unavailable; please await reschedule";
                        try { hpms.service.CommunicationService.addAlert(a.patientId, "Your doctor (" + (staff.name==null?staff.id:staff.name) + ") is unavailable today. We will reschedule or advise you shortly."); } catch (Exception ex) { }
                    }
                }
                // Disabled backup save - using database instead

                String[] options = new String[]{"Reschedule whole day", "Adjust times only", "Skip"};
                int choice = javax.swing.JOptionPane.showOptionDialog(this,
                        "Doctor marked unavailable. " + affected + " appointment(s) today will be affected.",
                        "Schedule Adjustment",
                        javax.swing.JOptionPane.DEFAULT_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);
                if (choice == 0) {
                    javax.swing.JSpinner dateSpinner = new javax.swing.JSpinner(new javax.swing.SpinnerDateModel());
                    javax.swing.JSpinner.DateEditor de = new javax.swing.JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
                    dateSpinner.setEditor(de);
                    javax.swing.JSpinner timeSpinner = new javax.swing.JSpinner(new javax.swing.SpinnerDateModel());
                    javax.swing.JSpinner.DateEditor te = new javax.swing.JSpinner.DateEditor(timeSpinner, "HH:mm");
                    timeSpinner.setEditor(te);
                    int r = javax.swing.JOptionPane.showConfirmDialog(this, new Object[]{"New Date", dateSpinner, "Start Time", timeSpinner}, "Reschedule Day", javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (r == javax.swing.JOptionPane.OK_OPTION) {
                        java.util.Date d = (java.util.Date) dateSpinner.getValue();
                        java.util.Date t = (java.util.Date) timeSpinner.getValue();
                        java.time.LocalDate newDate = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        java.time.LocalTime startTime = t.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();

                        java.util.List<hpms.model.Appointment> list = new java.util.ArrayList<>();
                        for (hpms.model.Appointment a : hpms.util.DataStore.appointments.values()) if (staff.id.equals(a.staffId) && a.dateTime.toLocalDate().equals(today) && !a.isCompleted) list.add(a);
                        list.sort(java.util.Comparator.comparing(a -> a.dateTime));

                        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(new String[]{"Patient", "Old Time", "New Date", "New Time"}, 0) { public boolean isCellEditable(int r,int c){return false;} };
                        java.time.LocalTime base = startTime;
                        for (hpms.model.Appointment a : list) {
                            java.time.LocalDateTime proposed = java.time.LocalDateTime.of(newDate, base);
                            boolean conflict = true; int guard = 0;
                            while (conflict && guard < 48) {
                                conflict = false;
                                java.time.LocalDateTime ps = proposed; java.time.LocalDateTime pe = proposed.plusHours(1);
                                for (hpms.model.Appointment x : hpms.util.DataStore.appointments.values()) {
                                    if (!x.id.equals(a.id) && staff.id.equals(x.staffId) && x.dateTime.toLocalDate().equals(newDate)) {
                                        java.time.LocalDateTime xs = x.dateTime; java.time.LocalDateTime xe = x.dateTime.plusHours(1);
                                        if (!(pe.isBefore(xs) || ps.isAfter(xe) || pe.equals(xs) || ps.equals(xe))) { conflict = true; break; }
                                    }
                                }
                                if (conflict) { proposed = proposed.plusHours(1); }
                                guard++;
                            }
                            model.addRow(new Object[]{a.patientId, a.dateTime.toLocalTime().toString(), proposed.toLocalDate().toString(), proposed.toLocalTime().toString()});
                            base = proposed.toLocalTime().plusHours(1);
                        }

                        javax.swing.JTable table = new javax.swing.JTable(model);
                        table.setRowHeight(22);
                        int rr = javax.swing.JOptionPane.showConfirmDialog(this, new Object[]{"Preview", new javax.swing.JScrollPane(table)}, "Confirm Reschedule", javax.swing.JOptionPane.OK_CANCEL_OPTION);
                        if (rr == javax.swing.JOptionPane.OK_OPTION) {
                            java.util.List<String> out = hpms.service.AppointmentService.rescheduleDoctorDay(staff.id, today, newDate, startTime);
                            if (!out.isEmpty()) javax.swing.JOptionPane.showMessageDialog(this, out.get(0));
                        }
                    }
                } else if (choice == 1) {
                    javax.swing.JSpinner timeSpinner = new javax.swing.JSpinner(new javax.swing.SpinnerDateModel());
                    javax.swing.JSpinner.DateEditor te = new javax.swing.JSpinner.DateEditor(timeSpinner, "HH:mm");
                    timeSpinner.setEditor(te);
                    int r = javax.swing.JOptionPane.showConfirmDialog(this, new Object[]{"New Start Time", timeSpinner}, "Adjust Times", javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (r == javax.swing.JOptionPane.OK_OPTION) {
                        java.util.Date t = (java.util.Date) timeSpinner.getValue();
                        java.time.LocalTime startTime = t.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();

                        java.util.List<hpms.model.Appointment> list = new java.util.ArrayList<>();
                        for (hpms.model.Appointment a : hpms.util.DataStore.appointments.values()) if (staff.id.equals(a.staffId) && a.dateTime.toLocalDate().equals(today) && !a.isCompleted) list.add(a);
                        list.sort(java.util.Comparator.comparing(a -> a.dateTime));

                        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(new String[]{"Patient", "Old Time", "New Date", "New Time"}, 0) { public boolean isCellEditable(int r,int c){return false;} };
                        java.time.LocalTime base = startTime;
                        for (hpms.model.Appointment a : list) {
                            java.time.LocalDateTime proposed = java.time.LocalDateTime.of(today, base);
                            boolean conflict = true; int guard = 0;
                            while (conflict && guard < 48) {
                                conflict = false;
                                java.time.LocalDateTime ps = proposed; java.time.LocalDateTime pe = proposed.plusHours(1);
                                for (hpms.model.Appointment x : hpms.util.DataStore.appointments.values()) {
                                    if (!x.id.equals(a.id) && staff.id.equals(x.staffId) && x.dateTime.toLocalDate().equals(today)) {
                                        java.time.LocalDateTime xs = x.dateTime; java.time.LocalDateTime xe = x.dateTime.plusHours(1);
                                        if (!(pe.isBefore(xs) || ps.isAfter(xe) || pe.equals(xs) || ps.equals(xe))) { conflict = true; break; }
                                    }
                                }
                                if (conflict) { proposed = proposed.plusHours(1); }
                                guard++;
                            }
                            model.addRow(new Object[]{a.patientId, a.dateTime.toLocalTime().toString(), proposed.toLocalDate().toString(), proposed.toLocalTime().toString()});
                            base = proposed.toLocalTime().plusHours(1);
                        }

                        javax.swing.JTable table = new javax.swing.JTable(model);
                        table.setRowHeight(22);
                        int rr = javax.swing.JOptionPane.showConfirmDialog(this, new Object[]{"Preview", new javax.swing.JScrollPane(table)}, "Confirm Adjust Times", javax.swing.JOptionPane.OK_CANCEL_OPTION);
                        if (rr == javax.swing.JOptionPane.OK_OPTION) {
                            java.util.List<String> out = hpms.service.AppointmentService.rescheduleDoctorDay(staff.id, today, today, startTime);
                            if (!out.isEmpty()) javax.swing.JOptionPane.showMessageDialog(this, out.get(0));
                        }
                    }
                }
            }
        }

        logStaffUpdate(staff.id, "Staff information updated");
        DataStore.staff.put(staff.id, staff);
        javax.swing.JOptionPane.showMessageDialog(this, "Staff information updated successfully", "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private JPanel createClinicSchedulePanel() {
        JPanel schedulePanel = new JPanel(new GridBagLayout());
        schedulePanel.setBackground(Color.WHITE);
        schedulePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 220, 240), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header row
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        addHeaderCell(schedulePanel, "Day", gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.15;
        addHeaderCell(schedulePanel, "Active", gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.3;
        addHeaderCell(schedulePanel, "Start Time", gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        addHeaderCell(schedulePanel, "End Time", gbc);

        // Data rows
        for (int i = 0; i < 7; i++) {
            gbc.gridy = i + 1;

            // Day name
            gbc.gridx = 0;
            gbc.weightx = 0.2;
            JLabel dayLabel = new JLabel(daysOfWeek[i]);
            dayLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            dayLabel.setForeground(new Color(60, 80, 100));
            dayLabel.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 245)));
            dayLabel.setOpaque(true);
            dayLabel.setBackground(new Color(255, 255, 255));
            schedulePanel.add(dayLabel, gbc);

            // Active checkbox
            gbc.gridx = 1;
            gbc.weightx = 0.15;
            dayCheckboxes[i] = new JCheckBox();
            dayCheckboxes[i].setBackground(Color.WHITE);
            schedulePanel.add(dayCheckboxes[i], gbc);

            // Start time
            gbc.gridx = 2;
            gbc.weightx = 0.3;
            startTimeFields[i] = new JTextField(5);
            startTimeFields[i].setText("08:00");
            startTimeFields[i].setFont(new Font("Arial", Font.PLAIN, 11));
            startTimeFields[i].setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
            startTimeFields[i].setHorizontalAlignment(JTextField.CENTER);
            schedulePanel.add(startTimeFields[i], gbc);

            // End time
            gbc.gridx = 3;
            gbc.weightx = 0.3;
            endTimeFields[i] = new JTextField(5);
            endTimeFields[i].setText("17:00");
            endTimeFields[i].setFont(new Font("Arial", Font.PLAIN, 11));
            endTimeFields[i].setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
            endTimeFields[i].setHorizontalAlignment(JTextField.CENTER);
            schedulePanel.add(endTimeFields[i], gbc);
        }

        return schedulePanel;
    }

    private void addHeaderCell(JPanel panel, String text, GridBagConstraints gbc) {
        JLabel header = new JLabel(text);
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(47, 111, 237));
        header.setOpaque(true);
        header.setHorizontalAlignment(JLabel.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        panel.add(header, gbc);
    }

    private void logStaffUpdate(String staffId, String action) {
        // Log to system or DataStore as needed
    }
}
