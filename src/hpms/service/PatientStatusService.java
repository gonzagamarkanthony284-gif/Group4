package hpms.service;

import hpms.model.*;
import hpms.util.*;

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
        PatientStatus st;
        try {
            st = PatientStatus.valueOf(status.toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            out.add("Error: Invalid status");
            return out;
        }

        // If changing to OUTPATIENT, clear any room assignment
        PatientStatus oldStatus = DataStore.patientStatus.get(patientId);
        if (st == PatientStatus.OUTPATIENT) {
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
        // Enhanced audit log with detailed status change information
        LogManager.log("status_change patient=" + patientId
                + " from=" + (oldStatus != null ? oldStatus : "NEW")
                + " to=" + st
                + " by=" + (byStaffId != null ? byStaffId : "UNKNOWN")
                + " note=" + (note != null && !note.isEmpty() ? note : "none"));
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.add("Status updated to " + st);
        return out;
    }

    public static PatientStatus getStatus(String patientId) {
        return DataStore.patientStatus.getOrDefault(patientId, PatientStatus.OUTPATIENT);
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
}
