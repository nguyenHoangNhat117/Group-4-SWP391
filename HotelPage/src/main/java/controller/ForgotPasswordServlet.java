package controller;

import dao.UserDAO;
import model.User;
import util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Locale;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = "/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AuthSupport.noStore(res);
        AuthSupport.token(req.getSession());
        req.getRequestDispatcher("/WEB-INF/login/forgot_password.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);
        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }
        HttpSession session = req.getSession(false);
        AuthSupport.clearRecovery(session);
        String email = AuthSupport.text(req.getParameter("email"));
        if (!AuthSupport.validEmail(email)) {
            fail(req, res, "Enter a valid email address.");
            return;
        }
        if (!AuthRateLimiter.allow("otp-ip:" + req.getRemoteAddr(), 10, 900000)
                || !AuthRateLimiter.allow("otp-email:" + email.toLowerCase(Locale.ROOT), 3, 900000)) {
            res.setStatus(429);
            fail(req, res, "Too many requests. Try again in 15 minutes.");
            return;
        }
        User user;
        try {
            user = new UserDAO().getUserByEmail(email);
        } catch (IllegalStateException e) {
            log("Recovery database unavailable", e);
            fail(req, res, "Password recovery is temporarily unavailable.");
            return;
        }
        String otp = Integer.toString(100000 + RANDOM.nextInt(900000));
        if (user != null) {
            try {
                EmailUtility.sendOtpEmail(email, otp);
            } catch (MessagingException e) {
                // Do not log SMTP credentials or the OTP.
                log("OTP email delivery failed: " + e.getClass().getSimpleName());
                fail(req, res, "Email delivery is temporarily unavailable. Try again later.");
                return;
            }
        }
        // Dummy challenge for unknown emails keeps the visible response generic.
        session.setAttribute("passwordRecovery", new PasswordRecovery(
                user == null ? -1 : user.getId(), user == null ? "" : user.getPassword(), otp, System.currentTimeMillis()));
        req.setAttribute("message", "If an eligible customer account exists, an OTP has been sent. It expires in 5 minutes.");
        req.getRequestDispatcher("/WEB-INF/login/enter_otp.jsp").forward(req, res);
    }

    private void fail(HttpServletRequest req, HttpServletResponse res, String error) throws ServletException, IOException {
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/login/forgot_password.jsp").forward(req, res);
    }
}
