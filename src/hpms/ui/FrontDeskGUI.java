package hpms.ui;

import hpms.auth.AuthSession;
import hpms.ui.frontdesk.FrontDeskDashboardPanel;
import hpms.ui.components.Theme;

import javax.swing.*;
import java.awt.*;

public class FrontDeskGUI extends JFrame {
    
    public FrontDeskGUI(AuthSession session) {
        // Apply global UI theme
        Theme.applyGlobalUI();
        setTitle("Hospital Patient Management System - Front Desk");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        // Create header with user info
        JPanel headerPanel = createHeaderPanel(session);
        add(headerPanel, BorderLayout.NORTH);

        // Add front desk dashboard
        FrontDeskDashboardPanel dashboardPanel = new FrontDeskDashboardPanel(session);
        add(dashboardPanel, BorderLayout.CENTER);

        // Create footer
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel(AuthSession session) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        header.setPreferredSize(new Dimension(getWidth(), 60));

        JLabel titleLabel = new JLabel("Front Desk Operations");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Logged in as: " + session.username + " (" + session.role + ")");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(Color.WHITE);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Theme.BACKGROUND);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                // Return to login screen
                SwingUtilities.invokeLater(() -> {
                    try {
                        Class<?> loginClass = Class.forName("hpms.ui.LoginWindow");
                        Object loginInstance = loginClass.getDeclaredConstructor().newInstance();
                        if (loginInstance instanceof JFrame) {
                            ((JFrame) loginInstance).setVisible(true);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, 
                            "Error returning to login: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                });
            }
        });

        footer.add(logoutButton);
        return footer;
    }
}
