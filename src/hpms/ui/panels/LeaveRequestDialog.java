package hpms.ui.panels;

import hpms.model.LeaveRequest;
import hpms.model.Staff;
import hpms.service.LeaveRequestService;
import hpms.util.DataStore;
import hpms.ui.components.Theme;
import hpms.auth.AuthService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class LeaveRequestDialog extends JDialog {
    private JComboBox<LeaveRequest.LeaveType> leaveTypeCombo;
    private JTextField startDateField, endDateField;
    private JTextArea reasonArea;
    private JButton submitBtn, cancelBtn;
    
    public LeaveRequestDialog(Frame owner) {
        super(owner, "New Leave Request", true);
        setSize(500, 400);
        setLocationRelativeTo(owner);
        buildUI();
        initializeData();
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Staff info (read-only)
        Staff currentStaff = DataStore.staff.get(AuthService.current.username);
        if (currentStaff != null) {
            formPanel.add(new JLabel("Staff Name:"), gbc);
            gbc.gridx = 1;
            JTextField nameField = new JTextField(currentStaff.name, 20);
            nameField.setEditable(false);
            nameField.setBackground(Color.LIGHT_GRAY);
            formPanel.add(nameField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Department:"), gbc);
            gbc.gridx = 1;
            JTextField deptField = new JTextField(currentStaff.department, 20);
            deptField.setEditable(false);
            deptField.setBackground(Color.LIGHT_GRAY);
            formPanel.add(deptField, gbc);
        }
        
        // Leave type
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Leave Type:"), gbc);
        gbc.gridx = 1;
        leaveTypeCombo = new JComboBox<>(LeaveRequest.LeaveType.values());
        formPanel.add(leaveTypeCombo, gbc);
        
        // Start date
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        startDateField = new JTextField(20);
        startDateField.setToolTipText("Enter date in format: YYYY-MM-DD");
        formPanel.add(startDateField, gbc);
        
        // End date
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        endDateField = new JTextField(20);
        endDateField.setToolTipText("Enter date in format: YYYY-MM-DD");
        formPanel.add(endDateField, gbc);
        
        // Reason
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        reasonArea = new JTextArea(4, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(reasonArea);
        formPanel.add(scrollPane, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitBtn = new JButton("Submit Request");
        cancelBtn = new JButton("Cancel");
        
        Theme.stylePrimary(submitBtn);
        Theme.styleSecondary(cancelBtn);
        
        submitBtn.addActionListener(e -> onSubmit());
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void initializeData() {
        // Set default dates to today and tomorrow
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        startDateField.setText(today.toString());
        endDateField.setText(tomorrow.toString());
    }
    
    private void onSubmit() {
        String startDateStr = startDateField.getText().trim();
        String endDateStr = endDateField.getText().trim();
        String reason = reasonArea.getText().trim();
        
        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both start and end dates", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a reason for the leave request", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            
            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(this, "End date cannot be before start date", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LeaveRequest.LeaveType leaveType = (LeaveRequest.LeaveType) leaveTypeCombo.getSelectedItem();
            
            List<String> result = LeaveRequestService.createLeaveRequest(
                AuthService.current.username, leaveType, startDate, endDate, reason);
            
            if (result.isEmpty() || result.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(this, 
                    result.isEmpty() ? "Request failed" : result.get(0), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.get(0), "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
