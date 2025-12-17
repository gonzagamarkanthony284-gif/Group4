package hpms.auth;

import hpms.model.UserRole;
import hpms.util.DBConnection;
import hpms.util.DataStore;
import hpms.util.LogManager;
import hpms.util.Validators;

import java.sql.*;
import java.util.*;
import java.security.SecureRandom;

/**
 * Database-backed Authentication Service
 * This version uses MySQL database instead of in-memory DataStore
 */
public class AuthServiceDB {
    public static User current;
    private static final Map<String, String> lastPlain = new HashMap<>();

    /**
     * Seed admin user in database
     */
    public static void seedAdmin() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Failed to connect to database for seeding admin");
                return;
            }

            // Check if admin already exists
            String checkSql = "SELECT username FROM users WHERE username = 'admin'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                    ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Admin user already exists");
                    return;
                }
            }

            // Create admin user
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hash("admin123", salt);

            String insertSql = "INSERT INTO users (username, password, salt, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, "admin");
                stmt.setString(2, hashedPassword);
                stmt.setString(3, salt);
                stmt.setString(4, UserRole.ADMIN.name());
                stmt.executeUpdate();
                System.out.println("Admin user created successfully");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Login user with username and password
     */
    public static List<String> login(String username, String password) {
        List<String> out = new ArrayList<>();

        if (username == null || username.trim().isEmpty()) {
            out.add("Error: Missing username");
            return out;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.add("Error: Database connection failed");
                return out;
            }

            String sql = "SELECT username, password, salt, role, display_password FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    out.add("Error: Invalid credentials");
                    return out;
                }

                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                String roleStr = rs.getString("role");
                String displayPassword = rs.getString("display_password");

                String hashedInput = PasswordUtil.hash(password, salt);

                if (hashedInput == null || !storedPassword.equals(hashedInput)) {
                    out.add("Error: Invalid credentials");
                    return out;
                }

                // Create user object
                UserRole role = UserRole.valueOf(roleStr);
                User user = new User(username, storedPassword, salt, role);
                user.displayPassword = displayPassword;
                current = user;

                LogManager.log("login " + username);
                // Log to audit database
                hpms.util.AuditLogService.logLogin(username, null);
                out.add("Login successful");
            }
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }

        return out;
    }

    /**
     * Logout current user
     */
    public static List<String> logout() {
        List<String> out = new ArrayList<>();

        if (current != null) {
            String username = current.username;
            LogManager.log("logout " + username);
            // Log to audit database
            hpms.util.AuditLogService.logLogout(username, null);
            current = null;
        }

        out.add("Logged out");
        return out;
    }

    /**
     * Register a new user (admin only)
     */
    public static List<String> register(String username, String password, String role) {
        List<String> out = new ArrayList<>();

        if (current == null || current.role != UserRole.ADMIN) {
            out.add("Error: Only admin can register");
            return out;
        }

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                role == null || role.trim().isEmpty()) {
            out.add("Error: Missing parameters");
            return out;
        }

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            out.add("Error: Invalid role");
            return out;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.add("Error: Database connection failed");
                return out;
            }

            // Check if username exists
            String checkSql = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    out.add("Error: Username exists");
                    return out;
                }
            }

            // Insert new user
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hash(password, salt);

            String insertSql = "INSERT INTO users (username, password, salt, role, display_password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, salt);
                stmt.setString(4, userRole.name());
                stmt.setString(5, password);
                stmt.executeUpdate();

                LogManager.log("register " + username + " " + userRole);
                // Log to audit database
                hpms.util.AuditLogService.logCreate(current.username, "USER", username, "Registered new " + userRole + " user");
                out.add("User registered: " + username);
            }
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }

        return out;
    }

    /**
     * Create a patient account without requiring admin
     */
    public static List<String> createPatientAccount(String username, String password) {
        List<String> out = new ArrayList<>();

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            out.add("Error: Missing parameters");
            return out;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.add("Error: Database connection failed");
                return out;
            }

            // Check if username exists
            String checkSql = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    out.add("Error: Username exists");
                    return out;
                }
            }

            // Insert new patient user
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hash(password, salt);

            String insertSql = "INSERT INTO users (username, password, salt, role, display_password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, salt);
                stmt.setString(4, UserRole.PATIENT.name());
                stmt.setString(5, password);
                stmt.executeUpdate();

                lastPlain.put(username, password);
                LogManager.log("register_patient " + username);
                // Log to audit database
                hpms.util.AuditLogService.logCreate("SYSTEM", "USER", username, "Patient account auto-created");
                out.add("Patient account created: " + username);
            }
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }

        return out;
    }

    /**
     * Verify credentials without setting current user
     */
    public static boolean verifyCredentials(String username, String password) {
        if (username == null || password == null)
            return false;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return false;

            String sql = "SELECT password, salt FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next())
                    return false;

                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                String hashedInput = PasswordUtil.hash(password, salt);

                return storedPassword.equals(hashedInput);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Change password (requires old password)
     */
    public static List<String> changePassword(String username, String oldPassword, String newPassword) {
        List<String> out = new ArrayList<>();

        if (Validators.empty(username) || Validators.empty(oldPassword) || Validators.empty(newPassword)) {
            out.add("Error: Missing parameters");
            return out;
        }

        if (newPassword.length() < 6) {
            out.add("Error: New password too short");
            return out;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.add("Error: Database connection failed");
                return out;
            }

            // Verify old password
            String selectSql = "SELECT password, salt FROM users WHERE username = ?";
            String oldSalt;
            String oldHash;

            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    out.add("Error: Unknown user");
                    return out;
                }

                oldHash = rs.getString("password");
                oldSalt = rs.getString("salt");
            }

            String oldPasswordHash = PasswordUtil.hash(oldPassword, oldSalt);
            if (!oldHash.equals(oldPasswordHash)) {
                out.add("Error: Current password incorrect");
                return out;
            }

            // Update with new password
            String newSalt = PasswordUtil.generateSalt();
            String newHash = PasswordUtil.hash(newPassword, newSalt);

            String updateSql = "UPDATE users SET password = ?, salt = ?, display_password = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, newHash);
                stmt.setString(2, newSalt);
                stmt.setString(3, newPassword);
                stmt.setString(4, username);
                stmt.executeUpdate();

                LogManager.log("change_password " + username);
                lastPlain.put(username, newPassword);
                out.add("Password changed");
            }
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }

        return out;
    }

    /**
     * Change password without old password (admin or self-service)
     */
    public static List<String> changePasswordNoOld(String username, String newPassword) {
        List<String> out = new ArrayList<>();

        if (Validators.empty(username) || Validators.empty(newPassword)) {
            out.add("Error: Missing parameters");
            return out;
        }

        if (newPassword.length() < 6) {
            out.add("Error: New password too short");
            return out;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.add("Error: Database connection failed");
                return out;
            }

            // Check if user exists
            String checkSql = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    out.add("Error: Unknown user");
                    return out;
                }
            }

            // Update password
            String newSalt = PasswordUtil.generateSalt();
            String newHash = PasswordUtil.hash(newPassword, newSalt);

            String updateSql = "UPDATE users SET password = ?, salt = ?, display_password = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, newHash);
                stmt.setString(2, newSalt);
                stmt.setString(3, newPassword);
                stmt.setString(4, username);
                stmt.executeUpdate();

                LogManager.log("change_password_no_old " + username);
                lastPlain.put(username, newPassword);
                out.add("Password changed");
            }
        } catch (SQLException e) {
            out.add("Error: Database error - " + e.getMessage());
            e.printStackTrace();
        }

        return out;
    }

    /**
     * Reset password and return new random password
     */
    public static String resetPassword(String username) {
        if (username == null || username.trim().isEmpty())
            return null;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return null;

            // Check if user exists
            String checkSql = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next())
                    return null;
            }

            // Generate new password
            String pwd = generateRandomPassword(8);
            String newSalt = PasswordUtil.generateSalt();
            String newHash = PasswordUtil.hash(pwd, newSalt);

            // Update password
            String updateSql = "UPDATE users SET password = ?, salt = ?, display_password = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, newHash);
                stmt.setString(2, newSalt);
                stmt.setString(3, pwd);
                stmt.setString(4, username);
                stmt.executeUpdate();

                LogManager.log("reset_password " + username);
                lastPlain.put(username, pwd);
                return pwd;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateRandomPasswordForUI() {
        return generate6DigitCode();
    }

    private static String generate6DigitCode() {
        SecureRandom r = new SecureRandom();
        int code = 100000 + r.nextInt(900000);
        return String.valueOf(code);
    }

    private static String generateRandomPassword(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    public static String getLastPlaintextForUI(String username) {
        String lp = lastPlain.get(username);
        if (lp != null && !lp.isEmpty())
            return lp;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return null;

            String sql = "SELECT display_password FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("display_password");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void clearPlaintextForUI(String username) {
        if (username != null)
            lastPlain.remove(username);
    }

    public static void migratePasswordsIfMissing() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return;

            String selectSql = "SELECT username, password, salt, role, display_password FROM users WHERE role != 'ADMIN'";
            List<String> usersToUpdate = new ArrayList<>();
            Map<String, String> newPasswords = new HashMap<>();

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(selectSql)) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String displayPassword = rs.getString("display_password");

                    if (password == null || password.isEmpty()) {
                        String plain = (displayPassword == null || displayPassword.isEmpty())
                                ? generateRandomPasswordForUI()
                                : displayPassword;
                        usersToUpdate.add(username);
                        newPasswords.put(username, plain);
                    }
                }
            }

            // Update users with missing passwords
            if (!usersToUpdate.isEmpty()) {
                String updateSql = "UPDATE users SET password = ?, salt = ?, display_password = ? WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    for (String username : usersToUpdate) {
                        String plain = newPasswords.get(username);
                        String salt = PasswordUtil.generateSalt();
                        String hash = PasswordUtil.hash(plain, salt);

                        stmt.setString(1, hash);
                        stmt.setString(2, salt);
                        stmt.setString(3, plain);
                        stmt.setString(4, username);
                        stmt.executeUpdate();

                        lastPlain.put(username, plain);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
