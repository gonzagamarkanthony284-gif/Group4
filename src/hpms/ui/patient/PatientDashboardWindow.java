package hpms.ui.patient;

import hpms.model.*;
import hpms.service.AppointmentService;
import hpms.service.BillingService;
import hpms.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PatientDashboardWindow extends JFrame {
    private Patient patient;
    private JPanel contentPanel;
    private JLabel profileTab, visitsTab, insuranceTab, requestsTab, medicalTab, billingTab, documentsTab, doctorProfileTab;
    private JTable todayTable, upcomingTable, requestsTable, historyTable, billingTable;

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
        contentPanel.add(createDocumentsPanel(), "documents");
        contentPanel.add(createBillingPanel(), "billing");
        contentPanel.add(createInsurancePanel(), "insurance");
        contentPanel.add(createRequestsPanel(), "requests");
        contentPanel.add(createDoctorProfilePanel(), "doctorProfile");
        
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
        documentsTab = createTabButton("📁 Documents", "documents");
        billingTab = createTabButton("💵 Billing", "billing");
        insuranceTab = createTabButton("💳 Insurance", "insurance");
        doctorProfileTab = createTabButton("👨‍⚕️ Doctor Profile", "doctorProfile");
        requestsTab = createTabButton("📝 Requests", "requests");

        sidebar.add(profileTab);
        sidebar.add(visitsTab);
        sidebar.add(medicalTab);
        sidebar.add(documentsTab);
        sidebar.add(billingTab);
        sidebar.add(insuranceTab);
        sidebar.add(requestsTab);
        sidebar.add(doctorProfileTab);
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
        panel.add(createFieldPanel("Assigned Doctor:", createAssignedDoctorPanel()));
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
            String status;
            if (b.paid) {
                status = "Paid";
            } else if (b.paymentMethod != null) {
                status = "Pending Confirmation";
            } else {
                status = "Unpaid";
            }
            m.addRow(new Object[] { b.id, String.format(Locale.US, "%.2f", b.total), status,
                    method, b.createdAt.toLocalDate(), b.updatedAt == null ? "" : b.updatedAt.toLocalDate() });
        }
        if (outstandingLabel != null) {
            double outstanding = getOutstandingBalance(bills);
            outstandingLabel.setText(outstanding <= 0 ? "No balance due"
                    : String.format(Locale.US, "Outstanding: $%.2f", outstanding));
        }
    }

    private void makePayment() {
        // Get selected bill
        int selectedRow = billingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill to pay", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = String.valueOf(billingTable.getValueAt(selectedRow, 0));
        Bill bill = DataStore.bills.get(billId);
        
        if (bill == null) {
            JOptionPane.showMessageDialog(this, "Invalid bill selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (bill.paid) {
            JOptionPane.showMessageDialog(this, "This bill is already paid", "Payment Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create payment dialog
        JDialog paymentDialog = new JDialog(this, "Make Payment - " + billId, true);
        paymentDialog.setLayout(new BorderLayout(10, 10));
        paymentDialog.setSize(400, 300);
        paymentDialog.setLocationRelativeTo(this);

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        infoPanel.add(new JLabel("Bill ID:"));
        infoPanel.add(new JLabel(bill.id));
        
        infoPanel.add(new JLabel("Amount:"));
        infoPanel.add(new JLabel(String.format(Locale.US, "$%.2f", bill.total)));
        
        infoPanel.add(new JLabel("Payment Method:"));
        JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{"CASH", "CARD", "INSURANCE"});
        infoPanel.add(paymentMethodCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton submitBtn = new JButton("Submit Payment");
        submitBtn.setBackground(new Color(34, 197, 94));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> {
            String method = (String) paymentMethodCombo.getSelectedItem();
            
            // Create payment record as pending confirmation
            List<String> result = BillingService.initiatePayment(bill.id, method);
            
            if (!result.isEmpty() && result.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(paymentDialog, result.get(0), "Payment Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(paymentDialog, "Payment submitted for confirmation!\n\nYour payment will be confirmed by cashier or admin.", "Payment Submitted", JOptionPane.INFORMATION_MESSAGE);
                paymentDialog.dispose();
                // Refresh billing table
                JLabel outstandingLabel = new JLabel();
                refreshBillingTable(outstandingLabel);
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> paymentDialog.dispose());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);

        paymentDialog.add(infoPanel, BorderLayout.CENTER);
        paymentDialog.add(buttonPanel, BorderLayout.SOUTH);
        paymentDialog.setVisible(true);
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
            } else if (latestPast == null || a.dateTime.isAfter(latestPast.dateTime)) {
                latestPast = a;
            }
        }
        Appointment chosen = (nearestFuture != null) ? nearestFuture : latestPast;
        if (chosen == null)
            return "Not assigned";
        Staff doctor = DataStore.staff.get(chosen.staffId);
        String name = doctor != null && doctor.name != null ? doctor.name : chosen.staffId;
        if (doctor != null && doctor.department != null && !doctor.department.isEmpty())
            return name + " (" + doctor.department + ")";
        return name;
    }
    
    private JPanel createAssignedDoctorPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        
        String assignedDoctor = getAssignedDoctorDisplay();
        if (!assignedDoctor.equals("Not assigned")) {
            // Get doctor ID from appointments
            final String doctorId;
            LocalDateTime now = LocalDateTime.now();
            Appointment nearestFuture = null;
            Appointment latestPast = null;
            for (Appointment a : DataStore.appointments.values()) {
                if (!patient.id.equals(a.patientId))
                    continue;
                if (a.dateTime.isAfter(now)) {
                    if (nearestFuture == null || a.dateTime.isBefore(nearestFuture.dateTime))
                        nearestFuture = a;
                } else if (latestPast == null || a.dateTime.isAfter(latestPast.dateTime)) {
                    latestPast = a;
                }
            }
            Appointment chosen = (nearestFuture != null) ? nearestFuture : latestPast;
            if (chosen != null) {
                doctorId = chosen.staffId;
            } else {
                doctorId = null;
            }
            
            if (doctorId != null) {
                JLabel doctorLabel = new JLabel("<html><u style='color: blue'>" + assignedDoctor + "</u></html>");
                doctorLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                doctorLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        new DoctorProfileViewDialog(PatientDashboardWindow.this, doctorId).setVisible(true);
                    }
                });
                panel.add(doctorLabel);
            } else {
                panel.add(new JLabel(assignedDoctor));
            }
        } else {
            panel.add(new JLabel(assignedDoctor));
        }
        
        return panel;
    }

    private JPanel createVisitsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

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
        historyTable = new JTable(new javax.swing.table.DefaultTableModel(
                new String[] { "ID", "Doctor", "Date", "Time", "Department", "Status", "DoctorId" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        hideIdColumns(todayTable);
        hideIdColumns(upcomingTable);
        hideIdColumns(requestsTable);
        hideIdColumns(historyTable);
        
        // Add mouse listeners for clickable doctor names
        addDoctorClickListener(todayTable);
        addDoctorClickListener(upcomingTable);
        addDoctorClickListener(requestsTable);
        addDoctorClickListener(historyTable);

        tabs.addTab("Today", new JScrollPane(todayTable));
        tabs.addTab("Upcoming", new JScrollPane(upcomingTable));
        tabs.addTab("History", new JScrollPane(historyTable));
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
        refreshHistoryTable();
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
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date()); // Set to today's date
        form.add(dateSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel timeLabel = new JLabel("Time");
        form.add(timeLabel, gbc);
        gbc.gridx = 1;
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        form.add(timeSpinner, gbc);

        Runnable refreshDerived = () -> {
            String docSel = (String) doctorCombo.getSelectedItem();
            String docId = docSel == null ? null : docSel.split(" - ")[0];
            Staff doc = docId == null ? null : DataStore.staff.get(docId);
            deptField.setText(doc != null && doc.department != null ? doc.department : "");
            timeSpinner.setValue(new java.util.Date()); // Reset to current time
        };
        doctorCombo.addActionListener(e -> refreshDerived.run());
        dateSpinner.addChangeListener(e -> refreshDerived.run());
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
            // Get date from spinner
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String dateSel = dateFormat.format(selectedDate);
            
            // Get time from spinner
            java.util.Date selectedTime = (java.util.Date) timeSpinner.getValue();
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
            String timeSel = timeFormat.format(selectedTime);
            
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
                        // Disabled backup save - using database instead
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

    private void refreshHistoryTable() {
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) historyTable.getModel();
        m.setRowCount(0);
        LocalDate today = LocalDate.now();
        java.util.List<Appointment> pastAppointments = new java.util.ArrayList<>();
        for (Appointment appt : DataStore.appointments.values()) {
            if (appt.patientId.equals(patient.id) && appt.dateTime.toLocalDate().isBefore(today)) {
                if (appt.notes != null && appt.notes.toLowerCase().contains("pending"))
                    continue;
                pastAppointments.add(appt);
            }
        }
        // Sort by date descending (most recent first)
        pastAppointments.sort((a, b) -> b.dateTime.compareTo(a.dateTime));
        for (Appointment appt : pastAppointments) {
            Staff doctor = DataStore.staff.get(appt.staffId);
            String doctorName = doctor != null && doctor.name != null ? doctor.name : appt.staffId;
            m.addRow(new Object[] { appt.id, doctorName, appt.dateTime.toLocalDate().toString(),
                    appt.dateTime.toLocalTime().toString(), appt.department, getAppointmentStatus(appt), appt.staffId });
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
    
    private void addDoctorClickListener(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                
                // Check if click is on the Doctor column (column index 1)
                if (column == 1 && row >= 0) {
                    String doctorId = selectedDoctorIdFrom(table);
                    if (doctorId != null && !doctorId.isEmpty()) {
                        // Open doctor profile dialog
                        new DoctorProfileViewDialog(PatientDashboardWindow.this, doctorId).setVisible(true);
                    }
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                
                // Change cursor to hand when hovering over doctor names
                if (column == 1 && row >= 0) {
                    table.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
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
                // Disabled backup save - using database instead
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
            refreshHistoryTable();
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
        panel.add(createFieldPanel("Assigned Doctor:", createAssignedDoctorPanel()));

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

        // Payment buttons panel
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        paymentPanel.setOpaque(false);
        
        JButton payBtn = new JButton("Make Payment");
        payBtn.setBackground(new Color(34, 197, 94));
        payBtn.setForeground(Color.WHITE);
        payBtn.setFocusPainted(false);
        payBtn.addActionListener(e -> makePayment());
        
        paymentPanel.add(payBtn);
        panel.add(paymentPanel, BorderLayout.NORTH);

        billingTable = new JTable(new javax.swing.table.DefaultTableModel(
                new String[] { "Bill ID", "Total", "Status", "Method", "Created", "Updated" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        JScrollPane scroll = new JScrollPane(billingTable);
        panel.add(scroll, BorderLayout.CENTER);

        refreshBillingTable(outstanding);
        return panel;
    }

    private JPanel createDocumentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Medical Documents");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(31, 41, 55));
        panel.add(title, BorderLayout.NORTH);

        // Use the existing MedicalDocumentFolderPanel
        hpms.ui.components.MedicalDocumentFolderPanel documentsPanel = 
            new hpms.ui.components.MedicalDocumentFolderPanel(patient.id);
        
        // Remove upload functionality for patients (view-only)
        documentsPanel.getUploadButton().setVisible(false);
        
        panel.add(documentsPanel, BorderLayout.CENTER);
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

    private JPanel createFieldPanel(String labelText, JComponent valueComponent) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(31, 41, 55));
        label.setPreferredSize(new Dimension(120, 20));
        
        panel.add(label, BorderLayout.WEST);
        panel.add(valueComponent, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createReadOnlyField(String labelText, String valueText) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setPreferredSize(new Dimension(180, 20));
        JLabel v = new JLabel(valueText == null ? "" : valueText);
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
        // Reset all tabs
        Color idleColor = new Color(60, 60, 60);
        profileTab.setOpaque(false);
        profileTab.setForeground(idleColor);
        visitsTab.setOpaque(false);
        visitsTab.setForeground(idleColor);
        medicalTab.setOpaque(false);
        medicalTab.setForeground(idleColor);
        documentsTab.setOpaque(false);
        documentsTab.setForeground(idleColor);
        billingTab.setOpaque(false);
        billingTab.setForeground(idleColor);
        insuranceTab.setOpaque(false);
        insuranceTab.setForeground(idleColor);
        requestsTab.setOpaque(false);
        requestsTab.setForeground(idleColor);
        doctorProfileTab.setOpaque(false);
        doctorProfileTab.setForeground(idleColor);

        switch (tabName) {
            case "profile":
                profileTab.setOpaque(true);
                profileTab.setBackground(new Color(0, 102, 102));
                profileTab.setForeground(Color.WHITE);
                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "profile");
                break;
            
            case "doctorProfile":
                doctorProfileTab.setOpaque(true);
                doctorProfileTab.setBackground(new Color(0, 102, 102));
                doctorProfileTab.setForeground(Color.WHITE);
                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "doctorProfile");
                break;
            case "visits":
                visitsTab.setOpaque(true);
                visitsTab.setBackground(new Color(0, 102, 102));
                visitsTab.setForeground(Color.WHITE);
                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "visits");
                break;
            case "medical":
                medicalTab.setOpaque(true);
                medicalTab.setBackground(new Color(0, 102, 102));
                medicalTab.setForeground(Color.WHITE);
                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "medical");
                break;
            case "documents":
                documentsTab.setOpaque(true);
                documentsTab.setBackground(new Color(0, 102, 102));
                documentsTab.setForeground(Color.WHITE);
                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "documents");
                break;
            case "billing":
                billingTab.setOpaque(true);
                billingTab.setBackground(new Color(0, 102, 102));
                billingTab.setForeground(Color.WHITE);
                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "billing");
                break;
            case "insurance":
                insuranceTab.setOpaque(true);
                insuranceTab.setBackground(new Color(0, 102, 102));
                insuranceTab.setForeground(Color.WHITE);
                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "insurance");
                break;
            case "requests":
                requestsTab.setOpaque(true);
                requestsTab.setBackground(new Color(0, 102, 102));
                requestsTab.setForeground(Color.WHITE);
                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "requests");
                break;
        }
    }
    
    private String getAssignedDoctorId() {
        List<Appointment> appointments = new ArrayList<>();
        for (Appointment appt : DataStore.appointments.values()) {
            if (appt.patientId.equals(patient.id)) {
                appointments.add(appt);
            }
        }
        if (appointments == null || appointments.isEmpty()) {
            return null;
        }
        
        Appointment mostRecent = null;
        for (Appointment appt : appointments) {
            if (appt.staffId != null && !appt.staffId.isEmpty()) {
                if (mostRecent == null || appt.dateTime.isAfter(mostRecent.dateTime)) {
                    mostRecent = appt;
                }
            }
        }
        return mostRecent != null ? mostRecent.staffId : null;
    }
    
    private JPanel createDoctorProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Doctor's Public Profile");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        java.util.List<Staff> doctors = hpms.service.DoctorService.getAllDoctors();
        if (doctors == null || doctors.isEmpty()) {
            panel.add(new JLabel("No doctors available."), BorderLayout.CENTER);
            return panel;
        }

        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[] { "Name", "Department", "Specialty", "Id" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Staff d : doctors) {
            model.addRow(new Object[] {
                    d.name != null ? d.name : "",
                    d.department != null ? d.department : "",
                    d.specialty != null ? d.specialty : "",
                    d.id
            });
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);

        // Hide ID column
        javax.swing.table.TableColumn idCol = table.getColumnModel().getColumn(3);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        JScrollPane listScroll = new JScrollPane(table);
        listScroll.setBorder(BorderFactory.createTitledBorder("All Doctors"));
        listScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel profileHost = new JPanel(new BorderLayout());
        profileHost.setBackground(Color.WHITE);
        profileHost.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        Runnable refreshSelected = () -> {
            int r = table.getSelectedRow();
            if (r < 0) return;
            String doctorId = String.valueOf(table.getValueAt(r, 3));
            Staff selected = hpms.service.DoctorService.getDoctorById(doctorId);
            profileHost.removeAll();
            if (selected != null) {
                profileHost.add(new hpms.ui.doctor.DoctorPublicProfilePanel(selected), BorderLayout.CENTER);
            } else {
                profileHost.add(new JLabel("Doctor not found or inactive."), BorderLayout.CENTER);
            }
            profileHost.revalidate();
            profileHost.repaint();
        };

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshSelected.run();
            }
        });

        // Default selection: assigned doctor if present, else first.
        String assignedDoctorId = getAssignedDoctorId();
        int selectRow = 0;
        if (assignedDoctorId != null && !assignedDoctorId.isEmpty()) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (assignedDoctorId.equals(String.valueOf(model.getValueAt(i, 3)))) {
                    selectRow = i;
                    break;
                }
            }
        }

        table.setRowSelectionInterval(selectRow, selectRow);
        refreshSelected.run();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, profileHost);
        split.setResizeWeight(0.30);
        split.setDividerLocation(280);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }
}
