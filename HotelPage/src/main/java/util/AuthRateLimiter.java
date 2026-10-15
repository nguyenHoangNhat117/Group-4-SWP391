package util;

import java.util.HashMap;
import java.util.Map;

/**
 * Single JVM, fixed-window limiter. Replace with shared storage for a cluster.
 */
public final class AuthRateLimiter {

    private static final Map<String, long[]> WINDOWS = new HashMap<>();

    private AuthRateLimiter() {
    }

    public static synchronized boolean allow(String key, int limit, long windowMillis) {
        long now = System.currentTimeMillis();
        WINDOWS.entrySet().removeIf(e -> now >= e.getValue()[0]);
        long[] entry = WINDOWS.get(key);
        if (entry == null) {
            if (WINDOWS.size() >= 20000) {
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
}
