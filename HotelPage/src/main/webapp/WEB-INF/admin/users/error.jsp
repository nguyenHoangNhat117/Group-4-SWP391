<%-- 
    Document   : error
    Created on : Nov 2, 2026, 11:52:33 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><title>User Accounts</title></head>
    <body><h1>Unable to load accounts</h1><p><c:out value="${error}"/></p>
        <a href="${pageContext.request.contextPath}/admin/users">Try Again</a></body></html>
