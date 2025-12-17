package hpms.ui.doctor;

import hpms.auth.AuthSession;
import hpms.model.Staff;
import hpms.service.FileService;
import hpms.service.StaffService;
import hpms.util.DataStore;
import hpms.util.ImageUtils;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Doctor Profile Panel - view and edit doctor profile
 */
public class DoctorProfilePanel extends JPanel {
    private Staff doctorStaff;
    private JLabel photoLabel;
    private JTextField nameField;
    private JTextField specializationField;
    private JTextField licenseField;
    private JTextField yearsField;
    private JTextArea bioArea;
    private JTextArea certificationsArea;
    private JTextArea educationArea;
    private JTextArea expertiseArea;
    private JTextArea skillsArea;
    private JTextArea competenciesArea;
    private boolean isEditing = false;
    private JButton editBtn;
    private JButton saveBtn;
    private JButton uploadPhotoBtn;

    public DoctorProfilePanel(AuthSession session) {
        this.doctorStaff = DataStore.staff.get(session.userId);

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(31, 41, 55));
        add(title, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new GridLayout(1, 2, 16, 16));
        content.setOpaque(false);
        content.add(createPhotoPanel());
        content.add(createDetailsPanel());
        add(content, BorderLayout.CENTER);

        // Bottom actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(new Color(248, 249, 250));
        actions.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));

        editBtn = new JButton("Edit Profile");
        editBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        editBtn.setBackground(new Color(47, 111, 237));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        editBtn.addActionListener(e -> toggleEdit());

        saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        saveBtn.setBackground(new Color(34, 197, 94));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveProfile());
        saveBtn.setVisible(false);

        actions.add(editBtn);
        actions.add(saveBtn);
        add(actions, BorderLayout.SOUTH);
    }

    private JPanel createPhotoPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        // Photo container with fixed size
        JPanel photoContainer = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 250);
            }
        };
        photoContainer.setBackground(Color.WHITE);
        photoContainer.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));

        // Photo label with centered icon and text
        photoLabel = new JLabel("", JLabel.CENTER);
        photoLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photoLabel.setHorizontalTextPosition(JLabel.CENTER);
        photoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        photoLabel.setForeground(new Color(107, 114, 128));
        
        // Make photo label clickable
        photoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        photoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                choosePhoto();
            }
        });

        // Load the current photo if exists
        if (doctorStaff != null && doctorStaff.photoPath != null && !doctorStaff.photoPath.trim().isEmpty()) {
            updatePhotoLabelFromPath(doctorStaff.photoPath);
        } else {
            updatePhotoLabelFromPath(null);
        }
        
        // Upload button
        uploadPhotoBtn = new JButton("Change Photo");
        uploadPhotoBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        uploadPhotoBtn.setBackground(new Color(47, 111, 237));
        uploadPhotoBtn.setForeground(Color.WHITE);
        uploadPhotoBtn.setFocusPainted(false);
        uploadPhotoBtn.setBorderPainted(false);
        uploadPhotoBtn.setOpaque(true);
        uploadPhotoBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        uploadPhotoBtn.addActionListener(e -> choosePhoto());
        
        // Add components to panel
        photoContainer.add(photoLabel, BorderLayout.CENTER);
        panel.add(photoContainer, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.add(uploadPhotoBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Name
        gbc.gridy = row++;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(nameLabel, gbc);
        gbc.gridy = row++;
        nameField = new JTextField(doctorStaff != null ? doctorStaff.name : "");
        nameField.setFont(new Font("Arial", Font.PLAIN, 11));
        nameField.setEditable(false);
        panel.add(nameField, gbc);

        // Specialization
        gbc.gridy = row++;
        JLabel specLabel = new JLabel("Specialization (Permanent):");
        specLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(specLabel, gbc);
        gbc.gridy = row++;
        specializationField = new JTextField(
                doctorStaff != null && doctorStaff.specialty != null ? doctorStaff.specialty : "");
        specializationField.setFont(new Font("Arial", Font.PLAIN, 11));
        specializationField.setEditable(false);
        specializationField.setBackground(new Color(240, 240, 240));
        specializationField.setToolTipText("Doctor specialization is permanent and cannot be changed");
        panel.add(specializationField, gbc);

        // License Number
        gbc.gridy = row++;
        JLabel licenseLabel = new JLabel("License Number:");
        licenseLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(licenseLabel, gbc);
        gbc.gridy = row++;
        licenseField = new JTextField(
                doctorStaff != null && doctorStaff.licenseNumber != null ? doctorStaff.licenseNumber : "");
        licenseField.setFont(new Font("Arial", Font.PLAIN, 11));
        licenseField.setEditable(false);
        panel.add(licenseField, gbc);

        // Years of Practice
        gbc.gridy = row++;
        JLabel yearsLabel = new JLabel("Years of Practice:");
        yearsLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(yearsLabel, gbc);
        gbc.gridy = row++;
        yearsField = new JTextField(
                doctorStaff != null && doctorStaff.yearsExperience != null ? String.valueOf(doctorStaff.yearsExperience)
                        : "");
        yearsField.setFont(new Font("Arial", Font.PLAIN, 11));
        yearsField.setEditable(false);
        panel.add(yearsField, gbc);

        // Bio
        gbc.gridy = row++;
        JLabel bioLabel = new JLabel("Professional Bio:");
        bioLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(bioLabel, gbc);
        gbc.gridy = row++;
        bioArea = new JTextArea(4, 30);
        bioArea.setFont(new Font("Arial", Font.PLAIN, 11));
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setEditable(false);
        bioArea.setText(doctorStaff != null && doctorStaff.bio != null ? doctorStaff.bio : "");
        panel.add(new JScrollPane(bioArea), gbc);

        // Certifications
        gbc.gridy = row++;
        JLabel certificationsLabel = new JLabel("Certifications:");
        certificationsLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(certificationsLabel, gbc);
        gbc.gridy = row++;
        certificationsArea = new JTextArea(3, 30);
        certificationsArea.setFont(new Font("Arial", Font.PLAIN, 11));
        certificationsArea.setLineWrap(true);
        certificationsArea.setWrapStyleWord(true);
        certificationsArea.setEditable(false);
        certificationsArea.setText(doctorStaff != null && doctorStaff.certifications != null ? doctorStaff.certifications : "");
        panel.add(new JScrollPane(certificationsArea), gbc);

        // Education
        gbc.gridy = row++;
        JLabel educationLabel = new JLabel("Education:");
        educationLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(educationLabel, gbc);
        gbc.gridy = row++;
        educationArea = new JTextArea(3, 30);
        educationArea.setFont(new Font("Arial", Font.PLAIN, 11));
        educationArea.setLineWrap(true);
        educationArea.setWrapStyleWord(true);
        educationArea.setEditable(false);
        educationArea.setText(doctorStaff != null && doctorStaff.education != null ? doctorStaff.education : "");
        panel.add(new JScrollPane(educationArea), gbc);

        // Expertise
        gbc.gridy = row++;
        JLabel expertiseLabel = new JLabel("Work Expertise:");
        expertiseLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(expertiseLabel, gbc);
        gbc.gridy = row++;
        expertiseArea = new JTextArea(3, 30);
        expertiseArea.setFont(new Font("Arial", Font.PLAIN, 11));
        expertiseArea.setLineWrap(true);
        expertiseArea.setWrapStyleWord(true);
        expertiseArea.setEditable(false);
        expertiseArea.setText(doctorStaff != null && doctorStaff.expertise != null ? doctorStaff.expertise : "");
        panel.add(new JScrollPane(expertiseArea), gbc);

        // Skills
        gbc.gridy = row++;
        JLabel skillsLabel = new JLabel("Skills:");
        skillsLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(skillsLabel, gbc);
        gbc.gridy = row++;
        skillsArea = new JTextArea(3, 30);
        skillsArea.setFont(new Font("Arial", Font.PLAIN, 11));
        skillsArea.setLineWrap(true);
        skillsArea.setWrapStyleWord(true);
        skillsArea.setEditable(false);
        skillsArea.setText(doctorStaff != null && doctorStaff.skills != null ? doctorStaff.skills : "");
        panel.add(new JScrollPane(skillsArea), gbc);

        // Competencies
        gbc.gridy = row++;
        JLabel competenciesLabel = new JLabel("Competencies:");
        competenciesLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(competenciesLabel, gbc);
        gbc.gridy = row++;
        competenciesArea = new JTextArea(3, 30);
        competenciesArea.setFont(new Font("Arial", Font.PLAIN, 11));
        competenciesArea.setLineWrap(true);
        competenciesArea.setWrapStyleWord(true);
        competenciesArea.setEditable(false);
        competenciesArea.setText(doctorStaff != null && doctorStaff.competencies != null ? doctorStaff.competencies : "");
        panel.add(new JScrollPane(competenciesArea), gbc);

        return panel;
    }

    private void toggleEdit() {
        isEditing = !isEditing;
        nameField.setEditable(isEditing);
        // Specialization is permanent and cannot be edited
        specializationField.setEditable(false);
        licenseField.setEditable(isEditing);
        yearsField.setEditable(isEditing);
        bioArea.setEditable(isEditing);
        certificationsArea.setEditable(isEditing);
        educationArea.setEditable(isEditing);
        expertiseArea.setEditable(isEditing);
        skillsArea.setEditable(isEditing);
        competenciesArea.setEditable(isEditing);
        saveBtn.setVisible(isEditing);
    }

    private void saveProfile() {
        if (doctorStaff != null) {
            // Specialization is permanent and cannot be changed
            // doctorStaff.specialty = specializationField.getText(); // REMOVED - permanent
            // field
            doctorStaff.licenseNumber = licenseField.getText();
            try {
                doctorStaff.yearsExperience = Integer.parseInt(yearsField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid years value", "Error", JOptionPane.ERROR_MESSAGE);
            }
            doctorStaff.bio = bioArea.getText();
            doctorStaff.certifications = certificationsArea.getText();
            doctorStaff.education = educationArea.getText();
            doctorStaff.expertise = expertiseArea.getText();
            doctorStaff.skills = skillsArea.getText();
            doctorStaff.competencies = competenciesArea.getText();

            // Persist to DB
            try {
                StaffService.updateStaff(doctorStaff);
            } catch (Exception ignored) {
            }
        }
        isEditing = false;
        saveBtn.setVisible(false);
        JOptionPane.showMessageDialog(this, "Profile saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refresh() {
        // Reload profile data
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
                                doctorStaff.id
                            );
                            
                            // Update the staff record
                            doctorStaff.photoPath = savedFilePath;
                            DataStore.staff.put(doctorStaff.id, doctorStaff);

                            // Persist to DB
                            try {
                                StaffService.updateStaff(doctorStaff);
                            } catch (Exception ignored) {
                            }
                            
                            // Update the UI on the EDT
                            SwingUtilities.invokeLater(() -> {
                                updatePhotoLabelFromPath(savedFilePath);
                                JOptionPane.showMessageDialog(
                                    DoctorProfilePanel.this,
                                    "Profile picture updated successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                            });
                            
                        } catch (IOException ex) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                    DoctorProfilePanel.this,
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
                        // Restore cursor
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
                    ImageIcon roundedIcon = ImageUtils.createRoundImageIcon(path, 160, 160);
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
        ImageIcon defaultIcon = ImageUtils.resizeImage(
            icon instanceof ImageIcon ? (ImageIcon) icon : new ImageIcon(), 80, 80);
        photoLabel.setIcon(defaultIcon);
        photoLabel.setText("No Photo");
        photoLabel.setToolTipText("Click to upload photo");
    }

}
