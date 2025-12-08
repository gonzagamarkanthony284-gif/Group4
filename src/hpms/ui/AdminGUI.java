package hpms.ui;

import hpms.ui.staff.StaffRegistrationForm;
import hpms.ui.staff.StaffPanel;
import hpms.auth.AuthService;
import hpms.model.*;
import hpms.service.*;
import hpms.util.*;
import hpms.ui.components.*;
import hpms.ui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminGUI extends JFrame {
    private final JPanel sidebar = new JPanel();
    private final JPanel content = new JPanel(new CardLayout());
    private SidebarButton currentSelected;
    private final java.util.concurrent.ScheduledExecutorService autosaveScheduler = java.util.concurrent.Executors
            .newSingleThreadScheduledExecutor();

    public AdminGUI() {
        // Apply global UI theme
        Theme.applyGlobalUI();
        setTitle("Hospital Patient Management System - Administration");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        // Build sidebar with admin-only menu items
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.BACKGROUND);
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                sidebar.getBorder()));

        // Admin-only menu items
        List<String> adminMenu = new ArrayList<>();
        adminMenu.addAll(java.util.Arrays.asList(
                "Dashboard", // Admin dashboard with system-wide metrics
                "Patients", // Patient management (Admin view - full access)
                "Staff", // Staff management and registration
                "Appointments", // Global appointment oversight
                "Billing", // Billing oversight
                "Rooms", // Room management
                "Reports", // System reports
                "Administration", // Admin tools: users, backups, security
                "Settings", // System settings
                "Logout"));

        SidebarButton dashboardBtn = null;
        for (String menuItem : adminMenu) {
            SidebarButton btn = menuBtnWithTooltip(menuItem);
            sidebar.add(btn);
            if (menuItem.equals("Dashboard")) {
                dashboardBtn = btn;
            }
        }
        add(sidebar, BorderLayout.WEST);

        // Add admin-specific panels
        content.add("Dashboard", new AdminDashboardPanel());
        content.add("Patients", new PatientsPanel());
        content.add("Staff", new StaffPanel());
        content.add("Appointments", new AppointmentsPanel());
        content.add("Billing", new BillingPanel());
        content.add("Rooms", new RoomsPanel());
        content.add("Reports", new ReportsPanel());
        content.add("Administration", new AdministrationPanel());
        content.add("Settings", new SettingsPanel());

        add(content, BorderLayout.CENTER);
        showCard("Dashboard");

        // Highlight Dashboard menu item on startup
        if (dashboardBtn != null) {
            currentSelected = dashboardBtn;
            dashboardBtn.setSelectedState(true);
        }

        // Setup autosave
        autosaveScheduler.scheduleAtFixedRate(() -> {
            try {
                BackupUtil.saveToDefault();
            } catch (Exception ex) {
            }
        }, 5, 5, java.util.concurrent.TimeUnit.MINUTES);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    BackupUtil.saveToDefault();
                } catch (Exception ex) {
                }
                try {
                    autosaveScheduler.shutdownNow();
                } catch (Exception ex) {
                }
                dispose();
                System.exit(0);
            }
        });
    }

    private String tooltipFor(String name) {
        switch (name) {
            case "Dashboard":
                return "Admin system overview and metrics";
            case "Patients":
                return "Full patient management (admin access)";
            case "Staff":
                return "Register and manage all staff members";
            case "Appointments":
                return "Global appointment management";
            case "Billing":
                return "System-wide billing oversight";
            case "Rooms":
                return "Hospital room and bed management";
            case "Reports":
                return "System reports and analytics";
            case "Administration":
                return "Admin tools: users, backups, security";
            case "Settings":
                return "System settings and configuration";
            case "Logout":
                return "Sign out of the application";
            default:
                return null;
        }
    }

    private SidebarButton menuBtnWithTooltip(String name) {
        SidebarButton b = new SidebarButton(name);
        String tip = tooltipFor(name);
        if (tip != null)
            b.setToolTipText(tip);
        b.addActionListener(e -> onMenu(b, name));
        return b;
    }

    private void onMenu(SidebarButton btn, String name) {
        if (currentSelected != null)
            currentSelected.setSelectedState(false);
        currentSelected = btn;
        currentSelected.setSelectedState(true);

        if (name.equals("Logout")) {
            try {
                BackupUtil.saveToDefault();
            } catch (Exception ex) {
            }
            AuthService.logout();
            new hpms.ui.login.LoginWindow().setVisible(true);
            dispose();
            return;
        }
        showCard(name);
    }

    private void showCard(String name) {
        ((CardLayout) content.getLayout()).show(content, name);
    }
}
