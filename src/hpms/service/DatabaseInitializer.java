package hpms.service;

import hpms.util.DBConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseInitializer {
    
    public static void initializeLeaveRequestsTable() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            Statement stmt = conn.createStatement();
            
            String sql = "CREATE TABLE IF NOT EXISTS leave_requests (" +
                        "id VARCHAR(20) PRIMARY KEY," +
                        "staff_id VARCHAR(20) NOT NULL," +
                        "staff_name VARCHAR(100) NOT NULL," +
                        "role VARCHAR(50) NOT NULL," +
                        "department VARCHAR(100)," +
                        "leave_type VARCHAR(20) NOT NULL," +
                        "start_date DATE NOT NULL," +
                        "end_date DATE NOT NULL," +
                        "total_days INT NOT NULL," +
                        "reason TEXT NOT NULL," +
                        "status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
                        "approved_by VARCHAR(20)," +
                        "approved_at TIMESTAMP," +
                        "rejection_reason TEXT," +
                        "created_at TIMESTAMP NOT NULL," +
                        "updated_at TIMESTAMP NOT NULL" +
                        ")";
            
            stmt.execute(sql);
            System.out.println("Leave requests table initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize leave requests table: " + e.getMessage());
        }
    }
    
    public static void initializePatientStatusTable() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            Statement stmt = conn.createStatement();
            
            // First, create the table if it doesn't exist
            String sql = "CREATE TABLE IF NOT EXISTS patient_status (" +
                        "patient_id VARCHAR(20) NOT NULL," +
                        "status VARCHAR(30) NOT NULL," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "note TEXT," +
                        "changed_by VARCHAR(20)," +
                        "PRIMARY KEY (patient_id, created_at)," +
                        "INDEX idx_patient_status (patient_id)" +
                        ")";
            
            stmt.execute(sql);
            
            // Check if status column needs to be enlarged and fix it if needed
            try {
                System.out.println("Checking status column size...");
                ResultSet rs = stmt.executeQuery("SELECT status FROM patient_status LIMIT 1");
                rs.close();
                System.out.println("status column exists");
                
                // Try to alter the column to ensure it's large enough
                try {
                    stmt.execute("ALTER TABLE patient_status MODIFY COLUMN status VARCHAR(30)");
                    System.out.println("status column size updated to VARCHAR(30)");
                } catch (SQLException modifyError) {
                    // Column might already be the right size, which is fine
                    System.out.println("status column size already sufficient or cannot be modified: " + modifyError.getMessage());
                }
                
            } catch (SQLException e) {
                System.err.println("Error checking status column: " + e.getMessage());
            }
            
            // Check if note column exists and add it if it doesn't
            try {
                System.out.println("Checking if note column exists in patient_status table...");
                ResultSet rs = stmt.executeQuery("SELECT note FROM patient_status LIMIT 1");
                rs.close(); // Column exists, no action needed
                System.out.println("note column already exists");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                System.out.println("Adding note column to patient_status table...");
                try {
                    stmt.execute("ALTER TABLE patient_status ADD COLUMN note TEXT");
                    System.out.println("note column added successfully");
                } catch (SQLException alterError) {
                    System.err.println("Failed to add note column: " + alterError.getMessage());
                }
            }
            
            // Check if created_at column exists and add it if it doesn't
            try {
                System.out.println("Checking if created_at column exists in patient_status table...");
                ResultSet rs = stmt.executeQuery("SELECT created_at FROM patient_status LIMIT 1");
                rs.close(); // Column exists, no action needed
                System.out.println("created_at column already exists");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                System.out.println("Adding created_at column to patient_status table...");
                try {
                    stmt.execute("ALTER TABLE patient_status ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
                    System.out.println("created_at column added successfully");
                } catch (SQLException alterError) {
                    System.err.println("Failed to add created_at column: " + alterError.getMessage());
                }
            }
            
            // Check if changed_by column exists and add it if it doesn't
            try {
                System.out.println("Checking if changed_by column exists in patient_status table...");
                ResultSet rs = stmt.executeQuery("SELECT changed_by FROM patient_status LIMIT 1");
                rs.close(); // Column exists, no action needed
                System.out.println("changed_by column already exists");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                System.out.println("Adding changed_by column to patient_status table...");
                try {
                    stmt.execute("ALTER TABLE patient_status ADD COLUMN changed_by VARCHAR(20)");
                    System.out.println("changed_by column added successfully");
                } catch (SQLException alterError) {
                    System.err.println("Failed to add changed_by column: " + alterError.getMessage());
                }
            }
            
            System.out.println("Patient status table initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize patient status table: " + e.getMessage());
        }
    }
}
