package hpms.auth;

import hpms.service.*;
import hpms.util.*;

import java.util.List;

public class Verifier {
    public static void main(String[] args) {
        AuthService.seedAdmin();
        hpms.ui.LoginWindow.seedRooms();
        System.out.println(step("login", AuthService.login("admin", "admin123")));
        new hpms.ui.MainGUI();
        List<String> p = PatientService.add("John Doe", "35", "1990-01-01", "M", "555-0001", "City", "OUTPATIENT");
        System.out.println(step("create_patient", p));
        String pid = DataStore.patients.values().stream().findFirst().get().id;
        StaffService.add("Dr Smith", "DOCTOR", "Cardiology");
        String sid = DataStore.staff.values().stream().findFirst().get().id;
        System.out.println(step("schedule_appointment",
                AppointmentService.schedule(pid, sid, "2025-12-01", "14:30", "Cardiology")));
        System.out.println(step("create_bill", BillingService.create(pid, "100")));
        System.out.println(step("assign_room", RoomService.assign(DataStore.rooms.keySet().iterator().next(), pid)));
        String backup = BackupUtil.toJson();
        System.out.println(backup != null && backup.startsWith("{") ? "backup: success" : "backup: fail");
        System.out.println(DataStore.activityLog.isEmpty() ? "activity_log: fail" : "activity_log: success");
        System.out.println(step("logout", AuthService.logout()));
    }

    static String step(String name, List<String> out) {
        return name + ": " + (out.size() > 0 && !out.get(0).startsWith("Error") ? "success" : "fail") + " -> "
                + String.join("; ", out);
    }
}
