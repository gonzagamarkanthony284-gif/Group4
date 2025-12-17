package hpms.service;

import hpms.model.*;
import hpms.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatientStatusService {
    public static List<String> setStatus(String patientId, String status, String byStaffId, String note) {
        List<String> out = new ArrayList<>();
        Patient p = DataStore.patients.get(patientId);
        if (p == null) {
            out.add("Error: Patient not found");
            return out;
        }

        // CRITICAL: Once a patient is marked as OUTPATIENT or DISCHARGED, their record
        // is LOCKED
        // No further status changes are allowed
        PatientStatus currentStatus = DataStore.patientStatus.get(patientId);
        if (currentStatus == PatientStatus.OUTPATIENT || currentStatus == PatientStatus.DISCHARGED
                || p.isOutpatientPermanent) {
            out.add("Error: Patient record is locked. Once marked as Outpatient or Discharged, status cannot be changed. "
                    + "If returning, please create a new patient record.");
            LogManager.log("status_change_rejected patient=" + patientId
                    + " reason=record_locked current_status=" + currentStatus
                    + " permanent_outpatient=" + p.isOutpatientPermanent
                    + " attempted_status=" + status.toUpperCase(java.util.Locale.ROOT));
            return out;
        }

        PatientStatus st;
        try {
            st = PatientStatus.valueOf(status.toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            out.add("Error: Invalid status");
            return out;
        }

        // If changing to OUTPATIENT, clear any room assignment and lock the record
        PatientStatus oldStatus = DataStore.patientStatus.get(patientId);
        if (st == PatientStatus.OUTPATIENT || st == PatientStatus.DISCHARGED) {
            // Mark patient as permanently outpatient/discharged
            p.isOutpatientPermanent = true;
            for (Room r : DataStore.rooms.values()) {
                if (patientId.equals(r.occupantPatientId)) {
                    r.status = RoomStatus.VACANT;
                    r.occupantPatientId = null;
                    // Enhanced audit log with previous and new status
                    LogManager.log("auto_vacate_room " + r.id + " patient_now_outpatient " + patientId
                            + " previous_status=" + (oldStatus != null ? oldStatus : "UNKNOWN")
                            + " by_staff=" + (byStaffId != null ? byStaffId : "SYSTEM"));
                    break;
                }
            }
        }

        DataStore.patientStatus.put(patientId, st);
        DataStore.statusHistory.computeIfAbsent(patientId, k -> new ArrayList<>())
                .add(new StatusHistoryEntry(st, LocalDateTime.now(), byStaffId, note));
        
        // Save to database
        try (Connection conn = DBConnection.getConnection()) {
            String insertSql = "INSERT INTO patient_status (patient_id, status, created_at, changed_by, note) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, patientId);
                stmt.setString(2, st.toString());
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(4, byStaffId);
                stmt.setString(5, note);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error saving patient status to database: " + e.getMessage());
            // Continue with in-memory update even if database save fails
        }
        
        // Enhanced audit log with detailed status change information
        LogManager.log("status_change patient=" + patientId
                + " from=" + (oldStatus != null ? oldStatus : "NEW")
                + " to=" + st
                + " by=" + (byStaffId != null ? byStaffId : "UNKNOWN")
                + " note=" + (note != null && !note.isEmpty() ? note : "none"));
        // Disabled backup save - using database instead
        out.add("Status updated to " + st);
        return out;
    }

    public static PatientStatus getStatus(String patientId) {
        return DataStore.patientStatus.getOrDefault(patientId, PatientStatus.INPATIENT);
    }

    /**
     * Get the category/status of a patient for UI grouping purposes.
     * Returns the patient's primary status from the PatientStatus registry.
     * Defaults to OUTPATIENT if no explicit status is set.
     */
    public static PatientStatus getPatientCategory(String patientId) {
        return getStatus(patientId);
    }

    public static List<StatusHistoryEntry> history(String patientId) {
        return DataStore.statusHistory.getOrDefault(patientId, new ArrayList<>());
    }
    
    /**
     * Load all patient status data from database into DataStore
     */
    public static void loadFromDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection is null - cannot load patient status");
                return;
            }
            
            System.out.println("Loading patient status data from database...");
            
            // Load patient status data
            String statusSql = "SELECT patient_id, status, created_at, changed_by, note FROM patient_status ORDER BY patient_id, created_at";
            try (PreparedStatement statusStmt = conn.prepareStatement(statusSql)) {
                ResultSet statusRs = statusStmt.executeQuery();
                int count = 0;
                while (statusRs.next()) {
                    String patientId = statusRs.getString("patient_id");
                    String statusStr = statusRs.getString("status");
                    PatientStatus status = PatientStatus.valueOf(statusStr.toUpperCase());
                    LocalDateTime changedAt = statusRs.getTimestamp("created_at").toLocalDateTime();
                    String changedBy = statusRs.getString("changed_by");
                    String note = statusRs.getString("note");
                    
                    // Set current status (last entry for each patient)
                    DataStore.patientStatus.put(patientId, status);
                    
                    // Add to history
                    DataStore.statusHistory.computeIfAbsent(patientId, k -> new ArrayList<>())
                            .add(new StatusHistoryEntry(status, changedAt, changedBy, note));
                    count++;
                }
                
                System.out.println("Successfully loaded patient status data for " + DataStore.patientStatus.size() + " patients (" + count + " records) from database");
            }
            
        } catch (SQLException e) {
            System.err.println("SQL Error loading patient status data: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.out.println("Patient status will be managed in-memory only.");
        } catch (Exception e) {
            System.err.println("General error loading patient status data: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Patient status will be managed in-memory only.");
        }
    }
}
