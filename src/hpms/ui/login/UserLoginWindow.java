package hpms.ui.login;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class UserLoginWindow extends JFrame {
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JCheckBox rememberMeCheck = new JCheckBox("Remember me");
    private final JButton loginButton = new JButton("Login");
    private final JButton backButton = new JButton("Back to Login");
    private char defaultEcho;

    public UserLoginWindow() {
        setTitle("MediCare Hospital - User Login");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // Main container with horizontal split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(0);
        splitPane.setEnabled(false);

        // Left Panel - Hospital Information
        JPanel leftPanel = createHospitalInfoPanel();

        // Right Panel - Login Form
        JPanel rightPanel = createLoginPanel();

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(550);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createHospitalInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(37, 99, 235));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Logo Section
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(new Color(37, 99, 235));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoLabel = new JLabel("⚕");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(logoLabel);

        JLabel hospitalName = new JLabel("MediCare Hospital");
        hospitalName.setFont(new Font("Segoe UI", Font.BOLD, 32));
        hospitalName.setForeground(Color.WHITE);
        hospitalName.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(hospitalName);

        JLabel tagline = new JLabel("Your Health, Our Priority");
        tagline.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        tagline.setForeground(new Color(200, 220, 255));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(tagline);

        panel.add(logoPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(createSeparator());
        panel.add(Box.createVerticalStrut(20));

        // About Section
        JPanel aboutPanel = createInfoBox("About Our Hospital",
                "MediCare Hospital is a leading healthcare institution committed to providing " +
                        "exceptional medical care with compassion and excellence. With state-of-the-art " +
                        "facilities and a team of experienced healthcare professionals, we offer " +
                        "comprehensive medical services to meet all your healthcare needs.\n\n" +
                        "Our hospital is equipped with advanced medical technology and maintains the " +
                        "highest standards of patient care, safety, and comfort.");
        panel.add(aboutPanel);
        panel.add(Box.createVerticalStrut(15));

        // Services Section
        JPanel servicesPanel = new hpms.ui.panels.ServicesPanel();
        panel.add(servicesPanel);
        panel.add(Box.createVerticalStrut(15));

        // Contact Info
        JPanel contactPanel = createContactPanel();
        panel.add(contactPanel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Center the login card
        panel.add(Box.createVerticalGlue());

        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)));
        loginCard.setMaximumSize(new Dimension(400, 600));

        // Header
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(30, 41, 59));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.add(welcomeLabel);

        JLabel subtitleLabel = new JLabel("Sign in to access your account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.add(subtitleLabel);
        loginCard.add(Box.createVerticalStrut(25));

        // Email Field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emailLabel.setForeground(new Color(51, 65, 85));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(emailLabel);
        loginCard.add(Box.createVerticalStrut(6));

        emailField.setMaximumSize(new Dimension(400, 40));
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        loginCard.add(emailField);
        loginCard.add(Box.createVerticalStrut(15));

        // Password Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passwordLabel.setForeground(new Color(51, 65, 85));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(passwordLabel);
        loginCard.add(Box.createVerticalStrut(6));

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setMaximumSize(new Dimension(400, 40));
        passwordPanel.setBackground(Color.WHITE);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JButton togglePassword = new JButton("👁");
        togglePassword.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        togglePassword.setBackground(Color.WHITE);
        togglePassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        togglePassword.setFocusPainted(false);
        defaultEcho = passwordField.getEchoChar();
        togglePassword.addActionListener(e -> {
            if (passwordField.getEchoChar() == defaultEcho) {
                passwordField.setEchoChar((char) 0);
                togglePassword.setText("🙈");
            } else {
                passwordField.setEchoChar(defaultEcho);
                togglePassword.setText("👁");
            }
        });

        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(togglePassword, BorderLayout.EAST);
        loginCard.add(passwordPanel);
        loginCard.add(Box.createVerticalStrut(12));

        // Remember me and Forgot Password
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setBackground(Color.WHITE);
        optionsPanel.setMaximumSize(new Dimension(400, 25));

        rememberMeCheck.setBackground(Color.WHITE);
        rememberMeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeCheck.setForeground(new Color(100, 116, 139));

        JLabel forgotPassword = new JLabel("<html><u>Forgot Password?</u></html>");
        forgotPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPassword.setForeground(new Color(37, 99, 235));
        forgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPassword.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showPasswordResetDialog();
            }
        });

        optionsPanel.add(rememberMeCheck, BorderLayout.WEST);
        optionsPanel.add(forgotPassword, BorderLayout.EAST);
        loginCard.add(optionsPanel);
        loginCard.add(Box.createVerticalStrut(20));

        // Login Button
        loginButton.setMaximumSize(new Dimension(400, 42));
        loginButton.setBackground(new Color(37, 99, 235));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> handleLogin());
        loginCard.add(loginButton);
        loginCard.add(Box.createVerticalStrut(20));

        // Info Note
        JPanel notePanel = createNotePanel();
        loginCard.add(notePanel);
        loginCard.add(Box.createVerticalStrut(15));

        // Help Section
        JPanel helpPanel = createHelpPanel();
        loginCard.add(helpPanel);
        loginCard.add(Box.createVerticalStrut(15));

        // Back Button
        backButton.setMaximumSize(new Dimension(400, 38));
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(new Color(37, 99, 235));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(37, 99, 235), 2),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> dispose());
        loginCard.add(backButton);

        panel.add(loginCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createInfoBox(String title, String content) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(59, 130, 246, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        JTextArea textArea = new JTextArea(content);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textArea.setForeground(new Color(230, 240, 255));
        textArea.setBackground(new Color(0, 0, 0, 0));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        panel.add(textArea);

        return panel;
    }

            private JPanel createContactPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(59, 130, 246, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] contacts = {
                "📞 24/7 Emergency: +1 (555) 123-4567",
                "📧 Email: info@medicarehospital.com",
                "📍 Address: 123 Medical Center Blvd, Healthcare City"
        };

        for (String contact : contacts) {
            JLabel label = new JLabel(contact);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(Color.WHITE);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(label);
            panel.add(Box.createVerticalStrut(6));
        }

        return panel;
    }

    private JPanel createNotePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(239, 246, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(37, 99, 235), 0, false),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        panel.setMaximumSize(new Dimension(400, 150));

        JLabel titleLabel = new JLabel("ℹ️ Note for Healthcare Professionals:");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(30, 64, 175));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));

        JTextArea textArea = new JTextArea(
                "Doctor accounts are created by the hospital administrator. " +
                        "If you're a doctor and haven't received your login credentials, " +
                        "please contact the admin department. Your username and password " +
                        "will be sent to your registered email address.");
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        textArea.setForeground(new Color(71, 85, 105));
        textArea.setBackground(new Color(239, 246, 255));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        panel.add(textArea);

        return panel;
    }

    private JPanel createHelpPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.setMaximumSize(new Dimension(400, 100));

        JLabel titleLabel = new JLabel("Need assistance? Contact IT Support:");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(51, 65, 85));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(6));

        JLabel emailLabel = new JLabel("📧 support@medicarehospital.com");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        emailLabel.setForeground(new Color(100, 116, 139));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(emailLabel);

        JLabel phoneLabel = new JLabel("📞 +1 (555) 123-4500 (ext. 101)");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        phoneLabel.setForeground(new Color(100, 116, 139));
        phoneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(phoneLabel);

        return panel;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 100));
        separator.setMaximumSize(new Dimension(500, 2));
        return separator;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both email and password.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Invalid Email", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mock authentication - Replace with actual authentication logic
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        Timer timer = new Timer(1000, e -> {
            loginButton.setEnabled(true);
            loginButton.setText("Login");

            JOptionPane.showMessageDialog(this,
                    "Login functionality will be connected to the authentication system.\n" +
                            "Please use the main login window for system access.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showPasswordResetDialog() {
        JDialog resetDialog = new JDialog(this, "Reset Password", true);
        resetDialog.setSize(400, 300);
        resetDialog.setLocationRelativeTo(this);
        resetDialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(20);
        formPanel.add(newPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton resetButton = new JButton("Reset Password");
        JButton cancelButton = new JButton("Cancel");
        
        resetButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(resetDialog, "Please enter username", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(resetDialog, "Please enter new password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(resetDialog, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(resetDialog, "Password must be at least 6 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Reset password
            boolean success = resetUserPassword(username, newPassword);
            if (success) {
                JOptionPane.showMessageDialog(resetDialog, "Password reset successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                resetDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(resetDialog, "Username not found. Please check your username.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> resetDialog.dispose());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        
        resetDialog.add(formPanel, BorderLayout.CENTER);
        resetDialog.add(buttonPanel, BorderLayout.SOUTH);
        resetDialog.setVisible(true);
    }
    
    private boolean resetUserPassword(String username, String newPassword) {
        try {
            // Check if user exists in DataStore
            hpms.auth.User user = hpms.util.DataStore.users.get(username);
            if (user == null) {
                return false;
            }
            
            // Generate new salt and hash
            String salt = hpms.auth.PasswordUtil.generateSalt();
            String hashedPassword = hpms.auth.PasswordUtil.hash(newPassword, salt);
            
            // Update user password
            hpms.auth.User updatedUser = new hpms.auth.User(username, hashedPassword, salt, user.role);
            updatedUser.status = user.status;
            updatedUser.displayPassword = null;
            
            hpms.util.DataStore.users.put(username, updatedUser);
            
            // Also update in database if available
            try (java.sql.Connection conn = hpms.util.DBConnection.getConnection()) {
                if (conn != null) {
                    String updateSql = "UPDATE users SET password = ?, salt = ? WHERE username = ?";
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.setString(1, hashedPassword);
                        stmt.setString(2, salt);
                        stmt.setString(3, username);
                        stmt.executeUpdate();
                    }
                }
            } catch (Exception e) {
                // Database update failed, but DataStore update succeeded
                System.out.println("Database update failed: " + e.getMessage());
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Password reset error: " + e.getMessage());
            return false;
        }
    }
}
