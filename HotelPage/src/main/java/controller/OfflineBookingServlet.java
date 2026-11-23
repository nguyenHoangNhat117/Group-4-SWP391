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

    private static final String FORMS = "uc09ReviewedFormsV2";
    private static final long MAX_AGE = 30 * 60 * 1000L;
    private static final long QUOTE_AGE = 5 * 60 * 1000L;
    private static final String[] FIELDS = {"checkIn", "checkOut", "adults", "children", "roomSelection",
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
            Map<String, String> form = read(r);
            boolean search = !form.get("checkIn").isEmpty() || !form.get("checkOut").isEmpty();
            if (!search) {
                LocalDate today = ReportPeriod.today();
                form.put("checkIn", today.toString());
                form.put("checkOut", today.plusDays(1).toString());
                if (form.get("adults").isEmpty()) {
                    form.put("adults", "1");
                }
            }
            render(r, s, form, newForm(r.getSession(false), user.getId()), search);
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
                render(r, s, form, newForm(r.getSession(false), user.getId()), true);
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
                    if ("search".equals(action) || "search_customer".equals(action)) {
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
                render(r, s, state.uncertain && state.pending != null ? state.pending.form : form, state, true);
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
        Integer customer = "new".equals(mode) ? null : existingCustomer(form.get("customerId"));
        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(form.get("checkIn"));
            checkOut = LocalDate.parse(form.get("checkOut"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Chọn ngày nhận và ngày trả phòng hợp lệ.");
        }
        int adults;
        int children;
        try {
            adults = adults(form);
            children = Integer.parseInt(form.get("children"));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Nhập số người lớn và trẻ em bằng số nguyên.");
        }
        return new OfflineBookingDraft(checkIn, checkOut, adults, children, roomSelection(form), customer,
                form.get("firstName"), form.get("lastName"), form.get("email"), form.get("phone"),
                form.get("promotionCode"), form.get("specialRequest"));
    }

    private static int existingCustomer(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ex) {
            // Hồ sơ cần được chọn trong danh sách kết quả.
        }
        throw new IllegalArgumentException("Chọn một hồ sơ trong danh sách Hồ sơ khách hàng trước khi kiểm tra giá.");
    }

    private Map<String, String> read(HttpServletRequest r) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : FIELDS) {
            values.put(key, AuthSupport.text(r.getParameter(key)));
        }
        if (values.get("customerMode").isEmpty()) {
            values.put("customerMode", "existing");
        }
        if (values.get("roomSelection").isEmpty()) {
            values.put("roomSelection", submittedRoomQuantities(r));
        }
        if (values.get("children").isEmpty()) {
            values.put("children", "0");
        }
        return values;
    }

    private void render(HttpServletRequest r, HttpServletResponse s, Map<String, String> form, DirectForm state,
            boolean searchRooms) throws ServletException, IOException {
        r.setAttribute("offlineBookingFormVersion", "multi-room-quote-1");
        r.setAttribute("form", form);
        r.setAttribute("submissionToken", state.token);
        r.setAttribute("searchedStay", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                Arrays.asList(form.get("checkIn"), form.get("checkOut"), form.get("adults"),
                        form.get("children"))));
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
        OfflineBookingDAO dao = new OfflineBookingDAO();
        if (!form.get("q").isEmpty()) {
            try {
                r.setAttribute("customers", dao.searchCustomers(form.get("q")));
            } catch (IllegalArgumentException ex) {
                if (r.getAttribute("error") == null) {
                    s.setStatus(400);
                    r.setAttribute("error", ex.getMessage());
                }
            } catch (SQLException ex) {
                log("Không thể tìm khách tại quầy", ex);
                if (r.getAttribute("error") == null) {
                    s.setStatus(503);
                    r.setAttribute("error", "Chưa thể tìm khách. Vui lòng thử lại.");
                }
            }
        }
        try {
            if (searchRooms) {
                int adults = adults(form);
                int guests = OfflineBookingDraft.countGuests(adults, Integer.parseInt(form.get("children")));
                List<OfflineBookingDAO.RoomOption> rooms = dao.availableTypes(LocalDate.parse(form.get("checkIn")),
                        LocalDate.parse(form.get("checkOut")), guests);
                long availableCapacity = rooms.stream().mapToLong(option
                        -> (long) option.getStock().getAvailable() * option.getStock().getType().getCapacity()).sum();
                r.setAttribute("availableCapacity", availableCapacity);
                r.setAttribute("requiredCapacity", guests);
                r.setAttribute("rooms", rooms);
                r.setAttribute("feasibleCapacity", OfflineBookingDAO.feasibleCapacity(rooms, adults));
                if (availableCapacity >= guests) {
                    restoreRoomQuantities(r, rooms, form);
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

    private static String submittedRoomQuantities(HttpServletRequest request) {
        final String prefix = "roomQty-";
        Map<Integer, Integer> quantities = new TreeMap<>();
        try {
            for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
                if (!parameter.getKey().startsWith(prefix)) {
                    continue;
                }
                String[] values = parameter.getValue();
                int id = Integer.parseInt(parameter.getKey().substring(prefix.length()));
                if (id < 1 || values.length != 1 || values[0].length() > 16 || quantities.containsKey(id)) {
                    return ":";
                }
                int count = values[0].isBlank() ? 0 : new java.math.BigDecimal(values[0]).intValueExact();
                if (count < 0 || count > 1000) {
                    return ":";
                }
                quantities.put(id, count);
            }
        } catch (NumberFormatException | ArithmeticException ex) {
            return ":";
        }
        StringJoiner selection = new StringJoiner(",");
        quantities.forEach((id, count) -> {
            if (count > 0) {
                selection.add(id + ":" + count);
            }
        });
        return selection.toString();
    }

    private static void restoreRoomQuantities(HttpServletRequest request, List<OfflineBookingDAO.RoomOption> rooms,
            Map<String, String> form) {
        Map<Integer, Integer> previous = roomSelection(form);
        Map<Integer, Integer> current = new TreeMap<>();
        for (OfflineBookingDAO.RoomOption option : rooms) {
            int id = option.getStock().getType().getId();
            int count = Math.min(previous.getOrDefault(id, 0), option.getMaximum());
            if (count > 0) {
                current.put(id, count);
            }
        }
        request.setAttribute("roomQuantities", current);
        request.setAttribute("roomStockChanged", !current.equals(previous));
    }

    private static int adults(Map<String, String> form) {
        String value = form.get("adults");
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    private static Map<Integer, Integer> roomSelection(Map<String, String> form) {
        String value = form.get("roomSelection");
        Map<Integer, Integer> rooms = new TreeMap<>();
        if (value.isEmpty()) {
            return rooms;
        }
        if (value.length() > 16000) {
            throw new IllegalArgumentException("Danh sách phòng đã chọn không hợp lệ.");
        }
        for (String line : value.split(",", -1)) {
            if (!line.matches("[0-9]{1,10}:[0-9]{1,4}")) {
                throw new IllegalArgumentException("Số lượng phòng đã chọn không hợp lệ.");
            }
            String[] entry = line.split(":");
            long typeId = Long.parseLong(entry[0]);
            if (typeId < 1 || typeId > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Mã loại phòng đã chọn không hợp lệ. Vui lòng tìm phòng lại.");
            }
            int id = (int) typeId;
            int count = Integer.parseInt(entry[1]);
            if (count < 1 || count > 1000 || rooms.putIfAbsent(id, count) != null) {
                throw new IllegalArgumentException("Danh sách phòng đã chọn không hợp lệ.");
            }
        }
        return rooms;
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
