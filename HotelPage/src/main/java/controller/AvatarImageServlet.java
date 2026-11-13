
package controller;

import dao.CustomerDAO;
import model.Customer;
import model.User;

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

        if (customer == null
                || customer.getAvatar() == null
                || customer.getAvatar().isBlank()) {
            response.sendError(404);
            return;
        }

        // Read filename from the authenticated user's database record.
        String filename = customer.getAvatar();

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
