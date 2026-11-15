
package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileDAO extends DBContext {

    private static final Logger LOG =
            Logger.getLogger(ProfileDAO.class.getName());

    public boolean updateProfile(
            int userId,
            String displayName,
            String firstName,
            String lastName,
            String email,
            String phone,
            String country,
            String city,
            String street) {

        String userSql =
                "UPDATE [User] SET DisplayName = ? "
                + "WHERE UserID = ? AND [Role] = 'customer'";

        String customerSql =
                "UPDATE Customer SET "
                + "FirstName = ?, "
                + "LastName = ?, "
                + "Email = ?, "
                + "Phone = ?, "
                + "Country = ?, "
                + "City = ?, "
                + "Street = ? "
                + "WHERE UserID = ?";

        try (Connection conn = getConnection()) {

            conn.setAutoCommit(false);

            try {
                // Update User
                try (PreparedStatement ps =
                        conn.prepareStatement(userSql)) {

                    ps.setString(1, displayName);
                    ps.setInt(2, userId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                // Update Customer
                try (PreparedStatement ps =
                        conn.prepareStatement(customerSql)) {

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
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOG.log(Level.SEVERE,
                            "Profile rollback failed",
                            rollbackEx);
                }

                LOG.log(Level.SEVERE,
                        "Profile update failed", ex);
                return false;
            }

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE,
                    "Profile database connection failed", ex);
            return false;
        }
    }


    public model.Customer getStaffProfile(int userId) throws SQLException {
        model.Customer profile = new model.Customer();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT FirstName,LastName,Email,Phone,Country,City,Street FROM AccountContact WHERE UserID=?")) {
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
            }
        }
        return profile;
    }

    public boolean updateStaffProfile(int userId, String displayName, String firstName, String lastName,
            String email, String phone, String country, String city, String street) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE [User] SET DisplayName=? WHERE UserID=? AND Role IN ('admin','staff','manager')")) {
                    ps.setString(1, displayName);
                    ps.setInt(2, userId);
                    if (ps.executeUpdate() != 1) { conn.rollback(); return false; }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "IF NOT EXISTS (SELECT 1 FROM AccountContact WITH (UPDLOCK,HOLDLOCK) WHERE UserID=?) "
                        + "INSERT INTO AccountContact(UserID) VALUES (?)")) {
                    ps.setInt(1, userId); ps.setInt(2, userId); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE AccountContact SET FirstName=?,LastName=?,Email=?,Phone=?,Country=?,City=?,Street=? WHERE UserID=?")) {
                    String[] values = {firstName,lastName,email,phone,country,city,street};
                    for (int i=0;i<values.length;i++) ps.setString(i+1, emptyToNull(values[i]));
                    ps.setInt(8,userId);
                    if (ps.executeUpdate()!=1) { conn.rollback(); return false; }
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                LOG.log(Level.WARNING, "Staff profile update failed", ex);
                return false;
            }
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Staff profile unavailable", ex);
            return false;
        }
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
