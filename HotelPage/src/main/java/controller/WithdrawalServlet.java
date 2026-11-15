package controller;

import dao.WithdrawalDAO;
import db.DBContext;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

@WebServlet("/withdrawal")
public class WithdrawalServlet extends HttpServlet {

    private static final SecureRandom RANDOM
            = new SecureRandom();

    private final WithdrawalDAO withdrawalDAO
            = new WithdrawalDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCustomer(request, response);

        if (user == null) {
            return;
        }

        HttpSession session = request.getSession();
        ensureCsrfToken(session);

        try (Connection conn
                = new DBContext().getConnection()) {

            int customerId
                    = findCustomerId(conn, user.getId());

            if (customerId <= 0) {
                response.sendError(
                        403, "Customer profile not found");
                return;
            }

            String balanceSql
                    = "SELECT Balance FROM Wallet "
                    + "WHERE CustomerID = ?";

            BigDecimal balance = BigDecimal.ZERO;

            try (PreparedStatement ps
                    = conn.prepareStatement(balanceSql)) {

                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        balance = rs.getBigDecimal("Balance");
                    }
                }
            }

            if (balance == null) {
                balance = BigDecimal.ZERO;
            }

            BigDecimal reserved
                    = withdrawalDAO.getReservedAmount(
                            conn, customerId);

            BigDecimal available
                    = balance.subtract(reserved);

            request.setAttribute(
                    "walletBalance", balance);

            request.setAttribute(
                    "reservedAmount", reserved);

            request.setAttribute(
                    "availableBalance", available);

            request.getRequestDispatcher(
                    "/WEB-INF/wallet/withdrawal.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            getServletContext().log(
                    "Cannot load withdrawal form", e);

            response.sendError(
                    503, "Withdrawal service unavailable");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCustomer(request, response);

        if (user == null) {
            return;
        }

        HttpSession session
                = request.getSession(false);

        if (!validCsrf(request, session)) {
            response.sendError(
                    403, "Invalid CSRF token");
            return;
        }

        String amountParam
                = trim(request.getParameter("amount"));

        String bankName
                = trim(request.getParameter("bankName"));

        String accountNumber
                = trim(request.getParameter("accountNumber"));

        String accountHolder
                = trim(request.getParameter("accountHolder"));

        BigDecimal amount;

        try {
            amount = new BigDecimal(amountParam);

            if (amount.scale() > 0
                    || amount.compareTo(
                            BigDecimal.ZERO) <= 0
                    || amount.precision() > 18) {

                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            redirectError(request, response,
                    "invalid-amount");
            return;
        }

        if (bankName.isEmpty()
                || bankName.length() > 100
                || accountNumber.isEmpty()
                || accountNumber.length() > 100
                || !accountNumber.matches("[0-9]{6,30}")
                || accountHolder.isEmpty()
                || accountHolder.length() > 150) {

            redirectError(request, response,
                    "invalid-bank");
            return;
        }

        try (Connection conn
                = new DBContext().getConnection()) {

            int customerId
                    = findCustomerId(conn, user.getId());

            if (customerId <= 0) {
                response.sendError(
                        403, "Customer profile not found");
                return;
            }

            boolean created
                    = withdrawalDAO.createRequest(
                            customerId,
                            amount,
                            bankName,
                            accountNumber,
                            accountHolder
                    );

            if (!created) {
                redirectError(request, response,
                        "insufficient-balance");
                return;
            }

            // Rotate token after successful submission.
            session.setAttribute(
                    "withdrawalCsrfToken",
                    generateToken());

            response.sendRedirect(
                    request.getContextPath()
                    + "/withdrawal?success=1");

        } catch (SQLException e) {
            getServletContext().log(
                    "Withdrawal creation failed", e);

            response.sendError(
                    503, "Withdrawal service unavailable");
        }
    }

    private User getCustomer(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session
                = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute(
                        "loggedUser");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return null;
        }

        if (!"customer".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    403, "Customer access only");
            return null;
        }

        return user;
    }

    private int findCustomerId(
            Connection conn,
            int userId) throws SQLException {

        String sql
                = "SELECT CustomerID FROM Customer "
                + "WHERE UserID = ?";

        try (PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getInt("CustomerID")
                        : -1;
            }
        }
    }

    private void ensureCsrfToken(
            HttpSession session) {

        if (session.getAttribute(
                "withdrawalCsrfToken") == null) {

            session.setAttribute(
                    "withdrawalCsrfToken",
                    generateToken());
        }
    }

    private boolean validCsrf(
            HttpServletRequest request,
            HttpSession session) {

        if (session == null) {
            return false;
        }

        Object token = session.getAttribute(
                "withdrawalCsrfToken");

        String submitted
                = request.getParameter("csrfToken");

        return token instanceof String
                && submitted != null
                && java.security.MessageDigest.isEqual(
                        ((String) token).getBytes(
                                java.nio.charset.StandardCharsets.UTF_8),
                        submitted.getBytes(
                                java.nio.charset.StandardCharsets.UTF_8)
                );
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void redirectError(
            HttpServletRequest request,
            HttpServletResponse response,
            String error) throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/withdrawal?error=" + error);
    }
}
