<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="vi" value="${requestScope.uiLanguage == 'vi'}"/>
<fmt:setLocale value="${vi ? 'vi_VN' : 'en_US'}"/>

<c:set var="recordsView" value="${not empty reportView}"/>
<c:set var="revenueView" value="${not recordsView and not empty view}"/>
<c:set var="loadError" value="${recordsView ? reportError : dashboardError}"/>
<c:set var="title" value="${vi ? 'Tổng quan khách sạn' : 'Hotel dashboard'}"/>

<c:if test="${recordsView}">
    <c:set var="title"
           value="${reportView == 'bookings'
                    ? (vi ? 'Báo cáo đặt phòng' : 'Booking report')
                    : reportView == 'payments'
                    ? (vi ? 'Báo cáo thanh toán' : 'Payment report')
                    : (vi ? 'Báo cáo đánh giá' : 'Review report')}"/>
</c:if>

<c:if test="${revenueView}">
    <c:set var="title"
           value="${vi ? 'Báo cáo doanh thu' : 'Revenue report'}"/>
</c:if>

<c:choose>
    <c:when test="${revenueView}">
        <c:set var="bookingsUrl" value="${revenueBookingsUrl}"/>
        <c:set var="paymentsUrl" value="${revenuePaymentsUrl}"/>
        <c:set var="reviewsUrl" value="${revenueReviewsUrl}"/>

        <c:set var="gross" value="${0}"/>
        <c:set var="refund" value="${0}"/>
        <c:set var="net" value="${0}"/>
        <c:set var="chartMax" value="${1}"/>

        <c:forEach var="r" items="${reportData}">
            <c:set var="gross" value="${gross + r.collectedAmount}"/>
            <c:set var="refund" value="${refund + r.refundedAmount}"/>
            <c:set var="net" value="${net + r.totalRevenue}"/>

            <c:if test="${r.totalRevenue > chartMax}">
                <c:set var="chartMax" value="${r.totalRevenue}"/>
            </c:if>
        </c:forEach>
    </c:when>

    <c:when test="${not recordsView}">
        <c:url var="bookingsUrl" value="/reports">
            <c:param name="view" value="bookings"/>
            <c:param name="fromDate" value="${fromDate}"/>
            <c:param name="toDate" value="${toDate}"/>
        </c:url>

        <c:url var="paymentsUrl" value="/reports">
            <c:param name="view" value="payments"/>
            <c:param name="fromDate" value="${fromDate}"/>
            <c:param name="toDate" value="${toDate}"/>
        </c:url>

        <c:url var="reviewsUrl" value="/reports">
            <c:param name="view" value="reviews"/>
            <c:param name="fromDate" value="${fromDate}"/>
            <c:param name="toDate" value="${toDate}"/>
        </c:url>

        <c:url var="revenueUrl" value="/reports">
            <c:param name="view" value="period"/>
            <c:param name="fromDate" value="${fromDate}"/>
            <c:param name="toDate" value="${toDate}"/>
        </c:url>

        <c:set var="gross" value="${collectedTotal}"/>
        <c:set var="refund" value="${refundedTotal}"/>
        <c:set var="net" value="${totalRevenue}"/>
        <c:set var="chartMax" value="${1}"/>

        <c:forEach var="r" items="${monthlyRevenue}">
            <c:if test="${r.totalRevenue > chartMax}">
                <c:set var="chartMax" value="${r.totalRevenue}"/>
            </c:if>
        </c:forEach>
    </c:when>

    <c:otherwise>
        <c:set var="gross" value="${paymentSummary.collectedAmount}"/>
        <c:set var="refund" value="${paymentSummary.refundedAmount}"/>
        <c:set var="net" value="${paymentSummary.totalRevenue}"/>
    </c:otherwise>
</c:choose>

