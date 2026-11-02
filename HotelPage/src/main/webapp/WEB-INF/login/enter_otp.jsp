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
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>Verify OTP | Luxe Escape</title>

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
                <h1 class="text-center">Verify OTP</h1>

                <% if (error != null) {%>
                <div class="alert alert-danger mt-3" role="alert">
                    <%= AuthSupport.html(error)%>
                </div>
                <% } %>

                <% if (message != null) {%>
                <div class="alert alert-success mt-3" role="status">
                    <%= AuthSupport.html(message)%>
                </div>
                <% } %>

                <% if (otpAvailable) {%>
                <p class="mt-3" style="color: white;">
                    Enter the 6-digit code from your email.
                    Remaining attempts:
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
                        <label for="otp">OTP</label>
                    </div>

                    <button class="btn" type="submit">
                        Verify
                    </button>
                </form>

                <% } else { %>
                <p class="mt-3" style="color: white;">
                    This code is no longer available.
                    Request a new code to continue.
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
                        Resend OTP
                    </button>
                </form>

                <div class="login-register mt-3">
                    <a href="<%= contextPath%>/forgot-password">
                        Use another email
                    </a>
                    <br>
                    <a href="<%= contextPath%>/login">
                        Back to login
                    </a>
                </div>
            </div>
        </main>

        <footer>
            &copy; 2026 FPT University SE2001 Group 4.
            All rights reserved.
        </footer>
    </body>
</html>