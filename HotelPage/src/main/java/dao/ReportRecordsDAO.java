package dao;

import db.DBContext;
import model.Booking;
import model.BookingDetail;
import model.Customer;
import model.Discount;
import model.Payment;
import model.User;
import model.Review;
import model.ReportFilter;
import model.RevenueReport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportRecordsDAO extends DBContext {

    private static final String PAYMENT_STATUS =
            "(CASE WHEN p.PaymentStatus='refunded' "
            + "OR (p.PaymentStatus='paid' "
            + "AND COALESCE(p.RefundedAmount,0)>=p.Amount "
            + "AND COALESCE(p.RefundedAmount,0)>0) "
            + "THEN 'refunded' "
            + "WHEN p.PaymentStatus='paid' "
            + "AND COALESCE(p.RefundedAmount,0)>0 "
            + "THEN 'partially_refunded' "
            + "ELSE p.PaymentStatus END)";

    private static final String BOOKING_COLUMNS =
            "b.BookingID, b.CustomerID, b.BookingDate, "
            + "b.Status AS BookingStatus, b.TotalPrice, "
            + "b.SpecialRequest, b.CancellationDate, "
            + "b.CancellationReason, "
            + "c.FirstName, c.LastName, c.Email, "
            + "u.UserID, u.Username, u.DisplayName ";

    private static final String CUSTOMER_JOINS =
            "JOIN Customer c ON c.CustomerID=b.CustomerID "
            + "LEFT JOIN [User] u ON u.UserID=c.UserID ";

    public RevenueReport getPaymentSummary(ReportFilter filter) {
        if (!"payments".equals(filter.getView())) {
            throw new IllegalArgumentException(
                    "Payment filter required."
            );
        }

        String sql =
                "SELECT COALESCE(SUM(p.Amount),0) AS Collected, "
                + "COALESCE(SUM("
                + "CASE WHEN p.PaymentStatus='refunded' THEN p.Amount "
                + "ELSE COALESCE(p.RefundedAmount,0) END"
                + "),0) AS Refunded "
                + "FROM Payment p "
                + condition(filter)
                + "AND p.PaymentStatus IN ('paid','refunded')";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            bindFilter(statement, filter);

            try (ResultSet rs = statement.executeQuery()) {
                rs.next();

                return new RevenueReport(
                        "filtered",
                        rs.getBigDecimal("Collected"),
                        rs.getBigDecimal("Refunded")
                );
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to load payment totals.",
                    ex
            );
        }
    }

    public ReviewReportSummary getReviewSummary(ReportFilter filter) {
        if (!"reviews".equals(filter.getView())) {
            throw new IllegalArgumentException(
                    "Review filter required."
            );
        }

        String sql =
                "SELECT rv.ReviewStatus,rv.Star,"
                + "COUNT_BIG(*) AS Total "
                + "FROM Review rv "
                + condition(filter)
                + "GROUP BY rv.ReviewStatus,rv.Star";

        long total = 0;
        long approved = 0;
        long pending = 0;
        long rejected = 0;

        BigDecimal stars = BigDecimal.ZERO;

        List<Long> counts =
                new ArrayList<>(Collections.nCopies(11, 0L));

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            bindFilter(statement, filter);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    long count = rs.getLong("Total");
                    String status = rs.getString("ReviewStatus");

                    total += count;

                    if ("approved".equals(status)) {
                        approved += count;

                        BigDecimal rating = rs.getBigDecimal("Star");

                        stars = stars.add(
                                rating.multiply(
                                        BigDecimal.valueOf(count)
                                )
                        );

                        BigDecimal doubled =
                                rating.multiply(BigDecimal.valueOf(2));

                        boolean supported =
                                doubled.stripTrailingZeros().scale() <= 0
                                && doubled.compareTo(
                                        BigDecimal.valueOf(2)
                                ) >= 0
                                && doubled.compareTo(
                                        BigDecimal.TEN
                                ) <= 0;

                        int index = supported
                                ? doubled.intValueExact() : 0;

                        counts.set(
                                index,
                                counts.get(index) + count
                        );

                    } else if ("pending".equals(status)) {
                        pending += count;

                    } else if ("rejected".equals(status)) {
                        rejected += count;
                    }
                }
            }

            return new ReviewReportSummary(
                    total,
                    approved,
                    pending,
                    rejected,
                    stars,
                    counts
            );

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to load review totals.",
                    ex
            );
        }
    }

    public long countRecords(ReportFilter filter) {
        String from =
                "payments".equals(filter.getView())
                ? "FROM Payment p "
                : "reviews".equals(filter.getView())
                        ? "FROM Review rv "
                        : "FROM Booking b ";

        String sql =
                "SELECT COUNT_BIG(*) "
                + from
                + condition(filter);

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            bindFilter(statement, filter);

            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to count report records.",
                    ex
            );
        }
    }

    public List<Booking> getBookingRecords(
            ReportFilter filter,
            long page) {

        validatePage(page, filter.getPageSize());

        String sql =
                "SELECT " + BOOKING_COLUMNS
                + "FROM Booking b "
                + CUSTOMER_JOINS
                + condition(filter)
                + "ORDER BY b.BookingDate DESC,b.BookingID DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Booking> records = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int index = bindFilter(statement, filter);

            statement.setLong(
                    index++,
                    Math.multiplyExact(
                            page - 1,
                            (long) filter.getPageSize()
                    )
            );
            statement.setInt(index, filter.getPageSize());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    records.add(mapBooking(rs));
                }
            }

            loadRoomDetails(connection, records);
            return records;

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to load booking report records.",
                    ex
            );
        }
    }

    public List<Payment> getPaymentRecords(
            ReportFilter filter,
            long page) {

        validatePage(page, filter.getPageSize());

        String sql =
                "SELECT " + BOOKING_COLUMNS
                + ", p.PaymentID,p.PaymentDate,p.Amount,"
                + "p.PaymentMethod,p.PaymentStatus,"
                + "p.RefundedAmount,p.DiscountAmount,"
                + PAYMENT_STATUS + " AS ReportPaymentStatus,"
                + "p.TransactionCode,p.DiscountID,"
                + "d.Code AS DiscountCode,d.SaleOff "
                + "FROM Payment p "
                + "JOIN Booking b ON b.BookingID=p.BookingID "
                + CUSTOMER_JOINS
                + "LEFT JOIN Discount d ON d.DiscountID=p.DiscountID "
                + condition(filter)
                + "ORDER BY p.PaymentDate DESC,p.PaymentID DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Payment> records = new ArrayList<>();
        Map<Integer, Booking> bookings = new LinkedHashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int index = bindFilter(statement, filter);

            statement.setLong(
                    index++,
                    Math.multiplyExact(
                            page - 1,
                            (long) filter.getPageSize()
                    )
            );
            statement.setInt(index, filter.getPageSize());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int bookingId = rs.getInt("BookingID");

                    Booking booking = bookings.get(bookingId);

                    if (booking == null) {
                        booking = mapBooking(rs);
                        bookings.put(bookingId, booking);
                    }

                    Payment payment = new Payment();

                    payment.setId(rs.getInt("PaymentID"));
                    payment.setBooking(booking);
                    payment.setAmount(rs.getBigDecimal("Amount"));
                    payment.setRefundedAmount(
                            rs.getBigDecimal("RefundedAmount")
                    );
                    payment.setDiscountAmount(
                            rs.getBigDecimal("DiscountAmount")
                    );
                    payment.setHistoryStatus(
                            rs.getString("ReportPaymentStatus")
                    );
                    payment.setHistoryRemainingAmount(
                            "paid".equals(rs.getString("PaymentStatus"))
                            ? payment.getAmount().subtract(
                                    payment.getRefundedAmount()
                            )
                            : BigDecimal.ZERO
                    );
                    payment.setPaymentMethod(
                            rs.getString("PaymentMethod")
                    );
                    payment.setPaymentStatus(
                            rs.getString("PaymentStatus")
                    );
                    payment.setTransactionCode(
                            rs.getString("TransactionCode")
                    );

                    Timestamp paymentDate =
                            rs.getTimestamp("PaymentDate");

                    if (paymentDate != null) {
                        payment.setPaymentDate(
                                paymentDate.toLocalDateTime()
                        );
                    }

                    Object discountId = rs.getObject("DiscountID");

                    if (discountId != null) {
                        Discount discount = new Discount();

                        discount.setId(
                                ((Number) discountId).intValue()
                        );
                        discount.setCode(
                                rs.getString("DiscountCode")
                        );
                        discount.setSaleOff(
                                rs.getBigDecimal("SaleOff")
                        );

                        payment.setDiscount(discount);
                    }

                    records.add(payment);
                }
            }

            loadRoomDetails(
                    connection,
                    new ArrayList<>(bookings.values())
            );

            return records;

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to load payment report records.",
                    ex
            );
        }
    }

    public List<Review> getReviewRecords(
            ReportFilter filter,
            long page) {

        validatePage(page, filter.getPageSize());

        String sql =
                "SELECT " + BOOKING_COLUMNS
                + ",rv.ReviewID,rv.Comment,rv.Star,"
                + "rv.ReviewDate,rv.ReviewStatus "
                + "FROM Review rv "
                + "JOIN Booking b ON b.BookingID=rv.BookingID "
                + CUSTOMER_JOINS
                + condition(filter)
                + "ORDER BY rv.ReviewDate DESC,rv.ReviewID DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Review> records = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int index = bindFilter(statement, filter);

            statement.setLong(
                    index++,
                    Math.multiplyExact(
                            page - 1,
                            (long) filter.getPageSize()
                    )
            );
            statement.setInt(index, filter.getPageSize());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Booking booking = mapBooking(rs);
                    bookings.add(booking);

                    Timestamp date = rs.getTimestamp("ReviewDate");

                    records.add(new Review(
                            rs.getInt("ReviewID"),
                            booking,
                            rs.getString("Comment"),
                            rs.getBigDecimal("Star"),
                            date == null
                                    ? null : date.toLocalDateTime(),
                            rs.getString("ReviewStatus")
                    ));
                }
            }

            loadRoomDetails(connection, bookings);
            return records;

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to load review report.",
                    ex
            );
        }
    }

    private String condition(ReportFilter filter) {
        String alias =
                "payments".equals(filter.getView()) ? "p"
                : "reviews".equals(filter.getView()) ? "rv" : "b";

        String dateColumn =
                "payments".equals(filter.getView()) ? "PaymentDate"
                : "reviews".equals(filter.getView()) ? "ReviewDate"
                : "BookingDate";

        String statusColumn =
                "payments".equals(filter.getView()) ? "PaymentStatus"
                : "reviews".equals(filter.getView()) ? "ReviewStatus"
                : "Status";

        String sql = "WHERE 1=1 ";

        if (filter.getFromDate() != null) {
            sql += "AND " + alias + "." + dateColumn + " >= ? "
                    + "AND " + alias + "." + dateColumn + " < ? ";
        }

        if (!filter.getStatus().isEmpty()) {
            String expression =
                    "payments".equals(filter.getView())
                    ? PAYMENT_STATUS
                    : alias + "." + statusColumn;

            sql += "AND " + expression + " = ? ";
        }

        if (!filter.getMethod().isEmpty()) {
            sql += "AND p.PaymentMethod = ? ";
        }

        if (filter.getBookingId() != null) {
            sql += "AND " + alias + ".BookingID = ? ";
        }

        return sql;
    }

    private int bindFilter(
            PreparedStatement statement,
            ReportFilter filter) throws SQLException {

        int index = 1;

        if (filter.getFromDate() != null) {
            statement.setTimestamp(
                    index++,
                    Timestamp.valueOf(
                            filter.getFromDate().atStartOfDay()
                    )
            );
            statement.setTimestamp(
                    index++,
                    Timestamp.valueOf(
                            filter.getToDate()
                                    .plusDays(1)
                                    .atStartOfDay()
                    )
            );
        }

        if (!filter.getStatus().isEmpty()) {
            statement.setString(index++, filter.getStatus());
        }

        if (!filter.getMethod().isEmpty()) {
            statement.setString(index++, filter.getMethod());
        }

        if (filter.getBookingId() != null) {
            statement.setInt(index++, filter.getBookingId());
        }

        return index;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Customer customer = new Customer();

        customer.setCustomerID(rs.getInt("CustomerID"));
        customer.setFirstName(rs.getString("FirstName"));
        customer.setLastName(rs.getString("LastName"));
        customer.setEmail(rs.getString("Email"));

        if (rs.getObject("UserID") != null) {
            User user = new User();

            user.setId(rs.getInt("UserID"));
            user.setUsername(rs.getString("Username"));
            user.setDisplayName(rs.getString("DisplayName"));

            customer.setUser(user);
        }

        Booking booking = new Booking();

        booking.setId(rs.getInt("BookingID"));
        booking.setCustomer(customer);
        booking.setStatus(rs.getString("BookingStatus"));
        booking.setTotalPrice(rs.getBigDecimal("TotalPrice"));
        booking.setSpecialRequest(rs.getString("SpecialRequest"));
        booking.setCancellationReason(
                rs.getString("CancellationReason")
        );

        Timestamp bookingDate = rs.getTimestamp("BookingDate");

        if (bookingDate != null) {
            booking.setBookingDate(
                    bookingDate.toLocalDateTime()
            );
        }

        Timestamp cancellationDate =
                rs.getTimestamp("CancellationDate");

        if (cancellationDate != null) {
            booking.setCancellationDate(
                    cancellationDate.toLocalDateTime()
            );
        }

        return booking;
    }

    private void loadRoomDetails(
            Connection connection,
            List<Booking> bookings) throws SQLException {

        if (bookings.isEmpty()) {
            return;
        }

        Map<Integer, Booking> indexed = new LinkedHashMap<>();

        for (Booking booking : bookings) {
            booking.setDetails(new ArrayList<>());
            indexed.put(booking.getId(), booking);
        }

        String placeholders = String.join(
                ",",
                Collections.nCopies(indexed.size(), "?")
        );

        String sql =
                "SELECT BookingDetailID,BookingID,RoomNumber,"
                + "CheckInDate,CheckOutDate,PricePerNight,"
                + "GuestCount,SpecialRequest "
                + "FROM BookingDetail "
                + "WHERE BookingID IN (" + placeholders + ") "
                + "ORDER BY BookingID,BookingDetailID";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int index = 1;

            for (Integer bookingId : indexed.keySet()) {
                statement.setInt(index++, bookingId);
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    BookingDetail detail = new BookingDetail();

                    detail.setId(rs.getInt("BookingDetailID"));
                    detail.setBookingId(rs.getInt("BookingID"));
                    detail.setRoomNumber(rs.getInt("RoomNumber"));
                    detail.setCheckInDate(
                            rs.getDate("CheckInDate").toLocalDate()
                    );
                    detail.setCheckOutDate(
                            rs.getDate("CheckOutDate").toLocalDate()
                    );
                    detail.setPricePerNight(
                            rs.getBigDecimal("PricePerNight")
                    );
                    detail.setGuestCount(rs.getInt("GuestCount"));
                    detail.setSpecialRequest(
                            rs.getString("SpecialRequest")
                    );

                    indexed.get(detail.getBookingId())
                            .getDetails()
                            .add(detail);
                }
            }
        }
    }

    private void validatePage(long page, int pageSize) {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException(
                    "Invalid report pagination."
            );
        }
    }

    public static final class ReviewReportSummary {

        private final long total;
        private final long approved;
        private final long pending;
        private final long rejected;

        private final BigDecimal starTotal;
        private final List<Long> ratingCounts;

        public ReviewReportSummary(
                long total,
                long approved,
                long pending,
                long rejected,
                BigDecimal starTotal,
                List<Long> ratingCounts) {

            this.total = total;
            this.approved = approved;
            this.pending = pending;
            this.rejected = rejected;
            this.starTotal = starTotal;
            this.ratingCounts = List.copyOf(ratingCounts);
        }

        public long getTotal() {
            return total;
        }

        public long getApproved() {
            return approved;
        }

        public long getPending() {
            return pending;
        }

        public long getRejected() {
            return rejected;
        }

        public BigDecimal getAverage() {
            if (approved == 0) {
                return BigDecimal.ZERO;
            }

            return starTotal.divide(
                    BigDecimal.valueOf(approved),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        public List<Long> getRatingCounts() {
            return ratingCounts;
        }
    }
}