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
                    <c:when test="${param.error == 'room-exist'}">
                        <p class="alert alert-danger text-center">
                            The room number you entered is already exist. Please edit it or try another number.
                        </p>
                    </c:when>
                </c:choose>
                <div class="form-card shadow rounded-4 bg-white p-5 w-50">
                    <form action="./room?action=create" method="post" class="d-flex flex-column">
                        <h2 class="mb-4 text-center">Room Creation</h2>

                        <div class="mb-4">    
                            <label for="room-number" class="form-label fs-5 fw-semibold">Room Number</label>
                            <input id="room-number" type="number" name="room-number"
                                   class="form-control form-control-lg" required>
                        </div>

                        <div class="mb-4">    
                            <label for="status" class="form-label fs-5 fw-semibold">Status</label>
                            <select id="status" name="status" class="form-select form-select-lg">
                                <option value="available">Available</option>
                                <option value="maintenance">Maintenance</option>
                                <option value="cleaning">Cleaning</option>
                            </select>
                        </div>

                        <div class="mb-4">    
                            <label for="room-type" class="form-label fs-5 fw-semibold">Room Type</label>
                            <select id="room-type" name="room-type-id" class="form-select form-select-lg">
                                <c:forEach var="category" items="${RequestScope.categorys}">
                                    <option value="${category.getId()}">
                                        ${category.getId()} - ${category.getName()}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="text-end">                     
                            <button type="submit" class="btn btn-primary fs-5 px-4 rounded-3">Create</button>
                        </div>  
                    </form>
                </div>
            </main>
            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>

        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
