package controller;

import dao.ReportDAO;
import dao.ReportRecordsDAO;
import model.ReportFilter;
import model.User;
import model.Payment;
import model.Review;
import model.RevenueReport;

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

@WebServlet(name = "ReportsServlet", urlPatterns = {"/reports"})
public class ReportsServlet extends HttpServlet {

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

        String view = trim(request.getParameter("view"));

        if (view.isEmpty()) {
            view = "bookings";
        }

        switch (view) {
            case "bookings":
            case "payments":
            case "reviews":
                showRecords(request, response, view);
                return;

            case "period":
            case "month":
            case "year":
                showRevenue(request, response, view);
                return;

            default:
                response.sendError(404);
        }
    }

    private void showRecords(
            HttpServletRequest request,
            HttpServletResponse response,
            String view)
            throws ServletException, IOException {

        request.setAttribute("reportView", view);

        ReportFilter filter = new ReportFilter(
                view,
                null,
                null,
                "",
                "",
                null,
                10
        );

        setFilter(request, filter, 1, 1);

        try {
            String fromText = trim(
                    request.getParameter("fromDate")
            );
            String toText = trim(
                    request.getParameter("toDate")
            );

            LocalDate from = fromText.isEmpty()
                    ? null : LocalDate.parse(fromText);

            LocalDate to = toText.isEmpty()
                    ? null : LocalDate.parse(toText);

            String id = trim(
                    request.getParameter("bookingId")
            );
            String size = trim(
                    request.getParameter("pageSize")
            );

            filter = new ReportFilter(
                    view,
                    from,
                    to,
                    trim(request.getParameter("status")),
                    trim(request.getParameter("method")),
                    id.isEmpty() ? null : Integer.valueOf(id),
                    size.isEmpty() ? 10 : Integer.parseInt(size)
            );

            setFilter(request, filter, 1, 1);

            ReportRecordsDAO dao = new ReportRecordsDAO();

            long total = dao.countRecords(filter);

            long pages = total == 0
                    ? 1 : 1 + (total - 1) / filter.getPageSize();

            long page = Math.min(
                    parsePage(request.getParameter("page-index")),
                    pages
            );

            if ("bookings".equals(view)) {
                request.setAttribute(
                        "bookings",
                        dao.getBookingRecords(filter, page)
                );

            } else if ("payments".equals(view)) {
                RevenueReport summary =
                        dao.getPaymentSummary(filter);

                List<Payment> records =
                        dao.getPaymentRecords(filter, page);

                request.setAttribute(
                        "paymentSummary",
                        summary
                );
                request.setAttribute("payments", records);

            } else {
                ReportRecordsDAO.ReviewReportSummary summary =
                        dao.getReviewSummary(filter);

                List<Review> records =
                        dao.getReviewRecords(filter, page);

                request.setAttribute(
                        "reviewSummary",
                        summary
                );
                request.setAttribute("reviews", records);
            }

            request.setAttribute("totalRecords", total);
            setFilter(request, filter, page, pages);

        } catch (DateTimeParseException ex) {
            reportError(
                    request,
                    response,
                    400,
                    "Please enter valid dates."
            );

        } catch (NumberFormatException ex) {
            reportError(
                    request,
                    response,
                    400,
                    "Booking ID and page size must be valid numbers."
            );

        } catch (IllegalArgumentException ex) {
            reportError(
                    request,
                    response,
                    400,
                    ex.getMessage()
            );

        } catch (IllegalStateException ex) {
            getServletContext().log(
                    "Unable to load manager report.",
                    ex
            );

            reportError(
                    request,
                    response,
                    503,
                    "Report data is temporarily unavailable. "
                    + "Please try again."
            );
        }

        render(request, response);
    }

    private void reportError(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String message) {

        response.setStatus(status);
        request.setAttribute("reportError", message);
        request.setAttribute("reviewLoadError", message);
    }

    private void setFilter(
            HttpServletRequest request,
            ReportFilter filter,
            long page,
            long pages) {

        request.setAttribute("filter", filter);
        request.setAttribute(
                "fromDate",
                filter.getFromDate()
        );
        request.setAttribute(
                "toDate",
                filter.getToDate()
        );
        request.setAttribute(
                "filterActive",
                filter.getFromDate() != null
        );
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", pages);

        String base = request.getContextPath() + "/reports";

        request.setAttribute(
                "bookingsUrl",
                base + "?view=bookings" + dates(filter)
        );
        request.setAttribute(
                "paymentsUrl",
                base + "?view=payments" + dates(filter)
        );
        request.setAttribute(
                "reviewsUrl",
                base + "?view=reviews" + dates(filter)
        );
        request.setAttribute(
                "revenueUrl",
                filter.getFromDate() == null
                        ? base + "?view=month"
                        : base + "?view=period" + dates(filter)
        );
        request.setAttribute(
                "dashboardUrl",
                request.getContextPath() + "/dashboard"
                + (filter.getFromDate() == null
                        ? ""
                        : "?fromDate=" + filter.getFromDate()
                        + "&toDate=" + filter.getToDate())
        );
        request.setAttribute(
                "resetUrl",
                base + "?view=" + filter.getView()
        );
        request.setAttribute(
                "reviewRetryUrl",
                url(base, filter, page)
        );
        request.setAttribute(
                "firstPageUrl",
                url(base, filter, 1)
        );
        request.setAttribute(
                "previousPageUrl",
                url(base, filter, Math.max(1, page - 1))
        );
        request.setAttribute(
                "nextPageUrl",
                url(base, filter, Math.min(pages, page + 1))
        );
        request.setAttribute(
                "lastPageUrl",
                url(base, filter, pages)
        );
    }

    private void revenueLinks(
            HttpServletRequest request,
            LocalDate from,
            LocalDate to) {

        String base =
                request.getContextPath() + "/reports?view=";

        String range = from == null
                ? ""
                : "&fromDate=" + from + "&toDate=" + to;

        request.setAttribute(
                "revenueBookingsUrl",
                base + "bookings" + range
        );
        request.setAttribute(
                "revenuePaymentsUrl",
                base + "payments" + range
        );
        request.setAttribute(
                "revenueReviewsUrl",
                base + "reviews" + range
        );
    }

    private String dates(ReportFilter filter) {
        return filter.getFromDate() == null
                ? ""
                : "&fromDate=" + filter.getFromDate()
                + "&toDate=" + filter.getToDate();
    }

    private String url(
            String base,
            ReportFilter filter,
            long page) {

        return base + "?view=" + filter.getView()
                + dates(filter)
                + "&status=" + filter.getStatus()
                + "&method=" + filter.getMethod()
                + (filter.getBookingId() == null
                        ? ""
                        : "&bookingId=" + filter.getBookingId())
                + "&pageSize=" + filter.getPageSize()
                + "&page-index=" + page;
    }

    private long parsePage(String value) {
        try {
            return Math.max(
                    1,
                    Long.parseLong(trim(value))
            );
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void showRevenue(
            HttpServletRequest request,
            HttpServletResponse response,
            String view)
            throws ServletException, IOException {

        if ("period".equals(view)) {
            try {
                LocalDate from = LocalDate.parse(
                        trim(request.getParameter("fromDate"))
                );
                LocalDate to = LocalDate.parse(
                        trim(request.getParameter("toDate"))
                );

                util.ReportPeriod.validate(from, to);

                request.setAttribute(
                        "reportData",
                        new ReportDAO().getRevenueForPeriod(from, to)
                );
                request.setAttribute("view", view);
                request.setAttribute("fromDate", from);
                request.setAttribute("toDate", to);
                request.setAttribute(
                        "selectedYear",
                        from.getYear()
                );

                revenueLinks(request, from, to);

                request.setAttribute(
                        "dashboardUrl",
                        request.getContextPath()
                        + "/dashboard?fromDate=" + from
                        + "&toDate=" + to
                );

                render(request, response);

            } catch (DateTimeParseException | IllegalArgumentException ex) {
                response.sendError(
                        400,
                        "Invalid reporting period. Select both dates, "
                        + "at most 3,661 days apart."
                );

            } catch (IllegalStateException ex) {
                getServletContext().log(
                        "Unable to load period revenue.",
                        ex
                );
                response.sendError(
                        503,
                        "Revenue data is temporarily unavailable."
                );
            }

            return;
        }

        request.setAttribute(
                "dashboardUrl",
                request.getContextPath() + "/dashboard"
        );

        int currentYear = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        ).getYear();

        try {
            ReportDAO dao = new ReportDAO();

            List<Integer> years =
                    dao.getAvailableYears();

            int selectedYear = currentYear;

            if ("month".equals(view)) {
                String yearText = trim(
                        request.getParameter("year")
                );

                if (!yearText.isEmpty()) {
                    selectedYear = Integer.parseInt(yearText);
                }

                if (selectedYear < 1 || selectedYear > 9998) {
                    throw new IllegalArgumentException(
                            "Invalid report year."
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
            request.setAttribute(
                    "currentYear",
                    currentYear
            );
            request.setAttribute(
                    "selectedYear",
                    selectedYear
            );
            request.setAttribute(
                    "availableYears",
                    years
            );

            if ("month".equals(view)) {
                revenueLinks(
                        request,
                        LocalDate.of(selectedYear, 1, 1),
                        LocalDate.of(selectedYear, 12, 31)
                );
            } else {
                revenueLinks(request, null, null);
            }

            render(request, response);

        } catch (IllegalArgumentException ex) {
            response.sendError(
                    400,
                    "Invalid report year."
            );

        } catch (IllegalStateException ex) {
            getServletContext().log(
                    "Unable to load revenue report.",
                    ex
            );
            response.sendError(
                    503,
                    "Revenue data is temporarily unavailable."
            );
        }
    }

    private void render(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/reports/dashboard.jsp"
        ).forward(request, response);
    }
}