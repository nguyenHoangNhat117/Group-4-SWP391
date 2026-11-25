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
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@WebServlet("/withdrawal")
public class WithdrawalServlet extends HttpServlet {

    private static final SecureRandom RANDOM
            = new SecureRandom();

    private final WithdrawalDAO withdrawalDAO
            = new WithdrawalDAO();

    // CHI DANH CHO DEMO SWP391, KHONG SU DUNG VOI GIAO DICH THAT.
    // Bat/Tat ro rang; khi false, khong tao yeu cau rut tien bang ma hien tren trang.
    private static final boolean LOCAL_DEMO_MODE = true;
    private static final long OTP_VALID_MS = 5 * 60 * 1000L;
    private static final int MAX_ATTEMPTS = 5;

    private static final class PendingWithdrawal implements Serializable {
        private static final long serialVersionUID = 1L;
        final int userId;
        final BigDecimal amount;
        final String bankName;
        final String accountNumber;
        final String accountHolder;
        final String demoOtp;
        final long expiry;
        int attempts;

        PendingWithdrawal(int userId, BigDecimal amount, String bankName,
                String accountNumber, String accountHolder, String demoOtp) {
            this.userId = userId;
            this.amount = amount;
            this.bankName = bankName;
            this.accountNumber = accountNumber;
            this.accountHolder = accountHolder;
            this.demoOtp = demoOtp;
            this.expiry = System.currentTimeMillis() + OTP_VALID_MS;
        }
    }


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
        PendingWithdrawal pending = (PendingWithdrawal) session.getAttribute("pendingWithdrawalDemo");
        if (pending != null &&
                (System.currentTimeMillis() > pending.expiry || pending.userId != user.getId())) {
            session.removeAttribute("pendingWithdrawalDemo");
            pending = null;
        }
        String remote = request.getRemoteAddr();
        request.setAttribute("demoEnabled", LOCAL_DEMO_MODE && (
                "127.0.0.1".equals(remote) || "::1".equals(remote)
                || "0:0:0:0:0:0:0:1".equals(remote)));
        request.setAttribute("pendingDemo", pending);
        request.setAttribute("demoOtp", pending == null ? null : pending.demoOtp);

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = getCustomer(request, response);
        if (user == null) return;
        HttpSession session = request.getSession(false);
        if (!validCsrf(request, session)) {
            response.sendError(403, "Invalid CSRF token");
            return;
        }

        // Khong cho phep giao dich that duoc xac nhan bang OTP hien tren cung trang.
        String remote = request.getRemoteAddr();
        boolean localRequest = "127.0.0.1".equals(remote)
                || "::1".equals(remote)
                || "0:0:0:0:0:0:0:1".equals(remote);
        if (!LOCAL_DEMO_MODE || !localRequest) {
            response.sendError(403, "Demo withdrawal only available on localhost");
            return;
        }

