package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Discount {

    private int id;
    private String code;
    private int quantity;
    private BigDecimal saleOff;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal minimumAmount;
    private BigDecimal maximumDiscount;

    private boolean active;

    public Discount() {
    }

    /*
     * Giữ constructor cũ để những file cũ chưa sửa
     * vẫn có thể compile.
     */
    public Discount(
            int id,
            String code,
            int quantity,
            BigDecimal saleOff) {

        this.id = id;
        this.code = code;
        this.quantity = quantity;
        this.saleOff = saleOff;
    }

    public Discount(
            int id,
            String code,
            int quantity,
            BigDecimal saleOff,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minimumAmount,
            BigDecimal maximumDiscount,
            boolean active) {

        this.id = id;
        this.code = code;
        this.quantity = quantity;
        this.saleOff = saleOff;
        this.startDate = startDate;
        this.endDate = endDate;
        this.minimumAmount = minimumAmount;
        this.maximumDiscount = maximumDiscount;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSaleOff() {
        return saleOff;
    }

    public void setSaleOff(BigDecimal saleOff) {
        this.saleOff = saleOff;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(BigDecimal minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public BigDecimal getMaximumDiscount() {
        return maximumDiscount;
    }

    public void setMaximumDiscount(
            BigDecimal maximumDiscount) {

        this.maximumDiscount = maximumDiscount;
    }

    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}