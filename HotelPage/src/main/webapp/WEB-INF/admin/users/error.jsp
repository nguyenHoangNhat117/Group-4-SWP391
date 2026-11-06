<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : error
    Created on : Nov 2, 2026, 11:52:33 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="${requestScope.uiLanguage}"><head><meta charset="UTF-8"><title data-i18n="screen.067b2320ae58b1fb">${requestScope.i18n['screen.067b2320ae58b1fb']}</title></head>
    <body><h1><ui:text key="screen.7a0439e01444c2b0"/></h1><p><ui:message text="${error}"/></p>
        <a href="${pageContext.request.contextPath}/admin/users"><ui:text key="screen.df0fe9e00d354b01"/></a></body></html>
