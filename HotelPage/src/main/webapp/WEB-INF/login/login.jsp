<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Login</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/login.css" />
    </head>
    <body class="page-login">
        <header>
            <span><a href="<%= request.getContextPath()%>/">Luxe Escape</a></span>
            <nav class="navigation">
                <a href="<%= request.getContextPath()%>/#rooms">Rooms</a>
                <a href="<%= request.getContextPath()%>/#restaurant-leap">Restaurant</a>
                <a href="<%= request.getContextPath()%>/booking">Booking</a>
            </nav>
        </header>

        <main class="form-signin wrapper">

            <div class="form-box">
                <form method="post" action="<%= request.getContextPath()%>/login">
                    <h1 class="text-center">Sign in</h1>

                    <%
                        String error = (String) request.getAttribute("error");
                        String enteredUsername = (String) request.getAttribute("enteredUsername");
                        if (error != null) {
                    %>
                    <div class="alert alert-danger mt-3 text-center text-danger" style="font-size: 0.9rem;">
                        <%= error%>
                    </div>
                    <% }%>

                    <%
                        String message = (String) request.getAttribute("message");
                        if (message != null) {
                    %>
                    <div class="alert alert-success text-center text-success" style="font-size: 0.9rem;">
                        <%= message%>
                    </div>
                    <% }%>

                    <div class="input-box">
                        <input
                            type="text"
                            name="username"
                            required
                            autocomplete="off"
                            value="<%= enteredUsername != null ? enteredUsername : ""%>"
                            placeholder=" "/>
                        <label for="username">Username</label>
                        <span class="icon">
                            <ion-icon name="person-outline"></ion-icon>
                        </span>
                    </div>

                    <div class="input-box">
                        <input type="password" name="password" id="password" required autocomplete="off" placeholder=" "/>
                        <label for="password">Password</label>
                        <span class="icon">
                            <ion-icon name="lock-closed-outline"></ion-icon>
                        </span>
                    </div>

                    <div class="form-footer">
                        <div class="remember-me">
                            <label>
                                <input class="form-check-input" type="checkbox" name="remember" id="remember" />
                                Remember me
                            </label>
                            <a href="<%= request.getContextPath()%>/forgot-password">
                                Forgot password?
                            </a>
                        </div>

                        <button class="btn" type="submit">Sign in</button>

                        <div class="mt-3 login-register">
                            <span>
                                Don't have an account?
                                <a class="register-link" href="<%= request.getContextPath()%>/register">Sign up</a>
                            </span>
                        </div>
                    </div>
                </form>
            </div>
        </main>

        <footer>
            &copy; 2025 FPT University SE1908 Group 3. All right reserved
        </footer>

        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js"></script>
        <script nomodule src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js"></script>
        <script>
            window.addEventListener("DOMContentLoaded", () => {
                document.querySelector(".wrapper").classList.add("show");
            });
        </script>
        <script>
            const link = document.createElement('link');
            link.rel = 'icon';
            link.type = 'image/png';
            link.href = '<%= request.getContextPath()%>/assets/img/favicon.png?v=' + new Date().getTime();
            document.head.appendChild(link);
        </script>
    </body>
</html>