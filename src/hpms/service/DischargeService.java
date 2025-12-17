package hpms.service;

import hpms.model.*;
import hpms.util.*;
import java.time.LocalDateTime;
import java.util.*;

public class DischargeService {

    public static List<String> discharge(String patientId, String roomId, String doctorId,
            String primaryDiagnosis, String dischargeSummary) {
        List<String> out = new ArrayList<>();

        if (DataStore.patients.get(patientId) == null) {
            out.add("Error: Patient not found");
            return out;
        }

        if (DataStore.staff.get(doctorId) == null) {
            out.add("Error: Doctor not found");
            return out;
        }

        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            out.add("Error: Room not found");
            return out;
        }

        // Check outpatient discharge rules
        PatientStatus currentStatus = PatientStatusService.getStatus(patientId);
        if (currentStatus == PatientStatus.OUTPATIENT) {
            // Check if patient was inpatient on the same day
            List<StatusHistoryEntry> history = PatientStatusService.history(patientId);
            LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
            boolean wasInpatientToday = history.stream()
                    .anyMatch(entry -> entry.status == PatientStatus.INPATIENT &&
                            entry.at.toLocalDate().isEqual(today.toLocalDate()));
            if (!wasInpatientToday) {
                out.add("Error: Outpatients cannot be discharged on the same day unless status was changed to Inpatient");
                return out;
            }
        }

        // Create discharge record
        String id = IDGenerator.nextId("D");
        Discharge discharge = new Discharge(id, patientId, roomId, doctorId, LocalDateTime.now(), LocalDateTime.now());
        discharge.primaryDiagnosis = primaryDiagnosis;
        discharge.dischargeSummary = dischargeSummary;

        DataStore.discharges.put(id, discharge);

        // Vacate room
        room.status = RoomStatus.VACANT;
        room.occupantPatientId = null;

        DataStore.log("discharge_patient " + patientId + " from " + roomId);
        // Disabled backup save - using database instead
        out.add("Patient discharged successfully. Discharge ID: " + id);
        return out;
    }

    public static List<String> addFollowUp(String dischargeId, String followUpDate, String instructions) {
        List<String> out = new ArrayList<>();

        Discharge discharge = DataStore.discharges.get(dischargeId);
        if (discharge == null) {
            out.add("Error: Discharge record not found");
            return out;
        }

        try {
            discharge.followUpAppointmentDate = LocalDateTime.parse(followUpDate);
            discharge.followUpInstructions = instructions;
            DataStore.log("add_followup " + dischargeId);
            out.add("Follow-up instructions added");
        } catch (Exception e) {
            out.add("Error: Invalid date format");
        }

        return out;
    }

    public static List<String> getDischargeSummary(String patientId) {
        List<String> out = new ArrayList<>();

        for (Discharge d : DataStore.discharges.values()) {
            if (d.patientId.equals(patientId)) {
                out.add("Discharge Date: " + d.dischargeDate);
                out.add("Diagnosis: " + d.primaryDiagnosis);
                out.add("Summary: " + d.dischargeSummary);
                if (d.followUpAppointmentDate != null) {
                    out.add("Follow-up: " + d.followUpAppointmentDate);
                }
            }
        }

        if (out.isEmpty())
            out.add("No discharge records found");
        return out;
    }
}
