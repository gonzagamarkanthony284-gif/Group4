package hpms.ui.staff;

import hpms.auth.AuthService;
import hpms.model.Staff;
import hpms.service.FileService;
import hpms.service.StaffService;
import hpms.util.DataStore;
import hpms.util.ImageUtils;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Modal dialog showing comprehensive staff profile with editable fields
 * Triggered by double-click on staff row or View Details button
 * Includes password management and change password section
 */
public class StaffProfileModal extends JDialog {
    private Staff staff;
    private JTextField nameField, phoneField, emailField, licenseField;
    private JTextField specialtyField, yearsExperienceField, yearsOfWorkField;
    private JComboBox<String> statusCombo;
    private JLabel staffIdLabel, roleLabel, deptLabel, joinedLabel;
    private JLabel photoLabel;
    private JButton uploadPhotoBtn;

    public StaffProfileModal(Window owner, Staff staff) {
        super((Frame) (owner instanceof Frame ? owner : SwingUtilities.getWindowAncestor((Component) owner)),
                "Staff Profile - " + staff.id, ModalityType.APPLICATION_MODAL);
        this.staff = staff;

        setSize(900, 950);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        loadStaffData();
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);

        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Details", createDetailsTab());
        tabs.addTab("Account", createAccountTab());
        mainPanel.add(tabs, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 250, 255));
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 220, 240), 2),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        // Left side - title and basic info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(staff.name);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(20, 60, 100));

        staffIdLabel = new JLabel("ID: " + staff.id);
        staffIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        staffIdLabel.setForeground(new Color(100, 100, 120));

        roleLabel = new JLabel("Role: " + (staff.role != null ? staff.role.toString() : "N/A"));
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(100, 100, 120));

        deptLabel = new JLabel("Department: " + staff.department);
        deptLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        deptLabel.setForeground(new Color(100, 100, 120));

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(staffIdLabel);
        leftPanel.add(roleLabel);
        leftPanel.add(deptLabel);

        // Right side - action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        JButton editBtn = new JButton("Edit");
        editBtn.setFont(new Font("Arial", Font.BOLD, 11));
        editBtn.setBackground(new Color(47, 111, 237));
        editBtn.setForeground(Color.WHITE);
        editBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        editBtn.setFocusPainted(false);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(
                e -> new StaffEditForm(SwingUtilities.getWindowAncestor(this), staff).setVisible(true));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 11));
        closeBtn.setBackground(new Color(200, 200, 210));
        closeBtn.setForeground(new Color(40, 40, 40));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        rightPanel.add(editBtn);
        rightPanel.add(closeBtn);

        // Center - Photo section
        JPanel photoPanel = createPhotoPanel();

        header.add(leftPanel, BorderLayout.WEST);
        header.add(photoPanel, BorderLayout.CENTER);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createPhotoPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        // Photo container with fixed size
        JPanel photoContainer = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 120);
            }
        };
        photoContainer.setBackground(Color.WHITE);
        photoContainer.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));

        photoLabel = new JLabel();
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setVerticalAlignment(SwingConstants.CENTER);
        photoLabel.setText("No Photo");
        photoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        photoLabel.setForeground(new Color(150, 150, 150));
        photoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        photoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                choosePhoto();
            }
        });

        // Load the current photo if exists
        if (staff != null && staff.photoPath != null && !staff.photoPath.trim().isEmpty()) {
            updatePhotoLabelFromPath(staff.photoPath);
        } else {
            updatePhotoLabelFromPath(null);
        }
        
        // Upload button
        uploadPhotoBtn = new JButton("Change Photo");
        uploadPhotoBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        uploadPhotoBtn.setBackground(new Color(47, 111, 237));
        uploadPhotoBtn.setForeground(Color.WHITE);
        uploadPhotoBtn.setFocusPainted(false);
        uploadPhotoBtn.setBorderPainted(false);
        uploadPhotoBtn.setOpaque(true);
        uploadPhotoBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        uploadPhotoBtn.addActionListener(e -> choosePhoto());
        
        // Add components to panel
        photoContainer.add(photoLabel, BorderLayout.CENTER);
        panel.add(photoContainer, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        buttonPanel.setOpaque(false);
        buttonPanel.add(uploadPhotoBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetailsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;

        // Name
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createReadOnlyLabel("Name"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(staff.name);
        nameField.setEditable(false);
        nameField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        nameField.setPreferredSize(new Dimension(250, 28));
        formPanel.add(nameField, gbc);

        // Staff ID (read-only)
        gbc.gridy = row;
        gbc.gridx = 2;
        formPanel.add(createReadOnlyLabel("Staff ID"), gbc);
        gbc.gridx = 3;
        JLabel idField = new JLabel(staff.id);
        idField.setFont(new Font("Arial", Font.BOLD, 12));
        idField.setForeground(new Color(80, 80, 80));
        formPanel.add(idField, gbc);

        // Phone
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createReadOnlyLabel("Phone"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(staff.phone != null ? staff.phone : "");
        phoneField.setEditable(false);
        phoneField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        formPanel.add(phoneField, gbc);

        // Email
        gbc.gridx = 2;
        formPanel.add(createReadOnlyLabel("Email"), gbc);
        gbc.gridx = 3;
        emailField = new JTextField(staff.email != null ? staff.email : "");
        emailField.setEditable(false);
        emailField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        formPanel.add(emailField, gbc);

        // Department
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createReadOnlyLabel("Department"), gbc);
        gbc.gridx = 1;
        JLabel deptField = new JLabel(staff.department);
        deptField.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(deptField, gbc);

        // Status
        gbc.gridx = 2;
        formPanel.add(createReadOnlyLabel("Status"), gbc);
        gbc.gridx = 3;
        statusCombo = new JComboBox<>(new String[] { "Active", "On Leave", "Out of Site", "Resigned" });
        statusCombo.setSelectedItem(staff.isAvailable ? "Active" : "Inactive");
        statusCombo.setEnabled(false);
        formPanel.add(statusCombo, gbc);

        // License Number
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createReadOnlyLabel("License #"), gbc);
        gbc.gridx = 1;
        licenseField = new JTextField(staff.licenseNumber != null ? staff.licenseNumber : "");
        licenseField.setEditable(false);
        licenseField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        formPanel.add(licenseField, gbc);

        // Years of Experience
        gbc.gridx = 2;
        formPanel.add(createReadOnlyLabel("Years Experience"), gbc);
        gbc.gridx = 3;
        yearsExperienceField = new JTextField(
                staff.yearsExperience != null ? String.valueOf(staff.yearsExperience) : "");
        yearsExperienceField.setEditable(false);
        yearsExperienceField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        formPanel.add(yearsExperienceField, gbc);

        // Years of Work at Hospital
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createReadOnlyLabel("Years at Hospital"), gbc);
        gbc.gridx = 1;
        yearsOfWorkField = new JTextField(staff.yearsOfWork != null ? String.valueOf(staff.yearsOfWork) : "");
        yearsOfWorkField.setEditable(false);
        yearsOfWorkField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        formPanel.add(yearsOfWorkField, gbc);

        // Clinical Schedule Table
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        formPanel.add(createReadOnlyLabel("Clinic Schedule"), gbc);
        gbc.gridwidth = 1;

        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        formPanel.add(createClinicSchedulePanel(), gbc);
        gbc.gridwidth = 1;

        // Specialization (Doctor only)
        if (staff.role != null && "DOCTOR".equals(staff.role.toString())) {
            row++;
            gbc.gridy = row;
            gbc.gridx = 0;
            formPanel.add(createReadOnlyLabel("Specialization (Permanent)"), gbc);
            gbc.gridx = 1;
            specialtyField = new JTextField(staff.specialty != null ? staff.specialty : "");
            specialtyField.setEditable(false);
            specialtyField.setBackground(new Color(240, 240, 240));
            specialtyField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            specialtyField.setToolTipText("Doctor specialization is permanent and cannot be changed");
            formPanel.add(specialtyField, gbc);
        }

        // Joined Date
        row += 2;
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(createReadOnlyLabel("Joined Date"), gbc);
        gbc.gridx = 1;
        String joinedDate = staff.createdAt != null
                ? staff.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "N/A";
        joinedLabel = new JLabel(joinedDate);
        joinedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(joinedLabel, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAccountTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Account status
        boolean hasAccount = DataStore.users.containsKey(staff.id);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(hasAccount ? new Color(230, 250, 235) : new Color(255, 240, 240));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(hasAccount ? new Color(100, 180, 120) : new Color(220, 80, 80), 2),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JPanel statusContent = new JPanel();
        statusContent.setLayout(new BoxLayout(statusContent, BoxLayout.Y_AXIS));
        statusContent.setOpaque(false);

        JLabel statusTitle = new JLabel(hasAccount ? "✓ Account Active" : "✗ No Account");
        statusTitle.setFont(new Font("Arial", Font.BOLD, 18));
        statusTitle.setForeground(hasAccount ? new Color(0, 120, 60) : new Color(180, 0, 0));

        String statusMsg = hasAccount ? "Staff account is active. Password is securely stored."
                : "No login account has been created for this staff member.";
        JLabel statusDetail = new JLabel(statusMsg);
        statusDetail.setFont(new Font("Arial", Font.PLAIN, 12));
        statusDetail.setForeground(hasAccount ? new Color(60, 120, 80) : new Color(140, 60, 60));

        statusContent.add(statusTitle);
        statusContent.add(Box.createVerticalStrut(8));
        statusContent.add(statusDetail);

        // No plaintext password display

        statusPanel.add(statusContent, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(statusPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionPanel.setOpaque(false);

        if (!hasAccount) {
            JButton createBtn = new JButton("Create Account");
            createBtn.setFont(new Font("Arial", Font.BOLD, 11));
            createBtn.setBackground(new Color(34, 150, 80));
            createBtn.setForeground(Color.WHITE);
            createBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            createBtn.setFocusPainted(false);
            createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // Account creation removed - handled in Administrator section
            createBtn.setEnabled(false);
            createBtn.setVisible(false);
            // actionPanel.add(createBtn);
        }

        topPanel.add(actionPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);

        // Password manager
        panel.add(new JScrollPane(new StaffPasswordManager(staff.id, () -> {
            // Refresh on password change
        })), BorderLayout.CENTER);

        return panel;
    }

    private JLabel createReadOnlyLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(60, 80, 120));
        return label;
    }

    private void loadStaffData() {
        nameField.setText(staff.name);
        phoneField.setText(staff.phone != null ? staff.phone : "");
        emailField.setText(staff.email != null ? staff.email : "");
        licenseField.setText(staff.licenseNumber != null ? staff.licenseNumber : "");
        yearsExperienceField.setText(staff.yearsExperience != null ? String.valueOf(staff.yearsExperience) : "");
        yearsOfWorkField.setText(staff.yearsOfWork != null ? String.valueOf(staff.yearsOfWork) : "");

        if (specialtyField != null) {
            specialtyField.setText(staff.specialty != null ? staff.specialty : "");
        }
    }

    private JPanel createClinicSchedulePanel() {
        JPanel schedulePanel = new JPanel(new BorderLayout(10, 10));
        schedulePanel.setBackground(Color.WHITE);
        schedulePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 230, 245), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Create table-like layout
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
        String[] daysOfWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        for (int i = 0; i < 7; i++) {
            Staff.ScheduleEntry entry = staff.clinicSchedule.getOrDefault(daysOfWeek[i],
                    new Staff.ScheduleEntry(false, "08:00", "17:00"));

            gbc.gridy = i + 1;

            // Day name
            gbc.gridx = 0;
            JLabel dayLabel = new JLabel(daysOfWeek[i]);
            dayLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            tablePanel.add(dayLabel, gbc);

            // Status (checkmark or empty)
            gbc.gridx = 1;
            JLabel statusLabel = new JLabel(entry.active ? "✓" : "");
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            statusLabel.setForeground(new Color(34, 150, 80));
            tablePanel.add(statusLabel, gbc);

            // Start time
            gbc.gridx = 2;
            JLabel startLabel = new JLabel(entry.startTime);
            startLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            startLabel.setHorizontalAlignment(JLabel.CENTER);
            tablePanel.add(startLabel, gbc);

            // End time
            gbc.gridx = 3;
            JLabel endLabel = new JLabel(entry.endTime);
            endLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            endLabel.setHorizontalAlignment(JLabel.CENTER);
            tablePanel.add(endLabel, gbc);
        }

        schedulePanel.add(tablePanel, BorderLayout.CENTER);
        return schedulePanel;
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Picture");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile != null) {
                // Show loading indicator
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                // Use a worker thread for file operations
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            // Save the photo and get the new path
                            String savedFilePath = FileService.saveProfilePicture(
                                selectedFile, 
                                staff.id
                            );
                            
                            // Update staff record
                            staff.photoPath = savedFilePath;
                            DataStore.staff.put(staff.id, staff);

                            // Persist to DB
                            try {
                                StaffService.updateStaff(staff);
                            } catch (Exception ignored) {
                            }
                            
                            // Update the UI on the EDT
                            SwingUtilities.invokeLater(() -> {
                                updatePhotoLabelFromPath(savedFilePath);
                                JOptionPane.showMessageDialog(
                                    StaffProfileModal.this,
                                    "Profile picture updated successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                            });
                            
                        } catch (IOException ex) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                    StaffProfileModal.this,
                                    "Error uploading profile picture: " + ex.getMessage(),
                                    "Upload Error",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            });
                        }
                        return null;
                    }
                    
                    @Override
                    protected void done() {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }.execute();
            }
        }
    }

    private void updatePhotoLabelFromPath(String path) {
        try {
            if (path != null && !path.trim().isEmpty()) {
                File file = new File(path);
                if (file.exists()) {
                    // Create a rounded image icon
                    ImageIcon roundedIcon = ImageUtils.createRoundImageIcon(path, 100, 100);
                    if (roundedIcon != null) {
                        photoLabel.setIcon(roundedIcon);
                        photoLabel.setText("");
                        photoLabel.setToolTipText("Click to change photo");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set default icon if no image or error
        Icon icon = UIManager.getIcon("FileView.directoryIcon");
        photoLabel.setIcon(icon);
        photoLabel.setText("No Photo");
        photoLabel.setToolTipText("Click to change photo");
    }

    // Reset code removed

    // Account creation method removed - handled in Administrator section
}
