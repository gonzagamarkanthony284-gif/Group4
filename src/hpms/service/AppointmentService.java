package hpms.service;

import hpms.model.*;
import hpms.util.*;

import java.time.*;
import java.util.*;
import java.sql.*;

public class AppointmentService {
    public static List<String> schedule(String patientId, String staffId, String date, String time, String department) {
        List<String> out = new ArrayList<>(); if (Validators.empty(patientId) || Validators.empty(staffId) || Validators.empty(date) || Validators.empty(time) || Validators.empty(department)) { out.add("Error: Missing parameters"); return out; }
        Patient p = DataStore.patients.get(patientId); Staff staffObj = DataStore.staff.get(staffId);
        if (p == null) { out.add("Error: Invalid patient ID"); return out; }
        if (staffObj == null) { out.add("Error: Invalid staff ID"); return out; }
        if (!DataStore.departments.contains(department.trim())) { out.add("Error: Invalid department"); return out; }
        LocalDate d; LocalTime t; try { d = LocalDate.parse(date.trim()); } catch (Exception e) { out.add("Error: Incorrect date"); return out; } try { t = LocalTime.parse(time.trim()); } catch (Exception e) { out.add("Error: Incorrect time"); return out; }
        LocalDateTime dt = LocalDateTime.of(d, t); if (dt.isBefore(LocalDateTime.now())) { out.add("Error: Scheduling in the past"); return out; }
        // assume appointments have 1-hour duration; prevent overlapping
        java.time.LocalDateTime proposedStart = dt; java.time.LocalDateTime proposedEnd = dt.plusHours(1);
        for (Appointment a : DataStore.appointments.values()) if (a.staffId.equals(staffId)) {
            java.time.LocalDateTime start = a.dateTime; java.time.LocalDateTime end = a.dateTime.plusHours(1);
            if (!(proposedEnd.isBefore(start) || proposedStart.isAfter(end) || proposedEnd.equals(start) || proposedStart.equals(end))) { out.add("Error: Staff double-booked (overlap)"); return out; }
        }
        String id = IDGenerator.nextId("A"); Appointment a = new Appointment(id, patientId, staffId, dt, department.trim(), LocalDateTime.now()); DataStore.appointments.put(id, a); LogManager.log("schedule_appt " + id);
        
        // Set initial status as pending
        a.notes = "Pending confirmation";
        
        // Also save to database
        saveToDatabase(a);
        
        // Send appointment reminder email to patient
        if (p.contact != null && p.contact.contains("@")) {
            Staff doctor = DataStore.staff.get(staffId);
            String doctorName = doctor != null ? doctor.name : "Doctor";
            hpms.util.EmailService.sendAppointmentReminderEmail(
                p.contact,
                p.name,
                doctorName,
                dt.toLocalDate().toString(),
                dt.toLocalTime().toString(),
                department.trim()
            );
        }
        
        // Disabled backup save - using database instead
        out.add("Appointment created " + id);
        return out;
    }
    public static List<String> cancel(String id) { 
        List<String> out = new ArrayList<>(); 
        Appointment a = DataStore.appointments.remove(id); 
        if (a == null) { out.add("Error: Invalid appointment ID"); return out; } 
        LogManager.log("cancel_appt " + id);
        
        // Also remove from database
        deleteFromDatabase(id);
        
        // Disabled backup save - using database instead 
        out.add("Appointment canceled " + id); 
        return out; 
    }
    public static List<String> reschedule(String id, String date, String time) {
        List<String> out = new ArrayList<>(); Appointment a = DataStore.appointments.get(id); if (a == null) { out.add("Error: Invalid appointment ID"); return out; }
        LocalDate d; LocalTime t; try { d = LocalDate.parse(date.trim()); } catch (Exception e) { out.add("Error: Incorrect date"); return out; } try { t = LocalTime.parse(time.trim()); } catch (Exception e) { out.add("Error: Incorrect time"); return out; }
        LocalDateTime dt = LocalDateTime.of(d, t); if (dt.isBefore(LocalDateTime.now())) { out.add("Error: Scheduling in the past"); return out; }
        // check overlap assuming 1-hour duration
        java.time.LocalDateTime proposedStart = dt; java.time.LocalDateTime proposedEnd = dt.plusHours(1);
        for (Appointment x : DataStore.appointments.values()) if (!x.id.equals(id) && x.staffId.equals(a.staffId)) {
            java.time.LocalDateTime s = x.dateTime; java.time.LocalDateTime e = x.dateTime.plusHours(1);
            if (!(proposedEnd.isBefore(s) || proposedStart.isAfter(e) || proposedEnd.equals(s) || proposedStart.equals(e))) { out.add("Error: Staff double-booked (overlap)"); return out; }
        }
        a.dateTime = dt; LogManager.log("reschedule_appt " + id);
        
        // Also update in database
        updateInDatabase(a);
        
        // Disabled backup save - using database instead
        out.add("Appointment rescheduled " + id);
        return out;
    }

