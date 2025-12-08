package hpms.ui.doctor;

import hpms.auth.AuthSession;
import hpms.model.Appointment;
import hpms.util.DataStore;
import hpms.service.AppointmentService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Doctor Appointments Panel - manage today's schedule, pending requests, and
 * history
 */
public class DoctorAppointmentsPanel extends JPanel {
    private AuthSession session;
    private JTabbedPane tabbedPane;
    private JTable todayTable;
    private JTable requestsTable;
    private JTable weekTable;
    private JButton scheduleBtn;
    private JButton rescheduleBtn;
    private JButton cancelBtn;
    private JButton acceptBtn;
    private JButton rejectBtn;

    public DoctorAppointmentsPanel(AuthSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("Appointments Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(31, 41, 55));
        add(title, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("Today's Schedule", createTodayPanel());
        tabbedPane.addTab("Pending Requests", createRequestsPanel());
        tabbedPane.addTab("Upcoming Week", createWeekPanel());
        // Add change listener to update action buttons based on active tab
        tabbedPane.addChangeListener(e -> updateActionButtons());
        add(tabbedPane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(new Color(248, 249, 250));
        actions.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));

        scheduleBtn = mkBtn("Schedule", new Color(47, 111, 237));
        rescheduleBtn = mkBtn("Reschedule", new Color(234, 179, 8));
        cancelBtn = mkBtn("Cancel", new Color(239, 68, 68));
        acceptBtn = mkBtn("Accept Request", new Color(34, 197, 94));
        rejectBtn = mkBtn("Reject Request", new Color(107, 114, 128));

        scheduleBtn.addActionListener(e -> scheduleAppointmentDialog());
        rescheduleBtn.addActionListener(e -> rescheduleSelected());
        cancelBtn.addActionListener(e -> cancelSelected());
        acceptBtn.addActionListener(e -> acceptSelectedRequest());
        rejectBtn.addActionListener(e -> rejectSelectedRequest());

        actions.add(scheduleBtn);
        actions.add(rescheduleBtn);
        actions.add(cancelBtn);
        actions.add(acceptBtn);
        actions.add(rejectBtn);
        add(actions, BorderLayout.SOUTH);

        // Initialize button visibility based on first tab
        updateActionButtons();
    }

    private JPanel createTodayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        todayTable = new JTable();
        todayTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todayTable.setRowHeight(28);
        refreshTodayTable();

        JScrollPane scroll = new JScrollPane(todayTable);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        requestsTable = new JTable();
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.setRowHeight(28);
        refreshRequestsTable();

        JScrollPane scroll = new JScrollPane(requestsTable);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createWeekPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        weekTable = new JTable();
        weekTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        weekTable.setRowHeight(28);
        refreshWeekTable();

        JScrollPane scroll = new JScrollPane(weekTable);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void refreshTodayTable() {
        LocalDate today = LocalDate.now();
        java.util.List<Appointment> appts = new ArrayList<>();

        for (Appointment a : DataStore.appointments.values()) {
            if (session.userId.equals(a.staffId) && a.dateTime.toLocalDate().equals(today)) {
                appts.add(a);
            }
        }

        appts.sort((a, b) -> a.dateTime.compareTo(b.dateTime));

        String[] columns = { "ID", "Time", "Patient ID", "Patient Name", "Department", "Status" };
        Object[][] data = new Object[appts.size()][6];

        for (int i = 0; i < appts.size(); i++) {
            Appointment a = appts.get(i);
            data[i][0] = a.id;
            data[i][1] = a.dateTime.toLocalTime().toString();
            data[i][2] = a.patientId;
            data[i][3] = DataStore.patients.containsKey(a.patientId) ? DataStore.patients.get(a.patientId).name
                    : "Unknown";
            data[i][4] = a.department;
            data[i][5] = "Scheduled";
        }

        todayTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private void refreshRequestsTable() {
        java.util.List<Appointment> pending = new ArrayList<>();
        for (Appointment a : DataStore.appointments.values()) {
            if (session.userId.equals(a.staffId) && a.notes != null && a.notes.toLowerCase().contains("pending")) {
                pending.add(a);
            }
        }
        pending.sort((a, b) -> a.dateTime.compareTo(b.dateTime));

        String[] columns = { "ID", "Patient", "Requested Date", "Department", "Notes" };
        Object[][] data = new Object[pending.size()][5];
        for (int i = 0; i < pending.size(); i++) {
            Appointment a = pending.get(i);
            String pname = DataStore.patients.containsKey(a.patientId) ? DataStore.patients.get(a.patientId).name
                    : "Unknown";
            data[i][0] = a.id;
            data[i][1] = pname + " (" + a.patientId + ")";
            data[i][2] = a.dateTime.toString();
            data[i][3] = a.department;
            data[i][4] = a.notes;
        }
        requestsTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private void refreshWeekTable() {
        java.util.List<Appointment> weekAppts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        for (Appointment a : DataStore.appointments.values()) {
            if (session.userId.equals(a.staffId)) {
                LocalDate d = a.dateTime.toLocalDate();
                if (!d.isBefore(today) && d.isBefore(nextWeek)) {
                    weekAppts.add(a);
                }
            }
        }
        weekAppts.sort((a, b) -> a.dateTime.compareTo(b.dateTime));
        String[] columns = { "ID", "Date", "Time", "Patient", "Department", "Status" };
        Object[][] data = new Object[weekAppts.size()][6];
        for (int i = 0; i < weekAppts.size(); i++) {
            Appointment a = weekAppts.get(i);
            String pname = DataStore.patients.containsKey(a.patientId) ? DataStore.patients.get(a.patientId).name
                    : "Unknown";
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = a.dateTime.plusHours(1);
            String st;
            if (now.isBefore(a.dateTime))
                st = "Upcoming";
            else if (now.isAfter(end))
                st = "Completed";
            else
                st = "In Progress";
            data[i][0] = a.id;
            data[i][1] = a.dateTime.toLocalDate().toString();
            data[i][2] = a.dateTime.toLocalTime().toString();
            data[i][3] = pname + " (" + a.patientId + ")";
            data[i][4] = a.department;
            data[i][5] = st;
        }
        weekTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    public void refresh() {
        refreshTodayTable();
        refreshRequestsTable();
        refreshWeekTable();
    }

    private void updateActionButtons() {
        int selectedTab = tabbedPane.getSelectedIndex();

        // Hide all buttons first
        scheduleBtn.setVisible(false);
        rescheduleBtn.setVisible(false);
        cancelBtn.setVisible(false);
        acceptBtn.setVisible(false);
        rejectBtn.setVisible(false);

        // Show appropriate buttons based on active tab
        if (selectedTab == 0) {
            // Today's Schedule tab - show Schedule, Reschedule, Cancel
            scheduleBtn.setVisible(true);
            rescheduleBtn.setVisible(true);
            cancelBtn.setVisible(true);
        } else if (selectedTab == 1) {
            // Pending Requests tab - show only Accept and Reject
            acceptBtn.setVisible(true);
            rejectBtn.setVisible(true);
        } else if (selectedTab == 2) {
            // Upcoming Week tab - show Schedule, Reschedule, Cancel
            scheduleBtn.setVisible(true);
            rescheduleBtn.setVisible(true);
            cancelBtn.setVisible(true);
        }
    }

    private JButton mkBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.PLAIN, 11));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void scheduleAppointmentDialog() {
        JComboBox<String> pid = new JComboBox<>(hpms.util.DataStore.patients.keySet().toArray(new String[0]));
        JTextField date = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField time = new JTextField(LocalTime.of(9, 0).toString());
        JComboBox<String> dept = new JComboBox<>(hpms.util.DataStore.departments.toArray(new String[0]));
        int r = JOptionPane.showConfirmDialog(this,
                new Object[] { "Patient", pid, "Date", date, "Time", time, "Department", dept }, "Schedule Appointment",
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            java.util.List<String> out = AppointmentService.schedule(String.valueOf(pid.getSelectedItem()),
                    session.userId, date.getText(), time.getText(), String.valueOf(dept.getSelectedItem()));
            if (!out.isEmpty() && out.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(this, out.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, String.join("\n", out), "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            }
        }
    }

    private String getSelectedAppointmentId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0)
            return null;
        Object val = table.getModel().getValueAt(row, 0);
        return val != null ? val.toString() : null;
    }

    private void rescheduleSelected() {
        JTable table = tabbedPane.getSelectedIndex() == 2 ? weekTable : todayTable;
        String id = getSelectedAppointmentId(table);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select an appointment first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField date = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField time = new JTextField(LocalTime.of(10, 0).toString());
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "New Date", date, "New Time", time }, "Reschedule",
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            java.util.List<String> out = AppointmentService.reschedule(id, date.getText(), time.getText());
            if (!out.isEmpty() && out.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(this, out.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, String.join("\n", out), "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            }
        }
    }

    private void cancelSelected() {
        JTable table = tabbedPane.getSelectedIndex() == 2 ? weekTable : todayTable;
        String id = getSelectedAppointmentId(table);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select an appointment first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Cancel appointment " + id + "?", "Confirm Cancel",
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            java.util.List<String> out = AppointmentService.cancel(id);
            if (!out.isEmpty() && out.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(this, out.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, String.join("\n", out), "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            }
        }
    }

    private void acceptSelectedRequest() {
        String id = getSelectedAppointmentId(requestsTable);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a request first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Appointment a = DataStore.appointments.get(id);
        if (a != null) {
            a.notes = (a.notes == null ? "" : a.notes.replaceAll("(?i)pending", "")).trim();
            if (a.notes.isEmpty())
                a.notes = "Accepted";
            try {
                hpms.util.BackupUtil.saveToDefault();
            } catch (Exception ex) {
            }
            JOptionPane.showMessageDialog(this, "Request accepted", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshRequestsTable();
        }
    }

    private void rejectSelectedRequest() {
        String id = getSelectedAppointmentId(requestsTable);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a request first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Reject and cancel request " + id + "?", "Confirm",
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            java.util.List<String> out = AppointmentService.cancel(id);
            if (!out.isEmpty() && out.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(this, out.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Request rejected and canceled", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refresh();
            }
        }
    }
}
