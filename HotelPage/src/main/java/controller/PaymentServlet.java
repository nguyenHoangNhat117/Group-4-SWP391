package controller;

import dao.BookingDAO;
import dao.DiscountDAO;
import dao.PaymentDAO;

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
        name = "PaymentServlet",
        urlPatterns = {"/payment"}
)
public class PaymentServlet
        extends HttpServlet {

    /*
     * =========================================================
     * GET PAYMENT PAGE
     * =========================================================
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute(
                        "loggedUser"
                ) == null
                || session.getAttribute(
                        "customer"
                ) == null) {

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

        /*
         * UC10 belongs to Customer.
         */
        if (!"customer".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        Integer bookingId =
                parseBookingId(
                        request.getParameter(
                                "bookingId"
                        )
                );

        if (bookingId == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=invalid-booking"
            );

            return;
        }

        BookingDAO bookingDAO =
                new BookingDAO();

        /*
         * Customer may pay only their own booking.
         */
        if (!bookingDAO
                .isBookingOwnedByCustomer(
                        bookingId,
                        customer.getCustomerID()
                )) {

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

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=booking-not-found"
            );

            return;
        }

        /*
         * Only pending Booking can be paid.
         */
        if (!"pending".equalsIgnoreCase(
                booking.getStatus())) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=cannot-pay"
            );

            return;
        }

        PaymentDAO paymentDAO =
                new PaymentDAO();

        /*
         * Avoid duplicate payment.
         */
        if (paymentDAO.isBookingPaid(
                bookingId)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=already-paid"
            );

            return;
        }

        DiscountDAO discountDAO =
                new DiscountDAO();

        request.setAttribute(
                "booking",
                booking
        );

        request.setAttribute(
                "promotions",
                discountDAO
                        .getActivePromotions()
        );

        request.getRequestDispatcher(
                "/WEB-INF/pay/payment.jsp"
        ).forward(request, response);
    }

    /*
     * =========================================================
     * POST PAYMENT
     * =========================================================
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute(
                        "loggedUser"
                ) == null
                || session.getAttribute(
                        "customer"
                ) == null) {

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
                        request.getParameter(
                                "bookingId"
                        )
                );

        if (bookingId == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=invalid-booking"
            );

            return;
        }

        String paymentMethod =
                request.getParameter(
                        "payment-method"
                );

        String discountCode =
                request.getParameter(
                        "discount-code"
                );

        if (discountCode != null) {

            discountCode =
                    discountCode.trim();

            if (discountCode.isEmpty()) {
                discountCode = null;
            }
        }

        PaymentDAO paymentDAO =
                new PaymentDAO();

        String result =
                paymentDAO.processPayment(
                        bookingId,
                        customer.getCustomerID(),
                        paymentMethod,
                        discountCode
                );

        /*
         * =============================================
         * SUCCESS
         * =============================================
         */
        if (PaymentDAO.PAYMENT_SUCCESS
                .equals(result)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=payment-success"
            );

            return;
        }

        /*
         * =============================================
         * DISCOUNT ERROR
         * =============================================
         */
        if (PaymentDAO.INVALID_DISCOUNT
                .equals(result)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/payment?bookingId="
                    + bookingId
                    + "&error=invalid-discount"
            );

            return;
        }

        /*
         * =============================================
         * INVALID PAYMENT METHOD
         * =============================================
         */
        if (PaymentDAO.INVALID_METHOD
                .equals(result)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/payment?bookingId="
                    + bookingId
                    + "&error=invalid-method"
            );

            return;
        }

        /*
         * =============================================
         * ALREADY PAID
         * =============================================
         */
        if (PaymentDAO.ALREADY_PAID
                .equals(result)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=already-paid"
            );

            return;
        }

        /*
         * =============================================
         * BOOKING INVALID / NOT PENDING
         * =============================================
         */
        if (PaymentDAO.INVALID_BOOKING
                .equals(result)
                || PaymentDAO.BOOKING_NOT_PENDING
                        .equals(result)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=cannot-pay"
            );

            return;
        }

        /*
         * =============================================
         * GENERAL PAYMENT FAILURE
         * =============================================
         */
        response.sendRedirect(
                request.getContextPath()
                + "/payment?bookingId="
                + bookingId
                + "&error=payment-failed"
        );
    }

    private Integer parseBookingId(
            String raw) {

        if (raw == null
                || raw.trim().isEmpty()) {

            return null;
        }

        try {

            int id =
                    Integer.parseInt(
                            raw.trim()
                    );

            return id > 0
                    ? id
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }

    @Override
    public String getServletInfo() {
        return "Processes customer booking payments.";
    }
}