    public static int countForDoctorOn(String doctorId, java.time.LocalDate date) {
        int c = 0; for (Appointment a : DataStore.appointments.values()) if (doctorId.equals(a.staffId) && a.dateTime.toLocalDate().equals(date) && !a.isCompleted) c++; return c;
    }

    public static List<String> rescheduleDoctorDay(String doctorId, java.time.LocalDate day, java.time.LocalDate newDate, java.time.LocalTime newStartTime) {
        List<String> out = new ArrayList<>();
        java.util.List<Appointment> list = new java.util.ArrayList<>();
        for (Appointment a : DataStore.appointments.values()) if (doctorId.equals(a.staffId) && a.dateTime.toLocalDate().equals(day) && !a.isCompleted) list.add(a);
        if (list.isEmpty()) { out.add("No appointments to reschedule"); return out; }
        list.sort(java.util.Comparator.comparing(a -> a.dateTime));
        java.time.LocalDate targetDate = (newDate == null ? day : newDate);
        java.time.LocalTime base = (newStartTime == null ? list.get(0).dateTime.toLocalTime() : newStartTime);
        for (int idx = 0; idx < list.size(); idx++) {
            Appointment a = list.get(idx);
            java.time.LocalDateTime proposed = java.time.LocalDateTime.of(targetDate, base);
            boolean conflict = true; int guard = 0;
            while (conflict && guard < 48) {
                conflict = false;
                java.time.LocalDateTime ps = proposed; java.time.LocalDateTime pe = proposed.plusHours(1);
                for (Appointment x : DataStore.appointments.values()) {
                    if (!x.id.equals(a.id) && doctorId.equals(x.staffId) && x.dateTime.toLocalDate().equals(targetDate)) {
                        java.time.LocalDateTime xs = x.dateTime; java.time.LocalDateTime xe = x.dateTime.plusHours(1);
                        if (!(pe.isBefore(xs) || ps.isAfter(xe) || pe.equals(xs) || ps.equals(xe))) { conflict = true; break; }
                    }
                }
                if (conflict) { proposed = proposed.plusHours(1); }
                guard++;
            }
            a.dateTime = proposed;
            a.notes = (a.notes==null?"":"[Rescheduled] ") + "Rescheduled by doctor";
            base = proposed.toLocalTime().plusHours(1);
        }
        
        // Also update all rescheduled appointments in database
        for (Appointment a : list) {
            updateInDatabase(a);
        }
        
        // Disabled backup save - using database instead
        out.add("Rescheduled " + list.size() + " appointment(s)");
        return out;
    }
    
