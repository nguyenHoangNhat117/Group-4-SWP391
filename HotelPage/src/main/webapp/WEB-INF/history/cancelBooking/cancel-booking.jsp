<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:url var="bookingHistoryUrl" value="/history">
    <c:param name="view" value="bookings"/>
</c:url>

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title data-i18n="screen.dcd0884db34dcece">${requestScope.i18n['screen.dcd0884db34dcece']}</title>
    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/general.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-modern.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-shared-shell.css">
    <link rel="stylesheet" href="${ctx}/assets/css/luxe-ui.css">
    <link rel="stylesheet" href="${ctx}/assets/css/management.css">
    <link rel="stylesheet" href="${ctx}/assets/css/customer-bookings.css">
</head>
<body class="customer-bookings-page">
<div class="body-wrapper">
    <%@include file="/WEB-INF/include/header.jsp" %>

    <main class="container py-4 py-lg-5">
        <div class="management-hero customer-bookings-heading">
            <div>
                <div class="eyebrow"><ui:text key="screen.948e03e7054b919e"/> #${booking.id}</div>
                <h1><ui:text key="screen.dcd0884db34dcece"/></h1>
            </div>
            <a class="btn btn-light" href="<c:out value='${bookingHistoryUrl}'/>">
                <ui:text key="history.bookings.back"/>
            </a>
        </div>

        <div class="booking-cancel-grid">
            <section class="booking-cancel-panel" aria-labelledby="booking-cancel-summary">
                <h2 id="booking-cancel-summary"><ui:text key="history.cancel.summary"/></h2>
                <dl class="booking-cancel-facts">
                    <div>
                        <dt><ui:text key="screen.755c8b2a9fb11446"/></dt>
                        <dd><ui:message text="${booking.status}"/></dd>
                    </div>
                    <div>
                        <dt><ui:text key="screen.c9b3c38247f744e1"/></dt>
                        <dd><ui:money amount="${booking.totalPrice}"/></dd>
                    </div>
                </dl>

                <c:if test="${not empty booking.details}">
                    <div class="booking-cancel-rooms">
                        <c:forEach var="detail" items="${booking.details}">
                            <article class="booking-cancel-room">
                                <h3><ui:text key="screen.911ea89c43d9dbb8"/> #${detail.roomNumber}</h3>
                                <dl class="booking-cancel-facts">
                                    <div>
                                        <dt><ui:text key="screen.a44e12f354688caf"/></dt>
                                        <dd><time datetime="${detail.checkInDate}">${fn:substring(detail.checkInDate, 8, 10)}/${fn:substring(detail.checkInDate, 5, 7)}/${fn:substring(detail.checkInDate, 0, 4)}</time></dd>
                                    </div>
                                    <div>
                                        <dt><ui:text key="screen.865998541ea4eba7"/></dt>
                                        <dd><time datetime="${detail.checkOutDate}">${fn:substring(detail.checkOutDate, 8, 10)}/${fn:substring(detail.checkOutDate, 5, 7)}/${fn:substring(detail.checkOutDate, 0, 4)}</time></dd>
                                    </div>
                                    <div>
                                        <dt><ui:text key="screen.1ac580f9fde7374f"/></dt>
                                        <dd>${detail.guestCount}</dd>
                                    </div>
                                </dl>
                            </article>
                        </c:forEach>
                    </div>
                </c:if>
            </section>

            <section class="booking-cancel-panel" aria-labelledby="booking-cancel-confirm">
                <h2 id="booking-cancel-confirm"><ui:text key="screen.79442059c04c095a"/></h2>
                <div class="booking-cancel-refund ${refundAmount > 0 ? 'has-refund' : ''}">
                    <c:choose>
                        <c:when test="${refundAmount > 0}">
                            <span><ui:text key="room.cancel.refund"/></span>
                            <strong><ui:money amount="${refundAmount}"/></strong>
                        </c:when>
                        <c:otherwise>
                            <span><ui:text key="room.cancel.no.refund"/></span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <form action="${ctx}/history/cancel-booking" method="post">
                    <input type="hidden" name="id" value="${booking.id}">
                    <label for="reason" class="form-label fw-semibold">
                        <ui:text key="screen.052aafb8ebd693e9"/>
                        <span class="text-muted fw-normal"><ui:text key="screen.0059798b7f7023e4"/></span>
                    </label>
                    <textarea id="reason" name="reason" class="form-control" rows="4" maxlength="255"
                              aria-describedby="cancel-reason-help"
                              placeholder="${requestScope.i18n['screen.4e158e9f92517c1a']}" data-i18n-placeholder="screen.4e158e9f92517c1a"></textarea>
                    <p id="cancel-reason-help" class="small text-muted mt-2"><ui:text key="history.cancel.reasonLimit"/></p>

                    <div class="booking-cancel-actions">
                        <a class="btn btn-outline-secondary" href="<c:out value='${bookingHistoryUrl}'/>">
                            <ui:text key="history.cancel.keep"/>
                        </a>
                        <button type="submit" class="btn btn-danger"><ui:text key="screen.79442059c04c095a"/></button>
                    </div>
                </form>
            </section>
        </div>
    </main>

    <%@include file="/WEB-INF/include/footer.jsp" %>
</div>
<script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
