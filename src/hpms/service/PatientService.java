package hpms.service;

import hpms.model.*;
import hpms.util.*;

import java.time.LocalDateTime;
import java.util.*;

public class PatientService {
    public static List<String> add(String name, String age, String birthday, String gender, String contact,
            String address, String patientType) {
        List<String> out = new ArrayList<>();
        if (Validators.empty(name) || Validators.empty(age) || Validators.empty(birthday) || Validators.empty(gender)
                || Validators.empty(contact) || Validators.empty(address) || Validators.empty(patientType)) {
            out.add("Error: Missing required fields. Please provide: Name, Age, Birthday, Gender, Contact, Address, and Patient Type (INPATIENT/OUTPATIENT)");
            return out;
        }
        Gender g;
        try {
            String gu = gender == null ? "" : gender.trim();
            String genderUpper = gu.toUpperCase(Locale.ROOT);
            // Normalize common forms and map to enum safely
            if ("MALE".equalsIgnoreCase(gu) || "M".equalsIgnoreCase(gu))
                g = Gender.Male;
            else if ("FEMALE".equalsIgnoreCase(gu) || "F".equalsIgnoreCase(gu))
                g = Gender.Female;
            else if ("LGBTQ+".equalsIgnoreCase(gu) || "LGBTQ_PLUS".equalsIgnoreCase(genderUpper)
                    || "LGBTQ".equalsIgnoreCase(genderUpper))
                g = Gender.LGBTQ_PLUS;
            else
                g = Gender.OTHER; // default for 'Other', 'Prefer not to say' and unknown values
        } catch (Exception e) {
            out.add("Error: Invalid gender");
            return out;
        }
        int a;
        try {
            a = Integer.parseInt(age.trim());
        } catch (Exception e) {
            out.add("Error: Invalid age");
            return out;
        }
        if (a <= 0) {
            out.add("Error: Invalid age");
            return out;
        }
        for (Patient p : DataStore.patients.values())
            if (p.contact.equalsIgnoreCase(contact.trim())) {
                out.add("Error: Duplicate contact");
                return out;
            }
        // Validate patient type
        String ptType = patientType.trim().toUpperCase();
        if (!ptType.equals("INPATIENT") && !ptType.equals("OUTPATIENT")) {
            out.add("Error: Patient Type must be either INPATIENT or OUTPATIENT");
            return out;
        }
        String id = IDGenerator.nextId("P");
        Patient p = new Patient(id, name.trim(), a, birthday.trim(), g, contact.trim(), address.trim(),
                LocalDateTime.now());
        p.patientType = ptType;
        p.validateCompleteness(); // Check if all required fields are complete
        DataStore.patients.put(id, p);
        LogManager.log("add_patient id=" + id + " complete=" + p.isComplete + " type=" + p.patientType);
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.add("Patient created " + id);
        return out;
    }

