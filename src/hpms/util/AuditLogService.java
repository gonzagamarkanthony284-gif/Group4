package hpms.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit Log Service - Logs all system activities to database
 * Provides admin-only access to audit logs
 */
public class AuditLogService {
    
    /**
     * Log an action to the audit_logs table
     */
    public static void logAction(String username, String action, String entityType, String entityId, String details) {
        logAction(username, action, entityType, entityId, details, null);
    }

    /**
     * Log an action to the audit_logs table with IP address
     */
    public static void logAction(String username, String action, String entityType, String entityId, String details, String ipAddress) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                // Fallback to in-memory logging if DB unavailable
                LogManager.log("audit_log_failed action=" + action + " reason=db_unavailable");
                return;
            }

            String sql = "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, ip_address) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, action);
                stmt.setString(3, entityType);
                stmt.setString(4, entityId);
                stmt.setString(5, details);
                stmt.setString(6, ipAddress);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("AuditLogService: Failed to log action: " + e.getMessage());
            // Fallback to in-memory logging
            LogManager.log("audit_log_failed action=" + action + " error=" + e.getMessage());
        }
    }

    /**
     * Log login action
     */
    public static void logLogin(String username, String ipAddress) {
        logAction(username, "LOGIN", "USER", username, "User logged in", ipAddress);
    }

    /**
     * Log logout action
     */
    public static void logLogout(String username, String ipAddress) {
        logAction(username, "LOGOUT", "USER", username, "User logged out", ipAddress);
    }

    /**
     * Log create action
     */
    public static void logCreate(String username, String entityType, String entityId, String details) {
        logAction(username, "CREATE", entityType, entityId, details, null);
    }

    /**
     * Log update action
     */
    public static void logUpdate(String username, String entityType, String entityId, String details) {
        logAction(username, "UPDATE", entityType, entityId, details, null);
    }

    /**
     * Log deactivate action (soft delete)
     */
    public static void logDeactivate(String username, String entityType, String entityId, String details) {
        logAction(username, "DEACTIVATE", entityType, entityId, details, null);
    }

    /**
     * Get audit logs (admin only)
     */
    public static List<AuditLogEntry> getAuditLogs(int limit, int offset) {
        List<AuditLogEntry> logs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return logs;
            }

            String sql = "SELECT username, action, entity_type, entity_id, details, ip_address, logged_at " +
                        "FROM audit_logs ORDER BY logged_at DESC LIMIT ? OFFSET ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                stmt.setInt(2, offset);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    AuditLogEntry entry = new AuditLogEntry(
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("entity_type"),
                        rs.getString("entity_id"),
                        rs.getString("details"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("logged_at")
                    );
                    logs.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("AuditLogService: Failed to get audit logs: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Get audit logs by username
     */
    public static List<AuditLogEntry> getAuditLogsByUser(String username, int limit) {
        List<AuditLogEntry> logs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return logs;
            }

            String sql = "SELECT username, action, entity_type, entity_id, details, ip_address, logged_at " +
                        "FROM audit_logs WHERE username = ? ORDER BY logged_at DESC LIMIT ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    AuditLogEntry entry = new AuditLogEntry(
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("entity_type"),
                        rs.getString("entity_id"),
                        rs.getString("details"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("logged_at")
                    );
                    logs.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("AuditLogService: Failed to get audit logs by user: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Audit log entry model
     */
    public static class AuditLogEntry {
        public final String username;
        public final String action;
        public final String entityType;
        public final String entityId;
        public final String details;
        public final String ipAddress;
        public final Timestamp loggedAt;

        public AuditLogEntry(String username, String action, String entityType, String entityId, 
                           String details, String ipAddress, Timestamp loggedAt) {
            this.username = username;
            this.action = action;
            this.entityType = entityType;
            this.entityId = entityId;
            this.details = details;
            this.ipAddress = ipAddress;
            this.loggedAt = loggedAt;
        }
    }
}

