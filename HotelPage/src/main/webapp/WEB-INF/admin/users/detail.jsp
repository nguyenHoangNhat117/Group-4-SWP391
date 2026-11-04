<%-- 
    Document   : detail
    Created on : Nov 2, 2026, 11:51:57 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><title>Account Details | Luxe Escape</title><%@ include file="head.jsp" %></head>
    <body class="ua-page"><%@ include file="/WEB-INF/include/header.jsp" %><main class="ua-main">
            <div class="ua-top"><div><div class="ua-eyebrow">LUXE ESCAPE / USER ACCOUNTS</div><h1><c:out value="${account.displayName}"/></h1><p class="ua-muted">Account #${account.id}</p></div>
                <a href="${pageContext.request.contextPath}/admin/users">← User Accounts</a></div>
            <div class="ua-card"><dl class="ua-detail">
                    <dt>Username</dt><dd><c:out value="${account.username}"/></dd><dt>Role</dt><dd>${account.role == 'manager' ? 'Hotel Manager' : account.role == 'admin' ? 'Administrator' : account.role == 'staff' ? 'Staff' : 'Customer'}</dd>
                    <dt>Status</dt><dd><c:out value="${account.status}"/></dd><dt>Email</dt><dd><c:out value="${account.email}" default="Not set"/></dd>
                    <dt>Phone</dt><dd><c:out value="${account.phone}" default="Not set"/></dd><dt>Address</dt><dd><c:out value="${account.street}"/> / <c:out value="${account.city}"/> / <c:out value="${account.country}"/></dd>
                    <dt>Created</dt><dd><c:out value="${account.createdAt}"/></dd><dt>CustomerID</dt><dd><c:out value="${account.customerId}" default="None"/></dd>
                    <dt>Bookings</dt><dd>${account.bookingCount}</dd></dl>
                <div class="ua-actions" style="margin-top:24px"><a class="ua-button" href="${pageContext.request.contextPath}/admin/users?view=edit&amp;id=${account.id}">Edit Account</a>
                    <c:if test="${account.id != sessionScope.loggedUser.id and account.bookingCount == 0}"><form method="post" action="${pageContext.request.contextPath}/admin/users" onsubmit="return confirm('Permanently delete this account?');">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${account.id}"><input type="hidden" name="version" value="<c:out value='${account.version}'/>">
                            <button class="ua-danger" type="submit">Delete Account</button></form></c:if></div>
                <c:if test="${account.bookingCount > 0}"><p class="ua-muted">Accounts with history cannot be deleted.</p></c:if></div>
                <div class="ua-card"><h2>Recent Bookings</h2>
                    <div class="ua-table-wrap"><table><thead><tr><th>Booking</th><th>Created</th><th>Status</th><th>Total (VND)</th></tr></thead><tbody>
                            <c:forEach items="${bookings}" var="b"><tr><td><a href="${pageContext.request.contextPath}/booking-management?view=detail&amp;id=${b.id}">#${b.id}</a></td><td><c:out value="${b.date}"/></td><td><c:out value="${b.status}"/></td><td><c:out value="${b.total}"/></td></tr></c:forEach>
                            <c:if test="${empty bookings}"><tr><td colspan="4">No bookings yet.</td></tr></c:if></tbody></table></div></div>
        </main></body></html>
