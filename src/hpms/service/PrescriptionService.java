package hpms.service;

import hpms.model.*;
import hpms.util.*;
import java.time.LocalDateTime;
import java.util.*;

public class PrescriptionService {
    
    public static List<String> prescribe(String appointmentId, String patientId, String doctorId, 
                                        String medicineName, String dosage, String frequency, 
                                        int durationDays, String indication) {
        List<String> out = new ArrayList<>();
        
        if (Validators.empty(appointmentId) || Validators.empty(patientId) || Validators.empty(doctorId) || 
            Validators.empty(medicineName) || Validators.empty(dosage) || Validators.empty(frequency)) {
            out.add("Error: Missing required fields");
            return out;
        }
        
        if (DataStore.patients.get(patientId) == null) {
            out.add("Error: Patient not found");
            return out;
        }
        
        if (DataStore.staff.get(doctorId) == null) {
            out.add("Error: Doctor not found");
            return out;
        }
        
        String id = IDGenerator.nextId("PR");
        Prescription p = new Prescription(id, appointmentId, patientId, doctorId, medicineName, 
                                         dosage, frequency, durationDays, LocalDateTime.now());
        p.indication = indication;
        
        DataStore.prescriptions.put(id, p);
        DataStore.log("prescribe " + id + " to " + patientId);
        out.add("Prescription created " + id);
        return out;
    }
    
    public static List<String> getPrescriptionsForPatient(String patientId) {
        List<String> out = new ArrayList<>();
        
        for (Prescription p : DataStore.prescriptions.values()) {
            if (p.patientId.equals(patientId) && p.isActive) {
                // Use medicineId field as medicine name since we removed the medicine system
                String medName = p.medicineId != null ? p.medicineId : "Unknown";
                out.add(p.id + ": " + medName + " " + p.dosage + " " + p.frequency + " for " + p.durationDays + " days");
            }
        }
        
        if (out.isEmpty()) out.add("No active prescriptions");
        return out;
    }
    
    public static List<String> discontinue(String prescriptionId) {
        List<String> out = new ArrayList<>();
        
        Prescription p = DataStore.prescriptions.get(prescriptionId);
        if (p == null) {
            out.add("Error: Prescription not found");
            return out;
        }
        
        p.isActive = false;
        DataStore.log("discontinue_prescription " + prescriptionId);
        out.add("Prescription discontinued");
        return out;
    }
}
