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
                    <h2 class="fw-bold text-center mb-4">${loggedUser.role == "admin" ? "Customer (ID: " : ""}${loggedUser.role == "admin" ? param.id : "Your "}${loggedUser.role == "admin" ? ") " : ""}Booking History</h2>

                    <c:choose>
                        <c:when test="${requestScope.bookings != null && not empty requestScope.bookings}">
                            <div class="d-flex flex-column gap-3">
                                <c:forEach var="booking" items="${requestScope.bookings}">
                                    <div class="d-flex flex-column flex-md-row shadow-sm rounded-4 p-3 align-items-md-center justify-content-between bg-white">
                                        <div class="mb-3 mb-md-0">
                                            <a class="fw-semibold text-dark mb-1 fs-4" href="./details?roomNumber=${booking.roomNumber}">Room #${booking.roomNumber}</a>
                                            <c:if test="${loggedUser.role == 'customer'}">     
                                                <p class="mb-1"><strong>Booking ID:</strong> ${booking.id}</p>
                                            </c:if>
                                            <p class="mb-1"><strong>Check-in:</strong> ${booking.checkInDate}</p>
                                            <p class="mb-1"><strong>Check-out:</strong> ${booking.checkOutDate}</p>

                                        </div>
                                        <div class="text-end">
                                            <p class="fw-bold fs-5 text-success mb-2">$${booking.totalPrice}</p>
                                            <c:choose>
                                                <c:when test="${paidMap[booking.id]}">
                                                    <span class="badge bg-success fs-6 px-3 py-2">Paid</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:if test="${loggedUser.role == 'customer'}">
                                                        <form action="payment" method="get" class="d-inline">
                                                            <input type="hidden" name="bookingId" value="${booking.id}" />
                                                            <button class="btn btn-success btn-sm" type="submit">Pay</button>
                                                        </form>

                                                        <a href="./history/cancel-booking?id=${booking.id}" class="btn btn-sm btn-danger">Cancel</a>
                                                    </c:if>
                                                </c:otherwise>
                                            </c:choose>

                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                            <c:if test="${requestScope.bookings != null && not empty requestScope.bookings}">
                                <c:if test="${totalPages > 1}">
                                    <nav aria-label="Page navigation" class="d-flex w-100 justify-content-center mt-4">
                                        <ul class="pagination">
                                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                                <a class="page-link" href="./history?id=${param.id}&view=bookings&page=${currentPage - 1}" tabindex="-1">Previous</a>
                                            </li>
                                            <c:forEach var="i" begin="1" end="${totalPages}">
                                                <li class="page-item">
                                                    <a class="page-link ${currentPage == i ? 'active' : ''}" 
                                                       href="./history?id=${param.id}&view=bookings&page=${i}">${i}</a>
                                                </li>
                                            </c:forEach>
                                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                                <a class="page-link" href="./history?id=${param.id}&view=bookings&page=${currentPage + 1}">Next</a>
                                            </li>
                                        </ul>
                                    </nav>
                                </c:if>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-info text-center mt-5">
                                ${loggedUser.role == "admin" ? "There are" : "You have"} no bookings yet...
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>


        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>

