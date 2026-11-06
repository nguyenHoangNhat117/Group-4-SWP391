package util;

public final class OfflineBookingPolicy {

    public static final int HOLD_MINUTES
            = setting("uc09.holdMinutes", 30, 1, 1440);

    public static final int DEPOSIT_PERCENT
            = setting("uc09.depositPercent", 20, 1, 100);

    private OfflineBookingPolicy() {
    }

    private static int setting(
            String name,
            int fallback,
            int min,
            int max
    ) {
        String value = System.getProperty(name);

        if (value == null || value.isBlank()) {
            return fallback;
        }

        int parsed = Integer.parseInt(value);

        if (parsed < min || parsed > max) {
            throw new IllegalArgumentException("Invalid " + name);
        }

        return parsed;
    }
}
