package controller;

import dao.OfflineBookingDAO;
import dao.OfflineBookingDAO.Quote;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import model.OfflineBookingDraft;
import model.User;
import util.AuthSupport;
import util.OfflineBookingPolicy;
import util.ReportPeriod;

@WebServlet("/offline-booking")
public class OfflineBookingServlet extends HttpServlet {
    private static final String FORMS="uc09DirectFormsV1";
    private static final long MAX_AGE=30*60*1000L;
    private static final String[] FIELDS={
        "checkIn","checkOut","guests","roomNumber","customerMode","customerId","q",
        "firstName","lastName","email","phone","promotionCode","specialRequest"
    };

    // Giữ kiểu Pending để các session cũ có thể được hủy an toàn khi triển khai lại.
    private static final class Pending implements Serializable {
        private static final long serialVersionUID=1L;
        final Quote quote;
        final Map<String,String> form;
        final long created=System.currentTimeMillis();
        Pending(Quote quote,Map<String,String> form){
            this.quote=quote;
            this.form=new LinkedHashMap<>(form);
        }
    }

    private static final class DirectForm implements Serializable {
        private static final long serialVersionUID=1L;
        final String token=UUID.randomUUID().toString();
        final int staffId;
        final long created=System.currentTimeMillis();
        Pending pending;
        int bookingId;
        DirectForm(int staffId){this.staffId=staffId;}
    }

