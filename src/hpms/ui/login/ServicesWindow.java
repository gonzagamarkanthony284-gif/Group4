package hpms.ui.login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class ServicesWindow extends JFrame {
    private Random random = new Random();
    public ServicesWindow() {
        setTitle("Hospital Services | HPMS");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        Color bg = new Color(249, 250, 251);
        Color primaryColor = new Color(30, 64, 175);
        Color accentColor = new Color(59, 130, 246);

        getContentPane().setBackground(bg);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Our Hospital Services");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Comprehensive Healthcare Services Available 24/7");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 220, 240));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Content Panel with services grid
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(bg);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Services grid
        JPanel servicesGridPanel = new JPanel();
        servicesGridPanel.setLayout(new GridLayout(0, 2, 15, 15)); // 2 columns, dynamic rows
        servicesGridPanel.setBackground(bg);

        // Create informative department cards instead of service cards
        createDepartmentCards(servicesGridPanel, accentColor);

        contentPanel.add(servicesGridPanel);
        contentPanel.add(Box.createVerticalGlue());

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(bg);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.X_AXIS));
        footerPanel.setBackground(new Color(240, 244, 248));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel infoLabel = new JLabel("For more information, please contact our reception or login to the system.");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(107, 114, 128));
        footerPanel.add(infoLabel);

        footerPanel.add(Box.createHorizontalGlue());

        JButton backBtn = new JButton("Back to Login");
        backBtn.setFont(new Font("Arial", Font.BOLD, 11));
        backBtn.setBackground(primaryColor);
        backBtn.setForeground(Color.WHITE);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        backBtn.addActionListener(e -> dispose());
        footerPanel.add(backBtn);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void createDepartmentCards(JPanel gridPanel, Color accentColor) {
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
            gridPanel.add(createInformativeDepartmentCard(dept, accentColor));
        }
    }
    private JPanel createInformativeDepartmentCard(DepartmentInfo dept, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        card.setPreferredSize(new Dimension(380, 220));
        
        // Top panel - Department image
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(380, 120));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Load actual image from resources folder
        Icon deptIcon = getServiceIcon(dept.imageKey);
        if (deptIcon != null) {
            imageLabel.setIcon(deptIcon);
            imageLabel.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        } else {
            // Fallback to colored circle
            JLabel badgeLabel = new JLabel("●");
            badgeLabel.setFont(new Font("Arial", Font.PLAIN, 40));
            badgeLabel.setForeground(accentColor);
            badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            badgeLabel.setVerticalAlignment(SwingConstants.CENTER);
            imagePanel.add(badgeLabel, BorderLayout.CENTER);
        }
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // Bottom panel - Department information
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Department name and subtitle
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(dept.name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(new Color(30, 30, 30));
        titlePanel.add(nameLabel, BorderLayout.NORTH);
        
        JLabel subtitleLabel = new JLabel(dept.subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitleLabel.setForeground(accentColor);
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);
        
        infoPanel.add(titlePanel, BorderLayout.NORTH);

        // Description
        JLabel descLabel = new JLabel("<html><div style='text-align: left; padding: 5px;'>" + dept.description + "</div></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setForeground(new Color(107, 114, 128));
        infoPanel.add(descLabel, BorderLayout.CENTER);

        // Combine image and info panels
        card.add(imagePanel, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }
    
    // Department information helper class
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
    
    private Icon getServiceIcon(String department) {
        if (department == null || department.trim().isEmpty()) {
            return null;
        }
        
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
                Image scaledImage = image.getScaledInstance(380, 120, Image.SCALE_SMOOTH);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServicesWindow().setVisible(true);
        });
    }
}
