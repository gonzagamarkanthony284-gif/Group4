package hpms.ui.doctor;

import hpms.auth.AuthSession;
import hpms.model.Staff;
import hpms.util.DataStore;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        photoLabel = new JLabel();
        if (doctorStaff != null && doctorStaff.photoPath != null && !doctorStaff.photoPath.trim().isEmpty()) {
            updatePhotoLabelFromPath(doctorStaff.photoPath);
        } else {
            photoLabel.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        }
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        photoLabel.setForeground(new Color(107, 114, 128));
        panel.add(photoLabel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        bottom.setOpaque(false);
        uploadPhotoBtn = new JButton("Upload Photo");
        uploadPhotoBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        uploadPhotoBtn.setBackground(new Color(47, 111, 237));
        uploadPhotoBtn.setForeground(Color.WHITE);
        uploadPhotoBtn.setFocusPainted(false);
        uploadPhotoBtn.addActionListener(e -> choosePhoto());
        bottom.add(uploadPhotoBtn);
        panel.add(bottom, BorderLayout.SOUTH);

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
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                String path = f.getAbsolutePath();
                if (doctorStaff != null) {
                    doctorStaff.photoPath = path;
                }
                updatePhotoLabelFromPath(path);
            }
        }
    }

    private void updatePhotoLabelFromPath(String path) {
        ImageIcon icon = new ImageIcon(path);
        int w = 160;
        int h = 160;
        Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        photoLabel.setIcon(new ImageIcon(scaled));
    }

}
