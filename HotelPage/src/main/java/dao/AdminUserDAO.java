package dao;

import db.DBContext;
import model.AdminAccount;
import util.AdminAccountPolicy;
import util.PasswordUtil;
import java.sql.*;
import java.util.*;

/**
 * UC18: independent of Customer Management; writes are atomic and reauthorize
 * the actor.
 */
public class AdminUserDAO extends DBContext {

    public static class AccountException extends Exception {

        public AccountException(String message) {
            super(message);
        }
    }
    private static final String SELECT = "SELECT u.UserID,u.Username,u.DisplayName,u.[Role],u.AccountStatus,"
            + "u.Email,u.Phone,u.FirstName,u.LastName,u.Country,u.City,u.Street,u.CreatedAt,"
            + "u.AccountVersion AS VersionBytes,c.CustomerID,"
            + "(SELECT COUNT(*) FROM Booking b WHERE b.CustomerID=c.CustomerID) AS BookingCount "
            + "FROM [User] u LEFT JOIN Customer c ON c.UserID=u.UserID ";
    private static final String FILTER = " WHERE u.[Role] IN ('customer','staff','manager','admin') "
            + "AND (?='' OR CHARINDEX(?,u.Username)>0 OR CHARINDEX(?,u.DisplayName)>0 OR CHARINDEX(?,u.Email)>0) "
            + "AND (?='' OR u.[Role]=?) AND (?='' OR u.AccountStatus=?) ";

    public static final class Page {

        private final List<AdminAccount> accounts;
        private final int number, total, pages;

        Page(List<AdminAccount> accounts, int number, int total, int size) {
            this.accounts = accounts;
            this.number = number;
            this.total = total;
            this.pages = Math.max(1, (total + size - 1) / size);
        }

        public List<AdminAccount> getAccounts() {
            return accounts;
        }

        public int getNumber() {
            return number;
        }

        public int getTotal() {
            return total;
        }

        public int getPages() {
            return pages;
        }
    }

