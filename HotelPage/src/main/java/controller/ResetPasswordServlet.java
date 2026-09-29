/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author Trần Nguyễn
 */
@WebServlet(name = "ResetPasswordServlet", urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    /**
     * Handles both GET and POST requests (Not used in actual logic).
     *
     * @param request The {@link HttpServletRequest} object that contains the client request
     * @param response The {@link HttpServletResponse} object that contains the response
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ResetPasswordServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ResetPasswordServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * Forwards the user to the reset password form.
     *
     * @param request The {@link HttpServletRequest} object that contains the client request
     * @param response The {@link HttpServletResponse} object that contains the response
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/login/reset_password.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * Validates the new password and updates it in the database for the corresponding email stored in session.
     *
     * @param request The {@link HttpServletRequest} object that contains the form data (new password)
     * @param response The {@link HttpServletResponse} object that sends the result back to the browser
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validate password input (empty check)
        if (request.getParameter("password") == null || request.getParameter("password").trim().equals("")) {
            request.setAttribute("error", "Missing password, Please try again.");
            request.getRequestDispatcher("/WEB-INF/login/reset_password.jsp").forward(request, response);
            return;
        }

        String password = request.getParameter("password");

        // Validate password length
        if (password.length() < 6) {
            request.setAttribute("error", "Password length must be greater than 6, Please try again.");
            request.getRequestDispatcher("/WEB-INF/login/reset_password.jsp").forward(request, response);
            return;
        }

        // Get the new password and user email from session
        String newPassword = request.getParameter("password");
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");

        // Hash the new password using MD5 (not recommended in real apps)
        String hashed = util.HashUtil.toMD5(newPassword);

        // Update the password in the database
        new UserDAO().updatePasswordByEmail(email, hashed);

        // Clear session after reset
        session.invalidate();

        // Redirect user to login page with success message
        response.sendRedirect(request.getContextPath() + "/login?success=reset");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return A brief description of the servlet's purpose
     */
    @Override
    public String getServletInfo() {
        return "Handles password reset requests by updating the user's password in the database.";
    }// </editor-fold>

}
