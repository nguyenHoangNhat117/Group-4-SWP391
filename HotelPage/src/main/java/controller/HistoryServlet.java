package controller;

import dao.BookingDAO;
import dao.PaymentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Booking;
import model.Customer;
import model.Payment;
import model.User;

@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {

    private static final int PAGE_SIZE = 5;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

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

        User user =
                (User) session.getAttribute(
                        "loggedUser"
                );

        Customer sessionCustomer =
                (Customer) session.getAttribute(
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
        Integer customerId =
                resolveCustomerId(
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
         * Customer cannot manually change ?id=...
         * to another customer's ID.
         */
        if ("customer".equalsIgnoreCase(
                user.getRole())) {

            if (sessionCustomer == null) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/login"
                );
                return;
            }

            customerId =
                    sessionCustomer.getCustomerID();
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
        String view =
                request.getParameter("view");

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

        BookingDAO bookingDAO =
                new BookingDAO();

        PaymentDAO paymentDAO =
                new PaymentDAO();

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

        int currentPage =
                parsePage(
                        request.getParameter("page")
                );

        int totalBookings =
                bookingDAO.countBookingsByCustomerID(
                        customerId
                );

        int totalPages =
                totalBookings == 0
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

        List<Booking> bookings =
                bookingDAO.getBookingsByCustomerID(
                        customerId,
                        currentPage,
                        PAGE_SIZE
                );

        /*
         * Temporary paid map.
         *
         * PaymentDAO will be refactored later.
         */
        Map<Integer, Boolean> paidMap =
                new HashMap<>();

        for (Booking booking : bookings) {

            paidMap.put(
                    booking.getId(),
                    paymentDAO.isBookingPaid(
                            booking.getId()
                    )
            );
        }

        request.setAttribute(
                "bookings",
                bookings
        );

        request.setAttribute(
                "paidMap",
                paidMap
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

    /*
     * =========================================================
     * PAYMENT HISTORY
     *
     * Kept compatible with existing PaymentDAO for now.
     * Payment module will be refactored next.
     * =========================================================
     */
    private void showPaymentHistory(
            HttpServletRequest request,
            HttpServletResponse response,
            int customerId)
            throws ServletException, IOException {

        PaymentDAO paymentDAO =
                new PaymentDAO();

        int currentPage =
                parsePage(
                        request.getParameter("page")
                );

        int totalPayments =
                paymentDAO.countPaymentsByCustomerId(
                        customerId
                );

        int totalPages =
                totalPayments == 0
                        ? 0
                        : (int) Math.ceil(
                                totalPayments
                                / (double) PAGE_SIZE
                        );

        if (totalPages > 0
                && currentPage > totalPages) {

            currentPage = totalPages;
        }

        List<Payment> payments =
                paymentDAO.getPaymentsByCustomerId(
                        customerId,
                        currentPage,
                        PAGE_SIZE
                );

        request.setAttribute(
                "payments",
                payments
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
                "/WEB-INF/history/payments.jsp"
        ).forward(request, response);
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

            if (customer == null) {
                return null;
            }

            return customer.getCustomerID();
        }

        /*
         * Staff/Admin must provide customer ID
         * when inspecting another customer.
         */
        String idParam =
                request.getParameter("id");

        if (idParam == null
                || idParam.trim().isEmpty()) {

            return null;
        }

        try {

            int id =
                    Integer.parseInt(
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

            int page =
                    Integer.parseInt(
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