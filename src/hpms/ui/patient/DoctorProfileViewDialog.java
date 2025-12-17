package hpms.ui.patient;

import hpms.model.Staff;
import hpms.util.DataStore;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DoctorProfileViewDialog extends JDialog {

    private static final Color BG_APP = new Color(245, 247, 250);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color ACCENT = new Color(0, 102, 102);

    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_SECTION = new Font("Arial", Font.BOLD, 13);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);
    private static final Font FONT_VALUE = new Font("Arial", Font.PLAIN, 12);
    private static final Font FONT_SUBTLE = new Font("Arial", Font.PLAIN, 11);

    private static final int PAD_OUTER = 16;
    private static final int PAD_CARD = 16;
    private static final int RADIUS_CARD = 14;

    public DoctorProfileViewDialog(JFrame parent, String doctorId) {
        super(parent, "Doctor Profile", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        setSize(920, 660);
        setLocationRelativeTo(parent);

        Staff doctor = DataStore.staff.get(doctorId);
        if (doctor == null) {
            JOptionPane.showMessageDialog(this, "Doctor not found", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setContentPane(buildRoot(doctor));
    }

    private JPanel buildRoot(Staff doctor) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_APP);

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(PAD_OUTER, PAD_OUTER, PAD_OUTER, PAD_OUTER));

        content.add(buildProfileCard(doctor));
        content.add(Box.createVerticalStrut(12));

        Map<String, Object> values = extractStaffValues(doctor);

        content.add(buildSectionCard("Personal Information", buildFieldsGrid(values, orderedKeys(
                "id", "name", "gender", "birthdate", "status"
        ), values)));
        content.add(Box.createVerticalStrut(12));

        content.add(buildSectionCard("Professional Credentials", buildFieldsGrid(values, orderedKeys(
                "department", "specialty", "licenseNumber", "yearsExperience"
        ), values)));
        content.add(Box.createVerticalStrut(12));

        content.add(buildSectionCard("Contact Details", buildFieldsGrid(values, orderedKeys(
                "phone", "email", "address", "emergencyContact"
        ), values)));
        content.add(Box.createVerticalStrut(12));

        content.add(buildSectionCard("Work Details", buildFieldsGrid(values, orderedKeys(
                "schedule", "shift", "availability"
        ), values)));
        content.add(Box.createVerticalStrut(12));

        content.add(buildSectionCard("Additional Information", buildFieldsGrid(values, orderedKeys(
                "biography", "summary", "notes"
        ), values)));
        content.add(Box.createVerticalStrut(12));

        java.util.List<String> displayed = new ArrayList<>();
        displayed.addAll(orderedKeys("id", "name", "gender", "birthdate", "status"));
        displayed.addAll(orderedKeys("department", "specialty", "licenseNumber", "yearsExperience"));
        displayed.addAll(orderedKeys("phone", "email", "address", "emergencyContact"));
        displayed.addAll(orderedKeys("schedule", "shift", "availability"));
        displayed.addAll(orderedKeys("biography", "summary", "notes"));

        java.util.List<String> extras = new ArrayList<>();
        for (String k : values.keySet()) {
            if (!displayed.contains(k)) {
                extras.add(k);
            }
        }
        extras.sort(String.CASE_INSENSITIVE_ORDER);
        if (!extras.isEmpty()) {
            content.add(buildSectionCard("Other Fields", buildFieldsGrid(values, extras, values)));
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_APP);
        scroll.setBackground(BG_APP);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Doctor Profile");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton close = createHeaderButton("Close");
        close.addActionListener(e -> dispose());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(close);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel buildProfileCard(Staff doctor) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(16, 0));

        JComponent avatar = createAvatar(doctor);
        avatar.setPreferredSize(new Dimension(96, 96));

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(avatar, BorderLayout.NORTH);
        card.add(left, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(displayValue(extractStaffValues(doctor).get("name"), true));
        name.setFont(new Font("Arial", Font.BOLD, 16));
        name.setForeground(TEXT_PRIMARY);
        info.add(name);
        info.add(Box.createVerticalStrut(6));

        String specialty = displayValue(extractStaffValues(doctor).get("specialty"), false);
        String dept = displayValue(extractStaffValues(doctor).get("department"), false);
        JLabel meta = new JLabel(joinMeta(specialty, dept));
        meta.setFont(FONT_VALUE);
        meta.setForeground(TEXT_MUTED);
        info.add(meta);

        String id = displayValue(extractStaffValues(doctor).get("id"), false);
        JLabel idLabel = new JLabel("Staff ID: " + id);
        idLabel.setFont(FONT_SUBTLE);
        idLabel.setForeground(TEXT_MUTED);
        info.add(Box.createVerticalStrut(8));
        info.add(idLabel);

        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private String joinMeta(String specialty, String dept) {
        java.util.List<String> parts = new ArrayList<>();
        if (specialty != null && !specialty.trim().isEmpty() && !"Not Provided".equals(specialty)) {
            parts.add(specialty);
        }
        if (dept != null && !dept.trim().isEmpty() && !"Not Provided".equals(dept)) {
            parts.add(dept);
        }
        if (parts.isEmpty()) {
            return "Not Provided";
        }
        return String.join(" • ", parts);
    }

    private JPanel buildSectionCard(String title, JComponent body) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());

        JLabel t = new JLabel(title);
        t.setFont(FONT_SECTION);
        t.setForeground(TEXT_PRIMARY);
        t.setBorder(new EmptyBorder(0, 0, 10, 0));

        card.add(t, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JComponent buildFieldsGrid(Map<String, Object> all, java.util.List<String> keys, Map<String, Object> values) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        int row = 0;
        for (String key : keys) {
            String label = prettifyKey(key);
            Object raw = values.get(key);

            JLabel l = new JLabel(label);
            l.setFont(FONT_LABEL);
            l.setForeground(TEXT_PRIMARY);

            JLabel v = new JLabel(displayValue(raw, false));
            v.setFont(FONT_VALUE);
            if (isMissing(raw)) {
                v.setForeground(TEXT_MUTED);
            } else {
                v.setForeground(new Color(75, 85, 99));
            }

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.28;
            gbc.insets = new Insets(6, 0, 6, 12);
            grid.add(l, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.72;
            gbc.insets = new Insets(6, 0, 6, 0);
            grid.add(v, gbc);

            row++;
        }

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        grid.add(Box.createVerticalGlue(), gbc);

        return grid;
    }

    private java.util.List<String> orderedKeys(String... keys) {
        java.util.List<String> list = new ArrayList<>();
        for (String k : keys) {
            list.add(k);
        }
        return list;
    }

    private JPanel createCardPanel() {
        RoundedPanel p = new RoundedPanel(RADIUS_CARD);
        p.setBackground(BG_CARD);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER_SOFT, 1, true), new EmptyBorder(PAD_CARD, PAD_CARD, PAD_CARD, PAD_CARD)));
        return p;
    }

    private JButton createHeaderButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0, 0, 0, 0));
        b.setBorder(new CompoundBorder(new LineBorder(new Color(255, 255, 255, 120), 1, true), new EmptyBorder(6, 12, 6, 12)));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);

        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setOpaque(true);
                b.setContentAreaFilled(true);
                b.setBackground(new Color(255, 255, 255, 35));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setOpaque(false);
                b.setContentAreaFilled(false);
                b.setBackground(new Color(0, 0, 0, 0));
            }
        });

        return b;
    }

    private JComponent createAvatar(Staff doctor) {
        String photoPath = safeString(extractStaffValues(doctor).get("photoPath"));
        BufferedImage img = null;
        if (photoPath != null && !photoPath.trim().isEmpty()) {
            try {
                img = ImageIO.read(new File(photoPath));
            } catch (Exception ignored) {
                img = null;
            }
        }

        String name = safeString(extractStaffValues(doctor).get("name"));
        String initials = initialsFromName(name);

        if (img == null) {
            return new InitialsAvatar(initials, ACCENT);
        }

        return new PhotoAvatar(img);
    }

    private static String initialsFromName(String name) {
        if (name == null) {
            return "DR";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() == 2) break;
        }
        if (sb.length() == 0) {
            return "DR";
        }
        if (sb.length() == 1) {
            sb.append("R");
        }
        return sb.toString();
    }

    private Map<String, Object> extractStaffValues(Staff doctor) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (doctor == null) {
            return map;
        }

        Class<?> c = doctor.getClass();
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            try {
                f.setAccessible(true);
                map.put(f.getName(), f.get(doctor));
            } catch (Exception ignored) {
            }
        }

        return map;
    }

    private boolean isMissing(Object v) {
        if (v == null) return true;
        if (v instanceof String) {
            return ((String) v).trim().isEmpty();
        }
        return false;
    }

    private String safeString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private String displayValue(Object v, boolean preferEmptyAsNotProvided) {
        if (v == null) return "Not Provided";

        if (v instanceof String) {
            String s = ((String) v).trim();
            if (s.isEmpty()) {
                return "Not Provided";
            }
            return s;
        }

        if (v instanceof LocalDate) {
            return v.toString();
        }
        if (v instanceof LocalDateTime) {
            return v.toString();
        }

        if (v instanceof Collection) {
            Collection<?> c = (Collection<?>) v;
            if (c.isEmpty()) return "Not Provided";
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Object o : c) {
                if (o == null) continue;
                String s = String.valueOf(o).trim();
                if (s.isEmpty()) continue;
                if (i > 0) sb.append(", ");
                sb.append(s);
                i++;
                if (i >= 6) {
                    if (c.size() > 6) sb.append(" …");
                    break;
                }
            }
            String out = sb.toString().trim();
            return out.isEmpty() ? "Not Provided" : out;
        }

        String s = String.valueOf(v);
        if (preferEmptyAsNotProvided && (s == null || s.trim().isEmpty())) {
            return "Not Provided";
        }
        return s == null || s.trim().isEmpty() ? "Not Provided" : s;
    }

    private String prettifyKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return "Field";
        }
        String s = key.trim();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (i == 0) {
                sb.append(Character.toUpperCase(ch));
                continue;
            }
            if (Character.isUpperCase(ch) && Character.isLetterOrDigit(s.charAt(i - 1))) {
                sb.append(' ');
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;

        private RoundedPanel(int radius) {
            super();
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            Shape rr = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);
            g2.fill(rr);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class InitialsAvatar extends JComponent {
        private final String initials;
        private final Color accent;

        private InitialsAvatar(String initials, Color accent) {
            this.initials = initials == null ? "DR" : initials;
            this.accent = accent;
            setPreferredSize(new Dimension(96, 96));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h);
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
            g2.fill(new Ellipse2D.Float(x, y, size, size));

            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new Ellipse2D.Float(x, y, size, size));

            g2.setColor(ACCENT.darker());
            g2.setFont(new Font("Arial", Font.BOLD, Math.max(18, size / 4)));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(this.initials);
            int th = fm.getAscent();
            g2.drawString(this.initials, x + (size - tw) / 2, y + (size + th) / 2 - 3);

            g2.dispose();
        }
    }

    private static class PhotoAvatar extends JComponent {
        private final BufferedImage image;

        private PhotoAvatar(BufferedImage image) {
            this.image = image;
            setPreferredSize(new Dimension(96, 96));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h);
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            Shape clip = new Ellipse2D.Float(x, y, size, size);
            g2.setClip(clip);

            if (image != null) {
                BufferedImage scaled = scaleToSquare(image, size);
                g2.drawImage(scaled, x, y, null);
            }

            g2.setClip(null);
            g2.setColor(new Color(229, 231, 235));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new Ellipse2D.Float(x, y, size, size));

            g2.dispose();
        }

        private static BufferedImage scaleToSquare(BufferedImage src, int size) {
            int sw = src.getWidth();
            int sh = src.getHeight();
            int s = Math.min(sw, sh);
            int sx = (sw - s) / 2;
            int sy = (sh - s) / 2;
            BufferedImage cropped = src.getSubimage(sx, sy, s, s);

            BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(cropped, 0, 0, size, size, null);
            g2.dispose();
            return out;
        }
    }
}
