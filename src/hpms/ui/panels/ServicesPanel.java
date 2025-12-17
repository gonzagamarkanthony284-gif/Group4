package hpms.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class ServicesPanel extends JPanel {
    private JPanel servicesContainer;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JButton refreshBtn;
    private Random random = new Random();
    
    // Department information class
    private static class DepartmentInfo {
        String name;
        String subtitle;
        String description;
        String imageKey;
        
        DepartmentInfo(String name, String subtitle, String description, String imageKey) {
            this.name = name;
            this.subtitle = subtitle;
            this.description = description;
            this.imageKey = imageKey;
        }
    }
    
    public ServicesPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(249, 250, 251));
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createServicesContainer(), BorderLayout.CENTER);
        
        // Initialize services
        createDepartmentCards();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Title
        JLabel titleLabel = new JLabel("Medical Departments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 30, 30));
        panel.add(titleLabel, BorderLayout.WEST);

        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBackground(new Color(249, 250, 251));

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        searchPanel.add(searchLabel);

        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(200, 28));
        searchField.setPreferredSize(new Dimension(200, 28));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchServices();
            }
        });
        searchPanel.add(searchField);

        searchPanel.add(Box.createHorizontalStrut(10));

        JLabel filterLabel = new JLabel("Filter: ");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        searchPanel.add(filterLabel);

        filterCombo = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
        filterCombo.setMaximumSize(new Dimension(100, 28));
        filterCombo.setPreferredSize(new Dimension(100, 28));
        filterCombo.addActionListener(e -> filterServices());
        searchPanel.add(filterCombo);

        // Add refresh button
        refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 11));
        refreshBtn.setBackground(new Color(76, 175, 80));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshServices());
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(refreshBtn);

        panel.add(searchPanel, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane createServicesContainer() {
        servicesContainer = new JPanel();
        servicesContainer.setLayout(new GridBagLayout());
        servicesContainer.setBackground(new Color(249, 250, 251));
        
        JScrollPane scrollPane = new JScrollPane(servicesContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }

    private void createDepartmentCards() {
        // Define department information
        DepartmentInfo[] departments = {
            new DepartmentInfo("Cardiology", "Heart & Vascular Care", 
                "Comprehensive cardiac care including diagnostics, surgery, and rehabilitation. " +
                "Our expert cardiologists provide treatment for heart conditions, " +
                "cardiac catheterization, and preventive cardiology services.", 
                "cardiology"),
            new DepartmentInfo("Emergency Room", "24/7 Emergency Care", 
                "Round-the-clock emergency medical services for trauma, acute illnesses, " +
                "and life-threatening conditions. Staffed by board-certified emergency " +
                "physicians and critical care nurses.", 
                "er"),
            new DepartmentInfo("Neurology", "Brain & Nervous System", 
                "Specialized care for neurological disorders including stroke, epilepsy, " +
                "Parkinson's disease, and multiple sclerosis. Advanced diagnostic and " +
                "treatment options for nervous system conditions.", 
                "neurology"),
            new DepartmentInfo("Orthopedics", "Bone & Joint Care", 
                "Complete orthopedic services including joint replacement, sports medicine, " +
                "fracture care, and spine surgery. Our orthopedic specialists use " +
                "minimally invasive techniques for faster recovery.", 
                "orthopedics"),
            new DepartmentInfo("Pediatrics", "Children's Health", 
                "Comprehensive healthcare for infants, children, and adolescents. " +
                "From routine check-ups to specialized pediatric care, we provide " +
                "child-friendly medical services in a nurturing environment.", 
                "pediatrics")
        };
        
        for (DepartmentInfo dept : departments) {
            servicesContainer.add(createInformativeDepartmentCard(dept));
        }
    }
    
    private JPanel createInformativeDepartmentCard(DepartmentInfo dept) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(350, 200));
        card.setPreferredSize(new Dimension(350, 200));
        
        // Left side - Image
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(150, 150));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Load department image
        ImageIcon icon = loadDepartmentImage(dept.imageKey);
        if (icon != null) {
            imageLabel.setIcon(icon);
        } else {
            // Fallback to a colored placeholder
            imageLabel.setOpaque(true);
            imageLabel.setBackground(getDepartmentColor(dept.imageKey));
            imageLabel.setText("No Image");
            imageLabel.setForeground(Color.WHITE);
            imageLabel.setFont(new Font("Arial", Font.BOLD, 14));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        }
        
        // Right side - Information
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setOpaque(true);
        
        JLabel nameLabel = new JLabel(dept.name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(new Color(30, 30, 30));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel(dept.subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Description with word wrap
        JTextArea descriptionArea = new JTextArea(dept.description);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 11));
        descriptionArea.setForeground(new Color(60, 60, 60));
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setOpaque(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(subtitleLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(descriptionArea);
        
        card.add(imageLabel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Add hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 210)),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return card;
    }
    
    private ImageIcon loadDepartmentImage(String department) {
        // Map department names to image files
        String deptLower = department.toLowerCase().trim();
        String imagePath = null;
        
        if (deptLower.contains("cardio")) {
            imagePath = getRandomImage("cardiology");
        } else if (deptLower.contains("emergency") || deptLower.contains("er")) {
            imagePath = getRandomImage("er");
        } else if (deptLower.contains("neuro")) {
            imagePath = getRandomImage("neurology");
        } else if (deptLower.contains("ortho")) {
            imagePath = getRandomImage("orthopedics");
        } else if (deptLower.contains("pediatric")) {
            imagePath = getRandomImage("pediatrics");
        }
        
        if (imagePath != null) {
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
                // Scale image to fit the label size
                Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } catch (IOException e) {
                System.err.println("Could not load image: " + imagePath + " - " + e.getMessage());
            }
        }
        
        return null;
    }
    
    private String getRandomImage(String department) {
        // Get the project root directory
        String projectRoot = System.getProperty("user.dir");
        String resourcesPath = projectRoot + File.separator + "resources";
        
        // List of available images for each department
        String[] cardiologyImages = {"cardiology1.jpg", "cardiology2.jpg", "cardiology3.jpg", "cardiology4.jpg"};
        String[] erImages = {"er1.jpg", "er2.jpg", "er3.jpg", "er4.jpg"};
        String[] neurologyImages = {"neurology1.jpg", "neurology2.jpg", "neurology3.jpg", "neurology4.jpg"};
        String[] orthopedicsImages = {"orthopedics1.jpg", "orthopedics2.jpg", "orthopedics3.jpg", "orthopedics4.jpg"};
        String[] pediatricsImages = {"pediatrics1.jpg", "pediatrics2.jpg", "pediatrics3.jpg", "pediatrics4.jpg"};
        
        String[] images = null;
        switch (department.toLowerCase()) {
            case "cardiology":
                images = cardiologyImages;
                break;
            case "er":
                images = erImages;
                break;
            case "neurology":
                images = neurologyImages;
                break;
            case "orthopedics":
                images = orthopedicsImages;
                break;
            case "pediatrics":
                images = pediatricsImages;
                break;
        }
        
        if (images != null && images.length > 0) {
            String selectedImage = images[random.nextInt(images.length)];
            return resourcesPath + File.separator + selectedImage;
        }
        
        return null;
    }
    
    private Color getDepartmentColor(String department) {
        switch (department.toLowerCase()) {
            case "cardiology":
                return new Color(220, 53, 69); // Red
            case "er":
                return new Color(255, 193, 7); // Yellow
            case "neurology":
                return new Color(13, 110, 253); // Blue
            case "orthopedics":
                return new Color(25, 135, 84); // Green
            case "pediatrics":
                return new Color(255, 127, 80); // Orange
            default:
                return new Color(108, 117, 125); // Gray
        }
    }
    
    private void searchServices() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        // Implementation for search functionality
        System.out.println("Searching for: " + searchTerm);
    }
    
    private void filterServices() {
        String filter = (String) filterCombo.getSelectedItem();
        // Implementation for filter functionality
        System.out.println("Filter: " + filter);
    }
    
    private void refreshServices() {
        servicesContainer.removeAll();
        createDepartmentCards();
        servicesContainer.revalidate();
        servicesContainer.repaint();
    }
}
