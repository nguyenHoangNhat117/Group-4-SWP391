package controller;

import dao.*;
import model.*;
import util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Locale;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AuthSupport.noStore(res);
        HttpSession session = req.getSession();
        AuthSupport.token(session);
        if ("reset".equals(req.getParameter("success"))) {
            req.setAttribute("message", "Password reset successful. Please sign in.");
        }
        if ("account-status".equals(req.getParameter("error"))) {
            req.setAttribute("error", "Your session expired or your account changed. Please sign in again.");
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("username".equals(cookie.getName())) {
                    try {
                        req.setAttribute("enteredUsername", java.net.URLDecoder.decode(cookie.getValue(), java.nio.charset.StandardCharsets.UTF_8));
                    } catch (IllegalArgumentException ignored) {
                        /* Ignore malformed old cookie. */ }
                }
            }
        }
        req.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);
        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }
        String username = AuthSupport.text(req.getParameter("username")), password = req.getParameter("password");
        req.setAttribute("enteredUsername", username);
        if (username.isEmpty() || username.length() > 100 || password == null || password.isEmpty() || password.length() > 128) {
            fail(req, res, "Enter a valid username and password.");
            return;
        }
        if (!AuthRateLimiter.allow("login-ip:" + req.getRemoteAddr(), 50, 900000)
                || !AuthRateLimiter.allow("login-user:" + username.toLowerCase(Locale.ROOT), 10, 900000)) {
            res.setStatus(429);
            fail(req, res, "Too many attempts. Try again in 15 minutes.");
            return;
        }
        UserDAO.AuthenticationResult result = new UserDAO().authenticate(username, password);
        if (!result.isSuccess()) {
            String message = UserDAO.LOGIN_ERROR.equals(result.getStatus()) ? "Login is temporarily unavailable. Try again later." : "Invalid credentials or account unavailable.";
            fail(req, res, message);
            return;
        }
        User user = result.getUser();
        Customer customer = null;
        if ("customer".equalsIgnoreCase(user.getRole())) {
            customer = new CustomerDAO().getCustomerByUserID(user.getId());
            if (customer == null) {
                fail(req, res, "Customer profile could not be loaded.");
                return;
            }
        }
        HttpSession session = req.getSession();
        // Change the ID while retaining safe search dates stored by RoomServlet.
        req.changeSessionId();
        AuthSupport.clearRecovery(session);
        session.removeAttribute("customer");
        session.setMaxInactiveInterval(1800);
        session.setAttribute("loggedUser", user);
        if (customer != null) {
            session.setAttribute("customer", customer);
        }
        session.removeAttribute("csrfToken");
        AuthSupport.token(session);
        // Remember username only; never create an automatic-login cookie.
        AuthSupport.remember(req, res, "on".equals(req.getParameter("remember")) ? username : null);
        String target = "/";
        if ("customer".equalsIgnoreCase(user.getRole())) {
            Object pending = session.getAttribute("pendingBookingUrl");
            if (pending instanceof String && ((String) pending).matches("/booking\\?roomNumber=[0-9]+")) {
                target = (String) pending;
            }
        } else if ("staff".equalsIgnoreCase(user.getRole())) {
            target = "/customers";
        } else if ("admin".equalsIgnoreCase(user.getRole())) {
            target = "/room";
        } else if ("manager".equalsIgnoreCase(user.getRole())) {
            target = "/dashboard";
        }
        session.removeAttribute("pendingBookingUrl");
        res.sendRedirect(req.getContextPath() + target);
    }

    private void fail(HttpServletRequest req, HttpServletResponse res, String error) throws ServletException, IOException {
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(req, res);
    }
}
