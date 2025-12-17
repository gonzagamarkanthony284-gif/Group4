package hpms.service;

import hpms.model.*;
import hpms.util.*;

import java.time.LocalDateTime;
import java.util.*;
import java.sql.*;

public class BillingService {
    public static List<String> create(String patientId, String initialAmount) {
        List<String> out = new ArrayList<>();
        if (Validators.empty(patientId) || Validators.empty(initialAmount)) {
            out.add("Error: Missing parameters");
            return out;
        }
        Patient p = DataStore.patients.get(patientId);
        if (p == null) {
            out.add("Error: Creating bill for unregistered patient");
            return out;
        }
        double amt;
        try {
            amt = Double.parseDouble(initialAmount.trim());
        } catch (Exception e) {
            out.add("Error: Invalid amount");
            return out;
        }
        if (amt < 0) {
            out.add("Error: Invalid amount");
            return out;
        }
        String id = IDGenerator.nextId("B");
        Bill b = new Bill(id, patientId, amt, LocalDateTime.now());
        DataStore.bills.put(id, b);
        LogManager.log("create_bill " + id);
        
        // Also save to database
        saveToDatabase(b);
        
        out.add("Bill created " + id);
        return out;
    }

    public static List<String> addItem(String billId, String description, String price) {
        List<String> out = new ArrayList<>();
        Bill b = DataStore.bills.get(billId);
        if (b == null) {
            out.add("Error: Invalid bill ID");
            return out;
        }
        if (b.paid) {
            out.add("Error: Cannot add item to paid bill");
            return out;
        }
        if (Validators.empty(description) || Validators.empty(price)) {
            out.add("Error: Missing parameters");
            return out;
        }
        double amt;
        try {
            amt = Double.parseDouble(price.trim());
        } catch (Exception e) {
            out.add("Error: Invalid amount");
            return out;
        }
        if (amt < 0) {
            out.add("Error: Invalid amount");
            return out;
        }
        b.items.add(new BillItem(description.trim(), amt));
        b.total = b.items.stream().mapToDouble(i -> i.price).sum();
        b.updatedAt = LocalDateTime.now();
        LogManager.log("add_bill_item " + billId);
        // Disabled backup save - using database instead
        out.add("Bill item added");
        out.add("Total " + String.format(java.util.Locale.US, "%.2f", b.total));
        return out;
    }

