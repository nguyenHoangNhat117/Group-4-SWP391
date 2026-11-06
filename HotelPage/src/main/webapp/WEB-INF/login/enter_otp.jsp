<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page import="util.AuthSupport"%>
<%-- 
    Document   : enter_otp
    Created on : Oct 14, 2026, 6:16:24 PM
    Author     : Nguyễn Hoàng Nhật
--%>

<%@page import="util.AuthSupport"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Updated by Nguyen Hoang Nhat --%>
<%
    String contextPath = request.getContextPath();

    String error = (String) request.getAttribute("error");
    String message = (String) request.getAttribute("message");

    boolean otpAvailable = Boolean.TRUE.equals(
            request.getAttribute("otpAvailable")
    );

    Object remaining = request.getAttribute("remainingAttempts");
    String recoveryEmail = (String) session.getAttribute("email");
%>
<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.23edc073bb72c0d2">${requestScope.i18n['screen.23edc073bb72c0d2']}</title>

        <link href="<%= contextPath%>/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/css/login.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/css/luxe-auth.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/img/favicon.png"
              rel="icon" type="image/png">
    </head>

    <body class="page-login">
        <header>
            <span>
                <a href="<%= contextPath%>/">Luxe Escape</a>
            </span>
        </header>

        <main class="form-signin wrapper show">
            <div class="form-box">
                <h1 class="text-center"><ui:text key="screen.9aa6fb054220c5b7"/></h1>

                <% if (error != null) {%>
                <div class="alert alert-danger mt-3" role="alert">
                    <ui:message text="<%= error %>"/>
                </div>
                <% } %>

                <% if (message != null) {%>
                <div class="alert alert-success mt-3" role="status">
                    <ui:message text="<%= message %>"/>
                </div>
                <% } %>

                <% if (otpAvailable) {%>
                <p class="mt-3" style="color: white;">
                    <ui:text key="screen.047b45c910874492"/>
                    <strong><%= AuthSupport.html(remaining)%></strong>.
                </p>

                <form method="post"
                      action="<%= contextPath%>/verify-otp">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <div class="input-box">
                        <input type="text"
                               id="otp"
                               name="otp"
                               inputmode="numeric"
                               pattern="[0-9]{6}"
                               minlength="6"
                               maxlength="6"
                               autocomplete="one-time-code"
                               placeholder=" "
                               required>
                        <label for="otp"><ui:text key="screen.6c0e6acd35694622"/></label>
                    </div>

                    <button class="btn" type="submit">
                        <ui:text key="screen.eea2745e2867a677"/>
                    </button>
                </form>

                <% } else { %>
                <p class="mt-3" style="color: white;">
                    <ui:text key="screen.df5cc6f9df892087"/>
                </p>
                <% }%>

                <%-- Resend uses POST and the email stored in this session. --%>
                <form method="post"
                      class="mt-3"
                      action="<%= contextPath%>/forgot-password">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <input type="hidden"
                           name="email"
                           value="<%= AuthSupport.html(recoveryEmail)%>">

                    <button class="btn" type="submit">
                        <ui:text key="screen.6747ac5ba6564a3e"/>
                    </button>
                </form>

                <div class="login-register mt-3">
                    <a href="<%= contextPath%>/forgot-password">
                        <ui:text key="screen.687da8b89baf2c10"/>
                    </a>
                    <br>
                    <a href="<%= contextPath%>/login">
                        <ui:text key="screen.3e3806ff17302cba"/>
                    </a>
                </div>
            </div>
        </main>

        <footer>
            <ui:text key="screen.e054863fe5b390e5"/>
        </footer>
    </body>
</html>