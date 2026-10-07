package controller;

import dao.BookingDAO;
import dao.RoomDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import model.Customer;
import model.Room;
import model.User;

@WebServlet(urlPatterns = {"/booking"})
public class BookingServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        /*
         * UC07 Search Rooms is public. Guest may open /booking to search.
         * UC09 Create Booking still requires an authenticated Customer.
         */
        String roomNumberParam = request.getParameter("roomNumber");

        if (roomNumberParam == null || roomNumberParam.trim().isEmpty()) {
            request.getRequestDispatcher(
                    "/WEB-INF/booking/search.jsp"
            ).forward(request, response);
            return;
        }

        if (session == null
                || !(session.getAttribute("loggedUser") instanceof User)) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (!"customer".equalsIgnoreCase(loggedUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        LocalDate checkInDate
                = (LocalDate) session.getAttribute(
                        "checkInDate"
                );

        LocalDate checkOutDate
                = (LocalDate) session.getAttribute(
                        "checkOutDate"
                );

        if (checkInDate == null || checkOutDate == null) {

            request.getRequestDispatcher(
                    "/WEB-INF/booking/search.jsp"
            ).forward(request, response);

            return;
        }

        int roomNumber;

        try {
            roomNumber = Integer.parseInt(
                    roomNumberParam
            );
        } catch (NumberFormatException e) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking"
            );
            return;
        }

        RoomDAO roomDAO = new RoomDAO();

        Room room
                = roomDAO.getRoomByNumber(roomNumber);

        /*
         * Room does not exist.
         */
        if (room == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking"
            );
            return;
        }

        /*
         * Room must be operationally bookable.
         */
        if (!"available".equalsIgnoreCase(
                room.getStatus())) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=room-unavailable"
            );
            return;
        }

        /*
         * Recheck conflict before showing confirmation page.
         */
        BookingDAO bookingDAO
                = new BookingDAO();

        if (bookingDAO.isRoomBookedInRange(
                roomNumber,
                checkInDate,
                checkOutDate)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=already-booked"
            );
            return;
        }

        /*
         * Send selected room and dates to JSP.
         */
        request.setAttribute("room", room);
        request.setAttribute(
                "checkInDate",
                checkInDate
        );
        request.setAttribute(
                "checkOutDate",
                checkOutDate
        );

        request.getRequestDispatcher(
                "/WEB-INF/booking/booking.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session
                = request.getSession(false);

        /*
         * Require login.
         */
        if (session == null
                || session.getAttribute("loggedUser") == null
                || session.getAttribute("customer") == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login"
            );
            return;
        }

        User loggedUser
                = (User) session.getAttribute(
                        "loggedUser"
                );

        /*
         * UC09 belongs to Customer.
         */
        if (!"customer".equalsIgnoreCase(
                loggedUser.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        String roomNumberParam
                = request.getParameter(
                        "room-number"
                );

        if (roomNumberParam == null
                || roomNumberParam.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=missing-room"
            );
            return;
        }

        int roomNumber;

        try {
            roomNumber = Integer.parseInt(
                    roomNumberParam.trim()
            );
        } catch (NumberFormatException e) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=invalid-room"
            );
            return;
        }

        /*
         * Dates must come from server-side session,
         * not hidden fields supplied by client.
         */
        LocalDate checkInDate
                = (LocalDate) session.getAttribute(
                        "checkInDate"
                );

        LocalDate checkOutDate
                = (LocalDate) session.getAttribute(
                        "checkOutDate"
                );

        if (checkInDate == null
                || checkOutDate == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=missing-dates"
            );
            return;
        }

        /*
         * Validate date range again.
         */
        if (!checkOutDate.isAfter(checkInDate)
                || checkInDate.isBefore(
                        LocalDate.now()
                )) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=invalid-dates"
            );
            return;
        }

        RoomDAO roomDAO = new RoomDAO();

        Room room
                = roomDAO.getRoomByNumber(
                        roomNumber
                );

        if (room == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=room-not-found"
            );
            return;
        }

        /*
         * A room under maintenance/out_of_service
         * cannot be booked.
         */
        if (!"available".equalsIgnoreCase(
                room.getStatus())) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=room-unavailable"
            );
            return;
        }

        /*
         * Guest count.
         *
         * If the form does not send guest-count yet,
         * use 1 temporarily.
         */
        int guestCount = 1;

        String guestCountParam
                = request.getParameter(
                        "guest-count"
                );

        if (guestCountParam != null
                && !guestCountParam.trim().isEmpty()) {

            try {
                guestCount = Integer.parseInt(
                        guestCountParam.trim()
                );
            } catch (NumberFormatException e) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/booking?roomNumber="
                        + roomNumber
                        + "&error=invalid-guests"
                );
                return;
            }
        }

        /*
         * Capacity validation required by RDS/SDS.
         */
        if (guestCount <= 0
                || guestCount
                > room.getRoomType().getCapacity()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?roomNumber="
                    + roomNumber
                    + "&error=invalid-guests"
            );
            return;
        }

        /*
         * Special request is optional.
         */
        String specialRequest
                = request.getParameter(
                        "special-request"
                );

        if (specialRequest != null) {
            specialRequest
                    = specialRequest.trim();

            if (specialRequest.isEmpty()) {
                specialRequest = null;
            }
        }

        /*
         * Calculate number of nights.
         */
        long nights
                = ChronoUnit.DAYS.between(
                        checkInDate,
                        checkOutDate
                );

        if (nights <= 0) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?error=invalid-dates"
            );
            return;
        }

        BigDecimal pricePerNight
                = room.getRoomType()
                        .getPricePerNight();

        BigDecimal totalPrice
                = pricePerNight.multiply(
                        BigDecimal.valueOf(nights)
                );

        Customer customer
                = (Customer) session.getAttribute(
                        "customer"
                );

        BookingDAO bookingDAO
                = new BookingDAO();

        /*
         * First recheck.
         *
         * createBooking() also performs another
         * availability check in its transaction.
         */
        if (bookingDAO.isRoomBookedInRange(
                roomNumber,
                checkInDate,
                checkOutDate)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?roomNumber="
                    + roomNumber
                    + "&error=already-booked"
            );
            return;
        }

        boolean created
                = bookingDAO.createBooking(
                        customer.getCustomerID(),
                        roomNumber,
                        checkInDate,
                        checkOutDate,
                        pricePerNight,
                        guestCount,
                        totalPrice,
                        specialRequest
                );

        if (!created) {

            /*
             * Database trigger may also reject:
             * - overlapping reservation
             * - guest count > capacity
             */
            response.sendRedirect(
                    request.getContextPath()
                    + "/booking?roomNumber="
                    + roomNumber
                    + "&error=create-failed"
            );
            return;
        }

        /*
         * Search result is now stale.
         * Remove it so the next search is refreshed.
         */
        session.removeAttribute(
                "availableRooms"
        );

        /*
         * UC09 creates pending Booking.
         * Customer should continue to booking history/payment,
         * not Room Management.
         */
        response.sendRedirect(
                request.getContextPath()
                + "/history"
        );
    }

    @Override
    public String getServletInfo() {
        return "Handles customer room booking.";
    }
}