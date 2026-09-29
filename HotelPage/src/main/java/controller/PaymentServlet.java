/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.BookingDAO;
import dao.DiscountDAO;
import dao.PaymentDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import model.Booking;
import model.Customer;
import model.Discount;
import model.Payment;

/**
 *
 * @author Hà Thái Phát
 */
@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment"})
public class PaymentServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP GET and POST methods.
     * This default implementation outputs a simple HTML page.
     *
     * @param request the HttpServletRequest containing the request parameters
     * @param response the HttpServletResponse used to return the response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an input/output error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            // Basic HTML output (not used in practice for payment)
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet PaymentServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet PaymentServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP GET method.
     * Retrieves a booking by ID and forwards to the payment JSP page.
     *
     * @param request the HttpServletRequest containing the bookingId parameter
     * @param response the HttpServletResponse to render the payment view
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get bookingId from request parameter
        String bookingIdRaw = request.getParameter("bookingId");
        int bookingId = Integer.parseInt(bookingIdRaw);

        // Fetch booking details from DAO
        BookingDAO dao = new BookingDAO();
        Booking booking = dao.getBookingById(bookingId);

        // Send booking to JSP
        request.setAttribute("booking", booking);
        request.getRequestDispatcher("/WEB-INF/pay/payment.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP POST method.
     * Applies discount (if valid), inserts a new payment, and redirects to booking history.
     *
     * @param request the HttpServletRequest with booking/payment information
     * @param response the HttpServletResponse used to redirect after payment
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Retrieve bookingId from form
            int bookingId = Integer.parseInt(request.getParameter("bookingId"));

            BookingDAO bookingDAO = new BookingDAO();
            DiscountDAO dDAO = new DiscountDAO();

            // Get discount code from request
            String code = request.getParameter("discount-code").trim();

            Discount discount;

            // Handle discount: if empty, skip. Otherwise, validate.
            if (code.equals("")) {
                discount = null;
            } else {
                // Check if code is valid
                if (dDAO.getDiscountIDByCode(code) == null) {
                    response.sendRedirect("./payment?bookingId=" + bookingId + "&error=invalid-discount");
                    return;
                }

                // Get discount by ID
                int discountId = dDAO.getDiscountIDByCode(code);
                discount = dDAO.getDiscountById(discountId);

                // Apply discount only if still available
                if (discount != null && discount.getQuantity() > 0) {
                    BigDecimal saleOff = discount.getSaleOff();
                    bookingDAO.updateBookingTotalPriceWithDiscount(bookingId, saleOff);
                    dDAO.decreaseDiscountQuantity(discountId);
                } else {
                    response.sendRedirect("./payment?bookingId=" + bookingId + "&error=invalid-discount");
                    return;
                }
            }

            // Fetch booking from database
            Booking booking = bookingDAO.getBookingById(bookingId);

            // Create payment object and populate it
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setPaymentDate(LocalDate.now()); // today's date
            payment.setDiscount(discount);

            // Insert payment into DB
            PaymentDAO paymentDAO = new PaymentDAO();
            paymentDAO.insertPayment(payment);

            // Get customer from session
            HttpSession session = request.getSession();
            Customer customer = (Customer) session.getAttribute("customer");

            // Redirect to booking history if logged in
            if (customer != null) {
                int customerId = customer.getCustomerID();
                response.sendRedirect("history?id=" + customerId + "&view=bookings");
            } else {
                // Not logged in, redirect to login
                response.sendRedirect("./login");
            }

        } catch (Exception e) {
            // Log error and return HTTP 500 response
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payment failed.");
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Handles booking payments with optional discount codes";
    }// </editor-fold>

}
