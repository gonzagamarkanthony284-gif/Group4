package hpms.model;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class LeaveRequest {
    public String id;
    public String staffId;
    public String staffName;
    public String role;
    public String department;
    public LeaveType leaveType;
    public LocalDate startDate;
    public LocalDate endDate;
    public int totalDays;
    public String reason;
    public LeaveStatus status;
    public String approvedBy;
    public LocalDateTime approvedAt;
    public String rejectionReason;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    
    public enum LeaveType {
        VACATION, SICK, PERSONAL, MATERNITY, PATERNITY, EMERGENCY
    }
    
    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
    
    public LeaveRequest() {
        this.id = hpms.util.IDGenerator.nextId("LR");
        this.status = LeaveStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public LeaveRequest(String staffId, String staffName, String role, String department, 
                       LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason) {
        this();
        this.staffId = staffId;
        this.staffName = staffName;
        this.role = role;
        this.department = department;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.totalDays = calculateDays(startDate, endDate);
    }
    
    private int calculateDays(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        if (end.isBefore(start)) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    }
    
    public void approve(String approvedBy) {
        this.status = LeaveStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reject(String rejectionReason) {
        this.status = LeaveStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = LeaveStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isOverlapping(LocalDate otherStart, LocalDate otherEnd) {
        return !this.endDate.isBefore(otherStart) && !this.startDate.isAfter(otherEnd);
    }
}
