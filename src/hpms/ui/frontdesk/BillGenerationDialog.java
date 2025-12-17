package hpms.ui.frontdesk;

import hpms.auth.AuthSession;
import hpms.model.*;
import hpms.ui.components.Theme;
import hpms.util.DataStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

public class BillGenerationDialog extends JDialog {
    
    private final String patientId;
    private JTextArea billArea;
    
    public BillGenerationDialog(Frame owner, String patientId, AuthSession session) {
        super(owner, "Generate Patient Bill", true);
        this.patientId = patientId;
        initializeComponents();
        layoutComponents();
        setupDialog();
        generateBillPreview();
    }
    
    private void initializeComponents() {
        billArea = new JTextArea(20, 50);
        billArea.setEditable(false);
        billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        billArea.setBackground(Theme.SURFACE);
        billArea.setForeground(Theme.FOREGROUND);
    }
    
    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        // Title
        JLabel titleLabel = new JLabel("Bill Preview");
        titleLabel.setFont(Theme.HEADING_3);
        titleLabel.setForeground(Theme.FOREGROUND);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        
        // Bill content
        JScrollPane billScroll = new JScrollPane(billArea);
        billScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.setOpaque(false);
        
        JButton printButton = new JButton("Print Bill");
        printButton.setBackground(Theme.PRIMARY);
        printButton.setForeground(Color.WHITE);
        printButton.setFocusPainted(false);
        printButton.setBorderPainted(false);
        printButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        printButton.addActionListener(this::onPrint);
        
        JButton saveButton = new JButton("Save Bill");
        saveButton.addActionListener(this::onSave);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(printButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(billScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupDialog() {
        setLayout(new BorderLayout());
        setSize(700, 600);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void generateBillPreview() {
        Patient patient = DataStore.patients.get(patientId);
        if (patient == null) {
            billArea.setText("Patient not found: " + patientId);
            return;
        }
        
        StringBuilder bill = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#0.00");
        
        // Header
        bill.append("========================================\n");
        bill.append("        HOSPITAL PATIENT MANAGEMENT SYSTEM\n");
        bill.append("              PATIENT BILL\n");
        bill.append("========================================\n\n");
        
        // Patient Information
        bill.append("PATIENT INFORMATION\n");
        bill.append("------------------\n");
        bill.append("Patient ID: ").append(patient.id).append("\n");
        bill.append("Name: ").append(patient.name).append("\n");
        bill.append("Contact: ").append(patient.contact).append("\n");
        if (patient.email != null) {
            bill.append("Email: ").append(patient.email).append("\n");
        }
        bill.append("Address: ").append(patient.address).append("\n\n");
        
        // Billing Details
        bill.append("BILLING DETAILS\n");
        bill.append("---------------\n");
        
        // Get room charges
        double roomCharges = 0.0;
        for (Room room : DataStore.rooms.values()) {
            if (patientId.equals(room.occupantPatientId)) {
                // Calculate days stayed (simplified - assume 1 day for demo)
                int daysStayed = 1; // TODO: Calculate actual days
                roomCharges = 100.0 * daysStayed; // Fixed rate for demo
                bill.append("Room ").append(room.id).append(": $")
                    .append(df.format(100.0)).append(" x ").append(daysStayed).append(" day(s) = $")
                    .append(df.format(roomCharges)).append("\n");
                break;
            }
        }
        
        // Get services/medications (simplified for demo)
        double serviceCharges = 0.0;
        bill.append("Medical Services: $").append(df.format(serviceCharges)).append("\n");
        bill.append("Medications: $").append(df.format(0.0)).append("\n");
        bill.append("Lab Tests: $").append(df.format(0.0)).append("\n");
        
        // Summary
        double subtotal = roomCharges + serviceCharges;
        double tax = subtotal * 0.1; // 10% tax
        double total = subtotal + tax;
        
        bill.append("\n----------------------------------------\n");
        bill.append("Subtotal: $").append(df.format(subtotal)).append("\n");
        bill.append("Tax (10%): $").append(df.format(tax)).append("\n");
        bill.append("----------------------------------------\n");
        bill.append("TOTAL AMOUNT: $").append(df.format(total)).append("\n");
        bill.append("========================================\n\n");
        
        bill.append("Payment Terms:\n");
        bill.append("- Payment due within 30 days\n");
        bill.append("- Late payments subject to 5% monthly fee\n");
        bill.append("- Please bring this bill to the billing counter\n\n");
        
        bill.append("Generated on: ").append(java.time.LocalDate.now()).append("\n");
        
        billArea.setText(bill.toString());
    }
    
    private void onPrint(ActionEvent e) {
        // Simple print implementation
        try {
            boolean printed = billArea.print();
            if (printed) {
                JOptionPane.showMessageDialog(this, 
                    "Bill sent to printer successfully",
                    "Print Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Printing cancelled or failed",
                    "Print Failed",
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error printing bill: " + ex.getMessage(),
                "Print Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onSave(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Bill");
        fileChooser.setSelectedFile(new java.io.File("Bill_" + patientId + "_" + 
            java.time.LocalDate.now().toString() + ".txt"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileToSave);
                writer.write(billArea.getText());
                writer.close();
                
                JOptionPane.showMessageDialog(this, 
                    "Bill saved successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Save Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving bill: " + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
