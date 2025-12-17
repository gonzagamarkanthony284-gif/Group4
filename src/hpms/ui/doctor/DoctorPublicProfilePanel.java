package hpms.ui.doctor;

import hpms.model.Staff;
import hpms.service.DoctorService;
import hpms.util.ImageUtils;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import javax.swing.event.HyperlinkEvent;

/**
 * Public view of a doctor's profile that can be accessed by patients and other users.
 */
public class DoctorPublicProfilePanel extends JPanel {
    
    private final Staff doctor;
    
    public DoctorPublicProfilePanel(Staff doctor) {
        this.doctor = doctor != null ? DoctorService.getDoctorById(doctor.id) : null;
        if (this.doctor == null) {
            throw new IllegalArgumentException("Doctor not found or inactive");
        }
        
        initComponents();
    }
    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header with doctor's name and title
        JLabel nameLabel = new JLabel("Dr. " + doctor.name + (doctor.licenseNumber != null ? ", " + doctor.licenseNumber : ""));
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(new Color(44, 62, 80));
        
        String specialtyText = "";
        if (doctor.specialty != null && !doctor.specialty.trim().isEmpty()) {
            specialtyText = doctor.specialty;
            if (doctor.subSpecialization != null && !doctor.subSpecialization.trim().isEmpty()) {
                specialtyText += " - " + doctor.subSpecialization;
            }
        }
        JLabel titleLabel = new JLabel(specialtyText, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        titleLabel.setForeground(new Color(127, 140, 141));
        
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(nameLabel, BorderLayout.NORTH);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Photo panel
        JPanel photoPanel = createPhotoPanel();
        
        // Details panel
        JPanel detailsPanel = createDetailsPanel();
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(30, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(photoPanel, BorderLayout.WEST);
        contentPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // Add some padding
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
    
    private JPanel createPhotoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel photoLabel = new JLabel("", JLabel.CENTER);
        photoLabel.setPreferredSize(new Dimension(200, 250));
        
        // Load and display the doctor's photo if available
        if (doctor.photoPath != null && !doctor.photoPath.trim().isEmpty()) {
            ImageIcon roundedIcon = ImageUtils.createRoundImageIcon(doctor.photoPath, 180, 180);
            if (roundedIcon != null) {
                photoLabel.setIcon(roundedIcon);
            } else {
                setDefaultPhotoIcon(photoLabel);
            }
        } else {
            setDefaultPhotoIcon(photoLabel);
        }
        
        panel.add(photoLabel, BorderLayout.CENTER);
        return panel;
    }

    private String toHtml(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        return "<html>" + t.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>") + "</html>";
    }
    
    private void setDefaultPhotoIcon(JLabel label) {
        Icon icon = UIManager.getIcon("FileView.directoryIcon");
        ImageIcon defaultIcon = ImageUtils.resizeImage(
            icon instanceof ImageIcon ? (ImageIcon) icon : new ImageIcon(), 100, 100);
        label.setIcon(defaultIcon);
        label.setText("No Photo");
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
    }
    
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridy = 0;
        
        // Add doctor details
        addDetailRow("Department", doctor.department, panel, gbc);

        addDetailRow("License", doctor.licenseNumber, panel, gbc);
        
        if (doctor.specialty != null && !doctor.specialty.trim().isEmpty()) {
            addDetailRow("Specialization", doctor.specialty, panel, gbc);
            
            if (doctor.subSpecialization != null && !doctor.subSpecialization.trim().isEmpty()) {
                addDetailRow("Sub-specialization", doctor.subSpecialization, panel, gbc);
            }
        }
        
        if (doctor.yearsExperience != null && doctor.yearsExperience > 0) {
            addDetailRow("Experience", doctor.yearsExperience + " years", panel, gbc);
        }
        
        if (doctor.qualifications != null && !doctor.qualifications.trim().isEmpty()) {
            addDetailRow("Qualifications", doctor.qualifications, panel, gbc);
        }

        if (doctor.certifications != null && !doctor.certifications.trim().isEmpty()) {
            addDetailRow("Certifications", toHtml(doctor.certifications), panel, gbc);
        }

        if (doctor.education != null && !doctor.education.trim().isEmpty()) {
            addDetailRow("Education", toHtml(doctor.education), panel, gbc);
        }

        if (doctor.expertise != null && !doctor.expertise.trim().isEmpty()) {
            addDetailRow("Expertise", toHtml(doctor.expertise), panel, gbc);
        }

        if (doctor.skills != null && !doctor.skills.trim().isEmpty()) {
            addDetailRow("Skills", toHtml(doctor.skills), panel, gbc);
        }

        if (doctor.competencies != null && !doctor.competencies.trim().isEmpty()) {
            addDetailRow("Competencies", toHtml(doctor.competencies), panel, gbc);
        }
        
        if (doctor.phone != null && !doctor.phone.trim().isEmpty()) {
            addDetailRow("Contact", doctor.phone, panel, gbc);
        }
        
        if (doctor.email != null && !doctor.email.trim().isEmpty()) {
            addDetailRow("Email", "<html><a href='mailto:" + doctor.email + "'>" + doctor.email + "</a></html>", panel, gbc);
        }
        
        if (doctor.bio != null && !doctor.bio.trim().isEmpty()) {
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            JLabel bioLabel = new JLabel("<html><b>About Dr. " + doctor.name.split(" ")[0] + ":</b><br>" + 
                doctor.bio.replace("\n", "<br>") + "</html>");
            bioLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            bioLabel.setForeground(new Color(44, 62, 80));
            
            gbc.gridy++;
            panel.add(bioLabel, gbc);
        }
        
        return panel;
    }
    
    private void addDetailRow(String label, String value, JPanel panel, GridBagConstraints gbc) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        
        JLabel labelLbl = new JLabel(label + ":");
        labelLbl.setFont(new Font("Arial", Font.BOLD, 13));
        labelLbl.setForeground(new Color(52, 73, 94));
        
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        valueLbl.setForeground(new Color(44, 62, 80));
        
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(labelLbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueLbl, gbc);
        
        gbc.gridy++;
    }
    
    /**
     * Shows a dialog with the doctor's public profile
     * @param parent The parent component (for positioning)
     * @param doctor The doctor whose profile to show
     */
    public static void showDialog(Component parent, Staff doctor) {
        if (doctor == null || doctor.id == null) {
            JOptionPane.showMessageDialog(parent, 
                "Doctor information is not available.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get fresh data from the service
        Staff freshDoctor = DoctorService.getDoctorById(doctor.id);
        if (freshDoctor == null) {
            JOptionPane.showMessageDialog(parent, 
                "Doctor not found or is no longer active.", 
                "Not Found", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(parent),
            "Doctor Profile - Dr. " + freshDoctor.name,
            Dialog.ModalityType.APPLICATION_MODAL
        );
        
        // Create a panel to hold the content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(new DoctorPublicProfilePanel(freshDoctor), BorderLayout.CENTER);
        
        // Add to dialog
        dialog.setContentPane(contentPanel);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(parent);
        
        dialog.setVisible(true);
    }
}
