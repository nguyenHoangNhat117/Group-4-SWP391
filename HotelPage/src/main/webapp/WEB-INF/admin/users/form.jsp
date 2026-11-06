<%@ taglib prefix="msg" uri="/WEB-INF/i18n.tld" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : form
    Created on : Nov 2, 2026, 11:52:44 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="${requestScope.uiLanguage}"><head><title data-i18n="screen.334944f2020670d6">${creating ? requestScope.i18n['screen.4759498ac2a719c6'] : requestScope.i18n['screen.464c4ffd019e1e96']} ${requestScope.i18n['screen.334944f2020670d6']}</title><%@ include file="head.jsp" %></head>
    <body class="ua-page"><%@ include file="/WEB-INF/include/header.jsp" %><main class="ua-main">
            <div class="ua-top"><div><div class="ua-eyebrow"><ui:text key="screen.6228800b0a8d2f21"/></div><h1>${creating ? 'Create Account' : requestScope.i18n['screen.1915995d4b16bd05']}</h1></div>
                <a href="${pageContext.request.contextPath}/admin/users"><ui:text key="screen.db94081a9019f53c"/></a></div>
            <c:if test="${not empty error}"><div class="ua-notice ua-error" role="alert"><ui:message text="${error}"/> <c:if test="${not creating}"><a href="${pageContext.request.contextPath}/admin/users?view=edit&amp;id=${account.id}"><ui:text key="screen.bdc090ec61e3fcfc"/></a></c:if></div></c:if>
            <c:if test="${not empty errors}"><div class="ua-notice ua-error" role="alert"><ui:text key="screen.e3475b24b9ae7343"/></div></c:if>
            <form class="ua-card" method="post" action="${pageContext.request.contextPath}/admin/users" autocomplete="off">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                <input type="hidden" name="action" value="${creating ? 'create' : 'update'}">
                <c:if test="${not creating}"><input type="hidden" name="id" value="${account.id}"><input type="hidden" name="version" value="<c:out value='${account.version}'/>"></c:if>
                    <div class="ua-grid">
                        <div><label for="username"><ui:text key="screen.9041ca608c0de35a"/></label><input id="username" name="username" type="text" maxlength="50" value="<c:out value='${account.username}'/>" ${creating ? '' : 'readonly'} required>
                        <c:if test="${not empty errors.username}"><p class="ua-field-error"><c:out value="${errors.username}"/></p></c:if></div>
                    <div><label for="firstName"><ui:text key="screen.7ca57be57e8d8b60"/></label><input id="firstName" name="firstName" type="text" maxlength="50" value="<c:out value='${account.firstName}'/>"  required>
                        <c:if test="${not empty errors.firstName}"><p class="ua-field-error"><c:out value="${errors.firstName}"/></p></c:if></div>
                    <div><label for="lastName"><ui:text key="screen.f55563b386a39dc3"/></label><input id="lastName" name="lastName" type="text" maxlength="50" value="<c:out value='${account.lastName}'/>"  >
                        <c:if test="${not empty errors.lastName}"><p class="ua-field-error"><c:out value="${errors.lastName}"/></p></c:if></div>
                    <div><label for="email"><ui:text key="screen.d652d515eb877584"/></label><input id="email" name="email" type="email" maxlength="100" value="<c:out value='${account.email}'/>"  required>
                        <c:if test="${not empty errors.email}"><p class="ua-field-error"><c:out value="${errors.email}"/></p></c:if></div>
                    <div><label for="phone"><ui:text key="screen.e671712ae0e14d29"/></label><input id="phone" name="phone" type="tel" maxlength="11" value="<c:out value='${account.phone}'/>"  required>
                        <c:if test="${not empty errors.phone}"><p class="ua-field-error"><c:out value="${errors.phone}"/></p></c:if></div>
                    <div><label for="country"><ui:text key="screen.701d021d08c54579"/></label><input id="country" name="country" type="text" maxlength="50" value="<c:out value='${account.country}'/>"  >
                        <c:if test="${not empty errors.country}"><p class="ua-field-error"><c:out value="${errors.country}"/></p></c:if></div>
                    <div><label for="city"><ui:text key="screen.fc33f73246f48295"/></label><input id="city" name="city" type="text" maxlength="50" value="<c:out value='${account.city}'/>"  >
                        <c:if test="${not empty errors.city}"><p class="ua-field-error"><c:out value="${errors.city}"/></p></c:if></div>
                    <div><label for="street"><ui:text key="screen.56ef8f20955f2564"/></label><input id="street" name="street" type="text" maxlength="100" value="<c:out value='${account.street}'/>"  >
                        <c:if test="${not empty errors.street}"><p class="ua-field-error"><c:out value="${errors.street}"/></p></c:if></div>
                    <div><label for="account-role"><ui:text key="screen.c865842159a7f620"/></label><select name="role" id="account-role" ${not creating and account.id == sessionScope.loggedUser.id ? 'disabled' : ''}>
                            <c:forTokens items="customer,staff,manager,admin" delims="," var="r"><option value="${r}" ${account.role == r ? 'selected' : ''}>${r == 'manager' ? 'Hotel Manager' : r == 'admin' ? requestScope.i18n['screen.e7d3e769f3f593da'] : r == 'staff' ? 'Staff' : requestScope.i18n['screen.bf3763383aaf4306']}</option></c:forTokens></select><p class="ua-field-error"><ui:message text="${errors.role}"/></p></div>
                    <div><label for="account-status"><ui:text key="screen.ff498ea05d98b128"/></label><select name="status" id="account-status" ${not creating and account.id == sessionScope.loggedUser.id ? 'disabled' : ''}>
                            <c:forTokens items="active,locked,inactive" delims="," var="s"><option value="${s}" ${account.status == s ? 'selected' : ''} data-i18n="${msg:key(s)}">${msg:text(s, requestScope.uiLanguage)}</option></c:forTokens></select><p class="ua-field-error"><ui:message text="${errors.status}"/></p></div>
                    <c:if test="${not creating and account.id == sessionScope.loggedUser.id}"><input type="hidden" name="role" value="admin"><input type="hidden" name="status" value="active"><p class="ua-wide ua-muted"><ui:text key="screen.4be8e339e5ec7000"/></p></c:if>
                    <c:if test="${creating}"><div class="ua-wide"><label for="password"><ui:text key="screen.8d3e2a011f48dea8"/></label><div class="ua-password">
                                <input id="password" name="password" type="password" minlength="8" maxlength="128" autocomplete="new-password" required aria-describedby="password-help">
                                <button type="button" class="ua-secondary" id="generate-password"><ui:text key="screen.49e49bb4401e67bd"/></button><button type="button" class="ua-secondary" id="toggle-password" aria-pressed="false"><ui:text key="screen.0df6f1cad36c49da"/></button></div>
                            <p class="ua-muted" id="password-help"><ui:text key="screen.79cea24d1ea9e3a2"/></p>
                            <p class="ua-field-error"><c:out value="${errors.password}"/></p></div></c:if>
                </div><div class="ua-actions" style="margin-top:24px"><button type="submit">${creating ? 'Create Account' : 'Save Changes'}</button>
                    <a href="${pageContext.request.contextPath}/admin/users"><ui:text key="screen.19766ed6ccb2f4a3"/></a></div></form></main>
        <c:if test="${creating}"><script src="${pageContext.request.contextPath}/assets/js/admin-users.js"></script></c:if>
    </body></html>
