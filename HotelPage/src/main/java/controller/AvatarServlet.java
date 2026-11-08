/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;


import dao.CustomerDAO;
import model.Customer;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@WebServlet("/avatar")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class AvatarServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute("loggedUser");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        CustomerDAO dao = new CustomerDAO();
        Customer customer = dao.getCustomerByUserID(user.getId());

        if (customer == null) {
            response.sendError(403, "Customer not found");
            return;
        }

        Part file = request.getPart("img");

        if (file == null || file.getSize() == 0) {
            response.sendError(400, "Please select an image");
            return;
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            response.sendError(400, "Image must be under 5MB");
            return;
        }

        String type = file.getContentType();
        String extension;

        if ("image/jpeg".equals(type)) {
            extension = ".jpg";
        } else if ("image/png".equals(type)) {
            extension = ".png";
        } else {
            response.sendError(400, "Only JPG and PNG allowed");
            return;
        }

        // Configure this folder outside the deployed WAR.
        String storagePath =
                getServletContext().getInitParameter("avatarStoragePath");

        if (storagePath == null || storagePath.isBlank()) {
            response.sendError(500, "Avatar storage not configured");
            return;
        }

        Path folder = Paths.get(storagePath);
        Files.createDirectories(folder);

        String filename = UUID.randomUUID() + extension;
        Path target = folder.resolve(filename);

        try {
            // Check file signature, not only Content-Type.
            try (InputStream input = file.getInputStream()) {
                byte[] signature = input.readNBytes(8);

                boolean jpeg = extension.equals(".jpg")
                        && signature.length >= 3
                        && (signature[0] & 0xff) == 0xff
                        && (signature[1] & 0xff) == 0xd8
                        && (signature[2] & 0xff) == 0xff;

                boolean png = extension.equals(".png")
                        && signature.length == 8
                        && java.util.Arrays.equals(
                            signature,
                            new byte[]{
                                (byte)137, 80, 78, 71,
                                13, 10, 26, 10
                            });

                if (!jpeg && !png) {
                    response.sendError(400, "Invalid image file");
                    return;
                }
            }

            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }

            // The database stores a filename, not a local disk path.
            if (!dao.updateAvatar(customer.getCustomerID(), filename)) {
                Files.deleteIfExists(target);
                response.sendError(500, "Avatar update failed");
                return;
            }

        } catch (Exception e) {
            Files.deleteIfExists(target);
            getServletContext().log("Avatar upload failed", e);
            response.sendError(500, "Cannot upload avatar");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }
}

