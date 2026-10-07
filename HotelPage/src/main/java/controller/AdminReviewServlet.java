package controller;

import dao.ReviewDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

import model.Review;
import model.User;

@WebServlet(
        name = "AdminReviewServlet",
        urlPatterns = {"/admin/reviews"}
)
public class AdminReviewServlet
        extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        /*
         * AuthenticationFilter already protects /admin/*,
         * but servlet checks again for safety.
         */
        if (session == null
                || session.getAttribute("loggedUser") == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login"
            );

            return;
        }

        User user =
                (User) session.getAttribute(
                        "loggedUser"
                );

        /*
         * Review Management is Admin-only.
         */
        if (!"admin".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        ReviewDAO reviewDAO =
                new ReviewDAO();

        List<Review> reviews =
                reviewDAO.getAllReviews();

        request.setAttribute(
                "reviews",
                reviews
        );

        request.getRequestDispatcher(
                "/WEB-INF/reports/reviews.jsp"
        ).forward(
                request,
                response
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Moderation is handled by:
         *
         * /admin/reviews/moderate
         */
        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED
        );
    }

    @Override
    public String getServletInfo() {

        return "Admin Review Management.";
    }
}