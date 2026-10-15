package controller;

import dao.UserDAO;
import util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AuthSupport.noStore(res);
        AuthSupport.token(req.getSession());
        req.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);
        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }
        String username = AuthSupport.text(req.getParameter("username")), email = AuthSupport.text(req.getParameter("email"));
        String password = req.getParameter("password");
        req.setAttribute("enteredUsername", username);
        req.setAttribute("enteredEmail", email);
        if (username.length() < 3 || username.length() > 100 || username.chars().anyMatch(Character::isISOControl)) {
            fail(req, res, "Username must contain 3 to 100 characters without control characters.");
            return;
        }
        if (!AuthSupport.validEmail(email)) {
            fail(req, res, "Enter a valid email address.");
            return;
        }
        if (!AuthSupport.validPassword(password)) {
            fail(req, res, "Password must contain 7 to 128 characters.");
            return;
        }
        if (!password.equals(req.getParameter("confirmPassword"))) {
            fail(req, res, "Passwords do not match.");
            return;
        }
        if (!AuthRateLimiter.allow("register:" + req.getRemoteAddr(), 10, 900000)) {
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
            if (!dao.register(username, password, email)) {
                fail(req, res, "Registration failed. Please retry later.");
                return;
            }
        } catch (IllegalStateException e) {
            log("Registration unavailable", e);
            fail(req, res, "Registration temporarily unavailable.");
            return;
        }
        req.setAttribute("message", "Registration successful. Please sign in.");
        req.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(req, res);
    }

    private void fail(HttpServletRequest req, HttpServletResponse res, String error) throws ServletException, IOException {
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(req, res);
    }
}
