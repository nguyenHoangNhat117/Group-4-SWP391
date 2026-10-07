package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Booking;
import model.BookingDetail;
import model.Customer;
import model.User;

public class BookingDAO extends DBContext {

    /*
     * =========================================================
     * 1. GET ALL BOOKINGS
     * Used mainly for Staff/Admin booking management.
     * =========================================================
     */
    public List<Booking> getAll() {

        List<Booking> bookings = new ArrayList<>();

        String sql
                = "SELECT "
                + "b.BookingID, "
                + "b.BookingDate, "
                + "b.Status, "
                + "b.TotalPrice, "
                + "b.SpecialRequest, "
                + "b.CancellationDate, "
                + "b.CancellationReason, "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.Street, "
                + "c.City, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.Password, "
                + "u.Role, "
                + "u.DisplayName "
                + "FROM Booking b "
                + "JOIN Customer c "
                + "ON b.CustomerID = c.CustomerID "
                + "JOIN [User] u "
                + "ON c.UserID = u.UserID "
                + "ORDER BY b.BookingDate DESC, b.BookingID DESC";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                User user = new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role")
                );

                user.setDisplayName(
                        rs.getString("DisplayName")
                );

                Customer customer = new Customer(
                        rs.getInt("CustomerID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        rs.getString("Country"),
                        rs.getString("City"),
                        rs.getString("Street"),
                        rs.getString("Avatar"),
                        user
                );

                Booking booking = mapBooking(rs);
                booking.setCustomer(customer);

                /*
                 * Load BookingDetail records belonging
                 * to this Booking.
                 */
                booking.setDetails(
                        getBookingDetails(
                                conn,
                                booking.getId()
                        )
                );

                bookings.add(booking);
            }

        } catch (SQLException ex) {
            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return bookings;
    }

    /*
     * =========================================================
     * 2. CREATE BOOKING + BOOKING DETAIL
     *
     * UC09:
     * - Revalidate room
     * - Create Booking with pending status
     * - Create BookingDetail
     * - Transaction
     * =========================================================
     */
    public boolean createBooking(
            int customerId,
            int roomNumber,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            BigDecimal pricePerNight,
            int guestCount,
            BigDecimal totalPrice,
            String specialRequest) {

        if (checkInDate == null
                || checkOutDate == null
                || !checkOutDate.isAfter(checkInDate)) {

            return false;
        }

        if (guestCount <= 0) {
            return false;
        }

        if (pricePerNight == null
                || pricePerNight.compareTo(
                        BigDecimal.ZERO
                ) < 0) {

            return false;
        }

        if (totalPrice == null
                || totalPrice.compareTo(
                        BigDecimal.ZERO
                ) < 0) {

            return false;
        }

        String bookingSql
                = "INSERT INTO Booking "
                + "(CustomerID, TotalPrice, Status, "
                + "SpecialRequest) "
                + "VALUES (?, ?, 'pending', ?)";

        String detailSql
                = "INSERT INTO BookingDetail "
                + "(BookingID, RoomNumber, "
                + "CheckInDate, CheckOutDate, "
                + "PricePerNight, GuestCount, "
                + "SpecialRequest) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;

        try {

            conn = getConnection();

            /*
             * Start transaction.
             */
            conn.setAutoCommit(false);

            /*
             * Recheck availability using the same connection
             * immediately before inserting.
             */
            if (isRoomBookedInRange(
                    conn,
                    roomNumber,
                    checkInDate,
                    checkOutDate)) {

                conn.rollback();
                return false;
            }

            int bookingId;

            /*
             * -------------------------
             * Insert Booking header
             * -------------------------
             */
            try (PreparedStatement ps
                    = conn.prepareStatement(
                            bookingSql,
                            Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, customerId);
                ps.setBigDecimal(2, totalPrice);
                ps.setString(3, specialRequest);

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet keys
                        = ps.getGeneratedKeys()) {

                    if (!keys.next()) {
                        conn.rollback();
                        return false;
                    }

                    bookingId = keys.getInt(1);
                }
            }

            /*
             * -------------------------
             * Insert BookingDetail
             * -------------------------
             */
            try (PreparedStatement ps
                    = conn.prepareStatement(detailSql)) {

                ps.setInt(1, bookingId);
                ps.setInt(2, roomNumber);
                ps.setObject(3, checkInDate);
                ps.setObject(4, checkOutDate);
                ps.setBigDecimal(5, pricePerNight);
                ps.setInt(6, guestCount);
                ps.setString(7, specialRequest);

                int detailRows = ps.executeUpdate();

                if (detailRows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            /*
             * Everything succeeded.
             */
            conn.commit();

            return true;

        } catch (SQLException ex) {

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {

                    Logger.getLogger(
                            BookingDAO.class.getName()
                    ).log(
                            Level.SEVERE,
                            "Rollback failed",
                            rollbackEx
                    );
                }
            }

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);

            return false;

        } finally {

            if (conn != null) {

                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }

                try {
                    conn.close();
                } catch (SQLException ex) {

                    Logger.getLogger(
                            BookingDAO.class.getName()
                    ).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /*
     * =========================================================
     * 3. CHECK ROOM CONFLICT
     * =========================================================
     */
    public boolean isRoomBookedInRange(
            int roomNumber,
            LocalDate checkIn,
            LocalDate checkOut) {

        try (Connection conn = getConnection()) {

            return isRoomBookedInRange(
                    conn,
                    roomNumber,
                    checkIn,
                    checkOut
            );

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);

            /*
             * Safer behaviour:
             * when DB check fails, don't assume room is free.
             */
            return true;
        }
    }

    /*
     * Internal version used inside createBooking transaction.
     */
    private boolean isRoomBookedInRange(
            Connection conn,
            int roomNumber,
            LocalDate checkIn,
            LocalDate checkOut)
            throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM BookingDetail bd "
                + "JOIN Booking b "
                + "ON b.BookingID = bd.BookingID "
                + "WHERE bd.RoomNumber = ? "
                + "AND b.Status <> 'cancelled' "
                + "AND bd.CheckInDate < ? "
                + "AND bd.CheckOutDate > ?";

        try (PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, roomNumber);
            ps.setObject(2, checkOut);
            ps.setObject(3, checkIn);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /*
     * =========================================================
     * 4. GET BOOKING DETAILS
     * =========================================================
     */
    public List<BookingDetail> getBookingDetails(
            int bookingId) {

        try (Connection conn = getConnection()) {

            return getBookingDetails(
                    conn,
                    bookingId
            );

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);

            return new ArrayList<>();
        }
    }

    /*
     * Internal method to reuse an existing connection.
     */
    private List<BookingDetail> getBookingDetails(
            Connection conn,
            int bookingId)
            throws SQLException {

        List<BookingDetail> details
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "BookingDetailID, "
                + "BookingID, "
                + "RoomNumber, "
                + "CheckInDate, "
                + "CheckOutDate, "
                + "PricePerNight, "
                + "GuestCount, "
                + "SpecialRequest "
                + "FROM BookingDetail "
                + "WHERE BookingID = ? "
                + "ORDER BY BookingDetailID";

        try (PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs
                    = ps.executeQuery()) {

                while (rs.next()) {

                    BookingDetail detail
                            = new BookingDetail();

                    detail.setId(
                            rs.getInt(
                                    "BookingDetailID"
                            )
                    );

                    detail.setBookingId(
                            rs.getInt(
                                    "BookingID"
                            )
                    );

                    detail.setRoomNumber(
                            rs.getInt(
                                    "RoomNumber"
                            )
                    );

                    detail.setCheckInDate(
                            rs.getObject(
                                    "CheckInDate",
                                    LocalDate.class
                            )
                    );

                    detail.setCheckOutDate(
                            rs.getObject(
                                    "CheckOutDate",
                                    LocalDate.class
                            )
                    );

                    detail.setPricePerNight(
                            rs.getBigDecimal(
                                    "PricePerNight"
                            )
                    );

                    detail.setGuestCount(
                            rs.getInt(
                                    "GuestCount"
                            )
                    );

                    detail.setSpecialRequest(
                            rs.getString(
                                    "SpecialRequest"
                            )
                    );

                    details.add(detail);
                }
            }
        }

        return details;
    }

    /*
     * =========================================================
     * 5. GET BOOKING BY ID
     * =========================================================
     */
    public Booking getBookingById(
            int bookingId) {

        String sql
                = "SELECT "
                + "b.BookingID, "
                + "b.BookingDate, "
                + "b.Status, "
                + "b.TotalPrice, "
                + "b.SpecialRequest, "
                + "b.CancellationDate, "
                + "b.CancellationReason, "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.Street, "
                + "c.City, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.Password, "
                + "u.Role, "
                + "u.DisplayName "
                + "FROM Booking b "
                + "JOIN Customer c "
                + "ON b.CustomerID = c.CustomerID "
                + "JOIN [User] u "
                + "ON c.UserID = u.UserID "
                + "WHERE b.BookingID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs
                    = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                User user = new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role")
                );

                user.setDisplayName(
                        rs.getString("DisplayName")
                );

                Customer customer = new Customer(
                        rs.getInt("CustomerID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        rs.getString("Country"),
                        rs.getString("City"),
                        rs.getString("Street"),
                        rs.getString("Avatar"),
                        user
                );

                Booking booking = mapBooking(rs);

                booking.setCustomer(customer);

                booking.setDetails(
                        getBookingDetails(
                                conn,
                                bookingId
                        )
                );

                return booking;
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /*
     * =========================================================
     * 6. GET BOOKINGS OF ONE CUSTOMER
     * =========================================================
     */
    public List<Booking> getBookingsByCustomerID(
            int customerId) {

        List<Booking> bookings
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "BookingID, "
                + "BookingDate, "
                + "Status, "
                + "TotalPrice, "
                + "SpecialRequest, "
                + "CancellationDate, "
                + "CancellationReason "
                + "FROM Booking "
                + "WHERE CustomerID = ? "
                + "ORDER BY BookingDate DESC, "
                + "BookingID DESC";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs
                    = ps.executeQuery()) {

                while (rs.next()) {

                    Booking booking
                            = mapBooking(rs);

                    booking.setDetails(
                            getBookingDetails(
                                    conn,
                                    booking.getId()
                            )
                    );

                    bookings.add(booking);
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return bookings;
    }

    /*
     * =========================================================
     * 7. PAGINATED CUSTOMER BOOKING HISTORY
     * =========================================================
     */
    public List<Booking> getBookingsByCustomerID(
            int customerId,
            int page,
            int pageSize) {

        List<Booking> bookings
                = new ArrayList<>();

        if (page < 1) {
            page = 1;
        }

        if (pageSize <= 0) {
            pageSize = 5;
        }

        String sql
                = "SELECT "
                + "BookingID, "
                + "BookingDate, "
                + "Status, "
                + "TotalPrice, "
                + "SpecialRequest, "
                + "CancellationDate, "
                + "CancellationReason "
                + "FROM Booking "
                + "WHERE CustomerID = ? "
                + "ORDER BY BookingDate DESC, "
                + "BookingID DESC "
                + "OFFSET ? ROWS "
                + "FETCH NEXT ? ROWS ONLY";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setInt(
                    2,
                    (page - 1) * pageSize
            );
            ps.setInt(3, pageSize);

            try (ResultSet rs
                    = ps.executeQuery()) {

                while (rs.next()) {

                    Booking booking
                            = mapBooking(rs);

                    booking.setDetails(
                            getBookingDetails(
                                    conn,
                                    booking.getId()
                            )
                    );

                    bookings.add(booking);
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return bookings;
    }

    /*
     * =========================================================
     * 8. COUNT CUSTOMER BOOKINGS
     * =========================================================
     */
    public int countBookingsByCustomerID(
            int customerId) {

        String sql
                = "SELECT COUNT(*) AS Total "
                + "FROM Booking "
                + "WHERE CustomerID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs
                    = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 9. CANCEL BOOKING
     *
     * Important:
     * DO NOT DELETE booking.
     * RDS requires retaining booking history.
     * =========================================================
     */
    public boolean cancelBooking(
            int bookingId,
            int customerId,
            String reason) {

        /*
         * BR-39:
         * cancellation must occur at least
         * 24 hours before check-in.
         *
         * Because Booking can theoretically contain
         * several rooms, MIN(CheckInDate) is used.
         */
        String sql
                = "UPDATE Booking "
                + "SET Status = 'cancelled', "
                + "CancellationDate = SYSDATETIME(), "
                + "CancellationReason = ? "
                + "WHERE BookingID = ? "
                + "AND CustomerID = ? "
                + "AND Status IN ('pending', 'confirmed') "
                + "AND EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    WHERE bd.BookingID = Booking.BookingID "
                + "    GROUP BY bd.BookingID "
                + "    HAVING MIN(bd.CheckInDate) "
                + "       > DATEADD(HOUR, 24, SYSDATETIME()) "
                + ")";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setString(1, reason);
            ps.setInt(2, bookingId);
            ps.setInt(3, customerId);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);

            return false;
        }
    }

    /*
     * =========================================================
     * 10. STAFF / ADMIN BOOKING STATUS UPDATE
     * UC22
     * =========================================================
     */
    public boolean updateBookingStatus(
            int bookingId,
            String newStatus) {

        /*
         * These are the statuses defined by HotelDB.
         */
        if (!isValidBookingStatus(newStatus)) {
            return false;
        }

        /*
         * Prevent arbitrary transitions.
         */
        Booking booking
                = getBookingById(bookingId);

        if (booking == null) {
            return false;
        }

        if (!isValidStatusTransition(
                booking.getStatus(),
                newStatus)) {

            return false;
        }

        String sql
                = "UPDATE Booking "
                + "SET Status = ?, "
                + "CancellationDate = "
                + "CASE "
                + "WHEN ? = 'cancelled' "
                + "THEN SYSDATETIME() "
                + "ELSE NULL "
                + "END, "
                + "CancellationReason = "
                + "CASE "
                + "WHEN ? = 'cancelled' "
                + "THEN COALESCE("
                + "CancellationReason, "
                + "'Cancelled by Staff/Admin') "
                + "ELSE NULL "
                + "END "
                + "WHERE BookingID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, newStatus);
            ps.setString(3, newStatus);
            ps.setInt(4, bookingId);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);

            return false;
        }
    }

    /*
     * =========================================================
     * 11. CONFIRM BOOKING AFTER SUCCESSFUL PAYMENT
     * UC10
     * =========================================================
     */
    public boolean confirmBooking(
            int bookingId) {

        String sql
                = "UPDATE Booking "
                + "SET Status = 'confirmed' "
                + "WHERE BookingID = ? "
                + "AND Status = 'pending'";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);

            return false;
        }
    }

    /*
     * =========================================================
     * 12. EXPIRE UNPAID PENDING BOOKINGS
     *
     * DO NOT DELETE THEM.
     *
     * Current example timeout = 30 minutes.
     * RDS/SDS should use the same value.
     * =========================================================
     */
    public int expirePendingBookings() {

        String sql
                = "UPDATE Booking "
                + "SET Status = 'cancelled', "
                + "CancellationDate = SYSDATETIME(), "
                + "CancellationReason = "
                + "'Expired pending booking' "
                + "WHERE Status = 'pending' "
                + "AND BookingDate < "
                + "DATEADD(MINUTE, -30, "
                + "SYSDATETIME())";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            return ps.executeUpdate();

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);

            return 0;
        }
    }

    /*
     * =========================================================
     * 13. GET LATEST COMPLETED BOOKING FOR REVIEW
     * =========================================================
     */
    public Integer getLatestCompletedBookingId(
            int customerId,
            int roomNumber) {

        String sql
                = "SELECT TOP 1 "
                + "b.BookingID "
                + "FROM Booking b "
                + "JOIN BookingDetail bd "
                + "ON bd.BookingID = b.BookingID "
                + "WHERE b.CustomerID = ? "
                + "AND bd.RoomNumber = ? "
                + "AND b.Status = 'completed' "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM Review r "
                + "    WHERE r.BookingID = b.BookingID "
                + ") "
                + "ORDER BY bd.CheckOutDate DESC";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setInt(2, roomNumber);

            try (ResultSet rs
                    = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(
                            "BookingID"
                    );
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /*
     * =========================================================
     * 14. CHECK IF BOOKING BELONGS TO CUSTOMER
     * Useful for authorization.
     * =========================================================
     */
    public boolean isBookingOwnedByCustomer(
            int bookingId,
            int customerId) {

        String sql
                = "SELECT 1 "
                + "FROM Booking "
                + "WHERE BookingID = ? "
                + "AND CustomerID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, customerId);

            try (ResultSet rs
                    = ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, null, ex);

            return false;
        }
    }

    /*
 * =========================================================
 * SEARCH BOOKINGS FOR STAFF / ADMIN
 *
 * Search:
 * - Booking ID
 * - Customer ID
 * - Customer name
 * - Email
 * - Username
 *
 * Filter:
 * - Booking status
 * - Booking date range
 * =========================================================
     */
    public List<Booking> searchBookings(
            String keyword,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int pageSize) {

        List<Booking> bookings
                = new ArrayList<>();

        if (page < 1) {
            page = 1;
        }

        if (pageSize <= 0) {
            pageSize = 10;
        }

        StringBuilder sql
                = new StringBuilder();

        sql.append(
                "SELECT "
                + "b.BookingID, "
                + "b.BookingDate, "
                + "b.Status, "
                + "b.TotalPrice, "
                + "b.SpecialRequest, "
                + "b.CancellationDate, "
                + "b.CancellationReason, "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.City, "
                + "c.Street, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.[Password], "
                + "u.[Role], "
                + "u.DisplayName, "
                + "u.AccountStatus, "
                + "u.CreatedAt "
                + "FROM Booking b "
                + "JOIN Customer c "
                + "ON c.CustomerID = b.CustomerID "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "WHERE 1 = 1 "
        );

        boolean hasKeyword
                = keyword != null
                && !keyword.trim().isEmpty();

        boolean hasStatus
                = status != null
                && !status.trim().isEmpty()
                && isValidBookingStatus(status);

        if (hasKeyword) {

            sql.append(
                    "AND ( "
                    + "CAST(b.BookingID AS VARCHAR(20)) LIKE ? "
                    + "OR CAST(c.CustomerID AS VARCHAR(20)) LIKE ? "
                    + "OR c.FirstName LIKE ? "
                    + "OR c.LastName LIKE ? "
                    + "OR CONCAT("
                    + "COALESCE(c.FirstName, ''), ' ', "
                    + "COALESCE(c.LastName, '')"
                    + ") LIKE ? "
                    + "OR c.Email LIKE ? "
                    + "OR u.Username LIKE ? "
                    + ") "
            );
        }

        if (hasStatus) {

            sql.append(
                    "AND b.Status = ? "
            );
        }

        /*
     * BookingDate >= fromDate
         */
        if (fromDate != null) {

            sql.append(
                    "AND CAST(b.BookingDate AS DATE) >= ? "
            );
        }

        /*
     * BookingDate <= toDate
         */
        if (toDate != null) {

            sql.append(
                    "AND CAST(b.BookingDate AS DATE) <= ? "
            );
        }

        sql.append(
                "ORDER BY "
                + "b.BookingDate DESC, "
                + "b.BookingID DESC "
                + "OFFSET ? ROWS "
                + "FETCH NEXT ? ROWS ONLY"
        );

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(
                        sql.toString()
                )) {

            int index = 1;

            /*
         * Keyword appears 7 times.
             */
            if (hasKeyword) {

                String search
                        = "%"
                        + keyword.trim()
                        + "%";

                for (int i = 0;
                        i < 7;
                        i++) {

                    ps.setString(
                            index++,
                            search
                    );
                }
            }

            if (hasStatus) {

                ps.setString(
                        index++,
                        status
                );
            }

            if (fromDate != null) {

                ps.setObject(
                        index++,
                        fromDate
                );
            }

            if (toDate != null) {

                ps.setObject(
                        index++,
                        toDate
                );
            }

            ps.setInt(
                    index++,
                    (page - 1) * pageSize
            );

            ps.setInt(
                    index,
                    pageSize
            );

            try (ResultSet rs
                    = ps.executeQuery()) {

                while (rs.next()) {

                    User user
                            = new User(
                                    rs.getInt(
                                            "UserID"
                                    ),
                                    rs.getString(
                                            "Username"
                                    ),
                                    rs.getString(
                                            "Password"
                                    ),
                                    rs.getString(
                                            "Role"
                                    )
                            );

                    user.setDisplayName(
                            rs.getString(
                                    "DisplayName"
                            )
                    );

                    user.setAccountStatus(
                            rs.getString(
                                    "AccountStatus"
                            )
                    );

                    if (rs.getTimestamp(
                            "CreatedAt") != null) {

                        user.setCreatedAt(
                                rs.getTimestamp(
                                        "CreatedAt"
                                ).toLocalDateTime()
                        );
                    }

                    Customer customer
                            = new Customer(
                                    rs.getInt(
                                            "CustomerID"
                                    ),
                                    rs.getString(
                                            "FirstName"
                                    ),
                                    rs.getString(
                                            "LastName"
                                    ),
                                    rs.getString(
                                            "Email"
                                    ),
                                    rs.getString(
                                            "Phone"
                                    ),
                                    rs.getString(
                                            "Country"
                                    ),
                                    rs.getString(
                                            "City"
                                    ),
                                    rs.getString(
                                            "Street"
                                    ),
                                    rs.getString(
                                            "Avatar"
                                    ),
                                    user
                            );

                    Booking booking
                            = mapBooking(rs);

                    booking.setCustomer(
                            customer
                    );

                    booking.setDetails(
                            getBookingDetails(
                                    conn,
                                    booking.getId()
                            )
                    );

                    bookings.add(
                            booking
                    );
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(
                    Level.SEVERE,
                    null,
                    ex
            );
        }

        return bookings;
    }


    /*
 * =========================================================
 * COUNT SEARCHED BOOKINGS
 * =========================================================
     */
    public int countBookings(
            String keyword,
            String status,
            LocalDate fromDate,
            LocalDate toDate) {

        StringBuilder sql
                = new StringBuilder();

        sql.append(
                "SELECT COUNT(*) AS Total "
                + "FROM Booking b "
                + "JOIN Customer c "
                + "ON c.CustomerID = b.CustomerID "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "WHERE 1 = 1 "
        );

        boolean hasKeyword
                = keyword != null
                && !keyword.trim().isEmpty();

        boolean hasStatus
                = status != null
                && !status.trim().isEmpty()
                && isValidBookingStatus(status);

        if (hasKeyword) {

            sql.append(
                    "AND ( "
                    + "CAST(b.BookingID AS VARCHAR(20)) LIKE ? "
                    + "OR CAST(c.CustomerID AS VARCHAR(20)) LIKE ? "
                    + "OR c.FirstName LIKE ? "
                    + "OR c.LastName LIKE ? "
                    + "OR CONCAT("
                    + "COALESCE(c.FirstName, ''), ' ', "
                    + "COALESCE(c.LastName, '')"
                    + ") LIKE ? "
                    + "OR c.Email LIKE ? "
                    + "OR u.Username LIKE ? "
                    + ") "
            );
        }

        if (hasStatus) {

            sql.append(
                    "AND b.Status = ? "
            );
        }

        if (fromDate != null) {

            sql.append(
                    "AND CAST(b.BookingDate AS DATE) >= ? "
            );
        }

        if (toDate != null) {

            sql.append(
                    "AND CAST(b.BookingDate AS DATE) <= ? "
            );
        }

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(
                        sql.toString()
                )) {

            int index = 1;

            if (hasKeyword) {

                String search
                        = "%"
                        + keyword.trim()
                        + "%";

                for (int i = 0;
                        i < 7;
                        i++) {

                    ps.setString(
                            index++,
                            search
                    );
                }
            }

            if (hasStatus) {

                ps.setString(
                        index++,
                        status
                );
            }

            if (fromDate != null) {

                ps.setObject(
                        index++,
                        fromDate
                );
            }

            if (toDate != null) {

                ps.setObject(
                        index,
                        toDate
                );
            }

            try (ResultSet rs
                    = ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(
                            "Total"
                    );
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(
                    Level.SEVERE,
                    null,
                    ex
            );
        }

        return 0;
    }

    /*
     * =========================================================
     * 15. MAP COMMON BOOKING FIELDS
     * =========================================================
     */
    private Booking mapBooking(
            ResultSet rs)
            throws SQLException {

        Booking booking
                = new Booking();

        booking.setId(
                rs.getInt("BookingID")
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
                rs.getString("Status")
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

        return booking;
    }

    /*
     * =========================================================
     * 16. VALID BOOKING STATUS
     * Matches HotelDB CHECK constraint.
     * =========================================================
     */
    private boolean isValidBookingStatus(
            String status) {

        if (status == null) {
            return false;
        }

        return status.equals("pending")
                || status.equals("confirmed")
                || status.equals("cancelled")
                || status.equals("checked_in")
                || status.equals("checked_out")
                || status.equals("completed");
    }

    /*
     * =========================================================
     * 17. BOOKING STATUS TRANSITIONS
     *
     * RDS BR-45:
     * Invalid status transitions must be rejected.
     * =========================================================
     */
    private boolean isValidStatusTransition(
            String currentStatus,
            String newStatus) {

        if (currentStatus == null
                || newStatus == null) {

            return false;
        }

        if (currentStatus.equals(newStatus)) {
            return true;
        }

        switch (currentStatus) {

            case "pending":
                return newStatus.equals("confirmed")
                        || newStatus.equals("cancelled");

            case "confirmed":
                return newStatus.equals("checked_in")
                        || newStatus.equals("cancelled");

            case "checked_in":
                return newStatus.equals("checked_out");

            case "checked_out":
                return newStatus.equals("completed");

            /*
             * Final states.
             */
            case "completed":
            case "cancelled":
                return false;

            default:
                return false;
        }
    }
}
