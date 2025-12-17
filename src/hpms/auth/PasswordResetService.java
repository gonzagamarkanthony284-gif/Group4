package hpms.auth;

import hpms.util.DBConnection;
import hpms.util.EmailService;
import hpms.util.LogManager;
import hpms.util.AuditLogService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Password Reset Service with Email Verification
 * Handles password reset requests with email verification codes
 */
public class PasswordResetService {
    
    /**
     * Request password reset - generates code and sends email
     */
    public static List<String> requestPasswordReset(String username, String email) {
        List<String> out = new ArrayList<>();
        
        if (username == null || username.trim().isEmpty()) {
            out.add("Error: Username required");
            return out;
        }
        
        if (email == null || email.trim().isEmpty()) {
            out.add("Error: Email required");
            return out;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.add("Error: Database connection failed");
                return out;
            }
            
            // Verify user exists and get email from patient/staff record
            String userEmail = getUserEmail(conn, username);
            if (userEmail == null || !userEmail.equalsIgnoreCase(email)) {
                // Don't reveal if user exists for security
                out.add("If an account exists with this email, a reset code has been sent");
                return out;
            }
            
            // Generate 6-digit reset code
            String resetCode = generateResetCode();
            
            // Calculate expiration (1 hour from now)
            Timestamp expiresAt = Timestamp.valueOf(LocalDateTime.now().plus(1, ChronoUnit.HOURS));
            
            // Invalidate any existing reset codes for this user
            String invalidateSql = "UPDATE password_resets SET is_used = TRUE WHERE username = ? AND is_used = FALSE";
            try (PreparedStatement stmt = conn.prepareStatement(invalidateSql)) {
                stmt.setString(1, username);
                stmt.executeUpdate();
            }
            
            // Insert new reset code
            String insertSql = "INSERT INTO password_resets (username, reset_code, email, expires_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, username);
                stmt.setString(2, resetCode);
                stmt.setString(3, email);
                stmt.setTimestamp(4, expiresAt);
                stmt.executeUpdate();
            }
            
            // Send email with reset code
            boolean emailSent = EmailService.sendPasswordResetCodeEmail(email, username, resetCode);
            if (emailSent) {
                LogManager.log("password_reset_requested username=" + username);
                AuditLogService.logAction(username, "PASSWORD_RESET_REQUEST", "USER", username, "Password reset requested");
                out.add("Password reset code sent to your email");
            } else {
                out.add("Error: Failed to send email. Please contact administrator");
            }
            
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }
        
        return out;
    }
    
    /**
     * Verify reset code and reset password
     */
    public static List<String> resetPasswordWithCode(String username, String resetCode, String newPassword) {
        List<String> out = new ArrayList<>();
        
        if (username == null || username.trim().isEmpty() || 
            resetCode == null || resetCode.trim().isEmpty() ||
            newPassword == null || newPassword.length() < 6) {
            out.add("Error: Invalid parameters. Password must be at least 6 characters");
            return out;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.add("Error: Database connection failed");
                return out;
            }
            
            // Verify reset code
            String verifySql = "SELECT id, expires_at FROM password_resets " +
                              "WHERE username = ? AND reset_code = ? AND is_used = FALSE " +
                              "ORDER BY created_at DESC LIMIT 1";
            Integer resetId = null;
            Timestamp expiresAt = null;
            
            try (PreparedStatement stmt = conn.prepareStatement(verifySql)) {
                stmt.setString(1, username);
                stmt.setString(2, resetCode);
                ResultSet rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    out.add("Error: Invalid or expired reset code");
                    return out;
                }
                
                resetId = rs.getInt("id");
                expiresAt = rs.getTimestamp("expires_at");
            }
            
            // Check if code expired
            if (expiresAt.before(new Timestamp(System.currentTimeMillis()))) {
                out.add("Error: Reset code has expired. Please request a new one");
                return out;
            }
            
            // Update password
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hash(newPassword, salt);
            
            String updateSql = "UPDATE users SET password = ?, salt = ?, display_password = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, hashedPassword);
                stmt.setString(2, salt);
                stmt.setString(3, newPassword);
                stmt.setString(4, username);
                stmt.executeUpdate();
            }
            
            // Mark reset code as used
            String markUsedSql = "UPDATE password_resets SET is_used = TRUE WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(markUsedSql)) {
                stmt.setInt(1, resetId);
                stmt.executeUpdate();
            }
            
            LogManager.log("password_reset_completed username=" + username);
            AuditLogService.logAction(username, "PASSWORD_RESET_COMPLETE", "USER", username, "Password reset completed via email verification");
            out.add("Password reset successful. You can now login with your new password");
            
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }
        
        return out;
    }
    
    /**
     * Get user email from patient or staff record
     */
    private static String getUserEmail(Connection conn, String username) throws SQLException {
        // Try to get from patients table (patient ID matches username)
        String patientSql = "SELECT contact FROM patients WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(patientSql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String contact = rs.getString("contact");
                // Check if contact is an email
                if (contact != null && contact.contains("@")) {
                    return contact;
                }
            }
        }
        
        // Try to get from staff table
        String staffSql = "SELECT email FROM staff WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(staffSql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        }
        
        return null;
    }
    
    /**
     * Generate 6-digit reset code
     */
    private static String generateResetCode() {
        SecureRandom r = new SecureRandom();
        int code = 100000 + r.nextInt(900000);
        return String.valueOf(code);
    }
}

