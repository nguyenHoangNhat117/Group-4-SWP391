package controller;

import db.DBContext;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import dao.WithdrawalDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@WebServlet(name = "WalletServlet", urlPatterns = {"/wallet"})
public class WalletServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Check login and role
        HttpSession session = req.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute("loggedUser");

        if (user == null) {
            resp.sendRedirect(
                    req.getContextPath() + "/login");
            return;
        }

        if (!"customer".equalsIgnoreCase(user.getRole())) {
            resp.sendError(403, "Bạn không có quyền truy cập");
            return;
        }

        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal reservedAmount = BigDecimal.ZERO;
        BigDecimal availableBalance = BigDecimal.ZERO;

        WithdrawalDAO withdrawalDAO = new WithdrawalDAO();

        List<Map<String, Object>> transactions
                = new ArrayList<>();

        // 2. Pagination
        final int pageSize = 10;

        int page = 1;
        int totalPages = 1;
        int totalTransactions = 0;

        String pageParam = req.getParameter("page");

        if (pageParam != null
                && !pageParam.trim().isEmpty()) {

            try {
                page = Integer.parseInt(pageParam.trim());

                if (page < 1) {
                    resp.sendError(
                            400, "Invalid page number");
                    return;
                }

            } catch (NumberFormatException e) {
                resp.sendError(
                        400, "Invalid page number");
                return;
            }
        }

        // 3. Read transaction filters
        String type = req.getParameter("type");

        type = type == null
                ? ""
                : type.trim().toLowerCase(Locale.ROOT);

        if (!type.isEmpty()
                && !"refund".equals(type)
                && !"withdrawal".equals(type)) {

            resp.sendError(
                    400, "Invalid transaction type");
            return;
        }

        String fromDateParam
                = req.getParameter("fromDate");

        String toDateParam
                = req.getParameter("toDate");

        LocalDate fromDate = null;
        LocalDate toDate = null;

        try {

            if (fromDateParam != null
                    && !fromDateParam.trim().isEmpty()) {

                fromDate = LocalDate.parse(
                        fromDateParam.trim());
            }

            if (toDateParam != null
                    && !toDateParam.trim().isEmpty()) {

                toDate = LocalDate.parse(
                        toDateParam.trim());
            }

            if (fromDate != null
                    && toDate != null
                    && fromDate.isAfter(toDate)) {

                resp.sendError(
                        400, "Invalid date range");
                return;
            }

            if (toDate != null
                    && toDate.equals(LocalDate.MAX)) {

                resp.sendError(
                        400, "Invalid end date");
                return;
            }

        } catch (DateTimeParseException e) {

            resp.sendError(
                    400, "Invalid date format");
            return;
        }

        // 4. Build filter conditions
        StringBuilder filterSql = new StringBuilder();

        if (!type.isEmpty()) {
            filterSql.append(
                    " AND t.TransactionType = ? ");
        }

        if (fromDate != null) {
            filterSql.append(
                    " AND t.CreatedAt >= ? ");
        }

        if (toDate != null) {
            filterSql.append(
                    " AND t.CreatedAt < ? ");
        }

        try (Connection conn
                = new DBContext().getConnection()) {

            // 5. Find CustomerID by logged-in UserID
            int customerId;

            String customerSql
                    = "SELECT CustomerID "
                    + "FROM Customer "
                    + "WHERE UserID = ?";

            try (PreparedStatement ps
                    = conn.prepareStatement(customerSql)) {

                ps.setInt(1, user.getId());

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        resp.sendError(
                                403,
                                "Customer profile not found");
                        return;
                    }

                    customerId = rs.getInt("CustomerID");
                }
            }

            // 6. Get Wallet Balance
            String balanceSql
                    = "SELECT Balance "
                    + "FROM Wallet "
                    + "WHERE CustomerID = ?";

            try (PreparedStatement ps
                    = conn.prepareStatement(balanceSql)) {

                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        BigDecimal value
                                = rs.getBigDecimal("Balance");

                        if (value != null) {
                            balance = value;
                        }
                    }
                }
            }

            reservedAmount = withdrawalDAO.getReservedAmount(
                    conn, customerId
            );

            availableBalance = balance.subtract(reservedAmount);

            if (availableBalance.compareTo(BigDecimal.ZERO) < 0) {
                availableBalance = BigDecimal.ZERO;
            }

            // 7. Count filtered transactions
            String countSql
                    = "SELECT COUNT(*) "
                    + "FROM WalletTransaction t "
                    + "JOIN Wallet w "
                    + "ON t.WalletID = w.WalletID "
                    + "WHERE w.CustomerID = ? "
                    + filterSql;

            try (PreparedStatement ps
                    = conn.prepareStatement(countSql)) {

                bindFilters(
                        ps,
                        customerId,
                        type,
                        fromDate,
                        toDate
                );

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        totalTransactions = rs.getInt(1);
                    }
                }
            }

            // 8. Calculate pagination
            totalPages = Math.max(
                    1,
                    (int) (((long) totalTransactions
                    + pageSize - 1) / pageSize)
            );

            if (page > totalPages) {
                page = totalPages;
            }

            int offset = (page - 1) * pageSize;

            // 9. Get filtered transaction history
            String transactionSql
                    = "SELECT t.WalletTransactionID, "
                    + "t.BookingID, "
                    + "t.Amount, "
                    + "t.TransactionType, "
                    + "t.Description, "
                    + "t.CreatedAt "
                    + "FROM WalletTransaction t "
                    + "JOIN Wallet w "
                    + "ON t.WalletID = w.WalletID "
                    + "WHERE w.CustomerID = ? "
                    + filterSql
                    + "ORDER BY t.CreatedAt DESC, "
                    + "t.WalletTransactionID DESC "
                    + "OFFSET ? ROWS "
                    + "FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement ps
                    = conn.prepareStatement(transactionSql)) {

                int index = bindFilters(
                        ps,
                        customerId,
                        type,
                        fromDate,
                        toDate
                );

                ps.setInt(index++, offset);
                ps.setInt(index, pageSize);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        Map<String, Object> item
                                = new HashMap<>();

                        item.put(
                                "transactionId",
                                rs.getInt("WalletTransactionID")
                        );

                        item.put(
                                "bookingId",
                                rs.getObject("BookingID")
                        );

                        item.put(
                                "amount",
                                rs.getBigDecimal("Amount")
                        );

                        item.put(
                                "type",
                                rs.getString("TransactionType")
                        );

                        item.put(
                                "description",
                                rs.getString("Description")
                        );

                        item.put(
                                "createdAt",
                                rs.getTimestamp("CreatedAt")
                        );

                        transactions.add(item);
                    }
                }
            }

        } catch (SQLException e) {

            getServletContext().log(
                    "Wallet query failed", e);

            resp.sendError(
                    503, "Wallet unavailable");
            return;
        }

        // 10. Send data to JSP
        req.setAttribute("balance", balance);
        req.setAttribute("transactions", transactions);
        req.setAttribute("reservedAmount", reservedAmount);
        req.setAttribute("availableBalance", availableBalance);

        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);

        req.setAttribute(
                "totalTransactions", totalTransactions);

        req.setAttribute("pageSize", pageSize);

        req.setAttribute("selectedType", type);
        req.setAttribute("selectedFromDate", fromDateParam);
        req.setAttribute("selectedToDate", toDateParam);

        req.getRequestDispatcher(
                "/WEB-INF/wallet/wallet.jsp"
        ).forward(req, resp);
    }

    // Bind the same filters for COUNT and SELECT.
    // Return the next available parameter index.
    private int bindFilters(
            PreparedStatement ps,
            int customerId,
            String type,
            LocalDate fromDate,
            LocalDate toDate)
            throws SQLException {

        int index = 1;

        ps.setInt(index++, customerId);

        if (!type.isEmpty()) {

            ps.setString(index++, type);
        }

        if (fromDate != null) {

            ps.setTimestamp(
                    index++,
                    Timestamp.valueOf(
                            fromDate.atStartOfDay()
                    )
            );
        }

        if (toDate != null) {

            ps.setTimestamp(
                    index++,
                    Timestamp.valueOf(
                            toDate.plusDays(1).atStartOfDay()
                    )
            );
        }

        return index;
    }
}
