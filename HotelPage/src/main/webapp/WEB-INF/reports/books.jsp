<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<fmt:setLocale value="vi_VN" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="en" class="hotel-reports-page">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Booking Report | Luxe Escape</title>

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
                <h2>Reservation records</h2>

                <span class="report-meta">
                    ${totalRecords} matching bookings
                    · ${fn:length(bookings)} on this page
                </span>
            </div>

            <c:choose>

                <c:when test="${not empty bookings}">

                    <div class="report-table-scroll">

                        <table class="report-table">

                            <thead>
                            <tr>
                                <th scope="col">Booking</th>
                                <th scope="col">Customer</th>
                                <th scope="col">Created at</th>
                                <th scope="col">Room / Stay</th>
                                <th scope="col">Status</th>
                                <th scope="col" class="numeric">
                                    Booking total
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
                                                Customer #${booking.customer.customerID}
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
                                                                Room #${detail.roomNumber}
                                                            </a>
                                                        </strong>

                                                        <span class="report-muted">
                                                            ${detail.checkInDate}
                                                            → ${detail.checkOutDate}
                                                        </span>

                                                        <span class="report-muted">
                                                            ${detail.guestCount} guests
                                                            ·
                                                            <fmt:formatNumber
                                                                    value="${detail.pricePerNight}"
                                                                    pattern="#,##0.##" />
                                                            ₫ / night
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
                                                    No room information
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
                        No bookings match the selected period.
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