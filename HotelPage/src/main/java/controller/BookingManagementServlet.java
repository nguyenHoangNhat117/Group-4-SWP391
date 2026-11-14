package controller;

import dao.BookingDAO;
import dao.OnlineCheckInDAO;
import dao.PaymentDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import model.Booking;
import model.User;

@WebServlet(
        name = "BookingManagementServlet",
        urlPatterns = {"/booking-management"}
)
public class BookingManagementServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE = 10;

    /*
     * =========================================================
     * GET
     *
     * list
     * detail
     * =========================================================
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user
                = getAuthorizedUser(
                        request
                );

        /*
         * Staff + Admin only.
         */
        if (user == null) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        String view
                = request.getParameter(
                        "view"
                );

        /*
         * Default view.
         */
        if (view == null
                || view.trim().isEmpty()
                || "list".equals(view)) {

            showBookingList(
                    request,
                    response
            );

            return;
        }

        if ("detail".equals(view)) {

            showBookingDetail(
                    request,
                    response
            );

            return;
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }


    /*
     * =========================================================
     * LIST / SEARCH / FILTER
     * =========================================================
     */
    private void showBookingList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        BookingDAO bookingDAO
                = new BookingDAO();

        String keyword
                = request.getParameter(
                        "keyword"
                );

        if (keyword != null) {

            keyword
                    = keyword.trim();
        }

        String status
                = request.getParameter(
                        "status"
                );

        if (status != null) {

            status
                    = status.trim();
        }

        /*
         * Invalid / empty status becomes null.
         */
        if (!isValidBookingStatus(
                status)) {

            status = null;
        }

        String roomInput = request.getParameter("room-number");
        String source = request.getParameter("source");
        if (!"online".equals(source) && !"offline".equals(source)) {
            source = null;
        }
        Integer roomNumber = parseId(roomInput == null ? null : roomInput.trim());
        boolean invalidRoom = roomInput != null && !roomInput.isBlank() && roomNumber == null;

        LocalDate fromDate
                = parseDate(
                        request.getParameter(
                                "from-date"
                        )
                );

        LocalDate toDate
                = parseDate(
                        request.getParameter(
                                "to-date"
                        )
                );

        /*
         * Date range validation.
         */
        boolean invalidDates = (fromDate == null && hasFilterValue(request, "from-date"))
                || (toDate == null && hasFilterValue(request, "to-date"))
                || (fromDate != null && toDate != null && toDate.isBefore(fromDate));
        boolean invalidFilters = invalidDates || invalidRoom;

        int currentPage
                = parsePositiveInt(
                        request.getParameter(
                                "page"
                        ),
                        1
                );

        int totalBookings
                = invalidFilters ? 0 : bookingDAO.countBookings(
                        keyword,
                        status,
                        fromDate,
                        toDate,
                        roomNumber,
                        source
                );

        int totalPages
                = totalBookings == 0
                        ? 0
                        : (int) Math.ceil(
                                totalBookings
                                / (double) PAGE_SIZE
                        );

        if (totalPages > 0
                && currentPage > totalPages) {

            currentPage
                    = totalPages;
        }

        List<Booking> bookings
                = invalidFilters ? List.of() : bookingDAO.searchBookings(
                        keyword,
                        status,
                        fromDate,
                        toDate,
                        roomNumber,
                        source,
                        currentPage,
                        PAGE_SIZE
                );

        request.setAttribute(
                "bookings",
                bookings
        );

        request.setAttribute(
                "keyword",
                keyword
        );
        request.setAttribute("roomInput", roomInput);
        request.setAttribute("selectedSource", source);
        request.setAttribute("invalidRoom", invalidRoom);
        request.setAttribute("invalidDates", invalidDates);

        request.setAttribute(
                "selectedStatus",
                status
        );

        request.setAttribute(
                "fromDate",
                fromDate
        );

        request.setAttribute(
                "toDate",
                toDate
        );

        request.setAttribute(
                "currentPage",
                currentPage
        );

        request.setAttribute(
                "totalPages",
                totalPages
        );

        request.setAttribute(
                "totalBookings",
                totalBookings
        );

        request.getRequestDispatcher(
                "/WEB-INF/booking-management/list.jsp"
        ).forward(
                request,
                response
        );
    }

    private boolean hasFilterValue(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value != null && !value.isBlank();
    }


    /*
     * =========================================================
     * BOOKING DETAIL
     * =========================================================
     */
    private void showBookingDetail(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Integer bookingId
                = parseId(
                        request.getParameter(
                                "id"
                        )
                );

        if (bookingId == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            return;
        }

        BookingDAO bookingDAO
                = new BookingDAO();

        Booking booking
                = bookingDAO.getBookingById(
                        bookingId
                );

        if (booking == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        PaymentDAO paymentDAO
                = new PaymentDAO();

        boolean paid
                = paymentDAO.isBookingPaid(
                        bookingId
                );

        List<model.Payment> payments
                = paymentDAO.getPaymentsByBookingId(
                        bookingId
                );

        request.setAttribute(
                "booking",
                booking
        );

        try {
            request.setAttribute(
                    "offlineCreator",
                    new dao.OfflineBookingDAO().creator(bookingId)
            );
        } catch (java.sql.SQLException e) {
            log(
                    "Offline booking audit unavailable; check UC09 migration",
                    e
            );
        }

        request.setAttribute(
                "paid",
                paid
        );

        request.setAttribute(
                "payments",
                payments
        );

        /*
         * Used by JSP to display only valid
         * next status options.
         */
        String[] nextStatuses = getNextStatuses(booking.getStatus());
        if (request.getAttribute("offlineCreator") != null) {
            nextStatuses = new String[0];
        } else if (booking.isOnlineDepositPolicy()) {
            nextStatuses = new String[0];
            if (java.util.Arrays.asList("confirmed", "checked_in", "checked_out", "completed")
                    .contains(booking.getStatus())) {
                try {
                    CounterPaymentServlet.prepareCounter(request, bookingId);
                } catch (SQLException ex) {
                    log("Counter actions unavailable for booking " + bookingId, ex);
                    request.setAttribute("counterUnavailable", true);
                }
            }
        }
        request.setAttribute("nextStatuses", nextStatuses);
        try {
            request.setAttribute("roomTypeNames", bookingDAO.getCurrentRoomTypeNames(bookingId));
        } catch (SQLException ex) {
            throw new ServletException("Unable to load assigned room types", ex);
        }

        request.getRequestDispatcher(
                "/WEB-INF/booking-management/detail.jsp"
        ).forward(
                request,
                response
        );
    }


    /*
     * =========================================================
     * POST
     *
     * Update Booking Status.
     * =========================================================
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        User user
                = getAuthorizedUser(
                        request
                );

        if (user == null) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        if (!util.AuthSupport.checkCsrf(request,response)) return;

        String action
                = request.getParameter(
                        "action"
                );

        if (!"update-status".equals(
                action)) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            return;
        }

        Integer bookingId
                = parseId(
                        request.getParameter(
                                "booking-id"
                        )
                );

        String newStatus
                = request.getParameter(
                        "status"
                );

        if (bookingId == null
                || !isValidBookingStatus(
                        newStatus)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking-management"
                    + "?message=invalid-data"
            );

            return;
        }

        if ("checked_in".equals(newStatus)) {
            String message;
            try {
                OnlineCheckInDAO.Result result = new OnlineCheckInDAO().checkIn(bookingId);
                message = result == OnlineCheckInDAO.Result.SUCCESS
                        ? "status-updated"
                        : "online-check-in-" + result.name().toLowerCase(Locale.ROOT).replace('_', '-');
            } catch (SQLException ex) {
                log("Unable to update online stay " + bookingId, ex);
                message = "online-check-in-failed";
            }
            response.sendRedirect(request.getContextPath() + "/booking-management?view=detail&id="
                    + bookingId + "&message=" + message);
            return;
        }

        BookingDAO bookingDAO
                = new BookingDAO();

        Booking booking
                = bookingDAO.getBookingById(
                        bookingId
                );

        if (booking == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking-management"
                    + "?message=booking-not-found"
            );

            return;
        }

        /*
         * Important payment rule:
         *
         * A pending booking should normally become
         * confirmed through successful payment.
         *
         * Therefore Staff/Admin should not manually
         * confirm an unpaid booking.
         */
        if ("pending".equals(
                booking.getStatus())
                && "confirmed".equals(
                        newStatus)) {

            PaymentDAO paymentDAO
                    = new PaymentDAO();

            if (!paymentDAO.isBookingPaid(
                    bookingId)) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/booking-management"
                        + "?view=detail"
                        + "&id="
                        + bookingId
                        + "&message=payment-required"
                );

                return;
            }
        }

        /*
         * BookingDAO checks valid transitions:
         *
         * pending -> confirmed/cancelled
         * confirmed -> checked_in/cancelled
         * checked_in -> checked_out
         * checked_out -> completed
         */
        boolean updated
                = bookingDAO.updateBookingStatus(
                        bookingId,
                        newStatus
                );

        if (!updated) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking-management"
                    + "?view=detail"
                    + "&id="
                    + bookingId
                    + "&message=invalid-transition"
            );

            return;
        }

        response.sendRedirect(
                request.getContextPath()
                + "/booking-management"
                + "?view=detail"
                + "&id="
                + bookingId
                + "&message=status-updated"
        );
    }


    /*
     * =========================================================
     * AUTHORIZATION
     *
     * UC22:
     * Staff + Admin.
     * =========================================================
     */
    private User getAuthorizedUser(
            HttpServletRequest request) {

        HttpSession session
                = request.getSession(false);

        if (session == null) {

            return null;
        }

        User user
                = (User) session.getAttribute(
                        "loggedUser"
                );

        if (user == null) {

            return null;
        }

        if (!"staff".equalsIgnoreCase(
                user.getRole())
                && !"admin".equalsIgnoreCase(
                        user.getRole())) {

            return null;
        }

        return user;
    }


    /*
     * =========================================================
     * NEXT VALID STATUS
     *
     * Used only for UI.
     *
     * DAO performs final validation.
     * =========================================================
     */
    private String[] getNextStatuses(
            String currentStatus) {

        if (currentStatus == null) {

            return new String[0];
        }

        switch (currentStatus) {

            case "pending":

                return new String[]{
                    "confirmed",
                    "cancelled"
                };

            case "confirmed":

                return new String[]{
                    "checked_in",
                    "cancelled"
                };

            case "checked_in":

                return new String[]{
                    "checked_out"
                };

            case "checked_out":

                return new String[]{
                    "completed"
                };

            case "completed":
            case "cancelled":
            default:

                return new String[0];
        }
    }


    /*
     * =========================================================
     * STATUS VALIDATION
     * =========================================================
     */
    private boolean isValidBookingStatus(
            String status) {

        if (status == null
                || status.trim().isEmpty()) {

            return false;
        }

        return "pending".equals(status)
                || "confirmed".equals(status)
                || "cancelled".equals(status)
                || "checked_in".equals(status)
                || "checked_out".equals(status)
                || "completed".equals(status);
    }


    /*
     * =========================================================
     * PARSE DATE
     * =========================================================
     */
    private LocalDate parseDate(
            String raw) {

        if (raw == null
                || raw.trim().isEmpty()) {

            return null;
        }

        try {

            return LocalDate.parse(
                    raw.trim()
            );

        } catch (DateTimeParseException e) {

            return null;
        }
    }


    /*
     * =========================================================
     * PARSE ID
     * =========================================================
     */
    private Integer parseId(
            String raw) {

        if (raw == null
                || raw.trim().isEmpty()) {

            return null;
        }

        try {

            int id
                    = Integer.parseInt(
                            raw.trim()
                    );

            return id > 0
                    ? id
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }


    /*
     * =========================================================
     * PARSE POSITIVE INTEGER
     * =========================================================
     */
    private int parsePositiveInt(
            String raw,
            int defaultValue) {

        if (raw == null
                || raw.trim().isEmpty()) {

            return defaultValue;
        }

        try {

            int value
                    = Integer.parseInt(
                            raw.trim()
                    );

            return value > 0
                    ? value
                    : defaultValue;

        } catch (NumberFormatException e) {

            return defaultValue;
        }
    }

    @Override
    public String getServletInfo() {

        return "Staff/Admin Booking Management.";
    }
}
