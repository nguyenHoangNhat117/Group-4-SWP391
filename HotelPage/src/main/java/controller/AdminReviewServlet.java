package controller;

// Import the ReviewDAO class to interact with the database for Review-related operations
import dao.ReviewDAO;

import java.io.IOException;

// Import servlet-related packages
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.util.List;

// Import the Review model class
import model.Review;

/**
 * This servlet handles administrative access to reports,
 * specifically review-related data.
 * 
 * Mapped to the URL pattern "/admin/reports".
 */
@WebServlet(name = "AdminReviewServlet", urlPatterns = {"/admin/reports"})
public class AdminReviewServlet extends HttpServlet {

    /**
     * Handles HTTP GET requests to display admin reports.
     * 
     * Depending on the value of the "view" parameter, this method
     * retrieves data and forwards it to the appropriate JSP page.
     *
     * @param request  the HttpServletRequest object containing client request data
     * @param response the HttpServletResponse object for sending responses to the client
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an input or output error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // Retrieve the "view" parameter from the request to determine which report to show
        String view = request.getParameter("view");

        // If the requested view is "reviews", handle the logic for displaying reviews
        if ("reviews".equals(view)) {
            // Create an instance of ReviewDAO to interact with the database
            ReviewDAO dao = new ReviewDAO();

            // Get a list of all reviews from the database
            List<Review> reviews = dao.getAllReviews();

            // Set the list of reviews as a request attribute so it can be accessed in the JSP
            request.setAttribute("reviews", reviews);

            // Forward the request to the JSP page that will display the reviews
            request.getRequestDispatcher("/WEB-INF/reports/reviews.jsp").forward(request, response);
        }

        // If the "view" parameter does not match any known value, return a 404 error
        // Additional view types such as "payments", "bookings", etc. can be added here
        else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
