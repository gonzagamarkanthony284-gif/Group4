package hpms.test;

import hpms.model.Patient;
import hpms.model.Gender;
import hpms.ui.panels.PatientsPanel;
import hpms.util.DataStore;

import javax.swing.*;
import java.time.LocalDateTime;

public class GuiSmokeTest {
    public static void main(String[] args) {
        // seed DataStore with a simple patient so the UI has something to show
        Patient p = new Patient("PTEST", "Smoke Tester", 33, "1990-01-01", Gender.Male, "555-0101", "123 Test St",
                LocalDateTime.now());
        DataStore.patients.put(p.id, p);

        System.out.println("Seeded patient: " + p.id + " - " + p.name);

        SwingUtilities.invokeLater(() -> {
            try {
                PatientsPanel panel = new PatientsPanel();
                // exercise refresh (should not throw)
                panel.refresh();
                System.out.println("PatientsPanel.refresh() ran successfully.");
                // small check: DataStore has our patient
                if (DataStore.patients.containsKey("PTEST"))
                    System.out.println("Smoke test success: patient present in DataStore");
                else
                    System.err.println("Smoke test failure: patient missing from DataStore");
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
            System.exit(0);
        });
    }
}
