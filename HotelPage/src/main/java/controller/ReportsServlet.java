/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.BookingDAO;
import dao.PaymentDAO;
import dao.ReportDAO;
import dao.ReviewDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import model.Booking;
import model.Payment;
import model.RevenueReport;
import model.Review;

/**
 *
 * @author Đặng Hoàng Vũ
 */
@WebServlet(name = "ReportsServlet", urlPatterns = {"/reports"})
public class ReportsServlet extends HttpServlet {

    /**
     * Processes both GET and POST requests.
     * This is a placeholder method and is not used in real logic.
     *
     * @param request the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ReportsServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ReportsServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP GET method.
     * Dynamically loads and forwards different types of reports based on the "view" parameter.
     *
     * @param request the HttpServletRequest containing parameters like view, page-index, year
     * @param response the HttpServletResponse used to redirect or forward
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (request.getParameter("view") == null) {
            response.sendRedirect("./reports?view=bookings");
            return;
        }

        String view = request.getParameter("view");

        switch (view) {
            case "bookings": {
                // Load all bookings, delete unpaid overdue ones
                BookingDAO bDAO = new BookingDAO();
                bDAO.expirePendingBookings();
                List<Booking> allBookings = bDAO.getAll();

                int pageSize = 5;
                int pageIndex = 1;
                String pageParam = request.getParameter("page-index");

                if (pageParam != null) {
                    try {
                        pageIndex = Integer.parseInt(pageParam);
                    } catch (NumberFormatException ignored) {}
                }

                int totalBookings = allBookings.size();
                int totalPages = (int) Math.ceil((double) totalBookings / pageSize);
                if (totalPages == 0) totalPages = 1;

                if (pageParam == null || pageIndex < 1) {
                    response.sendRedirect("./reports?view=bookings&page-index=1");
                    return;
                } else if (pageIndex > totalPages) {
                    response.sendRedirect("./reports?view=bookings&page-index=" + totalPages);
                    return;
                }

                int fromIndex = (pageIndex - 1) * pageSize;
                int toIndex = Math.min(fromIndex + pageSize, totalBookings);
                List<Booking> pageBookings = allBookings.subList(fromIndex, toIndex);

                request.setAttribute("bookings", pageBookings);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", pageIndex);
                request.getRequestDispatcher("/WEB-INF/reports/books.jsp").forward(request, response);
                break;
            }
            case "payments": {
                // Load all payments and paginate
                PaymentDAO paymentDAO = new PaymentDAO();
                List<Payment> allPayments = paymentDAO.getAllPayments();

                int pageSize = 10;
                int pageIndex = 1;
                String pageParam = request.getParameter("page-index");

                if (pageParam != null) {
                    try {
                        pageIndex = Integer.parseInt(pageParam);
                    } catch (NumberFormatException ignored) {}
                } else {
                    response.sendRedirect("./reports?view=payments&page-index=1");
                    return;
                }

                int totalPayments = allPayments.size();
                int totalPages = (int) Math.ceil((double) totalPayments / pageSize);
                if (totalPages == 0) totalPages = 1;

                if (pageIndex < 1) {
                    response.sendRedirect("./reports?view=payments&page-index=1");
                    return;
                } else if (pageIndex > totalPages) {
                    response.sendRedirect("./reports?view=payments&page-index=" + totalPages);
                    return;
                }

                int fromIndex = (pageIndex - 1) * pageSize;
                int toIndex = Math.min(fromIndex + pageSize, totalPayments);
                List<Payment> pagePayments = allPayments.subList(fromIndex, toIndex);

                request.setAttribute("payments", pagePayments);
                request.setAttribute("currentPage", pageIndex);
                request.setAttribute("totalPages", totalPages);
                request.getRequestDispatcher("/WEB-INF/reports/payments.jsp").forward(request, response);
                break;
            }
            case "reviews": {
                // Load all reviews
                ReviewDAO dao = new ReviewDAO();
                List<Review> reviews = dao.getAllReviews();

                request.setAttribute("reviews", reviews);
                request.getRequestDispatcher("/WEB-INF/reports/reviews.jsp").forward(request, response);
                break;
            }
            case "month": {
                // Load monthly revenue report for a given year
                int currentYear = LocalDate.now().getYear();
                request.setAttribute("currentYear", currentYear);

                String yearParam = request.getParameter("year");
                int year = currentYear;

                if (yearParam != null) {
                    try {
                        year = Integer.parseInt(yearParam);
                    } catch (NumberFormatException ignored) {}
                }

                ReportDAO rdao = new ReportDAO();
                List<Integer> availableYears = rdao.getAvailableYears();
                List<RevenueReport> data = rdao.getRevenueByMonth(year);

                request.setAttribute("reportData", data);
                request.setAttribute("view", "month");
                request.setAttribute("selectedYear", year);
                request.setAttribute("availableYears", availableYears);

                request.getRequestDispatcher("/WEB-INF/reports/revenue_chart.jsp").forward(request, response);
                break;
            }
            case "year": {
                // Load yearly revenue report
                int currentYear = LocalDate.now().getYear();
                request.setAttribute("currentYear", currentYear);

                ReportDAO rdao = new ReportDAO();
                List<RevenueReport> data = rdao.getRevenueByYear();
                List<Integer> availableYears = rdao.getAvailableYears();

                int selectedYear = availableYears.isEmpty() ? 2025 : availableYears.get(availableYears.size() - 1);

                request.setAttribute("reportData", data);
                request.setAttribute("view", "year");
                request.setAttribute("availableYears", availableYears);
                request.setAttribute("selectedYear", selectedYear);

                request.getRequestDispatcher("/WEB-INF/reports/revenue_chart.jsp").forward(request, response);
                break;
            }
            default: {
                // Invalid view fallback
                request.getRequestDispatcher("/WEB-INF/error/error404.jsp").forward(request, response);
                break;
            }
        }
    }

    /**
     * Handles the HTTP POST method.
     * Currently delegates to processRequest (not used).
     *
     * @param request the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String describing the servlet
     */
    @Override
    public String getServletInfo() {
        return "Handles viewing reports of bookings, payments, reviews, and revenue (monthly/yearly)";
    }// </editor-fold>

}
