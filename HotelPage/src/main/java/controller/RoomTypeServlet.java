/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.RoomTypeDAO;
import db.DBContext;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomType;
import model.User;
import util.FileUploadUtil;

/**
 *
 * @author Đặng Hoàng Vũ
 */
@WebServlet(name = "RoomTypeServlet", urlPatterns = {"/room-type"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB threshold before writing to disk
        maxFileSize = 1024 * 1024 * 10, // Maximum individual file size (10MB)
        maxRequestSize = 1024 * 1024 * 50 // Total maximum request size (50MB)
)
public class RoomTypeServlet extends HttpServlet {

    /**
     * Processes both GET and POST requests.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RoomTypeServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RoomTypeServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Displays a paginated list of room types.
     *
     * @param request HTTP request from client
     * @param response HTTP response to be sent to client
     * @throws ServletException if forwarding fails
     * @throws IOException if redirect or forwarding fails
     */
    protected void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RoomTypeDAO rtDAO = new RoomTypeDAO();
        int totalPages = rtDAO.getTotalPages();
        request.setAttribute("totalPages", totalPages);

        if (request.getParameter("page-index") == null) {
            response.sendRedirect("./room-type?page-index=1");
            return;
        } else {
            int pageIndex = Integer.parseInt(request.getParameter("page-index"));
            if (pageIndex < 1) {
                response.sendRedirect("./room-type?page-index=1");
                return;
            }
            if (pageIndex > totalPages) {
                response.sendRedirect("./room-type?page-index=" + totalPages);
                return;
            }
            List<RoomType> roomTypes = rtDAO.getPage(pageIndex);
            request.setAttribute("roomTypes", roomTypes);
        }

        request.getRequestDispatcher("/WEB-INF/roomType/roomType.jsp").forward(request, response);
        return;
    }

    /**
     * Handles HTTP GET requests.
     *
     * @param request HTTP request from client
     * @param response HTTP response to be sent to client
     * @throws ServletException if request dispatching fails
     * @throws IOException if input/output operation fails
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedUser = (User) session.getAttribute("loggedUser");

        // Redirect if user is customer (not authorized)
        if (loggedUser.getRole().equals("customer")) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            if (request.getParameter("view") == null) {
                showList(request, response);
                return;
            }

            String view = request.getParameter("view").strip();
            RoomTypeDAO rtDAO = new RoomTypeDAO();

            switch (view) {
                case "create": {
                    request.getRequestDispatcher("/WEB-INF/roomType/create.jsp").forward(request, response);
                    break;
                }
                case "delete": {
                    if (request.getParameter("roomType") == null || request.getParameter("roomType").trim().isEmpty()
                            || request.getParameter("id") == null || request.getParameter("id").trim().isEmpty()) {
                        request.getRequestDispatcher("/WEB-INF/roomType/delete.jsp").forward(request, response);
                    }
                    break;
                }
                case "update": {
                    if (request.getParameter("id") == null || request.getParameter("id").trim().isEmpty()) {
                        response.sendRedirect("./room-type");
                        return;
                    }

                    int id = Integer.parseInt(request.getParameter("id").trim());
                    RoomType rt = rtDAO.getRoomTypeById(id);

                    String imgPath = rt.getPicture();
                    request.setAttribute("roomType", rt);
                    session.setAttribute("updateRoomTypeId", id);
                    session.setAttribute("updateName", rt.getName());
                    session.setAttribute("updateImgPath", imgPath);

                    request.getRequestDispatcher("/WEB-INF/roomType/update.jsp").forward(request, response);
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
     * Handles HTTP POST requests.
     *
     * @param request HTTP request from client
     * @param response HTTP response to be sent to client
     * @throws ServletException if servlet error occurs
     * @throws IOException if input/output error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (request.getParameter("action") == null) {
                showList(request, response);
                return;
            }

            String action = request.getParameter("action").strip();
            RoomTypeDAO rtDAO = new RoomTypeDAO();

            switch (action) {
                case "create": {
                    // Validate required parameters
                    if (request.getParameter("name") == null
                            || request.getParameter("description") == null
                            || request.getParameter("pricePerNight") == null
                            || request.getParameter("beds") == null
                            || request.getParameter("capacity") == null
                            || request.getPart("img") == null
                            || request.getParameter("name").trim().isEmpty()
                            || request.getParameter("description").trim().isEmpty()
                            || request.getParameter("pricePerNight").trim().isEmpty()
                            || request.getParameter("beds").trim().isEmpty()
                            || request.getParameter("capacity").trim().isEmpty()) {
                        response.sendRedirect("./room-type?view=create&error=missing-params");
                        return;
                    }

                    String name = request.getParameter("name").trim();
                    if (rtDAO.doesRoomTypeNameExist(name)) {
                        response.sendRedirect("./room-type?view=create&error=name-exist");
                        return;
                    }

                    String description = request.getParameter("description").trim();
                    BigDecimal pricePerNight;
                    int beds;
                    int capacity;

                    try {
                        pricePerNight = new BigDecimal(request.getParameter("pricePerNight").trim());
                        beds = Integer.parseInt(request.getParameter("beds").trim());
                        capacity = Integer.parseInt(request.getParameter("capacity").trim());

                        if (pricePerNight.compareTo(BigDecimal.ZERO) < 0 || beds <= 0 || capacity <= 0) {
                            response.sendRedirect("./room-type?view=create&error=number-format");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        response.sendRedirect("./room-type?view=create&error=number-format");
                        return;
                    }

                    Part imgPart = request.getPart("img");
                    if (imgPart.getSubmittedFileName().trim().isEmpty() || imgPart.getSize() == 0) {
                        response.sendRedirect("./room-type?view=create&error=missing-image");
                        return;
                    }

                    File folder = new File(FileUploadUtil.UPLOAD_LOCATION + "/roomType");
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    String imgFileName = imgPart.getSubmittedFileName();
                    File outFile = new File(folder, imgFileName);
                    FileUploadUtil.uploadFile(imgPart, outFile);

                    RoomType roomType = new RoomType();
                    roomType.setName(name);
                    roomType.setDescription(description);
                    roomType.setPricePerNight(pricePerNight);
                    roomType.setBeds(beds);
                    roomType.setCapacity(capacity);
                    roomType.setPicture("roomType/" + imgFileName);

                    rtDAO.createRoomType(roomType);
                    break;
                }
                case "delete": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    rtDAO.deleteRoomType(id);
                    break;
                }
                case "update": {
                    HttpSession session = request.getSession();
                    int currentId = (int) session.getAttribute("updateRoomTypeId");
                    String currentName = (String) session.getAttribute("updateName");

                    if (request.getParameter("id") == null
                            || request.getParameter("name") == null
                            || request.getParameter("description") == null
                            || request.getParameter("pricePerNight") == null
                            || request.getParameter("beds") == null
                            || request.getParameter("capacity") == null
                            || request.getPart("img") == null
                            || request.getParameter("id").trim().isEmpty()
                            || request.getParameter("name").trim().isEmpty()
                            || request.getParameter("description").trim().isEmpty()
                            || request.getParameter("pricePerNight").trim().isEmpty()
                            || request.getParameter("beds").trim().isEmpty()
                            || request.getParameter("capacity").trim().isEmpty()) {
                        response.sendRedirect("./room-type?view=update&id=" + currentId + "&error=missing-params");
                        return;
                    }

                    int id;
                    try {
                        id = Integer.parseInt(request.getParameter("id").trim());
                    } catch (NumberFormatException e) {
                        response.sendRedirect("./room-type?view=update&id=" + currentId + "&error=number-format");
                        return;
                    }

                    if (!rtDAO.doesIDExist(id)) {
                        response.sendRedirect("./room-type?view=update&id=" + currentId + "&error=id-not-exist");
                        return;
                    }

                    if (id != currentId) {
                        response.sendRedirect("./room-type?view=update&id=" + currentId + "&error=id-different");
                        return;
                    }

                    String name = request.getParameter("name").trim();
                    if (!name.equals(currentName) && rtDAO.doesRoomTypeNameExist(name)) {
                        response.sendRedirect("./room-type?view=update&id=" + currentId + "&error=name-exist");
                        return;
                    }

                    String description = request.getParameter("description").trim();
                    BigDecimal pricePerNight;
                    int beds;
                    int capacity;
                    try {
                        pricePerNight = new BigDecimal(request.getParameter("pricePerNight").trim());
                        beds = Integer.parseInt(request.getParameter("beds").trim());
                        capacity = Integer.parseInt(request.getParameter("capacity").trim());

                        if (pricePerNight.compareTo(BigDecimal.ZERO) < 0 || beds <= 0 || capacity <= 0) {
                            response.sendRedirect("./room-type?view=update&id=" + currentId + "&error=number-format");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        response.sendRedirect("./room-type?view=update&id=" + currentId + "&error=number-format");
                        return;
                    }

                    Part imgPart = request.getPart("img");
                    String picture;

                    if (imgPart.getSubmittedFileName().trim().isEmpty() || imgPart.getSize() == 0) {
                        picture = (String) session.getAttribute("updateImgPath");
                    } else {
                        File folder = new File(FileUploadUtil.UPLOAD_LOCATION + "/roomType");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }

                        String imgFileName = imgPart.getSubmittedFileName();
                        File outFile = new File(folder, imgFileName);
                        FileUploadUtil.uploadFile(imgPart, outFile);
                        picture = "roomType/" + imgFileName;
                    }

                    rtDAO.updateRoomType(id, name, description, pricePerNight, beds, capacity, picture);
                    break;
                }
                default: {
                    // No action matched
                    break;
                }
            }

            response.sendRedirect("./room-type?page-index=1");

        } catch (Exception ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Provides a brief description of the servlet.
     *
     * @return servlet description
     */
    @Override
    public String getServletInfo() {
        return "RoomTypeServlet handles room type management operations including CRUD.";
    }// </editor-fold>
}
