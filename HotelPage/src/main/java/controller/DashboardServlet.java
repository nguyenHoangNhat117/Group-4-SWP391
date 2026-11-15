package controller;

import dao.ReportDAO;
import model.ReportStatistic;
import model.RevenueReport;
import model.ReviewStatistics;
import model.User;
import model.OccupancyEstimate;
import util.ReportPeriod;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute("loggedUser");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        if (!"manager".equalsIgnoreCase(user.getRole())) {
            response.sendError(403);
            return;
        }

        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );

        LocalDate fromDate = today.withDayOfMonth(1);
        LocalDate toDate = today;

        request.setAttribute("today", today);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);

        try {
            String fromParameter = request.getParameter("fromDate");
            String toParameter = request.getParameter("toDate");

            boolean hasFrom = fromParameter != null
                    && !fromParameter.trim().isEmpty();

            boolean hasTo = toParameter != null
                    && !toParameter.trim().isEmpty();

            if (hasFrom != hasTo) {
                throw new IllegalArgumentException(
                        "Please select both start date and end date."
                );
            }

            if (hasFrom) {
                fromDate = LocalDate.parse(fromParameter.trim());
                toDate = LocalDate.parse(toParameter.trim());
            }

            request.setAttribute("fromDate", fromDate);
            request.setAttribute("toDate", toDate);

            ReportPeriod.validate(fromDate, toDate);

            if (toDate.isAfter(today)) {
                throw new IllegalArgumentException(
                        "Select an end date no later than today."
                );
            }

            ReportDAO dao = new ReportDAO();

            List<ReportStatistic> bookingStatistics =
                    dao.getBookingStatistics(fromDate, toDate);

            List<ReportStatistic> paymentStatistics =
                    dao.getPaymentStatistics(fromDate, toDate);

            List<ReportStatistic> roomStatistics =
                    dao.getRoomStatusStatistics();

            long customerCount = dao.getCustomerCount();

            ReviewStatistics reviewStatistics =
                    dao.getReviewStatistics();

            OccupancyEstimate occupancy =
                    dao.getOccupancyEstimate(
                            fromDate,
                            toDate,
                            today
                    );

            List<RevenueReport> monthlyRevenue =
                    dao.getRevenueForPeriod(fromDate, toDate);

            long totalBookings = 0;

            for (ReportStatistic item : bookingStatistics) {
                totalBookings += item.getTotalCount();
            }

            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal collected = BigDecimal.ZERO;
            BigDecimal refunded = BigDecimal.ZERO;

            for (RevenueReport item : monthlyRevenue) {
                totalRevenue = totalRevenue.add(
                        item.getTotalRevenue()
                );
                collected = collected.add(
                        item.getCollectedAmount()
                );
                refunded = refunded.add(
                        item.getRefundedAmount()
                );
            }

            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("collectedTotal", collected);
            request.setAttribute("refundedTotal", refunded);
            request.setAttribute("totalBookings", totalBookings);
            request.setAttribute(
                    "bookingStatistics",
                    bookingStatistics
            );
            request.setAttribute(
                    "paymentStatistics",
                    paymentStatistics
            );
            request.setAttribute(
                    "roomStatistics",
                    roomStatistics
            );
            request.setAttribute("customerCount", customerCount);
            request.setAttribute(
                    "reviewStatistics",
                    reviewStatistics
            );
            request.setAttribute("occupancy", occupancy);
            request.setAttribute(
                    "occupancyRate",
                    occupancy.getRate()
            );
            request.setAttribute(
                    "availableRooms",
                    occupancy.isApplicable()
                            ? occupancy.getRoomCount() : 0
            );
            request.setAttribute(
                    "monthlyRevenue",
                    monthlyRevenue
            );
            request.setAttribute(
                    "chartGrouping",
                    ReportPeriod.grouping(fromDate, toDate)
            );
            request.setAttribute(
                    "chartYear",
                    fromDate.getYear()
            );

        } catch (DateTimeParseException ex) {
            response.setStatus(400);
            request.setAttribute(
                    "dashboardError",
                    "Invalid date. Please use the date picker."
            );

        } catch (IllegalArgumentException ex) {
            response.setStatus(400);
            request.setAttribute(
                    "dashboardError",
                    ex.getMessage()
            );

        } catch (IllegalStateException ex) {
            getServletContext().log(
                    "Unable to load dashboard statistics.",
                    ex
            );

            response.setStatus(503);
            request.setAttribute(
                    "dashboardError",
                    "Dashboard data is temporarily unavailable. "
                    + "Please try again later."
            );
        }

        request.getRequestDispatcher(
                "/WEB-INF/reports/dashboard.jsp"
        ).forward(request, response);
    }
}