    public static List<String> pay(String billId, String method) {
        List<String> out = new ArrayList<>();
        Bill b = DataStore.bills.get(billId);
        if (b == null) {
            out.add("Error: Invalid bill ID");
            return out;
        }
        if (b.paid) {
            out.add("Payment successful");
            return out;
        }
        PaymentMethod m;
        try {
            m = PaymentMethod.valueOf(method.toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            out.add("Error: Invalid payment method");
            return out;
        }
        if (!DataStore.allowedPaymentMethods.contains(m)) {
            out.add("Error: Payment method not allowed");
            return out;
        }
        b.paymentMethod = m;
        b.paid = true;
        b.updatedAt = LocalDateTime.now();
        LogManager.log("pay_bill " + billId + " " + m);
        // Also update in database
        updateInDatabase(b);
        
        out.add("Payment successful");
        return out;
    }

    public static List<String> initiatePayment(String billId, String method) {
        List<String> out = new ArrayList<>();
        Bill b = DataStore.bills.get(billId);
        if (b == null) {
            out.add("Error: Invalid bill ID");
            return out;
        }
        if (b.paid) {
            out.add("Error: Bill already paid");
            return out;
        }
        PaymentMethod m;
        try {
            m = PaymentMethod.valueOf(method.toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            out.add("Error: Invalid payment method");
            return out;
        }
        if (!DataStore.allowedPaymentMethods.contains(m)) {
            out.add("Error: Payment method not allowed");
            return out;
        }
        
        // Set payment method but mark as pending confirmation
        b.paymentMethod = m;
        b.updatedAt = LocalDateTime.now();
        // Note: b.paid remains false until confirmation
        
        LogManager.log("initiate_payment " + billId + " " + m);
        // Update in database to show pending payment
        updateInDatabase(b);
        
        out.add("Payment initiated successfully - awaiting confirmation");
        return out;
    }
    
    public static List<String> confirmPayment(String billId) {
        List<String> out = new ArrayList<>();
        Bill b = DataStore.bills.get(billId);
        if (b == null) {
            out.add("Error: Invalid bill ID");
            return out;
        }
        if (b.paid) {
            out.add("Error: Payment already confirmed");
            return out;
        }
        if (b.paymentMethod == null) {
            out.add("Error: No payment method set");
            return out;
        }
        
        // Confirm the payment
        b.paid = true;
        b.updatedAt = LocalDateTime.now();
        
        LogManager.log("confirm_payment " + billId + " " + b.paymentMethod);
        // Update in database
        updateInDatabase(b);
        
        out.add("Payment confirmed successfully");
        return out;
    }
    
    /**
     * Save bill to database
     */
    private static void saveToDatabase(Bill bill) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO bills (id, patient_id, total, created_at, paid, payment_method) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, bill.id);
                stmt.setString(2, bill.patientId);
                stmt.setDouble(3, bill.total);
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(bill.createdAt));
                stmt.setBoolean(5, bill.paid);
                stmt.setString(6, bill.paymentMethod != null ? bill.paymentMethod.name() : null);
                stmt.executeUpdate();
                LogManager.log("bill_db_save " + bill.id);
            }
        } catch (SQLException e) {
            System.err.println("Error saving bill to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update bill in database
     */
    private static void updateInDatabase(Bill bill) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE bills SET total=?, paid=?, payment_method=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, bill.total);
                stmt.setBoolean(2, bill.paid);
                stmt.setString(3, bill.paymentMethod != null ? bill.paymentMethod.name() : null);
                stmt.setString(4, bill.id);
                stmt.executeUpdate();
                LogManager.log("bill_db_update " + bill.id);
            }
        } catch (SQLException e) {
            System.err.println("Error updating bill in database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load all bills from database into DataStore
     */
    public static void loadFromDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            // Find highest bill ID to sync bCounter
            int maxId = 4000; // Start from default
            String maxIdSql = "SELECT id FROM bills WHERE id LIKE 'B%' ORDER BY CAST(SUBSTRING(id, 2) AS UNSIGNED) DESC LIMIT 1";
            try (PreparedStatement maxStmt = conn.prepareStatement(maxIdSql)) {
                ResultSet maxRs = maxStmt.executeQuery();
                if (maxRs.next()) {
                    String highestId = maxRs.getString("id");
                    if (highestId != null && highestId.startsWith("B")) {
                        try {
                            int idNum = Integer.parseInt(highestId.substring(1));
                            maxId = Math.max(maxId, idNum);
                        } catch (NumberFormatException e) {
                            // Ignore if ID format is unexpected
                        }
                    }
                }
            }
            
            // Sync bCounter with highest existing ID
            DataStore.bCounter.set(maxId);
            
            // Load bills
            String sql = "SELECT id, patient_id, total, created_at, paid, payment_method FROM bills";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                DataStore.bills.clear(); // Clear existing data
                while (rs.next()) {
                    String id = rs.getString("id");
                    String patientId = rs.getString("patient_id");
                    double amount = rs.getDouble("total");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    boolean isPaid = rs.getBoolean("paid");
                    String paymentMethod = rs.getString("payment_method");
                    
                    Bill bill = new Bill(id, patientId, amount, createdAt);
                    bill.paid = isPaid;
                    bill.paymentMethod = paymentMethod != null ? PaymentMethod.valueOf(paymentMethod) : null;
                    
                    DataStore.bills.put(id, bill);
                    LogManager.log("bill_db_load " + id);
                }
                System.out.println("Loaded " + DataStore.bills.size() + " bills from database");
            }
        } catch (SQLException e) {
            System.err.println("Error loading bills from database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading bills: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
