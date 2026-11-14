<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="stay-page" lang="${requestScope.uiLanguage}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${requestScope.i18n['room.qr.title']}"/></title>
    <link rel="stylesheet" href="<c:url value='/assets/css/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/general.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/luxe-shared-shell.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/room-checkout.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/payment-qr-display.css?v=1'/>">
</head>
<body class="body-wrapper">
    <%@ include file="../include/header.jsp" %>
    <main class="stay-content">
        <div class="container stay-shell">
            <a href="<c:url value='/history?view=bookings'/>" class="stay-back"><ui:text key="room.checkout.history"/></a>
            <div class="stay-heading qr-heading"><h1><ui:text key="room.qr.title"/></h1></div>
            <c:url var="qrPageUrl" value="/payment"><c:param name="bookingId" value="${qrBookingId}"/></c:url>
            <fmt:setLocale value="vi_VN" scope="page"/>
            <c:if test="${not empty qrAmount}">
                <fmt:formatNumber var="qrAmountFormatted" value="${qrAmount}" maxFractionDigits="0"/>
            </c:if>
            <div class="qr-layout">
                <aside>
                    <div class="stay-panel stay-summary p-4">
                        <h2 class="h5"><ui:text key="room.qr.booking"/> #${qrBookingId}</h2>
                        <c:if test="${not empty qrAmount}">
                            <div class="mt-3"><ui:text key="room.checkout.due"/></div>
                            <div class="stay-rate"><c:out value="${qrAmountFormatted}"/>&nbsp;VNĐ</div>
                        </c:if>
                        <c:if test="${qrState == 'pending'}">
                            <div class="qr-time rounded-3 p-3 mt-3">
                                <ui:text key="room.qr.time"/>
                                <div id="qr-clock" class="fs-3 fw-bold" role="timer">--:--</div>
                            </div>
                        </c:if>
                    </div>
                </aside>
                <div>
                    <section class="stay-panel p-3 p-md-4" id="qr-payment-options">
                        <c:choose>
                            <c:when test="${qrState == 'pending'}">
                                <div id="payos-checkout" class="qr-methods ${not empty qrAccountNumber && not empty qrDescription ? 'qr-methods-split' : ''}"
                                     data-page-url="<c:out value='${qrPageUrl}'/>"
                                     data-checkout-url="<c:out value='${qrCheckoutUrl}'/>"
                                     data-return-url="<c:out value='${qrReturnUrl}'/>" data-seconds="${qrSeconds}">
                                    <div class="qr-scan">
                                        <h2 class="h5 fw-bold mb-3"><ui:text key="room.qr.scan"/></h2>
                                        <div id="qr-image" class="qr-image" hidden role="img"
                                             aria-label="<c:out value="${requestScope.i18n['room.qr.scan']}"/>"
                                             data-i18n-aria-label="room.qr.scan"
                                             data-payload="<c:out value='${qrPayload}'/>"></div>
                                        <div id="qr-embedded-fallback" class="qr-embedded-fallback" hidden></div>
                                        <div id="qr-logos" class="qr-logos" hidden>
                                            <img src="https://upload.wikimedia.org/wikipedia/commons/7/77/VietQR_Logo.png"
                                                 width="76" height="23" alt="VietQR" referrerpolicy="no-referrer">
                                            <c:if test="${qrBankBin == '970422'}">
                                                <img src="https://api.vietqr.io/img/MB.png" width="60" height="23"
                                                     alt="MB Bank" referrerpolicy="no-referrer">
                                            </c:if>
                                        </div>
                                        <div id="qr-waiting" class="qr-waiting" role="status" hidden>
                                            <span class="qr-spinner" aria-hidden="true"></span>
                                            <span id="qr-waiting-text"><ui:text key="room.qr.waiting"/></span>
                                            <span id="qr-processing-text" hidden><ui:text key="room.qr.processing"/></span>
                                            <span id="qr-poll-error" hidden><ui:text key="room.qr.poll.error"/></span>
                                        </div>
                                        <p id="qr-public-page" class="small text-muted mt-3 mb-0" hidden>
                                            <a href="<c:out value='${qrReturnUrl}'/>" class="stay-back"><ui:text key="room.qr.open.public"/></a>
                                        </p>
                                        <p id="qr-fallback" class="small text-muted mt-3 mb-0">
                                            <ui:text key="room.qr.render.fallback"/>
                                        </p>
                                    </div>
                                <c:if test="${not empty qrAccountNumber && not empty qrDescription}">
                                    <div class="qr-manual">
                                        <h2 class="h5 fw-bold mb-3"><ui:text key="room.qr.manual"/></h2>
                                        <dl class="qr-transfer-list mb-2">
                                            <c:if test="${not empty qrAccountName}">
                                                <div><dt><ui:text key="room.qr.account.name"/></dt><dd><c:out value="${qrAccountName}"/></dd></div>
                                            </c:if>
                                            <c:if test="${not empty qrBankBin}">
                                                <div>
                                                    <dt><ui:text key="room.qr.bank"/></dt>
                                                    <dd><c:choose>
                                                        <c:when test="${qrBankBin == '970422'}"><ui:text key="room.qr.bank.mb"/></c:when>
                                                        <c:otherwise><ui:text key="room.qr.bank.bin"/> <c:out value="${qrBankBin}"/></c:otherwise>
                                                    </c:choose></dd>
                                                </div>
                                            </c:if>
                                            <div>
                                                <dt><ui:text key="room.qr.account.number"/></dt>
                                                <dd><span id="qr-account"><c:out value="${qrAccountNumber}"/></span>
                                                    <button type="button" class="qr-copy" data-copy-field="qr-account" hidden><ui:text key="room.qr.copy"/><span hidden><span aria-hidden="true">✓</span> <ui:text key="room.qr.copied"/></span></button>
                                                </dd>
                                            </div>
                                            <div>
                                                <dt><ui:text key="room.checkout.due"/></dt>
                                                <dd><span><c:out value="${qrAmountFormatted}"/>&nbsp;VNĐ</span>
                                                    <button type="button" class="qr-copy" data-copy-field="qr-amount" hidden><ui:text key="room.qr.copy"/><span hidden><span aria-hidden="true">✓</span> <ui:text key="room.qr.copied"/></span></button>
                                                    <span id="qr-amount" hidden><c:out value="${qrAmount.toBigIntegerExact()}"/></span>
                                                </dd>
                                            </div>
                                            <div>
                                                <dt><ui:text key="room.qr.description"/></dt>
                                                <dd><span id="qr-description"><c:out value="${qrDescription}"/></span>
                                                    <button type="button" class="qr-copy" data-copy-field="qr-description" hidden><ui:text key="room.qr.copy"/><span hidden><span aria-hidden="true">✓</span> <ui:text key="room.qr.copied"/></span></button>
                                                </dd>
                                            </div>
                                        </dl>
                                        <p class="small text-muted mb-0"><ui:text key="room.qr.manual.hint"/></p>
                                        <div id="qr-copy-feedback" role="status" class="small mt-2">
                                            <span id="qr-copy-failed" hidden><ui:text key="room.qr.copy.failed"/></span>
                                        </div>
                                    </div>
                                </c:if>
                                </div>
                                <div class="qr-actions mt-4">
                                    <a href="<c:out value='${qrCheckoutUrl}'/>" class="stay-back"><ui:text key="room.qr.hosted"/></a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="mb-3" role="status"><ui:text key="${'room.qr.state.'.concat(qrState)}"/></p>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </div>
            </div>
        </div>
    </main>
    <%@ include file="../include/footer.jsp" %>
    <script src="<c:url value='/assets/js/bootstrap.bundle.min.js'/>"></script>
    <c:if test="${qrState == 'pending'}">
        <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js" defer></script>
        <script src="https://cdn.payos.vn/payos-checkout/v1/stable/payos-initialize.js" defer></script>
        <script src="<c:url value='/assets/js/embedded-checkout.js'/>" defer></script>
    </c:if>
</body>
</html>
