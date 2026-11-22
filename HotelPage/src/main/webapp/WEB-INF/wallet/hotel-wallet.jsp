<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Ví khách sạn - Luxe Escape</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/general.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/luxe-modern.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">

    <style>
        .wallet-shortcuts {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(min(100%, 260px), 1fr));
            gap: 16px;
        }

        .wallet-shortcut {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            padding: 18px 20px;
            border: 1px solid #d9e5de;
            border-radius: 14px;
            background: #edf3ef;
            color: #245940;
            font-weight: 600;
            text-decoration: none;
        }

        .wallet-shortcut:hover {
            background: #dfece4;
            color: #18392f;
        }

        .wallet-shortcut:focus-visible {
            outline: 2px solid #245940;
            outline-offset: 3px;
        }

        body {
            background: #faf7f2;
            color: #32291f;
        }

        .hotel-wallet-container {
            max-width: 1250px;
            margin: 50px auto;
            padding: 20px;
        }

        .hotel-wallet-title {
            font-family: Georgia, serif;
            font-size: 38px;
            margin-bottom: 10px;
        }

        .wallet-summary {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin: 30px 0;
        }

        .wallet-stat {
            background: #fff;
            border: 1px solid #e8dfd4;
            border-radius: 16px;
            padding: 25px;
            box-shadow: 0 4px 15px rgba(0,0,0,.03);
        }

        .wallet-stat h4 {
            font-size: 14px;
            color: #8b755e;
            margin-bottom: 15px;
        }

        .wallet-stat .amount {
            font-size: 25px;
            font-weight: bold;
            color: #32291f;
        }

        .wallet-panel {
            background: white;
            padding: 25px;
            border: 1px solid #e8dfd4;
            border-radius: 16px;
            margin-top: 25px;
        }

        .wallet-table th {
            background: #f4eee6;
            color: #745e47;
            padding: 15px;
        }

        .wallet-table td {
            padding: 15px;
            vertical-align: middle;
        }

        .wallet-link {
            background: #b49a78;
            color: white;
            padding: 10px 18px;
            border-radius: 8px;
            text-decoration: none;
            display: inline-block;
        }

        .wallet-link:hover {
            background: #947955;
            color: white;
        }
    </style>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/management.css">
    <%@ include file="/WEB-INF/include/date-picker.jsp" %>
</head>

<body class="management-page ">

<jsp:include page="/WEB-INF/include/header.jsp"/>

<main class="hotel-wallet-container">

    <div class="management-hero"><div><div class="eyebrow">LUXE ESCAPE</div><h1><ui:text key="management.wallet"/></h1><p><ui:text key="management.walletDescription"/></p></div></div>



    <div class="wallet-shortcuts">
        <a class="wallet-shortcut" href="${pageContext.request.contextPath}/withdrawal-management">
            <ui:text key="management.withdrawals"/>
            <span aria-hidden="true">→</span>
        </a>
        <a class="wallet-shortcut" href="${pageContext.request.contextPath}/wallet-management">
            <ui:text key="management.customerWallet"/>
            <span aria-hidden="true">→</span>
        </a>
    </div>

    <div class="wallet-summary">

        <div class="wallet-stat">
            <h4><ui:text key="management.totalPaid"/></h4>
            <div class="amount">
                <fmt:formatNumber
                    value="${totalPayments}"
                    maxFractionDigits="0"/>
                VND
            </div>
        </div>

        <div class="wallet-stat">
            <h4><ui:text key="management.totalRefunds"/></h4>
            <div class="amount">
                <fmt:formatNumber
                    value="${totalRefunds}"
                    maxFractionDigits="0"/>
                VND
            </div>
        </div>

        <div class="wallet-stat">
            <h4><ui:text key="management.activePayments"/></h4>
            <div class="amount">
                <fmt:formatNumber
                    value="${activePayments}"
                    maxFractionDigits="0"/>
                VND
            </div>
        </div>

        <div class="wallet-stat">
            <h4><ui:text key="management.customerBalances"/></h4>
            <div class="amount">
                <fmt:formatNumber
                    value="${customerBalances}"
                    maxFractionDigits="0"/>
                VND
            </div>
        </div>

    </div>

    <div class="wallet-panel">

        <h3 class="mb-4"><ui:text key="management.transactionsTitle"/></h3>

        <form method="get" class="management-filters" action="${pageContext.request.contextPath}/hotel-wallet"><input type="hidden" name="sort" value="${param.sort == 'asc' ? 'asc' : param.sort == 'desc' ? 'desc' : ''}"><div><label for="paymentId"><ui:text key="management.paymentId"/></label><input id="paymentId" name="paymentId" class="form-control" type="text" inputmode="numeric" pattern="[0-9]{1,9}" value="<c:out value='${param.paymentId}'/>"></div><div><label for="method"><ui:text key="management.method"/></label><select class="form-select" id="method" name="method"><option value="">${requestScope.i18n['management.all']}</option><c:forEach var="option" items="${paymentMethods}"><option value="<c:out value='${option}'/>" ${param.method == option ? 'selected' : ''}><ui:message text="${option}"/></option></c:forEach></select></div><div><label for="paymentStatus"><ui:text key="management.paymentStatus"/></label><select class="form-select" id="paymentStatus" name="paymentStatus"><option value="">${requestScope.i18n['management.all']}</option><c:forEach var="option" items="${paymentStatuses}"><option value="<c:out value='${option}'/>" ${param.paymentStatus == option ? 'selected' : ''}><ui:message text="${option}"/></option></c:forEach></select></div><div><label for="bookingStatus"><ui:text key="management.bookingStatus"/></label><select class="form-select" id="bookingStatus" name="bookingStatus"><option value="">${requestScope.i18n['management.all']}</option><c:forEach var="option" items="${bookingStatuses}"><option value="<c:out value='${option}'/>" ${param.bookingStatus == option ? 'selected' : ''}><ui:message text="${option}"/></option></c:forEach></select></div><div><label for="paymentDate"><ui:text key="management.paymentDate"/></label><input id="paymentDate" class="form-control" type="date" name="date" value="<c:out value='${param.date}'/>"></div><button class="btn btn-primary" type="submit"><ui:text key="room.admin.applyFilters"/></button><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/hotel-wallet"><ui:text key="room.admin.clearFilters"/></a></form>
