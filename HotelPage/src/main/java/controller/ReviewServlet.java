package controller;

import dao.BookingDAO;
import dao.ReviewDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

import model.Customer;
import model.User;

@WebServlet(
        name = "ReviewServlet",
        urlPatterns = {"/review"}
)
public class ReviewServlet
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
         * ===============================================
         * 1. REQUIRE CUSTOMER LOGIN
         * ===============================================
         */
        if (session == null
                || session.getAttribute(
                        "loggedUser"
                ) == null
                || session.getAttribute(
                        "customer"
                ) == null) {

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

        Customer customer =
                (Customer) session.getAttribute(
                        "customer"
                );

        if (!"customer".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        /*
         * ===============================================
         * 2. ROOM NUMBER
         * ===============================================
         */
        Integer roomNumber =
                parsePositiveInt(
                        request.getParameter(
                                "room-number"
                        )
                );

        if (roomNumber == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            return;
        }

        /*
         * ===============================================
         * 3. STAR
         * ===============================================
         */
        BigDecimal star;

        try {

            star =
                    new BigDecimal(
                            request.getParameter(
                                    "star"
                            )
                    );

        } catch (Exception e) {

            redirectRoom(
                    request,
                    response,
                    roomNumber,
                    "invalid-star"
            );

            return;
        }

        if (star.compareTo(
                BigDecimal.ONE) < 0
                || star.compareTo(
                        new BigDecimal("5")) > 0) {

            redirectRoom(
                    request,
                    response,
                    roomNumber,
                    "invalid-star"
            );

            return;
        }

        /*
         * ===============================================
         * 4. COMMENT
         * ===============================================
         */
        String comment =
                request.getParameter(
                        "comment"
                );

        if (comment != null) {

            comment =
                    comment.trim();

            if (comment.isEmpty()) {
                comment = null;
            }

            if (comment != null
                    && comment.length() > 1000) {

                comment =
                        comment.substring(
                                0,
                                1000
                        );
            }
        }

        /*
         * ===============================================
         * 5. FIND LATEST COMPLETED BOOKING
         *
         * BookingDAO method was already fixed earlier:
         * - CustomerID
         * - RoomNumber through BookingDetail
         * - Booking.Status = completed
         * - no existing Review
         * ===============================================
         */
        BookingDAO bookingDAO =
                new BookingDAO();

        Integer bookingId =
                bookingDAO
                        .getLatestCompletedBookingId(
                                customer.getCustomerID(),
                                roomNumber
                        );

        if (bookingId == null) {

            redirectRoom(
                    request,
                    response,
                    roomNumber,
                    "not-book-yet"
            );

            return;
        }

        ReviewDAO reviewDAO =
                new ReviewDAO();

        /*
         * Double-check eligibility.
         */
        if (!reviewDAO.canCustomerReview(
                bookingId,
                customer.getCustomerID(),
                roomNumber)) {

            if (reviewDAO
                    .isBookingReviewed(
                            bookingId)) {

                redirectRoom(
                        request,
                        response,
                        roomNumber,
                        "already-review"
                );

            } else {

                redirectRoom(
                        request,
                        response,
                        roomNumber,
                        "not-book-yet"
                );
            }

            return;
        }

        /*
         * ===============================================
         * 6. INSERT REVIEW
         *
         * ReviewStatus = pending
         * ===============================================
         */
        boolean created =
                reviewDAO.addReview(
                        bookingId,
                        comment,
                        star
                );

        if (!created) {

            redirectRoom(
                    request,
                    response,
                    roomNumber,
                    "review-failed"
            );

            return;
        }

        /*
         * Review now waits for Admin moderation.
         */
        redirectRoom(
                request,
                response,
                roomNumber,
                "review-pending"
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED
        );
    }

    private Integer parsePositiveInt(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            int number =
                    Integer.parseInt(
                            value.trim()
                    );

            return number > 0
                    ? number
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private void redirectRoom(
            HttpServletRequest request,
            HttpServletResponse response,
            int roomNumber,
            String error)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/details?roomNumber="
                + roomNumber
                + "&error="
                + error
                + "#review-title"
        );
    }
}