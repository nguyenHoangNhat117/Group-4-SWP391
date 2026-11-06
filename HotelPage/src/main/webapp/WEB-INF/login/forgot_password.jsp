<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
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
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.a2e53a4889187d56">${requestScope.i18n['screen.a2e53a4889187d56']}</title>

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

                    <h1 class="text-center"><ui:text key="screen.fdfd41de7ae5e1d9"/></h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <ui:message text="<%= error %>"/>
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
                        <label for="email"><ui:text key="screen.969ccbd3cf6300ec"/></label>
                    </div>

                    <button class="btn" type="submit">
                        <ui:text key="screen.9c45665a6ee4c7d0"/>
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