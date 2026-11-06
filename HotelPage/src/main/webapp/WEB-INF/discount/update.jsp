<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title data-i18n="screen.b42ad76d868a2f21">${requestScope.i18n['screen.b42ad76d868a2f21']}</title>
    <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>
<body class="bg-light">
<div class="body-wrapper">
    <%@ include file="/WEB-INF/include/header.jsp" %>
    <main class="container py-5 min-vh-100">
        <div class="mx-auto bg-white p-5 rounded shadow" style="max-width: 760px;">
            <h2 class="mb-4 text-center"><ui:text key="screen.b42ad76d868a2f21"/></h2>

            <c:if test="${not empty param.error}">
                <div class="alert alert-danger">
                    <ui:text key="screen.9b832eed65a5b117"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/discounts" method="post">
                <input type="hidden" name="id" value="${discount.id}">

                <div class="mb-3">
                    <label class="form-label"><ui:text key="screen.fd947a841a798fb7"/></label>
                    <input type="text" name="code" value="${discount.code}"
                           class="form-control" maxlength="50" required>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label"><ui:text key="screen.822bab8d41bca8d7"/></label>
                        <input type="number" name="quantity" value="${discount.quantity}"
                               class="form-control" min="0" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label"><ui:text key="screen.7720e4889c1762e2"/></label>
                        <input type="number" name="sale-off" value="${discount.saleOff}"
                               class="form-control" min="0" max="100" step="0.01" required>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label"><ui:text key="screen.4c52faad5dc6e248"/></label>
                        <input type="date" name="start-date" value="${discount.startDate}"
                               class="form-control">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label"><ui:text key="screen.e970951cfb6b60b1"/></label>
                        <input type="date" name="end-date" value="${discount.endDate}"
                               class="form-control">
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label"><ui:text key="screen.261e63f6216d3e14"/></label>
                        <input type="number" name="minimum-amount" value="${discount.minimumAmount}"
                               class="form-control" min="0" step="0.01">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label"><ui:text key="screen.07c67858698c4fe6"/></label>
                        <input type="number" name="maximum-discount" value="${discount.maximumDiscount}"
                               class="form-control" min="0" step="0.01">
                    </div>
                </div>

                <div class="form-check mb-4">
                    <input class="form-check-input" type="checkbox" name="active"
                           id="active" ${discount.active ? 'checked' : ''}>
                    <label class="form-check-label" for="active"><ui:text key="screen.92340695899bd2d8"/></label>
                </div>

                <input type="hidden" name="action" value="update">
                <div class="text-end">
                    <button type="submit" class="btn btn-primary"><ui:text key="screen.c1c1009d3f37ec05"/></button>
                    <a href="${pageContext.request.contextPath}/discounts"
                       class="btn btn-secondary"><ui:text key="screen.19766ed6ccb2f4a3"/></a>
                </div>
            </form>
        </div>
    </main>
    <%@ include file="/WEB-INF/include/footer.jsp" %>
</div>
<script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
