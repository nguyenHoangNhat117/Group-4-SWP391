package util;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
// Updated by Nguyen Hoang Nhat
public final class AuthSupport {

    private static final SecureRandom RANDOM = new SecureRandom();

    private AuthSupport() {
        // Utility class: prevent creating instances.
    }

    public static String text(String input) {
        return input == null ? "" : input.trim();
    }

    public static boolean validUsername(String username) {
        // Match the database limit: Username NVARCHAR(50).
        return username != null
                && username.length() >= 3
                && username.length() <= 50
                && username.chars()
                        .noneMatch(Character::isISOControl);
    }

    public static boolean validEmail(String email) {
        // Match the database limit: Customer.Email NVARCHAR(100).
        return email != null
                && email.length() <= 100
                && email.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+"
                        + "(?:\\.[A-Za-z0-9-]+)+$"
                );
    }

    public static boolean validPhone(String phone) {
        // Accept exactly 10 or 11 ASCII digits.
        return phone != null && phone.matches("[0-9]{10,11}");
    }

    public static boolean validPassword(String password) {
        if (password == null
                || password.length() < 8
                || password.length() > 128) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        // Check the original password without trimming it.
        for (int cp : password.codePoints().toArray()) {
            if (Character.isWhitespace(cp)
                    || Character.isSpaceChar(cp)
                    || Character.isISOControl(cp)) {
                return false;
            }

            hasUpper |= Character.isUpperCase(cp);
            hasLower |= Character.isLowerCase(cp);
            hasDigit |= Character.isDigit(cp);
            hasSpecial |= !Character.isLetterOrDigit(cp);
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public static String token(HttpSession session) {
        synchronized (session) {
            String value = (String) session.getAttribute("csrfToken");

            if (value == null) {
                // Generate one random CSRF token for the session.
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);

                value = Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

                session.setAttribute("csrfToken", value);
            }

            return value;
        }
    }

    public static boolean checkCsrf(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session = request.getSession(false);

        Object stored = session == null
                ? null
                : session.getAttribute("csrfToken");

        String supplied = request.getParameter("csrfToken");

        // Reject missing sessions, missing tokens, or mismatched tokens.
        if (!(stored instanceof String)
                || supplied == null
                || !MessageDigest.isEqual(
                        ((String) stored).getBytes(StandardCharsets.UTF_8),
                        supplied.getBytes(StandardCharsets.UTF_8)
                )) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Form expired. Reload the page and try again."
            );

            return false;
        }

        return true;
    }

    public static void remember(
            HttpServletRequest request,
            HttpServletResponse response,
            String username
    ) {
        // Remember the username only, never the password.
        String value = username == null
                ? ""
                : java.net.URLEncoder.encode(
                        username,
                        StandardCharsets.UTF_8
                );

        Cookie cookie = new Cookie("username", value);

        cookie.setPath(
                request.getContextPath().isEmpty()
                        ? "/"
                        : request.getContextPath()
        );

        // A zero lifetime deletes the existing cookie.
        cookie.setMaxAge(
                username == null ? 0 : 7 * 24 * 60 * 60
        );

        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);
    }

    public static void noStore(HttpServletResponse response) {
        // Prevent sensitive pages from being stored in the browser cache.
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Referrer-Policy", "no-referrer");
    }

    public static void clearRecovery(HttpSession session) {
        session.removeAttribute("passwordRecovery");

        // Remove attributes used by the older recovery flow.
        session.removeAttribute("otp");
        session.removeAttribute("otp_time");
        session.removeAttribute("email");
    }

    public static String html(Object input) {
        if (input == null) {
            return "";
        }

        // Escape text before inserting it into HTML.
        return input.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}