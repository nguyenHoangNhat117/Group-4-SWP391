<%-- 
    Document   : list
    Created on : Jun 26, 2025, 10:11:27 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Booking Reports</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    </head>
    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main>
                <div class="container py-4">
                    <h2 class="fw-bold text-center mb-4">Booking Report</h2>

                    <c:choose>
                        <c:when test="${requestScope.bookings != null}">
                            <div class="d-flex flex-column gap-3">
                                <c:forEach var="booking" items="${requestScope.bookings}">
                                    <div class="d-flex flex-column flex-md-row shadow-sm rounded-4 p-3 align-items-md-center justify-content-between bg-white">
                                        <div class="mb-3 mb-md-0">
                                            <h5 class="fw-semibold text-dark mb-1"><a href="./details?roomNumber=${booking.roomNumber}" class="text-black fw-bold">Room #${booking.roomNumber}</a></h5>
                                            <p class="mb-1"><strong>Booking ID:</strong> ${booking.id}</p>
                                            <p class="mb-1"><strong>Customer ID:</strong> ${booking.customer.customerID}</p>
                                            <p class="mb-1"><strong>Check-in:</strong> ${booking.checkInDate}</p>
                                            <p class="mb-1"><strong>Check-out:</strong> ${booking.checkOutDate}</p>
                                        </div>
                                        <div class="text-end">
                                            <p class="fw-bold fs-5 text-success mb-1">$${booking.totalPrice}</p>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-info text-center mt-5">
                                There are no bookings yet...
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${requestScope.totalPages > 1}">
                        <nav aria-label="Page navigation" class="d-flex w-100 justify-content-center mt-4">
                            <ul class="pagination">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''} rounded-4">
                                    <a class="page-link" href="?view=bookings&page-index=${currentPage - 1}">Previous</a>
                                </li>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item ${i == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="?view=bookings&page-index=${i}">${i}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''} rounded-4">
                                    <a class="page-link" href="?view=bookings&page-index=${currentPage + 1}">Next</a>
                                </li>
                            </ul>
                        </nav>
                    </c:if>
                </div>
            </main>
            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>

