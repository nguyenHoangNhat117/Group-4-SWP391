package controller;

import dao.AmenityDAO;
import dao.RoomTypeDAO;
import dao.RoomTypeDAO.SaveResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomType;
import model.User;
import util.AuthSupport;

@WebServlet(name = "RoomTypeServlet", urlPatterns = {"/room-type"})
@MultipartConfig(fileSizeThreshold = 2 * 1024 * 1024, maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 50 * 1024 * 1024)
public class RoomTypeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RoomTypeServlet.class.getName());
    private static final List<String> FIELDS = List.of("name", "description", "pricePerNight", "beds", "capacity", "maxChildren");
    private static final BigDecimal MAX_NIGHTLY_PRICE = new BigDecimal("9999999999999999");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        AuthSupport.token(request.getSession(false));
        RoomTypeDAO dao = new RoomTypeDAO();
        String view = AuthSupport.text(request.getParameter("view"));
        try {
            if (view.isEmpty()) {
                showList(request, response, dao);
            } else if ("create".equals(view)) {
                showForm(request, response, null, Map.of("maxChildren", "0"), Set.of());
            } else if (List.of("update", "sale", "delete").contains(view)) {
                RoomType type = findType(request, response, dao);
                if (type == null) {
                    return;
                }
                if ("update".equals(view)) {
                    showForm(request, response, type, formValues(type), new AmenityDAO().getAssignedAmenityIds(type.getId()));
                } else {
                    request.setAttribute("roomType", type);
                    request.getRequestDispatcher("/WEB-INF/roomType/" + view + ".jsp").forward(request, response);
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            throw new ServletException("Could not load room type management", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!requireAdmin(request, response) || !AuthSupport.checkCsrf(request, response)) {
            return;
        }
        RoomTypeDAO dao = new RoomTypeDAO();
        String action = AuthSupport.text(request.getParameter("action"));
        try {
            switch (action) {
                case "create":
                    save(request, response, dao, null);
                    return;
                case "update": {
                    RoomType type = findType(request, response, dao);
                    if (type != null) {
                        save(request, response, dao, type);
                    }
                    return;
                }
                case "sale": {
                    int id = positiveInt(request.getParameter("id"));
                    String active = request.getParameter("active");
                    if (id == 0 || !("true".equals(active) || "false".equals(active))) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    } else if (!dao.setActive(id, Boolean.parseBoolean(active))) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        redirectToList(request, response, "&sale-updated=1");
                    }
                    return;
                }
                case "delete": {
                    int id = positiveInt(request.getParameter("id"));
                    if (id == 0) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                    RoomTypeDAO.DeleteResult result = dao.deleteRoomType(id);
                    String resultQuery;
                    switch (result) {
                        case DELETED: resultQuery = "&deleted=1"; break;
                        case IN_USE: resultQuery = "&error=in-use"; break;
                        default: resultQuery = "&error=delete-failed";
                    }
                    redirectToList(request, response, resultQuery);
                    return;
                }
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException ex) {
            throw new ServletException("Could not manage room type", ex);
        }
    }

    private static boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute("loggedUser");
        if (user instanceof User && "admin".equalsIgnoreCase(((User) user).getRole())) {
            return true;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    private static RoomType findType(HttpServletRequest request, HttpServletResponse response, RoomTypeDAO dao)
            throws IOException, SQLException {
        int id = positiveInt(request.getParameter("id"));
        if (id == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        RoomType type = dao.getAdminRoomType(id);
        if (type == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        return type;
    }

    private static void showList(HttpServletRequest request, HttpServletResponse response, RoomTypeDAO dao)
            throws ServletException, IOException, SQLException {
        int totalPages = dao.getTotalPages();
        int page = Math.max(1, Math.min(positiveInt(request.getParameter("page-index")), totalPages));
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageIndex", page);
        request.setAttribute("roomTypes", dao.getPage(page));
        request.getRequestDispatcher("/WEB-INF/roomType/roomType.jsp").forward(request, response);
    }

    private static void showForm(HttpServletRequest request, HttpServletResponse response, RoomType existing,
            Map<String, String> values, Set<Integer> selected)
            throws ServletException, IOException, SQLException {
        int id = existing == null ? 0 : existing.getId();
        request.setAttribute("roomType", existing);
        request.setAttribute("formValues", values);
        request.setAttribute("selectedAmenities", selected);
        request.setAttribute("allAmenities", new AmenityDAO().getSelectableAmenities(id));
        request.getRequestDispatcher(existing == null ? "/WEB-INF/roomType/create.jsp" : "/WEB-INF/roomType/update.jsp")
                .forward(request, response);
    }

    private static void save(HttpServletRequest request, HttpServletResponse response, RoomTypeDAO dao, RoomType existing)
            throws IOException, ServletException, SQLException {
        Map<String, String> values = new LinkedHashMap<>();
        for (String field : FIELDS) {
            values.put(field, AuthSupport.text(request.getParameter(field)));
        }

        Set<Integer> selected = new LinkedHashSet<>();
        String[] inputAmenities = request.getParameterValues("amenities");
        if (inputAmenities != null) {
            boolean valid = true;
            for (String value : inputAmenities) {
                int id = positiveInt(value);
                if (id == 0) {
                    valid = false;
                } else {
                    selected.add(id);
                }
            }
            if (!valid) {
                showError(request, response, existing, values, selected, "room.type.amenity.invalid");
                return;
            }
        }

        String name = values.get("name");
        String description = values.get("description");
        if (name.isEmpty() || name.length() > 50 || description.isEmpty() || description.length() > 500) {
            showError(request, response, existing, values, selected, "room.type.fields.invalid");
            return;
        }

        RoomType type;
        try {
            BigDecimal price = new BigDecimal(values.get("pricePerNight"));
            int beds = Integer.parseInt(values.get("beds"));
            int adults = Integer.parseInt(values.get("capacity"));
            int children = Integer.parseInt(values.get("maxChildren"));
            if (price.signum() < 0 || price.stripTrailingZeros().scale() > 0
                    || price.compareTo(MAX_NIGHTLY_PRICE) > 0 || beds < 1 || adults < 1 || children < 0) {
                throw new NumberFormatException();
            }
            type = new RoomType(existing == null ? 0 : existing.getId(), name, description, price, beds, adults);
            type.setMaxChildren(children);
        } catch (NumberFormatException ex) {
            showError(request, response, existing, values, selected, "room.type.numbers.invalid");
            return;
        }

        Part image;
        try {
            image = request.getPart("img");
        } catch (IllegalStateException ex) {
            showError(request, response, existing, values, selected, "room.type.image.size");
            return;
        }
        if (existing == null && (image == null || image.getSize() == 0)) {
            showError(request, response, existing, values, selected, "screen.de2e7a78d75b71fd");
            return;
        }
        if (image != null && image.getSize() > 0 && (image.getSubmittedFileName() == null
                || RoomTypeImageServlet.imageContentType(image.getSubmittedFileName()) == null)) {
            showError(request, response, existing, values, selected, "room.type.image.type");
            return;
        }

        SaveResult result;
        try {
            result = saveWithImage(dao, type, image, selected);
        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not save room type", ex);
            result = SaveResult.FAILED;
        }
        switch (result) {
            case SAVED:
                redirectToList(request, response, "&saved=1");
                return;
            case DUPLICATE_NAME:
                showError(request, response, existing, values, selected, "screen.9dda0b654299ce61");
                return;
            case INVALID_AMENITY:
                showError(request, response, existing, values, selected, "room.type.amenity.invalid");
                return;
            default:
                showError(request, response, existing, values, selected, "room.type.save.failed");
        }
    }

    private static void showError(HttpServletRequest request, HttpServletResponse response, RoomType existing,
            Map<String, String> values, Set<Integer> selected, String error)
            throws IOException, ServletException, SQLException {
        request.setAttribute("roomTypeError", error);
        showForm(request, response, existing, values, selected);
    }

    private static SaveResult saveWithImage(RoomTypeDAO dao, RoomType type, Part image, Set<Integer> selected)
            throws IOException, SQLException {
        if (image == null || image.getSize() == 0) {
            return dao.saveRoomType(type, selected);
        }
        String fileName = image.getSubmittedFileName();
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        Path directory = Files.createDirectories(RoomTypeImageServlet.uploadDirectory());
        Path uploaded = Files.createTempFile(directory, "room-type-", extension);
        SaveResult result = SaveResult.FAILED;
        try {
            try (InputStream contents = image.getInputStream()) {
                Files.copy(contents, uploaded, StandardCopyOption.REPLACE_EXISTING);
            }
            type.setPicture("roomType/" + uploaded.getFileName());
            result = dao.saveRoomType(type, selected);
            return result;
        } finally {
            if (result != SaveResult.SAVED) {
                try {
                    Files.deleteIfExists(uploaded);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not remove unused room type image", ex);
                }
            }
        }
    }

    private static Map<String, String> formValues(RoomType type) {
        return Map.of("name", type.getName(), "description", AuthSupport.text(type.getDescription()),
                "pricePerNight", type.getPricePerNight().stripTrailingZeros().toPlainString(),
                "beds", String.valueOf(type.getBeds()), "capacity", String.valueOf(type.getCapacity()),
                "maxChildren", String.valueOf(type.getMaxChildren()));
    }

    private static int positiveInt(String input) {
        try {
            return Math.max(0, Integer.parseInt(input));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static void redirectToList(HttpServletRequest request, HttpServletResponse response, String result) {
        int page = Math.max(1, positiveInt(request.getParameter("page-index")));
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", request.getContextPath() + "/room-type?page-index=" + page + result);
    }
}
