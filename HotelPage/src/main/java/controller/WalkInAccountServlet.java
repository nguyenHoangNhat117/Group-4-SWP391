package controller;

import dao.WalkInAccountDAO;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.SQLException;
import java.util.Locale;
import util.*;

@WebServlet("/walkin-account")
public class WalkInAccountServlet extends HttpServlet {
    private static final String KEY="uc09WalkinActivationV1";
    private static final SecureRandom RANDOM=new SecureRandom();
    private static final class Challenge implements Serializable {
        private static final long serialVersionUID=1L;
        final int customerId;
        final String email;
        final long expires;
        String otp;
        int attempts;
        boolean consumed;
        Challenge(int id,String email,String otp){this.customerId=id;this.email=email;this.otp=otp;expires=System.currentTimeMillis()+300000L;}
        boolean active(){return !consumed && otp!=null && attempts<3 && System.currentTimeMillis()<expires;}
        boolean matches(String supplied){
            return supplied!=null && supplied.matches("[0-9]{6}") && MessageDigest.isEqual(
                otp.getBytes(StandardCharsets.US_ASCII),supplied.getBytes(StandardCharsets.US_ASCII));
        }
        void consume(){consumed=true;otp=null;}
    }
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException {
        AuthSupport.noStore(s);
        try {
            HttpSession session=r.getSession();
            if(session.getAttribute("loggedUser")!=null){s.sendRedirect(r.getContextPath()+"/login");return;}
            AuthSupport.token(session);
            synchronized(session){
                r.setAttribute("activated",Boolean.TRUE.equals(session.getAttribute("uc09WalkinActivated")));
                session.removeAttribute("uc09WalkinActivated");show(r,s,session);
            }
        }catch(IllegalStateException ex){expired(s);}
    }
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException {
        r.setCharacterEncoding("UTF-8");AuthSupport.noStore(s);
        HttpSession session=r.getSession(false);
        if(session==null){expired(s);return;}
        try {
            synchronized(session){
                if(session.getAttribute("loggedUser")!=null){s.sendError(409,"Đăng xuất trước khi kích hoạt một tài khoản khác.");return;}
                if(!AuthSupport.checkCsrf(r,s))return;
                String action=AuthSupport.text(r.getParameter("action"));
                if("send".equals(action)){
                    String email=AuthSupport.text(r.getParameter("email"));r.setAttribute("enteredEmail",email);
                    if(!AuthUcPolicy.validEmail(email)){fail(r,s,session,"Nhập email hợp lệ, tối đa 100 ký tự.");return;}
                    if(!AuthRateLimiter.allow("walkin-send-ip:"+r.getRemoteAddr(),10,900000L)
                            || !AuthRateLimiter.allow("walkin-send-email:"+email.toLowerCase(Locale.ROOT),3,900000L)){
                        s.setStatus(429);fail(r,s,session,"Đã gửi quá nhiều yêu cầu. Thử lại sau 15 phút.");return;
                    }
                    Object old=session.getAttribute(KEY);if(old instanceof Challenge)((Challenge)old).consume();session.removeAttribute(KEY);
                    Integer id;
                    try{id=new WalkInAccountDAO().eligibleCustomer(email);}
                    catch(SQLException ex){log("Walkin lookup unavailable",ex);s.setStatus(503);fail(r,s,session,"Chưa kiểm tra được hồ sơ. Thử lại sau.");return;}
                    String otp=String.format(Locale.ROOT,"%06d",RANDOM.nextInt(1000000));
                    if(id!=null){
                        try{WalkInActivationMail.send(email,otp);}
                        catch(MessagingException ex){s.setStatus(503);fail(r,s,session,"Chưa gửi được email. Vui lòng thử lại sau.");return;}
                    }
                    session.setAttribute(KEY,new Challenge(id==null?-1:id,email,otp));
                    s.sendRedirect(r.getContextPath()+"/walkin-account?sent=1");return;
                }
                if(!"activate".equals(action)){s.sendError(400,"Thao tác không hợp lệ.");return;}
                Object stored=session.getAttribute(KEY);
                Challenge state=stored instanceof Challenge?(Challenge)stored:null;
                if(state==null||!state.active()){s.setStatus(409);fail(r,s,session,"Mã đã hết hạn hoặc hết lượt thử. Yêu cầu mã mới.");return;}
                if(!AuthRateLimiter.allow("walkin-verify-ip:"+r.getRemoteAddr(),30,900000L)){
                    s.setStatus(429);fail(r,s,session,"Quá nhiều yêu cầu xác minh. Thử lại sau.");return;
                }
                // Mỗi lần gửi tới bước kích hoạt đều tính một lượt thử mã.
                state.attempts++;
                String supplied=AuthSupport.text(r.getParameter("otp"));
                if(!state.matches(supplied)||state.customerId<=0){
                    if(state.attempts>=3)state.consume();
                    fail(r,s,session,"Mã không đúng, hết hạn hoặc hồ sơ chưa đủ điều kiện kích hoạt.");return;
                }
                String username=AuthSupport.text(r.getParameter("username"));
                String password=r.getParameter("password");String confirm=r.getParameter("confirmPassword");
                r.setAttribute("enteredUsername",username);
                if(!AuthSupport.validUsername(username)||!AuthSupport.validPassword(password)||!password.equals(confirm)){
                    if(state.attempts>=3)state.consume();
                    fail(r,s,session,"Username cần 3–50 ký tự. Mật khẩu cần 8–128 ký tự, đủ chữ hoa/thường, số, ký tự đặc biệt, không có khoảng trắng; xác nhận phải khớp.");return;
                }
                try {
                    new WalkInAccountDAO().activate(state.customerId,state.email,username,password);
                }catch(IllegalArgumentException ex){
                    if(state.attempts>=3)state.consume();fail(r,s,session,ex.getMessage());return;
                }catch(SQLException ex){
                    log("Walkin activation result unavailable",ex);
                    if(state.attempts>=3)state.consume();s.setStatus(503);
                    fail(r,s,session,"Chưa xác nhận được kết quả. Thử đăng nhập trước; nếu chưa được, yêu cầu mã mới rồi thử lại.");return;
                }
                state.consume();session.removeAttribute(KEY);
                r.changeSessionId();session.removeAttribute("csrfToken");AuthSupport.token(session);
                session.setAttribute("uc09WalkinActivated",true);
                s.sendRedirect(r.getContextPath()+"/walkin-account?activated=1");
            }
        }catch(IllegalStateException ex){expired(s);}
    }
    private void show(HttpServletRequest r,HttpServletResponse s,HttpSession session)throws ServletException,IOException {
        Object value=session.getAttribute(KEY);Challenge state=value instanceof Challenge?(Challenge)value:null;
        if(state!=null){
            r.setAttribute("challengeEmail",state.email);r.setAttribute("challengeActive",state.active());
            r.setAttribute("remainingAttempts",state.active()?3-state.attempts:0);
        }
        r.getRequestDispatcher("/WEB-INF/booking-management/walkin-account.jsp").forward(r,s);
    }
    private void fail(HttpServletRequest r,HttpServletResponse s,HttpSession session,String error)throws ServletException,IOException {
        r.setAttribute("error",error);show(r,s,session);
    }
    private void expired(HttpServletResponse s)throws IOException {if(!s.isCommitted())s.sendError(403,"Form đã hết hạn. Tải lại trang để tiếp tục.");}
}
