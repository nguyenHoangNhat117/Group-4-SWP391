/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.RoomDAO;
import dao.RoomTypeDAO;
import db.DBContext;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Room;
import model.RoomType;
import model.User;

/**
 *
 * @author Đặng Hoàng Vũ
 */
@WebServlet(name = "RoomServlet", urlPatterns = {"/room"})
public class RoomServlet extends HttpServlet {

    /**
     * Outputs a basic HTML page (default template-generated).
     *
     * @param request HTTP request object
     * @param response HTTP response object
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RoomServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RoomServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Show list of rooms for admin or search result for customers (with pagination).
     *
     * @param request HTTP request object
     * @param response HTTP response object
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RoomDAO roomDAO = new RoomDAO();
        HttpSession session = request.getSession();
        List<Room> availableRooms = (List<Room>) session.getAttribute("availableRooms");

        int totalPages;

        // If room list is already in session (e.g., from customer search)
        if (availableRooms != null) {
            totalPages = (int) (Math.ceil(availableRooms.size() * 1.0 / 5));
            request.setAttribute("totalPages", totalPages);
        } else {
            totalPages = roomDAO.getTotalPages();
            request.setAttribute("totalPages", totalPages);
        }

        // Redirect to first page if no page index is provided
        if (request.getParameter("page-index") == null) {
            response.sendRedirect("./room?page-index=1");
            return;
        } else {
            int pageIndex = Integer.parseInt(request.getParameter("page-index"));
            if (pageIndex < 1) {
                response.sendRedirect("./room?page-index=1");
                return;
            }
            if (pageIndex > totalPages) {
                response.sendRedirect("./room?page-index=" + totalPages);
                return;
            }

            // If session contains filtered room list (from search), paginate from that
            if (availableRooms != null) {
                LocalDate checkInDate = (LocalDate) session.getAttribute("checkInDate");
                LocalDate checkOutDate = (LocalDate) session.getAttribute("checkOutDate");
                availableRooms = roomDAO.getAvailableRoomPage(pageIndex, checkInDate, checkOutDate);
                request.setAttribute("rooms", availableRooms);
            } else {
                List<Room> rooms = roomDAO.getPage(pageIndex);
                request.setAttribute("rooms", rooms);
            }
        }

        request.getRequestDispatcher("/WEB-INF/room/room.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * Used for displaying views like list, create, update, delete.
     *
     * @param request HTTP request object
     * @param response HTTP response object
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        User loggedUser = (User) session.getAttribute("loggedUser");

        // Redirect customer to booking if they have not searched rooms
        if (loggedUser.getRole().equals("customer") && session.getAttribute("availableRooms") == null) {
            response.sendRedirect("./booking");
        }

        try {
            if (request.getParameter("view") == null) {
                showList(request, response);
                return;
            }

            String view = request.getParameter("view").strip();
            switch (view) {
                case "create": {
                    RoomTypeDAO rtDAO = new RoomTypeDAO();
                    List<RoomType> roomTypes = rtDAO.getAll();
                    request.setAttribute("roomTypes", roomTypes);
                    request.getRequestDispatcher("/WEB-INF/room/create.jsp").forward(request, response);
                    break;
                }
                case "delete": {
                    if (request.getParameter("roomNumber") == null || request.getParameter("roomNumber").trim().equals("")) {
                        response.sendRedirect("./room");
                        return;
                    }
                    request.getRequestDispatcher("/WEB-INF/room/delete.jsp").forward(request, response);
                    break;
                }
                case "update": {
                    RoomTypeDAO rtDAO = new RoomTypeDAO();
                    RoomDAO roomDAO = new RoomDAO();
                    List<RoomType> roomTypes = rtDAO.getAll();
                    request.setAttribute("roomTypes", roomTypes);

                    if (request.getParameter("roomNumber") == null || request.getParameter("roomNumber").trim().equals("")) {
                        response.sendRedirect("./room");
                        return;
                    }

                    int roomNumber = Integer.parseInt(request.getParameter("roomNumber"));

                    if (!roomDAO.doesRoomExist(roomNumber)) {
                        response.sendRedirect("./room");
                        return;
                    }

                    Room room = roomDAO.getRoomByNumber(roomNumber);
                    request.setAttribute("room", room);
                    session.setAttribute("updateRoomNumber", roomNumber);

                    request.getRequestDispatcher("/WEB-INF/room/update.jsp").forward(request, response);
                    break;
                }
                default: {
                    request.getRequestDispatcher("/WEB-INF/error/error404.jsp").forward(request, response);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * Handles form submission for create, update, delete, and search actions.
     *
     * @param request HTTP request object
     * @param response HTTP response object
     * @throws ServletException if servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RoomDAO roomDAO = new RoomDAO();
        String action = request.getParameter("action");

        switch (action) {
            case "create": {
                // Validate parameters
                if (request.getParameter("room-number") == null
                        || request.getParameter("status") == null
                        || request.getParameter("room-type-id") == null
                        || request.getParameter("room-number").trim().equals("")
                        || request.getParameter("status").trim().equals("")
                        || request.getParameter("room-type-id").trim().equals("")) {
                    response.sendRedirect("./room?view=create&error=missing-params");
                    return;
                }

                int roomNumber = Integer.parseInt(request.getParameter("room-number").trim());

                if (roomDAO.doesRoomExist(roomNumber)) {
                    response.sendRedirect("./room?view=create&error=room-exist");
                    return;
                }

                String status = request.getParameter("status").trim();
                int roomTypeId = Integer.parseInt(request.getParameter("room-type-id").trim());
                roomDAO.create(roomNumber, status, roomTypeId);
                break;
            }
            case "delete": {
                if (request.getParameter("roomNumber") == null || request.getParameter("roomNumber").trim().equals("")) {
                    response.sendRedirect("./room");
                    return;
                }
                int roomNumber = Integer.parseInt(request.getParameter("roomNumber"));

                if (!roomDAO.doesRoomExist(roomNumber)) {
                    response.sendRedirect("./room");
                    return;
                }

                roomDAO.delete(roomNumber);
                break;
            }
            case "update": {
                HttpSession session = request.getSession();
                int currentRoomNumber = (int) session.getAttribute("updateRoomNumber");

                if (request.getParameter("room-number") == null
                        || request.getParameter("status") == null
                        || request.getParameter("room-type-id") == null
                        || request.getParameter("room-number").trim().equals("")
                        || request.getParameter("status").trim().equals("")
                        || request.getParameter("room-type-id").trim().equals("")) {
                    response.sendRedirect("./room?view=update&roomNumber=" + currentRoomNumber + "&error=missing-params");
                    return;
                }

                int roomNumber;

                try {
                    roomNumber = Integer.parseInt(request.getParameter("room-number").trim());
                } catch (NumberFormatException e) {
                    response.sendRedirect("./room?view=update&id=" + currentRoomNumber + "&error=number-format");
                    return;
                }

                if (!roomDAO.doesRoomExist(roomNumber)) {
                    response.sendRedirect("./room?view=update&roomNumber=" + currentRoomNumber + "&error=room-not-exist");
                    return;
                }

                if (roomNumber != currentRoomNumber) {
                    response.sendRedirect("./room?view=update&roomNumber=" + currentRoomNumber + "&error=room-different");
                    return;
                }

                String status = request.getParameter("status").trim();
                int roomTypeId = Integer.parseInt(request.getParameter("room-type-id").trim());
                roomDAO.update(roomNumber, status, roomTypeId);
                break;
            }
            case "search": {
                HttpSession session = request.getSession();
                if (request.getParameter("booking-dates") == null || request.getParameter("booking-dates").trim().equals("")) {
                    response.sendRedirect("./booking?error=missing-dates");
                    return;
                }

                String bookingDates = request.getParameter("booking-dates");
                session.setAttribute("dates", bookingDates);
                String[] dates = bookingDates.split(" - ");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
                LocalDate checkInDate = LocalDate.parse(dates[0], formatter);
                LocalDate checkOutDate = LocalDate.parse(dates[1], formatter);

                List<Room> availableRooms = roomDAO.getAvailableRooms(checkInDate, checkOutDate);

                session.setAttribute("checkInDate", checkInDate);
                session.setAttribute("checkOutDate", checkOutDate);
                session.setAttribute("availableRooms", availableRooms);
                break;
            }
            default: {
                break;
            }
        }

        response.sendRedirect("./room?page-index=1");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
