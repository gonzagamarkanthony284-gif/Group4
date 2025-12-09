package hpms.service;

import hpms.model.*;
import hpms.util.*;

import java.time.LocalDateTime;
import java.util.*;

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
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
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
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
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
        try {
            BackupUtil.saveToDefault();
        } catch (Exception ex) {
        }
        out.add("Payment successful");
        return out;
    }
}
