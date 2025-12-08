package hpms.ui;

import hpms.model.*;
import hpms.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Locale;
import java.util.List;
import java.awt.image.BufferedImage;

/**
 * Modern patient details dialog with clean UI/UX:
 * - Large patient photo on the left
 * - Patient info + doctor expertise clearly displayed on the right
 * - Full medical details in tabs below
 */
public class PatientDetailsDialogNew extends JDialog {
    public PatientDetailsDialogNew(Window owner, Patient p) {
        super(owner, "Patient Details - " + p.name, ModalityType.APPLICATION_MODAL);
        setSize(1000, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Header with close button
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 210)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        header.setBackground(new Color(245, 250, 255));

        JLabel title = new JLabel(p.name);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        JLabel idLbl = new JLabel("ID: " + p.id);
        idLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        idLbl.setForeground(new Color(90, 90, 90));

        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.add(title);
        headerLeft.add(Box.createHorizontalStrut(15));
        headerLeft.add(idLbl);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        header.add(headerLeft, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Main content: Two-column layout
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 20));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // LEFT COLUMN: Large patient photo
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBackground(Color.WHITE);
        photoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel photoLabel = new JLabel();
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setVerticalAlignment(SwingConstants.CENTER);
        photoLabel.setIcon(loadPatientPhoto(p.photoPath, 200, 280));
        photoPanel.add(photoLabel, BorderLayout.CENTER);
        mainContent.add(photoPanel);

        // RIGHT COLUMN: Patient info + doctor expertise
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Patient Information Section
        infoPanel.add(createSectionHeader("PATIENT INFORMATION"));
        infoPanel.add(createInfoRow("Name:", p.name, 14, true));
        infoPanel.add(createInfoRow("Age / Gender:", p.age + " / " + (p.gender == null ? "N/A" : p.gender.name()), 12,
                false));
        infoPanel.add(createInfoRow("Birthday:", p.birthday == null ? "N/A" : p.birthday, 11, false));
        infoPanel.add(createInfoRow("Contact:", p.contact == null ? "" : p.contact, 11, false));
        infoPanel.add(createInfoRow("Address:", p.address == null ? "" : p.address, 11, false));

        // Display patient type with permanent status indicator
        String typeDisplay = p.patientType;
        if (p.isOutpatientPermanent) {
            typeDisplay += " (PERMANENT - Cannot be changed)";
        }
        infoPanel.add(createInfoRow("Type:", typeDisplay, 11, false));
        infoPanel.add(Box.createVerticalStrut(15));

        // Vitals Section
        infoPanel.add(createSectionHeader("VITALS & HEALTH"));
        infoPanel.add(createInfoRow("Height/Weight:",
                (p.heightCm == null ? "N/A" : String.format("%.0f cm", p.heightCm)) + " / " +
                        (p.weightKg == null ? "N/A" : String.format("%.0f kg", p.weightKg)),
                11, false));

        Double bmi = p.getBmi();
        String bmiDisplay = "N/A";
        Color bmiColor = Color.BLACK;
        if (bmi != null) {
            String category;
            if (bmi < 18.5) {
                category = "Underweight";
                bmiColor = new Color(0x2E86C1);
            } else if (bmi < 25.0) {
                category = "Normal";
                bmiColor = new Color(0x27AE60);
            } else if (bmi < 30.0) {
                category = "Overweight";
                bmiColor = new Color(0xF39C12);
            } else {
                category = "Obese";
                bmiColor = new Color(0xE74C3C);
            }
            bmiDisplay = String.format("%.1f (%s)", bmi, category);
        }
        infoPanel.add(createInfoRowColored("BMI:", bmiDisplay, bmiColor, 11));
        infoPanel.add(Box.createVerticalStrut(15));

        // Assignment Section
        Room assignedRoom = null;
        for (Room r : DataStore.rooms.values())
            if (p.id.equals(r.occupantPatientId)) {
                assignedRoom = r;
                break;
            }
        infoPanel.add(createSectionHeader("ASSIGNMENT & DOCTOR"));
        infoPanel.add(createInfoRow("Room:", assignedRoom == null ? "Not assigned" : assignedRoom.id, 11, false));

