<%-- 
    Document   : list
    Created on : Nov 2, 2026, 11:53:08 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><title>User Accounts | Luxe Escape</title><%@ include file="head.jsp" %></head>
    <body class="ua-page"><%@ include file="/WEB-INF/include/header.jsp" %>
        <main class="ua-main">
            <div class="ua-top"><div><div class="ua-eyebrow">LUXE ESCAPE / ADMINISTRATION</div><h1>User Accounts</h1>
                </div>
                <a class="ua-button" href="${pageContext.request.contextPath}/admin/users?view=create">+ Create Account</a></div>
            <c:if test="${not empty message}"><div class="ua-notice" role="status"><c:out value="${message}"/></div></c:if>
            <div class="ua-card"><form method="get" action="${pageContext.request.contextPath}/admin/users" class="ua-filters">
                    <div><label for="keyword">Search Accounts</label><input id="keyword" name="keyword" maxlength="100" value="<c:out value='${keyword}'/>" placeholder="Username, email or name"></div>
                    <div><label for="role">Role</label><select id="role" name="role"><option value="">All roles</option>
                            <c:forTokens items="customer,staff,manager,admin" delims="," var="r"><option value="${r}" ${role == r ? 'selected' : ''}>${r == 'manager' ? 'Hotel Manager' : r == 'admin' ? 'Administrator' : r == 'staff' ? 'Staff' : 'Customer'}</option></c:forTokens></select></div>
                        <div><label for="status">Status</label><select id="status" name="status"><option value="">All statuses</option>
                            <c:forTokens items="active,locked,inactive" delims="," var="s"><option value="${s}" ${status == s ? 'selected' : ''}>${s}</option></c:forTokens></select></div>
                        <button type="submit">Search</button></form></div>
                <div class="ua-card"><p><strong>${result.total}</strong> accounts</p><div class="ua-table-wrap"><table>
                        <thead><tr><th>Account</th><th>Contact</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead><tbody>
                            <c:forEach items="${result.accounts}" var="a"><tr>
                                    <td><strong><c:out value="${a.displayName}"/></strong><small><c:out value="${a.username}"/> #${a.id}<c:if test="${a.id == sessionScope.loggedUser.id}"> · You</c:if></small></td>
                                    <td><c:out value="${a.email}" default="No email"/><small><c:out value="${a.phone}"/></small></td>
                                    <td><span class="ua-badge">${a.role == 'manager' ? 'Hotel Manager' : a.role == 'admin' ? 'Administrator' : a.role == 'staff' ? 'Staff' : 'Customer'}</span></td><td><c:out value="${a.status}"/></td>
                                    <td><div class="ua-actions"><a href="${pageContext.request.contextPath}/admin/users?view=detail&amp;id=${a.id}">Details</a>
                                            <a href="${pageContext.request.contextPath}/admin/users?view=edit&amp;id=${a.id}">Edit</a></div></td></tr></c:forEach>
                            <c:if test="${empty result.accounts}"><tr><td colspan="5">No accounts found.</td></tr></c:if>
                            </tbody></table></div>
                    <div class="ua-pagination">
                        <div><c:if test="${result.number > 1}"><c:url var="prev" value="/admin/users"><c:param name="keyword" value="${keyword}"/><c:param name="role" value="${role}"/><c:param name="status" value="${status}"/><c:param name="page" value="${result.number - 1}"/></c:url><a href="<c:out value='${prev}'/>">← Previous</a></c:if></div>
                    <span>Page ${result.number} / ${result.pages}</span>
                    <div><c:if test="${result.number < result.pages}"><c:url var="next" value="/admin/users"><c:param name="keyword" value="${keyword}"/><c:param name="role" value="${role}"/><c:param name="status" value="${status}"/><c:param name="page" value="${result.number + 1}"/></c:url><a href="<c:out value='${next}'/>">Next →</a></c:if></div>
                </div></div></main></body></html>
