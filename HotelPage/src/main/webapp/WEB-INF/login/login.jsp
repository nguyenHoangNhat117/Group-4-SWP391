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
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>Login | Luxe Escape</title>

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
                <a href="<%= contextPath%>/#rooms">Rooms</a>
                <a href="<%= contextPath%>/#restaurant-leap">
                    Restaurant
                </a>
                <a href="<%= contextPath%>/booking">Booking</a>
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

                    <h1 class="text-center">Sign in</h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <%= AuthSupport.html(error)%>
                    </div>
                    <% } %>

                    <% if (message != null) {%>
                    <div class="alert alert-success mt-3 auth-message"
                         role="status">
                        <%= AuthSupport.html(message)%>
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
                        <label for="username">Username or Email</label>
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
                        <label for="password">Password</label>
                    </div>

                    <div class="remember-me">
                        <label for="remember">
                            <input class="form-check-input"
                                   type="checkbox"
                                   name="remember"
                                   id="remember"
                                   value="on"
                                   <%= rememberChecked ? "checked" : ""%>>
                            Remember username
                        </label>

                        <a href="<%= contextPath%>/forgot-password">
                            Forgot password?
                        </a>
                    </div>

                    <button class="btn" type="submit">
                        Sign in
                    </button>

                    <div class="mt-3 login-register">
                        <span>
                            Don't have an account?
                            <a href="<%= contextPath%>/register">
                                Sign up
                            </a>
                        </span>
                    </div>
                </form>
            </div>
        </main>

        <footer>
            &copy; 2026 FPT University SE2001 Group 4.
            All rights reserved.
        </footer>

        <script src="<%= contextPath%>/assets/js/bootstrap.bundle.min.js">
        </script>
    </body>
</html>