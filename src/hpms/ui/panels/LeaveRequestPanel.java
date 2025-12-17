package hpms.ui.panels;

import hpms.model.LeaveRequest;
import hpms.service.LeaveRequestService;
import hpms.util.DataStore;
import hpms.ui.components.Theme;
import hpms.auth.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeaveRequestPanel extends JPanel {
    private JTable leaveTable;
    private DefaultTableModel tableModel;
    private JButton newRequestBtn, approveBtn, rejectBtn, viewBtn;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    
    public LeaveRequestPanel() {
        initializeComponents();
        buildUI();
        refreshTable();
    }
    
    private void initializeComponents() {
        // Table setup
        String[] columns = {"Request ID", "Staff Name", "Department", "Leave Type", "Start Date", 
                          "End Date", "Days", "Status", "Created Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        leaveTable = new JTable(tableModel);
        leaveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leaveTable.setRowHeight(28);
        Theme.styleTable(leaveTable);
        
        // Buttons
        newRequestBtn = new JButton("New Request");
        approveBtn = new JButton("Approve");
        rejectBtn = new JButton("Reject");
        viewBtn = new JButton("View Details");
        
        // Search and filter
        searchField = new JTextField(15);
        statusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected", "Cancelled"});
        
        // Style buttons
        Theme.stylePrimary(newRequestBtn);
        Theme.styleSuccess(approveBtn);
        Theme.styleDanger(rejectBtn);
        Theme.styleSecondary(viewBtn);
        
        // Enable/disable buttons based on role
        boolean isAdmin = AuthService.current != null && AuthService.current.role == hpms.model.UserRole.ADMIN;
        approveBtn.setEnabled(isAdmin);
        rejectBtn.setEnabled(isAdmin);
        
        // Action listeners
        newRequestBtn.addActionListener(this::onNewRequest);
        approveBtn.addActionListener(this::onApprove);
        rejectBtn.addActionListener(this::onReject);
        viewBtn.addActionListener(this::onViewDetails);
        searchField.addActionListener(e -> refreshTable());
        statusFilter.addActionListener(e -> refreshTable());
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Leave Requests");
        titleLabel.setFont(Theme.HEADING_2);
        titleLabel.setForeground(Theme.TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search and filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Theme.BACKGROUND);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(statusFilter);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(leaveTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Theme.BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(newRequestBtn);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(approveBtn);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(rejectBtn);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(viewBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        
        List<LeaveRequest> requests;
        if (AuthService.current != null && AuthService.current.role == hpms.model.UserRole.ADMIN) {
            requests = LeaveRequestService.getAllLeaveRequests();
        } else {
            requests = LeaveRequestService.getStaffLeaveRequests(AuthService.current.username);
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        for (LeaveRequest request : requests) {
            // Apply status filter
            if (!selectedStatus.equals("All") && !request.status.name().equals(selectedStatus)) {
                continue;
            }
            
            // Apply search filter
            if (!searchText.isEmpty()) {
                boolean matches = request.staffName.toLowerCase().contains(searchText) ||
                                request.department.toLowerCase().contains(searchText) ||
                                request.id.toLowerCase().contains(searchText) ||
                                request.leaveType.name().toLowerCase().contains(searchText);
                if (!matches) continue;
            }
            
            tableModel.addRow(new Object[]{
                request.id,
                request.staffName,
                request.department,
                request.leaveType.name(),
                request.startDate.format(dateFormatter),
                request.endDate.format(dateFormatter),
                request.totalDays,
                request.status.name(),
                request.createdAt.format(dateFormatter)
            });
        }
        
        tableModel.fireTableDataChanged();
    }
    
    private void onNewRequest(ActionEvent e) {
        new LeaveRequestDialog((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
        refreshTable();
    }
    
    private void onApprove(ActionEvent e) {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a leave request to approve", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String requestId = (String) tableModel.getValueAt(selectedRow, 0);
        List<String> result = LeaveRequestService.approveLeaveRequest(requestId, AuthService.current.username);
        
        if (result.isEmpty() || result.get(0).startsWith("Error:")) {
            JOptionPane.showMessageDialog(this, 
                result.isEmpty() ? "Approval failed" : result.get(0), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result.get(0), "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        }
    }
    
    private void onReject(ActionEvent e) {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a leave request to reject", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String requestId = (String) tableModel.getValueAt(selectedRow, 0);
        String reason = JOptionPane.showInputDialog(this, "Enter rejection reason:", "Reject Request", JOptionPane.QUESTION_MESSAGE);
        
        if (reason != null && !reason.trim().isEmpty()) {
            List<String> result = LeaveRequestService.rejectLeaveRequest(requestId, reason.trim());
            
            if (result.isEmpty() || result.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(this, 
                    result.isEmpty() ? "Rejection failed" : result.get(0), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.get(0), "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            }
        }
    }
    
    private void onViewDetails(ActionEvent e) {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a leave request to view", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String requestId = (String) tableModel.getValueAt(selectedRow, 0);
        LeaveRequest request = DataStore.leaveRequests.get(requestId);
        
        if (request != null) {
            new LeaveRequestDetailsDialog((Frame) SwingUtilities.getWindowAncestor(this), request).setVisible(true);
        }
    }
}
