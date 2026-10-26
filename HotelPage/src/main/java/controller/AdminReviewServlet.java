package controller;

import dao.ReviewDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(
        name = "AdminReviewServlet",
        urlPatterns = {"/admin/reviews"}
)
public class AdminReviewServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        // AuthenticationFilter also protects this route.
        HttpSession session = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute("loggedUser");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        if (!"admin".equalsIgnoreCase(user.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        try {
            request.setAttribute(
                    "reviews",
                    new ReviewDAO().getAllReviews()
            );

        } catch (IllegalStateException ex) {

            getServletContext().log(
                    "Unable to load Admin review management.",
                    ex
            );

            response.setStatus(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE
            );

            request.setAttribute(
                    "reviewLoadError",
                    "Review data is temporarily unavailable. "
                    + "Please try again later."
            );
        }

        request.getRequestDispatcher(
                "/WEB-INF/reports/reviews.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Moderation remains handled by /admin/reviews/moderate.
        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED
        );
    }

    @Override
    public String getServletInfo() {
        return "Admin review management.";
    }
}
