package hpms.ui.dialogs;

import hpms.model.*;
import hpms.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Locale;
import java.awt.image.BufferedImage;

/**
 * Modal dialog which displays a modern, readable full patient profile
 * including assigned room, latest doctor and visit date, vitals and clinical
 * notes.
 */
public class PatientDetailsDialog extends JDialog {
    public PatientDetailsDialog(Window owner, Patient p) {
        super(owner, "Patient Details - " + p.name, ModalityType.APPLICATION_MODAL);
        setSize(820, 640);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 210)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        header.setBackground(new Color(245, 250, 255));
        JLabel title = new JLabel(p.name + "   ");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel idLbl = new JLabel("ID: " + p.id);
        idLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        idLbl.setForeground(new Color(90, 90, 90));

        // Doctors should NOT be able to change patient passwords
        boolean isDoctor = hpms.auth.AuthService.current != null
                && hpms.auth.AuthService.current.role == UserRole.DOCTOR;

        boolean hasPortal = DataStore.users.containsKey(p.id);
        // show last-known plaintext password if available (transient, not persisted)
        String lastPlain = hpms.auth.AuthService.getLastPlaintextForUI(p.id);
        String pwdText = hasPortal
                ? (lastPlain == null ? "Portal: Active (password hidden)" : "Portal: Active • Password: " + lastPlain)
                : "Portal: Not configured";
        JLabel pwdLbl = new JLabel(pwdText);
        pwdLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        pwdLbl.setForeground(new Color(90, 90, 90));
        JButton changePwdBtn = new JButton("Change Password");
        changePwdBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        changePwdBtn.addActionListener(e -> {
            if (!DataStore.users.containsKey(p.id)) {
                JOptionPane.showMessageDialog(this,
                        "No portal account exists for this patient. Use 'Reset Password' to create one.");
                return;
            }
            // Simplified change-password flow for patient accounts: only require new
            // password + confirm (no current password required here)
            JPasswordField nw = new JPasswordField();
            JPasswordField nw2 = new JPasswordField();
            Object[] msg = new Object[] { "New Password", nw, "Confirm New", nw2 };
            int r = JOptionPane.showConfirmDialog(this, msg, "Change Patient Password (no current required)",
                    JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                String nstr = new String(nw.getPassword());
                String n2 = new String(nw2.getPassword());
                if (!nstr.equals(n2)) {
                    JOptionPane.showMessageDialog(this, "New passwords do not match");
                    return;
                }
                java.util.List<String> out = hpms.auth.AuthService.changePasswordNoOld(p.id, nstr);
                if (!out.isEmpty() && out.get(0).startsWith("Error:"))
                    JOptionPane.showMessageDialog(this, out.get(0));
                else {
                    JOptionPane.showMessageDialog(this, "Password changed for " + p.id);
                    pwdLbl.setText("Portal: Active • Password: " + nstr);
                }
            }
        });
        JButton resetPwdBtn = new JButton("Reset Password");
        resetPwdBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        resetPwdBtn.addActionListener(e -> {
            // If portal account doesn't exist yet create it with a generated password
            if (!DataStore.users.containsKey(p.id)) {
                String newPw = hpms.auth.AuthService.generateRandomPasswordForUI();
                java.util.List<String> createOut = hpms.auth.AuthService.createPatientAccount(p.id, newPw);
                if (createOut.size() > 0 && createOut.get(0).startsWith("Patient account created")) {
                    JOptionPane.showMessageDialog(this, "New patient portal password: \n" + newPw);
                    pwdLbl.setText("Portal: Active • Password: " + newPw);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not create portal account: " + String.join(";", createOut));
                }
                return;
            }
            // Reset existing user's password and display it
            String newPwd = hpms.auth.AuthService.resetPassword(p.id);
            if (newPwd == null)
                JOptionPane.showMessageDialog(this, "Reset failed");
            else {
                JOptionPane.showMessageDialog(this, "New password for " + p.id + ":\n" + newPwd);
                pwdLbl.setText("Portal: Active • Password: " + newPwd);
            }
        });
        JPanel tleft = new JPanel();
        tleft.setOpaque(false);
        tleft.add(title);
        tleft.add(idLbl);

