package controller;

import dao.BookingDAO;
import java.io.IOException;
import java.math.BigDecimal;
import dao.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Customer;
import model.Review;

/**
 * Servlet to handle review submissions.
 * It allows customers to leave reviews for rooms they have booked and completed.
 * 
 * Author: Diệp Vỹ Khang
 */
@WebServlet(name = "ReviewServlet", urlPatterns = {"/review"})
public class ReviewServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     * Submits a review if the booking has been completed and not yet reviewed.
     *
     * @param request the {@link HttpServletRequest} that contains form data including room number, star rating, and comment.
     * @param response the {@link HttpServletResponse} used to send the response to the client.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set encoding to support Unicode characters
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            // Retrieve the current session and logged-in customer
            HttpSession session = request.getSession();
            Customer customer = (Customer) session.getAttribute("customer");

            // Get the room number from the request parameter
            int roomNumber = Integer.parseInt(request.getParameter("room-number"));

            // Check if customer has a completed booking for the room
            BookingDAO bDAO = new BookingDAO();
            Integer bookingID = bDAO.getLatestCompletedBookingId(customer.getCustomerID(), roomNumber);

            // If no completed booking exists, redirect with error
            if (bookingID == null) {
                response.sendRedirect("./details?error=not-book-yet&roomNumber=" + roomNumber + "#review-title");
                return;
            }

            // Check if a review has already been submitted for this booking
            ReviewDAO rDAO = new ReviewDAO();
            if (rDAO.isBookingReviewed(bookingID)) {
                response.sendRedirect("./details?error=already-review&roomNumber=" + roomNumber + "#review-title");
                return;
            }

            // Extract review comment and star rating from the form
            String comment = request.getParameter("comment");
            BigDecimal star = new BigDecimal(request.getParameter("star"));

            // Save the review to the database
            ReviewDAO dao = new ReviewDAO();
            dao.addReview(bookingID, comment, star);

            // Redirect back to the room details page
            response.sendRedirect("details?roomNumber=" + roomNumber);

        } catch (Exception e) {
            // Print the error to server log and display it on the browser
            e.printStackTrace();
            response.getWriter().println("An error occurred while submitting your review: " + e.getMessage());
        }
    }
}
