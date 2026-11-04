package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import util.AuthSupport;

/**
 * View model for UC18. Never includes password hashes.
 */
public class AdminAccount {

    public int id, customerId;
    public String username = "", role = "customer", status = "active", displayName = "";
    public String firstName = "", lastName = "", email = "", phone = "", country = "", city = "", street = "", version = "";
    public boolean hasHistory;
    public LocalDateTime createdAt;
    public static final List<String> ROLES = Collections.unmodifiableList(Arrays.asList("customer", "staff", "manager", "admin"));
    public static final List<String> STATUSES = Collections.unmodifiableList(Arrays.asList("active", "locked", "inactive"));

    public Map<String, String> validate(boolean creating) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (creating && (!AuthSupport.validUsername(username) || username.contains("@"))) {
            errors.put("username", "Username: 3–50 ký tự, không chứa @ hoặc ký tự điều khiển.");
        }
        if (!AuthSupport.validEmail(email)) {
            errors.put("email", "Email không hợp lệ hoặc vượt 100 ký tự.");
        }
        if (!AuthSupport.validPhone(phone)) {
            errors.put("phone", "Số điện thoại cần 10–11 chữ số.");
        }
        if (!validName(firstName) || !validName(lastName) || (lastName + " " + firstName).length() > 100) {
            errors.put("name", "Họ và tên bắt buộc, mỗi phần tối đa 50 ký tự, tổng tối đa 100.");
        }
        if (!ROLES.contains(role)) {
            errors.put("role", "Role không hợp lệ.");
        }
        if (!STATUSES.contains(status) || (creating && !"active".equals(status))) {
            errors.put("status", "Status không hợp lệ.");
        }
        if (!bounded(country, 50) || !bounded(city, 50) || !bounded(street, 100)) {
            errors.put("address", "Địa chỉ vượt giới hạn hoặc có ký tự điều khiển.");
        }
        return errors;
    }

    private boolean validName(String value) {
        return !value.isBlank() && bounded(value, 50);
    }

    private boolean bounded(String value, int max) {
        return value != null && value.length() <= max && value.chars().noneMatch(Character::isISOControl);
    }

    public static class BookingRow {

        public int id;
        public String status;
        public BigDecimal total;
        public LocalDateTime date;
    }

    public static class Page {

        public List<AdminAccount> rows = new ArrayList<>();
        public int total, page, pages;
        public int active, locked, inactive;
    }
}
