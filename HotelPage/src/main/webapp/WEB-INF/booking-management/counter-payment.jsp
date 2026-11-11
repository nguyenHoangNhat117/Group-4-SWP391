<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}" class="uc09-page"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title data-i18n="offline.title">${requestScope.i18n['offline.title']}</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/general.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/offline-booking.css?v=stable-1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/offline-counter.css?v=stable-1">
</head><body class="offline-booking-page"><div class="body-wrapper">
<%@include file="/WEB-INF/include/header.jsp"%>
<main class="ob-page"><div class="ob-heading"><div><p class="oc-eyebrow">LUXE ESCAPE · FRONT DESK</p>
<h1><ui:text key="offline.title"/> #${summary.bookingId}</h1></div>
<a class="ob-back" href="${pageContext.request.contextPath}/booking-management"><ui:text key="offline.back"/></a></div>
<c:if test="${not empty operationMessage}"><div class="oc-message" role="status"><ui:message text="${operationMessage}"/></div></c:if>
<c:if test="${param.created eq '1'}"><div class="oc-message"><ui:text key="offline.created"/></div></c:if>
<div class="oc-overview ob-panel"><div><small><ui:text key="offline.customer"/></small><strong><a class="ob-back" href="${pageContext.request.contextPath}/customers?view=detail&amp;id=${operations.customerId}"><c:out value="${summary.customerName}"/></a></strong></div>
<div><small><ui:text key="offline.status"/></small><strong><ui:text key="offline.state.${summary.status}"/></strong></div>
<div><small><ui:text key="offline.creator"/></small><strong><c:out value="${summary.creator}"/></strong></div>
<c:if test="${summary.status eq 'pending'}"><div><small><ui:text key="offline.deadline"/></small><strong><c:out value="${summary.holdExpiresAt}"/></strong></div></c:if></div>
<div class="oc-money"><div><small><ui:text key="offline.total"/></small><strong><fmt:formatNumber value="${summary.payable}"/> VND</strong></div>
<div><small><ui:text key="offline.paid"/></small><strong><fmt:formatNumber value="${summary.paid}"/> VND</strong></div>
<div><small><ui:text key="offline.remaining"/></small><strong><fmt:formatNumber value="${summary.remaining}"/> VND</strong></div></div>
<c:if test="${not empty summary.promotionError}"><div class="oc-message"><c:out value="${summary.promotionError}"/></div></c:if>
<section class="ob-panel oc-room-list"><h2><ui:text key="offline.rooms"/></h2>
<c:forEach items="${operations.rooms}" var="room"><p><strong>#${room.RoomNumber}</strong> · ${room.CheckInDate} → ${room.CheckOutDate} · ${room.GuestCount} <ui:text key="offline.guests"/>
<c:if test="${room.NeedsCleaning}"><span class="oc-badge"><ui:text key="offline.dirty"/></span></c:if></p></c:forEach></section>
<div class="oc-grid">
<c:if test="${summary.canPay}"><section class="ob-panel"><h2><ui:text key="offline.payment"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="pay">
<label><ui:text key="offline.method"/><select class="ob-input" name="method"><option value="cash"><ui:text key="offline.cash"/></option><option value="bank_transfer"><ui:text key="offline.bank"/></option></select></label>
<label><ui:text key="offline.reference"/><input class="ob-input" name="reference" maxlength="100" autocomplete="off"></label><p class="ob-note"><ui:text key="offline.bankNote"/></p><label><ui:text key="offline.plan"/><select class="ob-input" name="percent">
<c:if test="${summary.firstPayment and summary.depositPercent lt 100}"><option value="${summary.depositPercent}" data-amount="${summary.depositAmount}"><ui:text key="offline.deposit"/> ${summary.depositPercent}%</option></c:if>
<option value="100" selected data-amount="${summary.remaining}"><ui:text key="offline.balance"/></option></select></label><p><ui:text key="offline.dueNow"/>: <strong data-counter-due><fmt:formatNumber value="${summary.remaining}"/> VND</strong></p><p class="ob-note"><ui:text key="offline.paymentNote"/></p><button type="submit" class="ob-button ob-primary"><ui:text key="offline.pay"/></button></form></section></c:if>
<c:if test="${summary.canChange}"><section class="ob-panel"><h2><ui:text key="offline.promotion"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="promotion">
<input class="ob-input" name="promotionCode" maxlength="50" value="${fn:escapeXml(summary.promotionCode)}"><button type="submit" class="ob-button ob-primary"><ui:text key="offline.promotion"/></button></form></section></c:if>
<c:if test="${summary.status eq 'confirmed'}"><section class="ob-panel"><h2><ui:text key="offline.check_in"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="check_in">
<label><ui:text key="offline.actualGuests"/><input class="ob-input" type="number" name="actualGuests" min="1" max="1000" value="${operations.rooms[0].GuestCount}" required></label>
<label class="oc-check"><input type="checkbox" name="identityVerified" value="yes" required> <ui:text key="offline.identity"/></label><p class="ob-note"><ui:text key="offline.checkInNote"/></p><button type="submit" class="ob-button ob-primary"><ui:text key="offline.check_in"/></button></form></section></c:if>
<c:if test="${summary.status eq 'confirmed' or summary.status eq 'checked_in'}"><section class="ob-panel"><h2><ui:text key="offline.charge"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="charge">
<label><ui:text key="offline.description"/><input class="ob-input" name="description" maxlength="500" required></label>
<label><ui:text key="offline.amount"/><input class="ob-input" name="amount" type="number" min="1" max="1000000000" step="1" required></label><button type="submit" class="ob-button ob-primary"><ui:text key="offline.charge"/></button></form></section></c:if>
<c:if test="${summary.status eq 'pending' or summary.status eq 'confirmed'}"><section class="ob-panel"><h2><ui:text key="offline.cancel"/></h2><p class="ob-note"><ui:text key="offline.cancelPolicy"/> ${summary.depositPercent}%.</p><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="cancel">
<label><ui:text key="offline.reason"/><textarea class="ob-input" name="reason" maxlength="255" required></textarea></label>
<label class="oc-check"><input type="checkbox" required> <ui:text key="offline.cancelConfirm"/></label><button type="submit" class="ob-button ob-secondary"><ui:text key="offline.cancel"/></button></form></section></c:if>
<c:if test="${not empty operations.refunds}"><section class="ob-panel"><h2><ui:text key="offline.refund"/></h2><c:forEach items="${operations.refunds}" var="refund"><p><ui:text key="offline.refundAmount"/>: <strong><fmt:formatNumber value="${refund.Amount}"/> VND</strong> · <c:out value="${refund.Status}"/></p>
<c:if test="${refund.Status eq 'pending'}"><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="refund">
<label><ui:text key="offline.method"/><select class="ob-input" name="method"><option value="cash"><ui:text key="offline.cash"/></option><option value="bank_transfer"><ui:text key="offline.bank"/></option></select></label>
<label><ui:text key="offline.reference"/><input class="ob-input" name="reference" maxlength="100" autocomplete="off"></label><p class="ob-note"><ui:text key="offline.bankNote"/></p><label class="oc-check"><input type="checkbox" required> <ui:text key="offline.refundConfirm"/></label><button type="submit" class="ob-button ob-primary"><ui:text key="offline.refund"/></button></form></c:if></c:forEach></section></c:if>
<c:if test="${summary.status eq 'checked_in'}"><section class="ob-panel"><h2><ui:text key="offline.check_out"/></h2><p class="ob-note"><ui:text key="offline.checkoutNote"/></p><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="check_out">
<button type="submit" class="ob-button ob-primary"><ui:text key="offline.check_out"/></button></form></section></c:if>
<c:if test="${summary.status eq 'checked_out'}"><section class="ob-panel"><h2><ui:text key="offline.closing"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="complete">
<button type="submit" class="ob-button ob-primary"><ui:text key="offline.complete"/></button></form><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="clean">
<button type="submit" class="ob-button ob-secondary"><ui:text key="offline.clean"/></button></form></section></c:if>
<c:if test="${summary.status eq 'completed'}"><section class="ob-panel"><h2><ui:text key="offline.clean"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
<input type="hidden" name="paymentKey" value="${paymentKey}">
<input type="hidden" name="action" value="clean">
<button type="submit" class="ob-button ob-primary"><ui:text key="offline.clean"/></button></form></section></c:if>
</div><section class="ob-panel oc-history"><h2><ui:text key="offline.history"/></h2>
<div class="oc-table"><table><thead><tr><th><ui:text key="offline.action"/></th><th><ui:text key="offline.description"/></th><th><ui:text key="offline.amount"/></th><th><ui:text key="offline.actor"/></th><th>UTC</th></tr></thead><tbody>
<c:forEach items="${operations.events}" var="event"><tr><td><ui:text key="offline.action.${event.Action}"/></td><td><c:out value="${event.Note}"/></td><td><fmt:formatNumber value="${event.Amount}"/></td><td><c:out value="${event.Actor}"/></td><td><c:out value="${event.CreatedAt}"/></td></tr></c:forEach>
</tbody></table></div></section>
<section class="ob-panel oc-history"><h2><ui:text key="offline.receipts"/></h2><div class="oc-table"><table><thead><tr><th>ID</th><th><ui:text key="offline.amount"/></th><th><ui:text key="offline.method"/></th><th><ui:text key="offline.reference"/></th><th><ui:text key="offline.status"/></th><th><ui:text key="offline.refunded"/></th></tr></thead><tbody>
<c:forEach items="${operations.payments}" var="payment"><tr><td>${payment.PaymentID}</td><td><fmt:formatNumber value="${payment.Amount}"/></td><td><c:out value="${payment.PaymentMethod}"/></td><td><c:out value="${payment.TransactionCode}"/><br><c:out value="${payment.CounterReference}"/></td><td><c:out value="${payment.PaymentStatus}"/></td><td><fmt:formatNumber value="${payment.RefundedAmount}"/></td></tr></c:forEach>
</tbody></table></div></section>
<a class="ob-button ob-secondary" href="${pageContext.request.contextPath}/offline-booking"><ui:text key="offline.newBooking"/></a>
</main><%@include file="/WEB-INF/include/footer.jsp"%></div>
<script defer src="${pageContext.request.contextPath}/assets/js/offline-counter.js?v=1"></script>
</body></html>