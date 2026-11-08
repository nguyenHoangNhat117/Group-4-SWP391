package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Browser return is informational only; it never confirms payment. */
@WebServlet("/payment/return")
public class OnlinePaymentReturnServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException {
        String context=req.getContextPath();
        if("1".equals(req.getParameter("cancel"))) resp.sendRedirect(context+"/history?view=bookings&message=payment-cancelled");
        else resp.sendRedirect(context+"/history?view=bookings&message=payment-returned-checking");
    }
}