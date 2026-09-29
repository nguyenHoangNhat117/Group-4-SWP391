/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.DiscountDAO;
import db.DBContext;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

/**
 *
 * @author Đặng Hoàng Vũ
 */
@WebServlet(name = "DiscountServlet", urlPatterns = {"/discounts"})
public class DiscountServlet extends HttpServlet {

    /**
     * A placeholder method to generate a simple HTML page. Currently not used
     * in main logic.
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set content type to HTML with UTF-8 encoding
        response.setContentType("text/html;charset=UTF-8");

        // Print a simple HTML page
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DiscountServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet DiscountServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles HTTP GET requests.
     * 
     * Depending on the `view` parameter, this method forwards to the appropriate
     * JSP page for listing, creating, updating, or deleting discounts.
     * 
     * If no `view` is specified, the list of all discounts is retrieved and shown.
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Retrieve current session (do not create if not exists)
        HttpSession session = request.getSession(false);

        // Get the logged-in user from session
        User loggedUser = (User) session.getAttribute("loggedUser");

        // Redirect customers to home page - only admin/staff allowed
        if (loggedUser.getRole().equals("customer")) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            // If no view is specified, show the discount list page
            if (request.getParameter("view") == null) {
                DiscountDAO dDAO = new DiscountDAO();
                // Get all discount records from the database
                request.setAttribute("discounts", dDAO.getAll());
                // Forward to discount list JSP
                request.getRequestDispatcher("/WEB-INF/discount/discount.jsp").forward(request, response);
                return;
            }

            // Get value of "view" parameter to determine the action
            String view = request.getParameter("view").strip();

            switch (view) {
                case "create": {
                    // Forward to discount creation form
                    request.getRequestDispatcher("/WEB-INF/discount/create.jsp").forward(request, response);
                    break;
                }
                case "update": {
                    // Get discount ID from parameter
                    int id = Integer.parseInt(request.getParameter("id"));
                    DiscountDAO dDAO = new DiscountDAO();
                    // Load discount info by ID and send it to update form
                    request.setAttribute("discount", dDAO.getDiscountById(id));
                    request.getRequestDispatcher("/WEB-INF/discount/update.jsp").forward(request, response);
                    break;
                }
                case "delete": {
                    // Forward to delete confirmation page
                    request.getRequestDispatcher("/WEB-INF/discount/delete.jsp").forward(request, response);
                    break;
                }
                default: {
                    // If view is unknown, forward to error page
                    request.getRequestDispatcher("/WEB-INF/error/error404.jsp").forward(request, response);
                    break;
                }
            }
        } catch (Exception ex) {
            // Log any exceptions during processing
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles HTTP POST requests.
     * 
     * This method handles form submissions to create, update, or delete discounts,
     * based on the `action` parameter from the request.
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // If action is missing, redirect to list page
            if (request.getParameter("action") == null) {
                response.sendRedirect(request.getContextPath() + "/discounts");
                return;
            }

            // Determine the action to perform
            String action = request.getParameter("action").strip();
            DiscountDAO dDAO = new DiscountDAO();

            switch (action) {
                case "create": {
                    // Read discount data from form
                    String code = request.getParameter("code");
                    int quantity = Integer.parseInt(request.getParameter("quantity"));
                    BigDecimal saleOff = new BigDecimal(request.getParameter("sale-off"));
                    // Create new discount in database
                    dDAO.createDiscount(code, quantity, saleOff);
                    break;
                }
                case "update": {
                    // Read discount data and ID from form
                    int id = Integer.parseInt(request.getParameter("id"));
                    String code = request.getParameter("code");
                    int quantity = Integer.parseInt(request.getParameter("quantity"));
                    BigDecimal saleOff = new BigDecimal(request.getParameter("sale-off"));
                    // Update discount record in database
                    dDAO.updateDiscount(id, code, quantity, saleOff);
                    break;
                }
                case "delete": {
                    // Read ID of discount to delete
                    int id = Integer.parseInt(request.getParameter("id"));
                    // Delete discount by ID
                    dDAO.deleteDiscount(id);
                    break;
                }
                default: {
                    // Unknown action, show 404 error
                    request.getRequestDispatcher("/WEB-INF/error/error404.jsp").forward(request, response);
                    break;
                }
            }

            // Redirect back to the main discounts page after action
            response.sendRedirect(request.getContextPath() + "/discounts");

        } catch (Exception ex) {
            // Log unexpected exceptions during POST processing
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Handles CRUD operations for discount management.";
    }// </editor-fold>
}
