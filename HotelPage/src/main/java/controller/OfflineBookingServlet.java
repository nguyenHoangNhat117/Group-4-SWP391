package controller;

import dao.OfflineBookingDAO;
import dao.OfflineBookingDAO.Quote;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import model.OfflineBookingDraft;
import model.User;
import util.AuthSupport;
import util.OfflineBookingPolicy;
import util.ReportPeriod;

@WebServlet("/offline-booking")
public class OfflineBookingServlet extends HttpServlet {

    private static final String DRAFTS = "uc09Drafts";

    private static final String[] FIELDS = {
        "checkIn", "checkOut", "guests", "roomNumber",
        "customerMode", "customerId", "q",
        "firstName", "lastName", "email", "phone",
        "promotionCode", "specialRequest"
    };

    private static final class Pending implements Serializable {

        private static final long serialVersionUID = 1L;

        final Quote quote;
        final Map<String, String> form;
        final long created = System.currentTimeMillis();

        Pending(Quote quote, Map<String, String> form) {
            this.quote = quote;
            this.form = new LinkedHashMap<>(form);
        }
    }

    static User staff(
            HttpServletRequest r,
            HttpServletResponse s
    ) throws IOException {

        AuthSupport.noStore(s);

        HttpSession session = r.getSession(false);

        Object value = session == null
                ? null : session.getAttribute("loggedUser");

        if (!(value instanceof User)) {
            s.sendRedirect(r.getContextPath() + "/login");
            return null;
        }

        User user = (User) value;

        if (!"staff".equals(user.getRole())
                && !"admin".equals(user.getRole())) {
            s.sendError(403);
            return null;
        }

        AuthSupport.token(session);
        return user;
    }

    @Override
    protected void doGet(
            HttpServletRequest r,
            HttpServletResponse s
    ) throws ServletException, IOException {

        r.setCharacterEncoding("UTF-8");

        if (staff(r, s) == null) {
            return;
        }

        Map<String, String> form = read(r);

        Pending edit = pending(
                r.getSession(false), r.getParameter("edit")
        );

        if (edit != null) {
            form = edit.form;
        }

        render(r, s, form);
    }

    @Override
    protected void doPost(
            HttpServletRequest r,
            HttpServletResponse s
    ) throws ServletException, IOException {

        r.setCharacterEncoding("UTF-8");

        User user = staff(r, s);

        if (user == null || !AuthSupport.checkCsrf(r, s)) {
            return;
        }

        Map<String, String> form = read(r);

        try {
            String action = AuthSupport.text(
                    r.getParameter("action")
            );

            if ("search".equals(action)) {
                render(r, s, form);
                return;
            }

            if ("create".equals(action)) {
                Pending pending = pending(
                        r.getSession(false),
                        r.getParameter("requestKey")
                );

                if (pending == null) {
                    throw new IllegalArgumentException(
                            "Tóm tắt đã hết hạn. "
                            + "Vui lòng kiểm tra và xem lại đặt phòng."
                    );
                }

                form = pending.form;

                int id = new OfflineBookingDAO().create(
                        user.getId(), pending.quote
                );

                s.setStatus(303);
                s.setHeader(
                        "Location",
                        r.getContextPath()
                        + "/counter-payment?bookingId="
                        + id + "&created=1"
                );
                return;
            }

            if (!"preview".equals(action)) {
                s.sendError(400);
                return;
            }

            String mode = form.get("customerMode");

            if (!Arrays.asList("existing", "new").contains(mode)) {
                throw new IllegalArgumentException(
                        "Chọn khách có sẵn hoặc khách mới."
                );
            }

            OfflineBookingDraft draft = new OfflineBookingDraft(
                    LocalDate.parse(form.get("checkIn")),
                    LocalDate.parse(form.get("checkOut")),
                    Integer.parseInt(form.get("guests")),
                    Integer.parseInt(form.get("roomNumber")),
                    "new".equals(mode)
                    ? null
                    : Integer.valueOf(form.get("customerId")),
                    form.get("firstName"),
                    form.get("lastName"),
                    form.get("email"),
                    form.get("phone"),
                    form.get("promotionCode"),
                    form.get("specialRequest")
            );

            Quote quote = new OfflineBookingDAO().preview(
                    user.getId(), draft
            );

            save(
                    r.getSession(false),
                    new Pending(quote, form)
            );

            r.setAttribute("quote", quote);

        } catch (DateTimeParseException | NumberFormatException e) {
            s.setStatus(400);
            r.setAttribute(
                    "error",
                    "Vui lòng nhập ngày hợp lệ, số khách "
                    + "và chọn phòng/khách hàng."
            );

        } catch (IllegalArgumentException e) {
            s.setStatus(409);
            r.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            log("UC09 failed", e);
            s.setStatus(503);

            boolean duplicate
                    = e.getErrorCode() == 2601
                    || e.getErrorCode() == 2627;

            r.setAttribute(
                    "error",
                    duplicate
                            ? "Email hoặc điện thoại đã tồn tại. "
                            + "Tìm khách có sẵn rồi thử lại."
                            : "Chưa thể tạo đặt phòng. "
                            + "Dữ liệu chưa lưu đã được rollback. "
                            + "Kiểm tra SQL UC09 và thử lại."
            );
        }

        render(r, s, form);
    }

