<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="stay-page" lang="${requestScope.uiLanguage}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${requestScope.i18n['room.checkout.title']}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/general.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/room-checkout.css">
</head>
<body class="body-wrapper">
    <%@ include file="../include/header.jsp" %>
    <main class="stay-content">
      <div class="container stay-shell">
        <c:url var="stayUrl" value="/room-stay"><c:param name="roomTypeId" value="${roomType.id}"/></c:url>
        <c:choose>
            <c:when test="${empty existingBooking}">
                <a href="${stayUrl}" class="stay-back"><i class="bi bi-arrow-left" aria-hidden="true"></i> <ui:text key="room.back.stay"/></a>
            </c:when>
            <c:otherwise>
                <a href="<c:url value='/history?view=bookings'/>" class="stay-back"><i class="bi bi-arrow-left" aria-hidden="true"></i> <ui:text key="room.checkout.back.history"/></a>
            </c:otherwise>
        </c:choose>
        <div class="stay-heading">
            <div class="stay-brand">Luxe Escape</div>
            <h1><ui:text key="room.checkout.title"/></h1>
        </div>
        <c:set var="canPay" value="${empty existingBooking || awaitingPayment}"/>
        <c:set var="fullPayment" value="${param['payment-plan'] == 'transfer'}"/>
        <div class="row g-4">
            <aside class="col-lg-5 order-lg-2">
                <div class="stay-panel stay-summary p-3 p-md-4">
                    <h2 class="h5"><c:out value="${roomType.name}"/><c:if test="${not empty existingBooking}">#${existingBooking.id}</c:if></h2>
                    <p>${quantity} <ui:text key="room.quantity"/> · ${guests} <ui:text key="screen.3c8e0fde6fcc71e0"/></p>
                    <p class="stay-dates"><i class="bi bi-calendar3" aria-hidden="true"></i> ${checkIn} → ${checkOut}</p>
                    <dl class="mb-0" aria-live="polite">
                        <div><dt><ui:text key="room.checkout.stay.total"/></dt>
                            <dd id="checkout-total"><ui:money amount="${total}"/></dd></div>
                        <div><dt>
                            <c:choose><c:when test="${canPay}"><ui:text key="room.checkout.due"/></c:when>
                                <c:otherwise><ui:text key="room.checkout.paid"/></c:otherwise></c:choose>
                            </dt><dd id="checkout-due"><ui:money amount="${canPay ? (pendingDue != null ? pendingDue : fullPayment ? total : initialDeposit) : paidAmount}"/></dd></div>
                        <div><dt><ui:text key="room.checkout.hotel.balance"/></dt>
                            <dd id="checkout-balance"><ui:money amount="${canPay ? (pendingBalance != null ? pendingBalance : fullPayment ? 0 : initialBalance) : balanceAmount}"/></dd></div>
                    </dl>
                </div>
            </aside>
            <div class="col-lg-7">
                <c:if test="${awaitingPayment && param.message == 'payment-returned-checking'}">
                    <p class="alert alert-info" role="status"><ui:text key="room.checkout.verifying"/></p>
                </c:if>
                <c:if test="${checkoutError}">
                    <p class="alert alert-warning" role="alert"><ui:text key="room.checkout.error"/></p>
                </c:if>
                <c:if test="${voucherError || param.error == 'payment-failed' || param.error == 'online-not-configured'}">
                    <p class="alert alert-warning" role="alert">
                        <c:choose>
                            <c:when test="${voucherError}"><ui:text key="room.checkout.voucher.error"/></c:when>
                            <c:when test="${param.error == 'online-not-configured'}"><ui:text key="room.checkout.provider.error"/></c:when>
                            <c:otherwise><ui:text key="room.checkout.retry.error"/></c:otherwise>
                        </c:choose>
                    </p>
                </c:if>
                <c:choose>
                <c:when test="${not empty pendingCheckoutUrl}">
                    <a class="btn stay-action" href="<c:out value='${pendingCheckoutUrl}'/>"><ui:text key="room.checkout.resume"/></a>
                </c:when>
                <c:when test="${canPay && !paymentProcessing}">
                <c:url var="paymentAction" value="${empty existingBooking ? '/checkout' : '/payment'}"/>
                <form id="checkout-form" class="stay-panel p-3 p-md-4" action="${paymentAction}" method="post" data-total="${total}">
                    <c:choose>
                        <c:when test="${empty existingBooking}">
                            <input type="hidden" name="action" value="confirm">
                            <input type="hidden" name="draft" value="${draftToken}">
                        </c:when>
                        <c:otherwise><input type="hidden" name="bookingId" value="${existingBooking.id}"></c:otherwise>
                    </c:choose>
                    <h2 class="h5 stay-form-heading"><i class="bi bi-shield-check" aria-hidden="true"></i> <ui:text key="room.checkout.method"/></h2>
                    <div class="row g-3 mb-4">
                        <c:if test="${empty existingBooking}">
                        <div class="col-md-6">
                            <label for="contact-name" class="form-label"><ui:text key="room.contact.name"/></label>
                            <input id="contact-name" name="contact-name" autocomplete="name" maxlength="120" required class="form-control"
                                   value="<c:out value="${checkoutError ? param['contact-name'] : contactName}"/>">
                        </div>
                        <div class="col-md-6">
                            <label for="contact-phone" class="form-label"><ui:text key="room.contact.phone"/></label>
                            <input id="contact-phone" name="contact-phone" type="tel" autocomplete="tel" maxlength="32" required class="form-control"
                                   value="<c:out value="${checkoutError ? param['contact-phone'] : customer.phone}"/>">
                        </div>
                        <div class="col-md-6">
                            <label for="contact-email" class="form-label"><ui:text key="room.contact.email"/></label>
                            <input id="contact-email" name="contact-email" type="email" autocomplete="email" maxlength="254" required class="form-control"
                                   value="<c:out value="${checkoutError ? param['contact-email'] : customer.email}"/>">
                        </div>
                        <div class="col-md-6">
                            <label for="contact-country" class="form-label"><ui:text key="room.contact.country"/></label>
                            <input id="contact-country" name="contact-country" autocomplete="country-name" maxlength="80" required class="form-control"
                                   value="<c:out value="${checkoutError ? param['contact-country'] : customer.country}"/>">
                        </div>
                        </c:if>
                        <div class="col-12">
                            <label for="payment-plan" class="form-label"><ui:text key="room.checkout.method"/></label>
                            <select name="payment-plan" id="payment-plan" class="form-select" required>
                                <option value="deposit"><c:out value="${requestScope.i18n['room.checkout.deposit']}"/></option>
                                <option value="transfer" ${fullPayment ? 'selected' : ''}><c:out value="${requestScope.i18n['room.checkout.transfer']}"/></option>
                            </select>
                        </div>
                        <div class="col-12">
                            <label for="discount-code" class="form-label"><ui:text key="room.checkout.discount"/></label>
                            <select id="discount-code" name="discount-code" class="form-select">
                                <option value=""><c:out value="${requestScope.i18n['room.checkout.no.voucher']}"/></option>
                                <c:forEach var="voucher" items="${voucherTotals}">
                                    <option value="<c:out value='${voucher.key}'/>" data-total="${voucher.value}"
                                            ${param['discount-code'] == voucher.key ? 'selected' : ''}>
                                        <c:out value="${voucher.key}"/> · <ui:money amount="${total - voucher.value}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <button class="btn stay-action w-100" type="submit"><i class="bi bi-lock" aria-hidden="true"></i>
                        <ui:text key="room.checkout.confirm"/>
                    </button>
                </form>
                </c:when>
                <c:when test="${paymentProcessing}"><p class="alert alert-info"><ui:text key="room.checkout.processing"/></p></c:when>
                </c:choose>
                <div class="stay-panel stay-policy p-3 p-md-4 mt-4">
                    <h2 class="h5"><i class="bi bi-info-circle" aria-hidden="true"></i> <ui:text key="room.policy.title"/></h2>
                    <ul class="mb-0">
                        <li><ui:text key="room.policy.deposit"/></li>
                        <li><ui:text key="room.policy.free"/></li>
                        <li><ui:text key="room.policy.late"/></li>
                    </ul>
                </div>
            </div>
        </div>
      </div>
    </main>
    <%@ include file="../include/footer.jsp" %>
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/room-checkout.js" defer></script>
</body>
</html>