<!DOCTYPE html>
<html lang="${uiLanguage}" class="hotel-reports-page">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">

    <title><c:out value="${title}"/> | Luxe Escape</title>

    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/general.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-modern.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-shared-shell.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-ui.css">
    <link rel="stylesheet" href="${ctx}/assets/css/hotel-reports.css">

    <style>
        .uc23-cards {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 16px;
            margin: 20px 0;
        }

        .uc23-cards > div {
            min-width: 0;
            background: #fffdfa;
            border: 1px solid #e9ded0;
            border-radius: 16px;
            padding: 22px;
            color: #49382b;
        }

        .uc23-cards span,
        .uc23-cards small {
            display: block;
            color: #807365;
        }

        .uc23-cards strong {
            display: block;
            font-size: clamp(22px, 2.3vw, 32px);
            margin: 8px 0;
            overflow-wrap: anywhere;
            font-variant-numeric: tabular-nums;
        }

        .uc23-scope {
            color: #807365;
            font-size: 14px;
            line-height: 1.65;
            margin: 12px 0 22px;
        }

        .uc23-period {
            display: flex;
            gap: 12px;
            align-items: end;
            flex-wrap: wrap;
            padding: 20px;
            background: #fffdfa;
            border: 1px solid #e9ded0;
            border-radius: 16px;
            margin: 20px 0;
        }

        .uc23-period label {
            display: block;
            color: #807365;
            margin-bottom: 6px;
        }

        .uc23-period input,
        .uc23-period select {
            border: 1px solid #e0d2bf;
            border-radius: 10px;
            padding: 10px;
            background: white;
        }

        .uc23-columns {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 20px;
        }

        .uc23-columns .report-panel {
            min-width: 0;
            margin-top: 0;
        }

        .uc23-columns ul {
            list-style: none;
            padding: 20px;
            margin: 0;
        }

        .uc23-columns li {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            padding: 10px 0;
            border-bottom: 1px solid #f0e9df;
        }

        .uc23-chart {
            max-height: 430px;
            overflow: auto;
        }

        .uc23-chart meter {
            width: 100%;
            min-width: 100px;
            height: 20px;
            accent-color: #b29a7b;
        }

        .uc23-rating-row {
            display: grid;
            grid-template-columns: 60px minmax(0, 1fr) 45px;
            gap: 14px;
            align-items: center;
            margin: 12px 0;
        }

        .uc23-rating-row meter {
            width: 100%;
            accent-color: #b29a7b;
        }

        .hotel-reports .report-status.partially_refunded {
            background: #f2e7d8;
            color: #805b28;
        }

        @media (max-width: 700px) {
            .uc23-cards,
            .uc23-columns {
                grid-template-columns: 1fr;
            }

            .uc23-period > div {
                width: 100%;
            }

            .uc23-period input,
            .uc23-period select {
                width: 100%;
            }
        }
    </style>
</head>

<body class="hotel-reports-body ${cookie.animation.value == 'disable'
                                  ? 'disable-animation' : ''}">

<%@ include file="/WEB-INF/include/header.jsp" %>

