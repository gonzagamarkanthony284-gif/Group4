package hpms.ui.patient;

import hpms.model.*;
import hpms.util.*;
import hpms.service.AppointmentService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PatientDashboardWindow extends JFrame {
    private Patient patient;
    private JPanel contentPanel;
    private JLabel profileTab, visitsTab, insuranceTab, requestsTab, medicalTab, billingTab;
    private JTable todayTable, upcomingTable, requestsTable, billingTable;

    public PatientDashboardWindow(Patient patient) {
        this.patient = patient;

        setTitle("HPMS Patient Portal - " + patient.name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.add(createSidebar(), BorderLayout.WEST);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(createProfilePanel(), "profile");
        contentPanel.add(createVisitsPanel(), "visits");
        contentPanel.add(createMedicalPanel(), "medical");
        contentPanel.add(createBillingPanel(), "billing");
        contentPanel.add(createInsurancePanel(), "insurance");
        contentPanel.add(createRequestsPanel(), "requests");

        bodyPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(bodyPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
        switchTab("profile");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Patient Portal - Welcome, " + patient.name);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(220, 20, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            dispose();
            new hpms.ui.login.LoginWindow().setVisible(true);
        });

        header.add(title, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(240, 245, 250));
        sidebar.setBorder(new LineBorder(new Color(200, 200, 200), 1, false));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        profileTab = createTabButton("👤 My Profile", "profile");
        visitsTab = createTabButton("📋 My Visits", "visits");
        medicalTab = createTabButton("🩺 Medical", "medical");
        billingTab = createTabButton("💵 Billing", "billing");
        insuranceTab = createTabButton("💳 Insurance", "insurance");
        requestsTab = createTabButton("📝 Requests", "requests");

        sidebar.add(profileTab);
        sidebar.add(visitsTab);
        sidebar.add(medicalTab);
        sidebar.add(billingTab);
        sidebar.add(insuranceTab);
        sidebar.add(requestsTab);
        sidebar.add(Box.createVerticalGlue());

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel patientIdLabel = new JLabel("Patient ID: " + patient.id);
        patientIdLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        patientIdLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(patientIdLabel);

        JLabel lastActiveLabel = new JLabel("Last Active: Just now");
        lastActiveLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        lastActiveLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(lastActiveLabel);

        sidebar.add(infoPanel);
        return sidebar;
    }

    private JLabel createTabButton(String text, String tabName) {
        JLabel tab = new JLabel(text);
        tab.setFont(new Font("Arial", Font.PLAIN, 13));
        tab.setForeground(new Color(60, 60, 60));
        tab.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        tab.setOpaque(false);
        tab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switchTab(tabName);
            }
        });
        return tab;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("My Profile Information");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        panel.add(createSectionHeader("👤 Identity Information"));
        panel.add(createReadOnlyField("Name:", patient.name));
        panel.add(createReadOnlyField("Age:", String.valueOf(patient.age)));
        panel.add(createReadOnlyField("Gender:", patient.gender == null ? "" : patient.gender.name()));
        panel.add(createReadOnlyField("Status:", getPatientStatusText()));
        panel.add(createReadOnlyField("Assigned Doctor:", getAssignedDoctorDisplay()));
        panel.add(createReadOnlyField("Contact:", patient.contact));
        panel.add(createReadOnlyField("Address:", patient.address));

        panel.add(Box.createVerticalStrut(20));
        panel.add(createSectionHeader("📋 Medical Information"));
        panel.add(createReadOnlyField("Allergies:",
                patient.allergies == null || patient.allergies.isEmpty() ? "None reported" : patient.allergies));
        panel.add(createReadOnlyField("Current Medications:",
                patient.medications == null || patient.medications.isEmpty() ? "None reported" : patient.medications));
        panel.add(createReadOnlyField("Past Medical History:",
                patient.pastMedicalHistory == null || patient.pastMedicalHistory.isEmpty() ? "None reported"
                        : patient.pastMedicalHistory));

        panel.add(Box.createVerticalStrut(20));
        panel.add(createSectionHeader("💪 Lifestyle"));
        panel.add(createReadOnlyField("Smoking Status:",
                patient.smokingStatus == null ? "Not provided" : patient.smokingStatus));
        panel.add(
                createReadOnlyField("Alcohol Use:", patient.alcoholUse == null ? "Not provided" : patient.alcoholUse));
        panel.add(createReadOnlyField("Occupation:", patient.occupation == null ? "Not provided" : patient.occupation));

        panel.add(Box.createVerticalStrut(20));
        panel.add(createSectionHeader("💳 Insurance Information"));
        panel.add(createReadOnlyField("Insurance Provider:",
                patient.insuranceProvider == null ? "None" : patient.insuranceProvider));
        panel.add(createReadOnlyField("Insurance ID:",
                patient.insuranceId == null ? "N/A" : maskInsuranceId(patient.insuranceId)));
        panel.add(createReadOnlyField("Policy Holder:",
                patient.policyHolderName == null ? "N/A" : patient.policyHolderName));
        panel.add(createReadOnlyField("Policy Relationship:",
                patient.policyRelationship == null ? "N/A" : patient.policyRelationship));

        panel.add(Box.createVerticalGlue());
        JButton requestUpdateBtn = new JButton("🔄 Request Information Update");
        requestUpdateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(requestUpdateBtn);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void refreshBillingTable(JLabel outstandingLabel) {
        if (billingTable == null)
            return;
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) billingTable.getModel();
        m.setRowCount(0);
        List<Bill> bills = new ArrayList<>();
        for (Bill b : DataStore.bills.values()) {
            if (patient.id.equals(b.patientId))
                bills.add(b);
        }
        bills.sort(Comparator.comparing(b -> b.createdAt));
        for (Bill b : bills) {
            String method = b.paymentMethod == null ? "Not set" : b.paymentMethod.name();
            m.addRow(new Object[] { b.id, String.format(Locale.US, "%.2f", b.total), b.paid ? "Paid" : "Unpaid",
                    method, b.createdAt.toLocalDate(), b.updatedAt == null ? "" : b.updatedAt.toLocalDate() });
        }
        if (outstandingLabel != null) {
            double outstanding = getOutstandingBalance(bills);
            outstandingLabel.setText(outstanding <= 0 ? "No balance due"
                    : String.format(Locale.US, "Outstanding: $%.2f", outstanding));
        }
    }

    private double getOutstandingBalance(List<Bill> bills) {
        double total = 0.0;
        for (Bill b : bills) {
            if (!b.paid)
                total += b.total;
        }
        return total;
    }

    private String formatTestStatus(String status, String summary) {
        String base = (status == null || status.isEmpty()) ? "Not recorded" : status;
        if (summary != null && !summary.isEmpty())
            return base + " - " + summary;
        return base;
    }

    private String joinList(List<String> items, String emptyValue) {
        if (items == null || items.isEmpty())
            return emptyValue;
        return String.join("; ", items);
    }

    private String getPatientStatusText() {
        PatientStatus status = DataStore.patientStatus.get(patient.id);
        return status == null ? "Unknown" : status.name();
    }

    private String getAssignedDoctorDisplay() {
        LocalDateTime now = LocalDateTime.now();
        Appointment nearestFuture = null;
        Appointment latestPast = null;
        for (Appointment a : DataStore.appointments.values()) {
            if (!patient.id.equals(a.patientId))
                continue;
            if (a.dateTime.isAfter(now)) {
                if (nearestFuture == null || a.dateTime.isBefore(nearestFuture.dateTime))
                    nearestFuture = a;
            } else {
                if (latestPast == null || a.dateTime.isAfter(latestPast.dateTime))
                    latestPast = a;
            }
        }
        Appointment chosen = nearestFuture != null ? nearestFuture : latestPast;
        if (chosen == null)
            return "Not assigned";
        Staff doctor = DataStore.staff.get(chosen.staffId);
        String name = doctor != null && doctor.name != null ? doctor.name : chosen.staffId;
        if (doctor != null && doctor.department != null && !doctor.department.isEmpty())
            return name + " (" + doctor.department + ")";
        return name;
    }

    private JPanel createVisitsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("My Visits & Appointments");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        JButton scheduleBtn = new JButton("Schedule New Appointment");
        scheduleBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        scheduleBtn.setBackground(new Color(0, 102, 102));
        scheduleBtn.setForeground(Color.WHITE);
        scheduleBtn.setFocusPainted(false);
        scheduleBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        scheduleBtn.addActionListener(e -> openScheduleDialog());
        topBar.add(title, BorderLayout.WEST);
        topBar.add(scheduleBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        todayTable = new JTable(new javax.swing.table.DefaultTableModel(
                new String[] { "ID", "Doctor", "Date", "Time", "Department", "Status", "DoctorId" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        upcomingTable = new JTable(new javax.swing.table.DefaultTableModel(
                new String[] { "ID", "Doctor", "Date", "Time", "Department", "Status", "DoctorId" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        requestsTable = new JTable(new javax.swing.table.DefaultTableModel(
                new String[] { "ID", "Doctor", "Date", "Department", "Notes", "DoctorId" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        hideIdColumns(todayTable);
        hideIdColumns(upcomingTable);
        hideIdColumns(requestsTable);

        tabs.addTab("Today", new JScrollPane(todayTable));
        tabs.addTab("Upcoming", new JScrollPane(upcomingTable));
        tabs.addTab("Requests", new JScrollPane(requestsTable));
        panel.add(tabs, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewBtn = new JButton("View Details");
        JButton reqReschedBtn = new JButton("Request Reschedule");
        JButton cancelBtn = new JButton("Cancel Appointment");
        actions.add(viewBtn);
        actions.add(reqReschedBtn);
        actions.add(cancelBtn);
        panel.add(actions, BorderLayout.SOUTH);

        viewBtn.addActionListener(e -> viewAppointmentDetails());
        reqReschedBtn.addActionListener(e -> requestReschedule());
        cancelBtn.addActionListener(e -> cancelSelected());

        refreshTodayTable();
        refreshUpcomingTable();
        refreshRequestsTable();
        return panel;
    }

    private void openScheduleDialog() {
        JDialog dialog = new JDialog(this, "Schedule Appointment", true);
        dialog.setLayout(new BorderLayout());
        JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel doctorLabel = new JLabel("Doctor");
        form.add(doctorLabel, gbc);
        gbc.gridx = 1;
        java.util.List<String> doctorItems = new java.util.ArrayList<>();
        for (Staff s : DataStore.staff.values()) {
            if (s.role == StaffRole.DOCTOR) {
                String name = s.name == null ? s.id : s.name;
                doctorItems.add(s.id + " - " + name);
            }
        }
        JComboBox<String> doctorCombo = new JComboBox<>(doctorItems.toArray(new String[0]));
        form.add(doctorCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel deptLabel = new JLabel("Department");
        form.add(deptLabel, gbc);
        gbc.gridx = 1;
        JTextField deptField = new JTextField();
        deptField.setEditable(false);
        form.add(deptField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel dateLabel = new JLabel("Date");
        form.add(dateLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> dateCombo = new JComboBox<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            dateCombo.addItem(today.plusDays(i).toString());
        }
        form.add(dateCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel timeLabel = new JLabel("Time");
        form.add(timeLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> timeCombo = new JComboBox<>();
        form.add(timeCombo, gbc);

        Runnable refreshDerived = () -> {
            String docSel = (String) doctorCombo.getSelectedItem();
            String dateSel = (String) dateCombo.getSelectedItem();
            String docId = docSel == null ? null : docSel.split(" - ")[0];
            Staff doc = docId == null ? null : DataStore.staff.get(docId);
            deptField.setText(doc != null && doc.department != null ? doc.department : "");
            timeCombo.removeAllItems();
            if (docId != null && dateSel != null) {
                // Doctor availability scheduling removed - use default time slots
                for (int h = 9; h <= 17; h++) {
                    timeCombo.addItem(String.format(Locale.US, "%02d:00", h));
                }
            }
        };
        doctorCombo.addActionListener(e -> refreshDerived.run());
        dateCombo.addActionListener(e -> refreshDerived.run());
        if (doctorCombo.getItemCount() > 0)
            doctorCombo.setSelectedIndex(0);
        refreshDerived.run();

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        JButton schedule = new JButton("Schedule");
        schedule.setBackground(new Color(0, 102, 102));
        schedule.setForeground(Color.WHITE);
        schedule.setFocusPainted(false);
        actions.add(cancel);
        actions.add(schedule);
        cancel.addActionListener(e -> dialog.dispose());
        schedule.addActionListener(e -> {
            String docSel = (String) doctorCombo.getSelectedItem();
            String dateSel = (String) dateCombo.getSelectedItem();
            String timeSel = (String) timeCombo.getSelectedItem();
            String docId = docSel == null ? null : docSel.split(" - ")[0];
            String dept = deptField.getText();
            java.util.List<String> out = AppointmentService.schedule(patient.id, docId, dateSel, timeSel, dept);
            if (!out.isEmpty() && out.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(dialog, out.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (!out.isEmpty() && out.get(0).startsWith("Appointment created ")) {
                    String id = out.get(0).substring("Appointment created ".length()).trim();
                    Appointment a = hpms.util.DataStore.appointments.get(id);
                    if (a != null) {
                        a.notes = "Pending appointment request";
                        try {
                            hpms.util.BackupUtil.saveToDefault();
                        } catch (Exception ex) {
                        }
                    }
                }
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Appointment scheduled", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new hpms.ui.patient.PatientDashboardWindow(patient);
            }
        });
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setSize(520, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void hideIdColumns(JTable t) {
        if (t.getColumnModel().getColumnCount() > 0) {
            javax.swing.table.TableColumn c0 = t.getColumnModel().getColumn(0);
            c0.setMinWidth(0);
            c0.setMaxWidth(0);
            c0.setPreferredWidth(0);
        }
        if (t.getColumnModel().getColumnCount() > 6) {
            javax.swing.table.TableColumn c6 = t.getColumnModel().getColumn(6);
            c6.setMinWidth(0);
            c6.setMaxWidth(0);
            c6.setPreferredWidth(0);
        }
    }

    private String getAppointmentStatus(Appointment appt) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = appt.dateTime.plusHours(1);
        if (appt.notes != null && appt.notes.toLowerCase().contains("pending"))
            return "Pending";
        if (now.isBefore(appt.dateTime))
            return "Upcoming";
        if (now.isAfter(endTime))
            return "Completed";
        return "In Progress";
    }

    private void refreshTodayTable() {
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) todayTable.getModel();
        m.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Appointment appt : DataStore.appointments.values()) {
            if (appt.patientId.equals(patient.id) && appt.dateTime.toLocalDate().equals(today)) {
                if (appt.notes != null && appt.notes.toLowerCase().contains("pending"))
                    continue;
                Staff doctor = DataStore.staff.get(appt.staffId);
                String doctorName = doctor != null && doctor.name != null ? doctor.name : appt.staffId;
                m.addRow(new Object[] { appt.id, doctorName, appt.dateTime.toLocalDate().toString(),
                        String.format(Locale.US, "%02d:%02d", appt.dateTime.getHour(), appt.dateTime.getMinute()),
                        appt.department, getAppointmentStatus(appt), appt.staffId });
            }
        }
    }

    private void refreshUpcomingTable() {
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) upcomingTable.getModel();
        m.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Appointment appt : DataStore.appointments.values()) {
            if (appt.patientId.equals(patient.id) && appt.dateTime.toLocalDate().isAfter(today)) {
                if (appt.notes != null && appt.notes.toLowerCase().contains("pending"))
                    continue;
                Staff doctor = DataStore.staff.get(appt.staffId);
                String doctorName = doctor != null && doctor.name != null ? doctor.name : appt.staffId;
                m.addRow(new Object[] { appt.id, doctorName, appt.dateTime.toLocalDate().toString(),
                        String.format(Locale.US, "%02d:%02d", appt.dateTime.getHour(), appt.dateTime.getMinute()),
                        appt.department, getAppointmentStatus(appt), appt.staffId });
            }
        }
    }

    private void refreshRequestsTable() {
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) requestsTable.getModel();
        m.setRowCount(0);
        java.util.List<Appointment> pending = new java.util.ArrayList<>();
        for (Appointment a : DataStore.appointments.values()) {
            if (patient.id.equals(a.patientId) && a.notes != null && a.notes.toLowerCase().contains("pending")) {
                pending.add(a);
            }
        }
        pending.sort((a, b) -> a.dateTime.compareTo(b.dateTime));
        for (Appointment a : pending) {
            Staff doctor = DataStore.staff.get(a.staffId);
            String doctorName = doctor != null && doctor.name != null ? doctor.name : a.staffId;
            m.addRow(new Object[] { a.id, doctorName, a.dateTime.toString(), a.department, a.notes, a.staffId });
        }
    }

    private String selectedIdFrom(JTable t) {
        int r = t.getSelectedRow();
        if (r < 0)
            return null;
        return String.valueOf(t.getValueAt(r, 0));
    }

    private String selectedDoctorIdFrom(JTable t) {
        int r = t.getSelectedRow();
        if (r < 0)
            return null;
        int idx = t.getModel().getColumnCount() - 1;
        return String.valueOf(t.getValueAt(r, idx));
    }

    private void viewAppointmentDetails() {
        JTable sel = todayTable.getSelectedRow() >= 0 ? todayTable
                : (upcomingTable.getSelectedRow() >= 0 ? upcomingTable
                        : (requestsTable.getSelectedRow() >= 0 ? requestsTable : null));
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an appointment", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = sel.getSelectedRow();
        String id = String.valueOf(sel.getValueAt(row, 0));
        Appointment a = DataStore.appointments.get(id);
        if (a == null) {
            JOptionPane.showMessageDialog(this, "Appointment not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Patient p = DataStore.patients.get(a.patientId);
        Staff d = DataStore.staff.get(a.staffId);
        String details = "Appointment Details:\n\n" +
                "ID: " + a.id + "\n" +
                "Patient: " + (p != null ? p.name : a.patientId) + "\n" +
                "Doctor: " + (d != null && d.name != null ? d.name : a.staffId) + "\n" +
                "Date: " + a.dateTime.toLocalDate() + "\n" +
                "Time: " + String.format(Locale.US, "%02d:%02d", a.dateTime.getHour(), a.dateTime.getMinute()) + "\n" +
                "Department: " + a.department + "\n" +
                "Status: " + getAppointmentStatus(a) + "\n" +
                "Notes: " + (a.notes == null ? "" : a.notes);
        JOptionPane.showMessageDialog(this, details, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void requestReschedule() {
        JTable sel = todayTable.getSelectedRow() >= 0 ? todayTable
                : (upcomingTable.getSelectedRow() >= 0 ? upcomingTable : null);
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an appointment in Today or Upcoming", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = selectedIdFrom(sel);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a valid row", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String doctorId = selectedDoctorIdFrom(sel);
        JDialog dialog = new JDialog(this, "Request Reschedule", true);
        dialog.setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("New Date"), gbc);
        gbc.gridx = 1;
        JComboBox<String> dateCombo = new JComboBox<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++)
            dateCombo.addItem(today.plusDays(i).toString());
        form.add(dateCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("New Time"), gbc);
        gbc.gridx = 1;
        JComboBox<String> timeCombo = new JComboBox<>();
        form.add(timeCombo, gbc);
        Runnable refreshTimes = () -> {
            String ds = (String) dateCombo.getSelectedItem();
            timeCombo.removeAllItems();
            if (doctorId != null && ds != null) {
                for (int h = 9; h <= 17; h++)
                    timeCombo.addItem(String.format(Locale.US, "%02d:00", h));
            }
        };
        dateCombo.addActionListener(e -> refreshTimes.run());
        refreshTimes.run();
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        JButton submit = new JButton("Submit Request");
        actions.add(cancel);
        actions.add(submit);
        cancel.addActionListener(e -> dialog.dispose());
        submit.addActionListener(e -> {
            String ds = (String) dateCombo.getSelectedItem();
            String ts = (String) timeCombo.getSelectedItem();
            Appointment a = DataStore.appointments.get(id);
            if (a != null) {
                a.notes = "Pending reschedule to " + ds + " " + ts;
                try {
                    BackupUtil.saveToDefault();
                } catch (Exception ex) {
                }
                JOptionPane.showMessageDialog(this, "Reschedule request sent", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshRequestsTable();
                dialog.dispose();
            }
        });
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setSize(420, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void cancelSelected() {
        JTable sel = todayTable.getSelectedRow() >= 0 ? todayTable
                : (upcomingTable.getSelectedRow() >= 0 ? upcomingTable
                        : (requestsTable.getSelectedRow() >= 0 ? requestsTable : null));
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an appointment", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = selectedIdFrom(sel);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a valid row", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.util.List<String> out = AppointmentService.cancel(id);
        if (!out.isEmpty() && out.get(0).startsWith("Error:")) {
            JOptionPane.showMessageDialog(this, out.get(0), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Appointment canceled", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTodayTable();
            refreshUpcomingTable();
            refreshRequestsTable();
        }
    }

    private JPanel createInsurancePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        JLabel title = new JLabel("Insurance");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(createReadOnlyField("Provider:",
                patient.insuranceProvider == null ? "None" : patient.insuranceProvider));
        panel.add(createReadOnlyField("Policy ID:",
                patient.insuranceId == null ? "N/A" : maskInsuranceId(patient.insuranceId)));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createRequestsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        JLabel title = new JLabel("My Requests");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        java.util.List<Appointment> pending = new java.util.ArrayList<>();
        for (Appointment a : DataStore.appointments.values()) {
            if (patient.id.equals(a.patientId) && a.notes != null && a.notes.toLowerCase().contains("pending")) {
                pending.add(a);
            }
        }
        pending.sort((a, b) -> a.dateTime.compareTo(b.dateTime));

        if (pending.isEmpty()) {
            JLabel noRequests = new JLabel("No pending requests");
            noRequests.setFont(new Font("Arial", Font.ITALIC, 12));
            noRequests.setForeground(new Color(100, 100, 100));
            noRequests.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(noRequests);
        } else {
            for (Appointment a : pending) {
                JPanel card = new JPanel(new GridLayout(0, 2));
                card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
                card.setBackground(Color.WHITE);
                card.add(new JLabel("Request ID:"));
                card.add(new JLabel(a.id));
                card.add(new JLabel("Requested Date:"));
                card.add(new JLabel(a.dateTime.toString()));
                card.add(new JLabel("Department:"));
                card.add(new JLabel(a.department));
                Staff st = DataStore.staff.get(a.staffId);
                String dname = st == null ? a.staffId : (st.name == null ? a.staffId : st.name);
                card.add(new JLabel("Doctor:"));
                card.add(new JLabel(dname));
                card.add(new JLabel("Status:"));
                card.add(new JLabel("Pending"));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(card);
                panel.add(Box.createVerticalStrut(10));
            }
        }
        panel.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(panel);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMedicalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("My Medical Records");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        panel.add(createSectionHeader("🩺 Care Overview"));
        panel.add(createReadOnlyField("Status:", getPatientStatusText()));
        panel.add(createReadOnlyField("Patient Type:", patient.patientType == null ? "" : patient.patientType));
        panel.add(createReadOnlyField("Assigned Doctor:", getAssignedDoctorDisplay()));

        panel.add(Box.createVerticalStrut(15));
        panel.add(createSectionHeader("📈 Vitals"));
        panel.add(createReadOnlyField("Height (cm):", patient.heightCm == null ? "Not recorded"
                : String.format(Locale.US, "%.1f", patient.heightCm)));
        panel.add(createReadOnlyField("Weight (kg):", patient.weightKg == null ? "Not recorded"
                : String.format(Locale.US, "%.1f", patient.weightKg)));
        Double bmi = patient.getBmi();
        panel.add(createReadOnlyField("BMI:", bmi == null ? "Not available" : String.format(Locale.US, "%.2f", bmi)));
        panel.add(createReadOnlyField("Blood Pressure:", patient.bloodPressure == null ? "" : patient.bloodPressure));

        panel.add(Box.createVerticalStrut(15));
        panel.add(createSectionHeader("🧪 Tests & Imaging"));
        panel.add(createReadOnlyField("X-Ray:", formatTestStatus(patient.xrayStatus, patient.xraySummary)));
        panel.add(createReadOnlyField("Stool Test:", formatTestStatus(patient.stoolStatus, patient.stoolSummary)));
        panel.add(createReadOnlyField("Urine Test:", formatTestStatus(patient.urineStatus, patient.urineSummary)));
        panel.add(createReadOnlyField("Blood Test:", formatTestStatus(patient.bloodStatus, patient.bloodSummary)));

        panel.add(Box.createVerticalStrut(15));
        panel.add(createSectionHeader("📄 Clinical Notes"));
        panel.add(createReadOnlyField("Progress Notes:", joinList(patient.progressNotes, "No notes on file")));
        panel.add(createReadOnlyField("Diagnoses:", joinList(patient.diagnoses, "No diagnoses on file")));
        panel.add(createReadOnlyField("Treatment Plans:", joinList(patient.treatmentPlans, "No plans recorded")));
        panel.add(createReadOnlyField("Discharge Summaries:",
                joinList(patient.dischargeSummaries, "No discharge summaries")));

        panel.add(Box.createVerticalStrut(15));
        panel.add(createSectionHeader("📎 Attachments"));
        int attachmentCount = patient.fileAttachments == null ? 0 : patient.fileAttachments.size();
        panel.add(createReadOnlyField("Files Uploaded:",
                attachmentCount == 0 ? "No attachments" : attachmentCount + " file(s)"));

        panel.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(panel);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Billing & Payments");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        top.add(title, BorderLayout.WEST);

        JLabel outstanding = new JLabel();
        outstanding.setFont(new Font("Arial", Font.PLAIN, 13));
        outstanding.setForeground(new Color(120, 0, 0));
        top.add(outstanding, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        billingTable = new JTable(new javax.swing.table.DefaultTableModel(
                new String[] { "Bill ID", "Total", "Paid", "Method", "Created", "Updated" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        JScrollPane scroll = new JScrollPane(billingTable);
        panel.add(scroll, BorderLayout.CENTER);

        refreshBillingTable(outstanding);
        return panel;
    }

    private JPanel createSectionHeader(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(l, BorderLayout.WEST);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createReadOnlyField(String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(180, 20));
        JLabel v = new JLabel(value == null ? "" : value);
        p.add(l);
        p.add(v);
        return p;
    }

    private String maskInsuranceId(String id) {
        if (id == null || id.length() < 4)
            return "****";
        return id.substring(0, 2) + "****" + id.substring(Math.max(0, id.length() - 2));
    }

    private void switchTab(String tabName) {
        Color idleColor = new Color(60, 60, 60);
        profileTab.setOpaque(false);
        profileTab.setForeground(idleColor);
        visitsTab.setOpaque(false);
        visitsTab.setForeground(idleColor);
        medicalTab.setOpaque(false);
        medicalTab.setForeground(idleColor);
        billingTab.setOpaque(false);
        billingTab.setForeground(idleColor);
        insuranceTab.setOpaque(false);
        insuranceTab.setForeground(idleColor);
        requestsTab.setOpaque(false);
        requestsTab.setForeground(idleColor);

        switch (tabName) {
            case "profile":
                profileTab.setOpaque(true);
                profileTab.setBackground(new Color(0, 102, 102));
                profileTab.setForeground(Color.WHITE);
                break;
            case "visits":
                visitsTab.setOpaque(true);
                visitsTab.setBackground(new Color(0, 102, 102));
                visitsTab.setForeground(Color.WHITE);
                break;
            case "medical":
                medicalTab.setOpaque(true);
                medicalTab.setBackground(new Color(0, 102, 102));
                medicalTab.setForeground(Color.WHITE);
                break;
            case "billing":
                billingTab.setOpaque(true);
                billingTab.setBackground(new Color(0, 102, 102));
                billingTab.setForeground(Color.WHITE);
                break;
            case "insurance":
                insuranceTab.setOpaque(true);
                insuranceTab.setBackground(new Color(0, 102, 102));
                insuranceTab.setForeground(Color.WHITE);
                break;
            case "requests":
                requestsTab.setOpaque(true);
                requestsTab.setBackground(new Color(0, 102, 102));
                requestsTab.setForeground(Color.WHITE);
                break;
        }

        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, tabName);
    }
}
