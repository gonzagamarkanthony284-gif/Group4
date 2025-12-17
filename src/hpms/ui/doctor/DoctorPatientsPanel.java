package hpms.ui.doctor;

import hpms.auth.AuthSession;
import hpms.util.DataStore;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import hpms.model.Patient;
import hpms.model.StaffNote;
import java.time.LocalDateTime;
import hpms.service.AppointmentService;
import hpms.service.CommunicationService;
import hpms.service.PatientService;
import hpms.ui.dialogs.PatientDetailsDialog;
import java.util.Locale;

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

        JButton criticalBtn = new JButton("Critical Info");
        criticalBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        criticalBtn.setBackground(new Color(220, 38, 38));
        criticalBtn.setForeground(Color.WHITE);
        criticalBtn.setFocusPainted(false);
        criticalBtn.addActionListener(e -> addCriticalInfo());

        JButton clinicalInfoBtn = new JButton("Clinical Info");
        clinicalInfoBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        clinicalInfoBtn.setBackground(new Color(107, 114, 128));
        clinicalInfoBtn.setForeground(Color.WHITE);
        clinicalInfoBtn.setFocusPainted(false);
        clinicalInfoBtn.addActionListener(e -> openClinicalInfo());

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
        actions.add(criticalBtn);
        actions.add(clinicalInfoBtn);
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

        String[] columns = { "Patient ID", "Name", "Age", "Gender", "Contact", "Critical Info", "Conditions", "Latest Note" };
        Object[][] data = new Object[patientIds.size()][8];

        int i = 0;
        for (String pId : patientIds) {
            hpms.model.Patient p = DataStore.patients.get(pId);
            if (p != null) {
                data[i][0] = p.id;
                data[i][1] = p.name;
                data[i][2] = p.age;
                data[i][3] = p.gender != null ? p.gender.name() : "";
                data[i][4] = p.contact;
                data[i][5] = latestCriticalInfoPreview(pId);
                data[i][6] = p.pastMedicalHistory != null ? p.pastMedicalHistory : "-";
                data[i][7] = latestClinicalNotePreview(pId);
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

    private String latestClinicalNotePreview(String patientId) {
        if (patientId == null)
            return "";

        java.util.List<StaffNote> notes = hpms.util.DataStore.staffNotes.get(patientId);
        if (notes != null && !notes.isEmpty()) {
            StaffNote last = notes.get(notes.size() - 1);
            if (last != null && last.text != null) {
                return shorten(last.text, 60);
            }
        }

        Patient p = hpms.util.DataStore.patients.get(patientId);
        if (p != null && p.progressNotes != null && !p.progressNotes.isEmpty()) {
            String last = p.progressNotes.get(p.progressNotes.size() - 1);
            if (last != null) {
                return shorten(last, 60);
            }
        }

        return "";
    }

    private String latestCriticalInfoPreview(String patientId) {
        if (patientId == null)
            return "";
        java.util.List<String> alerts = CommunicationService.alerts(patientId);
        if (alerts != null && !alerts.isEmpty()) {
            String last = alerts.get(alerts.size() - 1);
            if (last != null) {
                return shorten(last, 60);
            }
        }
        return "";
    }

    private String shorten(String text, int max) {
        if (text == null)
            return "";
        String t = text.trim().replace("\n", " ");
        if (t.length() <= max)
            return t;
        return t.substring(0, Math.max(0, max - 3)) + "...";
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
        new PatientDetailsDialog(SwingUtilities.getWindowAncestor(this), p).setVisible(true);
    }

    private void addCriticalInfo() {
        String pid = getSelectedPatientId();
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Select a patient first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextArea area = new JTextArea(5, 30);
        int r = JOptionPane.showConfirmDialog(this, new Object[] { "Critical Note", new JScrollPane(area) }, "Add Critical Info",
                JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            String text = area.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Note cannot be empty", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            java.util.List<String> out = CommunicationService.addAlert(pid, text);
            refreshPatientsTable();
            if (out != null && !out.isEmpty() && out.get(0).startsWith("Error")) {
                JOptionPane.showMessageDialog(this, String.join("\n", out), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Critical info saved", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void openClinicalInfo() {
        String pid = getSelectedPatientId();
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Select a patient first", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        showClinicalInfoDialogFor(pid);
        refreshPatientsTable();
    }

    private void showClinicalInfoDialogFor(String id) {
        if (id == null)
            return;
        Patient p = DataStore.patients.get(id);
        if (p == null)
            return;

        JTextField height = new JTextField(p.heightCm == null ? "" : String.valueOf(p.heightCm));
        JTextField weight = new JTextField(p.weightKg == null ? "" : String.valueOf(p.weightKg));
        JTextField bp = new JTextField(p.bloodPressure == null ? "" : p.bloodPressure);
        JTextArea note = new JTextArea(4, 40);

        // --- X-RAY upload / summary ---
        JPanel xrayPanel = new JPanel(new GridBagLayout());
        xrayPanel.setOpaque(false);
        GridBagConstraints xc = new GridBagConstraints();
        xc.insets = new Insets(4, 4, 4, 4);
        xc.anchor = GridBagConstraints.WEST;
        xc.fill = GridBagConstraints.HORIZONTAL;
        xc.weightx = 1.0;
        JTextField xrayPath = new JTextField(p.xrayFilePath == null ? "" : p.xrayFilePath);
        xrayPath.setEditable(false);
        xrayPath.setPreferredSize(new Dimension(420, 24));
        JTextArea xNotes = new JTextArea(p.xraySummary == null ? "" : p.xraySummary, 3, 60);
        xNotes.setLineWrap(true);
        xNotes.setWrapStyleWord(true);
        JButton xrayBrowse = new JButton("Browse...");
        JComboBox<String> xrayStatus = new JComboBox<>(
                new String[] { p.xrayStatus == null ? "Not Uploaded" : p.xrayStatus, "Not Uploaded", "Uploaded",
                        "Reviewed", "Critical" });

        // --- Stool upload / summary ---
        JPanel stoolPanel = new JPanel(new GridBagLayout());
        stoolPanel.setOpaque(false);
        GridBagConstraints sc = new GridBagConstraints();
        sc.insets = new Insets(4, 4, 4, 4);
        sc.anchor = GridBagConstraints.WEST;
        sc.fill = GridBagConstraints.HORIZONTAL;
        sc.weightx = 1.0;
        JTextField stoolPath = new JTextField(p.stoolFilePath == null ? "" : p.stoolFilePath);
        stoolPath.setEditable(false);
        stoolPath.setPreferredSize(new Dimension(420, 24));
        JTextArea stoolNotes = new JTextArea(p.stoolSummary == null ? "" : p.stoolSummary, 3, 60);
        stoolNotes.setLineWrap(true);
        stoolNotes.setWrapStyleWord(true);
        JButton stoolBrowse = new JButton("Browse...");
        JComboBox<String> stoolStatus = new JComboBox<>(
                new String[] { p.stoolStatus == null ? "Not Uploaded" : p.stoolStatus, "Not Uploaded", "Uploaded",
                        "Reviewed", "Critical" });

        // --- Urinalysis upload / summary ---
        JPanel urinePanel = new JPanel(new GridBagLayout());
        urinePanel.setOpaque(false);
        GridBagConstraints uc = new GridBagConstraints();
        uc.insets = new Insets(4, 4, 4, 4);
        uc.anchor = GridBagConstraints.WEST;
        uc.fill = GridBagConstraints.HORIZONTAL;
        uc.weightx = 1.0;
        JTextField urinePath = new JTextField(p.urineFilePath == null ? "" : p.urineFilePath);
        urinePath.setEditable(false);
        urinePath.setPreferredSize(new Dimension(420, 24));
        JTextArea urineNotes = new JTextArea(p.urineSummary == null ? "" : p.urineSummary, 3, 60);
        urineNotes.setLineWrap(true);
        urineNotes.setWrapStyleWord(true);
        JButton urineBrowse = new JButton("Browse...");
        JComboBox<String> urineStatus = new JComboBox<>(
                new String[] { p.urineStatus == null ? "Not Uploaded" : p.urineStatus, "Not Uploaded", "Uploaded",
                        "Reviewed", "Critical" });

        // --- Blood test upload / summary ---
        JPanel bloodPanel = new JPanel(new GridBagLayout());
        bloodPanel.setOpaque(false);
        GridBagConstraints bc = new GridBagConstraints();
        bc.insets = new Insets(4, 4, 4, 4);
        bc.anchor = GridBagConstraints.WEST;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.weightx = 1.0;
        JTextField bloodPath = new JTextField(p.bloodFilePath == null ? "" : p.bloodFilePath);
        bloodPath.setEditable(false);
        bloodPath.setPreferredSize(new Dimension(420, 24));
        JTextArea bloodNotes = new JTextArea(p.bloodSummary == null ? "" : p.bloodSummary, 3, 60);
        bloodNotes.setLineWrap(true);
        bloodNotes.setWrapStyleWord(true);
        JButton bloodBrowse = new JButton("Browse...");
        JComboBox<String> bloodStatus = new JComboBox<>(
                new String[] { p.bloodStatus == null ? "Not Uploaded" : p.bloodStatus, "Not Uploaded", "Uploaded",
                        "Reviewed", "Critical" });

        // Basic layouts
        xc.gridy = 0;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("X-ray Upload"), xc);
        xc.gridy = 1;
        xrayPanel.add(xrayPath, xc);
        xc.gridx = 1;
        xrayPanel.add(xrayBrowse, xc);
        xc.gridy = 2;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("Status:"), xc);
        xc.gridx = 1;
        xrayPanel.add(xrayStatus, xc);
        xc.gridy = 3;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("Notes:"), xc);
        xc.gridx = 1;
        xrayPanel.add(new JScrollPane(xNotes), xc);

        sc.gridy = 0;
        sc.gridx = 0;
        stoolPanel.add(new JLabel("Stool Exam Upload"), sc);
        sc.gridy = 1;
        stoolPanel.add(stoolPath, sc);
        sc.gridx = 1;
        stoolPanel.add(stoolBrowse, sc);
        sc.gridy = 2;
        sc.gridx = 0;
        stoolPanel.add(new JLabel("Status:"), sc);
        sc.gridx = 1;
        stoolPanel.add(stoolStatus, sc);
        sc.gridy = 3;
        sc.gridx = 0;
        stoolPanel.add(new JLabel("Notes:"), sc);
        sc.gridx = 1;
        stoolPanel.add(new JScrollPane(stoolNotes), sc);

        uc.gridy = 0;
        uc.gridx = 0;
        urinePanel.add(new JLabel("Urinalysis Upload"), uc);
        uc.gridy = 1;
        urinePanel.add(urinePath, uc);
        uc.gridx = 1;
        urinePanel.add(urineBrowse, uc);
        uc.gridy = 2;
        uc.gridx = 0;
        urinePanel.add(new JLabel("Status:"), uc);
        uc.gridx = 1;
        urinePanel.add(urineStatus, uc);
        uc.gridy = 3;
        uc.gridx = 0;
        urinePanel.add(new JLabel("Notes:"), uc);
        uc.gridx = 1;
        urinePanel.add(new JScrollPane(urineNotes), uc);

        bc.gridy = 0;
        bc.gridx = 0;
        bloodPanel.add(new JLabel("Blood Test Upload"), bc);
        bc.gridy = 1;
        bloodPanel.add(bloodPath, bc);
        bc.gridx = 1;
        bloodPanel.add(bloodBrowse, bc);
        bc.gridy = 2;
        bc.gridx = 0;
        bloodPanel.add(new JLabel("Status:"), bc);
        bc.gridx = 1;
        bloodPanel.add(bloodStatus, bc);
        bc.gridy = 3;
        bc.gridx = 0;
        bloodPanel.add(new JLabel("Notes:"), bc);
        bc.gridx = 1;
        bloodPanel.add(new JScrollPane(bloodNotes), bc);

        // Create main dialog container
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(250, 250, 250));

        JPanel topVitals = new JPanel(new GridLayout(1, 3, 12, 0));
        topVitals.setOpaque(false);

        JPanel col1 = new JPanel(new BorderLayout(2, 2));
        col1.setOpaque(false);
        col1.add(new JLabel("Height (cm)"), BorderLayout.NORTH);
        col1.add(height, BorderLayout.CENTER);
        JPanel col2 = new JPanel(new BorderLayout(2, 2));
        col2.setOpaque(false);
        col2.add(new JLabel("Weight (kg)"), BorderLayout.NORTH);
        col2.add(weight, BorderLayout.CENTER);
        JPanel col3 = new JPanel(new BorderLayout(2, 2));
        col3.setOpaque(false);
        col3.add(new JLabel("Blood Pressure"), BorderLayout.NORTH);
        col3.add(bp, BorderLayout.CENTER);
        topVitals.add(col1);
        topVitals.add(col2);
        topVitals.add(col3);

        main.add(topVitals);
        main.add(Box.createVerticalStrut(8));
        main.add(new JLabel("Progress Note"));
        JScrollPane noteSP = new JScrollPane(note);
        noteSP.setPreferredSize(new Dimension(860, 160));
        noteSP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        main.add(noteSP);
        main.add(Box.createVerticalStrut(10));

        JTabbedPane testsTabs = new JTabbedPane(JTabbedPane.TOP);
        testsTabs.addTab("X-ray", xrayPanel);
        testsTabs.addTab("Stool Exam", stoolPanel);
        testsTabs.addTab("Urinalysis", urinePanel);
        testsTabs.addTab("Blood Test", bloodPanel);
        main.add(testsTabs);

        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner, "Add Clinical Info to " + id, true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(main);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionBar.setOpaque(false);
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        actionBar.add(cancelBtn);
        actionBar.add(saveBtn);
        dlg.add(actionBar, BorderLayout.SOUTH);
        dlg.setPreferredSize(new Dimension(980, 820));
        dlg.setMinimumSize(new Dimension(860, 720));
        dlg.setResizable(true);

        java.util.function.BiConsumer<JTextField, String[]> browseInto = (field, exts) -> {
            JFileChooser fc = new JFileChooser();
            if (exts != null && exts.length > 0) {
                fc.setFileFilter(new FileNameExtensionFilter("Files", exts));
            }
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (f != null) field.setText(f.getAbsolutePath());
            }
        };

        xrayBrowse.addActionListener(ae -> browseInto.accept(xrayPath, new String[] { "jpg", "jpeg", "png", "dcm", "dicom" }));
        stoolBrowse.addActionListener(ae -> browseInto.accept(stoolPath, new String[] { "pdf", "jpg", "jpeg", "png" }));
        urineBrowse.addActionListener(ae -> browseInto.accept(urinePath, new String[] { "pdf", "jpg", "jpeg", "png" }));
        bloodBrowse.addActionListener(ae -> browseInto.accept(bloodPath, new String[] { "pdf", "jpg", "jpeg", "png" }));

        saveBtn.addActionListener(ae -> {
            java.util.List<String> otherAttachList = null;
            java.util.List<String> out = PatientService.addClinicalInfo(id, height.getText(), weight.getText(), bp.getText(),
                    note.getText(), session.userId,
                    xrayPath.getText().trim().isEmpty() ? null : xrayPath.getText().trim(),
                    String.valueOf(xrayStatus.getSelectedItem()), xNotes.getText().trim().isEmpty() ? null : xNotes.getText().trim(),
                    stoolPath.getText().trim().isEmpty() ? null : stoolPath.getText().trim(),
                    String.valueOf(stoolStatus.getSelectedItem()), stoolNotes.getText().trim().isEmpty() ? null : stoolNotes.getText().trim(),
                    urinePath.getText().trim().isEmpty() ? null : urinePath.getText().trim(),
                    String.valueOf(urineStatus.getSelectedItem()), urineNotes.getText().trim().isEmpty() ? null : urineNotes.getText().trim(),
                    bloodPath.getText().trim().isEmpty() ? null : bloodPath.getText().trim(),
                    String.valueOf(bloodStatus.getSelectedItem()), bloodNotes.getText().trim().isEmpty() ? null : bloodNotes.getText().trim(),
                    otherAttachList);
            if (out != null && !out.isEmpty() && out.get(0).startsWith("Error")) {
                JOptionPane.showMessageDialog(this, String.join("\n", out), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, String.join("\n", out), "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            }
        });
        cancelBtn.addActionListener(ae -> dlg.dispose());

        dlg.pack();
        dlg.setSize(Math.max(dlg.getWidth(), 980), Math.max(dlg.getHeight(), 820));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
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
