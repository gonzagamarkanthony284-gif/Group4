package hpms.ui.panels;

import hpms.model.*;
import hpms.util.DataStore;
import hpms.ui.components.RoundedCard;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class DashboardPanel extends JPanel {
    // Store references to value labels for dynamic updates
    private JLabel patientsValue;
    private JLabel appointmentsValue;
    private JLabel bedOccupancyValue;
    private JLabel doctorsValue;
    private JLabel nursesValue;
    private JLabel cashiersValue;
    private JPanel grid;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(SectionHeader.info("Dashboard", "Overview: appointments, admissions, billing summaries."),
                BorderLayout.NORTH);

        grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setBackground(Theme.BACKGROUND);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(grid, BorderLayout.CENTER);

        // Create cards with stored label references
        grid.add(createCard("Patients", "0", l -> patientsValue = l));
        grid.add(createCard("Appointments Today", "0", l -> appointmentsValue = l));
        grid.add(createCard("Bed Occupancy", "0/0", l -> bedOccupancyValue = l));
        grid.add(createCard("Doctors", "0", l -> doctorsValue = l));
        grid.add(createCard("Nurses", "0", l -> nursesValue = l));
        grid.add(createCard("Cashiers", "0", l -> cashiersValue = l));

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

    private JComponent createCard(String title, String initialValue,
            java.util.function.Consumer<JLabel> labelConsumer) {
        RoundedCard card = new RoundedCard(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Theme.FOREGROUND);

        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLabel.setForeground(Theme.PRIMARY);

        // Store reference to value label for updates
        labelConsumer.accept(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refresh all dashboard metrics from current DataStore state.
     * Called automatically when panel becomes visible, or can be called manually.
     */
    public void refresh() {
        if (patientsValue != null) {
            patientsValue.setText(String.valueOf(DataStore.patients.size()));
        }

        if (appointmentsValue != null) {
            long todaysAppts = DataStore.appointments.values().stream()
                    .filter(a -> a.dateTime.toLocalDate().equals(LocalDate.now())).count();
            appointmentsValue.setText(String.valueOf(todaysAppts));
        }

        if (bedOccupancyValue != null) {
            long occupied = DataStore.rooms.values().stream().filter(r -> r.status == RoomStatus.OCCUPIED).count();
            long total = DataStore.rooms.size();
            bedOccupancyValue.setText(occupied + "/" + total);
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

        // Repaint to ensure visual update
        if (grid != null) {
            grid.revalidate();
            grid.repaint();
        }
    }
}