<div class="table-responsive">

            <table class="table wallet-table">

                <thead>
                    <tr>
                        <th><ui:text key="management.paymentId"/></th>
                        <th><ui:text key="management.bookingId"/></th>
                        <th><button type="button" class="management-sort" data-sort><ui:text key="management.amount"/></button></th>
                        <th><ui:text key="management.method"/></th>
                        <th><ui:text key="management.paymentStatus"/></th>
                        <th><ui:text key="management.bookingStatus"/></th>
                        <th><ui:text key="management.paymentDate"/></th>
                    </tr>
                </thead>

                <tbody>

                    <c:forEach var="p" items="${payments}">

                        <tr>
                            <td>
                                <c:out value="${p.paymentId}"/>
                            </td>

                            <td>
                                #<c:out value="${p.bookingId}"/>
                            </td>

                            <td>
                                <fmt:formatNumber
                                    value="${p.amount}"
                                    maxFractionDigits="0"/>
                                VND
                            </td>

                            <td>
                                <ui:message text="${p.method}"/>
                            </td>

                            <td>
                                <ui:message text="${p.status}"/>
                            </td>

                            <td>
                                <ui:message text="${p.bookingStatus}"/>
                            </td>

                            <td>
                                <c:out value="${p.date}"/>
                            </td>
                        </tr>

                    </c:forEach>

                    <c:if test="${empty payments}">
                        <tr>
                            <td colspan="7"
                                class="text-center">
                                <ui:text key="management.noPayments"/>
                            </td>
                        </tr>
                    </c:if>

                </tbody>
            </table><nav aria-label="Phân trang"><ul class="pagination"><li class="page-item ${currentPage <= 1 ? 'disabled' : ''}"><button type="button" class="page-link" data-page="${currentPage - 1}" ${currentPage <= 1 ? 'disabled' : ''} aria-label="${requestScope.i18n['screen.1208ec01f223c616']}">‹</button></li><c:forEach var="n" begin="${currentPage > 2 ? currentPage - 2 : 1}" end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}"><c:url var="pageUrl" value="/hotel-wallet"><c:param name="page" value="${n}"/><c:param name="paymentId" value="${param.paymentId}"/><c:param name="method" value="${param.method}"/><c:param name="paymentStatus" value="${param.paymentStatus}"/><c:param name="bookingStatus" value="${param.bookingStatus}"/><c:param name="date" value="${param.date}"/><c:param name="sort" value="${param.sort}"/></c:url><li class="page-item ${n == currentPage ? 'active' : ''}"><a class="page-link" href="<c:out value='${pageUrl}'/>" ${n == currentPage ? 'aria-current="page"' : ''}>${n}</a></li></c:forEach><li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}"><button type="button" class="page-link" data-page="${currentPage + 1}" ${currentPage >= totalPages ? 'disabled' : ''} aria-label="${requestScope.i18n['screen.c08ac736a5e29c29']}">›</button></li></ul><p class="text-center text-muted"><ui:text key="management.page"/> ${currentPage} / ${totalPages} · ${totalPaymentsCount} <ui:text key="management.transactions"/></p></nav>
        </div>
    </div>

</main>

<script src="${pageContext.request.contextPath}/assets/js/management.js"></script>
</body>
</html>
