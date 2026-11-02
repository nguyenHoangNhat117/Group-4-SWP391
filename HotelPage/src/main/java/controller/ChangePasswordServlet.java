package controller;

import dao.ChangePasswordDAO;
import dao.UserDAO;
import model.User;
import util.AuthSupport;
import util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

// Updated by Nguyen Hoang Nhat
@WebServlet(
        name = "ChangePasswordServlet",
        urlPatterns = "/change-password"
)
public class ChangePasswordServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        AuthSupport.noStore(res);

        User user = loggedUser(req, res);

        if (user == null) {
            return;
        }

        AuthSupport.token(req.getSession(false));

        req.getRequestDispatcher(
                "/WEB-INF/login/change_password.jsp"
        ).forward(req, res);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);

        User sessionUser = loggedUser(req, res);

        if (sessionUser == null) {
            return;
        }

        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }

        // Read passwords exactly as entered, without trimming.
        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        if (currentPassword == null
                || currentPassword.isEmpty()
                || currentPassword.length() > 128) {

            fail(req, res, "Enter your current password.");
            return;
        }

        if (!AuthSupport.validPassword(newPassword)) {
            fail(req, res,
                    "New password must contain 8 to 128 characters, "
                    + "including uppercase, lowercase, a number, "
                    + "and a special character, without spaces.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            fail(req, res, "Password confirmation does not match.");
            return;
        }

        User currentUser;

        try {
            // Always identify the account using the authenticated session.
            currentUser = new UserDAO().getUserById(
                    sessionUser.getId()
            );

        } catch (IllegalStateException e) {
            log("Unable to load account for password change", e);
            res.setStatus(503);

            fail(req, res,
                    "Account service is temporarily unavailable.");
            return;
        }

        if (currentUser == null
                || !"active".equalsIgnoreCase(
                        currentUser.getAccountStatus()
                )
                || !supportedRole(currentUser.getRole())
                || !Objects.equals(
                        currentUser.getPassword(),
                        sessionUser.getPassword()
                )) {

            endSession(req, res);

            res.sendRedirect(
                    req.getContextPath()
                    + "/login?error=account-status"
            );
            return;
        }

        // Existing passwords may be weaker than the new-password policy.
        if (!PasswordUtil.verify(
                currentPassword,
                currentUser.getPassword()
        )) {
            fail(req, res, "Current password is incorrect.");
            return;
        }

        if (PasswordUtil.verify(
                newPassword,
                currentUser.getPassword()
        )) {
            fail(req, res,
                    "New password must be different "
                    + "from the current password.");
            return;
        }

        // Keep the existing MD5 hashing implementation.
        String newHash = PasswordUtil.hash(newPassword);

        boolean changed;

        try {
            changed = new ChangePasswordDAO().changePassword(
                    currentUser.getId(),
                    currentUser.getPassword(),
                    currentUser.getRole(),
                    newHash
            );

        } catch (SQLException e) {
            log("Password change database error", e);
            res.setStatus(503);

            fail(req, res,
                    "Password change is temporarily unavailable. "
                    + "Please try again later.");
            return;
        }

        if (!changed) {
            // Another request may have changed the password or account state.
            endSession(req, res);

            res.sendRedirect(
                    req.getContextPath()
                    + "/login?error=account-status"
            );
            return;
        }

        // Require a fresh login with the new password.
        endSession(req, res);

        res.sendRedirect(req.getContextPath() + "/login");
    }

    private User loggedUser(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws IOException {

        HttpSession session = req.getSession(false);

        Object value = session == null
                ? null
                : session.getAttribute("loggedUser");

        if (!(value instanceof User)) {
            res.sendRedirect(req.getContextPath() + "/login");
            return null;
        }

        User user = (User) value;

        if (!supportedRole(user.getRole())) {
            res.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You cannot access this function."
            );
            return null;
        }

        return user;
    }

    private boolean supportedRole(String role) {
        return "customer".equalsIgnoreCase(role)
                || "staff".equalsIgnoreCase(role)
                || "admin".equalsIgnoreCase(role)
                || "manager".equalsIgnoreCase(role);
    }

    private void endSession(
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        HttpSession session = req.getSession(false);

        if (session != null) {
            try {
                session.invalidate();

            } catch (IllegalStateException ignored) {
                // Another request has already ended the session.
            }
        }

        AuthSupport.remember(req, res, null);

        SessionCookieConfig config
                = getServletContext().getSessionCookieConfig();

        String name = config.getName();

        if (name == null || name.isEmpty()) {
            name = "JSESSIONID";
        }

        String path = config.getPath();

        if (path == null || path.isEmpty()) {
            path = req.getContextPath().isEmpty()
                    ? "/"
                    : req.getContextPath();
        }

        Cookie cookie = new Cookie(name, "");

        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(req.isSecure() || config.isSecure());
        cookie.setAttribute("SameSite", "Lax");

        if (config.getDomain() != null
                && !config.getDomain().isEmpty()) {
            cookie.setDomain(config.getDomain());
        }

        res.addCookie(cookie);
    }

    private void fail(
            HttpServletRequest req,
            HttpServletResponse res,
            String error
    ) throws ServletException, IOException {

        // Never put submitted passwords into request or session attributes.
        req.setAttribute("error", error);

        req.getRequestDispatcher(
                "/WEB-INF/login/change_password.jsp"
        ).forward(req, res);
    }
}
