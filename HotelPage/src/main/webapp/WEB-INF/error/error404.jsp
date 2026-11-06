<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title data-i18n="screen.3f855ba1babcbd8a">${requestScope.i18n['screen.3f855ba1babcbd8a']}</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>
    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main class="d-flex flex-column align-items-center pt-3">
                <h1 class="pt-3 fw-bold ms-4"><ui:text key="screen.801a308be1879f70"/></h1>
                <div class="w-25">
                    <img class="img-fluid object-fit-cover" src="<%= request.getContextPath()%>/assets/img/404.jpg" alt="${requestScope.i18n['screen.2aab0875a8e87d72']}" data-i18n-alt="screen.2aab0875a8e87d72"/>  
                </div>

                <p><ui:text key="screen.e665e7520e63cd83"/></p>
                <a href="<%= request.getContextPath()%>/" class="fs-3 text-black"><ui:text key="screen.f219040fd0c28b52"/></a>
            </main>
            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>


        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>