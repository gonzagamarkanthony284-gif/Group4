package hpms.ui.factory;

import hpms.auth.AuthSession;
import javax.swing.*;
import java.awt.*;

/**
 * Factory for creating role-based menus and navigation
 */
public class MenuFactory {
    public static final Color SIDEBAR_BG = new Color(232, 240, 254);
    public static final Color SIDEBAR_HOVER = new Color(219, 234, 254);
    public static final Color ACCENT_BLUE = new Color(47, 111, 237);
    public static final Color TEXT_DARK = new Color(31, 41, 55);
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);

    public interface MenuItemListener {
        void onMenuItemSelected(String menuId);
    }

    public static JPanel createDoctorSidebar(MenuItemListener listener) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        sidebar.setPreferredSize(new Dimension(200, 600));

        // Logo/Header
        JPanel header = new JPanel();
        header.setBackground(ACCENT_BLUE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel logo = new JLabel("HPMS Doctor");
        logo.setFont(new Font("Arial", Font.BOLD, 14));
        logo.setForeground(Color.WHITE);
        header.add(logo);
        sidebar.add(header);

        // Menu items
        String[] items = {
            "Dashboard|dashboard",
            "My Patients|patients",
            "Appointments|appointments",
            "Requests|requests",
            "Availability|availability",
            "My Profile|profile",
            "Change Password|password",
            "Logout|logout"
        };

        for (String item : items) {
            String[] parts = item.split("\\|");
            String label = parts[0];
            String id = parts[1];
            sidebar.add(createMenuButton(label, id, listener));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private static JButton createMenuButton(String label, String menuId, MenuItemListener listener) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setForeground(TEXT_DARK);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, Color.WHITE));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(SIDEBAR_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> listener.onMenuItemSelected(menuId));
        return btn;
    }

    public static JPanel createAdminSidebar(MenuItemListener listener) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        sidebar.setPreferredSize(new Dimension(200, 600));

        // Logo/Header
        JPanel header = new JPanel();
        header.setBackground(ACCENT_BLUE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel logo = new JLabel("HPMS Admin");
        logo.setFont(new Font("Arial", Font.BOLD, 14));
        logo.setForeground(Color.WHITE);
        header.add(logo);
        sidebar.add(header);

        // Menu items
        String[] items = {
            "Dashboard|admin-dashboard",
            "Staff Management|admin-staff",
            "Patients|admin-patients",
            "Billing|admin-billing",
            "Reports|admin-reports",
            "Settings|admin-settings",
            "Logout|logout"
        };

        for (String item : items) {
            String[] parts = item.split("\\|");
            String label = parts[0];
            String id = parts[1];
            sidebar.add(createMenuButton(label, id, listener));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    public static JPanel createPatientSidebar(MenuItemListener listener) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        sidebar.setPreferredSize(new Dimension(200, 600));

        // Logo/Header
        JPanel header = new JPanel();
        header.setBackground(ACCENT_BLUE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel logo = new JLabel("HPMS Portal");
        logo.setFont(new Font("Arial", Font.BOLD, 14));
        logo.setForeground(Color.WHITE);
        header.add(logo);
        sidebar.add(header);

        // Menu items
        String[] items = {
            "Dashboard|patient-dashboard",
            "My Appointments|patient-appointments",
            "Medical Records|patient-records",
            "Payments|patient-payments",
            "Logout|logout"
        };

        for (String item : items) {
            String[] parts = item.split("\\|");
            String label = parts[0];
            String id = parts[1];
            sidebar.add(createMenuButton(label, id, listener));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    public static JPanel createSidebar(AuthSession session, MenuItemListener listener) {
        if (session.isDoctor()) {
            return createDoctorSidebar(listener);
        } else if (session.isAdmin()) {
            return createAdminSidebar(listener);
        } else if (session.isPatient()) {
            return createPatientSidebar(listener);
        }
        return new JPanel();
    }
}
