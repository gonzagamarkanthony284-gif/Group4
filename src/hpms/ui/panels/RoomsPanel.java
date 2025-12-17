package hpms.ui.panels;

import hpms.model.*;
import hpms.service.*;
import hpms.util.*;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RoomsPanel extends JPanel {
    private DefaultTableModel roomModel;
    private JTable roomTable;
    private JLabel statsLabel;

    public RoomsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(SectionHeader.info("Room & Bed Management", "Manage room occupancy and patient assignments"),
                BorderLayout.NORTH);

        // Stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.BEFORE_FIRST_LINE);

        // Room table
        roomModel = new DefaultTableModel(
                new String[] { "Room ID", "Status", "Patient", "Patient Name", "Room Type", "Floor" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        roomTable = new JTable(roomModel);
        roomTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        roomTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        roomTable.getColumnModel().getColumn(5).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(roomTable);
        add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);

        refresh();

        // keep data fresh when user navigates back to this panel
        this.addHierarchyListener(evt -> {
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing())
                    SwingUtilities.invokeLater(() -> {
                        if (!DataStore.rooms.isEmpty() && roomModel.getRowCount() == 0)
                            refresh();
                        else
                            refresh();
                    });
            }
        });
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        statsLabel = new JLabel("Total Rooms: 0 | Occupied: 0 | Vacant: 0 | Occupancy Rate: 0%");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(80, 80, 80));

        panel.add(statsLabel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(8, 12, 12, 12));

        JButton assignBtn = new JButton("Assign Patient");
        styleButton(assignBtn, new Color(0, 110, 102));

        JButton vacateBtn = new JButton("Vacate Room");
        styleButton(vacateBtn, new Color(192, 57, 43));

        JButton transferBtn = new JButton("Transfer Patient");
        styleButton(transferBtn, new Color(41, 128, 185));

        JButton viewDetailsBtn = new JButton("View Details");
        styleButton(viewDetailsBtn, new Color(155, 89, 182));

        assignBtn.addActionListener(e -> assignRoomDialog());
        vacateBtn.addActionListener(e -> vacateRoom());
        transferBtn.addActionListener(e -> transferPatient());
        viewDetailsBtn.addActionListener(e -> viewRoomDetails());

        panel.add(assignBtn);
        panel.add(vacateBtn);
        panel.add(transferBtn);
        panel.add(viewDetailsBtn);

        return panel;
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void assignRoomDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assign Patient to Room", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(new Color(245, 247, 250));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Available rooms
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Select Room *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> roomCombo = new JComboBox<>();
        for (Room r : DataStore.rooms.values()) {
            if (r.status == RoomStatus.VACANT) {
                roomCombo.addItem(r.id + " - " + r.status);
            }
        }
        panel.add(roomCombo, c);

        // Available patients (only inpatients can be assigned to rooms)
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("Select Patient (Inpatients Only) *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> patientCombo = new JComboBox<>();
        DataStore.patients.forEach((id, p) -> {
            // Only show active inpatients
            if (!p.isActive)
                return;
            PatientStatus status = PatientStatusService.getStatus(id);
            if (status != PatientStatus.INPATIENT)
                return;
            // Skip patients already assigned to any room
            boolean alreadyAssigned = DataStore.rooms.values().stream()
                    .anyMatch(r -> id.equals(r.occupantPatientId));
            if (!alreadyAssigned) {
                patientCombo.addItem(id + " - " + p.name);
            }
        });
        panel.add(patientCombo, c);

        // Notes
        c.gridx = 0;
        c.gridy = 2;
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton assignBtn = new JButton("Assign");
        styleButton(assignBtn, new Color(0, 110, 102));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(assignBtn);

        assignBtn.addActionListener(e -> {
            if (roomCombo.getSelectedIndex() < 0 || patientCombo.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select both room and patient", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String roomId = roomCombo.getSelectedItem().toString().split(" - ")[0];
            String patientId = patientCombo.getSelectedItem().toString().split(" - ")[0];

            java.util.List<String> result = RoomService.assign(roomId, patientId);
            if (result.get(0).startsWith("Room assigned")) {
                JOptionPane.showMessageDialog(dialog, "Patient assigned to room successfully!", "Success",
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

    private void vacateRoom() {
        int row = roomTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a room to vacate", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roomId = roomModel.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to vacate this room?",
                "Confirm Vacation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            java.util.List<String> result = RoomService.vacate(roomId);
            if (result.get(0).startsWith("Room vacated")) {
                JOptionPane.showMessageDialog(this, "Room vacated successfully", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void transferPatient() {
        int row = roomTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a room with a patient to transfer", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String currentRoomId = roomModel.getValueAt(row, 0).toString();
        Room currentRoom = DataStore.rooms.get(currentRoomId);
        if (currentRoom == null || currentRoom.occupantPatientId == null) {
            JOptionPane.showMessageDialog(this, "Selected room is not occupied", "Invalid Operation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Transfer Patient", true);
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("From Room"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField fromRoom = new JTextField(currentRoomId);
        fromRoom.setEditable(false);
        panel.add(fromRoom, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("To Room *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> toRoomCombo = new JComboBox<>();
        for (Room r : DataStore.rooms.values()) {
            if (r.status == RoomStatus.VACANT && !r.id.equals(currentRoomId)) {
                toRoomCombo.addItem(r.id + " - " + r.status);
            }
        }
        panel.add(toRoomCombo, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton transferBtn = new JButton("Transfer");
        styleButton(transferBtn, new Color(41, 128, 185));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(transferBtn);

        transferBtn.addActionListener(e -> {
            if (toRoomCombo.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a destination room", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String toRoomId = toRoomCombo.getSelectedItem().toString().split(" - ")[0];
            String patientId = currentRoom.occupantPatientId;

            // Vacate current room and assign to new room
            RoomService.vacate(currentRoomId);
            java.util.List<String> result = RoomService.assign(toRoomId, patientId);

            if (result.get(0).startsWith("Room assigned")) {
                JOptionPane.showMessageDialog(dialog, "Patient transferred successfully!", "Success",
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

    private void viewRoomDetails() {
        int row = roomTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a room to view details", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roomId = roomModel.getValueAt(row, 0).toString();
        Room room = DataStore.rooms.get(roomId);

        if (room == null)
            return;

        Patient patient = null;
        if (room.occupantPatientId != null) {
            patient = DataStore.patients.get(room.occupantPatientId);
        }

        String details = String.format(
                "Room ID: %s\n\n" +
                        "Status: %s\n" +
                        "Room Type: %s\n" +
                        "Floor: N/A\n\n" +
                        "Occupant: %s\n" +
                        "Patient ID: %s",
                room.id,
                room.status,
                room.status,
                patient != null ? patient.name : "Vacant",
                room.occupantPatientId != null ? room.occupantPatientId : "N/A");

        JOptionPane.showMessageDialog(this, details, "Room Details - " + roomId, JOptionPane.INFORMATION_MESSAGE);
    }

    public void refresh() {
        roomModel.setRowCount(0);

        int occupied = 0;
        int vacant = 0;

        for (Room room : DataStore.rooms.values()) {
            Patient patient = null;
            if (room.occupantPatientId != null) {
                patient = DataStore.patients.get(room.occupantPatientId);
            }

            String patientName = patient != null ? patient.name : "-";
            String status = room.status.toString();

            roomModel.addRow(new Object[] {
                    room.id,
                    status,
                    room.occupantPatientId != null ? room.occupantPatientId : "-",
                    patientName,
                    room.status,
                    "1" // Default floor
            });

            if (room.status == RoomStatus.OCCUPIED) {
                occupied++;
            } else {
                vacant++;
            }
        }

        int total = DataStore.rooms.size();
        double occupancyRate = total > 0 ? (occupied * 100.0 / total) : 0;

        String stats = String.format("Total Rooms: %d | Occupied: %d | Vacant: %d | Occupancy Rate: %.1f%%",
                total, occupied, vacant, occupancyRate);
        statsLabel.setText(stats);
    }
}
