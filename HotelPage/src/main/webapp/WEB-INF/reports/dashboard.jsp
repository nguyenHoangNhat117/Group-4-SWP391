<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN" scope="page" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Preserve the dashboard period when opening detailed records. --%>
<c:url var="dashboardBookingsUrl" value="/reports">
    <c:param name="view" value="bookings" />
    <c:param name="page-index" value="1" />
    <c:param name="fromDate" value="${fromDate}" />
    <c:param name="toDate" value="${toDate}" />
</c:url>

<c:url var="dashboardPaymentsUrl" value="/reports">
    <c:param name="view" value="payments" />
    <c:param name="page-index" value="1" />
    <c:param name="fromDate" value="${fromDate}" />
    <c:param name="toDate" value="${toDate}" />
</c:url>

<!DOCTYPE html>
<html lang="en" class="hotel-dashboard-page">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Hotel Analytics | Luxe Escape</title>

    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/general.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-modern.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-shared-shell.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-ui.css">
    <link rel="stylesheet" href="${ctx}/assets/css/hotel-dashboard.css?v=2">

    <script src="${ctx}/assets/js/hotel-dashboard.js?v=2" defer></script>
</head>

<body class="hotel-dashboard-body ${cookie.animation.value == 'disable' ? 'disable-animation' : ''}">

<%@ include file="/WEB-INF/include/header.jsp" %>