    // extended add allowing optional patient-provided and insurance fields
    public static List<String> add(String name, String age, String birthday, String gender, String contact,
            String address,
            String patientType, String registrationType, String incidentTime, String broughtBy,
            String initialBp, String initialHr, String initialSpo2, String chiefComplaint,
            String allergies, String medications, String pastMedicalHistory,
            String smokingStatus, String alcoholUse, String occupation,
            String insuranceProvider, String insuranceId, String policyHolderName,
            String policyHolderDob, String policyRelationship) {
        List<String> out = add(name, age, birthday, gender, contact, address, patientType);
        if (out.isEmpty()) {
            out.add("Error: unknown failure");
            return out;
        }
        // if creation succeeded, parse id from returned message "Patient created <id>"
        String created = out.get(0);
        if (!created.startsWith("Patient created "))
            return out;
        String id = created.split(" ")[2];
        Patient p = DataStore.patients.get(id);
        if (p == null) {
            out.clear();
            out.add("Error: creation failed");
            return out;
        }
        // registration / arrival metadata
        if (registrationType != null && !registrationType.trim().isEmpty())
            p.registrationType = registrationType.trim();
        if (p.registrationType == null || p.registrationType.trim().isEmpty())
            p.registrationType = "Walk-in Patient";
        if (incidentTime != null)
            p.incidentTime = incidentTime.trim();
        if (broughtBy != null)
            p.broughtBy = broughtBy.trim();
        if (initialBp != null)
            p.initialBp = initialBp.trim();
        if (initialHr != null)
            p.initialHr = initialHr.trim();
        if (initialSpo2 != null)
            p.initialSpo2 = initialSpo2.trim();
        if (chiefComplaint != null)
            p.chiefComplaint = chiefComplaint.trim();
        if (allergies != null)
            p.allergies = allergies.trim();
        if (medications != null)
            p.medications = medications.trim();
        if (pastMedicalHistory != null)
            p.pastMedicalHistory = pastMedicalHistory.trim();
        if (smokingStatus != null)
            p.smokingStatus = smokingStatus.trim();
        if (alcoholUse != null)
            p.alcoholUse = alcoholUse.trim();
        if (occupation != null)
            p.occupation = occupation.trim();
        if (insuranceProvider != null)
            p.insuranceProvider = insuranceProvider.trim();
        if (insuranceId != null)
            p.insuranceId = insuranceId.trim();
        if (policyHolderName != null)
            p.policyHolderName = policyHolderName.trim();
        if (policyHolderDob != null)
            p.policyHolderDob = policyHolderDob.trim();
        if (policyRelationship != null)
            p.policyRelationship = policyRelationship.trim();
        LogManager.log("add_patient_extended " + id);
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.clear();
        out.add("Patient created " + id);
        return out;
    }

    // extended edit to update patient-provided and insurance fields
    public static List<String> editExtended(String id, String name, String age, String birthday, String gender,
            String contact,
            String address, String patientType,
            String registrationType, String incidentTime, String broughtBy,
            String initialBp, String initialHr, String initialSpo2, String chiefComplaint,
            String allergies, String medications, String pastMedicalHistory,
            String smokingStatus, String alcoholUse, String occupation,
            String insuranceProvider, String insuranceId, String policyHolderName,
            String policyHolderDob, String policyRelationship) {
        List<String> out = edit(id, name, age, gender, contact, address);
        if (out.isEmpty()) {
            out.add("Error: unknown failure");
            return out;
        }
        Patient p = DataStore.patients.get(id);
        // Update birthday and patient type
        if (birthday != null && !birthday.trim().isEmpty())
            p.birthday = birthday.trim();

        // CRITICAL RULE: If patient is marked as OUTPATIENT, that status is PERMANENT
        // and CANNOT be changed
        if (patientType != null && !patientType.trim().isEmpty()) {
            String ptType = patientType.trim().toUpperCase();
            if (ptType.equals("INPATIENT") || ptType.equals("OUTPATIENT")) {
                // If patient is being set to OUTPATIENT for the first time, mark it permanent
                if ("OUTPATIENT".equals(ptType) && !p.isOutpatientPermanent) {
                    p.patientType = ptType;
                    p.isOutpatientPermanent = true; // Once OUTPATIENT, always OUTPATIENT
                } else if (p.isOutpatientPermanent) {
                    // If already marked as permanent OUTPATIENT, DO NOT allow any changes
                    // Silently ignore the change attempt - patient remains OUTPATIENT
                } else {
                    // Patient is not permanent outpatient yet, can change freely
                    p.patientType = ptType;
                }
            }
        }
        if (p == null) {
            out.clear();
            out.add("Error: Invalid patient ID");
            return out;
        }

        // IMPORTANT: Registration/arrival data is ONLY set on FIRST registration
        // Subsequent arrivals should use VisitService.createVisit() instead
        // This preserves the original arrival data forever
        boolean isFirstArrival = (p.registrationType == null || p.registrationType.trim().isEmpty());

        if (isFirstArrival) {
            // First arrival - set the initial arrival data in patient record
            if (registrationType != null && !registrationType.trim().isEmpty())
                p.registrationType = registrationType.trim();
            if (p.registrationType == null || p.registrationType.trim().isEmpty())
                p.registrationType = "Walk-in Patient";
            if (incidentTime != null)
                p.incidentTime = incidentTime.trim();
            if (broughtBy != null)
                p.broughtBy = broughtBy.trim();
            if (initialBp != null)
                p.initialBp = initialBp.trim();
            if (initialHr != null)
                p.initialHr = initialHr.trim();
            if (initialSpo2 != null)
                p.initialSpo2 = initialSpo2.trim();
            if (chiefComplaint != null)
                p.chiefComplaint = chiefComplaint.trim();

            LogManager.log("first_arrival_data_saved patient=" + id + " type=" + p.registrationType);
        } else {
            // Patient already has arrival data - DO NOT OVERWRITE
            // New arrivals should be handled by VisitService.createVisit()
            LogManager.log("edit_patient_skip_arrival_data patient=" + id + " (first arrival preserved)");
        }

        // These fields CAN be updated on subsequent edits (not arrival-specific)
        if (allergies != null)
            p.allergies = allergies.trim();
        if (medications != null)
            p.medications = medications.trim();
        if (pastMedicalHistory != null)
            p.pastMedicalHistory = pastMedicalHistory.trim();
        if (smokingStatus != null)
            p.smokingStatus = smokingStatus.trim();
        if (alcoholUse != null)
            p.alcoholUse = alcoholUse.trim();
        if (occupation != null)
            p.occupation = occupation.trim();
        if (insuranceProvider != null)
            p.insuranceProvider = insuranceProvider.trim();
        if (insuranceId != null)
            p.insuranceId = insuranceId.trim();
        if (policyHolderName != null)
            p.policyHolderName = policyHolderName.trim();
        if (policyHolderDob != null)
            p.policyHolderDob = policyHolderDob.trim();
        if (policyRelationship != null)
            p.policyRelationship = policyRelationship.trim();
        p.validateCompleteness(); // Re-validate after editing extended fields
        LogManager.log("edit_patient_extended " + id + " complete=" + p.isComplete);
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.clear();
        out.add("Patient updated " + id);
        return out;
    }

