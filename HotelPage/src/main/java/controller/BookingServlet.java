package controller;

import dao.BookingDAO;
import dao.RoomDAO;
import dao.RoomTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import model.Customer;
import model.Room;
import model.RoomType;
import model.User;
import util.OnlineStayDates;
import util.ReportPeriod;

@WebServlet(urlPatterns = {"/booking"})
public class BookingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String roomNumber = request.getParameter("roomNumber");
        String typeId = request.getParameter("roomTypeId");
        if (roomNumber == null && typeId == null) {
            request.setAttribute("roomTypes", new RoomTypeDAO().getAllActive());
            request.getRequestDispatcher("/WEB-INF/booking/search.jsp").forward(request, response);
            return;
        }

        RoomType type = resolveType(typeId, roomNumber);
        if (type == null) {
            response.sendRedirect(request.getContextPath() + "/booking?error=room-unavailable");
            return;
        }

        HttpSession session = request.getSession(true);
        if (!requireCustomer(request, response, session, type.getId())) {
            return;
        }

        LocalDate from = (LocalDate) session.getAttribute("checkInDate");
        LocalDate to = (LocalDate) session.getAttribute("checkOutDate");
        if (!OnlineStayDates.isValid(from, to, ReportPeriod.today())) {
            response.sendRedirect(request.getContextPath() + "/booking?error=invalid-dates");
            return;
        }

        try {
            new BookingDAO().expirePendingBookings();
            int available = new RoomDAO().countAvailableRoomsByType(from, to, type.getId());
            if (available == 0) {
                response.sendRedirect(request.getContextPath() + "/booking?error=already-booked");
                return;
            }
            request.setAttribute("available", Math.min(1000, available));
        } catch (SQLException ex) {
            throw new ServletException("Không thể kiểm tra số phòng còn trống.", ex);
        }

        request.setAttribute("roomType", type);
        request.setAttribute("nights", ChronoUnit.DAYS.between(from, to));
        request.setAttribute("checkoutCsrfToken", util.AuthSupport.token(session));
        request.getRequestDispatcher("/WEB-INF/booking/booking.jsp").forward(request, response);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        RoomType type = resolveType(request.getParameter("roomTypeId"), request.getParameter("room-number"));
        if (type == null) {
            response.sendRedirect(request.getContextPath() + "/booking?error=room-unavailable");
            return;
        }

        HttpSession session = request.getSession(true);
        if (!requireCustomer(request, response, session, type.getId())) {
            return;
        }

        String bookingPath = request.getContextPath() + "/booking?roomTypeId=" + type.getId();
        if (!util.AuthSupport.checkCsrf(request, response)) {
            return;
        }
        if (!"yes".equals(request.getParameter("booking-consent"))) {
            response.sendRedirect(bookingPath + "&error=consent");
            return;
        }
        LocalDate from = (LocalDate) session.getAttribute("checkInDate");
        LocalDate to = (LocalDate) session.getAttribute("checkOutDate");
        if (!OnlineStayDates.isValid(from, to, ReportPeriod.today())) {
            response.sendRedirect(request.getContextPath() + "/booking?error=invalid-dates");
            return;
        }

        int quantity = number(request.getParameter("quantity"), 1);
        int guests = number(request.getParameter("guest-count"), 1);
        if (quantity < 1 || guests < quantity || guests > 1000 || guests > (long) quantity * type.getCapacity()) {
            response.sendRedirect(bookingPath + "&error=invalid-guests");
            return;
        }

        int prepaymentPercent = number(request.getParameter("prepayment-percent"), -1);
        if (prepaymentPercent != 0 && prepaymentPercent != 10 && prepaymentPercent != 20 && prepaymentPercent != 100) {
            response.sendRedirect(bookingPath + "&error=invalid-payment-option");
            return;
        }

        String specialRequest = request.getParameter("special-request");
        specialRequest = specialRequest == null || specialRequest.isBlank() ? null : specialRequest.trim();
        if (specialRequest != null && specialRequest.length() > 500) {
            response.sendRedirect(bookingPath + "&error=create-failed");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        int bookingId;
        try {
            bookingDAO.expirePendingBookings();
            bookingId = bookingDAO.createBooking(((Customer) session.getAttribute("customer")).getCustomerID(),
                    type.getId(), from, to, quantity, guests, type.getPricePerNight(), specialRequest);
        } catch (IllegalArgumentException ex) {
            response.sendRedirect(bookingPath + "&error=already-booked");
            return;
        } catch (SQLException ex) {
            log("Không thể tạo đơn theo loại phòng.", ex);
            response.sendRedirect(bookingPath + "&error=create-failed");
            return;
        }

        session.removeAttribute("availableRooms");
        Map<Integer, Integer> initialChoices = (Map<Integer, Integer>) session.getAttribute("bookingInitialChoices");
        if (initialChoices == null) {
            initialChoices = new HashMap<>();
            session.setAttribute("bookingInitialChoices", initialChoices);
        }
        initialChoices.put(bookingId, prepaymentPercent);

        if (prepaymentPercent == 0) {
            bookingDAO.confirmBooking(bookingId);
            response.sendRedirect(request.getContextPath() + "/history?view=bookings&message=booking-created");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + bookingId + "&percent=" + prepaymentPercent);
    }

    private static RoomType resolveType(String typeId, String roomNumber) {
        if (typeId != null) {
            int id = number(typeId, 0);
            return id > 0 ? new RoomTypeDAO().getActiveRoomTypeById(id) : null;
        }
        int number = number(roomNumber, 0);
        Room room = number > 0 ? new RoomDAO().getRoomByNumber(number) : null;
        return room == null ? null : new RoomTypeDAO().getActiveRoomTypeById(room.getRoomType().getId());
    }

    private static boolean requireCustomer(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int typeId) throws IOException {
        Object loggedUser = session.getAttribute("loggedUser");
        if (!(loggedUser instanceof User)) {
            session.setAttribute("pendingBookingUrl", "/booking?roomTypeId=" + typeId);
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!"customer".equalsIgnoreCase(((User) loggedUser).getRole())
                || !(session.getAttribute("customer") instanceof Customer)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private static int number(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
