package controller;

import dao.PaymentDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import util.AuthRateLimiter;
import util.OnlinePaymentGateway;

@WebServlet("/payment/return")
public class OnlinePaymentReturnServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String message = "payment-returned-checking";
        if ("1".equals(req.getParameter("cancel"))) {
            message = "payment-cancelled";
        } else if ("payos".equals(req.getParameter("gateway")) && "PAID".equals(req.getParameter("status"))
                && AuthRateLimiter.allow("payos-return:" + req.getRemoteAddr(), 30, 60_000)) {
            // The browser only supplies lookup keys. Confirm the amount and status with payOS.
            try {
                long paymentId = Long.parseLong(req.getParameter("paymentId"));
                long orderCode = Long.parseLong(req.getParameter("orderCode"));
                if (paymentId > 0 && paymentId <= Integer.MAX_VALUE && orderCode > Integer.MAX_VALUE) {
                    PaymentDAO payments = new PaymentDAO();
                    long amount = payments.findPendingPayOSAmount(paymentId, orderCode);
                    if (OnlinePaymentGateway.isPayOSOrderPaid(orderCode, amount)
                            && payments.completePayOSPayment(orderCode, amount)) {
                        message = "payment-success";
                    }
                }
            } catch (NumberFormatException ignored) {
                // A return without valid lookup keys cannot trigger reconciliation.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (SQLException | IOException | ArithmeticException ex) {
                log("Could not verify the returned payOS payment", ex);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/history?view=bookings&message=" + message);
    }
}
