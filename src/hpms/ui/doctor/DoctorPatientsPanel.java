package hpms.ui.doctor;

import hpms.auth.AuthSession;
import hpms.util.DataStore;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import hpms.model.Patient;
import hpms.model.StaffNote;
import java.time.LocalDateTime;
import hpms.service.AppointmentService;

/**
 * Doctor Patients Panel - view assigned patients
 */
public class DoctorPatientsPanel extends JPanel {
    private AuthSession session;
    private JTable patientsTable;

    public DoctorPatientsPanel(AuthSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("My Patients");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(31, 41, 55));
        add(title, BorderLayout.NORTH);

        // Table
        patientsTable = new JTable();
        patientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientsTable.setRowHeight(28);
        refreshPatientsTable();

        JScrollPane scroll = new JScrollPane(patientsTable);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));
        add(scroll, BorderLayout.CENTER);

        // Bottom action panel
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBackground(new Color(248, 249, 250));
        actions.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));

        JButton viewBtn = new JButton("View Details");
        viewBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        viewBtn.setBackground(new Color(47, 111, 237));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFocusPainted(false);
        viewBtn.addActionListener(e -> openPatientDetails());

        JButton notesBtn = new JButton("Add Clinical Notes");
        notesBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        notesBtn.setBackground(new Color(107, 114, 128));
        notesBtn.setForeground(Color.WHITE);
        notesBtn.setFocusPainted(false);
        notesBtn.addActionListener(e -> addClinicalNote());

        JButton uploadBtn = new JButton("Upload Attachment");
        uploadBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        uploadBtn.setBackground(new Color(31, 41, 55));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setFocusPainted(false);
        uploadBtn.addActionListener(e -> uploadAttachment());

        JButton scheduleBtn = new JButton("Schedule Patient");
        scheduleBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        scheduleBtn.setBackground(new Color(34, 197, 94));
        scheduleBtn.setForeground(Color.WHITE);
        scheduleBtn.setFocusPainted(false);
        scheduleBtn.addActionListener(e -> scheduleSelectedPatient());

        JButton removeBtn = new JButton("Remove Patient");
        removeBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        removeBtn.setBackground(new Color(220, 38, 38));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setFocusPainted(false);
        removeBtn.addActionListener(e -> removeSelectedPatient());

        actions.add(viewBtn);
        actions.add(notesBtn);
        actions.add(uploadBtn);
        actions.add(scheduleBtn);
        actions.add(removeBtn);
        add(actions, BorderLayout.SOUTH);
    }

    private void refreshPatientsTable() {
        // Get unique patients from appointments for this doctor
        java.util.Set<String> patientIds = new java.util.LinkedHashSet<>();

        for (hpms.model.Appointment a : DataStore.appointments.values()) {
            if (session.userId.equals(a.staffId)) {
                patientIds.add(a.patientId);
            }
        }

        String[] columns = { "Patient ID", "Name", "Age", "Gender", "Contact", "Conditions" };
        Object[][] data = new Object[patientIds.size()][6];

        int i = 0;
        for (String pId : patientIds) {
            hpms.model.Patient p = DataStore.patients.get(pId);
            if (p != null) {
                data[i][0] = p.id;
                data[i][1] = p.name;
                data[i][2] = p.age;
                data[i][3] = p.gender != null ? p.gender.name() : "";
                data[i][4] = p.contact;
                data[i][5] = p.pastMedicalHistory != null ? p.pastMedicalHistory : "-";
                i++;
            }
        }

        patientsTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    public void refresh() {
        refreshPatientsTable();
    }

    private String getSelectedPatientId() {
        int row = patientsTable.getSelectedRow();
        if (row < 0)
            return null;
        Object val = patientsTable.getModel().getValueAt(row, 0);
        return val != null ? val.toString() : null;
    }

    private void openPatientDetails() {
        String pid = getSelectedPatientId();
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Select a patient first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Patient p = hpms.util.DataStore.patients.get(pid);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Patient not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new hpms.ui.PatientDetailsDialog(SwingUtilities.getWindowAncestor(this), p).setVisible(true);
    }

    private void addClinicalNote() {
        String pid = getSelectedPatientId();
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Select a patient first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextArea area = new JTextArea(5, 30);
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Clinical Note", new JScrollPane(area) }, "Add Note",
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            String text = area.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Note cannot be empty", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            hpms.util.DataStore.staffNotes.computeIfAbsent(pid, k -> new java.util.ArrayList<>())
                    .add(new StaffNote(session.userId, text, LocalDateTime.now()));
            Patient p = hpms.util.DataStore.patients.get(pid);
            if (p != null)
                p.progressNotes.add("Dr " + session.fullName + ": " + text);
            JOptionPane.showMessageDialog(this, "Note added", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void uploadAttachment() {
        String pid = getSelectedPatientId();
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Select a patient first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(
                new FileNameExtensionFilter("Documents & Images", "pdf", "doc", "docx", "jpg", "jpeg", "png"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                Patient p = hpms.util.DataStore.patients.get(pid);
                if (p != null) {
                    p.attachmentPaths.add(f.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Attachment uploaded", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    private void scheduleSelectedPatient() {
        String pid = getSelectedPatientId();
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Select a patient first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField date = new JTextField(java.time.LocalDate.now().plusDays(1).toString());
        JTextField time = new JTextField(java.time.LocalTime.of(9, 0).toString());
        JComboBox<String> dept = new JComboBox<>(hpms.util.DataStore.departments.toArray(new String[0]));
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Date", date, "Time", time, "Department", dept },
                "Schedule Appointment", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            java.util.List<String> out = AppointmentService.schedule(pid, session.userId, date.getText(),
                    time.getText(), String.valueOf(dept.getSelectedItem()));
            if (!out.isEmpty() && out.get(0).startsWith("Error:")) {
                JOptionPane.showMessageDialog(this, out.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, String.join("\n", out), "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void removeSelectedPatient() {
        String pid = getSelectedPatientId();
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Select a patient first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Patient p = hpms.util.DataStore.patients.get(pid);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Patient not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm removal
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove \"" + p.name + "\" from your patient list?\n\n" +
                        "This will:\n" +
                        "- Remove all appointments with this patient\n" +
                        "- Clear the patient's insurance information\n\n" +
                        "This action cannot be undone.",
                "Confirm Patient Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Call service to remove patient
        java.util.List<String> result = hpms.service.PatientService.removePatientFromDoctor(pid, session.userId);

        if (!result.isEmpty() && result.get(0).startsWith("Error:")) {
            JOptionPane.showMessageDialog(this, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result.get(0), "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshPatientsTable(); // Refresh the table to remove the patient
        }
    }
}
