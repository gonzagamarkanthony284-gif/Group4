package hpms.ui.panels;

import hpms.model.*;
import hpms.util.DataStore;
import hpms.ui.components.RoundedCard;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;
import hpms.auth.AuthService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminDashboardPanel extends JPanel {
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

        metricsPanel.add(createCard("Total Patients", String.valueOf(DataStore.patients.size()), Theme.PRIMARY));

        long todaysAppts = DataStore.appointments.values().stream()
                .filter(a -> a.dateTime.toLocalDate().equals(LocalDate.now())).count();
        metricsPanel.add(createCard("Appointments Today", String.valueOf(todaysAppts), new Color(52, 168, 224)));

        long occupied = DataStore.rooms.values().stream().filter(r -> r.status == RoomStatus.OCCUPIED).count();
        long total = DataStore.rooms.size();
        metricsPanel.add(createCard("Bed Occupancy", occupied + "/" + total, new Color(76, 175, 80)));

        double totalBills = DataStore.bills.values().stream().mapToDouble(b -> b.total).sum();
        metricsPanel.add(createCard("Total Billing", String.format("%.2f", totalBills), new Color(255, 152, 0)));

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

        staffPanel.add(createCard("Doctors", String.valueOf(doctors), new Color(244, 67, 54)));
        staffPanel.add(createCard("Nurses", String.valueOf(nurses), new Color(156, 39, 176)));
        staffPanel.add(createCard("Cashiers", String.valueOf(cashiers), new Color(0, 150, 136)));
        staffPanel.add(createCard("Admins", String.valueOf(admins), new Color(63, 81, 181)));

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

        statusPanel.add(createCard("Vacant Rooms", String.valueOf(vacantRooms), new Color(76, 175, 80)));
        statusPanel.add(createCard("Pending Appointments", String.valueOf(pendingAppts), new Color(255, 193, 7)));
        statusPanel.add(createCard("Unpaid Bills", String.valueOf(unpaidBills), new Color(244, 67, 54)));
        statusPanel.add(createCard("Active Users", String.valueOf(activeUsers), new Color(33, 150, 243)));

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

        JPanel logContent = new JPanel();
        logContent.setLayout(new BoxLayout(logContent, BoxLayout.Y_AXIS));
        logContent.setBackground(Color.WHITE);
        logContent.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

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

        JScrollPane logScroll = new JScrollPane(logContent);
        logScroll.setBorder(null);
        activitySection.add(logScroll, BorderLayout.CENTER);

        scrollPanel.add(activitySection);
        scrollPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(scrollPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JComponent createCard(String title, String value, Color accentColor) {
        RoundedCard card = new RoundedCard(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(new Color(128, 128, 128));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}
