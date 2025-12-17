package hpms.auth;

import hpms.model.UserRole;
import hpms.util.DataStore;
import hpms.util.LogManager;
import hpms.util.Validators;
import hpms.util.DBConnection;
import java.util.*;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    public static User current;
    // transient map to store last generated or reset plaintext passwords for UI display (not persisted)
    private static final Map<String,String> lastPlain = new HashMap<>();
    public static void seedAdmin() { 
        String salt = PasswordUtil.generateSalt(); 
        String hashedPassword = PasswordUtil.hash("admin123", salt);
        
        // Save to DataStore for backward compatibility
        DataStore.users.put("admin", new User("admin", hashedPassword, salt, UserRole.ADMIN));
        
        // Also save to database (with graceful fallback)
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String checkSql = "SELECT username FROM users WHERE username = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, "admin");
                    ResultSet rs = checkStmt.executeQuery();
                    if (!rs.next()) {
                        // Admin doesn't exist in database, create it
                        String insertSql = "INSERT INTO users (username, password, salt, role, display_password) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                            stmt.setString(1, "admin");
                            stmt.setString(2, hashedPassword);
                            stmt.setString(3, salt);
                            stmt.setString(4, UserRole.ADMIN.name());
                            stmt.setString(5, "admin123");
                            stmt.executeUpdate();
                            System.out.println("Admin user created in database");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Database connection failed, using DataStore fallback: " + e.getMessage());
            // Continue with DataStore approach - no need to fail completely
        }
    }
    public static List<String> login(String username, String password) {
        List<String> out = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) { out.add("Error: Missing username"); return out; }
        
        // Use database authentication
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT username, password, salt, role, status FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    String dbSalt = rs.getString("salt");
                    String dbRole = rs.getString("role");
                    String dbStatus = rs.getString("status");
                    
                    // Check if account is deactivated
                    if ("DEACTIVATED".equals(dbStatus)) {
                        out.add("Error: Account is deactivated. Please contact administrator.");
                        return out;
                    }
                    
                    String h = PasswordUtil.hash(password, dbSalt);
                    if (h != null && h.equals(dbPassword)) {
                        // Create user object for current session
                        UserRole role = UserRole.valueOf(dbRole);
                        current = new User(username, dbPassword, dbSalt, role);
                        LogManager.log("login " + username);
                        out.add("Login successful");
                        return out;
                    }
                }
            }
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
            return out;
        }
        
        out.add("Error: Invalid credentials");
        return out;
    }
    public static List<String> logout() { List<String> out = new ArrayList<>();
        // Disabled backup save - using database instead
        if (current != null) { LogManager.log("logout " + current.username); current = null; }
        out.add("Logged out");
        return out;
    }
    
    public static List<String> resetPassword(String username, String email) {
        List<String> out = new ArrayList<>();
        
        if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            out.add("Error: Username and email are required");
            return out;
        }
        
        // Validate email format
        if (!Validators.isValidEmail(email)) {
            out.add("Error: Invalid email format");
            return out;
        }
        
        // Check if user exists and email matches
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.add("Error: Database connection failed");
                return out;
            }
            
            String sql = "SELECT username, role FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    out.add("Error: Username not found");
                    return out;
                }
                
                String userRole = rs.getString("role");
                
                // For staff accounts, check email in staff table
                if (!userRole.equals("PATIENT")) {
                    String staffSql = "SELECT email FROM staff WHERE id = ?";
                    try (PreparedStatement staffStmt = conn.prepareStatement(staffSql)) {
                        staffStmt.setString(1, username);
                        ResultSet staffRs = staffStmt.executeQuery();
                        
                        if (!staffRs.next() || !email.equalsIgnoreCase(staffRs.getString("email"))) {
                            out.add("Error: Email does not match our records");
                            return out;
                        }
                    }
                } else {
                    // For patient accounts, check email in patient table
                    String patientSql = "SELECT email FROM patients WHERE id = ?";
                    try (PreparedStatement patientStmt = conn.prepareStatement(patientSql)) {
                        patientStmt.setString(1, username);
                        ResultSet patientRs = patientStmt.executeQuery();
                        
                        if (!patientRs.next() || !email.equalsIgnoreCase(patientRs.getString("email"))) {
                            out.add("Error: Email does not match our records");
                            return out;
                        }
                    }
                }
            }
            
            // Generate new password
            String newPassword = generateRandomPassword();
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hash(newPassword, salt);
            
            // Update password in database
            String updateSql = "UPDATE users SET password = ?, salt = ? WHERE username = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, salt);
                updateStmt.setString(3, username);
                updateStmt.executeUpdate();
            }
            
            // Send email with new password
            boolean emailSent = hpms.util.EmailService.sendPasswordResetEmail(email, username, newPassword);
            
            if (emailSent) {
                LogManager.log("password_reset username=" + username + " email=" + email);
                out.add("New password has been sent to your email address");
            } else {
                out.add("Password reset but email delivery failed. Please contact administrator");
            }
            
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }
        
        return out;
    }
    
    private static String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    public static List<String> register(String username, String password, String role) {
        List<String> out = new ArrayList<>();
        System.out.println("=== AuthService.register called ===");
        System.out.println("Username: " + username);
        System.out.println("Role: " + role);
        System.out.println("Current user: " + (current != null ? current.username + " (" + current.role + ")" : "null"));
        
        if (current == null || current.role != UserRole.ADMIN) { 
            System.out.println("ERROR: Only admin can register");
            out.add("Error: Only admin can register"); 
            return out; 
        }
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() || role == null || role.trim().isEmpty()) { 
            System.out.println("ERROR: Missing parameters");
            out.add("Error: Missing parameters"); 
            return out; 
        }
        
        // Use database registration
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Database connection obtained: " + (conn != null ? "SUCCESS" : "FAILED"));
            
            if (conn == null) {
                out.add("Error: Could not connect to database");
                return out;
            }
            
            // Check if username exists
            String checkSql = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("ERROR: Username already exists");
                    out.add("Error: Username exists");
                    return out;
                }
            }

            // Insert new user
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hash(password, salt);
            System.out.println("Generated salt and hashed password");

            String insertSql = "INSERT INTO users (username, password, salt, role, display_password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, salt);
                stmt.setString(4, role);
                stmt.setString(5, password);
                
                int rowsAffected = stmt.executeUpdate();
                System.out.println("SQL executed, rows affected: " + rowsAffected);

                LogManager.log("register " + username + " " + role);
                out.add("User registered: " + username);
                System.out.println("SUCCESS: User registered successfully");
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Database exception - " + e.getMessage());
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }
        
        return out;
    }

    // Create a patient account without requiring admin; used for patient portal accounts
    public static List<String> createPatientAccount(String username, String password) {
        List<String> out = new ArrayList<>();
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) { out.add("Error: Missing parameters"); return out; }
        String salt = PasswordUtil.generateSalt();
        String hashed = PasswordUtil.hash(password, salt);

        // Update in-memory store for immediate login/UI usage
        User nu = new hpms.auth.User(username, hashed, salt, hpms.model.UserRole.PATIENT);
        nu.displayPassword = password;
        DataStore.users.put(username, nu);
        lastPlain.put(username, password);

        boolean created = false;

        // Save to database (insert or update existing account)
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, salt, role, display_password) VALUES (?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE password=VALUES(password), salt=VALUES(salt), role=VALUES(role), display_password=VALUES(display_password)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, hashed);
                stmt.setString(3, salt);
                stmt.setString(4, hpms.model.UserRole.PATIENT.name());
                stmt.setString(5, password);
                int rows = stmt.executeUpdate();
                created = (rows == 1);
                LogManager.log("patient_account_db_save " + username + " rows=" + rows);
            }
        } catch (SQLException e) {
            System.err.println("Error saving patient account to database: " + e.getMessage());
            e.printStackTrace();
        }

        LogManager.log("register_patient " + username);
        // Disabled backup save - using database instead
        out.add((created ? "Patient account created: " : "Patient account updated: ") + username);
        return out;
    }

    // Verify credentials without setting current user
    public static boolean verifyCredentials(String username, String password) {
        if (username == null || password == null) return false;
        User u = DataStore.users.get(username);
        if (u == null) return false;
        String h = PasswordUtil.hash(password, u.salt);
        return Objects.equals(u.password, h);
    }
    public static List<String> changePassword(String username, String oldPassword, String newPassword) {
        List<String> out = new ArrayList<>();
        if (Validators.empty(username) || Validators.empty(oldPassword) || Validators.empty(newPassword)) { out.add("Error: Missing parameters"); return out; }
        User u = DataStore.users.get(username);
        if (u == null) { out.add("Error: Unknown user"); return out; }
        String oldHash = PasswordUtil.hash(oldPassword, u.salt);
        if (!Objects.equals(oldHash, u.password)) { out.add("Error: Current password incorrect"); return out; }
        if (newPassword.length() < 6) { out.add("Error: New password too short"); return out; }
        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hash(newPassword, newSalt);
        u.salt = newSalt; u.password = newHash; u.displayPassword = newPassword;
        LogManager.log("change_password " + username);
        // Keep plaintext for UI display flows (transient only)
        lastPlain.put(username, newPassword);
        // Disabled backup save - using database instead
        clearPlaintextForUI(username);
        out.add("Password changed");
        return out;
    }

    // Change a user's password without requiring the old password (used by admin or patient UI flows)
    public static List<String> changePasswordNoOld(String username, String newPassword) {
        List<String> out = new ArrayList<>();
        if (Validators.empty(username) || Validators.empty(newPassword)) { out.add("Error: Missing parameters"); return out; }
        User u = DataStore.users.get(username);
        if (u == null) { out.add("Error: Unknown user"); return out; }
        if (newPassword.length() < 6) { out.add("Error: New password too short"); return out; }
        String newSalt = PasswordUtil.generateSalt(); String newHash = PasswordUtil.hash(newPassword, newSalt); u.salt = newSalt; u.password = newHash; u.displayPassword = newPassword; LogManager.log("change_password_no_old " + username); out.add("Password changed");
        lastPlain.put(username, newPassword);
        // Disabled backup save - using database instead
        clearPlaintextForUI(username);
        return out;
    }

    public static String resetPassword(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        User u = DataStore.users.get(username);
        if (u == null) return null;
        String pwd = generateRandomPassword(8);
        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hash(pwd, newSalt);
        u.salt = newSalt; u.password = newHash; u.displayPassword = pwd;
        LogManager.log("reset_password " + username);
        // keep the reset plaintext available briefly for UI display
        lastPlain.put(username, pwd);
        // Disabled backup save - using database instead
        clearPlaintextForUI(username);
        return pwd;
    }

    // Generate a unique 6-digit code for easy entry and memorization
    public static String generateRandomPasswordForUI() {
        return generate6DigitCode();
    }

    private static String generate6DigitCode() {
        SecureRandom r = new SecureRandom();
        int code = 100000 + r.nextInt(900000); // Ensures 6 digits
        return String.valueOf(code);
    }

    private static String generateRandomPassword(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom r = new SecureRandom(); StringBuilder sb = new StringBuilder(len);
        for (int i=0;i<len;i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    // Helper to return last-known plaintext for the username (may be null)
    public static String getLastPlaintextForUI(String username) {
        String lp = lastPlain.get(username);
        if (lp != null && !lp.isEmpty()) return lp;
        hpms.auth.User u = hpms.util.DataStore.users.get(username);
        return u == null ? null : (u.displayPassword==null || u.displayPassword.isEmpty() ? null : u.displayPassword);
    }

    // Helper to clear the stored plaintext for a user (optional privacy step)
    public static void clearPlaintextForUI(String username) { if (username != null) lastPlain.remove(username); }

    public static void migratePasswordsIfMissing() {
        boolean changed = false;
        for (hpms.auth.User u : hpms.util.DataStore.users.values()) {
            if (u == null) continue;
            if (u.role == hpms.model.UserRole.ADMIN) continue;
            if (u.password == null || u.password.isEmpty()) {
                String plain = (u.displayPassword == null || u.displayPassword.isEmpty()) ? generateRandomPasswordForUI() : u.displayPassword;
                String salt = PasswordUtil.generateSalt();
                String hash = PasswordUtil.hash(plain, salt);
                u.salt = salt;
                u.password = hash;
                if (u.displayPassword == null || u.displayPassword.isEmpty()) u.displayPassword = plain;
                lastPlain.put(u.username, plain);
                changed = true;
            }
        }
        if (changed) { 
            // Disabled backup save - using database instead 
        }
    }
}
