package hpms.service;

import hpms.model.*;
import hpms.util.*;

import java.util.*;
import javax.swing.SwingUtilities;

public class RoomService {
    public static List<String> assign(String roomId, String patientId) {
        List<String> out = new ArrayList<>();
        Room r = DataStore.rooms.get(roomId);
        Patient p = DataStore.patients.get(patientId);
        if (r == null) {
            out.add("Error: Assigning room that does not exist");
            return out;
        }
        if (p == null) {
            out.add("Error: Invalid patient ID");
            return out;
        }
        PatientStatus status = PatientStatusService.getStatus(patientId);
        if (status != PatientStatus.INPATIENT) {
            // Enhanced error message with current status
            String message = "Error: Only inpatients can be assigned to rooms. Current status: " + status;
            out.add(message);
            // Audit log the failed attempt
            LogManager.log("assign_room_rejected patient=" + patientId + " room=" + roomId
                    + " reason=wrong_status current_status=" + status);
            return out;
        }
        // Check if room is occupied by a different patient
        if (r.occupantPatientId != null && !r.occupantPatientId.equals(patientId)) {
            out.add("Error: Room is already occupied by another patient");
            LogManager.log("assign_room_rejected patient=" + patientId + " room=" + roomId
                    + " reason=room_occupied_by_different_patient occupant=" + r.occupantPatientId);
            return out;
        }

        // Check if patient already has a room assigned
        for (Room room : DataStore.rooms.values()) {
            if (room.occupantPatientId != null && room.occupantPatientId.equals(patientId) && !room.id.equals(roomId)) {
                out.add("Error: Patient is already assigned to another room (" + room.id + ")");
                LogManager.log("assign_room_rejected patient=" + patientId + " room=" + roomId
                        + " reason=patient_already_has_room existing_room=" + room.id);
                return out;
            }
        }

        r.status = RoomStatus.OCCUPIED;
        r.occupantPatientId = patientId;
        // Enhanced audit log with more details
        LogManager.log("assign_room room=" + roomId + " patient=" + patientId
                + " patient_name=" + (p != null ? p.name : "unknown")
                + " status=" + status);
        // Disabled backup save - using database instead
        out.add("Room assigned");
        
        // Trigger global refresh of room displays
        SwingUtilities.invokeLater(() -> {
            refreshAllRoomPanels();
        });
        return out;
    }

    public static List<String> vacate(String roomId) {
        List<String> out = new ArrayList<>();
        Room r = DataStore.rooms.get(roomId);
        if (r == null) {
            out.add("Error: Room does not exist");
            return out;
        }
        String previousOccupant = r.occupantPatientId;
        r.status = RoomStatus.VACANT;
        r.occupantPatientId = null;
        // Enhanced audit log
        LogManager.log("vacate_room room=" + roomId
                + " previous_occupant=" + (previousOccupant != null ? previousOccupant : "none"));
        // Disabled backup save - using database instead
        out.add("Room vacated");
        
        // Trigger global refresh of room displays
        SwingUtilities.invokeLater(() -> {
            refreshAllRoomPanels();
        });
        return out;
    }
    
    /**
     * Refresh all room panels across the application to ensure synchronization
     */
    private static void refreshAllRoomPanels() {
        // Refresh all open room panels through the main GUI instances
        java.awt.Window[] windows = java.awt.Window.getWindows();
        for (java.awt.Window window : windows) {
            if (window instanceof hpms.ui.AdminGUI) {
                hpms.ui.AdminGUI adminGUI = (hpms.ui.AdminGUI) window;
                if (adminGUI.getRoomsPanel() != null) {
                    adminGUI.getRoomsPanel().refresh();
                }
            } else if (window instanceof hpms.ui.MainGUI) {
                hpms.ui.MainGUI mainGUI = (hpms.ui.MainGUI) window;
                // Refresh any room panels in main GUI
                java.awt.Component[] components = mainGUI.getContentPane().getComponents();
                for (java.awt.Component comp : components) {
                    if (comp instanceof hpms.ui.panels.RoomsPanel) {
                        ((hpms.ui.panels.RoomsPanel) comp).refresh();
                    }
                }
            }
        }
    }
}
