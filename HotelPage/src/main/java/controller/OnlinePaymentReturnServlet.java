package controller;

import dao.PaymentDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Customer;
import util.OnlinePaymentConfig;
import util.OnlinePaymentGateway;
import util.PayOSOrderCode;

/** The browser return never confirms payment. A cancelled link is verified with payOS. */
@WebServlet("/payment/return")
public class OnlinePaymentReturnServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException {
        String context = req.getContextPath();
        boolean cancelled = "1".equals(req.getParameter("cancel")) || "true".equals(req.getParameter("cancel"));
        String message = cancelled ? "payment-cancelled" : "payment-returned-checking";
        jakarta.servlet.http.HttpSession session = req.getSession(false);
        Object owner = session == null ? null : session.getAttribute("customer");
        if (owner instanceof Customer && "payos".equals(req.getParameter("gateway"))) {
            try {
                long paymentId = Long.parseLong(req.getParameter("paymentId"));
                PaymentDAO payments = new PaymentDAO();
                Integer bookingId = payments.getOnlineStayBookingForPayment(paymentId, ((Customer) owner).getCustomerID());
                if (bookingId != null) {
                    if (cancelled && OnlinePaymentConfig.payosConfigured()) {
                        String code = payments.getTransactionCodeByPaymentId(paymentId);
                        if (code != null && OnlinePaymentGateway.payOSLinkCancelled(PayOSOrderCode.forPayment(paymentId, code))) {
                            payments.markOnlinePaymentFailed(code, "bank_transfer");
                        }
                    }
                    resp.sendRedirect(context + "/payment?bookingId=" + bookingId + "&message=" + message);
                    return;
                }
            } catch (NumberFormatException ignored) {
                // A missing order identifier falls back to history.
            } catch (Exception ex) {
                getServletContext().log("Could not reconcile the payOS browser return", ex);
            }
        }
        resp.sendRedirect(context + "/history?view=bookings&message=" + message);
    }
}