    private Map<String, String> read(HttpServletRequest r) {
        Map<String, String> values = new LinkedHashMap<>();

        for (String key : FIELDS) {
            values.put(
                    key, AuthSupport.text(r.getParameter(key))
            );
        }

        if (values.get("customerMode").isEmpty()) {
            values.put("customerMode", "existing");
        }

        if (values.get("guests").isEmpty()) {
            values.put("guests", "1");
        }

        return values;
    }

    private void render(
            HttpServletRequest r,
            HttpServletResponse s,
            Map<String, String> form
    ) throws ServletException, IOException {

        r.setAttribute("form", form);
        r.setAttribute("today", ReportPeriod.today());
        r.setAttribute(
                "holdMinutes", OfflineBookingPolicy.HOLD_MINUTES
        );
        r.setAttribute(
                "depositPercent",
                OfflineBookingPolicy.DEPOSIT_PERCENT
        );

        if (r.getAttribute("quote") == null) {
            try {
                OfflineBookingDAO dao = new OfflineBookingDAO();

                if (!form.get("q").isEmpty()) {
                    r.setAttribute(
                            "customers",
                            dao.searchCustomers(form.get("q"))
                    );
                }

                if (!form.get("checkIn").isEmpty()
                        || !form.get("checkOut").isEmpty()) {
                    r.setAttribute(
                            "rooms",
                            dao.availableRooms(
                                    LocalDate.parse(
                                            form.get("checkIn")
                                    ),
                                    LocalDate.parse(
                                            form.get("checkOut")
                                    ),
                                    Integer.parseInt(
                                            form.get("guests")
                                    )
                            )
                    );

                    r.setAttribute("searched", true);
                }

            } catch (DateTimeParseException
                    | NumberFormatException e) {
                if (r.getAttribute("error") == null) {
                    s.setStatus(400);
                    r.setAttribute(
                            "error",
                            "Vui lòng nhập đủ ngày nhận/trả "
                            + "và số khách hợp lệ."
                    );
                }

            } catch (IllegalArgumentException e) {
                if (r.getAttribute("error") == null) {
                    s.setStatus(400);
                    r.setAttribute("error", e.getMessage());
                }

            } catch (SQLException e) {
                log("UC09 search failed", e);
                s.setStatus(503);
                r.setAttribute(
                        "error",
                        "Không thể tải dữ liệu. Vui lòng thử lại."
                );
            }
        }

        r.getRequestDispatcher(
                "/WEB-INF/booking-management/offline.jsp"
        ).forward(r, s);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Pending> drafts(HttpSession session) {
        Object existing = session.getAttribute(DRAFTS);

        if (existing instanceof Map) {
            return (Map<String, Pending>) existing;
        }

        Map<String, Pending> map = new LinkedHashMap<>();
        session.setAttribute(DRAFTS, map);
        return map;
    }

    private Pending pending(HttpSession session, String key) {
        synchronized (session) {
            Map<String, Pending> map = drafts(session);

            map.values().removeIf(
                    p -> System.currentTimeMillis() - p.created
                    > 30 * 60 * 1000L
            );

            return map.get(key);
        }
    }

    private void save(HttpSession session, Pending pending) {
        synchronized (session) {
            Map<String, Pending> map = drafts(session);

            while (map.size() >= 10) {
                map.remove(map.keySet().iterator().next());
            }

            map.put(pending.quote.draft.requestKey, pending);
        }
    }
}
