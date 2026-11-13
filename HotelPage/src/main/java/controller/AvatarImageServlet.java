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

        if (reviewIdParam != null && !reviewIdParam.isBlank()) {
            // Avatar người viết review: công khai, nhưng chỉ khi review đã approved
            int reviewId;
            try {
                reviewId = Integer.parseInt(reviewIdParam.trim());
            } catch (NumberFormatException e) {
                response.sendError(400);
                return;
            }
            filename = new ReviewDAO().getApprovedReviewAvatar(reviewId);
        } else {
            // Avatar của chính người đang đăng nhập (header, profile)
            HttpSession session = request.getSession(false);
            User user = session == null ? null : (User) session.getAttribute("loggedUser");
            if (user == null) {
                response.sendError(401);
                return;
            }
            Customer customer = new CustomerDAO().getCustomerByUserID(user.getId());
            filename = customer == null ? null : customer.getAvatar();
        }

        if (filename == null || filename.isBlank()) {
            response.sendError(404);
            return;
        }
        

        // Supports the previous "avatar/filename" format.
        if (filename.startsWith("avatar/")) {
            filename = filename.substring("avatar/".length());
        }

        if (!filename.matches("[A-Za-z0-9_-]+\\.(jpg|jpeg|png)")) {
            response.sendError(400, "Invalid avatar filename");
            return;
        }

        String storagePath = getServletContext()
                .getInitParameter("avatarStoragePath");

        if (storagePath == null || storagePath.isBlank()) {
            response.sendError(500, "Avatar storage not configured");
            return;
        }

        Path folder = Paths.get(storagePath).toAbsolutePath().normalize();
        Path image = folder.resolve(filename).normalize();

        if (!image.startsWith(folder)
                || !Files.isRegularFile(image)) {
            response.sendError(404, "Avatar image not found");
            return;
        }

        String contentType = filename.toLowerCase().endsWith(".png")
        ? "image/png" : "image/jpeg";

        response.setContentType(contentType);
        response.setHeader("Cache-Control", "no-store");
        response.setContentLengthLong(Files.size(image));

        Files.copy(image, response.getOutputStream());
    }
}
