package controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import model.BookingContact;
import model.Customer;

final class BookingContactForm {
    private static final Map<String, Integer> FIELDS = Map.of(
            "contact-name", 120, "contact-phone", 32, "contact-email", 254);

    private BookingContactForm() { }

    static Map<String, String> read(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        FIELDS.forEach((name, limit) -> {
            String value = Objects.toString(request.getParameter(name), "")
                    .replace('\u00a0', ' ').replace('\u202f', ' ').strip();
            fields.put(name, value.length() > limit || value.chars().anyMatch(Character::isISOControl) ? "" : value);
        });
        return fields;
    }

    static BookingContact validate(Map<String, String> fields) {
        return new BookingContact(fields.get("contact-name"), fields.get("contact-phone"),
                fields.get("contact-email"));
    }

    static void show(HttpServletRequest request, Customer customer, Map<String, String> fields,
            BookingContact lockedContact) {
        if (lockedContact != null) {
            request.setAttribute("contactReadOnly", true);
            fields = Map.of("contact-name", lockedContact.getName(), "contact-phone", lockedContact.getPhone(),
                    "contact-email", lockedContact.getEmail());
        } else if (fields.isEmpty()) {
            fields = Map.of("contact-name", (Objects.toString(customer.getLastName(), "") + " "
                            + Objects.toString(customer.getFirstName(), "")).strip(),
                    "contact-phone", Objects.toString(customer.getPhone(), ""),
                    "contact-email", Objects.toString(customer.getEmail(), ""));
        }
        request.setAttribute("contactFields", fields);
    }
}
