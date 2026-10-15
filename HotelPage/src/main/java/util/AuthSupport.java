package util;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class AuthSupport {

    private static final SecureRandom RANDOM = new SecureRandom();

    private AuthSupport() {
    }

    public static String text(String input) {
        return input == null ? "" : input.trim();
    }

    public static boolean validPassword(String password) {
        return password != null && password.length() >= 7 && password.length() <= 128
                && !password.trim().isEmpty();
    }

    public static boolean validEmail(String email) {
        return email != null && email.length() <= 254
                && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$");
    }

    public static String token(HttpSession session) {
        synchronized (session) {
            String value = (String) session.getAttribute("csrfToken");
            if (value == null) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute("csrfToken", value);
            }
            return value;
        }
    }

    public static boolean checkCsrf(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object stored = session == null ? null : session.getAttribute("csrfToken");
        String supplied = request.getParameter("csrfToken");
        if (!(stored instanceof String) || supplied == null
                || !MessageDigest.isEqual(((String) stored).getBytes(StandardCharsets.UTF_8),
                        supplied.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(403, "Form expired. Reload the page and try again.");
            return false;
        }
        return true;
    }

    public static void remember(HttpServletRequest request, HttpServletResponse response, String username) {
        Cookie cookie = new Cookie("username", username == null ? "" : java.net.URLEncoder.encode(username, StandardCharsets.UTF_8));
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setMaxAge(username == null ? 0 : 7 * 24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    public static void noStore(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Referrer-Policy", "no-referrer");
    }

    public static void clearRecovery(HttpSession session) {
        session.removeAttribute("passwordRecovery");
        // Remove attributes left by the previous implementation.
        session.removeAttribute("otp");
        session.removeAttribute("otp_time");
        session.removeAttribute("email");
    }

    public static String html(Object input) {
        if (input == null) {
            return "";
        }
        return input.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
