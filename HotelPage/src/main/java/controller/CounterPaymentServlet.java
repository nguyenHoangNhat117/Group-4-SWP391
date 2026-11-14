package controller;

import dao.OfflineBookingDAO;
import dao.OfflineOperationsDAO;
import dao.CounterQrDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import model.User;
import util.AuthSupport;
import util.OnlinePaymentConfig;

@WebServlet("/counter-payment")
public class CounterPaymentServlet extends HttpServlet {
    private static final String KEY="uc09CounterQuotes";

    @Override
    protected void doGet(HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException {
        if(OfflineBookingServlet.staff(r,s)==null) return;
        AuthSupport.noStore(s);
        try {
            int id=positive(r.getParameter("bookingId"));
            CounterQrDAO qr = new CounterQrDAO();
            if ("qr".equals(r.getParameter("view")) || "qr-status".equals(r.getParameter("view"))) {
                showQr(r,s,id,qr);
                return;
            }
            Map<String,Object> summary = new OfflineBookingDAO().counterSummary(id);
            if (!Boolean.TRUE.equals(summary.get("offline"))) {
                s.sendRedirect(r.getContextPath()+bookingDetail(id));
                return;
            }
            prepareCounter(r, id, summary);
            r.getRequestDispatcher("/WEB-INF/booking-management/counter-payment.jsp").forward(r,s);
        } catch(IllegalArgumentException ex) {
            s.sendError(400,ex.getMessage());
        } catch(SQLException ex) {
            log("Counter read failed",ex);
            s.sendError(503,"Không thể tải hồ sơ tại quầy. Kiểm tra database và migration counter_qr.sql.");
        }
    }

    static void prepareCounter(HttpServletRequest r, int id) throws SQLException {
        prepareCounter(r, id, new OfflineBookingDAO().counterSummary(id));
    }

    private static void prepareCounter(HttpServletRequest r, int id, Map<String,Object> summary) throws SQLException {
        Map<String,Object> activeQr = new CounterQrDAO().active(id);
        if (activeQr != null && Boolean.TRUE.equals(summary.get("offline"))
                && "pending".equals(summary.get("status"))) {
            BigDecimal quotedPayable = (BigDecimal) activeQr.get("Payable");
            BigDecimal paid = (BigDecimal) summary.get("paid");
            summary.put("payable", quotedPayable);
            summary.put("remaining", quotedPayable.subtract(paid).max(BigDecimal.ZERO));
            summary.remove("promotionError");
        }
        r.setAttribute("activeQr",activeQr);
        r.setAttribute("qrConfigured",OnlinePaymentConfig.payosConfigured()
                && OnlinePaymentConfig.callbacksConfigured());
        String token=UUID.randomUUID().toString();
        Map<String,Object> saved=new LinkedHashMap<>(summary);
        saved.put("createdAt",System.currentTimeMillis());
        synchronized(r.getSession(false)) {
            Map<String,Map<String,Object>> quotes=quotes(r.getSession(false));
            quotes.values().removeIf(q -> System.currentTimeMillis()-(Long)q.get("createdAt")>30*60*1000L);
            while(quotes.size()>=20) quotes.remove(quotes.keySet().iterator().next());
            quotes.put(token,saved);
        }
        r.setAttribute("summary",summary);
        r.setAttribute("operations",new OfflineOperationsDAO().operations(id));
        r.setAttribute("paymentKey",token);
        synchronized(r.getSession(false)) {
            r.setAttribute("operationMessage",r.getSession(false).getAttribute("offlineMessage:"+id));
            r.getSession(false).removeAttribute("offlineMessage:"+id);
        }
    }

    private static String bookingDetail(int id) {
        return "/booking-management?view=detail&id=" + id + "#booking-actions";
    }

    @Override
    protected void doPost(HttpServletRequest r,HttpServletResponse s) throws IOException {
        r.setCharacterEncoding("UTF-8");
        User actor=OfflineBookingServlet.staff(r,s);
        if(actor==null || !AuthSupport.checkCsrf(r,s)) return;
        String key=r.getParameter("paymentKey");
        Map<String,Object> quote;
        synchronized(r.getSession(false)) { quote=quotes(r.getSession(false)).get(key); }
        if(quote==null || System.currentTimeMillis()-(Long)quote.get("createdAt")>30*60*1000L) {
            s.sendError(409,"Màn thao tác đã hết hạn. Mở lại từ chi tiết booking.");
            return;
        }
        int id=(Integer)quote.get("bookingId");
        try {
            String action=AuthSupport.text(r.getParameter("action"));
            if(!"qr".equals(action) && new CounterQrDAO().active(id)!=null) {
                throw new IllegalArgumentException("QR đang chờ đối soát. Mở lại QR để kiểm tra trước khi thao tác.");
            }
            OfflineOperationsDAO ops=new OfflineOperationsDAO();
            switch(action) {
                case "": case "pay":
                    if (!"cash".equals(AuthSupport.text(r.getParameter("method")))) {
                        throw new IllegalArgumentException("Chuyển khoản phải dùng QR và chờ hệ thống xác nhận.");
                    }
                    ops.pay(id,actor.getId(),AuthSupport.text(r.getParameter("method")),positive(r.getParameter("percent")),key,
                            (BigDecimal)quote.get("paid"),(BigDecimal)quote.get("total"),(BigDecimal)quote.get("payable"),r.getParameter("reference"));
                    break;
                case "qr": {
                    CounterQrDAO qr = new CounterQrDAO();
                    long order = qr.start(id,actor.getId(),key,positive(r.getParameter("percent")),
                            (BigDecimal)quote.get("paid"),(BigDecimal)quote.get("total"),(BigDecimal)quote.get("payable"));
                    try { qr.ensureLink(order); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                    catch (Exception ex) { log("QR create result requires reconciliation; order="+order,ex); }
                    s.setStatus(303);
                    s.setHeader("Location",r.getContextPath()+"/counter-payment?bookingId="+id+"&view=qr&orderCode="+order);
                    return;
                }
                case "promotion":
                    new OfflineBookingDAO().changePending(actor.getId(),id,r.getParameter("promotionCode"),false);
                    break;
                case "cancel": ops.cancel(id,actor.getId(),key,r.getParameter("reason")); break;
                case "check_in": ops.checkIn(id,actor.getId(),key,"yes".equals(r.getParameter("identityVerified")),positive(r.getParameter("actualGuests"))); break;
                case "check_out": ops.checkOut(id,actor.getId(),key,"yes".equals(r.getParameter("earlyCheckoutAccepted"))); break;
                case "complete": ops.complete(id,actor.getId(),key); break;
                case "clean": ops.clean(id,actor.getId(),key); break;
                case "charge": ops.charge(id,actor.getId(),key,r.getParameter("description"),new BigDecimal(AuthSupport.text(r.getParameter("amount")))); break;
                case "refund": ops.refund(id,actor.getId(),key,AuthSupport.text(r.getParameter("method")),r.getParameter("reference")); break;
                default: throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            r.getSession(false).setAttribute("offlineMessage:"+id,"Đã ghi nhận thao tác thành công.");
        } catch(IllegalArgumentException ex) {
            r.getSession(false).setAttribute("offlineMessage:"+id,ex.getMessage());
        } catch(SQLException ex) {
            log("Counter write failed; bookingId="+id,ex);
            String message=ex.getErrorCode()==2601 || ex.getErrorCode()==2627
                    ? "Mã giao dịch đã được ghi nhận. Kiểm tra lịch sử trước khi thu/hoàn lại."
                    : "Không xác nhận được kết quả. Database có thể đang bận; tải lại và kiểm tra lịch sử trước khi thử lại.";
            r.getSession(false).setAttribute("offlineMessage:"+id,message);
        }
        s.setStatus(303);
        String destination = Boolean.TRUE.equals(quote.get("offline"))
                ? "/counter-payment?bookingId=" + id : bookingDetail(id);
        s.setHeader("Location",r.getContextPath()+destination);
    }

    private void showQr(HttpServletRequest r,HttpServletResponse s,int booking,CounterQrDAO dao)
            throws SQLException,ServletException,IOException {
        long code;
        try { code=Long.parseLong(r.getParameter("orderCode")); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("Mã QR không hợp lệ."); }
        Map<String,Object> q=dao.find(code);
        if(q==null || ((Number)q.get("BookingID")).intValue()!=booking) {
            s.sendError(404); return;
        }
        boolean available=true;
        if(!Arrays.asList("paid","rejected").contains(q.get("State"))) {
            try { dao.reconcile(code); }
            catch(InterruptedException ex) { Thread.currentThread().interrupt(); available=false; }
            catch(Exception ex) { available=false; log("Unable to reconcile counter QR "+code,ex); }
            q=dao.find(code);
        }
        if("qr-status".equals(r.getParameter("view"))) {
            s.setContentType("text/plain;charset=UTF-8");
            s.getWriter().write(available ? (String)q.get("State") : "unavailable");
            return;
        }
        r.setAttribute("qr",q);
        r.setAttribute("qrAvailable",available);
        r.setAttribute("counterBackUrl",r.getContextPath() + (q.get("CreatedByUserID") == null
                ? bookingDetail(booking) : "/counter-payment?bookingId=" + booking));
        r.getRequestDispatcher("/WEB-INF/booking-management/counter-qr.jsp").forward(r,s);
    }

    private int positive(String raw) {
        try { int id=Integer.parseInt(raw); if(id>0) return id; }
        catch(NumberFormatException ignored) { }
        throw new IllegalArgumentException("Nhập số nguyên dương hợp lệ.");
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Map<String,Object>> quotes(HttpSession session) {
        Object saved=session.getAttribute(KEY);
        if(saved instanceof Map) return (Map<String,Map<String,Object>>)saved;
        Map<String,Map<String,Object>> data=new LinkedHashMap<>();
        session.setAttribute(KEY,data); return data;
    }
}
