<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
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
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.569a24b2de3553fb">${requestScope.i18n['screen.569a24b2de3553fb']}</title>

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
                <a href="<%= contextPath%>/#rooms"><ui:text key="screen.bd32f6cedb5fb7ae"/></a>
                <a href="<%= contextPath%>/#restaurant-leap">
                    <ui:text key="screen.1212ea9e20949ffc"/>
                </a>
                <a href="<%= contextPath%>/booking"><ui:text key="screen.948e03e7054b919e"/></a>
            </nav>
        </header>

        <main class="form-signin wrapper show">
            <div class="form-box">

                <% if (success != null) {%>
                <h1 class="text-center"><ui:text key="screen.9966be402136ee5e"/></h1>

                <div class="alert alert-success mt-3 auth-message"
                     role="status">
                    <ui:message text="<%= success %>"/>
                </div>

                <a class="btn btn-primary"
                   href="<%= contextPath%>/login">
                    <ui:text key="screen.bfd402b2f6f38125"/>
                </a>

                <% } else {%>

                <form method="post"
                      accept-charset="UTF-8"
                      action="<%= contextPath%>/register">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.token(session)%>">

                    <h1 class="text-center"><ui:text key="screen.bb7234ec12451361"/></h1>

                    <% if (error != null) {%>
                    <div class="alert alert-danger mt-3 auth-message"
                         role="alert">
                        <ui:message text="<%= error %>"/>
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
                        <label for="username"><ui:text key="screen.e3b89e9d33f88e52"/></label>
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
                        <label for="lastName"><ui:text key="screen.7b4888049459f04a"/></label>
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
                        <label for="firstName"><ui:text key="screen.702ef921ed1d89bf"/></label>
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
                        <label for="email"><ui:text key="screen.969ccbd3cf6300ec"/></label>
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
                               title="${requestScope.i18n['screen.44a29b20510265e4']}"
                               placeholder=" "
                               required data-i18n-title="screen.44a29b20510265e4">
                        <label for="phone"><ui:text key="screen.306f1bb20677a622"/></label>
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
                        <label for="password"><ui:text key="screen.e7cf3ef4f17c3999"/></label>
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
                            <ui:text key="screen.5ac265f396a2c586"/>
                        </label>
                    </div>

                    <button class="btn btn-primary" type="submit">
                        <ui:text key="screen.bb7234ec12451361"/>
                    </button>

                    <div class="mt-3 login-register">
                        <span>
                            <ui:text key="screen.e77fea936d3e0118"/>
                            <a href="<%= contextPath%>/login">
                                <ui:text key="screen.bfd402b2f6f38125"/>
                            </a>
                        </span>
                    </div>
                </form>
                <% }%>
            </div>
        </main>

        <footer>
            <ui:text key="screen.e054863fe5b390e5"/>
        </footer>

        <script src="<%= contextPath%>/assets/js/bootstrap.bundle.min.js">
        </script>
    </body>
</html>