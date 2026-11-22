package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BookingHistoryDetails {
    private final List<RoomLine> rooms;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final boolean mixedDates;
    private final boolean unassigned;
    private final long guestCount;
    private final BigDecimal roomTotal;
    private final BigDecimal orderTotal;
    private final BigDecimal paid;
    private final BigDecimal refunded;
    private final BigDecimal discountAmount;
    private final String discountCode;

    public BookingHistoryDetails(Booking booking, List<Payment> payments) {
        Map<List<?>, RoomLine> grouped = new LinkedHashMap<>();
        LocalDate first = null;
        LocalDate last = null;
        int guests = 0;
        boolean missingRoom = false;
        for (BookingDetail detail : booking.getDetails()) {
            List<?> key = Arrays.asList(detail.getRoomTypeName(), detail.getCheckInDate(),
                    detail.getCheckOutDate(), detail.getPricePerNight(), detail.getSpecialRequest());
            RoomLine line = grouped.computeIfAbsent(key, ignored -> new RoomLine(detail));
            line.quantity++;
            Integer roomNumber = detail.getRoomNumber();
            if (roomNumber != null && roomNumber > 0) {
                line.roomNumbers.add(roomNumber);
            } else {
                missingRoom = true;
            }
            guests += detail.getGuestCount();
            if (first == null || detail.getCheckInDate().isBefore(first)) {
                first = detail.getCheckInDate();
            }
            if (last == null || detail.getCheckOutDate().isAfter(last)) {
                last = detail.getCheckOutDate();
            }
        }
        rooms = List.copyOf(grouped.values());
        checkIn = first;
        checkOut = last;
        mixedDates = rooms.stream().anyMatch(line -> !line.getCheckIn().equals(checkIn) || !line.getCheckOut().equals(checkOut));
        unassigned = missingRoom;
        guestCount = guests;
        roomTotal = rooms.stream().map(RoomLine::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidTotal = BigDecimal.ZERO;
        BigDecimal refunds = BigDecimal.ZERO;
        BigDecimal savedDiscount = BigDecimal.ZERO;
        BigDecimal pendingDiscount = BigDecimal.ZERO;
        String code = null;
        for (Payment payment : payments) {
            boolean received = "paid".equals(payment.getPaymentStatus()) || "refunded".equals(payment.getPaymentStatus());
            boolean reserved = "pending".equals(booking.getStatus()) && booking.isOnlineDepositPolicy()
                    && "pending".equals(payment.getPaymentStatus());
            if (received) {
                paidTotal = paidTotal.add(payment.getAmount());
                refunds = refunds.add(payment.getRefundedAmount());
            }
            boolean paidAfterCancellation = booking.getCancellationDate() != null && payment.getPaymentDate() != null
                    && payment.getPaymentDate().isAfter(booking.getCancellationDate());
            if ((received && !paidAfterCancellation) || reserved) {
                BigDecimal amount = payment.getDiscountAmount();
                if (payment.getDiscount() != null && code == null) {
                    code = payment.getDiscount().getCode();
                }
                if (amount != null && amount.signum() > 0) {
                    savedDiscount = savedDiscount.add(amount);
                    if (reserved) {
                        pendingDiscount = pendingDiscount.add(amount);
                    }
                }
            }
        }
        paid = paidTotal;
        refunded = refunds;
        discountAmount = savedDiscount;
        discountCode = code;
        // The reserved discount is only deducted from Booking.TotalPrice after payment is verified.
        orderTotal = booking.getTotalPrice().subtract(pendingDiscount).max(BigDecimal.ZERO);
    }

    public List<RoomLine> getRooms() { return rooms; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public long getNights() { return checkIn == null ? 0 : ChronoUnit.DAYS.between(checkIn, checkOut); }
    public boolean isMixedDates() { return mixedDates; }
    public boolean isUnassigned() { return unassigned; }
    public long getGuestCount() { return guestCount; }
    public BigDecimal getRoomTotal() { return roomTotal; }
    public BigDecimal getOrderTotal() { return orderTotal; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getRefunded() { return refunded; }
    public BigDecimal getNetPaid() { return paid.subtract(refunded); }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public String getDiscountCode() { return discountCode; }
    public BigDecimal getAdjustment() { return orderTotal.subtract(roomTotal).add(discountAmount); }
    public BigDecimal getRemaining() { return orderTotal.subtract(getNetPaid()).max(BigDecimal.ZERO); }

    public static final class RoomLine {
        private final BookingDetail detail;
        private final List<Integer> roomNumbers = new ArrayList<>();
        private int quantity;

        private RoomLine(BookingDetail detail) {
            this.detail = detail;
        }

        public String getName() { return detail.getRoomTypeName(); }
        public LocalDate getCheckIn() { return detail.getCheckInDate(); }
        public LocalDate getCheckOut() { return detail.getCheckOutDate(); }
        public String getRequest() { return detail.getSpecialRequest(); }
        public BigDecimal getNightly() { return detail.getPricePerNight(); }
        public long getNights() { return ChronoUnit.DAYS.between(getCheckIn(), getCheckOut()); }
        public int getQuantity() { return quantity; }
        public List<Integer> getRoomNumbers() { return roomNumbers; }
        public BigDecimal getSubtotal() {
            return getNightly().multiply(BigDecimal.valueOf(getNights())).multiply(BigDecimal.valueOf(quantity));
        }
    }
}
