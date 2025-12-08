package hpms.ui.login;

import hpms.auth.AuthService;
import hpms.util.BackupUtil;
import hpms.model.RoomStatus;
import hpms.model.Room;
import hpms.util.DataStore;
import hpms.util.IDGenerator;

import javax.swing.*;
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
                BackupUtil.loadFromDefault();
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
            // Route ADMIN users to AdminGUI, all other staff to MainGUI
            if (u != null && u.role == hpms.model.UserRole.ADMIN) {
                hpms.ui.AdminGUI adminGui = new hpms.ui.AdminGUI();
                adminGui.setVisible(true);
                dispose();
            } else {
                hpms.ui.MainGUI mainGui = new hpms.ui.MainGUI();
                mainGui.setVisible(true);
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, String.join("\n", out), "Login Failed", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
        }
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
