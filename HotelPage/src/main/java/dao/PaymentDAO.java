package dao;

import db.DBContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import model.Booking;
import model.Customer;
import model.Discount;
import model.Payment;

public class PaymentDAO extends DBContext {

    private static final String PAYOS_CODE_PREFIX = "PAYOS-";
    private static final long PAYOS_ORDER_MIN = 1_000_000_000_000_000L;
    private static final long PAYOS_ORDER_LIMIT = 9_000_000_000_000_000L;

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

    public static final String INVALID_COUNTER_PAYMENT = "INVALID_COUNTER_PAYMENT";

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

            String bookingSql
                    = "SELECT BookingID, CustomerID, TotalPrice, Status "
                    + "FROM Booking WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE BookingID = ? AND CustomerID = ? AND CreatedByUserID IS NULL";

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

            // Do not allow a cash payment to race with an online checkout.
            String pendingSql = "SELECT COUNT(*) FROM Payment WITH (UPDLOCK,HOLDLOCK) WHERE BookingID=? AND PaymentStatus='pending'";
            try (PreparedStatement ps = conn.prepareStatement(pendingSql)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        conn.rollback();
                        return PAYMENT_FAILED;
                    }
                }
            }

            String paidSql
                    = "SELECT COALESCE(SUM(Amount), 0) AS TotalPaid "
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

                String discountSql
                        = "SELECT DiscountID, SaleOff, MaximumDiscount "
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

                String updateTotalSql
                        = "UPDATE Booking SET TotalPrice = ? WHERE BookingID = ?";
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

            String transactionCode
                    = "TXN-" + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .toUpperCase();

            String insertPaymentSql
                    = "INSERT INTO Payment "
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
                String confirmSql
                        = "UPDATE Booking SET Status = 'confirmed' "
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
                String decreaseSql
                        = "UPDATE Discount SET Quantity = Quantity - 1 "
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
    /**
     * Creates a pending online payment. The booking total and voucher stock are
     * not changed yet.
     */
    public long createPendingOnlinePayment(int bookingId, int customerId, String method,
            String discountCode, Integer paymentPercent) throws SQLException {
        if (!"bank_transfer".equals(method) && !"e_wallet".equals(method)) {
            throw new SQLException("Invalid online method");
        }
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal total;
                String status;
                try (PreparedStatement ps = conn.prepareStatement("SELECT TotalPrice, Status FROM Booking WITH (UPDLOCK,HOLDLOCK) WHERE BookingID=? AND CustomerID=? AND CreatedByUserID IS NULL")) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Invalid booking");
                        }
                        total = rs.getBigDecimal(1);
                        status = rs.getString(2);
                    }
                }
                if ("cancelled".equalsIgnoreCase(status)) {
                    throw new SQLException("Booking is cancelled");
                }
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Payment WITH (UPDLOCK,HOLDLOCK) WHERE BookingID=? AND PaymentStatus='pending'")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new SQLException("There is already a pending payment for this booking");
                        }
                    }
                }
                BigDecimal paid = BigDecimal.ZERO;
                try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(Amount),0) FROM Payment WHERE BookingID=? AND PaymentStatus='paid'")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        paid = rs.getBigDecimal(1);
                    }
                }
                boolean first = paid.compareTo(BigDecimal.ZERO) == 0;
                Integer discountId = null;
                BigDecimal discountAmount = BigDecimal.ZERO;
                if (discountCode != null && !discountCode.trim().isEmpty()) {
                    if (!first) {
                        throw new SQLException("Discount only applies to first payment");
                    }
                    String sql = "SELECT DiscountID,SaleOff,MaximumDiscount FROM Discount WITH (UPDLOCK,HOLDLOCK) WHERE Code=? AND IsActive=1 AND Quantity>0 AND (StartDate IS NULL OR StartDate<=CAST(GETDATE() AS DATE)) AND (EndDate IS NULL OR EndDate>=CAST(GETDATE() AS DATE)) AND (MinimumAmount IS NULL OR ? >= MinimumAmount)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, discountCode.trim());
                        ps.setBigDecimal(2, total);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException("Invalid discount");
                            }
                            discountId = rs.getInt(1);
                            discountAmount = total.multiply(rs.getBigDecimal(2)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                            BigDecimal max = rs.getBigDecimal(3);
                            if (max != null && discountAmount.compareTo(max) > 0) {
                                discountAmount = max;
                            }
                        }
                    }
                }
                BigDecimal discounted = total.subtract(discountAmount).max(BigDecimal.ZERO);
                BigDecimal remaining = discounted.subtract(paid);
                if (remaining.signum() <= 0) {
                    throw new SQLException("Booking already paid");
                }
                BigDecimal amount;
                if (first) {
                    if (paymentPercent == null || (paymentPercent != 10 && paymentPercent != 20
                            && paymentPercent != 100 && !(paymentPercent == 50 && "bank_transfer".equals(method)))) {
                        throw new SQLException("Invalid payment percentage");
                    }
                    if (paymentPercent == 50 && "bank_transfer".equals(method)
                            && !util.OnlineStayPolicy.canDeposit(discounted)) {
                        throw new SQLException("The 50% deposit must be a whole VND amount");
                    }
                    amount = discounted.multiply(BigDecimal.valueOf(paymentPercent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                } else {
                    amount = remaining;
                }
                if (amount.compareTo(remaining) > 0) {
                    amount = remaining;
                }
                // Keep the provider code independent of the database identity.
                // The range is disjoint from legacy INT IDs and safe in JSON clients.
                String code = "bank_transfer".equals(method)
                        ? PAYOS_CODE_PREFIX + ThreadLocalRandom.current().nextLong(PAYOS_ORDER_MIN, PAYOS_ORDER_LIMIT)
                        : "HP-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
                String sql = "INSERT INTO Payment(BookingID,PaymentDate,Amount,PaymentMethod,PaymentStatus,TransactionCode,DiscountID,DiscountAmount) VALUES(?,NULL,?,?,'pending',?,?,?)";
                long id;
                try (PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, bookingId);
                    ps.setBigDecimal(2, amount);
                    ps.setString(3, method);
                    ps.setString(4, code);
                    if (discountId == null) {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(5, discountId);
                    }
                    ps.setBigDecimal(6, discountAmount);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Cannot create payment");
                        }
                        id = rs.getLong(1);
                    }
                }
                // Reserve one voucher unit while this online payment is pending.
                // It is restored if the provider reports failure or link creation fails.
                if (discountId != null) {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE Discount SET Quantity=Quantity-1 WHERE DiscountID=? AND Quantity>0")) {
                        ps.setInt(1, discountId);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("Discount stock is no longer available");
                        }
                    }
                }
                conn.commit();
                return id;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Marks a verified provider callback as paid. Repeated callbacks are
     * idempotent.
     */
    public boolean completeOnlinePayment(String transactionCode, long amount, String method) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int paymentId, bookingId, discountId;
                BigDecimal expected, discountAmount;
                String status, storedMethod;
                String q = "SELECT PaymentID,BookingID,Amount,PaymentStatus,PaymentMethod,DiscountID,COALESCE(DiscountAmount,0) DiscountAmount FROM Payment WITH (UPDLOCK,HOLDLOCK) WHERE TransactionCode=?";
                try (PreparedStatement ps = conn.prepareStatement(q)) {
                    ps.setString(1, transactionCode);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Payment not found");
                        }
                        paymentId = rs.getInt(1);
                        bookingId = rs.getInt(2);
                        expected = rs.getBigDecimal(3);
                        status = rs.getString(4);
                        storedMethod = rs.getString(5);
                        discountId = rs.getInt(6);
                        if (rs.wasNull()) {
                            discountId = 0;
                        }
                        discountAmount = rs.getBigDecimal(7);
                    }
                }
                if (!method.equals(storedMethod) || expected.compareTo(BigDecimal.valueOf(amount)) != 0) {
                    throw new SQLException("Payment callback mismatch");
                }
                if ("paid".equalsIgnoreCase(status)) {
                    conn.commit();
                    return true;
                }
                if (!"pending".equalsIgnoreCase(status)) {
                    throw new SQLException("Payment is not pending");
                }
                if (discountId > 0 && discountAmount.signum() > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE Booking SET TotalPrice=CASE WHEN TotalPrice>=? THEN TotalPrice-? ELSE 0 END, Status=CASE WHEN Status='pending' THEN 'confirmed' ELSE Status END WHERE BookingID=?")) {
                        ps.setBigDecimal(1, discountAmount);
                        ps.setBigDecimal(2, discountAmount);
                        ps.setInt(3, bookingId);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE Booking SET Status=CASE WHEN Status='pending' THEN 'confirmed' ELSE Status END WHERE BookingID=?")) {
                        ps.setInt(1, bookingId);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement("UPDATE Payment SET PaymentStatus='paid',PaymentDate=SYSDATETIME() WHERE PaymentID=? AND PaymentStatus='pending'")) {
                    ps.setInt(1, paymentId);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Payment state changed");
                    }
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean markOnlinePaymentFailed(String transactionCode, String method) throws SQLException {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                Integer discountId = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT DiscountID FROM Payment WITH (UPDLOCK,HOLDLOCK) WHERE TransactionCode=? AND PaymentMethod=? AND PaymentStatus='pending'")) {
                    ps.setString(1, transactionCode);
                    ps.setString(2, method);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.commit();
                            return false;
                        }
                        int id = rs.getInt(1);
                        if (!rs.wasNull()) {
                            discountId = id;
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE Payment SET PaymentStatus='failed' WHERE TransactionCode=? AND PaymentMethod=? AND PaymentStatus='pending'")) {
                    ps.setString(1, transactionCode);
                    ps.setString(2, method);
                    if (ps.executeUpdate() != 1) {
                        c.rollback();
                        return false;
                    }
                }
                if (discountId != null) {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE Discount SET Quantity=Quantity+1 WHERE DiscountID=?")) {
                        ps.setInt(1, discountId);
                        ps.executeUpdate();
                    }
                }
                c.commit();
                return true;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public BigDecimal getPaymentAmountById(long paymentId) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT Amount FROM Payment WHERE PaymentID=? AND PaymentStatus='pending'")) {
            ps.setLong(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Pending payment not found");
                }
                return rs.getBigDecimal(1);
            }
        }
    }

    public String getTransactionCodeByPaymentId(long paymentId) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT TransactionCode FROM Payment WHERE PaymentID=?")) {
            ps.setLong(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public long getPayOSOrderCode(long paymentId) throws SQLException {
        String code = getTransactionCodeByPaymentId(paymentId);
        if (code == null || !code.startsWith(PAYOS_CODE_PREFIX)) {
            throw new SQLException("Missing payOS order code for payment " + paymentId);
        }
        return Long.parseLong(code.substring(PAYOS_CODE_PREFIX.length()));
    }

    public boolean completePayOSPayment(long orderCode, long amount) throws SQLException {
        String code;
        if (orderCode >= PAYOS_ORDER_MIN && orderCode < PAYOS_ORDER_LIMIT) {
            code = PAYOS_CODE_PREFIX + orderCode;
        } else if (orderCode > 0 && orderCode <= Integer.MAX_VALUE) {
            // Existing links used PaymentID. Never let one complete a new payment.
            code = getTransactionCodeByPaymentId(orderCode);
            if (code == null || !code.startsWith("HP-")) {
                return false;
            }
        } else {
            return false;
        }
        return completeOnlinePayment(code, amount, "bank_transfer");
    }

    public long findPendingPayOSAmount(long paymentId, long orderCode) throws SQLException {
        String sql = "SELECT Amount FROM Payment WHERE PaymentID=? AND TransactionCode=? "
                + "AND PaymentMethod='bank_transfer' AND PaymentStatus='pending'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            ps.setString(2, PAYOS_CODE_PREFIX + orderCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1).longValueExact() : 0;
            }
        }
    }

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

        // This method records an immediately collected payment only.
        // Online methods must use createPendingOnlinePayment().
        return method.equals("cash");
    }

    public String processCounterPayment(int bookingId, int staffId, String paymentMethod,
            int paymentPercent, String paymentKey, BigDecimal quotedPaid,
            BigDecimal quotedTotal, BigDecimal quotedPayable) {
        return processCounterPayment(bookingId,staffId,paymentMethod,paymentPercent,paymentKey,
                quotedPaid,quotedTotal,quotedPayable,null);
    }

    public String processCounterPayment(int bookingId, int staffId, String paymentMethod,
            int paymentPercent, String paymentKey, BigDecimal quotedPaid,
            BigDecimal quotedTotal, BigDecimal quotedPayable, String reference) {
        try {
            new OfflineOperationsDAO().pay(bookingId,staffId,paymentMethod,paymentPercent,paymentKey,
                    quotedPaid,quotedTotal,quotedPayable,reference);
            return PAYMENT_SUCCESS;
        } catch (SQLException | IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger(PaymentDAO.class.getName()).log(
                    java.util.logging.Level.WARNING,"Counter payment rejected",ex);
            return INVALID_COUNTER_PAYMENT;
        }
    }
}
