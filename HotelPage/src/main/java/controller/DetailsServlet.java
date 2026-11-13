package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.AmenityDAO;
import dao.BookingDAO;
import dao.ReviewDAO;
import dao.RoomDAO;
import dao.RoomTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import model.Review;
import model.Room;
import model.RoomType;
import model.User;
import util.OnlineStayDates;
import util.ReportPeriod;

@WebServlet(name = "DetailsServlet", urlPatterns = {"/details"})
public class DetailsServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int MAX_GUESTS = 1000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDetails(request, response, false);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        handleDetails(request, response, "review".equals(request.getParameter("action")));
    }

    private void handleDetails(HttpServletRequest request, HttpServletResponse response, boolean review)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        boolean quoteOnly = "availability".equals(request.getParameter("action"));
        HttpSession session = request.getSession(true);
        User user = session.getAttribute("loggedUser") instanceof User
                ? (User) session.getAttribute("loggedUser") : null;
        boolean customer = user == null || "customer".equalsIgnoreCase(user.getRole());
        boolean admin = user != null && "admin".equalsIgnoreCase(user.getRole());
        if (review && !customer) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        RoomDAO rooms = new RoomDAO();
        String roomNumber = request.getParameter("roomNumber");
        Room room = roomNumber == null ? null : rooms.getRoomByNumber(number(roomNumber, 0));
        RoomType type = roomNumber == null
                ? new RoomTypeDAO().getActiveRoomTypeById(number(request.getParameter("roomTypeId"), 0))
                : room == null ? null : room.getRoomType();
        if (type == null || (room != null && !admin && !rooms.isRoomViewable(room.getRoomNumber()))) {
            if (quoteOnly) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                String path = session.getAttribute("checkInDate") == null
                        ? "/booking?error=room-unavailable" : "/room?page-index=1&error=room-unavailable";
                response.sendRedirect(request.getContextPath() + path);
            }
            return;
        }

        LocalDate today = ReportPeriod.today();
        LocalDate checkIn = (LocalDate) session.getAttribute("checkInDate");
        LocalDate checkOut = (LocalDate) session.getAttribute("checkOutDate");
        if (!OnlineStayDates.isValid(checkIn, checkOut, today)) {
            checkIn = today;
            checkOut = today.plusDays(1);
        }

        String rawRange = request.getParameter("booking-dates");
        boolean explicitDates = rawRange != null || request.getParameter("checkIn") != null
                || request.getParameter("checkOut") != null || quoteOnly || review;
        if (explicitDates) {
            if (rawRange != null && !review && !quoteOnly) {
                String[] parts = rawRange.trim().split("\\s+-\\s+");
                checkIn = parts.length == 2 ? parseSearchDate(parts[0]) : null;
                checkOut = parts.length == 2 ? parseSearchDate(parts[1]) : null;
            } else {
                checkIn = OnlineStayDates.parseIsoDate(request.getParameter("checkIn"));
                checkOut = OnlineStayDates.parseIsoDate(request.getParameter("checkOut"));
            }
        }

        int quantity = number(request.getParameter("quantity"), 1);
        int searchGuests = session.getAttribute("guestCount") instanceof Integer
                ? (Integer) session.getAttribute("guestCount") : 1;
        String rawGuests = request.getParameter("guests");
        if (rawGuests == null && !review && !quoteOnly) {
            rawGuests = request.getParameter("guest-count");
            if (rawGuests == null) {
                rawGuests = request.getParameter("guestCount");
            }
        }
        int guests = number(rawGuests, Math.max(1, Math.min(searchGuests, type.getCapacity())));
        boolean validDates = OnlineStayDates.isValid(checkIn, checkOut, today);
        boolean validQuantity = quantity >= 1 && guests >= quantity && guests <= MAX_GUESTS
                && (long) guests <= (long) quantity * type.getCapacity();
        int available = 0;
        long nights = validDates ? ChronoUnit.DAYS.between(checkIn, checkOut) : 0;
        BigDecimal total = BigDecimal.ZERO;
        if (validDates) {
            if (!quoteOnly) {
                new BookingDAO().expirePendingBookings();
            }
            try {
                available = rooms.countAvailableRoomsByType(checkIn, checkOut, type.getId());
            } catch (SQLException ex) {
                throw new ServletException("Unable to load room availability", ex);
            }
            if (validQuantity) {
                total = type.getPricePerNight().multiply(BigDecimal.valueOf(nights * quantity));
            }
        }

        boolean canBook = validDates && validQuantity && available >= quantity;
        int maxRooms = Math.min(available, MAX_GUESTS);
        int displayQuantity = Math.max(1, Math.min(quantity, maxRooms));
        int maxGuests = (int) Math.min(MAX_GUESTS, (long) displayQuantity * type.getCapacity());
        String error = !validDates ? "room.details.dates.error"
                : !validQuantity ? "room.quantity.error"
                : !canBook ? "room.details.stock.error" : "";
        if (quoteOnly) {
            response.setContentType("application/json");
            JSON.writeValue(response.getOutputStream(), Map.of(
                    "available", available, "nights", nights, "total", total,
                    "canBook", canBook, "error", error, "maxRooms", maxRooms));
            return;
        }

        if (validDates && !"invalid-dates".equals(request.getParameter("error"))) {
            session.setAttribute("dates", OnlineStayDates.formatSearchRange(checkIn, checkOut));
            session.setAttribute("checkInDate", checkIn);
            session.setAttribute("checkOutDate", checkOut);
        }
        if (review && canBook) {
            request.getRequestDispatcher("/checkout?action=review&roomTypeId=" + type.getId()
                    + "&checkIn=" + checkIn + "&checkOut=" + checkOut
                    + "&quantity=" + quantity + "&guests=" + guests).forward(request, response);
            return;
        }

        if (error.isEmpty()) {
            String redirectError = request.getParameter("error");
            if ("availability".equals(redirectError)) {
                error = "room.availability.error";
            } else if ("quantity".equals(redirectError)) {
                error = "room.quantity.error";
            } else if ("invalid-dates".equals(redirectError)) {
                error = "room.details.dates.error";
            }
        }

        if (room != null) {
            ReviewDAO reviews = new ReviewDAO();
            List<Review> approved = reviews.getReviewsByRoomNumber(room.getRoomNumber());
            request.setAttribute("reviews", approved);
            request.setAttribute("averageStar", approved.isEmpty() ? BigDecimal.ZERO
                    : reviews.getAverageStarByRoomNumber(room.getRoomNumber()));
            request.setAttribute("staffRoomAvailable", validDates && validQuantity
                    && guests <= type.getCapacity()
                    && "available".equalsIgnoreCase(room.getStatus())
                    && !new BookingDAO().isRoomBookedInRange(room.getRoomNumber(), checkIn, checkOut));
        }
        request.setAttribute("room", room);
        request.setAttribute("roomType", type);
        try {
            request.setAttribute("roomSpecifications", new RoomTypeDAO().getSpecifications(type.getId()));
        } catch (SQLException ex) {
            throw new ServletException("Unable to load room specifications", ex);
        }
        request.setAttribute("amenities", new AmenityDAO().getAmenitiesByRoomTypeId(type.getId()));
        request.setAttribute("checkInDate", checkIn);
        request.setAttribute("checkOutDate", checkOut);
        request.setAttribute("dateRange", validDates ? OnlineStayDates.formatSearchRange(checkIn, checkOut) : "");
        request.setAttribute("quantity", displayQuantity);
        request.setAttribute("guestCount", Math.max(displayQuantity, Math.min(guests, maxGuests)));
        request.setAttribute("maxRooms", maxRooms);
        request.setAttribute("maxGuests", maxGuests);
        request.setAttribute("nights", nights);
        request.setAttribute("totalStayPrice", total);
        request.setAttribute("available", available);
        request.setAttribute("canBook", canBook);
        request.setAttribute("stayError", error);
        request.getRequestDispatcher("/WEB-INF/details/details.jsp").forward(request, response);
    }

    private static int number(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static LocalDate parseSearchDate(String value) {
        try {
            return OnlineStayDates.parseSearchDate(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
