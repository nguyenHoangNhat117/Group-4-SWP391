<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title data-i18n="screen.a55a969205588064">${requestScope.i18n['screen.a55a969205588064']}</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <link rel="stylesheet" href="<c:url value='/assets/css/booking-management.css'/>">
    </head>

    <body>
        <div class="body-wrapper booking-detail-page">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main>
                <div class="container booking-detail-shell py-4 py-lg-5">
                    <c:set var="dateFormat" value="${DateTimeFormatter.ofPattern('dd/MM/uuuu')}"/>
                    <c:set var="dateTimeFormat" value="${DateTimeFormatter.ofPattern('dd/MM/uuuu HH:mm')}"/>
                    <c:set var="bookingTone" value="${booking.status == 'completed' ? 'success'
                           : booking.status == 'cancelled' ? 'danger'
                           : booking.status == 'pending' ? 'warning'
                           : booking.status == 'confirmed' or booking.status == 'checked_in' ? 'info' : 'neutral'}"/>
                    <c:set var="bookingStatusKey" value="${booking.status == 'pending' ? 'screen.331551b0de4157c9'
                           : booking.status == 'confirmed' ? 'screen.fe00b67b6dd1143f'
                           : booking.status == 'checked_in' ? 'screen.17c05c08a9ac3e5a'
                           : booking.status == 'checked_out' ? 'screen.cbe2b33735c6335c'
                           : booking.status == 'completed' ? 'screen.22a970d2e5b1cc23'
                           : booking.status == 'cancelled' ? 'screen.d353a99eb4556847' : ''}"/>

                    <div class="booking-detail-heading">
                        <div>
                            <h1><ui:text key="screen.8e9d3e1467f7cc3f"/>${booking.id}</h1>
                            <p>
                                <ui:text key="screen.22d435a3711855db"/>
                                <c:choose>
                                    <c:when test="${not empty booking.bookingDate}">
                                        <time datetime="${booking.bookingDate}">${booking.bookingDate.format(dateTimeFormat)}</time>
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                        <a href="${pageContext.request.contextPath}/booking-management" class="btn btn-outline-secondary">
                            <span aria-hidden="true">&larr;</span>
                            <ui:text key="screen.134b66d8c3497148"/>
                        </a>
                    </div>

                    <%-- UC09: người tạo và điểm vào thu tiền tại quầy --%>
                    <c:if test="${not empty offlineCreator}">
                        <div class="alert alert-info d-flex flex-wrap justify-content-between align-items-center gap-3">
                            <span>
                                <ui:text key="screen.c121af985135cfbc"/>
                                <strong>
                                    <c:out value="${offlineCreator}"/>
                                </strong>
                            </span>

                            <c:if test="${sessionScope.loggedUser.role eq 'staff'}">
                            <a class="btn btn-primary"
                               href="${pageContext.request.contextPath}/counter-payment?bookingId=${booking.id}">
                                <ui:text key="screen.feb1966292f16b45"/>
                            </a>
                        </c:if>
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'status-updated'}">
                        <div class="booking-success" role="status">
                            <span class="booking-success-icon" aria-hidden="true">✓</span>
                            <strong><ui:text key="screen.f6bc551c4d1033d8"/></strong>
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'invalid-transition'}">
                        <div class="alert alert-danger" role="alert">
                            <ui:text key="screen.1db22959473c83fa"/>
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'payment-required'}">
                        <div class="alert alert-warning" role="alert">
                            <ui:text key="screen.2353e54cae2389f7"/>
                        </div>
                    </c:if>
                    <c:forEach var="reason" items="wrong-date,room-not-ready,room-occupied,room-conflict,invalid-booking,failed">
                        <c:if test="${param.message == 'online-check-in-'.concat(reason)}">
                            <div class="alert alert-warning" role="alert"><ui:text key="${'booking.online.checkin.'.concat(reason)}"/></div>
                        </c:if>
                    </c:forEach>

                    <div class="booking-status-grid mb-4">
                        <section class="booking-status-panel">
                            <h2><ui:text key="screen.ea50259543e17999"/></h2>
                            <span class="booking-pill booking-pill--${bookingTone}" id="current-booking-status">
                                <c:choose>
                                    <c:when test="${not empty bookingStatusKey}"><ui:text key="${bookingStatusKey}"/></c:when>
                                    <c:otherwise><ui:message text="${booking.status}"/></c:otherwise>
                                </c:choose>
                            </span>
                        </section>
                        <section class="booking-status-panel">
                            <h2><ui:text key="screen.1f65bccb17385498"/></h2>
                            <span class="booking-pill booking-pill--${paid ? 'success' : 'warning'}">
                                <ui:text key="${paid ? 'screen.ba9054bc0354df6e' : 'screen.6a00df629fbad618'}"/>
                            </span>
                        </section>
                    </div>

                    <div class="card mb-4">
                        <div class="card-header">
                            <strong><ui:text key="screen.d275eb2a59741cbf"/></strong>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty nextStatuses}">
                                    <p class="small text-muted mb-3"><ui:text key="booking.detail.status.hint"/></p>
                                    <form id="booking-status-form" action="${pageContext.request.contextPath}/booking-management"
                                          method="post"
                                          class="row g-3 align-items-end">

                                        <input type="hidden"
                                               name="csrfToken"
                                               value="${sessionScope.csrfToken}">

                                        <input type="hidden"
                                               name="action"
                                               value="update-status">

                                        <input type="hidden"
                                               name="booking-id"
                                               value="${booking.id}">

                                        <div class="col-md-8">
                                            <label for="status"
                                                   class="form-label fw-semibold">
                                                <ui:text key="screen.6355462edd94e4b2"/>
                                            </label>

                                            <select id="status"
                                                    name="status"
                                                    class="form-select"
                                                    required>

                                                <option value="" selected disabled data-i18n="screen.b575d3e1b784356d">
                                                    ${requestScope.i18n['screen.b575d3e1b784356d']}
                                                </option>

                                                <c:forEach var="status"
                                                           items="${nextStatuses}">
                                                    <option value="${status}" data-i18n="${status == 'checked_in' ? 'screen.17c05c08a9ac3e5a'
                                                            : status == 'checked_out' ? 'screen.cbe2b33735c6335c'
                                                            : status == 'cancelled' ? 'screen.d353a99eb4556847'
                                                            : status == 'completed' ? 'screen.22a970d2e5b1cc23' : 'screen.fe00b67b6dd1143f'}">
                                                        <c:choose>
                                                            <c:when test="${status == 'confirmed'}">
                                                                ${requestScope.i18n['screen.fe00b67b6dd1143f']}
                                                            </c:when>
                                                            <c:when test="${status == 'cancelled'}">
                                                                ${requestScope.i18n['screen.d353a99eb4556847']}
                                                            </c:when>
                                                            <c:when test="${status == 'checked_in'}">
                                                                ${requestScope.i18n['screen.17c05c08a9ac3e5a']}
                                                            </c:when>
                                                            <c:when test="${status == 'checked_out'}">
                                                                ${requestScope.i18n['screen.cbe2b33735c6335c']}
                                                            </c:when>
                                                            <c:when test="${status == 'completed'}">
                                                                ${requestScope.i18n['screen.22a970d2e5b1cc23']}
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${status}"/>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>

                                        <div class="col-md-4">
                                            <button type="submit"
                                                    class="btn btn-primary w-100">
                                                <ui:text key="screen.3030ddce9921f3f9"/>
                                            </button>
                                        </div>
                                    </form>

                                    <dialog class="booking-status-dialog" id="booking-status-dialog"
                                            aria-labelledby="booking-dialog-title" aria-describedby="booking-dialog-description">
                                        <div class="booking-dialog-content">
                                            <h2 id="booking-dialog-title"><ui:text key="booking.detail.confirm.title"/></h2>
                                            <p id="booking-dialog-description"><ui:text key="booking.detail.confirm.description"/></p>
                                            <div class="booking-dialog-transition">
                                                <span id="booking-dialog-current"></span>
                                                <span aria-hidden="true">&rarr;</span>
                                                <strong id="booking-dialog-next"></strong>
                                            </div>
                                            <div class="booking-dialog-actions">
                                                <button type="button" class="btn btn-outline-secondary" id="booking-dialog-back" autofocus>
                                                    <ui:text key="booking.detail.confirm.back"/>
                                                </button>
                                                <button type="button" class="btn btn-primary" id="booking-dialog-submit">
                                                    <ui:text key="booking.detail.confirm.submit"/>
                                                </button>
                                            </div>
                                        </div>
                                    </dialog>
                                </c:when>

                                <c:otherwise>
                                    <div class="alert alert-secondary mb-0">
                                        <ui:text key="screen.da87095c74827c40"/>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <section class="card booking-data-card mb-4" aria-labelledby="stay-heading">
                        <h2 class="card-header" id="stay-heading"><ui:text key="screen.1c66fcafeab3b38e"/></h2>
                        <c:choose>
                            <c:when test="${not empty booking.details}">
                                <div class="table-responsive booking-table-scroll" role="region" aria-labelledby="stay-heading" tabindex="0">
                                    <table class="table booking-detail-table">
                                        <thead>
                                            <tr>
                                                <th scope="col">
                                                    <ui:text key="screen.911ea89c43d9dbb8"/> /
                                                    <ui:text key="screen.82dea9a01c0033ec"/>
                                                </th>
                                                <th scope="col"><ui:text key="screen.f49fc7767bcfb17b"/></th>
                                                <th scope="col"><ui:text key="screen.5b4ecf4c9808dca1"/></th>
                                                <th scope="col" class="text-center"><ui:text key="screen.3c8e0fde6fcc71e0"/></th>
                                                <th scope="col" class="text-end"><ui:text key="screen.2de469ee4cd7c680"/></th>
                                                <th scope="col"><ui:text key="screen.8d40b90ad50b8351"/></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="detail" items="${booking.details}">
                                                <tr>
                                                    <th scope="row" class="booking-room">
                                                        <span class="booking-table-id">
                                                            <ui:text key="screen.6c8c6af24c9b021c"/>${detail.roomNumber}
                                                        </span>
                                                        <c:if test="${not empty roomTypeNames[detail.roomNumber]}">
                                                            <span class="booking-room-type"><c:out value="${roomTypeNames[detail.roomNumber]}"/></span>
                                                        </c:if>
                                                    </th>
                                                    <td class="booking-stay-date">
                                                        <time datetime="${detail.checkInDate}">${detail.checkInDate.format(dateFormat)}</time>
                                                    </td>
                                                    <td class="booking-stay-date">
                                                        <time datetime="${detail.checkOutDate}">${detail.checkOutDate.format(dateFormat)}</time>
                                                    </td>
                                                    <td class="text-center">${detail.guestCount}</td>
                                                    <td class="text-end">
                                                        <span class="booking-money">
                                                            <fmt:formatNumber value="${detail.pricePerNight}" maxFractionDigits="0"/>
                                                            <small>VND</small>
                                                        </span>
                                                    </td>
                                                    <td class="booking-table-note">
                                                        <c:out value="${empty detail.specialRequest ? '—' : detail.specialRequest}"/>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="booking-table-empty"><ui:text key="screen.7c754327645dc989"/></p>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <section class="card booking-data-card mb-4" aria-labelledby="payment-heading">
                        <h2 class="card-header" id="payment-heading"><ui:text key="screen.e9143587ebf6f078"/></h2>
                        <c:choose>
                            <c:when test="${not empty payments}">
                                <div class="table-responsive booking-table-scroll" role="region" aria-labelledby="payment-heading" tabindex="0">
                                    <table class="table booking-detail-table">
                                        <thead>
                                            <tr>
                                                <th scope="col"><ui:text key="screen.3843971dcfdee508"/></th>
                                                <th scope="col"><ui:text key="screen.99c40ab405926cb5"/></th>
                                                <th scope="col" class="text-end"><ui:text key="screen.49e96d7cdf58069c"/></th>
                                                <th scope="col"><ui:text key="screen.52a0f9b65b278850"/></th>
                                                <th scope="col"><ui:text key="screen.920e413c7d411b61"/></th>
                                                <th scope="col"><ui:text key="screen.eec26ddd9a408449"/></th>
                                                <th scope="col"><ui:text key="screen.cb72ec985b5f3d75"/></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="payment" items="${payments}">
                                                <c:set var="paymentTone" value="${payment.paymentStatus == 'paid' ? 'success'
                                                       : payment.paymentStatus == 'pending' ? 'warning'
                                                       : payment.paymentStatus == 'failed' ? 'danger' : 'neutral'}"/>
                                                <tr>
                                                    <th scope="row" class="booking-table-id">#${payment.id}</th>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty payment.paymentDate}">
                                                                <time datetime="${payment.paymentDate}">${payment.paymentDate.format(dateTimeFormat)}</time>
                                                            </c:when>
                                                            <c:otherwise>—</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-end">
                                                        <span class="booking-money">
                                                            <fmt:formatNumber value="${payment.amount}" maxFractionDigits="0"/>
                                                            <small>VND</small>
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${payment.paymentMethod == 'bank_transfer'}">
                                                                <ui:text key="screen.d69d587c81659616"/>
                                                            </c:when>
                                                            <c:otherwise><ui:message text="${payment.paymentMethod}"/></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <span class="booking-pill booking-pill--${paymentTone}">
                                                            <ui:message text="${payment.paymentStatus}"/>
                                                        </span>
                                                    </td>
                                                    <td class="booking-table-reference">
                                                        <c:out value="${empty payment.transactionCode ? '—' : payment.transactionCode}"/>
                                                    </td>
                                                    <td><c:out value="${empty payment.discount.code ? '—' : payment.discount.code}"/></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="booking-table-empty"><ui:text key="screen.cff1cce696d02556"/></p>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <div class="card shadow-sm mb-4">
                        <div class="card-header">
                            <strong><ui:text key="screen.8258db6d14e588f6"/></strong>
                        </div>

                        <div class="card-body">
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <strong><ui:text key="screen.b01e76be2d45ae6f"/></strong>
                                    <br>
                                    ${booking.customer.customerID}
                                </div>

                                <div class="col-md-4">
                                    <strong><ui:text key="screen.2683cad48a14dc3f"/></strong>
                                    <br>

                                    <c:choose>
                                        <c:when test="${not empty booking.contact}">
                                            <c:out value="${booking.contact.name}"/>
                                        </c:when>
                                        <c:when test="${not empty booking.customer.firstName
                                                        or not empty booking.customer.lastName}">
                                            <c:out value="${booking.customer.lastName}"/>
                                            <c:out value="${booking.customer.firstName}"/>
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="col-md-4">
                                    <strong><ui:text key="screen.3806d61c34063f85"/></strong>
                                    <br>
                                    <c:out value="${booking.customer.user.username}"/>
                                </div>

                                <div class="col-md-4">
                                    <strong><ui:text key="screen.17507dcf845793fa"/></strong>
                                    <br>
                                    <c:out value="${empty booking.contact ? booking.customer.email : booking.contact.email}"/>
                                </div>

                                <div class="col-md-4">
                                    <strong><ui:text key="screen.3305eb58093dce17"/></strong>
                                    <br>
                                    <c:out value="${empty booking.contact ? booking.customer.phone : booking.contact.phone}" default="-"/>
                                </div>

                                <div class="col-md-4">
                                    <strong><ui:text key="screen.fa0882e21637cbe7"/></strong>
                                    <br>
                                    <c:choose>
                                        <c:when test="${not empty booking.contact}"><c:out value="${booking.contact.country}"/></c:when>
                                        <c:otherwise>
                                            <c:out value="${booking.customer.street}"/>
                                            <c:out value="${booking.customer.city}"/>
                                            <c:out value="${booking.customer.country}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-header">
                            <strong><ui:text key="screen.e3c166be8ed36aba"/></strong>
                        </div>

                        <div class="card-body">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <strong><ui:text key="screen.d104a659abae9ebc"/></strong>
                                    <br>
                                    <span class="booking-money booking-total">
                                        <fmt:formatNumber value="${booking.totalPrice}" maxFractionDigits="0"/>
                                        <small>VND</small>
                                    </span>
                                </div>

                                <div class="col-md-6">
                                    <strong><ui:text key="screen.bf6899307a899450"/></strong>
                                    <br>
                                    <c:choose>
                                        <c:when test="${not empty booking.bookingDate}">
                                            <time datetime="${booking.bookingDate}">${booking.bookingDate.format(dateTimeFormat)}</time>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="col-12">
                                    <strong><ui:text key="screen.906fb211c5e1ee2e"/></strong>
                                    <br>
                                    <c:out value="${empty booking.specialRequest
                                                    ? '-' : booking.specialRequest}"/>
                                </div>

                                <c:if test="${booking.status == 'cancelled'}">
                                    <div class="col-md-6">
                                        <strong><ui:text key="screen.873135e90ec6e045"/></strong>
                                        <br>
                                        <c:choose>
                                            <c:when test="${not empty booking.cancellationDate}">
                                                <time datetime="${booking.cancellationDate}">${booking.cancellationDate.format(dateTimeFormat)}</time>
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="col-md-6">
                                        <strong><ui:text key="screen.821b8e24bc1baa29"/></strong>
                                        <br>
                                        <c:out value="${empty booking.cancellationReason
                                                        ? '-' : booking.cancellationReason}"/>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>

                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp"%>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <c:if test="${not empty nextStatuses}">
            <script src="<c:url value='/assets/js/booking-detail.js'/>"></script>
        </c:if>
    </body>
</html>
