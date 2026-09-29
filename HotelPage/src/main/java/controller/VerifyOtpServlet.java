/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

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
@WebServlet(name = "VerifyOtpServlet", urlPatterns = {"/verify-otp"})
public class VerifyOtpServlet extends HttpServlet {

    /* *
     * This method generates a simple HTML response page.
     * It's typically used for debugging or placeholder purposes.
     * 
     * @param request  the HTTP request from client
     * @param response the HTTP response to be sent to client
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type to HTML with UTF-8 encoding
        response.setContentType("text/html;charset=UTF-8");

        // Open PrintWriter to write HTML response
        try ( PrintWriter out = response.getWriter()) {
            // Output a sample HTML page
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet VerifyOtpServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet VerifyOtpServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /* *
     * Handles HTTP GET request by forwarding user to the OTP input page.
     * This page allows the user to input the OTP code they received (e.g. via email).
     * 
     * @param request  the HTTP request from client
     * @param response the HTTP response to be sent to client
     * @throws ServletException if forwarding fails
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to the JSP page where the user can enter their OTP
        request.getRequestDispatcher("/WEB-INF/login/enter_otp.jsp").forward(request, response);
    }

    /* *
     * Handles HTTP POST request which is triggered when the user submits the OTP form.
     * It validates the submitted OTP with the one stored in session and checks expiration.
     * 
     * If OTP is valid and not expired (within 5 minutes), redirects to reset password page.
     * Otherwise, reloads the OTP page with an error message.
     * 
     * @param request  the HTTP request from client containing submitted OTP
     * @param response the HTTP response to be sent to client
     * @throws ServletException if forwarding fails
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the user-submitted OTP value from form
        String otpInput = request.getParameter("otp");

        // Retrieve current session (creates new if not exist)
        HttpSession session = request.getSession();

        // Get the OTP value that was previously stored in session
        String otpStored = (String) session.getAttribute("otp");

        // Get the timestamp when OTP was generated and stored
        long otpTime = (long) session.getAttribute("otp_time");

        // Get the current system time in milliseconds
        long now = System.currentTimeMillis();

        // Check if submitted OTP matches stored one, and it is not expired (within 5 minutes)
        if (otpInput.equals(otpStored) && now - otpTime <= 5 * 60 * 1000) {
            // If valid and within time limit, redirect user to the reset password page
            response.sendRedirect(request.getContextPath() + "/reset-password");
        } else {
            // If OTP is wrong or expired, show error and reload OTP input form
            request.setAttribute("error", "The OTP is incorrect or has expired.");
            request.getRequestDispatcher("/WEB-INF/login/enter_otp.jsp").forward(request, response);
        }
    }

    /* *
     * Returns a short description of this servlet.
     * 
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "This servlet handles OTP verification logic during the password reset process.";
    }// </editor-fold>

}
