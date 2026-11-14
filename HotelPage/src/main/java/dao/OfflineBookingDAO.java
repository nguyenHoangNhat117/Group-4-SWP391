package dao;

import util.RoomOccupancySql;

import db.DBContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import model.*;
import util.AuthSupport;

import util.ReportPeriod;

public class OfflineBookingDAO extends DBContext {

    public void expireHolds() throws SQLException {
        try (Connection c = getConnection(); CallableStatement ps = c.prepareCall(
                "{call dbo.ExpireOfflineBookingHolds}"
        )) {
            ps.execute();
        }
    }

    public String creator(int bookingId) throws SQLException {
        String sql
                = "SELECT u.DisplayName "
                + "FROM Booking b "
                + "JOIN [User] u ON u.UserID=b.CreatedByUserID "
                + "WHERE b.BookingID=?";

        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static final class Quote implements Serializable {

        private static final long serialVersionUID = 1L;

        public final OfflineBookingDraft draft;

        private final BigDecimal nightly;
        private final BigDecimal gross;
        private final BigDecimal payable;
        private final String customerName;

        public Quote(
                OfflineBookingDraft draft,
                BigDecimal nightly,
                BigDecimal payable,
                String name
        ) {
            this.draft = draft;
            this.nightly = nightly;
            this.payable = payable;
            this.customerName = name;

            gross = nightly.multiply(BigDecimal.valueOf(
                    ChronoUnit.DAYS.between(
                            draft.checkIn, draft.checkOut
                    )
            ));
        }

        public OfflineBookingDraft getDraft() {
            return draft;
        }

        public BigDecimal getNightly() {
            return nightly;
        }

        public BigDecimal getGross() {
            return gross;
        }

        public BigDecimal getPayable() {
            return payable;
        }

        public String getCustomerName() {
            return customerName;
        }

        public long getNights() {
            return ChronoUnit.DAYS.between(
                    draft.checkIn, draft.checkOut
            );
        }
    }

    public List<Room> availableRooms(
            LocalDate from,
            LocalDate to,
            int guests
    ) throws SQLException {

        OfflineBookingDraft.validateStay(
                from, to, guests, ReportPeriod.today()
        );

        expireHolds();

        List<Room> rooms = new ArrayList<>();

        String sql
                = "SELECT TOP(100) "
                + "r.RoomNumber,rt.RoomTypeID,rt.Name,"
                + "rt.PricePerNight,rt.Capacity,rt.Beds "
                + "FROM Room r "
                + "JOIN RoomType rt ON rt.RoomTypeID=r.RoomTypeID "
                + "WHERE r.Status='available' AND r.NeedsCleaning=0 "
                + "AND rt.IsActive=1 AND rt.Capacity>=? "
                + "AND NOT EXISTS("
                + "SELECT 1 FROM BookingDetail bd "
                + "JOIN Booking b ON b.BookingID=bd.BookingID "
                + "WHERE bd.RoomNumber=r.RoomNumber "
                + "AND " + RoomOccupancySql.overlaps("b", "bd")
                + ") ORDER BY r.RoomNumber";

        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, guests);
            ps.setObject(2, to);
            ps.setObject(3, from);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(new Room(
                            rs.getInt(1),
                            "available",
                            new RoomType(
                                    rs.getInt(2),
                                    rs.getString(3),
                                    "",
                                    rs.getBigDecimal(4),
                                    rs.getInt(6),
                                    rs.getInt(5)
                            )
                    ));
                }
            }
        }

        return rooms;
    }

    public List<Customer> searchCustomers(String query)
            throws SQLException {

        List<Customer> customers = new ArrayList<>();
        query = AuthSupport.text(query);

        if (query.isEmpty()) {
            return customers;
        }

        if (query.length() > 100) {
            throw new IllegalArgumentException("Từ khóa tối đa 100 ký tự.");
        }

        String key = "%"
                + query.replace("~", "~~")
                        .replace("%", "~%")
                        .replace("_", "~_")
                        .replace("[", "~[")
                + "%";

        String sql = "SELECT TOP(30) c.*, "
                + "u.UserID AS LinkedUserID, u.Username, "
                + "u.Role AS AccountRole, u.AccountStatus "
                + "FROM Customer c "
                + "LEFT JOIN [User] u ON u.UserID=c.UserID "
                + "WHERE (c.UserID IS NULL OR "
                + "(u.UserID IS NOT NULL "
                + "AND LOWER(LTRIM(RTRIM(u.AccountStatus)))='active')) "
                + "AND (c.Phone LIKE ? ESCAPE '~' "
                + "OR c.Email LIKE ? ESCAPE '~' "
                + "OR u.Username LIKE ? ESCAPE '~') "
                + "ORDER BY c.CustomerID DESC";

        try (Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setNString(1, key);
            ps.setNString(2, key);
            ps.setNString(3, key);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = null;
                    Object linkedUserId = rs.getObject("LinkedUserID");

                    if (linkedUserId != null) {
                        user = new User();
                        user.setId(((Number) linkedUserId).intValue());
                        user.setUsername(rs.getString("Username"));
                        user.setRole(rs.getString("AccountRole"));
                        user.setAccountStatus(rs.getString("AccountStatus"));
                    }

                    customers.add(new Customer(
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
                    ));
                }
            }
        }

        return customers;
    }

    public Quote preview(
            int actor,
            OfflineBookingDraft draft
    ) throws SQLException {

        draft.validate(ReportPeriod.today());
        expireHolds();

        try (Connection c = getConnection()) {
            c.setAutoCommit(false);

            try {
                requireActor(c, actor);

                String name = customerName(c, draft);

                BigDecimal nightly = BookingDAO.lockAvailableRoom(
                        c,
                        draft.roomNumber,
                        draft.checkIn,
                        draft.checkOut,
                        draft.guests
                );

                BigDecimal gross = nightly.multiply(
                        BigDecimal.valueOf(ChronoUnit.DAYS.between(
                                draft.checkIn, draft.checkOut
                        ))
                );

                Quote quote = new Quote(
                        draft,
                        nightly,
                        discounted(c, draft.promotionCode, gross),
                        name
                );

                c.commit();
                return quote;

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public int create(int actor, Quote accepted)
            throws SQLException {

        OfflineBookingDraft d = accepted.draft;

        try (Connection c = getConnection()) {
            c.setAutoCommit(false);

            try {
                requireActor(c, actor);

                String duplicateSql
                        = "SELECT BookingID,CreatedByUserID "
                        + "FROM Booking WITH(UPDLOCK,HOLDLOCK) "
                        + "WHERE OfflineRequestKey=?";

                try (PreparedStatement ps
                        = c.prepareStatement(duplicateSql)) {
                    ps.setString(1, d.requestKey);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (rs.getInt(2) != actor) {
                                throw new IllegalArgumentException(
                                        "Yêu cầu thuộc nhân viên khác."
                                );
                            }

                            int id = rs.getInt(1);
                            c.commit();
                            return id;
                        }
                    }
                }

                d.validate(ReportPeriod.today());
                customerName(c, d);

                BigDecimal nightly = BookingDAO.lockAvailableRoom(
                        c, d.roomNumber, d.checkIn,
                        d.checkOut, d.guests
                );

                BigDecimal gross = nightly.multiply(
                        BigDecimal.valueOf(ChronoUnit.DAYS.between(
                                d.checkIn, d.checkOut
                        ))
                );

                BigDecimal payable = discounted(
                        c, d.promotionCode, gross
                );

                if (nightly.compareTo(accepted.nightly) != 0
                        || payable.compareTo(accepted.payable) != 0) {
                    throw new IllegalArgumentException(
                            "Giá hoặc khuyến mãi đã thay đổi. "
                            + "Vui lòng xem lại tóm tắt."
                    );
                }

                int customer = d.customerId == null
                        ? createCustomer(c, d)
                        : d.customerId;

                int id = new BookingDAO()
                        .createBookingInTransaction(
                                c,
                                customer,
                                d.roomNumber,
                                d.checkIn,
                                d.checkOut,
                                nightly,
                                d.guests,
                                gross,
                                d.specialRequest,
                                actor,
                                d.requestKey,
                                d.promotionCode
                        );

                try (PreparedStatement audit=c.prepareStatement("INSERT OfflineBookingEvent(BookingID,ActorUserID,RequestKey,Action,Note,Amount) VALUES(?,?,?,'create',N'Tạo đơn giữ phòng tại quầy',?)")) {
                    audit.setInt(1,id); audit.setInt(2,actor); audit.setString(3,d.requestKey); audit.setBigDecimal(4,gross); audit.executeUpdate();
                }
                c.commit();
                return id;

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void requireActor(Connection c, int actor)
            throws SQLException {

        String sql
                = "SELECT UserID FROM [User] WITH(HOLDLOCK) "
                + "WHERE UserID=? "
                + "AND Role IN ('staff','admin') "
                + "AND AccountStatus='active'";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, actor);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Nhân viên không còn quyền tạo đặt phòng. "
                            + "Vui lòng đăng nhập lại."
                    );
                }
            }
        }
    }

    private String customerName(
            Connection c,
            OfflineBookingDraft d) throws SQLException {

        if (d.customerId == null) {
            return d.lastName + " " + d.firstName;
        }

        /*
         * HOLDLOCK giữ dữ liệu khách/tài khoản ổn định đến khi
         * transaction preview hoặc create kết thúc.
         */
        String sql = "SELECT c.FirstName, c.LastName, c.Email, c.Phone, "
                + "c.UserID, u.UserID AS LinkedUserID, u.AccountStatus "
                + "FROM Customer c WITH(HOLDLOCK) "
                + "LEFT JOIN [User] u WITH(HOLDLOCK) ON u.UserID=c.UserID "
                + "WHERE c.CustomerID=?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, d.customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Khách hàng không còn tồn tại. "
                            + "Vui lòng tìm và chọn lại khách."
                    );
                }

                Object customerUserId = rs.getObject("UserID");

                // UserID NULL là khách tại quầy chưa có tài khoản website.
                if (customerUserId != null) {
                    Object linkedUserId = rs.getObject("LinkedUserID");
                    String status = AuthSupport.text(
                            rs.getString("AccountStatus")
                    );

                    if (linkedUserId == null
                            || !"active".equalsIgnoreCase(status)) {
                        throw new IllegalArgumentException(
                                "Tài khoản của khách đang bị khóa, "
                                + "ngừng hoạt động hoặc không còn hợp lệ. "
                                + "Vui lòng chọn khách khác hoặc liên hệ Admin."
                        );
                    }
                }

                String first = AuthSupport.text(rs.getString("FirstName"));
                String last = AuthSupport.text(rs.getString("LastName"));

                if ((first + last).isBlank()
                        || !OfflineBookingDraft.validPhone(
                                rs.getString("Phone"))) {
                    throw new IllegalArgumentException(
                            "Hồ sơ khách thiếu họ tên "
                            + "hoặc điện thoại hợp lệ. "
                            + "Cập nhật trong Customers trước khi đặt."
                    );
                }

                return last + " " + first;
            }
        }
    }

    static BigDecimal discounted(
            Connection c,
            String code,
            BigDecimal gross
    ) throws SQLException {

        if (code == null || code.isEmpty()) {
            return gross;
        }

        String sql
                = "SELECT SaleOff,MaximumDiscount "
                + "FROM Discount WITH(HOLDLOCK) "
                + "WHERE Code=? AND IsActive=1 AND Quantity>0 "
                + "AND SaleOff BETWEEN 0 AND 100 "
                + "AND (StartDate IS NULL OR StartDate<=?) "
                + "AND (EndDate IS NULL OR EndDate>=?) "
                + "AND (StartDate IS NULL OR EndDate IS NULL "
                + "OR StartDate<=EndDate) "
                + "AND (MinimumAmount IS NULL OR "
                + "(MinimumAmount>=0 AND MinimumAmount<=?)) "
                + "AND (MaximumDiscount IS NULL "
                + "OR MaximumDiscount>=0)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setNString(1, code);
            ps.setObject(2, ReportPeriod.today());
            ps.setObject(3, ReportPeriod.today());
            ps.setBigDecimal(4, gross);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Mã khuyến mãi không hợp lệ, hết hạn, "
                            + "hết lượt hoặc chưa đủ giá trị tối thiểu."
                    );
                }

                BigDecimal amount = gross
                        .multiply(rs.getBigDecimal(1))
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

                BigDecimal cap = rs.getBigDecimal(2);

                if (cap != null) {
                    amount = amount.min(cap);
                }

                return gross.subtract(amount).max(BigDecimal.ZERO);
            }
        }
    }

    public Map<String, Object> counterSummary(int bookingId)
            throws SQLException {

        expireHolds();

        try (Connection c = getConnection()) {
            c.setAutoCommit(false);

            try {
                Map<String, Object> data = new LinkedHashMap<>();

                String sql
                        = "SELECT b.TotalPrice,b.Status,"
                        + "b.OfflinePromotionCode,"
                        + "u.DisplayName Creator,"
                        + "cu.FirstName,cu.LastName,"
                        + "b.HoldExpiresAt,b.OfflineDepositPercent "
                        + "FROM Booking b WITH(HOLDLOCK) "
                        + "JOIN [User] u "
                        + "ON u.UserID=b.CreatedByUserID "
                        + "JOIN Customer cu "
                        + "ON cu.CustomerID=b.CustomerID "
                        + "WHERE b.BookingID=?";

                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, bookingId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Không tìm thấy đặt phòng offline."
                            );
                        }

                        data.put("bookingId", bookingId);
                        data.put("total", rs.getBigDecimal(1));
                        data.put("status", rs.getString(2));
                        data.put(
                                "promotionCode",
                                AuthSupport.text(rs.getString(3))
                        );
                        data.put("creator", rs.getString(4));
                        data.put(
                                "customerName",
                                AuthSupport.text(rs.getString(6))
                                + " "
                                + AuthSupport.text(rs.getString(5))
                        );

                        String deadline = rs.getObject(
                                7, java.time.LocalDateTime.class
                        ).atOffset(java.time.ZoneOffset.UTC)
                                .atZoneSameInstant(
                                        java.time.ZoneId.of(
                                                "Asia/Ho_Chi_Minh"
                                        )
                                )
                                .format(
                                        java.time.format.DateTimeFormatter
                                                .ofPattern(
                                                        "dd/MM/yyyy HH:mm:ss"
                                                )
                                );

                        data.put("holdExpiresAt", deadline);
                        data.put("depositPercent", rs.getInt(8));
                    }
                }

                BigDecimal paid;

                String paidSql
                        = "SELECT COALESCE(SUM(Amount-COALESCE(RefundedAmount,0)),0) "
                        + "FROM Payment WITH(HOLDLOCK) "
                        + "WHERE BookingID=? "
                        + "AND PaymentStatus='paid'";

                try (PreparedStatement ps
                        = c.prepareStatement(paidSql)) {
                    ps.setInt(1, bookingId);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        paid = rs.getBigDecimal(1);
                    }
                }

                BigDecimal total = (BigDecimal) data.get("total");
                BigDecimal payable = total;

                boolean pending
                        = "pending".equals(data.get("status"));

                if (paid.signum() == 0 && pending) {
                    try {
                        payable = discounted(
                                c,
                                (String) data.get("promotionCode"),
                                total
                        );
                    } catch (IllegalArgumentException e) {
                        data.put("promotionError", e.getMessage());
                    }
                }

                data.put("paid", paid);
                data.put("payable", payable);
                data.put(
                        "remaining",
                        "cancelled".equals(data.get("status")) ? BigDecimal.ZERO
                                : payable.subtract(paid).max(BigDecimal.ZERO)
                );
                data.put("firstPayment", paid.signum() == 0);
                data.put("depositAmount", payable.multiply(BigDecimal.valueOf((Integer)data.get("depositPercent")))
                        .divide(BigDecimal.valueOf(100),0,RoundingMode.CEILING));
                data.put(
                        "canChange",
                        pending && paid.signum() == 0
                );

                boolean payableStatus = Arrays.asList(
                        "pending", "confirmed", "checked_in"
                ).contains(data.get("status"));

                data.put(
                        "canPay",
                        !data.containsKey("promotionError")
                        && payableStatus
                        && (payable.compareTo(paid) > 0
                        || (paid.signum() == 0 && pending))
                );

                c.commit();
                return data;

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void changePending(
            int actor,
            int bookingId,
            String code,
            boolean cancel
    ) throws SQLException {

        expireHolds();
        code = AuthSupport.text(code);

        if (code.length() > 50) {
            throw new IllegalArgumentException(
                    "Mã khuyến mãi tối đa 50 ký tự."
            );
        }

        try (Connection c = getConnection()) {
            c.setAutoCommit(false);

            try {
                requireActor(c, actor);

                BigDecimal total;

                String sql
                        = "SELECT TotalPrice "
                        + "FROM Booking WITH(UPDLOCK,HOLDLOCK) "
                        + "WHERE BookingID=? "
                        + "AND CreatedByUserID IS NOT NULL "
                        + "AND Status='pending' "
                        + "AND HoldExpiresAt>SYSUTCDATETIME()";

                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, bookingId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Đơn đã hết hạn hoặc không còn "
                                    + "pending. Tải lại trang."
                            );
                        }

                        total = rs.getBigDecimal(1);
                    }
                }

                String paidSql
                        = "SELECT 1 FROM Payment WITH(HOLDLOCK) "
                        + "WHERE BookingID=? "
                        + "AND PaymentStatus='paid'";

                try (PreparedStatement ps
                        = c.prepareStatement(paidSql)) {
                    ps.setInt(1, bookingId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalArgumentException(
                                    "Đơn đã có thanh toán. "
                                    + "Không thể sửa hoặc hủy qua UC09."
                            );
                        }
                    }
                }

                if (!cancel) {
                    discounted(c, code, total);
                }

                String updateSql = cancel
                        ? "UPDATE Booking SET Status='cancelled',"
                        + "CancellationDate=SYSDATETIME(),"
                        + "CancellationReason="
                        + "N'Khách hủy trước thanh toán' "
                        + "WHERE BookingID=?"
                        : "UPDATE Booking "
                        + "SET OfflinePromotionCode=? "
                        + "WHERE BookingID=?";

                try (PreparedStatement ps
                        = c.prepareStatement(updateSql)) {
                    if (cancel) {
                        ps.setInt(1, bookingId);
                    } else {
                        ps.setNString(
                                1, code.isEmpty() ? null : code
                        );
                        ps.setInt(2, bookingId);
                    }

                    ps.executeUpdate();
                }

                c.commit();

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private int createCustomer(Connection c, OfflineBookingDraft d) throws SQLException {
        // A front-desk guest is a customer profile, not an automatically activated login.
        String query = "SELECT TOP(1) CustomerID FROM Customer WITH(UPDLOCK,HOLDLOCK) WHERE Phone=? OR (?<>'' AND Email=?)";
        try (PreparedStatement ps=c.prepareStatement(query)) {
            ps.setString(1,d.phone); ps.setString(2,d.email); ps.setString(3,d.email);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next()) throw new IllegalArgumentException("Email hoặc điện thoại đã tồn tại. Tìm và chọn khách có sẵn.");
            }
        }
        try (PreparedStatement ps=c.prepareStatement("SET NOCOUNT ON; DECLARE @ids TABLE(ID INT); INSERT Customer(UserID,FirstName,LastName,Email,Phone) OUTPUT INSERTED.CustomerID INTO @ids VALUES(NULL,?,?,?,?); SELECT ID FROM @ids;")) {
            ps.setNString(1,d.firstName); ps.setNString(2,d.lastName);
            ps.setNString(3,d.email.isBlank()?null:d.email); ps.setString(4,d.phone);
            try(ResultSet rs=ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }
}
