package hpms.ui.cashier;

import hpms.auth.AuthSession;
import hpms.model.*;
import hpms.service.BillingService;
import hpms.util.DataStore;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Locale;

public class CashierDashboardPanel extends JPanel {
    private JTable pendingTable;
    private JLabel summaryLabel;
    private JComboBox<String> methodCombo;

    public CashierDashboardPanel(AuthSession session) {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Cashier Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Welcome, " + session.fullName);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        JPanel titleWrap = new JPanel();
        titleWrap.setOpaque(false);
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.add(title);
        titleWrap.add(subtitle);
        header.add(titleWrap, BorderLayout.WEST);
        summaryLabel = new JLabel("");
        summaryLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        header.add(summaryLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8,8));
        center.setBackground(new Color(248, 249, 250));
        center.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(226,232,240)), BorderFactory.createEmptyBorder(12,12,12,12)));

        JLabel listTitle = new JLabel("Pending Bills");
        listTitle.setFont(new Font("Arial", Font.BOLD, 13));
        center.add(listTitle, BorderLayout.NORTH);

        pendingTable = new JTable();
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingTable.setRowHeight(26);
        center.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        methodCombo = new JComboBox<>(new String[]{"CASH","CARD","MOBILE"});
        JButton payBtn = new JButton("Mark as Paid");
        actions.add(new JLabel("Method"));
        actions.add(methodCombo);
        actions.add(payBtn);
        center.add(actions, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        payBtn.addActionListener(e -> onPay());
        refresh();
        this.addHierarchyListener(evt -> {
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) SwingUtilities.invokeLater(this::refresh);
            }
        });
    }

    private String getSelectedBillId() {
        int i = pendingTable.getSelectedRow();
        if (i < 0) return null;
        return String.valueOf(pendingTable.getValueAt(i, 0));
    }

    private void onPay() {
        String id = getSelectedBillId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Select a bill"); return; }
        
        Bill bill = DataStore.bills.get(id);
        if (bill == null) {
            JOptionPane.showMessageDialog(this, "Invalid bill selected"); 
            return;
        }
        
        if (bill.paymentMethod != null) {
            // This is a pending confirmation
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Confirm payment for Bill " + id + " (" + String.format(Locale.US, "$%.2f", bill.total) + ")?\n" +
                "Payment Method: " + bill.paymentMethod.name(),
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                java.util.List<String> out = BillingService.confirmPayment(id);
                JOptionPane.showMessageDialog(this, String.join("\n", out));
                refresh();
            }
        } else {
            // This is a regular payment - set method and pay
            String method = String.valueOf(methodCombo.getSelectedItem());
            java.util.List<String> out = BillingService.pay(id, method);
            JOptionPane.showMessageDialog(this, String.join("\n", out));
            refresh();
        }
    }

    public void refresh() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"Bill ID","Patient","Total","Method","Created","Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        double unpaid = 0;
        int pendingCount = 0;
        int pendingConfirmationCount = 0;
        for (Bill b : DataStore.bills.values()) {
            if (!b.paid) {
                Patient p = DataStore.patients.get(b.patientId);
                String status;
                if (b.paymentMethod != null) {
                    status = "PENDING CONFIRMATION";
                    pendingConfirmationCount++;
                } else {
                    status = "UNPAID";
                    pendingCount++;
                }
                String method = b.paymentMethod != null ? b.paymentMethod.name() : "Not set";
                m.addRow(new Object[]{b.id, p != null ? p.name : b.patientId, 
                    String.format(Locale.US, "%.2f", b.total), method, b.createdAt, status});
                unpaid += b.total;
            }
        }
        pendingTable.setModel(m);
        summaryLabel.setText(String.format(Locale.US, "Unpaid: %d | Pending Confirmation: %d | Total Due: $%.2f", 
            pendingCount, pendingConfirmationCount, unpaid));
    }
}

