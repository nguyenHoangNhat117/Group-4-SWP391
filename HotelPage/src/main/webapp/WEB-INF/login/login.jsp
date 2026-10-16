<%-- 
    Document   : login
    Created on : Oct 14, 2026, 6:16:24 PM
    Author     : Nguyễn Hoàng Nhật
--%>
<%@page import="util.AuthSupport"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>Login</title>

        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/login.css"
              rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-auth.css" />
        <link href="<%= request.getContextPath()%>/assets/img/favicon.png"
              rel="icon"
              type="image/png">
    </head>

    <body class="page-login">
        <header>
            <span>
                <a href="<%= request.getContextPath()%>/">Luxe Escape</a>
            </span>

            <nav class="navigation">
                <a href="<%= request.getContextPath()%>/#rooms">Rooms</a>
                <a href="<%= request.getContextPath()%>/#restaurant-leap">
                    Restaurant
                </a>
                <a href="<%= request.getContextPath()%>/booking">Booking</a>
            </nav>
        </header>

        <main class="form-signin wrapper">
            <div class="form-box">

                <form method="post"
                      action="<%= request.getContextPath()%>/login">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <h1 class="text-center">Sign in</h1>

                    <%
                        String error = (String) request.getAttribute("error");
                        String message = (String) request.getAttribute("message");
                        String enteredUsername =
                                (String) request.getAttribute("enteredUsername");
                    %>

                    <% if (error != null) { %>
                    <div class="alert alert-danger mt-3 text-center text-danger"
                         style="font-size: 0.9rem;">
                        <%= AuthSupport.html(error)%>
                    </div>
                    <% } %>

                    <% if (message != null) { %>
                    <div class="alert alert-success text-center text-success"
                         style="font-size: 0.9rem;">
                        <%= AuthSupport.html(message)%>
                    </div>
                    <% } %>

                    <div class="input-box">
                        <input type="text"
                               id="username"
                               name="username"
                               maxlength="100"
                               required
                               autocomplete="username"
                               value="<%= AuthSupport.html(enteredUsername)%>"
                               placeholder=" ">

                        <label for="username">Username</label>

                        <span class="icon">
                            <ion-icon name="person-outline"></ion-icon>
                        </span>
                    </div>

                    <div class="input-box">
                        <input type="password"
                               id="password"
                               name="password"
                               maxlength="128"
                               required
                               autocomplete="current-password"
                               placeholder=" ">

                        <label for="password">Password</label>

                        <span class="icon">
                            <ion-icon name="lock-closed-outline"></ion-icon>
                        </span>
                    </div>

                    <div class="form-footer">
                        <div class="remember-me">
                            <label for="remember">
                                <input class="form-check-input"
                                       type="checkbox"
                                       name="remember"
                                       id="remember">
                                Remember username
                            </label>

                            <a href="<%= request.getContextPath()%>/forgot-password">
                                Forgot password?
                            </a>
                        </div>

                        <button class="btn" type="submit">
                            Sign in
                        </button>

                        <div class="mt-3 login-register">
                            <span>
                                Don't have an account?
                                <a class="register-link"
                                   href="<%= request.getContextPath()%>/register">
                                    Sign up
                                </a>
                            </span>
                        </div>
                    </div>
                </form>

            </div>
        </main>

        <footer>
            &copy; 2026 FPT University SE2001 Group 4. All rights reserved
        </footer>

        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>

        <script type="module"
                src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js"></script>

        <script nomodule
                src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js"></script>

        <script>
            window.addEventListener("DOMContentLoaded", () => {
                const wrapper = document.querySelector(".wrapper");
                if (wrapper) {
                    wrapper.classList.add("show");
                }
            });
        </script>
    </body>
</html>