<%-- 
    Document   : form
    Created on : Nov 2, 2026, 11:52:44 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><title>${creating ? 'Create' : 'Edit'} Account | Luxe Escape</title><%@ include file="head.jsp" %></head>
    <body class="ua-page"><%@ include file="/WEB-INF/include/header.jsp" %><main class="ua-main">
            <div class="ua-top"><div><div class="ua-eyebrow">LUXE ESCAPE / USER ACCOUNTS</div><h1>${creating ? 'Create Account' : 'Edit Account'}</h1></div>
                <a href="${pageContext.request.contextPath}/admin/users">← User Accounts</a></div>
            <c:if test="${not empty error}"><div class="ua-notice ua-error" role="alert"><c:out value="${error}"/> <c:if test="${not creating}"><a href="${pageContext.request.contextPath}/admin/users?view=edit&amp;id=${account.id}">Reload</a></c:if></div></c:if>
            <c:if test="${not empty errors}"><div class="ua-notice ua-error" role="alert">Check the highlighted fields.</div></c:if>
            <form class="ua-card" method="post" action="${pageContext.request.contextPath}/admin/users" autocomplete="off">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                <input type="hidden" name="action" value="${creating ? 'create' : 'update'}">
                <c:if test="${not creating}"><input type="hidden" name="id" value="${account.id}"><input type="hidden" name="version" value="<c:out value='${account.version}'/>"></c:if>
                    <div class="ua-grid">
                        <div><label for="username">Username *</label><input id="username" name="username" type="text" maxlength="50" value="<c:out value='${account.username}'/>" ${creating ? '' : 'readonly'} required>
                        <c:if test="${not empty errors.username}"><p class="ua-field-error"><c:out value="${errors.username}"/></p></c:if></div>
                    <div><label for="firstName">First Name *</label><input id="firstName" name="firstName" type="text" maxlength="50" value="<c:out value='${account.firstName}'/>"  required>
                        <c:if test="${not empty errors.firstName}"><p class="ua-field-error"><c:out value="${errors.firstName}"/></p></c:if></div>
                    <div><label for="lastName">Last Name</label><input id="lastName" name="lastName" type="text" maxlength="50" value="<c:out value='${account.lastName}'/>"  >
                        <c:if test="${not empty errors.lastName}"><p class="ua-field-error"><c:out value="${errors.lastName}"/></p></c:if></div>
                    <div><label for="email">Email *</label><input id="email" name="email" type="email" maxlength="100" value="<c:out value='${account.email}'/>"  required>
                        <c:if test="${not empty errors.email}"><p class="ua-field-error"><c:out value="${errors.email}"/></p></c:if></div>
                    <div><label for="phone">Phone *</label><input id="phone" name="phone" type="tel" maxlength="11" value="<c:out value='${account.phone}'/>"  required>
                        <c:if test="${not empty errors.phone}"><p class="ua-field-error"><c:out value="${errors.phone}"/></p></c:if></div>
                    <div><label for="country">Country</label><input id="country" name="country" type="text" maxlength="50" value="<c:out value='${account.country}'/>"  >
                        <c:if test="${not empty errors.country}"><p class="ua-field-error"><c:out value="${errors.country}"/></p></c:if></div>
                    <div><label for="city">City</label><input id="city" name="city" type="text" maxlength="50" value="<c:out value='${account.city}'/>"  >
                        <c:if test="${not empty errors.city}"><p class="ua-field-error"><c:out value="${errors.city}"/></p></c:if></div>
                    <div><label for="street">Address</label><input id="street" name="street" type="text" maxlength="100" value="<c:out value='${account.street}'/>"  >
                        <c:if test="${not empty errors.street}"><p class="ua-field-error"><c:out value="${errors.street}"/></p></c:if></div>
                    <div><label for="account-role">Role *</label><select name="role" id="account-role" ${not creating and account.id == sessionScope.loggedUser.id ? 'disabled' : ''}>
                            <c:forTokens items="customer,staff,manager,admin" delims="," var="r"><option value="${r}" ${account.role == r ? 'selected' : ''}>${r == 'manager' ? 'Hotel Manager' : r == 'admin' ? 'Administrator' : r == 'staff' ? 'Staff' : 'Customer'}</option></c:forTokens></select><p class="ua-field-error"><c:out value="${errors.role}"/></p></div>
                    <div><label for="account-status">Status *</label><select name="status" id="account-status" ${not creating and account.id == sessionScope.loggedUser.id ? 'disabled' : ''}>
                            <c:forTokens items="active,locked,inactive" delims="," var="s"><option value="${s}" ${account.status == s ? 'selected' : ''}>${s}</option></c:forTokens></select><p class="ua-field-error"><c:out value="${errors.status}"/></p></div>
                    <c:if test="${not creating and account.id == sessionScope.loggedUser.id}"><input type="hidden" name="role" value="admin"><input type="hidden" name="status" value="active"><p class="ua-wide ua-muted">Your role and status cannot be changed here.</p></c:if>
                    <c:if test="${creating}"><div class="ua-wide"><label for="password">Initial Password *</label><div class="ua-password">
                                <input id="password" name="password" type="password" minlength="8" maxlength="128" autocomplete="new-password" required aria-describedby="password-help">
                                <button type="button" class="ua-secondary" id="generate-password">Generate</button><button type="button" class="ua-secondary" id="toggle-password" aria-pressed="false">Show</button></div>
                            <p class="ua-muted" id="password-help">8–128 characters: uppercase, lowercase, number and symbol. No spaces.</p>
                            <p class="ua-field-error"><c:out value="${errors.password}"/></p></div></c:if>
                </div><div class="ua-actions" style="margin-top:24px"><button type="submit">${creating ? 'Create Account' : 'Save Changes'}</button>
                    <a href="${pageContext.request.contextPath}/admin/users">Cancel</a></div></form></main>
        <c:if test="${creating}"><script src="${pageContext.request.contextPath}/assets/js/admin-users.js"></script></c:if>
    </body></html>