    public static void migrateRegistrationTypeDefault() {
        boolean changed = false;
        for (Patient p : DataStore.patients.values()) {
            if (p == null)
                continue;
            if (p.registrationType == null || p.registrationType.trim().isEmpty()) {
                p.registrationType = "Walk-in Patient";
                changed = true;
            }
        }
        if (changed) {
            try {
                BackupUtil.saveToDefault();
            } catch (Exception ex) {
            }
        }
    }

    // staff (doctor/nurse) adds clinical info: vitals and a progress note
    public interface ClinicalUpdateListener {
        void clinicalUpdated(String patientId);
    }

    private static final java.util.List<ClinicalUpdateListener> clinicalListeners = new java.util.ArrayList<>();

    public static void addClinicalUpdateListener(ClinicalUpdateListener l) {
        if (l != null)
            clinicalListeners.add(l);
    }

    public static void removeClinicalUpdateListener(ClinicalUpdateListener l) {
        clinicalListeners.remove(l);
    }

    private static void notifyClinicalUpdate(String patientId) {
        for (ClinicalUpdateListener l : clinicalListeners) {
            try {
                l.clinicalUpdated(patientId);
            } catch (Exception ex) {
                /* ignore listener error */ }
        }
    }

    public static List<String> addClinicalInfo(String id, String heightCmStr, String weightKgStr, String bloodPressure,
            String note, String byStaffId,
            String xrayPath, String xrayStatus, String xraySummary,
            String stoolPath, String stoolStatus, String stoolSummary,
            String urinePath, String urineStatus, String urineSummary,
            String bloodPath, String bloodStatus, String bloodSummary,
            java.util.List<String> otherAttachments) {
        List<String> out = new ArrayList<>();
        Patient p = DataStore.patients.get(id);
        if (p == null) {
            out.add("Error: Invalid patient ID");
            return out;
        }
        try {
            if (heightCmStr != null && !heightCmStr.trim().isEmpty())
                p.heightCm = Double.parseDouble(heightCmStr.trim());
        } catch (Exception e) {
            out.add("Error: invalid height");
            return out;
        }
        try {
            if (weightKgStr != null && !weightKgStr.trim().isEmpty())
                p.weightKg = Double.parseDouble(weightKgStr.trim());
        } catch (Exception e) {
            out.add("Error: invalid weight");
            return out;
        }
        if (bloodPressure != null)
            p.bloodPressure = bloodPressure.trim();
        if (note != null && !note.trim().isEmpty())
            p.progressNotes.add(java.time.LocalDateTime.now() + " by " + (byStaffId == null ? "unknown" : byStaffId)
                    + ": " + note.trim());
        // store uploaded test paths and summaries (optional)
        if (xrayPath != null)
            p.xrayFilePath = xrayPath;
        if (xrayStatus != null)
            p.xrayStatus = xrayStatus;
        if (xraySummary != null)
            p.xraySummary = xraySummary;

        if (stoolPath != null)
            p.stoolFilePath = stoolPath;
        if (stoolStatus != null)
            p.stoolStatus = stoolStatus;
        if (stoolSummary != null)
            p.stoolSummary = stoolSummary;

        if (urinePath != null)
            p.urineFilePath = urinePath;
        if (urineStatus != null)
            p.urineStatus = urineStatus;
        if (urineSummary != null)
            p.urineSummary = urineSummary;

        if (bloodPath != null)
            p.bloodFilePath = bloodPath;
        if (bloodStatus != null)
            p.bloodStatus = bloodStatus;
        if (bloodSummary != null)
            p.bloodSummary = bloodSummary;
        // add any additional attachments to the patient's attachments list
        if (otherAttachments != null && !otherAttachments.isEmpty()) {
            for (String a : otherAttachments)
                if (a != null && !a.trim().isEmpty())
                    p.attachmentPaths.add(a.trim());
        }
        LogManager.log("add_clinical " + id + " by " + (byStaffId == null ? "?" : byStaffId));
        // notify listeners so dashboards and other UI can refresh
        notifyClinicalUpdate(id);
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.add("Clinical info updated " + id);
        return out;
    }

