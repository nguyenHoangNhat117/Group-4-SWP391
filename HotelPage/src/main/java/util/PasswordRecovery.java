package util;

import java.io.Serializable;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Access while synchronized on this object. No reset rights before verify().
 */
public final class PasswordRecovery implements Serializable {

    private static final long serialVersionUID = 1L;
    public final int userId;
    public final String expectedPasswordHash;
    private String otp;
    private final long expiresAt;
    private int attempts;
    private long resetExpiresAt;
    private boolean consumed;

    public PasswordRecovery(int userId, String expectedPasswordHash, String otp, long now) {
        this.userId = userId;
        this.expectedPasswordHash = expectedPasswordHash;
        this.otp = otp;
        this.expiresAt = now + 5 * 60 * 1000L;
    }

    public synchronized boolean verify(String supplied, long now) {
        if (consumed || otp == null || now >= expiresAt || attempts >= 5) {
            return false;
        }
        attempts++;
        if (supplied == null || !supplied.matches("[0-9]{6}")) {
            return false;
        }
        if (!MessageDigest.isEqual(otp.getBytes(StandardCharsets.US_ASCII),
                supplied.getBytes(StandardCharsets.US_ASCII))) {
            return false;
        }
        otp = null;
        resetExpiresAt = now + 5 * 60 * 1000L;
        return true;
    }

    public synchronized boolean canReset(long now) {
        return !consumed && resetExpiresAt > 0 && now < resetExpiresAt;
    }

    public synchronized void consume() {
        consumed = true;
        otp = null;
        resetExpiresAt = 0;
    }
}
