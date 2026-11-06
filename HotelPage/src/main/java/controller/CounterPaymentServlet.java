package controller;

import dao.OfflineBookingDAO;
import dao.PaymentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import model.User;
import util.AuthSupport;

@WebServlet("/counter-payment")
public class CounterPaymentServlet extends HttpServlet {

    private static final String KEY = "uc09CounterQuotes";

    @Override
    protected void doGet(
            HttpServletRequest r,
            HttpServletResponse s
    ) throws ServletException, IOException {

        if (OfflineBookingServlet.staff(r, s) == null) {
            return;
        }

        try {
            int id = positive(r.getParameter("bookingId"));

            Map<String, Object> summary
                    = new OfflineBookingDAO().counterSummary(id);

            String token = UUID.randomUUID().toString();

            Map<String, Object> saved
                    = new LinkedHashMap<>(summary);

            saved.put("createdAt", System.currentTimeMillis());

            synchronized (r.getSession(false)) {
                Map<String, Map<String, Object>> quotes
                        = quotes(r.getSession(false));

                while (quotes.size() >= 20) {
                    quotes.remove(
                            quotes.keySet().iterator().next()
                    );
                }

                quotes.put(token, saved);
            }

            r.setAttribute("summary", summary);
            r.setAttribute("paymentKey", token);

            r.getRequestDispatcher(
                    "/WEB-INF/booking-management/counter-payment.jsp"
            ).forward(r, s);

        } catch (IllegalArgumentException e) {
            s.sendError(400, e.getMessage());

        } catch (SQLException e) {
            log("Counter summary failed", e);
            s.sendError(
                    503,
                    "Không thể tải số tiền phải thu. "
                    + "Vui lòng thử lại."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest r,
            HttpServletResponse s
    ) throws IOException {

        r.setCharacterEncoding("UTF-8");

        User actor = OfflineBookingServlet.staff(r, s);

        if (actor == null || !AuthSupport.checkCsrf(r, s)) {
            return;
        }

        String key = r.getParameter("paymentKey");
        Map<String, Object> quote;

        synchronized (r.getSession(false)) {
            quote = quotes(r.getSession(false)).get(key);
        }

        if (quote == null
                || System.currentTimeMillis()
                - (Long) quote.get("createdAt")
                > 30 * 60 * 1000L) {
            s.sendError(
                    409,
                    "Màn thu tiền đã hết hạn. "
                    + "Mở lại từ chi tiết booking."
            );
            return;
        }

        int id = (Integer) quote.get("bookingId");

        String action = AuthSupport.text(
                r.getParameter("action")
        );

        if ("cancel".equals(action)
                || "promotion".equals(action)) {
            try {
                new OfflineBookingDAO().changePending(
                        actor.getId(),
                        id,
                        r.getParameter("promotionCode"),
                        "cancel".equals(action)
                );

                redirect(r, s, id, "");
                return;

            } catch (SQLException | IllegalArgumentException e) {
                log("Cannot change pending offline booking", e);
                redirect(r, s, id, "&error=change-rejected");
                return;
            }
        }

        if (!action.isEmpty() && !"pay".equals(action)) {
            s.sendError(400);
            return;
        }

        int percent;

        try {
            percent = positive(r.getParameter("percent"));
        } catch (IllegalArgumentException e) {
            s.sendError(400);
            return;
        }

        int minimum = (Integer) quote.get("depositPercent");

        if (percent != minimum && percent != 100) {
            s.sendError(400);
            return;
        }

        String result = new PaymentDAO().processCounterPayment(
                id,
                actor.getId(),
                AuthSupport.text(r.getParameter("method")),
                percent,
                key,
                (BigDecimal) quote.get("paid"),
                (BigDecimal) quote.get("total"),
                (BigDecimal) quote.get("payable")
        );

        String flag = PaymentDAO.PAYMENT_SUCCESS.equals(result)
                ? "&paid=1" : "&error=" + result;

        redirect(r, s, id, flag);
    }

    private void redirect(
            HttpServletRequest r,
            HttpServletResponse s,
            int id,
            String flag
    ) {
        s.setStatus(303);
        s.setHeader(
                "Location",
                r.getContextPath()
                + "/counter-payment?bookingId=" + id + flag
        );
    }

    private int positive(String raw) {
        try {
            int id = Integer.parseInt(raw);

            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
        }

        throw new IllegalArgumentException(
                "Mã đặt phòng không hợp lệ."
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> quotes(
            HttpSession session
    ) {
        Object value = session.getAttribute(KEY);

        if (value instanceof Map) {
            return (Map<String, Map<String, Object>>) value;
        }

        Map<String, Map<String, Object>> data
                = new LinkedHashMap<>();

        session.setAttribute(KEY, data);
        return data;
    }
}
