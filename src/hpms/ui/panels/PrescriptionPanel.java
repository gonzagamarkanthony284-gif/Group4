package hpms.ui.panels;

import hpms.model.*;
import hpms.ui.components.Theme;
import hpms.util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PrescriptionPanel extends JPanel {
    private JTable prescriptionTable;
    private DefaultTableModel tableModel;

    public PrescriptionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Theme.BACKGROUND);
        JLabel titleLabel = new JLabel("Prescription Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Theme.FOREGROUND);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Patient", "Doctor", "Medicine", "Dosage", "Frequency", "Days", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        prescriptionTable = new JTable(tableModel);
        prescriptionTable.setFont(new Font("Arial", Font.PLAIN, 11));
        prescriptionTable.setRowHeight(25);
        prescriptionTable.getTableHeader().setBackground(Theme.PRIMARY);
        prescriptionTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(prescriptionTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Theme.BACKGROUND);
        
        JButton createBtn = new JButton("Create");
        createBtn.setBackground(Theme.PRIMARY);
        createBtn.setForeground(Color.WHITE);
        createBtn.addActionListener(e -> showCreateDialog());
        
        JButton viewBtn = new JButton("View");
        viewBtn.setBackground(Theme.PRIMARY);
        viewBtn.setForeground(Color.WHITE);
        
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(244, 67, 54));
        deleteBtn.setForeground(Color.WHITE);
        
        buttonPanel.add(createBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    private void showCreateDialog() {
        JOptionPane.showMessageDialog(this, "Create prescription form would go here");
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Prescription p : DataStore.prescriptions.values()) {
            // Use medicineId field as medicine name since we removed the medicine system
            String medName = p.medicineId != null ? p.medicineId : "Unknown";
            tableModel.addRow(new Object[]{
                p.id, p.patientId, p.doctorId, medName,
                p.dosage, p.frequency, p.durationDays, p.isActive ? "Active" : "Discontinued"
            });
        }
    }
}
