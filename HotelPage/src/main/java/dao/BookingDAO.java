package dao;

import util.RoomOccupancySql;

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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Booking;
import model.BookingContact;
import model.BookingDetail;
import model.Customer;
import model.OnlineBookingDraft;
import model.User;
import util.OnlineStayPolicy;

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
                + "c.City, "
                + "c.Street, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.Password, "
                + "u.Role, "
                + "u.DisplayName "
                + "FROM Booking b "
                + "JOIN Customer c "
                + "ON b.CustomerID = c.CustomerID "
                + "LEFT JOIN [User] u "
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

    static BigDecimal lockAvailableRoomType(Connection conn, int typeId, LocalDate from,
            LocalDate to, int quantity, int guests, boolean walkIn) throws SQLException {
        model.OfflineBookingDraft.validateStay(from, to, guests, util.ReportPeriod.today());
        if (quantity < 1 || quantity > guests) {
            throw new IllegalArgumentException("Số lượng phòng không hợp lệ.");
        }

        int guestsPerRoom = (guests + quantity - 1) / quantity;
        BigDecimal nightly;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT PricePerNight FROM RoomType WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE RoomTypeID=? AND IsActive=1 AND Capacity>=?")) {
            ps.setInt(1, typeId);
            ps.setInt(2, guestsPerRoom);
            try (ResultSet rows = ps.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalArgumentException("Loại phòng không khả dụng hoặc vượt sức chứa.");
                }
                nightly = rows.getBigDecimal(1);
            }
        }

        if (RoomDAO.countAvailableRoomsByType(conn, from, to, typeId, guestsPerRoom) < quantity) {
            throw new IllegalArgumentException("Không đủ phòng loại đã chọn trong kỳ lưu trú.");
        }
        if (walkIn && from.equals(util.ReportPeriod.today())
                && RoomDAO.countReadyRooms(conn, typeId, from, to) < quantity) {
            throw new IllegalArgumentException("Hôm nay không đủ phòng sẵn sàng để nhận ngay.");
        }
        return nightly;
    }

    // Caller phải khóa và kiểm tra kho của loại phòng trên cùng connection trước khi ghi.
    int insertRoomTypeBooking(Connection conn, int customerId, int typeId, LocalDate from,
            LocalDate to, int quantity, int guests, BigDecimal nightly, String specialRequest,
            Integer actor, String requestKey, String promotionCode) throws SQLException {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(from, to);
        BigDecimal total = nightly.multiply(BigDecimal.valueOf(nights * quantity));
        List<Integer> rooms = actor == null
                ? RoomTypeInventory.load(conn, typeId, from, null).reserve(from, to, quantity)
                : java.util.Collections.nCopies(quantity, null);
        if (rooms.size() != quantity) {
            throw new IllegalArgumentException("Không đủ phòng loại đã chọn trong kỳ lưu trú.");
        }
        int bookingId = insertBooking(conn, customerId, total, specialRequest, actor, requestKey, promotionCode);
        insertBookingDetails(conn, bookingId, typeId, from, to, nightly, rooms, guests, specialRequest);
        return bookingId;
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

    private void insertBookingDetails(Connection conn, int id, int typeId, LocalDate checkIn,
            LocalDate checkOut, BigDecimal nightly, List<Integer> rooms, int guests, String specialRequest) throws SQLException {
        String sql = "INSERT BookingDetail(BookingID,RoomTypeID,RoomNumber,CheckInDate,CheckOutDate,"
                + "PricePerNight,GuestCount,SpecialRequest,IsProvisionalRoom) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, typeId);
            ps.setObject(4, checkIn);
            ps.setObject(5, checkOut);
            ps.setBigDecimal(6, nightly);
            ps.setNString(8, specialRequest);
            int quantity = rooms.size();
            for (int i = 0; i < quantity; i++) {
                ps.setObject(3, rooms.get(i), java.sql.Types.INTEGER);
                ps.setInt(7, guests / quantity + (i < guests % quantity ? 1 : 0));
                ps.setBoolean(9, rooms.get(i) != null);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int createBooking(int customerId, int typeId, LocalDate checkIn, LocalDate checkOut,
            int quantity, int guests, BigDecimal nightly, String specialRequest) throws SQLException {
        return createOnlineRoomTypeBooking(customerId, typeId, checkIn, checkOut, quantity, guests,
                nightly, specialRequest, null);
    }

    public int createRoomTypeBooking(int customerId, int typeId, LocalDate checkIn, LocalDate checkOut,
            int quantity, int guests, BigDecimal nightly, model.BookingContact contact) throws SQLException {
        return createOnlineRoomTypeBooking(customerId, typeId, checkIn, checkOut, quantity, guests,
                nightly, null, java.util.Objects.requireNonNull(contact));
    }

    private int createOnlineRoomTypeBooking(int customerId, int typeId, LocalDate checkIn, LocalDate checkOut,
            int quantity, int guests, BigDecimal nightly, String specialRequest, model.BookingContact contact)
            throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal currentPrice = lockAvailableRoomType(conn, typeId, checkIn, checkOut, quantity, guests, false);
                if (currentPrice.compareTo(nightly) != 0) {
                    throw new IllegalArgumentException("Giá phòng đã thay đổi. Vui lòng xem lại tóm tắt.");
                }

                int bookingId = insertRoomTypeBooking(conn, customerId, typeId, checkIn, checkOut,
                        quantity, guests, nightly, specialRequest, null, null, null);
                if (contact != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE Booking SET OnlineDepositPolicy=1, HoldExpiresAt=DATEADD(MINUTE,?,SYSUTCDATETIME()), ContactName=?, "
                            + "ContactPhone=?, ContactEmail=?, ContactCountry=? WHERE BookingID=?")) {
                        ps.setInt(1, OnlineStayPolicy.HOLD_MINUTES);
                        bindContact(ps, 2, contact);
                        ps.setInt(6, bookingId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return bookingId;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public static final class SelectionConflict extends Exception {
        private final String reason;
        private final String roomName;
        private final int available;

        private SelectionConflict(String reason, String roomName, int available) {
            this.reason = reason;
            this.roomName = roomName;
            this.available = available;
        }

        public String getReason() { return reason; }
        public String getRoomName() { return roomName; }
        public int getAvailable() { return available; }
    }

    public int confirmOnlineSelection(int customerId, OnlineBookingDraft draft, int percent,
            String voucherCode, BigDecimal payable)
            throws SQLException, SelectionConflict {
        List<OnlineBookingDraft.RoomLine> lines = new ArrayList<>(draft.getRooms());
        lines.sort(java.util.Comparator.comparingInt(OnlineBookingDraft.RoomLine::getTypeId));
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (OnlineBookingDraft.RoomLine line : lines) {
                    lockRoomType(conn, line.getTypeId());
                }
                int previous = findOnlineBooking(conn, customerId, draft.getToken());
                if (previous > 0) {
                    conn.commit();
                    return previous;
                }
                Map<Integer, List<Integer>> rooms = validateOnlineSelection(conn, draft, lines, percent, payable);
                int bookingId = insertOnlineSelection(conn, customerId, draft, percent);
                String sql = "INSERT BookingDetail(BookingID,RoomTypeID,RoomNumber,CheckInDate,CheckOutDate,"
                        + "PricePerNight,GuestCount,BookedAdults,BookedChildren,IsProvisionalRoom) VALUES(?,?,?,?,?,?,?,?,?,1)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (OnlineBookingDraft.RoomLine line : lines) {
                        ps.setInt(1, bookingId);
                        ps.setInt(2, line.getTypeId());
                        ps.setObject(4, draft.getCheckIn());
                        ps.setObject(5, draft.getCheckOut());
                        ps.setBigDecimal(6, line.getNightly());
                        List<Integer> assigned = rooms.get(line.getTypeId());
                        for (int room = 0; room < line.getQuantity(); room++) {
                            OnlineBookingDraft.RoomParty party = line.getParties().get(room);
                            ps.setInt(3, assigned.get(room));
                            ps.setInt(7, party.getAdults() + party.getChildren());
                            ps.setInt(8, party.getAdults());
                            ps.setInt(9, party.getChildren());
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
                if (voucherCode != null) {
                    try {
                        PaymentDAO.createPendingOnlinePayment(conn, bookingId, customerId,
                                "bank_transfer", voucherCode, percent, payable);
                    } catch (PaymentDAO.VoucherUnavailableException ex) {
                        throw new SelectionConflict("voucher", null, 0);
                    }
                }
                conn.commit();
                return bookingId;
            } catch (SQLException | RuntimeException | SelectionConflict ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private Map<Integer, List<Integer>> validateOnlineSelection(Connection conn, OnlineBookingDraft draft,
            List<OnlineBookingDraft.RoomLine> lines, int percent, BigDecimal payable) throws SQLException, SelectionConflict {
        if (!util.OnlineStayDates.isValid(draft.getCheckIn(), draft.getCheckOut(), util.ReportPeriod.today())
                || draft.getAdults() < 1 || draft.getChildren() < 0 || lines.isEmpty() || draft.getContact() == null
                || payable.signum() <= 0 || payable.stripTrailingZeros().scale() > 0 || payable.compareTo(draft.getTotal()) > 0
                || (percent != OnlineStayPolicy.FULL_PERCENT
                && (percent != OnlineStayPolicy.DEPOSIT_PERCENT || !OnlineStayPolicy.canDeposit(payable)))) {
            throw new SelectionConflict("criteria", null, 0);
        }
        Map<Integer, List<Integer>> rooms = new java.util.HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT PricePerNight,Capacity,MaxChildren FROM RoomType WHERE RoomTypeID=? AND IsActive=1")) {
            for (OnlineBookingDraft.RoomLine line : lines) {
                if (line.getQuantity() < 1 || rooms.containsKey(line.getTypeId())
                        || line.getParties().stream().anyMatch(party -> party.getAdults() < 1 || party.getChildren() < 0)) {
                    throw new SelectionConflict("criteria", null, 0);
                }
                ps.setInt(1, line.getTypeId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SelectionConflict("stock", line.getName(), 0);
                    }
                    if (line.getNightly().compareTo(rs.getBigDecimal(1)) != 0) {
                        throw new SelectionConflict("price", line.getName(), 0);
                    }
                    if (line.getMaxAdultsPerRoom() > rs.getInt(2)) {
                        throw new SelectionConflict("capacity", line.getName(), 0);
                    }
                    if (line.getMaxChildrenPerRoom() > rs.getInt(3)) {
                        throw new SelectionConflict("children", line.getName(), 0);
                    }
                }
                RoomTypeInventory inventory = RoomTypeInventory.load(conn, line.getTypeId(), draft.getCheckIn(), null);
                List<Integer> assigned = inventory.reserve(draft.getCheckIn(), draft.getCheckOut(), line.getQuantity());
                if (assigned.size() < line.getQuantity()) {
                    int available = inventory.available(draft.getCheckIn(), draft.getCheckOut());
                    throw new SelectionConflict("stock", line.getName(), available);
                }
                rooms.put(line.getTypeId(), assigned);
            }
        }
        return rooms;
    }

    private int insertOnlineSelection(Connection conn, int customerId, OnlineBookingDraft draft, int percent)
            throws SQLException {
        String sql = "SET NOCOUNT ON; DECLARE @ids TABLE(ID INT); "
                + "INSERT Booking(CustomerID,TotalPrice,Status,OnlineDepositPolicy,HoldExpiresAt,OnlineRequestKey,Adults,Children,OnlinePaymentPercent,"
                + "ContactName,ContactPhone,ContactEmail,ContactCountry) OUTPUT INSERTED.BookingID INTO @ids "
                + "VALUES(?,?,'pending',1,DATEADD(MINUTE,?,SYSUTCDATETIME()),?,?,?,?,?,?,?,?); SELECT ID FROM @ids;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setBigDecimal(2, draft.getTotal());
            ps.setInt(3, OnlineStayPolicy.HOLD_MINUTES);
            ps.setString(4, draft.getToken());
            ps.setInt(5, draft.getAdults());
            ps.setInt(6, draft.getChildren());
            ps.setInt(7, percent);
            bindContact(ps, 8, draft.getContact());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Missing BookingID");
                }
                return rs.getInt(1);
            }
        }
    }

    private static void bindContact(PreparedStatement ps, int from, BookingContact contact) throws SQLException {
        ps.setNString(from, contact.getName());
        ps.setNString(from + 1, contact.getPhone());
        ps.setNString(from + 2, contact.getEmail());
        ps.setNString(from + 3, contact.getCountry());
    }

    public int findOnlineBooking(int customerId, String requestKey) throws SQLException {
        try (Connection conn = getConnection()) {
            return findOnlineBooking(conn, customerId, requestKey);
        }
    }

    private int findOnlineBooking(Connection conn, int customerId, String requestKey) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT BookingID FROM Booking WHERE OnlineRequestKey=? AND CustomerID=?")) {
            ps.setString(1, requestKey);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    static void lockRoomType(Connection conn, int typeId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT RoomTypeID FROM RoomType WITH(UPDLOCK,HOLDLOCK) WHERE RoomTypeID=?")) {
            ps.setInt(1, typeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
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
                + "AND " + RoomOccupancySql.overlaps("b", "bd");

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
    List<BookingDetail> getBookingDetails(
            Connection conn,
            int bookingId)
            throws SQLException {

        List<BookingDetail> details
                = new ArrayList<>();

        String sql = "SELECT d.BookingDetailID,d.BookingID,CASE WHEN d.IsProvisionalRoom=0 THEN d.RoomNumber END AS RoomNumber,"
                + "d.CheckInDate,d.CheckOutDate,d.PricePerNight,d.GuestCount,d.BookedAdults,d.BookedChildren,"
                + "d.SpecialRequest,t.Name AS RoomTypeName "
                + "FROM BookingDetail d LEFT JOIN Room r ON r.RoomNumber=d.RoomNumber "
                + "LEFT JOIN RoomType t ON t.RoomTypeID=COALESCE(d.RoomTypeID,r.RoomTypeID) "
                + "WHERE d.BookingID=? ORDER BY d.BookingDetailID";

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

                    detail.setRoomNumber(rs.getObject("RoomNumber", Integer.class));
                    detail.setRoomTypeName(rs.getString("RoomTypeName"));
                    detail.setBookedAdults(rs.getObject("BookedAdults", Integer.class));
                    detail.setBookedChildren(rs.getObject("BookedChildren", Integer.class));

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
                + "b.OnlineRequestKey,b.OnlinePaymentPercent, "
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
                + "u.Password, "
                + "u.Role, "
                + "u.DisplayName "
                + "FROM Booking b "
                + "JOIN Customer c "
                + "ON b.CustomerID = c.CustomerID "
                + "LEFT JOIN [User] u "
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
                booking.setOnlineRequestKey(rs.getString("OnlineRequestKey"));
                Number percent = (Number) rs.getObject("OnlinePaymentPercent");
                booking.setOnlinePaymentPercent(percent == null ? null : percent.intValue());
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
                + "CancellationReason, OnlineDepositPolicy, Adults, Children, "
                + "ContactName, ContactPhone, ContactEmail, ContactCountry "
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
                    booking.setAdults(rs.getObject("Adults", Integer.class));
                    booking.setChildren(rs.getObject("Children", Integer.class));
                    if (rs.getString("ContactName") != null) {
                        booking.setContact(new model.BookingContact(rs.getString("ContactName"), rs.getString("ContactPhone"),
                                rs.getString("ContactEmail"), rs.getString("ContactCountry")));
                    }

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
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT CreatedByUserID FROM Booking WHERE BookingID=?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getObject(1) != null) {
                    return false;
                }
            }
        } catch (SQLException ex) {
            return false;
        }
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
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
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
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
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
     * True when the booking produced a wallet refund, including partial
     * refunds.
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
        if (checkIn == null) {
            return BigDecimal.ZERO;
        }
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

        if ("checked_in".equals(newStatus)) {
            // Nhận phòng cần phòng đã chọn, người đối chiếu và số khách thực tế.
            return false;
        }

        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT CreatedByUserID FROM Booking WHERE BookingID=?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getObject(1) != null) {
                    return false;
                }
            }
        } catch (SQLException ex) {
            return false;
        }

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

        // Deposit-policy stays must settle through the counter checkout transaction.
        if (booking.isOnlineDepositPolicy()
                && ("checked_out".equals(newStatus) || "completed".equals(newStatus))) {
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
                + "WHERE BookingID = ? AND Status=?";

        try (Connection conn = getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, newStatus);
            ps.setString(3, newStatus);
            ps.setInt(4, bookingId);
            ps.setString(5, booking.getStatus());

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
                + "AND Status = 'pending' AND CreatedByUserID IS NULL";

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
                + "AND CreatedByUserID IS NULL "
                + "AND ((HoldExpiresAt IS NOT NULL AND HoldExpiresAt<=SYSUTCDATETIME()) "
                + "OR (HoldExpiresAt IS NULL AND BookingDate<=DATEADD(MINUTE,-30,SYSDATETIME())))";

        String releaseVouchers = "DECLARE @released TABLE(DiscountID INT NOT NULL); "
                + "UPDATE p SET PaymentStatus='failed' OUTPUT deleted.DiscountID INTO @released "
                + "FROM Payment p JOIN Booking b ON b.BookingID=p.BookingID WHERE b.Status='cancelled' "
                + "AND b.OnlineRequestKey IS NOT NULL AND p.PaymentStatus='pending' AND p.DiscountID IS NOT NULL; "
                + "UPDATE d SET Quantity=d.Quantity+returned.Released FROM Discount d JOIN "
                + "(SELECT DiscountID,COUNT(*) Released FROM @released GROUP BY DiscountID) returned "
                + "ON returned.DiscountID=d.DiscountID;";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement expire = conn.prepareStatement(sql);
                    PreparedStatement release = conn.prepareStatement(releaseVouchers)) {
                int expired = expire.executeUpdate();
                release.executeUpdate();
                conn.commit();
                return expired;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }

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
     * - Stay date range and room number
 * =========================================================
     */
    private static void appendBookingFilters(StringBuilder sql, LocalDate fromDate, LocalDate toDate,
            Integer roomNumber, String source) {
        if ("online".equals(source)) {
            sql.append("AND b.CreatedByUserID IS NULL ");
        } else if ("offline".equals(source)) {
            sql.append("AND b.CreatedByUserID IS NOT NULL ");
        }
        if (fromDate == null && toDate == null && roomNumber == null) {
            return;
        }
        sql.append("AND EXISTS (SELECT 1 FROM BookingDetail stay WHERE stay.BookingID=b.BookingID ");
        if (fromDate != null) {
            sql.append("AND stay.CheckOutDate>? ");
        }
        if (toDate != null) {
            sql.append("AND stay.CheckInDate<=? ");
        }
        if (roomNumber != null) {
            sql.append("AND stay.RoomNumber=? ");
        }
        sql.append(") ");
    }

    private static int bindStayFilter(PreparedStatement ps, int index, LocalDate fromDate, LocalDate toDate,
            Integer roomNumber) throws SQLException {
        if (fromDate != null) {
            ps.setObject(index++, fromDate);
        }
        if (toDate != null) {
            ps.setObject(index++, toDate);
        }
        if (roomNumber != null) {
            ps.setInt(index++, roomNumber);
        }
        return index;
    }

    public List<Booking> searchBookings(
            String keyword,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            Integer roomNumber,
            String source,
            int page,
            int pageSize) {
        return searchBookings(keyword, status, fromDate, toDate, roomNumber, source, page, pageSize, null);
    }

    public List<Booking> searchBookings(String keyword, String status, LocalDate fromDate, LocalDate toDate, Integer roomNumber, String source, int page, int pageSize, String sort) {

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
                + "b.CreatedByUserID, "
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
                + "LEFT JOIN [User] u "
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

        appendBookingFilters(sql, fromDate, toDate, roomNumber, source);

        sql.append(
                "ORDER BY "
                + ("asc".equals(sort) ? "b.TotalPrice ASC, " : "desc".equals(sort) ? "b.TotalPrice DESC, " : "")
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

            index = bindStayFilter(ps, index, fromDate, toDate, roomNumber);

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

                    booking.setOffline(rs.getObject("CreatedByUserID") != null);
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
            LocalDate toDate,
            Integer roomNumber,
            String source) {

        StringBuilder sql
                = new StringBuilder();

        sql.append(
                "SELECT COUNT(*) AS Total "
                + "FROM Booking b "
                + "JOIN Customer c "
                + "ON c.CustomerID = b.CustomerID "
                + "LEFT JOIN [User] u "
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

        appendBookingFilters(sql, fromDate, toDate, roomNumber, source);

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

            bindStayFilter(ps, index, fromDate, toDate, roomNumber);

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
