<%-- 
    Document   : payments
    Created on : Jul 9, 2025, 2:33:03 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Review Reports</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    </head>
    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main>
                <div class="container py-4">
                    <h2 class="fw-bold text-center mb-4">Review Report</h2>
                    <c:choose>
                        <c:when test="${not empty reviews}">
                            <table class="table table-bordered table-striped table-hover">
                                <thead class="table-dark">
                                    <tr>
                                        <th>Review ID</th>
                                        <th>Room Number</th>
                                        <th>Customer</th>
                                        <th>Rating</th>
                                        <th>Comment</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="r" items="${reviews}">
                                        <tr>
                                            <td>${r.reviewID}</td>
                                            <td>${r.booking.roomNumber}</td>
                                            <td>${r.booking.customer.user.username}</td>
                                            <td>${r.star} ★</td>
                                            <td>${r.comment}</td>
                                            <td>
                                                <form action="reports/delete-reviews" method="post" onsubmit="return confirm('Are you sure to delete this review?');">
                                                    <input type="hidden" name="reviewID" value="${r.reviewID}" />
                                                    <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-info text-center mt-5">
                                There are no reviews yet...
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
