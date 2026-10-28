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

@WebServlet(
        name = "DashboardServlet",
        urlPatterns = {"/dashboard"}
)
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        // The authentication filter also validates the current account.
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
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );

        // Use the current month as the default reporting period.
        LocalDate fromDate = today.withDayOfMonth(1);
        LocalDate toDate = today;

        request.setAttribute("today", today);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);

        try {
            String fromParameter =
                    request.getParameter("fromDate");

            String toParameter =
                    request.getParameter("toDate");

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
                fromDate = LocalDate.parse(
                        fromParameter.trim()
                );

                toDate = LocalDate.parse(
                        toParameter.trim()
                );
            }

            // Store parsed dates only, never raw request values.
            request.setAttribute("fromDate", fromDate);
            request.setAttribute("toDate", toDate);

            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException(
                        "Start date must not be after end date."
                );
            }

            if (fromDate.getYear() < 1
                    || toDate.getYear() > 9998) {

                throw new IllegalArgumentException(
                        "Report years must be between 1 and 9998."
                );
            }

            ReportPeriod.validate(fromDate, toDate);
            if (toDate.isAfter(today)) {
                throw new IllegalArgumentException("Select an end date no later than today.");
            }
            ReportDAO reportDAO = new ReportDAO();

            BigDecimal totalRevenue;

            List<ReportStatistic> bookingStatistics =
                    reportDAO.getBookingStatistics(
                            fromDate,
                            toDate
                    );

            List<ReportStatistic> paymentStatistics =
                    reportDAO.getPaymentStatistics(
                            fromDate,
                            toDate
                    );

            List<ReportStatistic> roomStatistics =
                    reportDAO.getRoomStatusStatistics();

            long customerCount =
                    reportDAO.getCustomerCount();

            ReviewStatistics reviewStatistics =
                    reportDAO.getReviewStatistics();

            OccupancyEstimate occupancy = reportDAO.getOccupancyEstimate(fromDate, toDate, today);
            long totalBookings = 0;
            for (ReportStatistic item : bookingStatistics) totalBookings += item.getTotalCount();
            int chartYear = fromDate.getYear();
            List<RevenueReport> monthlyRevenue = reportDAO.getRevenueForPeriod(fromDate, toDate);
            // Derive the card from the same series to guarantee card/chart reconciliation.
            totalRevenue = BigDecimal.ZERO;
            for (RevenueReport item : monthlyRevenue) totalRevenue=totalRevenue.add(item.getTotalRevenue());
            request.setAttribute("occupancy", occupancy);
            request.setAttribute("chartGrouping", ReportPeriod.grouping(fromDate, toDate));

            // Publish statistics only after all queries succeed.
            request.setAttribute(
                    "totalRevenue",
                    totalRevenue
            );

            request.setAttribute(
                    "totalBookings",
                    totalBookings
            );

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

            request.setAttribute(
                    "customerCount",
                    customerCount
            );

            request.setAttribute(
                    "reviewStatistics",
                    reviewStatistics
            );

            request.setAttribute(
                    "occupancyRate",
                    occupancy.getRate()
            );

            request.setAttribute(
                    "availableRooms",
                    occupancy.isApplicable() ? occupancy.getRoomCount() : 0
            );

            request.setAttribute(
                    "monthlyRevenue",
                    monthlyRevenue
            );

            request.setAttribute(
                    "chartYear",
                    chartYear
            );



        } catch (DateTimeParseException ex) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            request.setAttribute(
                    "dashboardError",
                    "Invalid date. Please use the date picker."
            );

        } catch (IllegalArgumentException ex) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            request.setAttribute(
                    "dashboardError",
                    ex.getMessage()
            );

        } catch (IllegalStateException ex) {

            getServletContext().log(
                    "Unable to load dashboard statistics.",
                    ex
            );

            response.setStatus(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE
            );

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