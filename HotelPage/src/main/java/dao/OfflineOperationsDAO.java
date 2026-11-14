package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import util.ReportPeriod;

/** Counter operations lock the booking before payments, refunds and room records. */
public class OfflineOperationsDAO extends DBContext {
    @FunctionalInterface
    private interface Work<T> { T run(Connection c) throws SQLException; }

    private <T> T transaction(Work<T> work) throws SQLException {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new IllegalArgumentException(message);
    }

    private static void bind(PreparedStatement ps, Object... args) throws SQLException {
        for (int i=0; i<args.length; i++) ps.setObject(i+1, args[i]);
    }

    private static int execute(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps,args);
            return ps.executeUpdate();
        }
    }

    private static BigDecimal money(Connection c, String sql, int id) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,id);
            try (ResultSet rs=ps.executeQuery()) { rs.next(); return rs.getBigDecimal(1); }
        }
    }

    private static BigDecimal paid(Connection c, int id) throws SQLException {
        return money(c,"SELECT COALESCE(SUM(Amount-COALESCE(RefundedAmount,0)),0) FROM Payment WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=? AND PaymentStatus='paid'",id);
    }

    private static void actor(Connection c,int staff) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement("SELECT 1 FROM [User] WITH(HOLDLOCK) WHERE UserID=? AND Role IN ('staff','admin') AND AccountStatus='active'")) {
            ps.setInt(1,staff);
            try (ResultSet rs=ps.executeQuery()) { require(rs.next(),"Tài khoản không có quyền thao tác tại quầy."); }
        }
    }

    private static final class BookingRow {
        String status, promotion;
        BigDecimal total;
        int percent;
        boolean expired;
    }

    private static BookingRow booking(Connection c,int id) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement("SELECT Status,TotalPrice,OfflineDepositPercent,OfflinePromotionCode, CASE WHEN HoldExpiresAt IS NULL OR HoldExpiresAt<=SYSUTCDATETIME() THEN 1 ELSE 0 END Expired FROM Booking WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=? AND CreatedByUserID IS NOT NULL")) {
            ps.setInt(1,id);
            try (ResultSet rs=ps.executeQuery()) {
                require(rs.next(),"Không tìm thấy đặt phòng tại quầy.");
                BookingRow b=new BookingRow();
                b.status=rs.getString(1); b.total=rs.getBigDecimal(2); b.percent=rs.getInt(3);
                b.promotion=rs.getString(4); b.expired=rs.getBoolean(5);
                require(b.percent>=1 && b.percent<=100,"Chính sách tiền cọc chưa hợp lệ.");
                return b;
            }
        }
    }

    private static String requestKey(String key) {
        try { return UUID.fromString(key).toString(); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("Yêu cầu không hợp lệ. Tải lại trang."); }
    }

    private static boolean already(Connection c,int id,int staff,String key,String action) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement("SELECT BookingID,ActorUserID,Action FROM OfflineBookingEvent WITH(UPDLOCK,HOLDLOCK) WHERE RequestKey=?")) {
            ps.setString(1,key);
            try (ResultSet rs=ps.executeQuery()) {
                if(!rs.next()) return false;
                require(rs.getInt(1)==id && rs.getInt(2)==staff && action.equals(rs.getString(3)),"Mã yêu cầu đã được sử dụng.");
                return true;
            }
        }
    }

    private static void event(Connection c,int id,int staff,String key,String action,String note,BigDecimal amount) throws SQLException {
        execute(c,"INSERT OfflineBookingEvent(BookingID,ActorUserID,RequestKey,Action,Note,Amount) VALUES(?,?,?,?,?,?)",id,staff,key,action,note,amount);
    }

    private static String note(String input) {
        String s=input==null?"":input.trim();
        require(!s.isEmpty() && s.length()<=500 && s.chars().noneMatch(Character::isISOControl),"Nhập lý do/nội dung từ 1 đến 500 ký tự.");
        return s;
    }

    private static String reference(String method,String ref) {
        require("cash".equals(method)||"bank_transfer".equals(method),"Phương thức không hợp lệ.");
        String value=ref==null?"":ref.trim();
        require(value.length()<=100 && value.chars().noneMatch(Character::isISOControl),"Mã giao dịch không hợp lệ.");
        require(!"bank_transfer".equals(method)||!value.isEmpty(),"Nhập mã giao dịch ngân hàng đã đối chiếu.");
        return value.isEmpty()?null:value;
    }

    public void pay(int id,int staff,String method,int percent,String key,BigDecimal quotedPaid,
            BigDecimal quotedTotal,BigDecimal quotedPayable,String bankReference) throws SQLException {
        final String token=requestKey(key), ref=reference(method,bankReference);
        transaction(c -> {
            actor(c,staff); BookingRow b=booking(c,id);
            if(already(c,id,staff,token,"payment")) return null;
            require(Arrays.asList("pending","confirmed","checked_in").contains(b.status),"Trạng thái không cho phép thu tiền.");
            require(!"pending".equals(b.status)||!b.expired,"Đã hết hạn giữ phòng; không được thu tiền cho đơn này.");
            BigDecimal received=paid(c,id);
            BigDecimal payable=received.signum()==0 && "pending".equals(b.status)
                    ? OfflineBookingDAO.discounted(c,b.promotion,b.total):b.total;
            require(quotedPaid!=null && quotedTotal!=null && quotedPayable!=null
                    && received.compareTo(quotedPaid)==0 && b.total.compareTo(quotedTotal)==0
                    && payable.compareTo(quotedPayable)==0,"Số tiền hoặc khuyến mãi đã thay đổi. Tải lại màn thu tiền.");
            require(percent==100 || (received.signum()==0 && percent==b.percent),"Tỷ lệ thu tiền không đúng chính sách của đơn.");
            require(money(c,"SELECT COUNT_BIG(*) FROM Payment WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=? AND PaymentStatus='pending'",id).signum()==0,"Đang có giao dịch online chờ xác nhận. Hãy đối soát trước khi thu tại quầy.");
            BigDecimal amount=percent==100 ? payable.subtract(received)
                    : payable.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100),0,RoundingMode.CEILING);
            require((amount.signum()>0 || (amount.signum()==0 && payable.signum()==0 && "pending".equals(b.status)))
                    && amount.compareTo(payable.subtract(received))<=0,"Đơn đã thanh toán đủ hoặc số tiền không hợp lệ.");
            require(amount.stripTrailingZeros().scale()<=0,"Số tiền thu phải là số nguyên VND.");
            Integer discountId=null;
            if(received.signum()==0 && "pending".equals(b.status) && b.promotion!=null && !b.promotion.isBlank()) {
                try(PreparedStatement ps=c.prepareStatement("SELECT DiscountID FROM Discount WITH(UPDLOCK,HOLDLOCK) WHERE Code=? AND Quantity>0")) {
                    ps.setString(1,b.promotion);
                    try(ResultSet rs=ps.executeQuery()) { require(rs.next(),"Khuyến mãi đã hết lượt."); discountId=rs.getInt(1); }
                }
                require(execute(c,"UPDATE Discount SET Quantity=Quantity-1 WHERE DiscountID=? AND Quantity>0",discountId)==1,"Khuyến mãi đã hết lượt.");
            }
            execute(c,"INSERT Payment(BookingID,PaymentDate,Amount,PaymentMethod,PaymentStatus,TransactionCode,DiscountID,DiscountAmount,CollectedByUserID,CounterRequestKey,CounterReference) VALUES(?,SYSDATETIME(),?,?,'paid',?,?,?,?,?,?)",
                    id,amount,method,"COUNTER-"+token,discountId,b.total.subtract(payable),staff,token,ref);
            execute(c,"UPDATE Booking SET TotalPrice=?,Status=CASE WHEN Status='pending' THEN 'confirmed' ELSE Status END WHERE BookingID=?",payable,id);
            event(c,id,staff,token,"payment",method+(ref==null?"":" / "+ref),amount);
            return null;
        });
    }

    public void checkIn(int id,int staff,String key,boolean identityVerified,int actualGuests) throws SQLException {
        final String token=requestKey(key);
        transaction(c -> {
            actor(c,staff); BookingRow b=booking(c,id);
            if(already(c,id,staff,token,"check_in")) return null;
            require("confirmed".equals(b.status),"Chỉ nhận phòng cho đơn đã xác nhận.");
            require(identityVerified,"Cần đối chiếu giấy tờ của khách trước khi nhận phòng.");
            BigDecimal minimum=b.total.multiply(BigDecimal.valueOf(b.percent)).divide(BigDecimal.valueOf(100),0,RoundingMode.CEILING);
            require(paid(c,id).compareTo(minimum)>=0,"Chưa thu đủ tiền cọc.");
            List<Integer> rooms=new ArrayList<>(); int capacity=0;
            try(PreparedStatement ps=c.prepareStatement("SELECT bd.RoomNumber,bd.CheckInDate,bd.CheckOutDate,rt.Capacity,r.Status,r.NeedsCleaning FROM BookingDetail bd JOIN Room r WITH(UPDLOCK,HOLDLOCK) ON r.RoomNumber=bd.RoomNumber JOIN RoomType rt ON rt.RoomTypeID=r.RoomTypeID WHERE bd.BookingID=? ORDER BY bd.RoomNumber")) {
                ps.setInt(1,id);
                try(ResultSet rs=ps.executeQuery()) { while(rs.next()) {
                    require(rs.getObject(2,LocalDate.class).equals(ReportPeriod.today()),"Chỉ nhận phòng vào đúng ngày đã đặt. Cần xử lý lại lịch nếu khách đến sớm hoặc trễ.");
                    require("available".equals(rs.getString(5))&&!rs.getBoolean(6),"Phòng chưa sẵn sàng hoặc đang chờ dọn.");
                    capacity+=rs.getInt(4); rooms.add(rs.getInt(1));
                }}
            }
            require(!rooms.isEmpty() && actualGuests>0 && actualGuests<=capacity,"Số khách thực tế vượt sức chứa hoặc đơn không có phòng.");
            for(int room:rooms) {
                try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM BookingDetail d JOIN Booking b WITH(HOLDLOCK) ON b.BookingID=d.BookingID WHERE d.RoomNumber=? AND b.BookingID<>? AND b.Status='checked_in'")) {
                    bind(ps,room,id); try(ResultSet rs=ps.executeQuery()) { require(!rs.next(),"Phòng vẫn còn khách chưa trả."); }
                }
            }
            execute(c,"UPDATE Booking SET Status='checked_in',ActualCheckInAt=SYSUTCDATETIME(),IdentityVerifiedBy=?,ActualGuestCount=? WHERE BookingID=?",staff,actualGuests,id);
            event(c,id,staff,token,"check_in","Đã đối chiếu giấy tờ; số khách: "+actualGuests,BigDecimal.ZERO);
            return null;
        });
    }

    public void checkOut(int id,int staff,String key) throws SQLException {
        checkOut(id,staff,key,false);
    }

    public void checkOut(int id,int staff,String key,boolean earlyCheckoutAccepted) throws SQLException {
        final String token=requestKey(key);
        transaction(c -> {
            actor(c,staff); BookingRow b=booking(c,id);
            if(already(c,id,staff,token,"check_out")) return null;
            require("checked_in".equals(b.status),"Đơn chưa nhận phòng.");
            // Chặn cả giao dịch chờ và khoản cần đối soát, kể cả request từ tab cũ.
            require(money(c,"SELECT COUNT_BIG(*) FROM Payment WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=? AND PaymentStatus='pending'",id).signum()==0,
                    "Đang có giao dịch chờ. Đối soát xong trước khi trả phòng.");
            require(money(c,"SELECT COUNT_BIG(*) FROM CounterQrCheckout WITH(HOLDLOCK) WHERE BookingID=? AND (Active=1 OR State='review')",id).signum()==0,
                    "QR đang chờ hoặc cần đối soát. Chưa thể trả phòng.");
            require(paid(c,id).compareTo(b.total)>=0,"Còn công nợ. Thu đủ tiền trước khi trả phòng.");
            LocalDate today=ReportPeriod.today();
            boolean early=false;
            int roomCount=0;
            try(PreparedStatement ps=c.prepareStatement("SELECT d.CheckInDate,d.CheckOutDate FROM BookingDetail d "
                    + "JOIN Room r WITH(UPDLOCK,HOLDLOCK) ON r.RoomNumber=d.RoomNumber WHERE d.BookingID=? ORDER BY r.RoomNumber")) {
                ps.setInt(1,id);
                try(ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) {
                        LocalDate from=rs.getObject(1,LocalDate.class);
                        LocalDate to=rs.getObject(2,LocalDate.class);
                        require(from!=null && to!=null && to.isAfter(from),"Lịch lưu trú không hợp lệ. Kiểm tra booking.");
                        require(!today.isBefore(from),"Chưa đến ngày nhận phòng. Kiểm tra trạng thái booking.");
                        early|=today.isBefore(to);
                        roomCount++;
                    }
                }
            }
            require(roomCount>0,"Booking không có phòng để trả.");
            require(!early || earlyCheckoutAccepted,
                    "Khách trả sớm. Cần xác nhận đã thông báo chính sách giữ nguyên tiền phòng trước khi trả.");
            execute(c,"UPDATE r SET NeedsCleaning=1 FROM Room r JOIN BookingDetail d ON d.RoomNumber=r.RoomNumber WHERE d.BookingID=?",id);
            execute(c,"UPDATE Booking SET Status='checked_out',ActualCheckOutAt=SYSUTCDATETIME() WHERE BookingID=?",id);
            event(c,id,staff,token,"check_out",early
                    ? "Khách trả sớm ngày "+today+"; đã thông báo giữ nguyên tiền phòng. Phòng chờ dọn."
                    : "Đã quyết toán và trả phòng ngày "+today+"; phòng chờ dọn.",BigDecimal.ZERO);
            return null;
        });
    }

    public void complete(int id,int staff,String key) throws SQLException {
        final String token=requestKey(key);
        transaction(c -> {
            actor(c,staff); BookingRow b=booking(c,id);
            if(already(c,id,staff,token,"complete")) return null;
            require("checked_out".equals(b.status)&&paid(c,id).compareTo(b.total)>=0,"Chỉ hoàn tất sau khi trả phòng và thanh toán đủ.");
            execute(c,"UPDATE Booking SET Status='completed' WHERE BookingID=?",id);
            event(c,id,staff,token,"complete","Hoàn tất hồ sơ lưu trú.",BigDecimal.ZERO);
            return null;
        });
    }

    public void charge(int id,int staff,String key,String description,BigDecimal amount) throws SQLException {
        final String token=requestKey(key), detail=note(description);
        require(amount!=null && amount.signum()>0 && amount.compareTo(new BigDecimal("1000000000"))<=0 && amount.stripTrailingZeros().scale()<=0,"Khoản phát sinh phải từ 1 đến 1 tỷ VND, không có phần lẻ.");
        transaction(c -> {
            actor(c,staff); BookingRow b=booking(c,id);
            if(already(c,id,staff,token,"charge")) return null;
            require("confirmed".equals(b.status)||"checked_in".equals(b.status),"Chỉ thêm phát sinh trước khi trả phòng.");
            execute(c,"INSERT OfflineCharge(BookingID,Description,Amount,CreatedByUserID,RequestKey) VALUES(?,?,?,?,?)",id,detail,amount,staff,token);
            execute(c,"UPDATE Booking SET TotalPrice=TotalPrice+? WHERE BookingID=?",amount,id);
            event(c,id,staff,token,"charge",detail,amount);
            return null;
        });
    }

    public void cancel(int id,int staff,String key,String reason) throws SQLException {
        final String token=requestKey(key), detail=note(reason);
        require(detail.length()<=255,"Lý do hủy tối đa 255 ký tự.");
        transaction(c -> {
            actor(c,staff); BookingRow b=booking(c,id);
            if(already(c,id,staff,token,"cancel")) return null;
            require("pending".equals(b.status)||"confirmed".equals(b.status),"Không hủy đơn đã nhận/trả phòng.");
            BigDecimal received=paid(c,id), fee=BigDecimal.ZERO;
            LocalDate arrival;
            try(PreparedStatement ps=c.prepareStatement("SELECT MIN(CheckInDate) FROM BookingDetail WHERE BookingID=?")) {
                ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()) { rs.next(); arrival=rs.getObject(1,LocalDate.class); }
            }
            require(arrival!=null,"Thiếu thông tin ngày nhận phòng.");
            if(!ReportPeriod.today().isBefore(arrival)) {
                fee=b.total.multiply(BigDecimal.valueOf(b.percent)).divide(BigDecimal.valueOf(100),0,RoundingMode.CEILING).min(received);
            }
            BigDecimal refund=received.subtract(fee);
            require(money(c,"SELECT COUNT_BIG(*) FROM Payment WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=? AND PaymentStatus='pending'",id).signum()==0,"Có thanh toán online đang chờ; cần đối soát trước khi hủy.");
            execute(c,"UPDATE Booking SET Status='cancelled',CancellationDate=SYSDATETIME(),CancellationReason=? WHERE BookingID=?",detail,id);
            if(refund.signum()>0) execute(c,"INSERT OfflineRefund(BookingID,Amount,Fee) VALUES(?,?,?)",id,refund,fee);
            event(c,id,staff,token,"cancel",detail+"; phí giữ lại: "+fee.toPlainString(),refund);
            return null;
        });
    }

    public void refund(int id,int staff,String key,String method,String bankReference) throws SQLException {
        final String token=requestKey(key), ref=reference(method,bankReference);
        transaction(c -> {
            actor(c,staff); BookingRow b=booking(c,id);
            if(already(c,id,staff,token,"refund")) return null;
            require("cancelled".equals(b.status),"Chỉ hoàn tiền cho đơn đã hủy.");
            BigDecimal amount;
            try(PreparedStatement ps=c.prepareStatement("SELECT Amount FROM OfflineRefund WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=? AND Status='pending'")) {
                ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()) { require(rs.next(),"Không có khoản chờ hoàn hoặc đã hoàn tiền."); amount=rs.getBigDecimal(1); }
            }
            BigDecimal remaining=amount;
            try(PreparedStatement ps=c.prepareStatement("SELECT PaymentID,Amount,COALESCE(RefundedAmount,0) Refunded FROM Payment WITH(UPDLOCK,HOLDLOCK) WHERE BookingID=? AND PaymentStatus='paid' ORDER BY PaymentID")) {
                ps.setInt(1,id);
                try(ResultSet rs=ps.executeQuery()) { while(rs.next() && remaining.signum()>0) {
                    BigDecimal allocation=rs.getBigDecimal(2).subtract(rs.getBigDecimal(3)).min(remaining);
                    execute(c,"UPDATE Payment SET RefundedAmount=COALESCE(RefundedAmount,0)+?,PaymentStatus=CASE WHEN COALESCE(RefundedAmount,0)+?>=Amount THEN 'refunded' ELSE 'paid' END WHERE PaymentID=?",allocation,allocation,rs.getInt(1));
                    remaining=remaining.subtract(allocation);
                }}
            }
            require(remaining.signum()==0,"Số dư giao dịch không đủ để hoàn; cần đối soát.");
            execute(c,"UPDATE OfflineRefund SET Status='completed',Method=?,Reference=?,PaidByUserID=?,CompletedAt=SYSUTCDATETIME() WHERE BookingID=?",method,ref,staff,id);
            event(c,id,staff,token,"refund",method+(ref==null?"":" / "+ref),amount);
            return null;
        });
    }

    public void clean(int id,int staff,String key) throws SQLException {
        final String token=requestKey(key);
        transaction(c -> {
            actor(c,staff); BookingRow b=booking(c,id);
            if(already(c,id,staff,token,"clean")) return null;
            require("checked_out".equals(b.status)||"completed".equals(b.status),"Chỉ xác nhận dọn phòng sau khi khách trả phòng.");
            // Lock every affected room. A stale page cannot clear a room occupied by a later stay.
            try(PreparedStatement ps=c.prepareStatement("SELECT r.RoomNumber FROM Room r WITH(UPDLOCK,HOLDLOCK) JOIN BookingDetail d ON d.RoomNumber=r.RoomNumber WHERE d.BookingID=? ORDER BY r.RoomNumber")) {
                ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()) { while(rs.next()) {
                    int room=rs.getInt(1);
                    try(PreparedStatement latest=c.prepareStatement("SELECT 1 FROM BookingDetail d JOIN Booking b ON b.BookingID=d.BookingID WHERE d.RoomNumber=? AND b.ActualCheckOutAt>COALESCE((SELECT ActualCheckOutAt FROM Booking WHERE BookingID=?),CAST('1900-01-01' AS DATETIME2))")) {
                        bind(latest,room,id); try(ResultSet newer=latest.executeQuery()) { require(!newer.next(),"Đã có lượt trả phòng mới hơn; mở đúng đơn gần nhất để xác nhận dọn phòng."); }
                    }
                    try(PreparedStatement check=c.prepareStatement("SELECT 1 FROM BookingDetail d JOIN Booking b ON b.BookingID=d.BookingID WHERE d.RoomNumber=? AND b.Status='checked_in'")) {
                        check.setInt(1,room); try(ResultSet occupied=check.executeQuery()) { require(!occupied.next(),"Phòng đang có khách; không thể xác nhận dọn từ đơn cũ."); }
                    }
                    execute(c,"UPDATE Room SET NeedsCleaning=0 WHERE RoomNumber=?",room);
                    execute(c,"INSERT RoomServiceEvent(RoomNumber,ActorUserID) VALUES(?,?)",room,staff);
                }}
            }
            event(c,id,staff,token,"clean","Đã kiểm tra phòng sạch, sẵn sàng bàn giao.",BigDecimal.ZERO);
            return null;
        });
    }

    public Map<String,Object> operations(int id) throws SQLException {
        Map<String,Object> data=new LinkedHashMap<>();
        try(Connection c=getConnection()) {
            data.put("customerId",money(c,"SELECT CustomerID FROM Booking WHERE BookingID=?",id).intValueExact());
            data.put("events",rows(c,"SELECT TOP(100) e.Action,e.Note,e.Amount,e.CreatedAt,u.DisplayName Actor FROM OfflineBookingEvent e JOIN [User] u ON u.UserID=e.ActorUserID WHERE e.BookingID=? ORDER BY e.EventID DESC",id));
            data.put("payments",rows(c,"SELECT PaymentID,Amount,PaymentStatus,PaymentMethod,TransactionCode,CounterReference,PaymentDate,COALESCE(RefundedAmount,0) RefundedAmount FROM Payment WHERE BookingID=? ORDER BY PaymentID",id));
            data.put("rooms",rows(c,"SELECT d.RoomNumber,d.CheckInDate,d.CheckOutDate,d.GuestCount,r.NeedsCleaning FROM BookingDetail d JOIN Room r ON r.RoomNumber=d.RoomNumber WHERE d.BookingID=?",id));
            boolean earlyCheckout=false;
            for(Object value:(List<?>)data.get("rooms")) {
                Object end=((Map<?,?>)value).get("CheckOutDate");
                if(end!=null && ReportPeriod.today().isBefore(LocalDate.parse(end.toString()))) earlyCheckout=true;
            }
            data.put("earlyCheckout",earlyCheckout);
            List<Map<String,Object>> actualStay=rows(c,"SELECT CONVERT(date,DATEADD(MINUTE,420,ActualCheckOutAt)) ActualCheckoutDate FROM Booking WHERE BookingID=?",id);
            data.put("actualCheckoutDate",actualStay.isEmpty()?null:actualStay.get(0).get("ActualCheckoutDate"));
            data.put("refunds",rows(c,"SELECT Amount,Fee,Status,Method,Reference,CompletedAt FROM OfflineRefund WHERE BookingID=?",id));
            data.put("charges",rows(c,"SELECT Description,Amount,CreatedAt FROM OfflineCharge WHERE BookingID=? ORDER BY ChargeID",id));
            data.put("hasAccount",money(c,"SELECT CASE WHEN UserID IS NULL THEN 0 ELSE 1 END FROM Customer WHERE CustomerID=(SELECT CustomerID FROM Booking WHERE BookingID=?)",id).signum()>0);
        }
        return data;
    }

    private static List<Map<String,Object>> rows(Connection c,String sql,int id) throws SQLException {
        List<Map<String,Object>> rows=new ArrayList<>();
        try(PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) { Map<String,Object> row=new LinkedHashMap<>();
                    for(int i=1;i<=rs.getMetaData().getColumnCount();i++) row.put(rs.getMetaData().getColumnLabel(i),rs.getObject(i));
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
