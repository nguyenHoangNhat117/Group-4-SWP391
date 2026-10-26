<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Calculate display totals from the aggregated report rows. --%>
<c:set var="reportTotalRevenue" value="${0}" />
<c:set var="reportMaximumRevenue" value="${0}" />

<c:forEach var="item" items="${reportData}">
    <c:set var="reportTotalRevenue"
           value="${reportTotalRevenue + item.totalRevenue}" />

    <c:if test="${item.totalRevenue > reportMaximumRevenue}">
        <c:set var="reportMaximumRevenue"
               value="${item.totalRevenue}" />
    </c:if>
</c:forEach>

<%-- HTML meter requires a maximum greater than its minimum. --%>
<c:set var="reportMeterMaximum"
       value="${reportMaximumRevenue > 0 ? reportMaximumRevenue : 1}" />

<!DOCTYPE html>
<html lang="en" class="hotel-reports-page">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Revenue Report | Luxe Escape</title>

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

    <div class="report-heading">
        <div>
            <p class="report-eyebrow">Luxe Escape / Financial reports</p>
            <h1>Revenue report</h1>

            <p class="report-description">
                Successful payment amounts grouped by calendar month or year.
            </p>
        </div>

        <a class="report-button" href="${ctx}/dashboard">
            Back to dashboard ↗
        </a>
    </div>

    <div class="report-tabs" aria-label="Report navigation">
        <a href="${ctx}/reports?view=bookings&amp;page-index=1">
            Bookings
        </a>

        <a href="${ctx}/reports?view=payments&amp;page-index=1">
            Payments
        </a>

        <a href="${ctx}/reports?view=reviews">Reviews</a>

        <a href="${ctx}/reports?view=month"
           aria-current="page">Revenue</a>
    </div>

    <div class="report-toolbar">

        <a class="report-button ${view == 'month' ? 'primary' : ''}"
           href="${ctx}/reports?view=month&amp;year=${selectedYear}">
            Monthly revenue
        </a>

        <a class="report-button ${view == 'year' ? 'primary' : ''}"
           href="${ctx}/reports?view=year">
            Yearly revenue
        </a>

        <c:if test="${view == 'month'}">
            <form action="${ctx}/reports" method="get">
                <input type="hidden" name="view" value="month">

                <label for="reportYear">Report year</label>

                <select id="reportYear" name="year">
                    <%-- Preserve the selected year even when it has no paid records. --%>
                    <option value="${selectedYear}" selected>
                        ${selectedYear}
                    </option>

                    <c:forEach var="year" items="${availableYears}">
                        <c:if test="${year != selectedYear}">
                            <option value="${year}">${year}</option>
                        </c:if>
                    </c:forEach>
                </select>

                <button type="submit" class="report-button">
                    Apply
                </button>
            </form>
        </c:if>
    </div>

    <div class="report-total">
        <span>
            <c:choose>
                <c:when test="${view == 'month'}">
                    Total paid revenue · ${selectedYear}
                </c:when>

                <c:otherwise>
                    Total paid revenue · All reported years
                </c:otherwise>
            </c:choose>
        </span>

        <strong>
            <fmt:formatNumber
                    value="${reportTotalRevenue}"
                    pattern="#,##0.##" />
            ₫
        </strong>

        <small>
            Based on Payment.Amount with PaymentStatus = paid.
        </small>
    </div>

    <c:if test="${reportTotalRevenue == 0}">
        <div class="report-note">
            No paid revenue was recorded for this selection.
        </div>
    </c:if>

    <section class="report-panel">

        <div class="report-panel-heading">
            <h2>
                <c:choose>
                    <c:when test="${view == 'month'}">
                        Monthly breakdown · ${selectedYear}
                    </c:when>

                    <c:otherwise>
                        Yearly breakdown
                    </c:otherwise>
                </c:choose>
            </h2>

            <span class="report-meta">Currency: VND</span>
        </div>

        <c:choose>
            <c:when test="${not empty reportData}">
                <div class="report-table-scroll">
                    <table class="report-table">
                        <thead>
                        <tr>
                            <th scope="col">
                                <c:choose>
                                    <c:when test="${view == 'month'}">
                                        Month
                                    </c:when>

                                    <c:otherwise>Year</c:otherwise>
                                </c:choose>
                            </th>

                            <th scope="col" class="numeric">
                                Paid revenue
                            </th>

                            <th scope="col">
                                Relative amount
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach var="item" items="${reportData}">
                            <tr>
                                <td class="report-nowrap">
                                    <strong>
                                        <c:if test="${view == 'month'}">
                                            Month
                                        </c:if>

                                        <c:out value="${item.timeUnit}" />
                                    </strong>
                                </td>

                                <td class="numeric">
                                    <fmt:formatNumber
                                            value="${item.totalRevenue}"
                                            pattern="#,##0.##" />
                                    ₫
                                </td>

                                <td class="report-chart-cell">
                                    <meter class="report-meter"
                                           min="0"
                                           max="${reportMeterMaximum}"
                                           value="${item.totalRevenue}"
                                           aria-label="Paid revenue for period ${item.timeUnit}">
                                        <c:out value="${item.totalRevenue}" />
                                    </meter>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <c:otherwise>
                <div class="report-empty">
                    No revenue records found.
                </div>
            </c:otherwise>
        </c:choose>

    </section>

    <div class="report-note">
        Bar lengths compare revenue with the highest amount in this report.
        Revenue follows payment dates and includes successful deposits and
        balance payments. Pending, failed and refunded transactions are excluded.
        This is paid revenue, not a separate net-refund cash-flow report.
    </div>

</main>

<%@ include file="/WEB-INF/include/footer.jsp" %>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>

</body>
</html>