<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page import="util.AuthSupport"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Updated by Nguyen Hoang Nhat --%>
<%
    String contextPath = request.getContextPath();

    String error = (String) request.getAttribute("error");
    String message = (String) request.getAttribute("message");
    String enteredUsername
            = (String) request.getAttribute("enteredUsername");

    boolean rememberChecked = Boolean.TRUE.equals(
            request.getAttribute("rememberChecked")
    );
%>
<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.2b90f3ff0acd9fb4">${requestScope.i18n['screen.2b90f3ff0acd9fb4']}</title>

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

            <nav class="navigation">
                <a href="<%= contextPath%>/#rooms"><ui:text key="screen.bd32f6cedb5fb7ae"/></a>
                <a href="<%= contextPath%>/#restaurant-leap">
                    <ui:text key="screen.1212ea9e20949ffc"/>
                </a>
                <a href="<%= contextPath%>/booking"><ui:text key="screen.948e03e7054b919e"/></a>
            </nav>
        </header>

        <main class="form-signin wrapper show">
            <div class="form-box">
                <form method="post"
                      accept-charset="UTF-8"
                      action="<%= contextPath%>/login">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <h1 class="text-center"><ui:text key="screen.bfd402b2f6f38125"/></h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <ui:message text="<%= error %>"/>
                    </div>
                    <% } %>

                    <% if (message != null) {%>
                    <div class="alert alert-success mt-3 auth-message"
                         role="status">
                        <ui:message text="<%= message %>"/>
                    </div>
                    <% }%>

                    <div class="input-box">
                        <input type="text"
                               id="username"
                               name="username"
                               maxlength="100"
                               autocomplete="username"
                               autocapitalize="none"
                               spellcheck="false"
                               value="<%= AuthSupport.html(enteredUsername)%>"
                               placeholder=" "
                               required>
                        <label for="username"><ui:text key="screen.dd01f1017cbc1009"/></label>
                    </div>

                    <div class="input-box">
                        <%-- Never restore the submitted password. --%>
                        <input type="password"
                               id="password"
                               name="password"
                               maxlength="128"
                               autocomplete="current-password"
                               placeholder=" "
                               required>
                        <label for="password"><ui:text key="screen.e7cf3ef4f17c3999"/></label>
                    </div>

                    <div class="remember-me">
                        <label for="remember">
                            <input class="form-check-input"
                                   type="checkbox"
                                   name="remember"
                                   id="remember"
                                   value="on"
                                   <%= rememberChecked ? "checked" : ""%>>
                            <ui:text key="screen.0f766c8cd8e7f250"/>
                        </label>

                        <a href="<%= contextPath%>/forgot-password">
                            <ui:text key="screen.30c1d8d3e9128893"/>
                        </a>
                    </div>

                    <button class="btn" type="submit">
                        <ui:text key="screen.bfd402b2f6f38125"/>
                    </button>

                    <div class="mt-3 login-register">
                        <span>
                            <ui:text key="screen.1b545b23fae3997a"/>
                            <a href="<%= contextPath%>/register">
                                <ui:text key="screen.5e2b8e96503d722e"/>
                            </a>
                        </span>
                    </div>
                </form>
            </div>
        </main>

        <footer>
            <ui:text key="screen.e054863fe5b390e5"/>
        </footer>

        <script src="<%= contextPath%>/assets/js/bootstrap.bundle.min.js">
        </script>
    </body>
</html>