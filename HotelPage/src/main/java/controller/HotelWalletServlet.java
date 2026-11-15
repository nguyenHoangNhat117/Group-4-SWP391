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

        if (!"manager".equalsIgnoreCase(role)) {
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
                + "WHERE TransactionType='refund') + (SELECT COALESCE(SUM(Amount),0) FROM OfflineRefund WHERE Status='completed') AS TotalRefunds "
                + "FROM Payment";

        // Money recorded against bookings that are
        // still pending or confirmed.
        String activeSql
                = "SELECT COALESCE(SUM(p.Amount-COALESCE(p.RefundedAmount,0)), 0) AS ActivePayments "
                + "FROM Payment p "
                + "JOIN Booking b ON p.BookingID = b.BookingID "
                + "WHERE p.PaymentStatus = 'paid' "
                + "AND b.Status IN ('pending', 'confirmed')";

        String balanceSql
                = "SELECT COALESCE(SUM(Balance), 0) AS TotalBalance "
                + "FROM Wallet";

        List<Object> filters = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        String[] keys = {"paymentId", "method", "paymentStatus", "bookingStatus"};
        String[] columns = {"p.PaymentID", "p.PaymentMethod", "p.PaymentStatus", "b.Status"};
        for (int i=0;i<keys.length;i++) {
            String value = request.getParameter(keys[i]);
            if (value != null && !value.isBlank()) {
                if (value.length()>80 || (i==0 && !value.matches("[0-9]{1,9}"))) {
                    response.sendError(400, "Invalid payment filter"); return;
                }
                where.append(" AND ").append(columns[i]).append("=? ");
                filters.add(i==0 ? Integer.valueOf(value) : value);
            }
        }
        String date = request.getParameter("date");
        if (date != null && !date.isBlank()) {
            try {
                java.time.LocalDate day = java.time.LocalDate.parse(date);
                where.append(" AND p.PaymentDate>=? AND p.PaymentDate<? ");
                filters.add(java.sql.Date.valueOf(day));
                filters.add(java.sql.Date.valueOf(day.plusDays(1)));
            } catch (java.time.DateTimeException ex) { response.sendError(400, "Invalid payment date"); return; }
        }
        int page = 1;
        try { page = Math.max(1, Integer.parseInt(request.getParameter("page"))); }
        catch (NumberFormatException ignored) {}
        String sort = request.getParameter("sort");
        String order = "asc".equals(sort) ? "p.Amount ASC, p.PaymentID DESC" :
                "desc".equals(sort) ? "p.Amount DESC, p.PaymentID DESC" : "p.PaymentID DESC";
        String paymentSql
                = "SELECT "
                + "p.PaymentID, "
                + "p.BookingID, "
                + "p.Amount, "
                + "p.PaymentMethod, "
                + "p.PaymentStatus, "
                + "p.PaymentDate, "
                + "b.Status AS BookingStatus "
                + "FROM Payment p "
                + "JOIN Booking b ON p.BookingID = b.BookingID "
                + where + " ORDER BY " + order + " OFFSET ? ROWS FETCH NEXT 30 ROWS ONLY";

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

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Payment p JOIN Booking b ON b.BookingID=p.BookingID " + where)) {
                for (int i=0;i<filters.size();i++) ps.setObject(i+1, filters.get(i));
                try (ResultSet rs=ps.executeQuery()) {
                    rs.next();
                    int total=rs.getInt(1), pages=Math.max(1,(total+29)/30);
                    page=Math.min(page,pages);
                    request.setAttribute("totalPaymentsCount",total);
                    request.setAttribute("totalPages",pages);
                    request.setAttribute("currentPage",page);
                }
            }
            String[] optionSql = {
                "SELECT DISTINCT PaymentMethod FROM Payment WHERE PaymentMethod IS NOT NULL ORDER BY PaymentMethod",
                "SELECT DISTINCT PaymentStatus FROM Payment WHERE PaymentStatus IS NOT NULL ORDER BY PaymentStatus",
                "SELECT DISTINCT Status FROM Booking WHERE Status IS NOT NULL ORDER BY Status"
            };
            String[] optionNames={"paymentMethods","paymentStatuses","bookingStatuses"};
            for (int i=0;i<optionSql.length;i++) {
                List<String> options=new ArrayList<>();
                try (PreparedStatement ps=conn.prepareStatement(optionSql[i]);ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) options.add(rs.getString(1));
                }
                request.setAttribute(optionNames[i], options);
            }
            try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                int i=0;
                for (Object value:filters) ps.setObject(++i,value);
                ps.setInt(++i,(page-1)*30);
                try (ResultSet rs=ps.executeQuery()) {

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
