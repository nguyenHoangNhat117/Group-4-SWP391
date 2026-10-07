package dao;

import db.DBContext;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDateTime;

import java.util.logging.Level;
import java.util.logging.Logger;

import model.User;

public class UserDAO extends DBContext {

    /*
     * =========================================================
     * LOGIN RESULT CONSTANTS
     * =========================================================
     */

    public static final String LOGIN_SUCCESS =
            "success";

    public static final String LOGIN_INVALID =
            "invalid";

    public static final String LOGIN_LOCKED =
            "locked";

    public static final String LOGIN_INACTIVE =
            "inactive";

    /*
     * =========================================================
     * GET USERNAME BY ID
     * =========================================================
     */
    public String getUsernameById(
            int id) {

        String sql =
                "SELECT Username "
                + "FROM [User] "
                + "WHERE UserID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    id
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getString(
                            "Username"
                    );
                }
            }

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);
        }

        return null;
    }

    /*
     * =========================================================
     * LOGIN
     *
     * Important:
     * AccountStatus must be active.
     *
     * Returning null means:
     * - invalid username/password
     * - locked/inactive account
     *
     * LoginServlet can use authenticate()
     * below when it needs the exact reason.
     * =========================================================
     */
    public User login(
            String username,
            String password) {

        if (username == null
                || password == null) {

            return null;
        }

        String hashedPassword =
                hashMd5(password);

        String sql =
                "SELECT "
                + "UserID, "
                + "Username, "
                + "[Password], "
                + "[Role], "
                + "DisplayName, "
                + "AccountStatus, "
                + "CreatedAt "
                + "FROM [User] "
                + "WHERE Username = ? "
                + "AND [Password] = ? "
                + "AND AccountStatus = 'active'";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    username.trim()
            );

            ps.setString(
                    2,
                    hashedPassword
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    return mapUser(rs);
                }
            }

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);
        }

        return null;
    }

    /*
     * =========================================================
     * AUTHENTICATE WITH STATUS RESULT
     *
     * This version lets LoginServlet distinguish:
     *
     * invalid credentials
     * locked
     * inactive
     * success
     * =========================================================
     */
    public AuthenticationResult authenticate(
            String username,
            String password) {

        if (username == null
                || username.trim().isEmpty()
                || password == null
                || password.isEmpty()) {

            return new AuthenticationResult(
                    LOGIN_INVALID,
                    null
            );
        }

        String hashedPassword =
                hashMd5(password);

        String sql =
                "SELECT "
                + "UserID, "
                + "Username, "
                + "[Password], "
                + "[Role], "
                + "DisplayName, "
                + "AccountStatus, "
                + "CreatedAt "
                + "FROM [User] "
                + "WHERE Username = ? "
                + "AND [Password] = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    username.trim()
            );

            ps.setString(
                    2,
                    hashedPassword
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (!rs.next()) {

                    return new AuthenticationResult(
                            LOGIN_INVALID,
                            null
                    );
                }

                User user =
                        mapUser(rs);

                String status =
                        user.getAccountStatus();

                if ("locked".equalsIgnoreCase(
                        status)) {

                    return new AuthenticationResult(
                            LOGIN_LOCKED,
                            null
                    );
                }

                if ("inactive".equalsIgnoreCase(
                        status)) {

                    return new AuthenticationResult(
                            LOGIN_INACTIVE,
                            null
                    );
                }

                if (!"active".equalsIgnoreCase(
                        status)) {

                    return new AuthenticationResult(
                            LOGIN_INVALID,
                            null
                    );
                }

                return new AuthenticationResult(
                        LOGIN_SUCCESS,
                        user
                );
            }

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);

            return new AuthenticationResult(
                    LOGIN_INVALID,
                    null
            );
        }
    }

    /*
     * =========================================================
     * REGISTER USER + CUSTOMER
     *
     * Must be transactional.
     *
     * Database:
     * [User]
     *      ↓
     * Customer
     * =========================================================
     */
    public boolean register(
            String username,
            String password,
            String email) {

        if (username == null
                || username.trim().isEmpty()
                || password == null
                || password.isEmpty()
                || email == null
                || email.trim().isEmpty()) {

            return false;
        }

        /*
         * Database already has UNIQUE constraints,
         * but checking here gives cleaner behaviour.
         */
        if (isUsernameExists(username)
                || checkEmailExists(email)) {

            return false;
        }

        String userSql =
                "INSERT INTO [User] "
                + "(Username, [Password], [Role], "
                + "DisplayName, AccountStatus) "
                + "VALUES (?, ?, 'customer', ?, 'active')";

        String customerSql =
                "INSERT INTO Customer "
                + "(UserID, Email) "
                + "VALUES (?, ?)";

        Connection conn = null;

        try {

            conn = getConnection();

            conn.setAutoCommit(false);

            String hashedPassword =
                    hashMd5(password);

            int userId;

            /*
             * -----------------------------------------
             * Create User
             * -----------------------------------------
             */
            try (PreparedStatement ps =
                    conn.prepareStatement(
                            userSql,
                            Statement.RETURN_GENERATED_KEYS
                    )) {

                ps.setString(
                        1,
                        username.trim()
                );

                ps.setString(
                        2,
                        hashedPassword
                );

                ps.setString(
                        3,
                        username.trim()
                );

                if (ps.executeUpdate() != 1) {

                    conn.rollback();
                    return false;
                }

                try (ResultSet keys =
                        ps.getGeneratedKeys()) {

                    if (!keys.next()) {

                        conn.rollback();
                        return false;
                    }

                    userId =
                            keys.getInt(1);
                }
            }

            /*
             * -----------------------------------------
             * Create Customer
             * -----------------------------------------
             */
            try (PreparedStatement ps =
                    conn.prepareStatement(
                            customerSql)) {

                ps.setInt(
                        1,
                        userId
                );

                ps.setString(
                        2,
                        email.trim()
                );

                if (ps.executeUpdate() != 1) {

                    conn.rollback();
                    return false;
                }
            }

            conn.commit();

            return true;

        } catch (SQLException e) {

            if (conn != null) {

                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    Logger.getLogger(
                            UserDAO.class.getName()
                    ).log(
                            Level.SEVERE,
                            "Register rollback failed",
                            ex
                    );
                }
            }

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);

            return false;

        } finally {

            if (conn != null) {

                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }

                try {
                    conn.close();
                } catch (SQLException e) {

                    Logger.getLogger(
                            UserDAO.class.getName()
                    ).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    /*
     * =========================================================
     * CHECK EMAIL EXISTS
     * =========================================================
     */
    public boolean checkEmailExists(
            String email) {

        if (email == null
                || email.trim().isEmpty()) {

            return false;
        }

        String sql =
                "SELECT 1 "
                + "FROM Customer "
                + "WHERE Email = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    email.trim()
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);

            return false;
        }
    }

    /*
     * =========================================================
     * CHECK USERNAME EXISTS
     * =========================================================
     */
    public boolean isUsernameExists(
            String username) {

        if (username == null
                || username.trim().isEmpty()) {

            return false;
        }

        String sql =
                "SELECT 1 "
                + "FROM [User] "
                + "WHERE Username = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    username.trim()
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);

            return false;
        }
    }

    /*
     * =========================================================
     * GET USER BY ID
     *
     * Useful for Customer Management.
     * =========================================================
     */
    public User getUserById(
            int userId) {

        String sql =
                "SELECT "
                + "UserID, "
                + "Username, "
                + "[Password], "
                + "[Role], "
                + "DisplayName, "
                + "AccountStatus, "
                + "CreatedAt "
                + "FROM [User] "
                + "WHERE UserID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    userId
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    return mapUser(rs);
                }
            }

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);
        }

        return null;
    }

    /*
     * =========================================================
     * UPDATE ACCOUNT STATUS
     *
     * UC17 / Customer Management.
     * Staff/Admin can use this later.
     * =========================================================
     */
    public boolean updateAccountStatus(
            int userId,
            String accountStatus) {

        if (!isValidAccountStatus(
                accountStatus)) {

            return false;
        }

        String sql =
                "UPDATE [User] "
                + "SET AccountStatus = ? "
                + "WHERE UserID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    accountStatus
            );

            ps.setInt(
                    2,
                    userId
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);

            return false;
        }
    }

    /*
     * =========================================================
     * UPDATE DISPLAY NAME
     * =========================================================
     */
    public boolean updateDisplayName(
            int userId,
            String displayName) {

        if (displayName == null
                || displayName.trim().isEmpty()) {

            return false;
        }

        String sql =
                "UPDATE [User] "
                + "SET DisplayName = ? "
                + "WHERE UserID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    displayName.trim()
            );

            ps.setInt(
                    2,
                    userId
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);

            return false;
        }
    }

    /*
     * =========================================================
     * UPDATE PASSWORD BY EMAIL
     *
     * IMPORTANT:
     * Existing ForgotPassword flow already generates
     * the hash before calling this method.
     *
     * Therefore this method receives a HASH,
     * not raw password.
     * =========================================================
     */
    public int updatePasswordByEmail(
            String email,
            String newPasswordHash) {

        if (email == null
                || email.trim().isEmpty()
                || newPasswordHash == null
                || newPasswordHash.isEmpty()) {

            return 0;
        }

        String sql =
                "UPDATE [User] "
                + "SET [Password] = ? "
                + "WHERE UserID = ( "
                + "    SELECT UserID "
                + "    FROM Customer "
                + "    WHERE Email = ? "
                + ")";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    newPasswordHash
            );

            ps.setString(
                    2,
                    email.trim()
            );

            return ps.executeUpdate();

        } catch (SQLException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);

            return 0;
        }
    }

    /*
     * =========================================================
     * VALID ACCOUNT STATUS
     *
     * Must match HotelDB CK_User_AccountStatus.
     * =========================================================
     */
    private boolean isValidAccountStatus(
            String accountStatus) {

        if (accountStatus == null) {
            return false;
        }

        return accountStatus.equals("active")
                || accountStatus.equals("locked")
                || accountStatus.equals("inactive");
    }

    /*
     * =========================================================
     * MAP RESULTSET -> USER
     * =========================================================
     */
    private User mapUser(
            ResultSet rs)
            throws SQLException {

        User user =
                new User();

        user.setId(
                rs.getInt(
                        "UserID"
                )
        );

        user.setUsername(
                rs.getString(
                        "Username"
                )
        );

        user.setPassword(
                rs.getString(
                        "Password"
                )
        );

        user.setRole(
                rs.getString(
                        "Role"
                )
        );

        user.setDisplayName(
                rs.getString(
                        "DisplayName"
                )
        );

        user.setAccountStatus(
                rs.getString(
                        "AccountStatus"
                )
        );

        if (rs.getTimestamp(
                "CreatedAt") != null) {

            user.setCreatedAt(
                    rs.getTimestamp(
                            "CreatedAt"
                    ).toLocalDateTime()
            );
        }

        return user;
    }

    /*
     * =========================================================
     * PASSWORD HASH
     *
     * Current project/database already uses MD5 hashes.
     *
     * Keeping MD5 here avoids breaking the existing data.
     * For production, migrate to BCrypt/Argon2 separately.
     * =========================================================
     */
    public String hashMd5(
            String raw) {

        if (raw == null) {
            return "";
        }

        try {

            MessageDigest md =
                    MessageDigest.getInstance(
                            "MD5"
                    );

            byte[] message =
                    md.digest(
                            raw.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

            StringBuilder builder =
                    new StringBuilder();

            for (byte b : message) {

                builder.append(
                        String.format(
                                "%02x",
                                b & 0xff
                        )
                );
            }

            return builder.toString();

        } catch (NoSuchAlgorithmException e) {

            Logger.getLogger(
                    UserDAO.class.getName()
            ).log(Level.SEVERE, null, e);

            return "";
        }
    }

    /*
     * =========================================================
     * AUTHENTICATION RESULT DTO
     * =========================================================
     */
    public static class AuthenticationResult {

        private final String status;
        private final User user;

        public AuthenticationResult(
                String status,
                User user) {

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

            return LOGIN_SUCCESS.equals(
                    status
            );
        }
    }
}