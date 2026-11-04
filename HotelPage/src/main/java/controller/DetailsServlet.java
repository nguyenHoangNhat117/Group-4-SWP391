package controller;

import dao.AmenityDAO;
import dao.BookingDAO;
import dao.ReviewDAO;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import model.Amenity;
import model.Review;
import model.Room;
import model.User;

@WebServlet(name = "DetailsServlet", urlPatterns = {"/details"})
public class DetailsServlet extends HttpServlet {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRoomDetails(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        handleRoomDetails(request, response);
    }

    private void handleRoomDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        // 1. Validate RoomNumber parameter (Exception 2a)
        String roomNumberParam = request.getParameter("roomNumber");
        int roomNumber;
        try {
            roomNumber = Integer.parseInt(roomNumberParam == null ? "" : roomNumberParam.trim());
            if (roomNumber <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            redirectToSearchWithUnavailableError(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        User loggedUser = session.getAttribute("loggedUser") instanceof User
                ? (User) session.getAttribute("loggedUser") : null;
        boolean isAdmin = loggedUser != null && "admin".equalsIgnoreCase(loggedUser.getRole());

        RoomDAO roomDAO = new RoomDAO();
        Room room = roomDAO.getRoomByNumber(roomNumber);

        // Exception 2a: Room does not exist or its RoomType has been deactivated
        if (room == null || (!isAdmin && !roomDAO.isRoomViewable(roomNumber))) {
            redirectToSearchWithUnavailableError(request, response);
            return;
        }

        // Clean up expired pending reservations before verifying availability
        BookingDAO bookingDAO = new BookingDAO();
        bookingDAO.expirePendingBookings();

        // 2. Resolve Check-in, Check-out dates and Guest Count (Normal Flow Step 2 & Alternative Flow 4a)
        LocalDate checkIn = (LocalDate) session.getAttribute("checkInDate");
        LocalDate checkOut = (LocalDate) session.getAttribute("checkOutDate");
        int guestCount = session.getAttribute("guestCount") instanceof Integer
                ? (Integer) session.getAttribute("guestCount") : 1;

        String dateError = null;
        String rawDates = request.getParameter("booking-dates");

        if (rawDates != null && !rawDates.isBlank()) {
            String[] parts = rawDates.trim().split("\\s+-\\s+");
            if (parts.length == 2) {
                try {
                    LocalDate parsedIn = LocalDate.parse(parts[0].trim(), DATE_FORMAT);
                    LocalDate parsedOut = LocalDate.parse(parts[1].trim(), DATE_FORMAT);
                    if (parsedIn.isBefore(LocalDate.now()) || !parsedOut.isAfter(parsedIn)) {
                        dateError = "Check-out date must be later than Check-in date, and Check-in date cannot be in the past.";
                    } else {
                        checkIn = parsedIn;
                        checkOut = parsedOut;
                        session.setAttribute("dates", rawDates.trim());
                        session.setAttribute("checkInDate", checkIn);
                        session.setAttribute("checkOutDate", checkOut);
                    }
                } catch (DateTimeParseException ex) {
                    dateError = "Check-out date must be later than Check-in date, and Check-in date cannot be in the past.";
                }
            } else {
                dateError = "Check-out date must be later than Check-in date, and Check-in date cannot be in the past.";
            }
        } else if (request.getParameter("checkIn") != null && request.getParameter("checkOut") != null) {
            try {
                LocalDate parsedIn = LocalDate.parse(request.getParameter("checkIn").trim());
                LocalDate parsedOut = LocalDate.parse(request.getParameter("checkOut").trim());
                if (!parsedIn.isBefore(LocalDate.now()) && parsedOut.isAfter(parsedIn)) {
                    checkIn = parsedIn;
                    checkOut = parsedOut;
                    String formattedRange = checkIn.format(DATE_FORMAT) + " - " + checkOut.format(DATE_FORMAT);
                    session.setAttribute("dates", formattedRange);
                    session.setAttribute("checkInDate", checkIn);
                    session.setAttribute("checkOutDate", checkOut);
                } else {
                    dateError = "Check-out date must be later than Check-in date, and Check-in date cannot be in the past.";
                }
            } catch (DateTimeParseException ignored) {
            }
        }

        // Resolve Guest Count if provided/modified in availability widget
        String guestParam = request.getParameter("guest-count") != null
                ? request.getParameter("guest-count")
                : request.getParameter("guestCount");
        if (guestParam != null && !guestParam.isBlank()) {
            try {
                int parsedGuests = Integer.parseInt(guestParam.trim());
                if (parsedGuests > 0) {
                    guestCount = parsedGuests;
                    session.setAttribute("guestCount", guestCount);
                } else {
                    dateError = "Guest count must be a positive number.";
                }
            } catch (NumberFormatException ex) {
                dateError = "Guest count must be a valid positive integer.";
            }
        }

        // Default stay dates (Today -> Tomorrow) if none were selected yet
        if (checkIn == null || checkOut == null || checkIn.isBefore(LocalDate.now()) || !checkOut.isAfter(checkIn)) {
            checkIn = LocalDate.now();
            checkOut = checkIn.plusDays(1);
            String formattedRange = checkIn.format(DATE_FORMAT) + " - " + checkOut.format(DATE_FORMAT);
            session.setAttribute("dates", formattedRange);
            session.setAttribute("checkInDate", checkIn);
            session.setAttribute("checkOutDate", checkOut);
        }

        // 3. Verify Real-time Availability & Calculate Stay Price (BR-15, Alternative Flow 4a, Exception 4c)
        boolean isOperationallyAvailable = "available".equalsIgnoreCase(room.getStatus());
        boolean isCapacitySatisfied = guestCount > 0 && guestCount <= room.getRoomType().getCapacity();
        boolean isBookedInRange = bookingDAO.isRoomBookedInRange(roomNumber, checkIn, checkOut);

        boolean isRoomAvailable = (dateError == null)
                && isOperationallyAvailable
                && isCapacitySatisfied
                && !isBookedInRange;

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            nights = 1;
        }
        BigDecimal totalStayPrice = room.getRoomType().getPricePerNight()
                .multiply(BigDecimal.valueOf(nights));

        // 4. Load RoomType Amenities (Normal Flow Step 2)
        AmenityDAO amenityDAO = new AmenityDAO();
        List<Amenity> amenities = amenityDAO.getAmenitiesByRoomTypeId(room.getRoomType().getId());

        // 5. Load Approved Public Reviews & Average Rating (Normal Flow Step 3 & BR-48)
        ReviewDAO reviewDAO = new ReviewDAO();
        List<Review> reviews = reviewDAO.getReviewsByRoomNumber(roomNumber);
        BigDecimal averageStar = reviews.isEmpty()
                ? BigDecimal.ZERO
                : reviewDAO.getAverageStarByRoomNumber(roomNumber);

        // 6. Set attributes for details.jsp
        request.setAttribute("room", room);
        request.setAttribute("amenities", amenities);
        request.setAttribute("reviews", reviews);
        request.setAttribute("averageStar", averageStar);
        request.setAttribute("checkInDate", checkIn);
        request.setAttribute("checkOutDate", checkOut);
        request.setAttribute("guestCount", guestCount);
        request.setAttribute("nights", nights);
        request.setAttribute("totalStayPrice", totalStayPrice);
        request.setAttribute("isRoomAvailable", isRoomAvailable);
        request.setAttribute("isCapacitySatisfied", isCapacitySatisfied);
        request.setAttribute("dateError", dateError);

        if (!isRoomAvailable && dateError == null) {
            request.setAttribute("unavailableNotice",
                    "This room is unavailable for the selected dates. Please select alternative Check-in/Check-out dates.");
        }

        request.getRequestDispatcher("/WEB-INF/details/details.jsp").forward(request, response);
    }

    private void redirectToSearchWithUnavailableError(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("checkInDate") != null) {
            response.sendRedirect(request.getContextPath() + "/room?page-index=1&error=room-unavailable");
        } else {
            response.sendRedirect(request.getContextPath() + "/booking?error=room-unavailable");
        }
    }

    @Override
    public String getServletInfo() {
        return "Handles UC07 - View Room Details, real-time availability check, and approved reviews.";
    }
}