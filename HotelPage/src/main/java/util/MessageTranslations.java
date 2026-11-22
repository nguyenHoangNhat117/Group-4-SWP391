package util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class MessageTranslations {
    private static final String LEGACY_PREFIX = "legacy.";
    private static final Map<String, String> LEGACY_MESSAGES = loadLegacyMessages();

    private MessageTranslations() {
    }

    private static Map<String, String> loadLegacyMessages() {
        Map<String, String> messages = LanguageSupport.messages();
        Map<String, String> legacy = new HashMap<>();
        messages.forEach((key, value) -> {
            if (key.startsWith(LEGACY_PREFIX)) {
                legacy.put(key.substring(LEGACY_PREFIX.length()), messages.get(value));
            }
        });
        return Map.copyOf(legacy);
    }

    public static String text(Object value) {
        if (value == null) {
            return "";
        }
        String original = value.toString();
        String normalized = original.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return LEGACY_MESSAGES.getOrDefault(normalized, original);
    }

    public static String html(Object value) {
        return AuthSupport.html(text(value));
    }

    public static String js(Object value) {
        return LanguageSupport.json(text(value));
    }
}
