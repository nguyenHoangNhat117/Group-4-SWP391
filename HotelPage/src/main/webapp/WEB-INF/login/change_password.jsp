<%-- 
    Document   : change_password
    Created on : Nov 2, 2026, 8:19:27 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page import="util.AuthSupport"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Updated by Nguyen Hoang Nhat --%>
<%
    String contextPath = request.getContextPath();
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>Change Password | Luxe Escape</title>

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
                      action="<%= contextPath%>/change-password">

                    <%-- UserID is determined by the authenticated session. --%>
                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <h1 class="text-center">Change Password</h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <%= AuthSupport.html(error)%>
                    </div>
                    <% }%>

                    <div class="input-box">
                        <input type="password"
                               id="currentPassword"
                               name="currentPassword"
                               maxlength="128"
                               autocomplete="current-password"
                               placeholder=" "
                               required>
                        <label for="currentPassword">
                            Current password
                        </label>
                    </div>

                    <div class="input-box">
                        <input type="password"
                               id="newPassword"
                               name="newPassword"
                               minlength="8"
                               maxlength="128"
                               autocomplete="new-password"
                               placeholder=" "
                               required>
                        <label for="newPassword">
                            New password
                        </label>
                    </div>

                    <div class="input-box">
                        <input type="password"
                               id="confirmPassword"
                               name="confirmPassword"
                               minlength="8"
                               maxlength="128"
                               autocomplete="new-password"
                               placeholder=" "
                               required>
                        <label for="confirmPassword">
                            Confirm password
                        </label>
                    </div>

                    <button class="btn" type="submit">
                        Save password
                    </button>

                    <div class="login-register mt-3">
                        <a href="<%= contextPath%>/">
                            Cancel
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