<%-- 
    Document   : delete-booking
    Created on : Jul 11, 2025, 8:30:06 PM
    Author     : Đặng Hoàng 
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Delete Room</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">     
    </head>
    <body class="bg-light">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>

            <main class="min-vh-50 d-flex align-items-center justify-content-center">
                <div class="shadow rounded-4 bg-white p-5 w-50">
                    <form action="<%= request.getContextPath()%>/history/cancel-booking" method="post" class="text-center">
                        <h2 class="mb-4">Room Deletion</h2>
                        <p class="fs-5 mb-4">
                            Do you really want to delete Booking ID: <strong>${param.id}</strong>?
                        </p>

                        <input type="hidden" name="id" value="${param.id}"/>

                        <div class="d-flex justify-content-center gap-3">
                            <button type="submit" class="btn btn-danger btn-lg px-4 rounded-3">Yes</button>
                            <a href="./history?id=${sessionScope.customer.customerID}&view=bookings" class="btn btn-secondary btn-lg px-4 rounded-3">No</a>
                        </div>
                    </form>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>

        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>

