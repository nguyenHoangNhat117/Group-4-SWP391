package controller;

import db.DBContext;
import model.Customer;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@WebServlet(name = "WalletServlet", urlPatterns = {"/wallet"})
public class WalletServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loggedUser");
        Customer customer = session == null ? null : (Customer) session.getAttribute("customer");
        if (user == null || customer == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!"customer".equalsIgnoreCase(user.getRole())) {
            resp.sendError(403);
            return;
        }
        BigDecimal balance = BigDecimal.ZERO;
        List<Map<String, Object>> transactions = new ArrayList<>();
        try (Connection conn = new DBContext().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT Balance FROM Wallet WHERE CustomerID=?")) {
                ps.setInt(1, customer.getCustomerID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        balance = rs.getBigDecimal(1);
                    }
                }
            }
            String sql = "SELECT t.BookingID,t.Amount,t.TransactionType,t.Description,t.CreatedAt "
                    + "FROM WalletTransaction t JOIN Wallet w ON w.WalletID=t.WalletID "
                    + "WHERE w.CustomerID=? ORDER BY t.CreatedAt DESC,t.WalletTransactionID DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, customer.getCustomerID());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("bookingId", rs.getObject("BookingID"));
                        item.put("amount", rs.getBigDecimal("Amount"));
                        item.put("type", rs.getString("TransactionType"));
                        item.put("description", rs.getString("Description"));
                        item.put("createdAt", rs.getTimestamp("CreatedAt"));
                        transactions.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Wallet query failed", e);
            resp.sendError(503, "Wallet unavailable");
            return;
        }
        req.setAttribute("balance", balance);
        req.setAttribute("transactions", transactions);
        req.getRequestDispatcher("/WEB-INF/wallet/wallet.jsp").forward(req, resp);
    }
}
