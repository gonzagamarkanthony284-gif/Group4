package hpms.service;

import hpms.model.*;
import hpms.util.*;

import java.time.LocalDateTime;
import java.util.*;
import java.sql.*;

public class PatientService {

    private static void ensurePatientClinicalColumns(Connection conn) {
        if (conn == null)
            return;
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN email VARCHAR(255)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN patient_type VARCHAR(30)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN allergies TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN medications TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN past_medical_history TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        // Clinical snapshot
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN height_cm DOUBLE")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN weight_kg DOUBLE")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN blood_pressure VARCHAR(50)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN xray_file_path TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN xray_status VARCHAR(50)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN xray_summary TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN stool_file_path TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN stool_status VARCHAR(50)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN stool_summary TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN urine_file_path TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN urine_status VARCHAR(50)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN urine_summary TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN blood_file_path TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN blood_status VARCHAR(50)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE patients ADD COLUMN blood_summary TEXT")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
    }

    private static void ensurePatientProgressNotesTable(Connection conn) {
        if (conn == null)
            return;
        try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS patient_progress_notes (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "patient_id VARCHAR(20) NOT NULL," +
                        "note_text TEXT NOT NULL," +
                        "created_by VARCHAR(20)," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "INDEX idx_ppn_patient (patient_id)" +
                        ")")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        // Migration: older installations may have created this table without note_text
        try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE patient_progress_notes ADD COLUMN note_text TEXT NOT NULL")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        // Migration: older installations may have created this table without created_by
        try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE patient_progress_notes ADD COLUMN created_by VARCHAR(20)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        // Migration: older installations may have a legacy NOT NULL 'note' column that breaks inserts
        // (because newer code inserts into note_text only). Make it nullable.
        try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE patient_progress_notes MODIFY COLUMN note TEXT NULL")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }

        // Optional backfill if an older column name exists
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE patient_progress_notes SET note_text = note WHERE (note_text IS NULL OR note_text = '')")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
    }

    private static void addProgressNoteToDatabase(Connection conn, String patientId, String byStaffId, String note) {
        if (conn == null)
            return;
        if (patientId == null || patientId.trim().isEmpty())
            return;
        if (note == null || note.trim().isEmpty())
            return;
        ensurePatientProgressNotesTable(conn);
        String sql = "INSERT INTO patient_progress_notes (patient_id, note_text, created_by, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.setString(2, note.trim());
            stmt.setString(3, byStaffId);
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving progress note: " + e.getMessage());
        }
    }

    private static void loadProgressNotesFromDatabase(Connection conn) {
        if (conn == null)
            return;
        ensurePatientProgressNotesTable(conn);
        String sql = "SELECT patient_id, note_text, created_by, created_at FROM patient_progress_notes ORDER BY created_at ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String pid = rs.getString("patient_id");
                Patient p = DataStore.patients.get(pid);
                if (p == null)
                    continue;
                String noteText = rs.getString("note_text");
                String by = rs.getString("created_by");
                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                String at = ts != null ? ts.toLocalDateTime().toString() : java.time.LocalDateTime.now().toString();
                if (noteText != null && !noteText.trim().isEmpty()) {
                    p.progressNotes.add(at + " by " + (by == null ? "unknown" : by) + ": " + noteText.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading progress notes: " + e.getMessage());
        }
    }
    public static List<String> add(String name, String age, String birthday, String gender, String contact,
            String address, String patientType) {
        List<String> out = new ArrayList<>();
        if (Validators.empty(name) || Validators.empty(age) || Validators.empty(birthday) || Validators.empty(gender)
                || Validators.empty(contact) || Validators.empty(address) || Validators.empty(patientType)) {
            out.add("Error: Missing required fields. Please provide: Name, Age, Birthday, Gender, Contact, Address, and Patient Type (INPATIENT/EMERGENCY/OUTPATIENT)");
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
        if (!ptType.equals("INPATIENT") && !ptType.equals("OUTPATIENT") && !ptType.equals("EMERGENCY")) {
            out.add("Error: Patient Type must be either INPATIENT, EMERGENCY, or OUTPATIENT");
            return out;
        }
        String id = IDGenerator.nextId("P");
        Patient p = new Patient(id, name.trim(), a, birthday.trim(), g, contact.trim(), address.trim(),
                LocalDateTime.now());
        // If contact was stored as "<phone> | <email>", keep email separately as well
        if (p.contact != null) {
            String[] parts = p.contact.split("\\|");
            for (String part : parts) {
                String candidate = part == null ? null : part.trim();
                if (candidate != null && hpms.util.Validators.isValidEmail(candidate)) {
                    p.email = candidate;
                    break;
                }
            }
        }
        p.patientType = ptType;
        p.validateCompleteness(); // Check if all required fields are complete
        DataStore.patients.put(id, p);

        // Initialize patient status based on patient type
        PatientStatus initialStatus;
        if (ptType.equals("INPATIENT")) {
            initialStatus = PatientStatus.INPATIENT;
        } else if (ptType.equals("EMERGENCY")) {
            initialStatus = PatientStatus.EMERGENCY;
        } else {
            // Don't auto-set to OUTPATIENT - leave status unset until explicitly set
            initialStatus = null;
        }
        if (initialStatus != null) {
            DataStore.patientStatus.put(id, initialStatus);
            DataStore.statusHistory.computeIfAbsent(id, k -> new ArrayList<>())
                    .add(new StatusHistoryEntry(initialStatus, LocalDateTime.now(), "SYSTEM", "Initial registration"));
        }

        LogManager.log("add_patient id=" + id + " complete=" + p.isComplete + " type=" + p.patientType
                + " initial_status=" + initialStatus);
        
        // Also save to database
        saveToDatabase(p);
        
        // Disabled backup save - using database instead
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
        
        // Also save to database
        saveToDatabase(p);
        
        // Disabled backup save - using database instead
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
        // CRITICAL: Check if record is locked BEFORE attempting any edits
        Patient p = DataStore.patients.get(id);
        if (p == null) {
            List<String> out = new ArrayList<>();
            out.add("Error: Invalid patient ID");
            return out;
        }
        if (p.isOutpatientPermanent) {
            List<String> out = new ArrayList<>();
            out.add("Error: Patient record is locked. Once marked as Outpatient or Discharged, the record cannot be edited. "
                    + "If this patient returns, please create a new patient record.");
            LogManager.log("patient_edit_extended_rejected patient=" + id
                    + " reason=record_locked permanent_outpatient=" + p.isOutpatientPermanent);
            return out;
        }

        List<String> out = edit(id, name, age, gender, contact, address);
        if (out.isEmpty()) {
            out.add("Error: unknown failure");
            return out;
        }
        p = DataStore.patients.get(id);
        // Update birthday and patient type
        if (birthday != null && !birthday.trim().isEmpty())
            p.birthday = birthday.trim();

        // CRITICAL RULE: Patient Type is LOCKED after creation and CANNOT be changed
        // This applies to ALL patient types: INPATIENT, EMERGENCY, and OUTPATIENT
        // The patientType parameter is completely ignored in edit operations
        if (patientType != null && !patientType.trim().isEmpty()) {
            // Log the attempt but do NOT change the patient type
            LogManager.log("patient_type_change_rejected patient=" + id
                    + " current_type=" + p.patientType
                    + " attempted_type=" + patientType.trim().toUpperCase()
                    + " reason=patient_type_locked_after_creation");

            // Set permanent outpatient flag if applicable
            if ("OUTPATIENT".equals(p.patientType) && !p.isOutpatientPermanent) {
                p.isOutpatientPermanent = true;
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
        // Disabled backup save - using database instead
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
            // Disabled backup save - using database instead
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

        // Persist snapshot + note
        try (Connection conn = DBConnection.getConnection()) {
            ensurePatientClinicalColumns(conn);
            saveToDatabase(p);
            if (note != null && !note.trim().isEmpty()) {
                addProgressNoteToDatabase(conn, id, byStaffId, note);
            }

            String uploadedBy = (byStaffId == null || byStaffId.trim().isEmpty()) ? "SYSTEM" : byStaffId.trim();
            if (xrayPath != null && !xrayPath.trim().isEmpty()) {
                java.io.File f = new java.io.File(xrayPath.trim());
                hpms.service.AttachmentService.uploadAttachment(id, f.getName(), f.getAbsolutePath(),
                        "X-ray", "Imaging", xraySummary == null ? "" : xraySummary, uploadedBy);
            }
            if (stoolPath != null && !stoolPath.trim().isEmpty()) {
                java.io.File f = new java.io.File(stoolPath.trim());
                hpms.service.AttachmentService.uploadAttachment(id, f.getName(), f.getAbsolutePath(),
                        "Lab Results", "Laboratory", stoolSummary == null ? "" : stoolSummary, uploadedBy);
            }
            if (urinePath != null && !urinePath.trim().isEmpty()) {
                java.io.File f = new java.io.File(urinePath.trim());
                hpms.service.AttachmentService.uploadAttachment(id, f.getName(), f.getAbsolutePath(),
                        "Lab Results", "Laboratory", urineSummary == null ? "" : urineSummary, uploadedBy);
            }
            if (bloodPath != null && !bloodPath.trim().isEmpty()) {
                java.io.File f = new java.io.File(bloodPath.trim());
                hpms.service.AttachmentService.uploadAttachment(id, f.getName(), f.getAbsolutePath(),
                        "Lab Results", "Laboratory", bloodSummary == null ? "" : bloodSummary, uploadedBy);
            }
            if (otherAttachments != null && !otherAttachments.isEmpty()) {
                for (String a : otherAttachments) {
                    if (a == null || a.trim().isEmpty())
                        continue;
                    java.io.File f = new java.io.File(a.trim());
                    hpms.service.AttachmentService.uploadAttachment(id, f.getName(), f.getAbsolutePath(),
                            "General Document", "Documentation", "Uploaded via clinical notes", uploadedBy);
                }
            }
        } catch (SQLException ignored) {
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

        // CRITICAL: Once a patient is marked as OUTPATIENT or DISCHARGED, their record
        // is LOCKED
        // No further edits are allowed
        if (p.isOutpatientPermanent) {
            out.add("Error: Patient record is locked. Once marked as Outpatient or Discharged, the record cannot be edited. "
                    + "If this patient returns, please create a new patient record.");
            LogManager.log("patient_edit_rejected patient=" + id + " reason=record_locked permanent_outpatient="
                    + p.isOutpatientPermanent);
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
        // Keep email separately if present in contact string
        p.email = "";
        if (p.contact != null) {
            String[] parts = p.contact.split("\\|");
            for (String part : parts) {
                String candidate = part == null ? null : part.trim();
                if (candidate != null && hpms.util.Validators.isValidEmail(candidate)) {
                    p.email = candidate;
                    break;
                }
            }
        }
        p.address = address.trim();
        p.validateCompleteness(); // Re-validate completeness after edit
        LogManager.log("edit_patient " + id + " complete=" + p.isComplete);
        // Disabled backup save - using database instead
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
        
        // Check if patient is manually set to outpatient before allowing deactivation
        PatientStatus currentStatus = PatientStatusService.getStatus(id);
        if (currentStatus != PatientStatus.OUTPATIENT) {
            out.add("Error: Patient must be manually set to OUTPATIENT status before deactivation");
            out.add("Current status: " + (currentStatus != null ? currentStatus.name() : "UNKNOWN"));
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
        // Disabled backup save - using database instead
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
        // Disabled backup save - using database instead
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

        // Disabled backup save - using database instead

        out.add("Patient successfully removed from doctor assignment. " + appointmentsDeleted
                + " appointment(s) deleted. Insurance information cleared.");
        return out;
    }
    
    /**
     * Save patient to database
     */
    private static void saveToDatabase(Patient patient) {
        try (Connection conn = DBConnection.getConnection()) {
            ensurePatientClinicalColumns(conn);

            // Check if patient already exists in database
            String checkSql = "SELECT id FROM patients WHERE id = ?";
            boolean exists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, patient.id);
                ResultSet rs = checkStmt.executeQuery();
                exists = rs.next();
            }
            
            String sql;
            if (exists) {
                sql = "UPDATE patients SET name=?, age=?, gender=?, contact=?, address=?, email=?, registration_type=?, is_active=?, "
                        + "patient_type=?, allergies=?, medications=?, past_medical_history=?, "
                        + "height_cm=?, weight_kg=?, blood_pressure=?, "
                        + "xray_file_path=?, xray_status=?, xray_summary=?, "
                        + "stool_file_path=?, stool_status=?, stool_summary=?, "
                        + "urine_file_path=?, urine_status=?, urine_summary=?, "
                        + "blood_file_path=?, blood_status=?, blood_summary=? "
                        + "WHERE id=?";
            } else {
                sql = "INSERT INTO patients (id, name, age, gender, contact, address, email, registration_type, is_active, created_at, "
                        + "patient_type, allergies, medications, past_medical_history, "
                        + "height_cm, weight_kg, blood_pressure, "
                        + "xray_file_path, xray_status, xray_summary, "
                        + "stool_file_path, stool_status, stool_summary, "
                        + "urine_file_path, urine_status, urine_summary, "
                        + "blood_file_path, blood_status, blood_summary) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (exists) {
                    int idx = 1;
                    stmt.setString(idx++, patient.name);
                    stmt.setInt(idx++, patient.age);
                    stmt.setString(idx++, patient.gender != null ? patient.gender.toString() : null);
                    stmt.setString(idx++, patient.contact);
                    stmt.setString(idx++, patient.address);
                    stmt.setString(idx++, patient.email);
                    stmt.setString(idx++, patient.registrationType != null ? patient.registrationType : "Walk-in Patient");
                    stmt.setBoolean(idx++, patient.isActive);

                    stmt.setString(idx++, patient.patientType);
                    stmt.setString(idx++, patient.allergies);
                    stmt.setString(idx++, patient.medications);
                    stmt.setString(idx++, patient.pastMedicalHistory);

                    if (patient.heightCm == null)
                        stmt.setNull(idx++, java.sql.Types.DOUBLE);
                    else
                        stmt.setDouble(idx++, patient.heightCm);
                    if (patient.weightKg == null)
                        stmt.setNull(idx++, java.sql.Types.DOUBLE);
                    else
                        stmt.setDouble(idx++, patient.weightKg);
                    stmt.setString(idx++, patient.bloodPressure);

                    stmt.setString(idx++, patient.xrayFilePath);
                    stmt.setString(idx++, patient.xrayStatus);
                    stmt.setString(idx++, patient.xraySummary);
                    stmt.setString(idx++, patient.stoolFilePath);
                    stmt.setString(idx++, patient.stoolStatus);
                    stmt.setString(idx++, patient.stoolSummary);
                    stmt.setString(idx++, patient.urineFilePath);
                    stmt.setString(idx++, patient.urineStatus);
                    stmt.setString(idx++, patient.urineSummary);
                    stmt.setString(idx++, patient.bloodFilePath);
                    stmt.setString(idx++, patient.bloodStatus);
                    stmt.setString(idx++, patient.bloodSummary);

                    stmt.setString(idx++, patient.id);
                } else {
                    int idx = 1;
                    stmt.setString(idx++, patient.id);
                    stmt.setString(idx++, patient.name);
                    stmt.setInt(idx++, patient.age);
                    stmt.setString(idx++, patient.gender != null ? patient.gender.toString() : null);
                    stmt.setString(idx++, patient.contact);
                    stmt.setString(idx++, patient.address);
                    stmt.setString(idx++, patient.email);
                    stmt.setString(idx++, patient.registrationType != null ? patient.registrationType : "Walk-in Patient");
                    stmt.setBoolean(idx++, patient.isActive);
                    stmt.setTimestamp(idx++, java.sql.Timestamp.valueOf(patient.createdAt));

                    stmt.setString(idx++, patient.patientType);
                    stmt.setString(idx++, patient.allergies);
                    stmt.setString(idx++, patient.medications);
                    stmt.setString(idx++, patient.pastMedicalHistory);

                    if (patient.heightCm == null)
                        stmt.setNull(idx++, java.sql.Types.DOUBLE);
                    else
                        stmt.setDouble(idx++, patient.heightCm);
                    if (patient.weightKg == null)
                        stmt.setNull(idx++, java.sql.Types.DOUBLE);
                    else
                        stmt.setDouble(idx++, patient.weightKg);
                    stmt.setString(idx++, patient.bloodPressure);

                    stmt.setString(idx++, patient.xrayFilePath);
                    stmt.setString(idx++, patient.xrayStatus);
                    stmt.setString(idx++, patient.xraySummary);
                    stmt.setString(idx++, patient.stoolFilePath);
                    stmt.setString(idx++, patient.stoolStatus);
                    stmt.setString(idx++, patient.stoolSummary);
                    stmt.setString(idx++, patient.urineFilePath);
                    stmt.setString(idx++, patient.urineStatus);
                    stmt.setString(idx++, patient.urineSummary);
                    stmt.setString(idx++, patient.bloodFilePath);
                    stmt.setString(idx++, patient.bloodStatus);
                    stmt.setString(idx++, patient.bloodSummary);
                }
                
                stmt.executeUpdate();
                LogManager.log("patient_db_" + (exists ? "update" : "insert") + " " + patient.id);
            }
        } catch (SQLException e) {
            System.err.println("Error saving patient to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
                /**
     * Load all patients from database into DataStore
     */
    public static void loadFromDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            ensurePatientClinicalColumns(conn);
            ensurePatientProgressNotesTable(conn);

            // Find the highest patient ID to sync pCounter
            int maxId = 1000; // Start from default
            String maxIdSql = "SELECT id FROM patients WHERE id LIKE 'P%' ORDER BY CAST(SUBSTRING(id, 2) AS UNSIGNED) DESC LIMIT 1";
            try (PreparedStatement maxStmt = conn.prepareStatement(maxIdSql)) {
                ResultSet maxRs = maxStmt.executeQuery();
                if (maxRs.next()) {
                    String highestId = maxRs.getString("id");
                    if (highestId != null && highestId.startsWith("P")) {
                        try {
                            int idNum = Integer.parseInt(highestId.substring(1));
                            maxId = Math.max(maxId, idNum);
                        } catch (NumberFormatException e) {
                            // Ignore if ID format is unexpected
                        }
                    }
                }
            }
            
            // Sync pCounter with highest existing ID
            DataStore.pCounter.set(maxId);
            
            String sql = "SELECT id, name, age, gender, contact, address, registration_type, is_active, created_at, "
                    + "email, patient_type, allergies, medications, past_medical_history, "
                    + "height_cm, weight_kg, blood_pressure, "
                    + "xray_file_path, xray_status, xray_summary, "
                    + "stool_file_path, stool_status, stool_summary, "
                    + "urine_file_path, urine_status, urine_summary, "
                    + "blood_file_path, blood_status, blood_summary "
                    + "FROM patients";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                DataStore.patients.clear(); // Clear existing data
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    String birthday = "2000-01-01"; // Default birthday for database-loaded patients
                    // Fix case mismatch: database stores "MALE" but enum has "Male"
                    String genderStr = rs.getString("gender");
                    Gender gender;
                    try {
                        gender = Gender.valueOf(genderStr);
                    } catch (IllegalArgumentException e) {
                        // Handle case mismatch (e.g., "MALE" -> "Male")
                        gender = switch (genderStr.toUpperCase()) {
                            case "MALE" -> Gender.Male;
                            case "FEMALE" -> Gender.Female;
                            case "LGBTQ_PLUS" -> Gender.LGBTQ_PLUS;
                            case "OTHER" -> Gender.OTHER;
                            default -> Gender.OTHER;
                        };
                    }
                    String contact = rs.getString("contact");
                    String address = rs.getString("address");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    
                    Patient patient = new Patient(id, name, age, birthday, gender, contact, address, createdAt);
                    patient.email = rs.getString("email");
                    if ((patient.email == null || patient.email.trim().isEmpty()) && patient.contact != null) {
                        String[] parts = patient.contact.split("\\|");
                        for (String part : parts) {
                            String candidate = part == null ? null : part.trim();
                            if (candidate != null && hpms.util.Validators.isValidEmail(candidate)) {
                                patient.email = candidate;
                                break;
                            }
                        }
                    }
                    patient.registrationType = rs.getString("registration_type");
                    patient.isActive = rs.getBoolean("is_active");

                    patient.patientType = rs.getString("patient_type");
                    if (patient.patientType == null || patient.patientType.trim().isEmpty())
                        patient.patientType = "OUTPATIENT";

                    patient.allergies = rs.getString("allergies");
                    patient.medications = rs.getString("medications");
                    patient.pastMedicalHistory = rs.getString("past_medical_history");

                    try {
                        double h = rs.getDouble("height_cm");
                        patient.heightCm = rs.wasNull() ? null : h;
                    } catch (Exception ignored) {
                        patient.heightCm = null;
                    }
                    try {
                        double w = rs.getDouble("weight_kg");
                        patient.weightKg = rs.wasNull() ? null : w;
                    } catch (Exception ignored) {
                        patient.weightKg = null;
                    }
                    patient.bloodPressure = rs.getString("blood_pressure");

                    patient.xrayFilePath = rs.getString("xray_file_path");
                    patient.xrayStatus = rs.getString("xray_status");
                    patient.xraySummary = rs.getString("xray_summary");
                    patient.stoolFilePath = rs.getString("stool_file_path");
                    patient.stoolStatus = rs.getString("stool_status");
                    patient.stoolSummary = rs.getString("stool_summary");
                    patient.urineFilePath = rs.getString("urine_file_path");
                    patient.urineStatus = rs.getString("urine_status");
                    patient.urineSummary = rs.getString("urine_summary");
                    patient.bloodFilePath = rs.getString("blood_file_path");
                    patient.bloodStatus = rs.getString("blood_status");
                    patient.bloodSummary = rs.getString("blood_summary");
                    
                    DataStore.patients.put(id, patient);
                    LogManager.log("patient_db_load " + id);
                }
                // Load progress notes after patients exist
                loadProgressNotesFromDatabase(conn);
                System.out.println("Loaded " + DataStore.patients.size() + " patients from database");
            }
        } catch (SQLException e) {
            System.err.println("Error loading patients from database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