        // Doctor Information with Expertise
        Appointment latestAppt = DataStore.appointments.values().stream()
                .filter(a -> a.patientId.equals(p.id))
                .max((a, b) -> a.dateTime.compareTo(b.dateTime))
                .orElse(null);

        final Room fAssignedRoom = assignedRoom;
        final Appointment fLatestAppt = latestAppt;

        if (fLatestAppt != null) {
            Staff doctor = DataStore.staff.get(fLatestAppt.staffId);
            if (doctor != null) {
                infoPanel.add(createInfoRow("Primary Doctor:", doctor.name, 12, true));

                String expertise = "-";
                if (doctor.specialty != null && !doctor.specialty.trim().isEmpty())
                    expertise = doctor.specialty;
                else if (doctor.subSpecialization != null && !doctor.subSpecialization.trim().isEmpty())
                    expertise = doctor.subSpecialization;
                else if (doctor.department != null && !doctor.department.trim().isEmpty())
                    expertise = doctor.department;

                infoPanel.add(createInfoRow("Expertise:", expertise, 11, false));
                infoPanel.add(createInfoRow("Last Visit:", fLatestAppt.dateTime.toLocalDate().toString(), 11, false));
            }
        } else {
            infoPanel.add(createInfoRow("Primary Doctor:", "None", 12, true));
        }

        infoPanel.add(Box.createVerticalGlue());
        mainContent.add(infoPanel);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.add(mainContent, BorderLayout.NORTH);

