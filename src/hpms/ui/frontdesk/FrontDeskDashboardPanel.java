package hpms.ui.frontdesk;

import hpms.auth.AuthSession;
import hpms.model.*;
import hpms.service.*;
import hpms.ui.components.*;
import hpms.util.DataStore;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FrontDeskDashboardPanel extends JPanel {
    private final AuthSession session;
    private JTable patientTable;
    private JTable roomTable;
    private JLabel summaryLabel;
    private JButton newPatientBtn;
    private JButton checkInBtn;
    private JButton checkOutBtn;
    private JButton generateBillBtn;

    public FrontDeskDashboardPanel(AuthSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.BACKGROUND);
        tabbedPane.setForeground(Theme.FOREGROUND);
        
        // Patients Tab
        JPanel patientsPanel = createPatientsPanel();
        tabbedPane.addTab("Patients", patientsPanel);
        
        // Rooms Tab
        JPanel roomsPanel = createRoomsPanel();
        tabbedPane.addTab("Room Management", roomsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Action Buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshPatientTable();
        refreshRoomTable();
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        // Title and Subtitle
        JLabel title = new JLabel("Front Desk Dashboard");
        title.setFont(Theme.HEADING_2);
        title.setForeground(Theme.FOREGROUND);
        
        JLabel subtitle = new JLabel("Welcome, " + session.fullName);
        subtitle.setFont(Theme.APP_FONT);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(title);
        titlePanel.add(subtitle);
        
        // Summary
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        summaryLabel = new JLabel("Today: " + today);
        summaryLabel.setFont(Theme.APP_FONT);
        summaryLabel.setForeground(Theme.TEXT_SECONDARY);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(summaryLabel, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        
        return header;
    }
    
    private JPanel createPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchPanel.setOpaque(false);
        
        JTextField searchField = new JTextField(25);
        Theme.styleTextField(searchField);
        
        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(Theme.APP_FONT);
        searchBtn.setBackground(Theme.PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setBorderPainted(false);
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            searchPatients(query);
        });
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        // Patient Table
        patientTable = new JTable();
        patientTable.setModel(new DefaultTableModel(
            new Object[]{"ID", "Name", "Phone", "Email", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        
        // Style the table
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setFont(Theme.APP_FONT);
        patientTable.setRowHeight(32);
        patientTable.getTableHeader().setFont(Theme.APP_FONT_BOLD);
        patientTable.setShowGrid(false);
        patientTable.setIntercellSpacing(new Dimension(0, 0));
        patientTable.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER, 1, false));
        scrollPane.getViewport().setBackground(Theme.SURFACE);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        
        // Room Table
        roomTable = new JTable();
        roomTable.setModel(new DefaultTableModel(
            new Object[]{"Room No", "Type", "Status", "Patient", "Check-In Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        
        // Style the table
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.setFont(Theme.APP_FONT);
        roomTable.setRowHeight(32);
        roomTable.getTableHeader().setFont(Theme.APP_FONT_BOLD);
        roomTable.setShowGrid(false);
        roomTable.setIntercellSpacing(new Dimension(0, 0));
        roomTable.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER, 1, false));
        scrollPane.getViewport().setBackground(Theme.SURFACE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        
        // Create buttons with consistent styling
        newPatientBtn = createActionButton("New Patient", Theme.PRIMARY);
        checkInBtn = createActionButton("Check In", Theme.SUCCESS);
        checkOutBtn = createActionButton("Check Out", Theme.WARNING);
        generateBillBtn = createActionButton("Generate Bill", Theme.INFO);
        
        // Add action listeners
        newPatientBtn.addActionListener(this::onNewPatient);
        checkInBtn.addActionListener(this::onCheckIn);
        checkOutBtn.addActionListener(this::onCheckOut);
        generateBillBtn.addActionListener(this::onGenerateBill);
        
        // Add buttons to panel
        buttonPanel.add(newPatientBtn);
        buttonPanel.add(checkInBtn);
        buttonPanel.add(checkOutBtn);
        buttonPanel.add(generateBillBtn);
        
        return buttonPanel;
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(Theme.APP_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 36));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(darkenColor(bgColor, 0.9f));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private Color darkenColor(Color color, float factor) {
        return new Color(
            Math.max((int)(color.getRed() * factor), 0),
            Math.max((int)(color.getGreen() * factor), 0),
            Math.max((int)(color.getBlue() * factor), 0)
        );
    }
    
    private void refreshPatientTable() {
        DefaultTableModel model = (DefaultTableModel) patientTable.getModel();
        model.setRowCount(0);
        
        for (Patient patient : DataStore.patients.values()) {
            model.addRow(new Object[]{
                patient.id,
                patient.name,
                patient.contact,
                patient.email != null ? patient.email : "N/A",
                PatientStatusService.getStatus(patient.id).toString()
            });
        }
    }
    
    private void refreshRoomTable() {
        DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
        model.setRowCount(0);
        
        for (Room room : DataStore.rooms.values()) {
            String patientInfo = "-";
            String checkInDate = "-";
            
            if (room.occupantPatientId != null) {
                Patient patient = DataStore.patients.get(room.occupantPatientId);
                if (patient != null) {
                    patientInfo = patient.name + " (" + patient.id + ")";
                    // Get check-in date from patient status or visit data
                    checkInDate = "2025-12-10"; // TODO: Get actual check-in date
                }
            }
            
            model.addRow(new Object[]{
                room.id,
                room.status.toString(),
                patientInfo,
                checkInDate
            });
        }
    }
    
    private void onNewPatient(ActionEvent e) {
        new PatientRegistrationDialog((Frame) SwingUtilities.getWindowAncestor(this), session).setVisible(true);
        refreshPatientTable();
    }
    
    private void onCheckIn(ActionEvent e) {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a patient to check in",
                "No Patient Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String patientId = (String) patientTable.getValueAt(selectedRow, 0);
        new CheckInDialog((Frame) SwingUtilities.getWindowAncestor(this), patientId, session).setVisible(true);
        refreshPatientTable();
        refreshRoomTable();
    }
    
    private void onCheckOut(ActionEvent e) {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a patient to check out",
                "No Patient Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String patientId = (String) patientTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to check out patient " + patientId + "?",
            "Confirm Check Out",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            java.util.List<String> result = PatientStatusService.setStatus(patientId, "OUTPATIENT", session.userId, "Check out via front desk");
            if (result.isEmpty() || result.get(0).startsWith("Success")) {
                JOptionPane.showMessageDialog(this, 
                    "Patient checked out successfully",
                    "Check Out Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    String.join("\n", result),
                    "Check Out Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
            refreshPatientTable();
            refreshRoomTable();
        }
    }
    
    private void onGenerateBill(ActionEvent e) {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a patient to generate bill",
                "No Patient Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String patientId = (String) patientTable.getValueAt(selectedRow, 0);
        new BillGenerationDialog((Frame) SwingUtilities.getWindowAncestor(this), patientId, session).setVisible(true);
    }
    
    private void searchPatients(String query) {
        DefaultTableModel model = (DefaultTableModel) patientTable.getModel();
        model.setRowCount(0);
        
        if (query.isEmpty()) {
            refreshPatientTable();
            return;
        }
        
        String lowerQuery = query.toLowerCase();
        for (Patient patient : DataStore.patients.values()) {
            boolean matches = patient.name.toLowerCase().contains(lowerQuery) ||
                            patient.id.toLowerCase().contains(lowerQuery) ||
                            patient.contact.toLowerCase().contains(lowerQuery) ||
                            (patient.email != null && patient.email.toLowerCase().contains(lowerQuery));
            
            if (matches) {
                model.addRow(new Object[]{
                    patient.id,
                    patient.name,
                    patient.contact,
                    patient.email != null ? patient.email : "N/A",
                    PatientStatusService.getStatus(patient.id).toString()
                });
            }
        }
    }
}
