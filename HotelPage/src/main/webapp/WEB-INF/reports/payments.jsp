<%-- 
    Document   : payments
    Created on : Jul 9, 2025, 2:33:03 PM
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
                <div class="container py-5">
                    <div class="d-flex justify-content-end mb-4">
                        <a class="btn btn-primary me-3" href="${pageContext.request.contextPath}/reports?view=month&year=2025">
                            📅 Revenue by Month
                        </a>
                        <a class="btn btn-primary" href="${pageContext.request.contextPath}/reports?view=year">
                            📊 Revenue by Year
                        </a>
                    </div>
                    <h2 class="mb-4 text-center">All Payment Records</h2>

                    <c:choose>
                        <c:when test="${not empty payments}">
                            <div class="table-responsive">
                                <table class="table table-bordered table-hover">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>Payment ID</th>
                                            <th>Customer</th>
                                            <th>Room</th>
                                            <th>Booking Period</th>
                                            <th>Total Price</th>
                                            <th>Discount</th>
                                            <th>Payment Date</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="payment" items="${payments}">
                                            <tr>
                                                <td>${payment.id}</td>
                                                <td>ID: ${payment.booking.customer.customerID}
                                                    ${payment.booking.customer.firstName != null  || payment.booking.customer.lastName != null ? "(" : ""}
                                                    ${payment.booking.customer.firstName != null ? payment.booking.customer.firstName : ""}
                                                    ${payment.booking.customer.lastName != null ? payment.booking.customer.lastName : ""}
                                                    ${payment.booking.customer.firstName != null || payment.booking.customer.lastName != null ? ")" : ""}</td>
                                                <td>Room #${payment.booking.roomNumber}</td>
                                                <td>${payment.booking.checkInDate} to ${payment.booking.checkOutDate}</td>
                                                <td>$${payment.booking.totalPrice}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${payment.discount != null}">
                                                            ${payment.discount.saleOff}%
                                                        </c:when>
                                                        <c:otherwise>
                                                            None
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${payment.paymentDate}</td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                            <c:if test="${requestScope.totalPages > 1}">
                                <nav aria-label="Page navigation" class="d-flex w-100 justify-content-center mt-4">
                                    <ul class="pagination">
                                        <li class="page-item ${currentPage == 1 ? 'disabled' : ''} rounded-4">
                                            <a class="page-link" href="?view=payments&page-index=${currentPage - 1}">Previous</a>
                                        </li>
                                        <c:forEach var="i" begin="1" end="${totalPages}">
                                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                                <a class="page-link" href="?view=payments&page-index=${i}">${i}</a>
                                            </li>
                                        </c:forEach>
                                        <li class="page-item ${currentPage == totalPages ? 'disabled' : ''} rounded-4">
                                            <a class="page-link" href="?view=payments&page-index=${currentPage + 1}">Next</a>
                                        </li>
                                    </ul>
                                </nav>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-warning text-center">
                                No payment records found.
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