    public static List<String> edit(String id, String name, String age, String gender, String contact, String address) {
        List<String> out = new ArrayList<>();
        Patient p = DataStore.patients.get(id);
        if (p == null) {
            out.add("Error: Invalid patient ID");
            return out;
        }
        if (Validators.empty(name) || Validators.empty(age) || Validators.empty(gender) || Validators.empty(contact)
                || Validators.empty(address)) {
            out.add("Error: Missing parameters");
            return out;
        }
        Gender g;
        try {
            String gu = gender == null ? "" : gender.trim();
            String genderUpper = gu.toUpperCase(Locale.ROOT);
            if ("MALE".equalsIgnoreCase(gu) || "M".equalsIgnoreCase(gu))
                g = Gender.Male;
            else if ("FEMALE".equalsIgnoreCase(gu) || "F".equalsIgnoreCase(gu))
                g = Gender.Female;
            else if ("LGBTQ+".equalsIgnoreCase(gu) || "LGBTQ_PLUS".equalsIgnoreCase(genderUpper)
                    || "LGBTQ".equalsIgnoreCase(genderUpper))
                g = Gender.LGBTQ_PLUS;
            else
                g = Gender.OTHER;
        } catch (Exception e) {
            out.add("Error: Invalid gender");
            return out;
        }
        int a;
        try {
            a = Integer.parseInt(age.trim());
        } catch (Exception e) {
            out.add("Error: Invalid age");
            return out;
        }
        if (a <= 0) {
            out.add("Error: Invalid age");
            return out;
        }
        for (Patient other : DataStore.patients.values())
            if (!other.id.equals(id) && other.contact.equalsIgnoreCase(contact.trim())) {
                out.add("Error: Duplicate contact");
                return out;
            }
        p.name = name.trim();
        p.age = a;
        p.gender = g;
        p.contact = contact.trim();
        p.address = address.trim();
        p.validateCompleteness(); // Re-validate completeness after edit
        LogManager.log("edit_patient " + id + " complete=" + p.isComplete);
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.add("Patient updated " + id);
        return out;
    }

