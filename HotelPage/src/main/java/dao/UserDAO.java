package dao;

import db.DBContext;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Customer;
import model.User;

/**
 * Data Access Object for user-related operations, including login, registration,
 * and password management.
 * 
 * @Author: Diệp Vỹ Khang
 */
public class UserDAO extends DBContext {

    /**
     * Retrieves the username based on a given user ID.
     * 
     * @param id User ID
     * @return Username as String, or null if not found
     */
    public String getUsernameById(int id) {
        try (Connection conn = new DBContext().getConnection()) {
            String sql = "SELECT Username FROM [User] WHERE UserID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("Username");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Attempts to log in a user by checking username and hashed password.
     * 
     * @param name Username
     * @param password Raw password (will be hashed internally)
     * @return User object if successful login, null otherwise
     */
    public User login(String name, String password) {
        try {
            String hashPwd = hashMd5(password); // Hash the password before checking

            String query = "SELECT UserID, Username, Password, Role, DisplayName "
                         + "FROM [User] WHERE Username = ? AND Password = ?";
            ResultSet rs = this.executeSelectionQuery(query, new Object[]{name, hashPwd});

            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt(1));
                u.setUsername(rs.getString(2));
                u.setPassword(rs.getString(3));
                u.setRole(rs.getString(4));
                u.setDisplayName(rs.getString(5));
                return u;
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Hashes a given string using MD5.
     * 
     * @param raw Raw string to hash
     * @return MD5 hashed string
     */
    private String hashMd5(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] mess = md.digest(raw.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : mess) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    /**
     * Registers a new user and creates a customer record linked to that user.
     * 
     * @param username Username
     * @param password Password (will be hashed)
     * @param email Email address
     * @return true if registration successful, false otherwise
     */
    public boolean register(String username, String password, String email) {
        try {
            String hashedPwd = hashMd5(password);

            // Insert into User table
            String userQuery = "INSERT INTO [User] (Username, Password, Role, DisplayName) VALUES (?, ?, ?, ?)";
            int result = this.executeQuery(userQuery, new Object[]{username, hashedPwd, "customer", username});

            if (result > 0) {
                // Retrieve the newly inserted UserID
                String getIdQuery = "SELECT TOP 1 UserID FROM [User] WHERE Username = ? ORDER BY UserID DESC";
                ResultSet rs = this.executeSelectionQuery(getIdQuery, new Object[]{username});
                if (rs.next()) {
                    int userId = rs.getInt("UserID");

                    // Insert into Customer table
                    String customerQuery = "INSERT INTO Customer (UserID, Email) VALUES (?, ?)";
                    int inserted = this.executeQuery(customerQuery, new Object[]{userId, email});
                    return inserted > 0;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Checks whether an email already exists in the Customer table.
     * 
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    public boolean checkEmailExists(String email) {
        String sql = "SELECT * FROM Customer WHERE Email = ?";
        try {
            ResultSet rs = this.executeSelectionQuery(sql, new Object[]{email});
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the password (already hashed) for a user based on their email.
     * 
     * @param email Email address
     * @param newPasswordHash New hashed password
     * @return number of affected rows (1 if successful, 0 if not)
     */
    public int updatePasswordByEmail(String email, String newPasswordHash) {
        String sql = "UPDATE [User] SET Password = ? "
                   + "WHERE UserID = (SELECT UserID FROM Customer WHERE Email = ?)";
        try {
            return this.executeQuery(sql, new Object[]{newPasswordHash, email});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Checks whether a username already exists in the database.
     * 
     * @param username Username to check
     * @return true if exists, false otherwise
     */
    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM [User] WHERE Username = ?";
        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{username});
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
