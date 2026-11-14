package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Payment {

    private int id;
    private Booking booking;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal refundedAmount;
    private String historyStatus;
    private BigDecimal historyRemainingAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionCode;
    private Discount discount;

    public Payment() {
    }

    public Payment(
            int id,
            Booking booking,
            LocalDateTime paymentDate,
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus,
            String transactionCode,
            Discount discount) {

        this.id = id;
        this.booking = booking;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionCode = transactionCode;
        this.discount = discount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {

        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount;
    }

    public void setAmount(BigDecimal amount) {

        if (amount == null) {
            this.amount = BigDecimal.ZERO;
        } else {
            this.amount = amount.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getRefundedAmount() {
        // Older full refunds record the status without setting RefundedAmount.
        if ("refunded".equalsIgnoreCase(paymentStatus)) {
            return getAmount();
        }
        return refundedAmount == null ? BigDecimal.ZERO : refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public String getHistoryStatus() {
        return historyStatus == null ? paymentStatus : historyStatus;
    }

    public void setHistoryStatus(String historyStatus) {
        this.historyStatus = historyStatus;
    }

    public BigDecimal getHistoryRemainingAmount() {
        return historyRemainingAmount;
    }

    public void setHistoryRemainingAmount(BigDecimal historyRemainingAmount) {
        this.historyRemainingAmount = historyRemainingAmount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    /*
     * =========================================================
     * HELPER METHODS
     * =========================================================
     */

    /**
     * Kiểm tra payment này đã thanh toán thành công hay chưa.
     */
    public boolean isPaid() {

        return "paid".equalsIgnoreCase(
                paymentStatus
        );
    }

    /**
     * Xác định payment này là thanh toán toàn bộ
     * hay chỉ là thanh toán một phần.
     *
     * Lưu ý:
     * Đây chỉ xét riêng record Payment hiện tại.
     */
    public boolean isFullPayment() {

        if (booking == null
                || booking.getTotalPrice() == null
                || amount == null) {

            return false;
        }

        return amount.compareTo(
                booking.getTotalPrice()
        ) >= 0;
    }

    /**
     * Dùng cho JSP:
     *
     * Full Payment
     * Partial Payment
     */
    public String getPaymentType() {

        if (isFullPayment()) {
            return "Full Payment";
        }

        return "Partial Payment";
    }
}
