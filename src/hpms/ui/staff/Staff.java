package hpms.ui.staff;

import java.time.LocalDateTime;

/**
 * Data model for a staff member used by the Staff UI module.
 * This lightweight model is intentionally simple and designed for UI interaction.
 */
public class Staff {
    public String id;
    public String name;
    public String role; // DOCTOR, NURSE, CASHIER, ADMIN, FRONT_DESK
    public String department;
    public String phone;
    public String email;
    public String licenseNumber;
    public String specialization;
    public String subSpecialization;
    public String nursingField;
    public Integer yearsExperience;
    public Integer yearsPractice;
    public String clinicSchedule;
    public String bio;
    public String certifications;
    public String employeeId;
    public String status; // Active/On Leave/Resigned
    public String photoPath;
    public LocalDateTime createdAt;

    public Staff() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Staff[id=%s,name=%s,role=%s,dept=%s]", id, name, role, department);
    }
}
