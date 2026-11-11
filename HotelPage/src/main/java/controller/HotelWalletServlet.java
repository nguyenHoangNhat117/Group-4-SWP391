package controller;

import db.DBContext;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@WebServlet("/hotel-wallet")
public class HotelWalletServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute("loggedUser");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        String role = user.getRole();

        if (!"admin".equalsIgnoreCase(role)
                && !"staff".equalsIgnoreCase(role)) {
            response.sendError(403, "Access denied");
            return;
        }

        BigDecimal totalPayments = BigDecimal.ZERO;
        BigDecimal totalRefunds = BigDecimal.ZERO;
        BigDecimal activePayments = BigDecimal.ZERO;
        BigDecimal customerBalances = BigDecimal.ZERO;

        List<Map<String, Object>> payments
                = new ArrayList<>();

        // Paid + refunded payments were recorded as paid
        // at some point in the system.
        String summarySql
                = "SELECT "
                + "COALESCE(SUM(CASE "
                + "WHEN PaymentStatus IN ('paid', 'refunded') "
                + "THEN Amount ELSE 0 END), 0) AS TotalPayments, "
                + "(SELECT COALESCE(SUM(Amount), 0) FROM WalletTransaction "
                + "WHERE TransactionType='refund') AS TotalRefunds "
                + "FROM Payment";

        // Money recorded against bookings that are
        // still pending or confirmed.
        String activeSql
                = "SELECT COALESCE(SUM(p.Amount), 0) AS ActivePayments "
                + "FROM Payment p "
                + "JOIN Booking b ON p.BookingID = b.BookingID "
                + "WHERE p.PaymentStatus = 'paid' "
                + "AND b.Status IN ('pending', 'confirmed')";

        String balanceSql
                = "SELECT COALESCE(SUM(Balance), 0) AS TotalBalance "
                + "FROM Wallet";

        String paymentSql
                = "SELECT TOP (100) "
                + "p.PaymentID, "
                + "p.BookingID, "
                + "p.Amount, "
                + "p.PaymentMethod, "
                + "p.PaymentStatus, "
                + "p.PaymentDate, "
                + "b.Status AS BookingStatus "
                + "FROM Payment p "
                + "JOIN Booking b ON p.BookingID = b.BookingID "
                + "ORDER BY p.PaymentID DESC";

        try (Connection conn
                = new DBContext().getConnection()) {

            try (PreparedStatement ps
                    = conn.prepareStatement(summarySql); ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalPayments
                            = rs.getBigDecimal("TotalPayments");

                    totalRefunds
                            = rs.getBigDecimal("TotalRefunds");
                }
            }

            try (PreparedStatement ps
                    = conn.prepareStatement(activeSql); ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    activePayments
                            = rs.getBigDecimal("ActivePayments");
                }
            }

            try (PreparedStatement ps
                    = conn.prepareStatement(balanceSql); ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    customerBalances
                            = rs.getBigDecimal("TotalBalance");
                }
            }

            try (PreparedStatement ps
                    = conn.prepareStatement(paymentSql); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> item
                            = new HashMap<>();

                    item.put("paymentId",
                            rs.getInt("PaymentID"));

                    item.put("bookingId",
                            rs.getInt("BookingID"));

                    item.put("amount",
                            rs.getBigDecimal("Amount"));

                    item.put("method",
                            rs.getString("PaymentMethod"));

                    item.put("status",
                            rs.getString("PaymentStatus"));

                    item.put("bookingStatus",
                            rs.getString("BookingStatus"));

                    item.put("date",
                            rs.getTimestamp("PaymentDate"));

                    payments.add(item);
                }
            }

        } catch (SQLException e) {
            getServletContext().log(
                    "Hotel Wallet query failed", e);

            response.sendError(503,
                    "Hotel Wallet unavailable");
            return;
        }

        request.setAttribute(
                "totalPayments", totalPayments);

        request.setAttribute(
                "totalRefunds", totalRefunds);

        request.setAttribute(
                "activePayments", activePayments);

        request.setAttribute(
                "customerBalances", customerBalances);

        request.setAttribute("payments", payments);

        request.getRequestDispatcher(
                "/WEB-INF/wallet/hotel-wallet.jsp"
        ).forward(request, response);
    }
}
