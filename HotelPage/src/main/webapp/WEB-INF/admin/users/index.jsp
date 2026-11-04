<%-- 
    Document   : index
    Created on : Nov 4, 2026, 10:49:03 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="model.AdminAccount,model.User,util.AuthSupport,java.util.*,java.net.URLEncoder,java.nio.charset.StandardCharsets"%>
<%!
    private String h(Object x) {
        return AuthSupport.html(x);
    }

// Presentation-only translations. Controller/DAO decisions remain unchanged.
    private static final Map<String, String> UI_MESSAGES = new HashMap<>();

    static {
        UI_MESSAGES.put("Username: 3–50 ký tự, không chứa @ hoặc ký tự điều khiển.", "Username must contain 3–50 characters, without @ or control characters.");
        UI_MESSAGES.put("Email không hợp lệ hoặc vượt 100 ký tự.", "Enter a valid email address of up to 100 characters.");
        UI_MESSAGES.put("Số điện thoại cần 10–11 chữ số.", "Phone number must contain 10 or 11 digits.");
        UI_MESSAGES.put("Họ và tên bắt buộc, mỗi phần tối đa 50 ký tự, tổng tối đa 100.", "First and last names are required. Each allows up to 50 characters; the full name allows up to 100.");
        UI_MESSAGES.put("Role không hợp lệ.", "Please select a valid role.");
        UI_MESSAGES.put("Status không hợp lệ.", "Please select a valid account status.");
        UI_MESSAGES.put("Địa chỉ vượt giới hạn hoặc có ký tự điều khiển.", "Address fields exceed the allowed length or contain unsupported control characters.");
        UI_MESSAGES.put("Quyền Administrator đã thay đổi. Hãy đăng nhập lại.", "Your administrator access has changed. Please sign in again.");
        UI_MESSAGES.put("Tài khoản không tồn tại hoặc ngoài phạm vi UC18.", "This account was not found or is not available in User Management.");
        UI_MESSAGES.put("Dữ liệu đã thay đổi. Đóng form, tải lại và thử lại.", "This account has changed. Close the form, reload the page and try again.");
        UI_MESSAGES.put("Username hoặc Email đã được gán cho tài khoản khác.", "This username or email address is already assigned to another account.");
        UI_MESSAGES.put("Thông tin tài khoản không hợp lệ.", "Please review the account information and try again.");
        UI_MESSAGES.put("Không thể tự khóa, vô hiệu hóa hoặc hạ quyền tài khoản đang đăng nhập.", "You cannot lock, deactivate or demote your own signed-in account.");
        UI_MESSAGES.put("Không thể tự khóa tài khoản đang đăng nhập.", "You cannot lock your own signed-in account.");
        UI_MESSAGES.put("Tài khoản Inactive: dùng Edit để chọn trạng thái rõ ràng.", "This account is inactive. Use Edit to change its status.");
        UI_MESSAGES.put("Dùng Change Password cho tài khoản của bạn.", "Use Change Password to update your own password.");
        UI_MESSAGES.put("Cần cập nhật Email trước.", "Add an email address to this account first.");
        UI_MESSAGES.put("Không thể tự xóa tài khoản đang đăng nhập.", "You cannot delete your own signed-in account.");
        UI_MESSAGES.put("Tài khoản có lịch sử giao dịch. Hãy chọn Locked hoặc Inactive.", "This account has transaction history and cannot be deleted. Choose Locked or Inactive instead.");
        UI_MESSAGES.put("Đã lưu tài khoản và gửi email thông tin đăng nhập.", "Account saved. The sign-in credentials have been sent by email.");
        UI_MESSAGES.put("Đã lưu tài khoản nhưng chưa gửi được email. Kiểm tra SMTP/APP_BASE_URL, sau đó dùng nút gửi mật khẩu mới trong Inspect. Không tạo lại tài khoản.", "Account saved, but the email could not be sent. Check email delivery settings, then use Inspect > Send new password. Do not create the account again.");
        UI_MESSAGES.put("Đã lưu thay đổi.", "Changes saved successfully.");
        UI_MESSAGES.put("Đã xóa tài khoản chưa sử dụng.", "The unused account has been deleted successfully.");
        UI_MESSAGES.put("Tài khoản còn dữ liệu liên quan nên không thể xóa.", "This account has related records and cannot be deleted.");
        UI_MESSAGES.put("Có cập nhật đồng thời. Tải lại trang và thử lại.", "Another update is in progress. Reload the page and try again.");
        UI_MESSAGES.put("Không thể lưu thay đổi. Database đã rollback; hãy thử lại.", "Your changes could not be saved. Please try again.");
        UI_MESSAGES.put("Không thể tải danh sách tài khoản. Hãy thử lại.", "Unable to load user accounts. Please try again.");
    }

    private String english(Object value) {
        if (value == null) {
            return "";
        }
        String message = value.toString();
        return UI_MESSAGES.getOrDefault(message, message);
    }

    private String enc(String x) {
        return URLEncoder.encode(x == null ? "" : x, StandardCharsets.UTF_8);
    }

    private String icon(String name) {
        return "<svg class=\"ico\" aria-hidden=\"true\"><use href=\"#i-" + name + "\"/></svg>";
    }

    private String field(String label, String name, String value, String type, int max, boolean required, boolean readOnly, Map<String, String> errors) {
        String key = ("firstName".equals(name) || "lastName".equals(name)) ? "name" : name;
        String error = errors.get(key);
        return "<div class=\"col-md-6\"><label class=\"form-label\" for=\"f-" + name + "\">" + h(label) + (required ? " <span class=\"required\">*</span>" : "") + "</label><input class=\"form-control" + (error != null ? " is-invalid" : "") + "\" id=\"f-" + name + "\" name=\"" + name + "\" type=\"" + type + "\" value=\"" + h(value) + "\" maxlength=\"" + max + "\" " + (required ? "required " : "") + (readOnly ? "readonly " : "") + "><div class=\"invalid-feedback\">" + h(english(error)) + "</div></div>";
    }
