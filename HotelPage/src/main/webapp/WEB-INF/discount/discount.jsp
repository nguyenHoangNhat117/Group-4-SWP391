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
                    <a href="./discounts?view=create" class="btn btn-primary mb-4">Create</a>
                    <c:choose>
                        <c:when test="${discounts != null}">
                            <table class="table table-hover table-bordered" >
                                <caption class="text-end">List of Discounts</caption>
                                <thead class="table-dark">
                                    <tr>
                                        <th scope="col" class="text-center">ID</th>
                                        <th scope="col">Code</th>
                                        <th scope="col">Quantity</th>
                                        <th scope="col">Sale Off</th>
                                        <th scope="col" class="text-center">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="discount" items="${discounts}">
                                        <tr>
                                            <th scope="row" class="text-center">${discount.id}</th>
                                            <td>${discount.code}</td>
                                            <td>${discount.quantity}</td>
                                            <td>${discount.saleOff}%</td>
                                            <td>
                                                <div class="text-center">
                                                    <a href="./discounts?view=update&id=${discount.id}" class="btn btn-primary btn-sm">Edit</a>
                                                    <a href="./discounts?view=delete&id=${discount.id}" class="btn btn-danger btn-sm">Delete</a>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>

                        </c:when>
                        <c:otherwise>
                            <h2 class="mt-3">There are no discounts yet...</h2>
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

