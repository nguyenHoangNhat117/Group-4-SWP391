package controller;

import util.AuthSupport;

import jakarta.servlet.ServletException;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Updated by Nguyen Hoang Nhat
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        AuthSupport.noStore(res);

        HttpSession session = req.getSession(false);

        try {
            if (session == null
                    || session.getAttribute("loggedUser") == null) {

                invalidateSession(session);
                clearLoginCookies(req, res);

                res.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            // Pass the token without letting the JSP create a new session.
            req.setAttribute(
                    "logoutToken",
                    AuthSupport.token(session)
            );

        } catch (IllegalStateException e) {
            // Another tab may have invalidated the session already.
            clearLoginCookies(req, res);

            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.getRequestDispatcher(
                "/WEB-INF/login/logout.jsp"
        ).forward(req, res);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws IOException {

        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);

        try {
            // Reject missing or invalid tokens before changing login state.
            if (!AuthSupport.checkCsrf(req, res)) {
                return;
            }

        } catch (IllegalStateException e) {
            // An expired or concurrently invalidated session is not a 500.
            res.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Session expired. Please reload the page."
            );
            return;
        }

        invalidateSession(req.getSession(false));
        clearLoginCookies(req, res);

        res.sendRedirect(req.getContextPath() + "/login");
    }

    private void invalidateSession(HttpSession session) {
        if (session == null) {
            return;
        }

        try {
            // Remove every session attribute, including user and recovery data.
            session.invalidate();

        } catch (IllegalStateException ignored) {
            // The session was already invalidated by another request.
        }
    }

    private void clearLoginCookies(
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        // Delete the existing remembered-username cookie.
        AuthSupport.remember(req, res, null);

        SessionCookieConfig config
                = getServletContext().getSessionCookieConfig();

        String cookieName = config.getName();

        if (cookieName == null || cookieName.isEmpty()) {
            cookieName = "JSESSIONID";
        }

        String cookiePath = config.getPath();

        if (cookiePath == null || cookiePath.isEmpty()) {
            cookiePath = req.getContextPath().isEmpty()
                    ? "/"
                    : req.getContextPath();
        }

        // Use the same name, path, and domain as the session cookie.
        Cookie cookie = new Cookie(cookieName, "");

        cookie.setPath(cookiePath);
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
}
