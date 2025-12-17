package hpms.ui.login;

import javax.swing.*;
import hpms.model.RoomStatus;
import hpms.model.Room;
import hpms.util.DataStore;
import hpms.util.IDGenerator;
import hpms.auth.AuthService;
import java.awt.*;
import java.util.List;

public class LoginWindow extends JFrame {
    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JButton loginBtn = new JButton("Login");
    private final JButton servicesBtn = new JButton("View Services");
    private final JCheckBox showPass = new JCheckBox("Show Password");
    private char defaultEcho = (char) 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Disabled backup loading to use database instead
                // BackupUtil.loadFromDefault();
            } catch (Exception ex) {
            }
            try {
                // Check and clear expired clinic schedules after loading data
                hpms.service.StaffService.checkAndClearExpiredSchedules();
            } catch (Exception ex) {
            }
            try {
                AuthService.migratePasswordsIfMissing();
            } catch (Exception ex) {
            }
            try {
                hpms.service.PatientService.migrateRegistrationTypeDefault();
            } catch (Exception ex) {
            }
            if (!hpms.util.DataStore.users.containsKey("admin")) {
                AuthService.seedAdmin();
            }
            seedRooms();
            seedSampleAppointments();
            // Load staff from database
            try { hpms.service.StaffService.loadFromDatabase(); } catch (Exception ex) { }
            // Load patients from database
            try { hpms.service.PatientService.loadFromDatabase(); } catch (Exception ex) { }
            // Load notes/alerts from database (critical alerts + staff notes)
            try { hpms.service.CommunicationService.loadNotesAndAlertsFromDatabase(); } catch (Exception ex) { }
            // Initialize patient status table and load data
            try { hpms.service.DatabaseInitializer.initializePatientStatusTable(); } catch (Exception ex) { }
            try { hpms.service.PatientStatusService.loadFromDatabase(); } catch (Exception ex) { }
            // Load appointments from database
            try { hpms.service.AppointmentService.loadFromDatabase(); } catch (Exception ex) { }
            // Load bills from database
            try { hpms.service.BillingService.loadFromDatabase(); } catch (Exception ex) { }
            new LoginWindow().setVisible(true);
        });
    }

    public LoginWindow() {
        setTitle("Hospital Patient Management System - Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(720, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        Color bg = Color.WHITE, fg = new Color(30, 30, 30), accent = new Color(60, 120, 200);
        getContentPane().setBackground(bg);

        JPanel left = new JPanel(new GridBagLayout());
        left.setPreferredSize(new Dimension(280, getHeight()));
        left.setBackground(accent);

        GridBagConstraints lc = new GridBagConstraints();
        lc.insets = new Insets(8, 8, 8, 8);

        JLabel brand = new JLabel("HPMS");
        brand.setFont(brand.getFont().deriveFont(36f));
        brand.setForeground(Color.WHITE);
        lc.gridx = 0;
        lc.gridy = 0;
        left.add(brand, lc);

        JLabel tagline = new JLabel("Secure, Fast, Reliable");
        tagline.setForeground(new Color(200, 220, 240));
        lc.gridy = 1;
        left.add(tagline, lc);

        JLabel subtitle = new JLabel("Hospital Patient");
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        subtitle.setForeground(new Color(200, 220, 240));
        lc.gridy = 2;
        left.add(subtitle, lc);

        JLabel subtitle2 = new JLabel("Management System");
        subtitle2.setFont(subtitle2.getFont().deriveFont(12f));
        subtitle2.setForeground(new Color(200, 220, 240));
        lc.gridy = 3;
        left.add(subtitle2, lc);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(bg);
        right.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 210)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(350, 380));

        JLabel title = new JLabel("Login");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setForeground(fg);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        JLabel uLabel = new JLabel("Username");
        uLabel.setForeground(fg);
        uLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(uLabel);

        userField.setAlignmentX(Component.LEFT_ALIGNMENT);
        userField.setBackground(Color.WHITE);
        userField.setForeground(fg);
        userField.setCaretColor(fg);
        sizeInput(userField);
        card.add(userField);
        card.add(Box.createVerticalStrut(8));

        JLabel pLabel = new JLabel("Password");
        pLabel.setForeground(fg);
        pLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(pLabel);

        passField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passField.setBackground(Color.WHITE);
        passField.setForeground(fg);
        passField.setCaretColor(fg);
        sizeInput(passField);
        card.add(passField);

        showPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPass.setForeground(new Color(90, 90, 90));
        showPass.setBackground(Color.WHITE);
        card.add(showPass);
        card.add(Box.createVerticalStrut(12));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(Color.WHITE);

        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        stylePrimary(loginBtn, accent, Color.WHITE);
        loginBtn.setMaximumSize(new Dimension(100, 32));
        loginBtn.setPreferredSize(new Dimension(100, 32));
        buttonPanel.add(loginBtn);

        buttonPanel.add(Box.createHorizontalStrut(6));

        servicesBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        styleSecondary(servicesBtn, Color.WHITE, new Color(34, 197, 94));
        servicesBtn.setMaximumSize(new Dimension(120, 32));
        servicesBtn.setPreferredSize(new Dimension(120, 32));
        buttonPanel.add(servicesBtn);

        card.add(buttonPanel);
        card.add(Box.createVerticalStrut(8));
        
        // Forgot Password link
        JButton forgotPasswordBtn = new JButton("Forgot Password?");
        forgotPasswordBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgotPasswordBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        forgotPasswordBtn.setContentAreaFilled(false);
        forgotPasswordBtn.setForeground(new Color(60, 120, 200));
        forgotPasswordBtn.setFont(forgotPasswordBtn.getFont().deriveFont(11f));
        forgotPasswordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordBtn.addActionListener(e -> openForgotPasswordDialog());
        card.add(forgotPasswordBtn);

        right.add(Box.createVerticalGlue());
        right.add(card);
        right.add(Box.createVerticalGlue());

        setLayout(new BorderLayout());
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);

        defaultEcho = passField.getEchoChar();
        showPass.addActionListener(e -> passField.setEchoChar(showPass.isSelected() ? (char) 0 : defaultEcho));
        loginBtn.addActionListener(e -> doLogin());
        servicesBtn.addActionListener(e -> openServices());
        getRootPane().setDefaultButton(loginBtn);
    }

    private void doLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> out = AuthService.login(username, password);

        if (!out.isEmpty() && out.get(0).startsWith("Login successful")) {
            hpms.auth.User u = hpms.auth.AuthService.current;
            if (u != null && u.role == hpms.model.UserRole.PATIENT) {
                hpms.model.Patient patient = DataStore.patients.get(u.username);
                if (patient != null) {
                    new hpms.ui.patient.PatientDashboardWindow(patient);
                    dispose();
                    return;
                }
                JOptionPane.showMessageDialog(this, "Patient record not found", "Error", JOptionPane.ERROR_MESSAGE);
                passField.setText("");
                return;
            }
            // Route users to MainGUI (handles all roles)
            hpms.ui.MainGUI mainGui = new hpms.ui.MainGUI();
            mainGui.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, String.join("\n", out), "Login Failed", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
        }
    }

    private void openForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, "Reset Password", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Username field
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        formPanel.add(emailField, gbc);
        
        // Buttons
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton resetBtn = new JButton("Reset Password");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(resetBtn);
        buttonPanel.add(cancelBtn);
        formPanel.add(buttonPanel, gbc);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        resetBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            
            if (username.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter username and email", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Reset password logic
            List<String> result = AuthService.resetPassword(username, email);
            if (result.isEmpty() || result.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(dialog, 
                    result.isEmpty() ? "Password reset failed" : result.get(0), 
                    "Reset Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Password reset instructions have been sent to your email.\n" + result.get(0), 
                    "Reset Successful", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }

    private void openServices() {
        hpms.service.ServiceService.initializeDefaultServices();
        ServicesWindow servicesWindow = new ServicesWindow();
        servicesWindow.setVisible(true);
    }

    public static void seedRooms() {
        if (!DataStore.rooms.isEmpty())
            return;
        for (int i = 0; i < 10; i++) {
            String id = IDGenerator.nextId("R");
            DataStore.rooms.put(id, new Room(id, RoomStatus.VACANT, null));
        }
    }

    public static void seedSampleAppointments() {
        if (!DataStore.appointments.isEmpty())
            return;
        
        // Create sample patients first
        seedSamplePatients();
        seedSampleStaff();
        
        // Create sample appointments for testing different role views
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        // Sample appointment for today
        String appt1Id = IDGenerator.nextId("A");
        DataStore.appointments.put(appt1Id, 
            new hpms.model.Appointment(appt1Id, "1001", "2001", 
                now.withHour(10).withMinute(0), "Cardiology", now.minusDays(1)));
        
        // Sample appointment for tomorrow
        String appt2Id = IDGenerator.nextId("A");
        DataStore.appointments.put(appt2Id, 
            new hpms.model.Appointment(appt2Id, "1002", "2002", 
                now.plusDays(1).withHour(14).withMinute(30), "Neurology", now.minusDays(2)));
        
        // Sample pending appointment
        String appt3Id = IDGenerator.nextId("A");
        hpms.model.Appointment pendingAppt = new hpms.model.Appointment(appt3Id, "1003", "2001", 
            now.plusDays(2).withHour(9).withMinute(0), "Orthopedics", now.minusDays(3));
        pendingAppt.notes = "Pending confirmation";
        DataStore.appointments.put(appt3Id, pendingAppt);
    }

    private static void seedSamplePatients() {
        if (!DataStore.patients.isEmpty())
            return;
            
        // Create sample patients
        hpms.model.Patient patient1 = new hpms.model.Patient("1001", "John Doe", 45, "1980-01-15", 
            hpms.model.Gender.Male, "john@example.com", "123 Main St", java.time.LocalDateTime.now());
        DataStore.patients.put("1001", patient1);
        
        hpms.model.Patient patient2 = new hpms.model.Patient("1002", "Jane Smith", 32, "1993-05-22", 
            hpms.model.Gender.Female, "jane@example.com", "456 Oak Ave", java.time.LocalDateTime.now());
        DataStore.patients.put("1002", patient2);
        
        hpms.model.Patient patient3 = new hpms.model.Patient("1003", "Bob Johnson", 28, "1997-09-10", 
            hpms.model.Gender.Male, "bob@example.com", "789 Pine Rd", java.time.LocalDateTime.now());
        DataStore.patients.put("1003", patient3);
    }

    private static void seedSampleStaff() {
        if (!DataStore.staff.isEmpty())
            return;
            
        // Create sample doctors
        hpms.model.Staff doctor1 = new hpms.model.Staff("2001", "Dr. Alice Wilson", hpms.model.StaffRole.DOCTOR, 
            "Cardiology", java.time.LocalDateTime.now());
        DataStore.staff.put("2001", doctor1);
        
        hpms.model.Staff doctor2 = new hpms.model.Staff("2002", "Dr. Bob Brown", hpms.model.StaffRole.DOCTOR, 
            "Neurology", java.time.LocalDateTime.now());
        DataStore.staff.put("2002", doctor2);
        
        // Create corresponding user accounts for doctors (username = staffId)
        createDoctorUserAccount("2001", "doctor123");
        createDoctorUserAccount("2002", "doctor123");
    }
    
    private static void createDoctorUserAccount(String username, String password) {
        // Only create if user doesn't already exist
        if (!DataStore.users.containsKey(username)) {
            String salt = hpms.auth.PasswordUtil.generateSalt();
            String hashedPassword = hpms.auth.PasswordUtil.hash(password, salt);
            DataStore.users.put(username, new hpms.auth.User(username, hashedPassword, salt, hpms.model.UserRole.DOCTOR));
        }
    }

    private void stylePrimary(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondary(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(BorderFactory.createLineBorder(fg, 2));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void sizeInput(JComponent c) {
        Dimension d = new Dimension(300, 32);
        c.setPreferredSize(d);
        c.setMaximumSize(d);
        c.setMinimumSize(new Dimension(200, 32));
    }
}
