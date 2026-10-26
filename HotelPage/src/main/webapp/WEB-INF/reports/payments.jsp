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

    <title>Payment Report | Luxe Escape</title>

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
                <h2>Transaction records</h2>

                <span class="report-meta">
                    ${totalRecords} matching transactions
                    · ${fn:length(payments)} on this page
                </span>
            </div>

            <c:choose>

                <c:when test="${not empty payments}">

                    <div class="report-table-scroll">

                        <table class="report-table">

                            <thead>
                            <tr>
                                <th scope="col">Payment</th>
                                <th scope="col">Customer</th>
                                <th scope="col">Booking</th>
                                <th scope="col">Room / Stay</th>

                                <th scope="col" class="numeric">
                                    Booking total
                                </th>

                                <th scope="col" class="numeric">
                                    Transaction amount
                                </th>

                                <th scope="col">Method</th>
                                <th scope="col">Status</th>
                                <th scope="col">Promotion</th>
                                <th scope="col">Transaction code</th>
                                <th scope="col">Payment date</th>
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
                                                Customer #${payment.booking.customer.customerID}
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
                                                                Room #${detail.roomNumber}
                                                            </a>
                                                        </strong>

                                                        <span class="report-muted">
                                                            ${detail.checkInDate}
                                                            → ${detail.checkOutDate}
                                                        </span>

                                                        <span class="report-muted">
                                                            ${detail.guestCount} guests
                                                        </span>

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
                                                Cash
                                            </c:when>

                                            <c:when test="${payment.paymentMethod == 'bank_transfer'}">
                                                Bank transfer
                                            </c:when>

                                            <c:when test="${payment.paymentMethod == 'credit_card'}">
                                                Credit card
                                            </c:when>

                                            <c:when test="${payment.paymentMethod == 'e_wallet'}">
                                                E-wallet
                                            </c:when>

                                            <c:otherwise>
                                                <c:out value="${payment.paymentMethod}" />
                                            </c:otherwise>

                                        </c:choose>
                                    </td>

                                    <td>
                                        <span class="report-status <c:out value='${payment.paymentStatus}' />">
                                            <c:out value="${payment.paymentStatus}" />
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
                                                None
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
                                                    Not recorded
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
                        No payment transactions match the selected period.
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