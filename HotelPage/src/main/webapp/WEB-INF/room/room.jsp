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
        <title>Hotel Rooms</title>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css" />
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/room.css" />
    </head>
    <body>
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>
            <main>
                <!-- Search bar with destination and date filters -->
                <div class="search-bar">
                    <c:if test="${sessionScope.loggedUser.role == 'admin'}">
                        <a class="btn btn-primary" href="./room?view=create">Create</a>
                    </c:if>
                    <form class="ms-auto w-50 d-flex justify-content-end" method="post" action="<%= request.getContextPath()%>/room?action=search">
                        <div class="w-50 text-center">
                            <input type="text" class="form-control border-3 rounded-4 p-3" id="floatingBookingDate" placeholder="${sessionScope.dates}" name="booking-dates" readonly required>
                        </div>
                        <button class="update-button">Update Search</button>
                    </form>
                </div>

                <!--                 Filter buttons below search bar 
                                <div class="filter-buttons">
                                    <button class="filter-button">All Filters</button>
                                    <button class="filter-button">Price</button>
                                </div>-->


                <!-- List of hotel cards -->
                <div class="hotel-list">
                    <c:forEach var="room" items="${rooms}">
                        <div class="hotel-card w-4">
                            <a href="./details?roomNumber=${room.roomNumber}"><img class="img-fluid" src="<%= request.getContextPath()%>/assets/img/${room.roomType.picture}" alt="${room.roomType.name}"></a>
                            <div class="hotel-details">
                                <div class="hotel-name">Room ${room.roomNumber} (${room.roomType.name})</div>
                                <c:if test="${sessionScope.loggedUser.role == 'admin'}">
                                    <div class="hotel-status ${room.status.equals("available") ? "text-success" : "text-warning"}">${room.status}</div>
                                </c:if>
                                <div class="hotel-info">${room.roomType.description}</div>
                                <a href="./details?roomNumber=${room.roomNumber}" class="details-link">More details</a>
                                <div class="price-rate">
                                    <div class="price">${room.roomType.pricePerNight} USD / Night</div>
                                    <c:if test="${not empty sessionScope.loggedUser}">
                                        <c:choose>
                                            <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                                                <div>
                                                    <a class="btn btn-primary btn-sm" href="./room?view=update&roomNumber=${room.roomNumber}">Edit</a>
                                                    <a class="btn btn-danger btn-sm" href="room?view=delete&roomNumber=${room.roomNumber}">Delete</a>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <a class="view-button text-decoration-none" href="./booking?roomNumber=${room.roomNumber}">Book</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
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
                            <a class="page-link" href="./room?page-index=${param["page-index"] - 1}" tabindex="-1">Previous</a>
                        </li>
                        <c:forEach var = "i" begin = "1" end = "${totalPages}">
                            <li class="page-item" ><a class="page-link ${param["page-index"] == i ? "active" : ""}" href="./room?page-index=${i}">${i}</a></li>
                            </c:forEach>
                        <li class="page-item ${param["page-index"] == requestScope.totalPages ? "disabled" : ""} rounded-4">
                            <a class="page-link" href="./room?page-index=${param["page-index"] + 1}">Next</a>
                        </li>
                    </ul>
                </nav>
            </c:if>

            <%@include file="../include/footer.jsp" %>
        </div>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script>
            const picker = new Litepicker({
                element: document.getElementById('floatingBookingDate'),
                singleMode: false, // for date range
                numberOfMonths: 2, // show two months side by side
                numberOfColumns: 2,
                autoApply: true,
                format: 'MMM D, YYYY',
                tooltipText: {
                    one: 'day',
                    other: 'days'
                },
                minDate: new Date()
            });
        </script>
    </body>
</html>
