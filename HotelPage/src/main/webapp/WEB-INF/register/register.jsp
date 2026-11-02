<%@page import="util.AuthSupport"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Updated by Nguyen Hoang Nhat --%>
<%
    String contextPath = request.getContextPath();
    String error = (String) request.getAttribute("error");

    // Consume the one-time success message.
    String success = (String) session.getAttribute("registrationSuccess");

    if (success != null) {
        session.removeAttribute("registrationSuccess");
    }

    String enteredUsername
            = (String) request.getAttribute("enteredUsername");
    String enteredEmail
            = (String) request.getAttribute("enteredEmail");
    String enteredFirstName
            = (String) request.getAttribute("enteredFirstName");
    String enteredLastName
            = (String) request.getAttribute("enteredLastName");
    String enteredPhone
            = (String) request.getAttribute("enteredPhone");
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>Register | Luxe Escape</title>

        <link href="<%= contextPath%>/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/css/login.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/css/luxe-auth.css"
              rel="stylesheet">
        <link href="<%= contextPath%>/assets/img/favicon.png"
              rel="icon" type="image/png">

        <%@include file="/WEB-INF/include/auth-password-toggle.jsp"%>

        <style>
            /* Reduce spacing in the longer registration form. */
            .page-register .wrapper {
                min-height: auto;
            }

            .page-register .input-box {
                margin: 20px 0;
            }

            .page-register .wrapper .form-box {
                padding-top: 2rem;
                padding-bottom: 2rem;
            }
        </style>
    </head>

    <body class="page-register">
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

                <% if (success != null) {%>
                <h1 class="text-center">Account Created</h1>

                <div class="alert alert-success mt-3 auth-message"
                     role="status">
                    <%= AuthSupport.html(success)%>
                </div>

                <a class="btn btn-primary"
                   href="<%= contextPath%>/login">
                    Sign in
                </a>

                <% } else {%>

                <form method="post"
                      accept-charset="UTF-8"
                      action="<%= contextPath%>/register">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <h1 class="text-center">Register</h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <%= AuthSupport.html(error)%>
                    </div>
                    <% }%>

                    <div class="input-box">
                        <input type="text"
                               id="username"
                               name="username"
                               value="<%= AuthSupport.html(enteredUsername)%>"
                               minlength="3"
                               maxlength="50"
                               autocomplete="username"
                               placeholder=" "
                               required>
                        <label for="username">Username</label>
                    </div>

                    <div class="input-box">
                        <input type="text"
                               id="lastName"
                               name="lastName"
                               value="<%= AuthSupport.html(enteredLastName)%>"
                               maxlength="50"
                               autocomplete="family-name"
                               placeholder=" "
                               required>
                        <label for="lastName">Last name</label>
                    </div>

                    <div class="input-box">
                        <input type="text"
                               id="firstName"
                               name="firstName"
                               value="<%= AuthSupport.html(enteredFirstName)%>"
                               maxlength="50"
                               autocomplete="given-name"
                               placeholder=" "
                               required>
                        <label for="firstName">First name</label>
                    </div>

                    <div class="input-box">
                        <input type="email"
                               id="email"
                               name="email"
                               value="<%= AuthSupport.html(enteredEmail)%>"
                               maxlength="100"
                               autocomplete="email"
                               placeholder=" "
                               required>
                        <label for="email">Email</label>
                    </div>

                    <div class="input-box">
                        <input type="tel"
                               id="phone"
                               name="phone"
                               value="<%= AuthSupport.html(enteredPhone)%>"
                               minlength="10"
                               maxlength="11"
                               pattern="[0-9]{10,11}"
                               inputmode="numeric"
                               autocomplete="tel"
                               title="Enter 10 or 11 digits."
                               placeholder=" "
                               required>
                        <label for="phone">Phone number</label>
                    </div>

                    <%-- Password fields remain empty after errors. --%>
                    <div class="input-box">
                        <input type="password"
                               id="password"
                               name="password"
                               minlength="8"
                               maxlength="128"
                               autocomplete="new-password"
                               placeholder=" "
                               required>
                        <label for="password">Password</label>
                    </div>

                    <div class="input-box">
                        <input type="password"
                               id="confirmPassword"
                               name="confirmPassword"
                               minlength="8"
                               maxlength="128"
                               autocomplete="new-password"
                               placeholder=" "
                               required>
                        <label for="confirmPassword">
                            Confirm password
                        </label>
                    </div>

                    <button class="btn btn-primary" type="submit">
                        Register
                    </button>

                    <div class="mt-3 login-register">
                        <span>
                            Already have an account?
                            <a href="<%= contextPath%>/login">
                                Sign in
                            </a>
                        </span>
                    </div>
                </form>
                <% }%>
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