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

    public static final String PAYMENT_SUCCESS =
            "success";

    public static final String INVALID_BOOKING =
            "invalid-booking";

    public static final String BOOKING_NOT_PENDING =
            "booking-not-pending";

    public static final String ALREADY_PAID =
            "already-paid";

    public static final String INVALID_METHOD =
            "invalid-method";

    public static final String INVALID_DISCOUNT =
            "invalid-discount";

    public static final String PAYMENT_FAILED =
            "payment-failed";

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
            Integer paymentPercent) {

        if (!isValidPaymentMethod(paymentMethod)) {
            return INVALID_METHOD;
        }

        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String bookingSql =
                    "SELECT BookingID, CustomerID, TotalPrice, Status "
                    + "FROM Booking WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE BookingID = ? AND CustomerID = ?";

            BigDecimal bookingTotal;
            String bookingStatus;

            try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, customerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return INVALID_BOOKING;
                    }
                    bookingTotal = rs.getBigDecimal("TotalPrice");
                    bookingStatus = rs.getString("Status");
                }
            }

            if ("cancelled".equalsIgnoreCase(bookingStatus)) {
                conn.rollback();
                return BOOKING_NOT_PENDING;
            }

            String paidSql =
                    "SELECT COALESCE(SUM(Amount), 0) AS TotalPaid "
                    + "FROM Payment WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE BookingID = ? AND PaymentStatus = 'paid'";

            BigDecimal totalPaid = BigDecimal.ZERO;
            try (PreparedStatement ps = conn.prepareStatement(paidSql)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBigDecimal("TotalPaid") != null) {
                        totalPaid = rs.getBigDecimal("TotalPaid");
                    }
                }
            }

            boolean firstPayment = totalPaid.compareTo(BigDecimal.ZERO) == 0;

            /*
             * Promotions are accepted only on the first payment. Applying
             * a promotion later would make previously calculated balances
             * ambiguous. When a promotion is used, Booking.TotalPrice is
             * reduced first; all paid/remaining calculations then use that
             * single discounted total consistently.
             */
            Integer discountId = null;
            if (discountCode != null && !discountCode.trim().isEmpty()) {
                if (!firstPayment) {
                    conn.rollback();
                    return INVALID_DISCOUNT;
                }

                String discountSql =
                        "SELECT DiscountID, SaleOff, MaximumDiscount "
                        + "FROM Discount WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE Code = ? AND IsActive = 1 AND Quantity > 0 "
                        + "AND (StartDate IS NULL OR StartDate <= CAST(GETDATE() AS DATE)) "
                        + "AND (EndDate IS NULL OR EndDate >= CAST(GETDATE() AS DATE)) "
                        + "AND (MinimumAmount IS NULL OR ? >= MinimumAmount)";

                try (PreparedStatement ps = conn.prepareStatement(discountSql)) {
                    ps.setString(1, discountCode.trim());
                    ps.setBigDecimal(2, bookingTotal);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return INVALID_DISCOUNT;
                        }

                        discountId = rs.getInt("DiscountID");
                        BigDecimal saleOff = rs.getBigDecimal("SaleOff");
                        BigDecimal maximumDiscount = rs.getBigDecimal("MaximumDiscount");

                        BigDecimal discountAmount = bookingTotal
                                .multiply(saleOff)
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                        if (maximumDiscount != null
                                && discountAmount.compareTo(maximumDiscount) > 0) {
                            discountAmount = maximumDiscount;
                        }

                        bookingTotal = bookingTotal.subtract(discountAmount);
                        if (bookingTotal.compareTo(BigDecimal.ZERO) < 0) {
                            bookingTotal = BigDecimal.ZERO;
                        }
                    }
                }

                String updateTotalSql =
                        "UPDATE Booking SET TotalPrice = ? WHERE BookingID = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateTotalSql)) {
                    ps.setBigDecimal(1, bookingTotal);
                    ps.setInt(2, bookingId);
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return PAYMENT_FAILED;
                    }
                }
            }

            BigDecimal remainingAmount = bookingTotal.subtract(totalPaid);
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                conn.rollback();
                return ALREADY_PAID;
            }

            BigDecimal paymentAmount;

            if (firstPayment) {
                if (paymentPercent == null
                        || (paymentPercent != 10
                        && paymentPercent != 20
                        && paymentPercent != 100)) {
                    conn.rollback();
                    return PAYMENT_FAILED;
                }

                paymentAmount = bookingTotal
                        .multiply(BigDecimal.valueOf(paymentPercent))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                paymentAmount = remainingAmount;
            }

            if (paymentAmount.compareTo(remainingAmount) > 0) {
                paymentAmount = remainingAmount;
            }

            String transactionCode =
                    "TXN-" + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .toUpperCase();

            String insertPaymentSql =
                    "INSERT INTO Payment "
                    + "(BookingID, PaymentDate, Amount, PaymentMethod, "
                    + "PaymentStatus, TransactionCode, DiscountID) "
                    + "VALUES (?, SYSDATETIME(), ?, ?, 'paid', ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql)) {
                ps.setInt(1, bookingId);
                ps.setBigDecimal(2, paymentAmount);
                ps.setString(3, paymentMethod);
                ps.setString(4, transactionCode);

                if (discountId != null) {
                    ps.setInt(5, discountId);
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }

                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return PAYMENT_FAILED;
                }
            }

            if ("pending".equalsIgnoreCase(bookingStatus)) {
                String confirmSql =
                        "UPDATE Booking SET Status = 'confirmed' "
                        + "WHERE BookingID = ? AND Status = 'pending'";
                try (PreparedStatement ps = conn.prepareStatement(confirmSql)) {
                    ps.setInt(1, bookingId);
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return PAYMENT_FAILED;
                    }
                }
            }

            if (discountId != null) {
                String decreaseSql =
                        "UPDATE Discount SET Quantity = Quantity - 1 "
                        + "WHERE DiscountID = ? AND Quantity > 0";
                try (PreparedStatement ps = conn.prepareStatement(decreaseSql)) {
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
                } catch (SQLException ex) {
                    ex.printStackTrace();
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

        String sql =
                "SELECT COALESCE(SUM(Amount), 0) AS TotalPaid "
                + "FROM Payment "
                + "WHERE BookingID = ? AND PaymentStatus = 'paid'";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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

        String sql =
                "SELECT b.TotalPrice, "
                + "COALESCE(SUM(CASE WHEN p.PaymentStatus = 'paid' "
                + "THEN p.Amount ELSE 0 END), 0) AS TotalPaid "
                + "FROM Booking b "
                + "LEFT JOIN Payment p ON p.BookingID = b.BookingID "
                + "WHERE b.BookingID = ? "
                + "GROUP BY b.TotalPrice";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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

        String sql =
                "SELECT "
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

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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

        List<Payment> list =
                new ArrayList<>();

        if (page < 1) {
            page = 1;
        }

        if (pageSize <= 0) {
            pageSize = 5;
        }

        String sql =
                "SELECT "
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

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

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

            try (ResultSet rs =
                    ps.executeQuery()) {

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

        String sql =
                "SELECT COUNT(*) AS Total "
                + "FROM Payment p "
                + "JOIN Booking b "
                + "ON b.BookingID = p.BookingID "
                + "WHERE b.CustomerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    customerId
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

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

        List<Payment> list =
                new ArrayList<>();

        String sql =
                "SELECT "
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

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

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

        Booking booking =
                new Booking();

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

        Customer customer =
                new Customer();

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
        BookingDAO bookingDAO =
                new BookingDAO();

        booking.setDetails(
                bookingDAO.getBookingDetails(
                        booking.getId()
                )
        );

        Payment payment =
                new Payment();

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
        Object discountId =
                rs.getObject(
                        "DiscountID"
                );

        if (discountId != null) {

            Discount discount =
                    new Discount();

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