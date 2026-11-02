package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Updated by Nguyen Hoang Nhat
public class ChangePasswordDAO extends DBContext {

    public boolean changePassword(
            int userId,
            String expectedHash,
            String expectedRole,
            String newHash
    ) throws SQLException {

        /*
         * Compare the old hash and role during the update.
         * A concurrent password or role change makes this update fail.
         */
        String sql
                = "UPDATE [User] "
                + "SET [Password] = ? "
                + "WHERE UserID = ? "
                + "AND [Password] = ? "
                + "AND [Role] = ? "
                + "AND AccountStatus = 'active' "
                + "AND [Role] IN "
                + "('customer', 'staff', 'admin', 'manager')";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.setString(3, expectedHash);
            ps.setString(4, expectedRole);

            // One conditional UPDATE is atomic in SQL Server.
            return ps.executeUpdate() == 1;
        }
    }
}
