package controller;

import dao.BookingDAO;
import dao.PaymentDAO;
import dao.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.Booking;
import model.Customer;
import model.Payment;
import model.User;

@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE = 5;
    private static final Logger LOGGER = Logger.getLogger(HistoryServlet.class.getName());
    private static final List<String> PAYMENT_METHODS = List.of("bank_transfer", "cash");
    private static final List<String> PAYMENT_STATUSES = List.of("paid", "partial", "pending", "failed", "refunded");
    private static final Map<String, String> PAYMENT_LABELS = Map.of(
            "bank_transfer", "screen.ee82ae2801cf9097", "cash", "screen.e2da108f63008e44",
            "paid", "screen.fb81b961af456e5e", "partial", "screen.1390db305695537e",
            "pending", "screen.331551b0de4157c9", "failed", "screen.031a8f0f659df890",
            "refunded", "screen.117f6a7cf0372277");

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        /*
         * =====================================================
         * 1. REQUIRE LOGIN
         * =====================================================
         */
        if (session == null
                || session.getAttribute("loggedUser") == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login"
            );
            return;
        }

        User user
                = (User) session.getAttribute(
                        "loggedUser"
                );

        Customer sessionCustomer
                = (Customer) session.getAttribute(
                        "customer"
                );

        /*
         * =====================================================
         * 2. DETERMINE CUSTOMER ID
         *
         * Customer:
         *   may see only their own history.
         *
         * Staff/Admin:
         *   may inspect a customer's history.
         * =====================================================
         */
        Integer customerId
                = resolveCustomerId(
                        request,
                        user,
                        sessionCustomer
                );

        if (customerId == null) {

            request.getRequestDispatcher(
                    "/WEB-INF/error/error404.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * Staff/Admin may inspect customer history.
         * Other roles cannot.
         */
        if (!"customer".equalsIgnoreCase(user.getRole())
                && !"staff".equalsIgnoreCase(user.getRole())
                && !"admin".equalsIgnoreCase(user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        /*
         * =====================================================
         * 3. DEFAULT VIEW
         * =====================================================
         */
        String view
                = request.getParameter("view");

        if (view == null
                || view.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?id="
                    + customerId
                    + "&view=bookings"
            );
            return;
        }

        request.setAttribute(
                "historyCustomerId",
                customerId
        );
        request.setAttribute("ownHistory", "customer".equalsIgnoreCase(user.getRole()));

        switch (view) {

            case "bookings":
                showBookingHistory(
                        request,
                        response,
                        customerId
                );
                break;

            case "payments":
                showPaymentHistory(
                        request,
                        response,
                        customerId
                );
                break;

            default:

                request.getRequestDispatcher(
                        "/WEB-INF/error/error404.jsp"
                ).forward(request, response);

                break;
        }
    }

    /*
     * =========================================================
     * BOOKING HISTORY
     * =========================================================
     */
    private void showBookingHistory(
            HttpServletRequest request,
            HttpServletResponse response,
            int customerId)
            throws ServletException, IOException {

        BookingDAO bookingDAO
                = new BookingDAO();

        PaymentDAO paymentDAO
                = new PaymentDAO();

        ReviewDAO reviewDAO
                = new ReviewDAO();

        /*
         * DO NOT call the old:
         *
         * deleteUnpaidOverdueBookings()
         *
         * because RDS requires historical bookings
         * to remain stored.
         *
         * Use status update instead.
         */
        bookingDAO.expirePendingBookings();

        int currentPage
                = parsePage(
                        request.getParameter("page")
                );

        int totalBookings
                = bookingDAO.countBookingsByCustomerID(
                        customerId
                );

        int totalPages
                = totalBookings == 0
                        ? 0
                        : (int) Math.ceil(
                                totalBookings
                                / (double) PAGE_SIZE
                        );

        /*
         * Avoid requesting pages outside the valid range.
         */
        if (totalPages > 0
                && currentPage > totalPages) {

            currentPage = totalPages;
        }

        List<Booking> bookings
                = bookingDAO.getBookingsByCustomerID(
                        customerId,
                        currentPage,
                        PAGE_SIZE
                );

        Map<Integer, BigDecimal> paidAmountMap = new HashMap<>();
        Map<Integer, BigDecimal> remainingAmountMap = new HashMap<>();
        Map<Integer, Boolean> reviewedMap = new HashMap<>();
        Map<Integer, Boolean> refundedMap = new HashMap<>();
        Map<Integer, String> lateRefundMessageMap = new HashMap<>();

        Map<Integer, Boolean> cancellableMap = new HashMap<>();

        for (Booking booking : bookings) {
            BigDecimal paidAmount = paymentDAO.getTotalPaidAmount(booking.getId());
            BigDecimal remainingAmount = booking.getTotalPrice().subtract(paidAmount);
            if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
                remainingAmount = BigDecimal.ZERO;
            }

            paidAmountMap.put(booking.getId(), paidAmount);
            boolean refunded = bookingDAO.hasRefundedPayment(booking.getId(), customerId);
            refundedMap.put(booking.getId(), refunded);
            String lateRefundMessage = lateRefundMessage(booking, paymentDAO, refunded);
            if (lateRefundMessage != null) {
                lateRefundMessageMap.put(booking.getId(), lateRefundMessage);
            }
            remainingAmountMap.put(booking.getId(), remainingAmount);
            reviewedMap.put(
                    booking.getId(),
                    reviewDAO.isBookingReviewed(booking.getId())
            );
            cancellableMap.put(
                    booking.getId(),
                    bookingDAO.canCustomerCancelBooking(booking.getId(), customerId)
            );
        }

        request.setAttribute(
                "bookings",
                bookings
        );

        request.setAttribute("paidAmountMap", paidAmountMap);
        request.setAttribute("refundedMap", refundedMap);
        request.setAttribute("lateRefundMessageMap", lateRefundMessageMap);
        request.setAttribute("remainingAmountMap", remainingAmountMap);

        request.setAttribute(
                "reviewedMap",
                reviewedMap
        );

        request.setAttribute(
                "cancellableMap",
                cancellableMap
        );

        request.setAttribute(
                "totalPages",
                totalPages
        );

        request.setAttribute(
                "currentPage",
                currentPage
        );

        request.getRequestDispatcher(
                "/WEB-INF/history/books.jsp"
        ).forward(request, response);
    }

    private String lateRefundMessage(Booking booking, PaymentDAO payments, boolean refunded) {
        if (!booking.isOnlineDepositPolicy() || !"cancelled".equals(booking.getStatus())
                || booking.getCancellationDate() == null) {
            return null;
        }
        boolean lateRefunded = false;
        for (Payment payment : payments.getPaymentsByBookingId(booking.getId())) {
            if (!"bank_transfer".equals(payment.getPaymentMethod()) || payment.getPaymentDate() == null
                    || !payment.getPaymentDate().isAfter(booking.getCancellationDate())) {
                continue;
            }
            if ("paid".equals(payment.getPaymentStatus())) {
                return "room.late.refund.pending";
            }
            lateRefunded |= "refunded".equals(payment.getPaymentStatus());
        }
        return lateRefunded && refunded ? "room.late.refund.complete" : null;
    }

    /*
     * =========================================================
     * PAYMENT HISTORY
     *
     * =========================================================
     */
    private void showPaymentHistory(
            HttpServletRequest request,
            HttpServletResponse response,
            int customerId)
            throws ServletException, IOException {

        PaymentDAO paymentDAO = new PaymentDAO();
        int currentPage = parsePage(request.getParameter("page"));
        String query = request.getParameter("q");
        String search = query == null ? "" : query.strip();
        if (search.length() > 100) {
            search = search.substring(0, 100);
        }
        request.setAttribute("paymentSearch", search);
        String method = allowedPaymentFilter(request.getParameter("method"), PAYMENT_METHODS);
        String status = allowedPaymentFilter(request.getParameter("status"), PAYMENT_STATUSES);
        LocalDate date = parsePaymentDate(request.getParameter("date"));
        request.setAttribute("paymentMethodFilter", method);
        request.setAttribute("paymentStatusFilter", status);
        request.setAttribute("paymentDateFilter", date);
        request.setAttribute("paymentMethods", PAYMENT_METHODS);
        request.setAttribute("paymentStatuses", PAYMENT_STATUSES);
        request.setAttribute("paymentLabels", PAYMENT_LABELS);
        request.setAttribute("hasPaymentFilters",
                !search.isEmpty() || !method.isEmpty() || !status.isEmpty() || date != null);
        String lookup = search.replaceFirst("^#(?=[0-9]+$)", "");
        try {
            int totalPayments = paymentDAO.countPaymentsByCustomerId(customerId, lookup, method, status, date);
            int totalPages = (int) Math.ceil(totalPayments / (double) PAGE_SIZE);
            currentPage = Math.min(currentPage, Math.max(1, totalPages));
            if (totalPayments > 0) {
                request.setAttribute("payments",
                        paymentDAO.getPaymentsByCustomerId(customerId, lookup, method, status, date,
                                currentPage, PAGE_SIZE));
            }
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("firstPageLink", Math.max(1, currentPage - 1));
            request.setAttribute("lastPageLink", Math.min(totalPages, currentPage + 1));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load payment history", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("paymentHistoryUnavailable", true);
        }

        request.setAttribute("currentPage", currentPage);
        request.setAttribute("paymentDateFormat",
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .withLocale(request.getLocale()));
        request.setAttribute("stayDateFormat",
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(request.getLocale()));
        request.getRequestDispatcher("/WEB-INF/history/payments.jsp").forward(request, response);
    }

    private static String allowedPaymentFilter(String value, List<String> allowed) {
        return value != null && allowed.contains(value) ? value : "";
    }

    private static LocalDate parsePaymentDate(String value) {
        if (value == null || value.length() != 10) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value);
            return date.getYear() > 0 && date.isBefore(LocalDate.of(9999, 12, 31)) ? date : null;
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /*
     * =========================================================
     * RESOLVE CUSTOMER ID
     * =========================================================
     */
    private Integer resolveCustomerId(
            HttpServletRequest request,
            User user,
            Customer customer) {

        /*
         * Customer history always belongs to
         * the logged-in Customer.
         */
        if ("customer".equalsIgnoreCase(
                user.getRole())) {

            if (customer == null || customer.getUser() == null
                    || customer.getUser().getId() != user.getId()) {
                return null;
            }

            return customer.getCustomerID();
        }

        /*
         * Staff/Admin must provide customer ID
         * when inspecting another customer.
         */
        String idParam
                = request.getParameter("id");

        if (idParam == null
                || idParam.trim().isEmpty()) {

            return null;
        }

        try {

            int id
                    = Integer.parseInt(
                            idParam.trim()
                    );

            return id > 0
                    ? id
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }

    /*
     * =========================================================
     * SAFE PAGE PARSING
     * =========================================================
     */
    private int parsePage(
            String pageParam) {

        if (pageParam == null
                || pageParam.trim().isEmpty()) {

            return 1;
        }

        try {

            int page
                    = Integer.parseInt(
                            pageParam.trim()
                    );

            return page > 0
                    ? page
                    : 1;

        } catch (NumberFormatException e) {

            return 1;
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * History is a read-only page.
         */
        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED
        );
    }

    @Override
    public String getServletInfo() {
        return "Displays booking and payment history.";
    }
}
