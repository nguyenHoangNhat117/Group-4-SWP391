package controller;

import dao.ReportDAO;
import dao.ReportRecordsDAO;
import dao.ReviewDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(
        name = "ReportsServlet",
        urlPatterns = {"/reports"}
)
public class ReportsServlet extends HttpServlet {

    private static final int PAGE_SIZE = 5;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        // AuthenticationFilter also revalidates account status and role.
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

        String view = request.getParameter("view");

        if (view == null || view.trim().isEmpty()) {
            view = "bookings";
        }

        switch (view) {
            case "bookings":
            case "payments":
                showRecords(request, response, view);
                return;

            case "reviews": {

                try {
                    // Publish records only after the complete query succeeds.
                    request.setAttribute(
                            "reviews",
                            new ReviewDAO().getAllReviews()
                    );

                } catch (IllegalStateException ex) {

                    // Keep technical details in server logs.
                    getServletContext().log(
                            "Unable to load the Manager review report.",
                            ex
                    );

                    response.setStatus(
                            HttpServletResponse.SC_SERVICE_UNAVAILABLE
                    );

                    request.setAttribute(
                            "reviewLoadError",
                            "Review data is temporarily unavailable. "
                            + "Please try again later."
                    );
                }

                request.getRequestDispatcher(
                        "/WEB-INF/reports/reviews.jsp"
                ).forward(request, response);

                return;
            }

            case "month":
            case "year":
                showRevenue(request, response, view);
                return;

            default:
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
        }
    }

    private void showRecords(
            HttpServletRequest request,
            HttpServletResponse response,
            String view)
            throws ServletException, IOException {

        request.setAttribute("reportView", view);
        request.setAttribute("filterActive", false);

        configureLinks(request, view, null, null, 1, 1);

        try {
            String fromParameter
                    = trim(request.getParameter("fromDate"));

            String toParameter
                    = trim(request.getParameter("toDate"));

            LocalDate fromDate = null;
            LocalDate toDate = null;

            if (!fromParameter.isEmpty()
                    || !toParameter.isEmpty()) {

                if (fromParameter.isEmpty()
                        || toParameter.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Please select both start date and end date."
                    );
                }

                fromDate = LocalDate.parse(fromParameter);
                toDate = LocalDate.parse(toParameter);

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
            }

            ReportRecordsDAO dao = new ReportRecordsDAO();

            boolean bookingsView = "bookings".equals(view);

            long totalRecords = bookingsView
                    ? dao.countBookingRecords(fromDate, toDate)
                    : dao.countPaymentRecords(fromDate, toDate);

            long totalPages = totalRecords == 0
                    ? 1
                    : 1 + (totalRecords - 1) / PAGE_SIZE;

            // Clamp invalid or excessive page numbers to a valid page.
            long page = Math.min(
                    parsePage(request.getParameter("page-index")),
                    totalPages
            );

            if (bookingsView) {
                request.setAttribute(
                        "bookings",
                        dao.getBookingRecords(
                                fromDate,
                                toDate,
                                page,
                                PAGE_SIZE
                        )
                );
            } else {
                request.setAttribute(
                        "payments",
                        dao.getPaymentRecords(
                                fromDate,
                                toDate,
                                page,
                                PAGE_SIZE
                        )
                );
            }

            request.setAttribute(
                    "filterActive",
                    fromDate != null
            );
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPage", page);

            configureLinks(
                    request,
                    view,
                    fromDate,
                    toDate,
                    page,
                    totalPages
            );

        } catch (DateTimeParseException ex) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            request.setAttribute(
                    "reportError",
                    "Invalid date. Please use a valid date in YYYY-MM-DD format."
            );

        } catch (IllegalArgumentException ex) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            request.setAttribute(
                    "reportError",
                    ex.getMessage()
            );

        } catch (IllegalStateException ex) {

            getServletContext().log(
                    "Unable to load report records.",
                    ex
            );

            response.setStatus(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE
            );

            request.setAttribute(
                    "reportError",
                    "Report data is temporarily unavailable. Please try again."
            );
        }

        String jsp = "bookings".equals(view)
                ? "/WEB-INF/reports/books.jsp"
                : "/WEB-INF/reports/payments.jsp";

        request.getRequestDispatcher(jsp)
                .forward(request, response);
    }

    private void showRevenue(
            HttpServletRequest request,
            HttpServletResponse response,
            String view)
            throws ServletException, IOException {

        int currentYear = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        ).getYear();

        try {
            ReportDAO dao = new ReportDAO();
            List<Integer> years = dao.getAvailableYears();

            int selectedYear = currentYear;

            if ("month".equals(view)) {
                String parameter
                        = trim(request.getParameter("year"));

                if (!parameter.isEmpty()) {
                    selectedYear = Integer.parseInt(parameter);
                }

                if (selectedYear < 1 || selectedYear > 9998) {
                    throw new IllegalArgumentException(
                            "Report years must be between 1 and 9998."
                    );
                }

                request.setAttribute(
                        "reportData",
                        dao.getRevenueByMonth(selectedYear)
                );

            } else {
                if (!years.isEmpty()) {
                    selectedYear = years.get(years.size() - 1);
                }

                request.setAttribute(
                        "reportData",
                        dao.getRevenueByYear()
                );
            }

            request.setAttribute("view", view);
            request.setAttribute("currentYear", currentYear);
            request.setAttribute("selectedYear", selectedYear);
            request.setAttribute("availableYears", years);

            request.getRequestDispatcher(
                    "/WEB-INF/reports/revenue_chart.jsp"
            ).forward(request, response);

        } catch (IllegalArgumentException ex) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid report year."
            );

        } catch (IllegalStateException ex) {

            getServletContext().log(
                    "Unable to load revenue report.",
                    ex
            );

            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Revenue data is temporarily unavailable."
            );
        }
    }

    private void configureLinks(
            HttpServletRequest request,
            String view,
            LocalDate fromDate,
            LocalDate toDate,
            long page,
            long totalPages) {

        request.setAttribute(
                "bookingsUrl",
                reportUrl(
                        request, "bookings", 1,
                        fromDate, toDate
                )
        );

        request.setAttribute(
                "paymentsUrl",
                reportUrl(
                        request, "payments", 1,
                        fromDate, toDate
                )
        );

        request.setAttribute(
                "resetUrl",
                reportUrl(request, view, 1, null, null)
        );

        request.setAttribute(
                "firstPageUrl",
                reportUrl(request, view, 1, fromDate, toDate)
        );

        request.setAttribute(
                "previousPageUrl",
                reportUrl(
                        request, view, Math.max(1, page - 1),
                        fromDate, toDate
                )
        );

        request.setAttribute(
                "nextPageUrl",
                reportUrl(
                        request, view, Math.min(totalPages, page + 1),
                        fromDate, toDate
                )
        );

        request.setAttribute(
                "lastPageUrl",
                reportUrl(
                        request, view, totalPages,
                        fromDate, toDate
                )
        );

        String dashboardUrl
                = request.getContextPath() + "/dashboard";

        if (fromDate != null && toDate != null) {
            dashboardUrl += "?fromDate=" + fromDate
                    + "&toDate=" + toDate;
        }

        request.setAttribute("dashboardUrl", dashboardUrl);
    }

    private String reportUrl(
            HttpServletRequest request,
            String view,
            long page,
            LocalDate fromDate,
            LocalDate toDate) {

        String url = request.getContextPath()
                + "/reports?view=" + view
                + "&page-index=" + page;

        if (fromDate != null && toDate != null) {
            url += "&fromDate=" + fromDate
                    + "&toDate=" + toDate;
        }

        return url;
    }

    private long parsePage(String value) {

        try {
            return Math.max(1, Long.parseLong(trim(value)));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
