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
@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    /**
     * Generates a simple HTML page. This method is not used in the real flow but
     * can serve as a fallback or debug placeholder.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set content type to HTML with UTF-8 encoding
        response.setContentType("text/html;charset=UTF-8");

        // Output a basic HTML response
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ForgotPasswordServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ForgotPasswordServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles HTTP GET requests.
     *
     * This method is called when the user accesses the "/forgot-password" page.
     * It simply forwards the request to the JSP page that contains the email form.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Forward the user to the forgot password JSP page
        request.getRequestDispatcher("/WEB-INF/login/forgot_password.jsp").forward(request, response);
    }

    /**
     * Handles HTTP POST requests.
     *
     * This method processes the form submission when a user submits their email
     * to reset the password. It checks if the email exists, generates an OTP,
     * stores the OTP and email in the session, sends the OTP via email, and
     * forwards the user to the OTP entry page. If the email does not exist, it
     * forwards back to the forgot password page with an error message.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the email entered by the user from the form
        String email = request.getParameter("email");

        // Create instance of DAO to interact with database
        UserDAO dao = new UserDAO();

        // Check if email exists in the user database
        if (dao.checkEmailExists(email)) {

            // Generate a random 6-digit OTP (from 100000 to 999999)
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

            // Get or create session for current user
            HttpSession session = request.getSession();

            // Store the OTP, email, and timestamp in session
            session.setAttribute("otp", otp); // Store generated OTP
            session.setAttribute("email", email); // Store email to verify later
            session.setAttribute("otp_time", System.currentTimeMillis()); // Store time for timeout handling

            // Send OTP to user via email
            util.EmailUtility.sendOtpEmail(email, otp);

            // Forward to the page where user can input received OTP
            request.getRequestDispatcher("/WEB-INF/login/enter_otp.jsp").forward(request, response);
        } else {
            // If email does not exist, show error message on forgot password page
            request.setAttribute("error", "Email does not exist in user database.");
            request.getRequestDispatcher("/WEB-INF/login/forgot_password.jsp").forward(request, response);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Handles the forgot password logic including OTP generation and email sending.";
    }// </editor-fold>
}
