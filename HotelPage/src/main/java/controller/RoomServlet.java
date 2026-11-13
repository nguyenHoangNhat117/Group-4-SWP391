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
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import model.Room;
import model.RoomPriceBand;
import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.RoomType;
import model.User;
import util.OnlineStayDates;
import util.ReportPeriod;

@WebServlet(name = "RoomServlet", urlPatterns = {"/room"})
public class RoomServlet extends HttpServlet {

    private static final int PAGE_SIZE = 6;
    private static final String CREATED_ROOMS = "roomManagement.createdRooms";

    private boolean isAdmin(HttpSession session) {
        if (session == null) {
            return false;
        }
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

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showRoomTypes(request);
        request.setAttribute("maxQuantity", RoomDAO.MAX_CREATE_QUANTITY);
        request.getRequestDispatcher("/WEB-INF/room/create.jsp").forward(request, response);
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
        if ("availability".equals(request.getParameter("action"))) {
            showAvailability(request, response);
            return;
        }
        HttpSession session = request.getSession(false);
        User user = session == null ? null
                : (session.getAttribute("loggedUser") instanceof User
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
                showCreateForm(request, response);
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

    private void showAvailability(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Integer guests = positiveInt(request.getParameter("guest-count"));
        Integer typeId = positiveInt(request.getParameter("room-type-id"));
        Integer quantity = positiveInt(request.getParameter("quantity"));
        if (request.getParameter("quantity") != null && (quantity == null || quantity > 1000)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        RoomPriceBand band;
        LocalDate today = ReportPeriod.today();
        try {
            band = RoomPriceBand.parse(request.getParameter("price-band"));
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        new dao.BookingDAO().expirePendingBookings();
        try {
            LocalDate lastDate = today.plusDays(OnlineStayDates.ADVANCE_DAYS);
            java.util.Map<String, String> dates = new RoomDAO().findCalendarAvailability(
                    today, lastDate, guests == null ? 1 : guests, typeId, band, quantity == null ? 1 : quantity);
            response.setContentType("application/json");
            response.setHeader("Cache-Control", "no-store");
            new ObjectMapper().writeValue(response.getOutputStream(), java.util.Map.of("dates", dates));
        } catch (SQLException ex) {
            throw new ServletException("Unable to load calendar availability", ex);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, User user)
            throws ServletException, IOException {
        RoomDAO roomDAO = new RoomDAO();
        boolean admin = user != null && "admin".equalsIgnoreCase(user.getRole());

        if (admin) {
            showAdminList(request, response, session, roomDAO);
            return;
        }

        // UC06 Assumption: Clean up expired pending reservations before checking availability
        new dao.BookingDAO().expirePendingBookings();

        LocalDate checkIn = session == null ? null : (LocalDate) session.getAttribute("checkInDate");
        LocalDate checkOut = session == null ? null : (LocalDate) session.getAttribute("checkOutDate");

        // BR-14 revalidation on session dates
        if (!OnlineStayDates.isValid(checkIn, checkOut, ReportPeriod.today())) {
            redirect(request, response, "/booking?error=invalid-dates");
            return;
        }

        Integer guestCount = session.getAttribute("guestCount") instanceof Integer
                ? (Integer) session.getAttribute("guestCount") : 1;
        if (guestCount <= 0) {
            guestCount = 1;
        }

        Integer selectedRoomTypeId = session.getAttribute("searchRoomTypeId") instanceof Integer
                ? (Integer) session.getAttribute("searchRoomTypeId") : null;
        RoomPriceBand band = RoomPriceBand.parse((String) session.getAttribute("searchPriceBand"));
        String sort = (String) session.getAttribute("searchSortBy");
        List<RoomDAO.AvailableType> types;
        try {
            types = roomDAO.findAvailableTypes(checkIn, checkOut, guestCount,
                    selectedRoomTypeId, band, sort == null ? "room_asc" : sort);
        } catch (SQLException ex) {
            throw new ServletException("Unable to load room inventory", ex);
        }
        int totalPages = (types.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        Integer parsedPage = positiveInt(request.getParameter("page-index"));
        int page = parsedPage == null ? 1 : Math.min(parsedPage, Math.max(1, totalPages));
        int offset = (page - 1) * PAGE_SIZE;
        showRoomTypes(request);
        request.setAttribute("availableTypes", types.subList(offset, Math.min(offset + PAGE_SIZE, types.size())));
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageIndex", page);

        if (request.getParameter("page-index") == null && totalPages > 0) {
            redirect(request, response, "/room?page-index=1");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/room/room.jsp")
                .forward(request, response);
    }

    private void showAdminList(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RoomDAO roomDAO) throws ServletException, IOException {
        String roomNumberInput = request.getParameter("room-number");
        Integer roomNumber = positiveInt(roomNumberInput == null ? null : roomNumberInput.strip());
        Integer roomTypeId = positiveInt(request.getParameter("room-type-id"));
        boolean invalidRoomNumber = roomNumber == null
                && roomNumberInput != null && !roomNumberInput.isBlank();

        Integer requestedPage = positiveInt(request.getParameter("page-index"));
        int totalPages = 0;
        int page = 1;
        List<Room> rooms = Collections.emptyList();
        try {
            if (!invalidRoomNumber) {
                totalPages = roomDAO.getTotalPages(roomTypeId, roomNumber);
                if (totalPages > 0) {
                    page = requestedPage == null ? 1 : Math.min(requestedPage, totalPages);
                    rooms = roomDAO.getPage(page, roomTypeId, roomNumber);
                }
            }
        } catch (SQLException ex) {
            throw new ServletException("Could not load the room management list", ex);
        }

        showRoomTypes(request);
        request.setAttribute("adminRoomTypeId", roomTypeId);
        request.setAttribute("adminRoomNumber", roomNumber);
        request.setAttribute("invalidRoomNumber", invalidRoomNumber);
        request.setAttribute("rooms", rooms);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageIndex", page);
        synchronized (session) {
            request.setAttribute("createdRooms", session.getAttribute(CREATED_ROOMS));
            session.removeAttribute(CREATED_ROOMS);
        }
        request.getRequestDispatcher("/WEB-INF/room/room.jsp").forward(request, response);
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
                String quantityInput = request.getParameter("quantity");
                Integer quantity = positiveInt(quantityInput == null ? "1" : quantityInput.strip());
                String error;
                if (quantity == null || quantity > RoomDAO.MAX_CREATE_QUANTITY) {
                    error = "invalid-quantity";
                } else if (!isValidStatus(status)) {
                    error = "invalid-status";
                } else if (roomTypeId == null || new RoomTypeDAO().getRoomTypeById(roomTypeId) == null) {
                    error = "invalid-room-type";
                } else {
                    List<Integer> roomNumbers = roomDAO.create(status, roomTypeId, quantity);
                    if (!roomNumbers.isEmpty()) {
                        synchronized (session) {
                            session.setAttribute(CREATED_ROOMS, roomNumbers);
                        }
                        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
                        response.setHeader("Location", request.getContextPath() + "/room");
                        return;
                    }
                    error = "create-failed";
                }
                request.setAttribute("createError", error);
                request.setAttribute("selectedRoomTypeId", roomTypeId);
                showCreateForm(request, response);
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
                    String error = changed == RoomDAO.CHANGE_BLOCKED_BY_BOOKING
                            ? "active-booking" : "update-failed";
                    redirect(request, response, "/room?view=update&roomNumber="
                            + roomNumber + "&error=" + error);
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
                if (changed == RoomDAO.CHANGE_BLOCKED_BY_BOOKING) {
                    redirect(request, response, "/room?view=delete&roomNumber="
                            + roomNumber + "&error=active-booking");
                    return;
                }
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
        String sourcePage = request.getParameter("source");
        String errorTarget = "room".equalsIgnoreCase(sourcePage) ? "/room?page-index=1&" : "/booking?";

        // 1. Validate Check-in & Check-out dates (BR-14 & Exception 4a)
        String rawDates = request.getParameter("booking-dates");
        if (rawDates == null || rawDates.isBlank()) {
            redirect(request, response, errorTarget + "error=missing-dates");
            return;
        }
        String[] dates = rawDates.trim().split("\\s+-\\s+");
        if (dates.length != 2) {
            redirect(request, response, errorTarget + "error=invalid-dates");
            return;
        }

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = OnlineStayDates.parseSearchDate(dates[0]);
            checkOut = OnlineStayDates.parseSearchDate(dates[1]);
            if (!OnlineStayDates.isValid(checkIn, checkOut, ReportPeriod.today())) {
                redirect(request, response, errorTarget + "error=invalid-dates");
                return;
            }
        } catch (DateTimeParseException ex) {
            redirect(request, response, errorTarget + "error=invalid-dates");
            return;
        }

        // 2. Validate Guest Count (BR-15 & Exception 4b)
        int guestCount = 0;
        String guestCountParam = request.getParameter("guest-count");
        if (guestCountParam != null && !guestCountParam.isBlank()) {
            try {
                guestCount = Integer.parseInt(guestCountParam.trim());
            } catch (NumberFormatException ex) {
                redirect(request, response, errorTarget + "error=invalid-filter");
                return;
            }
        } else {
            // Support adults + children fields from search.jsp if submitted separately
            String adultsParam = request.getParameter("adults");
            String childrenParam = request.getParameter("children");
            try {
                int adults = (adultsParam == null || adultsParam.isBlank()) ? 1 : Integer.parseInt(adultsParam.trim());
                int children = (childrenParam == null || childrenParam.isBlank()) ? 0 : Integer.parseInt(childrenParam.trim());
                if (adults <= 0 || children < 0) {
                    redirect(request, response, errorTarget + "error=invalid-filter");
                    return;
                }
                guestCount = adults + children;
            } catch (NumberFormatException ex) {
                redirect(request, response, errorTarget + "error=invalid-filter");
                return;
            }
        }

        if (guestCount <= 0) {
            redirect(request, response, errorTarget + "error=invalid-filter");
            return;
        }

        // 3. Validate Optional RoomType & Price Range Filters (BR-16, BR-32 & Exception 4b)
        String roomTypeParam = request.getParameter("room-type-id");
        Integer roomTypeId = null;
        if (roomTypeParam != null && !roomTypeParam.isBlank()) {
            roomTypeId = positiveInt(roomTypeParam.trim());
            if (roomTypeId == null) {
                redirect(request, response, errorTarget + "error=invalid-filter");
                return;
            }
        }

        RoomPriceBand band;
        try {
            band = RoomPriceBand.parse(request.getParameter("price-band"));
        } catch (IllegalArgumentException ex) {
            redirect(request, response, errorTarget + "error=invalid-filter");
            return;
        }

        String sortBy = request.getParameter("sort-by");
        if (sortBy == null || (!"price_asc".equals(sortBy)
                && !"price_desc".equals(sortBy)
                && !"capacity_desc".equals(sortBy))) {
            sortBy = "room_asc";
        }

        // 4. Retain search criteria in session (UC06 Postconditions)
        session.setAttribute("dates", OnlineStayDates.formatSearchRange(checkIn, checkOut));
        session.setAttribute("checkInDate", checkIn);
        session.setAttribute("checkOutDate", checkOut);
        session.setAttribute("guestCount", guestCount);
        session.setAttribute("searchRoomTypeId", roomTypeId);
        session.setAttribute("searchPriceBand", band.name());
        session.setAttribute("searchSortBy", sortBy);
        session.removeAttribute("availableRooms"); // prevent stale results

        redirect(request, response, "/room?page-index=1");
    }
}
