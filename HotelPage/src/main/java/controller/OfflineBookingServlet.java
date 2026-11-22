package controller;

import dao.OfflineBookingDAO;
import dao.RoomDAO;
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

    private static final String FORMS = "uc09ReviewedFormsV2";
    private static final long MAX_AGE = 30 * 60 * 1000L;
    private static final long QUOTE_AGE = 5 * 60 * 1000L;
    private static final String[] FIELDS = {"checkIn", "checkOut", "guests", "quantity", "roomTypeId",
        "customerMode", "customerId", "q", "firstName", "lastName", "email", "phone",
        "promotionCode", "specialRequest"};

    private static final class Pending implements Serializable {

        private static final long serialVersionUID = 2L;
        final Quote quote;
        final Map<String, String> form;
        final String token = UUID.randomUUID().toString();
        final long created = System.currentTimeMillis();

        Pending(Quote quote, Map<String, String> form) {
            this.quote = quote;
            this.form = new LinkedHashMap<>(form);
        }
    }

    private static final class DirectForm implements Serializable {

        private static final long serialVersionUID = 2L;
        final String token = UUID.randomUUID().toString();
        final int staffId;
        final long created = System.currentTimeMillis();
        Pending pending;
        boolean uncertain;
        int bookingId;

        DirectForm(int staffId) {
            this.staffId = staffId;
        }
    }

    static User staff(HttpServletRequest r, HttpServletResponse s) throws IOException {
        AuthSupport.noStore(s);
        HttpSession session = r.getSession(false);
        Object value = session == null ? null : session.getAttribute("loggedUser");
        if (!(value instanceof User)) {
            s.sendRedirect(r.getContextPath() + "/login");
            return null;
        }
        User user = (User) value;
        if (!"staff".equalsIgnoreCase(user.getRole())
                || !"active".equalsIgnoreCase(user.getAccountStatus())) {
            s.sendError(403, "Chỉ Staff đang hoạt động được thao tác tại quầy.");
            return null;
        }
        AuthSupport.token(session);
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        r.setCharacterEncoding("UTF-8");
        try {
            User user = staff(r, s);
            if (user == null) {
                return;
            }
            if ("lookup".equals(r.getParameter("view"))) {
                showLookup(r, s, "");
                return;
            }
            render(r, s, read(r), newForm(r.getSession(false), user.getId()));
        } catch (IllegalStateException ex) {
            expired(s);
        }
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        r.setCharacterEncoding("UTF-8");
        try {
            User user = staff(r, s);
            if (user == null || !AuthSupport.checkCsrf(r, s)) {
                return;
            }
            if ("lookup".equals(r.getParameter("action"))) {
                showLookup(r, s, AuthSupport.text(r.getParameter("bookingQuery")));
                return;
            }
            DirectForm state = findForm(r.getSession(false), r.getParameter("submissionToken"));
            Map<String, String> form = read(r);
            if (state == null || state.staffId != user.getId()) {
                s.setStatus(409);
                r.setAttribute("error", "Form đã hết hạn. Kiểm tra lịch sử nếu đã xác nhận tạo đơn, rồi mở form mới.");
                render(r, s, form, newForm(r.getSession(false), user.getId()));
                return;
            }
            synchronized (state) {
                if (state.bookingId > 0) {
                    redirect(s, r, state.bookingId);
                    return;
                }
                String action = AuthSupport.text(r.getParameter("action"));
                try {
                    if (state.uncertain && !"retry".equals(action)) {
                        throw new IllegalArgumentException("Yêu cầu trước chưa rõ kết quả. Kiểm tra lịch sử hoặc bấm Kiểm tra lại yêu cầu cũ; không tạo yêu cầu mới.");
                    }
                    if ("search".equals(action)) {
                        state.pending = null;
                    } else if ("preview".equals(action)) {
                        state.pending = null;
                        OfflineBookingDraft draft = draft(form);
                        Quote quote = new OfflineBookingDAO().preview(user.getId(), draft);
                        state.pending = new Pending(quote, form);
                    } else if ("create".equals(action) || "retry".equals(action)) {
                        boolean retry = "retry".equals(action);
                        if (state.pending == null || !state.pending.token.equals(r.getParameter("quoteToken"))) {
                            throw new IllegalArgumentException("Báo giá không còn hợp lệ. Bấm Xem và kiểm tra giá trước khi xác nhận.");
                        }
                        if (retry && !state.uncertain) {
                            throw new IllegalArgumentException("Không có yêu cầu đang chờ kiểm tra kết quả.");
                        }
                        if (retry) {
                            form = new LinkedHashMap<>(state.pending.form);
                            if (System.currentTimeMillis() - state.pending.created >= QUOTE_AGE) {
                                Integer known = new OfflineBookingDAO().findCreatedRequest(user.getId(), state.pending.quote.draft.requestKey);
                                if (known != null) {
                                    state.bookingId = known;
                                    state.uncertain = false;
                                    redirect(s, r, known);
                                    return;
                                }
                                state.pending = null;
                                state.uncertain = false;
                                throw new IllegalArgumentException("Chưa tìm thấy đơn của yêu cầu cũ và báo giá đã hết hạn. Vui lòng xem giá mới; chưa tạo thêm đơn.");
                            }
                        }
                        if (!retry && (!sameBooking(state.pending.form, form)
                                || System.currentTimeMillis() - state.pending.created >= QUOTE_AGE)) {
                            state.pending = null;
                            throw new IllegalArgumentException("Thông tin đã đổi hoặc báo giá quá 5 phút. Vui lòng xem lại giá.");
                        }
                        try {
                            state.bookingId = new OfflineBookingDAO().create(user.getId(), state.pending.quote);
                            state.uncertain = false;
                        } catch (IllegalArgumentException ex) {
                            state.pending = null;
                            state.uncertain = false;
                            throw ex;
                        } catch (SQLException ex) {
                            // Giữ nguyên requestKey, kể cả lỗi mạng sau COMMIT.
                            state.uncertain = true;
                            throw ex;
                        }
                        redirect(s, r, state.bookingId);
                        return;
                    } else {
                        throw new IllegalArgumentException("Thao tác không hợp lệ. Vui lòng xem và xác nhận báo giá.");
                    }
                } catch (DateTimeParseException | NumberFormatException ex) {
                    s.setStatus(400);
                    r.setAttribute("error", "Nhập đủ ngày, số khách và chọn phòng/khách hàng hợp lệ.");
                } catch (IllegalArgumentException ex) {
                    s.setStatus(409);
                    r.setAttribute("error", ex.getMessage());
                } catch (SQLException ex) {
                    log("UC09 request failed", ex);
                    s.setStatus(503);
                    r.setAttribute("error", state.uncertain
                            ? "Chưa xác nhận được kết quả. Kiểm tra lịch sử trước; nút Kiểm tra lại yêu cầu cũ sẽ không sinh mã yêu cầu khác."
                            : "Chưa thể kiểm tra dữ liệu. Vui lòng thử lại, chưa tạo đơn.");
                }
                render(r, s, state.uncertain && state.pending != null ? state.pending.form : form, state);
            }
        } catch (IllegalStateException ex) {
            expired(s);
        }
    }

    private void showLookup(HttpServletRequest request, HttpServletResponse response, String keyword)
            throws ServletException, IOException {
        request.setAttribute("lookupView", true);
        request.setAttribute("bookingQuery", keyword);
        if (!keyword.isEmpty()) {
            try {
                request.setAttribute("matchingBookings", new OfflineBookingDAO().lookupBookings(keyword));
            } catch (IllegalArgumentException ex) {
                response.setStatus(400);
                request.setAttribute("error", ex.getMessage());
            } catch (SQLException ex) {
                log("Không thể tra đơn tại quầy", ex);
                response.setStatus(503);
                request.setAttribute("error", "Chưa thể tra đơn. Vui lòng thử lại.");
            }
        }
        request.getRequestDispatcher("/WEB-INF/booking-management/offline.jsp").forward(request, response);
    }

    private void expired(HttpServletResponse s) throws IOException {
        if (!s.isCommitted()) {
            s.sendError(403, "Phiên đã hết hạn. Đăng nhập lại và kiểm tra lịch sử trước khi tạo lại.");
        }
    }

    private void redirect(HttpServletResponse s, HttpServletRequest r, int id) {
        s.setStatus(303);
        s.setHeader("Location", r.getContextPath() + "/counter-payment?bookingId=" + id + "&created=1");
    }

    private boolean sameBooking(Map<String, String> a, Map<String, String> b) {
        for (String key : FIELDS) {
            if (!"q".equals(key) && !Objects.equals(a.get(key), b.get(key))) {
                return false;
            }
        }
        return true;
    }

    private OfflineBookingDraft draft(Map<String, String> form) {
        String mode = form.get("customerMode");
        if (!Arrays.asList("existing", "new").contains(mode)) {
            throw new IllegalArgumentException("Chọn khách có sẵn hoặc khách mới.");
        }
        return new OfflineBookingDraft(
                LocalDate.parse(form.get("checkIn")), LocalDate.parse(form.get("checkOut")),
                Integer.parseInt(form.get("guests")), Integer.parseInt(form.get("roomTypeId")),
                Integer.parseInt(form.get("quantity")),
                "new".equals(mode) ? null : Integer.valueOf(form.get("customerId")),
                form.get("firstName"), form.get("lastName"), form.get("email"), form.get("phone"),
                form.get("promotionCode"), form.get("specialRequest"));
    }

    private Map<String, String> read(HttpServletRequest r) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : FIELDS) {
            values.put(key, AuthSupport.text(r.getParameter(key)));
        }
        if (values.get("customerMode").isEmpty()) {
            values.put("customerMode", "existing");
        }
        if (values.get("guests").isEmpty()) {
            values.put("guests", "1");
        }
        if (values.get("quantity").isEmpty()) {
            values.put("quantity", "1");
        }
        return values;
    }

    private void render(HttpServletRequest r, HttpServletResponse s, Map<String, String> form, DirectForm state) throws ServletException, IOException {
        r.setAttribute("form", form);
        r.setAttribute("submissionToken", state.token);
        r.setAttribute("searchedStay", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                Arrays.asList(form.get("checkIn"), form.get("checkOut"), form.get("guests"), form.get("quantity"))));
        r.setAttribute("today", ReportPeriod.today());
        r.setAttribute("holdMinutes", OfflineBookingPolicy.HOLD_MINUTES);
        r.setAttribute("depositPercent", OfflineBookingPolicy.DEPOSIT_PERCENT);
        r.setAttribute("uncertain", state.uncertain);
        if (state.pending != null) {
            r.setAttribute("quote", state.pending.quote);
            r.setAttribute("quoteToken", state.pending.token);
            r.setAttribute("quoteRemainingMillis", Math.max(0, QUOTE_AGE - (System.currentTimeMillis() - state.pending.created)));
            java.util.List<String> quotedValues = new java.util.ArrayList<>();
            for (String key : FIELDS) {
                if (!"q".equals(key)) {
                    quotedValues.add(state.pending.form.get(key));
                }
            }
            r.setAttribute("quoteSignature", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(quotedValues));
            r.setAttribute("quotedDeposit", state.pending.quote.getPayable().multiply(java.math.BigDecimal.valueOf(OfflineBookingPolicy.DEPOSIT_PERCENT))
                    .divide(java.math.BigDecimal.valueOf(100), 0, java.math.RoundingMode.CEILING));
        }
        try {
            OfflineBookingDAO dao = new OfflineBookingDAO();
            if (!form.get("q").isEmpty()) {
                r.setAttribute("customers", dao.searchCustomers(form.get("q")));
            }
            if (!form.get("checkIn").isEmpty() || !form.get("checkOut").isEmpty()) {
                List<RoomDAO.AvailableType> rooms = dao.availableTypes(LocalDate.parse(form.get("checkIn")),
                        LocalDate.parse(form.get("checkOut")), Integer.parseInt(form.get("guests")),
                        Integer.parseInt(form.get("quantity")));
                r.setAttribute("rooms", rooms);
                if (!rooms.isEmpty()) {
                    r.setAttribute("promotions", dao.availablePromotions());
                }
                r.setAttribute("searched", true);
            }
        } catch (DateTimeParseException | NumberFormatException ex) {
            if (r.getAttribute("error") == null) {
                s.setStatus(400);
                r.setAttribute("error", "Nhập đủ ngày và số khách hợp lệ.");
            }
        } catch (IllegalArgumentException ex) {
            if (r.getAttribute("error") == null) {
                s.setStatus(400);
                r.setAttribute("error", ex.getMessage());
            }
        } catch (SQLException ex) {
            log("UC09 search failed", ex);
            s.setStatus(503);
            if (r.getAttribute("error") == null) {
                r.setAttribute("error", "Không thể tải dữ liệu. Vui lòng thử lại.");
            }
        }
        r.getRequestDispatcher("/WEB-INF/booking-management/offline.jsp").forward(r, s);
    }

    @SuppressWarnings("unchecked")
    private Map<String, DirectForm> forms(HttpSession session) {
        Object existing = session.getAttribute(FORMS);
        if (existing instanceof Map) {
            return (Map<String, DirectForm>) existing;
        }
        Map<String, DirectForm> values = new LinkedHashMap<>();
        session.setAttribute(FORMS, values);
        return values;
    }

    private DirectForm newForm(HttpSession session, int staffId) {
        synchronized (session) {
            Map<String, DirectForm> values = forms(session);
            values.values().removeIf(value -> System.currentTimeMillis() - value.created > MAX_AGE);
            while (values.size() >= 10) {
                values.remove(values.keySet().iterator().next());
            }
            DirectForm state = new DirectForm(staffId);
            values.put(state.token, state);
            return state;
        }
    }

    private DirectForm findForm(HttpSession session, String token) {
        synchronized (session) {
            Map<String, DirectForm> values = forms(session);
            values.values().removeIf(value -> System.currentTimeMillis() - value.created > MAX_AGE);
            return values.get(token);
        }
    }
}