    /**
     * Save appointment to database
     */
    private static void saveToDatabase(Appointment appointment) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO appointments (id, patient_id, staff_id, date_time, department, created_at, notes, is_completed) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, appointment.id);
                stmt.setString(2, appointment.patientId);
                stmt.setString(3, appointment.staffId);
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(appointment.dateTime));
                stmt.setString(5, appointment.department);
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(appointment.createdAt));
                stmt.setString(7, appointment.notes);
                stmt.setBoolean(8, appointment.isCompleted);
                stmt.executeUpdate();
                LogManager.log("appointment_db_save " + appointment.id);
            }
        } catch (SQLException e) {
            System.err.println("Error saving appointment to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update appointment in database
     */
    public static void updateInDatabase(Appointment appointment) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE appointments SET date_time=?, department=?, notes=?, is_completed=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, java.sql.Timestamp.valueOf(appointment.dateTime));
                stmt.setString(2, appointment.department);
                stmt.setString(3, appointment.notes);
                stmt.setBoolean(4, appointment.isCompleted);
                stmt.setString(5, appointment.id);
                stmt.executeUpdate();
                LogManager.log("appointment_db_update " + appointment.id);
            }
        } catch (SQLException e) {
            System.err.println("Error updating appointment in database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Public method to update appointment confirmation status
     */
    public static void confirmAppointment(Appointment appointment) {
        updateInDatabase(appointment);
    }
    
    /**
     * Confirm appointment - only doctors can confirm
     */
    public static List<String> confirmAppointment(String appointmentId, String staffId) {
        List<String> out = new ArrayList<>();
        
        if (Validators.empty(appointmentId)) {
            out.add("Error: Appointment ID is required");
            return out;
        }
        
        if (Validators.empty(staffId)) {
            out.add("Error: Staff ID is required");
            return out;
        }
        
        Appointment appointment = DataStore.appointments.get(appointmentId);
        if (appointment == null) {
            out.add("Error: Appointment not found");
            return out;
        }
        
        Staff staff = DataStore.staff.get(staffId);
        if (staff == null) {
            out.add("Error: Staff not found");
            return out;
        }
        
        // Only doctors can confirm appointments
        if (staff.role != StaffRole.DOCTOR) {
            out.add("Error: Only doctors can confirm appointments");
            return out;
        }
        
        // Only the assigned doctor can confirm
        if (!appointment.staffId.equals(staffId)) {
            out.add("Error: Only the assigned doctor can confirm this appointment");
            return out;
        }
        
        // Update appointment status
        appointment.notes = "Confirmed by doctor";
        confirmAppointment(appointment);
        LogManager.log("confirm_appt " + appointmentId + " by " + staffId);
        
        out.add("Appointment confirmed successfully");
        return out;
    }
    
    /**
     * Delete appointment from database
     */
    private static void deleteFromDatabase(String appointmentId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM appointments WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, appointmentId);
                stmt.executeUpdate();
                LogManager.log("appointment_db_delete " + appointmentId);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting appointment from database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load all appointments from database into DataStore
     */
    public static void loadFromDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            // Find highest appointment ID to sync aCounter
            int maxId = 3000; // Start from default
            String maxIdSql = "SELECT id FROM appointments WHERE id LIKE 'A%' ORDER BY CAST(SUBSTRING(id, 2) AS UNSIGNED) DESC LIMIT 1";
            try (PreparedStatement maxStmt = conn.prepareStatement(maxIdSql)) {
                ResultSet maxRs = maxStmt.executeQuery();
                if (maxRs.next()) {
                    String highestId = maxRs.getString("id");
                    if (highestId != null && highestId.startsWith("A")) {
                        try {
                            int idNum = Integer.parseInt(highestId.substring(1));
                            maxId = Math.max(maxId, idNum);
                        } catch (NumberFormatException e) {
                            // Ignore if ID format is unexpected
                        }
                    }
                }
            }
            
            // Sync aCounter with highest existing ID
            DataStore.aCounter.set(maxId);
            
            // Load appointments
            String sql = "SELECT id, patient_id, staff_id, date_time, department, created_at, notes, is_completed FROM appointments";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                DataStore.appointments.clear(); // Clear existing data
                while (rs.next()) {
                    String id = rs.getString("id");
                    String patientId = rs.getString("patient_id");
                    String staffId = rs.getString("staff_id");
                    LocalDateTime dateTime = rs.getTimestamp("date_time").toLocalDateTime();
                    String department = rs.getString("department");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    String notes = rs.getString("notes");
                    boolean isCompleted = rs.getBoolean("is_completed");
                    
                    Appointment appointment = new Appointment(id, patientId, staffId, dateTime, department, createdAt);
                    appointment.notes = notes;
                    appointment.isCompleted = isCompleted;
                    
                    DataStore.appointments.put(id, appointment);
                    LogManager.log("appointment_db_load " + id);
                }
                System.out.println("Loaded " + DataStore.appointments.size() + " appointments from database");
            }
        } catch (SQLException e) {
            System.err.println("Error loading appointments from database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
