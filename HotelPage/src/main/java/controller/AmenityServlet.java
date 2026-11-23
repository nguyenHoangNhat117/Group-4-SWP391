package controller;

import dao.AmenityDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.Amenity;
import model.User;
import util.AuthSupport;

@WebServlet(name = "AmenityServlet", urlPatterns = {"/amenity"})
public class AmenityServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        AuthSupport.token(request.getSession(false));
        try {
            AmenityDAO dao = new AmenityDAO();
            List<Amenity> amenities = dao.getAllAmenities();
            String edit = request.getParameter("edit");
            Amenity editing = null;
            if (edit != null) {
                int id = amenityId(edit);
                editing = amenities.stream().filter(amenity -> amenity.getAmenityID() == id).findFirst().orElse(null);
                if (editing == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
            show(request, response, dao, amenities, editing, null);
        } catch (SQLException ex) {
            throw new ServletException("Could not load amenities", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!requireAdmin(request, response) || !AuthSupport.checkCsrf(request, response)) {
            return;
        }
        String action = request.getParameter("action");
        int id = amenityId(AuthSupport.text(request.getParameter("id")));
        AmenityDAO dao = new AmenityDAO();
        try {
            if ("status".equals(action)) {
                String active = request.getParameter("active");
                if (id < 1 || !("true".equals(active) || "false".equals(active))) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else if (!dao.setActive(id, Boolean.parseBoolean(active))) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    redirectToList(request, response);
                }
                return;
            }
            if (!"save".equals(action) || id < 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String name = AuthSupport.text(request.getParameter("name"));
            String description = AuthSupport.text(request.getParameter("description"));
            Amenity form = new Amenity(id, name, description);
            if (name.isEmpty() || name.length() > 100 || description.length() > 255) {
                show(request, response, dao, dao.getAllAmenities(), form, "room.amenity.invalid");
                return;
            }
            AmenityDAO.SaveResult result = dao.save(form);
            if (result == AmenityDAO.SaveResult.SAVED) {
                redirectToList(request, response);
            } else if (result == AmenityDAO.SaveResult.NOT_FOUND) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                show(request, response, dao, dao.getAllAmenities(), form, "room.amenity.duplicate");
            }
        } catch (SQLException ex) {
            throw new ServletException("Could not save amenity", ex);
        }
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, AmenityDAO dao,
            List<Amenity> amenities, Amenity editing, String error) throws ServletException, IOException, SQLException {
        request.setAttribute("amenities", amenities);
        request.setAttribute("amenityTypes", dao.getRoomTypesByAmenity());
        request.setAttribute("editingAmenity", editing);
        request.setAttribute("amenityError", error);
        request.getRequestDispatcher("/WEB-INF/roomType/amenities.jsp").forward(request, response);
    }

    private static int amenityId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
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

    private static void redirectToList(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", request.getContextPath() + "/amenity?saved=1");
    }
}
