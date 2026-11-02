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

// Updated by Nguyen Hoang Nhat
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        AuthSupport.noStore(res);
        AuthSupport.token(req.getSession());

        req.getRequestDispatcher(
                "/WEB-INF/register/register.jsp"
        ).forward(req, res);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        // Read Vietnamese text correctly before accessing parameters.
        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);

        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }

        String username = AuthSupport.text(req.getParameter("username"));
        String email = AuthSupport.text(req.getParameter("email"));
        String firstName = AuthSupport.text(req.getParameter("firstName"));
        String lastName = AuthSupport.text(req.getParameter("lastName"));
        String phone = AuthSupport.text(req.getParameter("phone"));

        // Never trim or preserve submitted passwords.
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        // Preserve non-sensitive values when validation fails.
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

        // DisplayName is limited to 100 characters in the database.
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

        // Keep the existing registration request limit.
        if (!AuthRateLimiter.allow(
                "register:" + req.getRemoteAddr(),
                10,
                900000
        )) {
            res.setStatus(429);
            fail(req, res, "Too many requests. Try again later.");
            return;
        }

        UserDAO dao = new UserDAO();

        try {
            if (dao.isUsernameExists(username)) {
                fail(req, res, "Username is already registered.");
                return;
            }

            if (dao.checkEmailExists(email)) {
                fail(req, res, "Email is already registered.");
                return;
            }

            boolean registered = dao.register(
                    username,
                    password,
                    email,
                    firstName,
                    lastName,
                    phone
            );

            if (!registered) {
                fail(req, res,
                        "Registration failed. The username or email "
                        + "may have just been registered, "
                        + "or the service is temporarily unavailable.");
                return;
            }

        } catch (IllegalStateException e) {
            log("Registration unavailable", e);

            fail(req, res,
                    "Registration temporarily unavailable. "
                    + "Please try again later.");
            return;
        }

        // Redirect after success to avoid submitting the form again.
        req.getSession().setAttribute(
                "registrationSuccess",
                "Registration successful. Please sign in."
        );

        res.sendRedirect(req.getContextPath() + "/register?success=1");
    }

    private boolean validName(String name) {
        // Allow Unicode names without imposing an ASCII-only pattern.
        return name != null
                && !name.isBlank()
                && name.length() <= 50
                && name.chars()
                        .noneMatch(Character::isISOControl);
    }

    private void fail(
            HttpServletRequest req,
            HttpServletResponse res,
            String error
    ) throws ServletException, IOException {

        req.setAttribute("error", error);

        req.getRequestDispatcher(
                "/WEB-INF/register/register.jsp"
        ).forward(req, res);
    }
}