    public Page search(String keyword, String role, String status, int page) throws SQLException {
        final int size = 10;
        Object[] filters = {keyword, keyword, keyword, keyword, role, role, status, status};
        try (Connection c = getConnection()) {
            int total = scalar(c, "SELECT COUNT(*) FROM [User] u " + FILTER, filters);
            int pages = Math.max(1, (total + size - 1) / size);
            page = Math.max(1, Math.min(page, pages));
            List<Object> args = new ArrayList<>(Arrays.asList(filters));
            args.add((page - 1) * size);
            args.add(size);
            List<AdminAccount> rows = new ArrayList<>();
            try (PreparedStatement ps = prepare(c, SELECT + FILTER + "ORDER BY u.UserID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY", args.toArray()); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
            return new Page(rows, page, total, size);
        }
    }

    public AdminAccount find(int id) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = prepare(c, SELECT + "WHERE u.UserID=?", id); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? map(rs) : null;
        }
    }

    public List<Map<String, Object>> bookings(int userId) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = prepare(c,
                "SELECT TOP (10) b.BookingID,b.BookingDate,b.Status,b.TotalPrice FROM Booking b "
                + "JOIN Customer c ON c.CustomerID=b.CustomerID WHERE c.UserID=? ORDER BY b.BookingID DESC", userId); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt(1));
                row.put("date", rs.getTimestamp(2));
                row.put("status", rs.getString(3));
                row.put("total", rs.getBigDecimal(4));
                rows.add(row);
            }
        }
        return rows;
    }

    /**
     * Returns only after commit; callers may send the creation email
     * afterwards.
     */
    public int save(int actorId, String actorHash, AdminAccount a, String password, boolean create)
            throws SQLException, AccountException {
        if (!AdminAccountPolicy.validate(a, password, create).isEmpty()) {
            throw problem("Invalid account information.");
        }
        return mutate(actorId, actorHash, a, password, create, false);
    }

    public void delete(int actorId, String actorHash, int id, String version) throws SQLException, AccountException {
        AdminAccount a = new AdminAccount();
        a.setId(id);
        a.setVersion(version);
        mutate(actorId, actorHash, a, null, false, true);
    }

    private int mutate(int actorId, String actorHash, AdminAccount a, String password, boolean create, boolean delete)
            throws SQLException, AccountException {
        String hash = create ? PasswordUtil.hash(password) : null;
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                // SQL Server JDBC uses implicit transactions after setAutoCommit(false).
                // A stored procedure / SELECT of a variable does not start one. Touch a
                // table before requesting a transaction-owned application lock.
                scalar(c, "SELECT TOP (1) UserID FROM [User]");
                // Serialize admin mutations before taking row locks, preventing two admins
                // from jointly removing all active admins. Released automatically on commit/rollback.
                try (PreparedStatement ps = prepare(c, "DECLARE @r int; EXEC @r=sys.sp_getapplock "
                        + "@Resource=N'UC18.AdminAccounts',@LockMode='Exclusive',@LockOwner='Transaction',@LockTimeout=10000; SELECT @r;"); ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("sp_getapplock returned no result");
                    }
                    int lockCode = rs.getInt(1);
                    if (lockCode == -1) {
                        throw problem("Another account update is in progress. Please try again.");
                    }
                    if (lockCode == -2 || lockCode == -3) {
                        throw problem("Transaction interrupted (code " + lockCode + "). Please try again.");
                    }
                    if (lockCode < 0) {
                        throw new SQLException("Unable to acquire UC18 transaction lock; sp_getapplock code=" + lockCode);
                    }
                }
                if (scalar(c, "SELECT COUNT(*) FROM [User] WITH (UPDLOCK,HOLDLOCK) "
                        + "WHERE UserID=? AND [Role]='admin' AND AccountStatus='active' AND [Password]=?", actorId, actorHash) != 1) {
                    throw problem("Administrator access has changed. Please sign in again.");
                }
                String oldRole = null, oldStatus = null;
                if (!create) {
                    try (PreparedStatement ps = prepare(c, "SELECT [Role],AccountStatus,AccountVersion "
                            + "FROM [User] WITH (UPDLOCK,HOLDLOCK) WHERE UserID=?", a.getId()); ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw problem("Account no longer exists.");
                        }
                        oldRole = rs.getString(1);
                        oldStatus = rs.getString(2);
                        if (a.getVersion() == null || !a.getVersion().equalsIgnoreCase(versionHex(rs.getBytes(3)))) {
                            throw problem("Account changed elsewhere. Reload before saving.");
                        }
                    }
                    if (!AdminAccountPolicy.role(oldRole)) {
                        throw problem("This account role is not supported.");
                    }
                    if (AdminAccountPolicy.selfRestricted(actorId, a.getId(), a.getRole(), a.getStatus(), delete)) {
                        throw problem("You cannot disable, change the role of, or delete your own administrator account.");
                    }
                    if ("admin".equals(oldRole) && "active".equals(oldStatus)
                            && (delete || !"admin".equals(a.getRole()) || !"active".equals(a.getStatus()))
                            && scalar(c, "SELECT COUNT(*) FROM [User] WITH (UPDLOCK,HOLDLOCK) WHERE [Role]='admin' AND AccountStatus='active'") <= 1) {
                        throw problem("At least one active administrator is required.");
                    }
                }
                if (delete) {
                    if (scalar(c, "SELECT COUNT(*) FROM Booking b JOIN Customer x ON x.CustomerID=b.CustomerID WHERE x.UserID=?", a.getId()) > 0) {
                        throw problem("Account has booking history. Use locked or inactive instead.");
                    }
                    execute(c, "DELETE FROM Customer WHERE UserID=?", a.getId());
                    // Any additional FK (staff audit, payment, etc.) also prevents deletion.
                    if (execute(c, "DELETE FROM [User] WHERE UserID=?", a.getId()) != 1) {
                        throw new SQLException("Missing account during delete");
                    }
                } else {
                    int except = create ? 0 : a.getId();
                    if (scalar(c, "SELECT COUNT(*) FROM [User] WHERE UserID<>? AND (Email=? OR Username=?)", except, a.getEmail(), a.getEmail()) > 0) {
                        throw problem("Email is already used by another account.");
                    }
                    if (create && scalar(c, "SELECT COUNT(*) FROM [User] WHERE Username=? OR Email=?", a.getUsername(), a.getUsername()) > 0) {
                        throw problem("Username is already used by another account.");
                    }
                    if (create) {
                        try (PreparedStatement ps = c.prepareStatement("INSERT INTO [User] (Username,[Password],[Role],DisplayName,AccountStatus,"
                                + "Email,Phone,FirstName,LastName,Country,City,Street) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                            bind(ps, a.getUsername(), hash, a.getRole(), AdminAccountPolicy.displayName(a), a.getStatus(),
                                    a.getEmail(), a.getPhone(), a.getFirstName(), a.getLastName(), a.getCountry(), a.getCity(), a.getStreet());
                            if (ps.executeUpdate() != 1) {
                                throw new SQLException("Account not created");
                            }
                            try (ResultSet keys = ps.getGeneratedKeys()) {
                                if (!keys.next()) {
                                    throw new SQLException("Missing UserID");
                                }
                                a.setId(keys.getInt(1));
                            }
                        }
                    } else {
                        execute(c, "UPDATE [User] SET [Role]=?,DisplayName=?,AccountStatus=?,Email=?,Phone=?,FirstName=?,LastName=?,Country=?,City=?,Street=? WHERE UserID=?",
                                a.getRole(), AdminAccountPolicy.displayName(a), a.getStatus(), a.getEmail(), a.getPhone(), a.getFirstName(), a.getLastName(),
                                a.getCountry(), a.getCity(), a.getStreet(), a.getId());
                    }
                    boolean hasCustomer = scalar(c, "SELECT COUNT(*) FROM Customer WHERE UserID=?", a.getId()) > 0;
                    if (hasCustomer) {
                        // Never delete/recreate Customer on role changes: CustomerID/history are stable.
                        execute(c, "UPDATE Customer SET FirstName=?,LastName=?,Email=?,Phone=?,Country=?,City=?,Street=? WHERE UserID=?",
                                a.getFirstName(), a.getLastName(), a.getEmail(), a.getPhone(), a.getCountry(), a.getCity(), a.getStreet(), a.getId());
                    } else if ("customer".equals(a.getRole())) {
                        execute(c, "INSERT INTO Customer (FirstName,LastName,Email,Phone,Country,City,Street,UserID) VALUES (?,?,?,?,?,?,?,?)",
                                a.getFirstName(), a.getLastName(), a.getEmail(), a.getPhone(), a.getCountry(), a.getCity(), a.getStreet(), a.getId());
                    }
                }
                c.commit();
                return a.getId();
            } catch (SQLException | AccountException | RuntimeException e) {
                try {
                    c.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                if (e instanceof SQLException) {
                    int code = ((SQLException) e).getErrorCode();
                    if (code == 2601 || code == 2627) {
                        throw problem("Username or email is already in use.");
                    }
                    if (delete && code == 547) {
                        throw problem("Account has history and cannot be deleted. Use locked or inactive.");
                    }
                }
                throw e;
            }
        }
    }

    private static AccountException problem(String message) {
        return new AccountException(message);
    }

    private static PreparedStatement prepare(Connection c, String sql, Object... args) throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        try {
            bind(ps, args);
            return ps;
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
    }

    private static void bind(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    private static int scalar(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement ps = prepare(c, sql, args); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int execute(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement ps = prepare(c, sql, args)) {
            return ps.executeUpdate();
        }
    }

    /**
     * SQL Server rowversion is eight binary bytes, never text in a character
     * encoding.
     */
    private static String versionHex(byte[] bytes) throws SQLException {
        if (bytes == null || bytes.length != 8) {
            throw new SQLException("AccountVersion must contain exactly 8 bytes; check the UC18 schema");
        }
        final String digits = "0123456789abcdef";
        StringBuilder hex = new StringBuilder("0x");
        for (byte value : bytes) {
            int unsigned = value & 0xff;
            hex.append(digits.charAt(unsigned >>> 4)).append(digits.charAt(unsigned & 15));
        }
        return hex.toString();
    }

    private static AdminAccount map(ResultSet r) throws SQLException {
        AdminAccount a = new AdminAccount();
        a.setId(r.getInt("UserID"));
        a.setUsername(r.getString("Username"));
        a.setDisplayName(r.getString("DisplayName"));
        a.setRole(r.getString("Role"));
        a.setStatus(r.getString("AccountStatus"));
        a.setEmail(r.getString("Email"));
        a.setPhone(r.getString("Phone"));
        a.setFirstName(r.getString("FirstName"));
        a.setLastName(r.getString("LastName"));
        if (a.getFirstName() == null || a.getFirstName().isBlank()) {
            a.setFirstName(a.getDisplayName());
        }
        a.setCountry(r.getString("Country"));
        a.setCity(r.getString("City"));
        a.setStreet(r.getString("Street"));
        a.setVersion(versionHex(r.getBytes("VersionBytes")));
        int customer = r.getInt("CustomerID");
        a.setCustomerId(r.wasNull() ? null : customer);
        a.setBookingCount(r.getInt("BookingCount"));
        Timestamp created = r.getTimestamp("CreatedAt");
        a.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return a;
    }
}
