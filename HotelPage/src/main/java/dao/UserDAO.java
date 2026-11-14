package dao;

import db.DBContext;
import model.User;
import util.AuthRateLimiter;
import util.AuthSupport;
import util.PasswordUtil;

import java.sql.*;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO extends DBContext {

    public static final String LOGIN_SUCCESS = "success";
    public static final String LOGIN_INVALID = "invalid";
    public static final String LOGIN_LOCKED = "locked";
    public static final String LOGIN_INACTIVE = "inactive";
    public static final String LOGIN_ERROR = "error";
    public static final String LOGIN_RATE_LIMITED = "rate_limited";

    private static final Logger LOG =
            Logger.getLogger(UserDAO.class.getName());

    private static final String COLUMNS =
            "UserID, Username, [Password], [Role], "
            + "DisplayName, AccountStatus, CreatedAt";

    private static final String DUMMY =
            PasswordUtil.hash("dummy-password-not-an-account");

    public User login(String identifier, String password) {
        return authenticate(identifier, password).getUser();
    }

    public AuthenticationResult authenticate(
            String identifier,
            String password) {

        identifier = AuthSupport.text(identifier);

        if (identifier.isEmpty()
                || identifier.length() > 100
                || identifier.chars().anyMatch(Character::isISOControl)
                || password == null
                || password.isEmpty()
                || password.length() > 128) {
            return new AuthenticationResult(LOGIN_INVALID, null);
        }

        User user;

        try {
            user = findUserForLogin(identifier);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Authentication database error", ex);
            return new AuthenticationResult(LOGIN_ERROR, null);
        }

        // Username và email của cùng tài khoản dùng chung bộ đếm.
        String rateKey = user == null
                ? "unknown:" + identifier.toLowerCase(Locale.ROOT)
                : "user:" + user.getId();

        synchronized (AuthRateLimiter.class) {
            if (AuthRateLimiter.isLoginBlocked(rateKey)) {
                return new AuthenticationResult(
                        LOGIN_RATE_LIMITED, null);
            }

            boolean passwordMatches = PasswordUtil.verify(
                    password,
                    user == null ? DUMMY : user.getPassword());

            if (!passwordMatches || user == null) {
                return failedLogin(rateKey, LOGIN_INVALID);
            }

            if ("locked".equalsIgnoreCase(user.getAccountStatus())) {
                return failedLogin(rateKey, LOGIN_LOCKED);
            }

            if ("inactive".equalsIgnoreCase(user.getAccountStatus())) {
                return failedLogin(rateKey, LOGIN_INACTIVE);
            }

            if (!"active".equalsIgnoreCase(user.getAccountStatus())) {
                return failedLogin(rateKey, LOGIN_INVALID);
            }

            if (!supportedRole(user.getRole())) {
                return failedLogin(rateKey, LOGIN_INVALID);
            }

            AuthRateLimiter.clearLoginFailures(rateKey);
            return new AuthenticationResult(LOGIN_SUCCESS, user);
        }
    }

    private AuthenticationResult failedLogin(
            String rateKey,
            String status) {

        AuthRateLimiter.recordLoginFailure(rateKey);

        if (AuthRateLimiter.isLoginBlocked(rateKey)) {
            return new AuthenticationResult(
                    LOGIN_RATE_LIMITED, null);
        }

        return new AuthenticationResult(status, null);
    }

    private boolean supportedRole(String role) {
        return "customer".equalsIgnoreCase(role)
                || "staff".equalsIgnoreCase(role)
                || "admin".equalsIgnoreCase(role)
                || "manager".equalsIgnoreCase(role);
    }

    private User findUserForLogin(String identifier)
            throws SQLException {

        String sql = "SELECT u.* "
                + "FROM [User] u "
                + "LEFT JOIN AccountContact c ON c.UserID = u.UserID "
                + "WHERE u.Username = ? OR c.Email = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, identifier);
            ps.setString(2, identifier);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                User user = mapUser(rs);

                // Không chọn tùy ý khi dữ liệu cũ trùng định danh.
                if (rs.next()) {
                    throw new SQLException(
                            "Ambiguous username/email login identifier.");
                }

                return user;
            }
        }
    }

    public boolean register(
            String username,
            String password,
            String email,
            String firstName,
            String lastName,
            String phone) {

        username = AuthSupport.text(username);
        email = AuthSupport.text(email);
        firstName = AuthSupport.text(firstName);
        lastName = AuthSupport.text(lastName);
        phone = AuthSupport.text(phone);

        // Kiểm tra lại trước khi ghi database.
        if (!AuthSupport.validUsername(username)
                || !AuthSupport.validEmail(email)
                || !AuthSupport.validPhone(phone)
                || !AuthSupport.validPassword(password)
                || firstName.isBlank()
                || lastName.isBlank()
                || firstName.length() > 50
                || lastName.length() > 50
                || firstName.chars().anyMatch(Character::isISOControl)
                || lastName.chars().anyMatch(Character::isISOControl)) {
            return false;
        }

        String displayName = lastName + " " + firstName;

        if (displayName.length() > 100) {
            return false;
        }

        String hash = PasswordUtil.hash(password);

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Kiểm tra lại trong transaction và giữ khóa đến khi kết thúc.
                if (registrationIdentifiersTaken(
                        conn, username, email, true)) {
                    conn.rollback();
                    return false;
                }

                int userId;

                String userSql = "INSERT INTO [User] "
                        + "(Username, [Password], [Role], "
                        + "DisplayName, AccountStatus) "
                        + "VALUES (?, ?, 'customer', ?, 'active')";

                try (PreparedStatement ps = conn.prepareStatement(
                        userSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, hash);
                    ps.setString(3, displayName);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("User not inserted");
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Missing generated UserID");
                        }

                        userId = keys.getInt(1);
                    }
                }

                // Cả hai bảng nhận cùng thông tin và cùng UserID.
                String[] contactInserts = {
                    "INSERT INTO AccountContact "
                        + "(UserID, FirstName, LastName, Email, Phone) "
                        + "VALUES (?, ?, ?, ?, ?)",
                    "INSERT INTO Customer "
                        + "(UserID, FirstName, LastName, Email, Phone) "
                        + "VALUES (?, ?, ?, ?, ?)"
                };

                for (String sql : contactInserts) {
                    try (PreparedStatement ps =
                            conn.prepareStatement(sql)) {

                        ps.setInt(1, userId);
                        ps.setString(2, firstName);
                        ps.setString(3, lastName);
                        ps.setString(4, email);
                        ps.setString(5, phone);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Registration contact not inserted");
                        }
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }

                throw ex;
            }

        } catch (SQLException | RuntimeException ex) {
            LOG.log(Level.SEVERE, "Registration database error", ex);
            return false;
        }
    }

    /*
     * Chỉ dùng trong UC01.
     * Không thay đổi các hàm kiểm tra hiện có của module khác.
     */
    public boolean isRegistrationIdentifierTaken(String identifier) {
        String value = AuthSupport.text(identifier);

        if (value.isEmpty()) {
            return false;
        }

        try (Connection conn = getConnection()) {
            return registrationIdentifiersTaken(
                    conn, value, value, false);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Database unavailable", ex);
        }
    }

    private boolean registrationIdentifiersTaken(
            Connection conn,
            String username,
            String email,
            boolean lock) throws SQLException {

        String hint = lock ? " WITH (UPDLOCK, HOLDLOCK)" : "";

        /*
         * Kiểm tra chéo cả hai giá trị:
         * - Username mới với username/email đã tồn tại.
         * - Email mới với username/email đã tồn tại.
         *
         * Customer.Email được kiểm tra thêm để nhận diện dữ liệu cũ
         * chưa có AccountContact.
         */
        String[] checks = {
            "SELECT 1 FROM [User]" + hint
                + " WHERE Username IN (?, ?)",
            "SELECT 1 FROM AccountContact" + hint
                + " WHERE Email IN (?, ?)",
            "SELECT 1 FROM Customer" + hint
                + " WHERE Email IN (?, ?)"
        };

        for (String sql : checks) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isUsernameExists(String username) {
        return exists(
                "SELECT 1 FROM [User] WHERE Username = ?",
                username);
    }

    public boolean checkEmailExists(String email) {
        return exists(
                "SELECT 1 FROM AccountContact WHERE Email = ?",
                email);
    }

    private boolean exists(String sql, String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Database unavailable", ex);
        }
    }

    public User getUserById(int id) {
        String sql = "SELECT " + COLUMNS
                + " FROM [User] WHERE UserID = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Database unavailable", ex);
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT u.* "
                + "FROM [User] u "
                + "JOIN Customer c ON c.UserID = u.UserID "
                + "WHERE c.Email = ? "
                + "AND u.[Role] = 'customer' "
                + "AND u.AccountStatus = 'active'";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Database unavailable", ex);
        }
    }

    public int resetPassword(
            int userId,
            String expectedHash,
            String newHash) {

        String sql = "UPDATE [User] SET [Password] = ? "
                + "WHERE UserID = ? AND [Password] = ? "
                + "AND AccountStatus = 'active' "
                + "AND [Role] = 'customer'";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.setString(3, expectedHash);

            return ps.executeUpdate();

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Password reset database error", ex);
            return 0;
        }
    }

    // Giữ tương thích với các nơi gọi cũ.
    public int updatePasswordByEmail(String email, String newHash) {
        if (email == null || newHash == null) {
            return 0;
        }

        String sql = "UPDATE [User] SET [Password] = ? "
                + "WHERE UserID = "
                + "(SELECT UserID FROM Customer WHERE Email = ?)";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setString(2, email.trim());

            return ps.executeUpdate();

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Password update database error", ex);
            return 0;
        }
    }

    public String getUsernameById(int id) {
        User user = getUserById(id);
        return user == null ? null : user.getUsername();
    }

    public boolean updateAccountStatus(int id, String status) {
        if (!"active".equals(status)
                && !"locked".equals(status)
                && !"inactive".equals(status)) {
            return false;
        }

        return update(
                "UPDATE [User] SET AccountStatus = ? "
                + "WHERE UserID = ? AND [Role] = 'customer'",
                status,
                id);
    }

    public boolean updateDisplayName(int id, String name) {
        return name != null
                && !name.trim().isEmpty()
                && update(
                        "UPDATE [User] SET DisplayName = ? "
                        + "WHERE UserID = ?",
                        name.trim(),
                        id);
    }

    private boolean update(String sql, String value, int id) {
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);
            ps.setInt(2, id);

            return ps.executeUpdate() == 1;

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "User update database error", ex);
            return false;
        }
    }

    @Deprecated
    public String hashMd5(String password) {
        return PasswordUtil.legacyMd5(password);
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("UserID"));
        user.setUsername(rs.getString("Username"));
        user.setPassword(rs.getString("Password"));
        user.setRole(rs.getString("Role"));
        user.setDisplayName(rs.getString("DisplayName"));
        user.setAccountStatus(rs.getString("AccountStatus"));

        Timestamp created = rs.getTimestamp("CreatedAt");

        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }

        return user;
    }

    public static class AuthenticationResult {

        private final String status;
        private final User user;

        public AuthenticationResult(String status, User user) {
            this.status = status;
            this.user = user;
        }

        public String getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }

        public boolean isSuccess() {
            return LOGIN_SUCCESS.equals(status);
        }
    }
}