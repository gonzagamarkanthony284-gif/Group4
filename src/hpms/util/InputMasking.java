package hpms.util;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Utility for applying input masks to Swing text components.
 * Currently supports US-style phone number masking (10 digits).
 */
public class InputMasking {
    private InputMasking() {}

    /**
     * Applies phone number masking to the given JTextField.
     * Display format: (XXX) XXX-XXXX while keeping only digits internally.
     * @param field JTextField to attach the mask to.
     */
    public static void applyPhoneNumberMask(JTextField field) {
        if (field == null) return;
        ((PlainDocument) field.getDocument()).setDocumentFilter(new PhoneMaskFilter(field));
    }

    /**
     * Returns the numeric digits from the text field (strips all non-digits).
     */
    public static String getRawPhoneNumber(JTextField field) {
        if (field == null) return "";
        return field.getText().replaceAll("\\D", "");
    }

    private static class PhoneMaskFilter extends DocumentFilter {
        private static final int MAX_LEN = 10;
        PhoneMaskFilter(JTextField tf) { }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
            String proposed = new StringBuilder(cur).replace(offset, offset + length, text == null ? "" : text).toString();
            String digits = proposed.replaceAll("\\D", "");
            if (digits.length() > MAX_LEN || !digits.matches("\\d*")) return; // reject if too long or contains nondigits
            fb.replace(0, fb.getDocument().getLength(), format(digits), attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            replace(fb, offset, length, "", null);
        }

        private String format(String digits) {
            if (digits.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            sb.append(digits.substring(0, Math.min(3, digits.length())));
            if (digits.length() >= 4) {
                sb.append(") ");
                sb.append(digits.substring(3, Math.min(6, digits.length())));
            }
            if (digits.length() >= 7) {
                sb.append("-");
                sb.append(digits.substring(6));
            }
            return sb.toString();
        }
    }
}
