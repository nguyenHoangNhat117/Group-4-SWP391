/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.math.BigDecimal;
/**
 *
 * @author Nguyen Hoang Nhat
 */
public class ReportStatistic {

    private final String status;
    private final long totalCount;
    private final BigDecimal totalAmount;

    public ReportStatistic(
            String status,
            long totalCount,
            BigDecimal totalAmount) {

        this.status = status;
        this.totalCount = totalCount;
        this.totalAmount = totalAmount == null
                ? BigDecimal.ZERO
                : totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
