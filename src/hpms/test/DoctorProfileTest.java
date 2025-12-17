package hpms.test;

import hpms.model.Staff;
import hpms.model.StaffRole;
import hpms.service.DoctorService;
import hpms.ui.doctor.DoctorListDialog;
import hpms.ui.doctor.DoctorPublicProfilePanel;
import hpms.util.DataStore;

import javax.swing.*;
import java.awt.*;

/**
 * Test class for doctor profile functionality
 */
public class DoctorProfileTest extends JFrame {
    
    public DoctorProfileTest() {
        setTitle("Doctor Profile Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create test data if needed
        createTestData();
        
        // Create test UI
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton viewListBtn = new JButton("View Doctor List");
        viewListBtn.addActionListener(e -> DoctorListDialog.showDialog(this));
        
        JButton viewSingleBtn = new JButton("View First Doctor");
        viewSingleBtn.addActionListener(e -> {
            Staff firstDoctor = DoctorService.getAllDoctors().stream().findFirst().orElse(null);
            if (firstDoctor != null) {
                DoctorPublicProfilePanel.showDialog(this, firstDoctor);
            } else {
                JOptionPane.showMessageDialog(this, "No doctors found!");
            }
        });
        
        JButton addTestDoctorBtn = new JButton("Add Test Doctor");
        addTestDoctorBtn.addActionListener(e -> addTestDoctor());
        
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.add(viewListBtn);
        buttonPanel.add(viewSingleBtn);
        buttonPanel.add(addTestDoctorBtn);
        
        panel.add(buttonPanel);
        add(panel);
    }
    
    private void createTestData() {
        // Only create test data if no doctors exist
        if (DoctorService.getAllDoctors().isEmpty()) {
            addTestDoctor();
        }
    }
    
    private void addTestDoctor() {
        Staff doctor = new Staff();
        doctor.id = "D" + (DataStore.staff.size() + 1);
        doctor.name = "John Smith";
        doctor.role = StaffRole.DOCTOR;
        doctor.department = "Cardiology";
        doctor.specialty = "Cardiologist";
        doctor.subSpecialization = "Interventional Cardiology";
        doctor.yearsExperience = 10;
        doctor.qualifications = "MD, FACC";
        doctor.phone = "(555) 123-4567";
        doctor.email = "john.smith@example.com";
        doctor.bio = "Dr. Smith is a board-certified cardiologist with over 10 years of experience. " +
                   "He specializes in interventional cardiology and has performed over 1,000 procedures. " +
                   "Dr. Smith is dedicated to providing compassionate care to his patients.";
        
        DataStore.staff.put(doctor.id, doctor);
        JOptionPane.showMessageDialog(this, "Added test doctor: Dr. " + doctor.name);
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run the test
        SwingUtilities.invokeLater(() -> {
            DoctorProfileTest test = new DoctorProfileTest();
            test.setVisible(true);
        });
    }
}
