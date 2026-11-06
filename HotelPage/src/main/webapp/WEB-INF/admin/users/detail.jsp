<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : detail
    Created on : Nov 2, 2026, 11:51:57 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="${requestScope.uiLanguage}"><head><title data-i18n="screen.dc8f79179c7fb0ae">${requestScope.i18n['screen.dc8f79179c7fb0ae']}</title><%@ include file="head.jsp" %></head>
    <body class="ua-page"><%@ include file="/WEB-INF/include/header.jsp" %><main class="ua-main">
            <div class="ua-top"><div><div class="ua-eyebrow"><ui:text key="screen.6228800b0a8d2f21"/></div><h1><c:out value="${account.displayName}"/></h1><p class="ua-muted"><ui:text key="screen.bf2a09892f3c04ef"/>${account.id}</p></div>
                <a href="${pageContext.request.contextPath}/admin/users"><ui:text key="screen.db94081a9019f53c"/></a></div>
            <div class="ua-card"><dl class="ua-detail">
                    <dt><ui:text key="screen.e3b89e9d33f88e52"/></dt><dd><c:out value="${account.username}"/></dd><dt><ui:text key="screen.14736a2eb9f4159f"/></dt><dd>${account.role == 'manager' ? 'Hotel Manager' : account.role == 'admin' ? requestScope.i18n['screen.e7d3e769f3f593da'] : account.role == 'staff' ? 'Staff' : requestScope.i18n['screen.bf3763383aaf4306']}</dd>
                    <dt><ui:text key="screen.920e413c7d411b61"/></dt><dd><ui:message text="${account.status}"/></dd><dt><ui:text key="screen.969ccbd3cf6300ec"/></dt><dd><c:out value="${account.email}" default="Not set"/></dd>
                    <dt><ui:text key="screen.63dceb8800b2dd4a"/></dt><dd><c:out value="${account.phone}" default="Not set"/></dd><dt><ui:text key="screen.56ef8f20955f2564"/></dt><dd><c:out value="${account.street}"/> / <c:out value="${account.city}"/> / <c:out value="${account.country}"/></dd>
                    <dt><ui:text key="screen.d70b9e24bca26b40"/></dt><dd><c:out value="${account.createdAt}"/></dd><dt>CustomerID</dt><dd><c:out value="${account.customerId}" default="None"/></dd>
                    <dt><ui:text key="screen.4e5f81ada70c344e"/></dt><dd>${account.bookingCount}</dd></dl>
                <div class="ua-actions" style="margin-top:24px"><a class="ua-button" href="${pageContext.request.contextPath}/admin/users?view=edit&amp;id=${account.id}"><ui:text key="screen.1915995d4b16bd05"/></a>
                    <c:if test="${account.id != sessionScope.loggedUser.id and account.bookingCount == 0}"><form method="post" action="${pageContext.request.contextPath}/admin/users" onsubmit="return confirm('Permanently delete this account?');">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${account.id}"><input type="hidden" name="version" value="<c:out value='${account.version}'/>">
                            <button class="ua-danger" type="submit"><ui:text key="screen.c031bf992e172f57"/></button></form></c:if></div>
                <c:if test="${account.bookingCount > 0}"><p class="ua-muted"><ui:text key="screen.58ba91a92890ce3a"/></p></c:if></div>
                <div class="ua-card"><h2><ui:text key="screen.401a931e39350267"/></h2>
                    <div class="ua-table-wrap"><table><thead><tr><th><ui:text key="screen.948e03e7054b919e"/></th><th><ui:text key="screen.d70b9e24bca26b40"/></th><th><ui:text key="screen.920e413c7d411b61"/></th><th><ui:text key="screen.c6c35383ee38ed77"/></th></tr></thead><tbody>
                            <c:forEach items="${bookings}" var="b"><tr><td><a href="${pageContext.request.contextPath}/booking-management?view=detail&amp;id=${b.id}">#${b.id}</a></td><td><c:out value="${b.date}"/></td><td><ui:message text="${b.status}"/></td><td><c:out value="${b.total}"/></td></tr></c:forEach>
                            <c:if test="${empty bookings}"><tr><td colspan="4"><ui:text key="screen.5744538165afb283"/></td></tr></c:if></tbody></table></div></div>
        </main></body></html>
