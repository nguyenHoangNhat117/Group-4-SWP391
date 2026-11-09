package util;

import java.util.concurrent.ThreadLocalRandom;

/** Keeps provider order codes separate from resettable SQL payment IDs. */
public final class PayOSOrderCode {
    private static final String PREFIX = "HP-PAYOS-";
    private static final long MIN_CODE = 1_000_000_000_000_000L;
    private static final long MAX_CODE_EXCLUSIVE = 9_000_000_000_000_000L;

    private PayOSOrderCode() {
    }

    public static String newTransactionCode() {
        return transactionCode(ThreadLocalRandom.current().nextLong(MIN_CODE, MAX_CODE_EXCLUSIVE));
    }

    public static String transactionCode(long orderCode) {
        return PREFIX + orderCode;
    }

    public static boolean isNewOrder(long orderCode) {
        return orderCode >= MIN_CODE && orderCode < MAX_CODE_EXCLUSIVE;
    }

    public static boolean usesProviderCode(String transactionCode) {
        return transactionCode != null && transactionCode.startsWith(PREFIX);
    }

    public static long forPayment(long paymentId, String transactionCode) {
        return usesProviderCode(transactionCode)
                ? Long.parseLong(transactionCode.substring(PREFIX.length())) : paymentId;
    }
}
