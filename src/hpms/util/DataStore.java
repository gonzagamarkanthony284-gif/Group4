package hpms.util;

import hpms.model.*;
import hpms.auth.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataStore {
    public static final Map<String, Patient> patients = new LinkedHashMap<>();
    public static final Map<String, Staff> staff = new LinkedHashMap<>();
    public static final Map<String, Appointment> appointments = new LinkedHashMap<>();
    public static final Map<String, Bill> bills = new LinkedHashMap<>();
    public static final Map<String, Room> rooms = new LinkedHashMap<>();
    public static final Map<String, User> users = new LinkedHashMap<>();
    public static final Map<String, hpms.model.LabTestRequest> labTests = new LinkedHashMap<>();
    public static final Map<String, hpms.model.LabResult> labResults = new LinkedHashMap<>();
    public static final Map<String, hpms.model.Service> services = new LinkedHashMap<>();
    public static final Map<String, hpms.model.LeaveRequest> leaveRequests = new LinkedHashMap<>();
    // Medicine functionality removed
    public static final Map<String, hpms.model.PatientStatus> patientStatus = new LinkedHashMap<>();
    public static final Map<String, java.util.List<hpms.model.StatusHistoryEntry>> statusHistory = new LinkedHashMap<>();
    public static final Map<String, java.util.List<hpms.model.StaffNote>> staffNotes = new LinkedHashMap<>();
    public static final Map<String, java.util.List<String>> criticalAlerts = new LinkedHashMap<>();

    // NEW: Prescription, Lab Test Types (Medicine removed)
    public static final Map<String, hpms.model.Prescription> prescriptions = new LinkedHashMap<>();
    public static final Map<String, hpms.model.LabTestType> labTestTypes = new LinkedHashMap<>();
    public static final Map<String, hpms.model.Discharge> discharges = new LinkedHashMap<>();
    public static final Map<String, hpms.model.Communication> communications = new LinkedHashMap<>();

    public static final List<String> activityLog = new ArrayList<>();
    public static final Set<String> departments = new LinkedHashSet<>(Arrays.asList("Cardiology", "Neurology",
            "Orthopedics", "Pediatrics", "Oncology", "ER", "Admin", "Nursing", "Billing", "Reception"));
    public static final java.util.EnumSet<hpms.model.PaymentMethod> allowedPaymentMethods = java.util.EnumSet
            .of(hpms.model.PaymentMethod.CASH, hpms.model.PaymentMethod.INSURANCE);

    public static final AtomicInteger pCounter = new AtomicInteger(1000);
    public static final AtomicInteger sCounter = new AtomicInteger(2000);
    public static final AtomicInteger aCounter = new AtomicInteger(3000);
    public static final AtomicInteger bCounter = new AtomicInteger(4000);
    public static final AtomicInteger rCounter = new AtomicInteger(5000);
    public static final java.util.concurrent.atomic.AtomicInteger lCounter = new java.util.concurrent.atomic.AtomicInteger(
            6000);
    public static final AtomicInteger prCounter = new AtomicInteger(7000); // Prescription counter
    // Medicine counter removed
    public static final AtomicInteger dischargeCounter = new AtomicInteger(9000); // Discharge counter
    public static final AtomicInteger commCounter = new AtomicInteger(10000); // Communication counter

    public static void log(String message) {
        activityLog.add(LocalDateTime.now() + " " + message);
    }
}