        // Medical details in tabbed pane below
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("Medical History", createMedicalPanel(p));
        tabs.addTab("Visits", createVisitsPanel(p));
        tabs.addTab("Insurance", createInsurancePanel(p));
        centerWrapper.add(tabs, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // Footer with actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton printBtn = new JButton("Print Summary");
        printBtn.addActionListener(e -> {
            JTextArea summary = new JTextArea(buildSummary(p, fAssignedRoom, fLatestAppt));
            summary.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(summary), "Patient Summary",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        footer.add(printBtn);
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);
        setVisible(true);
    }

    private JLabel createSectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        lbl.setForeground(new Color(0, 102, 102));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel createInfoRow(String label, String value, int fontSize, boolean bold) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, fontSize));
        lblField.setForeground(new Color(50, 50, 50));
        lblField.setPreferredSize(new Dimension(130, 20));

        JLabel valField = new JLabel(value);
        valField.setFont(new Font("Arial", Font.PLAIN, fontSize));
        valField.setForeground(new Color(80, 80, 80));

        panel.add(lblField);
        panel.add(valField);
        return panel;
    }

    private JPanel createInfoRowColored(String label, String value, Color valueColor, int fontSize) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Arial", Font.BOLD, fontSize));
        lblField.setForeground(new Color(50, 50, 50));
        lblField.setPreferredSize(new Dimension(130, 20));

        JLabel valField = new JLabel(value);
        valField.setFont(new Font("Arial", Font.PLAIN, fontSize));
        valField.setForeground(valueColor);

        panel.add(lblField);
        panel.add(valField);
        return panel;
    }

    private Icon loadPatientPhoto(String path, int w, int h) {
        try {
            if (path != null && !path.trim().isEmpty()) {
                ImageIcon icon = new ImageIcon(path);
                if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (Exception ignored) {
        }
        // Placeholder silhouette
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(235, 238, 242));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(180, 190, 200));
        g2.fillOval(w / 4, h / 8, w / 2, h / 2);
        g2.fillRoundRect(w / 4, h / 2, w / 2, h / 3, 30, 30);
        g2.dispose();
        return new ImageIcon(img);
    }

    private String buildSummary(Patient p, Room r, Appointment a) {
        StringBuilder sb = new StringBuilder();
        sb.append("PATIENT SUMMARY\n");
        sb.append("===============\n\n");
        sb.append("ID: ").append(p.id).append("\n");
        sb.append("Name: ").append(p.name).append("\n");
        sb.append("Age/Gender: ").append(p.age).append(" / ").append(p.gender).append("\n");
        sb.append("Birthday: ").append(p.birthday).append("\n");
        sb.append("Contact: ").append(p.contact).append("\n");
        sb.append("Address: ").append(p.address).append("\n");
        sb.append("Type: ").append(p.patientType);
        if (p.isOutpatientPermanent) {
            sb.append(" (PERMANENT - Cannot be changed)");
        }
        sb.append("\n\n");

        sb.append("VITALS\n");
        sb.append("------\n");
        sb.append("Height: ").append(p.heightCm == null ? "N/A" : p.heightCm).append(" cm\n");
        sb.append("Weight: ").append(p.weightKg == null ? "N/A" : p.weightKg).append(" kg\n");
        Double bmi = p.getBmi();
        if (bmi != null) {
            String cat;
            if (bmi < 18.5)
                cat = "Underweight";
            else if (bmi < 25.0)
                cat = "Normal";
            else if (bmi < 30.0)
                cat = "Overweight";
            else
                cat = "Obese";
            sb.append("BMI: ").append(String.format("%.2f (%s)", bmi, cat)).append("\n");
        } else {
            sb.append("BMI: N/A\n");
        }
        sb.append("\n");

        sb.append("ASSIGNMENT\n");
        sb.append("----------\n");
        sb.append("Room: ").append(r == null ? "Not assigned" : r.id).append("\n");
        if (a != null) {
            sb.append("Last Doctor Visit: ").append(a.dateTime).append("\n");
        }
        sb.append("\n");

        sb.append("MEDICAL\n");
        sb.append("-------\n");
        sb.append("Allergies: ").append(p.allergies == null || p.allergies.isEmpty() ? "None" : p.allergies)
                .append("\n");
        sb.append("Medications: ").append(p.medications == null || p.medications.isEmpty() ? "None" : p.medications)
                .append("\n");
        sb.append("Past History: ")
                .append(p.pastMedicalHistory == null || p.pastMedicalHistory.isEmpty() ? "None" : p.pastMedicalHistory)
                .append("\n");

        return sb.toString();
    }

    private JScrollPane createMedicalPanel(Patient p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createSectionHeader("MEDICAL OVERVIEW"));
        panel.add(createSection("Allergies:", p.allergies));
        panel.add(createSection("Current Medications:", p.medications));
        panel.add(createSection("Past Medical History:", p.pastMedicalHistory));
        panel.add(Box.createVerticalStrut(12));

        panel.add(createSectionHeader("UPLOADED TESTS & RESULTS"));
        panel.add(testSummaryRow("X-ray", p.xrayFilePath, p.xrayStatus, p.xraySummary));
        panel.add(testSummaryRow("Stool Exam", p.stoolFilePath, p.stoolStatus, p.stoolSummary));
        panel.add(testSummaryRow("Urinalysis", p.urineFilePath, p.urineStatus, p.urineSummary));
        panel.add(testSummaryRow("Blood Test", p.bloodFilePath, p.bloodStatus, p.bloodSummary));

        if (p.labResults != null && !p.labResults.isEmpty()) {
            panel.add(Box.createVerticalStrut(8));
            panel.add(createListSection("Lab Results", p.labResults, "No lab results uploaded"));
        }
        if (p.radiologyReports != null && !p.radiologyReports.isEmpty()) {
            panel.add(Box.createVerticalStrut(8));
            panel.add(createListSection("Radiology Reports", p.radiologyReports, "No radiology reports"));
        }

        if (p.fileAttachments != null && !p.fileAttachments.isEmpty()) {
            panel.add(Box.createVerticalStrut(12));
            panel.add(createSectionHeader("MEDICAL ATTACHMENTS"));
            for (FileAttachment fa : p.fileAttachments) {
                panel.add(attachmentRow(fa));
            }
        }

        if (p.attachmentPaths != null && !p.attachmentPaths.isEmpty()) {
            panel.add(Box.createVerticalStrut(8));
            panel.add(createSectionHeader("Other Attachments"));
            for (String path : p.attachmentPaths) {
                panel.add(filePreviewRow("Attachment", path));
            }
        }

        panel.add(Box.createVerticalStrut(12));
        panel.add(createSectionHeader("PROGRESS NOTES"));
        panel.add(createListSection("Notes", p.progressNotes, "No progress notes"));

        panel.add(Box.createVerticalGlue());
        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private JScrollPane createVisitsPanel(Patient p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createSectionHeader("VISIT HISTORY"));
        String[] cols = { "Date/Time", "Doctor", "Department", "Status" };
        javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (Appointment a : DataStore.appointments.values()) {
            if (p.id.equals(a.patientId)) {
                Staff doc = DataStore.staff.get(a.staffId);
                String doctorName = doc != null ? doc.name : a.staffId;
                m.addRow(new Object[] { a.dateTime, doctorName, a.department, a.notes == null ? "" : a.notes });
            }
        }
        JTable visitTable = new JTable(m);
        visitTable.setRowHeight(26);
        JScrollPane visitScroll = new JScrollPane(visitTable);
        visitScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(visitScroll);

        panel.add(Box.createVerticalStrut(10));
        panel.add(createSectionHeader("DIAGNOSES"));
        panel.add(createListSection("Diagnoses", p.diagnoses, "No diagnoses recorded"));

        panel.add(Box.createVerticalStrut(8));
        panel.add(createSectionHeader("TREATMENT PLANS"));
        panel.add(createListSection("Plans", p.treatmentPlans, "No treatment plans recorded"));

        panel.add(Box.createVerticalStrut(8));
        panel.add(createSectionHeader("DISCHARGE SUMMARIES"));
        panel.add(createListSection("Summaries", p.dischargeSummaries, "No discharge summaries"));

        panel.add(Box.createVerticalGlue());
        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private JScrollPane createInsurancePanel(Patient p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createSection("Insurance Provider:", p.insuranceProvider));
        panel.add(createSection("Insurance ID:", p.insuranceId));
        panel.add(createSection("Policy Holder:", p.policyHolderName));
        panel.add(Box.createVerticalGlue());

        return new JScrollPane(panel);
    }

    private JPanel createSection(String title, String content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(new Color(50, 50, 50));

        JTextArea contentArea = new JTextArea(content == null ? "" : content);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 11));
        contentArea.setBackground(new Color(250, 250, 250));
        contentArea.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        return panel;
    }

    private JPanel testSummaryRow(String label, String path, String status, String summary) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel name = new JLabel(label);
        name.setFont(new Font("Arial", Font.BOLD, 12));
        name.setPreferredSize(new Dimension(120, 22));
        row.add(name, BorderLayout.WEST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        center.setOpaque(false);
        center.add(statusBadge(status == null ? "Not Uploaded" : status));
        if (summary != null && !summary.trim().isEmpty()) {
            JLabel s = new JLabel(summary);
            s.setFont(new Font("Arial", Font.PLAIN, 11));
            s.setForeground(new Color(60, 60, 60));
            center.add(s);
        }
        row.add(center, BorderLayout.CENTER);

        if (path != null && !path.trim().isEmpty()) {
            JButton preview = new JButton("Preview");
            preview.setFont(new Font("Arial", Font.PLAIN, 11));
            preview.addActionListener(e -> showFilePreview(path, PatientDetailsDialogNew.this));
            row.add(preview, BorderLayout.EAST);
        }

        return row;
    }

    private JPanel attachmentRow(FileAttachment fa) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        String label = fa.fileName != null ? fa.fileName : "Attachment";
        JLabel name = new JLabel(label + (fa.fileType != null ? " (" + fa.fileType + ")" : ""));
        name.setFont(new Font("Arial", Font.BOLD, 12));
        row.add(name, BorderLayout.WEST);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        meta.setOpaque(false);
        meta.add(new JLabel(fa.category == null ? "" : fa.category));
        meta.add(new JLabel(fa.getFormattedSize()));
        if (fa.uploadedBy != null)
            meta.add(new JLabel("by " + fa.uploadedBy));
        row.add(meta, BorderLayout.CENTER);

        JButton preview = new JButton("Preview");
        preview.setFont(new Font("Arial", Font.PLAIN, 11));
        preview.addActionListener(e -> showFilePreview(fa.filePath, PatientDetailsDialogNew.this));
        row.add(preview, BorderLayout.EAST);

        return row;
    }

    private JPanel createListSection(String title, List<String> entries, String emptyText) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(createSectionHeader(title.toUpperCase(Locale.ROOT)));
        if (entries == null || entries.isEmpty()) {
            JLabel none = new JLabel(emptyText);
            none.setForeground(new Color(120, 120, 120));
            none.setFont(new Font("Arial", Font.PLAIN, 11));
            section.add(none);
        } else {
            for (String item : entries) {
                JLabel lbl = new JLabel("• " + item);
                lbl.setFont(new Font("Arial", Font.PLAIN, 11));
                lbl.setForeground(new Color(60, 60, 60));
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                section.add(lbl);
            }
        }
        return section;
    }

    private JComponent filePreviewRow(String label, String path) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel l = new JLabel(label + (path == null ? ": (Not uploaded)" : ""));
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setPreferredSize(new Dimension(180, 22));
        row.add(l, BorderLayout.WEST);
        if (path != null && !path.trim().isEmpty()) {
            JLabel status = new JLabel(path);
            status.setFont(new Font("Arial", Font.PLAIN, 11));
            status.setForeground(new Color(80, 80, 80));
            JButton preview = new JButton("Preview");
            preview.setFont(new Font("Arial", Font.PLAIN, 11));
            preview.addActionListener(e -> showFilePreview(path, PatientDetailsDialogNew.this));
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            right.setOpaque(false);
            right.add(status);
            right.add(preview);
            row.add(right, BorderLayout.CENTER);
        }
        return row;
    }

    private JLabel statusBadge(String s) {
        String status = (s == null || s.trim().isEmpty()) ? "Not Uploaded" : s;
        Color bg = new Color(240, 240, 240);
        Color fg = new Color(90, 90, 90);
        if (status.equalsIgnoreCase("uploaded")) {
            bg = new Color(232, 245, 233);
            fg = new Color(46, 125, 50);
        } else if (status.equalsIgnoreCase("reviewed")) {
            bg = new Color(232, 240, 254);
            fg = new Color(25, 118, 210);
        } else if (status.toLowerCase(Locale.ROOT).contains("critical")) {
            bg = new Color(255, 230, 230);
            fg = new Color(192, 57, 43);
        }
        JLabel lbl = new JLabel(status);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        lbl.setFont(new Font("Arial", Font.BOLD, 10));
        return lbl;
    }

    private void showFilePreview(String path, Window owner) {
        try {
            java.io.File f = new java.io.File(path);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(owner, "File not found: " + path);
                return;
            }
            String lower = path.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) {
                ImageIcon ic = new ImageIcon(path);
                Image img = ic.getImage();
                int w = Math.min(img.getWidth(null), 1000);
                int h = Math.min(img.getHeight(null), 800);
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                JLabel lbl = new JLabel(new ImageIcon(scaled));
                JScrollPane sp = new JScrollPane(lbl);
                JDialog d = new JDialog((Frame) null, "Preview - " + f.getName(), true);
                d.setSize(Math.min(w + 60, 1100), Math.min(h + 60, 900));
                d.setLocationRelativeTo(owner);
                d.add(sp);
                d.setVisible(true);
            } else if (lower.endsWith(".pdf")) {
                if (java.awt.Desktop.isDesktopSupported())
                    java.awt.Desktop.getDesktop().open(f);
                else
                    JOptionPane.showMessageDialog(owner, "PDF preview not supported on this platform. Path: " + path);
            } else {
                if (java.awt.Desktop.isDesktopSupported())
                    java.awt.Desktop.getDesktop().open(f);
                else
                    JOptionPane.showMessageDialog(owner, "Cannot preview this file type. Path: " + path);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Could not open file: " + path + "\n" + ex.getMessage());
        }
    }
}
