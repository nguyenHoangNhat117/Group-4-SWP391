package dao;

import db.DBContext;
import model.User;
import util.PasswordUtil;
import java.sql.*;
import java.util.logging.*;

public class UserDAO extends DBContext {

    public static final String LOGIN_SUCCESS = "success", LOGIN_INVALID = "invalid",
            LOGIN_LOCKED = "locked", LOGIN_INACTIVE = "inactive", LOGIN_ERROR = "error";
    private static final Logger LOG = Logger.getLogger(UserDAO.class.getName());
    private static final String COLUMNS = "UserID, Username, [Password], [Role], DisplayName, AccountStatus, CreatedAt";
    // Use the same MD5 verification path for nonexistent usernames.
    private static final String DUMMY = PasswordUtil.hash("dummy-password-not-an-account");

    public User login(String username, String password) {
        return authenticate(username, password).getUser();
    }

    public AuthenticationResult authenticate(String username, String password) {
        if (username == null || password == null) {
            return new AuthenticationResult(LOGIN_INVALID, null);
        }
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT " + COLUMNS + " FROM [User] WHERE Username=?")) {
            ps.setString(1, username.trim());
            User user;
            try (ResultSet rs = ps.executeQuery()) {
                user = rs.next() ? mapUser(rs) : null;
            }
            if (!PasswordUtil.verify(password, user == null ? DUMMY : user.getPassword()) || user == null) {
                return new AuthenticationResult(LOGIN_INVALID, null);
            }
            if ("locked".equalsIgnoreCase(user.getAccountStatus())) {
                return new AuthenticationResult(LOGIN_LOCKED, null);
            }
            if ("inactive".equalsIgnoreCase(user.getAccountStatus())) {
                return new AuthenticationResult(LOGIN_INACTIVE, null);
            }
            if (!"active".equalsIgnoreCase(user.getAccountStatus())) {
                return new AuthenticationResult(LOGIN_INVALID, null);
            }
            return new AuthenticationResult(LOGIN_SUCCESS, user);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Authentication database error", e);
            return new AuthenticationResult(LOGIN_ERROR, null);
        }
    }

    public boolean register(String username, String password, String email) {
        if (username == null || email == null || password == null) {
            return false;
        }
        String hash = PasswordUtil.hash(password);
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO [User] (Username,[Password],[Role],DisplayName,AccountStatus) VALUES (?,?,'customer',?,'active')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username.trim());
                    ps.setString(2, hash);
                    ps.setString(3, username.trim());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("User not inserted");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Missing UserID");
                        }
                        userId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Customer (UserID,Email) VALUES (?,?)")) {
                    ps.setInt(1, userId);
                    ps.setString(2, email.trim());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Customer not inserted");
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Registration database error", e);
            return false;
        }
    }

    public boolean isUsernameExists(String username) {
        return exists("SELECT 1 FROM [User] WHERE Username=?", username);
    }

    public boolean checkEmailExists(String email) {
        return exists("SELECT 1 FROM Customer WHERE Email=?", email);
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
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT " + COLUMNS + " FROM [User] WHERE UserID=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database unavailable", e);
        }
    }

    /**
     * Recovery is currently for Customer accounts, whose email is stored in
     * Customer.
     */
    public User getUserByEmail(String email) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT u.* FROM [User] u JOIN Customer c ON c.UserID=u.UserID WHERE c.Email=? AND u.[Role]='customer' AND u.AccountStatus='active'")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database unavailable", e);
        }
    }

    /**
     * Optimistic check prevents a concurrent/replayed recovery from overwriting
     * a newer password.
     */
    public int resetPassword(int userId, String expectedHash, String newHash) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE [User] SET [Password]=? WHERE UserID=? AND [Password]=? AND AccountStatus='active' AND [Role]='customer'")) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.setString(3, expectedHash);
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Password reset database error", e);
            return 0;
        }
    }

    /**
     * Retained for compatibility; the recovery servlet uses resetPassword()
     * instead.
     */
    public int updatePasswordByEmail(String email, String newHash) {
        if (email == null || newHash == null) {
            return 0;
        }
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE [User] SET [Password]=? WHERE UserID=(SELECT UserID FROM Customer WHERE Email=?)")) {
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
        if (!"active".equals(status) && !"locked".equals(status) && !"inactive".equals(status)) {
            return false;
        }
        return update("UPDATE [User] SET AccountStatus=? WHERE UserID=? AND [Role]='customer'", status, id);
    }

    public boolean updateDisplayName(int id, String name) {
        return name != null && !name.trim().isEmpty() && update("UPDATE [User] SET DisplayName=? WHERE UserID=?", name.trim(), id);
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
