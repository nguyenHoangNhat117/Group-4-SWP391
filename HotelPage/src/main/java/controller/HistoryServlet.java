/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.BookingDAO;
import dao.PaymentDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Booking;
import model.Customer;
import model.Payment;
import model.User;

/**
 *
 * @author Đặng Hoàng Vũ
 */
@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet HistoryServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet HistoryServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("id") == null) {
            request.getRequestDispatcher("/WEB-INF/error/error404.jsp").forward(request, response);
            return;
        }

        int paramId = Integer.parseInt(request.getParameter("id"));

        if (request.getParameter("view") == null) {
            response.sendRedirect("./history?id=" + paramId + "&view=bookings");
            return;
        }

        HttpSession session = request.getSession();
        Customer customer = (Customer) session.getAttribute("customer");
        User user = (User) session.getAttribute("loggedUser");

        if (customer != null) {
            if (customer.getCustomerID() != paramId) {
                response.sendRedirect("./history?id=" + customer.getCustomerID() + "&view=bookings");
                return;
            }
        } else if (!user.getRole().equals("admin")) {
            response.sendRedirect("./login");
            return;
        }

        String view = request.getParameter("view");
        switch (view) {
            case "bookings": {
                BookingDAO bookingDAO = new BookingDAO();
                PaymentDAO paymentDAO = new PaymentDAO();

                bookingDAO.deleteUnpaidOverdueBookings();

                int pageSize = 5;
                int currentPage = 1;

                if (request.getParameter("page") != null) {
                    currentPage = Integer.parseInt(request.getParameter("page"));
                }

                int totalBookings = bookingDAO.countBookingsByCustomerID(paramId);
                int totalPages = (int) Math.ceil((double) totalBookings / pageSize);

                List<Booking> bookings = bookingDAO.getBookingsByCustomerID(paramId, currentPage, pageSize);

                Map<Integer, Boolean> paidMap = new HashMap<>();
                for (Booking b : bookings) {
                    paidMap.put(b.getId(), paymentDAO.isBookingPaid(b.getId()));
                }

                request.setAttribute("bookings", bookings);
                request.setAttribute("paidMap", paidMap); 
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", currentPage);
                request.getRequestDispatcher("/WEB-INF/history/books.jsp").forward(request, response);
                break;
            }

            case "payments": {
                PaymentDAO paymentDAO = new PaymentDAO();

                int pageSize = 5;
                int currentPage = 1; 

                if (request.getParameter("page") != null) {
                    currentPage = Integer.parseInt(request.getParameter("page"));
                }

                int totalPayments = paymentDAO.countPaymentsByCustomerId(paramId);
                int totalPages = (int) Math.ceil((double) totalPayments / pageSize);

                List<Payment> payments = paymentDAO.getPaymentsByCustomerId(paramId, currentPage, pageSize);

                request.setAttribute("payments", payments);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", currentPage);
                request.getRequestDispatcher("/WEB-INF/history/payments.jsp").forward(request, response);
                break;
            }
            default: {
                request.getRequestDispatcher("/WEB-INF/error/error404.jsp").forward(request, response);
                break;
            }
        }

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
