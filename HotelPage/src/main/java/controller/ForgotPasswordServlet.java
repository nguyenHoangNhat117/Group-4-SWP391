package controller;

import dao.UserDAO;
import model.User;
import util.AuthRateLimiter;
import util.AuthSupport;
import util.EmailUtility;
import util.PasswordRecovery;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Locale;

// Updated by Nguyen Hoang Nhat
@WebServlet(
        name = "ForgotPasswordServlet",
        urlPatterns = "/forgot-password"
)
public class ForgotPasswordServlet extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        AuthSupport.noStore(res);
        AuthSupport.token(req.getSession());

        req.getRequestDispatcher(
                "/WEB-INF/login/forgot_password.jsp"
        ).forward(req, res);
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
            res.sendError(403, "Session expired. Reload the page.");
            return;
        }

        try {
            /*
             * Serialize recovery changes in the same session.
             * Resend, verification, and reset use this same lock.
             */
            synchronized (session) {
                if (!AuthSupport.checkCsrf(req, res)) {
                    return;
                }

                String email = AuthSupport.text(
                        req.getParameter("email")
                );

                req.setAttribute("enteredEmail", email);

                if (!AuthSupport.validEmail(email)) {
                    fail(req, res,
                            "Enter a valid email address "
                            + "with a maximum of 100 characters.");
                    return;
                }

                if (!AuthRateLimiter.allow(
                        "otp-ip:" + req.getRemoteAddr(),
                        10,
                        15 * 60 * 1000L
                ) || !AuthRateLimiter.allow(
                        "otp-email:" + email.toLowerCase(Locale.ROOT),
                        3,
                        15 * 60 * 1000L
                )) {
                    res.setStatus(429);

                    fail(req, res,
                            "Too many OTP requests. "
                            + "Please try again later.");
                    return;
                }

                // Invalidate the previous challenge before issuing another.
                Object previous = session.getAttribute(
                        "passwordRecovery"
                );

                if (previous instanceof PasswordRecovery) {
                    ((PasswordRecovery) previous).consume();
                }

                AuthSupport.clearRecovery(session);

                User user;

                try {
                    user = new UserDAO().getUserByEmail(email);

                } catch (IllegalStateException e) {
                    log("Password recovery database unavailable", e);
                    res.setStatus(503);

                    fail(req, res,
                            "Password recovery is temporarily unavailable.");
                    return;
                }

                String otp = String.format(
                        Locale.ROOT,
                        "%06d",
                        RANDOM.nextInt(1000000)
                );

                if (user != null) {
                    try {
                        EmailUtility.sendOtpEmail(email, otp);

                    } catch (MessagingException e) {
                        // Never log the OTP or SMTP credentials.
                        log("OTP email delivery failed: "
                                + e.getClass().getSimpleName());

                        res.setStatus(503);

                        fail(req, res,
                                "Email delivery is temporarily unavailable. "
                                + "Please try again later.");
                        return;
                    }
                }

                /*
                 * Unknown emails get a dummy challenge and the same
                 * normal response without receiving an email.
                 */
                PasswordRecovery state = new PasswordRecovery(
                        user == null ? -1 : user.getId(),
                        user == null ? "" : user.getPassword(),
                        otp,
                        System.currentTimeMillis()
                );

                session.setAttribute("passwordRecovery", state);
                session.setAttribute("email", email);
            }

        } catch (IllegalStateException e) {
            res.sendError(
                    403,
                    "Session expired. Start password recovery again."
            );
            return;
        }

        // Redirect so refreshing the OTP page does not send another email.
        res.sendRedirect(req.getContextPath() + "/verify-otp?sent=1");
    }

    private void fail(
            HttpServletRequest req,
            HttpServletResponse res,
            String error
    ) throws ServletException, IOException {

        req.setAttribute("error", error);

        req.getRequestDispatcher(
                "/WEB-INF/login/forgot_password.jsp"
        ).forward(req, res);
    }
}
