package hpms.ui.login;

import hpms.model.Service;
import hpms.service.ServiceService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ServicesWindow extends JFrame {
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
        servicesGridPanel.setLayout(new GridLayout(2, 3, 15, 15));
        servicesGridPanel.setBackground(bg);

        List<Service> services = ServiceService.getAllServices();
        for (Service service : services) {
            servicesGridPanel.add(createServiceCard(service, accentColor));
        }

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

    private JPanel createServiceCard(Service service, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(20, 15, 20, 15)
        ));

        // Service Icon/Badge
        JLabel badgeLabel = new JLabel("●");
        badgeLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        badgeLabel.setForeground(accentColor);
        badgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(badgeLabel);

        card.add(Box.createVerticalStrut(10));

        // Service Name
        JLabel nameLabel = new JLabel(service.serviceName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(30, 30, 30));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(nameLabel);

        card.add(Box.createVerticalStrut(8));

        // Service Description
        JLabel descLabel = new JLabel("<html><center>" + service.description + "</center></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setForeground(new Color(107, 114, 128));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setMaximumSize(new java.awt.Dimension(200, 80));
        card.add(descLabel);

        card.add(Box.createVerticalStrut(12));

        // Bed Information
        JLabel bedsLabel = new JLabel("Available Beds: " + service.availableBeds + " / " + service.totalBeds);
        bedsLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        bedsLabel.setForeground(new Color(59, 130, 246));
        bedsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(bedsLabel);

        card.add(Box.createVerticalStrut(8));

        // Status Badge
        JLabel statusLabel = new JLabel(service.status);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 10));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBackground("ACTIVE".equals(service.status) ? new Color(16, 185, 129) : new Color(107, 114, 128));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setMaximumSize(new java.awt.Dimension(80, 24));
        card.add(statusLabel);

        card.add(Box.createVerticalGlue());

        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServiceService.initializeDefaultServices();
            new ServicesWindow().setVisible(true);
        });
    }
}
