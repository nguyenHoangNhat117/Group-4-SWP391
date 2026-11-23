package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class OnlineStayPolicy {
    public static final int DEPOSIT_PERCENT = 50;
    public static final int FULL_PERCENT = 100;
    public static final int HOLD_MINUTES = 15;

    private OnlineStayPolicy() {
    }

    public static boolean fitsParty(int adults, int children, long adultCapacity, long childCapacity) {
        return adults >= 1 && children >= 0 && adultCapacity >= adults
                && childCapacity >= children;
    }

    public static Integer percentage(String plan) {
        if ("deposit".equals(plan)) {
            return DEPOSIT_PERCENT;
        }
        return "transfer".equals(plan) ? FULL_PERCENT : null;
    }

    public static BigDecimal deposit(BigDecimal total) {
        return total.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
    }

    public static boolean canDeposit(BigDecimal total) {
        return total.signum() > 0 && total.remainder(BigDecimal.valueOf(2)).signum() == 0;
    }

    public static BigDecimal voucherDiscount(BigDecimal total, BigDecimal rate, BigDecimal maximum) {
        BigDecimal discount = total.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return maximum == null ? discount : discount.min(maximum);
    }

    public static boolean freeCancellation(LocalDate checkIn, LocalDate today) {
        return !today.isAfter(checkIn.minusDays(7));
    }

    public static BigDecimal refund(BigDecimal total, BigDecimal paid, LocalDate checkIn, LocalDate today) {
        return freeCancellation(checkIn, today) ? paid : paid.subtract(deposit(total)).max(BigDecimal.ZERO);
    }
}
