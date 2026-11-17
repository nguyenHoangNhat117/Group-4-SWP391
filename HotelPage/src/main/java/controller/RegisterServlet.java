package controller;

import dao.UserDAO;
import util.AuthRateLimiter;
import util.AuthSupport;
import util.AuthUcPolicy;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        AuthSupport.noStore(res);

        HttpSession session = req.getSession();
        AuthSupport.token(session);

        
        if ("1".equals(req.getParameter("success"))) {
            synchronized (session) {
                Object message = session.getAttribute("registrationSuccess");

                if (message instanceof String) {
                    req.setAttribute("registrationSuccess", message);
                    session.removeAttribute("registrationSuccess");
                }
            }
        }

        show(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);

        HttpSession session = req.getSession(false);

        if (session == null) {
            res.sendError(403, "Form expired. Reload the page and try again.");
            return;
        }

        try {
            if (!AuthSupport.checkCsrf(req, res)) {
                return;
            }

            // Đếm cả request sai định dạng, trước khi gọi database.
            if (!AuthRateLimiter.allow(
                    "register:" + req.getRemoteAddr(), 10, 900000L)) {
                res.setStatus(429);
                fail(req, res, "Too many requests. Try again later.");
                return;
            }

            String username = AuthSupport.text(req.getParameter("username"));
            String email = AuthSupport.text(req.getParameter("email"));
            String firstName = AuthSupport.text(req.getParameter("firstName"));
            String lastName = AuthSupport.text(req.getParameter("lastName"));
            String phone = AuthSupport.text(req.getParameter("phone"));

            String password = req.getParameter("password");
            String confirm = req.getParameter("confirmPassword");

            req.setAttribute("enteredUsername", username);
            req.setAttribute("enteredEmail", email);
            req.setAttribute("enteredFirstName", firstName);
            req.setAttribute("enteredLastName", lastName);
            req.setAttribute("enteredPhone", phone);

            if (!AuthSupport.validUsername(username)) {
                fail(req, res,
                        "Username must contain 3 to 50 characters "
                        + "without control characters.");
                return;
            }

            if (!validName(firstName) || !validName(lastName)
                    || (lastName + " " + firstName).length() > 100) {
                fail(req, res,
                        "First name and last name are required, "
                        + "with a maximum of 50 characters each "
                        + "and 100 characters for the full name.");
                return;
            }

            if (!AuthUcPolicy.validEmail(email)) {
                fail(req, res,
                        "Enter a valid email address "
                        + "with a maximum of 100 characters.");
                return;
            }

            if (!AuthSupport.validPhone(phone)) {
                fail(req, res, "Phone number must contain 10 or 11 digits.");
                return;
            }

            if (!AuthSupport.validPassword(password)) {
                fail(req, res,
                        "Password must contain 8 to 128 characters, "
                        + "including uppercase, lowercase, a number, "
                        + "and a special character, without spaces.");
                return;
            }

            if (!password.equals(confirm)) {
                fail(req, res, "Passwords do not match.");
                return;
            }

            UserDAO dao = new UserDAO();

            if (dao.isRegistrationIdentifierTaken(username)) {
                fail(req, res, "Username is already registered.");
                return;
            }

            if (dao.isRegistrationIdentifierTaken(email)) {
                fail(req, res, "Email is already registered.");
                return;
            }

            if (!dao.register(
                    username, password, email, firstName, lastName, phone)) {
                fail(req, res,
                        "Registration failed. The username or email "
                        + "may have just been registered, "
                        + "or the service is temporarily unavailable.");
                return;
            }

        } catch (IllegalStateException ex) {
            /*
             * Bao gồm DB không khả dụng hoặc session hết hạn.
             * Không báo thất bại đăng ký nếu DB đã commit:
             * phần lưu thông báo thành công nằm bên ngoài khối này.
             */
            log("Registration could not be completed", ex);
            res.setStatus(503);
            fail(req, res,
                    "Registration temporarily unavailable. "
                    + "Please try again later.");
            return;
        }

        /*
         * Đến đây database đã commit.
         * Nếu session cũ vừa hết hạn, tạo session mới để chuyển thông báo.
         */
        try {
            session.setAttribute(
                    "registrationSuccess",
                    "Registration successful. Please sign in.");
        } catch (IllegalStateException ex) {
            req.getSession(true).setAttribute(
                    "registrationSuccess",
                    "Registration successful. Please sign in.");
        }

        res.sendRedirect(req.getContextPath() + "/register?success=1");
    }

    private boolean validName(String name) {
        return name != null
                && !name.isBlank()
                && name.length() <= 50
                && name.chars().noneMatch(Character::isISOControl);
    }

    private void show(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher(
                "/WEB-INF/register/register.jsp").forward(req, res);
    }

    private void fail(HttpServletRequest req, HttpServletResponse res,
            String error) throws ServletException, IOException {
        req.setAttribute("error", error);
        show(req, res);
    }
}