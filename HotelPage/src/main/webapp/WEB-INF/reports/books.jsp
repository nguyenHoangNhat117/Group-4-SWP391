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

    <title data-i18n="screen.d087fa13cc38edb1">${requestScope.i18n['screen.d087fa13cc38edb1']}</title>

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

    <%-- Do not display an empty results table when loading fails. --%>
    <c:if test="${empty reportError}">

        <section class="report-panel">

            <div class="report-panel-heading">
                <h2><ui:text key="screen.a47bf2af83d478c2"/></h2>

                <span class="report-meta">
                    ${totalRecords} <ui:text key="screen.804486f0fad7fd3f"/> ${fn:length(bookings)} <ui:text key="screen.c36ee348ff2d354a"/>
                </span>
            </div>

            <c:choose>

                <c:when test="${not empty bookings}">

                    <div class="report-table-scroll">

                        <table class="report-table">

                            <thead>
                            <tr>
                                <th scope="col"><ui:text key="screen.948e03e7054b919e"/></th>
                                <th scope="col"><ui:text key="screen.bf3763383aaf4306"/></th>
                                <th scope="col"><ui:text key="screen.775b5bf9646856d5"/></th>
                                <th scope="col"><ui:text key="screen.fe43f0a1373a3d68"/></th>
                                <th scope="col"><ui:text key="screen.920e413c7d411b61"/></th>
                                <th scope="col" class="numeric">
                                    <ui:text key="screen.dac7b8fa08bd5f4e"/>
                                </th>
                            </tr>
                            </thead>

                            <tbody>

                            <c:forEach var="booking" items="${bookings}">

                                <tr>

                                    <td>
                                        <strong>#${booking.id}</strong>

                                        <c:if test="${not empty booking.specialRequest}">
                                            <span class="report-muted">
                                                <c:out value="${booking.specialRequest}" />
                                            </span>
                                        </c:if>
                                    </td>

                                    <td>
                                        <c:choose>

                                            <c:when test="${not empty booking.customer.firstName
                                                            or not empty booking.customer.lastName}">
                                                <c:out value="${booking.customer.firstName}" />
                                                <c:out value="${booking.customer.lastName}" />
                                            </c:when>

                                            <c:otherwise>
                                                <ui:text key="screen.641d305570b93564"/>${booking.customer.customerID}
                                            </c:otherwise>

                                        </c:choose>

                                        <span class="report-muted">
                                            <c:out value="${booking.customer.email}" />
                                        </span>
                                    </td>

                                    <td class="report-nowrap">
                                        <c:out value="${fn:replace(booking.bookingDate, 'T', ' ')}" />
                                    </td>

                                    <td>
                                        <c:choose>

                                            <c:when test="${not empty booking.details}">

                                                <c:forEach var="detail"
                                                           items="${booking.details}">

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
                                                            ${detail.guestCount} <ui:text key="screen.45f69c6d7bebdee3"/>
                                                            <fmt:formatNumber
                                                                    value="${detail.pricePerNight}"
                                                                    pattern="#,##0.##" />
                                                            <ui:text key="screen.9504b0b5c7c215ef"/>
                                                        </span>

                                                        <c:if test="${not empty detail.specialRequest}">
                                                            <span class="report-muted">
                                                                <c:out value="${detail.specialRequest}" />
                                                            </span>
                                                        </c:if>

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

                                    <td>
                                        <span class="report-status <c:out value='${booking.status}' />">
                                            <c:out value="${fn:replace(booking.status, '_', ' ')}" />
                                        </span>

                                        <c:if test="${booking.status == 'cancelled'}">

                                            <c:if test="${not empty booking.cancellationDate}">
                                                <span class="report-muted">
                                                    <c:out value="${fn:replace(booking.cancellationDate, 'T', ' ')}" />
                                                </span>
                                            </c:if>

                                            <c:if test="${not empty booking.cancellationReason}">
                                                <span class="report-muted">
                                                    <c:out value="${booking.cancellationReason}" />
                                                </span>
                                            </c:if>

                                        </c:if>
                                    </td>

                                    <td class="numeric">
                                        <strong>
                                            <fmt:formatNumber
                                                    value="${booking.totalPrice}"
                                                    pattern="#,##0.##" />
                                            ₫
                                        </strong>
                                    </td>

                                </tr>

                            </c:forEach>

                            </tbody>

                        </table>

                    </div>

                </c:when>

                <c:otherwise>
                    <div class="report-empty">
                        <ui:text key="screen.3c63a71979d97ec7"/>
                    </div>
                </c:otherwise>

            </c:choose>

        </section>

    </c:if>

    <%-- Shared pagination preserves the selected dates. --%>
    <%@ include file="report-list-pagination.jsp" %>

</main>

<%@ include file="/WEB-INF/include/footer.jsp" %>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>

</body>
</html>