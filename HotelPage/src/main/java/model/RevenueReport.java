package model;

import java.math.BigDecimal;

public class RevenueReport {

    private String timeUnit;
    private BigDecimal totalRevenue;
    private BigDecimal collectedAmount = BigDecimal.ZERO;
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    public RevenueReport(
            String timeUnit,
            BigDecimal collected,
            BigDecimal refunded) {

        this.timeUnit = timeUnit;
        this.collectedAmount = collected == null
                ? BigDecimal.ZERO : collected;
        this.refundedAmount = refunded == null
                ? BigDecimal.ZERO : refunded;

        setTotalRevenue(
                this.collectedAmount.subtract(this.refundedAmount)
        );
    }

    public RevenueReport(String timeUnit, BigDecimal totalRevenue) {
        this.timeUnit = timeUnit;
        setTotalRevenue(totalRevenue);
    }

    public RevenueReport(String timeUnit, double totalRevenue) {
        this(timeUnit, BigDecimal.valueOf(totalRevenue));
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public BigDecimal getCollectedAmount() {
        return collectedAmount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue == null
                ? BigDecimal.ZERO : totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        setTotalRevenue(BigDecimal.valueOf(totalRevenue));
    }
}