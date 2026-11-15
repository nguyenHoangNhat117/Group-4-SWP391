package dao;

import db.DBContext;
import model.WithdrawalRequest;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WithdrawalDAO {

    // =====================================================
    // 1. GET RESERVED AMOUNT
    // =====================================================
    public BigDecimal getReservedAmount(
            Connection conn, int customerId)
            throws SQLException {

        String sql
                = "SELECT COALESCE(SUM(Amount), 0) "
                + "AS ReservedAmount "
                + "FROM dbo.WithdrawalRequest "
                + "WHERE CustomerID = ? "
                + "AND Status IN ('pending', 'approved')";

        try (PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("ReservedAmount");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    // =====================================================
    // 2. CREATE WITHDRAWAL REQUEST
    // =====================================================
    public boolean createRequest(
            int customerId,
            BigDecimal amount,
            String bankName,
            String accountNumber,
            String accountHolder)
            throws SQLException {

        if (customerId <= 0
                || amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 0
                || amount.precision() > 18
                || bankName == null
                || bankName.trim().isEmpty()
                || bankName.trim().length() > 100
                || accountNumber == null
                || !accountNumber.trim().matches("[0-9]{6,30}")
                || accountHolder == null
                || accountHolder.trim().isEmpty()
                || accountHolder.trim().length() > 150) {

            return false;
        }

        String walletSql
                = "SELECT WalletID, Balance "
                + "FROM dbo.Wallet WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE CustomerID = ?";

        String insertSql
                = "INSERT INTO dbo.WithdrawalRequest "
                + "(CustomerID, Amount, BankName, "
                + "AccountNumber, AccountHolder, Status) "
                + "VALUES (?, ?, ?, ?, ?, 'pending')";

        try (Connection conn
                = new DBContext().getConnection()) {

            conn.setAutoCommit(false);

            try {
                BigDecimal balance;

                // Lock wallet row before calculating balance.
                try (PreparedStatement ps
                        = conn.prepareStatement(walletSql)) {

                    ps.setInt(1, customerId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        balance = rs.getBigDecimal("Balance");

                        if (balance == null) {
                            balance = BigDecimal.ZERO;
                        }
                    }
                }

                BigDecimal reserved
                        = getReservedAmount(conn, customerId);

                BigDecimal available
                        = balance.subtract(reserved);

                if (amount.compareTo(available) > 0) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps
                        = conn.prepareStatement(insertSql)) {

                    ps.setInt(1, customerId);
                    ps.setBigDecimal(2, amount);
                    ps.setString(3, bankName.trim());
                    ps.setString(4, accountNumber.trim());
                    ps.setString(5, accountHolder.trim());

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    // =====================================================
    // 3. APPROVE WITHDRAWAL
    // =====================================================
    public boolean approve(
            int withdrawalId,
            int managerId)
            throws SQLException {

        if (withdrawalId <= 0 || managerId <= 0) {
            return false;
        }

        String sql
                = "UPDATE dbo.WithdrawalRequest "
                + "SET Status = 'approved', "
                + "ReviewedBy = ?, "
                + "ReviewedAt = SYSUTCDATETIME() "
                + "WHERE WithdrawalID = ? "
                + "AND Status = 'pending'";

        try (Connection conn
                = new DBContext().getConnection()) {

            conn.setAutoCommit(false);

            try {
                // Admin only until manager scope is configured.

                if (!isAdminOrManager(conn, managerId)) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps
                        = conn.prepareStatement(sql)) {

                    ps.setInt(1, managerId);
                    ps.setInt(2, withdrawalId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    // =====================================================
    // 4. REJECT WITHDRAWAL
    // =====================================================
    public boolean reject(
            int withdrawalId,
            int managerId,
            String reason)
            throws SQLException {

        if (withdrawalId <= 0
                || managerId <= 0
                || reason == null
                || reason.trim().isEmpty()
                || reason.trim().length() > 500) {

            return false;
        }

        String sql
                = "UPDATE dbo.WithdrawalRequest "
                + "SET Status = 'rejected', "
                + "RejectReason = ?, "
                + "ReviewedBy = ?, "
                + "ReviewedAt = SYSUTCDATETIME() "
                + "WHERE WithdrawalID = ? "
                + "AND Status = 'pending'";

        try (Connection conn
                = new DBContext().getConnection()) {

            conn.setAutoCommit(false);

            try {

                if (!isAdminOrManager(conn, managerId)) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps
                        = conn.prepareStatement(sql)) {

                    ps.setString(1, reason.trim());
                    ps.setInt(2, managerId);
                    ps.setInt(3, withdrawalId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    // =====================================================
    // 5. MARK AS PAID
    // =====================================================
    public boolean markPaid(
            int withdrawalId,
            int operatorId,
            String paymentReference)
            throws SQLException {

        if (withdrawalId <= 0
                || operatorId <= 0
                || paymentReference == null
                || paymentReference.trim().isEmpty()
                || paymentReference.trim().length() > 100) {
            return false;
        }

        String reference = paymentReference.trim();

        String findSql
                = "SELECT CustomerID "
                + "FROM dbo.WithdrawalRequest "
                + "WHERE WithdrawalID = ?";

        String walletSql
                = "SELECT WalletID, Balance "
                + "FROM dbo.Wallet WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE CustomerID = ?";

        String requestSql
                = "SELECT CustomerID, Amount, Status "
                + "FROM dbo.WithdrawalRequest "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE WithdrawalID = ?";

        String updateWalletSql
                = "UPDATE dbo.Wallet "
                + "SET Balance = Balance - ? "
                + "WHERE WalletID = ? "
                + "AND Balance >= ?";

        String transactionSql
                = "INSERT INTO dbo.WalletTransaction "
                + "(WalletID, BookingID, Amount, "
                + "TransactionType, Description, CreatedAt) "
                + "VALUES (?, NULL, ?, 'withdrawal', "
                + "?, SYSUTCDATETIME())";

        String updateRequestSql
                = "UPDATE dbo.WithdrawalRequest "
                + "SET Status = 'paid', "
                + "PaidBy = ?, "
                + "PaidAt = SYSUTCDATETIME(), "
                + "PaymentReference = ? "
                + "WHERE WithdrawalID = ? "
                + "AND Status = 'approved'";

        try (Connection conn
                = new DBContext().getConnection()) {

            conn.setAutoCommit(false);

            try {

                // 1. Verify operator is an Admin.
                if (!isAdminOrManager(conn, operatorId)) {
                    conn.rollback();
                    return false;
                }

                // 2. Find owner of the withdrawal.
                int customerId;

                try (PreparedStatement ps
                        = conn.prepareStatement(findSql)) {

                    ps.setInt(1, withdrawalId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        customerId = rs.getInt("CustomerID");
                    }
                }

                // 3. Lock Wallet first.
                int walletId;
                BigDecimal balance;

                try (PreparedStatement ps
                        = conn.prepareStatement(walletSql)) {

                    ps.setInt(1, customerId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        walletId = rs.getInt("WalletID");
                        balance = rs.getBigDecimal("Balance");
                    }
                }

                if (balance == null) {
                    conn.rollback();
                    return false;
                }

                // 4. Lock request and verify status.
                BigDecimal amount;

                try (PreparedStatement ps
                        = conn.prepareStatement(requestSql)) {

                    ps.setInt(1, withdrawalId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()
                                || rs.getInt("CustomerID")
                                != customerId
                                || !"approved".equalsIgnoreCase(
                                        rs.getString("Status"))) {

                            conn.rollback();
                            return false;
                        }

                        amount = rs.getBigDecimal("Amount");
                    }
                }

                if (amount == null
                        || amount.compareTo(BigDecimal.ZERO) <= 0
                        || balance.compareTo(amount) < 0) {

                    conn.rollback();
                    return false;
                }

                // 5. Check reserved funds.
                BigDecimal reserved
                        = getReservedAmount(conn, customerId);

                // Prevent consuming money reserved
                // by other pending/approved requests.
                if (balance.compareTo(reserved) < 0) {
                    conn.rollback();
                    return false;
                }

                // 6. Debit Wallet exactly once.
                try (PreparedStatement ps
                        = conn.prepareStatement(updateWalletSql)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, walletId);
                    ps.setBigDecimal(3, amount);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                // 7. Record a negative wallet transaction.
                try (PreparedStatement ps
                        = conn.prepareStatement(transactionSql)) {

                    ps.setInt(1, walletId);
                    ps.setBigDecimal(2, amount.negate());

                    ps.setString(
                            3,
                            "Withdrawal request #" + withdrawalId
                    );

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                // 8. Mark withdrawal as Paid.
                try (PreparedStatement ps
                        = conn.prepareStatement(updateRequestSql)) {

                    ps.setInt(1, operatorId);
                    ps.setString(2, reference);
                    ps.setInt(3, withdrawalId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                // All database changes succeed together.
                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    // =====================================================
    // 6. COUNT MANAGEABLE REQUESTS
    // =====================================================
    public int countManageableRequests(
            int userId,
            String role,
            String status)
            throws SQLException {

        if (!"admin".equalsIgnoreCase(role)
                && !"manager".equalsIgnoreCase(role)) {

            throw new SecurityException("Access denied");
        }

        String selectedStatus = normalizeStatus(status);

        String sql
                = "SELECT COUNT(*) "
                + "FROM dbo.WithdrawalRequest "
                + (selectedStatus.isEmpty()
                ? ""
                : "WHERE Status = ?");

        try (Connection conn
                = new DBContext().getConnection()) {

            if (!isAdminOrManager(conn, userId)) {
                throw new SecurityException("Access denied");
            }

            try (PreparedStatement ps
                    = conn.prepareStatement(sql)) {

                if (!selectedStatus.isEmpty()) {
                    ps.setString(1, selectedStatus);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        return 0;
    }

    // =====================================================
    // 7. LIST MANAGEABLE REQUESTS
    // =====================================================
    public List<WithdrawalRequest> listManageableRequests(
            int userId,
            String role,
            String status,
            int page,
            int pageSize)
            throws SQLException {

        if (!"admin".equalsIgnoreCase(role)
                && !"manager".equalsIgnoreCase(role)) {

            throw new SecurityException("Access denied");
        }

        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException(
                    "Invalid pagination");
        }

        String selectedStatus = normalizeStatus(status);

        long offset = ((long) page - 1) * pageSize;

        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Page number too large");
        }

        String sql
                = "SELECT WithdrawalID, CustomerID, Amount, "
                + "BankName, AccountNumber, AccountHolder, "
                + "Status, RejectReason, ReviewedBy, "
                + "ReviewedAt, PaidBy, PaidAt, "
                + "PaymentReference, CreatedAt "
                + "FROM dbo.WithdrawalRequest "
                + (selectedStatus.isEmpty()
                ? ""
                : "WHERE Status = ? ")
                + "ORDER BY CreatedAt DESC, WithdrawalID DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<WithdrawalRequest> requests
                = new ArrayList<>();

        try (Connection conn
                = new DBContext().getConnection()) {

            if (!isAdminOrManager(conn, userId)) {
                throw new SecurityException("Access denied");
            }

            try (PreparedStatement ps
                    = conn.prepareStatement(sql)) {

                int index = 1;

                if (!selectedStatus.isEmpty()) {
                    ps.setString(index++, selectedStatus);
                }

                ps.setInt(index++, (int) offset);
                ps.setInt(index, pageSize);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        requests.add(mapWithdrawal(rs));
                    }
                }
            }
        }

        return requests;
    }

    // =====================================================
    // 8. LIST CUSTOMER REQUESTS
    // =====================================================
    public List<WithdrawalRequest> listCustomerRequests(
            int customerId)
            throws SQLException {

        List<WithdrawalRequest> requests
                = new ArrayList<>();

        if (customerId <= 0) {
            return requests;
        }

        String sql
                = "SELECT WithdrawalID, CustomerID, Amount, "
                + "BankName, AccountNumber, AccountHolder, "
                + "Status, RejectReason, ReviewedBy, "
                + "ReviewedAt, PaidBy, PaidAt, "
                + "PaymentReference, CreatedAt "
                + "FROM dbo.WithdrawalRequest "
                + "WHERE CustomerID = ? "
                + "ORDER BY CreatedAt DESC, WithdrawalID DESC";

        try (Connection conn
                = new DBContext().getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    requests.add(mapWithdrawal(rs));
                }
            }
        }

        return requests;
    }

    // =====================================================
    // 9. GET REQUEST BY ID
    // =====================================================
    public WithdrawalRequest getRequestById(
            int withdrawalId,
            int userId,
            String role)
            throws SQLException {

        if (withdrawalId <= 0 || userId <= 0) {
            return null;
        }

        try (Connection conn
                = new DBContext().getConnection()) {

            boolean admin
                    = ("admin".equalsIgnoreCase(role)
                    || "manager".equalsIgnoreCase(role))
                    && isAdminOrManager(conn, userId);

            boolean customer
                    = "customer".equalsIgnoreCase(role)
                    && isCustomer(conn, userId);

            if (!admin && !customer) {
                return null;
            }

            String sql
                    = "SELECT r.WithdrawalID, r.CustomerID, "
                    + "r.Amount, r.BankName, r.AccountNumber, "
                    + "r.AccountHolder, r.Status, "
                    + "r.RejectReason, r.ReviewedBy, "
                    + "r.ReviewedAt, r.PaidBy, r.PaidAt, "
                    + "r.PaymentReference, r.CreatedAt "
                    + "FROM dbo.WithdrawalRequest r "
                    + "WHERE r.WithdrawalID = ? ";

            if (!admin) {
                sql
                        += "AND EXISTS ("
                        + "SELECT 1 FROM dbo.Customer c "
                        + "WHERE c.CustomerID = r.CustomerID "
                        + "AND c.UserID = ?)";
            }

            try (PreparedStatement ps
                    = conn.prepareStatement(sql)) {

                ps.setInt(1, withdrawalId);

                if (!admin) {
                    ps.setInt(2, userId);
                }

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return mapWithdrawal(rs);
                    }
                }
            }
        }

        return null;
    }

    // =====================================================
    // HELPER: MAP RESULTSET TO MODEL
    // =====================================================
    private WithdrawalRequest mapWithdrawal(
            ResultSet rs)
            throws SQLException {

        WithdrawalRequest w
                = new WithdrawalRequest();

        w.setWithdrawalId(
                rs.getInt("WithdrawalID"));

        w.setCustomerId(
                rs.getInt("CustomerID"));

        w.setAmount(
                rs.getBigDecimal("Amount"));

        w.setBankName(
                rs.getString("BankName"));

        w.setAccountNumber(
                rs.getString("AccountNumber"));

        w.setAccountHolder(
                rs.getString("AccountHolder"));

        w.setStatus(
                rs.getString("Status"));

        w.setRejectReason(
                rs.getString("RejectReason"));

        int reviewedBy = rs.getInt("ReviewedBy");

        w.setReviewedBy(
                rs.wasNull() ? null : reviewedBy);

        Timestamp reviewedAt
                = rs.getTimestamp("ReviewedAt");

        w.setReviewedAt(
                reviewedAt == null
                        ? null
                        : reviewedAt.toLocalDateTime());

        int paidBy = rs.getInt("PaidBy");

        w.setPaidBy(
                rs.wasNull() ? null : paidBy);

        Timestamp paidAt
                = rs.getTimestamp("PaidAt");

        w.setPaidAt(
                paidAt == null
                        ? null
                        : paidAt.toLocalDateTime());

        w.setPaymentReference(
                rs.getString("PaymentReference"));

        Timestamp createdAt
                = rs.getTimestamp("CreatedAt");

        w.setCreatedAt(
                createdAt == null
                        ? null
                        : createdAt.toLocalDateTime());

        return w;
    }

    // =====================================================
    // HELPER: VALIDATE STATUS FILTER
    // =====================================================
    private String normalizeStatus(String status) {

        String value
                = status == null
                        ? ""
                        : status.trim().toLowerCase(
                                java.util.Locale.ROOT);

        if (!value.isEmpty()
                && !"pending".equals(value)
                && !"approved".equals(value)
                && !"rejected".equals(value)
                && !"paid".equals(value)) {

            throw new IllegalArgumentException(
                    "Invalid withdrawal status");
        }

        return value;
    }

    // =====================================================
    // HELPER: CHECK ADMIN FROM DATABASE
    // =====================================================
    private boolean isAdminOrManager(
            Connection conn,
            int userId)
            throws SQLException {

        String sql
                = "SELECT Role FROM dbo.[User] "
                + "WHERE UserID = ?";

        try (PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                String role = rs.getString("Role");

                return "admin".equalsIgnoreCase(role)
                        || "manager".equalsIgnoreCase(role);
            }
        }
    }

    // =====================================================
    // HELPER: CHECK CUSTOMER FROM DATABASE
    // =====================================================
    private boolean isCustomer(
            Connection conn,
            int userId)
            throws SQLException {

        String sql
                = "SELECT Role FROM dbo.[User] "
                + "WHERE UserID = ?";

        try (PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        && "customer".equalsIgnoreCase(
                                rs.getString("Role"));
            }
        }
    }

    // =====================================================
    // HELPER: ROLLBACK
    // =====================================================
    private void rollback(
            Connection conn,
            Exception original) {

        try {
            conn.rollback();

        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
