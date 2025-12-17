package hpms.service;

import hpms.model.FileAttachment;
import hpms.util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class AttachmentService {

    private static void ensurePatientFileAttachmentsTable(Connection conn) {
        if (conn == null)
            return;
        try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS patient_file_attachments (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "patient_id VARCHAR(20) NOT NULL," +
                        "file_name VARCHAR(255) NOT NULL," +
                        "file_path TEXT NOT NULL," +
                        "file_type VARCHAR(100)," +
                        "category VARCHAR(50)," +
                        "file_size BIGINT," +
                        "mime_type VARCHAR(100)," +
                        "description TEXT," +
                        "uploaded_by VARCHAR(50)," +
                        "uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "file_hash VARCHAR(128)," +
                        "is_encrypted BOOLEAN DEFAULT FALSE," +
                        "status VARCHAR(20) DEFAULT 'Active'," +
                        "INDEX idx_pfa_patient (patient_id)," +
                        "INDEX idx_pfa_status (status)" +
                        ")")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
    }

    /**
     * Upload and store a file attachment for a patient
     */
    public static List<String> uploadAttachment(String patientId, String fileName, String filePath,
            String fileType, String category, String description,
            String uploadedBy) {
        List<String> result = new ArrayList<>();

        if (patientId == null || patientId.isEmpty()) {
            result.add("Error: Patient ID is required");
            return result;
        }

        if (fileName == null || fileName.isEmpty()) {
            result.add("Error: File name is required");
            return result;
        }

        if (filePath == null || filePath.isEmpty()) {
            result.add("Error: File path is required");
            return result;
        }

        // Create attachment object
        FileAttachment attachment = new FileAttachment(patientId, fileName, filePath, fileType,
                category, description, uploadedBy);

        // Store in database
        String sql = "INSERT INTO patient_file_attachments (patient_id, file_name, file_path, " +
                "file_type, category, file_size, mime_type, description, uploaded_by, " +
                "uploaded_at, last_modified, file_hash, is_encrypted, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            ensurePatientFileAttachmentsTable(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, patientId);
                stmt.setString(2, fileName);
                stmt.setString(3, filePath);
                stmt.setString(4, fileType != null ? fileType : "Unknown");
                stmt.setString(5, category != null ? category : "General");
                stmt.setLong(6, attachment.fileSize);
                stmt.setString(7, attachment.mimeType);
                stmt.setString(8, description != null ? description : "");
                stmt.setString(9, uploadedBy != null ? uploadedBy : "System");
                stmt.setTimestamp(10, Timestamp.valueOf(attachment.uploadedAt));
                stmt.setTimestamp(11, Timestamp.valueOf(attachment.lastModified));
                stmt.setString(12, attachment.fileHash);
                stmt.setBoolean(13, attachment.isEncrypted);
                stmt.setString(14, attachment.status);

                stmt.executeUpdate();
                result.add("File attachment uploaded successfully");
            }

        } catch (SQLException e) {
            result.add("Error uploading attachment: " + e.getMessage());
        }

        return result;
    }

    /**
     * Get all attachments for a patient
     */
    public static List<FileAttachment> getAttachmentsByPatient(String patientId) {
        List<FileAttachment> attachments = new ArrayList<>();

        String sql = "SELECT id, patient_id, file_name, file_path, file_type, category, " +
                "file_size, mime_type, description, uploaded_by, uploaded_at, " +
                "last_modified, file_hash, is_encrypted, status " +
                "FROM patient_file_attachments " +
                "WHERE patient_id = ? AND status = 'Active' " +
                "ORDER BY uploaded_at DESC";

        try (Connection conn = DBConnection.getConnection()) {
            ensurePatientFileAttachmentsTable(conn);
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FileAttachment attachment = new FileAttachment(
                            rs.getInt("id"),
                            rs.getString("patient_id"),
                            rs.getString("file_name"),
                            rs.getString("file_path"),
                            rs.getString("file_type"),
                            rs.getString("category"),
                            rs.getLong("file_size"),
                            rs.getString("mime_type"),
                            rs.getString("description"),
                            rs.getString("uploaded_by"),
                            rs.getTimestamp("uploaded_at").toLocalDateTime(),
                            rs.getTimestamp("last_modified").toLocalDateTime(),
                            rs.getString("file_hash"),
                            rs.getBoolean("is_encrypted"),
                            rs.getString("status"));
                    attachments.add(attachment);
                }
            }
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving attachments: " + e.getMessage());
        }

        return attachments;
    }

    /**
     * Get attachments filtered by category for a patient
     */
    public static List<FileAttachment> getAttachmentsByCategory(String patientId, String category) {
        List<FileAttachment> attachments = new ArrayList<>();

        String sql = "SELECT id, patient_id, file_name, file_path, file_type, category, " +
                "file_size, mime_type, description, uploaded_by, uploaded_at, " +
                "last_modified, file_hash, is_encrypted, status " +
                "FROM patient_file_attachments " +
                "WHERE patient_id = ? AND category = ? AND status = 'Active' " +
                "ORDER BY uploaded_at DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            stmt.setString(2, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FileAttachment attachment = new FileAttachment(
                            rs.getInt("id"),
                            rs.getString("patient_id"),
                            rs.getString("file_name"),
                            rs.getString("file_path"),
                            rs.getString("file_type"),
                            rs.getString("category"),
                            rs.getLong("file_size"),
                            rs.getString("mime_type"),
                            rs.getString("description"),
                            rs.getString("uploaded_by"),
                            rs.getTimestamp("uploaded_at").toLocalDateTime(),
                            rs.getTimestamp("last_modified").toLocalDateTime(),
                            rs.getString("file_hash"),
                            rs.getBoolean("is_encrypted"),
                            rs.getString("status"));
                    attachments.add(attachment);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving attachments by category: " + e.getMessage());
        }

        return attachments;
    }

    /**
     * Get a specific attachment by ID
     */
    public static FileAttachment getAttachmentById(int attachmentId) {
        String sql = "SELECT id, patient_id, file_name, file_path, file_type, category, " +
                "file_size, mime_type, description, uploaded_by, uploaded_at, " +
                "last_modified, file_hash, is_encrypted, status " +
                "FROM patient_file_attachments " +
                "WHERE id = ? AND status = 'Active'";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attachmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new FileAttachment(
                            rs.getInt("id"),
                            rs.getString("patient_id"),
                            rs.getString("file_name"),
                            rs.getString("file_path"),
                            rs.getString("file_type"),
                            rs.getString("category"),
                            rs.getLong("file_size"),
                            rs.getString("mime_type"),
                            rs.getString("description"),
                            rs.getString("uploaded_by"),
                            rs.getTimestamp("uploaded_at").toLocalDateTime(),
                            rs.getTimestamp("last_modified").toLocalDateTime(),
                            rs.getString("file_hash"),
                            rs.getBoolean("is_encrypted"),
                            rs.getString("status"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving attachment: " + e.getMessage());
        }

        return null;
    }

    /**
     * Update attachment description and metadata
     */
    public static List<String> updateAttachment(int attachmentId, String description, String fileType,
            String category) {
        List<String> result = new ArrayList<>();

        String sql = "UPDATE patient_file_attachments " +
                "SET description = ?, file_type = ?, category = ?, last_modified = ? " +
                "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, description != null ? description : "");
            stmt.setString(2, fileType != null ? fileType : "Unknown");
            stmt.setString(3, category != null ? category : "General");
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, attachmentId);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                result.add("Attachment updated successfully");
            } else {
                result.add("Error: Attachment not found");
            }

        } catch (SQLException e) {
            result.add("Error updating attachment: " + e.getMessage());
        }

        return result;
    }

    /**
     * Delete (soft delete) an attachment
     */
    public static List<String> deleteAttachment(int attachmentId) {
        List<String> result = new ArrayList<>();

        String sql = "UPDATE patient_file_attachments " +
                "SET status = 'Deleted', last_modified = ? " +
                "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, attachmentId);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                result.add("Attachment deleted successfully");
            } else {
                result.add("Error: Attachment not found");
            }

        } catch (SQLException e) {
            result.add("Error deleting attachment: " + e.getMessage());
        }

        return result;
    }

    /**
     * Get statistics for a patient's attachments
     */
    public static Map<String, Object> getAttachmentStats(String patientId) {
        Map<String, Object> stats = new HashMap<>();

        String sql = "SELECT COUNT(*) as total_files, " +
                "SUM(file_size) as total_size, " +
                "COUNT(DISTINCT category) as categories " +
                "FROM patient_file_attachments " +
                "WHERE patient_id = ? AND status = 'Active'";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalFiles", rs.getInt("total_files"));
                    stats.put("totalSize", rs.getLong("total_size"));
                    stats.put("categories", rs.getInt("categories"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving attachment stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Get available file categories
     */
    public static List<String> getAvailableCategories() {
        return Arrays.asList(
                "Imaging",
                "Laboratory",
                "Documentation",
                "Consultation",
                "Discharge",
                "Prescription",
                "Insurance",
                "Other");
    }

    /**
     * Get available file types for a category
     */
    public static List<String> getFileTypesForCategory(String category) {
        Map<String, List<String>> categoryTypes = new HashMap<>();

        categoryTypes.put("Imaging", Arrays.asList(
                "X-ray", "MRI Scan", "CT Scan", "Ultrasound", "DICOM Image"));

        categoryTypes.put("Laboratory", Arrays.asList(
                "Lab Results", "Blood Test", "Urinalysis", "Pathology Report", "Culture Results"));

        categoryTypes.put("Documentation", Arrays.asList(
                "Medical Certificate", "Progress Notes", "Treatment Plan", "Discharge Summary"));

        categoryTypes.put("Consultation", Arrays.asList(
                "Consultation Report", "Specialist Opinion", "Second Opinion"));

        categoryTypes.put("Discharge", Arrays.asList(
                "Discharge Summary", "Discharge Instructions", "Medication List"));

        categoryTypes.put("Prescription", Arrays.asList(
                "Prescription Document", "Medication List", "Refill Request"));

        categoryTypes.put("Insurance", Arrays.asList(
                "Insurance Document", "Authorization", "Claim Form"));

        categoryTypes.put("Other", Arrays.asList(
                "General Document", "Report", "Image", "PDF"));

        return categoryTypes.getOrDefault(category, Arrays.asList("Unknown"));
    }
}
