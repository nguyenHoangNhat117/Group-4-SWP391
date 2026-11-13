package controller;

import dao.AmenityDAO;
import dao.BookingDAO;
import dao.DiscountDAO;
import dao.RoomDAO;
import dao.RoomTypeDAO;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import model.BookingContact;
import model.Booking;
import model.Customer;
import model.RoomPriceBand;
import model.RoomType;
import model.User;
import util.ReportPeriod;
import util.OnlineStayPolicy;
import dao.ReviewDAO;

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
        if (request.getDispatcherType() == DispatcherType.REQUEST) {
            redirect(request, response, checkoutPath(draft));
            return;
        }
        Customer customer = requireCustomer(request, response, session, draft);
        if (customer == null) {
            return;
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
        ReviewDAO reviewDAO = new ReviewDAO();
        java.util.List<model.Review> typeReviews = reviewDAO.getReviewsByRoomTypeId(typeId);
        request.setAttribute("typeReviews", typeReviews);
        request.setAttribute("typeAverageStar",
                typeReviews.isEmpty() ? BigDecimal.ZERO : reviewDAO.getAverageStarByRoomTypeId(typeId));
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
        redirect(request, response, checkoutPath(draft));
    }

    private void confirmCheckout(HttpServletRequest request, HttpServletResponse response,
            StayDraft draft, Customer customer) throws ServletException, IOException {
        String voucher = request.getParameter("discount-code");
        Integer percentage = OnlineStayPolicy.percentage(request.getParameter("payment-plan"));
        BigDecimal payable = voucher == null || voucher.isBlank()
                ? draft.total() : getVoucherTotals(draft.total()).get(voucher);
        if (percentage == null || payable == null || payable.signum() <= 0
                || payable.stripTrailingZeros().scale() > 0
                || (percentage == OnlineStayPolicy.DEPOSIT_PERCENT && !OnlineStayPolicy.canDeposit(payable))) {
            request.setAttribute("paymentChoiceError", true);
            showCheckout(request, response, draft, customer);
            return;
        }
        int bookingId;
        synchronized (draft) {
            if (draft.bookingId > 0) {
                if (draft.paymentError == null) {
                    redirect(request, response, checkoutPath(draft));
                    return;
                }
                BookingDAO bookings = new BookingDAO();
                bookings.expirePendingBookings();
                Booking booking = bookings.getBookingById(draft.bookingId);
                if (booking == null || !"pending".equals(booking.getStatus())) {
                    redirect(request, response, "/history?view=bookings");
                    return;
                }
            } else {
                BookingContact contact;
                try {
                    contact = new BookingContact(request.getParameter("contact-name"), request.getParameter("contact-phone"),
                            request.getParameter("contact-email"), request.getParameter("contact-country"));
                } catch (IllegalArgumentException ex) {
                    request.setAttribute("checkoutError", true);
                    showCheckout(request, response, draft, customer);
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
                draft.contact = contact;
                draft.customerId = customer.getCustomerID();
            }
            bookingId = draft.bookingId;
            draft.paymentError = null;
            draft.paymentPlan = request.getParameter("payment-plan");
            draft.voucher = voucher;
        }
        forwardToPayment(request, response, draft, bookingId, percentage);
    }

    private void forwardToPayment(HttpServletRequest request, HttpServletResponse response,
            StayDraft draft, int bookingId, int percentage) throws ServletException, IOException {
        String paymentPath = "/payment?bookingId=" + bookingId;
        String errorPrefix = request.getContextPath() + paymentPath + "&error=";
        HttpServletResponse checkoutResponse = new HttpServletResponseWrapper(response) {
            @Override
            public void sendRedirect(String location) throws IOException {
                if (location.startsWith(errorPrefix)) {
                    synchronized (draft) {
                        draft.paymentError = location.substring(errorPrefix.length());
                    }
                    super.sendRedirect(request.getContextPath() + checkoutPath(draft));
                } else {
                    super.sendRedirect(location);
                }
            }
        };
        request.getRequestDispatcher(paymentPath + "&payment-method=bank_transfer&payment-percent=" + percentage)
                .forward(request, checkoutResponse);
    }

    private void showCheckout(HttpServletRequest request, HttpServletResponse response,
            StayDraft draft, Customer customer) throws ServletException, IOException {
        request.setAttribute("draftToken", draft.token);
        request.setAttribute("roomType", draft.type);
        request.setAttribute("checkIn", draft.from);
        request.setAttribute("checkOut", draft.to);
        request.setAttribute("quantity", draft.quantity);
        request.setAttribute("guests", draft.guests);
        String paymentPlan = request.getParameter("payment-plan");
        String voucher = request.getParameter("discount-code");
        synchronized (draft) {
            request.setAttribute("bookingContact", draft.contact);
            request.setAttribute("paymentError", draft.paymentError);
            request.setAttribute("paymentSubmitted", draft.bookingId > 0 && draft.paymentError == null);
            paymentPlan = paymentPlan == null ? draft.paymentPlan : paymentPlan;
            voucher = voucher == null ? draft.voucher : voucher;
        }
        request.setAttribute("selectedPlan", paymentPlan);
        request.setAttribute("selectedVoucher", voucher);
        BigDecimal total = draft.total();
        boolean canDeposit = OnlineStayPolicy.canDeposit(total);
        BigDecimal initialDue = canDeposit && !"transfer".equals(paymentPlan)
                ? OnlineStayPolicy.deposit(total) : total;
        request.setAttribute("total", total);
        request.setAttribute("canDeposit", canDeposit);
        request.setAttribute("initialDue", initialDue);
        request.setAttribute("initialBalance", total.subtract(initialDue));
        request.setAttribute("voucherTotals", getVoucherTotals(total));
        request.setAttribute("customer", customer);
        request.setAttribute("contactName", ((customer.getLastName() == null ? "" : customer.getLastName())
                + " " + (customer.getFirstName() == null ? "" : customer.getFirstName())).trim());
        request.getRequestDispatcher("/WEB-INF/booking/checkout.jsp").forward(request, response);
    }

    private Map<String, BigDecimal> getVoucherTotals(BigDecimal total) {
        Map<String, BigDecimal> voucherTotals = new LinkedHashMap<>();
        new DiscountDAO().getActivePromotions().forEach(voucher -> {
            if (voucher.getMinimumAmount() != null && total.compareTo(voucher.getMinimumAmount()) < 0) {
                return;
            }
            BigDecimal discount = total.multiply(voucher.getSaleOff())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (voucher.getMaximumDiscount() != null) {
                discount = discount.min(voucher.getMaximumDiscount());
            }
            BigDecimal discounted = total.subtract(discount);
            if (discounted.signum() > 0 && discounted.stripTrailingZeros().scale() <= 0) {
                voucherTotals.put(voucher.getCode(), discounted.setScale(0));
            }
        });
        return voucherTotals;
    }

    private Customer requireCustomer(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, StayDraft draft) throws IOException {
        Object user = session.getAttribute("loggedUser");
        if (!(user instanceof User)) {
            session.setAttribute("pendingBookingUrl", checkoutPath(draft));
            redirect(request, response, "/login");
            return null;
        }
        Object customer = session.getAttribute("customer");
        if (!"customer".equalsIgnoreCase(((User) user).getRole()) || !(customer instanceof Customer)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        Customer bookingCustomer = (Customer) customer;
        synchronized (draft) {
            if (draft.bookingId > 0 && draft.customerId != bookingCustomer.getCustomerID()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        }
        return bookingCustomer;
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

    private static String checkoutPath(StayDraft draft) {
        return "/payment?draft=" + draft.token;
    }

    private static final class StayDraft {

        private final String token = UUID.randomUUID().toString();
        private final RoomType type;
        private final LocalDate from;
        private final LocalDate to;
        private final int quantity;
        private final int guests;
        private int bookingId;
        private int customerId;
        private BookingContact contact;
        private String paymentError;
        private String paymentPlan;
        private String voucher;

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
