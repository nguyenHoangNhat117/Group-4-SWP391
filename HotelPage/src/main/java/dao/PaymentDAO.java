package dao;

import db.DBContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.Booking;
import model.Customer;
import model.Discount;
import model.Payment;

public class PaymentDAO extends DBContext {

    public static final String PAYMENT_SUCCESS
            = "success";

    public static final String INVALID_BOOKING
            = "invalid-booking";

    public static final String BOOKING_NOT_PENDING
            = "booking-not-pending";

    public static final String ALREADY_PAID
            = "already-paid";

    public static final String INVALID_METHOD
            = "invalid-method";

    public static final String INVALID_DISCOUNT
            = "invalid-discount";

    public static final String PAYMENT_FAILED
            = "payment-failed";

    /*
     * =========================================================
     * PROCESS PAYMENT TRANSACTION
     * =========================================================
     */
    public String processPayment(
            int bookingId,
            int customerId,
            String paymentMethod,
            String discountCode,
            Integer paymentPercent
    ) {
        return processPayment(
                bookingId, customerId, paymentMethod,
                discountCode, paymentPercent,
                null, null, null, null, null
        );
    }

    public String processCounterPayment(
            int bookingId,
            int actor,
            String method,
            Integer percent,
            String key,
            BigDecimal expectedPaid,
            BigDecimal expectedTotal,
            BigDecimal expectedPayable
    ) {
        if (key == null
                || !key.matches("[0-9a-fA-F-]{36}")
                || expectedPaid == null
                || expectedTotal == null
                || expectedPayable == null) {
            return PAYMENT_FAILED;
        }

        return processPayment(
                bookingId, 0, method, null, percent,
                actor, key, expectedPaid,
                expectedTotal, expectedPayable
        );
    }

