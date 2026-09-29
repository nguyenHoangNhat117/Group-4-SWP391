<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create Room</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    </head>
    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main class="d-flex flex-column align-items-center pt-3">
                <h1 class="pt-3 fw-bold ms-4">Oops! Page Not Found (404)</h1>
                <div class="w-25">
                    <img class="img-fluid object-fit-cover" src="<%= request.getContextPath()%>/assets/img/404.jpg" alt="404 not found"/>  
                </div>

                <p>The page you're looking for might have been removed or had its name changed.</p>
                <a href="<%= request.getContextPath()%>/" class="fs-3 text-black">Go to Homepage</a>
            </main>
            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>


        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>