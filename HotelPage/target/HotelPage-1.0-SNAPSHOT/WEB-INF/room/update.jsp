<%-- 
    Document   : create
    Created on : May 26, 2025, 9:46:21 AM
    Author     : Đặng Hoàng Vũ
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create Room</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>

            <main class="min-vh-50 d-flex flex-column align-items-center justify-content-center">
                <c:choose>
                    <c:when test="${param.error == 'missing-params'}">
                        <p class="alert alert-danger text-center">
                            Missing parameters in the form. Please try again.
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'room-not-exist'}">
                        <p class="alert alert-danger text-center">
                            The submitted room number is not existed. Please try again.
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'room-different'}">
                        <p class="alert alert-danger text-center">
                            The submitted room number is not different from the original. Please try again.
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'number-format'}">
                        <p class="alert alert-danger text-center">
                            The submitted room number is not a positive number. Please try again.
                        </p>
                    </c:when>
                </c:choose>
                <div class="shadow rounded-4 bg-white p-5 w-50">
                    <form action="./room?action=update" method="post" class="d-flex flex-column">
                        <h2 class="mb-4 text-center">Room Update</h2>

                        <div class="mb-4">
                            <label for="room-number" class="form-label fs-5 fw-semibold">Room Number</label>
                            <input id="room-number" type="number" value="${param.roomNumber}" disabled
                                   class="form-control form-control-lg">
                            <input type="hidden" name="room-number" value="${param.roomNumber}" />
                        </div>

                        <div class="mb-4">
                            <label for="status" class="form-label fs-5 fw-semibold">Status</label>
                            <select id="status" name="status" class="form-select form-select-lg">
                                <option value="available" ${requestScope.room.status == "available" ? "selected" : ""}>Available</option>
                                <option value="maintenance" ${requestScope.room.status == "maintenance" ? "selected" : ""}>Maintenance</option>
                                <option value="cleaning" ${requestScope.room.status == "cleaning" ? "selected" : ""}>Cleaning</option>
                            </select>
                        </div>

                        <div class="mb-4">
                            <label for="room-type" class="form-label fs-5 fw-semibold">Room Type</label>
                            <select id="room-type" name="room-type-id" class="form-select form-select-lg">
                                <c:forEach var="roomType" items="${roomTypes}">
                                    <option value="${roomType.id}" ${requestScope.room.roomType.id == roomType.id ? "selected" : ""}>
                                        ${roomType.getId()} - ${roomType.getName()}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="text-end">
                            <button type="submit" class="btn btn-primary fs-5 px-4 rounded-3">Update</button>
                        </div>
                    </form>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>

        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
