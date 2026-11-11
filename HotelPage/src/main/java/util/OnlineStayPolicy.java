package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class OnlineStayPolicy {
    public static final int DEPOSIT_PERCENT = 50;
    public static final int FULL_PERCENT = 100;

    private OnlineStayPolicy() {
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

    public static boolean freeCancellation(LocalDate checkIn, LocalDate today) {
        return !today.isAfter(checkIn.minusDays(7));
    }

    public static BigDecimal refund(BigDecimal total, BigDecimal paid, LocalDate checkIn, LocalDate today) {
        return freeCancellation(checkIn, today) ? paid : paid.subtract(deposit(total)).max(BigDecimal.ZERO);
    }
}
