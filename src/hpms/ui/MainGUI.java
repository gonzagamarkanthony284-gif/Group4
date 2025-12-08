package hpms.ui;

import hpms.ui.staff.StaffRegistrationForm;

import hpms.auth.AuthService;
import hpms.model.*;
import hpms.service.*;
import hpms.util.*;
import hpms.ui.components.*;
import hpms.ui.panels.*;
import hpms.ui.nurse.NurseDashboardPanel;
import hpms.ui.cashier.CashierDashboardPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainGUI extends JFrame {
    private final JPanel sidebar = new JPanel();
    private final JPanel content = new JPanel(new CardLayout());
    private SidebarButton currentSelected;
    private final java.util.concurrent.ScheduledExecutorService autosaveScheduler = java.util.concurrent.Executors
            .newSingleThreadScheduledExecutor();

    private final DefaultTableModel patientsModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Age", "Gender", "Contact", "Address" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel staffModel = new DefaultTableModel(
            new String[] { "Staff ID", "Name", "Department", "Details", "Status", "Joined Date" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel apptModel = new DefaultTableModel(
            new String[] { "ID", "Patient", "Staff", "DateTime", "Dept" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel billModel = new DefaultTableModel(
            new String[] { "ID", "Patient", "Total", "Status", "Method" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel roomModel = new DefaultTableModel(new String[] { "ID", "Status", "Occupant" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel logModel = new DefaultTableModel(new String[] { "Entry" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private hpms.ui.panels.PatientsPanel patientsPanel;

    public MainGUI() {
        // Apply global UI theme
        hpms.ui.components.Theme.applyGlobalUI();
        setTitle("Hospital Patient Management System");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.BACKGROUND);
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                sidebar.getBorder()));
        UserRole role = AuthService.current == null ? null : AuthService.current.role;
        java.util.List<String> menuList = new java.util.ArrayList<>();

        // ADMIN users should NOT use MainGUI - they should use AdminGUI
        // Prevent admin access to MainGUI
        if (role == UserRole.ADMIN) {
            JOptionPane.showMessageDialog(this, "Administrators must use the Administration interface. Redirecting...",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            try {
                hpms.util.BackupUtil.saveToDefault();
            } catch (Exception ex) {
            }
            AuthService.logout();
            new hpms.ui.login.LoginWindow().setVisible(true);
            dispose();
            return;
        } else if (role == UserRole.DOCTOR) {
            menuList.addAll(
                    java.util.Arrays.asList("Dashboard", "Patients", "Appointments", "Reports", "Settings", "Logout"));
        } else if (role == UserRole.NURSE) {
            menuList.addAll(java.util.Arrays.asList("Dashboard", "Patients", "Appointments", "Rooms", "Reports",
                    "Settings", "Logout"));
        } else if (role == UserRole.CASHIER) {
            menuList.addAll(java.util.Arrays.asList("Dashboard", "Billing", "Reports", "Settings", "Logout"));
        } else {
            // Default staff menu (no admin, no patient access)
            menuList.addAll(java.util.Arrays.asList("Dashboard", "Patients", "Appointments", "Billing", "Rooms",
                    "Reports", "Settings", "Logout"));
        }
        SidebarButton dashboardBtn = null;
        for (String m : menuList) {
            SidebarButton btn = menuBtnWithTooltip(m);
            sidebar.add(btn);
            if (m.equals("Dashboard")) {
                dashboardBtn = btn;
            }
        }
        add(sidebar, BorderLayout.WEST);

        // (Administration menu is only added for admins above)

        // add panels according to role visibility
        if (role == UserRole.DOCTOR) {
            hpms.auth.User u = hpms.auth.AuthService.current;
            hpms.model.Staff s = u != null ? hpms.util.DataStore.staff.get(u.username) : null;
            hpms.auth.AuthSession sessionObj = new hpms.auth.AuthSession(
                    u != null ? u.username : "",
                    u != null ? u.username : "",
                    s != null && s.name != null ? s.name : (u != null ? u.username : "Doctor"),
                    hpms.model.UserRole.DOCTOR,
                    s != null ? s.department : "");
            content.add("Dashboard", new hpms.ui.doctor.DoctorDashboardPanel(sessionObj));
            if (menuList.contains("Patients")) {
                content.add("Patients", new hpms.ui.doctor.DoctorPatientsPanel(sessionObj));
            }
            if (menuList.contains("Appointments"))
                content.add("Appointments", new hpms.ui.doctor.DoctorAppointmentsPanel(sessionObj));
        } else if (role == UserRole.NURSE) {
            hpms.auth.User u = hpms.auth.AuthService.current;
            hpms.model.Staff s = u != null ? hpms.util.DataStore.staff.get(u.username) : null;
            hpms.auth.AuthSession sessionObj = new hpms.auth.AuthSession(
                    u != null ? u.username : "",
                    u != null ? u.username : "",
                    s != null && s.name != null ? s.name : (u != null ? u.username : "Nurse"),
                    hpms.model.UserRole.NURSE,
                    s != null ? s.department : "");
            content.add("Dashboard", new NurseDashboardPanel(sessionObj));
            if (menuList.contains("Patients")) {
                patientsPanel = new PatientsPanel();
                content.add("Patients", patientsPanel);
            }
            if (menuList.contains("Appointments"))
                content.add("Appointments", new AppointmentsPanel());
            if (menuList.contains("Rooms"))
                content.add("Rooms", new RoomsPanel());
        } else if (role == UserRole.CASHIER) {
            hpms.auth.User u = hpms.auth.AuthService.current;
            hpms.model.Staff s = u != null ? hpms.util.DataStore.staff.get(u.username) : null;
            hpms.auth.AuthSession sessionObj = new hpms.auth.AuthSession(
                    u != null ? u.username : "",
                    u != null ? u.username : "",
                    s != null && s.name != null ? s.name : (u != null ? u.username : "Cashier"),
                    hpms.model.UserRole.CASHIER,
                    s != null ? s.department : "");
            content.add("Dashboard", new CashierDashboardPanel(sessionObj));
            if (menuList.contains("Billing"))
                content.add("Billing", new BillingPanel());
            if (menuList.contains("Reports"))
                content.add("Reports", new ReportsPanel());
        } else {
            content.add("Dashboard", new DashboardPanel());
            if (menuList.contains("Patients")) {
                patientsPanel = new PatientsPanel();
                content.add("Patients", patientsPanel);
            }
            if (menuList.contains("Appointments"))
                content.add("Appointments", new AppointmentsPanel());
        }
        if (role != UserRole.CASHIER) {
            if (menuList.contains("Billing"))
                content.add("Billing", new BillingPanel());
        }
        if (role != UserRole.NURSE) {
            if (menuList.contains("Rooms"))
                content.add("Rooms", new RoomsPanel());
        }

        // NOTE: Staff and Administration panels are NOT added here - they are
        // admin-only features
        // Non-admin staff cannot access Staff management or Administration panels

        if (role != UserRole.CASHIER) {
            if (menuList.contains("Reports"))
                content.add("Reports", new ReportsPanel());
        }
        if (menuList.contains("Settings"))
            content.add("Settings", new SettingsPanel());
        add(content, BorderLayout.CENTER);
        showCard("Dashboard");

        // Highlight Dashboard menu item on startup
        if (dashboardBtn != null) {
            currentSelected = dashboardBtn;
            dashboardBtn.setSelectedState(true);
        }

        refreshTables();

        // periodic autosave and auto-save on close
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

    private JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(Theme.APP_FONT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return b;
    }

    private SidebarButton menuBtn(String name) {
        SidebarButton b = new SidebarButton(name);
        b.addActionListener(e -> onMenu(b, name));
        return b;
    }

    private String tooltipFor(String name) {
        switch (name) {
            case "Dashboard":
                return "Overview of key metrics";
            case "Patients":
                return "Search and manage patients";
            case "Appointments":
                return "Manage appointments and schedules";
            case "Billing":
                return "Create and process bills/payments";
            case "Rooms":
                return "Assign or vacate rooms and beds";

            case "Staff":
                return "Manage staff records and user accounts";
            case "Reports":
                return "Run reports and view activity logs";
            case "Administration":
                return "Admin tools: users, backups, security";
            case "Settings":
                return "Application settings and preferences";
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
                hpms.util.BackupUtil.saveToDefault();
            } catch (Exception ex) {
            }
            AuthService.logout();
            new hpms.ui.login.LoginWindow().setVisible(true);
            dispose();
            return;
        }
        showCard(name);
        refreshTables();
    }

    private void showCard(String name) {
        ((CardLayout) content.getLayout()).show(content, name);
    }

    private JPanel buildDashboard() {
        JPanel p = new JPanel(new GridLayout(2, 2, 12, 12));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(card("Patients", String.valueOf(DataStore.patients.size())));
        long todaysAppts = DataStore.appointments.values().stream()
                .filter(a -> a.dateTime.toLocalDate().equals(LocalDate.now())).count();
        p.add(card("Appointments Today", String.valueOf(todaysAppts)));
        long occupied = DataStore.rooms.values().stream().filter(r -> r.status == RoomStatus.OCCUPIED).count();
        long total = DataStore.rooms.size();
        p.add(card("Bed Occupancy", occupied + "/" + total));
        long pending = DataStore.bills.values().stream().filter(b -> !b.paid).count();
        p.add(card("Pending Bills", String.valueOf(pending)));
        return p;
    }

    private JPanel buildAppointments() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = new JTable(apptModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel actions = new JPanel();
        JButton add = new JButton("Schedule"), res = new JButton("Reschedule"), cancel = new JButton("Cancel");
        actions.add(add);
        actions.add(res);
        actions.add(cancel);
        p.add(actions, BorderLayout.NORTH);
        add.addActionListener(e -> scheduleApptDialog());
        res.addActionListener(e -> rescheduleApptDialog(t));
        cancel.addActionListener(e -> cancelAppt(t));
        return p;
    }

    private JPanel buildBilling() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = new JTable(billModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel actions = new JPanel();
        JButton create = new JButton("Create"), addItem = new JButton("Add Item"), pay = new JButton("Pay"),
                history = new JButton("Payment History"), del = new JButton("Delete");
        actions.add(create);
        actions.add(addItem);
        actions.add(pay);
        actions.add(history);
        actions.add(del);
        p.add(actions, BorderLayout.NORTH);
        create.addActionListener(e -> createBillDialog());
        addItem.addActionListener(e -> addItemDialog(t));
        pay.addActionListener(e -> payBillDialog(t));
        history.addActionListener(e -> showPaymentHistory());
        del.addActionListener(e -> deleteBill(t));
        return p;
    }

    private JPanel buildRooms() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = new JTable(roomModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel actions = new JPanel();
        JButton assign = new JButton("Assign"), vacate = new JButton("Vacate");
        actions.add(assign);
        actions.add(vacate);
        p.add(actions, BorderLayout.NORTH);
        assign.addActionListener(e -> assignRoomDialog());
        vacate.addActionListener(e -> vacateRoom(t));
        return p;
    }

    private JPanel buildStaff() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = new JTable(staffModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel actions = new JPanel();
        JButton add = new JButton("Add"), del = new JButton("Delete"), view = new JButton("View Details"),
                reg = new JButton("Register User");
        actions.add(add);
        actions.add(del);
        actions.add(view);
        if (AuthService.current != null && AuthService.current.role == UserRole.ADMIN)
            actions.add(reg);
        p.add(actions, BorderLayout.NORTH);
        add.addActionListener(e -> addStaffDialog());
        del.addActionListener(e -> deleteStaff(t));
        view.addActionListener(e -> viewStaffDetails(t));
        reg.addActionListener(e -> registerUserDialog());
        // Double-click to view details
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2)
                    viewStaffDetails(t);
            }
        });
        return p;
    }

    private JPanel buildReports() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = new JTable(logModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JButton daily = new JButton("Today's Appointments"), activity = new JButton("Activity Log"),
                apptSum = new JButton("Appointment Summary"), billOv = new JButton("Billing Overview"),
                stock = new JButton("Medicine Stock"), patientsBtn = new JButton("Patient List");
        JPanel actions = new JPanel();
        actions.add(daily);
        actions.add(activity);
        actions.add(apptSum);
        actions.add(billOv);
        actions.add(stock);
        actions.add(patientsBtn);
        p.add(actions, BorderLayout.NORTH);
        daily.addActionListener(e -> showDaily());
        activity.addActionListener(e -> showActivity());
        apptSum.addActionListener(e -> showAppointmentSummary());
        billOv.addActionListener(e -> showBillingOverview());
        stock.addActionListener(e -> showMedicineStock());
        patientsBtn.addActionListener(e -> showPatientList());
        return p;
    }

    private void refreshTables() {
        if (patientsPanel != null)
            patientsPanel.refresh();
        staffModel.setRowCount(0);
        for (Staff s : DataStore.staff.values()) {
            String details = "";
            if (s.role == StaffRole.DOCTOR) {
                details = (s.specialty != null && !s.specialty.isEmpty() ? s.specialty : "")
                        + (s.licenseNumber != null && !s.licenseNumber.isEmpty() ? " | License: " + s.licenseNumber
                                : "");
            } else if (s.role == StaffRole.NURSE) {
                details = (s.qualifications != null && !s.qualifications.isEmpty() ? s.qualifications : "")
                        + (s.licenseNumber != null && !s.licenseNumber.isEmpty() ? " | License: " + s.licenseNumber
                                : "");
            }
            String joinDate = s.createdAt != null ? s.createdAt.toLocalDate().toString() : "";
            String status = s.isAvailable ? "Available" : "Unavailable";
            staffModel.addRow(new Object[] { s.id, s.name, s.department, details, status, joinDate });
        }
        apptModel.setRowCount(0);
        for (Appointment a : DataStore.appointments.values())
            apptModel.addRow(new Object[] { a.id, a.patientId, a.staffId, a.dateTime, a.department });
        billModel.setRowCount(0);
        for (Bill b : DataStore.bills.values())
            billModel.addRow(new Object[] { b.id, b.patientId, String.format(Locale.US, "%.2f", b.total),
                    b.paid ? "PAID" : "UNPAID", b.paymentMethod == null ? "" : b.paymentMethod });
        roomModel.setRowCount(0);
        for (Room r : DataStore.rooms.values())
            roomModel.addRow(new Object[] { r.id, r.status, r.occupantPatientId == null ? "" : r.occupantPatientId });
        logModel.setRowCount(0);
        for (String entry : ReportService.activity())
            logModel.addRow(new Object[] { entry });
    }

    public void addPatientDialog() {
        // If patientsPanel is available, reuse its full add dialog (keeps UI
        // consistent) — fallback to simple dialog when not present
        if (patientsPanel != null) {
            patientsPanel.addPatientDialog();
            refreshTables();
            return;
        }

        JTextField name = new JTextField();
        JTextField age = new JTextField();
        JComboBox<String> gender = new JComboBox<>(new String[] { "Male", "Female", "LGBTQ+", "Other" });
        JTextField contact = new JTextField();
        JTextField address = new JTextField();
        int r = JOptionPane.showConfirmDialog(this,
                new Object[] { "Name", name, "Age", age, "Gender", gender, "Contact", contact, "Address", address },
                "Add Patient", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            String n = name.getText().trim();
            String a = age.getText().trim();
            if (n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required");
                return;
            }
            Integer ageVal = parseIntSafe(a);
            if (ageVal == null || ageVal <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid age");
                return;
            }
            List<String> out = PatientService.add(n, a, "1990-01-01", String.valueOf(gender.getSelectedItem()),
                    contact.getText(),
                    address.getText(), "OUTPATIENT");
            showOut(out);
            refreshTables();
        }
    }

    private void editPatient(JTable t) {
        // Delegate to the PatientsPanel edit dialog whenever it exists (ensures full
        // form consistency)
        if (patientsPanel != null) {
            patientsPanel.editPatient(t);
            refreshTables();
            return;
        }

        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        Patient p = DataStore.patients.get(id);
        JTextField name = new JTextField(p.name);
        JTextField age = new JTextField(String.valueOf(p.age));
        JComboBox<String> gender = new JComboBox<>(new String[] { "Male", "Female", "LGBTQ+", "Other" });
        String gsel = "Other";
        if (p.gender == hpms.model.Gender.Male)
            gsel = "Male";
        else if (p.gender == hpms.model.Gender.Female)
            gsel = "Female";
        else if (p.gender == hpms.model.Gender.LGBTQ_PLUS)
            gsel = "LGBTQ+";
        gender.setSelectedItem(gsel);
        JTextField contact = new JTextField(p.contact);
        JTextField address = new JTextField(p.address);
        int r = JOptionPane.showConfirmDialog(this,
                new Object[] { "Name", name, "Age", age, "Gender", gender, "Contact", contact, "Address", address },
                "Edit Patient " + id, JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            showOut(PatientService.edit(id, name.getText(), age.getText(), String.valueOf(gender.getSelectedItem()),
                    contact.getText(), address.getText()));
            refreshTables();
        }
    }

    private void deletePatient(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        Patient p = DataStore.patients.get(id);
        if (p != null && !p.isActive) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reactivate patient " + p.name + "?",
                    "Reactivate Patient", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                showOut(PatientService.reactivate(id));
                refreshTables();
            }
        } else {
            showOut(PatientService.delete(id));
            refreshTables();
        }
    }

    private void searchPatient() {
        String id = JOptionPane.showInputDialog(this, "Patient ID");
        if (id != null)
            showOut(PatientService.search(id.trim()));
    }

    private void addStaffDialog() {
        // Use the new StaffRegistrationForm instead of a simple JOptionPane
        StaffRegistrationForm form = new StaffRegistrationForm();
        form.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                refreshTables();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                refreshTables();
            }
        });
        form.setVisible(true);
    }

    private void deleteStaff(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        showOut(StaffService.delete(id));
        refreshTables();
    }

    private void viewStaffDetails(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        hpms.model.Staff staff = DataStore.staff.get(id);
        if (staff != null) {
            new hpms.ui.staff.StaffDetailsDialog(this, staff).setVisible(true);
        }
    }

    private void registerUserDialog() {
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(new String[] { "DOCTOR", "CASHIER", "NURSE" });
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Username", user, "Password", pass, "Role", role },
                "Register User", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            String u = user.getText().trim();
            String p = new String(pass.getPassword());
            if (u.isEmpty() || p.length() < 6) {
                JOptionPane.showMessageDialog(this, "Username required and password must be >=6 chars");
                return;
            }
            showOut(hpms.auth.AuthService.register(u, p, String.valueOf(role.getSelectedItem())));
            refreshTables();
        }
    }

    private void scheduleApptDialog() {
        JComboBox<String> pid = new JComboBox<>(DataStore.patients.keySet().toArray(new String[0]));
        JComboBox<String> sid = new JComboBox<>(DataStore.staff.values().stream()
                .filter(s -> s.role == StaffRole.DOCTOR).map(s -> s.id).toArray(String[]::new));
        JTextField date = new JTextField(LocalDate.now().toString());
        JTextField time = new JTextField(LocalTime.now().withSecond(0).withNano(0).toString());
        JComboBox<String> dept = new JComboBox<>(DataStore.departments.toArray(new String[0]));
        int r = JOptionPane.showConfirmDialog(this,
                new Object[] { "Patient", pid, "Doctor", sid, "Date", date, "Time", time, "Department", dept },
                "Schedule Appointment", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            showOut(AppointmentService.schedule(String.valueOf(pid.getSelectedItem()),
                    String.valueOf(sid.getSelectedItem()), date.getText(), time.getText(),
                    String.valueOf(dept.getSelectedItem())));
            refreshTables();
        }
    }

    private void cancelAppt(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        showOut(AppointmentService.cancel(id));
        refreshTables();
    }

    private void rescheduleApptDialog(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        JTextField date = new JTextField(LocalDate.now().toString());
        JTextField time = new JTextField(LocalTime.now().withSecond(0).withNano(0).toString());
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Date", date, "Time", time }, "Reschedule " + id,
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            showOut(AppointmentService.reschedule(id, date.getText(), time.getText()));
            refreshTables();
        }
    }

    private void createBillDialog() {
        JComboBox<String> pid = new JComboBox<>(DataStore.patients.keySet().toArray(new String[0]));
        JTextField amt = new JTextField("0");
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Patient", pid, "Initial Amount", amt },
                "Create Bill", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            try {
                double v = Double.parseDouble(amt.getText().trim());
                if (v < 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be >= 0");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount");
                return;
            }
            showOut(BillingService.create(String.valueOf(pid.getSelectedItem()), amt.getText()));
            refreshTables();
        }
    }

    private void addItemDialog(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        JTextField desc = new JTextField();
        JTextField price = new JTextField();
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Description", desc, "Price", price },
                "Add Bill Item", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            try {
                double v = Double.parseDouble(price.getText().trim());
                if (v < 0) {
                    JOptionPane.showMessageDialog(this, "Price must be >= 0");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid price");
                return;
            }
            showOut(BillingService.addItem(id, desc.getText(), price.getText()));
            refreshTables();
        }
    }

    private void payBillDialog(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        JComboBox<String> method = new JComboBox<>(new String[] { "CASH", "CARD", "INSURANCE" });
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Method", method }, "Pay Bill",
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            showOut(BillingService.pay(id, String.valueOf(method.getSelectedItem())));
            refreshTables();
        }
    }

    private void deleteBill(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        List<String> out = new ArrayList<>();
        DataStore.bills.remove(id);
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.add("Bill deleted " + id);
        showOut(out);
        refreshTables();
    }

    private void assignRoomDialog() {
        JComboBox<String> rid = new JComboBox<>(DataStore.rooms.keySet().toArray(new String[0]));
        JComboBox<String> pid = new JComboBox<>();
        // Only show active inpatients
        DataStore.patients.forEach((id, p) -> {
            if (!p.isActive)
                return;
            if (PatientStatusService.getStatus(id) == PatientStatus.INPATIENT) {
                pid.addItem(id);
            }
        });
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Room", rid, "Patient (Inpatients Only)", pid },
                "Assign Room",
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            showOut(RoomService.assign(String.valueOf(rid.getSelectedItem()), String.valueOf(pid.getSelectedItem())));
            refreshTables();
        }
    }

    private void vacateRoom(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        showOut(RoomService.vacate(id));
        refreshTables();
    }

    private void showDaily() {
        JTable t = new JTable(new DefaultTableModel(new String[] { "Appt" }, 0));
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        for (String s : ReportService.dailyAppointments())
            m.addRow(new Object[] { s });
        JOptionPane.showMessageDialog(this, new JScrollPane(t));
    }

    private void showActivity() {
        refreshTables();
        showCard("Reports");
    }

    private void showAppointmentSummary() {
        JTable t = new JTable(new DefaultTableModel(new String[] { "Summary" }, 0));
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        for (String s : ReportService.appointmentSummary())
            m.addRow(new Object[] { s });
        JOptionPane.showMessageDialog(this, new JScrollPane(t));
    }

    private void showBillingOverview() {
        JTable t = new JTable(new DefaultTableModel(new String[] { "Daily Billing" }, 0));
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        for (String s : ReportService.billingOverviewDaily())
            m.addRow(new Object[] { s });
        JOptionPane.showMessageDialog(this, new JScrollPane(t));
    }

    private void showMedicineStock() {
        JTable t = new JTable(new DefaultTableModel(new String[] { "Stock" }, 0));
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        for (String s : ReportService.medicineStockSummary())
            m.addRow(new Object[] { s });
        JOptionPane.showMessageDialog(this, new JScrollPane(t));
    }

    private void showPatientList() {
        JTable t = new JTable(new DefaultTableModel(new String[] { "Patients" }, 0));
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        for (String s : ReportService.patientList())
            m.addRow(new Object[] { s });
        JOptionPane.showMessageDialog(this, new JScrollPane(t));
    }

    private void showOut(List<String> out) {
        JOptionPane.showMessageDialog(this, String.join("\n", out));
    }

    private Room findRoomForPatient(String pid) {
        for (Room r : DataStore.rooms.values())
            if (pid.equals(r.occupantPatientId))
                return r;
        return null;
    }

    private Appointment latestAppointmentForPatient(String pid) {
        return DataStore.appointments.values().stream().filter(a -> a.patientId.equals(pid))
                .max(Comparator.comparing(a -> a.dateTime)).orElse(null);
    }

    private Integer parseIntSafe(String s) {
        try {
            if (s == null || s.trim().isEmpty())
                return null;
            return Integer.parseInt(s.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private JPanel card(String title, String value) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 210)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD));
        JLabel v = new JLabel(value);
        v.setFont(v.getFont().deriveFont(24f));
        c.add(t, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        return c;
    }

    private JPanel buildAdministration() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel usersModel = new DefaultTableModel(new String[] { "Username", "Role" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(usersModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel actions = new JPanel();
        JButton add = new JButton("Register User"), backup = new JButton("Backup"), restore = new JButton("Restore"),
                saveFile = new JButton("Save..."), loadFile = new JButton("Load..."),
                resetPwd = new JButton("Reset Password");
        actions.add(add);
        actions.add(backup);
        actions.add(restore);
        actions.add(saveFile);
        actions.add(loadFile);
        actions.add(resetPwd);
        p.add(actions, BorderLayout.NORTH);
        add.addActionListener(e -> registerUserDialog());
        backup.addActionListener(e -> doBackup());
        restore.addActionListener(e -> doRestore());
        saveFile.addActionListener(e -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setDialogTitle("Save Backup JSON");
            if (fc.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.nio.file.Path pth = fc.getSelectedFile().toPath();
                boolean ok = BackupUtil.saveToFile(pth);
                JOptionPane.showMessageDialog(this, ok ? "Saved" : "Save failed");
            }
        });
        loadFile.addActionListener(e -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setDialogTitle("Load Backup JSON");
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.nio.file.Path pth = fc.getSelectedFile().toPath();
                boolean ok = BackupUtil.loadFromFile(pth);
                JOptionPane.showMessageDialog(this, ok ? "Loaded" : "Load failed");
                if (ok)
                    refreshTables();
            }
        });
        resetPwd.addActionListener(e -> {
            int sel = t.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Select a user to reset");
                return;
            }
            String username = String.valueOf(t.getValueAt(sel, 0));
            String newPwd = hpms.auth.AuthService.resetPassword(username);
            if (newPwd == null)
                JOptionPane.showMessageDialog(this, "Reset failed");
            else
                JOptionPane.showMessageDialog(this, "New password for " + username + ":\n" + newPwd);
            refreshTables();
        });
        SwingUtilities.invokeLater(() -> {
            usersModel.setRowCount(0);
            for (hpms.auth.User u : DataStore.users.values())
                usersModel.addRow(new Object[] { u.username, u.role });
        });
        return p;
    }

    private JPanel buildSettings() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(new JLabel("Allowed Payment Methods:"));
        JCheckBox cash = new JCheckBox("Cash", DataStore.allowedPaymentMethods.contains(PaymentMethod.CASH));
        JCheckBox card = new JCheckBox("Card", DataStore.allowedPaymentMethods.contains(PaymentMethod.CARD));
        JCheckBox insurance = new JCheckBox("Insurance",
                DataStore.allowedPaymentMethods.contains(PaymentMethod.INSURANCE));
        JButton save = new JButton("Save");
        p.add(cash);
        p.add(card);
        p.add(insurance);
        p.add(save);
        save.addActionListener(e -> {
            DataStore.allowedPaymentMethods.clear();
            if (cash.isSelected())
                DataStore.allowedPaymentMethods.add(PaymentMethod.CASH);
            if (card.isSelected())
                DataStore.allowedPaymentMethods.add(PaymentMethod.CARD);
            if (insurance.isSelected())
                DataStore.allowedPaymentMethods.add(PaymentMethod.INSURANCE);
            try {
                BackupUtil.saveToDefault();
            } catch (Exception ex) {
            }
            JOptionPane.showMessageDialog(this, "Settings saved");
        });
        JButton changePwd = new JButton("Change Password");
        p.add(changePwd);
        changePwd.addActionListener(e -> {
            if (AuthService.current == null) {
                JOptionPane.showMessageDialog(this, "Not logged in");
                return;
            }
            // Simplified change-password flow for current user: only ask for new password
            // and confirm (no current password required here)
            JPasswordField nw = new JPasswordField();
            JPasswordField nw2 = new JPasswordField();
            int r = JOptionPane.showConfirmDialog(this, new Object[] { "New Password", nw, "Confirm New", nw2 },
                    "Change Password", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                String nstr = new String(nw.getPassword());
                String n2 = new String(nw2.getPassword());
                if (!nstr.equals(n2)) {
                    JOptionPane.showMessageDialog(this, "New passwords do not match");
                    return;
                }
                java.util.List<String> out = AuthService.changePasswordNoOld(AuthService.current.username, nstr);
                JOptionPane.showMessageDialog(this, String.join("\n", out));
            }
        });
        return p;
    }

    private void showPaymentHistory() {
        DefaultTableModel m = new DefaultTableModel(new String[] { "Bill", "Patient", "Total", "Method", "Date" }, 0);
        for (Bill b : DataStore.bills.values())
            if (b.paid)
                m.addRow(new Object[] { b.id, b.patientId, String.format(Locale.US, "%.2f", b.total), b.paymentMethod,
                        b.updatedAt });
        JTable t = new JTable(m);
        JOptionPane.showMessageDialog(this, new JScrollPane(t), "Payment History", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doBackup() {
        String json = BackupUtil.toJson();
        JTextArea ta = new JTextArea(json, 20, 60);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Backup JSON", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doRestore() {
        String json = JOptionPane.showInputDialog(this, "Paste backup JSON");
        if (json == null || json.trim().isEmpty())
            return;
        BackupUtil.fromJson(json);
        JOptionPane.showMessageDialog(this, "Restore complete");
        refreshTables();
    }

}
