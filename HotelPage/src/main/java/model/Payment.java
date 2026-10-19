package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Payment {

    private int id;
    private Booking booking;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
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