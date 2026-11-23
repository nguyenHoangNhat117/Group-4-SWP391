package dao;

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

    public static final class RoomLine implements Serializable {

        private static final long serialVersionUID = 1L;
        private final int typeId;
        private final String name;
        private final int quantity;
        private final BigDecimal nightly;
        private final int guests;

        private RoomLine(int typeId, String name, int quantity, BigDecimal nightly, int guests) {
            this.typeId = typeId;
            this.name = name;
            this.quantity = quantity;
            this.nightly = nightly;
            this.guests = guests;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getNightly() {
            return nightly;
        }
    }

    public static final class Quote implements Serializable {

        private static final long serialVersionUID = 1L;
        public final OfflineBookingDraft draft;
        private final List<RoomLine> lines;
        private final BigDecimal gross;
        private final BigDecimal payable;
        private final String customerName;

        private Quote(OfflineBookingDraft draft, List<RoomLine> lines, BigDecimal gross,
                BigDecimal payable, String customerName) {
            this.draft = draft;
            this.lines = List.copyOf(lines);
            this.gross = gross;
            this.payable = payable;
            this.customerName = customerName;
        }

        public OfflineBookingDraft getDraft() {
            return draft;
        }

        public List<RoomLine> getLines() {
            return lines;
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
            return ChronoUnit.DAYS.between(draft.checkIn, draft.checkOut);
        }
    }

    public static final class RoomOption {

        private final RoomDAO.AvailableType stock;
        private final int maximum;

        private RoomOption(RoomDAO.AvailableType stock) {
            this.stock = stock;
            this.maximum = stock.getReadyNow() == null ? stock.getAvailable()
                    : Math.min(stock.getAvailable(), stock.getReadyNow());
        }

        public RoomDAO.AvailableType getStock() {
            return stock;
        }

        public int getMaximum() {
            return maximum;
        }
    }

    public List<RoomOption> availableTypes(LocalDate from, LocalDate to, int guests) throws SQLException {
        LocalDate today = ReportPeriod.today();
        OfflineBookingDraft.validateStay(from, to, guests, today);
        expireHolds();
        new BookingDAO().expirePendingBookings();
        List<RoomDAO.AvailableType> types = new RoomDAO().findAvailableTypes(from, to,
                1, null, RoomPriceBand.ALL, "room_asc");
        List<RoomOption> options = new ArrayList<>();
        if (!from.equals(today) || types.isEmpty()) {
            types.forEach(type -> options.add(new RoomOption(type)));
            return options;
        }
        try (Connection connection = getConnection()) {
            for (RoomDAO.AvailableType type : types) {
                int ready = RoomDAO.countReadyRooms(connection, type.getType().getId(), from, to);
                options.add(new RoomOption(type.withReadyNow(ready)));
            }
        }
        return options;
    }

    public static long feasibleCapacity(List<RoomOption> rooms, int adults) {
        List<RoomOption> ranked = new ArrayList<>(rooms);
        ranked.sort(Comparator.comparingInt((RoomOption option) -> option.stock.getType().getCapacity()).reversed());
        long capacity = 0;
        int remaining = adults;
        for (RoomOption option : ranked) {
            int count = Math.min(option.maximum, remaining);
            capacity += (long) count * option.stock.getType().getCapacity();
            remaining -= count;
        }
        return capacity;
    }

    public List<Map<String, Object>> lookupBookings(String query) throws SQLException {
        String text = AuthSupport.text(query);
        if (text.isEmpty() || text.length() > 120) {
            throw new IllegalArgumentException("Nhập mã đơn, số điện thoại hoặc tên khách (tối đa 120 ký tự).");
        }
        Integer id = null;
        if (text.matches("#?[0-9]{1,10}")) {
            try {
                id = Integer.valueOf(text.replace("#", ""));
            } catch (NumberFormatException ignored) {
                // Chuỗi số vẫn có thể là số điện thoại.
            }
        }
        String phone = null;
        if (text.matches("[+() .0-9-]+")) {
            String digits = text.replaceAll("[^0-9]", "");
            if (digits.length() >= 3) {
                phone = digits;
            }
        }

        String sql = "WITH Matches AS (SELECT TOP(30) b.BookingID,b.Status,b.TotalPrice,b.CreatedByUserID,"
                + "b.OfflinePromotionCode,b.BookingDate, "
                + "COALESCE(NULLIF(b.ContactName,N''),TRIM(CONCAT(c.LastName,N' ',c.FirstName))) AS CustomerName "
                + "FROM Booking b JOIN Customer c ON c.CustomerID=b.CustomerID "
                + "CROSS APPLY (SELECT TRANSLATE(CONCAT(c.Phone,N'|',b.ContactPhone),N'+()-. ',N'      ') AS Phone) contact "
                + "WHERE b.BookingID=? OR CHARINDEX(?,REPLACE(contact.Phone,N' ',N''))>0 "
                + "OR (LEN(?)>=2 AND (CHARINDEX(?,b.ContactName)>0 "
                + "OR CHARINDEX(?,TRIM(CONCAT(c.LastName,N' ',c.FirstName)))>0 "
                + "OR CHARINDEX(?,TRIM(CONCAT(c.FirstName,N' ',c.LastName)))>0)) "
                + "ORDER BY CASE WHEN b.BookingID=? THEN 0 ELSE 1 END,b.BookingDate DESC,b.BookingID DESC) "
                + "SELECT m.*,stay.RoomTypes,stay.CheckIn,stay.CheckOut,paid.Amount AS Paid,q.Payable AS QrPayable "
                + "FROM Matches m OUTER APPLY (SELECT MIN(x.CheckIn) AS CheckIn,MAX(x.CheckOut) AS CheckOut, "
                + "STRING_AGG(CONCAT(x.TypeName,N' × ',x.Quantity),N', ') WITHIN GROUP (ORDER BY x.TypeName) AS RoomTypes "
                + "FROM (SELECT COALESCE(t.Name,N'Chưa rõ loại phòng') AS TypeName,COUNT(*) AS Quantity, "
                + "MIN(d.CheckInDate) AS CheckIn,MAX(d.CheckOutDate) AS CheckOut "
                + "FROM BookingDetail d LEFT JOIN Room r ON r.RoomNumber=d.RoomNumber "
                + "LEFT JOIN RoomType t ON t.RoomTypeID=COALESCE(d.RoomTypeID,r.RoomTypeID) "
                + "WHERE d.BookingID=m.BookingID GROUP BY t.RoomTypeID,t.Name) x) stay "
                + "OUTER APPLY (SELECT COALESCE(SUM(Amount-COALESCE(RefundedAmount,0)),0) AS Amount "
                + "FROM Payment WHERE BookingID=m.BookingID AND PaymentStatus='paid') paid "
                + "OUTER APPLY (SELECT TOP(1) Payable FROM CounterQrCheckout WHERE BookingID=m.BookingID "
                + "AND (Active=1 OR State='review') ORDER BY CreatedAt DESC) q "
                + "ORDER BY CASE WHEN m.BookingID=? THEN 0 ELSE 1 END,m.BookingDate DESC,m.BookingID DESC";

        expireHolds();
        List<Map<String, Object>> results = new ArrayList<>();
        java.time.format.DateTimeFormatter dateFormat = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id, Types.INTEGER);
            ps.setNString(2, phone);
            for (int i = 3; i <= 6; i++) {
                ps.setNString(i, text);
            }
            ps.setObject(7, id, Types.INTEGER);
            ps.setObject(8, id, Types.INTEGER);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    String status = rows.getString("Status");
                    BigDecimal paid = rows.getBigDecimal("Paid");
                    row.put("id", rows.getInt("BookingID"));
                    row.put("name", rows.getString("CustomerName"));
                    row.put("status", status);
                    row.put("roomTypes", rows.getString("RoomTypes"));
                    LocalDate checkIn = rows.getObject("CheckIn", LocalDate.class);
                    LocalDate checkOut = rows.getObject("CheckOut", LocalDate.class);
                    row.put("dates", checkIn == null || checkOut == null ? "—"
                            : checkIn.format(dateFormat) + " → " + checkOut.format(dateFormat));
                    row.put("paid", paid);
                    row.put("total", rows.getBigDecimal("TotalPrice"));
                    if ("pending".equals(status) && rows.getObject("CreatedByUserID") != null) {
                        row.put("qrPayable", rows.getBigDecimal("QrPayable"));
                        if (paid.signum() == 0) {
                            row.put("promotion", rows.getString("OfflinePromotionCode"));
                        }
                    }
                    results.add(row);
                }
            }
            for (Map<String, Object> row : results) {
                BigDecimal payable = (BigDecimal) row.get("total");
                if (row.get("qrPayable") instanceof BigDecimal) {
                    payable = (BigDecimal) row.get("qrPayable");
                } else if (row.get("promotion") instanceof String) {
                    try {
                        payable = discounted(connection, (String) row.get("promotion"), payable);
                    } catch (IllegalArgumentException ignored) {
                        // Như màn hình quầy, hiện tổng gốc cho đến khi sửa được mã ưu đãi.
                    }
                }
                row.put("remaining", "cancelled".equals(row.get("status")) ? BigDecimal.ZERO
                        : payable.subtract((BigDecimal) row.get("paid")).max(BigDecimal.ZERO));
            }
        }
        return results;
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
                + "AND (" + util.OfflineCustomerContact.phoneSql("c.Phone") + " LIKE ? ESCAPE '~' "
                + "OR c.Email LIKE ? ESCAPE '~' "
                + "OR u.Username LIKE ? ESCAPE '~') "
                + "ORDER BY c.CustomerID DESC";

        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            String phone = util.OfflineCustomerContact.phone(query);
            String phoneKey = phone.matches("[+]?[0-9]{1,15}") ? "%" + phone + "%" : key;
            ps.setNString(1, phoneKey);
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

                Quote quote = quote(c, draft, customerName(c, draft));

                c.commit();
                return quote;

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public Integer findCreatedRequest(int actor, String requestKey) throws SQLException {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                requireActor(c, actor);
                Integer id = null;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT BookingID,CreatedByUserID FROM Booking WITH(UPDLOCK,HOLDLOCK) WHERE OfflineRequestKey=?")) {
                    ps.setString(1, requestKey);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (rs.getInt(2) != actor) {
                                throw new IllegalArgumentException("Yêu cầu thuộc nhân viên khác.");
                            }
                            id = rs.getInt(1);
                        }
                    }
                }
                c.commit();
                return id;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
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
                Quote current = quote(c, d, customerName(c, d));
                if (!samePrices(accepted.lines, current.lines)
                        || current.payable.compareTo(accepted.payable) != 0) {
                    throw new IllegalArgumentException("Giá hoặc khuyến mãi đã thay đổi. Vui lòng xem lại tóm tắt.");
                }

                int customer = d.customerId == null ? createCustomer(c, d) : d.customerId;
                int id = insertReservation(c, customer, actor, current);

                try (PreparedStatement audit = c.prepareStatement("INSERT OfflineBookingEvent(BookingID,ActorUserID,RequestKey,Action,Note,Amount) VALUES(?,?,?,'create',N'Tạo đơn giữ phòng tại quầy',?)")) {
                    audit.setInt(1, id);
                    audit.setInt(2, actor);
                    audit.setString(3, d.requestKey);
                    audit.setBigDecimal(4, current.gross);
                    audit.executeUpdate();
                }
                c.commit();
                return id;

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Quote quote(Connection connection, OfflineBookingDraft draft, String customer) throws SQLException {
        List<RoomLine> lines = new ArrayList<>();
        int remaining = draft.guests - draft.quantity;
        long nights = ChronoUnit.DAYS.between(draft.checkIn, draft.checkOut);
        BigDecimal gross = BigDecimal.ZERO;
        // draft.rooms được xếp theo mã loại để các đơn khóa kho theo cùng thứ tự.
        try (PreparedStatement type = connection.prepareStatement(
                "SELECT Name,Capacity FROM RoomType WHERE RoomTypeID=?")) {
            for (Map.Entry<Integer, Integer> entry : draft.rooms.entrySet()) {
                int id = entry.getKey();
                int count = entry.getValue();
                BigDecimal nightly = BookingDAO.lockAvailableRoomType(
                        connection, id, draft.checkIn, draft.checkOut, count, count, true);
                type.setInt(1, id);
                try (ResultSet rows = type.executeQuery()) {
                    if (!rows.next()) {
                        throw new SQLException("Không tìm được loại phòng đã khóa.");
                    }
                    // Dành một chỗ mỗi phòng trước, rồi phân thêm khách theo sức chứa.
                    int assigned = (int) Math.min(remaining, (long) count * (rows.getInt(2) - 1));
                    lines.add(new RoomLine(id, rows.getString(1), count, nightly, count + assigned));
                    remaining -= assigned;
                }
                gross = gross.add(nightly.multiply(BigDecimal.valueOf(nights * count)));
            }
        }
        if (remaining > 0) {
            throw new IllegalArgumentException("Các phòng đã chọn chưa đủ sức chứa cho cả đoàn.");
        }
        return new Quote(draft, lines, gross, discounted(connection, draft.promotionCode, gross), customer);
    }

    private static boolean samePrices(List<RoomLine> accepted, List<RoomLine> current) {
        if (accepted == null || accepted.size() != current.size()) {
            return false;
        }
        for (int i = 0; i < current.size(); i++) {
            RoomLine before = accepted.get(i);
            RoomLine after = current.get(i);
            if (before.typeId != after.typeId || before.quantity != after.quantity
                    || before.nightly.compareTo(after.nightly) != 0) {
                return false;
            }
        }
        return true;
    }

    private int insertReservation(Connection connection, int customer, int actor, Quote quote) throws SQLException {
        OfflineBookingDraft draft = quote.draft;
        String sql = "SET NOCOUNT ON; DECLARE @ids TABLE(ID INT); "
                + "INSERT Booking(CustomerID,TotalPrice,Status,SpecialRequest,CreatedByUserID,OfflineRequestKey,"
                + "OfflinePromotionCode,HoldExpiresAt,OfflineDepositPercent) OUTPUT INSERTED.BookingID INTO @ids "
                + "VALUES(?,?,'pending',?,?,?,?,DATEADD(MINUTE,?,SYSUTCDATETIME()),?); SELECT ID FROM @ids;";
        int id;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customer);
            ps.setBigDecimal(2, quote.gross);
            ps.setNString(3, draft.specialRequest);
            ps.setInt(4, actor);
            ps.setString(5, draft.requestKey);
            ps.setNString(6, draft.promotionCode.isEmpty() ? null : draft.promotionCode);
            ps.setInt(7, util.OfflineBookingPolicy.HOLD_MINUTES);
            ps.setInt(8, util.OfflineBookingPolicy.DEPOSIT_PERCENT);
            try (ResultSet rows = ps.executeQuery()) {
                if (!rows.next()) {
                    throw new SQLException("Không lấy được mã đơn đặt phòng.");
                }
                id = rows.getInt(1);
            }
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT BookingDetail(BookingID,RoomTypeID,RoomNumber,CheckInDate,CheckOutDate,"
                + "PricePerNight,GuestCount,SpecialRequest) VALUES(?,?,NULL,?,?,?,?,?)")) {
            ps.setInt(1, id);
            ps.setObject(3, draft.checkIn);
            ps.setObject(4, draft.checkOut);
            ps.setNString(7, draft.specialRequest);
            for (RoomLine line : quote.lines) {
                ps.setInt(2, line.typeId);
                ps.setBigDecimal(5, line.nightly);
                for (int i = 0; i < line.quantity; i++) {
                    ps.setInt(6, line.guests / line.quantity + (i < line.guests % line.quantity ? 1 : 0));
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
        return id;
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

    private static final String ELIGIBLE_PROMOTION
            = "IsActive=1 AND Quantity>0 AND SaleOff BETWEEN 0 AND 100 "
            + "AND (StartDate IS NULL OR StartDate<=?) "
            + "AND (EndDate IS NULL OR EndDate>=?) "
            + "AND (StartDate IS NULL OR EndDate IS NULL OR StartDate<=EndDate) "
            + "AND (MinimumAmount IS NULL OR MinimumAmount>=0) "
            + "AND (MaximumDiscount IS NULL OR MaximumDiscount>=0)";

    public List<Map<String, Object>> availablePromotions() throws SQLException {
        String sql = "SELECT Code, SaleOff, COALESCE(MinimumAmount, 0) AS MinimumAmount "
                + "FROM Discount WHERE " + ELIGIBLE_PROMOTION + " ORDER BY Code";
        List<Map<String, Object>> promotions = new ArrayList<>();
        LocalDate today = ReportPeriod.today();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, today);
            ps.setObject(2, today);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    promotions.add(Map.of("code", rs.getString("Code"),
                            "percent", rs.getBigDecimal("SaleOff"),
                            "minimum", rs.getBigDecimal("MinimumAmount")));
                }
            }
        }
        return promotions;
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
                + "WHERE Code=? AND " + ELIGIBLE_PROMOTION
                + " AND (MinimumAmount IS NULL OR MinimumAmount<=?)";

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
                        + "b.HoldExpiresAt,b.OfflineDepositPercent,b.CreatedByUserID,b.OnlineDepositPolicy,b.BookingDate "
                        + "FROM Booking b WITH(HOLDLOCK) "
                        + "LEFT JOIN [User] u "
                        + "ON u.UserID=b.CreatedByUserID "
                        + "JOIN Customer cu "
                        + "ON cu.CustomerID=b.CustomerID "
                        + "WHERE b.BookingID=?";

                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, bookingId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Không tìm thấy đơn đặt phòng."
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
                        data.put("offline", rs.getObject("CreatedByUserID") != null);
                        data.put("legacyOnline", rs.getObject("CreatedByUserID") == null
                                && !rs.getBoolean("OnlineDepositPolicy"));
                        data.put(
                                "customerName",
                                AuthSupport.text(rs.getString(6))
                                + " "
                                + AuthSupport.text(rs.getString(5))
                        );

                        java.time.LocalDateTime expiresAt = rs.getObject(7, java.time.LocalDateTime.class);
                        String deadline = expiresAt == null ? null
                                : expiresAt.atOffset(java.time.ZoneOffset.UTC)
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
                        data.put("bookingDate", rs.getObject("BookingDate", java.time.LocalDateTime.class));
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
                boolean offline = Boolean.TRUE.equals(data.get("offline"));

                if (offline && paid.signum() == 0 && pending) {
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
                data.put("firstPayment", offline && paid.signum() == 0);
                data.put("depositAmount", payable.multiply(BigDecimal.valueOf((Integer) data.get("depositPercent")))
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.CEILING));
                data.put(
                        "canChange",
                        offline && pending && paid.signum() == 0
                );

                boolean payableStatus = Arrays.asList(
                        "pending", "confirmed", "checked_in"
                ).contains(data.get("status"));

                data.put(
                        "canPay",
                        !Boolean.TRUE.equals(data.get("legacyOnline"))
                        && !data.containsKey("promotionError")
                        && payableStatus
                        && (offline || !pending)
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
        String query = "SELECT TOP(1) CustomerID FROM Customer WITH(UPDLOCK,HOLDLOCK) WHERE " + util.OfflineCustomerContact.phoneSql("Phone") + "=? OR (?<>'' AND Email=?)";
        try (PreparedStatement ps = c.prepareStatement(query)) {
            ps.setString(1, d.phone);
            ps.setString(2, d.email);
            ps.setString(3, d.email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Email hoặc điện thoại đã tồn tại. Tìm và chọn khách có sẵn.");
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement("SET NOCOUNT ON; DECLARE @ids TABLE(ID INT); INSERT Customer(UserID,FirstName,LastName,Email,Phone) OUTPUT INSERTED.CustomerID INTO @ids VALUES(NULL,?,?,?,?); SELECT ID FROM @ids;")) {
            ps.setNString(1, d.firstName);
            ps.setNString(2, d.lastName);
            ps.setNString(3, d.email.isBlank() ? null : d.email);
            ps.setString(4, d.phone);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
