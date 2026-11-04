package dao;

import db.DBContext;
import model.AdminAccount;
import java.sql.*;
import java.util.*;

public class AdminUserDAO extends DBContext {

    private static final String HISTORY = "EXISTS(SELECT 1 FROM Booking b JOIN Customer bc ON bc.CustomerID=b.CustomerID WHERE bc.UserID=u.UserID)";
    private static final String FROM = " FROM [User] u LEFT JOIN AccountContact p ON p.UserID=u.UserID LEFT JOIN Customer c ON c.UserID=u.UserID ";
    private static final String FIELDS = "u.UserID,u.Username,u.Role,u.AccountStatus,u.DisplayName,u.CreatedAt,c.CustomerID,p.FirstName,p.LastName,p.Email,p.Phone,p.Country,p.City,p.Street,sys.fn_varbintohexstr(u.AccountVersion) UV,sys.fn_varbintohexstr(p.ContactVersion) PV,CASE WHEN " + HISTORY + " THEN 1 ELSE 0 END HasHistory";

    public static class Rejected extends Exception {

        public Rejected(String message) {
            super(message);
        }
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private AdminAccount map(ResultSet r) throws SQLException {
        AdminAccount a = new AdminAccount();
        a.id = r.getInt("UserID");
        a.customerId = r.getInt("CustomerID");
        a.username = r.getString("Username");
        a.role = r.getString("Role");
        a.status = r.getString("AccountStatus");
        a.displayName = r.getString("DisplayName");
        a.firstName = nz(r.getString("FirstName"));
        a.lastName = nz(r.getString("LastName"));
        a.email = nz(r.getString("Email"));
        a.phone = nz(r.getString("Phone"));
        a.country = nz(r.getString("Country"));
        a.city = nz(r.getString("City"));
        a.street = nz(r.getString("Street"));
        a.version = r.getString("UV") + ":" + nz(r.getString("PV"));
        a.hasHistory = r.getBoolean("HasHistory");
        a.createdAt = r.getTimestamp("CreatedAt").toLocalDateTime();
        return a;
    }

    private void bind(PreparedStatement s, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            s.setObject(i + 1, values[i]);
        }
    }

