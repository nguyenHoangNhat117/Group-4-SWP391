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
                <div class="w-75 mx-auto mt-5">
                    <c:choose>
                        <c:when test="${customers != null}">
                            <table class="table table-hover table-bordered" >
                                <caption class="text-end">List of Customers</caption>
                                <thead class="table-dark">
                                    <tr>
                                        <th scope="col" class="text-center">ID</th>
                                        <th scope="col">First Name</th>
                                        <th scope="col">Last Name</th>
                                        <th scope="col">Email</th>
                                        <th scope="col">Phone</th>
                                        <th scope="col">Username</th>
                                        <th scope="col">History</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="customer" items="${customers}">
                                        <tr>
                                            <th scope="row" class="text-center">${customer.customerID}</th>
                                            <td>${customer.firstName}</td>
                                            <td>${customer.lastName}</td>
                                            <td>${customer.email}</td>
                                            <td>${customer.phone}</td>
                                            <td>${customer.user.username}</td>
                                            <td><a href="./history?id=${customer.customerID}&view=bookings" class="text-primary text-decoration-none-hover">Show</a></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>

                        </c:when>
                        <c:otherwise>
                            <h2 class="mt-3">There are no customers yet...</h2>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${totalPages > 1}">
                        <nav aria-label="Page navigation" class="d-flex w-100 justify-content-center mt-4">
                            <ul class="pagination">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''} rounded-4">
                                    <a class="page-link" href="?page-index=${currentPage - 1}">Previous</a>
                                </li>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item ${i == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="?page-index=${i}">${i}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''} rounded-4">
                                    <a class="page-link" href="?page-index=${currentPage + 1}">Next</a>
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

