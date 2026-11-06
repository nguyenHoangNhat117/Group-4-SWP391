package util;

import java.util.HashMap;
import java.util.Map;

public final class MessageTranslations {
    private static final Map<String, String> KEYS = build();
    private MessageTranslations() {}
    private static String normalize(Object text) {
        return text == null ? "" : text.toString().trim().replaceAll("\\s+", " ");
    }
    private static Map<String, String> build() {
        Map<String, String> result = new HashMap<>();
        for (String language : new String[]{"en", "vi"}) {
            for (Map.Entry<String, String> entry : LanguageSupport.messages(language).entrySet()) {
                result.put(normalize(entry.getValue()), entry.getKey());
            }
        }
        return result;
    }
    public static String key(Object text) {
        String normalized = normalize(text);
        String exact = KEYS.get(normalized);
        if (exact != null) return exact;
        for (Map.Entry<String, String> entry : KEYS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(normalized)) return entry.getValue();
        }
        return null;
    }
    public static String text(Object text, Object language) {
        String key = key(text);
        return key == null ? (text == null ? "" : text.toString())
                : LanguageSupport.messages(String.valueOf(language)).get(key);
    }
    public static String html(Object value, Object language) {
        String key = key(value);
        String escaped = AuthSupport.html(text(value, language));
        return key == null ? escaped : "<span data-i18n=\"" + key + "\">" + escaped + "</span>";
    }
    public static String js(Object value, Object language) {
        return LanguageSupport.json(text(value, language));
    }
}
