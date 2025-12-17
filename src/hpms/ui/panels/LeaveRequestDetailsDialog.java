package hpms.ui.panels;

import hpms.model.LeaveRequest;
import hpms.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class LeaveRequestDetailsDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm");
    
    public LeaveRequestDetailsDialog(Frame owner, LeaveRequest request) {
        super(owner, "Leave Request Details - " + request.id, true);
        setSize(600, 500);
        setLocationRelativeTo(owner);
        buildUI(request);
    }
    
    private void buildUI(LeaveRequest request) {
        setLayout(new BorderLayout());
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        // Request ID
        addFieldRow(contentPanel, gbc, "Request ID:", request.id, 0);
        
        // Staff Information
        addFieldRow(contentPanel, gbc, "Staff Name:", request.staffName, 1);
        addFieldRow(contentPanel, gbc, "Staff ID:", request.staffId, 2);
        addFieldRow(contentPanel, gbc, "Department:", request.department, 3);
        addFieldRow(contentPanel, gbc, "Role:", request.role, 4);
        
        // Leave Details
        addFieldRow(contentPanel, gbc, "Leave Type:", request.leaveType.name(), 5);
        addFieldRow(contentPanel, gbc, "Start Date:", request.startDate.format(DATE_FORMATTER), 6);
        addFieldRow(contentPanel, gbc, "End Date:", request.endDate.format(DATE_FORMATTER), 7);
        addFieldRow(contentPanel, gbc, "Total Days:", String.valueOf(request.totalDays), 8);
        
        // Status
        gbc.gridy = 9;
        contentPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        JLabel statusLabel = new JLabel(request.status.name());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        
        // Color code the status
        switch (request.status) {
            case PENDING:
                statusLabel.setForeground(new Color(255, 165, 0)); // Orange
                break;
            case APPROVED:
                statusLabel.setForeground(new Color(0, 128, 0)); // Green
                break;
            case REJECTED:
                statusLabel.setForeground(new Color(220, 20, 60)); // Crimson
                break;
            case CANCELLED:
                statusLabel.setForeground(new Color(128, 128, 128)); // Gray
                break;
        }
        
        contentPanel.add(statusLabel, gbc);
        
        // Reason
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.NORTHWEST;
        contentPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        JTextArea reasonArea = new JTextArea(request.reason, 4, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setEditable(false);
        reasonArea.setBackground(Color.LIGHT_GRAY);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        contentPanel.add(reasonScroll, gbc);
        
        // Approval information (if approved)
        if (request.status == LeaveRequest.LeaveStatus.APPROVED && request.approvedBy != null) {
            gbc.gridx = 0;
            gbc.gridy = 11;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;
            addFieldRow(contentPanel, gbc, "Approved By:", request.approvedBy, 11);
            
            gbc.gridy = 12;
            contentPanel.add(new JLabel("Approved At:"), gbc);
            gbc.gridx = 1;
            contentPanel.add(new JLabel(request.approvedAt.format(DATE_TIME_FORMATTER)), gbc);
        }
        
        // Rejection reason (if rejected)
        if (request.status == LeaveRequest.LeaveStatus.REJECTED && request.rejectionReason != null) {
            gbc.gridx = 0;
            gbc.gridy = 11;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;
            contentPanel.add(new JLabel("Rejection Reason:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            JTextArea rejectionArea = new JTextArea(request.rejectionReason, 3, 30);
            rejectionArea.setLineWrap(true);
            rejectionArea.setWrapStyleWord(true);
            rejectionArea.setEditable(false);
            rejectionArea.setBackground(new Color(255, 220, 220)); // Light red background
            contentPanel.add(rejectionArea, gbc);
        }
        
        // Timestamps
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        addFieldRow(contentPanel, gbc, "Created At:", request.createdAt.format(DATE_TIME_FORMATTER), 13);
        addFieldRow(contentPanel, gbc, "Updated At:", request.updatedAt.format(DATE_TIME_FORMATTER), 14);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        Theme.styleSecondary(closeBtn);
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addFieldRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(new JLabel(value), gbc);
    }
}
