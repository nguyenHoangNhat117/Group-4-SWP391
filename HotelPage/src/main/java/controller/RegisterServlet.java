package controller;

import dao.UserDAO;
import util.AuthRateLimiter;
import util.AuthSupport;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {

        AuthSupport.noStore(res);
        AuthSupport.token(req.getSession());

        req.getRequestDispatcher(
                "/WEB-INF/register/register.jsp"
        ).forward(req, res);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);

        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }

        String username =
                AuthSupport.text(req.getParameter("username"));
        String email =
                AuthSupport.text(req.getParameter("email"));
        String firstName =
                AuthSupport.text(req.getParameter("firstName"));
        String lastName =
                AuthSupport.text(req.getParameter("lastName"));
        String phone =
                AuthSupport.text(req.getParameter("phone"));

        // Không trim và không đưa mật khẩu trở lại form.
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        // Giữ thông tin thường khi form có lỗi.
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

        if (!validName(firstName) || !validName(lastName)) {
            fail(req, res,
                    "First name and last name are required, "
                    + "with a maximum of 50 characters each "
                    + "and 100 characters for the full name.");
            return;
        }

        String displayName = lastName + " " + firstName;

        if (displayName.length() > 100) {
            fail(req, res,
                    "Full name must not exceed 100 characters.");
            return;
        }

        if (!AuthSupport.validEmail(email)) {
            fail(req, res,
                    "Enter a valid email address "
                    + "with a maximum of 100 characters.");
            return;
        }

        if (!AuthSupport.validPhone(phone)) {
            fail(req, res,
                    "Phone number must contain 10 or 11 digits.");
            return;
        }

        if (!AuthSupport.validPassword(password)) {
            fail(req, res,
                    "Password must contain 8 to 128 characters, "
                    + "including uppercase, lowercase, a number, "
                    + "and a special character, without spaces.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            fail(req, res, "Passwords do not match.");
            return;
        }

        if (!AuthRateLimiter.allow(
                "register:" + req.getRemoteAddr(),
                10,
                900000)) {

            res.setStatus(429);
            fail(req, res, "Too many requests. Try again later.");
            return;
        }

        UserDAO dao = new UserDAO();

        try {
            // Kiểm tra username với cả username/email đã tồn tại.
            if (dao.isRegistrationIdentifierTaken(username)) {
                fail(req, res, "Username is already registered.");
                return;
            }

            // Kiểm tra email với cả username/email đã tồn tại.
            if (dao.isRegistrationIdentifierTaken(email)) {
                fail(req, res, "Email is already registered.");
                return;
            }

            boolean registered = dao.register(
                    username,
                    password,
                    email,
                    firstName,
                    lastName,
                    phone);

            if (!registered) {
                fail(req, res,
                        "Registration failed. The username or email "
                        + "may have just been registered, "
                        + "or the service is temporarily unavailable.");
                return;
            }

        } catch (IllegalStateException ex) {
            log("Registration unavailable", ex);

            fail(req, res,
                    "Registration temporarily unavailable. "
                    + "Please try again later.");
            return;
        }

        req.getSession().setAttribute(
                "registrationSuccess",
                "Registration successful. Please sign in.");

        // Chuyển trang sau thành công, tránh F5 gửi lại POST.
        res.sendRedirect(
                req.getContextPath() + "/register?success=1");
    }

    private boolean validName(String name) {
        return name != null
                && !name.isBlank()
                && name.length() <= 50
                && name.chars().noneMatch(Character::isISOControl);
    }

    private void fail(
            HttpServletRequest req,
            HttpServletResponse res,
            String error)
            throws ServletException, IOException {

        req.setAttribute("error", error);

        req.getRequestDispatcher(
                "/WEB-INF/register/register.jsp"
        ).forward(req, res);
    }
}