<main class="hd" id="hotel-dashboard">

    <div class="hd-wrap">

        <div class="hd-hero">
            <div>
                <p class="hd-eyebrow">
                    Luxe Escape · Management overview
                </p>

                <h1>Hotel <span>Analytics</span></h1>

                <p class="hd-subtitle">
                    A considered view of your hotel’s performance.
                </p>
            </div>

            <c:if test="${empty dashboardError}">
                <a class="hd-button hd-button-light"
                   href="<c:out value='${dashboardBookingsUrl}' />">
                    Explore records
                    <span aria-hidden="true">↗</span>
                </a>
            </c:if>
        </div>

        <form class="hd-filter"
              action="${ctx}/dashboard"
              method="get">

            <div class="hd-filter-intro">
                <span class="hd-overline">Reporting period</span>
                <strong>Choose your dates</strong>
            </div>

            <div class="hd-field">
                <label for="fromDate">From</label>

                <input type="date"
                       id="fromDate"
                       name="fromDate"
                       min="0001-01-01"
                       max="9998-12-31"
                       value="${fromDate}"
                       required>
            </div>

            <div class="hd-field">
                <label for="toDate">To</label>

                <input type="date"
                       id="toDate"
                       name="toDate"
                       min="0001-01-01"
                       max="9998-12-31"
                       value="${toDate}"
                       required>
            </div>

            <button type="submit" class="hd-button">
                Apply period
                <span aria-hidden="true">→</span>
            </button>

            <a class="hd-reset" href="${ctx}/dashboard">
                Reset
            </a>

        </form>

        <div class="hd-period">
            <span class="hd-period-dot" aria-hidden="true"></span>

            Selected period

            <strong>${fromDate} — ${toDate}</strong>

            <span>Both dates included</span>
        </div>

        <c:choose>

            <c:when test="${not empty dashboardError}">
                <div class="hd-error" role="alert">
                    <c:out value="${dashboardError}" />
                </div>
            </c:when>

            <c:otherwise>

                <%-- Chart inputs contain typed values from DashboardServlet. --%>
                <div id="hd-data" hidden>

                    <div id="hd-month-data">
                        <c:forEach var="item" items="${monthlyRevenue}">
                            <span data-label="${item.timeUnit}"
                                  data-value="${item.totalRevenue}">
                            </span>
                        </c:forEach>
                    </div>

                    <div id="hd-booking-data">
                        <c:forEach var="item" items="${bookingStatistics}">
                            <span data-label="<c:out value='${item.status}' />"
                                  data-value="${item.totalCount}">
                            </span>
                        </c:forEach>
                    </div>

                    <div id="hd-payment-data">
                        <c:forEach var="item" items="${paymentStatistics}">
                            <span data-label="<c:out value='${item.status}' />"
                                  data-value="${item.totalAmount}"
                                  data-count="${item.totalCount}">
                            </span>
                        </c:forEach>
                    </div>

                    <div id="hd-room-data">
                        <c:forEach var="item" items="${roomStatistics}">
                            <span data-label="<c:out value='${item.status}' />"
                                  data-value="${item.totalCount}">
                            </span>
                        </c:forEach>
                    </div>

                </div>

                <div class="hd-kpis">

                    <section class="hd-kpi hd-kpi-dark">

                        <div class="hd-kpi-top">
                            <span>Paid revenue</span>
                            <span class="hd-symbol">₫</span>
                        </div>

                        <strong class="hd-kpi-value"
                                data-counter="${totalRevenue}"
                                data-format="money">
                            <fmt:formatNumber
                                    value="${totalRevenue}"
                                    pattern="#,##0.##" />
                            ₫
                        </strong>

                        <p>Successful payments · Selected period</p>

                        <div class="hd-kpi-decoration"
                             aria-hidden="true"></div>
                    </section>

                    <section class="hd-kpi hd-kpi-gold">

                        <div class="hd-kpi-top">
                            <span>Bookings created</span>
                            <span class="hd-symbol">B</span>
                        </div>

                        <strong class="hd-kpi-value"
                                data-counter="${totalBookings}">
                            <fmt:formatNumber
                                    value="${totalBookings}"
                                    pattern="#,##0" />
                        </strong>

                        <p>All statuses · Selected period</p>

                        <div class="hd-kpi-decoration"
                             aria-hidden="true"></div>
                    </section>

                    <section class="hd-kpi hd-kpi-sage">

                        <div class="hd-kpi-top">
                            <span>Registered customers</span>
                            <span class="hd-symbol">C</span>
                        </div>

                        <strong class="hd-kpi-value"
                                data-counter="${customerCount}">
                            <fmt:formatNumber
                                    value="${customerCount}"
                                    pattern="#,##0" />
                        </strong>

                        <p>Current total · All periods</p>

                        <div class="hd-kpi-decoration"
                             aria-hidden="true"></div>
                    </section>

                    <section class="hd-kpi hd-kpi-clay">

                        <div class="hd-kpi-top">
                            <span>Estimated occupancy</span>
                            <span class="hd-symbol">%</span>
                        </div>

                        <div class="hd-occupancy-layout">

                            <div>
                                <strong class="hd-kpi-value">
                                    <c:choose>
                                        <c:when test="${availableRooms > 0}">
                                            <span data-counter="${occupancyRate}"
                                                  data-format="percent">
                                                <fmt:formatNumber
                                                        value="${occupancyRate}"
                                                        pattern="0.##" />%
                                            </span>
                                        </c:when>

                                        <c:otherwise>
                                            N/A
                                        </c:otherwise>
                                    </c:choose>
                                </strong>

                                <p>Stay dates · Selected period</p>
                            </div>

                            <svg class="hd-occupancy"
                                 viewBox="0 0 100 100"
                                 role="img"
                                 aria-label="Estimated occupancy">

                                <circle cx="50"
                                        cy="50"
                                        r="39"
                                        class="hd-ring-track" />

                                <circle id="hd-occupancy-ring"
                                        cx="50"
                                        cy="50"
                                        r="39"
                                        class="hd-ring-value"
                                        stroke-dasharray="0 245.044"
                                        data-value="${occupancyRate}"
                                        data-available="${availableRooms}" />
                            </svg>

                        </div>
                    </section>

                </div>

                <div class="hd-primary-grid">

                    <section class="hd-panel hd-revenue-panel">

                        <div class="hd-panel-heading">
                            <div>
                                <p class="hd-overline">
                                    Financial performance
                                </p>

                                <h2>Revenue overview</h2>

                                <p class="hd-description">
                                    Monthly paid amounts
                                    · Full calendar year ${chartYear}
                                </p>
                            </div>

                            <span class="hd-tag">VND</span>
                        </div>

                        <div class="hd-chart-toolbar">

                            <span class="hd-key">
                                <i class="hd-key-gold"></i>
                                Successful payments
                            </span>

                            <div class="hd-toggle"
                                 role="group"
                                 aria-label="Revenue chart display">

                                <button type="button"
                                        data-revenue-mode="bar"
                                        aria-pressed="true">
                                    Columns
                                </button>

                                <button type="button"
                                        data-revenue-mode="area"
                                        aria-pressed="false">
                                    Area
                                </button>
                            </div>

                        </div>

                        <svg id="hd-revenue-chart"
                             class="hd-chart hd-revenue-chart"
                             viewBox="0 0 800 310"
                             role="img"
                             aria-label="Monthly paid revenue">
                        </svg>

                        <p class="hd-note">
                            Full year containing the start date;
                            the selected date range applies to the summary cards.
                            Point to or focus a chart mark for its exact amount.
                        </p>

                        <details class="hd-data-details">
                            <summary>View monthly values</summary>

                            <div class="hd-table-scroll">
                                <table class="hd-table">

                                    <thead>
                                    <tr>
                                        <th scope="col">Month</th>
                                        <th scope="col">Paid revenue (VND)</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    <c:forEach var="item"
                                               items="${monthlyRevenue}">
                                        <tr>
                                            <td>${item.timeUnit}</td>

                                            <td>
                                                <fmt:formatNumber
                                                        value="${item.totalRevenue}"
                                                        pattern="#,##0.##" />
                                                ₫
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>

                                </table>
                            </div>
                        </details>

                    </section>

                    <section class="hd-panel">

                        <div class="hd-panel-heading">
                            <div>
                                <p class="hd-overline">Reservations</p>
                                <h2>Booking distribution</h2>

                                <p class="hd-description">
                                    Bookings created in the selected period
                                </p>
                            </div>

                            <span class="hd-tag">By status</span>
                        </div>

                        <div class="hd-donut-wrap">

                            <svg id="hd-booking-chart"
                                 viewBox="0 0 230 230"
                                 class="hd-donut"
                                 role="img"
                                 aria-label="Booking counts by current status">
                            </svg>

                            <div class="hd-donut-center">
                                <strong data-counter="${totalBookings}">
                                    ${totalBookings}
                                </strong>

                                <span>Total bookings</span>
                            </div>
                        </div>

                        <div id="hd-booking-legend"
                             class="hd-legend"></div>

                    </section>

                </div>

                <div class="hd-bottom-grid">

                    <section class="hd-panel hd-bottom-panel">

                        <div class="hd-panel-heading">
                            <div>
                                <p class="hd-overline">Payment activity</p>
                                <h2>Transaction overview</h2>
                            </div>

                            <span class="hd-panel-index">01</span>
                        </div>

                        <p class="hd-description">
                            Dated transactions within the selected period.
                        </p>

                        <div class="hd-toggle hd-payment-toggle"
                             role="group"
                             aria-label="Payment chart metric">

                            <button type="button"
                                    data-payment-mode="amount"
                                    aria-pressed="true">
                                Amount · VND
                            </button>

                            <button type="button"
                                    data-payment-mode="count"
                                    aria-pressed="false">
                                Transactions
                            </button>
                        </div>

                        <svg id="hd-payment-chart"
                             class="hd-chart hd-payment-chart"
                             viewBox="0 0 440 280"
                             role="img"
                             aria-label="Payment amounts by status">
                        </svg>

                        <div id="hd-payment-summary"
                             class="hd-mini-grid"></div>

                        <p class="hd-note hd-bottom-note">
                            Only paid amounts contribute to revenue.
                            Transactions without a payment date are excluded.
                        </p>

                    </section>

                    <section class="hd-panel hd-bottom-panel">

                        <div class="hd-panel-heading">
                            <div>
                                <p class="hd-overline">Room inventory</p>
                                <h2>Operational balance</h2>
                            </div>

                            <span class="hd-panel-index">02</span>
                        </div>

                        <p class="hd-description">
                            Current room conditions · All rooms
                        </p>

                        <div class="hd-donut-wrap hd-room-donut">

                            <svg id="hd-room-chart"
                                 viewBox="0 0 230 230"
                                 class="hd-donut"
                                 role="img"
                                 aria-label="Current room status distribution">
                            </svg>

                            <div class="hd-donut-center">
                                <strong id="hd-room-total">—</strong>
                                <span>Physical rooms</span>
                            </div>

                        </div>

                        <div id="hd-room-legend"
                             class="hd-legend"></div>

                        <p class="hd-note hd-bottom-note">
                            Available means operational, not vacant.
                            Occupancy assumes the currently available room
                            inventory throughout the selected period.
                        </p>

                    </section>

                    <section class="hd-panel hd-bottom-panel">

                        <div class="hd-panel-heading">
                            <div>
                                <p class="hd-overline">Guest experience</p>
                                <h2>Guest satisfaction</h2>
                            </div>

                            <span class="hd-panel-index">03</span>
                        </div>

                        <p class="hd-description">
                            Approved reviews · All periods
                        </p>

                        <c:choose>

                            <c:when test="${reviewStatistics.hasReviews}">

                                <div class="hd-rating-gauge"
                                     id="hd-rating"
                                     data-value="${reviewStatistics.averageRating}">

                                    <svg id="hd-rating-chart"
                                         viewBox="0 0 300 190"
                                         class="hd-chart"
                                         role="img"
                                         aria-label="Average approved rating out of five">
                                    </svg>

                                    <div class="hd-rating-value">

                                        <strong data-counter="${reviewStatistics.averageRating}"
                                                data-format="decimal">
                                            <fmt:formatNumber
                                                    value="${reviewStatistics.averageRating}"
                                                    pattern="0.##" />
                                        </strong>

                                        <span>out of 5</span>

                                    </div>

                                </div>

                                <div class="hd-stars" aria-hidden="true">
                                    <span>★★★★★</span>

                                    <span id="hd-rating-stars">
                                        ★★★★★
                                    </span>
                                </div>

                            </c:when>

                            <c:otherwise>
                                <div class="hd-no-rating">
                                    <strong>N/A</strong>
                                    <span>No approved reviews yet</span>
                                </div>
                            </c:otherwise>

                        </c:choose>

                        <div class="hd-review-total">

                            <strong data-counter="${reviewStatistics.totalReviews}">
                                ${reviewStatistics.totalReviews}
                            </strong>

                            <span>approved reviews</span>

                            <a href="${ctx}/reports?view=reviews"
                               aria-label="View review records">
                                ↗
                            </a>

                        </div>

                        <p class="hd-note hd-bottom-note">
                            Average rating uses approved reviews only.
                            Pending and rejected reviews are excluded.
                        </p>

                    </section>

                </div>

                <section class="hd-panel hd-records">

                    <div>
                        <p class="hd-overline">A closer look</p>
                        <h2>Explore hotel records</h2>

                        <p class="hd-description">
                            Booking and payment records follow
                            the selected reporting period.
                        </p>
                    </div>

                    <div class="hd-record-links">

                        <a class="hd-button hd-button-light"
                           href="<c:out value='${dashboardBookingsUrl}' />">
                            Bookings ↗
                        </a>

                        <a class="hd-button hd-button-light"
                           href="<c:out value='${dashboardPaymentsUrl}' />">
                            Payments ↗
                        </a>

                        <%-- Revenue reports retain their calendar-year scope. --%>
                        <a class="hd-button"
                           href="${ctx}/reports?view=month&amp;year=${chartYear}">
                            Revenue ↗
                        </a>

                    </div>

                </section>

            </c:otherwise>

        </c:choose>

    </div>

</main>

<%@ include file="/WEB-INF/include/footer.jsp" %>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>

</body>
</html>