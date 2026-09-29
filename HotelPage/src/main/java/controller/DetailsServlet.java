/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.ReviewDAO;
import dao.RoomDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;

/**
 *
 * @author Đặng Hoàng Vũ
 */
@WebServlet(name = "DetailsServlet", urlPatterns = {"/details"})
public class DetailsServlet extends HttpServlet {

    /**
     * Handles requests for both GET and POST methods.
     * 
     * Currently used for testing purpose; generates a sample HTML response.
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set the content type to HTML with UTF-8 encoding
        response.setContentType("text/html;charset=UTF-8");

        // Create a PrintWriter to write HTML response
        try (PrintWriter out = response.getWriter()) {
            // Output sample HTML page (placeholder content)
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DetailsServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet DetailsServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP GET method.
     * 
     * This method loads the room's details by its number, retrieves all reviews
     * associated with the room, calculates the average rating, and forwards
     * the data to the JSP page for display.
     *
     * @param request  the HttpServletRequest containing the roomNumber parameter
     * @param response the HttpServletResponse
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Create DAO to interact with room data
        RoomDAO roomDAO = new RoomDAO();

        // Parse the room number from request parameter
        int roomNumber = Integer.parseInt(request.getParameter("roomNumber"));

        // Retrieve the room information using the room number
        Room room = roomDAO.getRoomByNumber(roomNumber);

        // Set the room object as an attribute to send to the JSP
        request.setAttribute("room", room);

        // Create DAO to interact with review data
        ReviewDAO reviewDAO = new ReviewDAO();

        // Get the list of reviews for the given room number
        request.setAttribute("reviews", reviewDAO.getReviewsByRoomNumber(roomNumber));

        // Check if the room has any reviews
        if (reviewDAO.getReviewsByRoomNumber(roomNumber).size() > 0) {
            // If reviews exist, calculate and set the average star rating
            request.setAttribute("averageStar", reviewDAO.getAverageStarByRoomNumber(roomNumber));
        } else {
            // If no reviews, set average rating to 0
            request.setAttribute("averageStar", 0);
        }

        // Forward the request and response to the details.jsp page for rendering
        request.getRequestDispatcher("/WEB-INF/details/details.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP POST method.
     * 
     * Currently not used for processing; redirects to the processRequest method.
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect POST request to processRequest (currently placeholder HTML)
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet that shows room details and its associated reviews.";
    }// </editor-fold>

}
