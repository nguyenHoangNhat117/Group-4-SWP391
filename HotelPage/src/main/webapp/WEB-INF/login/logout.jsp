<%-- 
    Document   : logout
    Created on : Oct 14, 2026, 6:16:24 PM
    Author     : Nguyễn Hoàng Nhật
--%>
<%@page import="util.AuthSupport"%>
<%@page contentType="text/html; charset=UTF-8"
        pageEncoding="UTF-8"
        session="false"%>
<%-- Updated by Nguyen Hoang Nhat --%>
<%
    String contextPath = request.getContextPath();

    // The servlet supplies the token from the existing session.
    String logoutToken = (String) request.getAttribute("logoutToken");
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>Logout | Luxe Escape</title>

        <link href="<%= contextPath%>/assets/css/bootstrap.min.css"
              rel="stylesheet">

        <link href="<%= contextPath%>/assets/css/login.css"
              rel="stylesheet">

        <link href="<%= contextPath%>/assets/css/luxe-auth.css"
              rel="stylesheet">

        <link href="<%= contextPath%>/assets/img/favicon.png"
              rel="icon"
              type="image/png">

        <style>
            .logout-description {
                color: #fff;
                line-height: 1.6;
                text-align: center;
                margin: 24px 0;
            }

            .logout-cancel {
                display: block;
                margin-top: 20px;
                text-align: center;
                color: #fff;
            }

            .logout-cancel:hover {
                color: #fff;
                text-decoration: underline;
            }
        </style>
    </head>

    <body class="page-login">
        <header>
            <span>
                <a href="<%= contextPath%>/">Luxe Escape</a>
            </span>

            <nav class="navigation">
                <a href="<%= contextPath%>/#rooms">Rooms</a>
                <a href="<%= contextPath%>/booking">Booking</a>
            </nav>
        </header>

        <main class="form-signin wrapper show">
            <div class="form-box">

                <h1 class="text-center">Sign out?</h1>

                <p class="logout-description">
                    Confirm to end your session.
                    Your remembered username will also be removed.
                </p>

                <%-- Logout changes session state, so submit using POST. --%>
                <form method="post"
                      action="<%= contextPath%>/logout">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.html(logoutToken)%>">

                    <button class="btn btn-primary" type="submit">
                        Sign out
                    </button>
                </form>

                <a class="logout-cancel"
                   href="<%= contextPath%>/">
                    Cancel
                </a>
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