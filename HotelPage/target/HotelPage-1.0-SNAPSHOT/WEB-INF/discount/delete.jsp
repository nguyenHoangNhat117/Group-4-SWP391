<%-- 
    Document   : delete.jsp
    Created on : Jun 23, 2025, 4:28:15 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Delete Discount</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">     
    </head>
    <body class="bg-light">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main class="min-vh-50 d-flex justify-content-center align-items-center">
                <div class="bg-white shadow rounded-4 p-5 w-100" style="max-width: 600px;">
                    <form action="./discounts?action=delete" method="post" class="text-center">
                        <h2 class="mb-4 text-dark">Discount Deletion</h2>
                        <p class="fs-5 fw-semibold mb-4">
                            Do you really want to Discount with the ID of <strong>${param.id}</strong>?
                        </p>

                        <input type="hidden" name="id" value="${param.id}" />

                        <div class="d-flex justify-content-center gap-3">
                            <button type="submit" class="btn btn-danger btn-lg px-5 py-2 rounded-3">Yes</button>
                            <a href="./discounts" class="btn btn-secondary btn-lg px-5 py-2 rounded-3">No</a>
                        </div>
                    </form>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
