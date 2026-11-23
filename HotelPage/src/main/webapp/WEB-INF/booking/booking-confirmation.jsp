<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html class="stay-page" lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${requestScope.i18n['room.booking.checkout.title']}"/></title>
    <link rel="stylesheet" href="<c:url value='/assets/css/bootstrap.min.css'/>">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="<c:url value='/assets/css/general.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/luxe-shared-shell.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/room-checkout.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/online-booking-select.css?v=4'/>">
</head>
<body class="body-wrapper">
    <%@ include file="../include/header.jsp" %>
    <main class="stay-content">
        <div class="container stay-shell">
            <div class="stay-heading"><h1><ui:text key="room.booking.checkout.title"/></h1></div>
            <c:if test="${not empty selectionConflict}">
                <div class="alert alert-warning" role="alert">
                    <c:choose>
                        <c:when test="${selectionConflict.reason == 'stock'}">
                            <c:out value="${selectionConflict.roomName}"/> <ui:text key="room.booking.confirm.stock"/>
                            <strong>${selectionConflict.available}</strong> <ui:text key="room.selection.roomUnit"/>.
                        </c:when>
                        <c:when test="${selectionConflict.reason == 'price'}"><ui:text key="room.booking.confirm.priceChanged"/></c:when>
                        <c:when test="${selectionConflict.reason == 'capacity'}"><ui:text key="room.selection.shortGuests"/></c:when>
                        <c:when test="${selectionConflict.reason == 'children'}"><ui:text key="room.selection.shortChildren"/></c:when>
                        <c:otherwise><ui:text key="room.selection.invalid"/></c:otherwise>
                    </c:choose>
                    <a class="alert-link ms-2" href="<c:url value='/room'/>"><ui:text key="room.booking.confirm.back"/></a>
                </div>
            </c:if>
            <c:if test="${confirmationUnavailable}">
                <div class="alert alert-warning" role="alert"><ui:text key="room.confirm.unavailable"/></div>
            </c:if>
            <form class="row g-4" id="booking-confirmation-form" method="post" action="<c:url value='/booking-confirmation'/>"
                  data-total="${confirmationDraft.total}" data-conflict="${not empty selectionConflict}">
                <input type="hidden" name="draft" value="${confirmationDraft.token}">
                <input type="hidden" name="csrfToken" value="${confirmationCsrfToken}">
                <input type="hidden" name="action" value="confirm">
                <section class="col-lg-7">
                    <fieldset class="stay-panel p-4 mb-3">
                        <legend class="h5 stay-form-heading"><ui:text key="room.booking.contact.representative"/></legend>
                        <c:if test="${contactError}">
                            <p class="alert alert-warning" role="alert"><ui:text key="room.booking.contact.error"/></p>
                        </c:if>
                        <c:set var="showConfirmationRequiredMarks" value="${true}"/>
                        <%@ include file="booking-contact-fields.jspf" %>
                    </fieldset>
                    <section class="stay-panel stay-overview p-3 p-sm-4" aria-labelledby="stay-overview-title">
                        <c:set var="stayDateFormat" value="${DateTimeFormatter.ofPattern('dd/MM/uuuu')}"/>
                        <div class="stay-overview-heading">
                            <h2 class="h5 fw-bold mb-0" id="stay-overview-title"><ui:text key="room.booking.confirm.stay"/></h2>
                            <span class="stay-overview-nights"><i class="bi bi-moon-stars" aria-hidden="true"></i>
                                ${confirmationDraft.nights} <ui:text key="room.search.party.nights"/></span>
                        </div>
                        <dl class="stay-overview-facts">
                            <div>
                                <dt><ui:text key="history.bookings.detail.checkIn"/></dt>
                                <dd><time datetime="${confirmationDraft.checkIn}">${confirmationDraft.checkIn.format(stayDateFormat)}</time></dd>
                            </div>
                            <div>
                                <dt><ui:text key="history.bookings.detail.checkOut"/></dt>
                                <dd><time datetime="${confirmationDraft.checkOut}">${confirmationDraft.checkOut.format(stayDateFormat)}</time></dd>
                            </div>
                            <div class="stay-overview-party">
                                <dt><ui:text key="room.selection.partyTotal"/></dt>
                                <dd>${confirmationDraft.adults} <ui:text key="room.selection.adultUnit"/> &middot;
                                    ${confirmationDraft.children} <ui:text key="room.selection.childUnit"/></dd>
                            </div>
                        </dl>
                        <ul class="stay-overview-rooms list-unstyled mb-0" role="list">
                            <c:forEach var="line" items="${confirmationDraft.rooms}">
                                <li class="stay-overview-room">
                                    <div>
                                        <h3 class="h6 fw-bold mb-2">${line.quantity} &times; <c:out value="${line.name}"/></h3>
                                        <ul class="stay-overview-room-parties list-unstyled mb-0" role="list">
                                            <c:forEach var="party" items="${line.parties}" varStatus="roomIndex">
                                                <li class="stay-overview-room-party stay-overview-room-meta">
                                                    <strong><ui:text key="room.confirm.roomOrdinal"/> ${roomIndex.count}</strong>
                                                    <span>${party.adults} <ui:text key="room.selection.adultUnit"/> &middot;
                                                        ${party.children} <ui:text key="room.selection.childUnit"/></span>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </div>
                                    <div class="stay-overview-price">
                                        <strong><span class="visually-hidden"><ui:text key="room.confirm.lineTotal"/> </span>
                                            <ui:money amount="${line.nightly * line.quantity * confirmationDraft.nights}"/></strong>
                                        <span class="stay-overview-room-meta"><ui:money amount="${line.nightly}"/> / <ui:text key="room.search.party.nights"/></span>
                                    </div>
                                </li>
                            </c:forEach>
                        </ul>
                    </section>
                </section>
                <div class="col-lg-5">
                    <div class="stay-panel p-4">
                        <h2 class="h5 stay-form-heading"><ui:text key="room.booking.payment.title"/></h2>
                        <label class="form-label booking-select-label" for="payment-plan"><ui:text key="room.checkout.method"/></label>
                        <select id="payment-plan" name="payment-plan" class="form-select booking-select mb-3"
                                aria-invalid="${paymentChoiceError eq true}" required>
                            <option value="deposit" ${canDeposit ? '' : 'disabled'} ${selectedPlan == 'deposit' ? 'selected' : ''}><ui:text key="room.checkout.deposit"/></option>
                            <option value="transfer" ${selectedPlan == 'transfer' ? 'selected' : ''}><ui:text key="room.checkout.transfer"/></option>
                        </select>
                        <c:if test="${paymentChoiceError}"><p class="text-danger small" role="alert"><ui:text key="room.checkout.choice.error"/></p></c:if>
                        <label class="form-label booking-select-label" for="discount-code"><ui:text key="room.checkout.discount"/></label>
                        <select id="discount-code" name="discount-code" class="form-select booking-select mb-3"
                                aria-invalid="${voucherError eq true}">
                            <option value=""><c:out value="${requestScope.i18n['room.checkout.no.voucher']}"/></option>
                            <c:forEach var="voucher" items="${voucherTotals}">
                                <option value="<c:out value='${voucher.key}'/>" data-total="${voucher.value}"
                                        ${selectedVoucher == voucher.key ? 'selected' : ''}>
                                    <c:out value="${voucher.key}"/> &middot; <c:out value="${requestScope.i18n['room.confirm.voucher.subtract']}"/>
                                    <ui:money amount="${confirmationDraft.total - voucher.value}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${empty voucherTotals}"><p class="small text-muted"><ui:text key="room.confirm.voucher.empty"/></p></c:if>
                        <c:if test="${voucherError}"><p class="text-danger small" role="alert"><ui:text key="room.confirm.voucher.changed"/></p></c:if>
                        <div class="stay-summary mb-3"><dl class="mb-0">
                            <div><dt><ui:text key="room.selection.total"/></dt>
                                <dd><ui:money amount="${confirmationDraft.total}"/></dd></div>
                            <div id="confirmation-discount-row" class="stay-voucher-discount" ${discountAmount == 0 ? 'hidden' : ''}>
                                <dt><ui:text key="room.confirm.voucher.amount"/>
                                    <span id="confirmation-voucher-code" class="stay-voucher-code"><c:out value="${selectedVoucher}"/></span>
                                </dt>
                                <dd id="confirmation-discount">−<ui:money amount="${discountAmount}"/></dd>
                            </div>
                            <div><dt><ui:text key="room.confirm.orderTotal"/></dt>
                                <dd id="confirmation-total" aria-live="polite"><ui:money amount="${payableTotal}"/></dd></div>
                            <div class="checkout-due-row"><dt><ui:text key="room.checkout.due"/></dt>
                                <dd id="confirmation-due" aria-live="polite"><ui:money amount="${paymentDue}"/></dd></div>
                        </dl></div>
                        <details class="mb-3">
                            <summary class="fw-semibold"><ui:text key="room.booking.consent.terms"/></summary>
                            <ul class="small mt-2 mb-0"><li><ui:text key="room.policy.deposit"/></li>
                                <li><ui:text key="room.policy.free"/></li><li><ui:text key="room.policy.late"/></li></ul>
                        </details>
                        <%@ include file="booking-consent.jspf" %>
                        <button class="btn stay-action w-100" type="submit" ${not empty selectionConflict ? 'disabled' : ''}>
                            <ui:text key="room.confirm.pay"/>
                        </button>
                        <button class="btn btn-link stay-back p-0 mt-3" type="submit" name="back-to-rooms" value="yes" formnovalidate>
                            <i class="bi bi-arrow-left me-1" aria-hidden="true"></i><ui:text key="room.booking.confirm.back"/>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </main>
    <%@ include file="../include/footer.jsp" %>
    <script src="<c:url value='/assets/js/bootstrap.bundle.min.js'/>"></script>
    <script type="module" src="<c:url value='/assets/js/booking-confirmation.js?v=4'/>"></script>
</body>
</html>
