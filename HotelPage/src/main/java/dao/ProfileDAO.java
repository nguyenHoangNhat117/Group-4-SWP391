
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

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
