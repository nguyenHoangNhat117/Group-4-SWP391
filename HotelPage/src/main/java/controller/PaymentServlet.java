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
import java.math.BigDecimal;

import model.Booking;
import model.Customer;
import model.User;
import util.OnlinePaymentConfig;
import util.OnlinePaymentGateway;
import util.OnlineStayPolicy;
import util.PayOSOrderCode;

@WebServlet(
        name = "PaymentServlet",
        urlPatterns = {"/payment"}
)
public class PaymentServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

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
         * Any non-cancelled booking with a remaining balance can be paid.
         */
        if ("cancelled".equalsIgnoreCase(booking.getStatus())) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/history?view=bookings"
                    + "&message=cannot-pay"
            );

            return;
        }

        PaymentDAO paymentDAO =
                new PaymentDAO();

        if (booking.isOnlineDepositPolicy()) {
            showStayCheckout(request, response, booking, paymentDAO);
            return;
        }

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

        BigDecimal paidAmount = paymentDAO.getTotalPaidAmount(bookingId);
        BigDecimal remainingAmount = booking.getTotalPrice().subtract(paidAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        boolean firstPayment = paidAmount.compareTo(BigDecimal.ZERO) == 0;
        Integer selectedPercent = getInitialChoice(session, bookingId);
        if (selectedPercent == null) {
            // Existing bookings made before this feature remain payable.
            selectedPercent = parsePaymentPercent(request.getParameter("percent"));
        }
        if (selectedPercent != null && selectedPercent == 0) {
            selectedPercent = 100; // Pay the full outstanding balance later.
        }
        if (!firstPayment) {
            selectedPercent = null;
        }

        request.setAttribute("paidAmount", paidAmount);
        request.setAttribute("remainingAmount", remainingAmount);
        request.setAttribute("firstPayment", firstPayment);
        request.setAttribute("selectedPercent", selectedPercent);

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
        if (!new BookingDAO().isBookingOwnedByCustomer(bookingId, customer.getCustomerID())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Booking booking = new BookingDAO().getBookingById(bookingId);
        if (booking == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

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

        Integer paymentPercent = parsePaymentPercent(
                request.getParameter("payment-percent")
        );

        PaymentDAO paymentDAO =
                new PaymentDAO();

        if (booking.isOnlineDepositPolicy()) {
            paymentMethod = "bank_transfer";
            paymentPercent = OnlineStayPolicy.percentage(request.getParameter("payment-plan"));
            if (!"pending".equals(booking.getStatus()) || paymentPercent == null) {
                response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + bookingId);
                return;
            }
        }

        BigDecimal alreadyPaid = paymentDAO.getTotalPaidAmount(bookingId);
        boolean firstPayment = alreadyPaid.compareTo(BigDecimal.ZERO) == 0;

        Integer initialChoice = getInitialChoice(session, bookingId);
        if (!booking.isOnlineDepositPolicy() && firstPayment && initialChoice != null) {
            if (initialChoice == 0) {
                // A no-deposit booking has no initial payment requirement;
                // later payment from history can pay the full amount.
                paymentPercent = 100;
            } else {
                paymentPercent = initialChoice;
            }
        }

        if (firstPayment && paymentPercent == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/payment?bookingId="
                    + bookingId
                    + "&error=invalid-payment-percent"
            );
            return;
        }

        // Online gateways create a pending payment first. Only a verified
        // webhook/IPN is allowed to mark it as paid.

        if ("bank_transfer".equals(paymentMethod)) {

            if (!OnlinePaymentConfig.callbacksConfigured()
                    || !OnlinePaymentConfig.payosConfigured()) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/payment?bookingId=" + bookingId
                        + "&error=online-not-configured"
                );
                return;
            }

            long paymentId = -1;
            String transactionCode = null;

            try {
                if (booking.isOnlineDepositPolicy()) {
                    PaymentDAO.PendingCheckout pending = paymentDAO.getPendingCheckout(bookingId);
                    if (pending != null) {
                        response.sendRedirect(pending.url == null
                                ? request.getContextPath() + "/payment?bookingId=" + bookingId : pending.url);
                        return;
                    }
                }
                paymentId = paymentDAO.createPendingOnlinePayment(
                        bookingId,
                        customer.getCustomerID(),
                        paymentMethod,
                        discountCode,
                        paymentPercent
                );
                if (paymentId == 0) {
                    response.sendRedirect(request.getContextPath() + "/history?view=bookings&message=payment-success");
                    return;
                }

                BigDecimal pendingAmount =
                        paymentDAO.getPaymentAmountById(paymentId);

                long gatewayAmount = pendingAmount
                        .setScale(0, java.math.RoundingMode.UNNECESSARY)
                        .longValueExact();

                transactionCode = paymentDAO.getTransactionCodeByPaymentId(paymentId);
                if (!PayOSOrderCode.usesProviderCode(transactionCode)) {
                    throw new IllegalStateException("Missing payOS order code for new payment");
                }
                String checkoutUrl = OnlinePaymentGateway.createPayOSLink(
                        paymentId,
                        PayOSOrderCode.forPayment(paymentId, transactionCode),
                        gatewayAmount,
                        OnlinePaymentConfig.publicBaseUrl()
                );
                if (booking.isOnlineDepositPolicy()) {
                    try {
                        paymentDAO.saveCheckoutUrl(paymentId, checkoutUrl);
                    } catch (java.sql.SQLException ex) {
                        getServletContext().log("Could not save the existing payOS checkout URL", ex);
                    }
                }

                response.sendRedirect(checkoutUrl);
                return;

            } catch (Exception ex) {
                getServletContext().log("Could not start payOS payment for booking " + bookingId, ex);

                if (paymentId > 0) {
                    try {
                        paymentDAO.markOnlinePaymentFailed(
                                transactionCode == null ? paymentDAO.getTransactionCodeByPaymentId(paymentId) : transactionCode,
                                paymentMethod
                        );
                    } catch (Exception ignored) {
                        // Keep the original payment error.
                    }
                }

                response.sendRedirect(
                        request.getContextPath()
                        + "/payment?bookingId=" + bookingId
                        + "&error=payment-failed"
                );
                return;
            }
        }

        String result =
                paymentDAO.processPayment(
                        bookingId,
                        customer.getCustomerID(),
                        paymentMethod,
                        discountCode,
                        paymentPercent
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

    private void showStayCheckout(HttpServletRequest request, HttpServletResponse response,
            Booking booking, PaymentDAO paymentDAO) throws ServletException, IOException {
        boolean awaitingPayment = "pending".equals(booking.getStatus());
        RoomStayServlet.setCheckoutSummary(request, booking.getTotalPrice(), awaitingPayment);
        request.setAttribute("existingBooking", booking);
        request.setAttribute("quantity", booking.getDetails().size());
        request.setAttribute("guests", booking.getDetails().stream().mapToInt(model.BookingDetail::getGuestCount).sum());
        if (!booking.getDetails().isEmpty()) {
            request.setAttribute("checkIn", booking.getDetails().get(0).getCheckInDate());
            request.setAttribute("checkOut", booking.getDetails().get(0).getCheckOutDate());
        }
        BigDecimal paid = paymentDAO.getTotalPaidAmount(booking.getId());
        request.setAttribute("paidAmount", paid);
        request.setAttribute("balanceAmount", booking.getTotalPrice().subtract(paid).max(BigDecimal.ZERO));
        request.setAttribute("awaitingPayment", awaitingPayment);
        try {
            PaymentDAO.PendingCheckout pending = awaitingPayment ? paymentDAO.getPendingCheckout(booking.getId()) : null;
            if (pending != null) {
                request.setAttribute("total", pending.total);
                request.setAttribute("pendingDue", pending.amount);
                request.setAttribute("pendingBalance", pending.total.subtract(pending.amount));
                request.setAttribute("pendingCheckoutUrl", pending.url);
                request.setAttribute("paymentProcessing", pending.url == null);
            }
        } catch (java.sql.SQLException ex) {
            throw new ServletException("Unable to load pending payment", ex);
        }
        request.getRequestDispatcher("/WEB-INF/booking/checkout.jsp").forward(request, response);
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

    @SuppressWarnings("unchecked")
    private Integer getInitialChoice(HttpSession session, Integer bookingId) {
        if (session == null || bookingId == null) return null;
        Object choices = session.getAttribute("bookingInitialChoices");
        if (!(choices instanceof java.util.Map)) return null;
        Object choice = ((java.util.Map<?, ?>) choices).get(bookingId);
        return choice instanceof Integer ? (Integer) choice : null;
    }

    private Integer parsePaymentPercent(String raw) {

        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        try {
            int value = Integer.parseInt(raw.trim());
            return (value == 10 || value == 20 || value == 100)
                    ? value
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
