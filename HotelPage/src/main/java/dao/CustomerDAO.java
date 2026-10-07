package dao;

import db.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import model.Customer;
import model.User;

public class CustomerDAO extends DBContext {

    /*
     * =========================================================
     * 1. GET ALL
     *
     * Kept for compatibility with old source.
     * =========================================================
     */
    public List<Customer> getAll() {

        List<Customer> customers =
                new ArrayList<>();

        String sql =
                "SELECT "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.City, "
                + "c.Street, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.[Password], "
                + "u.[Role], "
                + "u.DisplayName, "
                + "u.AccountStatus, "
                + "u.CreatedAt "
                + "FROM Customer c "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "ORDER BY c.CustomerID DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                customers.add(
                        mapCustomer(rs)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    /*
     * =========================================================
     * 2. GET CUSTOMER BY USER ID
     *
     * Used by Login/Profile.
     * =========================================================
     */
    public Customer getCustomerByUserID(
            int userID) {

        String sql =
                "SELECT "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.City, "
                + "c.Street, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.[Password], "
                + "u.[Role], "
                + "u.DisplayName, "
                + "u.AccountStatus, "
                + "u.CreatedAt "
                + "FROM Customer c "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "WHERE c.UserID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    userID
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    return mapCustomer(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * =========================================================
     * 3. GET CUSTOMER BY CUSTOMER ID
     *
     * Used by Staff/Admin Customer Management.
     * =========================================================
     */
    public Customer getCustomerById(
            int customerId) {

        String sql =
                "SELECT "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.City, "
                + "c.Street, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.[Password], "
                + "u.[Role], "
                + "u.DisplayName, "
                + "u.AccountStatus, "
                + "u.CreatedAt "
                + "FROM Customer c "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "WHERE c.CustomerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    customerId
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    return mapCustomer(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * =========================================================
     * 4. SEARCH + PAGINATION
     *
     * Search supports:
     *
     * Customer ID
     * First/Last name
     * Email
     * Phone
     * Username
     * DisplayName
     * =========================================================
     */
    public List<Customer> searchCustomers(
            String keyword,
            String accountStatus,
            int page,
            int pageSize) {

        List<Customer> customers =
                new ArrayList<>();

        if (page < 1) {
            page = 1;
        }

        if (pageSize <= 0) {
            pageSize = 10;
        }

        StringBuilder sql =
                new StringBuilder();

        sql.append(
                "SELECT "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.City, "
                + "c.Street, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.[Password], "
                + "u.[Role], "
                + "u.DisplayName, "
                + "u.AccountStatus, "
                + "u.CreatedAt "
                + "FROM Customer c "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "WHERE 1 = 1 "
        );

        boolean hasKeyword =
                keyword != null
                && !keyword.trim().isEmpty();

        boolean hasStatus =
                isValidAccountStatus(
                        accountStatus
                );

        if (hasKeyword) {

            sql.append(
                    "AND ( "
                    + "CAST(c.CustomerID AS VARCHAR(20)) LIKE ? "
                    + "OR c.FirstName LIKE ? "
                    + "OR c.LastName LIKE ? "
                    + "OR CONCAT("
                    + "COALESCE(c.FirstName, ''), ' ', "
                    + "COALESCE(c.LastName, '')"
                    + ") LIKE ? "
                    + "OR c.Email LIKE ? "
                    + "OR c.Phone LIKE ? "
                    + "OR u.Username LIKE ? "
                    + "OR u.DisplayName LIKE ? "
                    + ") "
            );
        }

        if (hasStatus) {

            sql.append(
                    "AND u.AccountStatus = ? "
            );
        }

        sql.append(
                "ORDER BY c.CustomerID DESC "
                + "OFFSET ? ROWS "
                + "FETCH NEXT ? ROWS ONLY"
        );

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             sql.toString()
                     )) {

            int parameterIndex = 1;

            if (hasKeyword) {

                String search =
                        "%"
                        + keyword.trim()
                        + "%";

                for (int i = 0;
                        i < 8;
                        i++) {

                    ps.setString(
                            parameterIndex++,
                            search
                    );
                }
            }

            if (hasStatus) {

                ps.setString(
                        parameterIndex++,
                        accountStatus
                );
            }

            ps.setInt(
                    parameterIndex++,
                    (page - 1) * pageSize
            );

            ps.setInt(
                    parameterIndex,
                    pageSize
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                while (rs.next()) {

                    customers.add(
                            mapCustomer(rs)
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    /*
     * =========================================================
     * 5. COUNT SEARCH RESULT
     * =========================================================
     */
    public int countCustomers(
            String keyword,
            String accountStatus) {

        StringBuilder sql =
                new StringBuilder();

        sql.append(
                "SELECT COUNT(*) AS Total "
                + "FROM Customer c "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "WHERE 1 = 1 "
        );

        boolean hasKeyword =
                keyword != null
                && !keyword.trim().isEmpty();

        boolean hasStatus =
                isValidAccountStatus(
                        accountStatus
                );

        if (hasKeyword) {

            sql.append(
                    "AND ( "
                    + "CAST(c.CustomerID AS VARCHAR(20)) LIKE ? "
                    + "OR c.FirstName LIKE ? "
                    + "OR c.LastName LIKE ? "
                    + "OR CONCAT("
                    + "COALESCE(c.FirstName, ''), ' ', "
                    + "COALESCE(c.LastName, '')"
                    + ") LIKE ? "
                    + "OR c.Email LIKE ? "
                    + "OR c.Phone LIKE ? "
                    + "OR u.Username LIKE ? "
                    + "OR u.DisplayName LIKE ? "
                    + ") "
            );
        }

        if (hasStatus) {

            sql.append(
                    "AND u.AccountStatus = ? "
            );
        }

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             sql.toString()
                     )) {

            int parameterIndex = 1;

            if (hasKeyword) {

                String search =
                        "%"
                        + keyword.trim()
                        + "%";

                for (int i = 0;
                        i < 8;
                        i++) {

                    ps.setString(
                            parameterIndex++,
                            search
                    );
                }
            }

            if (hasStatus) {

                ps.setString(
                        parameterIndex,
                        accountStatus
                );
            }

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(
                            "Total"
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /*
     * =========================================================
     * 6. UPDATE ACCOUNT STATUS
     *
     * Used by Staff/Admin Customer Management.
     * =========================================================
     */
    public boolean updateAccountStatus(
            int customerId,
            String accountStatus) {

        if (!isValidAccountStatus(
                accountStatus)) {

            return false;
        }

        String sql =
                "UPDATE u "
                + "SET u.AccountStatus = ? "
                + "FROM [User] u "
                + "JOIN Customer c "
                + "ON c.UserID = u.UserID "
                + "WHERE c.CustomerID = ? "
                + "AND u.[Role] = 'customer'";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    accountStatus
            );

            ps.setInt(
                    2,
                    customerId
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * 7. UPDATE CUSTOMER PROFILE
     *
     * Existing ProfileServlet compatibility.
     * =========================================================
     */
    public void updateCustomer(
            int userId,
            String firstName,
            String lastName,
            String email,
            String phone,
            String country,
            String city,
            String street) {

        String sql =
                "UPDATE Customer "
                + "SET "
                + "FirstName = ?, "
                + "LastName = ?, "
                + "Email = ?, "
                + "Phone = ?, "
                + "Country = ?, "
                + "City = ?, "
                + "Street = ? "
                + "WHERE UserID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    emptyToNull(firstName)
            );

            ps.setString(
                    2,
                    emptyToNull(lastName)
            );

            ps.setString(
                    3,
                    email == null
                            ? null
                            : email.trim()
            );

            ps.setString(
                    4,
                    emptyToNull(phone)
            );

            ps.setString(
                    5,
                    emptyToNull(country)
            );

            ps.setString(
                    6,
                    emptyToNull(city)
            );

            ps.setString(
                    7,
                    emptyToNull(street)
            );

            ps.setInt(
                    8,
                    userId
            );

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * =========================================================
     * 8. UPDATE CUSTOMER INCLUDING AVATAR
     * =========================================================
     */
    public void updateCustomerWithAvatar(
            Customer customer) {

        if (customer == null
                || customer.getUser() == null) {

            return;
        }

        String sql =
                "UPDATE Customer "
                + "SET "
                + "FirstName = ?, "
                + "LastName = ?, "
                + "Email = ?, "
                + "Phone = ?, "
                + "Country = ?, "
                + "City = ?, "
                + "Street = ?, "
                + "Avatar = ? "
                + "WHERE UserID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    emptyToNull(
                            customer.getFirstName()
                    )
            );

            ps.setString(
                    2,
                    emptyToNull(
                            customer.getLastName()
                    )
            );

            ps.setString(
                    3,
                    customer.getEmail()
            );

            ps.setString(
                    4,
                    emptyToNull(
                            customer.getPhone()
                    )
            );

            ps.setString(
                    5,
                    emptyToNull(
                            customer.getCountry()
                    )
            );

            ps.setString(
                    6,
                    emptyToNull(
                            customer.getCity()
                    )
            );

            ps.setString(
                    7,
                    emptyToNull(
                            customer.getStreet()
                    )
            );

            ps.setString(
                    8,
                    emptyToNull(
                            customer.getAvatar()
                    )
            );

            ps.setInt(
                    9,
                    customer
                            .getUser()
                            .getId()
            );

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * =========================================================
     * 9. CHECK EMAIL BELONGS TO ANOTHER CUSTOMER
     *
     * Useful when Profile module is cleaned up later.
     * =========================================================
     */
    public boolean isEmailUsedByOtherCustomer(
            String email,
            int customerId) {

        if (email == null
                || email.trim().isEmpty()) {

            return false;
        }

        String sql =
                "SELECT 1 "
                + "FROM Customer "
                + "WHERE Email = ? "
                + "AND CustomerID <> ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    email.trim()
            );

            ps.setInt(
                    2,
                    customerId
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return true;
        }
    }

    /*
     * =========================================================
     * MAPPER
     * =========================================================
     */
    private Customer mapCustomer(
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

        Customer customer =
                new Customer();

        customer.setCustomerID(
                rs.getInt(
                        "CustomerID"
                )
        );

        customer.setFirstName(
                rs.getString(
                        "FirstName"
                )
        );

        customer.setLastName(
                rs.getString(
                        "LastName"
                )
        );

        customer.setEmail(
                rs.getString(
                        "Email"
                )
        );

        customer.setPhone(
                rs.getString(
                        "Phone"
                )
        );

        customer.setCountry(
                rs.getString(
                        "Country"
                )
        );

        customer.setCity(
                rs.getString(
                        "City"
                )
        );

        customer.setStreet(
                rs.getString(
                        "Street"
                )
        );

        customer.setAvatar(
                rs.getString(
                        "Avatar"
                )
        );

        customer.setUser(
                user
        );

        return customer;
    }

    private boolean isValidAccountStatus(
            String status) {

        return "active".equals(status)
                || "locked".equals(status)
                || "inactive".equals(status);
    }

    private String emptyToNull(
            String value) {

        if (value == null) {
            return null;
        }

        value = value.trim();

        return value.isEmpty()
                ? null
                : value;
    }
}