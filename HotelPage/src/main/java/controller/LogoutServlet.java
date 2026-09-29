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
 * @author Đặng Hoàng Vũ
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    /**
     * This method processes requests for both GET and POST methods.
     * It outputs a simple HTML response (for demonstration purposes).
     *
     * @param request the HttpServletRequest object that contains the client's request
     * @param response the HttpServletResponse object that contains the servlet's response
     * @throws ServletException if the request could not be handled
     * @throws IOException if an input or output error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set the content type of the response to HTML with UTF-8 encoding
        response.setContentType("text/html;charset=UTF-8");

        // Get the output writer to write the response body
        try (PrintWriter out = response.getWriter()) {
            // Print a basic HTML structure
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LogoutServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet LogoutServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP GET method for logging out a user.
     * It checks if a session exists, then invalidates it to log the user out,
     * and finally redirects the user to the home page.
     *
     * @param request the HttpServletRequest object that contains the client's request
     * @param response the HttpServletResponse object that contains the servlet's response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Retrieve the current session, do not create a new one if it doesn't exist
        HttpSession session = request.getSession(false);

        // If a session exists, invalidate it to log the user out
        if (session != null) {
            // Optionally, you can remove specific attributes instead of invalidating the session
            // session.removeAttribute("loggedUser");
            session.invalidate();
        }

        // Redirect the user to the home page after logging out
        response.sendRedirect(request.getContextPath() + "/");
    }

    /**
     * Handles the HTTP POST method.
     * This method delegates to the processRequest() method for processing.
     *
     * @param request the HttpServletRequest object that contains the client's request
     * @param response the HttpServletResponse object that contains the servlet's response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an input or output error occurs
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
