package hpms.ui.panels;

import hpms.model.*;
import hpms.util.DataStore;
import hpms.ui.components.RoundedCard;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class AdminDashboardPanel extends JPanel {
    // Store references to value labels for dynamic updates
    private JLabel totalPatientsValue;
    private JLabel appointmentsTodayValue;
    private JLabel bedOccupancyValue;
    private JLabel totalBillingValue;
    private JLabel doctorsValue;
    private JLabel nursesValue;
    private JLabel cashiersValue;
    private JLabel adminsValue;
    private JLabel vacantRoomsValue;
    private JLabel pendingApptsValue;
    private JLabel unpaidBillsValue;
    private JLabel activeUsersValue;
    private JPanel logContent;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(SectionHeader.info("Admin Dashboard", "System overview with admin-level metrics and status indicators."),
                BorderLayout.NORTH);

        JPanel scrollPanel = new JPanel();
        scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));
        scrollPanel.setBackground(Theme.BACKGROUND);

        // Top metrics row
        JPanel metricsPanel = new JPanel(new GridLayout(2, 4, 20, 20));
        metricsPanel.setBackground(Theme.BACKGROUND);
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        metricsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        metricsPanel.add(createCard("Total Patients", String.valueOf(DataStore.patients.size()), Theme.PRIMARY,
                l -> totalPatientsValue = l));

        long todaysAppts = DataStore.appointments.values().stream()
                .filter(a -> a.dateTime.toLocalDate().equals(LocalDate.now())).count();
        metricsPanel.add(createCard("Appointments Today", String.valueOf(todaysAppts), new Color(52, 168, 224),
                l -> appointmentsTodayValue = l));

        long occupied = DataStore.rooms.values().stream().filter(r -> r.status == RoomStatus.OCCUPIED).count();
        long total = DataStore.rooms.size();
        metricsPanel.add(createCard("Bed Occupancy", occupied + "/" + total, new Color(76, 175, 80),
                l -> bedOccupancyValue = l));

        double totalBills = DataStore.bills.values().stream().mapToDouble(b -> b.total).sum();
        metricsPanel.add(createCard("Total Billing", String.format("%.2f", totalBills), new Color(255, 152, 0),
                l -> totalBillingValue = l));

        scrollPanel.add(metricsPanel);

        // Staff breakdown
        JPanel staffPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        staffPanel.setBackground(Theme.BACKGROUND);
        staffPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        staffPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        long doctors = DataStore.staff.values().stream().filter(s -> s.role == StaffRole.DOCTOR).count();
        long nurses = DataStore.staff.values().stream().filter(s -> s.role == StaffRole.NURSE).count();
        long cashiers = DataStore.staff.values().stream().filter(s -> s.role == StaffRole.CASHIER).count();
        long admins = DataStore.staff.values().stream().filter(s -> s.role == StaffRole.ADMIN).count();

        staffPanel.add(createCard("Doctors", String.valueOf(doctors), new Color(244, 67, 54), l -> doctorsValue = l));
        staffPanel.add(createCard("Nurses", String.valueOf(nurses), new Color(156, 39, 176), l -> nursesValue = l));
        staffPanel
                .add(createCard("Cashiers", String.valueOf(cashiers), new Color(0, 150, 136), l -> cashiersValue = l));
        staffPanel.add(createCard("Admins", String.valueOf(admins), new Color(63, 81, 181), l -> adminsValue = l));

        scrollPanel.add(staffPanel);

        // System status section
        JPanel statusPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statusPanel.setBackground(Theme.BACKGROUND);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        long vacantRooms = DataStore.rooms.values().stream().filter(r -> r.status == RoomStatus.VACANT).count();
        long pendingAppts = DataStore.appointments.values().stream()
                .filter(a -> a.isCompleted == false).count();
        long unpaidBills = DataStore.bills.values().stream()
                .filter(b -> b.paid == false).count();
        long activeUsers = DataStore.users.size();

        statusPanel.add(createCard("Vacant Rooms", String.valueOf(vacantRooms), new Color(76, 175, 80),
                l -> vacantRoomsValue = l));
        statusPanel.add(createCard("Pending Appointments", String.valueOf(pendingAppts), new Color(255, 193, 7),
                l -> pendingApptsValue = l));
        statusPanel.add(createCard("Unpaid Bills", String.valueOf(unpaidBills), new Color(244, 67, 54),
                l -> unpaidBillsValue = l));
        statusPanel.add(createCard("Active Users", String.valueOf(activeUsers), new Color(33, 150, 243),
                l -> activeUsersValue = l));

        scrollPanel.add(statusPanel);

        // Recent activity log
        JPanel activitySection = new JPanel(new BorderLayout());
        activitySection.setBackground(Theme.BACKGROUND);
        activitySection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        activitySection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        JLabel activityTitle = new JLabel("Recent Activity Log");
        activityTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        activityTitle.setForeground(Theme.FOREGROUND);
        activitySection.add(activityTitle, BorderLayout.NORTH);

        logContent = new JPanel();
        logContent.setLayout(new BoxLayout(logContent, BoxLayout.Y_AXIS));
        logContent.setBackground(Color.WHITE);
        logContent.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        updateActivityLog();

        JScrollPane logScroll = new JScrollPane(logContent);
        logScroll.setBorder(null);
        activitySection.add(logScroll, BorderLayout.CENTER);

        scrollPanel.add(activitySection);
        scrollPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(scrollPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Initial refresh to populate data
        refresh();

        // Add hierarchy listener to refresh when panel becomes visible
        this.addHierarchyListener(evt -> {
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) {
                    SwingUtilities.invokeLater(this::refresh);
                }
            }
        });
    }

    private JComponent createCard(String title, String value, Color accentColor,
            java.util.function.Consumer<JLabel> labelConsumer) {
        RoundedCard card = new RoundedCard(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(new Color(128, 128, 128));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);

        // Store reference to value label for updates
        labelConsumer.accept(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void updateActivityLog() {
        logContent.removeAll();
        java.util.List<String> recentLogs = DataStore.activityLog.stream()
                .skip(Math.max(0, DataStore.activityLog.size() - 10))
                .collect(java.util.stream.Collectors.toList());

        if (recentLogs.isEmpty()) {
            JLabel noDataLabel = new JLabel("No recent activity");
            noDataLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noDataLabel.setForeground(new Color(128, 128, 128));
            logContent.add(noDataLabel);
        } else {
            for (String log : recentLogs) {
                JLabel logEntry = new JLabel(log);
                logEntry.setFont(new Font("Monospaced", Font.PLAIN, 11));
                logEntry.setForeground(Theme.FOREGROUND);
                logEntry.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                logContent.add(logEntry);
            }
        }
        logContent.revalidate();
        logContent.repaint();
    }

    /**
     * Refresh all dashboard metrics from current DataStore state.
     * Called automatically when panel becomes visible, or can be called manually.
     */
    public void refresh() {
        if (totalPatientsValue != null) {
            totalPatientsValue.setText(String.valueOf(DataStore.patients.size()));
        }

        if (appointmentsTodayValue != null) {
            long todaysAppts = DataStore.appointments.values().stream()
                    .filter(a -> a.dateTime.toLocalDate().equals(LocalDate.now())).count();
            appointmentsTodayValue.setText(String.valueOf(todaysAppts));
        }

        if (bedOccupancyValue != null) {
            long occupied = DataStore.rooms.values().stream().filter(r -> r.status == RoomStatus.OCCUPIED).count();
            long total = DataStore.rooms.size();
            bedOccupancyValue.setText(occupied + "/" + total);
        }

        if (totalBillingValue != null) {
            double totalBills = DataStore.bills.values().stream().mapToDouble(b -> b.total).sum();
            totalBillingValue.setText(String.format("%.2f", totalBills));
        }

        if (doctorsValue != null) {
            long doctors = DataStore.staff.values().stream().filter(s -> s.role == StaffRole.DOCTOR).count();
            doctorsValue.setText(String.valueOf(doctors));
        }

        if (nursesValue != null) {
            long nurses = DataStore.staff.values().stream().filter(s -> s.role == StaffRole.NURSE).count();
            nursesValue.setText(String.valueOf(nurses));
        }

        if (cashiersValue != null) {
            long cashiers = DataStore.staff.values().stream().filter(s -> s.role == StaffRole.CASHIER).count();
            cashiersValue.setText(String.valueOf(cashiers));
        }

        if (adminsValue != null) {
            long admins = DataStore.staff.values().stream().filter(s -> s.role == StaffRole.ADMIN).count();
            adminsValue.setText(String.valueOf(admins));
        }

        if (vacantRoomsValue != null) {
            long vacantRooms = DataStore.rooms.values().stream().filter(r -> r.status == RoomStatus.VACANT).count();
            vacantRoomsValue.setText(String.valueOf(vacantRooms));
        }

        if (pendingApptsValue != null) {
            long pendingAppts = DataStore.appointments.values().stream()
                    .filter(a -> a.isCompleted == false).count();
            pendingApptsValue.setText(String.valueOf(pendingAppts));
        }

        if (unpaidBillsValue != null) {
            long unpaidBills = DataStore.bills.values().stream()
                    .filter(b -> b.paid == false).count();
            unpaidBillsValue.setText(String.valueOf(unpaidBills));
        }

        if (activeUsersValue != null) {
            long activeUsers = DataStore.users.size();
            activeUsersValue.setText(String.valueOf(activeUsers));
        }

        // Update activity log
        updateActivityLog();

        // Repaint to ensure visual update
        revalidate();
        repaint();
    }
}
