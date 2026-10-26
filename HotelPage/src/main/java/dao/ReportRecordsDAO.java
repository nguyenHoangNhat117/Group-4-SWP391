package dao;

import db.DBContext;
import model.Booking;
import model.BookingDetail;
import model.Customer;
import model.Discount;
import model.Payment;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides read-only booking and payment records for reports.
 */
public class ReportRecordsDAO extends DBContext {

    private static final String BOOKING_COLUMNS =
            "b.BookingID, b.CustomerID, b.BookingDate, "
            + "b.Status AS BookingStatus, b.TotalPrice, "
            + "b.SpecialRequest, b.CancellationDate, "
            + "b.CancellationReason, "
            + "c.FirstName, c.LastName, c.Email, "
            + "u.UserID, u.Username, u.DisplayName ";

    private static final String CUSTOMER_JOINS =
            "JOIN Customer c ON c.CustomerID = b.CustomerID "
            + "LEFT JOIN [User] u ON u.UserID = c.UserID ";

    public long countBookingRecords(
            LocalDate fromDate,
            LocalDate toDate) {

        return countRecords(false, fromDate, toDate);
    }

    public long countPaymentRecords(
            LocalDate fromDate,
            LocalDate toDate) {

        return countRecords(true, fromDate, toDate);
    }

    public List<Booking> getBookingRecords(
            LocalDate fromDate,
            LocalDate toDate,
            long page,
            int pageSize) {

        validatePage(page, pageSize);

        String sql =
                "SELECT " + BOOKING_COLUMNS
                + "FROM Booking b "
                + CUSTOMER_JOINS
                + dateCondition(false, fromDate, toDate)
                + "ORDER BY b.BookingDate DESC, b.BookingID DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Booking> records = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int index = bindDates(
                    statement,
                    fromDate,
                    toDate
            );

            statement.setLong(
                    index++,
                    Math.multiplyExact(page - 1, (long) pageSize)
            );

            statement.setInt(index, pageSize);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    records.add(mapBooking(rs));
                }
            }

            // Load room details after closing the parent result set.
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
            LocalDate fromDate,
            LocalDate toDate,
            long page,
            int pageSize) {

        validatePage(page, pageSize);

        String sql =
                "SELECT " + BOOKING_COLUMNS
                + ", p.PaymentID, p.PaymentDate, p.Amount, "
                + "p.PaymentMethod, p.PaymentStatus, "
                + "p.TransactionCode, p.DiscountID, "
                + "d.Code AS DiscountCode, d.SaleOff "
                + "FROM Payment p "
                + "JOIN Booking b ON b.BookingID = p.BookingID "
                + CUSTOMER_JOINS
                + "LEFT JOIN Discount d ON d.DiscountID = p.DiscountID "
                + dateCondition(true, fromDate, toDate)
                + "ORDER BY p.PaymentDate DESC, p.PaymentID DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Payment> records = new ArrayList<>();
        Map<Integer, Booking> bookings = new LinkedHashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int index = bindDates(
                    statement,
                    fromDate,
                    toDate
            );

            statement.setLong(
                    index++,
                    Math.multiplyExact(page - 1, (long) pageSize)
            );

            statement.setInt(index, pageSize);

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

                    Object discountId =
                            rs.getObject("DiscountID");

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

    private long countRecords(
            boolean paymentReport,
            LocalDate fromDate,
            LocalDate toDate) {

        String from = paymentReport
                ? "FROM Payment p "
                : "FROM Booking b ";

        String sql =
                "SELECT COUNT_BIG(*) AS TotalRecords "
                + from
                + dateCondition(
                        paymentReport,
                        fromDate,
                        toDate
                );

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            bindDates(statement, fromDate, toDate);

            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong("TotalRecords");
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to count report records.",
                    ex
            );
        }
    }

    /**
     * Column names are selected internally, never from request input.
     */
    private String dateCondition(
            boolean paymentReport,
            LocalDate fromDate,
            LocalDate toDate) {

        validatePeriod(fromDate, toDate);

        if (fromDate == null) {
            return "";
        }

        String column = paymentReport
                ? "p.PaymentDate"
                : "b.BookingDate";

        return "WHERE " + column + " >= ? "
                + "AND " + column + " < ? ";
    }

    /**
     * Uses an exclusive upper bound to include the entire end date.
     */
    private int bindDates(
            PreparedStatement statement,
            LocalDate fromDate,
            LocalDate toDate)
            throws SQLException {

        validatePeriod(fromDate, toDate);

        if (fromDate == null) {
            return 1;
        }

        statement.setTimestamp(
                1,
                Timestamp.valueOf(fromDate.atStartOfDay())
        );

        statement.setTimestamp(
                2,
                Timestamp.valueOf(
                        toDate.plusDays(1).atStartOfDay()
                )
        );

        return 3;
    }

    private Booking mapBooking(ResultSet rs)
            throws SQLException {

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
        booking.setSpecialRequest(
                rs.getString("SpecialRequest")
        );
        booking.setCancellationReason(
                rs.getString("CancellationReason")
        );

        Timestamp bookingDate =
                rs.getTimestamp("BookingDate");

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

    /**
     * Loads room details for the current page in one additional query.
     */
    private void loadRoomDetails(
            Connection connection,
            List<Booking> bookings)
            throws SQLException {

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
                "SELECT BookingDetailID, BookingID, RoomNumber, "
                + "CheckInDate, CheckOutDate, PricePerNight, "
                + "GuestCount, SpecialRequest "
                + "FROM BookingDetail "
                + "WHERE BookingID IN (" + placeholders + ") "
                + "ORDER BY BookingID, BookingDetailID";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int index = 1;

            for (Integer bookingId : indexed.keySet()) {
                statement.setInt(index++, bookingId);
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {

                    BookingDetail detail = new BookingDetail();

                    detail.setId(
                            rs.getInt("BookingDetailID")
                    );
                    detail.setBookingId(
                            rs.getInt("BookingID")
                    );
                    detail.setRoomNumber(
                            rs.getInt("RoomNumber")
                    );
                    detail.setCheckInDate(
                            rs.getDate("CheckInDate").toLocalDate()
                    );
                    detail.setCheckOutDate(
                            rs.getDate("CheckOutDate").toLocalDate()
                    );
                    detail.setPricePerNight(
                            rs.getBigDecimal("PricePerNight")
                    );
                    detail.setGuestCount(
                            rs.getInt("GuestCount")
                    );
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

    private void validatePeriod(
            LocalDate fromDate,
            LocalDate toDate) {

        if (fromDate == null && toDate == null) {
            return;
        }

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "Please select both start date and end date."
            );
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date."
            );
        }

        if (fromDate.getYear() < 1
                || toDate.getYear() > 9998) {

            throw new IllegalArgumentException(
                    "Report years must be between 1 and 9998."
            );
        }
    }

    private void validatePage(long page, int pageSize) {

        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException(
                    "Invalid report pagination."
            );
        }
    }
}