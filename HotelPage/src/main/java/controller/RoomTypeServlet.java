package controller;

import dao.RoomTypeDAO;
import db.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomType;
import model.User;
import util.FileUploadUtil;

@WebServlet(name = "RoomTypeServlet", urlPatterns = {"/room-type"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class RoomTypeServlet extends HttpServlet {

    // ĐÃ SỬA: xử lý trang thiếu/sai và danh sách trống.
    protected void showList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        RoomTypeDAO rtDAO = new RoomTypeDAO();
        int totalPages = rtDAO.getTotalPages();

        request.setAttribute("totalPages", totalPages);

        int pageIndex = 1;

        try {
            pageIndex = Integer.parseInt(
                    request.getParameter("page-index")
            );
        } catch (NumberFormatException ignored) {
            // Không có page-index hoặc giá trị sai thì dùng trang 1.
        }

        pageIndex = Math.max(
                1,
                Math.min(pageIndex, Math.max(1, totalPages))
        );

        request.setAttribute("pageIndex", pageIndex);
        request.setAttribute("roomTypes", rtDAO.getPage(pageIndex));

        request.getRequestDispatcher(
                "/WEB-INF/roomType/roomType.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User loggedUser = (User) session.getAttribute("loggedUser");

        // AuthenticationFilter hiện có bảo vệ route /room-type.
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
                    request.getRequestDispatcher(
                            "/WEB-INF/roomType/create.jsp"
                    ).forward(request, response);
                    break;
                }

                // ĐÃ SỬA: lấy loại phòng theo ID và mở trang xác nhận.
                case "delete": {
                    int id;

                    try {
                        id = Integer.parseInt(
                                request.getParameter("id")
                        );
                    } catch (NumberFormatException ex) {
                        response.sendError(
                                HttpServletResponse.SC_BAD_REQUEST
                        );
                        return;
                    }

                    RoomType roomType = rtDAO.getRoomTypeById(id);

                    if (roomType == null) {
                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND
                        );
                        return;
                    }

                    request.setAttribute("roomType", roomType);

                    request.getRequestDispatcher(
                            "/WEB-INF/roomType/delete.jsp"
                    ).forward(request, response);
                    break;
                }

                case "update": {
                    if (request.getParameter("id") == null
                            || request.getParameter("id").trim().isEmpty()) {
                        response.sendRedirect("./room-type");
                        return;
                    }

                    int id = Integer.parseInt(
                            request.getParameter("id").trim()
                    );

                    RoomType rt = rtDAO.getRoomTypeById(id);
                    String imgPath = rt.getPicture();

                    request.setAttribute("roomType", rt);
                    session.setAttribute("updateRoomTypeId", id);
                    session.setAttribute("updateName", rt.getName());
                    session.setAttribute("updateImgPath", imgPath);

                    request.getRequestDispatcher(
                            "/WEB-INF/roomType/update.jsp"
                    ).forward(request, response);
                    break;
                }

                default: {
                    request.getRequestDispatcher(
                            "/WEB-INF/error/error404.jsp"
                    ).forward(request, response);
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(DBContext.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
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

                        response.sendRedirect(
                                "./room-type?view=create&error=missing-params"
                        );
                        return;
                    }

                    String name = request.getParameter("name").trim();

                    if (rtDAO.doesRoomTypeNameExist(name)) {
                        response.sendRedirect(
                                "./room-type?view=create&error=name-exist"
                        );
                        return;
                    }

                    String description = request.getParameter(
                            "description"
                    ).trim();

                    BigDecimal pricePerNight;
                    int beds;
                    int capacity;

                    try {
                        pricePerNight = new BigDecimal(
                                request.getParameter("pricePerNight").trim()
                        );
                        beds = Integer.parseInt(
                                request.getParameter("beds").trim()
                        );
                        capacity = Integer.parseInt(
                                request.getParameter("capacity").trim()
                        );

                        if (pricePerNight.compareTo(BigDecimal.ZERO) < 0
                                || beds <= 0
                                || capacity <= 0) {
                            response.sendRedirect(
                                    "./room-type?view=create&error=number-format"
                            );
                            return;
                        }
                    } catch (NumberFormatException e) {
                        response.sendRedirect(
                                "./room-type?view=create&error=number-format"
                        );
                        return;
                    }

                    Part imgPart = request.getPart("img");

                    if (imgPart.getSubmittedFileName().trim().isEmpty()
                            || imgPart.getSize() == 0) {
                        response.sendRedirect(
                                "./room-type?view=create&error=missing-image"
                        );
                        return;
                    }

                    File folder = new File(
                            FileUploadUtil.UPLOAD_LOCATION + "/roomType"
                    );

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
                    int id = Integer.parseInt(
                            request.getParameter("id")
                    );
                    rtDAO.deleteRoomType(id);
                    break;
                }

                case "update": {
                    HttpSession session = request.getSession();

                    int currentId = (int) session.getAttribute(
                            "updateRoomTypeId"
                    );
                    String currentName = (String) session.getAttribute(
                            "updateName"
                    );

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

                        response.sendRedirect(
                                "./room-type?view=update&id="
                                + currentId
                                + "&error=missing-params"
                        );
                        return;
                    }

                    int id;

                    try {
                        id = Integer.parseInt(
                                request.getParameter("id").trim()
                        );
                    } catch (NumberFormatException e) {
                        response.sendRedirect(
                                "./room-type?view=update&id="
                                + currentId
                                + "&error=number-format"
                        );
                        return;
                    }

                    if (!rtDAO.doesIDExist(id)) {
                        response.sendRedirect(
                                "./room-type?view=update&id="
                                + currentId
                                + "&error=id-not-exist"
                        );
                        return;
                    }

                    if (id != currentId) {
                        response.sendRedirect(
                                "./room-type?view=update&id="
                                + currentId
                                + "&error=id-different"
                        );
                        return;
                    }

                    String name = request.getParameter("name").trim();

                    if (!name.equals(currentName)
                            && rtDAO.doesRoomTypeNameExist(name)) {
                        response.sendRedirect(
                                "./room-type?view=update&id="
                                + currentId
                                + "&error=name-exist"
                        );
                        return;
                    }

                    String description = request.getParameter(
                            "description"
                    ).trim();

                    BigDecimal pricePerNight;
                    int beds;
                    int capacity;

                    try {
                        pricePerNight = new BigDecimal(
                                request.getParameter("pricePerNight").trim()
                        );
                        beds = Integer.parseInt(
                                request.getParameter("beds").trim()
                        );
                        capacity = Integer.parseInt(
                                request.getParameter("capacity").trim()
                        );

                        if (pricePerNight.compareTo(BigDecimal.ZERO) < 0
                                || beds <= 0
                                || capacity <= 0) {
                            response.sendRedirect(
                                    "./room-type?view=update&id="
                                    + currentId
                                    + "&error=number-format"
                            );
                            return;
                        }
                    } catch (NumberFormatException e) {
                        response.sendRedirect(
                                "./room-type?view=update&id="
                                + currentId
                                + "&error=number-format"
                        );
                        return;
                    }

                    Part imgPart = request.getPart("img");
                    String picture;

                    if (imgPart.getSubmittedFileName().trim().isEmpty()
                            || imgPart.getSize() == 0) {
                        picture = (String) session.getAttribute(
                                "updateImgPath"
                        );
                    } else {
                        File folder = new File(
                                FileUploadUtil.UPLOAD_LOCATION + "/roomType"
                        );

                        if (!folder.exists()) {
                            folder.mkdir();
                        }

                        String imgFileName = imgPart.getSubmittedFileName();
                        File outFile = new File(folder, imgFileName);

                        FileUploadUtil.uploadFile(imgPart, outFile);
                        picture = "roomType/" + imgFileName;
                    }

                    rtDAO.updateRoomType(
                            id,
                            name,
                            description,
                            pricePerNight,
                            beds,
                            capacity,
                            picture
                    );
                    break;
                }

                default: {
                    break;
                }
            }

            response.sendRedirect("./room-type?page-index=1");

        } catch (Exception ex) {
            Logger.getLogger(DBContext.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "RoomTypeServlet handles room type management operations including CRUD.";
    }
}