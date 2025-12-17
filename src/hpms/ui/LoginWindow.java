package hpms.ui;

import javax.swing.*;
import hpms.model.RoomStatus;
import hpms.model.Room;
import hpms.util.DataStore;
import hpms.util.IDGenerator;
import hpms.auth.AuthService;
import hpms.service.PatientService;
import hpms.service.PatientStatusService;
import hpms.service.DatabaseInitializer;
import hpms.service.StaffService;
import hpms.service.AppointmentService;
import hpms.service.BillingService;

public class LoginWindow extends JFrame {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Disabled backup loading to use database instead
            // try { BackupUtil.loadFromDefault(); } catch (Exception ex) { }
            try { AuthService.migratePasswordsIfMissing(); } catch (Exception ex) { }
            try { PatientService.migrateRegistrationTypeDefault(); } catch (Exception ex) { }
            if (!DataStore.users.containsKey("admin")) { AuthService.seedAdmin(); }
            seedRooms();
            // Load staff from database
            try { StaffService.loadFromDatabase(); } catch (Exception ex) { }
            // Load patients from database
            try { PatientService.loadFromDatabase(); } catch (Exception ex) { }
            // Initialize patient status table and load data
            try { DatabaseInitializer.initializePatientStatusTable(); } catch (Exception ex) { }
            try { PatientStatusService.loadFromDatabase(); } catch (Exception ex) { }
            // Load appointments from database
            try { AppointmentService.loadFromDatabase(); } catch (Exception ex) { }
            // Load bills from database
            try { BillingService.loadFromDatabase(); } catch (Exception ex) { }
            // Medicine functionality removed
            new hpms.ui.login.LoginWindow().setVisible(true);
        });
    }
    
    public LoginWindow() {
        hpms.ui.login.LoginWindow delegate = new hpms.ui.login.LoginWindow();
        delegate.setVisible(true);
        dispose();
    }

    public static void seedRooms() {
        if (!DataStore.rooms.isEmpty()) return;
        for (int i=0; i<10; i++) {
            String id = IDGenerator.nextId("R");
            DataStore.rooms.put(id, new Room(id, RoomStatus.VACANT, null));
        }
    }
}