    static User staff(HttpServletRequest r,HttpServletResponse s)throws IOException {
        AuthSupport.noStore(s);
        HttpSession session=r.getSession(false);
        Object value=session==null?null:session.getAttribute("loggedUser");
        if(!(value instanceof User)){
            s.sendRedirect(r.getContextPath()+"/login");
            return null;
        }
        User user=(User)value;
        if(!"staff".equalsIgnoreCase(user.getRole())
                || !"active".equalsIgnoreCase(user.getAccountStatus())){
            s.sendError(403,"Chỉ Staff đang hoạt động được thao tác tại quầy.");
            return null;
        }
        AuthSupport.token(session);
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException {
        r.setCharacterEncoding("UTF-8");
        User user=staff(r,s);
        if(user==null)return;
        DirectForm state=newForm(r.getSession(false),user.getId());
        render(r,s,read(r),state);
    }

    @Override
    protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException {
        r.setCharacterEncoding("UTF-8");
        User user=staff(r,s);
        if(user==null || !AuthSupport.checkCsrf(r,s))return;
        Map<String,String> form=read(r);
        DirectForm state=findForm(r.getSession(false),r.getParameter("submissionToken"));
        if(state==null || state.staffId!=user.getId()){
            s.setStatus(409);
            r.setAttribute("error","Form đã hết hạn hoặc thuộc phiên khác. Kiểm tra lịch sử nếu đã bấm tạo, rồi thử lại trên form mới này.");
            render(r,s,form,newForm(r.getSession(false),user.getId()));
            return;
        }

        String action=AuthSupport.text(r.getParameter("action"));
        if("search".equals(action)){
            render(r,s,form,state);
            return;
        }
        if(!"create".equals(action)){
            s.setStatus(409);
            r.setAttribute("error","Form cũ không còn dùng bước xem tóm tắt. Kiểm tra thông tin rồi bấm Tạo đặt phòng.");
            render(r,s,form,state);
            return;
        }

        try {
            // Chỉ tuần tự hóa các lần bấm trên cùng một form, không khóa mọi tab.
            synchronized(state){
                if(state.bookingId>0){redirect(s,r,state.bookingId);return;}
                OfflineBookingDAO dao=new OfflineBookingDAO();
                if(state.pending!=null && !state.pending.form.equals(form)){
                    throw new IllegalArgumentException("Yêu cầu trước chưa xác định được kết quả. Giữ nguyên thông tin để thử lại cùng yêu cầu, hoặc kiểm tra lịch sử rồi mở form mới.");
                }
                if(state.pending==null){
                    OfflineBookingDraft draft=draft(form);
                    // Giữ nguyên kiểm tra và tính giá của DAO, chỉ bỏ màn preview riêng.
                    Quote checked=dao.preview(user.getId(),draft);
                    state.pending=new Pending(checked,form);
                }
                try {
                    state.bookingId=dao.create(user.getId(),state.pending.quote);
                } catch(IllegalArgumentException ex){
                    // DAO đã rollback yêu cầu bị từ chối; cho phép sửa dữ liệu và thử lại.
                    state.pending=null;
                    throw ex;
                } catch(SQLException ex){
                    if(ex.getErrorCode()==2601 || ex.getErrorCode()==2627)state.pending=null;
                    // Kết quả SQL chưa rõ: giữ quote/requestKey để retry không sinh đơn khác.
                    throw ex;
                }
                redirect(s,r,state.bookingId);
                return;
            }
        } catch(DateTimeParseException | NumberFormatException ex){
            s.setStatus(400);
            r.setAttribute("error","Vui lòng nhập ngày hợp lệ, số khách và chọn phòng/khách hàng.");
        } catch(IllegalArgumentException ex){
            s.setStatus(409);
            r.setAttribute("error",ex.getMessage());
        } catch(SQLException ex){
            log("UC09 direct create failed",ex);
            s.setStatus(503);
            boolean duplicate=ex.getErrorCode()==2601 || ex.getErrorCode()==2627;
            r.setAttribute("error",duplicate
                    ? "Thông tin bị trùng. Tìm khách có sẵn hoặc kiểm tra lịch sử booking trước khi tạo lại."
                    : "Chưa xác nhận được kết quả tạo đơn. Kiểm tra lịch sử trước; nếu thử lại cùng form, hệ thống sẽ dùng lại yêu cầu cũ.");
        }
        render(r,s,form,state);
    }

    private void redirect(HttpServletResponse s,HttpServletRequest r,int id){
        s.setStatus(303);
        s.setHeader("Location",r.getContextPath()+"/counter-payment?bookingId="+id+"&created=1");
    }

    private OfflineBookingDraft draft(Map<String,String> form){
        String mode=form.get("customerMode");
        if(!Arrays.asList("existing","new").contains(mode)){
            throw new IllegalArgumentException("Chọn khách có sẵn hoặc khách mới.");
        }
        return new OfflineBookingDraft(
                LocalDate.parse(form.get("checkIn")),LocalDate.parse(form.get("checkOut")),
                Integer.parseInt(form.get("guests")),Integer.parseInt(form.get("roomNumber")),
                "new".equals(mode)?null:Integer.valueOf(form.get("customerId")),
                form.get("firstName"),form.get("lastName"),form.get("email"),form.get("phone"),
                form.get("promotionCode"),form.get("specialRequest"));
    }

    private Map<String,String> read(HttpServletRequest r){
        Map<String,String> values=new LinkedHashMap<>();
        for(String key:FIELDS)values.put(key,AuthSupport.text(r.getParameter(key)));
        if(values.get("customerMode").isEmpty())values.put("customerMode","existing");
        if(values.get("guests").isEmpty())values.put("guests","1");
        return values;
    }

    private void render(HttpServletRequest r,HttpServletResponse s,Map<String,String> form,DirectForm state)
            throws ServletException,IOException {
        r.setAttribute("form",form);
        r.setAttribute("submissionToken",state.token);
        r.setAttribute("today",ReportPeriod.today());
        r.setAttribute("holdMinutes",OfflineBookingPolicy.HOLD_MINUTES);
        r.setAttribute("depositPercent",OfflineBookingPolicy.DEPOSIT_PERCENT);
        try {
            OfflineBookingDAO dao=new OfflineBookingDAO();
            if(!form.get("q").isEmpty())r.setAttribute("customers",dao.searchCustomers(form.get("q")));
            if(!form.get("checkIn").isEmpty() || !form.get("checkOut").isEmpty()){
                r.setAttribute("rooms",dao.availableRooms(LocalDate.parse(form.get("checkIn")),
                        LocalDate.parse(form.get("checkOut")),Integer.parseInt(form.get("guests"))));
                r.setAttribute("searched",true);
            }
        } catch(DateTimeParseException | NumberFormatException ex){
            if(r.getAttribute("error")==null){
                s.setStatus(400);
                r.setAttribute("error","Vui lòng nhập đủ ngày nhận/trả và số khách hợp lệ.");
            }
        } catch(IllegalArgumentException ex){
            if(r.getAttribute("error")==null){s.setStatus(400);r.setAttribute("error",ex.getMessage());}
        } catch(SQLException ex){
            log("UC09 search failed",ex);
            s.setStatus(503);
            if(r.getAttribute("error")==null)r.setAttribute("error","Không thể tải dữ liệu. Vui lòng thử lại.");
        }
        r.getRequestDispatcher("/WEB-INF/booking-management/offline.jsp").forward(r,s);
    }

    @SuppressWarnings("unchecked")
    private Map<String,DirectForm> forms(HttpSession session){
        Object existing=session.getAttribute(FORMS);
        if(existing instanceof Map)return (Map<String,DirectForm>)existing;
        Map<String,DirectForm> values=new LinkedHashMap<>();
        session.setAttribute(FORMS,values);
        return values;
    }

    private DirectForm newForm(HttpSession session,int staffId){
        synchronized(session){
            Map<String,DirectForm> values=forms(session);
            values.values().removeIf(value->System.currentTimeMillis()-value.created>MAX_AGE);
            while(values.size()>=10)values.remove(values.keySet().iterator().next());
            DirectForm state=new DirectForm(staffId);
            values.put(state.token,state);
            return state;
        }
    }

    private DirectForm findForm(HttpSession session,String token){
        synchronized(session){
            Map<String,DirectForm> values=forms(session);
            values.values().removeIf(value->System.currentTimeMillis()-value.created>MAX_AGE);
            return values.get(token);
        }
    }
}
