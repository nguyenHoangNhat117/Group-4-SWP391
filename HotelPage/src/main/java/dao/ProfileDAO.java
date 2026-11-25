package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileDAO extends DBContext {

    private static final Logger LOG
            = Logger.getLogger(ProfileDAO.class.getName());

    public enum UpdateResult {
        SUCCESS,
        EMAIL_EXISTS,
        PHONE_EXISTS,
        FAILED
    }

    public UpdateResult updateProfile(
            int userId,
            String displayName,
            String firstName,
            String lastName,
            String email,
            String phone,
            String country,
            String city,
            String street) {

        String userSql
                = "UPDATE [User] SET DisplayName = ? "
                + "WHERE UserID = ? AND [Role] = 'customer'";

        String customerSql
                = "UPDATE Customer SET "
                + "FirstName = ?, "
                + "LastName = ?, "
                + "Email = ?, "
                + "Phone = ?, "
                + "Country = ?, "
                + "City = ?, "
                + "Street = ? "
                + "WHERE UserID = ?";

        try (Connection conn = getConnection()) {

            conn.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);

            try {
                if (!isEmailAvailable(conn, userId, email)) {
                    conn.rollback();
                    return UpdateResult.EMAIL_EXISTS;
                }

                if (!isPhoneAvailable(conn, userId, phone)) {
                    conn.rollback();
                    return UpdateResult.PHONE_EXISTS;
                }

                try (PreparedStatement ps
                        = conn.prepareStatement(userSql)) {

                    ps.setString(1, displayName);
                    ps.setInt(2, userId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return UpdateResult.FAILED;
                    }
                }

                try (PreparedStatement ps
                        = conn.prepareStatement(customerSql)) {

                    ps.setString(1, emptyToNull(firstName));
                    ps.setString(2, emptyToNull(lastName));
                    ps.setString(3, emptyToNull(email));
                    ps.setString(4, emptyToNull(phone));
                    ps.setString(5, emptyToNull(country));
                    ps.setString(6, emptyToNull(city));
                    ps.setString(7, emptyToNull(street));
                    ps.setInt(8, userId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return UpdateResult.FAILED;
                    }
                }

                conn.commit();
                return UpdateResult.SUCCESS;

            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOG.log(Level.SEVERE,
                            "Profile rollback failed", rollbackEx);
                }

                LOG.log(Level.SEVERE,
                        "Profile update failed", ex);
                return UpdateResult.FAILED;
            }

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE,
                    "Profile database connection failed", ex);
            return UpdateResult.FAILED;
        }
    }

    public model.Customer getStaffProfile(int userId)
            throws SQLException {

        String sql = "SELECT FirstName, LastName, Email, Phone, "
                + "Country, City, Street "
                + "FROM AccountContact WHERE UserID = ?";

        model.Customer profile = new model.Customer();

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    profile.setFirstName(rs.getString("FirstName"));
                    profile.setLastName(rs.getString("LastName"));
                    profile.setEmail(rs.getString("Email"));
                    profile.setPhone(rs.getString("Phone"));
                    profile.setCountry(rs.getString("Country"));
                    profile.setCity(rs.getString("City"));
                    profile.setStreet(rs.getString("Street"));
                }

                return profile;
            }
        }
    }

    public UpdateResult updateStaffProfile(
            int userId,
            String displayName,
            String firstName,
            String lastName,
            String email,
            String phone,
            String country,
            String city,
            String street) {

        try (Connection conn = getConnection()) {

            conn.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);

            try {

                // Check duplicate email across Customer and AccountContact
                if (!isEmailAvailable(conn, userId, email)) {
                    conn.rollback();
                    return UpdateResult.EMAIL_EXISTS;
                }

                if (!isPhoneAvailable(conn, userId, phone)) {
                    conn.rollback();
                    return UpdateResult.PHONE_EXISTS;
                }

                // Update User
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE [User] SET DisplayName=? "
                        + "WHERE UserID=? "
                        + "AND Role IN ('admin','staff','manager')")) {

                    ps.setString(1, displayName);
                    ps.setInt(2, userId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return UpdateResult.FAILED;
                    }
                }

                // Create AccountContact if it does not exist
                try (PreparedStatement ps = conn.prepareStatement(
                        "IF NOT EXISTS ("
                        + "SELECT 1 FROM AccountContact "
                        + "WITH (UPDLOCK,HOLDLOCK) WHERE UserID=?"
                        + ") "
                        + "INSERT INTO AccountContact(UserID) VALUES (?)")) {

                    ps.setInt(1, userId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }

                // Update AccountContact
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE AccountContact SET "
                        + "FirstName=?, LastName=?, Email=?, "
                        + "Phone=?, Country=?, City=?, Street=? "
                        + "WHERE UserID=?")) {

                    String[] values = {
                        firstName, lastName, email,
                        phone, country, city, street
                    };

                    for (int i = 0; i < values.length; i++) {
                        ps.setString(i + 1, emptyToNull(values[i]));
                    }

                    ps.setInt(8, userId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return UpdateResult.FAILED;
                    }
                }

                conn.commit();
                return UpdateResult.SUCCESS;

            } catch (SQLException ex) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOG.log(Level.SEVERE,
                            "Staff profile rollback failed", rollbackEx);
                }

                LOG.log(Level.SEVERE,
                        "Staff profile update failed", ex);
                return UpdateResult.FAILED;
            }

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE,
                    "Staff profile database unavailable", ex);
            return UpdateResult.FAILED;
        }
    }

    private boolean isEmailAvailable(
            Connection conn,
            int userId,
            String email) throws SQLException {

        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        String customerSql
                = "SELECT COUNT(*) "
                + "FROM Customer WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE Email IS NOT NULL "
                + "AND LOWER(LTRIM(RTRIM(Email))) = LOWER(?) "
                + "AND (UserID IS NULL OR UserID <> ?)";

        String staffSql
                = "SELECT COUNT(*) "
                + "FROM AccountContact WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE Email IS NOT NULL "
                + "AND LOWER(LTRIM(RTRIM(Email))) = LOWER(?) "
                + "AND UserID <> ?";

        try (PreparedStatement ps
                = conn.prepareStatement(customerSql)) {

            ps.setString(1, email.trim());
            ps.setInt(2, userId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
        }

        try (PreparedStatement ps
                = conn.prepareStatement(staffSql)) {

            ps.setString(1, email.trim());
            ps.setInt(2, userId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isPhoneAvailable(
            Connection conn,
            int userId,
            String phone) throws SQLException {

        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }

        String normalizedPhone = phone.trim();

        // Allow the user to keep their own existing phone number.
        String ownPhoneSql
                = "SELECT CASE WHEN "
                + "EXISTS (SELECT 1 FROM Customer "
                + "WHERE UserID = ? "
                + "AND LTRIM(RTRIM(Phone)) = ?) "
                + "OR EXISTS (SELECT 1 FROM AccountContact "
                + "WHERE UserID = ? "
                + "AND LTRIM(RTRIM(Phone)) = ?) "
                + "THEN 1 ELSE 0 END";

        try (PreparedStatement ps
                = conn.prepareStatement(ownPhoneSql)) {

            ps.setInt(1, userId);
            ps.setString(2, normalizedPhone);
            ps.setInt(3, userId);
            ps.setString(4, normalizedPhone);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 1) {
                    return true;
                }
            }
        }

        // Check other customers
        String customerSql
                = "SELECT COUNT(*) "
                + "FROM Customer WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE Phone IS NOT NULL "
                + "AND LTRIM(RTRIM(Phone)) = ? "
                + "AND (UserID IS NULL OR UserID <> ?)";

        // Check other staff, admins and managers
        String staffSql
                = "SELECT COUNT(*) "
                + "FROM AccountContact WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE Phone IS NOT NULL "
                + "AND LTRIM(RTRIM(Phone)) = ? "
                + "AND UserID <> ?";

        try (PreparedStatement ps
                = conn.prepareStatement(customerSql)) {

            ps.setString(1, normalizedPhone);
            ps.setInt(2, userId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
        }

        try (PreparedStatement ps
                = conn.prepareStatement(staffSql)) {

            ps.setString(1, normalizedPhone);
            ps.setInt(2, userId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
