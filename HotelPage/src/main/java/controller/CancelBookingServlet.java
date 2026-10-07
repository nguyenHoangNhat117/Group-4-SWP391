package controller;

import dao.BookingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import model.Booking;
import model.Customer;
import model.User;

@WebServlet(
        name = "CancelBookingServlet",
        urlPatterns = {"/history/cancel-booking"}
)
public class CancelBookingServlet
        extends HttpServlet {

    /*
     * =========================================================
     * GET
     *
     * Display cancellation confirmation.
     * =========================================================
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        /*
         * Only authenticated Customer may cancel
         * their own booking from this screen.
         */
        if (session == null
                || session.getAttribute("loggedUser") == null
                || session.getAttribute("customer") == null) {

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

        Customer customer =
                (Customer) session.getAttribute(
                        "customer"
                );

        if (!"customer".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        Integer bookingId =
                parseBookingId(
                        request.getParameter("id")
                );

        if (bookingId == null) {

            redirectHistory(
                    request,
                    response,
                    customer.getCustomerID(),
                    "invalid-booking"
            );

            return;
        }

        BookingDAO bookingDAO =
                new BookingDAO();

        /*
         * Prevent Customer A from opening
         * cancellation page of Customer B.
         */
        if (!bookingDAO.isBookingOwnedByCustomer(
                bookingId,
                customer.getCustomerID())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        Booking booking =
                bookingDAO.getBookingById(
                        bookingId
                );

        if (booking == null) {

            redirectHistory(
                    request,
                    response,
                    customer.getCustomerID(),
                    "booking-not-found"
            );

            return;
        }

        /*
         * Only pending / confirmed booking
         * may enter cancellation flow.
         */
        if (!"pending".equalsIgnoreCase(
                    booking.getStatus())
                && !"confirmed".equalsIgnoreCase(
                    booking.getStatus())) {

            redirectHistory(
                    request,
                    response,
                    customer.getCustomerID(),
                    "cannot-cancel"
            );

            return;
        }

        request.setAttribute(
                "booking",
                booking
        );

        request.getRequestDispatcher(
                "/WEB-INF/history/cancelBooking/"
                + "cancel-booking.jsp"
        ).forward(request, response);
    }

    /*
     * =========================================================
     * POST
     *
     * Perform cancellation.
     * =========================================================
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("loggedUser") == null
                || session.getAttribute("customer") == null) {

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

        Customer customer =
                (Customer) session.getAttribute(
                        "customer"
                );

        if (!"customer".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        Integer bookingId =
                parseBookingId(
                        request.getParameter("id")
                );

        if (bookingId == null) {

            redirectHistory(
                    request,
                    response,
                    customer.getCustomerID(),
                    "invalid-booking"
            );

            return;
        }

        BookingDAO bookingDAO =
                new BookingDAO();

        /*
         * Ownership check is mandatory.
         */
        if (!bookingDAO.isBookingOwnedByCustomer(
                bookingId,
                customer.getCustomerID())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        String reason =
                request.getParameter(
                        "reason"
                );

        if (reason != null) {

            reason =
                    reason.trim();

            if (reason.isEmpty()) {
                reason = null;
            }

            /*
             * Keep DB input reasonable.
             */
            if (reason != null
                    && reason.length() > 500) {

                reason =
                        reason.substring(
                                0,
                                500
                        );
            }
        }

        /*
         * Store a readable default reason.
         */
        if (reason == null) {

            reason =
                    "Cancelled by customer";
        }

        /*
         * cancelBooking() already checks:
         *
         * - BookingID
         * - CustomerID
         * - pending / confirmed status
         * - at least 24h before check-in
         *
         * It performs UPDATE, not DELETE.
         */
        boolean cancelled =
                bookingDAO.cancelBooking(
                        bookingId,
                        customer.getCustomerID(),
                        reason
                );

        if (!cancelled) {

            redirectHistory(
                    request,
                    response,
                    customer.getCustomerID(),
                    "cannot-cancel"
            );

            return;
        }

        redirectHistory(
                request,
                response,
                customer.getCustomerID(),
                "cancel-success"
        );
    }

    /*
     * =========================================================
     * PARSE BOOKING ID
     * =========================================================
     */
    private Integer parseBookingId(
            String idParam) {

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
     * REDIRECT TO CUSTOMER HISTORY
     * =========================================================
     */
    private void redirectHistory(
            HttpServletRequest request,
            HttpServletResponse response,
            int customerId,
            String message)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/history?id="
                + customerId
                + "&view=bookings"
                + "&message="
                + message
        );
    }

    @Override
    public String getServletInfo() {
        return "Cancels eligible customer bookings without deleting history.";
    }
}