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
import java.util.Objects;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {

        AuthSupport.noStore(res);

        // Đã đăng nhập hợp lệ thì trở về trang của tài khoản hiện tại.
        if (handleExistingLogin(req, res)) {
            return;
        }

        AuthSupport.token(req.getSession());

        if ("reset".equals(req.getParameter("success"))) {
            req.setAttribute(
                    "message",
                    "Password reset successful. Please sign in.");
        }

        if ("account-status".equals(req.getParameter("error"))) {
            req.setAttribute(
                    "error",
                    "Your session expired or your account changed. "
                    + "Please sign in again.");
        }

        // Chỉ khôi phục username đã nhớ, không khôi phục mật khẩu.
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("username".equals(cookie.getName())) {
                    try {
                        String remembered = URLDecoder.decode(
                                cookie.getValue(),
                                StandardCharsets.UTF_8);

                        if (remembered.length() <= 100
                                && remembered.chars()
                                        .noneMatch(Character::isISOControl)) {

                            req.setAttribute(
                                    "enteredUsername",
                                    remembered);

                            req.setAttribute(
                                    "rememberChecked",
                                    Boolean.TRUE);
                        }

                    } catch (IllegalArgumentException ignored) {
                        // Cookie sai định dạng không làm lỗi trang login.
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
            HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);

        // Form cũ không được ghi đè tài khoản đang đăng nhập.
        if (handleExistingLogin(req, res)) {
            return;
        }

        HttpSession loginSession = req.getSession(false);

        try {
            if (!AuthSupport.checkCsrf(req, res)) {
                return;
            }
        } catch (IllegalStateException ex) {
            expiredForm(res);
            return;
        }

        String identifier = AuthSupport.text(
                req.getParameter("username"));

        // Không trim mật khẩu hoặc áp dụng quy tắc mật khẩu mới.
        String password = req.getParameter("password");

        boolean remember =
                "on".equals(req.getParameter("remember"));

        req.setAttribute("enteredUsername", identifier);
        req.setAttribute("rememberChecked", remember);

        // Giới hạn yêu cầu theo IP.
        // Giới hạn 5 lần sai theo tài khoản vẫn do UserDAO xử lý.
        if (!AuthRateLimiter.allow(
                "login-ip:" + req.getRemoteAddr(),
                50,
                15 * 60 * 1000L)) {

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

        UserDAO.AuthenticationResult result =
                new UserDAO().authenticate(identifier, password);

        if (!result.isSuccess()) {
            if (UserDAO.LOGIN_RATE_LIMITED.equals(
                    result.getStatus())) {

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

            // Dùng chung thông báo cho tài khoản sai/khóa/ngừng hoạt động.
            fail(req, res,
                    "Invalid credentials or account unavailable.");
            return;
        }

        User user = result.getUser();
        Customer customer = null;

        // Customer phải có hồ sơ khách trước khi tạo trạng thái đăng nhập.
        if ("customer".equalsIgnoreCase(user.getRole())) {
            try {
                customer = new CustomerDAO()
                        .getCustomerByUserID(user.getId());

            } catch (RuntimeException ex) {
                log("Customer profile lookup failed", ex);
                res.setStatus(503);

                fail(req, res,
                        "Customer profile is temporarily unavailable.");
                return;
            }

            if (customer == null) {
                res.setStatus(503);

                fail(req, res,
                        "Customer profile could not be loaded.");
                return;
            }
        }

        if (loginSession == null) {
            expiredForm(res);
            return;
        }

        try {
            /*
             * Đồng bộ bước xác lập đăng nhập:
             * hai tab gửi đồng thời không ghi đè tài khoản của nhau.
             */
            synchronized (loginSession) {
                // Phát hiện session bị hủy trong lúc xác thực.
                loginSession.getAttribute("loggedUser");

                if (handleExistingLogin(req, res)) {
                    return;
                }

                // Đổi session ID nhưng giữ thông tin tìm phòng của Guest.
                req.changeSessionId();

                AuthSupport.clearRecovery(loginSession);
                loginSession.removeAttribute("customer");
                loginSession.setMaxInactiveInterval(30 * 60);
                loginSession.setAttribute("loggedUser", user);

                if (customer != null) {
                    loginSession.setAttribute("customer", customer);
                }

                // Đổi token form sau khi đăng nhập.
                loginSession.removeAttribute("csrfToken");
                AuthSupport.token(loginSession);

                // Nhớ username thực tế kể cả khi đăng nhập bằng email.
                AuthSupport.remember(
                        req,
                        res,
                        remember ? user.getUsername() : null);

                String target = homeForRole(user.getRole());

                // Giữ nguyên các đường quay lại đặt phòng hiện tại.
                if ("customer".equalsIgnoreCase(user.getRole())) {
                    Object pending =
                            loginSession.getAttribute("pendingBookingUrl");

                    if (pending instanceof String) {
                        String pendingUrl = ((String) pending).trim();

                        if (pendingUrl.matches(
                                "/booking\\?roomNumber=[0-9]+")) {

                            target = pendingUrl.replaceFirst(
                                    "^/booking",
                                    "/details");

                        } else if (pendingUrl.matches(
                                "/details\\?roomNumber=[0-9]+")
                                || pendingUrl.matches(
                                "/(?:payment|checkout)\\?draft=[0-9a-f-]{36}")) {

                            target = pendingUrl.replaceFirst(
                                    "^/checkout",
                                    "/payment");
                        }
                    }
                }

                loginSession.removeAttribute("pendingBookingUrl");

                res.sendRedirect(
                        req.getContextPath() + target);
            }

        } catch (IllegalStateException ex) {
            expiredForm(res);
        }
    }

    /*
     * true: đã xử lý response, caller phải dừng.
     * false: chưa đăng nhập, tiếp tục luồng login.
     */
    private boolean handleExistingLogin(
            HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session == null) {
            return false;
        }

        try {
            synchronized (session) {
                Object stored = session.getAttribute("loggedUser");

                if (!(stored instanceof User)) {
                    return false;
                }

                User previous = (User) stored;
                User current;

                try {
                    current = new UserDAO()
                            .getUserById(previous.getId());

                } catch (IllegalStateException ex) {
                    log("Unable to validate existing login", ex);
                    res.setStatus(503);

                    fail(req, res,
                            "Login is temporarily unavailable. "
                            + "Please try again later.");
                    return true;
                }

                boolean valid = current != null
                        && "active".equalsIgnoreCase(
                                current.getAccountStatus())
                        && supportedRole(current.getRole())
                        && Objects.equals(
                                previous.getRole(),
                                current.getRole())
                        && Objects.equals(
                                previous.getPassword(),
                                current.getPassword());

                if (!valid) {
                    // Xóa dữ liệu session của tài khoản không còn hợp lệ.
                    session.invalidate();

                    if ("POST".equalsIgnoreCase(req.getMethod())) {
                        expiredForm(res);
                        return true;
                    }

                    req.setAttribute(
                            "error",
                            "Your session expired or your account changed. "
                            + "Please sign in again.");
                    return false;
                }

                session.setAttribute("loggedUser", current);

                res.sendRedirect(
                        req.getContextPath()
                        + homeForRole(current.getRole()));
                return true;
            }

        } catch (IllegalStateException ex) {
            if ("POST".equalsIgnoreCase(req.getMethod())) {
                expiredForm(res);
                return true;
            }

            return false;
        }
    }

    private boolean supportedRole(String role) {
        return "customer".equalsIgnoreCase(role)
                || "staff".equalsIgnoreCase(role)
                || "admin".equalsIgnoreCase(role)
                || "manager".equalsIgnoreCase(role);
    }

    private String homeForRole(String role) {
        if ("staff".equalsIgnoreCase(role)) {
            return "/customers";
        }

        if ("admin".equalsIgnoreCase(role)) {
            return "/room";
        }

        if ("manager".equalsIgnoreCase(role)) {
            return "/dashboard";
        }

        return "/";
    }

    private void expiredForm(HttpServletResponse res)
            throws IOException {

        res.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Form expired. Reload the page and try again.");
    }

    private void fail(
            HttpServletRequest req,
            HttpServletResponse res,
            String error)
            throws ServletException, IOException {

        req.setAttribute("error", error);

        req.getRequestDispatcher(
                "/WEB-INF/login/login.jsp"
        ).forward(req, res);
    }
}