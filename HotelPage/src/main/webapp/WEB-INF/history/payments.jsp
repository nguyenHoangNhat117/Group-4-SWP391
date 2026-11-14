<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="${requestScope.uiLanguage == 'vi' ? 'vi_VN' : 'en_US'}" scope="page"/>
<c:url var="paymentHistoryUrl" value="/history">
    <c:param name="view" value="payments"/>
    <c:if test="${not ownHistory}">
        <c:param name="id" value="${historyCustomerId}"/>
    </c:if>
</c:url>
<c:set var="clearPaymentSearchUrl" value="${paymentHistoryUrl}"/>
<c:if test="${hasPaymentFilters}">
    <c:url var="paymentHistoryUrl" value="/history">
        <c:param name="view" value="payments"/>
        <c:if test="${not ownHistory}">
            <c:param name="id" value="${historyCustomerId}"/>
        </c:if>
        <c:if test="${not empty paymentSearch}"><c:param name="q" value="${paymentSearch}"/></c:if>
        <c:if test="${not empty paymentMethodFilter}"><c:param name="method" value="${paymentMethodFilter}"/></c:if>
        <c:if test="${not empty paymentStatusFilter}"><c:param name="status" value="${paymentStatusFilter}"/></c:if>
        <c:if test="${not empty paymentDateFilter}"><c:param name="date" value="${paymentDateFilter}"/></c:if>
    </c:url>
</c:if>
<c:url var="bookingHistoryUrl" value="/history">
    <c:param name="view" value="bookings"/>
    <c:if test="${not ownHistory}">
        <c:param name="id" value="${historyCustomerId}"/>
    </c:if>
</c:url>
<!DOCTYPE html>
<html lang="<c:out value='${requestScope.uiLanguage}'/>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title data-i18n="screen.d4c7bc94c3af4d4f"><c:out value="${requestScope.i18n['screen.d4c7bc94c3af4d4f']}"/></title>
    <link rel="stylesheet" href="<c:url value='/assets/css/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/general.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/luxe-modern.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/luxe-shared-shell.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/luxe-ui.css'/>">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css">
    <link rel="stylesheet" href="<c:url value='/assets/css/payment-history.css?v=7'/>">
