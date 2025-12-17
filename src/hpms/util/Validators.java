package hpms.util;

import java.util.regex.Pattern;

public class Validators {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]+$");

    public static boolean empty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (empty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (empty(phone)) return false;
        String digits = phone.replaceAll("\\D", "");
        return PHONE_PATTERN.matcher(digits).matches();
    }

    public static boolean isValidAge(int age) {
        return age >= 0 && age <= 120;
    }

    public static boolean isValidName(String name) {
        if (empty(name)) return false;
        return NAME_PATTERN.matcher(name).matches();
    }

    public static String getErrorMessage(String fieldName, String value, String validationType) {
        switch (validationType.toLowerCase()) {
            case "email":
                return String.format("'%s' is not a valid email address", value);
            case "phone":
                return String.format("'%s' is not a valid phone number. Please enter exactly 10 digits.", value);
            case "age":
                return String.format("'%s' is not a valid age. Age must be between 0 and 120.", value);
            case "name":
                return String.format("'%s' is not a valid name. Only letters, spaces, hyphens, and apostrophes are allowed.", value);
            default:
                return String.format("'%s' is not a valid value for %s", value, fieldName);
        }
    }
}

