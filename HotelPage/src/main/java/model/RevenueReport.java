package model;

import java.math.BigDecimal;

/**
 * Stores revenue aggregated by a time unit, such as a month or year.
 */
public class RevenueReport {

    private String timeUnit;
    private BigDecimal totalRevenue;

    public RevenueReport(
            String timeUnit,
            BigDecimal totalRevenue) {

        this.timeUnit = timeUnit;
        setTotalRevenue(totalRevenue);
    }

    /**
     * Preserves compatibility with existing code that passes a double.
     */
    public RevenueReport(
            String timeUnit,
            double totalRevenue) {

        this(timeUnit, BigDecimal.valueOf(totalRevenue));
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue == null
                ? BigDecimal.ZERO
                : totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        setTotalRevenue(BigDecimal.valueOf(totalRevenue));
    }
}