        String action = trim(request.getParameter("action"));
        if ("sendDemoOtp".equals(action)) {
            createDemoOtp(request, response, user, session);
        } else if ("verifyDemoOtp".equals(action)) {
            verifyDemoOtp(request, response, user, session);
        } else if ("cancelDemoOtp".equals(action)) {
            session.removeAttribute("pendingWithdrawalDemo");
            response.sendRedirect(request.getContextPath() + "/withdrawal");
        } else {
            redirectError(request, response, "invalid-action");
        }
    }

    private void createDemoOtp(HttpServletRequest request, HttpServletResponse response,
            User user, HttpSession session) throws IOException {
        String rawAmount = trim(request.getParameter("amount"));
        String bankName = trim(request.getParameter("bankName"));
        String accountNumber = trim(request.getParameter("accountNumber"));
        String accountHolder = trim(request.getParameter("accountHolder"));
        BigDecimal amount;
        try {
            amount = new BigDecimal(rawAmount);
            if (amount.scale() > 0 || amount.signum() <= 0 || amount.precision() > 18)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            redirectError(request, response, "invalid-amount");
            return;
        }
        if (bankName.isEmpty() || bankName.length() > 100 ||
                !accountNumber.matches("[0-9]{6,30}") ||
                accountHolder.isEmpty() || accountHolder.length() > 150) {
            redirectError(request, response, "invalid-bank");
            return;
        }
        // UX: kiem tra so du som, DAO van kiem tra lai trong transaction luc tao request.
        try (Connection conn = new DBContext().getConnection()) {
            int customerId = findCustomerId(conn, user.getId());
            if (customerId <= 0) {
                response.sendError(403, "Customer profile not found");
                return;
            }
            BigDecimal balance = BigDecimal.ZERO;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT Balance FROM Wallet WHERE CustomerID = ?")) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBigDecimal(1) != null)
                        balance = rs.getBigDecimal(1);
                }
            }
            BigDecimal available = balance.subtract(withdrawalDAO.getReservedAmount(conn, customerId));
            if (available.compareTo(amount) < 0) {
                redirectError(request, response, "insufficient-balance");
                return;
            }
        } catch (SQLException ex) {
            getServletContext().log("Unable to check withdrawal demo balance", ex);
            response.sendError(503, "Withdrawal service unavailable");
            return;
        }
        String otp = String.format(java.util.Locale.ROOT, "%06d", RANDOM.nextInt(1000000));
        // Khong gui email: chi hien ma cho de kiem thu trong do an.
        session.setAttribute("pendingWithdrawalDemo", new PendingWithdrawal(
                user.getId(), amount, bankName, accountNumber, accountHolder, otp));
        response.sendRedirect(request.getContextPath() + "/withdrawal?demo=sent");
    }

    private void verifyDemoOtp(HttpServletRequest request, HttpServletResponse response,
            User user, HttpSession session) throws IOException {
        String entered = trim(request.getParameter("otp"));
        PendingWithdrawal pending;
        synchronized (session) {
            pending = (PendingWithdrawal) session.getAttribute("pendingWithdrawalDemo");
            if (pending == null || pending.userId != user.getId()) {
                redirectError(request, response, "otp-required");
                return;
            }
            if (System.currentTimeMillis() > pending.expiry) {
                session.removeAttribute("pendingWithdrawalDemo");
                redirectError(request, response, "otp-expired");
                return;
            }
            if (pending.attempts >= MAX_ATTEMPTS) {
                session.removeAttribute("pendingWithdrawalDemo");
                redirectError(request, response, "otp-locked");
                return;
            }
            boolean matched = entered.matches("[0-9]{6}") && MessageDigest.isEqual(
                    entered.getBytes(StandardCharsets.UTF_8),
                    pending.demoOtp.getBytes(StandardCharsets.UTF_8));
            if (!matched) {
                pending.attempts++;
                if (pending.attempts >= MAX_ATTEMPTS) {
                    session.removeAttribute("pendingWithdrawalDemo");
                    redirectError(request, response, "otp-locked");
                } else {
                    redirectError(request, response, "otp-invalid");
                }
                return;
            }
            // Thu hoi ma ngay khi xac thuc dung, khong cho gui request nhieu lan.
            session.removeAttribute("pendingWithdrawalDemo");
        }
        try (Connection conn = new DBContext().getConnection()) {
            int customerId = findCustomerId(conn, user.getId());
            if (customerId <= 0) {
                response.sendError(403, "Customer profile not found");
                return;
            }
            boolean created = withdrawalDAO.createRequest(customerId,
                    pending.amount, pending.bankName, pending.accountNumber, pending.accountHolder);
            if (!created) {
                redirectError(request, response, "insufficient-balance");
                return;
            }
            session.setAttribute("withdrawalCsrfToken", generateToken());
            response.sendRedirect(request.getContextPath() + "/withdrawal?success=1");
        } catch (SQLException ex) {
            getServletContext().log("Withdrawal demo creation failed", ex);
            response.sendError(503, "Withdrawal service unavailable");
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
