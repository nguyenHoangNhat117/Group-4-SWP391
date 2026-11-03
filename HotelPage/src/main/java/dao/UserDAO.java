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

// Updated by Nguyen Hoang Nhat
public class UserDAO extends DBContext {

    public static final String LOGIN_SUCCESS = "success";
    public static final String LOGIN_INVALID = "invalid";
    public static final String LOGIN_LOCKED = "locked";
    public static final String LOGIN_INACTIVE = "inactive";
    public static final String LOGIN_ERROR = "error";
    public static final String LOGIN_RATE_LIMITED = "rate_limited";

    private static final Logger LOG
            = Logger.getLogger(UserDAO.class.getName());

    private static final String COLUMNS
            = "UserID, Username, [Password], [Role], "
            + "DisplayName, AccountStatus, CreatedAt";

    // Use the same password verification path for unknown accounts.
    private static final String DUMMY
            = PasswordUtil.hash("dummy-password-not-an-account");

    public User login(String identifier, String password) {
        return authenticate(identifier, password).getUser();
    }

    public AuthenticationResult authenticate(
            String identifier,
            String password
    ) {
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
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Authentication database error", e);
            return new AuthenticationResult(LOGIN_ERROR, null);
        }

        /*
         * Username and email belonging to the same account use one key.
         * This prevents switching identifiers to bypass the failure limit.
         */
        String rateKey = user == null
                ? "unknown:" + identifier.toLowerCase(Locale.ROOT)
                : "user:" + user.getId();

        /*
         * Keep the block check, verification, and counter update atomic
         * for concurrent login requests in this JVM.
         * The database query has already finished before taking this lock.
         */
        synchronized (AuthRateLimiter.class) {
            if (AuthRateLimiter.isLoginBlocked(rateKey)) {
                return new AuthenticationResult(
                        LOGIN_RATE_LIMITED, null
                );
            }

            boolean passwordMatches = PasswordUtil.verify(
                    password,
                    user == null ? DUMMY : user.getPassword()
            );

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

            // Restrict login to the four roles in the current scope.
            if (!supportedRole(user.getRole())) {
                return failedLogin(rateKey, LOGIN_INVALID);
            }

            AuthRateLimiter.clearLoginFailures(rateKey);

            return new AuthenticationResult(LOGIN_SUCCESS, user);
        }
    }

    private AuthenticationResult failedLogin(
            String rateKey,
            String status
    ) {
        AuthRateLimiter.recordLoginFailure(rateKey);

        if (AuthRateLimiter.isLoginBlocked(rateKey)) {
            return new AuthenticationResult(LOGIN_RATE_LIMITED, null);
        }

        return new AuthenticationResult(status, null);
    }

    private boolean supportedRole(String role) {
        return "customer".equalsIgnoreCase(role)
                || "staff".equalsIgnoreCase(role)
                || "admin".equalsIgnoreCase(role)
                || "manager".equalsIgnoreCase(role);
    }

    private User findUserForLogin(String identifier) throws SQLException {
        /*
         * LEFT JOIN keeps employee accounts visible even when they do not
         * have a Customer profile.
         */
        String sql
                = "SELECT u.* "
                + "FROM [User] u "
                + "LEFT JOIN Customer c ON c.UserID = u.UserID "
                + "WHERE u.Username = ? OR u.Email = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, identifier);
            ps.setString(2, identifier);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                User user = mapUser(rs);

                /*
                 * Do not choose an arbitrary account when one user's
                 * username matches another customer's email.
                 */
                if (rs.next()) {
                    throw new SQLException(
                            "Ambiguous username/email login identifier."
                    );
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
            String phone
    ) {
        username = AuthSupport.text(username);
        email = AuthSupport.text(email);
        firstName = AuthSupport.text(firstName);
        lastName = AuthSupport.text(lastName);
        phone = AuthSupport.text(phone);

        // Validate again before writing to the database.
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

        // Keep the existing MD5 implementation.
        String hash = PasswordUtil.hash(password);

        String insertUser
                = "INSERT INTO [User] "
                + "(Username, [Password], [Role], "
                + "DisplayName, AccountStatus) "
                + "VALUES (?, ?, 'customer', ?, 'active')";

        String insertCustomer
                = "INSERT INTO Customer "
                + "(UserID, FirstName, LastName, Email, Phone) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                int userId;

                try (PreparedStatement ps = conn.prepareStatement(
                        insertUser,
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    ps.setString(1, username);
                    ps.setString(2, hash);
                    ps.setString(3, displayName);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("User not inserted");
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Missing generated UserID"
                            );
                        }

                        userId = keys.getInt(1);
                    }
                }

                // Both inserts must use the same transaction.
                try (PreparedStatement ps = conn.prepareStatement(
                        insertCustomer
                )) {
                    ps.setInt(1, userId);
                    ps.setString(2, firstName);
                    ps.setString(3, lastName);
                    ps.setString(4, email);
                    ps.setString(5, phone);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Customer not inserted");
                    }
                }

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

        } catch (SQLException | RuntimeException e) {
            LOG.log(Level.SEVERE, "Registration database error", e);
            return false;
        }
    }

    public boolean isUsernameExists(String username) {
        return exists(
                "SELECT 1 FROM [User] WHERE ? IN (Username, Email)",
                username
        );
    }

    public boolean checkEmailExists(String email) {
        return exists(
                "SELECT 1 FROM [User] WHERE ? IN (Username, Email)",
                email
        );
    }

    private boolean exists(String sql, String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Database unavailable", e);
        }
    }

    public User getUserById(int id) {
        String sql
                = "SELECT " + COLUMNS + " FROM [User] WHERE UserID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Database unavailable", e);
        }
    }

    /*
     * Preserve the existing recovery scope:
     * active Customer accounts with a registered email.
     */
    public User getUserByEmail(String email) {
        String sql
                = "SELECT u.* "
                + "FROM [User] u "
                + "JOIN Customer c ON c.UserID = u.UserID "
                + "WHERE c.Email = ? "
                + "AND u.[Role] = 'customer' "
                + "AND u.AccountStatus = 'active'";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Database unavailable", e);
        }
    }

    public int resetPassword(
            int userId,
            String expectedHash,
            String newHash
    ) {
        /*
         * Check the previous hash to avoid overwriting a password that
         * another request has already changed.
         */
        String sql
                = "UPDATE [User] SET [Password] = ? "
                + "WHERE UserID = ? AND [Password] = ? "
                + "AND AccountStatus = 'active' "
                + "AND [Role] = 'customer'";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.setString(3, expectedHash);

            return ps.executeUpdate();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Password reset database error", e);
            return 0;
        }
    }

    // Retained for compatibility with existing callers.
    public int updatePasswordByEmail(String email, String newHash) {
        if (email == null || newHash == null) {
            return 0;
        }

        String sql
                = "UPDATE [User] SET [Password] = ? "
                + "WHERE UserID = "
                + "(SELECT UserID FROM Customer WHERE Email = ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setString(2, email.trim());

            return ps.executeUpdate();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Password update database error", e);
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

        // Preserve the current Customer Management scope.
        return update(
                "UPDATE [User] SET AccountStatus = ? "
                + "WHERE UserID = ? AND [Role] = 'customer'",
                status,
                id
        );
    }

    public boolean updateDisplayName(int id, String name) {
        return name != null
                && !name.trim().isEmpty()
                && update(
                        "UPDATE [User] SET DisplayName = ? "
                        + "WHERE UserID = ?",
                        name.trim(),
                        id
                );
    }

    private boolean update(String sql, String value, int id) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);
            ps.setInt(2, id);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "User update database error", e);
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
