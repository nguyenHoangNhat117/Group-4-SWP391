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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import model.Room;
import model.OnlineBookingDraft;
import model.RoomPriceBand;
import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.RoomType;
import model.User;
import util.OnlineStayDates;
import util.OnlineStayPolicy;
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

        util.AuthSupport.token(session);
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

        int guestCount = session.getAttribute("guestCount") instanceof Integer
                ? (Integer) session.getAttribute("guestCount") : 0;
        int adults = searchAdults(session);
        if (adults < 1 || guestCount < adults) {
            redirect(request, response, "/booking?error=invalid-guests");
            return;
        }

        String sort = searchSort(session.getAttribute("searchSortBy"));
        List<RoomDAO.AvailableType> types;
        try {
            types = roomDAO.findRoomSelectionTypes(checkIn, checkOut, sort);
        } catch (SQLException ex) {
            throw new ServletException("Unable to load room inventory", ex);
        }
        request.setAttribute("insufficientPartyCapacity",
                !types.isEmpty() && !hasCapacityForParty(types, adults, guestCount, RoomDAO.AvailableType::getAvailable));
        request.setAttribute("availableTypes", types);
        request.setAttribute("searchSortBy", sort);
        request.setAttribute("searchAdults", adults);
        request.setAttribute("searchChildren", guestCount - adults);
        request.setAttribute("searchNights", ChronoUnit.DAYS.between(checkIn, checkOut));
        request.setAttribute("searchCriteria", searchCriteria(checkIn, checkOut, adults, guestCount));
        request.setAttribute("selectionCsrfToken", util.AuthSupport.token(session));
        request.getRequestDispatcher("/WEB-INF/room/room.jsp")
                .forward(request, response);
    }

    private static int searchAdults(HttpSession session) {
        return session.getAttribute("searchAdults") instanceof Integer
                ? (Integer) session.getAttribute("searchAdults") : 0;
    }

    private static String searchCriteria(LocalDate from, LocalDate to, int adults, int guests) {
        return from + "|" + to + "|" + adults + "|" + guests;
    }

    private static boolean hasCapacityForParty(List<RoomDAO.AvailableType> types, int adults, int guests,
            ToIntFunction<RoomDAO.AvailableType> quantity) {
        long totalCapacity = 0;
        long childCapacity = 0;
        for (RoomDAO.AvailableType result : types) {
            long capacity = result.getType().getCapacity();
            int rooms = quantity.applyAsInt(result);
            totalCapacity += capacity * rooms;
            childCapacity += (long) result.getMaxChildren() * rooms;
        }
        // This checks available stock. The maximum number of rooms applies to selected rooms.
        return OnlineStayPolicy.fitsParty(adults, guests - adults, totalCapacity, childCapacity);
    }

    private static String searchSort(Object value) {
        return "price_desc".equals(value) ? "price_desc" : "price_asc";
    }

    private void checkSelection(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        LocalDate from = session == null ? null : (LocalDate) session.getAttribute("checkInDate");
        LocalDate to = session == null ? null : (LocalDate) session.getAttribute("checkOutDate");
        int guests = session != null && session.getAttribute("guestCount") instanceof Integer
                ? (Integer) session.getAttribute("guestCount") : 0;
        int adults = session == null ? 0 : searchAdults(session);
        if (!OnlineStayDates.isValid(from, to, ReportPeriod.today()) || adults < 1 || guests < adults
                || !searchCriteria(from, to, adults, guests).equals(request.getParameter("criteria"))) {
            writeSelectionResponse(response, HttpServletResponse.SC_CONFLICT, Map.of("status", "search-changed"));
            return;
        }
        if (!util.AuthSupport.checkCsrf(request, response)) {
            return;
        }

        String[] ids = request.getParameterValues("roomTypeId");
        String[] quantities = request.getParameterValues("quantity");
        Map<Integer, Integer> selected = new HashMap<>();
        long roomCount = 0;
        if (ids == null || quantities == null || ids.length != quantities.length) {
            writeSelectionResponse(response, HttpServletResponse.SC_BAD_REQUEST, Map.of("status", "capacity"));
            return;
        }
        for (int index = 0; index < ids.length; index++) {
            Integer id = positiveInt(ids[index]);
            Integer quantity = positiveInt(quantities[index]);
            if (id == null || quantity == null || selected.putIfAbsent(id, quantity) != null) {
                writeSelectionResponse(response, HttpServletResponse.SC_BAD_REQUEST, Map.of("status", "capacity"));
                return;
            }
            roomCount += quantity;
        }

        List<RoomDAO.AvailableType> inventory;
        try {
            new dao.BookingDAO().expirePendingBookings();
            inventory = new RoomDAO().findRoomSelectionTypes(from, to, "price_asc");
        } catch (SQLException ex) {
            throw new ServletException("Unable to check room selection", ex);
        }
        Map<Integer, Integer> stock = new HashMap<>();
        for (RoomDAO.AvailableType type : inventory) {
            stock.put(type.getType().getId(), type.getAvailable());
        }
        if (selected.entrySet().stream().anyMatch(entry -> entry.getValue() > stock.getOrDefault(entry.getKey(), 0))) {
            writeSelectionResponse(response, HttpServletResponse.SC_CONFLICT, Map.of("status", "stock", "stock", stock));
        } else if (roomCount < 1 || roomCount > guests
                || !hasCapacityForParty(inventory, adults, guests, type -> selected.getOrDefault(type.getType().getId(), 0))) {
            writeSelectionResponse(response, HttpServletResponse.SC_BAD_REQUEST, Map.of("status", "capacity"));
        } else {
            List<OnlineBookingDraft.RoomLine> lines = new ArrayList<>();
            for (RoomDAO.AvailableType type : inventory) {
                Integer quantity = selected.get(type.getType().getId());
                if (quantity != null) {
                    lines.add(new OnlineBookingDraft.RoomLine(type.getType(), quantity));
                }
            }
            OnlineBookingDraft draft;
            synchronized (session) {
                if (!from.equals(session.getAttribute("checkInDate")) || !to.equals(session.getAttribute("checkOutDate"))
                        || !Integer.valueOf(guests).equals(session.getAttribute("guestCount"))
                        || adults != searchAdults(session)) {
                    writeSelectionResponse(response, HttpServletResponse.SC_CONFLICT, Map.of("status", "search-changed"));
                    return;
                }
                draft = new OnlineBookingDraft(from, to, adults, guests - adults, lines);
                Object previous = session.getAttribute(OnlineBookingDraft.SESSION_ATTRIBUTE);
                if (previous instanceof OnlineBookingDraft) {
                    draft.copyContactFrom((OnlineBookingDraft) previous);
                }
                session.setAttribute(OnlineBookingDraft.SESSION_ATTRIBUTE, draft);
            }
            writeSelectionResponse(response, HttpServletResponse.SC_OK, Map.of(
                    "status", "available", "nextUrl", request.getContextPath() + draft.getPath(),
                    "requestKey", draft.getToken()));
        }
    }

    private static void writeSelectionResponse(HttpServletResponse response, int status, Map<String, ?> body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

    private void showAdminList(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RoomDAO roomDAO) throws ServletException, IOException {
        util.AuthSupport.token(session);
        String status = request.getParameter("status");
        if (!isValidStatus(status)) status = null;
        String sort = "asc".equals(request.getParameter("sort")) ? "asc" : "desc".equals(request.getParameter("sort")) ? "desc" : "";
        request.setAttribute("adminStatus", status);
        request.setAttribute("priceSort", sort);
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
                totalPages = roomDAO.getTotalPages(roomTypeId, roomNumber, status);
                if (totalPages > 0) {
                    page = requestedPage == null ? 1 : Math.min(requestedPage, totalPages);
                    rooms = roomDAO.getPage(page, roomTypeId, roomNumber, status, sort);
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
        if ("check-selection".equals(action)) {
            checkSelection(request, response);
            return;
        }
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
                if (!util.AuthSupport.checkCsrf(request, response)) return;
                // The list dialog and legacy delete form both submit room-number.
                Integer roomNumber = positiveInt(request.getParameter("room-number"));
                if (roomNumber == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                int changed = roomDAO.delete(roomNumber);
                if (changed == RoomDAO.CHANGE_BLOCKED_BY_BOOKING) {
                    redirect(request, response, "/room?error=active-booking");
                    return;
                }
                if (changed <= 0) {
                    redirect(request, response, "/room?error=delete-failed");
                    return;
                }
                redirect(request, response, "/room?page-index=1");
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác không hợp lệ.");
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

        String adultsParam = request.getParameter("adults");
        String childrenParam = request.getParameter("children");
        Integer adults = positiveInt(adultsParam == null ? null : adultsParam.strip());
        int children;
        int guestCount;
        try {
            children = Integer.parseInt(childrenParam == null ? "" : childrenParam.strip());
            if (adults == null || children < 0) {
                redirect(request, response, errorTarget + "error=invalid-guests");
                return;
            }
            guestCount = Math.addExact(adults, children);
        } catch (NumberFormatException | ArithmeticException ex) {
            redirect(request, response, errorTarget + "error=invalid-guests");
            return;
        }

        synchronized (session) {
            session.setAttribute("dates", OnlineStayDates.formatSearchRange(checkIn, checkOut));
            session.setAttribute("checkInDate", checkIn);
            session.setAttribute("checkOutDate", checkOut);
            session.setAttribute("searchAdults", adults);
            session.setAttribute("searchChildren", children);
            session.setAttribute("guestCount", guestCount);
            session.removeAttribute("searchRoomTypeId");
            session.removeAttribute("searchPriceBand");
            session.setAttribute("searchSortBy", searchSort(request.getParameter("sort-by")));
            session.removeAttribute("availableRooms");
        }

        redirect(request, response, "/room?page-index=1");
    }
}