        // Only show password management for non-doctors
        if (!isDoctor) {
            tleft.add(Box.createHorizontalStrut(8));
            tleft.add(pwdLbl);
            tleft.add(Box.createHorizontalStrut(6));
            tleft.add(changePwdBtn);
        }
        header.add(tleft, BorderLayout.WEST);
        if (!isDoctor) {
            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> dispose());
            header.add(closeBtn, BorderLayout.EAST);
        }
        add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        body.setBackground(Color.WHITE);

        // Top area - identity + vitals/assignment
        JPanel top = new JPanel(new GridLayout(1, 2, 12, 12));
        top.setOpaque(false);

        // Left - Identity + photo
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(new Color(255, 255, 255));
        left.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        int y = 0;

        // Profile photo (placeholder if none)
        JLabel photo = new JLabel();
        photo.setHorizontalAlignment(SwingConstants.CENTER);
        photo.setVerticalAlignment(SwingConstants.CENTER);
        photo.setPreferredSize(new Dimension(140, 140));
        photo.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        photo.setIcon(loadPatientPhoto(p.photoPath, 140, 140));
        c.gridy = y++;
        c.gridx = 0;
        c.gridwidth = 1;
        left.add(photo, c);
        c.gridy = y++;
        left.add(labeledField("Name", p.name), c);
        c.gridy = y++;
        left.add(labeledField("Age", String.valueOf(p.age)), c);
        c.gridy = y++;
        left.add(labeledField("Gender", p.gender == null ? "N/A" : p.gender.name()), c);
        c.gridy = y++;
        left.add(labeledField("Contact", p.contact == null ? "" : p.contact), c);
        c.gridy = y++;
        left.add(labeledField("Address", p.address == null ? "" : p.address), c);

        top.add(left);

        // Right - Vitals + assignment
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 230, 240)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        right.add(createStatRow("Height (cm)",
                p.heightCm == null ? "N/A" : String.format(Locale.US, "%.1f", p.heightCm)));
        right.add(createStatRow("Weight (kg)",
                p.weightKg == null ? "N/A" : String.format(Locale.US, "%.1f", p.weightKg)));
        Double bmiVal = p.getBmi();
        String bmiText = "N/A";
        Color bmiColor = new Color(70, 70, 70);
        if (bmiVal != null) {
            String bmiCategory;
            if (bmiVal < 18.5) {
                bmiCategory = "Underweight";
                bmiColor = new Color(0x2E86C1);
            } else if (bmiVal < 25.0) {
                bmiCategory = "Normal";
                bmiColor = new Color(0x27AE60);
            } else if (bmiVal < 30.0) {
                bmiCategory = "Overweight";
                bmiColor = new Color(0xF39C12);
            } else {
                bmiCategory = "Obese";
                bmiColor = new Color(0xE74C3C);
            }
            bmiText = String.format(Locale.US, "%.2f (%s)", bmiVal, bmiCategory);
        }
        right.add(createColoredStatRow("BMI", bmiText, bmiColor));
        right.add(Box.createVerticalStrut(8));
        right.add(new JSeparator());
        right.add(Box.createVerticalStrut(8));

        // assigned room
        Room assigned = null;
        for (Room r : DataStore.rooms.values())
            if (p.id.equals(r.occupantPatientId)) {
                assigned = r;
                break;
            }
        right.add(createStatRow("Room",
                assigned == null ? "Not assigned" : (assigned.id + " (" + assigned.status + ")")));

        // latest appointment / doctor
        Appointment latest = DataStore.appointments.values().stream().filter(a -> a.patientId.equals(p.id))
                .max((a, b) -> a.dateTime.compareTo(b.dateTime)).orElse(null);
        String docText = "None";
        String dateText = "-";
        String docSpecialty = "-";
        if (latest != null) {
            Staff doc = DataStore.staff.get(latest.staffId);
            if (doc != null) {
                docText = doc.name + " (" + doc.department + ")";
                if (doc.specialty != null && !doc.specialty.trim().isEmpty())
                    docSpecialty = doc.specialty;
                else if (doc.subSpecialization != null && !doc.subSpecialization.trim().isEmpty())
                    docSpecialty = doc.subSpecialization;
                else if (doc.department != null && !doc.department.trim().isEmpty())
                    docSpecialty = doc.department;
            } else {
                docText = latest.staffId;
            }
            dateText = latest.dateTime.toLocalDate().toString() + " "
                    + latest.dateTime.toLocalTime().withSecond(0).withNano(0).toString();
        }
        right.add(createStatRow("Primary Doctor / Last Visit", docText));
        right.add(createStatRow("Doctor Specialization", docSpecialty));
        right.add(createStatRow("Appointment Date", dateText));

        top.add(right);
        body.add(top, BorderLayout.NORTH);

        // Middle - Tabs for Medical, Visits, Insurance
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("Medical", createMedicalPanel(p));
        tabs.addTab("Visits", createVisitsPanel(p));
        tabs.addTab("Insurance", createInsurancePanel(p));
        body.add(tabs, BorderLayout.CENTER);

        // Footer - Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        final Room finalAssigned = assigned;
        final Appointment finalLatest = latest;
        JButton print = new JButton("Print Summary");
        print.addActionListener(e -> {
            // Build a simple printable summary
            JTextArea ta = new JTextArea(20, 60);
            ta.setText(buildSummary(p, finalAssigned, finalLatest));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Patient Summary",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        footer.add(print);
        footer.add(close);

        add(body, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    private String buildSummary(Patient p, Room r, Appointment a) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(p.id).append('\n');
        sb.append("Name: ").append(p.name).append('\n');
        sb.append("Age: ").append(p.age).append('\n');
        sb.append("Gender: ").append(p.gender).append('\n');
        sb.append("Contact: ").append(p.contact).append('\n');
        sb.append("Address: ").append(p.address).append('\n');
        sb.append('\n');
        sb.append("Height: ").append(p.heightCm == null ? "N/A" : p.heightCm).append(" cm\n");
        sb.append("Weight: ").append(p.weightKg == null ? "N/A" : p.weightKg).append(" kg\n");
        Double b = p.getBmi();
        if (b == null)
            sb.append("BMI: N/A\n");
        else {
            String bmiCategory;
            if (b < 18.5)
                bmiCategory = "Underweight";
            else if (b < 25.0)
                bmiCategory = "Normal";
            else if (b < 30.0)
                bmiCategory = "Overweight";
            else
                bmiCategory = "Obese";
            sb.append("BMI: ").append(String.format(Locale.US, "%.2f (%s)", b, bmiCategory)).append('\n');
        }
        sb.append('\n');
        sb.append("Assigned Room: ").append(r == null ? "Not assigned" : r.id + " (" + r.status + ")").append('\n');
        if (a != null)
            sb.append("Last Visit: ").append(a.dateTime.toString()).append(" by ").append(a.staffId).append('\n');
        sb.append('\n');
        sb.append("Allergies: ").append(p.allergies == null || p.allergies.isEmpty() ? "None" : p.allergies)
                .append('\n');
        sb.append("Medications: ").append(p.medications == null || p.medications.isEmpty() ? "None" : p.medications)
                .append('\n');
        sb.append("Past History: ")
                .append(p.pastMedicalHistory == null || p.pastMedicalHistory.isEmpty() ? "None" : p.pastMedicalHistory)
                .append('\n');
        return sb.toString();
    }

    private JPanel createStatRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel l = new JLabel(label + ":");
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setPreferredSize(new Dimension(150, 24));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.PLAIN, 12));
        v.setForeground(new Color(70, 70, 70));
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JPanel createColoredStatRow(String label, String value, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel l = new JLabel(label + ":");
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setPreferredSize(new Dimension(150, 24));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.PLAIN, 12));
        v.setForeground(valueColor);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        return p;
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
        // Placeholder: simple gray silhouette
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

    private JPanel labeledField(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 11));
        t.setForeground(new Color(80, 80, 80));
        JTextArea v = new JTextArea(value);
        v.setLineWrap(true);
        v.setWrapStyleWord(true);
        v.setEditable(false);
        v.setFont(new Font("Arial", Font.PLAIN, 13));
        v.setBackground(new Color(255, 255, 255));
        v.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        panel.add(t, BorderLayout.NORTH);
        panel.add(v, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createMedicalPanel(Patient p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(hpms.ui.components.DetailSection.createHeader("Allergies"));
        panel.add(hpms.ui.components.DetailSection.createContent(p.allergies));
        panel.add(Box.createVerticalStrut(6));
        panel.add(hpms.ui.components.DetailSection.createHeader("Current Medications"));
        panel.add(hpms.ui.components.DetailSection.createContent(p.medications));
        panel.add(Box.createVerticalStrut(6));
        panel.add(hpms.ui.components.DetailSection.createHeader("Past Medical History"));
        panel.add(hpms.ui.components.DetailSection.createContent(p.pastMedicalHistory));
        panel.add(Box.createVerticalStrut(6));
        // Show uploaded test files with preview buttons
        panel.add(hpms.ui.components.DetailSection.createHeader("Uploaded Tests & Results"));
        // Improved test summary rows: icon + colored status badge + brief summary +
        // preview
        panel.add(testSummaryRow("X-ray", p.xrayFilePath, p.xrayStatus, p.xraySummary));
        panel.add(testSummaryRow("Stool Exam", p.stoolFilePath, p.stoolStatus, p.stoolSummary));
        panel.add(testSummaryRow("Urinalysis", p.urineFilePath, p.urineStatus, p.urineSummary));
        panel.add(testSummaryRow("Blood Test", p.bloodFilePath, p.bloodStatus, p.bloodSummary));
        // show any other uploaded attachments
        if (p.attachmentPaths != null && !p.attachmentPaths.isEmpty()) {
            panel.add(Box.createVerticalStrut(6));
            panel.add(sectionHeader("Other Attachments"));
            for (String path : p.attachmentPaths)
                panel.add(filePreviewRow("Attachment", path));
        }
        panel.add(Box.createVerticalStrut(6));
        panel.add(sectionHeader("Progress Notes"));
        if (p.progressNotes == null || p.progressNotes.isEmpty())
            panel.add(hpms.ui.components.DetailSection.createContent("No notes"));
        else {
            for (String n : p.progressNotes)
                panel.add(hpms.ui.components.DetailSection.createContent(n));
        }
        JScrollPane sp = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Keep the viewport width stable to avoid horizontal expansion — prefer
        // wrapping inside rows
        sp.getViewport().setPreferredSize(new Dimension(760, 300));
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    // Helper to create a small row showing uploaded file and preview button
    private JComponent filePreviewRow(String label, String path) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel l = new JLabel(label + (path == null ? ": (Not uploaded)" : ""));
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setPreferredSize(new Dimension(220, 24));
        row.add(l, BorderLayout.WEST);
        if (path != null && !path.trim().isEmpty()) {
            JLabel status = new JLabel(path);
            status.setFont(new Font("Arial", Font.PLAIN, 11));
            status.setForeground(new Color(80, 80, 80));
            JButton preview = new JButton("Preview");
            preview.addActionListener(e -> {
                showFilePreview(path, this);
            });
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            right.setOpaque(false);
            right.add(status);
            right.add(preview);
            row.add(right, BorderLayout.CENTER);
        }
        return row;
    }

    // Modern test summary row: shows a small icon, status badge, short summary and
    // preview
    private JComponent testSummaryRow(String label, String path, String status, String summary) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        left.setOpaque(false);
        // small icon
        JLabel icon = new JLabel(UIManager.getIcon("FileView.fileIcon"));
        icon.setPreferredSize(new Dimension(20, 20));
        left.add(icon);
        // label and small summary stacked
        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(label);
        title.setFont(new Font("Arial", Font.BOLD, 12));
        title.setForeground(new Color(40, 80, 80));
        String sum = (summary == null || summary.trim().isEmpty()) ? "No summary available" : summary.trim();
        JTextArea sumArea = new JTextArea(sum);
        sumArea.setFont(new Font("Arial", Font.PLAIN, 11));
        sumArea.setLineWrap(true);
        sumArea.setWrapStyleWord(true);
        sumArea.setEditable(false);
        sumArea.setOpaque(false);
        // constrain width to avoid horizontal growth and force wrapping
        sumArea.setMaximumSize(new Dimension(560, 48));
        textWrap.add(title);
        textWrap.add(sumArea);
        left.add(textWrap);

        row.add(left, BorderLayout.WEST);

        // right side: status badge and preview button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        right.setOpaque(false);
        JLabel badge = statusBadge(status);
        right.add(badge);
        if (path != null && !path.trim().isEmpty()) {
            JButton preview = new JButton("Preview");
            preview.setFont(new Font("Arial", Font.PLAIN, 11));
            preview.addActionListener(e -> {
                showFilePreview(path, this);
            });
            right.add(preview);
        }
        // Make title and summary clickable to preview if path exists
        if (path != null && !path.trim().isEmpty()) {
            java.awt.event.MouseListener clickOpen = new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showFilePreview(path, PatientDetailsDialog.this);
                }
            };
            title.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            title.addMouseListener(clickOpen);
            sumArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            sumArea.addMouseListener(clickOpen);
        }
        row.add(right, BorderLayout.CENTER);
        return row;
    }

    private JLabel statusBadge(String status) {
        String s = status == null ? "Not Uploaded" : status;
        Color bg = new Color(200, 200, 200);
        Color fg = new Color(50, 50, 50);
        if (s.equalsIgnoreCase("uploaded")) {
            bg = new Color(214, 238, 216);
            fg = new Color(34, 139, 34);
        } else if (s.equalsIgnoreCase("reviewed")) {
            bg = new Color(235, 245, 255);
            fg = new Color(25, 118, 210);
        } else if (s.equalsIgnoreCase("critical")) {
            bg = new Color(255, 230, 230);
            fg = new Color(192, 57, 43);
        } else if (s.toLowerCase().contains("not")) {
            bg = new Color(250, 250, 250);
            fg = new Color(120, 120, 120);
        }
        JLabel l = new JLabel(s);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        l.setFont(new Font("Arial", Font.BOLD, 11));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // Try to show an image preview inside the app, otherwise open externally
    private void showFilePreview(String path, Window owner) {
        try {
            java.io.File f = new java.io.File(path);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(owner, "File not found: " + path);
                return;
            }
            String lower = path.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) {
                ImageIcon ic = new ImageIcon(path);
                // scale to fit dialog
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
                // try to open with platform default
                if (java.awt.Desktop.isDesktopSupported())
                    java.awt.Desktop.getDesktop().open(f);
                else
                    JOptionPane.showMessageDialog(owner, "Cannot preview file. Path: " + path);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Unable to open file: " + ex.getMessage());
        }
    }

    private JScrollPane createVisitsPanel(Patient p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boolean found = false;
        java.util.List<Appointment> list = new java.util.ArrayList<>(DataStore.appointments.values());
        list.sort((a, b) -> b.dateTime.compareTo(a.dateTime));
        for (Appointment ap : list)
            if (ap.patientId.equals(p.id)) {
                found = true;
                Staff st = DataStore.staff.get(ap.staffId);
                String sname = st == null ? ap.staffId : st.name;
                panel.add(contentText(ap.dateTime.toString() + " — " + sname + " — " + ap.department));
                panel.add(Box.createVerticalStrut(6));
            }
        if (!found)
            panel.add(contentText("No visits recorded"));
        return new JScrollPane(panel);
    }

    private JScrollPane createInsurancePanel(Patient p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(hpms.ui.components.DetailSection.createHeader("Insurance Provider"));
        panel.add(hpms.ui.components.DetailSection.createContent(
                p.insuranceProvider == null || p.insuranceProvider.isEmpty() ? "None" : p.insuranceProvider));
        panel.add(Box.createVerticalStrut(6));
        panel.add(hpms.ui.components.DetailSection.createHeader("Policy ID"));
        panel.add(hpms.ui.components.DetailSection
                .createContent(p.insuranceId == null || p.insuranceId.isEmpty() ? "N/A" : p.insuranceId));
        return new JScrollPane(panel);
    }

    private JLabel sectionHeader(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(new Color(20, 80, 80));
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        // In BoxLayout containers, default alignment is centered — force left alignment
        // so headers appear at the left edge
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private JComponent contentText(String txt) {
        JTextArea ta = new JTextArea(txt == null || txt.isEmpty() ? "-" : txt);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setBackground(new Color(250, 250, 250));
        ta.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        // Make sure the text area aligns left in vertical BoxLayout and fills
        // horizontally where possible
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return ta;
    }
}
