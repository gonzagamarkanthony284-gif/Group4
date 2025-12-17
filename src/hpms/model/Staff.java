package hpms.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data model for a staff member used by the Staff UI module.
 * This lightweight model is intentionally simple and designed for UI
 * interaction.
 */
public class Staff {
    public String id;
    public String name;
    public StaffRole role;
    public String department;
    public String phone;
    public String email;
    public String address;
    public String licenseNumber;
    public String specialty;
    public String subSpecialization;
    public String nursingField;
    public Integer yearsExperience;
    public Integer yearsPractice;
    public Integer yearsOfWork;
    public String clinicSchedule_str; // String version for compatibility
    public Map<String, ScheduleEntry> clinicSchedule;
    public LocalDateTime scheduleStartDate; // Schedule validity start date
    public LocalDateTime scheduleEndDate; // Schedule validity end date (schedule expires after this)
    public String qualifications;
    public String certifications;
    public String education;
    public String expertise;
    public String skills;
    public String competencies;
    public String bio;
    public String employeeId;
    public String status; // Active/On Leave/Resigned
    public String photoPath;
    public boolean isAvailable;
    public boolean isActive = true; // soft-delete flag
    public LocalDateTime createdAt;

    public Staff() {
        this.createdAt = LocalDateTime.now();
        this.clinicSchedule = new HashMap<>();
        this.isAvailable = true;
        this.isActive = true;
    }

    public Staff(String id, String name, StaffRole role, String department, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.department = department;
        this.createdAt = createdAt;
        this.clinicSchedule = new HashMap<>();
        this.isAvailable = true;
        this.isActive = true;
    }

    public static class ScheduleEntry {
        public boolean active;
        public String startTime;
        public String endTime;

        public ScheduleEntry(boolean active, String startTime, String endTime) {
            this.active = active;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    /**
     * Check if the clinic schedule has expired based on the end date
     * 
     * @return true if schedule has expired or end date has passed
     */
    public boolean isScheduleExpired() {
        if (scheduleEndDate == null) {
            return false; // No end date means schedule doesn't expire
        }
        return LocalDateTime.now().isAfter(scheduleEndDate);
    }

    /**
     * Clear expired schedule and reset to default (no active schedule)
     */
    public void clearExpiredSchedule() {
        if (isScheduleExpired()) {
            this.clinicSchedule = new HashMap<>();
            this.clinicSchedule_str = null;
            this.scheduleStartDate = null;
            this.scheduleEndDate = null;
        }
    }

    @Override
    public String toString() {
        return String.format("Staff[id=%s,name=%s,role=%s,dept=%s]", id, name, role, department);
    }
}
