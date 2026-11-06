<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page import="util.AuthSupport"%>
<%-- 
    Document   : reset_password
    Created on : Oct 14, 2026, 6:16:24 PM
    Author     : Nguyễn Hoàng Nhật
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

        <title data-i18n="screen.2611d4632dce2a7a">${requestScope.i18n['screen.2611d4632dce2a7a']}</title>

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
                      action="<%= contextPath%>/reset-password">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <h1 class="text-center"><ui:text key="screen.4e70f1fd24babdea"/></h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <ui:message text="<%= error %>"/>
                    </div>
                    <% }%>

                    <%-- Visibility buttons are added by the shared JSP. --%>
                    <div class="input-box">
                        <input type="password"
                               id="password"
                               name="password"
                               minlength="8"
                               maxlength="128"
                               autocomplete="new-password"
                               placeholder=" "
                               required>
                        <label for="password"><ui:text key="screen.3dd9df4441fb348f"/></label>
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
                        <ui:text key="screen.e0edfeb3a71a1002"/>
                    </button>

                    <div class="login-register mt-3">
                        <a href="<%= contextPath%>/login">
                            <ui:text key="screen.3e3806ff17302cba"/>
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