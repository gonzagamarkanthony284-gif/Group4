package hpms.ui.panels;

import hpms.model.*;
import hpms.service.AppointmentService;
import hpms.util.*;
import hpms.ui.components.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppointmentsPanel extends JPanel {
    private DefaultTableModel todayModel, upcomingModel, pendingModel;
    private JTable todayTable, upcomingTable, pendingTable;
    private JLabel todayCount, upcomingCount, pendingCount;
    private JComboBox<String> doctorCombo;

    public AppointmentsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header with doctor selector
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Three-section main content
        JPanel mainContent = createMainContentPanel();
        add(mainContent, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);

        refresh();

        // Refresh when shown
        this.addHierarchyListener(evt -> {
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing())
                    SwingUtilities.invokeLater(this::refresh);
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));

        JLabel titleLabel = new JLabel("Appointment Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Theme.FOREGROUND);
        panel.add(titleLabel, BorderLayout.WEST);

        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        selectorPanel.setBackground(Theme.BACKGROUND);
        JLabel doctorLabel = new JLabel("Filter Doctor:");
        doctorLabel.setForeground(Theme.FOREGROUND);
        doctorLabel.setFont(new Font("Arial", Font.BOLD, 12));

        doctorCombo = new JComboBox<>();
        doctorCombo.addItem("-- All Doctors --");
        for (Staff staff : DataStore.staff.values()) {
            if (staff.role.name().equalsIgnoreCase("DOCTOR")) {
                doctorCombo.addItem(staff.id + " - " + staff.name);
            }
        }
        doctorCombo.setPreferredSize(new Dimension(250, 28));
        doctorCombo.setBackground(Color.WHITE);
        doctorCombo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        doctorCombo.addActionListener(e -> refresh());

        selectorPanel.add(doctorLabel);
        selectorPanel.add(doctorCombo);
        panel.add(selectorPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBackground(Theme.BACKGROUND);

        panel.add(createSectionPanel("Today's Appointments", createTodayTable(),
                todayCount = new JLabel("0 appointments")));
        panel.add(createSectionPanel("Upcoming Appointments (Next 7 Days)", createUpcomingTable(),
                upcomingCount = new JLabel("0 appointments")));
        panel.add(createSectionPanel("Pending Appointments (Awaiting Confirmation)", createPendingTable(),
                pendingCount = new JLabel("0 pending")));

        return panel;
    }

    private JPanel createSectionPanel(String title, JScrollPane table, JLabel countLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(Theme.PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        countLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(countLabel, BorderLayout.EAST);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(table, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createTodayTable() {
        String[] columns = { "Patient", "Doctor", "Time", "Department", "Status" };
        todayModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        todayTable = new JTable(todayModel);
        todayTable.setFont(new Font("Arial", Font.PLAIN, 11));
        todayTable.setRowHeight(24);
        todayTable.getTableHeader().setBackground(new Color(220, 240, 255));
        todayTable.getTableHeader().setForeground(Theme.PRIMARY);
        todayTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(todayTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return scrollPane;
    }

    private JScrollPane createUpcomingTable() {
        String[] columns = { "Date", "Patient", "Doctor", "Time", "Department", "Status" };
        upcomingModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        upcomingTable = new JTable(upcomingModel);
        upcomingTable.setFont(new Font("Arial", Font.PLAIN, 11));
        upcomingTable.setRowHeight(24);
        upcomingTable.getTableHeader().setBackground(new Color(220, 240, 255));
        upcomingTable.getTableHeader().setForeground(Theme.PRIMARY);
        upcomingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(upcomingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return scrollPane;
    }

    private JScrollPane createPendingTable() {
        String[] columns = { "Patient", "Doctor", "Requested DateTime", "Department", "Notes", "Status" };
        pendingModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pendingTable = new JTable(pendingModel);
        pendingTable.setFont(new Font("Arial", Font.PLAIN, 11));
        pendingTable.setRowHeight(24);
        pendingTable.getTableHeader().setBackground(new Color(255, 240, 220));
        pendingTable.getTableHeader().setForeground(new Color(200, 100, 0));
        pendingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(pendingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return scrollPane;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JButton scheduleBtn = createStyledButton("Schedule New", Theme.PRIMARY);
        scheduleBtn.addActionListener(e -> scheduleAppointmentDialog());

        JButton rescheduleBtn = createStyledButton("Reschedule", new Color(33, 150, 243));
        rescheduleBtn.addActionListener(e -> rescheduleAppointmentDialog());

        JButton cancelBtn = createStyledButton("Cancel", new Color(244, 67, 54));
        cancelBtn.addActionListener(e -> cancelAppointment());

        JButton confirmBtn = createStyledButton("Confirm", new Color(76, 175, 80));
        confirmBtn.addActionListener(e -> confirmPendingAppointment());

        JButton viewDetailsBtn = createStyledButton("View Details", new Color(156, 39, 176));
        viewDetailsBtn.addActionListener(e -> viewAppointmentDetails());

        JButton refreshBtn = createStyledButton("Refresh", new Color(155, 155, 155));
        refreshBtn.addActionListener(e -> refresh());

        panel.add(scheduleBtn);
        panel.add(rescheduleBtn);
        panel.add(cancelBtn);
        panel.add(confirmBtn);
        panel.add(new JSeparator(JSeparator.VERTICAL));
        panel.add(viewDetailsBtn);
        panel.add(refreshBtn);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void refresh() {
        refreshTodayTable();
        refreshUpcomingTable();
        refreshPendingTable();
    }

    private void refreshTodayTable() {
        todayModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        int count = 0;

        String selectedDoctorId = getSelectedDoctorId();
        for (Appointment appt : DataStore.appointments.values()) {
            if (appt.dateTime.toLocalDate().equals(today)) {
                if (selectedDoctorId == null || appt.staffId.equals(selectedDoctorId)) {
                    Staff doctor = DataStore.staff.get(appt.staffId);
                    String doctorName = doctor != null ? doctor.name : appt.staffId;
                    Patient patient = DataStore.patients.get(appt.patientId);
                    String patientName = patient != null ? patient.name : appt.patientId;
                    todayModel.addRow(new Object[] {
                            patientName,
                            doctorName,
                            appt.dateTime.toLocalTime(),
                            appt.department,
                            getAppointmentStatus(appt)
                    });
                    count++;
                }
            }
        }
        todayCount.setText(count + " appointments");
    }

    private void refreshUpcomingTable() {
        upcomingModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        int count = 0;

        String selectedDoctorId = getSelectedDoctorId();
        for (Appointment appt : DataStore.appointments.values()) {
            LocalDate apptDate = appt.dateTime.toLocalDate();
            if (apptDate.isAfter(today) && apptDate.isBefore(nextWeek)) {
                if (selectedDoctorId == null || appt.staffId.equals(selectedDoctorId)) {
                    Staff doctor = DataStore.staff.get(appt.staffId);
                    String doctorName = doctor != null ? doctor.name : appt.staffId;
                    Patient patient = DataStore.patients.get(appt.patientId);
                    String patientName = patient != null ? patient.name : appt.patientId;
                    upcomingModel.addRow(new Object[] {
                            apptDate,
                            patientName,
                            doctorName,
                            appt.dateTime.toLocalTime(),
                            appt.department,
                            getAppointmentStatus(appt)
                    });
                    count++;
                }
            }
        }
        upcomingCount.setText(count + " appointments");
    }

    private void refreshPendingTable() {
        pendingModel.setRowCount(0);
        int count = 0;

        String selectedDoctorId = getSelectedDoctorId();
        for (Appointment appt : DataStore.appointments.values()) {
            if (!appt.isCompleted && appt.notes.toLowerCase().contains("pending")) {
                if (selectedDoctorId == null || appt.staffId.equals(selectedDoctorId)) {
                    Staff doctor = DataStore.staff.get(appt.staffId);
                    String doctorName = doctor != null ? doctor.name : appt.staffId;
                    Patient patient = DataStore.patients.get(appt.patientId);
                    String patientName = patient != null ? patient.name : appt.patientId;
                    pendingModel.addRow(new Object[] {
                            patientName,
                            doctorName,
                            appt.dateTime,
                            appt.department,
                            appt.notes.length() > 30 ? appt.notes.substring(0, 30) + "..." : appt.notes,
                            "Pending Confirmation"
                    });
                    count++;
                }
            }
        }
        pendingCount.setText(count + " pending");
    }

    private String getSelectedDoctorId() {
        if (doctorCombo.getSelectedIndex() <= 0)
            return null;
        String selected = (String) doctorCombo.getSelectedItem();
        return selected.split(" - ")[0];
    }

    private String getAppointmentStatus(Appointment appt) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = appt.dateTime.plusHours(1);

        if (now.isBefore(appt.dateTime)) {
            return "Upcoming";
        } else if (now.isAfter(endTime)) {
            return "Completed";
        } else {
            return "In Progress";
        }
    }

    private void scheduleAppointmentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Schedule Appointment", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Patient *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> patientCombo = new JComboBox<>();
        DataStore.patients.forEach((id, p) -> patientCombo.addItem(id + " - " + p.name));
        panel.add(patientCombo, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("Doctor *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> doctorCombo = new JComboBox<>();
        DataStore.staff.forEach((id, s) -> {
            if (s.role == StaffRole.DOCTOR)
                doctorCombo.addItem(id + " - " + s.name);
        });
        panel.add(doctorCombo, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.3;
        panel.add(new JLabel("Date *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        panel.add(dateSpinner, c);

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.3;
        panel.add(new JLabel("Time *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        panel.add(timeSpinner, c);

        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0.3;
        panel.add(new JLabel("Department *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> deptCombo = new JComboBox<>(DataStore.departments.toArray(new String[0]));
        panel.add(deptCombo, c);

        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0.3;
        panel.add(new JLabel("Notes"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JTextArea notesArea = new JTextArea(3, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(notesArea), c);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn = new JButton("Schedule");
        saveBtn.setBackground(Theme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            if (patientCombo.getSelectedIndex() < 0 || doctorCombo.getSelectedIndex() < 0
                    || deptCombo.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String patientId = patientCombo.getSelectedItem().toString().split(" - ")[0];
            String docId = doctorCombo.getSelectedItem().toString().split(" - ")[0];
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateSpinner.getValue());
            String time = new java.text.SimpleDateFormat("HH:mm").format((java.util.Date) timeSpinner.getValue());
            String dept = deptCombo.getSelectedItem().toString();

            java.util.List<String> result = AppointmentService.schedule(patientId, docId, date, time, dept);
            if (result.get(0).startsWith("Appointment created")) {
                JOptionPane.showMessageDialog(dialog, "Appointment scheduled successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void rescheduleAppointmentDialog() {
        JTable selectedTable = null;
        int row = -1;

        if (todayTable.getSelectedRow() >= 0) {
            selectedTable = todayTable;
            row = todayTable.getSelectedRow();
        } else if (upcomingTable.getSelectedRow() >= 0) {
            selectedTable = upcomingTable;
            row = upcomingTable.getSelectedRow();
        }

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to reschedule", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = (String) selectedTable.getValueAt(row, 0);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reschedule Appointment", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("New Date *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        panel.add(dateSpinner, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("New Time *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        panel.add(timeSpinner, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn = new JButton("Reschedule");
        saveBtn.setBackground(new Color(41, 128, 185));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateSpinner.getValue());
            String time = new java.text.SimpleDateFormat("HH:mm").format((java.util.Date) timeSpinner.getValue());

            java.util.List<String> result = AppointmentService.reschedule(appointmentId, date, time);
            if (result.get(0).startsWith("Appointment rescheduled")) {
                JOptionPane.showMessageDialog(dialog, "Appointment rescheduled successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void cancelAppointment() {
        JTable selectedTable = null;
        int row = -1;

        if (todayTable.getSelectedRow() >= 0) {
            selectedTable = todayTable;
            row = todayTable.getSelectedRow();
        } else if (upcomingTable.getSelectedRow() >= 0) {
            selectedTable = upcomingTable;
            row = upcomingTable.getSelectedRow();
        }

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = (String) selectedTable.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            java.util.List<String> result = AppointmentService.cancel(appointmentId);
            if (result.get(0).startsWith("Appointment canceled")) {
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void confirmPendingAppointment() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pending appointment to confirm", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String patientId = (String) pendingModel.getValueAt(row, 0);
        java.time.LocalDateTime dateTime = (java.time.LocalDateTime) pendingModel.getValueAt(row, 2);
        String doctorName = String.valueOf(pendingModel.getValueAt(row, 1));
        String dept = String.valueOf(pendingModel.getValueAt(row, 3));
        hpms.model.Appointment target = null;
        for (hpms.model.Appointment a : hpms.util.DataStore.appointments.values()) {
            if (patientId.equals(a.patientId) && a.dateTime.equals(dateTime) && dept.equals(a.department)) {
                target = a;
                break;
            }
        }
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Could not locate appointment to confirm", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (target.notes != null)
            target.notes = target.notes.replaceAll("(?i)pending", "").trim();
        if (target.notes == null || target.notes.isEmpty())
            target.notes = "Confirmed";
        try {
            hpms.util.BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        JOptionPane.showMessageDialog(this, "Appointment for patient " + patientId + " confirmed!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }

    private void viewAppointmentDetails() {
        JTable selectedTable = null;
        int row = -1;

        if (todayTable.getSelectedRow() >= 0) {
            selectedTable = todayTable;
            row = todayTable.getSelectedRow();
        } else if (upcomingTable.getSelectedRow() >= 0) {
            selectedTable = upcomingTable;
            row = upcomingTable.getSelectedRow();
        } else if (pendingTable.getSelectedRow() >= 0) {
            selectedTable = pendingTable;
            row = pendingTable.getSelectedRow();
        }

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to view", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patientId = (String) selectedTable.getValueAt(row,
                selectedTable == todayTable ? 0 : (selectedTable == upcomingTable ? 1 : 0));
        Patient patient = DataStore.patients.get(patientId);

        String details = "Appointment Details:\n\n" +
                "Patient ID: " + patientId + "\n" +
                "Patient Name: " + (patient != null ? patient.name : "N/A") + "\n" +
                "Status: " + (selectedTable == pendingTable ? "Pending Confirmation" : "Confirmed") + "\n" +
                "Detailed information available upon selection.";

        JOptionPane.showMessageDialog(this, details, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }
}
