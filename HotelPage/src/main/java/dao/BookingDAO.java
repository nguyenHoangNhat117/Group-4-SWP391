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
            ).log(Level.SEVERE, "Operation failed", ex);
        }

        return bookings;
    }

    public int createBooking(
            int customerId,
            int roomNumber,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            BigDecimal pricePerNight,
            int guestCount,
            BigDecimal totalPrice,
            String specialRequest
    ) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                int id = createBookingInTransaction(
                        conn,
                        customerId,
                        roomNumber,
                        checkInDate,
                        checkOutDate,
                        pricePerNight,
                        guestCount,
                        totalPrice,
                        specialRequest,
                        null,
                        null,
                        null
                );

                conn.commit();
                return id;

            } catch (SQLException | IllegalArgumentException ex) {
                conn.rollback();

                Logger.getLogger(BookingDAO.class.getName())
                        .log(Level.WARNING, "Booking rejected", ex);

                return 0;
            }

        } catch (SQLException ex) {
            Logger.getLogger(BookingDAO.class.getName())
                    .log(Level.SEVERE, "Booking failed", ex);

            return 0;
        }
    }

    /*
 * Online và offline dùng cùng cơ chế khóa phòng.
 * Method này không tự commit: caller quản lý transaction.
     */
    int createBookingInTransaction(
            Connection conn,
            int customerId,
            int roomNumber,
            LocalDate checkIn,
            LocalDate checkOut,
            BigDecimal pricePerNight,
            int guests,
            BigDecimal total,
            String specialRequest,
            Integer actor,
            String requestKey,
            String promotionCode
    ) throws SQLException {

        model.OfflineBookingDraft.validateStay(
                checkIn,
                checkOut,
                guests,
                util.ReportPeriod.today()
        );

        BigDecimal currentPrice = lockAvailableRoom(
                conn, roomNumber, checkIn, checkOut, guests
        );

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                checkIn, checkOut
        );

        BigDecimal expectedTotal = currentPrice.multiply(
                BigDecimal.valueOf(nights)
        );

        if (pricePerNight == null
                || total == null
                || currentPrice.compareTo(pricePerNight) != 0
                || expectedTotal.compareTo(total) != 0) {
            throw new IllegalArgumentException(
                    "Giá phòng đã thay đổi. Vui lòng xem lại tóm tắt."
            );
        }

        int id = insertBooking(conn, customerId, total, specialRequest, actor, requestKey, promotionCode);
        insertBookingDetail(conn, id, roomNumber, checkIn, checkOut, currentPrice, guests, specialRequest);
        return id;
    }

    private int insertBooking(Connection conn, int customerId, BigDecimal total, String specialRequest,
            Integer actor, String requestKey, String promotionCode) throws SQLException {
        String sql;

        if (actor == null) {
            sql = "SET NOCOUNT ON; "
                    + "DECLARE @ids TABLE(ID INT); "
                    + "INSERT Booking("
                    + "CustomerID,TotalPrice,Status,SpecialRequest"
                    + ") OUTPUT INSERTED.BookingID INTO @ids "
                    + "VALUES(?,?,'pending',?); "
                    + "SELECT ID FROM @ids;";
        } else {
            sql = "SET NOCOUNT ON; "
                    + "DECLARE @ids TABLE(ID INT); "
                    + "INSERT Booking("
                    + "CustomerID,TotalPrice,Status,SpecialRequest,"
                    + "CreatedByUserID,OfflineRequestKey,"
                    + "OfflinePromotionCode,HoldExpiresAt,"
                    + "OfflineDepositPercent"
                    + ") OUTPUT INSERTED.BookingID INTO @ids "
                    + "VALUES(?,?,'pending',?,?,?,?,"
                    + "DATEADD(MINUTE,?,SYSUTCDATETIME()),?); "
                    + "SELECT ID FROM @ids;";
        }

        int id;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setBigDecimal(2, total);
            ps.setNString(3, specialRequest);

            if (actor != null) {
                ps.setInt(4, actor);
                ps.setString(5, requestKey);
                ps.setNString(
                        6,
                        promotionCode == null || promotionCode.isEmpty()
                        ? null : promotionCode
                );
                ps.setInt(7, util.OfflineBookingPolicy.HOLD_MINUTES);
                ps.setInt(8, util.OfflineBookingPolicy.DEPOSIT_PERCENT);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Missing BookingID");
                }

                id = rs.getInt(1);
            }
        }

        return id;
    }

    private void insertBookingDetail(Connection conn, int id, int roomNumber, LocalDate checkIn,
            LocalDate checkOut, BigDecimal currentPrice, int guests, String specialRequest) throws SQLException {
        String detailSql
                = "INSERT BookingDetail("
                + "BookingID,RoomNumber,CheckInDate,CheckOutDate,"
                + "PricePerNight,GuestCount,SpecialRequest"
                + ") VALUES(?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
            ps.setInt(1, id);
            ps.setInt(2, roomNumber);
            ps.setObject(3, checkIn);
            ps.setObject(4, checkOut);
            ps.setBigDecimal(5, currentPrice);
            ps.setInt(6, guests);
            ps.setNString(7, specialRequest);
            ps.executeUpdate();
        }
    }

    public int createRoomTypeBooking(int customerId, int typeId, LocalDate checkIn, LocalDate checkOut,
            int quantity, int guests, BigDecimal nightly, model.BookingContact contact) throws SQLException {
        model.OfflineBookingDraft.validateStay(checkIn, checkOut, guests, util.ReportPeriod.today());
        if (quantity < 1 || quantity > guests) {
            throw new IllegalArgumentException("Invalid room quantity");
        }

        int guestsPerRoom = (guests + quantity - 1) / quantity;
        String candidates = "SELECT RoomNumber FROM Room WHERE RoomTypeID=? AND Status='available' ORDER BY RoomNumber";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Integer> roomNumbers = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(candidates)) {
                    ps.setInt(1, typeId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            roomNumbers.add(rs.getInt(1));
                        }
                    }
                }

                List<Integer> selected = new ArrayList<>();
                for (int roomNumber : roomNumbers) {
                    BigDecimal currentPrice;
                    try {
                        currentPrice = lockAvailableRoom(conn, roomNumber, checkIn, checkOut, guestsPerRoom, typeId);
                    } catch (IllegalArgumentException unavailable) {
                        continue;
                    }
                    if (currentPrice.compareTo(nightly) != 0) {
                        throw new IllegalArgumentException("Room price has changed");
                    }
                    selected.add(roomNumber);
                    if (selected.size() == quantity) {
                        break;
                    }
                }
                if (selected.size() != quantity) {
                    throw new IllegalArgumentException("Insufficient room availability");
                }

                long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                BigDecimal total = nightly.multiply(BigDecimal.valueOf(nights * quantity));
                int bookingId = insertBooking(conn, customerId, total, null, null, null, null);
                try (PreparedStatement ps = conn.prepareStatement("UPDATE Booking SET OnlineDepositPolicy=1, ContactName=?, "
                        + "ContactPhone=?, ContactEmail=?, ContactCountry=? WHERE BookingID=?")) {
                    ps.setNString(1, contact.getName());
                    ps.setNString(2, contact.getPhone());
                    ps.setNString(3, contact.getEmail());
                    ps.setNString(4, contact.getCountry());
                    ps.setInt(5, bookingId);
                    ps.executeUpdate();
                }

                for (int i = 0; i < quantity; i++) {
                    int roomGuests = guests / quantity + (i < guests % quantity ? 1 : 0);
                    insertBookingDetail(conn, bookingId, selected.get(i), checkIn, checkOut, nightly, roomGuests, null);
                }
                conn.commit();
                return bookingId;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    static BigDecimal lockAvailableRoom(
            Connection conn,
            int roomNumber,
            LocalDate from,
            LocalDate to,
            int guests
    ) throws SQLException {

        return lockAvailableRoom(conn, roomNumber, from, to, guests, null);
    }

    private static BigDecimal lockAvailableRoom(Connection conn, int roomNumber,
            LocalDate from, LocalDate to, int guests, Integer expectedType) throws SQLException {
        BigDecimal price;

        String roomSql
                = "SELECT rt.PricePerNight "
                + "FROM Room r WITH(UPDLOCK,HOLDLOCK) "
                + "JOIN RoomType rt WITH(HOLDLOCK) "
                + "ON rt.RoomTypeID=r.RoomTypeID "
                + "WHERE r.RoomNumber=? "
                + "AND r.Status='available' "
                + "AND rt.IsActive=1 "
                + "AND rt.Capacity>=? AND (? IS NULL OR r.RoomTypeID=?)";

        try (PreparedStatement ps = conn.prepareStatement(roomSql)) {
            ps.setInt(1, roomNumber);
            ps.setInt(2, guests);
            ps.setObject(3, expectedType, java.sql.Types.INTEGER);
            ps.setObject(4, expectedType, java.sql.Types.INTEGER);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Phòng không khả dụng hoặc vượt sức chứa."
                    );
                }

                price = rs.getBigDecimal(1);
            }
        }

        String conflictSql
                = "SELECT 1 FROM BookingDetail bd "
                + "JOIN Booking b ON b.BookingID=bd.BookingID "
                + "WHERE bd.RoomNumber=? "
                + "AND b.Status<>'cancelled' "
                + "AND bd.CheckInDate<? "
                + "AND bd.CheckOutDate>?";

        try (PreparedStatement ps = conn.prepareStatement(conflictSql)) {
            ps.setInt(1, roomNumber);
            ps.setObject(2, to);
            ps.setObject(3, from);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException(
                            "Phòng đã được đặt trong kỳ lưu trú. "
                            + "Vui lòng chọn phòng khác."
                    );
                }
            }
        }

        return price;
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
            ).log(Level.SEVERE, "Operation failed", ex);

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
            ).log(Level.SEVERE, "Operation failed", ex);

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
                + "b.ContactName, b.ContactPhone, b.ContactEmail, b.ContactCountry, b.OnlineDepositPolicy, "
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
                booking.setOnlineDepositPolicy(rs.getBoolean("OnlineDepositPolicy"));
                if (rs.getString("ContactName") != null) {
                    booking.setContact(new model.BookingContact(rs.getString("ContactName"), rs.getString("ContactPhone"),
                            rs.getString("ContactEmail"), rs.getString("ContactCountry")));
                }

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
            ).log(Level.SEVERE, "Operation failed", ex);
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
                + "CancellationReason, OnlineDepositPolicy "
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
                    booking.setOnlineDepositPolicy(rs.getBoolean("OnlineDepositPolicy"));

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
            ).log(Level.SEVERE, "Operation failed", ex);
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
                + "CancellationReason, OnlineDepositPolicy "
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
                    booking.setOnlineDepositPolicy(rs.getBoolean("OnlineDepositPolicy"));

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
            ).log(Level.SEVERE, "Operation failed", ex);
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
            ).log(Level.SEVERE, "Operation failed", ex);
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
    public boolean cancelBooking(int bookingId, int customerId, String reason) {
        return cancelBookingInternal(bookingId, customerId, reason);
    }

    private boolean cancelBookingInternal(int bookingId, Integer customerId, String reason) {
        return cancelBookingInternal(bookingId, customerId, reason, false);
    }

    private boolean cancelBookingInternal(int bookingId, Integer customerId, String reason, boolean noShow) {
        String lockSql = "SELECT Status, OnlineDepositPolicy, TotalPrice, CustomerID FROM Booking WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE BookingID = ?" + (customerId == null ? "" : " AND CustomerID = ?");
        String checkSql = "SELECT MIN(CheckInDate) AS FirstCheckIn FROM BookingDetail WHERE BookingID = ?";
        String updateSql = "UPDATE Booking SET Status = 'cancelled', "
                + "CancellationDate = SYSDATETIME(), CancellationReason = ? "
                + "WHERE BookingID = ? AND Status IN ('pending', 'confirmed')";
        String refundSql = "UPDATE Payment SET PaymentStatus = 'refunded' "
                + "WHERE BookingID = ? AND PaymentStatus = 'paid'";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            boolean onlinePolicy;
            BigDecimal total;
            int ownerId;
            try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                ps.setInt(1, bookingId);
                if (customerId != null) {
                    ps.setInt(2, customerId);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    String status = rs.getString(1);
                    onlinePolicy = rs.getBoolean(2);
                    total = rs.getBigDecimal(3);
                    ownerId = rs.getInt(4);
                    if (noShow ? !onlinePolicy || !"confirmed".equals(status)
                            : !"pending".equals(status) && !"confirmed".equals(status)) {
                        conn.rollback();
                        return false;
                    }
                }
            }
            java.sql.Date firstCheckIn;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || (firstCheckIn = rs.getDate(1)) == null) {
                        conn.rollback();
                        return false;
                    }
                }
            }
            LocalDate today = util.ReportPeriod.today();
            LocalDate checkIn = firstCheckIn.toLocalDate();
            if (noShow ? !checkIn.isBefore(today) : onlinePolicy ? today.isAfter(checkIn) : !today.isBefore(checkIn)) {
                conn.rollback();
                return false;
            }
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, reason);
                ps.setInt(2, bookingId);
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }
            BigDecimal paid = paidForCancellation(conn, bookingId);
            BigDecimal refund = onlinePolicy ? util.OnlineStayPolicy.refund(total, paid, checkIn, today) : paid;
            if (refund.signum() > 0) {
                creditRefund(conn, bookingId, ownerId, refund);
                if (refund.compareTo(paid) == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(refundSql)) {
                        ps.setInt(1, bookingId);
                        ps.executeUpdate();
                    }
                } else {
                    recordPartialRefund(conn, bookingId, refund);
                }
            }
            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            Logger.getLogger(BookingDAO.class.getName()).log(Level.SEVERE, "Cancellation failed", ex);
            return false;
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private static BigDecimal paidForCancellation(Connection conn, int bookingId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(Amount),0) FROM Payment WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE BookingID=? AND PaymentStatus='paid'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private static void recordPartialRefund(Connection conn, int bookingId, BigDecimal refund) throws SQLException {
        String sql = "WITH Allocated AS (SELECT PaymentID, Amount, SUM(Amount) OVER (ORDER BY PaymentID "
                + "ROWS UNBOUNDED PRECEDING)-Amount AS PreviousAmount FROM Payment "
                + "WHERE BookingID=? AND PaymentStatus='paid') "
                + "UPDATE p SET RefundedAmount=CASE WHEN a.Amount<?-a.PreviousAmount THEN a.Amount "
                + "ELSE ?-a.PreviousAmount END FROM Payment p JOIN Allocated a ON a.PaymentID=p.PaymentID "
                + "WHERE a.PreviousAmount<?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, refund);
            ps.setBigDecimal(3, refund);
            ps.setBigDecimal(4, refund);
            ps.executeUpdate();
        }
    }

    private static void creditRefund(Connection conn, int bookingId, int customerId, BigDecimal amount) throws SQLException {
        // Legacy installations still require Wallet.UserID (NOT NULL).
        // CustomerID and UserID are different keys: resolve UserID from Customer.
        boolean legacyUserIdColumn;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT CASE WHEN COL_LENGTH('dbo.Wallet','UserID') IS NULL THEN 0 ELSE 1 END")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                legacyUserIdColumn = rs.getInt(1) == 1;
            }
        }
        if (legacyUserIdColumn) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "IF NOT EXISTS (SELECT 1 FROM Wallet WITH (UPDLOCK, HOLDLOCK) WHERE CustomerID = ?) "
                    + "INSERT INTO Wallet(CustomerID, UserID, Balance) "
                    + "SELECT c.CustomerID, c.UserID, 0 FROM Customer c WHERE c.CustomerID = ?")) {
                ps.setInt(1, customerId);
                ps.setInt(2, customerId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "IF NOT EXISTS (SELECT 1 FROM Wallet WITH (UPDLOCK, HOLDLOCK) WHERE CustomerID = ?) "
                    + "INSERT INTO Wallet(CustomerID, Balance) VALUES (?, 0)")) {
                ps.setInt(1, customerId);
                ps.setInt(2, customerId);
                ps.executeUpdate();
            }
        }
        int walletId;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT WalletID FROM Wallet WITH (UPDLOCK, HOLDLOCK) WHERE CustomerID = ?")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Refund wallet not found");
                }
                walletId = rs.getInt(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE WalletTransaction SET Amount=Amount+? "
                + "WHERE WalletID=? AND BookingID=? AND TransactionType='refund'; "
                + "IF @@ROWCOUNT=0 INSERT INTO WalletTransaction(WalletID,BookingID,Amount,TransactionType,Description) "
                + "VALUES (?,?,?,'refund',N'Booking cancellation refund')")) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, walletId);
            ps.setInt(3, bookingId);
            ps.setInt(4, walletId);
            ps.setInt(5, bookingId);
            ps.setBigDecimal(6, amount);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Wallet SET Balance = Balance + ? WHERE WalletID = ?")) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, walletId);
            ps.executeUpdate();
        }
    }

    public void expireOnlineNoShows() throws SQLException {
        String sql = "SELECT b.BookingID FROM Booking b JOIN BookingDetail d ON d.BookingID=b.BookingID "
                + "WHERE b.OnlineDepositPolicy=1 AND b.Status='confirmed' GROUP BY b.BookingID HAVING MIN(d.CheckInDate)<?";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, util.ReportPeriod.today());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { ids.add(rs.getInt(1)); }
            }
        }
        for (int id : ids) {
            cancelBookingInternal(id, null, "No-show after check-in day", true);
        }
    }

    public void refundLateOnlinePayments() throws SQLException {
        String sql = "SELECT DISTINCT b.BookingID FROM Booking b JOIN Payment p ON p.BookingID=b.BookingID "
                + "WHERE b.OnlineDepositPolicy=1 AND b.Status='cancelled' AND p.PaymentStatus='paid' "
                + "AND p.PaymentMethod='bank_transfer' AND p.PaymentDate>b.CancellationDate";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        for (int bookingId : ids) {
            refundLateOnlinePayment(bookingId);
        }
    }

    private void refundLateOnlinePayment(int bookingId) throws SQLException {
        String lock = "SELECT CustomerID, CancellationDate FROM Booking WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE BookingID=? AND OnlineDepositPolicy=1 AND Status='cancelled'";
        String refund = "SET NOCOUNT ON; DECLARE @refunds TABLE (Amount DECIMAL(18,2)); "
                + "UPDATE Payment SET PaymentStatus='refunded' "
                + "OUTPUT INSERTED.Amount-INSERTED.RefundedAmount INTO @refunds "
                + "WHERE BookingID=? AND PaymentStatus='paid' AND PaymentMethod='bank_transfer' AND PaymentDate>?; "
                + "SELECT COALESCE(SUM(Amount),0) FROM @refunds;";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int customerId;
                java.sql.Timestamp cancelledAt;
                try (PreparedStatement ps = conn.prepareStatement(lock)) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return;
                        }
                        customerId = rs.getInt(1);
                        cancelledAt = rs.getTimestamp(2);
                    }
                }
                BigDecimal total;
                try (PreparedStatement ps = conn.prepareStatement(refund)) {
                    ps.setInt(1, bookingId);
                    ps.setTimestamp(2, cancelledAt);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        total = rs.getBigDecimal(1);
                    }
                }
                if (total.signum() > 0) {
                    creditRefund(conn, bookingId, customerId, total);
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    /**
     * True when the booking produced a wallet refund, including partial refunds.
     */
    public boolean hasRefundedPayment(int bookingId, int customerId) {
        String sql = "SELECT 1 FROM WalletTransaction t JOIN Booking b ON b.BookingID=t.BookingID "
                + "WHERE t.BookingID=? AND b.CustomerID=? AND t.TransactionType='refund'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingDAO.class.getName()).log(Level.SEVERE, "Refund status query failed", ex);
            return false;
        }
    }

    public boolean canCustomerCancelBooking(int bookingId, int customerId) {
        String sql = "SELECT 1 FROM Booking b CROSS APPLY (SELECT MIN(bd.CheckInDate) AS FirstCheckIn "
                + "FROM BookingDetail bd WHERE bd.BookingID=b.BookingID) stay "
                + "WHERE b.BookingID=? AND b.CustomerID=? AND b.Status IN ('pending', 'confirmed') "
                + "AND (stay.FirstCheckIn>? OR (b.OnlineDepositPolicy=1 AND stay.FirstCheckIn=?))";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, customerId);
            LocalDate today = util.ReportPeriod.today();
            ps.setObject(3, today);
            ps.setObject(4, today);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingDAO.class.getName()).log(Level.SEVERE, "Operation failed", ex);
            return false;
        }
    }

    public BigDecimal getCancellationRefund(Booking booking) {
        LocalDate checkIn = booking.getDetails().stream().map(BookingDetail::getCheckInDate)
                .min(LocalDate::compareTo).orElse(null);
        if (checkIn == null) { return BigDecimal.ZERO; }
        BigDecimal paid = new PaymentDAO().getTotalPaidAmount(booking.getId());
        return booking.isOnlineDepositPolicy()
                ? util.OnlineStayPolicy.refund(booking.getTotalPrice(), paid, checkIn, util.ReportPeriod.today()) : paid;
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

        if ("cancelled".equals(newStatus)) {
            return cancelBookingInternal(bookingId, null, "Cancelled by Staff/Admin");
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
                + "WHERE BookingID = ? AND Status=? "
                + "AND (?<>'checked_in' OR OnlineDepositPolicy=0 OR NOT EXISTS (SELECT 1 FROM BookingDetail "
                + "WHERE BookingID=Booking.BookingID AND CheckInDate<?))";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, newStatus);
            ps.setString(3, newStatus);
            ps.setInt(4, bookingId);
            ps.setString(5, booking.getStatus());
            ps.setString(6, newStatus);
            ps.setObject(7, util.ReportPeriod.today());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            Logger.getLogger(
                    BookingDAO.class.getName()
            ).log(Level.SEVERE, "Operation failed", ex);

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
            ).log(Level.SEVERE, "Operation failed", ex);

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
            ).log(Level.SEVERE, "Operation failed", ex);

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
            ).log(Level.SEVERE, "Operation failed", ex);
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
            ).log(Level.SEVERE, "Operation failed", ex);

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
                    "Operation failed",
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
                    "Operation failed",
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
