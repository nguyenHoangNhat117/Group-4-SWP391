package dao;

import db.DBContext;
import model.AdminAccount;
import java.sql.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdminUserDAO extends DBContext {

    private static final String HISTORY = "EXISTS(SELECT 1 FROM Booking b JOIN Customer bc ON bc.CustomerID=b.CustomerID WHERE bc.UserID=u.UserID)";
    private static final String FROM = " FROM [User] u LEFT JOIN AccountContact p ON p.UserID=u.UserID LEFT JOIN Customer c ON c.UserID=u.UserID ";
    private static final String FIELDS = "u.UserID,u.Username,u.Role,u.AccountStatus,u.DisplayName,u.CreatedAt,c.CustomerID,p.FirstName,p.LastName,p.Email,p.Phone,p.Country,p.City,p.Street,c.FirstName C_FirstName,c.LastName C_LastName,c.Email C_Email,c.Phone C_Phone,c.Country C_Country,c.City C_City,c.Street C_Street,sys.fn_varbintohexstr(u.AccountVersion) UV,sys.fn_varbintohexstr(p.ContactVersion) PV,CASE WHEN " + HISTORY + " THEN 1 ELSE 0 END HasHistory";

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
        a.firstName = contact(r, "FirstName");
        a.lastName = contact(r, "LastName");
        a.email = contact(r, "Email");
        a.phone = contact(r, "Phone");
        a.country = contact(r, "Country");
        a.city = contact(r, "City");
        a.street = contact(r, "Street");
        a.version = r.getString("UV") + ":" + nz(r.getString("PV")) + ":" + customerVersion(r);
        a.hasHistory = r.getBoolean("HasHistory");
        a.createdAt = r.getTimestamp("CreatedAt").toLocalDateTime();
        return a;
    }

    private String contact(ResultSet r, String field) throws SQLException {
        return nz(r.getString(r.getObject("CustomerID") == null ? field : "C_" + field));
    }

    // Customer chưa có rowversion: chụp đúng các trường UC18 có thể ghi đè.
    private String customerVersion(ResultSet r) throws SQLException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(ByteBuffer.allocate(4).putInt(r.getInt("CustomerID")).array());
            for (String field : Arrays.asList("FirstName", "LastName", "Email", "Phone", "Country", "City", "Street")) {
                String value = r.getString("C_" + field);
                digest.update((byte) (value == null ? 0 : 1));
                if (value != null) {
                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                    digest.update(ByteBuffer.allocate(4).putInt(bytes.length).array());
                    digest.update(bytes);
                }
            }
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
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
        return search(keyword, role, status, page, null);
    }

    public AdminAccount.Page search(String keyword, String role, String status, int page, java.time.LocalDate date) throws SQLException {
        AdminAccount.Page result = new AdminAccount.Page();
        List<Object> args = new ArrayList<>();
        String where = " WHERE u.Role IN ('customer','staff','manager','admin') ";
        if (!keyword.isEmpty()) {
            where += " AND (u.Username LIKE ? ESCAPE '~' OR (CASE WHEN c.CustomerID IS NULL THEN p.Email ELSE c.Email END) LIKE ? ESCAPE '~' OR u.DisplayName LIKE ? ESCAPE '~')";
            String k = "%" + keyword.replace("~", "~~").replace("%", "~%").replace("_", "~_").replace("[", "~[") + "%";
            Collections.addAll(args, k, k, k);
        }
        if (date != null) {
            where += " AND u.CreatedAt >= ? AND u.CreatedAt < ?";
            args.add(java.sql.Date.valueOf(date));
            args.add(java.sql.Date.valueOf(date.plusDays(1)));
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
        try (PreparedStatement s = c.prepareStatement("SELECT CustomerID FROM Customer WITH(UPDLOCK,HOLDLOCK) WHERE UserID=?")) {
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
        String[] queries = {
            "SELECT 1 FROM [User] WITH(UPDLOCK,HOLDLOCK) WHERE UserID<>? AND Username IN (?,?)",
            "SELECT 1 FROM AccountContact WITH(UPDLOCK,HOLDLOCK) WHERE UserID<>? AND Email IN (?,?)",
            "SELECT 1 FROM Customer WITH(UPDLOCK,HOLDLOCK) WHERE (UserID IS NULL OR UserID<>?) AND Email IN (?,?)"
        };
        for (int i = 0; i < queries.length; i++) {
            try (PreparedStatement s = c.prepareStatement(queries[i])) {
                bind(s, a.id, a.username, a.email);
                try (ResultSet r = s.executeQuery()) {
                    if (r.next()) {
                        throw new Rejected(i == 2
                                ? "Email/định danh đã thuộc hồ sơ khách khác. Tìm hồ sơ có sẵn; khách tại quầy cần xác minh qua liên kết kích hoạt, không tạo tài khoản trùng."
                                : "Username hoặc Email đã được gán cho tài khoản khác.");
                    }
                }
            }
        }
    }

    private void saveContact(Connection c, int id, AdminAccount a) throws SQLException {
        int changed = execute(c, "UPDATE AccountContact SET FirstName=?,LastName=?,Email=?,Phone=?,Country=?,City=?,Street=? WHERE UserID=?",
                a.firstName, a.lastName, a.email, a.phone, a.country, a.city, a.street, id);
        if (changed == 0) {
            changed = execute(c, "INSERT AccountContact(UserID,FirstName,LastName,Email,Phone,Country,City,Street) VALUES(?,?,?,?,?,?,?,?)",
                    id, a.firstName, a.lastName, a.email, a.phone, a.country, a.city, a.street);
        }
        if (changed != 1) throw new SQLException("Contact was not saved");
    }

    public int create(int actor, AdminAccount a, String hash) throws SQLException, Rejected {
        a.id = 0; // ID của form create không được dùng để bỏ qua kiểm tra trùng.
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
            saveContact(c, id, a);
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
            saveContact(c, a.id, a);
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
            if (!util.AuthUcPolicy.validEmail(a.email)) {
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
