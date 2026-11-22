package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class LanguageSupport {
    private static final String RESOURCE = "/i18n/messages_vi.properties";
    private static final Map<String, String> MESSAGES = loadMessages();

    private LanguageSupport() {
    }

    public static Map<String, String> messages() {
        return MESSAGES;
    }

    private static Map<String, String> loadMessages() {
        Properties properties = new Properties();
        try (InputStream input = LanguageSupport.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing " + RESOURCE);
            }
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load " + RESOURCE, ex);
        }

        Map<String, String> messages = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            messages.put(key, properties.getProperty(key));
        }
        return Collections.unmodifiableMap(messages);
    }

    public static String json(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 32 || c == '<' || c == '>' || c == '&' || c == '\u2028' || c == '\u2029') {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.append('"').toString();
    }
}
