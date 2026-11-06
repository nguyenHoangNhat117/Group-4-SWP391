<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
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
<html lang="${requestScope.uiLanguage}" class="hotel-dashboard-page">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.ed04e63239ea7aa8">${requestScope.i18n['screen.ed04e63239ea7aa8']}</title>

        <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/assets/css/general.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-modern.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-ui.css">
        <link rel="stylesheet" href="${ctx}/assets/css/hotel-dashboard.css?v=3">

        <script src="${ctx}/assets/js/hotel-dashboard.js?v=3" defer></script>
    </head>

    <body class="hotel-dashboard-body ${cookie.animation.value == 'disable' ? 'disable-animation' : ''}">

        <%@ include file="/WEB-INF/include/header.jsp" %>

        <main class="hd" id="hotel-dashboard" data-report-scope="period">

            <div class="hd-wrap">

                <div class="hd-hero">
                    <div>
                        <p class="hd-eyebrow">
                            <ui:text key="screen.45ffb66e6ad7ba6c"/>
                        </p>

                        <h1><ui:text key="screen.509b3c4e5dad347d"/> <span><ui:text key="screen.94c116ee118a7299"/></span></h1>

                        <p class="hd-subtitle">
                            <ui:text key="screen.fab518b0efcf2bc8"/>
                        </p>
                    </div>

                    <c:if test="${empty dashboardError}">
                        <a class="hd-button hd-button-light"
                           href="<c:out value='${dashboardBookingsUrl}' />">
                            <ui:text key="screen.dd2c97d7ec4b29b7"/>
                            <span aria-hidden="true">↗</span>
                        </a>
                    </c:if>
                </div>

                <form class="hd-filter"
                      action="${ctx}/dashboard"
                      method="get">

                    <div class="hd-filter-intro">
                        <span class="hd-overline"><ui:text key="screen.d4bd1581397c2210"/></span>
                        <strong><ui:text key="screen.d81248f1cf996822"/></strong>
                    </div>

                    <div class="hd-field">
                        <label for="fromDate"><ui:text key="screen.218197693424e015"/></label>

                        <input type="date"
                               id="fromDate"
                               name="fromDate"
                               min="0001-01-01"
                               max="${today}"
                               value="${fromDate}"
                               required>
                    </div>

                    <div class="hd-field">
                        <label for="toDate"><ui:text key="screen.f4b06ef6d3c81436"/></label>

                        <input type="date"
                               id="toDate"
                               name="toDate"
                               min="0001-01-01"
                               max="${today}"
                               value="${toDate}"
                               required>
                    </div>

                    <button type="submit" class="hd-button">
                        <ui:text key="screen.04b6c8fb3080304b"/>
                        <span aria-hidden="true">→</span>
                    </button>

                    <a class="hd-reset" href="${ctx}/dashboard">
                        <ui:text key="screen.daee7606b339f3c3"/>
                    </a>

                </form>

                <div class="hd-period">
                    <span class="hd-period-dot" aria-hidden="true"></span>

                    <ui:text key="screen.7aa351a109bd9c63"/>

                    <strong>${fromDate} — ${toDate}</strong>

                    <span><ui:text key="screen.10721c4ab77ee84e"/></span>
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

                            <div id="hd-month-data" data-grouping="${chartGrouping}">
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
                                    <span><ui:text key="screen.dc742a717fcb496d"/></span>
                                    <span class="hd-symbol">₫</span>
                                </div>

                                <strong class="hd-kpi-value"
                                        data-counter="${totalRevenue}"
                                        data-format="money-number">
                                    <fmt:formatNumber
                                        value="${totalRevenue}"
                                        pattern="#,##0.##" />
                                </strong>

                                <p><ui:text key="screen.dfc9d4b99d0210d2"/></p>

                                <div class="hd-kpi-decoration"
                                     aria-hidden="true"></div>
                            </section>

                            <section class="hd-kpi hd-kpi-gold">

                                <div class="hd-kpi-top">
                                    <span><ui:text key="screen.49a0995914d7f726"/></span>
                                    <span class="hd-symbol">B</span>
                                </div>

                                <strong class="hd-kpi-value"
                                        data-counter="${totalBookings}">
                                    <fmt:formatNumber
                                        value="${totalBookings}"
                                        pattern="#,##0" />
                                </strong>

                                <p><ui:text key="screen.6de74abe0a94f115"/></p>

                                <div class="hd-kpi-decoration"
                                     aria-hidden="true"></div>
                            </section>

                            <section class="hd-kpi hd-kpi-sage">

                                <div class="hd-kpi-top">
                                    <span><ui:text key="screen.a6cd9d506db1c961"/></span>
                                    <span class="hd-symbol">C</span>
                                </div>

                                <strong class="hd-kpi-value"
                                        data-counter="${customerCount}">
                                    <fmt:formatNumber
                                        value="${customerCount}"
                                        pattern="#,##0" />
                                </strong>

                                <p><ui:text key="screen.5ff03d865c9dabde"/></p>

                                <div class="hd-kpi-decoration"
                                     aria-hidden="true"></div>
                            </section>

                            <section class="hd-kpi hd-kpi-clay" title="${requestScope.i18n['screen.a1b1500acd196326']}" data-i18n-title="screen.a1b1500acd196326">

                                <div class="hd-kpi-top">
                                    <span><ui:text key="screen.6e34721d3ac699b2"/></span>
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
                                                    <ui:text key="screen.e2f79e5b60330bba"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </strong>

                                        <c:if test="${occupancy.applicable}">
                                            <span class="hd-scope"><ui:text key="screen.f4b06ef6d3c81436"/> ${occupancy.throughDate}</span>
                                        </c:if>
                                    </div>

                                    <svg class="hd-occupancy"
                                         viewBox="0 0 100 100"
                                         role="img"
                                         aria-label="${requestScope.i18n['screen.6e34721d3ac699b2']}" data-i18n-aria-label="screen.6e34721d3ac699b2">

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
                                            <ui:text key="screen.3315afd97f7f2ebc"/>
                                        </p>

                                        <h2><ui:text key="screen.f08f9a759de6b4b0"/></h2>

                                        <p class="hd-description">
                                            <ui:text key="screen.dcdee6a81d4fd6a3"/>
                                        </p>
                                    </div>

                                    <span class="hd-scope"><ui:text key="screen.170ac176d7972868"/></span>
                                </div>

                                <div class="hd-chart-toolbar">

                                    <span class="hd-key">
                                        <i class="hd-key-gold"></i>
                                        <ui:text key="screen.64f087a27f90bba1"/>
                                    </span>

                                    <div class="hd-toggle"
                                         role="group"
                                         aria-label="${requestScope.i18n['screen.739f7bcad8792a71']}" data-i18n-aria-label="screen.739f7bcad8792a71">

                                        <button type="button"
                                                data-revenue-mode="bar"
                                                aria-pressed="true">
                                            <ui:text key="screen.53aade77cd69a77b"/>
                                        </button>

                                        <button type="button"
                                                data-revenue-mode="area"
                                                aria-pressed="false">
                                            <ui:text key="screen.024dc204d7ba77a8"/>
                                        </button>
                                    </div>

                                </div>

                                <div class="hd-chart-scroll">
                                    <svg id="hd-revenue-chart"
                                         class="hd-chart hd-revenue-chart"
                                         viewBox="0 0 800 310"
                                         role="img"
                                         aria-label="${requestScope.i18n['screen.0b831fa69085addd']}" data-i18n-aria-label="screen.0b831fa69085addd">
                                    </svg>
                                </div>

                                <p class="hd-note">
                                    <ui:text key="screen.993199c30e34feca"/>
                                </p>

                                <details class="hd-data-details">
                                    <summary><ui:text key="screen.f26cec1171e24415"/></summary>

                                    <div class="hd-table-scroll">
                                        <table class="hd-table">

                                            <thead>
                                                <tr>
                                                    <th scope="col"><ui:text key="screen.6e795d4d3cc21c5c"/></th>
                                                    <th scope="col"><ui:text key="screen.96ecbad5d5fa01f6"/></th>
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
                                        <p class="hd-overline"><ui:text key="screen.5c210b27b08e009d"/></p>
                                        <h2><ui:text key="screen.27ad17b7c82ecf34"/></h2>

                                        <p class="hd-description">
                                            <ui:text key="screen.e65a485296ed3a57"/>
                                        </p>
                                    </div>

                                    <span class="hd-scope"><ui:text key="screen.0108466a7d7e6d7a"/></span>
                                </div>

                                <div class="hd-donut-wrap">

                                    <svg id="hd-booking-chart"
                                         viewBox="0 0 230 230"
                                         class="hd-donut"
                                         role="img"
                                         aria-label="${requestScope.i18n['screen.da48d7c5a7795ebc']}" data-i18n-aria-label="screen.da48d7c5a7795ebc">
                                    </svg>

                                    <div class="hd-donut-center">
                                        <strong data-counter="${totalBookings}">
                                            ${totalBookings}
                                        </strong>

                                        <span><ui:text key="screen.c9b1e8c94c489113"/></span>
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
                                        <p class="hd-overline"><ui:text key="screen.2367a31b344564fb"/></p>
                                        <h2><ui:text key="screen.bfbab9d396c6eb77"/></h2>
                                    </div>

                                    <span class="hd-panel-index">01</span>
                                </div>

                                <p class="hd-description">
                                    <ui:text key="screen.d40bf1b662a00b42"/>
                                </p>

                                <div class="hd-toggle hd-payment-toggle"
                                     role="group"
                                     aria-label="${requestScope.i18n['screen.cc2eddd254417d0d']}" data-i18n-aria-label="screen.cc2eddd254417d0d">

                                    <button type="button"
                                            data-payment-mode="amount"
                                            aria-pressed="true">
                                        <ui:text key="screen.a36c8a35fe9c727f"/>
                                    </button>

                                    <button type="button"
                                            data-payment-mode="count"
                                            aria-pressed="false">
                                        <ui:text key="screen.3e5136fd4b11c0dd"/>
                                    </button>
                                </div>

                                <svg id="hd-payment-chart"
                                     class="hd-chart hd-payment-chart"
                                     viewBox="0 0 440 280"
                                     role="img"
                                     aria-label="${requestScope.i18n['screen.094dce89a8f8a2de']}" data-i18n-aria-label="screen.094dce89a8f8a2de">
                                </svg>

                                <div id="hd-payment-summary"
                                     class="hd-mini-grid"></div>

                                <p class="hd-note hd-bottom-note">
                                    <ui:text key="screen.61adf1fe079a9041"/>
                                </p>

                            </section>

                            <section class="hd-panel hd-bottom-panel">

                                <div class="hd-panel-heading">
                                    <div>
                                        <p class="hd-overline"><ui:text key="screen.4dd44c74f9f37b38"/></p>
                                        <h2><ui:text key="screen.c47fb6ace89008c3"/></h2>
                                    </div>

                                    <span class="hd-panel-index">02</span>
                                </div>

                                <p class="hd-description">
                                    <ui:text key="screen.88cb5c8132e5c08d"/>
                                </p>

                                <div class="hd-donut-wrap hd-room-donut">

                                    <svg id="hd-room-chart"
                                         viewBox="0 0 230 230"
                                         class="hd-donut"
                                         role="img"
                                         aria-label="${requestScope.i18n['screen.9d074cd21242c33f']}" data-i18n-aria-label="screen.9d074cd21242c33f">
                                    </svg>

                                    <div class="hd-donut-center">
                                        <strong id="hd-room-total">—</strong>
                                        <span><ui:text key="screen.2b609c7c33e7ecf4"/></span>
                                    </div>

                                </div>

                                <div id="hd-room-legend"
                                     class="hd-legend"></div>

                                <p class="hd-note hd-bottom-note">
                                    <ui:text key="screen.f95690edbb30ecfd"/>
                                </p>

                            </section>

                            <section class="hd-panel hd-bottom-panel">

                                <div class="hd-panel-heading">
                                    <div>
                                        <p class="hd-overline"><ui:text key="screen.bb96105efae8b6b1"/></p>
                                        <h2><ui:text key="screen.295554dd649c5aa0"/></h2>
                                    </div>

                                    <span class="hd-panel-index">03</span>
                                </div>

                                <p class="hd-description">
                                    <ui:text key="screen.f33dda4e59249351"/>
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
                                                 aria-label="${requestScope.i18n['screen.306767a9cb81a868']}" data-i18n-aria-label="screen.306767a9cb81a868">
                                            </svg>

                                            <div class="hd-rating-value">

                                                <strong data-counter="${reviewStatistics.averageRating}"
                                                        data-format="decimal">
                                                    <fmt:formatNumber
                                                        value="${reviewStatistics.averageRating}"
                                                        pattern="0.##" />
                                                </strong>

                                                <span><ui:text key="screen.8b923af58c1d2f29"/></span>

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
                                            <strong><ui:text key="screen.e2f79e5b60330bba"/></strong>
                                            <span><ui:text key="screen.539fd8609f5b572c"/></span>
                                        </div>
                                    </c:otherwise>

                                </c:choose>

                                <div class="hd-review-total">

                                    <strong data-counter="${reviewStatistics.totalReviews}">
                                        ${reviewStatistics.totalReviews}
                                    </strong>

                                    <span><ui:text key="screen.77717242b063c08d"/></span>

                                    <a href="${ctx}/reports?view=reviews&amp;status=approved"
                                       aria-label="${requestScope.i18n['screen.e89169f6c8fc91b2']}" data-i18n-aria-label="screen.e89169f6c8fc91b2">
                                        <ui:text key="screen.3076426d947dd6d5"/>
                                    </a>

                                </div>

                                <p class="hd-note hd-bottom-note">
                                    <ui:text key="screen.c80c853616ca2e82"/>
                                </p>

                            </section>

                        </div>

                        <details class="hd-definitions">
                            <summary><ui:text key="screen.874d5a93f95cdfe1"/></summary>
                            <ul>
                                <li><strong><ui:text key="screen.e5beb132b20462b6"/></strong> <ui:text key="screen.e6b3bf04577067b0"/></li>
                                <li><strong><ui:text key="screen.c0761ba7b2476830"/></strong> <ui:text key="screen.4101fe6983daffb6"/></li>
                                <li><strong><ui:text key="screen.1346d5c75c538436"/></strong> <ui:text key="screen.1623700055ece805"/>
                                    <c:if test="${occupancy.applicable}">
                                        ${occupancy.roomNights} <ui:text key="screen.451ef2e45c1c913d"/> ${occupancy.capacityNights} <ui:text key="screen.5e1a24394f830248"/>
                                    </c:if>
                                    <c:if test="${not occupancy.applicable}"><ui:text key="screen.369ceed30eeffa6c"/></c:if></li>
                                    <li><strong><ui:text key="screen.9224a447d4ead64d"/></strong> <ui:text key="screen.a14a6e14c0f45992"/></li>
                                    <li><strong><ui:text key="screen.36aea3446441ad8c"/></strong> <ui:text key="screen.1a3294e230101513"/></li>
                                </ul>
                            </details>

                            <section class="hd-panel hd-records">

                                <div>
                                    <p class="hd-overline"><ui:text key="screen.e7a1fd07adbb3234"/></p>
                                    <h2><ui:text key="screen.ccac87f4f6698de0"/></h2>

                                    <p class="hd-description">
                                        <ui:text key="screen.88c56e64ef314a26"/>
                                    </p>
                                </div>

                                <div class="hd-record-links">

                                    <a class="hd-button hd-button-light"
                                       href="<c:out value='${dashboardBookingsUrl}' />">
                                    <ui:text key="screen.08da2b51479183d7"/>
                                </a>

                                <a class="hd-button hd-button-light"
                                   href="<c:out value='${dashboardPaymentsUrl}' />">
                                    <ui:text key="screen.25703a5e8ddbd2cc"/>
                                </a>

                                <%-- Revenue details retain the selected reporting period. --%>
                                <a class="hd-button"
                                   href="${ctx}/reports?view=period&amp;fromDate=${fromDate}&amp;toDate=${toDate}">
                                    <ui:text key="screen.31de06fffae89482"/>
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