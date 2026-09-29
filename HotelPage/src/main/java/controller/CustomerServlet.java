/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.CustomerDAO;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import model.Customer;

/**
 * Servlet that handles the listing of customers with pagination.
 * 
 * URL pattern: /customers
 * 
 * Author: Đặng Hoàng Vũ
 */
@WebServlet(name = "CustomerServlet", urlPatterns = {"/customers"})
public class CustomerServlet extends HttpServlet {

    /**
     * This method is auto-generated for demo HTML purposes.
     * It is not used in actual application logic.
     *
     * @param request  the HttpServletRequest from the client
     * @param response the HttpServletResponse to send back to client
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        // Output sample HTML
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CustomerServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CustomerServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles HTTP GET requests for displaying a paginated list of customers.
     *
     * @param request  the HttpServletRequest containing the page-index (optional)
     * @param response the HttpServletResponse used to render the customer list
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Create DAO instance to interact with database
        CustomerDAO cDAO = new CustomerDAO();

        // Get full list of all customers
        List<Customer> allCustomers = cDAO.getAll();

        int pageSize = 10;   // Number of customers per page
        int pageIndex = 1;   // Default to page 1

        // Get the "page-index" parameter from the request
        String pageParam = request.getParameter("page-index");

        if (pageParam != null) {
            try {
                // Try to parse the page index from the URL
                pageIndex = Integer.parseInt(pageParam);
            } catch (NumberFormatException ignored) {
                // If parsing fails, keep default pageIndex = 1
            }
        }

        // Calculate total number of customers
        int totalCustomers = allCustomers.size();

        // Calculate total pages required
        int totalPages = (int) Math.ceil((double) totalCustomers / pageSize);
        if (totalPages == 0) {
            totalPages = 1;  // Ensure there's at least one page
        }

        // ✅ Validate the page index
        if (pageParam == null || pageIndex < 1) {
            // Redirect to page 1 if missing or less than 1
            response.sendRedirect("./customers?page-index=1");
            return;
        } else if (pageIndex > totalPages) {
            // Redirect to last valid page if requested index is too high
            response.sendRedirect("./customers?page-index=" + totalPages);
            return;
        }

        // Determine the start and end indexes for pagination
        int fromIndex = (pageIndex - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalCustomers);

        // Extract the list of customers for the current page
        List<Customer> pageCustomers = allCustomers.subList(fromIndex, toIndex);

        // Pass data to the JSP
        request.setAttribute("customers", pageCustomers);
        request.setAttribute("currentPage", pageIndex);
        request.setAttribute("totalPages", totalPages);

        // Forward the request to the JSP page to render the customer list
        request.getRequestDispatcher("/WEB-INF/customer/customer.jsp").forward(request, response);
    }

    /**
     * Handles HTTP POST requests. Currently not used directly, but calls processRequest().
     *
     * @param request  the HttpServletRequest from the client
     * @param response the HttpServletResponse sent to client
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Call processRequest (default template behavior, not used in actual logic)
        processRequest(request, response);
    }

    /**
     * Provides a brief description of this servlet.
     *
     * @return a String description
     */
    @Override
    public String getServletInfo() {
        return "Servlet that displays a paginated list of all customers.";
    }// </editor-fold>

}
