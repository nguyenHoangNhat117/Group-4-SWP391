<%@page import="util.AuthSupport"%>
<%-- 
    Document   : forgot password
    Created on : Oct 14, 2026, 6:16:24 PM
    Author     : Nguyễn Hoàng Nhật
--%>

<%@page import="util.AuthSupport"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Updated by Nguyen Hoang Nhat --%>
<%
    String contextPath = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String enteredEmail = (String) request.getAttribute("enteredEmail");
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>Forgot Password | Luxe Escape</title>

        <link href="<%= contextPath%>/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/css/login.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/css/luxe-auth.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/img/favicon.png"
              rel="icon" type="image/png">

        <%@include file="/WEB-INF/include/auth-password-toggle.jsp"%>
    </head>

    <body class="page-login">
        <header>
            <span>
                <a href="<%= contextPath%>/">Luxe Escape</a>
            </span>
        </header>

        <main class="form-signin wrapper show">
            <div class="form-box">
                <form method="post"
                      accept-charset="UTF-8"
                      action="<%= contextPath%>/forgot-password">

                    <%-- Preserve CSRF protection for OTP requests. --%>
                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <h1 class="text-center">Forgot Password</h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <%= AuthSupport.html(error)%>
                    </div>
                    <% }%>

                    <div class="input-box">
                        <input type="email"
                               id="email"
                               name="email"
                               maxlength="100"
                               autocomplete="email"
                               value="<%= AuthSupport.html(enteredEmail)%>"
                               placeholder=" "
                               required>
                        <label for="email">Email</label>
                    </div>

                    <button class="btn" type="submit">
                        Send OTP
                    </button>

                    <div class="login-register mt-3">
                        <a href="<%= contextPath%>/login">
                            Back to login
                        </a>
                    </div>
                </form>
            </div>
        </main>

        <footer>
            &copy; 2026 FPT University SE2001 Group 4.
            All rights reserved.
        </footer>
    </body>
</html>