<%@ taglib prefix="msg" uri="/WEB-INF/i18n.tld" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : list
    Created on : Nov 2, 2026, 11:53:08 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="${requestScope.uiLanguage}"><head><title data-i18n="screen.e5c65f85a93274aa">${requestScope.i18n['screen.e5c65f85a93274aa']}</title><%@ include file="head.jsp" %></head>
    <body class="ua-page"><%@ include file="/WEB-INF/include/header.jsp" %>
        <main class="ua-main">
            <div class="ua-top"><div><div class="ua-eyebrow"><ui:text key="screen.d3416c4cb386ab2c"/></div><h1><ui:text key="screen.067b2320ae58b1fb"/></h1>
                </div>
                <a class="ua-button" href="${pageContext.request.contextPath}/admin/users?view=create"><ui:text key="screen.ccaf3b79652cbafa"/></a></div>
            <c:if test="${not empty message}"><div class="ua-notice" role="status"><ui:message text="${message}"/></div></c:if>
            <div class="ua-card"><form method="get" action="${pageContext.request.contextPath}/admin/users" class="ua-filters">
                    <div><label for="keyword"><ui:text key="screen.71fb8d1b2653df9d"/></label><input id="keyword" name="keyword" maxlength="100" value="<c:out value='${keyword}'/>" placeholder="${requestScope.i18n['screen.f379ab386cc7270a']}" data-i18n-placeholder="screen.f379ab386cc7270a"></div>
                    <div><label for="role"><ui:text key="screen.14736a2eb9f4159f"/></label><select id="role" name="role"><option value="" data-i18n="screen.78f133afd3ec55cd">${requestScope.i18n['screen.78f133afd3ec55cd']}</option>
                            <c:forTokens items="customer,staff,manager,admin" delims="," var="r"><option value="${r}" ${role == r ? 'selected' : ''}>${r == 'manager' ? 'Hotel Manager' : r == 'admin' ? requestScope.i18n['screen.e7d3e769f3f593da'] : r == 'staff' ? 'Staff' : requestScope.i18n['screen.bf3763383aaf4306']}</option></c:forTokens></select></div>
                        <div><label for="status"><ui:text key="screen.920e413c7d411b61"/></label><select id="status" name="status"><option value="" data-i18n="screen.8ee57323a6f24cc7">${requestScope.i18n['screen.8ee57323a6f24cc7']}</option>
                            <c:forTokens items="active,locked,inactive" delims="," var="s"><option value="${s}" ${status == s ? 'selected' : ''} data-i18n="${msg:key(s)}">${msg:text(s, requestScope.uiLanguage)}</option></c:forTokens></select></div>
                        <button type="submit"><ui:text key="screen.49c266baaaa70981"/></button></form></div>
                <div class="ua-card"><p><strong>${result.total}</strong> <ui:text key="screen.bc62a3c14fec277e"/></p><div class="ua-table-wrap"><table>
                        <thead><tr><th><ui:text key="screen.7e1b0d5641f2640c"/></th><th><ui:text key="screen.2b5c3d26721ae9c3"/></th><th><ui:text key="screen.14736a2eb9f4159f"/></th><th><ui:text key="screen.920e413c7d411b61"/></th><th><ui:text key="screen.ff8059dc6752afdd"/></th></tr></thead><tbody>
                            <c:forEach items="${result.accounts}" var="a"><tr>
                                    <td><strong><c:out value="${a.displayName}"/></strong><small><c:out value="${a.username}"/> #${a.id}<c:if test="${a.id == sessionScope.loggedUser.id}"> <ui:text key="screen.c7e89fc8f2c43551"/></c:if></small></td>
                                    <td><c:out value="${a.email}" default="No email"/><small><c:out value="${a.phone}"/></small></td>
                                    <td><span class="ua-badge">${a.role == 'manager' ? 'Hotel Manager' : a.role == 'admin' ? requestScope.i18n['screen.e7d3e769f3f593da'] : a.role == 'staff' ? 'Staff' : requestScope.i18n['screen.bf3763383aaf4306']}</span></td><td><ui:message text="${a.status}"/></td>
                                    <td><div class="ua-actions"><a href="${pageContext.request.contextPath}/admin/users?view=detail&amp;id=${a.id}"><ui:text key="screen.45989de49fb7f66d"/></a>
                                            <a href="${pageContext.request.contextPath}/admin/users?view=edit&amp;id=${a.id}"><ui:text key="screen.464c4ffd019e1e96"/></a></div></td></tr></c:forEach>
                            <c:if test="${empty result.accounts}"><tr><td colspan="5"><ui:text key="screen.7fae03bf4bd8e96b"/></td></tr></c:if>
                            </tbody></table></div>
                    <div class="ua-pagination">
                        <div><c:if test="${result.number > 1}"><c:url var="prev" value="/admin/users"><c:param name="keyword" value="${keyword}"/><c:param name="role" value="${role}"/><c:param name="status" value="${status}"/><c:param name="page" value="${result.number - 1}"/></c:url><a href="<c:out value='${prev}'/>"><ui:text key="screen.435567b142898cc2"/></a></c:if></div>
                    <span><ui:text key="screen.0a30a815d67d7dd2"/> ${result.number} / ${result.pages}</span>
                    <div><c:if test="${result.number < result.pages}"><c:url var="next" value="/admin/users"><c:param name="keyword" value="${keyword}"/><c:param name="role" value="${role}"/><c:param name="status" value="${status}"/><c:param name="page" value="${result.number + 1}"/></c:url><a href="<c:out value='${next}'/>"><ui:text key="screen.bcf0489a7b45328d"/></a></c:if></div>
                </div></div></main></body></html>
