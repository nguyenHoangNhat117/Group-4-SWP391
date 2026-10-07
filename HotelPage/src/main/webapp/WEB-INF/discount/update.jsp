<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Update Promotion</title>
    <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="body-wrapper">
    <%@ include file="/WEB-INF/include/header.jsp" %>
    <main class="container py-5 min-vh-100">
        <div class="mx-auto bg-white p-5 rounded shadow" style="max-width: 760px;">
            <h2 class="mb-4 text-center">Update Promotion</h2>

            <c:if test="${not empty param.error}">
                <div class="alert alert-danger">
                    Invalid promotion data. Please check code, numbers and date range.
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/discounts" method="post">
                <input type="hidden" name="id" value="${discount.id}">

                <div class="mb-3">
                    <label class="form-label">Promotion Code</label>
                    <input type="text" name="code" value="${discount.code}"
                           class="form-control" maxlength="50" required>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Quantity</label>
                        <input type="number" name="quantity" value="${discount.quantity}"
                               class="form-control" min="0" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Discount Percent</label>
                        <input type="number" name="sale-off" value="${discount.saleOff}"
                               class="form-control" min="0" max="100" step="0.01" required>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Start Date</label>
                        <input type="date" name="start-date" value="${discount.startDate}"
                               class="form-control">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">End Date</label>
                        <input type="date" name="end-date" value="${discount.endDate}"
                               class="form-control">
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Minimum Booking Amount</label>
                        <input type="number" name="minimum-amount" value="${discount.minimumAmount}"
                               class="form-control" min="0" step="0.01">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Maximum Discount</label>
                        <input type="number" name="maximum-discount" value="${discount.maximumDiscount}"
                               class="form-control" min="0" step="0.01">
                    </div>
                </div>

                <div class="form-check mb-4">
                    <input class="form-check-input" type="checkbox" name="active"
                           id="active" ${discount.active ? 'checked' : ''}>
                    <label class="form-check-label" for="active">Active</label>
                </div>

                <input type="hidden" name="action" value="update">
                <div class="text-end">
                    <button type="submit" class="btn btn-primary">Update</button>
                    <a href="${pageContext.request.contextPath}/discounts"
                       class="btn btn-secondary">Cancel</a>
                </div>
            </form>
        </div>
    </main>
    <%@ include file="/WEB-INF/include/footer.jsp" %>
</div>
<script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
