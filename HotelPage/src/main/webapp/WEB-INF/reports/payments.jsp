<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<fmt:setLocale value="vi_VN" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}" class="hotel-reports-page">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title data-i18n="screen.a0bcbfd785ede222">${requestScope.i18n['screen.a0bcbfd785ede222']}</title>

    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/general.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-modern.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-shared-shell.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-ui.css">
    <link rel="stylesheet" href="${ctx}/assets/css/hotel-reports.css">
</head>

<body class="hotel-reports-body ${cookie.animation.value == 'disable' ? 'disable-animation' : ''}">

<%@ include file="/WEB-INF/include/header.jsp" %>

<main class="hotel-reports">

    <%-- Shared heading, navigation and date filter. --%>
    <%@ include file="report-list-toolbar.jsp" %>

    <%-- Hide the results panel when loading or validation fails. --%>
    <c:if test="${empty reportError}">

        <section class="report-panel">

            <div class="report-panel-heading">
                <h2><ui:text key="screen.fcce2e6a27bbcd2c"/></h2>

                <span class="report-meta">
                    ${totalRecords} <ui:text key="screen.19c42541889b0f94"/> ${fn:length(payments)} <ui:text key="screen.c36ee348ff2d354a"/>
                </span>
            </div>

            <c:choose>

                <c:when test="${not empty payments}">

                    <div class="report-table-scroll">

                        <table class="report-table">

                            <thead>
                            <tr>
                                <th scope="col"><ui:text key="screen.7048f38b33f135df"/></th>
                                <th scope="col"><ui:text key="screen.bf3763383aaf4306"/></th>
                                <th scope="col"><ui:text key="screen.948e03e7054b919e"/></th>
                                <th scope="col"><ui:text key="screen.fe43f0a1373a3d68"/></th>

                                <th scope="col" class="numeric">
                                    <ui:text key="screen.dac7b8fa08bd5f4e"/>
                                </th>

                                <th scope="col" class="numeric">
                                    <ui:text key="screen.7c4d078344a4d56a"/>
                                </th>

                                <th scope="col"><ui:text key="screen.52a0f9b65b278850"/></th>
                                <th scope="col"><ui:text key="screen.920e413c7d411b61"/></th>
                                <th scope="col"><ui:text key="screen.8c6a0024dad9f069"/></th>
                                <th scope="col"><ui:text key="screen.346055a5f7d303b7"/></th>
                                <th scope="col"><ui:text key="screen.65d5165f258772f9"/></th>
                            </tr>
                            </thead>

                            <tbody>

                            <c:forEach var="payment" items="${payments}">

                                <tr>

                                    <td>
                                        <strong>#${payment.id}</strong>
                                    </td>

                                    <td>
                                        <c:choose>

                                            <c:when test="${not empty payment.booking.customer.firstName
                                                            or not empty payment.booking.customer.lastName}">
                                                <c:out value="${payment.booking.customer.firstName}" />
                                                <c:out value="${payment.booking.customer.lastName}" />
                                            </c:when>

                                            <c:otherwise>
                                                <ui:text key="screen.641d305570b93564"/>${payment.booking.customer.customerID}
                                            </c:otherwise>

                                        </c:choose>
                                    </td>

                                    <td class="report-nowrap">

                                        <strong>#${payment.booking.id}</strong>

                                        <span class="report-muted">
                                            <c:out value="${fn:replace(payment.booking.status, '_', ' ')}" />
                                        </span>

                                    </td>

                                    <td>
                                        <c:choose>

                                            <c:when test="${not empty payment.booking.details}">

                                                <c:forEach var="detail"
                                                           items="${payment.booking.details}">

                                                    <div class="report-room">

                                                        <strong>
                                                            <a href="${ctx}/details?roomNumber=${detail.roomNumber}">
                                                                <ui:text key="screen.6c8c6af24c9b021c"/>${detail.roomNumber}
                                                            </a>
                                                        </strong>

                                                        <span class="report-muted">
                                                            ${detail.checkInDate}
                                                            → ${detail.checkOutDate}
                                                        </span>

                                                        <span class="report-muted">
                                                            ${detail.guestCount} <ui:text key="screen.21d1f8ed9293da7a"/>
                                                        </span>

                                                    </div>

                                                </c:forEach>

                                            </c:when>

                                            <c:otherwise>
                                                <span class="report-muted">
                                                    <ui:text key="screen.448d8aa4cbfe963c"/>
                                                </span>
                                            </c:otherwise>

                                        </c:choose>
                                    </td>

                                    <td class="numeric">
                                        <fmt:formatNumber
                                                value="${payment.booking.totalPrice}"
                                                pattern="#,##0.##" />
                                        ₫
                                    </td>

                                    <td class="numeric">
                                        <strong>
                                            <fmt:formatNumber
                                                    value="${payment.amount}"
                                                    pattern="#,##0.##" />
                                            ₫
                                        </strong>
                                    </td>

                                    <td class="report-nowrap">
                                        <c:choose>

                                            <c:when test="${payment.paymentMethod == 'cash'}">
                                                <ui:text key="screen.e2da108f63008e44"/>
                                            </c:when>

                                            <c:when test="${payment.paymentMethod == 'bank_transfer'}">
                                                <ui:text key="screen.d69d587c81659616"/>
                                            </c:when>

                                            <c:when test="${payment.paymentMethod == 'credit_card'}">
                                                <ui:text key="screen.4ddae6e2f13784aa"/>
                                            </c:when>

                                            <c:when test="${payment.paymentMethod == 'e_wallet'}">
                                                <ui:text key="screen.59769b23ea9ec19e"/>
                                            </c:when>

                                            <c:otherwise>
                                                <c:out value="${payment.paymentMethod}" />
                                            </c:otherwise>

                                        </c:choose>
                                    </td>

                                    <td>
                                        <span class="report-status <c:out value='${payment.paymentStatus}' />">
                                            <ui:message text="${payment.paymentStatus}"/>
                                        </span>
                                    </td>

                                    <td>
                                        <c:choose>

                                            <c:when test="${not empty payment.discount}">

                                                <strong>
                                                    <c:out value="${payment.discount.code}" />
                                                </strong>

                                                <span class="report-muted">
                                                    <fmt:formatNumber
                                                            value="${payment.discount.saleOff}"
                                                            pattern="0.##" />%
                                                </span>

                                            </c:when>

                                            <c:otherwise>
                                                <ui:text key="screen.dc937b59892604f5"/>
                                            </c:otherwise>

                                        </c:choose>
                                    </td>

                                    <td>
                                        <span class="report-transaction">
                                            <c:out
                                                    value="${payment.transactionCode}"
                                                    default="—" />
                                        </span>
                                    </td>

                                    <td class="report-nowrap">
                                        <c:choose>

                                            <c:when test="${not empty payment.paymentDate}">
                                                <c:out value="${fn:replace(payment.paymentDate, 'T', ' ')}" />
                                            </c:when>

                                            <c:otherwise>
                                                <span class="report-muted">
                                                    <ui:text key="screen.b37c7879f6c359f6"/>
                                                </span>
                                            </c:otherwise>

                                        </c:choose>
                                    </td>

                                </tr>

                            </c:forEach>

                            </tbody>

                        </table>

                    </div>

                </c:when>

                <c:otherwise>
                    <div class="report-empty">
                        <ui:text key="screen.c79a98ccca090390"/>
                    </div>
                </c:otherwise>

            </c:choose>

        </section>

    </c:if>

    <%-- The shared pagination retains the selected reporting period. --%>
    <%@ include file="report-list-pagination.jsp" %>

</main>

<%@ include file="/WEB-INF/include/footer.jsp" %>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>

</body>
</html>