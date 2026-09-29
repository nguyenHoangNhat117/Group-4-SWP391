<%-- 
    Document   : room
    Created on : Jun 18, 2025, 1:35:35 PM
    Author     : Hà Thái Phát
--%>

<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="model.Room"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Hotel Room Types</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/room.css" />
    </head>
    <body>
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>
            <main>
                <div class="search-bar">
                    <c:if test="${sessionScope.loggedUser.role == 'admin'}">
                        <a class="btn btn-primary" href="./room-type?view=create">Create</a>
                    </c:if>
                </div>
                <!-- List of hotel cards -->
                <div class="hotel-list">
                    <c:forEach var="roomType" items="${roomTypes}">
                        <div class="hotel-card w-4">
                            <img src="<%= request.getContextPath()%>/assets/img/${roomType.picture}" alt="${roomType.name}">
                            <div class="hotel-details">
                                <div class="hotel-name">${roomType.name}</div>
                                <div class="hotel-info">${roomType.description}</div>
                                <div class="price-rate">
                                    <div class="price">${roomType.pricePerNight} USD / Night</div>
                                    <div>
                                        <a href="./room-type?view=update&id=${roomType.id}" class="btn btn-primary btn-sm">Edit</a>
                                        <a href="./room-type?view=delete&roomType=${roomType.name}&id=${roomType.id}" class="btn btn-danger btn-sm">Delete</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </main>
            <c:if test="${requestScope.totalPages > 1}">
                <nav aria-label="Page navigation" class="d-flex w-100 justify-content-center">
                    <ul class="pagination">
                        <li class="page-item ${param["page-index"] == 1 ? "disabled" : ""} rounded-4">
                            <a class="page-link" href="./room-type?page-index=${param["page-index"] - 1}" tabindex="-1">Previous</a>
                        </li>
                        <c:forEach var = "i" begin = "1" end = "${totalPages}">
                            <li class="page-item" ><a class="page-link ${param["page-index"] == i ? "active" : ""}" href="./room-type?page-index=${i}">${i}</a></li>
                            </c:forEach>
                        <li class="page-item ${param["page-index"] == requestScope.totalPages ? "disabled" : ""} rounded-4">
                            <a class="page-link" href="./room-type?page-index=${param["page-index"] + 1}">Next</a>
                        </li>
                    </ul>
                </nav>
            </c:if>
            <%@include file="../include/footer.jsp" %>
        </div>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
