package controller;

import dao.AdminUserDAO;
import dao.AdminUserDAO.AccountException;
import model.AdminAccount;
import model.User;
import util.AdminAccountPolicy;
import util.AuthSupport;
import util.EmailUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "AdminUserServlet", urlPatterns = "/admin/users")
public class AdminUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AuthSupport.noStore(res);
        if (actor(req, res) == null) {
            return;
        }
        AuthSupport.token(req.getSession(false));
        AdminUserDAO dao = new AdminUserDAO();
        String view = AuthSupport.text(req.getParameter("view"));
        try {
            if ("create".equals(view)) {
                AdminAccount a = new AdminAccount();
                a.setRole("customer");
                a.setStatus("active");
                req.setAttribute("account", a);
                form(req, res, true);
                return;
            }
            if ("edit".equals(view) || "detail".equals(view)) {
                Integer id = id(req.getParameter("id"));
                if (id == null) {
                    res.sendError(400);
                    return;
                }
                AdminAccount a = dao.find(id);
                if (a == null || !AdminAccountPolicy.role(a.getRole())) {
                    res.sendError(404);
                    return;
                }
                req.setAttribute("account", a);
                if ("edit".equals(view)) {
                    form(req, res, false);
                } else {
                    req.setAttribute("bookings", dao.bookings(id));
                    req.getRequestDispatcher("/WEB-INF/admin/users/detail.jsp").forward(req, res);
                }
                return;
            }
            if (!view.isEmpty() && !"list".equals(view)) {
                res.sendError(404);
                return;
            }
            String keyword = AuthSupport.text(req.getParameter("keyword"));
            String role = AuthSupport.text(req.getParameter("role"));
            String status = AuthSupport.text(req.getParameter("status"));
            if (keyword.length() > 100 || (!role.isEmpty() && !AdminAccountPolicy.role(role))
                    || (!status.isEmpty() && !AdminAccountPolicy.status(status))) {
                res.sendError(400);
                return;
            }
            Integer page = id(req.getParameter("page"));
            req.setAttribute("keyword", keyword);
            req.setAttribute("role", role);
            req.setAttribute("status", status);
            req.setAttribute("result", dao.search(keyword, role, status, page == null ? 1 : page));
            Object flash = req.getSession(false).getAttribute("uc18Message");
            req.getSession(false).removeAttribute("uc18Message");
            req.setAttribute("message", flash);
            req.getRequestDispatcher("/WEB-INF/admin/users/list.jsp").forward(req, res);
        } catch (SQLException e) {
            unavailable(req, res, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        AuthSupport.noStore(res);
        User actor = actor(req, res);
        if (actor == null || !AuthSupport.checkCsrf(req, res)) {
            return;
        }
        String action = AuthSupport.text(req.getParameter("action"));
        if (!"create".equals(action) && !"update".equals(action) && !"delete".equals(action)) {
            res.sendError(400);
            return;
        }
        Integer target = id(req.getParameter("id"));
        String version = AuthSupport.text(req.getParameter("version"));
        if (!"create".equals(action) && (target == null || !version.matches("0x[0-9A-Fa-f]{16}"))) {
            res.sendError(400);
            return;
        }
        AdminUserDAO dao = new AdminUserDAO();
        if ("delete".equals(action)) {
            try {
                dao.delete(actor.getId(), actor.getPassword(), target, version);
                flash(req, "Account deleted.");
            } catch (AccountException e) {
                flash(req, e.getMessage());
            } catch (SQLException e) {
                unavailable(req, res, e);
                return;
            }
            redirect(req, res);
            return;
        }
        boolean create = "create".equals(action);
        AdminAccount a = read(req);
        a.setId(create ? 0 : target);
        a.setVersion(version);
        String password = req.getParameter("password"); // Do not trim, log, or put passwords in request/session attributes.
        Map<String, String> errors = AdminAccountPolicy.validate(a, password, create);
        req.setAttribute("account", a);
        req.setAttribute("errors", errors);
        if (!errors.isEmpty()) {
            res.setStatus(400);
            form(req, res, create);
            return;
        }
        try {
            dao.save(actor.getId(), actor.getPassword(), a, password, create);
        } catch (AccountException e) {
            res.setStatus(409);
            req.setAttribute("error", e.getMessage());
            form(req, res, create);
            return;
        } catch (SQLException e) {
            unavailable(req, res, e);
            return;
        }
        if (create) {
            try {
                // Database commit has succeeded. SMTP failure must not roll back the account.
                EmailUtility.sendAccountCreatedEmail(a.getEmail(), a.getUsername(), password, a.getRole());
                flash(req, "Account created. Login details emailed.");
            } catch (Exception e) {
                log("UC18 account committed but creation email failed for UserID=" + a.getId(), e);
                flash(req, "Account created, but email could not be sent. Share the initial password securely.");
            }
        } else {
            flash(req, "Account updated.");
        }
        redirect(req, res);
    }

    private User actor(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession s = req.getSession(false);
        Object value = s == null ? null : s.getAttribute("loggedUser");
        if (!(value instanceof User)) {
            res.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        User u = (User) value;
        if (!"admin".equalsIgnoreCase(u.getRole()) || !"active".equalsIgnoreCase(u.getAccountStatus())) {
            res.sendError(403);
            return null;
        }
        return u;
    }

    private AdminAccount read(HttpServletRequest r) {
        AdminAccount a = new AdminAccount();
        a.setUsername(text(r, "username"));
        a.setFirstName(text(r, "firstName"));
        a.setLastName(text(r, "lastName"));
        a.setEmail(text(r, "email"));
        a.setPhone(text(r, "phone"));
        a.setRole(text(r, "role"));
        a.setStatus(text(r, "status"));
        a.setCountry(text(r, "country"));
        a.setCity(text(r, "city"));
        a.setStreet(text(r, "street"));
        return a;
    }

    private String text(HttpServletRequest r, String key) {
        return AuthSupport.text(r.getParameter(key));
    }

    private Integer id(String value) {
        try {
            int n = Integer.parseInt(value);
            return n > 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void form(HttpServletRequest req, HttpServletResponse res, boolean create) throws ServletException, IOException {
        req.setAttribute("creating", create);
        req.getRequestDispatcher("/WEB-INF/admin/users/form.jsp").forward(req, res);
    }

    private void flash(HttpServletRequest r, String message) {
        r.getSession(false).setAttribute("uc18Message", message);
    }

    private void redirect(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private void unavailable(HttpServletRequest req, HttpServletResponse res, SQLException e) throws ServletException, IOException {
        log("UC18 database operation failed", e);
        res.setStatus(503);
        req.setAttribute("error", "Account service unavailable. Please try again later.");
        req.getRequestDispatcher("/WEB-INF/admin/users/error.jsp").forward(req, res);
    }
}