%>
<%
    String ctx = request.getContextPath(), base = ctx + "/admin/users";
    String q = (String) request.getAttribute("q"), rf = (String) request.getAttribute("roleFilter"), sf = (String) request.getAttribute("statusFilter");
    String search = "q=" + enc(q) + "&role=" + enc(rf) + "&status=" + enc(sf);
    AdminAccount.Page data = (AdminAccount.Page) request.getAttribute("pageData");
    User actor = (User) session.getAttribute("loggedUser");
    String csrf = AuthSupport.token(session);
    String state = "<input type=\"hidden\" name=\"csrfToken\" value=\"" + h(csrf) + "\"><input type=\"hidden\" name=\"q\" value=\"" + h(q) + "\"><input type=\"hidden\" name=\"role\" value=\"" + h(rf) + "\"><input type=\"hidden\" name=\"status\" value=\"" + h(sf) + "\"><input type=\"hidden\" name=\"page\" value=\"" + data.page + "\">";
    AdminAccount form = (AdminAccount) request.getAttribute("form"), inspect = (AdminAccount) request.getAttribute("inspectAccount");
    boolean creating = "create".equals(request.getAttribute("formMode"));
    Map<String, String> errors = (Map<String, String>) request.getAttribute("errors");
    if (errors == null) {
        errors = Collections.emptyMap();
    }
    int aPct = data.total == 0 ? 0 : (int) Math.round(data.active * 100.0 / data.total), lPct = data.total == 0 ? 0 : (int) Math.round(data.locked * 100.0 / data.total);
