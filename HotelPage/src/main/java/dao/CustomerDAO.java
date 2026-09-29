/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.DBContext;
import model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 * CustomerDAO is responsible for handling database operations related to the Customer entity.
 * It includes methods for retrieving, updating, and managing customer records in the database.
 * 
 * This class extends DBContext, which provides database connection utilities.
 * 
 * Author: Trần Nguyễn
 */
public class CustomerDAO extends DBContext {

    /**
     * Retrieves a list of all customers from the database, including their associated user accounts.
     * 
     * @return a list of Customer objects
     */
    public List<Customer> getAll() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = new DBContext().getConnection()) {
            String sql = "SELECT CustomerID, FirstName, LastName, Email, Phone, Country, Street, City, Avatar, "
                       + "c.UserID, Username, Password, Role "
                       + "FROM Customer c JOIN [User] u ON c.UserID = u.UserID;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // Process each result row and build a Customer object
            while (rs.next()) {
                Customer c = new Customer();
                c.setCustomerID(rs.getInt("CustomerID"));
                c.setFirstName(rs.getString("FirstName"));
                c.setLastName(rs.getString("LastName"));
                c.setEmail(rs.getString("Email"));
                c.setPhone(rs.getString("Phone"));
                c.setCountry(rs.getString("Country"));
                c.setCity(rs.getString("City"));
                c.setStreet(rs.getString("Street"));
                c.setAvatar(rs.getString("Avatar"));

                // Create associated User object
                int userId = rs.getInt("UserID");
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                String role = rs.getString("Role"); // fixed from original code

                c.setUser(new User(userId, username, password, role));
                customers.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return customers;
    }

    /**
     * Retrieves a single customer based on the provided user ID.
     * This method also includes associated user account information.
     * 
     * @param userID the ID of the user linked to the customer
     * @return a Customer object if found, or null if no match exists
     */
    public Customer getCustomerByUserID(int userID) {
        try (Connection conn = new DBContext().getConnection()) {
            String sql = "SELECT CustomerID, FirstName, LastName, Email, Phone, Country, Street, City, Avatar, "
                       + "c.UserID, Username, Password, Role "
                       + "FROM Customer c JOIN [User] u ON c.UserID = u.UserID "
                       + "WHERE c.UserID = ?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Customer c = new Customer();
                c.setCustomerID(rs.getInt("CustomerID"));
                c.setFirstName(rs.getString("FirstName"));
                c.setLastName(rs.getString("LastName"));
                c.setEmail(rs.getString("Email"));
                c.setPhone(rs.getString("Phone"));
                c.setCountry(rs.getString("Country"));
                c.setCity(rs.getString("City"));
                c.setStreet(rs.getString("Street"));
                c.setAvatar(rs.getString("Avatar"));

                int userId = rs.getInt("UserID");
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                String role = rs.getString("Role"); // fixed from original code

                c.setUser(new User(userId, username, password, role));
                return c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Updates customer information in the database, excluding the avatar image.
     * 
     * @param userId the ID of the user linked to the customer
     * @param firstName updated first name
     * @param lastName updated last name
     * @param email updated email
     * @param phone updated phone number
     * @param country updated country
     * @param city updated city
     * @param street updated street address
     */
    public void updateCustomer(int userId, String firstName, String lastName, String email,
                               String phone, String country, String city, String street) {
        String sql = "UPDATE Customer SET FirstName=?, LastName=?, Email=?, Phone=?, Country=?, City=?, Street=? WHERE UserID=?";
        Object[] params = {firstName, lastName, email, phone, country, city, street, userId};

        try {
            executeQuery(sql, params); // executeQuery is inherited from DBContext
        } catch (SQLException e) {
            e.printStackTrace(); // consider logging this
        }
    }

    /**
     * Updates customer information including avatar image.
     * 
     * @param c a Customer object containing updated data
     */
    public void updateCustomerWithAvatar(Customer c) {
        try (Connection conn = new DBContext().getConnection()) {
            String sql = "UPDATE Customer SET FirstName=?, LastName=?, Email=?, Phone=?, Country=?, City=?, Street=?, Avatar=? WHERE UserID=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getCountry());
            ps.setString(6, c.getCity());
            ps.setString(7, c.getStreet());
            ps.setString(8, c.getAvatar());
            ps.setInt(9, c.getUser().getId());

            ps.executeUpdate(); // execute the update statement
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
