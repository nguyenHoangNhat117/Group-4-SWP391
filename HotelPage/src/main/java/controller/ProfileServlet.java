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
import dao.ProfileDAO;

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
        Customer customer;
        util.AuthSupport.token(request.getSession());
        try {
            customer = "customer".equals(loggedUser.getRole())
                    ? dao.getCustomerByUserID(loggedUser.getId())
                    : new ProfileDAO().getStaffProfile(loggedUser.getId());
        } catch (java.sql.SQLException ex) {
            throw new ServletException("Unable to load profile", ex);
        }

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
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User loggedUser = (User) request.getSession()
                .getAttribute("loggedUser");

        if (loggedUser == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        if (!util.AuthSupport.checkCsrf(request, response)) return;
        int userId = loggedUser.getId();

        String displayName = trim(
                request.getParameter("displayName"));
        String firstName = trim(
                request.getParameter("firstName"));
        String lastName = trim(
                request.getParameter("lastName"));
        String email = trim(
                request.getParameter("email"));
        String phone = trim(
                request.getParameter("phone"));
        String country = trim(
                request.getParameter("country"));
        String city = trim(
                request.getParameter("city"));
        String street = trim(
                request.getParameter("street"));

        // Basic validation
        if (displayName.isEmpty()
                || displayName.length() > 100) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/profile?error=invalid-name");
            return;
        }

        if (!email.isEmpty()
                && !email.matches(
                        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/profile?error=invalid-email");
            return;
        }

        if (firstName.length() > 50 || lastName.length() > 50 || email.length() > 100
                || phone.length() > 20 || country.length() > 50 || city.length() > 50 || street.length() > 100) {
            response.sendRedirect(request.getContextPath() + "/profile?error=invalid-name");
            return;
        }
        // Save both tables in one transaction
        ProfileDAO profileDAO = new ProfileDAO();

        boolean success = !"customer".equals(loggedUser.getRole())
                ? profileDAO.updateStaffProfile(userId, displayName, firstName, lastName, email, phone, country, city, street)
                : profileDAO.updateProfile(
                userId,
                displayName,
                firstName,
                lastName,
                email,
                phone,
                country,
                city,
                street
        );

        if (!success) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/profile?error=update-failed");
            return;
        }

        // Update Session only after DB success
        loggedUser.setDisplayName(displayName);

        request.getSession().setAttribute(
                "loggedUser", loggedUser);

        response.sendRedirect(
                request.getContextPath()
                + "/profile?success=updated");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

}
