package util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

public final class LanguageSupport {

    public static final String SESSION_KEY = "uiLanguage";
    public static final String COOKIE_NAME = "hotel_language";

    private static final String TOKEN_KEY = "uc24Csrf";

    private static final SecureRandom RANDOM
            = new SecureRandom();

    private static final Logger LOG
            = Logger.getLogger(LanguageSupport.class.getName());

    private static final Map<String, String> EN
            = load("messages_en.properties");

    private static final Map<String, String> VI
            = loadVietnamese();

    private LanguageSupport() {
    }

    public static boolean supported(String language) {
        return "en".equals(language)
                || "vi".equals(language);
    }

    private static Map<String, String> load(String fileName) {
        Properties properties = new Properties();

        try (InputStream input
                = LanguageSupport.class.getResourceAsStream(
                        "/i18n/" + fileName
                )) {

                    if (input == null) {
                        throw new IllegalStateException(
                                "Missing translation file: i18n/" + fileName
                        );
                    }

                    properties.load(
                            new InputStreamReader(
                                    input,
                                    StandardCharsets.UTF_8
                            )
                    );

                } catch (IOException ex) {
                    throw new IllegalStateException(
                            "Cannot load translation file: " + fileName,
                            ex
                    );
                }

                Map<String, String> result = new LinkedHashMap<>();

                for (String key
                        : new TreeSet<>(properties.stringPropertyNames())) {

                    result.put(
                            key,
                            properties.getProperty(key)
                    );
                }

                return Collections.unmodifiableMap(result);
    }

    private static Map<String, String> loadVietnamese() {
        Map<String, String> translated;

        try {
            translated = load("messages_vi.properties");

        } catch (IllegalStateException ex) {
            LOG.warning(
                    "Vietnamese translations unavailable. "
                    + "Using English."
            );

            translated = Collections.emptyMap();
        }

        Map<String, String> result
                = new LinkedHashMap<>(EN);

        for (String key : EN.keySet()) {
            String value = translated.get(key);

            if (value == null || value.isBlank()) {
                LOG.warning(
                        "Missing Vietnamese translation: " + key
                );

            } else {
                result.put(key, value);
            }
        }

        // Không bỏ mất khóa chỉ được bổ sung ở bản VI. Kiểm tra bundle trước
        // khi bàn giao vẫn phải yêu cầu bản EN tương ứng.
        for (Map.Entry<String, String> entry : translated.entrySet()) {
            if (!EN.containsKey(entry.getKey())) {
                LOG.warning("Missing English translation: " + entry.getKey());
                if (!entry.getValue().isBlank()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    public static Map<String, String> messages(
            String language
    ) {
        return "vi".equals(language) ? VI : EN;
    }

    /**
     * Priority: 1. Session 2. Valid preference cookie 3. English
     */
    public static String resolve(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            try {
                Object saved = session.getAttribute(SESSION_KEY);
                if (saved instanceof String && supported((String) saved)) {
                    return (String) saved;
                }
            } catch (IllegalStateException ignored) {
                // Đăng xuất ở tab khác: tiếp tục đọc cookie, không gây lỗi 500.
            }
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())
                        && supported(cookie.getValue())) {

                    return cookie.getValue();
                }
            }
        }

        return "en";
    }

    public static String token(HttpSession session) {
        synchronized (session) {
            Object saved
                    = session.getAttribute(TOKEN_KEY);

            if (saved instanceof String) {
                return (String) saved;
            }

            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);

            String token = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(bytes);

            session.setAttribute(TOKEN_KEY, token);

            return token;
        }
    }

    public static boolean validToken(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);

        Object saved = session == null
                ? null
                : session.getAttribute(TOKEN_KEY);

        String supplied
                = request.getParameter("csrfToken");

        return saved instanceof String
                && supplied != null
                && supplied.length() <= 100
                && MessageDigest.isEqual(
                        ((String) saved).getBytes(
                                StandardCharsets.UTF_8
                        ),
                        supplied.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    public static void save(
            HttpServletRequest request,
            HttpServletResponse response,
            String language
    ) {
        if (!supported(language)) {
            throw new IllegalArgumentException(
                    "Unsupported language"
            );
        }

        request.getSession().setAttribute(
                SESSION_KEY,
                language
        );

        Cookie cookie = new Cookie(
                COOKIE_NAME,
                language
        );

        cookie.setPath(
                request.getContextPath().isEmpty()
                ? "/"
                : request.getContextPath()
        );

        cookie.setMaxAge(365 * 24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);
    }

    public static String json(String value) {
        StringBuilder result
                = new StringBuilder("\"");

        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);

            switch (character) {
                case '"':
                    result.append("\\\"");
                    break;

                case '\\':
                    result.append("\\\\");
                    break;

                case '\n':
                    result.append("\\n");
                    break;

                case '\r':
                    result.append("\\r");
                    break;

                case '\t':
                    result.append("\\t");
                    break;

                default:
                    if (character < 32
                            || character == '<'
                            || character == '>'
                            || character == '&'
                            || character == '\u2028'
                            || character == '\u2029') {

                        result.append(
                                String.format(
                                        "\\u%04x",
                                        (int) character
                                )
                        );

                    } else {
                        result.append(character);
                    }
            }
        }

        return result.append('"').toString();
    }
}
