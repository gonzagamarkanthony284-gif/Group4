package hpms.ui.frontdesk;

import hpms.auth.AuthSession;
import hpms.model.*;
import hpms.service.*;
import hpms.ui.components.Theme;
import hpms.util.DataStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class CheckInDialog extends JDialog {
    
    private final String patientId;
    private JComboBox<String> roomCombo;
    private JTextArea notesArea;
    
    public CheckInDialog(Frame owner, String patientId, AuthSession session) {
        super(owner, "Patient Check-In", true);
        this.patientId = patientId;
        initializeComponents();
        layoutComponents();
        setupDialog();
        loadAvailableRooms();
    }
    
    private void initializeComponents() {
        roomCombo = new JComboBox<>();
        roomCombo.setPreferredSize(new Dimension(200, 30));
        Theme.styleComboBox(roomCombo);
        
        notesArea = new JTextArea(3, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(Theme.SURFACE);
        notesArea.setForeground(Theme.FOREGROUND);
        notesArea.setBorder(new JTextField().getBorder());
    }
    
    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Patient Info
        Patient patient = DataStore.patients.get(patientId);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel patientInfo = new JLabel("Patient: " + (patient != null ? patient.name : "Unknown"));
        patientInfo.setFont(Theme.APP_FONT_BOLD);
        patientInfo.setForeground(Theme.FOREGROUND);
        mainPanel.add(patientInfo, gbc);
        
        // Room Selection
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Select Room *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(roomCombo, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Check-in Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(roomCombo.getBorder());
        mainPanel.add(notesScroll, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.setOpaque(false);
        
        JButton checkInButton = new JButton("Check In");
        checkInButton.setBackground(Theme.SUCCESS);
        checkInButton.setForeground(Color.WHITE);
        checkInButton.setFocusPainted(false);
        checkInButton.setBorderPainted(false);
        checkInButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkInButton.addActionListener(this::onCheckIn);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(checkInButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupDialog() {
        setLayout(new BorderLayout());
        setSize(450, 300);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }
    
    private void loadAvailableRooms() {
        roomCombo.removeAllItems();
        roomCombo.addItem("Select a room...");
        
        for (Room room : DataStore.rooms.values()) {
            if (room.status == RoomStatus.VACANT) {
                roomCombo.addItem(room.id + " - Available");
            }
        }
    }
    
    private void onCheckIn(ActionEvent e) {
        String selected = (String) roomCombo.getSelectedItem();
        if (selected == null || selected.equals("Select a room...")) {
            JOptionPane.showMessageDialog(this, 
                "Please select a room",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String roomId = selected.split(" - ")[0];
        String notes = notesArea.getText().trim();
        
        // First check if patient can be checked in
        java.util.List<String> checkInResult = PatientStatusService.setStatus(patientId, "INPATIENT", "frontdesk", notes);
        if (!checkInResult.isEmpty() && !checkInResult.get(0).startsWith("Success")) {
            JOptionPane.showMessageDialog(this, 
                String.join("\n", checkInResult),
                "Check-In Failed",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Assign room
        List<String> assignResult = RoomService.assign(roomId, patientId);
        if (assignResult.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Patient checked in successfully!\nRoom " + roomId + " assigned.",
                "Check-In Complete",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                String.join("\n", assignResult),
                "Room Assignment Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
