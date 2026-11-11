package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import model.OfflineBookingDraft;
import util.ReportPeriod;

/** Explicit integration harness. Run ONLY against an isolated HotelOfflineTest_* database. */
public final class OfflineWorkflowIT {
    private static final OfflineOperationsDAO OPS=new OfflineOperationsDAO();
    private static final OfflineBookingDAO OFFLINE=new OfflineBookingDAO();
    private static final AtomicInteger CONTACT=new AtomicInteger(1000000+(int)(System.currentTimeMillis()%1000000));
    private static int staff,roomType,checks;
    private static final LocalDate TODAY=ReportPeriod.today();

    private static String key() { return UUID.randomUUID().toString(); }
    private static void check(boolean value,String description) {
        if(!value) throw new AssertionError(description);
        checks++; System.out.println("PASS: "+description);
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
    private static void rejected(Action a,String description) throws Exception {
        try { a.run(); } catch(IllegalArgumentException|SQLException expected) { check(true,description); return; }
        throw new AssertionError("Expected rejection: "+description);
    }
    private static void sql(String query,Object... args) throws SQLException {
        try(Connection c=new DBContext().getConnection();PreparedStatement ps=c.prepareStatement(query)) {
            for(int i=0;i<args.length;i++) ps.setObject(i+1,args[i]); ps.executeUpdate();
        }
    }
    private static BigDecimal number(String query,int id) throws SQLException {
        try(Connection c=new DBContext().getConnection();PreparedStatement ps=c.prepareStatement(query)) {
            ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()) { rs.next(); return rs.getBigDecimal(1); }
        }
    }
    private static int insert(String query,Object... args) throws SQLException {
        try(Connection c=new DBContext().getConnection();PreparedStatement ps=c.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)) {
            for(int i=0;i<args.length;i++) ps.setObject(i+1,args[i]);ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        }
    }
    private static int room() throws SQLException { return insert("INSERT Room(RoomTypeID,Status) VALUES(?,'available')",roomType); }
    private static OfflineBookingDAO.Quote quote(int room,LocalDate from,String code) throws SQLException {
        return OFFLINE.preview(staff,new OfflineBookingDraft(from,from.plusDays(2),1,room,null,"Test","Guest","","+84"+CONTACT.incrementAndGet(),code,"Integration test"));
    }
    private static int booking(LocalDate from) throws SQLException { return OFFLINE.create(staff,quote(room(),from,"")); }
    private static void pay(int id,int percent,String request) throws SQLException {
        Map<String,Object> q=OFFLINE.counterSummary(id);
        OPS.pay(id,staff,"cash",percent,request,(BigDecimal)q.get("paid"),(BigDecimal)q.get("total"),(BigDecimal)q.get("payable"),null);
    }
    private static String status(int id) { return new BookingDAO().getBookingById(id).getStatus(); }

    public static void main(String[] args) throws Exception {
        try(Connection c=new DBContext().getConnection()) {
            String catalog=c.getCatalog();
            if(catalog==null || !catalog.startsWith("HotelOfflineTest_")) throw new IllegalStateException("Refusing to write test fixtures outside HotelOfflineTest_*.");
        }
        staff=insert("INSERT [User](Username,[Password],Role,DisplayName,AccountStatus) VALUES(?,?,'staff','Counter test','active')","it_"+key().substring(0,15),"test-only-no-login");
        roomType=insert("INSERT RoomType(Name,PricePerNight,Beds,Capacity,IsActive) VALUES('Integration type',1000000,1,2,1)");

        int selected=room(); OfflineBookingDAO.Quote draft=quote(selected,TODAY,"");
        int id=OFFLINE.create(staff,draft);
        check(OFFLINE.create(staff,draft)==id,"Repeated creation returns the same booking");
        check(number("SELECT COUNT(*) FROM Customer c JOIN Booking b ON b.CustomerID=c.CustomerID WHERE b.BookingID=? AND c.UserID IS NULL AND c.Email IS NULL",id).intValue()==1,"Walk-in guest needs neither email nor login");
        check(new BookingDAO().getBookingById(id)!=null,"Guest booking remains visible to management");
        int guest=new BookingDAO().getBookingById(id).getCustomer().getCustomerID();
        check(new CustomerDAO().getCustomerById(guest)!=null,"Guest profile can be opened in customer management");
        check(new CustomerDAO().updateCustomerInformation(guest,"Test","Guest",null,draft.draft.phone,null,null,null),"Guest information can be updated without creating a login or requiring email");
        check(!OFFLINE.searchCustomers(draft.draft.phone).isEmpty(),"Guest profile can be found again by phone");
        rejected(() -> quote(selected,TODAY,""),"Overlapping reservation rejected");
        check(OFFLINE.availableRooms(TODAY.plusDays(2),TODAY.plusDays(3),1).stream().anyMatch(x->x.getRoomNumber()==selected),"Back-to-back reservations allowed");

        Map<String,Object> q=OFFLINE.counterSummary(id);
        String paymentKey=key();
        OPS.pay(id,staff,"cash",20,paymentKey,(BigDecimal)q.get("paid"),(BigDecimal)q.get("total"),(BigDecimal)q.get("payable"),null);
        OPS.pay(id,staff,"cash",20,paymentKey,(BigDecimal)q.get("paid"),(BigDecimal)q.get("total"),(BigDecimal)q.get("payable"),null);
        check("confirmed".equals(status(id)),"Deposit confirms booking");
        check(number("SELECT COUNT(*) FROM Payment WHERE BookingID=?",id).intValue()==1,"Replayed payment cannot collect twice");
        rejected(() -> OPS.pay(id,staff,"cash",100,key(),BigDecimal.ZERO,(BigDecimal)q.get("total"),(BigDecimal)q.get("payable"),null),"Stale payment quote rejected");
        rejected(() -> OPS.checkIn(id,staff,key(),false,1),"Identity verification required");
        sql("UPDATE Room SET NeedsCleaning=1 WHERE RoomNumber=?",selected);
        rejected(() -> OPS.checkIn(id,staff,key(),true,1),"Dirty room cannot be checked in");
        sql("UPDATE Room SET NeedsCleaning=0 WHERE RoomNumber=?",selected);
        rejected(() -> OPS.checkIn(id,staff,key(),true,3),"Actual guest count limited to capacity");
        OPS.checkIn(id,staff,key(),true,1);
        OPS.charge(id,staff,key(),"Laundry",new BigDecimal("12345"));
        rejected(() -> OPS.checkOut(id,staff,key()),"Unpaid balance blocks checkout");
        pay(id,100,key());
        String out=key();OPS.checkOut(id,staff,out);OPS.checkOut(id,staff,out);
        check("checked_out".equals(status(id)),"Paid booking can check out; replay is safe");
        check(number("SELECT CAST(NeedsCleaning AS INT) FROM Room WHERE RoomNumber=?",selected).intValue()==1,"Checkout marks room awaiting cleaning");
        OPS.complete(id,staff,key());OPS.clean(id,staff,key());
        check(number("SELECT CAST(NeedsCleaning AS INT) FROM Room WHERE RoomNumber=?",selected).intValue()==0,"Cleaning releases the room");
        check("completed".equals(status(id)),"Stay can be completed after settlement");
        check(!new BookingDAO().updateBookingStatus(id,"cancelled"),"Legacy status route cannot bypass offline workflow");

        int future=booking(TODAY.plusDays(3));pay(future,100,key());
        rejected(() -> OPS.checkIn(future,staff,key(),true,1),"Early check-in blocked");
        OPS.cancel(future,staff,key(),"Guest changed plans");
        check(number("SELECT Amount FROM OfflineRefund WHERE BookingID=? AND Status='pending'",future).compareTo(new BigDecimal("2000000"))==0,"Cancellation before arrival creates full refund due");
        check(number("SELECT SUM(Amount) FROM Payment WHERE BookingID=? AND PaymentStatus='paid'",future).signum()>0,"Refund is not marked paid before real payout");
        String refundKey=key();OPS.refund(future,staff,refundKey,"cash",null);OPS.refund(future,staff,refundKey,"cash",null);
        check(((BigDecimal)OFFLINE.counterSummary(future).get("remaining")).signum()==0,"Cancelled booking does not show a fictitious outstanding balance after refund");
        check(number("SELECT COUNT(*) FROM OfflineRefund WHERE BookingID=? AND Status='completed'",future).intValue()==1,"Refund confirmation is idempotent");
        check(number("SELECT SUM(RefundedAmount) FROM Payment WHERE BookingID=?",future).compareTo(new BigDecimal("2000000"))==0,"Refund allocated to original receipts");
        int sameDay=booking(TODAY);pay(sameDay,100,key());OPS.cancel(sameDay,staff,key(),"No show");
        check(number("SELECT Amount FROM OfflineRefund WHERE BookingID=?",sameDay).compareTo(new BigDecimal("1600000"))==0,"Same-day cancellation retains the configured deposit");

        int held=booking(TODAY.plusDays(5));
        sql("UPDATE Booking SET BookingDate=DATEADD(MINUTE,-40,SYSDATETIME()),HoldExpiresAt=DATEADD(MINUTE,20,SYSUTCDATETIME()) WHERE BookingID=?",held);
        new BookingDAO().expirePendingBookings();OFFLINE.expireHolds();
        check("pending".equals(status(held)),"Generic 30-minute expiry does not cancel an extended offline hold");
        sql("UPDATE Booking SET HoldExpiresAt=DATEADD(SECOND,-1,SYSUTCDATETIME()) WHERE BookingID=?",held);
        rejected(() -> pay(held,20,key()),"Expired hold cannot collect money");
        OFFLINE.expireHolds();check("cancelled".equals(status(held)),"Expired offline hold releases reservation");

        int discountedRoom=room();String code="IT"+key().substring(0,12);
        insert("INSERT Discount(Code,Quantity,SaleOff,IsActive) VALUES(?,1,10,1)",code);
        int discounted=OFFLINE.create(staff,quote(discountedRoom,TODAY.plusDays(6),code));pay(discounted,20,key());
        check(number("SELECT TotalPrice FROM Booking WHERE BookingID=?",discounted).compareTo(new BigDecimal("1800000"))==0,"Promotion reduces total exactly once");
        pay(discounted,100,key());
        check(number("SELECT SUM(Amount) FROM Payment WHERE BookingID=?",discounted).compareTo(new BigDecimal("1800000"))==0,"Second collection pays only the balance");
        check(number("SELECT d.Quantity FROM Discount d JOIN Booking b ON b.OfflinePromotionCode=d.Code WHERE b.BookingID=?",discounted).intValue()==0,"Voucher stock consumed once");

        String bankRef="IT-"+key();
        int bank=booking(TODAY.plusDays(8));Map<String,Object> bankQuote=OFFLINE.counterSummary(bank);
        rejected(() -> OPS.pay(bank,staff,"bank_transfer",100,key(),BigDecimal.ZERO,(BigDecimal)bankQuote.get("total"),(BigDecimal)bankQuote.get("payable"),null),"Manual bank transfer requires a reference");
        OPS.pay(bank,staff,"bank_transfer",100,key(),BigDecimal.ZERO,(BigDecimal)bankQuote.get("total"),(BigDecimal)bankQuote.get("payable"),bankRef);
        int bank2=booking(TODAY.plusDays(8));Map<String,Object> bank2Quote=OFFLINE.counterSummary(bank2);
        rejected(() -> OPS.pay(bank2,staff,"bank_transfer",100,key(),BigDecimal.ZERO,(BigDecimal)bank2Quote.get("total"),(BigDecimal)bank2Quote.get("payable"),bankRef),"Bank reference cannot be reused on another booking");
        check("pending".equals(status(bank2)),"Rejected bank payment rolls back booking status");

        int racing=booking(TODAY.plusDays(9));Map<String,Object> snapshot=OFFLINE.counterSummary(racing);
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        List<Future<Boolean>> results=new ArrayList<>();
        for(int i=0;i<2;i++) results.add(pool.submit(() -> {
            start.await();
            try { OPS.pay(racing,staff,"cash",20,key(),(BigDecimal)snapshot.get("paid"),(BigDecimal)snapshot.get("total"),(BigDecimal)snapshot.get("payable"),null);return true; }
            catch(IllegalArgumentException|SQLException expected) { return false; }
        }));
        start.countDown();int accepted=0;for(Future<Boolean> result:results) if(result.get(20,TimeUnit.SECONDS)) accepted++;pool.shutdownNow();
        check(accepted==1,"Two staff collecting simultaneously cannot both accept the same quote");
        check(number("SELECT COUNT(*) FROM Payment WHERE BookingID=?",racing).intValue()==1,"Concurrent collection creates exactly one receipt");

        int custom=booking(TODAY.plusDays(10));
        sql("UPDATE Booking SET OfflineDepositPercent=25 WHERE BookingID=?",custom);
        pay(custom,25,key());
        check(number("SELECT SUM(Amount) FROM Payment WHERE BookingID=?",custom).compareTo(new BigDecimal("500000"))==0,"Counter supports a custom deposit policy, not only 10/20 percent");
        String freeCode="FREE"+key().substring(0,10);
        insert("INSERT Discount(Code,Quantity,SaleOff,IsActive) VALUES(?,1,100,1)",freeCode);
        int free=OFFLINE.create(staff,quote(room(),TODAY.plusDays(11),freeCode));pay(free,100,key());
        check("confirmed".equals(status(free)),"A fully discounted booking can be confirmed without collecting money");
        check(number("SELECT SUM(Amount) FROM Payment WHERE BookingID=?",free).signum()==0,"Zero-amount receipt is recorded without inventing a payment");
        int paidLong=booking(TODAY.plusDays(12));pay(paidLong,20,key());
        sql("UPDATE Booking SET HoldExpiresAt=DATEADD(DAY,-1,SYSUTCDATETIME()) WHERE BookingID=?",paidLong);
        OFFLINE.expireHolds();check("confirmed".equals(status(paidLong)),"Paid confirmed reservations do not expire with the original hold");

        int denied=booking(TODAY.plusDays(10));sql("UPDATE [User] SET AccountStatus='locked' WHERE UserID=?",staff);
        rejected(() -> pay(denied,100,key()),"Revoked staff permissions checked inside transaction");
        sql("UPDATE [User] SET AccountStatus='active' WHERE UserID=?",staff);
        System.out.println("All "+checks+" integration checks passed.");
    }
}
