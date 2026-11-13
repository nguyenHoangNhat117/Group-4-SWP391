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

@WebServlet("/wallet-management")
public class WalletManagementServlet extends HttpServlet {

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

        List<Map<String, Object>> wallets = new ArrayList<>();
        List<Map<String, Object>> transactions = new ArrayList<>();

        Integer selectedCustomerId = null;
        String customerIdParam = request.getParameter("customerId");

        if (customerIdParam != null
                && !customerIdParam.isBlank()) {
            try {
                selectedCustomerId
                        = Integer.parseInt(customerIdParam);

                if (selectedCustomerId <= 0) {
                    response.sendError(400, "Invalid customer ID");
                    return;
                }
            } catch (NumberFormatException e) {
                response.sendError(400, "Invalid customer ID");
                return;
            }
        }

        String walletSql
                = "SELECT TOP (100) c.CustomerID, "
                + "c.FirstName, c.LastName, c.Email, "
                + "COALESCE(w.Balance, 0) AS Balance "
                + "FROM Customer c "
                + "LEFT JOIN Wallet w "
                + "ON c.CustomerID = w.CustomerID "
                + "ORDER BY c.CustomerID DESC";

        String transactionSql
                = "SELECT TOP (100) t.WalletTransactionID, "
                + "t.BookingID, t.Amount, "
                + "t.TransactionType, t.Description, "
                + "t.CreatedAt "
                + "FROM WalletTransaction t "
                + "JOIN Wallet w ON t.WalletID = w.WalletID "
                + "WHERE w.CustomerID = ? "
                + "ORDER BY t.CreatedAt DESC, "
                + "t.WalletTransactionID DESC";

        try (Connection conn = new DBContext().getConnection()) {

            try (PreparedStatement ps
                    = conn.prepareStatement(walletSql); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> wallet = new HashMap<>();

                    wallet.put("customerId",
                            rs.getInt("CustomerID"));

                    wallet.put("name",
                            rs.getString("FirstName") + " "
                            + rs.getString("LastName"));

                    wallet.put("email", rs.getString("Email"));
                    wallet.put("balance",
                            rs.getBigDecimal("Balance"));

                    wallets.add(wallet);
                }
            }

            if (selectedCustomerId != null) {

                try (PreparedStatement ps
                        = conn.prepareStatement(transactionSql)) {

                    ps.setInt(1, selectedCustomerId);

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                            Map<String, Object> tx = new HashMap<>();

                            tx.put("transactionId",
                                    rs.getInt("WalletTransactionID"));

                            tx.put("bookingId",
                                    rs.getObject("BookingID"));

                            tx.put("amount",
                                    rs.getBigDecimal("Amount"));

                            tx.put("type",
                                    rs.getString("TransactionType"));

                            tx.put("description",
                                    rs.getString("Description"));

                            tx.put("createdAt",
                                    rs.getTimestamp("CreatedAt"));

                            transactions.add(tx);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            getServletContext().log(
                    "Wallet management query failed", e);

            response.sendError(503,
                    "Wallet service unavailable");
            return;
        }

        request.setAttribute("wallets", wallets);
        request.setAttribute("transactions", transactions);
        request.setAttribute("selectedCustomerId",
                selectedCustomerId);

        request.getRequestDispatcher(
                "/WEB-INF/wallet/wallet-management.jsp")
                .forward(request, response);
    }
}
