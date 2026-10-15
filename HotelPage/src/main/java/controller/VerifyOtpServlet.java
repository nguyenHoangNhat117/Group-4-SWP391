package controller;

import util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "VerifyOtpServlet", urlPatterns = "/verify-otp")
public class VerifyOtpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AuthSupport.noStore(res);
        if (recovery(req) == null) {
            res.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/login/enter_otp.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);
        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }
        PasswordRecovery state = recovery(req);
        if (!AuthRateLimiter.allow("verify-ip:" + req.getRemoteAddr(), 30, 900000)) {
            res.sendError(429, "Too many attempts. Try again later.");
            return;
        }
        if (state != null && state.verify(AuthSupport.text(req.getParameter("otp")), System.currentTimeMillis())) {
            req.changeSessionId();
            res.sendRedirect(req.getContextPath() + "/reset-password");
            return;
        }
        if (state == null) {
            res.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }
        req.setAttribute("error", "OTP is incorrect, expired or has reached its attempt limit. Request a new code if needed.");
        req.getRequestDispatcher("/WEB-INF/login/enter_otp.jsp").forward(req, res);
    }

    static PasswordRecovery recovery(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session == null ? null : session.getAttribute("passwordRecovery");
        return value instanceof PasswordRecovery ? (PasswordRecovery) value : null;
    }
}