</head>
<body>
<div class="body-wrapper">
    <%@ include file="/WEB-INF/include/header.jsp" %>

    <main class="payment-history container" aria-labelledby="payment-history-title">
        <a class="payment-history-back" href="<c:out value='${pageContext.request.contextPath}'/>/">
            <span aria-hidden="true">←</span>
            <span data-i18n="history.payments.backHome"><c:out
                value="${requestScope.i18n['history.payments.backHome']}"
                default="${requestScope.uiLanguage == 'vi' ? 'Về trang chủ' : 'Back to home'}"/></span>
        </a>
        <div class="payment-history-heading">
            <h1 id="payment-history-title"><ui:text key="screen.d4c7bc94c3af4d4f"/></h1>
            <a class="payment-history-bookings-link" href="<c:out value='${bookingHistoryUrl}'/>">
                <ui:text key="screen.0927faa59b0c912b"/>
                <span aria-hidden="true">→</span>
            </a>
        </div>

        <form class="payment-history-search" method="get" action="<c:out value='${pageContext.request.contextPath}'/>/history" role="search"
              aria-labelledby="payment-search-label">
            <input type="hidden" name="view" value="payments">
            <c:if test="${not ownHistory}">
                <input type="hidden" name="id" value="${historyCustomerId}">
            </c:if>
            <div class="payment-history-search-field">
                <label id="payment-search-label" for="payment-search"><ui:text key="history.payments.search"/></label>
                <input class="form-control" id="payment-search" name="q" type="search" maxlength="100"
                       value="<c:out value='${paymentSearch}'/>" data-i18n-placeholder="history.payments.searchHint"
                       placeholder="<c:out value='${requestScope.i18n["history.payments.searchHint"]}'/>">
            </div>
            <div class="payment-history-filter">
                <label for="payment-method"><ui:text key="screen.52a0f9b65b278850"/></label>
                <select class="form-select" id="payment-method" name="method">
                    <option value="" data-i18n="screen.a52ace420f2175d0"><c:out value="${requestScope.i18n['screen.a52ace420f2175d0']}"/></option>
                    <c:forEach var="method" items="${paymentMethods}">
                        <option value="${method}" data-i18n="${paymentLabels[method]}" ${method == paymentMethodFilter ? 'selected' : ''}><c:out value="${requestScope.i18n[paymentLabels[method]]}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="payment-history-filter">
                <label for="payment-status"><ui:text key="screen.920e413c7d411b61"/></label>
                <select class="form-select" id="payment-status" name="status">
                    <option value="" data-i18n="screen.a52ace420f2175d0"><c:out value="${requestScope.i18n['screen.a52ace420f2175d0']}"/></option>
                    <c:forEach var="status" items="${paymentStatuses}">
                        <option value="${status}" data-i18n="${paymentLabels[status]}" ${status == paymentStatusFilter ? 'selected' : ''}><c:out value="${requestScope.i18n[paymentLabels[status]]}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="payment-history-filter">
                <label for="payment-date"><ui:text key="screen.10b5440f239de9cb"/></label>
                <input class="form-control" id="payment-date" name="date" type="date"
                       value="<c:out value='${paymentDateFilter}'/>" min="0001-01-01" max="9999-12-30">
            </div>
            <div class="payment-history-search-actions">
                <button class="btn payment-history-search-submit" type="submit"><ui:text key="screen.49c266baaaa70981"/></button>
                <c:if test="${hasPaymentFilters}">
                    <a class="payment-history-back payment-history-clear" href="<c:out value='${clearPaymentSearchUrl}'/>">
                        <ui:text key="history.payments.clearSearch"/>
                    </a>
                </c:if>
            </div>
        </form>

        <c:choose>
            <c:when test="${paymentHistoryUnavailable}">
                <div class="payment-history-state" role="alert">
                    <p><ui:text key="history.payments.unavailable"/></p>
                    <a class="btn btn-outline-secondary"
                       href="<c:out value='${paymentHistoryUrl}'/>&amp;page=${currentPage}">
                        <ui:text key="history.payments.retry"/>
                    </a>
                </div>
            </c:when>
            <c:when test="${empty payments}">
                <div class="payment-history-state">
                    <p class="mb-0">
                        <c:choose>
                            <c:when test="${hasPaymentFilters}"><ui:text key="history.payments.noMatches"/></c:when>
                            <c:otherwise><ui:text key="screen.68a815f07fea6622"/></c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="payment-history-columns payment-history-grid" aria-hidden="true">
                    <ui:text key="screen.a85aca2f532a24ab"/>
                    <ui:text key="history.payments.amount"/>
                    <ui:text key="screen.920e413c7d411b61"/>
                    <ui:text key="screen.52a0f9b65b278850"/>
                    <ui:text key="screen.10b5440f239de9cb"/>
                </div>
                <ul class="payment-history-list" role="list">
                    <c:forEach var="payment" items="${payments}">
                        <li class="payment-history-card">
                            <dl class="payment-history-grid payment-history-overview">
                                <div>
                                    <dt><ui:text key="screen.a85aca2f532a24ab"/></dt>
                                    <dd class="payment-history-booking">#${payment.booking.id}</dd>
                                </div>
                                <div>
                                    <dt><ui:text key="history.payments.amount"/></dt>
                                    <dd class="payment-history-amount">
                                        <span data-history-money="${payment.amount}"><fmt:formatNumber value="${payment.amount}" maxFractionDigits="2"/></span>&nbsp;VND
                                    </dd>
                                </div>
                                <div>
                                    <dt><ui:text key="screen.920e413c7d411b61"/></dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${payment.historyStatus == 'refunded'}">
                                                <span class="payment-history-status status-refunded"><ui:text key="screen.117f6a7cf0372277"/></span>
                                            </c:when>
                                            <c:when test="${payment.historyStatus == 'partially_refunded'}">
                                                <span class="payment-history-status status-refunded"><ui:text key="history.payments.partiallyRefunded"/></span>
                                            </c:when>
                                            <c:when test="${payment.historyStatus == 'partial'}">
                                                <span class="payment-history-status status-partial"><ui:text key="screen.1390db305695537e"/></span>
                                            </c:when>
                                            <c:when test="${payment.historyStatus == 'paid'}">
                                                <span class="payment-history-status status-paid"><ui:text key="screen.fb81b961af456e5e"/></span>
                                            </c:when>
                                            <c:when test="${payment.paymentStatus == 'pending'}">
                                                <span class="payment-history-status status-pending"><ui:text key="screen.331551b0de4157c9"/></span>
                                            </c:when>
                                            <c:when test="${payment.paymentStatus == 'failed'}">
                                                <span class="payment-history-status status-failed"><ui:text key="screen.031a8f0f659df890"/></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="payment-history-status"><ui:message text="${payment.paymentStatus}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                                <div>
                                    <dt><ui:text key="screen.52a0f9b65b278850"/></dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${payment.paymentMethod == 'cash'}"><ui:text key="screen.e2da108f63008e44"/></c:when>
                                            <c:when test="${payment.paymentMethod == 'bank_transfer'}"><ui:text key="screen.ee82ae2801cf9097"/></c:when>
                                            <c:when test="${payment.paymentMethod == 'credit_card'}"><ui:text key="screen.9f629f57b7d02936"/></c:when>
                                            <c:when test="${payment.paymentMethod == 'e_wallet'}"><ui:text key="screen.f7bb9a1dcdb515df"/></c:when>
                                            <c:otherwise><c:out value="${payment.paymentMethod}" default="—"/></c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                                <div>
                                    <dt><ui:text key="screen.10b5440f239de9cb"/></dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${payment.paymentDate != null}">
                                                <time datetime="${payment.paymentDate}" data-history-datetime>
                                                    <c:out value="${payment.paymentDate.format(paymentDateFormat)}"/>
                                                </time>
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                            </dl>

                            <details class="payment-history-details">
                                <summary>
                                    <span class="payment-history-show"><ui:text key="screen.90789c12d0731b44"/></span>
                                    <span class="payment-history-hide" data-i18n="history.payments.hideDetails"><c:out
                                        value="${requestScope.i18n['history.payments.hideDetails']}"
                                        default="${requestScope.uiLanguage == 'vi' ? 'Thu gọn' : 'Hide details'}"/></span>
                                    <span class="payment-history-chevron" aria-hidden="true"></span>
                                </summary>
                                <div class="payment-history-expanded">
                                    <section class="payment-history-panel">
                                        <h2><ui:text key="screen.e9143587ebf6f078"/></h2>
                                        <dl class="payment-history-totals">
                                            <div>
                                                <dt><ui:text key="history.payments.bookingTotal"/></dt>
                                                <dd>
                                                    <span data-history-money="${payment.booking.totalPrice}"><fmt:formatNumber value="${payment.booking.totalPrice}" maxFractionDigits="2"/></span>&nbsp;VND
                                                </dd>
                                            </div>
                                            <c:if test="${payment.historyRemainingAmount > 0 and (payment.historyStatus == 'partial' or payment.historyStatus == 'partially_refunded')}">
                                                <div class="payment-history-balance">
                                                    <dt><ui:text key="history.payments.balance"/></dt>
                                                    <dd><span data-history-money="${payment.historyRemainingAmount}"><fmt:formatNumber value="${payment.historyRemainingAmount}" maxFractionDigits="2"/></span>&nbsp;VND</dd>
                                                </div>
                                            </c:if>
                                        </dl>
                                        <dl class="payment-history-meta">
                                            <div>
                                                <dt><ui:text key="screen.d9c9afd836811e5a"/></dt>
                                                <dd>#${payment.id}</dd>
                                            </div>
                                            <div class="payment-history-reference">
                                                <dt><ui:text key="screen.346055a5f7d303b7"/></dt>
                                                <dd><code><c:out value="${empty payment.transactionCode ? '—' : payment.transactionCode}"/></code></dd>
                                            </div>
                                            <c:if test="${payment.discount != null or payment.discountAmount > 0}">
                                                <div>
                                                    <dt><ui:text key="screen.cb72ec985b5f3d75"/></dt>
                                                    <dd class="payment-history-discount">
                                                        <c:if test="${not empty payment.discount.code}">
                                                            <span class="payment-history-discount-code"><c:out value="${payment.discount.code}"/></span>
                                                        </c:if>
                                                        <c:if test="${payment.discountAmount != null}">
                                                            <span><span data-history-money="${payment.discountAmount}"><fmt:formatNumber value="${payment.discountAmount}" maxFractionDigits="2"/></span>&nbsp;VND</span>
                                                        </c:if>
                                                    </dd>
                                                </div>
                                            </c:if>
                                            <c:if test="${payment.refundedAmount > 0}">
                                                <div class="payment-history-refund">
                                                    <dt><ui:text key="history.payments.refundedAmount"/></dt>
                                                    <dd>
                                                        <span data-history-money="${payment.refundedAmount}"><fmt:formatNumber value="${payment.refundedAmount}" maxFractionDigits="2"/></span>&nbsp;VND
                                                    </dd>
                                                </div>
                                            </c:if>
                                        </dl>
                                    </section>
                                    <section class="payment-history-panel">
                                        <h2><ui:text key="screen.fe43f0a1373a3d68"/></h2>
                                        <dl class="payment-history-stay-status">
                                            <dt><ui:text key="screen.ea50259543e17999"/></dt>
                                            <dd><ui:message text="${payment.booking.status}"/></dd>
                                        </dl>
                                        <c:if test="${not empty payment.booking.details}">
                                            <ul class="payment-history-stays" role="list">
                                                <c:forEach var="detail" items="${payment.booking.details}">
                                                    <li>
                                                        <div class="payment-history-room">
                                                            <strong><ui:text key="screen.6c8c6af24c9b021c"/>${detail.roomNumber}</strong>
                                                            <span><ui:text key="screen.1ac580f9fde7374f"/> ${detail.guestCount}</span>
                                                        </div>
                                                        <dl class="payment-history-breakdown">
                                                            <div>
                                                                <dt><ui:text key="screen.f49fc7767bcfb17b"/></dt>
                                                                <dd><time datetime="${detail.checkInDate}" data-history-date><c:out value="${detail.checkInDate.format(stayDateFormat)}"/></time></dd>
                                                            </div>
                                                            <div>
                                                                <dt><ui:text key="screen.5b4ecf4c9808dca1"/></dt>
                                                                <dd><time datetime="${detail.checkOutDate}" data-history-date><c:out value="${detail.checkOutDate.format(stayDateFormat)}"/></time></dd>
                                                            </div>
                                                        </dl>
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </c:if>
                                    </section>
                                </div>
                            </details>
                        </li>
                    </c:forEach>
                </ul>

                <c:if test="${totalPages > 1}">
                    <nav class="payment-history-pages" data-i18n-aria-label="screen.34c35b4b93d2c51e"
                         aria-label="<c:out value="${requestScope.i18n['screen.34c35b4b93d2c51e']}"/>">
                        <c:if test="${currentPage > 1}">
                            <a href="<c:out value='${paymentHistoryUrl}'/>&amp;page=${currentPage - 1}"><ui:text key="screen.a57b08a480b822a0"/></a>
                        </c:if>
                        <c:if test="${firstPageLink > 1}">
                            <a href="<c:out value='${paymentHistoryUrl}'/>&amp;page=1">1</a>
                            <c:if test="${firstPageLink > 2}"><span aria-hidden="true">…</span></c:if>
                        </c:if>
                        <c:forEach var="pageNumber" begin="${firstPageLink}" end="${lastPageLink}">
                            <c:choose>
                                <c:when test="${pageNumber == currentPage}">
                                    <span aria-current="page">${pageNumber}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="<c:out value='${paymentHistoryUrl}'/>&amp;page=${pageNumber}">${pageNumber}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                        <c:if test="${lastPageLink < totalPages}">
                            <c:if test="${lastPageLink < totalPages - 1}"><span aria-hidden="true">…</span></c:if>
                            <a href="<c:out value='${paymentHistoryUrl}'/>&amp;page=${totalPages}">${totalPages}</a>
                        </c:if>
                        <c:if test="${currentPage < totalPages}">
                            <a href="<c:out value='${paymentHistoryUrl}'/>&amp;page=${currentPage + 1}"><ui:text key="screen.1ff57a29d7c9d11b"/></a>
                        </c:if>
                    </nav>
                </c:if>
            </c:otherwise>
        </c:choose>
    </main>

    <%@ include file="/WEB-INF/include/footer.jsp" %>
</div>
<script src="<c:url value='/assets/js/bootstrap.bundle.min.js'/>"></script>
<script defer src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>
<script defer src="<c:url value='/assets/js/payment-history.js?v=3'/>"></script>
</body>
</html>
