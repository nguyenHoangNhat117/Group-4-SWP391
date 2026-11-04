package controller;

import dao.AdminUserDAO;
import dao.UserDAO;
import model.AdminAccount;
import model.User;
import util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminUserServlet.class.getName());

    private String text(HttpServletRequest r, String key) {
        return AuthSupport.text(r.getParameter(key));
    }

    private int number(String value, int fallback) {
        try {
            int n = Integer.parseInt(value);
            return n > 0 && n <= 10000000 ? n : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private User admin(HttpServletRequest r, HttpServletResponse s) throws IOException {
        AuthSupport.noStore(s);
        HttpSession session = r.getSession(false);
        Object saved = session == null ? null : session.getAttribute("loggedUser");
        if (!(saved instanceof User)) {
            s.sendRedirect(r.getContextPath() + "/login");
            return null;
        }
        User old = (User) saved, current;
        try {
            current = new UserDAO().getUserById(old.getId());
        } catch (IllegalStateException e) {
            s.sendError(503, "Database temporarily unavailable.");
            return null;
        }
        if (current == null || !"active".equals(current.getAccountStatus()) || !Objects.equals(old.getPassword(), current.getPassword())) {
            session.invalidate();
            s.sendRedirect(r.getContextPath() + "/login");
            return null;
        }
        if (!"admin".equals(current.getRole())) {
            s.sendError(403);
            return null;
        }
        session.setAttribute("loggedUser", current);
        AuthSupport.token(session);
        return current;
    }

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        r.setCharacterEncoding("UTF-8");
        if (admin(r, s) == null) {
            return;
        }
        try {
            render(r, s);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "UC18 read failed", e);
            s.sendError(503, "Không thể tải danh sách tài khoản. Hãy thử lại.");
        }
    }

    private void render(HttpServletRequest r, HttpServletResponse s) throws SQLException, ServletException, IOException {
        String q = text(r, "q"), role = text(r, "role"), status = text(r, "status");
        if (q.length() > 100 || (!role.isEmpty() && !AdminAccount.ROLES.contains(role)) || (!status.isEmpty() && !AdminAccount.STATUSES.contains(status))) {
            s.sendError(400, "Invalid search filters");
            return;
        }
        AdminUserDAO dao = new AdminUserDAO();
        r.setAttribute("pageData", dao.search(q, role, status, number(r.getParameter("page"), 1)));
        r.setAttribute("q", q);
        r.setAttribute("roleFilter", role);
        r.setAttribute("statusFilter", status);
        if (r.getAttribute("form") == null) {
            String mode = text(r, "mode");
            if ("create".equals(mode)) {
                r.setAttribute("form", new AdminAccount());
                r.setAttribute("formMode", "create");
            } else if ("edit".equals(mode)) {
                AdminAccount a = dao.find(number(r.getParameter("id"), 0));
                if (a == null || !AdminAccount.ROLES.contains(a.role)) {
                    s.sendError(404);
                    return;
                }
                r.setAttribute("form", a);
                r.setAttribute("formMode", "edit");
            }
        }
        if (!text(r, "inspect").isEmpty()) {
            AdminAccount a = dao.find(number(r.getParameter("inspect"), 0));
            if (a == null || !AdminAccount.ROLES.contains(a.role)) {
                s.sendError(404);
                return;
            }
            int page = number(r.getParameter("historyPage"), 1);
            r.setAttribute("inspectAccount", a);
            r.setAttribute("history", dao.history(a.id, page));
            r.setAttribute("historyPage", page);
        }
        Object flash = r.getSession().getAttribute("uc18Flash");
        r.getSession().removeAttribute("uc18Flash");
        if (flash != null) {
            r.setAttribute("flash", flash);
        }
        r.getRequestDispatcher("/WEB-INF/admin/users/index.jsp").forward(r, s);
    }

    private AdminAccount form(HttpServletRequest r) {
        AdminAccount a = new AdminAccount();
        a.id = number(r.getParameter("id"), 0);
        a.username = text(r, "username");
        a.firstName = text(r, "firstName");
        a.lastName = text(r, "lastName");
        a.email = text(r, "email");
        a.phone = text(r, "phone");
        a.country = text(r, "country");
        a.city = text(r, "city");
        a.street = text(r, "street");
        a.role = text(r, "accountRole");
        a.status = text(r, "accountStatus");
        a.version = text(r, "version");
        return a;
    }

    private String email(AdminAccount a, String password) {
        try {
            CredentialEmail.send(a.email, a.username, password, a.role);
            return "Đã lưu tài khoản và gửi email thông tin đăng nhập.";
        } catch (MessagingException | RuntimeException e) {
            LOG.warning("UC18 credential email failed; accountId=" + a.id + "; type=" + e.getClass().getSimpleName());
            return "Đã lưu tài khoản nhưng chưa gửi được email. Kiểm tra SMTP/APP_BASE_URL, sau đó dùng nút gửi mật khẩu mới trong Inspect. Không tạo lại tài khoản.";
        }
    }

    private void redirect(HttpServletRequest r, HttpServletResponse s, String message) throws IOException {
        r.getSession().setAttribute("uc18Flash", message);
        StringBuilder url = new StringBuilder(r.getContextPath() + "/admin/users?");
        for (String key : Arrays.asList("q", "role", "status", "page")) {
            url.append(key).append('=').append(URLEncoder.encode(text(r, key), StandardCharsets.UTF_8)).append('&');
        }
        s.setStatus(303);
        s.setHeader("Location", url.toString());
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        r.setCharacterEncoding("UTF-8");
        User actor = admin(r, s);
        if (actor == null || !AuthSupport.checkCsrf(r, s)) {
            return;
        }
        String action = text(r, "action");
        AdminUserDAO dao = new AdminUserDAO();
        AdminAccount a = form(r);
        if (!Arrays.asList("create", "update", "toggle", "delete", "resend").contains(action)) {
            s.sendError(400);
            return;
        }
        if (!"create".equals(action) && a.id == 0) {
            s.sendError(400);
            return;
        }
        if ("create".equals(action) || "update".equals(action)) {
            Map<String, String> errors = a.validate("create".equals(action));
            if (!errors.isEmpty()) {
                r.setAttribute("errors", errors);
                showInvalid(r, s, a, action);
                return;
            }
        }
        try {
            String message = "Đã lưu thay đổi.";
            switch (action) {
                case "create": {
                    String password = PasswordUtil.generateInitialPassword();
                    a.id = dao.create(actor.getId(), a, PasswordUtil.hash(password));
                    message = email(a, password);
                    break;
                }
                case "update":
                    dao.update(actor.getId(), a);
                    break;
                case "toggle":
                    dao.toggle(actor.getId(), a.id, a.version);
                    break;
                case "delete":
                    dao.delete(actor.getId(), a.id, a.version);
                    message = "Đã xóa tài khoản chưa sử dụng.";
                    break;
                case "resend": {
                    String password = PasswordUtil.generateInitialPassword();
                    AdminAccount target = dao.resetInitialPassword(actor.getId(), a.id, a.version, PasswordUtil.hash(password));
                    message = email(target, password);
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
            redirect(r, s, message);
        } catch (AdminUserDAO.Rejected e) {
            failure(r, s, a, action, e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "UC18 database write failed", e);
            int code = e.getErrorCode();
            String message = (code == 2601 || code == 2627 || code == 51002 || code == 51003) ? "Username hoặc Email đã được gán cho tài khoản khác." : code == 547 ? "Tài khoản còn dữ liệu liên quan nên không thể xóa." : code == 1205 ? "Có cập nhật đồng thời. Tải lại trang và thử lại." : "Không thể lưu thay đổi. Database đã rollback; hãy thử lại.";
            failure(r, s, a, action, message);
        }
    }

    private void failure(HttpServletRequest r, HttpServletResponse s, AdminAccount a, String action, String message) throws ServletException, IOException {
        if ("create".equals(action) || "update".equals(action)) {
            r.setAttribute("errors", Collections.singletonMap("general", message));
            showInvalid(r, s, a, action);
        } else {
            redirect(r, s, message);
        }
    }

    private void showInvalid(HttpServletRequest r, HttpServletResponse s, AdminAccount a, String action) throws ServletException, IOException {
        r.setAttribute("form", a);
        r.setAttribute("formMode", "create".equals(action) ? "create" : "edit");
        try {
            render(r, s);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "UC18 form reload failed", e);
            s.sendError(503);
        }
    }
}
