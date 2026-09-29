package controller;

import dao.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * Servlet that handles deletion of a review by its ID.
 * 
 * URL pattern: /reports/delete-reviews
 * 
 * This servlet accepts POST requests to delete a review, and redirects
 * to the reviews page after successful deletion.
 * 
 * GET requests are redirected to the reviews list page.
 */
@WebServlet(name = "DeleteReviewServlet", urlPatterns = {"/reports/delete-reviews"})
public class DeleteReviewServlet extends HttpServlet {

    /**
     * Handles HTTP POST requests for deleting a review.
     *
     * @param request  the HttpServletRequest containing the review ID to delete
     * @param response the HttpServletResponse used to redirect or return error
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        try {
            // Parse the reviewID from the request parameters
            int reviewID = Integer.parseInt(request.getParameter("reviewID"));

            // Create DAO instance to interact with the database
            ReviewDAO dao = new ReviewDAO();

            // Call the delete method to remove the review from DB
            dao.deleteReview(reviewID);

            // ✅ Redirect to the review list page after successful deletion
            response.sendRedirect(request.getContextPath() + "/reports?view=reviews");
        } catch (Exception e) {
            // Print the error for debugging
            e.printStackTrace();

            // Output error message to client
            response.getWriter().println("Error deleting review: " + e.getMessage());
        }
    }

    /**
     * Handles HTTP GET requests by redirecting to the reviews list.
     * 
     * GET is not supported for deletion, so this ensures safe redirection.
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // Redirect GET requests to the review list page to prevent deletion
        response.sendRedirect(request.getContextPath() + "/admin/reports?view=reviews");
    }
}
