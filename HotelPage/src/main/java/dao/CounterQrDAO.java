package dao;

import com.fasterxml.jackson.databind.JsonNode;
import db.DBContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import util.OnlinePaymentConfig;
import util.OnlinePaymentGateway;

/** QR tại quầy: booking được khóa trước checkout và payment. */
public class CounterQrDAO extends DBContext {
    public static boolean owns(long code) {
        return code >= 900_000_000_000_000L && code < 1_000_000_000_000_000L;
    }

    @FunctionalInterface
    private interface Work<T> { T run(Connection c) throws SQLException; }

    private <T> T tx(Work<T> work) throws SQLException {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                T value = work.run(c);
                c.commit();
                return value;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalArgumentException(message);
    }

    private static PreparedStatement sql(Connection c, String query, Object... args)
            throws SQLException {
        PreparedStatement ps = c.prepareStatement(query);
        for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
        return ps;
    }

    private static int update(Connection c, String query, Object... args) throws SQLException {
        try (PreparedStatement ps = sql(c, query, args)) { return ps.executeUpdate(); }
    }

    private static Map<String,Object> row(Connection c, String query, Object... args)
            throws SQLException {
        try (PreparedStatement ps = sql(c, query, args); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            Map<String,Object> result = new LinkedHashMap<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                result.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
            }
            return result;
        }
    }

    private static BigDecimal paid(Connection c, int booking) throws SQLException {
        Map<String,Object> value = row(c,
                "SELECT COALESCE(SUM(Amount-COALESCE(RefundedAmount,0)),0) Amount "
                + "FROM Payment WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=? AND PaymentStatus='paid'", booking);
        return (BigDecimal) value.get("Amount");
    }

    public Map<String,Object> active(int booking) throws SQLException {
        try (Connection c = getConnection()) {
            return row(c, "SELECT q.*,p.Amount FROM CounterQrCheckout q "
                    + "JOIN Payment p ON p.PaymentID=q.PaymentID WHERE q.BookingID=? "
                    + "AND (q.Active=1 OR q.State='review') ORDER BY q.CreatedAt DESC", booking);
        }
    }

    public Map<String,Object> find(long code) throws SQLException {
        try (Connection c = getConnection()) {
            return row(c, "SELECT q.*,p.Amount,b.CreatedByUserID FROM CounterQrCheckout q "
                    + "JOIN Payment p ON p.PaymentID=q.PaymentID "
                    + "JOIN Booking b ON b.BookingID=q.BookingID WHERE q.OrderCode=?", code);
        }
    }

    private static boolean canCollect(String status, boolean offline) {
        return "confirmed".equals(status) || "checked_in".equals(status)
                || (offline && "pending".equals(status));
    }

    public long start(int booking, int staff, String requestKey, int percent,
            BigDecimal quotedPaid, BigDecimal quotedTotal, BigDecimal quotedPayable) throws SQLException {
        require(OnlinePaymentConfig.payosConfigured() && OnlinePaymentConfig.callbacksConfigured(),
                "Chưa cấu hình payOS hoặc địa chỉ HTTPS công khai. Không tạo được QR.");
        String key = UUID.fromString(requestKey).toString();
        return tx(c -> {
            require(row(c, "SELECT UserID FROM [User] WITH(HOLDLOCK) WHERE UserID=? "
                    + "AND Role IN ('staff','admin') AND AccountStatus='active'", staff) != null,
                    "Tài khoản không có quyền thu tại quầy.");
            Map<String,Object> b = row(c, "SELECT Status,TotalPrice,CreatedByUserID,OfflineDepositPercent,"
                    + "OfflinePromotionCode,DATEDIFF_BIG(SECOND,'19700101',"
                    + "HoldExpiresAt) HoldEpoch FROM Booking WITH(UPDLOCK,HOLDLOCK) "
                    + "WHERE BookingID=? AND (CreatedByUserID IS NOT NULL OR OnlineDepositPolicy=1)", booking);
            require(b != null, "Không tìm thấy booking tại quầy.");
            Map<String,Object> existing = row(c,
                    "SELECT * FROM CounterQrCheckout WHERE RequestKey=?", key);
            if (existing != null) {
                require(((Number) existing.get("BookingID")).intValue() == booking
                        && ((Number) existing.get("StaffID")).intValue() == staff,
                        "Yêu cầu đã được dùng cho thao tác khác.");
                return ((Number) existing.get("OrderCode")).longValue();
            }
            existing = row(c, "SELECT * FROM CounterQrCheckout WHERE BookingID=? AND Active=1", booking);
            if (existing != null) return ((Number) existing.get("OrderCode")).longValue();
            require(row(c, "SELECT TOP(1) OrderCode FROM CounterQrCheckout WHERE BookingID=? AND State='review'", booking) == null,
                    "Booking có khoản chuyển cần đối soát. Không tạo QR thu thêm.");

            String status = (String) b.get("Status");
            boolean offline = b.get("CreatedByUserID") != null;
            Integer policyPercent = (Integer) b.get("OfflineDepositPercent");
            require(!offline || (policyPercent != null && policyPercent >= 1 && policyPercent <= 100),
                    "Chính sách tiền cọc chưa hợp lệ.");
            require(canCollect(status, offline),
                    "Trạng thái booking không cho phép thu tiền.");
            require(offline || percent == 100,
                    "Chỉ tạo QR phần còn lại cho đơn online đã xác nhận.");
            long now = Instant.now().getEpochSecond();
            long expires = now + 600;
            if ("pending".equals(status)) {
                require(b.get("HoldEpoch") != null, "Booking thiếu hạn giữ phòng.");
                expires = Math.min(expires, ((Number) b.get("HoldEpoch")).longValue());
            }
            require(expires > now + 60, "Thời gian giữ phòng còn quá ít. Không tạo QR mới.");
            BigDecimal total = (BigDecimal) b.get("TotalPrice");
            BigDecimal received = paid(c, booking);
            String promotion = (String) b.get("OfflinePromotionCode");
            boolean first = received.signum() == 0 && "pending".equals(status);
            BigDecimal payable = first ? OfflineBookingDAO.discounted(c, promotion, total) : total;
            require(quotedPaid != null && quotedTotal != null && quotedPayable != null
                    && received.compareTo(quotedPaid) == 0 && total.compareTo(quotedTotal) == 0
                    && payable.compareTo(quotedPayable) == 0,
                    "Số tiền hoặc mã giảm đã thay đổi. Tải lại trang trước khi tạo QR.");
            require(percent == 100 || (offline && received.signum() == 0
                    && percent == policyPercent),
                    "Tỷ lệ thu không đúng chính sách booking.");
            BigDecimal amount = amountDue(payable, received, percent);
            require(row(c, "SELECT TOP(1) PaymentID FROM Payment WITH(UPDLOCK,HOLDLOCK) "
                    + "WHERE BookingID=? AND PaymentStatus='pending'", booking) == null,
                    "Booking đang có giao dịch chờ. Cần đối soát trước.");
            Integer discount = null;
            if (first && promotion != null && !promotion.isBlank()) {
                Map<String,Object> d = row(c, "SELECT DiscountID FROM Discount WITH(UPDLOCK,HOLDLOCK) "
                        + "WHERE Code=? AND Quantity>0", promotion);
                require(d != null, "Mã giảm đã hết lượt.");
                discount = ((Number) d.get("DiscountID")).intValue();
                require(update(c, "UPDATE Discount SET Quantity=Quantity-1 WHERE DiscountID=? AND Quantity>0",
                        discount) == 1, "Mã giảm đã hết lượt.");
            }
            long code = ThreadLocalRandom.current().nextLong(900_000_000_000_000L, 1_000_000_000_000_000L);
            Map<String,Object> inserted = row(c,
                    "SET NOCOUNT ON; DECLARE @ids TABLE(ID INT); "
                    + "INSERT Payment(BookingID,Amount,PaymentMethod,PaymentStatus,TransactionCode,"
                    + "DiscountID,DiscountAmount,CollectedByUserID,CounterRequestKey) "
                    + "OUTPUT INSERTED.PaymentID INTO @ids VALUES(?,?,'bank_transfer','pending',?,?,?,?,?); "
                    + "SELECT ID FROM @ids", booking, amount, "CQR-" + code, discount,
                    total.subtract(payable), staff, key);
            require(inserted != null, "Không tạo được giao dịch.");
            update(c, "INSERT CounterQrCheckout(OrderCode,BookingID,PaymentID,StaffID,RequestKey,"
                    + "OriginalTotal,Payable,PreviouslyPaid,ExpiresEpoch) VALUES(?,?,?,?,?,?,?,?,?)",
                    code, booking, inserted.get("ID"), staff, key, total, payable, received, expires);
            return code;
        });
    }

    static BigDecimal amountDue(BigDecimal payable, BigDecimal received, int percent) {
        require(percent > 0 && percent <= 100, "Tỷ lệ thu không hợp lệ.");
        BigDecimal remaining = payable.subtract(received);
        BigDecimal amount = percent == 100 ? remaining
                : payable.multiply(BigDecimal.valueOf(percent))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.CEILING);
        require(amount.signum() > 0 && amount.compareTo(remaining) <= 0,
                "Không còn khoản tiền phù hợp để tạo QR. Khoản 0 đồng dùng xác nhận tại quầy.");
        require(amount.stripTrailingZeros().scale() <= 0, "Số tiền phải là số nguyên VND.");
        amount.longValueExact();
        return amount;
    }

    /** API timeout không có nghĩa tạo link thất bại: giữ nguyên mã để đối soát. */
    public void ensureLink(long code) throws Exception {
        Map<String,Object> q = find(code);
        require(q != null, "Không tìm thấy QR.");
        if (!Boolean.TRUE.equals(q.get("Active")) || q.get("CheckoutUrl") != null) return;
        require(((Number) q.get("ExpiresEpoch")).longValue() > Instant.now().getEpochSecond(),
                "QR đã hết thời gian tạo. Cần đối soát giao dịch trước.");
        // Chỉ một request được gọi tạo link. Request lặp chỉ tra cùng orderCode.
        try (Connection c = getConnection()) {
            if (update(c, "UPDATE CounterQrCheckout SET LinkAttempted=1 WHERE OrderCode=? "
                    + "AND Active=1 AND LinkAttempted=0", code) != 1) {
                reconcile(code);
                return;
            }
        }
        String returnUrl = OnlinePaymentConfig.publicBaseUrl() + "/counter-payment?bookingId="
                + q.get("BookingID") + "&view=qr&orderCode=" + code;
        JsonNode result;
        try {
            result = OnlinePaymentGateway.createEmbeddedPayOSLink(
                    ((Number) q.get("PaymentID")).longValue(), code,
                    ((BigDecimal) q.get("Amount")).longValueExact(), returnUrl,
                    ((Number) q.get("ExpiresEpoch")).longValue());
        } catch (OnlinePaymentGateway.PayOSLinkRejectedException ex) {
            // Chỉ request tạo đầu tiên đi vào đây; timeout không đi vào nhánh này.
            closeRejected(code);
            return;
        }
        String url = result.path("checkoutUrl").asText();
        require(validUrl(url), "Đường dẫn thanh toán không hợp lệ.");
        require(result.path("orderCode").asLong(-1) == code
                && result.path("amount").asLong(-1) == ((BigDecimal) q.get("Amount")).longValueExact(),
                "Thông tin QR trả về không khớp giao dịch.");
        try (Connection c = getConnection()) {
            update(c, "UPDATE CounterQrCheckout SET CheckoutUrl=? WHERE OrderCode=? AND Active=1", url, code);
        }
    }

    private void closeRejected(long code) throws SQLException {
        Map<String,Object> initial = find(code);
        int booking = ((Number) initial.get("BookingID")).intValue();
        tx(c -> {
            row(c, "SELECT BookingID FROM Booking WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=?", booking);
            Map<String,Object> q = row(c, "SELECT * FROM CounterQrCheckout WITH(UPDLOCK,HOLDLOCK) WHERE OrderCode=?", code);
            if (!Boolean.TRUE.equals(q.get("Active"))) return null;
            int payment = ((Number) q.get("PaymentID")).intValue();
            Map<String,Object> p = row(c, "SELECT DiscountID FROM Payment WITH(UPDLOCK,HOLDLOCK) WHERE PaymentID=?", payment);
            require(update(c, "UPDATE Payment SET PaymentStatus='failed' WHERE PaymentID=? AND PaymentStatus='pending'", payment) == 1,
                    "Giao dịch đã đổi trạng thái; cần đối soát.");
            update(c, "UPDATE CounterQrCheckout SET Active=0,State='rejected',CheckedAt=SYSUTCDATETIME() WHERE OrderCode=?", code);
            if (p.get("DiscountID") != null) update(c, "UPDATE Discount SET Quantity=Quantity+1 WHERE DiscountID=?", p.get("DiscountID"));
            return null;
        });
    }

    public static boolean validUrl(String url) {
        if (url == null) return false;
        try {
            java.net.URI uri = java.net.URI.create(url);
            return "https".equals(uri.getScheme()) && uri.getRawUserInfo() == null
                    && (uri.getPort() == -1 || uri.getPort() == 443)
                    && Arrays.asList("pay.payos.vn", "next.pay.payos.vn").contains(uri.getHost());
        } catch (IllegalArgumentException ex) { return false; }
    }

    public static String providerState(JsonNode data, long amount) {
        long received = data.path("amountPaid").asLong(-1);
        long remaining = data.path("amountRemaining").asLong(-1);
        String status = data.path("status").asText();
        if ("PAID".equals(status) && received == amount && remaining == 0) return "paid";
        if (("CANCELLED".equals(status) || "EXPIRED".equals(status)) && received == 0) return "closed";
        if (received > 0 || "PAID".equals(status)) return "review";
        return "pending";
    }

    /** Chỉ dữ liệu API server-to-server mới được quyết định ghi nhận tiền. */
    public void reconcile(long code) throws Exception {
        Map<String,Object> initial = find(code);
        require(initial != null, "Không tìm thấy giao dịch QR.");
        if (Arrays.asList("paid","rejected").contains(initial.get("State"))) return;
        try (Connection c = getConnection()) {
            update(c, "UPDATE CounterQrCheckout SET CheckedAt=SYSUTCDATETIME() WHERE OrderCode=?", code);
        }
        long amount = ((BigDecimal) initial.get("Amount")).longValueExact();
        JsonNode provider = OnlinePaymentGateway.getPayOSOrder(code, amount);
        String state = providerState(provider, amount);
        int booking = ((Number) initial.get("BookingID")).intValue();
        tx(c -> {
            Map<String,Object> b = row(c, "SELECT * FROM Booking WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=?", booking);
            Map<String,Object> q = row(c, "SELECT * FROM CounterQrCheckout WITH(UPDLOCK,HOLDLOCK) WHERE OrderCode=?", code);
            if (!Boolean.TRUE.equals(q.get("Active"))) {
                if (provider.path("amountPaid").asLong(-1)>0 && !"paid".equals(q.get("State"))) {
                    update(c, "UPDATE CounterQrCheckout SET State='review',ActualReceived=?,CheckedAt=SYSUTCDATETIME() WHERE OrderCode=?",
                            provider.path("amountPaid").asLong(),code);
                }
                return null;
            }
            update(c, "UPDATE CounterQrCheckout SET ActualReceived=? WHERE OrderCode=?",
                    provider.path("amountPaid").asLong(-1),code);
            int payment = ((Number) q.get("PaymentID")).intValue();
            if ("paid".equals(state)) {
                BigDecimal received = paid(c, booking);
                boolean offline = b != null && b.get("CreatedByUserID") != null;
                boolean matches = b != null
                        && (offline || Boolean.TRUE.equals(b.get("OnlineDepositPolicy")))
                        && canCollect((String) b.get("Status"), offline)
                        && ((BigDecimal) b.get("TotalPrice")).compareTo((BigDecimal) q.get("OriginalTotal")) == 0
                        && received.compareTo((BigDecimal) q.get("PreviouslyPaid")) == 0;
                if (!matches) {
                    update(c, "UPDATE CounterQrCheckout SET State='review',CheckedAt=SYSUTCDATETIME() WHERE OrderCode=?", code);
                    return null;
                }
                // Cùng transaction: đóng chốt QR rồi mới cập nhật tiền và booking.
                update(c, "UPDATE CounterQrCheckout SET Active=0,State='paid',CheckedAt=SYSUTCDATETIME() WHERE OrderCode=?", code);
                require(update(c, "UPDATE Payment SET PaymentStatus='paid',PaymentDate=SYSDATETIME(),"
                        + "CounterReference=? WHERE PaymentID=? AND PaymentStatus='pending'",
                        "payOS:" + code, payment) == 1, "Trạng thái giao dịch không còn phù hợp.");
                update(c, "UPDATE Booking SET TotalPrice=?,Status=CASE WHEN Status='pending' THEN 'confirmed' ELSE Status END "
                        + "WHERE BookingID=?", q.get("Payable"), booking);
                update(c, "INSERT OfflineBookingEvent(BookingID,ActorUserID,RequestKey,Action,Note,Amount) "
                        + "VALUES(?,?,?,'payment',?,?)", booking, q.get("StaffID"), q.get("RequestKey"),
                        "QR payOS / " + code, BigDecimal.valueOf(amount));
            } else if ("closed".equals(state)) {
                update(c, "UPDATE CounterQrCheckout SET Active=0,State='closed',CheckedAt=SYSUTCDATETIME() WHERE OrderCode=?", code);
                Map<String,Object> p = row(c, "SELECT DiscountID FROM Payment WITH(UPDLOCK,HOLDLOCK) WHERE PaymentID=?", payment);
                require(update(c, "UPDATE Payment SET PaymentStatus='failed' WHERE PaymentID=? AND PaymentStatus='pending'", payment) == 1,
                        "Trạng thái giao dịch không còn phù hợp.");
                if (p.get("DiscountID") != null) update(c, "UPDATE Discount SET Quantity=Quantity+1 WHERE DiscountID=?", p.get("DiscountID"));
            } else {
                update(c, "UPDATE CounterQrCheckout SET State=?,CheckedAt=SYSUTCDATETIME() WHERE OrderCode=?", state, code);
                String linkId = provider.path("id").asText();
                if (linkId.matches("[a-fA-F0-9]{32}")) {
                    update(c, "UPDATE CounterQrCheckout SET CheckoutUrl=COALESCE(CheckoutUrl,?) WHERE OrderCode=?",
                            "https://pay.payos.vn/web/" + linkId, code);
                }
            }
            return null;
        });
    }

    public List<Long> pendingCodes() throws SQLException {
        List<Long> result = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT TOP(30) OrderCode FROM CounterQrCheckout WHERE Active=1 "
                + "OR (State IN ('closed','review') AND CreatedAt>DATEADD(DAY,-1,SYSUTCDATETIME())) "
                + "ORDER BY COALESCE(CheckedAt,CreatedAt),OrderCode"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(rs.getLong(1));
        }
        return result;
    }
}
