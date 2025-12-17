package hpms.service;

import hpms.model.LeaveRequest;
import hpms.model.Staff;
import hpms.util.DataStore;
import hpms.util.LogManager;
import hpms.util.DBConnection;
import java.util.*;
import java.sql.*;
import java.time.LocalDate;
import java.sql.Timestamp;

public class LeaveRequestService {
    
    public static List<String> createLeaveRequest(String staffId, LeaveRequest.LeaveType leaveType, 
                                                 LocalDate startDate, LocalDate endDate, String reason) {
        List<String> out = new ArrayList<>();
        
        if (staffId == null || staffId.trim().isEmpty()) {
            out.add("Error: Staff ID is required");
            return out;
        }
        
        if (startDate == null || endDate == null) {
            out.add("Error: Start date and end date are required");
            return out;
        }
        
        if (endDate.isBefore(startDate)) {
            out.add("Error: End date cannot be before start date");
            return out;
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            out.add("Error: Reason is required");
            return out;
        }
        
        // Get staff information
        Staff staff = DataStore.staff.get(staffId);
        if (staff == null) {
            out.add("Error: Staff not found");
            return out;
        }
        
        // Check for overlapping leave requests
        for (LeaveRequest existing : DataStore.leaveRequests.values()) {
            if (existing.staffId.equals(staffId) && 
                (existing.status == LeaveRequest.LeaveStatus.PENDING || 
                 existing.status == LeaveRequest.LeaveStatus.APPROVED) &&
                existing.isOverlapping(startDate, endDate)) {
                out.add("Error: Leave request overlaps with existing leave period: " + 
                       existing.startDate + " to " + existing.endDate);
                return out;
            }
        }
        
        // Create leave request
        LeaveRequest request = new LeaveRequest(staffId, staff.name, staff.role.name(), 
                                              staff.department, leaveType, startDate, endDate, reason);
        
        // Save to DataStore
        DataStore.leaveRequests.put(request.id, request);
        
        // Save to database
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "INSERT INTO leave_requests (id, staff_id, staff_name, role, department, " +
                           "leave_type, start_date, end_date, total_days, reason, status, " +
                           "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, request.id);
                    stmt.setString(2, request.staffId);
                    stmt.setString(3, request.staffName);
                    stmt.setString(4, request.role);
                    stmt.setString(5, request.department);
                    stmt.setString(6, request.leaveType.name());
                    stmt.setDate(7, java.sql.Date.valueOf(request.startDate));
                    stmt.setDate(8, java.sql.Date.valueOf(request.endDate));
                    stmt.setInt(9, request.totalDays);
                    stmt.setString(10, request.reason);
                    stmt.setString(11, request.status.name());
                    stmt.setTimestamp(12, Timestamp.valueOf(request.createdAt));
                    stmt.setTimestamp(13, Timestamp.valueOf(request.updatedAt));
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            out.add("Warning: Leave request created but database save failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        LogManager.log("leave_request_created id=" + request.id + " staff=" + staffId + 
                      " type=" + leaveType + " days=" + request.totalDays);
        
        out.add("Leave request created successfully. Request ID: " + request.id);
        return out;
    }
    
    public static List<String> approveLeaveRequest(String requestId, String approvedBy) {
        List<String> out = new ArrayList<>();
        
        if (requestId == null || requestId.trim().isEmpty()) {
            out.add("Error: Request ID is required");
            return out;
        }
        
        LeaveRequest request = DataStore.leaveRequests.get(requestId);
        if (request == null) {
            out.add("Error: Leave request not found");
            return out;
        }
        
        if (request.status != LeaveRequest.LeaveStatus.PENDING) {
            out.add("Error: Request is not pending. Current status: " + request.status);
            return out;
        }
        
        request.approve(approvedBy);
        
        // Update database
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "UPDATE leave_requests SET status = ?, approved_by = ?, " +
                           "approved_at = ?, updated_at = ? WHERE id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, request.status.name());
                    stmt.setString(2, request.approvedBy);
                    stmt.setTimestamp(3, Timestamp.valueOf(request.approvedAt));
                    stmt.setTimestamp(4, Timestamp.valueOf(request.updatedAt));
                    stmt.setString(5, requestId);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            out.add("Warning: Request approved but database update failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        LogManager.log("leave_request_approved id=" + requestId + " by=" + approvedBy);
        out.add("Leave request approved successfully");
        return out;
    }
    
    public static List<String> rejectLeaveRequest(String requestId, String rejectionReason) {
        List<String> out = new ArrayList<>();
        
        if (requestId == null || requestId.trim().isEmpty()) {
            out.add("Error: Request ID is required");
            return out;
        }
        
        LeaveRequest request = DataStore.leaveRequests.get(requestId);
        if (request == null) {
            out.add("Error: Leave request not found");
            return out;
        }
        
        if (request.status != LeaveRequest.LeaveStatus.PENDING) {
            out.add("Error: Request is not pending. Current status: " + request.status);
            return out;
        }
        
        request.reject(rejectionReason);
        
        // Update database
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "UPDATE leave_requests SET status = ?, rejection_reason = ?, " +
                           "updated_at = ? WHERE id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, request.status.name());
                    stmt.setString(2, request.rejectionReason);
                    stmt.setTimestamp(3, Timestamp.valueOf(request.updatedAt));
                    stmt.setString(4, requestId);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            out.add("Warning: Request rejected but database update failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        LogManager.log("leave_request_rejected id=" + requestId + " reason=" + rejectionReason);
        out.add("Leave request rejected");
        return out;
    }
    
    public static List<LeaveRequest> getAllLeaveRequests() {
        return new ArrayList<>(DataStore.leaveRequests.values());
    }
    
    public static List<LeaveRequest> getPendingLeaveRequests() {
        List<LeaveRequest> pending = new ArrayList<>();
        for (LeaveRequest request : DataStore.leaveRequests.values()) {
            if (request.status == LeaveRequest.LeaveStatus.PENDING) {
                pending.add(request);
            }
        }
        return pending;
    }
    
    public static List<LeaveRequest> getStaffLeaveRequests(String staffId) {
        List<LeaveRequest> staffRequests = new ArrayList<>();
        for (LeaveRequest request : DataStore.leaveRequests.values()) {
            if (request.staffId.equals(staffId)) {
                staffRequests.add(request);
            }
        }
        return staffRequests;
    }
    
    public static void loadFromDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "SELECT * FROM leave_requests";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    LeaveRequest request = new LeaveRequest();
                    request.id = rs.getString("id");
                    request.staffId = rs.getString("staff_id");
                    request.staffName = rs.getString("staff_name");
                    request.role = rs.getString("role");
                    request.department = rs.getString("department");
                    request.leaveType = LeaveRequest.LeaveType.valueOf(rs.getString("leave_type"));
                    request.startDate = rs.getDate("start_date").toLocalDate();
                    request.endDate = rs.getDate("end_date").toLocalDate();
                    request.totalDays = rs.getInt("total_days");
                    request.reason = rs.getString("reason");
                    request.status = LeaveRequest.LeaveStatus.valueOf(rs.getString("status"));
                    request.approvedBy = rs.getString("approved_by");
                    request.rejectionReason = rs.getString("rejection_reason");
                    request.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    request.updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
                    
                    if (rs.getTimestamp("approved_at") != null) {
                        request.approvedAt = rs.getTimestamp("approved_at").toLocalDateTime();
                    }
                    
                    DataStore.leaveRequests.put(request.id, request);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load leave requests from database: " + e.getMessage());
        }
    }
}