<main class="hotel-reports">

    <c:choose>
        <c:when test="${recordsView}">

            <div class="report-heading">
                <div>
                    <p class="report-eyebrow">
                        LUXE ESCAPE / ${vi ? 'BÁO CÁO' : 'REPORTS'}
                    </p>

                    <h1><c:out value="${title}"/></h1>
                </div>

                <a class="report-button"
                   href="<c:out value='${dashboardUrl}'/>">
                    ${vi ? 'Tổng quan' : 'Dashboard'}
                </a>
            </div>

            <div class="report-tabs">
                <a href="<c:out value='${bookingsUrl}'/>"
                   aria-current="${reportView == 'bookings'
                                   ? 'page' : 'false'}">
                    ${vi ? 'Đặt phòng' : 'Bookings'}
                </a>

                <a href="<c:out value='${paymentsUrl}'/>"
                   aria-current="${reportView == 'payments'
                                   ? 'page' : 'false'}">
                    ${vi ? 'Thanh toán' : 'Payments'}
                </a>

                <a href="<c:out value='${reviewsUrl}'/>"
                   aria-current="${reportView == 'reviews'
                                   ? 'page' : 'false'}">
                    ${vi ? 'Đánh giá' : 'Reviews'}
                </a>

                <a href="<c:out value='${revenueUrl}'/>">
                    ${vi ? 'Doanh thu' : 'Revenue'}
                </a>
            </div>

            <form class="report-date-filter"
                  action="${ctx}/reports"
                  method="get">

                <input type="hidden" name="view" value="${reportView}">
                <input type="hidden" name="page-index" value="1">

                <div>
                    <label for="fromDate">
                        ${vi ? 'Từ ngày' : 'From date'}
                    </label>

                    <input type="date"
                           id="fromDate"
                           name="fromDate"
                           min="0001-01-01"
                           max="9998-12-31"
                           value="${fromDate}">
                </div>

                <div>
                    <label for="toDate">
                        ${vi ? 'Đến ngày' : 'To date'}
                    </label>

                    <input type="date"
                           id="toDate"
                           name="toDate"
                           min="0001-01-01"
                           max="9998-12-31"
                           value="${toDate}">
                </div>

                <div>
                    <label for="reportStatus">
                        ${vi ? 'Trạng thái' : 'Status'}
                    </label>

                    <select id="reportStatus" name="status">
                        <option value="">
                            ${vi ? 'Tất cả' : 'All'}
                        </option>

                        <c:forEach var="option" items="${filter.statuses}">
                            <option value="${option}"
                                    ${filter.status == option
                                      ? 'selected' : ''}>
                                <c:out value="${fn:replace(option,'_',' ')}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <c:if test="${reportView == 'payments'}">
                    <div>
                        <label for="reportMethod">
                            ${vi ? 'Phương thức' : 'Method'}
                        </label>

                        <select id="reportMethod" name="method">
                            <option value="">
                                ${vi ? 'Tất cả' : 'All'}
                            </option>

                            <c:forEach var="option" items="${filter.methods}">
                                <option value="${option}"
                                        ${filter.method == option
                                          ? 'selected' : ''}>
                                    <c:out value="${fn:replace(option,'_',' ')}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </c:if>

                <div>
                    <label for="reportBookingId">
                        ${vi ? 'Mã đặt phòng' : 'Booking ID'}
                    </label>

                    <input id="reportBookingId"
                           type="number"
                           name="bookingId"
                           min="1"
                           max="2147483647"
                           value="${filter.bookingId}">
                </div>

                <div>
                    <label for="reportPageSize">
                        ${vi ? 'Số dòng' : 'Rows'}
                    </label>

                    <select id="reportPageSize" name="pageSize">
                        <c:forEach var="size" items="${[10,25,50]}">
                            <option value="${size}"
                                    ${filter.pageSize == size
                                      ? 'selected' : ''}>
                                ${size}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <button class="report-button primary">
                    ${vi ? 'Áp dụng' : 'Apply'}
                </button>

                <a class="report-button"
                   href="<c:out value='${resetUrl}'/>">
                    ${vi ? 'Hiện tất cả' : 'Reset'}
                </a>
            </form>

            <p class="uc23-scope">
                ${filterActive
                  ? (vi ? 'Kỳ: ' : 'Period: ')
                  : (vi ? 'Toàn bộ thời gian' : 'All periods')}

                <c:if test="${filterActive}">
                    ${fromDate} → ${toDate}
                </c:if>
            </p>

        </c:when>

        <c:otherwise>

            <div class="report-heading">
                <div>
                    <p class="report-eyebrow">
                        LUXE ESCAPE / ${vi ? 'BÁO CÁO' : 'REPORTS'}
                    </p>

                    <h1><c:out value="${title}"/></h1>

                    <p class="report-description">
                        ${vi
                          ? 'Đối soát theo ngày thanh toán gốc và dữ liệu đã lưu.'
                          : 'Reconcile original payment dates and stored records.'}
                    </p>
                </div>

                <a class="report-button" href="${ctx}/dashboard">
                    ${vi ? 'Tổng quan' : 'Dashboard'}
                </a>
            </div>

            <div class="report-tabs">
                <a href="<c:out value='${bookingsUrl}'/>">
                    ${vi ? 'Đặt phòng' : 'Bookings'}
                </a>

                <a href="<c:out value='${paymentsUrl}'/>">
                    ${vi ? 'Thanh toán' : 'Payments'}
                </a>

                <a href="<c:out value='${reviewsUrl}'/>">
                    ${vi ? 'Đánh giá' : 'Reviews'}
                </a>

                <a href="${ctx}/reports?view=month">
                    ${vi ? 'Doanh thu' : 'Revenue'}
                </a>
            </div>

            <c:choose>
                <c:when test="${revenueView}">

                    <form class="uc23-period"
                          action="${ctx}/reports"
                          method="get">

                        <input type="hidden" name="view" value="period">

                        <div>
                            <label for="fromDate">
                                ${vi ? 'Từ ngày' : 'From date'}
                            </label>

                            <input id="fromDate"
                                   type="date"
                                   name="fromDate"
                                   value="${fromDate}"
                                   min="0001-01-01"
                                   max="9998-12-31"
                                   required>
                        </div>

                        <div>
                            <label for="toDate">
                                ${vi ? 'Đến ngày' : 'To date'}
                            </label>

                            <input id="toDate"
                                   type="date"
                                   name="toDate"
                                   value="${toDate}"
                                   min="0001-01-01"
                                   max="9998-12-31"
                                   required>
                        </div>

                        <button class="report-button primary">
                            ${vi ? 'Xem theo kỳ' : 'View period'}
                        </button>
                    </form>

                    <form class="uc23-period"
                          action="${ctx}/reports"
                          method="get">

                        <input type="hidden" name="view" value="month">

                        <div>
                            <label for="year">
                                ${vi ? 'Theo tháng của năm' : 'Months of year'}
                            </label>

                            <input id="year"
                                   type="number"
                                   name="year"
                                   min="1"
                                   max="9998"
                                   value="${selectedYear}"
                                   required>
                        </div>

                        <button class="report-button">
                            ${vi ? 'Xem theo tháng' : 'View months'}
                        </button>

                        <a class="report-button"
                           href="${ctx}/reports?view=year">
                            ${vi ? 'So sánh các năm' : 'Compare years'}
                        </a>
                    </form>

                    <p class="uc23-scope">
                        ${view == 'year'
                          ? (vi
                              ? 'Toàn bộ các năm. Tab chi tiết mở toàn thời gian.'
                              : 'All years. Detail tabs open all periods.')
                          : view == 'month'
                          ? (vi ? 'Theo năm: ' : 'Year: ')
                          : (vi ? 'Theo kỳ: ' : 'Period: ')}

                        <c:if test="${view == 'month'}">
                            ${selectedYear}
                        </c:if>

                        <c:if test="${view == 'period'}">
                            ${fromDate} → ${toDate}
                        </c:if>
                    </p>

                </c:when>

                <c:otherwise>

                    <form class="uc23-period"
                          action="${ctx}/dashboard"
                          method="get">

                        <div>
                            <label for="fromDate">
                                ${vi ? 'Từ ngày' : 'From date'}
                            </label>

                            <input id="fromDate"
                                   type="date"
                                   name="fromDate"
                                   min="0001-01-01"
                                   max="${today}"
                                   value="${fromDate}"
                                   required>
                        </div>

                        <div>
                            <label for="toDate">
                                ${vi ? 'Đến ngày' : 'To date'}
                            </label>

                            <input id="toDate"
                                   type="date"
                                   name="toDate"
                                   min="0001-01-01"
                                   max="${today}"
                                   value="${toDate}"
                                   required>
                        </div>

                        <button class="report-button primary">
                            ${vi ? 'Áp dụng' : 'Apply'}
                        </button>

                        <a class="report-button" href="${ctx}/dashboard">
                            ${vi ? 'Tháng hiện tại' : 'Current month'}
                        </a>
                    </form>

                </c:otherwise>
            </c:choose>

        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${not empty loadError}">
            <div class="report-note" role="alert">
                <c:out value="${loadError}"/>

                <c:if test="${recordsView}">
                    <p>
                        <a class="report-button"
                           href="<c:out value='${reviewRetryUrl}'/>">
                            ${vi ? 'Thử lại' : 'Retry'}
                        </a>
                    </p>
                </c:if>
            </div>
        </c:when>

        <c:otherwise>

            <c:if test="${not recordsView or reportView == 'payments'}">
                <section class="uc23-cards uc23-money">
                    <div>
                        <span>${vi ? 'Tổng đã thu' : 'Gross collected'}</span>
                        <strong>
                            <fmt:formatNumber
                                value="${gross}"
                                pattern="#,##0.##"/> ₫
                        </strong>
                    </div>

                    <div>
                        <span>${vi ? 'Đã hoàn' : 'Refunded'}</span>
                        <strong>
                            <fmt:formatNumber
                                value="${refund}"
                                pattern="#,##0.##"/> ₫
                        </strong>
                    </div>

                    <div>
                        <span>
                            ${vi ? 'Còn lại sau hoàn' : 'Net after refunds'}
                        </span>
                        <strong>
                            <fmt:formatNumber
                                value="${net}"
                                pattern="#,##0.##"/> ₫
                        </strong>
                    </div>
                </section>

                <p class="uc23-scope">
                    ${vi
                      ? 'Tiền hoàn là số lũy kế hiện tại của các giao dịch có ngày thanh toán gốc thuộc bộ lọc; không phải tiền hoàn phát sinh trong kỳ. Không trừ thêm tiền rút ví. Giao dịch chờ/thất bại không tính vào tổng đã thu.'
                      : 'Refunds are current cumulative refunds of payments whose original dates match the filter, not refunds issued during the period. Wallet withdrawals are not deducted again. Pending/failed transactions do not contribute to gross collected.'}
                </p>
            </c:if>

            <c:if test="${not recordsView and not revenueView}">

                <section class="uc23-cards">
                    <div>
                        <span>
                            ${vi
                              ? 'Đơn tạo trong kỳ'
                              : 'Bookings created in period'}
                        </span>
                        <strong>${totalBookings}</strong>
                    </div>

                    <div>
                        <span>
                            ${vi
                              ? 'Hồ sơ khách hiện tại'
                              : 'Current customer profiles'}
                        </span>
                        <strong>${customerCount}</strong>
                    </div>

                    <div>
                        <span>
                            ${vi
                              ? 'Công suất ước tính'
                              : 'Estimated occupancy'}
                        </span>

                        <strong>
                            <c:choose>
                                <c:when test="${occupancy.applicable}">
                                    <fmt:formatNumber
                                        value="${occupancy.rate}"
                                        pattern="0.##"/>%
                                </c:when>

                                <c:otherwise>N/A</c:otherwise>
                            </c:choose>
                        </strong>

                        <small>
                            <c:if test="${occupancy.applicable}">
                                ${occupancy.roomNights}
                                /
                                ${occupancy.capacityNights}
                                ${vi
                                  ? 'đêm phòng; đến'
                                  : 'room nights; through'}
                                ${occupancy.throughDate}
                            </c:if>
                        </small>
                    </div>
                </section>

                <p class="uc23-scope">
                    ${vi
                      ? 'Công suất dựa trên lịch lưu trú đã qua và số phòng hiện có; không phải lịch sử công suất thực tế đầy đủ. Khách, trạng thái phòng và đánh giá tổng hợp là số hiện tại/toàn hệ thống.'
                      : 'Occupancy uses elapsed scheduled stays and current inventory, not a complete actual occupancy history. Customers, room statuses and overall reviews are current/all-time values.'}
                </p>

                <div class="uc23-columns">

                    <section class="report-panel">
                        <div class="report-panel-heading">
                            <h2>
                                ${vi
                                  ? 'Đặt phòng theo trạng thái'
                                  : 'Booking statuses'}
                            </h2>
                        </div>

                        <ul>
                            <c:forEach var="r" items="${bookingStatistics}">
                                <li>
                                    <span><c:out value="${r.status}"/></span>
                                    <strong>${r.totalCount}</strong>
                                </li>
                            </c:forEach>
                        </ul>
                    </section>

                    <section class="report-panel">
                        <div class="report-panel-heading">
                            <h2>
                                ${vi
                                  ? 'Trạng thái phòng hiện tại'
                                  : 'Current room statuses'}
                            </h2>
                        </div>

                        <ul>
                            <c:forEach var="r" items="${roomStatistics}">
                                <li>
                                    <span><c:out value="${r.status}"/></span>
                                    <strong>${r.totalCount}</strong>
                                </li>
                            </c:forEach>
                        </ul>
                    </section>

                </div>

                <section class="report-panel">
                    <div class="report-panel-heading">
                        <h2>
                            ${vi
                              ? 'Giao dịch theo trạng thái lưu trong DB'
                              : 'Transactions by stored status'}
                        </h2>
                    </div>

                    <div class="report-table-scroll">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <th>${vi ? 'Trạng thái' : 'Status'}</th>
                                <th>${vi ? 'Số giao dịch' : 'Count'}</th>
                                <th class="numeric">
                                    ${vi
                                      ? 'Tiền giao dịch trước hoàn'
                                      : 'Original transaction amount'}
                                </th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach var="r" items="${paymentStatistics}">
                                <tr>
                                    <td><c:out value="${r.status}"/></td>
                                    <td>${r.totalCount}</td>
                                    <td class="numeric">
                                        <fmt:formatNumber
                                            value="${r.totalAmount}"
                                            pattern="#,##0.##"/> ₫
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>

                <p class="uc23-scope">
                    ${vi
                      ? 'Bảng trạng thái dùng số tiền gốc và ngày thanh toán; giao dịch chưa có ngày không nằm trong kỳ. Đây không phải tổng còn lại sau hoàn.'
                      : 'Status totals use original amounts and payment dates; undated transactions are excluded from the period. These are not net-after-refund totals.'}
                </p>

                <section class="report-panel">
                    <div class="report-panel-heading">
                        <h2>
                            ${vi
                              ? 'Đánh giá đã duyệt toàn hệ thống'
                              : 'All-time approved reviews'}
                        </h2>

                        <span>${reviewStatistics.totalReviews}</span>
                    </div>

                    <div style="padding:22px">
                        <c:choose>
                            <c:when test="${reviewStatistics.hasReviews}">
                                <strong>
                                    <fmt:formatNumber
                                        value="${reviewStatistics.averageRating}"
                                        pattern="0.00"/> / 5
                                </strong>
                            </c:when>

                            <c:otherwise>N/A</c:otherwise>
                        </c:choose>
                    </div>
                </section>

            </c:if>

            <c:if test="${not recordsView}">

                <section class="report-panel">
                    <div class="report-panel-heading">
                        <h2>
                            ${vi
                              ? 'Còn lại sau hoàn theo ngày thanh toán gốc'
                              : 'Net after refunds by original payment date'}
                        </h2>

                        <c:if test="${not revenueView}">
                            <a class="report-button"
                               href="<c:out value='${revenueUrl}'/>">
                                ${vi ? 'Chi tiết' : 'Details'}
                            </a>
                        </c:if>
                    </div>

                    <div class="report-table-scroll uc23-chart">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <th>${vi ? 'Kỳ' : 'Period'}</th>
                                <th class="numeric">VND</th>
                                <th>${vi ? 'So sánh' : 'Comparison'}</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach var="r"
                                       items="${revenueView
                                                ? reportData
                                                : monthlyRevenue}">
                                <tr>
                                    <td><c:out value="${r.timeUnit}"/></td>

                                    <td class="numeric">
                                        <fmt:formatNumber
                                            value="${r.totalRevenue}"
                                            pattern="#,##0.##"/> ₫
                                    </td>

                                    <td>
                                        <meter min="0"
                                               max="${chartMax}"
                                               value="${r.totalRevenue}"
                                               aria-label="Net after refunds ${r.timeUnit}">
                                            <c:out value="${r.totalRevenue}"/>
                                        </meter>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>

                        <c:if test="${revenueView and empty reportData}">
                            <div class="report-empty">
                                ${vi
                                  ? 'Không có giao dịch đã thanh toán.'
                                  : 'No successful payments.'}
                            </div>
                        </c:if>
                    </div>
                </section>

            </c:if>

            <c:if test="${recordsView and reportView == 'reviews'}">

                <section class="uc23-review-summary">

                    <p class="uc23-scope">
                        ${vi
                          ? 'Thống kê toàn bộ kết quả theo bộ lọc, không đổi khi chuyển trang.'
                          : 'Totals cover all filtered results, independent of pagination.'}
                    </p>

                    <div class="uc23-cards uc23-money">
                        <div>
                            <span>
                                ${vi ? 'Tổng đánh giá' : 'Total reviews'}
                            </span>

                            <strong>${reviewSummary.total}</strong>

                            <small>
                                ${vi
                                  ? 'Chờ / Từ chối'
                                  : 'Pending / Rejected'}:
                                ${reviewSummary.pending}
                                /
                                ${reviewSummary.rejected}
                            </small>
                        </div>

                        <div>
                            <span>${vi ? 'Đã duyệt' : 'Approved'}</span>
                            <strong>${reviewSummary.approved}</strong>
                        </div>

                        <div>
                            <span>
                                ${vi
                                  ? 'Trung bình đã duyệt'
                                  : 'Approved average'}
                            </span>

                            <strong>
                                <c:choose>
                                    <c:when test="${reviewSummary.approved > 0}">
                                        <fmt:formatNumber
                                            value="${reviewSummary.average}"
                                            pattern="0.00"/> / 5
                                    </c:when>

                                    <c:otherwise>N/A</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                    </div>

                    <section class="report-panel">
                        <div class="report-panel-heading">
                            <h2>
                                ${vi
                                  ? 'Phân bố sao đã duyệt'
                                  : 'Approved rating distribution'}
                            </h2>
                        </div>

                        <div style="padding:22px">

                            <c:forEach var="i" begin="2" end="10">
                                <c:set var="idx" value="${12-i}"/>

                                <c:set var="count"
                                       value="${reviewSummary.ratingCounts[idx]}"/>

                                <c:if test="${count > 0}">
                                    <div class="uc23-rating-row">
                                        <span>
                                            <fmt:formatNumber
                                                value="${idx/2.0}"
                                                pattern="0.0"/> ★
                                        </span>

                                        <meter class="rating-distribution-fill"
                                               min="0"
                                               max="${reviewSummary.approved}"
                                               value="${count}"
                                               aria-label="${idx/2.0} stars">
                                            <c:out value="${count}"/>
                                        </meter>

                                        <strong>${count}</strong>
                                    </div>
                                </c:if>
                            </c:forEach>

                            <c:if test="${reviewSummary.approved == 0}">
                                <p>
                                    ${vi
                                      ? 'Chưa có đánh giá đã duyệt.'
                                      : 'No approved reviews.'}
                                </p>
                            </c:if>

                            <c:if test="${reviewSummary.ratingCounts[0] > 0}">
                                <p>
                                    ${vi
                                      ? 'Mức sao khác (dữ liệu cũ)'
                                      : 'Other ratings (legacy data)'}:
                                    ${reviewSummary.ratingCounts[0]}
                                </p>
                            </c:if>

                        </div>
                    </section>

                </section>

            </c:if>

            <c:if test="${recordsView}">

                <section class="report-panel">
                    <div class="report-panel-heading">
                        <h2>
                            ${vi ? 'Danh sách chi tiết' : 'Detailed records'}
                        </h2>

                        <span>
                            ${totalRecords}
                            ${vi ? 'kết quả' : 'results'}
                        </span>
                    </div>

                    <div class="report-table-scroll">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <c:choose>
                                    <c:when test="${reportView == 'payments'}">
                                        <th>ID</th>
                                        <th>
                                            ${vi
                                              ? 'Đặt phòng / Khách'
                                              : 'Booking / Guest'}
                                        </th>
                                        <th>${vi ? 'Phòng' : 'Rooms'}</th>
                                        <th>
                                            ${vi
                                              ? 'Ngày thanh toán'
                                              : 'Payment date'}
                                        </th>
                                        <th class="numeric">
                                            ${vi ? 'Tiền giao dịch' : 'Amount'}
                                        </th>
                                        <th class="numeric">
                                            ${vi ? 'Đã hoàn' : 'Refunded'}
                                        </th>
                                        <th class="numeric">
                                            ${vi ? 'Còn lại' : 'Net'}
                                        </th>
                                        <th>${vi ? 'Trạng thái' : 'Status'}</th>
                                        <th>${vi ? 'Phương thức' : 'Method'}</th>
                                        <th>
                                            ${vi
                                              ? 'Giảm đã lưu'
                                              : 'Stored discount'}
                                        </th>
                                        <th>
                                            ${vi
                                              ? 'Mã giao dịch'
                                              : 'Reference'}
                                        </th>
                                    </c:when>

                                    <c:when test="${reportView == 'reviews'}">
                                        <th>ID</th>
                                        <th>
                                            ${vi
                                              ? 'Đặt phòng / Khách'
                                              : 'Booking / Guest'}
                                        </th>
                                        <th>${vi ? 'Phòng' : 'Rooms'}</th>
                                        <th>${vi ? 'Ngày gửi' : 'Submitted'}</th>
                                        <th>${vi ? 'Sao' : 'Rating'}</th>
                                        <th>${vi ? 'Nhận xét' : 'Comment'}</th>
                                        <th>${vi ? 'Trạng thái' : 'Status'}</th>
                                    </c:when>

                                    <c:otherwise>
                                        <th>ID</th>
                                        <th>${vi ? 'Khách' : 'Guest'}</th>
                                        <th>${vi ? 'Ngày tạo' : 'Created'}</th>
                                        <th>
                                            ${vi
                                              ? 'Phòng / Lưu trú'
                                              : 'Rooms / Stay'}
                                        </th>
                                        <th>${vi ? 'Trạng thái' : 'Status'}</th>
                                        <th class="numeric">
                                            ${vi
                                              ? 'Tổng đơn'
                                              : 'Booking total'}
                                        </th>
                                        <th>
                                            ${vi
                                              ? 'Ghi chú / Hủy'
                                              : 'Notes / Cancellation'}
                                        </th>
                                    </c:otherwise>
                                </c:choose>
                            </tr>
                            </thead>

                            <tbody>
                            <c:choose>

                                <c:when test="${reportView == 'payments'}">
                                    <c:forEach var="p" items="${payments}">
                                        <tr>
                                            <td>#${p.id}</td>

                                            <td>
                                                <strong>#${p.booking.id}</strong>

                                                <div>
                                                    <c:out value="${p.booking.customer.firstName}"/>
                                                    <c:out value="${p.booking.customer.lastName}"/>
                                                </div>
                                            </td>

                                            <td>
                                                <c:forEach var="d"
                                                           items="${p.booking.details}">
                                                    <div>${d.roomNumber}</div>
                                                </c:forEach>
                                            </td>

                                            <td>
                                                <c:out value="${empty p.paymentDate
                                                               ? '—'
                                                               : fn:replace(p.paymentDate,'T',' ')}"/>
                                            </td>

                                            <td class="numeric">
                                                <fmt:formatNumber
                                                    value="${p.amount}"
                                                    pattern="#,##0.##"/> ₫
                                            </td>

                                            <td class="numeric">
                                                <fmt:formatNumber
                                                    value="${p.refundedAmount}"
                                                    pattern="#,##0.##"/> ₫
                                            </td>

                                            <td class="numeric">
                                                <c:choose>
                                                    <c:when test="${p.paymentStatus == 'paid'
                                                                    or p.paymentStatus == 'refunded'}">
                                                        <fmt:formatNumber
                                                            value="${p.historyRemainingAmount}"
                                                            pattern="#,##0.##"/> ₫
                                                    </c:when>

                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td>
                                                <span class="report-status <c:out value='${p.historyStatus}'/>">
                                                    <c:choose>
                                                        <c:when test="${p.historyStatus == 'partially_refunded'}">
                                                            ${vi
                                                              ? 'Hoàn một phần'
                                                              : 'Partially refunded'}
                                                        </c:when>

                                                        <c:when test="${p.historyStatus == 'refunded'}">
                                                            ${vi
                                                              ? 'Đã hoàn toàn bộ'
                                                              : 'Fully refunded'}
                                                        </c:when>

                                                        <c:otherwise>
                                                            <c:out value="${p.paymentStatus}"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </td>

                                            <td>
                                                <c:out value="${p.paymentMethod}"/>
                                            </td>

                                            <td>
                                                <c:out value="${p.discount.code}"/>

                                                <div>
                                                    <fmt:formatNumber
                                                        value="${p.discountAmount}"
                                                        pattern="#,##0.##"/> ₫
                                                </div>
                                            </td>

                                            <td>
                                                <c:out value="${p.transactionCode}"/>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>

                                <c:when test="${reportView == 'reviews'}">
                                    <c:forEach var="r" items="${reviews}">
                                        <tr>
                                            <td>#${r.reviewID}</td>

                                            <td>
                                                #${r.booking.id}

                                                <div>
                                                    <c:out value="${r.booking.customer.firstName}"/>
                                                    <c:out value="${r.booking.customer.lastName}"/>
                                                </div>
                                            </td>

                                            <td>
                                                <c:forEach var="d"
                                                           items="${r.booking.details}">
                                                    <div>${d.roomNumber}</div>
                                                </c:forEach>
                                            </td>

                                            <td>
                                                <c:out value="${fn:replace(r.reviewDate,'T',' ')}"/>
                                            </td>

                                            <td>
                                                <fmt:formatNumber
                                                    value="${r.star}"
                                                    pattern="0.0"/> / 5
                                            </td>

                                            <td>
                                                <c:out value="${r.comment}"/>
                                            </td>

                                            <td>
                                                <span class="report-status <c:out value='${r.reviewStatus}'/>">
                                                    <c:out value="${r.reviewStatus}"/>
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach var="b" items="${bookings}">
                                        <tr>
                                            <td>#${b.id}</td>

                                            <td>
                                                <c:out value="${b.customer.firstName}"/>
                                                <c:out value="${b.customer.lastName}"/>

                                                <div>
                                                    <c:out value="${b.customer.email}"/>
                                                </div>
                                            </td>

                                            <td>
                                                <c:out value="${fn:replace(b.bookingDate,'T',' ')}"/>
                                            </td>

                                            <td>
                                                <c:forEach var="d"
                                                           items="${b.details}">
                                                    <div>
                                                        <strong>${d.roomNumber}</strong>:
                                                        ${d.checkInDate}
                                                        →
                                                        ${d.checkOutDate};
                                                        ${d.guestCount}
                                                        ${vi ? 'khách' : 'guests'}
                                                    </div>
                                                </c:forEach>
                                            </td>

                                            <td>
                                                <span class="report-status <c:out value='${b.status}'/>">
                                                    <c:out value="${b.status}"/>
                                                </span>
                                            </td>

                                            <td class="numeric">
                                                <fmt:formatNumber
                                                    value="${b.totalPrice}"
                                                    pattern="#,##0.##"/> ₫
                                            </td>

                                            <td>
                                                <c:out value="${b.specialRequest}"/>

                                                <c:if test="${not empty b.cancellationReason}">
                                                    <div>
                                                        <c:out value="${b.cancellationReason}"/>
                                                    </div>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>

                            </c:choose>
                            </tbody>
                        </table>

                        <c:if test="${totalRecords == 0}">
                            <div class="report-empty">
                                ${vi
                                  ? 'Không có kết quả phù hợp.'
                                  : 'No matching records.'}
                            </div>
                        </c:if>
                    </div>
                </section>

                <%@ include file="report-list-pagination.jsp" %>

            </c:if>

        </c:otherwise>
    </c:choose>

</main>

<%@ include file="/WEB-INF/include/footer.jsp" %>

<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>

</body>
</html>