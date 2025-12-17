package hpms.ui.doctor;

import hpms.model.Staff;
import hpms.util.DataStore;
import hpms.util.ImageUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for browsing and selecting doctors.
 * Shows a grid of doctor cards with photos and basic info.
 */
public class DoctorListDialog extends JDialog {
    
    public DoctorListDialog(Window owner) {
        super(owner, "Our Doctors", Dialog.ModalityType.APPLICATION_MODAL);
        
        setLayout(new BorderLayout(10, 10));
        setSize(900, 700);
        setLocationRelativeTo(owner);
        
        // Header
        JLabel headerLabel = new JLabel("Our Medical Team", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        headerLabel.setForeground(new Color(44, 62, 80));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        
        JTextField searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setMargin(new Insets(5, 10, 5, 10));
        
        JButton searchButton = new JButton("Search");
        styleButton(searchButton, new Color(52, 152, 219));
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Doctors panel - using JPanel with FlowLayout for responsive grid
        JPanel doctorsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 20));
        doctorsPanel.setBackground(new Color(245, 247, 250));
        
        // Get all doctors
        List<Staff> doctors = DataStore.staff.values().stream()
            .filter(staff -> staff.role != null && staff.role.name().equals("DOCTOR") && staff.isActive)
            .collect(Collectors.toList());
        
        // Create a card for each doctor
        for (Staff doctor : doctors) {
            doctorsPanel.add(createDoctorCard(doctor));
        }
        
        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(doctorsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Add components to dialog
        add(headerLabel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        
        // Search functionality
        searchButton.addActionListener(e -> filterDoctors(searchField.getText().trim(), doctorsPanel, doctors));
        searchField.addActionListener(e -> filterDoctors(searchField.getText().trim(), doctorsPanel, doctors));
    }
    
    private JPanel createDoctorCard(Staff doctor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(250, 350));
        
        // Make card interactive
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DoctorPublicProfilePanel.showDialog(DoctorListDialog.this, doctor);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(245, 248, 250));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });
        
        // Photo
        JLabel photoLabel = new JLabel("", JLabel.CENTER);
        photoLabel.setPreferredSize(new Dimension(180, 180));
        
        if (doctor.photoPath != null && !doctor.photoPath.trim().isEmpty()) {
            ImageIcon roundedIcon = ImageUtils.createRoundImageIcon(doctor.photoPath, 150, 150);
            if (roundedIcon != null) {
                photoLabel.setIcon(roundedIcon);
            } else {
                setDefaultPhotoIcon(photoLabel);
            }
        } else {
            setDefaultPhotoIcon(photoLabel);
        }
        
        // Doctor info
        JLabel nameLabel = new JLabel("Dr. " + doctor.name, JLabel.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(44, 62, 80));
        
        JLabel specLabel = new JLabel(doctor.specialty != null ? doctor.specialty : "", JLabel.CENTER);
        specLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        specLabel.setForeground(new Color(127, 140, 141));
        
        JLabel deptLabel = new JLabel(doctor.department != null ? doctor.department : "", JLabel.CENTER);
        deptLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        deptLabel.setForeground(new Color(149, 165, 166));
        
        JButton viewProfileBtn = new JButton("View Profile");
        styleButton(viewProfileBtn, new Color(46, 204, 113));
        viewProfileBtn.addActionListener(e -> 
            DoctorPublicProfilePanel.showDialog(this, doctor)
        );
        
        // Layout
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel);
        infoPanel.add(specLabel);
        infoPanel.add(deptLabel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewProfileBtn);
        
        card.add(photoLabel, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private void setDefaultPhotoIcon(JLabel label) {
        Icon icon = UIManager.getIcon("FileView.directoryIcon");
        Image image = icon instanceof ImageIcon ? ((ImageIcon)icon).getImage() : 
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ImageIcon defaultIcon = ImageUtils.resizeImage(new ImageIcon(image), 100, 100);
        label.setIcon(defaultIcon);
        label.setText("No Photo");
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
    }
    
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(darkenColor(bgColor, 0.9));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }
    
    private Color darkenColor(Color color, double factor) {
        return new Color(
            Math.max((int)(color.getRed() * factor), 0),
            Math.max((int)(color.getGreen() * factor), 0),
            Math.max((int)(color.getBlue() * factor), 0)
        );
    }
    
    private void filterDoctors(String query, JPanel container, List<Staff> allDoctors) {
        container.removeAll();
        
        List<Staff> filtered = allDoctors;
        
        if (query != null && !query.isEmpty()) {
            String lowerQuery = query.toLowerCase();
            filtered = allDoctors.stream()
                .filter(doctor -> 
                    doctor.name.toLowerCase().contains(lowerQuery) ||
                    (doctor.specialty != null && doctor.specialty.toLowerCase().contains(lowerQuery)) ||
                    (doctor.department != null && doctor.department.toLowerCase().contains(lowerQuery))
                )
                .collect(Collectors.toList());
        }
        
        if (filtered.isEmpty()) {
            JLabel noResults = new JLabel("No doctors found matching '\"" + query + "\"'", JLabel.CENTER);
            noResults.setFont(new Font("Arial", Font.ITALIC, 14));
            noResults.setForeground(new Color(127, 140, 141));
            container.add(noResults);
        } else {
            for (Staff doctor : filtered) {
                container.add(createDoctorCard(doctor));
            }
        }
        
        container.revalidate();
        container.repaint();
    }
    
    /**
     * Shows the doctor list dialog
     * @param parent The parent component (for positioning)
     */
    public static void showDialog(Component parent) {
        DoctorListDialog dialog = new DoctorListDialog(SwingUtilities.getWindowAncestor(parent));
        dialog.setVisible(true);
    }
    
    }
