package util;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// Updated by Nguyen Hoang Nhat
public final class PasswordRecovery implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MAX_ATTEMPTS = 3;
    private static final long OTP_LIFETIME = 5 * 60 * 1000L;
    private static final long RESET_LIFETIME = 5 * 60 * 1000L;

    public final int userId;
    public final String expectedPasswordHash;

    private String otp;
    private final long expiresAt;

    private int attempts;
    private long resetExpiresAt;
    private boolean consumed;

    public PasswordRecovery(
            int userId,
            String expectedPasswordHash,
            String otp,
            long now
    ) {
        this.userId = userId;
        this.expectedPasswordHash = expectedPasswordHash;
        this.otp = otp;
        this.expiresAt = now + OTP_LIFETIME;
    }

    public synchronized boolean verify(String supplied, long now) {
        if (!isOtpActive(now)) {
            return false;
        }

        // Every submitted code consumes one attempt, including bad formats.
        attempts++;

        boolean matches = supplied != null
                && supplied.matches("[0-9]{6}")
                && MessageDigest.isEqual(
                        otp.getBytes(StandardCharsets.US_ASCII),
                        supplied.getBytes(StandardCharsets.US_ASCII)
                );

        if (!matches) {
            if (attempts >= MAX_ATTEMPTS) {
                otp = null;
            }

            return false;
        }

        // A verified OTP cannot be used again.
        otp = null;
        resetExpiresAt = now + RESET_LIFETIME;

        return true;
    }

    public synchronized boolean isOtpActive(long now) {
        return !consumed
                && otp != null
                && now < expiresAt
                && attempts < MAX_ATTEMPTS;
    }

    public synchronized int getRemainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    public synchronized boolean canReset(long now) {
        return !consumed
                && resetExpiresAt > 0
                && now < resetExpiresAt;
    }

    public synchronized void consume() {
        consumed = true;
        otp = null;
        resetExpiresAt = 0;
    }
}
