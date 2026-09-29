package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import model.Booking;
import model.Customer;
import model.Review;
import model.User;

/**
 * DAO (Data Access Object) class for handling Review-related database operations.
 * Author: Diệp vỹ Khang
 */
public class ReviewDAO extends DBContext {

    /**
     * Adds a new review to the database.
     *
     * @param bookingId the ID of the booking being reviewed
     * @param comment   the review comment
     * @param star      the star rating (as BigDecimal)
     */
    public void addReview(int bookingId, String comment, BigDecimal star) {
        String sql = "INSERT INTO Review (BookingID, Comment, Star) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setInt(1, bookingId);
            ps.setString(2, comment);
            ps.setBigDecimal(3, star);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of reviews for a specific room number, including full customer and booking details.
     *
     * @param roomNumber the room number to filter reviews
     * @return list of Review objects
     */
    public List<Review> getReviewsByRoomNumber(int roomNumber) {
        List<Review> list = new ArrayList<>();

        String sql = "SELECT r.ReviewID, r.Comment, r.Star, "
                   + "b.BookingID, b.RoomNumber, b.CheckInDate, b.CheckOutDate, b.TotalPrice, "
                   + "c.CustomerID, c.FirstName, c.LastName, c.Email, c.Phone, "
                   + "c.Country, c.City, c.Street, c.Avatar, c.UserID, "
                   + "u.Username, u.Password, u.Role "
                   + "FROM Review r "
                   + "JOIN Booking b ON r.BookingID = b.BookingID "
                   + "JOIN Customer c ON b.CustomerID = c.CustomerID "
                   + "JOIN [User] u ON c.UserID = u.UserID "
                   + "WHERE b.RoomNumber = ? "
                   + "ORDER BY r.ReviewID DESC";

        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setInt(1, roomNumber);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Map user info
                User user = new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role")
                );

                // Map customer info
                Customer customer = new Customer(
                        rs.getInt("CustomerID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        rs.getString("Country"),
                        rs.getString("City"),
                        rs.getString("Street"),
                        rs.getString("Avatar"),
                        user
                );

                // Map booking info
                Booking booking = new Booking(
                        rs.getInt("BookingID"),
                        customer,
                        rs.getInt("RoomNumber"),
                        rs.getObject("CheckInDate", LocalDate.class),
                        rs.getObject("CheckOutDate", LocalDate.class),
                        rs.getBigDecimal("TotalPrice")
                );

                // Map review info
                Review review = new Review(
                        rs.getInt("ReviewID"),
                        booking,
                        rs.getString("Comment"),
                        rs.getBigDecimal("Star")
                );

                list.add(review);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Checks whether a review already exists for a given booking.
     *
     * @param bookingId the ID of the booking to check
     * @return true if a review exists, false otherwise
     */
    public boolean isBookingReviewed(int bookingId) {
        String sql = "SELECT 1 FROM Review WHERE BookingID = ?";

        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{bookingId});
            return rs.next(); // true if a result is found
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Calculates the average star rating for a given room number.
     *
     * @param roomNumber the room number to calculate rating for
     * @return the average star rating, rounded to 1 decimal
     */
    public BigDecimal getAverageStarByRoomNumber(int roomNumber) {
        String sql = "SELECT AVG(r.Star) AS AvgStar "
                   + "FROM Review r "
                   + "JOIN Booking b ON r.BookingID = b.BookingID "
                   + "WHERE b.RoomNumber = ?";

        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{roomNumber});
            if (rs.next()) {
                BigDecimal avg = rs.getBigDecimal("AvgStar");
                return avg.setScale(1, RoundingMode.HALF_UP); // Round to 1 decimal
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO.setScale(1);
    }

    /**
     * Deletes a review by its ID.
     *
     * @param reviewID the ID of the review to delete
     */
    public void deleteReview(int reviewID) {
        String sql = "DELETE FROM Review WHERE ReviewID = ?";
        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setInt(1, reviewID);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all reviews in the system along with full details of booking, customer, and user.
     *
     * @return list of all Review objects
     */
    public List<Review> getAllReviews() {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.ReviewID, r.Comment, r.Star, "
                   + "b.BookingID, b.RoomNumber, b.CheckInDate, b.CheckOutDate, b.TotalPrice, "
                   + "c.CustomerID, c.FirstName, c.LastName, c.Email, c.Phone, "
                   + "c.Country, c.City, c.Street, c.Avatar, c.UserID, "
                   + "u.Username, u.Password, u.Role "
                   + "FROM Review r "
                   + "JOIN Booking b ON r.BookingID = b.BookingID "
                   + "JOIN Customer c ON b.CustomerID = c.CustomerID "
                   + "JOIN [User] u ON c.UserID = u.UserID";

        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Map user info
                User user = new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role")
                );

                // Map customer info
                Customer customer = new Customer(
                        rs.getInt("CustomerID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        rs.getString("Country"),
                        rs.getString("City"),
                        rs.getString("Street"),
                        rs.getString("Avatar"),
                        user
                );

                // Map booking info
                Booking booking = new Booking(
                        rs.getInt("BookingID"),
                        customer,
                        rs.getInt("RoomNumber"),
                        rs.getObject("CheckInDate", LocalDate.class),
                        rs.getObject("CheckOutDate", LocalDate.class),
                        rs.getBigDecimal("TotalPrice")
                );

                // Map review info
                Review review = new Review(
                        rs.getInt("ReviewID"),
                        booking,
                        rs.getString("Comment"),
                        rs.getBigDecimal("Star")
                );

                list.add(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
