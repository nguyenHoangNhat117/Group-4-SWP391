package controller;

import util.AuthRateLimiter;
import util.AuthSupport;
import util.PasswordRecovery;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Updated by Nguyen Hoang Nhat
@WebServlet(name = "VerifyOtpServlet", urlPatterns = "/verify-otp")
public class VerifyOtpServlet extends HttpServlet {

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
                PasswordRecovery state = recovery(req);

                if (state == null) {
                    res.sendRedirect(
                            req.getContextPath() + "/forgot-password"
                    );
                    return;
                }

                if (state.canReset(System.currentTimeMillis())) {
                    res.sendRedirect(
                            req.getContextPath() + "/reset-password"
                    );
                    return;
                }

                if ("1".equals(req.getParameter("sent"))) {
                    req.setAttribute(
                            "message",
                            "If an eligible customer account exists, "
                            + "an OTP has been sent. "
                            + "It expires in 5 minutes."
                    );
                }

                showForm(req, res, state);
            }

        } catch (IllegalStateException e) {
            res.sendError(403, "Session expired. Request a new OTP.");
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
            res.sendError(403, "Session expired. Request a new OTP.");
            return;
        }

        try {
            synchronized (session) {
                if (!AuthSupport.checkCsrf(req, res)) {
                    return;
                }

                if (!AuthRateLimiter.allow(
                        "verify-ip:" + req.getRemoteAddr(),
                        30,
                        15 * 60 * 1000L
                )) {
                    res.sendError(
                            429,
                            "Too many verification requests. "
                            + "Please try again later."
                    );
                    return;
                }

                PasswordRecovery state = recovery(req);

                if (state == null) {
                    res.sendRedirect(
                            req.getContextPath() + "/forgot-password"
                    );
                    return;
                }

                // A repeated verification does not extend reset permission.
                if (state.canReset(System.currentTimeMillis())) {
                    res.sendRedirect(
                            req.getContextPath() + "/reset-password"
                    );
                    return;
                }

                String supplied = AuthSupport.text(
                        req.getParameter("otp")
                );

                if (state.verify(supplied, System.currentTimeMillis())) {
                    // Rotate the session ID after proving email ownership.
                    req.changeSessionId();

                    res.sendRedirect(
                            req.getContextPath() + "/reset-password"
                    );
                    return;
                }

                req.setAttribute(
                        "error",
                        "The OTP is incorrect, expired, "
                        + "or has reached its attempt limit."
                );

                showForm(req, res, state);
            }

        } catch (IllegalStateException e) {
            res.sendError(403, "Session expired. Request a new OTP.");
        }
    }

    private void showForm(
            HttpServletRequest req,
            HttpServletResponse res,
            PasswordRecovery state
    ) throws ServletException, IOException {

        boolean available = state.isOtpActive(
                System.currentTimeMillis()
        );

        req.setAttribute("otpAvailable", available);

        req.setAttribute(
                "remainingAttempts",
                available ? state.getRemainingAttempts() : 0
        );

        req.getRequestDispatcher(
                "/WEB-INF/login/enter_otp.jsp"
        ).forward(req, res);
    }

    static PasswordRecovery recovery(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        Object value = session.getAttribute("passwordRecovery");

        return value instanceof PasswordRecovery
                ? (PasswordRecovery) value
                : null;
    }
}
