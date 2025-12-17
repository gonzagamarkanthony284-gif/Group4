package hpms.ui;

import hpms.auth.AuthService;
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

    // Dashboard auto-refresh timer to keep metrics synchronized across all modules
    private javax.swing.Timer dashboardAutoRefresh;

    // Store references to panels for refresh
    private AdminDashboardPanel adminDashboardPanel;
    private PatientsPanel patientsPanel;
    private StaffPanel staffPanel;
    private AppointmentsPanel appointmentsPanel;
    private BillingPanel billingPanel;
    private RoomsPanel roomsPanel;
    private ReportsPanel reportsPanel;
    private AdministrationPanel administrationPanel;

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
                "Staff", // Staff management and registration (including Front Desk)
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

        // Add admin-specific panels with stored references for refresh
        adminDashboardPanel = new AdminDashboardPanel();
        content.add("Dashboard", adminDashboardPanel);

        patientsPanel = new PatientsPanel();
        content.add("Patients", patientsPanel);

        staffPanel = new StaffPanel();
        content.add("Staff", staffPanel);

        appointmentsPanel = new AppointmentsPanel();
        content.add("Appointments", appointmentsPanel);

        billingPanel = new BillingPanel();
        content.add("Billing", billingPanel);

        roomsPanel = new RoomsPanel();
        content.add("Rooms", roomsPanel);

        reportsPanel = new ReportsPanel();
        content.add("Reports", reportsPanel);

        administrationPanel = new AdministrationPanel();
        content.add("Administration", administrationPanel);

        content.add("Settings", new SettingsPanel());
        add(content, BorderLayout.CENTER);
        
        // Show dashboard by default
        if (dashboardBtn != null) {
            dashboardBtn.setSelected(true);
        }
        
        // Initialize window settings and auto-refresh
        initializeWindow();
        initializeAutoRefresh();
    }
    
    /**
     * Get the RoomsPanel for refresh operations
     */
    public RoomsPanel getRoomsPanel() {
        return roomsPanel;
    }
    
    private void initializeAutoRefresh() {
        dashboardAutoRefresh = new javax.swing.Timer(30000, e -> refreshPanels());
        dashboardAutoRefresh.setRepeats(true);
        dashboardAutoRefresh.start();
    }
    
    private void initializeWindow() {
        // Disabled autosave - using database instead
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Disabled backup save - using database instead
                try {
                    autosaveScheduler.shutdownNow();
                } catch (Exception ex) {
                }
                try {
                    if (dashboardAutoRefresh != null) {
                        dashboardAutoRefresh.stop();
                    }
                } catch (Exception ex) {
                }
                dispose();
                System.exit(0);
            }
        });
    }

    /**
     * Refresh all panels to ensure data synchronization across modules.
     * Called periodically by auto-refresh timer.
     */
    private void refreshPanels() {
        if (adminDashboardPanel != null) {
            adminDashboardPanel.refresh();
        }
        if (patientsPanel != null) {
            patientsPanel.refresh();
        }
        if (staffPanel != null) {
            staffPanel.refresh();
        }
        if (appointmentsPanel != null) {
            appointmentsPanel.refresh();
        }
        if (billingPanel != null) {
            billingPanel.refresh();
        }
        if (roomsPanel != null) {
            roomsPanel.refresh();
        }
        if (reportsPanel != null) {
            reportsPanel.refresh();
        }
        if (administrationPanel != null) {
            administrationPanel.refresh();
        }
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
            // Disabled backup save - using database instead
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
