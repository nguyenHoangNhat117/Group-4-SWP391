<%-- 
    Document   : enter_otp
    Created on : Jul 1, 2025, 6:16:24 PM
    Author     : Trần Nguyễn
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8" />
        <title>Enter OTP</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/login.css" />
    </head>
    <body>
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
                <form action="verify-otp" method="post">
                    <h1 class="text-center">Enter OTP</h1>
                    <%
                        String error = (String) request.getAttribute("error");
                        String message = (String) request.getAttribute("message");
                    %>
                    <% if (error != null) {%>
                    <div class="alert alert-danger text-center form-error"><%= error%></div>
                    <% } %>
                    <% if (message != null) {%>
                    <div class="alert alert-success text-center form-error"><%= message%></div>
                    <% }%>
                    <div class="input-box">
                        <input type="text" name="otp" required placeholder=" " />
                        <label>OTP</label>
                        <span class="icon">
                            <ion-icon name="key-outline"></ion-icon>
                        </span>
                    </div>
                    <button class="btn" type="submit">Verify</button>
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