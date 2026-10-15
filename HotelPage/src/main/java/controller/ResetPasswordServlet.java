package controller;

import dao.UserDAO;
import util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "ResetPasswordServlet", urlPatterns = "/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AuthSupport.noStore(res);
        PasswordRecovery state = VerifyOtpServlet.recovery(req);
        if (state == null || !state.canReset(System.currentTimeMillis())) {
            res.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/login/reset_password.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);
        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }
        PasswordRecovery state = VerifyOtpServlet.recovery(req);
        if (state == null || !state.canReset(System.currentTimeMillis())) {
            res.sendError(403, "Verify a new OTP before resetting your password.");
            return;
        }
        String password = req.getParameter("password");
        if (!AuthSupport.validPassword(password)) {
            fail(req, res, "Password must contain 7 to 128 characters.");
            return;
        }
        if (!password.equals(req.getParameter("confirmPassword"))) {
            fail(req, res, "Passwords do not match.");
            return;
        }
        int changed;
        synchronized (state) {
            // Hash before the final deadline check; compare-and-update is atomic in SQL.
            String hash = PasswordUtil.hash(password);
            if (!state.canReset(System.currentTimeMillis())) {
                res.sendError(403, "Reset permission expired.");
                return;
            }
            changed = new UserDAO().resetPassword(state.userId, state.expectedPasswordHash, hash);
            // Consume even after a failed DB attempt; a new OTP is required to retry.
            state.consume();
        }
        AuthSupport.clearRecovery(req.getSession(false));
        if (changed != 1) {
            req.setAttribute("error", "Password was not changed. Request a new OTP and try again.");
            req.getRequestDispatcher("/WEB-INF/login/forgot_password.jsp").forward(req, res);
            return;
        }
        req.getSession(false).invalidate();
        AuthSupport.remember(req, res, null);
        res.sendRedirect(req.getContextPath() + "/login?success=reset");
    }

    private void fail(HttpServletRequest req, HttpServletResponse res, String message) throws ServletException, IOException {
        req.setAttribute("error", message);
        req.getRequestDispatcher("/WEB-INF/login/reset_password.jsp").forward(req, res);
    }
}
