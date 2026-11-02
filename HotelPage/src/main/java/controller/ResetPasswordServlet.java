package controller;

import dao.UserDAO;
import util.AuthSupport;
import util.PasswordRecovery;
import util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Updated by Nguyen Hoang Nhat
@WebServlet(
        name = "ResetPasswordServlet",
        urlPatterns = "/reset-password"
)
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        AuthSupport.noStore(res);

        HttpSession session = req.getSession(false);

        if (session == null) {
            res.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        try {
            synchronized (session) {
                PasswordRecovery state = VerifyOtpServlet.recovery(req);

                if (state == null
                        || !state.canReset(System.currentTimeMillis())) {

                    res.sendRedirect(
                            req.getContextPath() + "/forgot-password"
                    );
                    return;
                }

                req.getRequestDispatcher(
                        "/WEB-INF/login/reset_password.jsp"
                ).forward(req, res);
            }

        } catch (IllegalStateException e) {
            res.sendError(
                    403,
                    "Session expired. Verify a new OTP."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);

        HttpSession session = req.getSession(false);

        if (session == null) {
            res.sendError(403, "Verify a new OTP before resetting.");
            return;
        }

        try {
            synchronized (session) {
                if (!AuthSupport.checkCsrf(req, res)) {
                    return;
                }

                PasswordRecovery state = VerifyOtpServlet.recovery(req);

                if (state == null
                        || !state.canReset(System.currentTimeMillis())) {

                    res.sendError(
                            403,
                            "Verify a new OTP before resetting your password."
                    );
                    return;
                }

                // Do not trim passwords or store them in request attributes.
                String password = req.getParameter("password");
                String confirmPassword
                        = req.getParameter("confirmPassword");

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

                int changed;

                synchronized (state) {
                    // Preserve the current MD5 implementation.
                    String hash = PasswordUtil.hash(password);

                    if (!state.canReset(System.currentTimeMillis())) {
                        res.sendError(
                                403,
                                "Reset permission expired. Request a new OTP."
                        );
                        return;
                    }

                    /*
                     * SQL checks the expected old hash and active status.
                     * Concurrent resets cannot overwrite a newer password.
                     */
                    changed = new UserDAO().resetPassword(
                            state.userId,
                            state.expectedPasswordHash,
                            hash
                    );

                    // Each verified recovery allows one database attempt.
                    state.consume();
                }

                AuthSupport.clearRecovery(session);

                if (changed != 1) {
                    req.setAttribute(
                            "error",
                            "Password was not changed. "
                            + "Request a new OTP and try again."
                    );

                    req.getRequestDispatcher(
                            "/WEB-INF/login/forgot_password.jsp"
                    ).forward(req, res);
                    return;
                }

                // End this recovery session after a successful reset.
                session.invalidate();
                AuthSupport.remember(req, res, null);
            }

        } catch (IllegalStateException e) {
            res.sendError(
                    403,
                    "Session expired. Start password recovery again."
            );
            return;
        }

        res.sendRedirect(
                req.getContextPath() + "/login?success=reset"
        );
    }

    private void fail(
            HttpServletRequest req,
            HttpServletResponse res,
            String error
    ) throws ServletException, IOException {

        req.setAttribute("error", error);

        req.getRequestDispatcher(
                "/WEB-INF/login/reset_password.jsp"
        ).forward(req, res);
    }
}
