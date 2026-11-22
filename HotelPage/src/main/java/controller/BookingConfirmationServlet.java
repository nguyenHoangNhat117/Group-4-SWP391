package controller;

import dao.BookingDAO;
import dao.DiscountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import model.BookingContact;
import model.Customer;
import model.OnlineBookingDraft;
import util.AuthSupport;
import util.OnlineStayPolicy;

@WebServlet(OnlineBookingDraft.PATH)
public class BookingConfirmationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response, false);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (AuthSupport.checkCsrf(request, response)) {
            handle(request, response, true);
        }
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, boolean confirm)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object customer = session == null ? null : session.getAttribute("customer");
        if (!(customer instanceof Customer)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String key = request.getParameter("draft");
        if (key == null || !key.matches("[0-9a-f-]{36}")) {
            response.sendRedirect(request.getContextPath() + "/room");
            return;
        }
        int customerId = ((Customer) customer).getCustomerID();
        BookingDAO bookings = new BookingDAO();
        boolean back = confirm && "yes".equals(request.getParameter("back-to-rooms"));
        Map<String, String> submittedFields = confirm ? BookingContactForm.read(request) : Map.of();
        OnlineBookingDraft draft;
        Map<String, String> savedFields;
        synchronized (session) {
            Object stored = session.getAttribute(OnlineBookingDraft.SESSION_ATTRIBUTE);
            draft = stored instanceof OnlineBookingDraft && key.equals(((OnlineBookingDraft) stored).getToken())
                    ? (OnlineBookingDraft) stored : null;
            if (draft != null && !draft.claim(customerId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            savedFields = draft == null ? Map.of() : draft.getContactFields();
        }
        BookingContactForm.show(request, (Customer) customer, confirm ? submittedFields : savedFields, null);
        Integer percent = OnlineStayPolicy.percentage(request.getParameter("payment-plan"));
        try {
            int bookingId = bookings.findOnlineBooking(customerId, key);
            if (bookingId > 0) {
                if (confirm && !back) {
                    forwardToPayment(request, response, bookingId);
                } else {
                    response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + bookingId);
                }
                return;
            }
            if (draft == null) {
                response.sendRedirect(request.getContextPath() + "/room");
                return;
            }
            if (!confirm) {
                show(request, response, draft, percent);
                return;
            }
            if (back) {
                synchronized (session) {
                    if (session.getAttribute(OnlineBookingDraft.SESSION_ATTRIBUTE) == draft) {
                        draft.saveContact(submittedFields, null);
                    }
                }
                response.setStatus(HttpServletResponse.SC_SEE_OTHER);
                response.setHeader("Location", request.getContextPath() + "/room");
                return;
            }
            BookingContact contact = null;
            try {
                contact = BookingContactForm.validate(submittedFields);
            } catch (IllegalArgumentException ex) {
                request.setAttribute("contactError", true);
            }
            boolean consent = "yes".equals(request.getParameter("booking-consent"));
            String voucherCode = voucherCode(request);
            BigDecimal payable = voucherCode == null ? draft.getTotal()
                    : new DiscountDAO().getOnlineVoucherTotals(draft.getTotal()).get(voucherCode);
            if (payable == null) {
                request.setAttribute("voucherError", true);
            }
            boolean validPayment = percent != null && (percent != OnlineStayPolicy.DEPOSIT_PERCENT
                    || payable == null || OnlineStayPolicy.canDeposit(payable));
            request.setAttribute("consentError", !consent);
            request.setAttribute("paymentChoiceError", !validPayment);
            synchronized (session) {
                if (session.getAttribute(OnlineBookingDraft.SESSION_ATTRIBUTE) != draft) {
                    bookingId = bookings.findOnlineBooking(customerId, key);
                } else {
                    draft.saveContact(submittedFields, contact);
                    if (contact != null && consent && validPayment && payable != null) {
                        bookingId = bookings.confirmOnlineSelection(customerId, draft, percent, voucherCode, payable);
                        session.removeAttribute(OnlineBookingDraft.SESSION_ATTRIBUTE);
                    } else {
                        bookingId = 0;
                    }
                }
            }
            if (bookingId > 0) {
                forwardToPayment(request, response, bookingId);
            } else if (contact == null || !consent || !validPayment || payable == null) {
                show(request, response, draft, percent);
            } else {
                response.sendRedirect(request.getContextPath() + "/room");
            }
        } catch (BookingDAO.SelectionConflict ex) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            if ("voucher".equals(ex.getReason())) {
                request.setAttribute("voucherError", true);
            } else {
                request.setAttribute("selectionConflict", ex);
            }
            show(request, response, draft, percent);
        } catch (SQLException ex) {
            if (draft != null && !response.isCommitted()) {
                log("Unable to confirm online booking", ex);
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                request.setAttribute("confirmationUnavailable", true);
                show(request, response, draft, percent);
                return;
            }
            throw new ServletException("Unable to confirm online booking", ex);
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response, OnlineBookingDraft draft, Integer percent)
            throws ServletException, IOException {
        Map<String, BigDecimal> vouchers = new DiscountDAO().getOnlineVoucherTotals(draft.getTotal());
        String selectedVoucher = voucherCode(request);
        BigDecimal payable = selectedVoucher == null ? draft.getTotal()
                : vouchers.getOrDefault(selectedVoucher, draft.getTotal());
        boolean canDeposit = OnlineStayPolicy.canDeposit(payable);
        boolean depositSelected = canDeposit && !Integer.valueOf(OnlineStayPolicy.FULL_PERCENT).equals(percent);
        request.setAttribute("confirmationDraft", draft);
        request.setAttribute("confirmationCsrfToken", AuthSupport.token(request.getSession(false)));
        request.setAttribute("canDeposit", canDeposit);
        request.setAttribute("voucherTotals", vouchers);
        request.setAttribute("selectedVoucher", selectedVoucher);
        request.setAttribute("discountAmount", draft.getTotal().subtract(payable));
        request.setAttribute("payableTotal", payable);
        request.setAttribute("selectedPlan", depositSelected ? "deposit" : "transfer");
        request.setAttribute("paymentDue", depositSelected ? OnlineStayPolicy.deposit(payable) : payable);
        request.getRequestDispatcher("/WEB-INF/booking/booking-confirmation.jsp").forward(request, response);
    }

    private static String voucherCode(HttpServletRequest request) {
        String code = request.getParameter("discount-code");
        return code == null || code.isBlank() ? null : code.strip();
    }

    private void forwardToPayment(HttpServletRequest request, HttpServletResponse response, int bookingId)
            throws ServletException, IOException {
        request.getRequestDispatcher("/payment?bookingId=" + bookingId + "&payment-method=bank_transfer")
                .forward(request, response);
    }
}
