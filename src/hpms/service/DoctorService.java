package hpms.service;

import hpms.model.Staff;
import hpms.model.StaffRole;
import hpms.util.DataStore;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for doctor-related operations
 */
public class DoctorService {
    
    /**
     * Get all active doctors
     * @return List of active doctors
     */
    public static List<Staff> getAllDoctors() {
        return DataStore.staff.values().stream()
            .filter(staff -> staff.role == StaffRole.DOCTOR && staff.isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * Get doctor by ID
     * @param id Doctor's staff ID
     * @return Doctor staff or null if not found
     */
    public static Staff getDoctorById(String id) {
        if (id == null) return null;
        Staff staff = DataStore.staff.get(id);
        return (staff != null && staff.role == StaffRole.DOCTOR && staff.isActive) ? staff : null;
    }
    
    /**
     * Search doctors by name, specialty, or department
     * @param query Search query
     * @return List of matching doctors
     */
    public static List<Staff> searchDoctors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDoctors();
        }
        
        String lowerQuery = query.toLowerCase().trim();
        return DataStore.staff.values().stream()
            .filter(staff -> staff.role == StaffRole.DOCTOR && 
                            staff.isActive && 
                            (staff.name.toLowerCase().contains(lowerQuery) ||
                             (staff.specialty != null && staff.specialty.toLowerCase().contains(lowerQuery)) ||
                             (staff.department != null && staff.department.toLowerCase().contains(lowerQuery))))
            .collect(Collectors.toList());
    }
    
    /**
     * Update doctor's profile information
     * @param doctor The updated doctor object
     * @return true if update was successful
     */
    public static boolean updateDoctor(Staff doctor) {
        if (doctor == null || doctor.id == null || doctor.role != StaffRole.DOCTOR) {
            return false;
        }
        
        Staff existing = DataStore.staff.get(doctor.id);
        if (existing == null || !existing.isActive) {
            return false;
        }
        
        // Update the doctor's information
        existing.name = doctor.name;
        existing.specialty = doctor.specialty;
        existing.qualifications = doctor.qualifications;
        existing.bio = doctor.bio;
        existing.phone = doctor.phone;
        existing.email = doctor.email;
        
        // Note: Photo updates are handled separately via FileService
        
        // Save changes
        DataStore.staff.put(existing.id, existing);
        return true;
    }
    
    /**
     * Get doctors by department
     * @param department Department name
     * @return List of doctors in the specified department
     */
    public static List<Staff> getDoctorsByDepartment(String department) {
        if (department == null) {
            return getAllDoctors();
        }
        
        return DataStore.staff.values().stream()
            .filter(staff -> staff.role == StaffRole.DOCTOR && 
                            staff.isActive && 
                            department.equalsIgnoreCase(staff.department))
            .collect(Collectors.toList());
    }
    
    /**
     * Get all available departments with doctors
     * @return List of department names
     */
    public static List<String> getAllDepartments() {
        return DataStore.staff.values().stream()
            .filter(staff -> staff.role == StaffRole.DOCTOR && 
                            staff.isActive && 
                            staff.department != null && 
                            !staff.department.trim().isEmpty())
            .map(staff -> staff.department.trim())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}
