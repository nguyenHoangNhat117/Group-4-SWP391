package controller;

import dao.CustomerDAO;
import dao.UserDAO;
import model.Customer;
import model.User;
import util.AuthRateLimiter;
import util.AuthSupport;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

// Updated by Nguyen Hoang Nhat
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        AuthSupport.noStore(res);
        AuthSupport.token(req.getSession());

        if ("reset".equals(req.getParameter("success"))) {
            req.setAttribute(
                    "message",
                    "Password reset successful. Please sign in."
            );
        }

        if ("account-status".equals(req.getParameter("error"))) {
            req.setAttribute(
                    "error",
                    "Your session expired or your account changed. "
                    + "Please sign in again."
            );
        }

        // Restore only the remembered username, never a password.
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("username".equals(cookie.getName())) {
                    try {
                        String remembered = URLDecoder.decode(
                                cookie.getValue(),
                                StandardCharsets.UTF_8
                        );

                        if (remembered.length() <= 100) {
                            req.setAttribute(
                                    "enteredUsername", remembered
                            );
                            req.setAttribute(
                                    "rememberChecked", Boolean.TRUE
                            );
                        }

                    } catch (IllegalArgumentException ignored) {
                        // Ignore malformed cookie values.
                    }

                    break;
                }
            }
        }

        req.getRequestDispatcher(
                "/WEB-INF/login/login.jsp"
        ).forward(req, res);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);

        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }

        String identifier = AuthSupport.text(
                req.getParameter("username")
        );

        // Do not trim the password or apply new-password complexity rules.
        String password = req.getParameter("password");

        boolean remember = "on".equals(req.getParameter("remember"));

        req.setAttribute("enteredUsername", identifier);
        req.setAttribute("rememberChecked", remember);

        /*
         * Keep a separate request limit by IP.
         * The five-failure account limit is enforced by UserDAO.
         */
        if (!AuthRateLimiter.allow(
                "login-ip:" + req.getRemoteAddr(),
                50,
                15 * 60 * 1000L
        )) {
            res.setStatus(429);

            fail(req, res,
                    "Too many login requests. Please try again later.");
            return;
        }

        if (identifier.isEmpty()
                || identifier.length() > 100
                || identifier.chars().anyMatch(Character::isISOControl)
                || password == null
                || password.isEmpty()
                || password.length() > 128) {

            fail(req, res,
                    "Enter a valid username or email and password.");
            return;
        }

        UserDAO.AuthenticationResult result
                = new UserDAO().authenticate(identifier, password);

        if (!result.isSuccess()) {
            if (UserDAO.LOGIN_RATE_LIMITED.equals(result.getStatus())) {
                res.setStatus(429);

                fail(req, res,
                        "Too many failed login attempts. "
                        + "Login is temporarily blocked. "
                        + "Please try again after the 15-minute block.");
                return;
            }

            if (UserDAO.LOGIN_ERROR.equals(result.getStatus())) {
                res.setStatus(503);

                fail(req, res,
                        "Login is temporarily unavailable. "
                        + "Please try again later.");
                return;
            }

            // Use one message for unknown, locked, or inactive accounts.
            fail(req, res,
                    "Invalid credentials or account unavailable.");
            return;
        }

        User user = result.getUser();
        Customer customer = null;

        if ("customer".equalsIgnoreCase(user.getRole())) {
            try {
                customer = new CustomerDAO()
                        .getCustomerByUserID(user.getId());

            } catch (RuntimeException e) {
                log("Customer profile lookup failed", e);
                res.setStatus(503);

                fail(req, res,
                        "Customer profile is temporarily unavailable.");
                return;
            }

            if (customer == null) {
                fail(req, res,
                        "Customer profile could not be loaded.");
                return;
            }
        }

        HttpSession session = req.getSession();

        // Rotate the session ID while retaining pending search dates.
        req.changeSessionId();

        AuthSupport.clearRecovery(session);

        session.removeAttribute("customer");
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("loggedUser", user);

        if (customer != null) {
            session.setAttribute("customer", customer);
        }

        // Replace the CSRF token after authentication.
        session.removeAttribute("csrfToken");
        AuthSupport.token(session);

        /*
         * Even when the user signs in with email, remember the actual
         * username so the existing cookie retains its original purpose.
         */
        AuthSupport.remember(
                req,
                res,
                remember ? user.getUsername() : null
        );

        String target = "/";
        if ("customer".equalsIgnoreCase(user.getRole())) {
            Object pending = session.getAttribute("pendingBookingUrl");
            if (pending instanceof String) {
                String pendingUrl = ((String) pending).trim();
                // Nếu Filter lưu "/booking?roomNumber=..." -> Tự động đổi thành "/details?roomNumber=..."
                if (pendingUrl.matches("/booking\\?roomNumber=[0-9]+")) {
                    target = pendingUrl.replaceFirst("^/booking", "/details");
                } else if (pendingUrl.matches("/details\\?roomNumber=[0-9]+")
                        || pendingUrl.matches("/checkout\\?draft=[0-9a-f-]{36}")) {
                    target = pendingUrl;
                }
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

    private void fail(
            HttpServletRequest req,
            HttpServletResponse res,
            String error
    ) throws ServletException, IOException {

        req.setAttribute("error", error);

        req.getRequestDispatcher(
                "/WEB-INF/login/login.jsp"
        ).forward(req, res);
    }
}
