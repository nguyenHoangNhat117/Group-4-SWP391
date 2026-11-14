package controller;

import dao.CustomerDAO;
import model.Customer;
import model.User;
import dao.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/avatar/image")
public class AvatarImageServlet extends HttpServlet {

    
@Override
protected void doGet(HttpServletRequest request,
                     HttpServletResponse response)
        throws ServletException, IOException {

    String filename;
    String reviewIdParam = request.getParameter("reviewId");

    if (reviewIdParam != null) {
        // Public avatar for approved reviews only
        int reviewId;

        try {
            reviewId = Integer.parseInt(reviewIdParam);
            if (reviewId <= 0) {
                response.sendError(400);
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(400);
            return;
        }

        try {
            ReviewDAO reviewDAO = new ReviewDAO();
            filename = reviewDAO.getApprovedReviewAvatar(reviewId);
        } catch (IllegalStateException e) {
            getServletContext().log(
                "Unable to load review avatar", e);
            response.sendError(503);
            return;
        }

    } else {
        // Existing private profile avatar
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendError(401);
            return;
        }

        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            response.sendError(401);
            return;
        }

        CustomerDAO dao = new CustomerDAO();
        Customer customer = dao.getCustomerByUserID(user.getId());

        if (customer == null) {
            response.sendError(404);
            return;
        }

        filename = customer.getAvatar();
    }

    if (filename == null || filename.isBlank()) {
        response.sendError(404);
        return;
    }

    if (filename.startsWith("avatar/")) {
        filename = filename.substring("avatar/".length());
    }

    // Prevent unsafe file paths
    if (!filename.matches(
            "[A-Za-z0-9_-]+\\.(?i:jpg|jpeg|png)")) {
        response.sendError(400, "Invalid avatar filename");
        return;
    }

    String storagePath = getServletContext()
            .getInitParameter("avatarStoragePath");

    if (storagePath == null || storagePath.isBlank()) {
        response.sendError(500, "Avatar storage not configured");
        return;
    }

    Path folder = Paths.get(storagePath)
            .toAbsolutePath().normalize();

    Path image = folder.resolve(filename).normalize();

    if (!image.startsWith(folder)
            || !Files.isRegularFile(image)
            || Files.isSymbolicLink(image)) {
        response.sendError(404, "Avatar image not found");
        return;
    }

    String contentType = Files.probeContentType(image);

    if (!"image/jpeg".equals(contentType)
            && !"image/png".equals(contentType)) {
        response.sendError(415, "Unsupported image type");
        return;
    }

    response.setContentType(contentType);
    response.setHeader("Cache-Control", "no-store");
    response.setContentLengthLong(Files.size(image));

    Files.copy(image, response.getOutputStream());
}
}
