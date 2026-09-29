/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.CustomerDAO;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;

import model.Customer;
import model.User;
import util.FileUploadUtil;

/**
 *
 * @author Tran Nguyen
 */
@WebServlet(name = "AvatarServlet", urlPatterns = {"/avatar"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // Files larger than 1MB will be written to disk
    maxFileSize = 1024 * 1024 * 5,   // Maximum file size allowed is 5MB
    maxRequestSize = 1024 * 1024 * 10 // Maximum total request size is 10MB
)
public class AvatarServlet extends HttpServlet {

    /**
     * This method is auto-generated and displays basic HTML for testing.
     * Not used in production.
     *
     * @param request the HttpServletRequest object containing the request
     * @param response the HttpServletResponse object for sending the response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");

        // Output sample HTML content
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AvatarServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AvatarServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles HTTP GET requests.
     * Currently not implemented.
     *
     * @param request the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // This servlet does not handle GET requests for avatar uploads
    }

    /**
     * Handles HTTP POST requests for avatar image uploading.
     * It saves the uploaded file, updates the user's avatar path in the database,
     * and redirects to the profile page.
     *
     * @param request the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ensure the request uses UTF-8 encoding to support special characters
        request.setCharacterEncoding("UTF-8");

        // Get the currently logged-in user from the session
        User user = (User) request.getSession().getAttribute("loggedUser");

        // If no user is logged in, redirect to the login page
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        // Get the uploaded image file from the form field named "img"
        Part imgPart = request.getPart("img");

        // Define the folder path where avatars will be stored
        File folder = new File(FileUploadUtil.UPLOAD_LOCATION + "/avatar");

        // Create the folder if it doesn't exist
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Get the original filename of the uploaded image
        String imgFileName = imgPart.getSubmittedFileName();

        // Check if the uploaded file has a name (i.e., is not empty)
        if (imgFileName != null && imgFileName.length() > 0) {
            // Create the output file object representing the saved image
            File outFile = new File(folder, imgFileName);

            // Use the utility method to upload the image to the output file
            FileUploadUtil.uploadFile(imgPart, outFile);
        }

        // Construct the relative path of the uploaded avatar image
        String avatarRelativePath = "avatar/" + imgFileName;

        // Create an instance of CustomerDAO to update the user's avatar info in DB
        CustomerDAO dao = new CustomerDAO();

        // Retrieve the Customer object associated with the logged-in user
        Customer customer = dao.getCustomerByUserID(user.getId());

        // If the customer is found in the database, update their avatar path
        if (customer != null) {
            customer.setAvatar(avatarRelativePath); // Set the new avatar path
            dao.updateCustomerWithAvatar(customer); // Save the change in the DB
        }

        // Redirect the user back to their profile page
        response.sendRedirect("profile");
    }

    /**
     * Provides a short description of the servlet.
     *
     * @return a brief description of this servlet
     */
    @Override
    public String getServletInfo() {
        return "Servlet responsible for uploading and saving user avatar images.";
    }

}
