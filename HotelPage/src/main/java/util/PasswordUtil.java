package util;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * New passwords use PBKDF2. Existing MD5 accounts keep working.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 210000;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password required");
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        if (stored.matches("(?i)[0-9a-f]{32}")) {
            return MessageDigest.isEqual(legacyMd5(password).getBytes(StandardCharsets.US_ASCII), stored.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
        }
        try {
            String[] p = stored.split("\\$", -1);
            if (p.length != 4 || !"pbkdf2".equals(p[0])) {
                return false;
            }
            int iterations = Integer.parseInt(p[1]);
            if (iterations < 100000 || iterations > 1000000) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(p[2]), expected = Base64.getDecoder().decode(p[3]);
            return salt.length == 16 && expected.length == 32 && MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Only for compatibility with explicitly named legacy callers.
     */
    public static String legacyMd5(String password) {
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            for (byte b : bytes) {
                s.append(String.format("%02x", b & 255));
            }
            return s.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String generateInitialPassword() {
        String[] groups = {"ABCDEFGHJKLMNPQRSTUVWXYZ", "abcdefghijkmnopqrstuvwxyz", "23456789", "!@#$%*-_"};
        List<Character> chars = new ArrayList<>();
        String all = String.join("", groups);
        for (String g : groups) {
            chars.add(g.charAt(RANDOM.nextInt(g.length())));
        }
        while (chars.size() < 16) {
            chars.add(all.charAt(RANDOM.nextInt(all.length())));
        }
        Collections.shuffle(chars, RANDOM);
        StringBuilder s = new StringBuilder();
        for (char c : chars) {
            s.append(c);
        }
        return s.toString();
    }
}
