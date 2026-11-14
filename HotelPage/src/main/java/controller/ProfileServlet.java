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
import dao.UserDAO;
import model.Customer;
import model.User;


@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    /**
     * Processes both HTTP GET and POST requests. Currently used as a
     * placeholder with sample HTML output.
     *
     * @param request the HttpServletRequest object that contains the request
     * the client made
     * @param response the HttpServletResponse object that contains the response
     * the servlet returns
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an input or output error is detected
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            // Simple demo HTML output (not used in real scenario)
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ProfileServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ProfileServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP GET method. Retrieves the current logged-in user's
     * customer profile and forwards it to the profile JSP page.
     *
     * @param request the HttpServletRequest object with session information
     * @param response the HttpServletResponse object used to forward or
     * redirect
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get logged-in user from session
        User loggedUser = (User) request.getSession().getAttribute("loggedUser");

        // If user is not logged in, redirect to login page
        if (loggedUser == null) {
            response.sendRedirect("./login");
            return;
        }

        // Get customer information based on user's ID
        CustomerDAO dao = new CustomerDAO();
        Customer customer = dao.getCustomerByUserID(loggedUser.getId());

        // Set customer object as request attribute and forward to JSP
        request.setAttribute("customer", customer);
        request.getRequestDispatcher("/WEB-INF/profile/profile.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP POST method. Updates the profile of the logged-in user
     * with the provided form data.
     *
     * @param request the HttpServletRequest containing form data for profile
     * update
     * @param response the HttpServletResponse used to redirect after update
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        
        User loggedUser = (User) request.getSession().getAttribute("loggedUser");

        
        if (loggedUser == null) {
            response.sendRedirect("./login");
            return;
        }

        
        int userId = loggedUser.getId();

        // Retrieve updated profile fields from request
        String displayName = request.getParameter("displayName");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String country = request.getParameter("country");
        String city = request.getParameter("city");
        String street = request.getParameter("street");

        if (displayName == null
                || displayName.trim().isEmpty()
                || displayName.trim().length() > 100) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid display name"
            );
            return;
        }

        
        UserDAO userDAO = new UserDAO();

        boolean updated = userDAO.updateDisplayName(
                userId, displayName.trim()
        );

        if (!updated) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to update display name"
            );
            return;
        }


        CustomerDAO customerDAO = new CustomerDAO();

        customerDAO.updateCustomer(
                userId,
                firstName,
                lastName,
                email,
                phone,
                country,
                city,
                street
        );


        loggedUser.setDisplayName(displayName.trim());

        request.getSession().setAttribute(
                "loggedUser", loggedUser
        );


        response.sendRedirect(request.getContextPath() + "/profile");
        return;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String describing the servlet
     */
    @Override
    public String getServletInfo() {
        return "Handles user profile viewing and updating";
    }// </editor-fold>

}
