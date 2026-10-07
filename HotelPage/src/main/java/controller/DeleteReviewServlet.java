package controller;

import dao.ReviewDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import model.User;

@WebServlet(
        name = "DeleteReviewServlet",
        urlPatterns = {"/admin/reviews/moderate"}
)
public class DeleteReviewServlet
        extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        HttpSession session =
                request.getSession(false);

        /*
         * Authentication.
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
         * Admin only.
         */
        if (!"admin".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        /*
         * ===============================================
         * 1. REVIEW ID
         * ===============================================
         */
        Integer reviewId =
                parsePositiveInt(
                        request.getParameter(
                                "reviewID"
                        )
                );

        /*
         * ===============================================
         * 2. ACTION
         *
         * approve
         * reject
         * ===============================================
         */
        String action =
                request.getParameter(
                        "action"
                );

        if (reviewId == null
                || action == null
                || action.trim().isEmpty()) {

            redirect(
                    request,
                    response,
                    "invalid-review"
            );

            return;
        }

        ReviewDAO reviewDAO =
                new ReviewDAO();

        boolean success;

        switch (action) {

            case "approve":

                success =
                        reviewDAO.approveReview(
                                reviewId
                        );

                break;

            case "reject":

                success =
                        reviewDAO.rejectReview(
                                reviewId
                        );

                break;

            default:

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                return;
        }

        /*
         * ===============================================
         * 3. RESULT
         * ===============================================
         */
        if (!success) {

            redirect(
                    request,
                    response,
                    "update-failed"
            );

            return;
        }

        if ("approve".equals(action)) {

            redirect(
                    request,
                    response,
                    "approved"
            );

        } else {

            redirect(
                    request,
                    response,
                    "rejected"
            );
        }
    }

    /*
     * Prevent moderation through GET.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/admin/reviews"
        );
    }

    private Integer parsePositiveInt(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            int id =
                    Integer.parseInt(
                            value.trim()
                    );

            return id > 0
                    ? id
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private void redirect(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/admin/reviews"
                + "?message="
                + message
        );
    }

    @Override
    public String getServletInfo() {

        return "Admin review approval/rejection.";
    }
}