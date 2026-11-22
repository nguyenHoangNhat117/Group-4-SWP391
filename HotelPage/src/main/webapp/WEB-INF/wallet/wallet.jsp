
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title>
            ${requestScope.i18n['screen.97e8a79942bca1da']}
        </title>

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
            .wallet-page {
                background: #faf7f2;
                color: #32291f;
                min-height: 100vh;
            }

            .wallet-wrap {
                max-width: 1160px;
                margin: auto;
                padding: 56px 22px 85px;
            }

            .wallet-eyebrow {
                font-size: 12px;
                letter-spacing: .30em;
                text-transform: uppercase;
                color: #987d5b;
                font-weight: 700;
            }

            .wallet-heading {
                font-family: Georgia, serif;
                font-size: clamp(38px, 5vw, 64px);
                margin: 12px 0 28px;
            }

            .wallet-balance {
                background: linear-gradient(
                    115deg, #31271f, #64503d
                    );
                border-radius: 22px;
                padding: 38px;
                color: #fff;
                box-shadow: 0 18px 38px #2e241f26;
            }

            .wallet-balance .amount {
                font-family: Georgia, serif;
                font-size: clamp(37px, 5vw, 58px);
                font-weight: 600;
            }

            .wallet-balance p {
                color: #e8ded2;
            }

            .wallet-panel {
                background: white;
                border: 1px solid #e7ded1;
                border-radius: 20px;
                margin-top: 30px;
                overflow: hidden;
            }

            .wallet-panel-head {
                padding: 27px 28px 13px;
            }

            .wallet-panel h2 {
                font-family: Georgia, serif;
                font-size: 27px;
            }

            .wallet-filter {
                padding: 15px 28px 26px;
            }

            .wallet-filter .form-label {
                font-size: 13px;
                font-weight: 600;
                color: #79644f;
            }

            .wallet-filter .form-control,
            .wallet-filter .form-select {
                border: 1px solid #e7ded1;
                border-radius: 9px;
                min-height: 42px;
            }

            .wallet-filter .form-control:focus,
            .wallet-filter .form-select:focus {
                border-color: #b49a78;
                box-shadow: 0 0 0 .2rem #b49a7826;
            }

            .wallet-table {
                margin-bottom: 0;
            }

            .wallet-table th {
                background: #f6f0e8;
                color: #79644f;
                font-size: 12px;
                letter-spacing: .09em;
                text-transform: uppercase;
                padding: 15px 20px;
                white-space: nowrap;
            }

            .wallet-table td {
                padding: 18px 20px;
                color: #493d30;
                vertical-align: middle;
            }

            .wallet-pill {
                background: #e7f4eb;
                color: #24764b;
                border-radius: 99px;
                padding: 5px 12px;
                font-size: 12px;
                font-weight: 700;
                display: inline-block;
            }

            .wallet-pill.withdrawal {
                background: #fbe8e8;
                color: #b43f3f;
            }

            .wallet-empty {
                text-align: center;
                padding: 45px 20px;
                color: #8b7d6f;
            }

            .wallet-links {
                display: flex;
                align-items: center;
                gap: 12px;
                flex-wrap: wrap;
            }

            .wallet-links a {
                text-decoration: none;
            }

            .wallet-btn {
                background: #b49a78;
                border: 1px solid #b49a78;
                color: white;
                padding: 10px 18px;
                border-radius: 9px;
                text-decoration: none;
                display: inline-block;
            }

            .wallet-btn:hover {
                background: #967c5b;
                border-color: #967c5b;
                color: white;
            }

            .wallet-btn-outline {
                border: 1px solid #b49a78;
                color: #79644f;
                background: transparent;
                padding: 10px 18px;
                border-radius: 9px;
                text-decoration: none;
                display: inline-block;
            }

            .wallet-btn-outline:hover {
                background: #f6f0e8;
                color: #493d30;
            }

            .wallet-booking-link {
                color: #7c6040;
                text-decoration: none;
            }

            .wallet-booking-link:hover {
                text-decoration: underline;
            }

            .wallet-back-link,
            .wallet-back-link:link,
            .wallet-back-link:visited {
                color: #7c6040 !important;
                font-size: 14px;
                font-weight: 500;
                text-decoration: none;
                white-space: nowrap;
            }

            .wallet-back-link:hover,
            .wallet-back-link:focus {
                color: #967c5b !important;
                text-decoration: underline;
            }


            .wallet-description {
                font-size: 13px;
                color: #8b7d6f;
                margin-top: 4px;
                max-width: 320px;
                overflow-wrap: anywhere;
            }

            .wallet-pagination {
                border-top: 1px solid #e7ded1;
                padding: 22px;
            }

            @media (max-width: 650px) {
                .wallet-wrap {
                    padding: 38px 14px;
                }

                .wallet-balance {
                    padding: 26px;
                }

                .wallet-panel-head {
                    padding: 22px 18px 12px;
                }

                .wallet-filter {
                    padding: 15px 18px 23px;
                }

                .wallet-table th,
                .wallet-table td {
                    padding: 12px 10px;
                    font-size: 12px;
                }

                .wallet-balance .amount {
                    overflow-wrap: anywhere;
                }
            }
        </style>
    <%@ include file="/WEB-INF/include/date-picker.jsp" %>
    </head>

    <body class="wallet-page">

        <%@ include file="/WEB-INF/include/header.jsp" %>

        <main class="wallet-wrap">

            <!-- PAGE HEADER -->
            <div class="wallet-eyebrow">
                <ui:text key="screen.281a98aef303211d"/>
            </div>


            <div class="d-flex justify-content-between
                 align-items-center flex-wrap gap-3 mb-4">

                <h1 class="wallet-heading mb-0">
                    <ui:text key="screen.46d8ad210d354832"/>
                </h1>

                <a href="${pageContext.request.contextPath}/history?view=bookings"
                   class="wallet-back-link">
                    &larr; Về lịch sử đặt phòng
                </a>

            </div>



            <!-- WALLET BALANCE -->
            <section class="wallet-balance">

                <div class="small text-uppercase"
                     style="letter-spacing:.14em;color:#e8d8c7">
                    SỐ DƯ KHẢ DỤNG
                </div>

                <div class="amount mt-2">
                    <fmt:formatNumber
                        value="${availableBalance}"
                        type="number"
                        maxFractionDigits="0"/>

                    <span style="font-size:22px">
                        VND
                    </span>
                </div>

            </section>


            <!-- WALLET ACTIONS -->
            <div class="wallet-links mt-4">



                <a href="${pageContext.request.contextPath}/withdrawal"
                   class="wallet-btn">
                    Yêu cầu rút tiền
                </a>

                <a href="${pageContext.request.contextPath}/my-withdrawals"
                   class="wallet-btn-outline">
                    Yêu cầu rút tiền của tôi
                </a>

            </div>

            <!-- TRANSACTION HISTORY -->
            <section class="wallet-panel" id="transactions">

                <div class="wallet-panel-head">

                    <h2>
                        <ui:text key="screen.de7c340f640fdc51"/>
                    </h2>

                    <p class="text-muted mb-0">
                        <ui:text key="screen.979374c44fb4d98e"/>
                    </p>

                </div>

                <!-- TRANSACTION FILTER -->
                <div class="wallet-filter">

                    <form action="${pageContext.request.contextPath}/wallet"
                          method="get"
                          class="row g-3 align-items-end">

                        <div class="col-md-3">

                            <label for="transactionType"
                                   class="form-label">
                                Loại giao dịch
                            </label>

                            <select id="transactionType"
                                    name="type"
                                    class="form-select">

                                <option value="">
                                    Tất cả giao dịch
                                </option>

                                <option value="refund"
                                        ${param.type == 'refund' ? 'selected' : ''}>
                                    Hoàn tiền
                                </option>

                                <option value="withdrawal"
                                        ${param.type == 'withdrawal' ? 'selected' : ''}>
                                    Rút tiền
                                </option>

                            </select>
                        </div>

                        <div class="col-md-3">

                            <label for="fromDate"
                                   class="form-label">
                                Từ ngày
                            </label>

                            <input type="date"
                                   id="fromDate"
                                   name="fromDate"
                                   class="form-control"
                                   value="${fn:escapeXml(param.fromDate)}">
                        </div>

                        <div class="col-md-3">

                            <label for="toDate"
                                   class="form-label">
                                Đến ngày
                            </label>

                            <input type="date"
                                   id="toDate"
                                   name="toDate"
                                   class="form-control"
                                   value="${fn:escapeXml(param.toDate)}">
                        </div>

                        <div class="col-md-3">

                            <button type="submit"
                                    class="wallet-btn">
                                Áp dụng bộ lọc
                            </button>

                            <a href="${pageContext.request.contextPath}/wallet"
                               class="btn btn-outline-secondary ms-1">
                                Xóa bộ lọc
                            </a>

                        </div>

                    </form>

                </div>

                <!-- TRANSACTION TABLE -->
                <div class="table-responsive">

                    <table class="table wallet-table">

                        <thead>
                            <tr>
                                <th>Mã giao dịch</th>
                                <th>Ngày</th>
                                <th>Đặt phòng / Nội dung</th>
                                <th>Loại</th>
                                <th class="text-end">Số tiền</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:forEach var="t"
                                       items="${transactions}">

                                <tr>

                                    <!-- TRANSACTION ID -->
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty t.transactionId}">
                                                #<c:out value="${t.transactionId}"/>
                                            </c:when>
                                            <c:otherwise>
                                                —
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <!-- DATE -->
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty t.createdAt}">
                                                <c:out value="${fn:substring(t.createdAt, 8, 10)}"/>/<c:out value="${fn:substring(t.createdAt, 5, 7)}"/>/<c:out value="${fn:substring(t.createdAt, 0, 4)}"/>
                                            </c:when>
                                            <c:otherwise>
                                                —
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <!-- BOOKING AND DESCRIPTION -->
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty t.bookingId}">
                                                <div class="fw-semibold">
                                                    Đặt phòng #
                                                    <c:out value="${t.bookingId}"/>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="fw-semibold">
                                                    Giao dịch ví
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <c:if test="${not empty t.description}">
                                            <div class="wallet-description">
                                                <c:out value="${t.description}"/>
                                            </div>
                                        </c:if>
                                    </td>

                                    <!-- TYPE -->
                                    <td>

                                        <c:choose>

                                            <c:when test="${t.type == 'withdrawal'}">
                                                <span class="wallet-pill withdrawal">
                                                    Rút tiền
                                                </span>
                                            </c:when>

                                            <c:when test="${t.type == 'refund'}">
                                                <span class="wallet-pill">
                                                    Hoàn tiền
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="wallet-pill">
                                                    <ui:message text="${t.type}"/>
                                                </span>
                                            </c:otherwise>

                                        </c:choose>
                                    </td>

                                    <!-- AMOUNT -->
                                    <td class="text-end fw-bold">

                                        <c:choose>

                                            <c:when test="${t.type == 'refund'}">

                                                <span class="text-success">
                                                    <c:if test="${t.amount >= 0}">
                                                        +
                                                    </c:if>

                                                    <fmt:formatNumber
                                                        value="${t.amount}"
                                                        type="number"
                                                        maxFractionDigits="0"/>
                                                    VND
                                                </span>

                                            </c:when>

                                            <c:when test="${t.type == 'withdrawal'}">

                                                <span class="text-danger">

                                                    <c:if test="${t.amount > 0}">
                                                        -
                                                    </c:if>

                                                    <fmt:formatNumber
                                                        value="${t.amount}"
                                                        type="number"
                                                        maxFractionDigits="0"/>
                                                    VND
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="text-secondary">

                                                    <fmt:formatNumber
                                                        value="${t.amount}"
                                                        type="number"
                                                        maxFractionDigits="0"/>
                                                    VND

                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                </tr>

                            </c:forEach>

                            <!-- EMPTY HISTORY -->

                            <c:if test="${empty transactions}">
                                <tr>
                                    <td colspan="5">

                                        <div class="wallet-empty">

                                            <c:choose>

                                                <c:when test="${param.type == 'withdrawal'}">
                                                    <div class="fw-semibold">
                                                        Chưa có giao dịch rút tiền.
                                                    </div>

                                                    <div class="mt-2 small">
                                                        Giao dịch rút tiền sẽ xuất hiện sau khi xác nhận chi trả.
                                                    </div>
                                                </c:when>

                                                <c:when test="${param.type == 'refund'}">
                                                    <div class="fw-semibold">
                                                        Chưa có giao dịch hoàn tiền.
                                                    </div>

                                                    <div class="mt-2 small">
                                                        Không có khoản hoàn tiền phù hợp bộ lọc.
                                                    </div>
                                                </c:when>

                                                <c:otherwise>
                                                    <div class="fw-semibold">
                                                        Chưa có giao dịch.
                                                    </div>

                                                    <div class="mt-2 small">
                                                        Hãy thay đổi bộ lọc hoặc xóa khoảng ngày đã chọn.
                                                    </div>
                                                </c:otherwise>

                                            </c:choose>

                                        </div>

                                    </td>
                                </tr>
                            </c:if>


                        </tbody>

                    </table>

                </div>

                <!-- PAGINATION -->
                <c:if test="${totalPages > 1}">

                    <nav class="wallet-pagination
                         d-flex justify-content-center
                         align-items-center gap-3 flex-wrap"
                         aria-label="Phân trang giao dịch ví">

                        <!-- PREVIOUS -->
                        <c:choose>

                            <c:when test="${currentPage > 1}">

                                <c:url var="previousUrl"
                                       value="/wallet">

                                    <c:param name="page"
                                             value="${currentPage - 1}"/>

                                    <c:param name="type"
                                             value="${param.type}"/>

                                    <c:param name="fromDate"
                                             value="${param.fromDate}"/>

                                    <c:param name="toDate"
                                             value="${param.toDate}"/>

                                </c:url>

                                <a href="${previousUrl}#transactions"
                                   class="btn btn-outline-secondary">
                                    Trước
                                </a>

                            </c:when>

                            <c:otherwise>

                                <button type="button"
                                        class="btn btn-outline-secondary"
                                        disabled>
                                    Trước
                                </button>

                            </c:otherwise>

                        </c:choose>

                        <!-- PAGE INFORMATION -->
                        <span class="small text-muted">

                            Trang
                            <c:out value="${currentPage}"/>
                            trên
                            <c:out value="${totalPages}"/>

                            <c:if test="${not empty totalTransactions}">
                                (
                                <c:out value="${totalTransactions}"/>
                                giao dịch)
                            </c:if>

                        </span>

                        <!-- NEXT -->
                        <c:choose>

                            <c:when test="${currentPage < totalPages}">

                                <c:url var="nextUrl"
                                       value="/wallet">

                                    <c:param name="page"
                                             value="${currentPage + 1}"/>

                                    <c:param name="type"
                                             value="${param.type}"/>

                                    <c:param name="fromDate"
                                             value="${param.fromDate}"/>

                                    <c:param name="toDate"
                                             value="${param.toDate}"/>

                                </c:url>

                                <a href="${nextUrl}#transactions"
                                   class="btn btn-outline-secondary">
                                    Tiếp
                                </a>

                            </c:when>

                            <c:otherwise>

                                <button type="button"
                                        class="btn btn-outline-secondary"
                                        disabled>
                                    Tiếp
                                </button>

                            </c:otherwise>

                        </c:choose>

                    </nav>

                </c:if>

            </section>

        </main>

        <script
            src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js">
        </script>

    </body>
</html>
