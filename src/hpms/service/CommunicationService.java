package hpms.service;

import hpms.model.*;
import hpms.util.*;
import hpms.util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class CommunicationService {

    private static void ensureCriticalAlertsTable(Connection conn) {
        if (conn == null)
            return;
        String sql = "CREATE TABLE IF NOT EXISTS patient_critical_alerts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "patient_id VARCHAR(20) NOT NULL," +
                "alert_text TEXT NOT NULL," +
                "created_by VARCHAR(20)," +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_pca_patient (patient_id)" +
                ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
    }

    private static void ensureStaffNotesTable(Connection conn) {
        if (conn == null)
            return;
        String sql = "CREATE TABLE IF NOT EXISTS patient_staff_notes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "patient_id VARCHAR(20) NOT NULL," +
                "staff_id VARCHAR(20) NOT NULL," +
                "note_text TEXT NOT NULL," +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_psn_patient (patient_id)" +
                ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
    }

    private static void saveCriticalAlert(Connection conn, String patientId, String createdBy, String text) throws SQLException {
        ensureCriticalAlertsTable(conn);
        String sql = "INSERT INTO patient_critical_alerts (patient_id, alert_text, created_by, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.setString(2, text == null ? "" : text.trim());
            stmt.setString(3, createdBy);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }

    private static void saveStaffNote(Connection conn, String patientId, String staffId, String text) throws SQLException {
        ensureStaffNotesTable(conn);
        String sql = "INSERT INTO patient_staff_notes (patient_id, staff_id, note_text, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.setString(2, staffId);
            stmt.setString(3, text == null ? "" : text.trim());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }

    public static void loadNotesAndAlertsFromDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return;
            ensureCriticalAlertsTable(conn);
            ensureStaffNotesTable(conn);

            // Critical alerts
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT patient_id, alert_text FROM patient_critical_alerts ORDER BY created_at ASC")) {
                ResultSet rs = stmt.executeQuery();
                DataStore.criticalAlerts.clear();
                while (rs.next()) {
                    String pid = rs.getString("patient_id");
                    String txt = rs.getString("alert_text");
                    if (pid == null)
                        continue;
                    DataStore.criticalAlerts.computeIfAbsent(pid, k -> new ArrayList<>())
                            .add(txt == null ? "" : txt);
                }
            }

            // Staff notes
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT patient_id, staff_id, note_text, created_at FROM patient_staff_notes ORDER BY created_at ASC")) {
                ResultSet rs = stmt.executeQuery();
                DataStore.staffNotes.clear();
                while (rs.next()) {
                    String pid = rs.getString("patient_id");
                    String sid = rs.getString("staff_id");
                    String txt = rs.getString("note_text");
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime at = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    if (pid == null)
                        continue;
                    DataStore.staffNotes.computeIfAbsent(pid, k -> new ArrayList<>())
                            .add(new StaffNote(sid, txt == null ? "" : txt, at));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading notes/alerts from DB: " + e.getMessage());
        }
    }

    public static List<String> addNote(String patientId, String staffId, String text) {
        List<String> out = new ArrayList<>();
        if (!DataStore.patients.containsKey(patientId)) {
            out.add("Error: Patient not found");
            return out;
        }
        if (!DataStore.staff.containsKey(staffId)) {
            out.add("Error: Staff not found");
            return out;
        }
        String cleaned = text == null ? "" : text.trim();
        LocalDateTime now = LocalDateTime.now();
        DataStore.staffNotes.computeIfAbsent(patientId, k -> new ArrayList<>())
                .add(new StaffNote(staffId, cleaned, now));
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null)
                saveStaffNote(conn, patientId, staffId, cleaned);
        } catch (SQLException e) {
            System.err.println("Error saving staff note to DB: " + e.getMessage());
        }
        LogManager.log("staff_note " + patientId);
        out.add("Note added");
        return out;
    }

    public static List<StaffNote> notes(String patientId) {
        return DataStore.staffNotes.getOrDefault(patientId, new ArrayList<>());
    }

    public static List<String> addAlert(String patientId, String text) {
        List<String> out = new ArrayList<>();
        if (!DataStore.patients.containsKey(patientId)) {
            out.add("Error: Patient not found");
            return out;
        }
        String cleaned = text == null ? "" : text.trim();
        DataStore.criticalAlerts.computeIfAbsent(patientId, k -> new ArrayList<>()).add(cleaned);
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null)
                saveCriticalAlert(conn, patientId, hpms.auth.AuthService.current == null ? null : hpms.auth.AuthService.current.username,
                        cleaned);
        } catch (SQLException e) {
            System.err.println("Error saving critical alert to DB: " + e.getMessage());
        }
        LogManager.log("critical_alert " + patientId);
        out.add("Alert added");
        return out;
    }

    public static List<String> alerts(String patientId) {
        return DataStore.criticalAlerts.getOrDefault(patientId, new ArrayList<>());
    }

    // NEW: Messaging system for patient-doctor communication
    public static List<String> sendMessage(String senderId, String recipientId, String subject, String messageContent,
            String messageType) {
        List<String> out = new ArrayList<>();

        if (Validators.empty(senderId) || Validators.empty(recipientId) || Validators.empty(subject)
                || Validators.empty(messageContent)) {
            out.add("Error: Missing required fields");
            return out;
        }

        String id = IDGenerator.nextId("C");
        Communication comm = new Communication(id, senderId, recipientId, subject, messageContent, LocalDateTime.now());
        comm.messageType = messageType;

        DataStore.communications.put(id, comm);
        DataStore.log("send_message " + senderId + " to " + recipientId);
        out.add("Message sent successfully");
        return out;
    }

    public static List<String> getInbox(String userId) {
        List<String> out = new ArrayList<>();

        for (Communication comm : DataStore.communications.values()) {
            if (comm.recipientId.equals(userId)) {
                String status = comm.isRead ? "[Read]" : "[NEW]";
                // Get sender's name - could be staff (doctor) or patient
                String senderName = comm.senderId;
                if (comm.senderId.startsWith("S")) {
                    // Staff member (doctor, nurse, etc.)
                    Staff staff = DataStore.staff.get(comm.senderId);
                    if (staff != null && staff.name != null) {
                        senderName = staff.name;
                    }
                } else if (comm.senderId.startsWith("P")) {
                    // Patient
                    Patient patient = DataStore.patients.get(comm.senderId);
                    if (patient != null && patient.name != null) {
                        senderName = patient.name;
                    }
                }
                out.add(status + " " + comm.sentDate + " From: " + senderName + " - " + comm.subject);
            }
        }

        if (out.isEmpty())
            out.add("No messages");
        return out;
    }

    public static List<String> markAsRead(String messageId) {
        List<String> out = new ArrayList<>();

        Communication comm = DataStore.communications.get(messageId);
        if (comm == null) {
            out.add("Error: Message not found");
            return out;
        }

        comm.isRead = true;
        comm.readDate = LocalDateTime.now();
        out.add("Message marked as read");
        return out;
    }

    public static List<String> getUnreadCount(String userId) {
        List<String> out = new ArrayList<>();
        int count = 0;

        for (Communication comm : DataStore.communications.values()) {
            if (comm.recipientId.equals(userId) && !comm.isRead) {
                count++;
            }
        }

        out.add("Unread messages: " + count);
        return out;
    }
}
