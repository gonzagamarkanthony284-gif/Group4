package hpms.ui.staff;

import hpms.model.Staff;
import hpms.model.StaffRole;
import hpms.ui.doctor.DoctorPublicProfilePanel;
import hpms.util.DataStore;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Admin Staff Management Panel with 4 role-based tabs
 * Features: Doctors, Nurses, Cashiers, Admins tabs with full CRUD operations
 * White-blue professional theme
 */
public class StaffPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private java.util.Map<String, TabPanel> tabPanels = new java.util.HashMap<>();

    public StaffPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        // Create tabs for each role
        tabPanels.put("DOCTOR", new TabPanel(StaffRole.DOCTOR, "Doctors"));
        tabPanels.put("NURSE", new TabPanel(StaffRole.NURSE, "Nurses"));
        tabPanels.put("CASHIER", new TabPanel(StaffRole.CASHIER, "Cashiers"));

        for (TabPanel panel : tabPanels.values()) {
            tabbedPane.addTab(panel.getTitle(), panel);
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 250, 255));
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 220, 240), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Staff Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 70, 140));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton addBtn = new JButton("+ Add Staff");
        addBtn.setFont(new Font("Arial", Font.BOLD, 11));
        addBtn.setBackground(new Color(34, 150, 80));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> openRegistrationForm());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 11));
        refreshBtn.setBackground(new Color(47, 111, 237));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshAllTabs());

        rightPanel.add(addBtn);
        rightPanel.add(refreshBtn);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    /**
     * Inner class for each role-based tab
     */
    private class TabPanel extends JPanel {
        private StaffRole role;
        private String title;
        private StaffTableModel tableModel;
        private JTable table;

        public TabPanel(StaffRole role, String title) {
            this.role = role;
            this.title = title;
            this.tableModel = new StaffTableModel();
            this.tableModel.setFilterRole(role);

            setLayout(new BorderLayout(12, 12));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Table
            table = new JTable(tableModel);
            table.setRowHeight(30);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setBackground(Color.WHITE);
            table.setGridColor(new Color(220, 230, 240));

            // Style header
            JTableHeader header = table.getTableHeader();
            header.setBackground(new Color(232, 240, 254));
            header.setForeground(new Color(30, 70, 140));
            header.setFont(new Font("Arial", Font.BOLD, 12));
            header.setBorder(new LineBorder(new Color(200, 220, 240)));

            // Double-click to open profile
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row >= 0) {
                            Staff staff = tableModel.getStaffAt(row);
                            if (staff != null) {
                                new StaffProfileModal(SwingUtilities.getWindowAncestor(TabPanel.this), staff).setVisible(true);
                            }
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getViewport().setBackground(Color.WHITE);
            add(scrollPane, BorderLayout.CENTER);

            // Action buttons
            add(createActionPanel(), BorderLayout.SOUTH);
        }

        private JPanel createActionPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            panel.setBackground(new Color(245, 250, 255));
            panel.setBorder(new LineBorder(new Color(220, 230, 240)));

            JButton editBtn = new JButton("Edit");
            editBtn.setFont(new Font("Arial", Font.BOLD, 11));
            editBtn.setBackground(new Color(47, 111, 237));
            editBtn.setForeground(Color.WHITE);
            editBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            editBtn.setFocusPainted(false);
            editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editBtn.addActionListener(e -> editStaff());

            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setFont(new Font("Arial", Font.BOLD, 11));
            deleteBtn.setBackground(new Color(220, 53, 69));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            deleteBtn.setFocusPainted(false);
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtn.addActionListener(e -> deleteStaff());

            JButton viewBtn = new JButton("View Details");
            viewBtn.setFont(new Font("Arial", Font.BOLD, 11));
            viewBtn.setBackground(new Color(108, 117, 125));
            viewBtn.setForeground(Color.WHITE);
            viewBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            viewBtn.setFocusPainted(false);
            viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            viewBtn.addActionListener(e -> viewDetails());
            
            // Add View Services button for doctors
            JButton servicesBtn = new JButton("View Services");
            servicesBtn.setFont(new Font("Arial", Font.BOLD, 11));
            servicesBtn.setBackground(new Color(40, 167, 69));
            servicesBtn.setForeground(Color.WHITE);
            servicesBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            servicesBtn.setFocusPainted(false);
            servicesBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            servicesBtn.addActionListener(e -> viewServices());
            servicesBtn.setVisible(role == StaffRole.DOCTOR); // Only show for doctors

            panel.add(editBtn);
            panel.add(deleteBtn);
            panel.add(viewBtn);
            panel.add(servicesBtn);

            return panel;
        }

        private void editStaff() {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a staff member to edit", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Staff staff = tableModel.getStaffAt(row);
            if (staff != null) {
                new StaffEditForm(SwingUtilities.getWindowAncestor(this), staff).setVisible(true);
                refresh();
            }
        }

        private void deleteStaff() {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a staff member to delete", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Staff staff = tableModel.getStaffAt(row);
            if (staff != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete staff member:\n" + staff.name + " (" + staff.id + ")?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    // Delete from DataStore
                    DataStore.staff.remove(staff.id);
                    
                    // Also delete account if exists
                    if (DataStore.users.containsKey(staff.id)) {
                        DataStore.users.remove(staff.id);
                    }

                    tableModel.removeStaff(staff.id);
                    JOptionPane.showMessageDialog(this, "Staff member deleted successfully", "Delete Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        private void viewDetails() {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a staff member to view", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Staff staff = tableModel.getStaffAt(row);
            if (staff != null) {
                new StaffProfileModal(SwingUtilities.getWindowAncestor(this), staff).setVisible(true);
            }
        }
        
        private void viewServices() {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a doctor to view services", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Staff doctor = tableModel.getStaffAt(row);
            if (doctor != null && doctor.role == StaffRole.DOCTOR) {
                // Show the doctor's public profile which includes their services/biography
                DoctorPublicProfilePanel.showDialog(SwingUtilities.getWindowAncestor(this), doctor);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a doctor to view services", "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            }
        }

        public String getTitle() {
            return title;
        }

        public void refresh() {
            tableModel.refreshData();
        }
    }

    private void refreshAllTabs() {
        for (TabPanel panel : tabPanels.values()) {
            panel.refresh();
        }
    }

    private void openRegistrationForm() {
        new UnifiedRegistrationForm(SwingUtilities.getWindowAncestor(this)).setVisible(true);
        refreshAllTabs();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (tabPanels != null) {
            for (TabPanel panel : tabPanels.values()) {
                panel.refresh();
            }
        }
    }
}
