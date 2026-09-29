package controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import dao.BookingDAO;
import dao.RoomDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import model.Customer;
import model.Room;
import model.User;

/**
 *
 * @author Đặng Hoàng Vũ
 */
@WebServlet(urlPatterns = {"/booking"})
public class BookingServlet extends HttpServlet {

    /**
     * This method is auto-generated and outputs a basic HTML page.
     * It is not used in the actual business logic.
     *
     * @param request  HttpServletRequest object from client
     * @param response HttpServletResponse object to send data to client
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");

        // Return a simple test HTML page
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet MainPageServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MainPageServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles HTTP GET requests to the /booking endpoint.
     * This method determines whether to show a booking form or search form,
     * depending on whether the room number and check-in/check-out dates are provided.
     *
     * @param request  HttpServletRequest object from client
     * @param response HttpServletResponse object to send data to client
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the current session, do not create a new one if it doesn't exist
        HttpSession session = request.getSession(false);

        // Get the currently logged-in user
        User loggedUser = (User) session.getAttribute("loggedUser");

        // If the user is an admin, redirect them to the room management page
        if (loggedUser.getRole().equals("admin")) {
            response.sendRedirect(request.getContextPath() + "/room");
            return;
        }

        // If the room number and dates are already selected, show the booking form
        if (request.getParameter("roomNumber") != null
                && session.getAttribute("checkInDate") != null
                && session.getAttribute("checkOutDate") != null) {

            // Parse the room number from request
            int roomNumber = Integer.parseInt(request.getParameter("roomNumber"));

            RoomDAO roomDAO = new RoomDAO();

            // Check if the room exists
            if (!roomDAO.doesRoomExist(roomNumber)) {
                // Redirect to home if room does not exist
                response.sendRedirect("./");
                return;
            }

            // Get room details and forward to booking page
            Room room = roomDAO.getRoomByNumber(roomNumber);
            request.setAttribute("room", room);
            request.getRequestDispatcher("/WEB-INF/booking/booking.jsp").forward(request, response);
            return;
        }

        // If room number or dates not provided, show the room search form
        request.getRequestDispatcher("/WEB-INF/booking/search.jsp").forward(request, response);
    }

    /**
     * Handles HTTP POST requests to the /booking endpoint.
     * This method performs room availability check, calculates total cost,
     * creates a new booking, and redirects to room page on success.
     *
     * @param request  HttpServletRequest object from client
     * @param response HttpServletResponse object to send data to client
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If no room number is provided, redirect to the booking form
        if (request.getParameter("room-number") == null || request.getParameter("room-number").isEmpty()) {
            response.sendRedirect("./booking");
            return;
        }

        // Parse the room number from the form input
        int roomNumber = Integer.parseInt(request.getParameter("room-number"));
        RoomDAO roomDAO = new RoomDAO();

        // Check if the room exists
        if (!roomDAO.doesRoomExist(roomNumber)) {
            response.sendRedirect("./booking");
            return;
        }

        // Get the room details
        Room room = roomDAO.getRoomByNumber(roomNumber);

        // Get session and retrieve previously stored dates
        HttpSession session = request.getSession();
        LocalDate checkinDate = (LocalDate) session.getAttribute("checkInDate");
        LocalDate checkOutDate = (LocalDate) session.getAttribute("checkOutDate");

        BookingDAO bDAO = new BookingDAO();

        // Check if the room is already booked in the given date range
        if (bDAO.isRoomBookedInRange(roomNumber, checkinDate, checkOutDate)) {
            // Redirect with error if already booked
            response.sendRedirect("./booking?roomNumber=" + roomNumber + "&error=already-booked");
            return;
        }

        // Calculate total days of stay
        BigDecimal totalDays = BigDecimal.valueOf(ChronoUnit.DAYS.between(checkinDate, checkOutDate));

        // Calculate total price = number of days × price per night
        BigDecimal totalPrice = totalDays.multiply(room.getRoomType().getPricePerNight());

        // Get the customer object from session
        Customer customer = (Customer) session.getAttribute("customer");

        // Create the booking in the database
        bDAO.create(customer.getCustomerID(), roomNumber, checkinDate, checkOutDate, totalPrice);

        // Redirect to the room listing after successful booking
        response.sendRedirect("./room");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet that handles room booking requests.";
    }// </editor-fold>
}