    private String processPayment(
            int bookingId,
            int customerId,
            String paymentMethod,
            String discountCode,
            Integer paymentPercent,
            Integer actor,
            String requestKey,
            BigDecimal expectedPaid,
            BigDecimal expectedTotal,
            BigDecimal expectedPayable
    ) {
        if (!isValidPaymentMethod(paymentMethod)) {
            return INVALID_METHOD;
        }

        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            if (actor != null) {
                String actorSql
                        = "SELECT UserID FROM [User] WITH(HOLDLOCK) "
                        + "WHERE UserID=? "
                        + "AND Role IN ('staff','admin') "
                        + "AND AccountStatus='active'";

                try (PreparedStatement ps
                        = conn.prepareStatement(actorSql)) {
                    ps.setInt(1, actor);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return INVALID_BOOKING;
                        }
                    }
                }
            }

            String bookingSql
                    = "SELECT BookingID,CustomerID,TotalPrice,Status,"
                    + "OfflinePromotionCode,HoldExpiresAt,"
                    + "OfflineDepositPercent "
                    + "FROM Booking WITH(UPDLOCK,HOLDLOCK) "
                    + "WHERE BookingID=? "
                    + (actor == null
                            ? "AND CustomerID=?"
                            : "AND CreatedByUserID IS NOT NULL");

            BigDecimal bookingTotal;
            String bookingStatus;
            boolean offline;
            int minimumPercent;
            String savedPromotion;

            try (PreparedStatement ps
                    = conn.prepareStatement(bookingSql)) {
                ps.setInt(1, bookingId);

                if (actor == null) {
                    ps.setInt(2, customerId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return INVALID_BOOKING;
                    }

                    bookingTotal = rs.getBigDecimal("TotalPrice");
                    bookingStatus = rs.getString("Status");
                    offline = rs.getTimestamp("HoldExpiresAt") != null;
                    minimumPercent = offline
                            ? rs.getInt("OfflineDepositPercent") : 0;
                    savedPromotion
                            = rs.getString("OfflinePromotionCode");
                }
            }

            if (actor != null) {
                String duplicateSql
                        = "SELECT PaymentID FROM Payment "
                        + "WHERE BookingID=? AND TransactionCode=? "
                        + "AND PaymentStatus='paid'";

                try (PreparedStatement ps
                        = conn.prepareStatement(duplicateSql)) {
                    ps.setInt(1, bookingId);
                    ps.setString(2, "COUNTER-" + requestKey);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            conn.commit();
                            return PAYMENT_SUCCESS;
                        }
                    }
                }

                if (expectedTotal.compareTo(bookingTotal) != 0) {
                    conn.rollback();
                    return "stale-payment";
                }

                if (!java.util.Arrays.asList(
                        "cash", "bank_transfer"
                ).contains(paymentMethod)) {
                    conn.rollback();
                    return INVALID_METHOD;
                }

                if (!java.util.Arrays.asList(
                        "pending", "confirmed", "checked_in"
                ).contains(bookingStatus)) {
                    conn.rollback();
                    return BOOKING_NOT_PENDING;
                }
            }

            if ("cancelled".equalsIgnoreCase(bookingStatus)) {
                conn.rollback();
                return BOOKING_NOT_PENDING;
            }

            /*
         * Kiểm tra thời gian SQL sau khi đã lấy khóa booking.
         * Áp dụng cả khi Customer thanh toán booking offline.
             */
            if (offline && "pending".equals(bookingStatus)) {
                String expiredSql
                        = "SELECT 1 FROM Booking WHERE BookingID=? "
                        + "AND HoldExpiresAt<=SYSUTCDATETIME()";

                try (PreparedStatement ps
                        = conn.prepareStatement(expiredSql)) {
                    ps.setInt(1, bookingId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String cancelSql
                                    = "UPDATE Booking "
                                    + "SET Status='cancelled',"
                                    + "CancellationDate=SYSDATETIME(),"
                                    + "CancellationReason="
                                    + "N'Hết hạn thanh toán' "
                                    + "WHERE BookingID=? "
                                    + "AND NOT EXISTS("
                                    + "SELECT 1 FROM Payment "
                                    + "WHERE BookingID=? "
                                    + "AND PaymentStatus='paid')";

                            try (PreparedStatement cancel
                                    = conn.prepareStatement(cancelSql)) {
                                cancel.setInt(1, bookingId);
                                cancel.setInt(2, bookingId);
                                cancel.executeUpdate();
                            }

                            conn.commit();
                            return "hold-expired";
                        }
                    }
                }
            }

            BigDecimal totalPaid = BigDecimal.ZERO;

            String paidSql
                    = "SELECT COALESCE(SUM(Amount),0) AS TotalPaid "
                    + "FROM Payment WITH(UPDLOCK,HOLDLOCK) "
                    + "WHERE BookingID=? AND PaymentStatus='paid'";

            try (PreparedStatement ps
                    = conn.prepareStatement(paidSql)) {
                ps.setInt(1, bookingId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalPaid = rs.getBigDecimal("TotalPaid");
                    }
                }
            }

            if (actor != null
                    && expectedPaid.compareTo(totalPaid) != 0) {
                conn.rollback();
                return "stale-payment";
            }

            boolean firstPayment = totalPaid.signum() == 0;

            if (offline) {
                discountCode = firstPayment ? savedPromotion : null;
            }

            if (offline && firstPayment
                    && (paymentPercent == null
                    || (paymentPercent != minimumPercent
                    && paymentPercent != 100))) {
                conn.rollback();
                return "invalid-deposit";
            }

            if (actor != null && !firstPayment) {
                discountCode = null;
            }

            Integer discountId = null;

            if (discountCode != null
                    && !discountCode.trim().isEmpty()) {
                if (!firstPayment) {
                    conn.rollback();
                    return INVALID_DISCOUNT;
                }

                String discountSql
                        = "SELECT DiscountID,SaleOff,MaximumDiscount "
                        + "FROM Discount WITH(UPDLOCK,HOLDLOCK) "
                        + "WHERE Code=? AND IsActive=1 AND Quantity>0 "
                        + "AND SaleOff BETWEEN 0 AND 100 "
                        + "AND (MaximumDiscount IS NULL "
                        + "OR MaximumDiscount>=0) "
                        + "AND (StartDate IS NULL OR EndDate IS NULL "
                        + "OR StartDate<=EndDate) "
                        + "AND (StartDate IS NULL OR StartDate<=?) "
                        + "AND (EndDate IS NULL OR EndDate>=?) "
                        + "AND (MinimumAmount IS NULL OR "
                        + "(MinimumAmount>=0 AND ?>=MinimumAmount))";

                try (PreparedStatement ps
                        = conn.prepareStatement(discountSql)) {
                    ps.setString(1, discountCode.trim());
                    ps.setObject(2, util.ReportPeriod.today());
                    ps.setObject(3, util.ReportPeriod.today());
                    ps.setBigDecimal(4, bookingTotal);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return INVALID_DISCOUNT;
                        }

                        discountId = rs.getInt("DiscountID");

                        BigDecimal discountAmount = bookingTotal
                                .multiply(rs.getBigDecimal("SaleOff"))
                                .divide(
                                        BigDecimal.valueOf(100),
                                        2,
                                        RoundingMode.HALF_UP
                                );

                        BigDecimal maximumDiscount
                                = rs.getBigDecimal("MaximumDiscount");

                        if (maximumDiscount != null) {
                            discountAmount
                                    = discountAmount.min(maximumDiscount);
                        }

                        bookingTotal = bookingTotal
                                .subtract(discountAmount)
                                .max(BigDecimal.ZERO);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Booking SET TotalPrice=? "
                        + "WHERE BookingID=?"
                )) {
                    ps.setBigDecimal(1, bookingTotal);
                    ps.setInt(2, bookingId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return PAYMENT_FAILED;
                    }
                }
            }

            if (actor != null
                    && expectedPayable.compareTo(bookingTotal) != 0) {
                conn.rollback();
                return "stale-payment";
            }

            BigDecimal remainingAmount
                    = bookingTotal.subtract(totalPaid);

            boolean freeFirstCounterPayment
                    = actor != null
                    && firstPayment
                    && "pending".equals(bookingStatus)
                    && bookingTotal.signum() == 0;

            if (remainingAmount.signum() <= 0
                    && !freeFirstCounterPayment) {
                conn.rollback();
                return ALREADY_PAID;
            }

            BigDecimal paymentAmount;

            if (firstPayment) {
                boolean validPercent = paymentPercent != null
                        && (paymentPercent == 10
                        || paymentPercent == 20
                        || paymentPercent == 100
                        || (offline
                        && paymentPercent == minimumPercent));

                if (!validPercent) {
                    conn.rollback();
                    return PAYMENT_FAILED;
                }

                paymentAmount = bookingTotal
                        .multiply(BigDecimal.valueOf(paymentPercent))
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );
            } else {
                paymentAmount = remainingAmount;
            }

            paymentAmount = paymentAmount.min(remainingAmount);

            String transactionCode = actor != null
                    ? "COUNTER-" + requestKey
                    : "TXN-" + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .toUpperCase();

            String insertSql
                    = "INSERT Payment("
                    + "BookingID,PaymentDate,Amount,PaymentMethod,"
                    + "PaymentStatus,TransactionCode,DiscountID"
                    + ") VALUES(?,SYSDATETIME(),?,?,'paid',?,?)";

            try (PreparedStatement ps
                    = conn.prepareStatement(insertSql)) {
                ps.setInt(1, bookingId);
                ps.setBigDecimal(2, paymentAmount);
                ps.setString(3, paymentMethod);
                ps.setString(4, transactionCode);

                if (discountId == null) {
                    ps.setNull(5, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(5, discountId);
                }

                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return PAYMENT_FAILED;
                }
            }

            if ("pending".equalsIgnoreCase(bookingStatus)) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Booking SET Status='confirmed' "
                        + "WHERE BookingID=? AND Status='pending'"
                )) {
                    ps.setInt(1, bookingId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return PAYMENT_FAILED;
                    }
                }
            }

            if (discountId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Discount SET Quantity=Quantity-1 "
                        + "WHERE DiscountID=? AND Quantity>0"
                )) {
                    ps.setInt(1, discountId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return INVALID_DISCOUNT;
                    }
                }
            }

            conn.commit();
            return PAYMENT_SUCCESS;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }

            e.printStackTrace();
            return PAYMENT_FAILED;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }

                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * =========================================================
     * IS BOOKING PAID
     *
     * Old DAO only checked whether any Payment existed.
     * Now only PaymentStatus = paid counts.
     * =========================================================
     */
    public BigDecimal getTotalPaidAmount(int bookingId) {

        String sql
                = "SELECT COALESCE(SUM(Amount), 0) AS TotalPaid "
                + "FROM Payment "
                + "WHERE BookingID = ? AND PaymentStatus = 'paid'";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("TotalPaid");
                    return total == null ? BigDecimal.ZERO : total;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public boolean isBookingPaid(int bookingId) {

        String sql
                = "SELECT b.TotalPrice, "
                + "COALESCE(SUM(CASE WHEN p.PaymentStatus = 'paid' "
                + "THEN p.Amount ELSE 0 END), 0) AS TotalPaid "
                + "FROM Booking b "
                + "LEFT JOIN Payment p ON p.BookingID = b.BookingID "
                + "WHERE b.BookingID = ? "
                + "GROUP BY b.TotalPrice";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal totalPrice = rs.getBigDecimal("TotalPrice");
                    BigDecimal totalPaid = rs.getBigDecimal("TotalPaid");
                    return totalPrice != null
                            && totalPaid != null
                            && totalPaid.compareTo(totalPrice) >= 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
     * =========================================================
     * PAYMENT DETAILS FOR ONE BOOKING (STAFF / ADMIN)
     * =========================================================
     */
    public List<Payment> getPaymentsByBookingId(int bookingId) {

        List<Payment> list = new ArrayList<>();

        String sql
                = "SELECT "
                + "p.PaymentID, p.PaymentDate, p.Amount, p.PaymentMethod, "
                + "p.PaymentStatus, p.TransactionCode, p.DiscountID, "
                + "b.BookingID, b.BookingDate, b.Status AS BookingStatus, "
                + "b.TotalPrice, b.SpecialRequest, b.CancellationDate, b.CancellationReason, "
                + "c.CustomerID, c.FirstName, c.LastName, "
                + "d.Code AS DiscountCode, d.Quantity AS DiscountQuantity, d.SaleOff, "
                + "d.StartDate, d.EndDate, d.MinimumAmount, d.MaximumDiscount, d.IsActive "
                + "FROM Payment p "
                + "JOIN Booking b ON b.BookingID = p.BookingID "
                + "JOIN Customer c ON c.CustomerID = b.CustomerID "
                + "LEFT JOIN Discount d ON d.DiscountID = p.DiscountID "
                + "WHERE p.BookingID = ? "
                + "ORDER BY p.PaymentDate DESC, p.PaymentID DESC";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /*
     * =========================================================
     * CUSTOMER PAYMENT HISTORY
     * =========================================================
     */
    public List<Payment> getPaymentsByCustomerId(
            int customerId) {

        return getPaymentsByCustomerId(
                customerId,
                1,
                Integer.MAX_VALUE
        );
    }

    public List<Payment> getPaymentsByCustomerId(
            int customerId,
            int page,
            int pageSize) {

        List<Payment> list
                = new ArrayList<>();

        if (page < 1) {
            page = 1;
        }

        if (pageSize <= 0) {
            pageSize = 5;
        }

        String sql
                = "SELECT "
                + "p.PaymentID, "
                + "p.PaymentDate, "
                + "p.Amount, "
                + "p.PaymentMethod, "
                + "p.PaymentStatus, "
                + "p.TransactionCode, "
                + "p.DiscountID, "
                + "b.BookingID, "
                + "b.BookingDate, "
                + "b.Status AS BookingStatus, "
                + "b.TotalPrice, "
                + "b.SpecialRequest, "
                + "b.CancellationDate, "
                + "b.CancellationReason, "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "d.Code AS DiscountCode, "
                + "d.Quantity AS DiscountQuantity, "
                + "d.SaleOff, "
                + "d.StartDate, "
                + "d.EndDate, "
                + "d.MinimumAmount, "
                + "d.MaximumDiscount, "
                + "d.IsActive "
                + "FROM Payment p "
                + "JOIN Booking b "
                + "ON b.BookingID = p.BookingID "
                + "JOIN Customer c "
                + "ON c.CustomerID = b.CustomerID "
                + "LEFT JOIN Discount d "
                + "ON d.DiscountID = p.DiscountID "
                + "WHERE b.CustomerID = ? "
                + "ORDER BY "
                + "p.PaymentDate DESC, "
                + "p.PaymentID DESC "
                + "OFFSET ? ROWS "
                + "FETCH NEXT ? ROWS ONLY";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    customerId
            );

            ps.setInt(
                    2,
                    (page - 1) * pageSize
            );

            ps.setInt(
                    3,
                    pageSize
            );

            try (ResultSet rs
                    = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(
                            mapPayment(rs)
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /*
     * =========================================================
     * COUNT CUSTOMER PAYMENTS
     * =========================================================
     */
    public int countPaymentsByCustomerId(
            int customerId) {

        String sql
                = "SELECT COUNT(*) AS Total "
                + "FROM Payment p "
                + "JOIN Booking b "
                + "ON b.BookingID = p.BookingID "
                + "WHERE b.CustomerID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    customerId
            );

            try (ResultSet rs
                    = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(
                            "Total"
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /*
     * =========================================================
     * ALL PAYMENTS
     * Used by reports.
     * =========================================================
     */
    public List<Payment> getAllPayments() {

        List<Payment> list
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "p.PaymentID, "
                + "p.PaymentDate, "
                + "p.Amount, "
                + "p.PaymentMethod, "
                + "p.PaymentStatus, "
                + "p.TransactionCode, "
                + "p.DiscountID, "
                + "b.BookingID, "
                + "b.BookingDate, "
                + "b.Status AS BookingStatus, "
                + "b.TotalPrice, "
                + "b.SpecialRequest, "
                + "b.CancellationDate, "
                + "b.CancellationReason, "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "d.Code AS DiscountCode, "
                + "d.Quantity AS DiscountQuantity, "
                + "d.SaleOff, "
                + "d.StartDate, "
                + "d.EndDate, "
                + "d.MinimumAmount, "
                + "d.MaximumDiscount, "
                + "d.IsActive "
                + "FROM Payment p "
                + "JOIN Booking b "
                + "ON b.BookingID = p.BookingID "
                + "JOIN Customer c "
                + "ON c.CustomerID = b.CustomerID "
                + "LEFT JOIN Discount d "
                + "ON d.DiscountID = p.DiscountID "
                + "ORDER BY "
                + "p.PaymentDate DESC, "
                + "p.PaymentID DESC";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql); ResultSet rs
                = ps.executeQuery()) {

            while (rs.next()) {

                list.add(
                        mapPayment(rs)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /*
     * =========================================================
     * MAP RESULTSET
     * =========================================================
     */
    private Payment mapPayment(
            ResultSet rs)
            throws SQLException {

        Booking booking
                = new Booking();

        booking.setId(
                rs.getInt(
                        "BookingID"
                )
        );

        if (rs.getTimestamp(
                "BookingDate") != null) {

            booking.setBookingDate(
                    rs.getTimestamp(
                            "BookingDate"
                    ).toLocalDateTime()
            );
        }

        booking.setStatus(
                rs.getString(
                        "BookingStatus"
                )
        );

        booking.setTotalPrice(
                rs.getBigDecimal(
                        "TotalPrice"
                )
        );

        booking.setSpecialRequest(
                rs.getString(
                        "SpecialRequest"
                )
        );

        if (rs.getTimestamp(
                "CancellationDate") != null) {

            booking.setCancellationDate(
                    rs.getTimestamp(
                            "CancellationDate"
                    ).toLocalDateTime()
            );
        }

        booking.setCancellationReason(
                rs.getString(
                        "CancellationReason"
                )
        );

        Customer customer
                = new Customer();

        customer.setCustomerID(
                rs.getInt(
                        "CustomerID"
                )
        );

        customer.setFirstName(
                rs.getString(
                        "FirstName"
                )
        );

        customer.setLastName(
                rs.getString(
                        "LastName"
                )
        );

        booking.setCustomer(
                customer
        );

        /*
         * Load BookingDetail(s).
         */
        BookingDAO bookingDAO
                = new BookingDAO();

        booking.setDetails(
                bookingDAO.getBookingDetails(
                        booking.getId()
                )
        );

        Payment payment
                = new Payment();

        payment.setId(
                rs.getInt(
                        "PaymentID"
                )
        );

        payment.setBooking(
                booking
        );

        if (rs.getTimestamp(
                "PaymentDate") != null) {

            payment.setPaymentDate(
                    rs.getTimestamp(
                            "PaymentDate"
                    ).toLocalDateTime()
            );
        }

        payment.setAmount(
                rs.getBigDecimal(
                        "Amount"
                )
        );

        payment.setPaymentMethod(
                rs.getString(
                        "PaymentMethod"
                )
        );

        payment.setPaymentStatus(
                rs.getString(
                        "PaymentStatus"
                )
        );

        payment.setTransactionCode(
                rs.getString(
                        "TransactionCode"
                )
        );

        /*
         * Optional Discount.
         */
        Object discountId
                = rs.getObject(
                        "DiscountID"
                );

        if (discountId != null) {

            Discount discount
                    = new Discount();

            discount.setId(
                    ((Number) discountId)
                            .intValue()
            );

            discount.setCode(
                    rs.getString(
                            "DiscountCode"
                    )
            );

            discount.setQuantity(
                    rs.getInt(
                            "DiscountQuantity"
                    )
            );

            discount.setSaleOff(
                    rs.getBigDecimal(
                            "SaleOff"
                    )
            );

            discount.setStartDate(
                    rs.getObject(
                            "StartDate",
                            java.time.LocalDate.class
                    )
            );

            discount.setEndDate(
                    rs.getObject(
                            "EndDate",
                            java.time.LocalDate.class
                    )
            );

            discount.setMinimumAmount(
                    rs.getBigDecimal(
                            "MinimumAmount"
                    )
            );

            discount.setMaximumDiscount(
                    rs.getBigDecimal(
                            "MaximumDiscount"
                    )
            );

            discount.setActive(
                    rs.getBoolean(
                            "IsActive"
                    )
            );

            payment.setDiscount(
                    discount
            );
        }

        return payment;
    }

    /*
     * =========================================================
     * PAYMENT METHOD VALIDATION
     *
     * Must match HotelDB CHECK constraint.
     * =========================================================
     */
    private boolean isValidPaymentMethod(
            String method) {

        if (method == null) {
            return false;
        }

        return method.equals("cash")
                || method.equals("bank_transfer")
                || method.equals("credit_card")
                || method.equals("e_wallet");
    }
}