    // Deactivate patient instead of deleting
    public static List<String> deactivate(String id) {
        List<String> out = new ArrayList<>();
        Patient p = DataStore.patients.get(id);
        if (p == null) {
            out.add("Error: Invalid patient ID");
            return out;
        }
        if (!p.isActive) {
            out.add("Error: Patient is already inactive");
            return out;
        }
        p.isActive = false;
        // Clear room assignment if patient is in a room
        String clearedRoom = null;
        for (Room r : DataStore.rooms.values()) {
            if (id.equals(r.occupantPatientId)) {
                clearedRoom = r.id;
                r.status = RoomStatus.VACANT;
                r.occupantPatientId = null;
            }
        }
        // Enhanced audit log
        PatientStatus status = PatientStatusService.getStatus(id);
        LogManager.log("deactivate_patient patient=" + id
                + " name=" + p.name
                + " status=" + status
                + " room_cleared=" + (clearedRoom != null ? clearedRoom : "none")
                + " reason=manual_deactivation");
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.add("Patient deactivated " + id + " - Record preserved in database");
        return out;
    }

    // Reactivate patient
    public static List<String> reactivate(String id) {
        List<String> out = new ArrayList<>();
        Patient p = DataStore.patients.get(id);
        if (p == null) {
            out.add("Error: Invalid patient ID");
            return out;
        }
        if (p.isActive) {
            out.add("Error: Patient is already active");
            return out;
        }
        p.isActive = true;
        // Enhanced audit log
        PatientStatus status = PatientStatusService.getStatus(id);
        LogManager.log("reactivate_patient patient=" + id
                + " name=" + p.name
                + " status=" + status
                + " reason=manual_reactivation");
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.add("Patient reactivated " + id);
        return out;
    }

    // Legacy delete method - now calls deactivate
    @Deprecated
    public static List<String> delete(String id) {
        return deactivate(id);
    }

    public static List<String> search(String id) {
        List<String> out = new ArrayList<>();
        Patient p = DataStore.patients.get(id);
        if (p == null) {
            out.add("Error: Invalid patient ID");
            return out;
        }
        out.add(p.id + " " + p.name + " " + p.age + " " + p.gender);
        return out;
    }

    /**
     * Remove a patient from a doctor's assignment.
     * This deletes all appointments between the doctor and patient,
     * and clears the patient's insurance information.
     */
    public static List<String> removePatientFromDoctor(String patientId, String doctorId) {
        List<String> out = new ArrayList<>();

        // Validate inputs
        if (Validators.empty(patientId) || Validators.empty(doctorId)) {
            out.add("Error: Missing patient or doctor ID");
            return out;
        }

        Patient p = DataStore.patients.get(patientId);
        if (p == null) {
            out.add("Error: Patient not found");
            return out;
        }

        hpms.model.Staff doctor = DataStore.staff.get(doctorId);
        if (doctor == null) {
            out.add("Error: Doctor not found");
            return out;
        }

        // Remove all appointments between doctor and patient
        java.util.List<String> appointmentsToRemove = new java.util.ArrayList<>();
        for (hpms.model.Appointment appt : DataStore.appointments.values()) {
            if (appt.patientId.equals(patientId) && appt.staffId.equals(doctorId)) {
                appointmentsToRemove.add(appt.id);
            }
        }

        // Delete the appointments
        int appointmentsDeleted = 0;
        for (String apptId : appointmentsToRemove) {
            DataStore.appointments.remove(apptId);
            appointmentsDeleted++;
        }

        // Clear insurance information
        p.insuranceProvider = "";
        p.insuranceId = "";
        p.insuranceGroup = "";
        p.policyHolderName = "";
        p.policyHolderDob = "";
        p.policyRelationship = "";
        p.secondaryInsurance = "";

        // Log the action
        LogManager.log("remove_patient_from_doctor " + patientId + " from " + doctorId);

        // Save to backup
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
            out.add("Warning: Changes saved but backup failed");
            return out;
        }

        out.add("Patient successfully removed from doctor assignment. " + appointmentsDeleted
                + " appointment(s) deleted. Insurance information cleared.");
        return out;
    }
}
