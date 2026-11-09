package controller;

import dao.AmenityDAO;
import dao.BookingDAO;
import dao.DiscountDAO;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import model.BookingContact;
import model.Customer;
import model.RoomPriceBand;
import model.RoomType;
import model.User;
import util.ReportPeriod;
import util.OnlineStayPolicy;

@WebServlet(urlPatterns = {"/room-stay", "/checkout"})
public class RoomStayServlet extends HttpServlet {
    private static final String DRAFT = "roomStayDraft";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if ("/room-stay".equals(request.getServletPath())) {
            showRoomStay(request, response, session);
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        StayDraft draft = getDraft(request, session);
        if (draft == null) {
            redirect(request, response, "/booking");
            return;
        }
        Customer customer = requireCustomer(request, response, session, draft);
        if (customer == null) {
            return;
        }
        synchronized (draft) {
            if (draft.bookingId > 0) {
                redirect(request, response, "/payment?bookingId=" + draft.bookingId);
                return;
            }
        }
        showCheckout(request, response, draft, customer);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/checkout".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(true);
        if ("review".equals(request.getParameter("action"))) {
            prepareCheckout(request, response, session);
            return;
        }
        if (!"confirm".equals(request.getParameter("action"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        StayDraft draft = getDraft(request, session);
        if (draft == null) {
            redirect(request, response, "/booking");
            return;
        }
        Customer customer = requireCustomer(request, response, session, draft);
        if (customer == null) {
            return;
        }
        confirmCheckout(request, response, draft, customer);
    }

    private void showRoomStay(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        int typeId = number(request.getParameter("roomTypeId"), 0);
        if (typeId < 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        LocalDate from = session == null ? null : (LocalDate) session.getAttribute("checkInDate");
        LocalDate to = session == null ? null : (LocalDate) session.getAttribute("checkOutDate");
        if (!validStay(from, to)) {
            redirect(request, response, "/booking?error=invalid-dates");
            return;
        }
        RoomType type = new RoomTypeDAO().getActiveRoomTypeById(typeId);
        if (type == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        int available = availableCount(typeId, from, to, 1);
        request.setAttribute("roomType", type);
        request.setAttribute("amenities", new AmenityDAO().getAmenitiesByRoomTypeId(typeId));
        request.setAttribute("checkIn", from);
        request.setAttribute("checkOut", to);
        request.setAttribute("nights", ChronoUnit.DAYS.between(from, to));
        request.setAttribute("available", available);
        request.getRequestDispatcher("/WEB-INF/booking/room-stay.jsp").forward(request, response);
    }

    private void prepareCheckout(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        int typeId = number(request.getParameter("roomTypeId"), 0);
        int quantity = number(request.getParameter("quantity"), 0);
        int guests = number(request.getParameter("guests"), 0);
        if (typeId < 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        LocalDate from = (LocalDate) session.getAttribute("checkInDate");
        LocalDate to = (LocalDate) session.getAttribute("checkOutDate");
        if (!validStay(from, to) || !from.toString().equals(request.getParameter("checkIn"))
                || !to.toString().equals(request.getParameter("checkOut"))) {
            redirect(request, response, "/booking?error=invalid-dates");
            return;
        }
        RoomType type = new RoomTypeDAO().getActiveRoomTypeById(typeId);
        if (type == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (quantity < 1 || guests < quantity || guests > 1000
                || (guests + quantity - 1) / quantity > type.getCapacity()) {
            redirect(request, response, "/room-stay?roomTypeId=" + typeId + "&error=quantity");
            return;
        }
        if (availableCount(typeId, from, to, (guests + quantity - 1) / quantity) < quantity) {
            redirect(request, response, "/room-stay?roomTypeId=" + typeId + "&error=availability");
            return;
        }
        StayDraft draft = new StayDraft(type, from, to, quantity, guests);
        session.setAttribute(DRAFT, draft);
        redirect(request, response, "/checkout?draft=" + draft.token);
    }

    private void confirmCheckout(HttpServletRequest request, HttpServletResponse response,
            StayDraft draft, Customer customer) throws ServletException, IOException {
        BookingContact contact;
        try {
            if (OnlineStayPolicy.percentage(request.getParameter("payment-plan")) == null) {
                throw new IllegalArgumentException("Invalid payment choice");
            }
            contact = new BookingContact(request.getParameter("contact-name"), request.getParameter("contact-phone"),
                    request.getParameter("contact-email"), request.getParameter("contact-country"));
        } catch (IllegalArgumentException ex) {
            request.setAttribute("checkoutError", true);
            showCheckout(request, response, draft, customer);
            return;
        }
        String voucher = request.getParameter("discount-code");
        if (voucher != null && !voucher.isBlank() && !availableVoucherTotals(draft.total()).containsKey(voucher)) {
            request.setAttribute("voucherError", true);
            showCheckout(request, response, draft, customer);
            return;
        }

        synchronized (draft) {
            if (draft.bookingId > 0) {
                redirect(request, response, "/payment?bookingId=" + draft.bookingId);
                return;
            }
            new BookingDAO().expirePendingBookings();
            try {
                draft.bookingId = new BookingDAO().createRoomTypeBooking(customer.getCustomerID(), draft.type.getId(),
                        draft.from, draft.to, draft.quantity, draft.guests, draft.type.getPricePerNight(), contact);
            } catch (IllegalArgumentException ex) {
                redirect(request, response, "/room-stay?roomTypeId=" + draft.type.getId() + "&error=availability");
                return;
            } catch (SQLException ex) {
                throw new ServletException("Unable to create booking", ex);
            }
        }
        request.getRequestDispatcher("/payment?bookingId=" + draft.bookingId).forward(request, response);
    }

    private void showCheckout(HttpServletRequest request, HttpServletResponse response,
            StayDraft draft, Customer customer) throws ServletException, IOException {
        request.setAttribute("draftToken", draft.token);
        request.setAttribute("roomType", draft.type);
        request.setAttribute("checkIn", draft.from);
        request.setAttribute("checkOut", draft.to);
        request.setAttribute("quantity", draft.quantity);
        request.setAttribute("guests", draft.guests);
        setCheckoutSummary(request, draft.total(), true);
        request.setAttribute("customer", customer);
        request.setAttribute("contactName", ((customer.getLastName() == null ? "" : customer.getLastName())
                + " " + (customer.getFirstName() == null ? "" : customer.getFirstName())).trim());
        request.getRequestDispatcher("/WEB-INF/booking/checkout.jsp").forward(request, response);
    }

    static void setCheckoutSummary(HttpServletRequest request, BigDecimal total, boolean vouchersAllowed) {
        request.setAttribute("total", total);
        request.setAttribute("initialDeposit", OnlineStayPolicy.deposit(total));
        request.setAttribute("initialBalance", total.subtract(OnlineStayPolicy.deposit(total)));
        request.setAttribute("voucherTotals", vouchersAllowed ? availableVoucherTotals(total) : Map.of());
    }

    private static Map<String, BigDecimal> availableVoucherTotals(BigDecimal total) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        new DiscountDAO().getActivePromotions().forEach(discount -> {
            if (discount.getMinimumAmount() == null || total.compareTo(discount.getMinimumAmount()) >= 0) {
                totals.put(discount.getCode(), OnlineStayPolicy.discountedTotal(
                        total, discount.getSaleOff(), discount.getMaximumDiscount()));
            }
        });
        return totals;
    }

    private Customer requireCustomer(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, StayDraft draft) throws IOException {
        Object user = session.getAttribute("loggedUser");
        if (!(user instanceof User)) {
            session.setAttribute("pendingBookingUrl", "/checkout?draft=" + draft.token);
            redirect(request, response, "/login");
            return null;
        }
        Object customer = session.getAttribute("customer");
        if (!"customer".equalsIgnoreCase(((User) user).getRole()) || !(customer instanceof Customer)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return (Customer) customer;
    }

    private int availableCount(int typeId, LocalDate from, LocalDate to, int guests) throws ServletException {
        new BookingDAO().expirePendingBookings();
        try {
            List<RoomDAO.AvailableType> types = new RoomDAO().findAvailableTypes(
                    from, to, guests, typeId, RoomPriceBand.ALL, "room_asc");
            return types.isEmpty() ? 0 : types.get(0).getAvailable();
        } catch (SQLException ex) {
            throw new ServletException("Unable to load room availability", ex);
        }
    }

    private static StayDraft getDraft(HttpServletRequest request, HttpSession session) {
        Object value = session == null ? null : session.getAttribute(DRAFT);
        if (value instanceof StayDraft && ((StayDraft) value).token.equals(request.getParameter("draft"))) {
            return (StayDraft) value;
        }
        return null;
    }

    private static boolean validStay(LocalDate from, LocalDate to) {
        LocalDate today = ReportPeriod.today();
        return from != null && to != null && !from.isBefore(today)
                && to.isAfter(from) && !to.isAfter(today.plusDays(365));
    }

    private static int number(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    private static final class StayDraft {
        private final String token = UUID.randomUUID().toString();
        private final RoomType type;
        private final LocalDate from;
        private final LocalDate to;
        private final int quantity;
        private final int guests;
        private int bookingId;

        private BigDecimal total() {
            return type.getPricePerNight().multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(from, to) * quantity));
        }

        private StayDraft(RoomType type, LocalDate from, LocalDate to, int quantity, int guests) {
            this.type = type;
            this.from = from;
            this.to = to;
            this.quantity = quantity;
            this.guests = guests;
        }
    }
}
