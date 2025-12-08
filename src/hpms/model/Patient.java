package hpms.model;

import java.time.LocalDateTime;

public class Patient {
    public final String id;
    public String name;
    public int age;
    public String birthday; // Format: YYYY-MM-DD
    public Gender gender;
    public String contact;
    public String address;
    public String patientType = ""; // INPATIENT or OUTPATIENT - required field
    public boolean isOutpatientPermanent = false; // Once marked as OUTPATIENT, this flag is set and NEVER changed
    public boolean isComplete = false; // Flag to track if all required fields are filled
    // Patient-provided / administrative fields
    public String allergies = ""; // free text or comma-separated
    public String medications = ""; // free text
    public String pastMedicalHistory = "";
    public String surgicalHistory = "";
    public String familyHistory = "";

    // Lifestyle
    public String smokingStatus = ""; // Current/Former/Never
    public String alcoholUse = "";
    public String drugUse = "";
    public String occupation = "";

    // Profile photo (optional path to image). If null/empty, UI shows a
    // placeholder.
    public String photoPath = null;

    // uploaded files (paths) for records
    public java.util.List<String> attachmentPaths = new java.util.ArrayList<>();

    // Medical file attachments (managed by AttachmentService)
    public java.util.List<FileAttachment> fileAttachments = new java.util.ArrayList<>();

    // Clinical / hospital-entered
    public Double heightCm = null;
    public Double weightKg = null;
    public String bloodPressure = "";
    // Registration / arrival metadata
    public String registrationType = ""; // Mode of arrival / type of registration
    public String incidentTime = ""; // if emergency / accident - time of incident (HH:mm or ISO)
    public String broughtBy = ""; // e.g., Ambulance;Family;Bystander;Police
    public String initialBp = ""; // e.g., "120/80"
    public String initialHr = ""; // heart rate
    public String initialSpo2 = ""; // SpO2 percent
    public String chiefComplaint = ""; // initial complaint text
    public java.util.List<String> progressNotes = new java.util.ArrayList<>();
    public java.util.List<String> labResults = new java.util.ArrayList<>();
    public java.util.List<String> radiologyReports = new java.util.ArrayList<>();
    // Clinical test uploads and summaries
    public String xrayFilePath = null; // path to uploaded x-ray image
    public String xrayStatus = null; // e.g., Not Uploaded, Uploaded, Reviewed, Critical
    public String xraySummary = null; // short summary / findings

    public String stoolFilePath = null;
    public String stoolStatus = null;
    public String stoolSummary = null;

    public String urineFilePath = null;
    public String urineStatus = null;
    public String urineSummary = null;

    public String bloodFilePath = null;
    public String bloodStatus = null;
    public String bloodSummary = null;
    public java.util.List<String> diagnoses = new java.util.ArrayList<>();
    public java.util.List<String> treatmentPlans = new java.util.ArrayList<>();
    public java.util.List<String> dischargeSummaries = new java.util.ArrayList<>();

    // Insurance / billing
    public String insuranceProvider = "";
    public String insuranceId = "";
    public String insuranceGroup = "";
    public String policyHolderName = "";
    public String policyHolderDob = "";
    public String policyRelationship = "";
    public String secondaryInsurance = "";
    public boolean isActive = true; // Deactivation flag - false means inactive
    public final LocalDateTime createdAt;

    public Patient(String id, String name, int age, String birthday, Gender gender, String contact, String address,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.birthday = birthday;
        this.gender = gender;
        this.contact = contact;
        this.address = address;
        this.createdAt = createdAt;
        this.isActive = true; // New patients are active by default
        this.isComplete = false; // Will be set to true after validation
        // Note: patient portal passwords are managed by AuthService and stored securely
        // in DataStore.users
    }

    /**
     * Compute BMI from stored height (cm) and weight (kg).
     * Returns null if height or weight isn't set.
     * BMI is rounded to two decimal places.
     */
    public Double getBmi() {
        if (heightCm == null || weightKg == null)
            return null;
        if (heightCm == 0)
            return null;
        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);
        // round to two decimals
        return Math.round(bmi * 100.0) / 100.0;
    }

    /**
     * Validates that all required fields are filled and updates isComplete flag.
     * Required fields: name, age > 0, birthday, gender, contact, address,
     * patientType
     */
    public void validateCompleteness() {
        this.isComplete = name != null && !name.trim().isEmpty()
                && age > 0
                && birthday != null && !birthday.trim().isEmpty()
                && gender != null
                && contact != null && !contact.trim().isEmpty()
                && address != null && !address.trim().isEmpty()
                && patientType != null && !patientType.trim().isEmpty();
    }
}
