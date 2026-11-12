package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import util.FileUploadUtil;

@WebServlet(name = "RoomTypeImageServlet", urlPatterns = {"/assets/img/roomType/*"})
public class RoomTypeImageServlet extends HttpServlet {

    private static final String ASSET_DIRECTORY = "/assets/img/roomType/";

    static Path uploadDirectory() {
        String configured = System.getProperty("hotel.roomTypeImageDirectory");
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath();
        }
        return Path.of(System.getProperty("user.home"), ".hotel", "room-type-images");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        String filename = path == null ? "" : path.substring(1);
        String contentType = imageContentType(filename);
        if (contentType == null || filename.startsWith(".") || filename.contains("/")
                || filename.contains("\\") || filename.contains(":")
                || filename.chars().anyMatch(Character::isISOControl)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try (InputStream image = openImage(filename)) {
            if (image != null) {
                response.setContentType(contentType);
                response.setHeader("X-Content-Type-Options", "nosniff");
                image.transferTo(response.getOutputStream());
                return;
            }
        }

        try (InputStream placeholder = getServletContext().getResourceAsStream("/assets/img/avatar.jpg")) {
            if (placeholder == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("image/jpeg");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "no-store");
            placeholder.transferTo(response.getOutputStream());
        }
    }

    private InputStream openImage(String filename) throws IOException {
        Path uploaded = uploadDirectory().resolve(filename);
        if (Files.isRegularFile(uploaded, LinkOption.NOFOLLOW_LINKS)) {
            return Files.newInputStream(uploaded, LinkOption.NOFOLLOW_LINKS);
        }

        InputStream bundled = getServletContext().getResourceAsStream(ASSET_DIRECTORY + filename);
        if (bundled != null) {
            return bundled;
        }

        // Older deployments wrote uploads to this source directory instead of the running webapp.
        Path legacy = Path.of(FileUploadUtil.UPLOAD_LOCATION, "roomType", filename);
        return Files.isRegularFile(legacy, LinkOption.NOFOLLOW_LINKS)
                ? Files.newInputStream(legacy, LinkOption.NOFOLLOW_LINKS) : null;
    }

    static String imageContentType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return lower.endsWith(".png") ? "image/png" : null;
    }
}
