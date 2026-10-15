<%@page import="util.AuthSupport"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>Register</title>

        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/login.css"
              rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/img/favicon.png"
              rel="icon"
              type="image/png">
    </head>

    <body class="page-register">
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
                      action="<%= request.getContextPath()%>/register">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <h1 class="text-center">Register</h1>

                    <%
                        String error =
                                (String) request.getAttribute("error");
                        String enteredUsername =
                                (String) request.getAttribute("enteredUsername");
                        String enteredEmail =
                                (String) request.getAttribute("enteredEmail");
                    %>

                    <% if (error != null) { %>
                    <div class="alert alert-danger mt-3 text-center text-danger"
                         style="font-size: 0.9rem;">
                        <%= AuthSupport.html(error)%>
                    </div>
                    <% } %>

                    <div class="input-box">
                        <input type="text"
                               id="username"
                               name="username"
                               value="<%= AuthSupport.html(enteredUsername)%>"
                               required
                               autocomplete="username"
                               placeholder=" "
                               minlength="3"
                               maxlength="100">

                        <label for="username">Username</label>

                        <span class="icon">
                            <ion-icon name="person-outline"></ion-icon>
                        </span>
                    </div>

                    <div class="input-box">
                        <input type="email"
                               id="email"
                               name="email"
                               value="<%= AuthSupport.html(enteredEmail)%>"
                               required
                               autocomplete="email"
                               placeholder=" "
                               maxlength="254">

                        <label for="email">Email</label>

                        <span class="icon">
                            <ion-icon name="mail-outline"></ion-icon>
                        </span>
                    </div>

                    <div class="input-box">
                        <input type="password"
                               id="password"
                               name="password"
                               required
                               autocomplete="new-password"
                               placeholder=" "
                               minlength="7"
                               maxlength="128">

                        <label for="password">Password</label>

                        <span class="icon">
                            <ion-icon name="lock-closed-outline"></ion-icon>
                        </span>
                    </div>

                    <div class="input-box">
                        <input type="password"
                               id="confirmPassword"
                               name="confirmPassword"
                               required
                               autocomplete="new-password"
                               placeholder=" "
                               minlength="7"
                               maxlength="128">

                        <label for="confirmPassword">Confirm Password</label>

                        <span class="icon">
                            <ion-icon name="checkmark-circle-outline"></ion-icon>
                        </span>
                    </div>

                    <button class="btn btn-primary" type="submit">
                        Register
                    </button>

                    <div class="mt-3 login-register">
                        <span>
                            Already have an account?
                            <a class="register-link"
                               href="<%= request.getContextPath()%>/login">
                                Sign in
                            </a>
                        </span>
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