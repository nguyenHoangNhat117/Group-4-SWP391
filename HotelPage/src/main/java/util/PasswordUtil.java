package util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * MD5 UTF-8, kept intentionally for compatibility with this project.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Returns exactly 32 lowercase hexadecimal characters.
     */
    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password is required");
        }
        try {
            byte[] bytes = MessageDigest.getInstance("MD5")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(32);
            for (byte b : bytes) {
                result.append(String.format("%02x", b & 255));
            }
            return result.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("MD5 unavailable", e);
        }
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null || !stored.matches("(?i)[0-9a-f]{32}")) {
            return false;
        }
        return MessageDigest.isEqual(hash(password).getBytes(StandardCharsets.US_ASCII),
                stored.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Alias retained for existing callers.
     */
    public static String legacyMd5(String password) {
        return hash(password);
    }
}
