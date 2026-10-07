<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Promotion Management</title>
    <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
</head>
<body>
<div class="body-wrapper">
    <%@ include file="/WEB-INF/include/header.jsp" %>
    <main class="container-fluid px-5 py-5 min-vh-100">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Promotion Management</h2>
            <a href="${pageContext.request.contextPath}/discounts?view=create"
               class="btn btn-primary">Create Promotion</a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover table-bordered align-middle">
                <thead class="table-dark">
                <tr>
                    <th>ID</th>
                    <th>Code</th>
                    <th>Quantity</th>
                    <th>Discount</th>
                    <th>Start</th>
                    <th>End</th>
                    <th>Minimum</th>
                    <th>Max Discount</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="discount" items="${discounts}">
                    <tr>
                        <td>${discount.id}</td>
                        <td>${discount.code}</td>
                        <td>${discount.quantity}</td>
                        <td>${discount.saleOff}%</td>
                        <td>${discount.startDate}</td>
                        <td>${discount.endDate}</td>
                        <td>${discount.minimumAmount}</td>
                        <td>${discount.maximumDiscount}</td>
                        <td>
                            <c:choose>
                                <c:when test="${discount.active}">
                                    <span class="badge bg-success">Active</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Inactive</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-nowrap">
                            <a href="${pageContext.request.contextPath}/discounts?view=update&id=${discount.id}"
                               class="btn btn-primary btn-sm">Edit</a>
                            <a href="${pageContext.request.contextPath}/discounts?view=delete&id=${discount.id}"
                               class="btn btn-danger btn-sm">Deactivate</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <c:if test="${empty discounts}">
            <div class="alert alert-info">There are no promotions yet.</div>
        </c:if>
    </main>
    <%@ include file="/WEB-INF/include/footer.jsp" %>
</div>
<script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
