/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.BookingDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Customer;

/**
 * Servlet that handles the cancellation of a booking made by a customer.
 * 
 * URL mapping: /history/cancel-booking
 * 
 * Author: Đặng Hoàng Vũ
 */
@WebServlet(name = "CancelBookingServlet", urlPatterns = {"/history/cancel-booking"})
public class CancelBookingServlet extends HttpServlet {

    /**
     * This method is auto-generated for demo HTML purposes and is not used in real logic.
     *
     * @param request  HttpServletRequest from the client
     * @param response HttpServletResponse to send content to the client
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        // Output a sample HTML page
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CancelBookingServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CancelBookingServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles HTTP GET requests. Used to display the booking cancellation confirmation page.
     *
     * @param request  HttpServletRequest object containing the booking ID parameter
     * @param response HttpServletResponse to forward to the JSP view
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If the "id" parameter is missing or empty, redirect to homepage
        if (request.getParameter("id") == null || request.getParameter("id").trim().equals("")) {
            response.sendRedirect("./");
            return;
        }

        // Forward the request to the JSP confirmation page
        request.getRequestDispatcher("/WEB-INF/history/cancelBooking/cancel-booking.jsp").forward(request, response);
    }

    /**
     * Handles HTTP POST requests. Cancels a booking if a valid ID is provided.
     * After cancellation, it redirects back to the user's booking history.
     *
     * @param request  HttpServletRequest object containing booking ID to cancel
     * @param response HttpServletResponse object to redirect to booking history
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get current session
        HttpSession session = request.getSession();

        // Get the logged-in customer object from session
        Customer customer = (Customer) session.getAttribute("customer");

        // Try parsing the booking ID from the request
        int id = Integer.parseInt(request.getParameter("id"));

        // If the ID is valid (greater than 0), attempt to delete the booking
        if (id > 0) {
            BookingDAO bDAO = new BookingDAO(); // DAO for booking operations
            bDAO.deleteBooking(id); // Delete booking by ID
        }

        // Redirect back to the user's booking history after deletion
        response.sendRedirect(request.getContextPath() + "/history?id=" + customer.getCustomerID() + "&view=bookings");

        // Note: You had a commented-out try-catch for NumberFormatException; it's not needed here
    }

    /**
     * Provides a short description of this servlet.
     *
     * @return A brief description string
     */
    @Override
    public String getServletInfo() {
        return "Servlet that cancels customer bookings and redirects to booking history.";
    }// </editor-fold>
}
