package hpms.ui.nurse;

import hpms.auth.AuthSession;
import hpms.model.*;
import hpms.service.PatientService;
import hpms.service.PatientStatusService;
import hpms.ui.components.CardPanel;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;
import hpms.util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Locale;

public class NurseDashboardPanel extends JPanel {
    private final AuthSession session;
    private JTable inpatientsTable;
    private JLabel statsLabel;
    private JTextField bpField;
    private JTextField hrField;
    private JTextField spo2Field;
    private JTextField heightField;
    private JTextField weightField;
    private JComboBox<String> statusCombo;

    public NurseDashboardPanel(AuthSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Theme.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(SectionHeader.info("Nurse Dashboard", "Quick vitals and status updates"), BorderLayout.WEST);
        statsLabel = new JLabel("");
        statsLabel.setFont(Theme.SMALL_FONT);
        header.add(statsLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);

        CardPanel left = new CardPanel();
        left.setLayout(new BorderLayout(8, 8));
        JLabel leftTitle = new JLabel("Inpatients");
        leftTitle.setFont(Theme.HEADING_4);
        leftTitle.setForeground(Theme.TEXT);
        left.add(leftTitle, BorderLayout.NORTH);
        inpatientsTable = new JTable();
        inpatientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inpatientsTable.setRowHeight(26);
        JScrollPane sp = new JScrollPane(inpatientsTable);
        Theme.styleScrollPane(sp);
        left.add(sp, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton viewAttachmentsBtn = new JButton("View Attachments");
        viewAttachmentsBtn.setBackground(new Color(59, 130, 246));
        viewAttachmentsBtn.setForeground(Color.WHITE);
        viewAttachmentsBtn.setFocusPainted(false);
        viewAttachmentsBtn.addActionListener(e -> viewPatientAttachments());
        buttonPanel.add(viewAttachmentsBtn);
        left.add(buttonPanel, BorderLayout.SOUTH);
        split.setLeftComponent(left);

        CardPanel right = new CardPanel();
        right.setLayout(new GridBagLayout());
        JLabel rightTitle = new JLabel("Quick Vitals & Status");
        rightTitle.setFont(Theme.HEADING_4);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.anchor = GridBagConstraints.WEST; gc.insets = new Insets(0,0,8,0);
        right.add(rightTitle, gc);

        bpField = new JTextField(12);
        hrField = new JTextField(12);
        spo2Field = new JTextField(12);
        heightField = new JTextField(8);
        weightField = new JTextField(8);
        statusCombo = new JComboBox<>(new String[]{"OUTPATIENT","INPATIENT","EMERGENCY","DISCHARGED"});
        Theme.styleTextField(bpField);
        Theme.styleTextField(hrField);
        Theme.styleTextField(spo2Field);
        Theme.styleTextField(heightField);
        Theme.styleTextField(weightField);
        Theme.styleComboBox(statusCombo);
        JButton saveVitals = new JButton("Save Vitals");
        JButton updateStatus = new JButton("Update Status");
        Theme.styleSuccess(saveVitals);
        Theme.stylePrimary(updateStatus);

        gc.gridwidth = 1; gc.gridy = 1; gc.insets = new Insets(4,0,4,8);
        right.add(new JLabel("Blood Pressure"), gc);
        gc.gridx = 1; right.add(bpField, gc);
        gc.gridx = 0; gc.gridy = 2; right.add(new JLabel("Heart Rate"), gc);
        gc.gridx = 1; right.add(hrField, gc);
        gc.gridx = 0; gc.gridy = 3; right.add(new JLabel("SpO2"), gc);
        gc.gridx = 1; right.add(spo2Field, gc);
        gc.gridx = 0; gc.gridy = 4; right.add(new JLabel("Height (cm)"), gc);
        gc.gridx = 1; right.add(heightField, gc);
        gc.gridx = 0; gc.gridy = 5; right.add(new JLabel("Weight (kg)"), gc);
        gc.gridx = 1; right.add(weightField, gc);
        gc.gridx = 0; gc.gridy = 6; right.add(new JLabel("Patient Status"), gc);
        gc.gridx = 1; right.add(statusCombo, gc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(saveVitals);
        actions.add(updateStatus);
        gc.gridx = 0; gc.gridy = 7; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(12,0,0,0);
        right.add(actions, gc);

        split.setRightComponent(right);
        add(split, BorderLayout.CENTER);

        saveVitals.addActionListener(e -> onSaveVitals());
        updateStatus.addActionListener(e -> onUpdateStatus());

        refresh();
        this.addHierarchyListener(evt -> {
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) SwingUtilities.invokeLater(this::refresh);
            }
        });
    }

