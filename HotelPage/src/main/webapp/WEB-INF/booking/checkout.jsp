<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="stay-page" lang="${requestScope.uiLanguage}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${requestScope.i18n['room.booking.checkout.title']}"/></title>
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
        <c:url var="stayUrl" value="/details">
            <c:param name="roomTypeId" value="${roomType.id}"/>
            <c:param name="checkIn" value="${checkIn}"/>
            <c:param name="checkOut" value="${checkOut}"/>
            <c:param name="quantity" value="${quantity}"/>
            <c:param name="guests" value="${guests}"/>
        </c:url>
        <a href="${stayUrl}" class="stay-back"><i class="bi bi-arrow-left" aria-hidden="true"></i> <ui:text key="room.back.stay"/></a>
        <div class="stay-heading"><div class="stay-brand">Luxe Escape</div><h1><ui:text key="room.booking.checkout.title"/></h1></div>
        <div class="row g-4">
            <aside class="col-lg-5 order-lg-2">
                <div class="stay-panel stay-summary p-3 p-md-4">
                    <h2 class="h4 fw-bold mb-3"><c:out value="${roomType.name}"/></h2>
                    <p class="checkout-stay-meta mb-2">
                        <span><i class="bi bi-door-open" aria-hidden="true"></i> <ui:text key="room.quantity"/> <strong>${quantity}</strong></span>
                        <span><i class="bi bi-people" aria-hidden="true"></i> <ui:text key="room.guests.total"/> <strong>${guests}</strong></span>
                    </p>
                    <p class="stay-dates flex-wrap"><i class="bi bi-calendar3" aria-hidden="true"></i> <time datetime="${checkIn}">${checkIn}</time> <span aria-hidden="true">&rarr;</span> <time datetime="${checkOut}">${checkOut}</time></p>
                    <dl class="mb-0" aria-live="polite">
                        <div><dt><ui:text key="room.checkout.stay.total"/></dt>
                            <dd><ui:money amount="${total}"/></dd></div>
                        <div class="checkout-due-row"><dt><ui:text key="room.checkout.due"/></dt>
                            <dd id="checkout-due"><ui:money amount="${initialDue}"/></dd></div>
                        <div><dt><ui:text key="room.checkout.hotel.balance"/></dt>
                            <dd id="checkout-balance"><ui:money amount="${initialBalance}"/></dd></div>
                    </dl>
                </div>
            </aside>
            <div class="col-lg-7">
                <c:if test="${checkoutError}"><p class="alert alert-warning" role="alert"><ui:text key="room.booking.contact.error"/></p></c:if>
                <c:if test="${paymentChoiceError}"><p class="alert alert-warning" role="alert"><ui:text key="room.checkout.choice.error"/></p></c:if>
                <c:if test="${not empty paymentError}">
                    <p class="alert alert-warning" role="alert">
                        <c:choose>
                            <c:when test="${paymentError == 'online-not-configured'}"><ui:text key="room.checkout.qr.config"/></c:when>
                            <c:when test="${paymentError == 'invalid-discount'}"><ui:text key="room.checkout.choice.error"/></c:when>
                            <c:otherwise><ui:text key="room.checkout.qr.failed"/></c:otherwise>
                        </c:choose>
                    </p>
                </c:if>
                <c:if test="${paymentSubmitted}">
                    <div class="alert alert-info" role="status">
                        <p class="mb-2"><ui:text key="room.checkout.qr.submitted"/></p>
                        <a href="<c:url value='/history?view=bookings'/>"><ui:text key="room.checkout.history"/></a>
                    </div>
                </c:if>
                <c:if test="${!paymentSubmitted}">
                <form id="checkout-form" class="stay-panel p-3 p-md-4" action="<c:url value='/payment'/>" method="post" data-total="${total}">
                    <input type="hidden" name="action" value="confirm">
                    <input type="hidden" name="draft" value="${draftToken}">
                    <fieldset class="mb-4">
                      <legend class="h5 stay-form-heading"><i class="bi bi-person me-2" aria-hidden="true"></i><ui:text key="room.booking.contact.title"/></legend>
                      <div class="row g-3">
                        <div class="col-md-6">
                            <label for="contact-name" class="form-label"><ui:text key="room.contact.name"/></label>
                            <input id="contact-name" name="contact-name" autocomplete="name" maxlength="120" required class="form-control"
                                   ${not empty bookingContact ? 'readonly' : ''}
                                   value="<c:out value="${not empty bookingContact ? bookingContact.name : checkoutError ? param['contact-name'] : contactName}"/>">
                        </div>
                        <div class="col-md-6">
                            <label for="contact-phone" class="form-label"><ui:text key="room.contact.phone"/></label>
                            <input id="contact-phone" name="contact-phone" type="tel" autocomplete="tel" maxlength="32" required class="form-control"
                                   ${not empty bookingContact ? 'readonly' : ''}
                                   value="<c:out value="${not empty bookingContact ? bookingContact.phone : checkoutError ? param['contact-phone'] : customer.phone}"/>">
                        </div>
                        <div class="col-md-6">
                            <label for="contact-email" class="form-label"><ui:text key="room.contact.email"/></label>
                            <input id="contact-email" name="contact-email" type="email" autocomplete="email" maxlength="254" required class="form-control"
                                   ${not empty bookingContact ? 'readonly' : ''}
                                   value="<c:out value="${not empty bookingContact ? bookingContact.email : checkoutError ? param['contact-email'] : customer.email}"/>">
                        </div>
                        <div class="col-md-6">
                            <label for="contact-country" class="form-label"><ui:text key="room.contact.country"/></label>
                            <input id="contact-country" name="contact-country" autocomplete="country-name" maxlength="80" required class="form-control"
                                   ${not empty bookingContact ? 'readonly' : ''}
                                   value="<c:out value="${not empty bookingContact ? bookingContact.country : checkoutError ? param['contact-country'] : customer.country}"/>">
                        </div>
                      </div>
                    </fieldset>
                    <fieldset class="border-top pt-4">
                        <legend class="h5 stay-form-heading"><i class="bi bi-credit-card me-2" aria-hidden="true"></i><ui:text key="room.booking.payment.title"/></legend>
                        <div class="mb-3">
                            <label for="payment-plan" class="form-label"><ui:text key="room.checkout.method"/></label>
                            <select id="payment-plan" name="payment-plan" class="form-select" required>
                                <option value="deposit" ${canDeposit ? '' : 'disabled'}><c:out value="${requestScope.i18n['room.checkout.deposit']}"/></option>
                                <option value="transfer" ${!canDeposit || selectedPlan == 'transfer' ? 'selected' : ''}><c:out value="${requestScope.i18n['room.checkout.transfer']}"/></option>
                            </select>
                            <div id="deposit-note" class="form-text" ${canDeposit ? 'hidden' : ''}><ui:text key="room.checkout.deposit.unavailable"/></div>
                        </div>
                        <div class="mb-4">
                            <label for="discount-code" class="form-label"><ui:text key="room.checkout.discount"/></label>
                            <select id="discount-code" name="discount-code" class="form-select">
                                <option value=""><c:out value="${requestScope.i18n['room.checkout.no.voucher']}"/></option>
                                <c:forEach var="voucher" items="${voucherTotals}">
                                    <option value="<c:out value='${voucher.key}'/>" data-total="${voucher.value}"
                                            ${selectedVoucher == voucher.key ? 'selected' : ''}>
                                        <c:out value="${voucher.key}"/> · <ui:money amount="${total - voucher.value}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </fieldset>
                    <button class="btn stay-action w-100" type="submit"><ui:text key="room.booking.payment.confirm"/></button>
                </form>
                </c:if>
                <div class="stay-panel stay-policy p-3 p-md-4 mt-4">
                    <h2 class="h5"><ui:text key="room.policy.title"/></h2>
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
