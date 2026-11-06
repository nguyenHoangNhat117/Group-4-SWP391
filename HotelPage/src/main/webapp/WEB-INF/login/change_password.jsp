<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
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
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.e454a1f447293281">${requestScope.i18n['screen.e454a1f447293281']}</title>

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

                    <h1 class="text-center"><ui:text key="screen.a949f46230748d3f"/></h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <ui:message text="<%= error %>"/>
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
                            <ui:text key="screen.72ed2bd767cef627"/>
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
                            <ui:text key="screen.3dd9df4441fb348f"/>
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
                            <ui:text key="screen.5ac265f396a2c586"/>
                        </label>
                    </div>

                    <button class="btn" type="submit">
                        <ui:text key="screen.e86cf7b88cde4602"/>
                    </button>

                    <div class="login-register mt-3">
                        <a href="<%= contextPath%>/">
                            <ui:text key="screen.19766ed6ccb2f4a3"/>
                        </a>
                    </div>
                </form>
            </div>
        </main>

        <footer>
            <ui:text key="screen.e054863fe5b390e5"/>
        </footer>
    </body>
</html>