    private String getSelectedPatientId() {
        int i = inpatientsTable.getSelectedRow();
        if (i < 0) return null;
        return String.valueOf(inpatientsTable.getValueAt(i, 0));
    }

    private void onSaveVitals() {
        String pid = getSelectedPatientId();
        if (pid == null) { JOptionPane.showMessageDialog(this, "Select a patient in Inpatients"); return; }
        String bp = bpField.getText().trim();
        String hr = hrField.getText().trim();
        String spo2 = spo2Field.getText().trim();
        String h = heightField.getText().trim();
        String w = weightField.getText().trim();
        java.util.List<String> out = PatientService.addClinicalInfo(pid, h, w, bp, "Vitals updated: HR=" + hr + ", SpO2=" + spo2, session.userId,
                null, null, null,
                null, null, null,
                null, null, null,
                null, null, null,
                null);
        JOptionPane.showMessageDialog(this, String.join("\n", out));
        refresh();
    }

    private void onUpdateStatus() {
        String pid = getSelectedPatientId();
        if (pid == null) { JOptionPane.showMessageDialog(this, "Select a patient in Inpatients"); return; }
        String st = String.valueOf(statusCombo.getSelectedItem());
        java.util.List<String> out = PatientStatusService.setStatus(pid, st, session.userId, "Updated by " + session.fullName);
        JOptionPane.showMessageDialog(this, String.join("\n", out));
        refresh();
    }

    public void refresh() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"Patient ID","Name","Room","Status","BP","HR","SpO2"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        int inpatients = 0;
        int totalPatients = 0;
        
        // Show all patients, not just those in rooms
        for (Patient p : DataStore.patients.values()) {
            totalPatients++;
            String patientId = p.id;
            String name = p.name;
            String room = "N/A";
            PatientStatus status = DataStore.patientStatus.getOrDefault(patientId, PatientStatus.INPATIENT);
            
            // Find room if patient is in one
            for (Room r : DataStore.rooms.values()) {
                if (r.status == RoomStatus.OCCUPIED && r.occupantPatientId != null && r.occupantPatientId.equals(patientId)) {
                    room = r.id;
                    inpatients++;
                    break;
                }
            }
            
            String bp = p.bloodPressure != null ? p.bloodPressure : "";
            String hr = p.initialHr != null ? p.initialHr : "";
            String sp = p.initialSpo2 != null ? p.initialSpo2 : "";
            
            m.addRow(new Object[]{patientId, name, room, status, bp, hr, sp});
        }
        
        inpatientsTable.setModel(m);
        Theme.styleTable(inpatientsTable);
        inpatientsTable.setRowSorter(new TableRowSorter<>(m));
        inpatientsTable.setDefaultRenderer(Object.class, new javax.swing.table.TableCellRenderer() {
            private final DefaultTableCellRenderer d = new DefaultTableCellRenderer();
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = d.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (comp instanceof JComponent) ((JComponent)comp).setOpaque(true);
                if (sel) { comp.setBackground(Theme.SELECTED_BACKGROUND); comp.setForeground(Theme.SELECTED_FOREGROUND); }
                else { comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(245,246,248)); comp.setForeground(Theme.FOREGROUND); }
                return comp;
            }
        });
        statsLabel.setText(String.format(Locale.US, "Total Patients: %d | Inpatients: %d", totalPatients, inpatients));
    }

    private void viewPatientAttachments() {
        // Get selected patient from inpatients table
        int selectedRow = inpatientsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the patient list", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patientId = String.valueOf(inpatientsTable.getValueAt(selectedRow, 0));
        if (patientId == null || patientId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid patient selection", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get patient name for dialog title
        Patient patient = DataStore.patients.get(patientId);
        String patientName = patient != null ? patient.name : "Unknown";

        // Create attachment dialog
        JDialog attachmentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Medical Documents - " + patientName + " (" + patientId + ")", true);
        attachmentDialog.setLayout(new BorderLayout(10, 10));
        attachmentDialog.setSize(900, 600);
        attachmentDialog.setLocationRelativeTo(this);

        // Create medical document folder panel
        hpms.ui.components.MedicalDocumentFolderPanel folderPanel = 
            new hpms.ui.components.MedicalDocumentFolderPanel(patientId);

        // Remove upload functionality for nurses (view-only)
        folderPanel.getUploadButton().setVisible(false);

        attachmentDialog.add(folderPanel, BorderLayout.CENTER);

        // Close button
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> attachmentDialog.dispose());
        closePanel.add(closeBtn);
        attachmentDialog.add(closePanel, BorderLayout.SOUTH);

        attachmentDialog.setVisible(true);
    }
}

