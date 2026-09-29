/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import model.User;
import dao.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Trần Nguyễn
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP GET and POST methods.
     * This method is just a placeholder and renders a basic HTML page.
     *
     * @param request the HttpServletRequest containing client request
     * @param response the HttpServletResponse used to return response to client
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an input/output error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            // Sample HTML output (not used in real scenario)
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RegisterServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RegisterServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP GET method.
     * Displays the registration form (JSP).
     *
     * @param request the HttpServletRequest used to access parameters and session
     * @param response the HttpServletResponse used to forward to JSP
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP POST method.
     * Validates user input, checks for existing username/email, and registers a new user if valid.
     *
     * @param request the HttpServletRequest containing submitted form data
     * @param response the HttpServletResponse used to forward or redirect after processing
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validate username presence
        if (request.getParameter("username") == null || request.getParameter("username").trim().equals("")) {
            request.setAttribute("error", "Missing username, Please try again.");
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        String username = request.getParameter("username");

        // Validate username length
        if (username.length() < 2) {
            request.setAttribute("error", "Username length must be greater than 2, Please try again.");
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        UserDAO dao = new UserDAO();

        // Check if username is already taken
        if (dao.isUsernameExists(username)) {
            request.setAttribute("error", "Username is already exists, Please try again.");
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        // Validate password presence
        if (request.getParameter("password") == null || request.getParameter("password").trim().equals("")) {
            request.setAttribute("error", "Missing password, Please try again.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        String password = request.getParameter("password");

        // Validate password length
        if (password.length() < 6) {
            request.setAttribute("error", "Password length must be greater than 6, Please try again.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        // Confirm password match
        String confirmPassword = request.getParameter("confirmPassword");
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Password and Confirm Password do not match.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        // Validate email presence
        if (request.getParameter("email") == null || request.getParameter("email").trim().equals("")) {
            request.setAttribute("error", "Missing email, Please try again.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        String email = request.getParameter("email");

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            request.setAttribute("error", "Submitting email must be in email format (example: abc@gmail.com)");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        // Check if email already exists
        if (dao.checkEmailExists(email)) {
            request.setAttribute("error", "This email is already registered.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
            return;
        }

        // Try registering the user
        boolean success = dao.register(username, password, email);

        if (success) {
            request.setAttribute("message", "Registration successful. Please log in.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Registration failed. Something is wrong. Please try again later.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher("/WEB-INF/register/register.jsp").forward(request, response);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Handles new user registration with validation for username, password, and email";
    }// </editor-fold>

}
