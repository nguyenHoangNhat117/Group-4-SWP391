<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Promotions - Luxe Escape</title>
    <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="body-wrapper">
    <%@ include file="/WEB-INF/include/header.jsp" %>

    <main class="container py-5 min-vh-100">
        <div class="text-center mb-5">
            <h1>Current Promotions</h1>
            <p class="text-muted">Active promotion codes and their applicable conditions.</p>
        </div>

        <c:choose>
            <c:when test="${empty promotions}">
                <div class="alert alert-info text-center">
                    No active promotions are available at the moment.
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-4">
                    <c:forEach var="promotion" items="${promotions}">
                        <div class="col-md-6 col-lg-4">
                            <div class="card h-100 shadow-sm">
                                <div class="card-body">
                                    <h4 class="card-title">${promotion.code}</h4>
                                    <p class="fs-3 fw-bold mb-3">${promotion.saleOff}% OFF</p>

                                    <p class="mb-1">
                                        <strong>Available quantity:</strong> ${promotion.quantity}
                                    </p>
                                    <p class="mb-1">
                                        <strong>Valid from:</strong>
                                        <c:choose>
                                            <c:when test="${not empty promotion.startDate}">${promotion.startDate}</c:when>
                                            <c:otherwise>No start limit</c:otherwise>
                                        </c:choose>
                                    </p>
                                    <p class="mb-1">
                                        <strong>Valid until:</strong>
                                        <c:choose>
                                            <c:when test="${not empty promotion.endDate}">${promotion.endDate}</c:when>
                                            <c:otherwise>No end limit</c:otherwise>
                                        </c:choose>
                                    </p>
                                    <p class="mb-1">
                                        <strong>Minimum booking:</strong>
                                        <c:choose>
                                            <c:when test="${not empty promotion.minimumAmount}">${promotion.minimumAmount} USD</c:when>
                                            <c:otherwise>None</c:otherwise>
                                        </c:choose>
                                    </p>
                                    <p class="mb-0">
                                        <strong>Maximum discount:</strong>
                                        <c:choose>
                                            <c:when test="${not empty promotion.maximumDiscount}">${promotion.maximumDiscount} USD</c:when>
                                            <c:otherwise>No maximum</c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </main>

    <%@ include file="/WEB-INF/include/footer.jsp" %>
</div>
<script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