    private int execute(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql)) {
            bind(s, args);
            return s.executeUpdate();
        }
    }

    public AdminAccount find(int id) throws SQLException {
        try (Connection c = getConnection()) {
            return find(c, id);
        }
    }

    private AdminAccount find(Connection c, int id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT " + FIELDS + FROM + " WHERE u.UserID=?")) {
            bind(s, id);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public AdminAccount.Page search(String keyword, String role, String status, int page) throws SQLException {
        AdminAccount.Page result = new AdminAccount.Page();
        List<Object> args = new ArrayList<>();
        String where = " WHERE u.Role IN ('customer','staff','manager','admin') ";
        if (!keyword.isEmpty()) {
            where += " AND (u.Username LIKE ? ESCAPE '~' OR p.Email LIKE ? ESCAPE '~' OR u.DisplayName LIKE ? ESCAPE '~')";
            String k = "%" + keyword.replace("~", "~~").replace("%", "~%").replace("_", "~_").replace("[", "~[") + "%";
            Collections.addAll(args, k, k, k);
        }
        if (!role.isEmpty()) {
            where += " AND u.Role=?";
            args.add(role);
        }
        if (!status.isEmpty()) {
            where += " AND u.AccountStatus=?";
            args.add(status);
        }
        try (Connection c = getConnection()) {
            try (PreparedStatement s = c.prepareStatement("SELECT COUNT(*) Total,COALESCE(SUM(CASE WHEN u.AccountStatus='active' THEN 1 ELSE 0 END),0) A,COALESCE(SUM(CASE WHEN u.AccountStatus='locked' THEN 1 ELSE 0 END),0) L,COALESCE(SUM(CASE WHEN u.AccountStatus='inactive' THEN 1 ELSE 0 END),0) I" + FROM + where)) {
                bind(s, args.toArray());
                try (ResultSet r = s.executeQuery()) {
                    r.next();
                    result.total = r.getInt("Total");
                    result.active = r.getInt("A");
                    result.locked = r.getInt("L");
                    result.inactive = r.getInt("I");
                }
            }
            result.pages = Math.max(1, (result.total + 9) / 10);
            result.page = Math.max(1, Math.min(page, result.pages));
            try (PreparedStatement s = c.prepareStatement("SELECT " + FIELDS + FROM + where + " ORDER BY u.UserID DESC OFFSET ? ROWS FETCH NEXT 10 ROWS ONLY")) {
                List<Object> all = new ArrayList<>(args);
                all.add((result.page - 1) * 10);
                bind(s, all.toArray());
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        result.rows.add(map(r));
                    }
                }
            }
        }
        return result;
    }

    public List<AdminAccount.BookingRow> history(int userId, int page) throws SQLException {
        List<AdminAccount.BookingRow> rows = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement s = c.prepareStatement("SELECT b.BookingID,b.BookingDate,b.Status,b.TotalPrice FROM Booking b JOIN Customer c ON c.CustomerID=b.CustomerID WHERE c.UserID=? ORDER BY b.BookingID DESC OFFSET ? ROWS FETCH NEXT 11 ROWS ONLY")) {
            bind(s, userId, (page - 1) * 10);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    AdminAccount.BookingRow b = new AdminAccount.BookingRow();
                    b.id = r.getInt(1);
                    b.date = r.getTimestamp(2).toLocalDateTime();
                    b.status = r.getString(3);
                    b.total = r.getBigDecimal(4);
                    rows.add(b);
                }
            }
        }
        return rows;
    }

    @FunctionalInterface
    private interface Work<T> {

        T run(Connection c) throws SQLException, Rejected;
    }

    private <T> T transaction(Work<T> work) throws SQLException, Rejected {
        try (Connection c = getConnection()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            c.setAutoCommit(false);
            try {
                T value = work.run(c);
                c.commit();
                return value;
            } catch (SQLException | Rejected | RuntimeException e) {
                try {
                    c.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                throw e;
            }
        }
    }

    private void requireAdmin(Connection c, int actor) throws SQLException, Rejected {
        try (PreparedStatement s = c.prepareStatement("SELECT UserID FROM [User] WITH(UPDLOCK,HOLDLOCK) WHERE UserID=? AND Role='admin' AND AccountStatus='active'")) {
            bind(s, actor);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) {
                    throw new Rejected("Quyền Administrator đã thay đổi. Hãy đăng nhập lại.");
                }
            }
        }
    }

    private AdminAccount lock(Connection c, int id, String version) throws SQLException, Rejected {
        try (PreparedStatement s = c.prepareStatement("SELECT UserID FROM [User] WITH(UPDLOCK,HOLDLOCK) WHERE UserID=?")) {
            bind(s, id);
            s.executeQuery().close();
        }
        try (PreparedStatement s = c.prepareStatement("SELECT UserID FROM AccountContact WITH(UPDLOCK,HOLDLOCK) WHERE UserID=?")) {
            bind(s, id);
            s.executeQuery().close();
        }
        AdminAccount a = find(c, id);
        if (a == null || !AdminAccount.ROLES.contains(a.role)) {
            throw new Rejected("Tài khoản không tồn tại hoặc ngoài phạm vi UC18.");
        }
        if (version == null || !version.equals(a.version)) {
            throw new Rejected("Dữ liệu đã thay đổi. Đóng form, tải lại và thử lại.");
        }
        return a;
    }

    private void unique(Connection c, AdminAccount a) throws SQLException, Rejected {
        String sql = "SELECT 1 FROM [User] u LEFT JOIN AccountContact p ON p.UserID=u.UserID WHERE u.UserID<>? AND (u.Username=? OR p.Email=? OR u.Username=? OR p.Email=?)";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            bind(s, a.id, a.username, a.email, a.email, a.username);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    throw new Rejected("Username hoặc Email đã được gán cho tài khoản khác.");
                }
            }
        }
    }

    public int create(int actor, AdminAccount a, String hash) throws SQLException, Rejected {
        if (!a.validate(true).isEmpty()) {
            throw new Rejected("Thông tin tài khoản không hợp lệ.");
        }
        return transaction(c -> {
            requireAdmin(c, actor);
            unique(c, a);
            int id;
            try (PreparedStatement s = c.prepareStatement("SET NOCOUNT ON; DECLARE @ids TABLE(UserID INT); INSERT [User](Username,[Password],Role,DisplayName,AccountStatus) OUTPUT INSERTED.UserID INTO @ids VALUES(?,?,?,?,'active'); SELECT UserID FROM @ids;")) {
                bind(s, a.username, hash, a.role, a.lastName + " " + a.firstName);
                try (ResultSet r = s.executeQuery()) {
                    r.next();
                    id = r.getInt(1);
                }
            }
            execute(c, "INSERT AccountContact(UserID,FirstName,LastName,Email,Phone,Country,City,Street) VALUES(?,?,?,?,?,?,?,?)", id, a.firstName, a.lastName, a.email, a.phone, a.country, a.city, a.street);
            if ("customer".equals(a.role)) {
                ensureCustomer(c, id, a);
            }
            return id;
        });
    }

    private void ensureCustomer(Connection c, int id, AdminAccount a) throws SQLException {
        execute(c, "IF NOT EXISTS(SELECT 1 FROM Customer WHERE UserID=?) INSERT Customer(UserID,FirstName,LastName,Email,Phone,Country,City,Street) VALUES(?,?,?,?,?,?,?,?)", id, id, a.firstName, a.lastName, a.email, a.phone, a.country, a.city, a.street);
    }

    public void update(int actor, AdminAccount a) throws SQLException, Rejected {
        if (!a.validate(false).isEmpty()) {
            throw new Rejected("Thông tin tài khoản không hợp lệ.");
        }
        transaction(c -> {
            requireAdmin(c, actor);
            AdminAccount old = lock(c, a.id, a.version);
            a.username = old.username;
            if (actor == a.id && (!"admin".equals(a.role) || !"active".equals(a.status))) {
                throw new Rejected("Không thể tự khóa, vô hiệu hóa hoặc hạ quyền tài khoản đang đăng nhập.");
            }
            unique(c, a);
            execute(c, "UPDATE [User] SET DisplayName=?,Role=?,AccountStatus=? WHERE UserID=?", a.lastName + " " + a.firstName, a.role, a.status, a.id);
            execute(c, "UPDATE AccountContact SET FirstName=?,LastName=?,Email=?,Phone=?,Country=?,City=?,Street=? WHERE UserID=?", a.firstName, a.lastName, a.email, a.phone, a.country, a.city, a.street, a.id);
            if ("customer".equals(a.role)) {
                ensureCustomer(c, a.id, a);
            }
            // Preserve CustomerID and history even when the account leaves the customer role.
            execute(c, "UPDATE Customer SET FirstName=?,LastName=?,Email=?,Phone=?,Country=?,City=?,Street=? WHERE UserID=?", a.firstName, a.lastName, a.email, a.phone, a.country, a.city, a.street, a.id);
            return null;
        });
    }

    public void toggle(int actor, int id, String version) throws SQLException, Rejected {
        transaction(c -> {
            requireAdmin(c, actor);
            AdminAccount a = lock(c, id, version);
            if (actor == id) {
                throw new Rejected("Không thể tự khóa tài khoản đang đăng nhập.");
            }
            if ("inactive".equals(a.status)) {
                throw new Rejected("Tài khoản Inactive: dùng Edit để chọn trạng thái rõ ràng.");
            }
            execute(c, "UPDATE [User] SET AccountStatus=? WHERE UserID=?", "locked".equals(a.status) ? "active" : "locked", id);
            return null;
        });
    }

    public AdminAccount resetInitialPassword(int actor, int id, String version, String hash) throws SQLException, Rejected {
        return transaction(c -> {
            requireAdmin(c, actor);
            AdminAccount a = lock(c, id, version);
            if (actor == id) {
                throw new Rejected("Dùng Change Password cho tài khoản của bạn.");
            }
            if (a.email.isBlank()) {
                throw new Rejected("Cần cập nhật Email trước.");
            }
            execute(c, "UPDATE [User] SET [Password]=? WHERE UserID=?", hash, id);
            return a;
        });
    }

    public void delete(int actor, int id, String version) throws SQLException, Rejected {
        transaction(c -> {
            requireAdmin(c, actor);
            AdminAccount a = lock(c, id, version);
            if (actor == id) {
                throw new Rejected("Không thể tự xóa tài khoản đang đăng nhập.");
            }
            if (a.hasHistory) {
                throw new Rejected("Tài khoản có lịch sử giao dịch. Hãy chọn Locked hoặc Inactive.");
            }
            // SERIALIZABLE + FK checks prevent a booking inserted concurrently from being orphaned.
            execute(c, "DELETE Customer WHERE UserID=?", id);
            execute(c, "DELETE AccountContact WHERE UserID=?", id);
            execute(c, "DELETE [User] WHERE UserID=?", id);
            return null;
        });
    }
}
