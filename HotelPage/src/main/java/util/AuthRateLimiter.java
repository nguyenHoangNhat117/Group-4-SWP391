package util;

import java.util.HashMap;
import java.util.Map;

// Updated by Nguyen Hoang Nhat
public final class AuthRateLimiter {

    private static final int MAX_ENTRIES = 20000;

    private static final int MAX_LOGIN_FAILURES = 5;
    private static final long LOGIN_WINDOW_MILLIS = 15 * 60 * 1000L;
    private static final long LOGIN_BLOCK_MILLIS = 15 * 60 * 1000L;

    private static final Map<String, long[]> WINDOWS = new HashMap<>();

    private static final Map<String, LoginFailures> LOGIN_FAILURES
            = new HashMap<>();

    private AuthRateLimiter() {
        // Utility class: prevent creating instances.
    }

    /*
     * Preserve the existing request limiter for registration and OTP.
     * This counts requests, not failed login attempts.
     */
    public static synchronized boolean allow(
            String key,
            int limit,
            long windowMillis
    ) {
        if (key == null || limit <= 0 || windowMillis <= 0) {
            throw new IllegalArgumentException(
                    "Invalid rate limit configuration."
            );
        }

        long now = System.currentTimeMillis();

        WINDOWS.entrySet().removeIf(
                entry -> now >= entry.getValue()[0]
        );

        long[] entry = WINDOWS.get(key);

        if (entry == null) {
            if (WINDOWS.size() >= MAX_ENTRIES) {
                return false;
            }

            entry = new long[]{now + windowMillis, 0};
            WINDOWS.put(key, entry);
        }

        if (entry[1] >= limit) {
            return false;
        }

        entry[1]++;
        return true;
    }

    public static synchronized boolean isLoginBlocked(String key) {
        long now = System.currentTimeMillis();
        cleanLoginFailures(now);

        LoginFailures entry = LOGIN_FAILURES.get(key);

        if (entry != null) {
            return entry.blockedUntil > now;
        }

        // Reject new tracking entries when storage is full.
        return LOGIN_FAILURES.size() >= MAX_ENTRIES;
    }

    public static synchronized void recordLoginFailure(String key) {
        long now = System.currentTimeMillis();
        cleanLoginFailures(now);

        LoginFailures entry = LOGIN_FAILURES.get(key);

        if (entry == null) {
            if (LOGIN_FAILURES.size() >= MAX_ENTRIES) {
                return;
            }

            entry = new LoginFailures(now);
            LOGIN_FAILURES.put(key, entry);
        }

        if (entry.blockedUntil > now) {
            return;
        }

        entry.count++;

        // The fifth failure starts a new 15-minute blocking period.
        if (entry.count >= MAX_LOGIN_FAILURES) {
            entry.blockedUntil = now + LOGIN_BLOCK_MILLIS;
        }
    }

    public static synchronized void clearLoginFailures(String key) {
        LOGIN_FAILURES.remove(key);
    }

    private static void cleanLoginFailures(long now) {
        LOGIN_FAILURES.entrySet().removeIf(item -> {
            LoginFailures entry = item.getValue();

            if (entry.blockedUntil > 0) {
                return now >= entry.blockedUntil;
            }

            return now - entry.firstFailureAt >= LOGIN_WINDOW_MILLIS;
        });
    }

    private static final class LoginFailures {

        private final long firstFailureAt;
        private int count;
        private long blockedUntil;

        private LoginFailures(long firstFailureAt) {
            this.firstFailureAt = firstFailureAt;
        }
    }
}
