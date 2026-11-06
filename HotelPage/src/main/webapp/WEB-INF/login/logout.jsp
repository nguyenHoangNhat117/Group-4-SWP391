<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
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
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.de97b83b09a01c68">${requestScope.i18n['screen.de97b83b09a01c68']}</title>

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
                <a href="<%= contextPath%>/#rooms"><ui:text key="screen.bd32f6cedb5fb7ae"/></a>
                <a href="<%= contextPath%>/booking"><ui:text key="screen.948e03e7054b919e"/></a>
            </nav>
        </header>

        <main class="form-signin wrapper show">
            <div class="form-box">

                <h1 class="text-center"><ui:text key="screen.c1c084addfcc4f90"/></h1>

                <p class="logout-description">
                    <ui:text key="screen.b96ee565123c3b79"/>
                </p>

                <%-- Logout changes session state, so submit using POST. --%>
                <form method="post"
                      action="<%= contextPath%>/logout">

                    <input type="hidden"
                           name="csrfToken"
                           value="<%= AuthSupport.html(logoutToken)%>">

                    <button class="btn btn-primary" type="submit">
                        <ui:text key="screen.48f0d3d397d49f13"/>
                    </button>
                </form>

                <a class="logout-cancel"
                   href="<%= contextPath%>/">
                    <ui:text key="screen.19766ed6ccb2f4a3"/>
                </a>
            </div>
        </main>

        <footer>
            <ui:text key="screen.e054863fe5b390e5"/>
        </footer>

        <script src="<%= contextPath%>/assets/js/bootstrap.bundle.min.js">
        </script>
    </body>
</html>