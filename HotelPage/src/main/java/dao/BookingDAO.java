/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Booking;
import model.Customer;
import model.User;

/**
 *
 * @author Đặng Hoàng Vũ
 */
public class BookingDAO extends DBContext {

    public List<Booking> getAll() {
        List<Booking> bookings = new ArrayList<>();
        try {
            String query = "SELECT BookingID, RoomNumber, CheckInDate, CheckOutDate, TotalPrice, "
                    + "b.CustomerID, FirstName, LastName, Email, Phone, "
                    + "Country, Street, City, Avatar, c.UserID, Username, Password, Role "
                    + "FROM Booking b "
                    + "JOIN Customer c ON b.CustomerID = c.CustomerID "
                    + "JOIN [User] u ON c.UserID = u.UserID "
                    + "ORDER BY BookingID DESC, CheckInDate DESC;";

            PreparedStatement pstatement = this.getConnection().prepareStatement(query);
            ResultSet rs = pstatement.executeQuery();

            while (rs.next()) {
                int bookingId = rs.getInt("BookingID");
                int roomNumber = rs.getInt("RoomNumber");
                LocalDate checkInDate = rs.getObject("CheckInDate", LocalDate.class);
                LocalDate checkOutDate = rs.getObject("CheckOutDate", LocalDate.class);
                BigDecimal totalPrice = rs.getBigDecimal("TotalPrice");

                int customerId = rs.getInt("CustomerID");
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                String email = rs.getString("Email");
                String phone = rs.getString("Phone");

                String country = rs.getString("Country");
                String street = rs.getString("Street");
                String city = rs.getString("City");
                String avatar = rs.getString("Avatar");

                int userId = rs.getInt("UserID");
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                String role = rs.getString("Role");

                // Optional: create a User object if needed
                User user = new User(userId, username, password, role);

                Customer customer = new Customer(customerId, firstName, lastName, email, phone, country, city, street, avatar, user);
                Booking booking = new Booking(bookingId, customer, roomNumber, checkInDate, checkOutDate, totalPrice);

                bookings.add(booking);
            }

        } catch (SQLException ex) {
            Logger.getLogger(BookingDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bookings;
    }

    public boolean create(int customerId, int roomNumber, LocalDate checkInDate, LocalDate checkOutDate, BigDecimal totalPrice) {
        String query = "INSERT INTO Booking (CustomerID, RoomNumber, CheckInDate, CheckOutDate, TotalPrice) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            int rows = this.executeQuery(query, new Object[]{
                customerId,
                roomNumber,
                checkInDate,
                checkOutDate,
                totalPrice
            });

            return rows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(BookingDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public Integer getLatestCompletedBookingId(int customerId, int roomNumber) {
        String sql = "SELECT TOP 1 b.BookingID "
                + "FROM Booking b "
                + "JOIN Payment p ON b.BookingID = p.BookingID "
                + "WHERE b.CustomerID = ? AND b.RoomNumber = ? AND b.CheckOutDate < GETDATE() "
                + "ORDER BY b.CheckOutDate DESC";

        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{customerId, roomNumber});
            if (rs.next()) {
                return rs.getInt("BookingID");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // or log
        }

        return null;
    }

    public List<Booking> getBookingsByCustomerID(int customerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM Booking WHERE CustomerID = ? ORDER BY BookingID DESC, CheckOutDate DESC";
        try {
            PreparedStatement ps = this.getConnection().prepareStatement(sql);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("BookingID"));
                booking.setRoomNumber(rs.getInt("RoomNumber"));
                booking.setCheckInDate(rs.getDate("CheckInDate").toLocalDate());
                booking.setCheckOutDate(rs.getDate("CheckOutDate").toLocalDate());
                booking.setTotalPrice(rs.getBigDecimal("TotalPrice"));
                list.add(booking);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isRoomBookedInRange(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        String sql = "SELECT 1 FROM Booking "
                + "WHERE RoomNumber = ? "
                + "AND NOT (CheckOutDate <= ? OR CheckInDate >= ?)";

        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{roomNumber, checkIn, checkOut});
            return rs.next(); // true if a conflict exists
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // no conflict found or error occurred
    }

    public boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM Booking WHERE BookingID = ?";

        try {
            int affectedRows = executeQuery(sql, new Object[]{bookingId});
            return affectedRows > 0; // true if deletion was successful
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int deleteUnpaidOverdueBookings() {
        String sql = "DELETE FROM Booking "
                + "WHERE CheckInDate < CAST(GETDATE() AS DATE) "
                + "AND BookingID NOT IN (SELECT BookingID FROM Payment)";

        try {
            return executeQuery(sql, null); // Inherited from DBContext
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM Booking WHERE BookingID = ?";
        try {
            PreparedStatement ps = this.getConnection().prepareStatement(sql);
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("BookingID"));
                booking.setRoomNumber(rs.getInt("RoomNumber"));
                booking.setCheckInDate(rs.getDate("CheckInDate").toLocalDate());
                booking.setCheckOutDate(rs.getDate("CheckOutDate").toLocalDate());
                booking.setTotalPrice(rs.getBigDecimal("TotalPrice"));
                return booking;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateBookingTotalPriceWithDiscount(int bookingID, BigDecimal discountPercent) {
        String sql = " UPDATE Booking SET TotalPrice = TotalPrice - (TotalPrice * ?  / 100) WHERE BookingID =  ?";

        try {
            int rows = executeQuery(sql, new Object[]{discountPercent, bookingID});
            return rows > 0; // true if update succeeded
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int countBookingsByCustomerID(int customerId) {
        String sql = "SELECT COUNT(*) AS total FROM Booking WHERE CustomerID = ?";
        try {
            PreparedStatement ps = this.getConnection().prepareStatement(sql);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Booking> getBookingsByCustomerID(int customerId, int page, int pageSize) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM Booking WHERE CustomerID = ? "
                + "ORDER BY BookingID DESC, CheckOutDate DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try {
            PreparedStatement ps = this.getConnection().prepareStatement(sql);
            ps.setInt(1, customerId);
            ps.setInt(2, (page - 1) * pageSize);
            ps.setInt(3, pageSize);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("BookingID"));
                booking.setRoomNumber(rs.getInt("RoomNumber"));
                booking.setCheckInDate(rs.getDate("CheckInDate").toLocalDate());
                booking.setCheckOutDate(rs.getDate("CheckOutDate").toLocalDate());
                booking.setTotalPrice(rs.getBigDecimal("TotalPrice"));
                list.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
