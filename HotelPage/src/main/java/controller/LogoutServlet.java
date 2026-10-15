package controller;

import util.AuthSupport;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    // Existing header links still work: GET opens confirmation, POST logs out.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AuthSupport.noStore(res);
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            AuthSupport.remember(req, res, null);
            res.sendRedirect(req.getContextPath() + "/");
            return;
        }
        AuthSupport.token(session);
        req.getRequestDispatcher("/WEB-INF/login/logout.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        AuthSupport.noStore(res);
        if (!AuthSupport.checkCsrf(req, res)) {
            return;
        }
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        AuthSupport.remember(req, res, null);
        res.sendRedirect(req.getContextPath() + "/");
    }
}
