/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.CustomerDAO;
import model.User;
import dao.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Customer;

/**
 *
 * @author Đặng Hoàng Vũ
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    /**
     * Processes both GET and POST requests.
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
            /* Example HTML output (unused in production) */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LoginServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet LoginServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP GET method.
     * Used for displaying the login page and pre-filling saved cookies.
     *
     * @param request the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check if the user was redirected after password reset
        String success = request.getParameter("success");
        if ("reset".equals(success)) {
            request.setAttribute("message", "Password reset successful. Please sign in.");
        }

        // Retrieve cookies from the request
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Set saved username in the request
                if ("username".equals(cookie.getName())) {
                    request.setAttribute("enteredUsername", cookie.getValue());
                }
                // Set saved password (if any - though insecure)
                if ("password".equals(cookie.getName())) {
                    request.setAttribute("savedPassword", cookie.getValue());
                }
            }
        }

        // Forward the request to login.jsp for display
        request.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP POST method.
     * Used for authenticating the user based on form input.
     *
     * @param request the HttpServletRequest object containing form data
     * @param response the HttpServletResponse object for redirecting or forwarding
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if username is missing or empty
        if (request.getParameter("username") == null || request.getParameter("username").trim().equals("")) {
            request.setAttribute("error", "Missing username, Please try again.");
            request.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(request, response);
            return;
        }

        // Get the username from the form
        String username = request.getParameter("username");

        // Check if password is missing or empty
        if (request.getParameter("password") == null || request.getParameter("password").trim().equals("")) {
            request.setAttribute("error", "Missing password, Please try again.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(request, response);
            return;
        }

        // Get the password from the form
        String password = request.getParameter("password");

        // Instantiate DAO to access user data
        UserDAO dao = new UserDAO();

        // Attempt to log in with the given credentials
        User loggedUser = dao.login(username, password);

        // If login is successful
        if (loggedUser != null) {
            // Get or create the current session
            HttpSession session = request.getSession();

            // Save the logged-in user in the session
            session.setAttribute("loggedUser", loggedUser);

            // If the user role is "customer", also load and save customer details
            if (loggedUser.getRole().equals("customer")) {
                CustomerDAO cDAO = new CustomerDAO();
                Customer customer = cDAO.getCustomerByUserID(loggedUser.getId());
                session.setAttribute("customer", customer);
            }

            // Get the "remember" checkbox value
            String remember = request.getParameter("remember");

            if ("on".equals(remember)) {
                // If checked, create a cookie to store the username
                Cookie userCookie = new Cookie("username", username);
                userCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
                response.addCookie(userCookie);
            } else {
                // If not checked, delete the username cookie
                Cookie userCookie = new Cookie("username", "");
                userCookie.setMaxAge(0); // delete immediately
                response.addCookie(userCookie);
            }

            // Redirect to the home page after successful login
            response.sendRedirect(request.getContextPath() + "/");

        } else {
            // If login fails, return to login page with error message
            request.setAttribute("error", "No account found. Please sign up first!");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(request, response);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "LoginServlet handles user login authentication and session setup.";
    }

}
