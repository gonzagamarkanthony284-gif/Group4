package hpms.ui.panels;

import hpms.model.*;
import hpms.service.*;
import hpms.util.*;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Locale;

public class BillingPanel extends JPanel {
    private DefaultTableModel billModel;
    private JTable billTable;
    private JLabel statsLabel;

    public BillingPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(SectionHeader.info("Billing & Payment Management",
                "Create, manage bills and process payments with detailed tracking"), BorderLayout.NORTH);

        // Stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.BEFORE_FIRST_LINE);

        // Billing table
        billModel = new DefaultTableModel(
                new String[] { "Bill ID", "Patient Name", "Total Amount", "Items", "Status", "Payment Method", "Date" },
                0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        billTable = new JTable(billModel);
        billTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        billTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        billTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        billTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        billTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        billTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        billTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        billTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(billTable);
        add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);

        refresh();

        // keep billing table up-to-date when returning to this panel
        this.addHierarchyListener(evt -> {
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing())
                    SwingUtilities.invokeLater(this::refresh);
            }
        });
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        statsLabel = new JLabel("Total Bills: 0 | Paid: $0 | Unpaid: $0 | Due Amount: $0");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(80, 80, 80));

        panel.add(statsLabel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(8, 12, 12, 12));

        JButton createBtn = new JButton("Create Bill");
        styleButton(createBtn, new Color(0, 110, 102));

        JButton addItemBtn = new JButton("Add Item");
        styleButton(addItemBtn, new Color(41, 128, 185));

        JButton payBtn = new JButton("Pay Bill");
        styleButton(payBtn, new Color(39, 174, 96));

        JButton confirmBtn = new JButton("Confirm Payment");
        styleButton(confirmBtn, new Color(230, 126, 34));

        JButton viewItemsBtn = new JButton("View Items");
        styleButton(viewItemsBtn, new Color(155, 89, 182));

        JButton historyBtn = new JButton("Payment History");
        styleButton(historyBtn, new Color(127, 140, 141));

        JButton deleteBtn = new JButton("Delete");
        styleButton(deleteBtn, new Color(192, 57, 43));

        createBtn.addActionListener(e -> createBillDialog());
        addItemBtn.addActionListener(e -> addItemDialog());
        payBtn.addActionListener(e -> payBillDialog());
        confirmBtn.addActionListener(e -> confirmPaymentDialog());
        viewItemsBtn.addActionListener(e -> viewBillItems());
        historyBtn.addActionListener(e -> showPaymentHistory());
        deleteBtn.addActionListener(e -> deleteBill());

        panel.add(createBtn);
        panel.add(addItemBtn);
        panel.add(payBtn);
        panel.add(confirmBtn);
        panel.add(viewItemsBtn);
        panel.add(historyBtn);
        panel.add(deleteBtn);

        return panel;
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void createBillDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create New Bill", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(new Color(245, 247, 250));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Patient *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> patientCombo = new JComboBox<>();
        DataStore.patients.forEach((id, p) -> patientCombo.addItem(id + " - " + p.name));
        panel.add(patientCombo, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("Bill Description"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextArea descArea = new JTextArea(4, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descArea), c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.3;
        panel.add(new JLabel("Initial Amount"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField amountField = new JTextField();
        amountField.setText("0.00");
        panel.add(amountField, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn = new JButton("Create");
        styleButton(saveBtn, new Color(0, 110, 102));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            if (patientCombo.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a patient", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String patientId = patientCombo.getSelectedItem().toString().split(" - ")[0];
            String amount = amountField.getText().trim();

            java.util.List<String> result = BillingService.create(patientId, amount);
            if (result.get(0).startsWith("Bill created")) {
                JOptionPane.showMessageDialog(dialog, "Bill created successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addItemDialog() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill to add items", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = billModel.getValueAt(row, 0).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Bill Item", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Item Description *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField descField = new JTextField();
        panel.add(descField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("Quantity"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(quantitySpinner, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.3;
        panel.add(new JLabel("Unit Price *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField priceField = new JTextField();
        panel.add(priceField, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn = new JButton("Add Item");
        styleButton(saveBtn, new Color(41, 128, 185));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            if (descField.getText().trim().isEmpty() || priceField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Description and Price are required", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String description = descField.getText().trim() + " (Qty: " + quantitySpinner.getValue() + ")";
            String price = priceField.getText().trim();

            java.util.List<String> result = BillingService.addItem(billId, description, price);
            if (result.get(0).startsWith("Item added")) {
                JOptionPane.showMessageDialog(dialog, "Item added successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void payBillDialog() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill to pay", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = billModel.getValueAt(row, 0).toString();
        Bill bill = DataStore.bills.get(billId);

        if (bill == null)
            return;

        if (bill.paid) {
            JOptionPane.showMessageDialog(this, "This bill is already paid", "Payment Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (bill.paymentMethod != null) {
            // This is a pending confirmation from patient
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Confirm payment for Bill " + billId + " (" + String.format(Locale.US, "$%.2f", bill.total) + ")?\n" +
                "Patient Payment Method: " + bill.paymentMethod.name(),
                "Confirm Patient Payment", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                java.util.List<String> result = BillingService.confirmPayment(billId);
                if (result.get(0).startsWith("Payment confirmed")) {
                    JOptionPane.showMessageDialog(this, "Payment confirmed successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }

        // Regular admin payment processing
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Process Payment", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        panel.add(new JLabel("Bill ID"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField billIdDisplay = new JTextField(billId);
        billIdDisplay.setEditable(false);
        panel.add(billIdDisplay, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.3;
        panel.add(new JLabel("Total Amount"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField totalDisplay = new JTextField(String.format(Locale.US, "%.2f", bill.total));
        totalDisplay.setEditable(false);
        panel.add(totalDisplay, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.3;
        panel.add(new JLabel("Payment Method *"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JComboBox<String> methodCombo = new JComboBox<>(
                new String[] { "CASH", "CARD", "INSURANCE", "CHECK", "BANK_TRANSFER" });
        panel.add(methodCombo, c);

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.3;
        panel.add(new JLabel("Receipt/Reference"), c);
        c.gridx = 1;
        c.weightx = 0.7;
        JTextField refField = new JTextField();
        panel.add(refField, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton payBtn = new JButton("Process Payment");
        styleButton(payBtn, new Color(39, 174, 96));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(payBtn);

        payBtn.addActionListener(e -> {
            java.util.List<String> result = BillingService.pay(billId, methodCombo.getSelectedItem().toString());
            if (result.get(0).startsWith("Payment successful")) {
                JOptionPane.showMessageDialog(dialog, "Payment processed successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void viewBillItems() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill to view items", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = billModel.getValueAt(row, 0).toString();
        Bill bill = DataStore.bills.get(billId);

        if (bill == null)
            return;

        DefaultTableModel itemModel = new DefaultTableModel(new String[] { "Item Description", "Amount" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        double total = 0;
        for (BillItem item : bill.items) {
            itemModel.addRow(new Object[] { item.description, String.format(Locale.US, "%.2f", item.price) });
            total += item.price;
        }

        JTable itemTable = new JTable(itemModel);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        JLabel totalLabel = new JLabel("Total: " + String.format(Locale.US, "%.2f", total));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(totalLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Bill Items - " + billId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showPaymentHistory() {
        DefaultTableModel historyModel = new DefaultTableModel(
                new String[] { "Bill ID", "Patient", "Amount", "Method", "Paid Date" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        double totalPaid = 0;
        for (Bill bill : DataStore.bills.values()) {
            if (bill.paid) {
                Patient patient = DataStore.patients.get(bill.patientId);
                historyModel.addRow(new Object[] {
                        bill.id,
                        patient != null ? patient.name : bill.patientId,
                        String.format(Locale.US, "%.2f", bill.total),
                        bill.paymentMethod != null ? bill.paymentMethod : "N/A",
                        bill.updatedAt
                });
                totalPaid += bill.total;
            }
        }

        JTable historyTable = new JTable(historyModel);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JLabel summaryLabel = new JLabel("Total Paid: " + String.format(Locale.US, "%.2f", totalPaid));
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        summaryLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(summaryLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Payment History", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmPaymentDialog() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill to confirm payment", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = billModel.getValueAt(row, 0).toString();
        Bill bill = DataStore.bills.get(billId);

        if (bill == null) {
            JOptionPane.showMessageDialog(this, "Invalid bill selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (bill.paid) {
            JOptionPane.showMessageDialog(this, "This bill is already paid", "Payment Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (bill.paymentMethod == null) {
            JOptionPane.showMessageDialog(this, "This bill has no pending payment to confirm", "Payment Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Confirm payment for Bill " + billId + " (" + String.format(Locale.US, "$%.2f", bill.total) + ")?\n" +
            "Patient Payment Method: " + bill.paymentMethod.name() + "\n\n" +
            "This will finalize the patient's payment.",
            "Confirm Patient Payment", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            java.util.List<String> result = BillingService.confirmPayment(billId);
            if (result.get(0).startsWith("Payment confirmed")) {
                JOptionPane.showMessageDialog(this, "Payment confirmed successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteBill() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill to delete", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = billModel.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this bill?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DataStore.bills.remove(billId);
            // Disabled backup save - using database instead
            JOptionPane.showMessageDialog(this, "Bill deleted successfully", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            refresh();
        }
    }

    public void refresh() {
        billModel.setRowCount(0);

        double paidAmount = 0;
        double unpaidAmount = 0;
        double pendingAmount = 0;
        int pendingCount = 0;

        for (Bill bill : DataStore.bills.values()) {
            Patient patient = DataStore.patients.get(bill.patientId);
            String patientName = patient != null ? patient.name : bill.patientId;
            String status;
            
            if (bill.paid) {
                status = "PAID";
                paidAmount += bill.total;
            } else if (bill.paymentMethod != null) {
                status = "PENDING CONFIRMATION";
                unpaidAmount += bill.total;
                pendingAmount += bill.total;
                pendingCount++;
            } else {
                status = "UNPAID";
                unpaidAmount += bill.total;
            }

            billModel.addRow(new Object[] {
                    bill.id,
                    patientName,
                    String.format(Locale.US, "%.2f", bill.total),
                    bill.items.size(),
                    status,
                    bill.paymentMethod != null ? bill.paymentMethod : "-",
                    bill.updatedAt
            });
        }

        String stats = String.format(Locale.US, 
            "Total Bills: %d | Paid: $%.2f | Unpaid: $%.2f | Pending: %d ($%.2f)",
            DataStore.bills.size(), paidAmount, unpaidAmount, pendingCount, pendingAmount);
        statsLabel.setText(stats);
    }
}
