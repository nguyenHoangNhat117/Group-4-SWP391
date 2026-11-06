<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title data-i18n="screen.fa6c1175dc521169">${requestScope.i18n['screen.fa6c1175dc521169']}</title>
    <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">

    <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>
<body class="luxe-admin-suite luxe-admin-discounts">
<div class="body-wrapper">
    <%@ include file="/WEB-INF/include/header.jsp" %>
    <main class="container-fluid px-5 py-5 min-vh-100">
        <div class="luxe-admin-banner" aria-label="${requestScope.i18n['screen.bf10d5aa163cf2b9']}" data-i18n-aria-label="screen.bf10d5aa163cf2b9">
          <span class="luxe-admin-banner-eyebrow">LUXE ESCAPE <span aria-hidden="true">/</span> <ui:text key="screen.65345f6b7bf401ea"/></span>
          <span class="luxe-admin-banner-title"><ui:text key="screen.bf10d5aa163cf2b9"/></span>
          <span class="luxe-admin-banner-art" aria-hidden="true"></span>
        </div>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2><ui:text key="screen.fa6c1175dc521169"/></h2>
            <a href="${pageContext.request.contextPath}/discounts?view=create"
               class="btn btn-primary"><ui:text key="screen.3fac12665eec5b15"/></a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover table-bordered align-middle">
                <thead class="table-dark">
                <tr>
                    <th><ui:text key="screen.3843971dcfdee508"/></th>
                    <th><ui:text key="screen.340f463033e0fd5d"/></th>
                    <th><ui:text key="screen.822bab8d41bca8d7"/></th>
                    <th><ui:text key="screen.cb72ec985b5f3d75"/></th>
                    <th><ui:text key="screen.e4bb9f1ece9af926"/></th>
                    <th><ui:text key="screen.f4db1e48476f6075"/></th>
                    <th><ui:text key="screen.735beee2e390c797"/></th>
                    <th><ui:text key="screen.9b713d267b545caf"/></th>
                    <th><ui:text key="screen.920e413c7d411b61"/></th>
                    <th><ui:text key="screen.ff8059dc6752afdd"/></th>
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
                                    <span class="badge bg-success"><ui:text key="screen.92340695899bd2d8"/></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary"><ui:text key="screen.ac7c949f1211b781"/></span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-nowrap">
                            <a href="${pageContext.request.contextPath}/discounts?view=update&id=${discount.id}"
                               class="btn btn-primary btn-sm"><ui:text key="screen.464c4ffd019e1e96"/></a>
                            <a href="${pageContext.request.contextPath}/discounts?view=delete&id=${discount.id}"
                               class="btn btn-danger btn-sm"><ui:text key="screen.fb1e6fa55327160e"/></a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <c:if test="${empty discounts}">
            <div class="alert alert-info"><ui:text key="screen.097deef7a07d962f"/></div>
        </c:if>
    </main>
    <%@ include file="/WEB-INF/include/footer.jsp" %>
</div>
<script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
