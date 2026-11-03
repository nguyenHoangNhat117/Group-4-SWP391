package util;

import java.util.LinkedHashMap;
import java.util.Map;
import model.AdminAccount;

/**
 * Shared UC18 validation, repeated at the DAO boundary.
 */
public final class AdminAccountPolicy {

    private AdminAccountPolicy() {
    }

    public static boolean role(String value) {
        return "customer".equals(value) || "staff".equals(value)
                || "manager".equals(value) || "admin".equals(value);
    }

    public static boolean status(String value) {
        return "active".equals(value) || "locked".equals(value) || "inactive".equals(value);
    }

    public static String displayName(AdminAccount a) {
        return (AuthSupport.text(a.getLastName()) + " " + AuthSupport.text(a.getFirstName())).trim();
    }

    public static Map<String, String> validate(AdminAccount a, String password, boolean create) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (create && !AuthSupport.validUsername(a.getUsername())) {
            errors.put("username", "Use 3–50 characters without control characters.");
        }
        if (!validText(a.getFirstName(), 50, true)) {
            errors.put("firstName", "First name is required (max 50 characters).");
        }
        if (!validText(a.getLastName(), 50, false) || displayName(a).length() > 100) {
            errors.put("lastName", "Last name: max 50 characters. Full name: max 100.");
        }
        if (!AuthSupport.validEmail(a.getEmail())) {
            errors.put("email", "Enter a valid email (max 100 characters).");
        }
        if (!AuthSupport.validPhone(a.getPhone())) {
            errors.put("phone", "Phone must contain 10 or 11 digits.");
        }
        if (!role(a.getRole())) {
            errors.put("role", "Invalid role.");
        }
        if (!status(a.getStatus())) {
            errors.put("status", "Invalid status.");
        }
        if (!validText(a.getCountry(), 50, false)) {
            errors.put("country", "Country must be at most 50 characters.");
        }
        if (!validText(a.getCity(), 50, false)) {
            errors.put("city", "City must be at most 50 characters.");
        }
        if (!validText(a.getStreet(), 100, false)) {
            errors.put("street", "Address must be at most 100 characters.");
        }
        if (create && !AuthSupport.validPassword(password)) {
            errors.put("password", "Use 8–128 characters with uppercase, lowercase, a number and a symbol. No spaces.");
        }
        return errors;
    }

    private static boolean validText(String s, int max, boolean required) {
        return s != null && (!required || !s.isBlank()) && s.length() <= max
                && s.chars().noneMatch(Character::isISOControl);
    }

    public static boolean selfRestricted(int actor, int target, String role, String status, boolean delete) {
        return actor == target && (delete || !"admin".equals(role) || !"active".equals(status));
    }
}
