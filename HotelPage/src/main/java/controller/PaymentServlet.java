package controller;

import com.fasterxml.jackson.databind.JsonNode;
import dao.BookingDAO;
import dao.DiscountDAO;
import dao.PaymentDAO;
import jakarta.servlet.DispatcherType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;

import model.Booking;
import model.Customer;
import model.Payment;
import model.User;
import util.OnlinePaymentConfig;
import util.OnlinePaymentGateway;
import util.PayOSCheckoutStore;

@WebServlet(
        name = "PaymentServlet",
        urlPatterns = {"/payment"}
)
public class PaymentServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private PayOSCheckoutStore checkoutStore() {
        String serverDirectory = System.getProperty("catalina.base", System.getProperty("user.home"));
        String appDirectory = getServletContext().getContextPath().replace('/', '_');
        return new PayOSCheckoutStore(Path.of(serverDirectory, "luxe-payos-checkouts", "app" + appDirectory));
    }

    private void showOnlineQr(HttpServletRequest request, HttpServletResponse response, Customer customer,
            Booking booking) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        boolean statusOnly = "qr-status".equals(request.getParameter("view"));
        String state = "unavailable";
        long deadline = 0;
        String checkoutUrl = null;
        String returnUrl = OnlinePaymentConfig.callbacksConfigured()
                ? OnlinePaymentConfig.publicBaseUrl() + qrPath(booking.getId()) : "";
        BigDecimal amount = null;
        try {
            PaymentDAO payments = new PaymentDAO();
            Instant checkedAt = Instant.now();
            long seconds = payments.getOnlineHoldSeconds(booking.getId(), customer.getCustomerID());
            deadline = checkedAt.plusSeconds(seconds).toEpochMilli();
            Payment payment = payments.findPendingPayOSPayment(booking.getId(), customer.getCustomerID());
            if (payment != null && OnlinePaymentConfig.payosConfigured()) {
                amount = payment.getAmount();
                long orderCode = payments.getPayOSOrderCode(payment.getId());
                long expectedAmount = amount.longValueExact();
                JsonNode order = OnlinePaymentGateway.getPayOSOrder(orderCode, expectedAmount);
                String status = order.path("status").asText();
                String linkId = order.path("id").asText();
                if ("PAID".equals(status) && order.path("amountPaid").asLong(-1) == expectedAmount
                        && order.path("amountRemaining").asLong(-1) == 0) {
                    if (statusOnly) {
                        writeQrStatus(response, "paid");
                        return;
                    }
                    // Use the same verified reconciliation as the hosted return.
                    response.sendRedirect(request.getContextPath() + "/payment/return?gateway=payos&status=PAID"
                            + "&paymentId=" + payment.getId() + "&orderCode=" + orderCode);
                    return;
                }
                if ("CANCELLED".equals(status) || ("cancelled".equals(booking.getStatus())
                        && !"Expired pending booking".equals(booking.getCancellationReason()))) {
                    state = "cancelled";
                } else if ("EXPIRED".equals(status) || Instant.now().toEpochMilli() >= deadline) {
                    state = "expired";
                } else if ("PENDING".equals(status) && linkId.matches("[a-fA-F0-9]{32}")) {
                    checkoutUrl = "https://pay.payos.vn/web/" + linkId;
                    JsonNode saved = null;
                    if (!statusOnly) {
                        try {
                            saved = checkoutStore().load(orderCode, Instant.now());
                        } catch (IOException ex) {
                            log("Unable to read saved payOS checkout " + orderCode, ex);
                        }
                    }
                    if (saved != null) {
                        JsonNode details = saved.path("data");
                        String originalUrl = details.path("checkoutUrl").asText();
                        if (linkId.equals(details.path("paymentLinkId").asText())
                                && expectedAmount == details.path("amount").asLong(-1)
                                && isPayOSCheckoutUrl(originalUrl)) {
                            checkoutUrl = originalUrl;
                            returnUrl = saved.path("returnUrl").asText(returnUrl);
                            request.setAttribute("qrPayload", details.path("qrCode").asText());
                            request.setAttribute("qrBankBin", details.path("bin").asText());
                            request.setAttribute("qrAccountNumber", details.path("accountNumber").asText());
                            request.setAttribute("qrAccountName", details.path("accountName").asText());
                            request.setAttribute("qrDescription", details.path("description").asText());
                        }
                    }
                    state = "pending";
                } else if ("PROCESSING".equals(status)) {
                    state = "checking";
                }
            } else if ("cancelled".equals(booking.getStatus())
                    && !"Expired pending booking".equals(booking.getCancellationReason())) {
                state = "cancelled";
            } else if ("confirmed".equals(booking.getStatus())) {
                if (statusOnly) {
                    writeQrStatus(response, "paid");
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/history?view=bookings");
                return;
            } else if (seconds == 0) {
                state = "expired";
            } else if (payment == null) {
                state = "missing";
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (SQLException | IOException | ArithmeticException ex) {
            log("Unable to look up payOS checkout for booking " + booking.getId(), ex);
        }
        if ("unavailable".equals(state) && deadline > 0 && Instant.now().toEpochMilli() >= deadline) {
            state = "expired";
        }
        if (statusOnly) {
            writeQrStatus(response, state);
            return;
        }
        request.setAttribute("qrBookingId", booking.getId());
        request.setAttribute("qrState", state);
        request.setAttribute("qrCheckoutUrl", checkoutUrl);
        request.setAttribute("qrReturnUrl", returnUrl);
        request.setAttribute("qrAmount", amount);
        request.setAttribute("qrSeconds", Math.max(0, (deadline - Instant.now().toEpochMilli() + 999) / 1000));
        request.getRequestDispatcher("/WEB-INF/pay/embedded-checkout.jsp").forward(request, response);
    }

    private static void writeQrStatus(HttpServletResponse response, String state) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(state);
    }

    private void startOnlineQr(HttpServletRequest request, HttpServletResponse response, PaymentDAO payments,
            Customer customer, Booking booking, String voucher, Integer percent) throws IOException {
        if (!OnlinePaymentConfig.callbacksConfigured() || !OnlinePaymentConfig.payosConfigured()) {
            response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + booking.getId()
                    + "&error=online-not-configured");
            return;
        }
        if (percent == null || (percent != 50 && percent != 100)) {
            response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + booking.getId()
                    + "&error=invalid-payment-percent");
            return;
        }
        long paymentId = -1;
        try {
            int customerId = customer.getCustomerID();
            if (payments.findPendingPayOSPayment(booking.getId(), customerId) == null) {
                Instant checkedAt = Instant.now();
                long seconds = payments.getOnlineHoldSeconds(booking.getId(), customerId);
                if (seconds > 0) {
                    paymentId = payments.createPendingOnlinePayment(booking.getId(), customerId,
                            "bank_transfer", voucher, percent);
                    long amount = payments.getPaymentAmountById(paymentId).longValueExact();
                    long orderCode = payments.getPayOSOrderCode(paymentId);
                    String returnUrl = OnlinePaymentConfig.publicBaseUrl() + qrPath(booking.getId());
                    Instant expiresAt = checkedAt.plusSeconds(seconds);
                    JsonNode details = OnlinePaymentGateway.createEmbeddedPayOSLink(paymentId, orderCode,
                            amount, returnUrl, expiresAt.getEpochSecond());
                    String checkoutUrl = details.path("checkoutUrl").asText();
                    if (isPayOSCheckoutUrl(checkoutUrl)) {
                        PayOSCheckoutStore store = checkoutStore();
                        store.save(orderCode, details, returnUrl, expiresAt);
                        store.removeExpired(Instant.now());
                    }
                }
            }
        } catch (OnlinePaymentGateway.PayOSLinkRejectedException ex) {
            try {
                payments.markOnlinePaymentFailed(payments.getTransactionCodeByPaymentId(paymentId), "bank_transfer");
            } catch (SQLException cleanupError) {
                log("Unable to release rejected payOS payment " + paymentId, cleanupError);
            }
            response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + booking.getId()
                    + "&error=payment-failed");
            return;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            // A timeout can leave a valid provider link. Keep the same order code for lookup.
            log("Unable to start payOS checkout for booking " + booking.getId(), ex);
        }
        response.sendRedirect(request.getContextPath() + qrPath(booking.getId()));
    }

    private static String qrPath(int bookingId) {
        return "/payment?bookingId=" + bookingId;
    }

    private static boolean isPayOSCheckoutUrl(String url) {
        return url.startsWith("https://pay.payos.vn/") || url.startsWith("https://next.pay.payos.vn/");
    }

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

        if (booking.isOnlineDepositPolicy()) {
            showOnlineQr(request, response, customer, booking);
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

        Booking onlineBooking = new BookingDAO().getBookingById(bookingId);
        if (onlineBooking != null && onlineBooking.isOnlineDepositPolicy()) {
            if (request.getDispatcherType() != DispatcherType.FORWARD
                    || !"confirm".equals(request.getParameter("action"))
                    || !"bank_transfer".equals(paymentMethod)) {
                response.sendRedirect(request.getContextPath() + qrPath(bookingId));
                return;
            }
            startOnlineQr(request, response, paymentDAO, customer, onlineBooking, discountCode, paymentPercent);
            return;
        }

        BigDecimal alreadyPaid = paymentDAO.getTotalPaidAmount(bookingId);
        boolean firstPayment = alreadyPaid.compareTo(BigDecimal.ZERO) == 0;

        Integer initialChoice = getInitialChoice(session, bookingId);
        if (firstPayment && initialChoice != null) {
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
        
// Online payment through payOS only.
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
    String checkoutUrl;

    try {
        paymentId = paymentDAO.createPendingOnlinePayment(
                bookingId,
                customer.getCustomerID(),
                paymentMethod,
                discountCode,
                paymentPercent
        );

        BigDecimal pendingAmount =
                paymentDAO.getPaymentAmountById(paymentId);

        long gatewayAmount = pendingAmount
                .setScale(0, java.math.RoundingMode.UNNECESSARY)
                .longValueExact();

        checkoutUrl = OnlinePaymentGateway.createPayOSLink(
                paymentId,
                paymentDAO.getPayOSOrderCode(paymentId),
                gatewayAmount,
                OnlinePaymentConfig.publicBaseUrl()
        );

    } catch (Exception ex) {
        ex.printStackTrace();

        if (paymentId > 0) {
            try {
                paymentDAO.markOnlinePaymentFailed(
                        paymentDAO.getTransactionCodeByPaymentId(paymentId),
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

    response.sendRedirect(checkoutUrl);
    return;
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
            return (value == 10 || value == 20 || value == 50 || value == 100)
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
