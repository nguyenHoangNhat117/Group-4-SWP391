package controller;

import com.fasterxml.jackson.databind.JsonNode;
import dao.PaymentDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.OnlinePaymentGateway;
import java.io.IOException;

@WebServlet("/payment/payos-webhook")
public class PayOSWebhookServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            StringBuilder body = new StringBuilder();
            String line;
            try (java.io.BufferedReader r = req.getReader()) {
                while ((line = r.readLine()) != null) {
                    body.append(line);
                }
            }
            JsonNode root = OnlinePaymentGateway.parse(body.toString());
            if (!OnlinePaymentGateway.verifyPayOSWebhook(root)) {
                resp.sendError(400, "Invalid signature");
                return;
            }
            JsonNode data = root.path("data");
            if (!"00".equals(root.path("code").asText())
                    || !root.path("success").asBoolean()) {
                resp.setStatus(200);
                resp.getWriter().write("{\"received\":true}");
                return;
            }
            long paymentId = data.path("orderCode").asLong(-1);
            long amount = data.path("amount").asLong(-1);
            if (paymentId <= 0 || amount <= 0) {
                resp.sendError(400, "Invalid payment data");
                return;
            }
            PaymentDAO dao = new PaymentDAO();
            String code = dao.getTransactionCodeByPaymentId(paymentId);
            if (code == null || !dao.completeOnlinePayment(code, amount, "bank_transfer")) {
                resp.sendError(400, "Payment could not be reconciled");
                return;
            }
            resp.setStatus(200);
            resp.getWriter().write("{\"received\":true}");
        } catch (Exception ex) {
            ex.printStackTrace();
            resp.sendError(400, "Webhook processing failed");
        }
    }
}
