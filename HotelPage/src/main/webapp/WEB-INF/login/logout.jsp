<%-- 
    Document   : logout
    Created on : Oct 14, 2026, 6:16:24 PM
    Author     : Nguyễn Hoàng Nhật
--%>
<%@page contentType="text/html" pageEncoding="UTF-8" import="util.AuthSupport"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign out</title>
    <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="container py-5">
    <main><h1>Sign out?</h1><p>Confirm to end your session.</p>
        <form method="post" action="<%= request.getContextPath()%>/logout">
            <input type="hidden" name="csrfToken" value="<%= AuthSupport.token(session)%>">
            <button class="btn btn-primary" type="submit">Sign out</button>
            <a class="btn btn-secondary" href="<%= request.getContextPath()%>/">Cancel</a>
        </form>
    </main>
</body>
</html>
