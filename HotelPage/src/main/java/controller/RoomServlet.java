package controller;

import dao.RoomDAO;
import dao.RoomTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import model.Room;
import model.RoomType;
import model.User;

@WebServlet(name = "RoomServlet", urlPatterns = {"/room"})
public class RoomServlet extends HttpServlet {

    private static final int PAGE_SIZE = 5;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private boolean isAdmin(HttpSession session) {
        if (session == null) return false;
        Object value = session.getAttribute("loggedUser");
        return value instanceof User
                && "admin".equalsIgnoreCase(((User) value).getRole());
    }

    private void showRoomTypes(HttpServletRequest request) {
        List<RoomType> types = new RoomTypeDAO().getAll();
        // The original JSP templates use categorys, whereas earlier servlet
        // code used roomTypes. Supply both until the JSPs are standardized.
        request.setAttribute("categorys", types);
        request.setAttribute("roomTypes", types);
    }

    private Integer positiveInt(String value) {
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : null;
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }
    }

    private boolean isValidStatus(String status) {
        return "available".equals(status)
                || "maintenance".equals(status)
                || "out_of_service".equals(status);
    }

    private void redirect(HttpServletRequest request,
                          HttpServletResponse response, String relativeUrl)
            throws IOException {
        response.sendRedirect(request.getContextPath() + relativeUrl);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null :
                (session.getAttribute("loggedUser") instanceof User
                        ? (User) session.getAttribute("loggedUser") : null);

        String view = request.getParameter("view");
        if (view == null || view.isBlank()) {
            showList(request, response, session, user);
            return;
        }

        if (!isAdmin(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only administrators can manage rooms.");
            return;
        }

        RoomDAO roomDAO = new RoomDAO();
        switch (view) {
            case "create":
                showRoomTypes(request);
                request.getRequestDispatcher("/WEB-INF/room/create.jsp")
                        .forward(request, response);
                return;
            case "update": {
                Integer roomNumber = positiveInt(request.getParameter("roomNumber"));
                if (roomNumber == null) {
                    redirect(request, response, "/room?page-index=1");
                    return;
                }
                Room room = roomDAO.getRoomByNumber(roomNumber);
                if (room == null) {
                    redirect(request, response, "/room?page-index=1");
                    return;
                }
                showRoomTypes(request);
                request.setAttribute("room", room);
                request.getRequestDispatcher("/WEB-INF/room/update.jsp")
                        .forward(request, response);
                return;
            }
            case "delete": {
                Integer roomNumber = positiveInt(request.getParameter("roomNumber"));
                if (roomNumber == null || !roomDAO.doesRoomExist(roomNumber)) {
                    redirect(request, response, "/room?page-index=1");
                    return;
                }
                request.setAttribute("room", roomDAO.getRoomByNumber(roomNumber));
                request.getRequestDispatcher("/WEB-INF/room/delete.jsp")
                        .forward(request, response);
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response,
                          HttpSession session, User user)
            throws ServletException, IOException {
        RoomDAO roomDAO = new RoomDAO();
        boolean admin = user != null && "admin".equalsIgnoreCase(user.getRole());
        LocalDate checkIn = session == null ? null :
                (LocalDate) session.getAttribute("checkInDate");
        LocalDate checkOut = session == null ? null :
                (LocalDate) session.getAttribute("checkOutDate");

        if (!admin && (checkIn == null || checkOut == null)) {
            redirect(request, response, "/booking");
            return;
        }

        int totalRooms = admin
                ? roomDAO.getAll().size()
                : roomDAO.countAvailableRooms(checkIn, checkOut);
        int totalPages = (totalRooms + PAGE_SIZE - 1) / PAGE_SIZE;
        int page = 1;
        Integer parsedPage = positiveInt(request.getParameter("page-index"));
        if (parsedPage != null) page = parsedPage;
        if (totalPages > 0 && page > totalPages) page = totalPages;

        List<Room> rooms = totalRooms == 0
                ? java.util.Collections.emptyList()
                : (admin ? roomDAO.getPage(page)
                         : roomDAO.getAvailableRoomPage(page, checkIn, checkOut));
        request.setAttribute("rooms", rooms);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageIndex", page);

        // Existing pagination JSP expects a page-index request parameter.
        if (request.getParameter("page-index") == null && totalPages > 0) {
            redirect(request, response, "/room?page-index=1");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/room/room.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        // Search is public for Guest/Customer. Create a session so the selected
        // dates survive until the user logs in and proceeds to booking.
        if ("search".equals(action)) {
            HttpSession searchSession = request.getSession(true);
            handleSearch(request, response, searchSession);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("loggedUser") instanceof User)) {
            redirect(request, response, "/login");
            return;
        }
        if (!isAdmin(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only administrators can create, update or delete rooms.");
            return;
        }

        RoomDAO roomDAO = new RoomDAO();
        switch (action == null ? "" : action) {
            case "create": {
                String status = request.getParameter("status");
                Integer roomTypeId = positiveInt(request.getParameter("room-type-id"));
                if (!isValidStatus(status) || roomTypeId == null
                        || new RoomTypeDAO().getRoomTypeById(roomTypeId) == null) {
                    redirect(request, response, "/room?view=create&error=missing-params");
                    return;
                }
                // HotelDB.RoomNumber is IDENTITY; do not take room-number from form.
                int generatedNumber = roomDAO.create(status, roomTypeId);
                if (generatedNumber <= 0) {
                    redirect(request, response, "/room?view=create&error=create-failed");
                    return;
                }
                redirect(request, response, "/room?page-index=1");
                return;
            }
            case "update": {
                Integer roomNumber = positiveInt(request.getParameter("room-number"));
                Integer roomTypeId = positiveInt(request.getParameter("room-type-id"));
                String status = request.getParameter("status");
                if (roomNumber == null || roomTypeId == null || !isValidStatus(status)) {
                    redirect(request, response, "/room?page-index=1");
                    return;
                }
                if (roomDAO.getRoomByNumber(roomNumber) == null
                        || new RoomTypeDAO().getRoomTypeById(roomTypeId) == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                int changed = roomDAO.update(roomNumber, status, roomTypeId);
                if (changed <= 0) {
                    redirect(request, response, "/room?view=update&roomNumber="
                            + roomNumber + "&error=update-failed");
                    return;
                }
                redirect(request, response, "/room?page-index=1");
                return;
            }
            case "delete": {
                // Original delete.jsp submits room-number, not roomNumber.
                Integer roomNumber = positiveInt(request.getParameter("room-number"));
                if (roomNumber == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                int changed = roomDAO.delete(roomNumber);
                if (changed <= 0) {
                    response.sendError(HttpServletResponse.SC_CONFLICT,
                            "Room removal failed; check booking history or database constraints.");
                    return;
                }
                redirect(request, response, "/room?page-index=1");
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
        }
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session) throws IOException {
        String rawDates = request.getParameter("booking-dates");
        if (rawDates == null || rawDates.isBlank()) {
            redirect(request, response, "/booking?error=missing-dates");
            return;
        }
        String[] dates = rawDates.trim().split("\\s+-\\s+");
        if (dates.length != 2) {
            redirect(request, response, "/booking?error=invalid-dates");
            return;
        }
        try {
            LocalDate checkIn = LocalDate.parse(dates[0].trim(), DATE_FORMAT);
            LocalDate checkOut = LocalDate.parse(dates[1].trim(), DATE_FORMAT);
            if (checkIn.isBefore(LocalDate.now()) || !checkOut.isAfter(checkIn)) {
                redirect(request, response, "/booking?error=invalid-dates");
                return;
            }
            session.setAttribute("dates", rawDates.trim());
            session.setAttribute("checkInDate", checkIn);
            session.setAttribute("checkOutDate", checkOut);
            session.removeAttribute("availableRooms"); // prevent stale results
            redirect(request, response, "/room?page-index=1");
        } catch (DateTimeParseException ex) {
            redirect(request, response, "/booking?error=invalid-dates");
        }
    }
}