%>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>User Management | Luxe Escape</title>
                <link rel="stylesheet" href="<%=ctx%>/assets/css/bootstrap.min.css"><link rel="stylesheet" href="<%=ctx%>/assets/css/admin-users.css?v=readable-en-v3"></head>
                        <body>
                            <svg class="sprite" xmlns="http://www.w3.org/2000/svg" aria-hidden="true"><defs>
                                    <symbol id="i-users" viewBox="0 0 24 24"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2m20 0v-2a4 4 0 0 0-3-3.87M16 3a4 4 0 0 1 0 8"/><circle cx="9" cy="7" r="4"/></symbol>
                                    <symbol id="i-customer" viewBox="0 0 24 24"><circle cx="12" cy="7" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></symbol>
                                    <symbol id="i-admin" viewBox="0 0 24 24"><path d="m12 3 8 3v6c0 5-8 9-8 9s-8-4-8-9V6z"/><path d="m8 12 3 3 5-6"/></symbol>
                                    <symbol id="i-manager" viewBox="0 0 24 24"><rect x="3" y="7" width="18" height="14" rx="2"/><path d="M8 7V3h8v4M3 12h18m-9 0v4"/></symbol>
                                    <symbol id="i-staff" viewBox="0 0 24 24"><path d="M3 18h18M5 18v-3a7 7 0 0 1 14 0v3M12 8V4m-2 0h4M2 21h20"/></symbol>
                                    <symbol id="i-search" viewBox="0 0 24 24"><circle cx="10" cy="10" r="6"/><path d="m15 15 6 6"/></symbol>
                                    <symbol id="i-plus" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></symbol>
                                    <symbol id="i-eye" viewBox="0 0 24 24"><path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></symbol>
                                    <symbol id="i-edit" viewBox="0 0 24 24"><path d="m16 3 5 5-12 12-6 1 1-6Zm-2 2 5 5"/></symbol>
                                    <symbol id="i-lock" viewBox="0 0 24 24"><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V6a4 4 0 0 1 8 0v4m-4 4v3"/></symbol>
                                    <symbol id="i-unlock" viewBox="0 0 24 24"><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V6a4 4 0 0 1 8 0m-4 8v3"/></symbol>
                                    <symbol id="i-trash" viewBox="0 0 24 24"><path d="M3 6h18M9 6V3h6v3M5 6l1 15h12l1-15M10 10v7m4-7v7"/></symbol>
                                    <symbol id="i-mail" viewBox="0 0 24 24"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 6 9 7 9-7"/></symbol>
                                    <symbol id="i-home" viewBox="0 0 24 24"><path d="m3 10 9-8 9 8v11h-6v-7H9v7H3Z"/></symbol>
                                    <symbol id="i-arrow" viewBox="0 0 24 24"><path d="m9 5 7 7-7 7"/></symbol>
                                    <symbol id="i-check" viewBox="0 0 24 24"><path d="m5 12 4 4L19 6"/></symbol>
                                </defs></svg>
                            <div class="admin-shell">
                                <aside class="side-rail"><a href="<%=ctx%>/" class="brand-mark" title="Luxe Escape">LE<span></span></a><nav aria-label="Admin navigation">
                                        <a href="<%=ctx%>/" title="Hotel home"><%=icon("home")%></a><a class="current" href="<%=base%>" title="User management" aria-current="page"><%=icon("users")%></a>
                                    </nav><span class="rail-caption">LUXE</span></aside>
                                <main class="workspace">
                                    <header class="page-heading"><div><div class="eyebrow">LUXE ESCAPE · ADMINISTRATION</div><h1>User management</h1></div><div class="admin-chip"><span class="avatar small"><%=h(actor.getDisplayName() == null ? "A" : actor.getDisplayName().substring(0, 1).toUpperCase(Locale.ROOT))%></span><div><strong><%=h(actor.getDisplayName())%></strong><small>Administrator</small></div></div></header>
                                    <% if (request.getAttribute("flash") != null) {%><div class="alert alert-info border-0 d-flex gap-2 align-items-center" role="status"><%=icon("check")%><span><%=h(english(request.getAttribute("flash")))%></span></div><% }%>
                                    <section class="metric-grid" aria-label="Filtered account statistics">
                                        <div class="metric"><span class="metric-icon violet"><%=icon("users")%></span><div><small>Total users</small><strong><%=data.total%></strong></div><div class="mini-donut" style="--chart:conic-gradient(#21a574 0% <%=aPct%>%,#e8a02e <%=aPct%>% <%=Math.min(100, aPct + lPct)%>%,#df6570 <%=Math.min(100, aPct + lPct)%>% 100%)" aria-label="Account status distribution"></div></div>
                                        <% String[] statuses = {"active", "locked", "inactive"};
    int[] amounts = {data.active, data.locked, data.inactive};
    for (int n = 0; n < 3; n++) {%><div class="metric"><span class="metric-dot <%=statuses[n]%>"></span><div><small><%=h(statuses[n].substring(0, 1).toUpperCase() + statuses[n].substring(1))%></small><strong><%=amounts[n]%></strong></div><div class="metric-track"><span class="<%=statuses[n]%>" style="width:<%=data.total == 0 ? 0 : amounts[n] * 100 / data.total%>%"></span></div></div><% }%>
                                    </section>
                                    <section class="accounts-panel">
                                        <form class="toolbar" method="get" action="<%=base%>"><div class="search-box"><%=icon("search")%><input type="search" name="q" value="<%=h(q)%>" maxlength="100" placeholder="Search username / email" aria-label="Search username or email"><button type="submit" class="search-go" title="Search" aria-label="Search"><%=icon("arrow")%></button></div>
                                            <select name="role" class="form-select" aria-label="Filter role"><option value="">All roles</option><%for (String r : AdminAccount.ROLES) {%><option value="<%=r%>" <%=r.equals(rf) ? "selected" : ""%>><%=r.substring(0, 1).toUpperCase() + r.substring(1)%></option><%}%></select>
                                            <select name="status" class="form-select" aria-label="Filter status"><option value="">All statuses</option><%for (String st : AdminAccount.STATUSES) {%><option value="<%=st%>" <%=st.equals(sf) ? "selected" : ""%>><%=st.substring(0, 1).toUpperCase() + st.substring(1)%></option><%}%></select>
                                            <button class="btn btn-filter" type="submit">Filter</button><a href="<%=base%>" class="reset-link" title="Reset filters">Reset</a>
                                            <a class="btn btn-luxe add-user" href="<%=h(base + "?" + search + "&page=" + data.page + "&mode=create")%>"><%=icon("plus")%> Add New User</a></form>
                                        <div class="table-responsive"><table class="table user-table align-middle"><thead><tr><th>User</th><th>Contact</th><th>Role</th><th>Status</th><th>Joined</th><th class="text-end">Actions</th></tr></thead><tbody>
                                                            <%for (AdminAccount row : data.rows) {
        boolean self = row.id == actor.getId(), noDelete = self || row.hasHistory, canToggle = !self && !"inactive".equals(row.status);
        String modalState = "<input type=\"hidden\" name=\"id\" value=\"" + row.id + "\"><input type=\"hidden\" name=\"version\" value=\"" + h(row.version) + "\">";%>
                                                    <tr><td><div class="user-cell"><span class="avatar"><%=h(row.displayName.isBlank() ? "?" : row.displayName.substring(0, 1).toUpperCase(Locale.ROOT))%></span><div><strong><%=h(row.displayName)%><%if (self) {%><span class="you-tag">You</span><%}%></strong><small>@<%=h(row.username)%></small></div></div></td>
                                                        <td><span class="contact-email"><%=h(row.email.isEmpty() ? "—" : row.email)%></span><small class="contact-phone"><%=h(row.phone)%></small></td>
                                                        <td><span class="role-pill role-<%=h(row.role)%>"><%=icon(row.role)%><%=h(row.role.substring(0, 1).toUpperCase() + row.role.substring(1))%></span></td>
                                                        <td><span class="status-badge"><i class="status-dot <%=h(row.status)%>"></i><%=h(row.status.substring(0, 1).toUpperCase() + row.status.substring(1))%></span></td>
                                                        <td class="joined"><%=h(row.createdAt.toLocalDate())%></td><td><div class="actions">
                                                                <a class="icon-btn" href="<%=h(base + "?" + search + "&page=" + data.page + "&inspect=" + row.id)%>" data-bs-toggle="tooltip" title="Inspect booking history" aria-label="Inspect <%=h(row.username)%>"><%=icon("eye")%></a>
                                                                <a class="icon-btn" href="<%=h(base + "?" + search + "&page=" + data.page + "&mode=edit&id=" + row.id)%>" data-bs-toggle="tooltip" title="Edit" aria-label="Edit <%=h(row.username)%>"><%=icon("edit")%></a>
                                                                <form action="<%=base%>" method="post" data-confirm="<%="locked".equals(row.status) ? "Unlock this account?" : "Lock this account?"%>"><%=state%><%=modalState%><input type="hidden" name="action" value="toggle"><span data-bs-toggle="tooltip" title="<%=self ? "Cannot lock your own account" : !canToggle ? "Use Edit to reactivate" : "locked".equals(row.status) ? "Unlock" : "Lock"%>"><button class="icon-btn" type="submit" <%=canToggle ? "" : "disabled"%> aria-label="Toggle lock"><%=icon("locked".equals(row.status) ? "unlock" : "lock")%></button></span></form>
                                                                <form action="<%=base%>" method="post" data-confirm="Permanently delete this unused account?"><%=state%><%=modalState%><input type="hidden" name="action" value="delete"><span data-bs-toggle="tooltip" title="<%=self ? "Cannot delete yourself" : row.hasHistory ? "BR-43: Transaction history must be retained" : "Delete unused account"%>"><button class="icon-btn danger" type="submit" <%=noDelete ? "disabled" : ""%> aria-label="Delete <%=h(row.username)%>"><%=icon("trash")%></button></span></form>
                                                            </div></td></tr><%}%>
                                                    <%if (data.rows.isEmpty()) {%><tr><td colspan="6"><div class="empty-state"><%=icon("search")%><strong>No users found</strong><span>Try another search or filter.</span></div></td></tr><%}%>
                                                </tbody></table></div>
                                        <footer class="table-footer"><span><%=data.total == 0 ? 0 : (data.page - 1) * 10 + 1%>–<%=Math.min(data.page * 10, data.total)%> of <%=data.total%> users</span><nav aria-label="User pages"><%if (data.page > 1) {%><a class="page-button" href="<%=h(base + "?" + search + "&page=" + (data.page - 1))%>" aria-label="Previous page">‹</a><%}%><span class="page-current"><%=data.page%> / <%=data.pages%></span><%if (data.page < data.pages) {%><a class="page-button" href="<%=h(base + "?" + search + "&page=" + (data.page + 1))%>" aria-label="Next page">›</a><%}%></nav></footer></section>
                                    <div class="workspace-footer"><span>Luxe Escape Hotel</span><span>Account administration</span></div>
                                </main></div>
                                <%if (form != null) {%>
                            <div class="modal fade glass-modal" id="userModal" tabindex="-1" aria-labelledby="userModalTitle" aria-hidden="true" data-auto-open="true"><div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content">
                                        <form method="post" action="<%=base%>"><%=state%><input type="hidden" name="action" value="<%=creating ? "create" : "update"%>"><input type="hidden" name="id" value="<%=form.id%>"><input type="hidden" name="version" value="<%=h(form.version)%>">
                                                        <div class="modal-header"><div><div class="modal-eyebrow">ACCOUNT DETAILS</div><h2 class="modal-title fs-5" id="userModalTitle"><%=creating ? "Add new user" : "Edit user"%></h2></div><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
                                                        <div class="modal-body"><%if (!errors.isEmpty()) {%><div class="alert alert-danger" role="alert"><%for (String e : new LinkedHashSet<>(errors.values())) {%><div><%=h(english(e))%></div><%}%></div><%}%>
                                                            <div class="row g-3">
                                                                <%=field("Username", "username", form.username, "text", 50, true, !creating, errors)%><%=field("Email", "email", form.email, "email", 100, true, false, errors)%>
                                                                <%=field("First name", "firstName", form.firstName, "text", 50, true, false, errors)%><%=field("Last name", "lastName", form.lastName, "text", 50, true, false, errors)%>
                                                                <%=field("Phone", "phone", form.phone, "tel", 11, true, false, errors)%>
                                                                <div class="col-md-6"><label class="form-label" for="accountRole">Role <span class="required">*</span></label><select class="form-select" id="accountRole" name="accountRole" required><%for (String r : AdminAccount.ROLES) {%><option value="<%=r%>" <%=r.equals(form.role) ? "selected" : ""%>><%=r.substring(0, 1).toUpperCase() + r.substring(1)%></option><%}%></select></div>
                                                                        <%=field("Country", "country", form.country, "text", 50, false, false, errors)%><%=field("City", "city", form.city, "text", 50, false, false, errors)%>
                                                                        <%=field("Street", "street", form.street, "text", 100, false, false, errors)%>
                                                                <div class="col-md-6"><label class="form-label" for="accountStatus">Status <span class="required">*</span></label><%if (creating) {%><input type="hidden" name="accountStatus" value="active"><div class="status-field"><i class="status-dot active"></i> Active</div><%} else {%><select class="form-select" id="accountStatus" name="accountStatus" required><%for (String st : AdminAccount.STATUSES) {%><option value="<%=st%>" <%=st.equals(form.status) ? "selected" : ""%>><%=st.substring(0, 1).toUpperCase() + st.substring(1)%></option><%}%></select><%}%></div>
                                                            </div><%if (creating) {%><div class="auto-password"><%=icon("mail")%><span>Auto-generated password · Email notification</span></div><%}%></div>
                                                        <div class="modal-footer"><button type="button" class="btn btn-light border" data-bs-dismiss="modal">Cancel</button><button type="submit" class="btn btn-luxe"><%=icon("check")%> <%=creating ? "Create user" : "Save changes"%></button></div>
                                                        </form></div></div></div>
                                                        <%}%>
                                                        <%if (inspect != null) {
        List<AdminAccount.BookingRow> bookings = (List<AdminAccount.BookingRow>) request.getAttribute("history");
        int hp = (Integer) request.getAttribute("historyPage");
        boolean more = bookings.size() > 10;%>
                                                        <div class="modal fade glass-modal" id="inspectModal" tabindex="-1" aria-labelledby="inspectTitle" aria-hidden="true" data-auto-open="true"><div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content"><div class="modal-header"><div><div class="modal-eyebrow">ACCOUNT OVERVIEW</div><h2 class="modal-title fs-5" id="inspectTitle"><%=h(inspect.displayName)%></h2></div><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
                                                                    <div class="modal-body"><div class="inspect-profile"><span class="role-pill role-<%=h(inspect.role)%>"><%=icon(inspect.role)%><%=h(inspect.role)%></span><span class="status-badge"><i class="status-dot <%=h(inspect.status)%>"></i><%=h(inspect.status)%></span><span class="text-secondary">@<%=h(inspect.username)%></span></div>
                                                                        <dl class="inspect-details"><div><dt>Email</dt><dd><%=h(inspect.email)%></dd></div><div><dt>Phone</dt><dd><%=h(inspect.phone)%></dd></div><div><dt>Address</dt><dd><%=h(String.join(" · ", Arrays.asList(inspect.street, inspect.city, inspect.country)))%></dd></div><div><dt>Joined</dt><dd><%=h(inspect.createdAt.toLocalDate())%></dd></div></dl>
                                                                        <h3 class="history-heading">Booking history <span class="history-icon"><%=icon("eye")%></span></h3>
                                                                        <div class="table-responsive"><table class="table history-table"><thead><tr><th>Booking</th><th>Date</th><th>Status</th><th class="text-end">Total (VND)</th></tr></thead><tbody>
                                                                                    <%for (int i = 0; i < Math.min(10, bookings.size()); i++) {
        AdminAccount.BookingRow b = bookings.get(i);%><tr><td>#<%=b.id%></td><td><%=h(b.date.toLocalDate())%></td><td><span class="booking-state"><%=h(b.status)%></span></td><td class="text-end"><%=h(String.format(Locale.US, "%,.0f", b.total))%></td></tr><%}%>
                                                                                            <%if (bookings.isEmpty()) {%><tr><td colspan="4" class="text-center text-secondary py-4">No booking history</td></tr><%}%>
                                                                                </tbody></table></div><div class="history-pagination"><%if (hp > 1) {%><a href="<%=h(base + "?" + search + "&page=" + data.page + "&inspect=" + inspect.id + "&historyPage=" + (hp - 1))%>">‹ Previous</a><%}%><span>Page <%=hp%></span><%if (more) {%><a href="<%=h(base + "?" + search + "&page=" + data.page + "&inspect=" + inspect.id + "&historyPage=" + (hp + 1))%>">Next ›</a><%}%></div></div>
                                                                    <div class="modal-footer justify-content-between"><form method="post" action="<%=base%>" data-confirm="Generate a NEW password and send it by email? This replaces the current password."><%=state%><input type="hidden" name="action" value="resend"><input type="hidden" name="id" value="<%=inspect.id%>"><input type="hidden" name="version" value="<%=h(inspect.version)%>"><button class="btn btn-outline-secondary btn-sm" type="submit" <%=inspect.id == actor.getId() ? "disabled" : ""%>><%=icon("mail")%> Send new password</button></form><button type="button" class="btn btn-light border" data-bs-dismiss="modal">Close</button></div>
                                                                                        </div></div></div><%}%>
                                                                                        <script src="<%=ctx%>/assets/js/bootstrap.bundle.min.js"></script><script src="<%=ctx%>/assets/js/admin-users.js"></script>
                                                                                        </body></html>
