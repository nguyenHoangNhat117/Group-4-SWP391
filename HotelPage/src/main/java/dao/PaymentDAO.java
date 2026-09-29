/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Booking;
import model.Customer;
import model.Payment;

/**
 *
 * @author Hà Thái Phát
 */
public class PaymentDAO extends DBContext {

    public void insertPayment(Payment payment) {
        String sql = "INSERT INTO Payment (BookingID, PaymentDate, DiscountID) VALUES (?, ?, ?)";

        try ( PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, payment.getBooking().getId());
            ps.setDate(2, Date.valueOf(payment.getPaymentDate()));

            if (payment.getDiscount() != null) {
                ps.setInt(3, payment.getDiscount().getId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBookingPaid(int bookingId) {
        String sql = "SELECT 1 FROM Payment WHERE BookingID = ?";
        try ( PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // Có bản ghi → đã thanh toán
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Payment> getPaymentsByCustomerId(int customerId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.PaymentID, p.BookingID, p.PaymentDate, p.DiscountID, "
                + "b.RoomNumber, b.CheckInDate, b.CheckOutDate, b.TotalPrice "
                + "FROM Payment p JOIN Booking b ON p.BookingID = b.BookingID "
                + "WHERE b.CustomerID = ? ORDER BY p.PaymentDate DESC, p.BookingID DESC";
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

                Payment payment = new Payment();
                payment.setId(rs.getInt("PaymentID"));
                payment.setBooking(booking);
                payment.setPaymentDate(rs.getDate("PaymentDate").toLocalDate());
                DiscountDAO dDAO = new DiscountDAO();
                payment.setDiscount(dDAO.getDiscountById(rs.getInt("DiscountID")));

                list.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.PaymentID, p.BookingID, p.PaymentDate, p.DiscountID, "
                + "b.RoomNumber, b.CheckInDate, b.CheckOutDate, b.TotalPrice, "
                + "c.CustomerID, c.FirstName, c.LastName "
                + "FROM Payment p "
                + "JOIN Booking b ON p.BookingID = b.BookingID "
                + "JOIN Customer c ON b.CustomerID = c.CustomerID "
                + "ORDER BY p.PaymentDate DESC, p.bookingID DESC";
        try {
            PreparedStatement ps = this.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("BookingID"));
                booking.setRoomNumber(rs.getInt("RoomNumber"));
                booking.setCheckInDate(rs.getDate("CheckInDate").toLocalDate());
                booking.setCheckOutDate(rs.getDate("CheckOutDate").toLocalDate());
                booking.setTotalPrice(rs.getBigDecimal("TotalPrice"));

                Customer customer = new Customer();
                customer.setCustomerID(rs.getInt("CustomerID"));
                customer.setFirstName(rs.getString("FirstName"));
                customer.setLastName(rs.getString("LastName"));
                booking.setCustomer(customer);

                Payment payment = new Payment();
                payment.setId(rs.getInt("PaymentID"));
                payment.setBooking(booking);
                payment.setPaymentDate(rs.getDate("PaymentDate").toLocalDate());
                DiscountDAO dDAO = new DiscountDAO();
                if (rs.getObject("DiscountID") != null) {
                    payment.setDiscount(dDAO.getDiscountById((Integer) rs.getObject("DiscountID")));
                } else {
                    payment.setDiscount(null);
                }

                list.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countPaymentsByCustomerId(int customerId) {
        String sql = "SELECT COUNT(*) AS total FROM Payment p JOIN Booking b ON p.BookingID = b.BookingID WHERE b.CustomerID = ?";
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

    public List<Payment> getPaymentsByCustomerId(int customerId, int page, int pageSize) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.PaymentID, p.BookingID, p.PaymentDate, p.DiscountID, "
                + "b.RoomNumber, b.CheckInDate, b.CheckOutDate, b.TotalPrice "
                + "FROM Payment p JOIN Booking b ON p.BookingID = b.BookingID "
                + "WHERE b.CustomerID = ? "
                + "ORDER BY p.PaymentDate DESC, p.BookingID DESC "
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

                Payment payment = new Payment();
                payment.setId(rs.getInt("PaymentID"));
                payment.setBooking(booking);
                payment.setPaymentDate(rs.getDate("PaymentDate").toLocalDate());
                DiscountDAO dDAO = new DiscountDAO();
                payment.setDiscount(dDAO.getDiscountById(rs.getInt("DiscountID")));

                list.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
