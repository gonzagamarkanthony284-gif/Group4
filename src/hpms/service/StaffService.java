package hpms.service;

import hpms.model.*;
import hpms.util.*;

import java.time.LocalDateTime;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffService {
    private static void ensureStaffPhotoPathColumn(Connection conn) {
        if (conn == null) return;
        try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE staff ADD COLUMN photo_path VARCHAR(255)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Column likely already exists or cannot be added; ignore to avoid breaking startup.
        }
    }

     private static void ensureStaffExtendedProfileColumns(Connection conn) {
         if (conn == null) return;
         ensureStaffPhotoPathColumn(conn);
         try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE staff ADD COLUMN certifications TEXT")) {
             stmt.executeUpdate();
         } catch (SQLException e) {
         }
         try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE staff ADD COLUMN education TEXT")) {
             stmt.executeUpdate();
         } catch (SQLException e) {
         }
         try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE staff ADD COLUMN expertise TEXT")) {
             stmt.executeUpdate();
         } catch (SQLException e) {
         }
         try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE staff ADD COLUMN skills TEXT")) {
             stmt.executeUpdate();
         } catch (SQLException e) {
         }
         try (PreparedStatement stmt = conn.prepareStatement("ALTER TABLE staff ADD COLUMN competencies TEXT")) {
             stmt.executeUpdate();
         } catch (SQLException e) {
         }
     }

    public static List<String> add(String name, String role, String department) {
        return add(name, role, department, null, null, null, null, null, null);
    }

    public static List<String> add(String name, String role, String department, String specialization, String phone,
            String email, String licenseNumber, String qualifications, String notes) {
        List<String> out = new ArrayList<>();
        if (Validators.empty(name) || Validators.empty(role) || Validators.empty(department)) {
            out.add("Error: Missing parameters");
            return out;
        }
        StaffRole r;
        try {
            r = StaffRole.valueOf(role.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            out.add("Error: Invalid role");
            return out;
        }
        if (!DataStore.departments.contains(department.trim())) {
            out.add("Error: Invalid department");
            return out;
        }

        // Basic input checks
        if (name.trim().length() > 120) {
            out.add("Error: Name too long");
            return out;
        }
        if (!Validators.empty(email) && !isValidEmail(email)) {
            out.add("Error: Invalid email address");
            return out;
        }
        if (!Validators.empty(phone) && !isValidPhone(phone)) {
            out.add("Error: Invalid phone number");
            return out;
        }

        // Prevent obvious duplicates by name+role+department
        for (Staff existing : DataStore.staff.values()) {
            if (existing.name != null && existing.name.equalsIgnoreCase(name.trim()) && existing.role == r
                    && existing.department != null && existing.department.equalsIgnoreCase(department.trim())) {
                out.add("Error: Staff already exists with same name, role and department");
                return out;
            }
            // If licenseNumber is provided and matches an existing license -> reject
            if (!Validators.empty(licenseNumber) && licenseNumber.equalsIgnoreCase(existing.licenseNumber)) {
                out.add("Error: A staff member with the same license number already exists");
                return out;
            }
            // If email provided and matches existing -> reject
            if (!Validators.empty(email) && email.equalsIgnoreCase(existing.email)) {
                out.add("Error: A staff member with the same email already exists");
                return out;
            }
        }

        String id = IDGenerator.nextId("S");
        System.err.println("DEBUG: Generated staff ID: " + id + " (counter was at: " + DataStore.sCounter.get() + ")");
        Staff s = new Staff(id, name.trim(), r, department.trim(), LocalDateTime.now());
        if (!Validators.empty(specialization))
            s.specialty = specialization.trim();
        if (!Validators.empty(phone))
            s.phone = phone.trim();
        if (!Validators.empty(email))
            s.email = email.trim();
        if (!Validators.empty(licenseNumber))
            s.licenseNumber = licenseNumber.trim();
        if (!Validators.empty(qualifications))
            s.qualifications = qualifications.trim();
        else
            s.qualifications = ""; // Initialize to empty string
        
        if (!Validators.empty(notes)) {
            // reuse qualifications field as notes fallback if qualifications empty
            s.qualifications = (s.qualifications == null ? "" : s.qualifications)
                    + ((s.qualifications == null || s.qualifications.isEmpty()) ? "" : " | ") + notes.trim();
        }

        DataStore.staff.put(id, s);
        LogManager.log("add_staff " + id);
        
        // Also save to database
        boolean dbSaveSuccess = saveToDatabase(s);
        if (!dbSaveSuccess) {
            System.err.println("Warning: Staff saved to DataStore but failed to save to database");
        }
        
        // Disabled backup save - using database instead
        out.add("Staff added " + id);
        return out;
    }

    private static boolean isValidEmail(String email) {
        if (email == null)
            return false;
        // Simple, permissive pattern — sufficient for basic validation
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static boolean isValidPhone(String phone) {
        if (phone == null)
            return false;
        String p = phone.trim();
        // Accept digits, space, +, -, parentheses and between 7 and 25 characters
        return p.matches("^[0-9+()\\\\\\-\\s]{7,25}$");
    }

    public static List<String> deactivate(String id) {
        List<String> out = new ArrayList<>();
        Staff s = DataStore.staff.get(id);
        if (s == null) {
            out.add("Error: Invalid staff ID");
            return out;
        }
        if (!s.isActive) {
            out.add("Staff already deactivated " + id);
            return out;
        }
        s.isActive = false;
        s.isAvailable = false;
        s.status = "Inactive";

        int affected = 0;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (Appointment a : DataStore.appointments.values()) {
            if (id.equals(a.staffId) && !a.isCompleted && a.dateTime.isAfter(now.minusHours(1))) {
                affected++;
                a.notes = (a.notes == null ? "" : "[Doctor unavailable] ") + "Doctor deactivated; please reschedule";
                try {
                    CommunicationService.addAlert(a.patientId, "Your doctor (" + (s.name == null ? id : s.name)
                            + ") is currently unavailable. Please choose another doctor.");
                } catch (Exception ex) {
                }
            }
        }

        LogManager.log("deactivate_staff " + id + " affected_appts=" + affected);
        // Disabled backup save - using database instead
        out.add("Staff deactivated " + id
                + (affected > 0 ? (" — notified patients for " + affected + " appointment(s)") : ""));
        return out;
    }

    public static List<String> reactivate(String id) {
        List<String> out = new ArrayList<>();
        Staff s = DataStore.staff.get(id);
        if (s == null) {
            out.add("Error: Invalid staff ID");
            return out;
        }
        if (s.isAvailable) {
            out.add("Error: Staff is already active");
            return out;
        }
        s.isAvailable = true;
        // Disabled backup save - using database instead
        out.add("Staff reactivated " + id);
        return out;
    }

    public static List<String> delete(String id) {
        // Legacy delete now performs a soft deactivate to preserve records
        return deactivate(id);
    }

    public static List<String> updateStaff(Staff staff) {
        List<String> out = new ArrayList<>();
        if (staff == null) {
            out.add("Error: Staff object is null");
            return out;
        }
        if (staff.id == null || staff.id.trim().isEmpty()) {
            out.add("Error: Staff ID is required");
            return out;
        }
        
        // Validate input
        if (!Validators.empty(staff.email) && !isValidEmail(staff.email)) {
            out.add("Error: Invalid email address");
            return out;
        }
        if (!Validators.empty(staff.phone) && !isValidPhone(staff.phone)) {
            out.add("Error: Invalid phone number");
            return out;
        }
        
        // Update in DataStore
        DataStore.staff.put(staff.id, staff);

        // Also update in database
        boolean dbUpdated = updateInDatabase(staff);
        if (!dbUpdated) {
            System.err.println("Warning: Staff updated in DataStore but failed to update in database");
        }
        
        // Log the update
        LogManager.log("staff_updated " + staff.id + " name=" + staff.name + " role=" + staff.role);
        
        out.add("Staff updated successfully: " + staff.id);
        return out;
    }

    private static boolean updateInDatabase(Staff staff) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection is null");
                return false;
            }

            ensureStaffExtendedProfileColumns(conn);

            String sql = "UPDATE staff SET name = ?, role = ?, department = ?, phone = ?, email = ?, license_number = ?, specialty = ?, qualifications = ?, certifications = ?, education = ?, expertise = ?, skills = ?, competencies = ?, status = ?, photo_path = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, staff.name);
                stmt.setString(2, staff.role != null ? staff.role.toString() : null);
                stmt.setString(3, staff.department);
                stmt.setString(4, staff.phone);
                stmt.setString(5, staff.email);
                stmt.setString(6, staff.licenseNumber);
                stmt.setString(7, staff.specialty);
                stmt.setString(8, staff.qualifications);
                stmt.setString(9, staff.certifications);
                stmt.setString(10, staff.education);
                stmt.setString(11, staff.expertise);
                stmt.setString(12, staff.skills);
                stmt.setString(13, staff.competencies);
                stmt.setString(14, staff.isActive ? "Active" : "Inactive");
                stmt.setString(15, staff.photoPath);
                stmt.setString(16, staff.id);

                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    // Fallback: record not present in DB yet, insert it
                    return saveToDatabase(staff);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Failed to update staff in database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveToDatabase(Staff staff) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection is null");
                return false;
            }

            ensureStaffExtendedProfileColumns(conn);

            String sql = "INSERT INTO staff (id, name, role, department, phone, email, license_number, specialty, qualifications, certifications, education, expertise, skills, competencies, status, created_at, photo_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, staff.id);
                stmt.setString(2, staff.name);
                stmt.setString(3, staff.role != null ? staff.role.toString() : null);
                stmt.setString(4, staff.department);
                stmt.setString(5, staff.phone);
                stmt.setString(6, staff.email);
                stmt.setString(7, staff.licenseNumber);
                stmt.setString(8, staff.specialty);
                stmt.setString(9, staff.qualifications);
                stmt.setString(10, staff.certifications);
                stmt.setString(11, staff.education);
                stmt.setString(12, staff.expertise);
                stmt.setString(13, staff.skills);
                stmt.setString(14, staff.competencies);
                stmt.setString(15, staff.isActive ? "Active" : "Inactive");
                stmt.setTimestamp(16, java.sql.Timestamp.valueOf(staff.createdAt != null ? staff.createdAt : java.time.LocalDateTime.now()));
                stmt.setString(17, staff.photoPath);
                stmt.executeUpdate();
                LogManager.log("staff_db_save " + staff.id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Failed to save staff to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check and clear expired schedules for all staff members.
     * Should be called periodically (e.g., daily) or when displaying staff lists.
     * 
     * @return Number of schedules that were cleared
     */
    public static int checkAndClearExpiredSchedules() {
        int clearedCount = 0;
        for (Staff staff : DataStore.staff.values()) {
            if (staff.isScheduleExpired()) {
                staff.clearExpiredSchedule();
                clearedCount++;
                LogManager.log("schedule_expired " + staff.id + " - cleared expired schedule");
            }
        }
        // Disabled backup saving - using database instead
        return clearedCount;
    }

    /**
     * Clear expired schedule for a specific staff member
     * 
     * @param staffId Staff ID
     * @return true if schedule was expired and cleared
     */
    public static boolean checkAndClearExpiredSchedule(String staffId) {
        Staff staff = DataStore.staff.get(staffId);
        if (staff != null && staff.isScheduleExpired()) {
            staff.clearExpiredSchedule();
            LogManager.log("schedule_expired " + staffId + " - cleared expired schedule");
            // Disabled backup saving - using database instead
            return true;
        }
        return false;

    }

    /**
     * Load all staff from database into DataStore
     */
    public static void loadFromDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection is null - cannot load staff");
                return;
            }

            ensureStaffExtendedProfileColumns(conn);

            String sql = "SELECT id, name, role, department, phone, email, license_number, specialty, qualifications, certifications, education, expertise, skills, competencies, status, created_at, photo_path FROM staff";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                DataStore.staff.clear();
                int maxStaffId = 2000;

                while (rs.next()) {
                    Staff staff = new Staff();
                    staff.id = rs.getString("id");
                    staff.name = rs.getString("name");
                    try {
                        staff.role = StaffRole.valueOf(rs.getString("role"));
                    } catch (Exception e) {
                        staff.role = null;
                    }
                    staff.department = rs.getString("department");
                    staff.phone = rs.getString("phone");
                    staff.email = rs.getString("email");
                    staff.licenseNumber = rs.getString("license_number");
                    staff.specialty = rs.getString("specialty");
                    staff.qualifications = rs.getString("qualifications");
                    staff.certifications = rs.getString("certifications");
                    staff.education = rs.getString("education");
                    staff.expertise = rs.getString("expertise");
                    staff.skills = rs.getString("skills");
                    staff.competencies = rs.getString("competencies");
                    staff.isActive = "Active".equals(rs.getString("status"));

                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    staff.createdAt = ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now();

                    staff.photoPath = rs.getString("photo_path");

                    DataStore.staff.put(staff.id, staff);

                    if (staff.id != null && staff.id.startsWith("S")) {
                        try {
                            int idNum = Integer.parseInt(staff.id.substring(1));
                            if (idNum > maxStaffId) {
                                maxStaffId = idNum;
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid IDs
                        }
                    }
                }

                DataStore.sCounter.set(maxStaffId);
                System.err.println("DEBUG: Staff loading complete. Max staff ID found: " + maxStaffId + ", counter set to: " + DataStore.sCounter.get());
                LogManager.log("staff_db_load " + DataStore.staff.size() + " staff loaded, counter updated to " + maxStaffId);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load staff from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

 }
