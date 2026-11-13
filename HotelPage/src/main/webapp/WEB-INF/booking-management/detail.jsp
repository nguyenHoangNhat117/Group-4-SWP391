<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main>
                <div class="container booking-detail-shell py-4 py-lg-5">

                    <%-- UC09: người tạo và điểm vào thu tiền tại quầy --%>
                    <c:if test="${not empty offlineCreator}">
                        <div class="alert alert-info d-flex flex-wrap justify-content-between align-items-center gap-3">
                            <span>
                                <ui:text key="screen.c121af985135cfbc"/>
                                <strong>
                                    <c:out value="${offlineCreator}"/>
                                </strong>
                            </span>

                            <a class="btn btn-primary"
                               href="${pageContext.request.contextPath}/counter-payment?bookingId=${booking.id}">
                                <ui:text key="screen.feb1966292f16b45"/>
                            </a>
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'status-updated'}">
                        <div class="alert alert-success">
                            <ui:text key="screen.f6bc551c4d1033d8"/>
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'invalid-transition'}">
                        <div class="alert alert-danger">
                            <ui:text key="screen.1db22959473c83fa"/>
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'payment-required'}">
                        <div class="alert alert-warning">
                            <ui:text key="screen.2353e54cae2389f7"/>
                        </div>
                    </c:if>
                    <c:forEach var="reason" items="wrong-date,room-not-ready,room-occupied,room-conflict,invalid-booking,failed">
                        <c:if test="${param.message == 'online-check-in-'.concat(reason)}">
                            <div class="alert alert-warning" role="alert"><ui:text key="${'booking.online.checkin.'.concat(reason)}"/></div>
                        </c:if>
                    </c:forEach>

                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                        <div>
                            <h2 class="fw-bold mb-1">
                                <ui:text key="screen.8e9d3e1467f7cc3f"/>${booking.id}
                            </h2>
                            <span class="text-muted">
                                <ui:text key="screen.22d435a3711855db"/>
                                <c:out value="${booking.bookingDate}"/>
                            </span>
                        </div>

                        <a href="${pageContext.request.contextPath}/booking-management"
                           class="btn btn-outline-secondary">
                            <ui:text key="screen.134b66d8c3497148"/>
                        </a>
                    </div>

                    <div class="row g-4 mb-4">
                        <div class="col-md-6">
                            <div class="card shadow-sm h-100">
                                <div class="card-body">
                                    <h5 class="card-title"><ui:text key="screen.ea50259543e17999"/></h5>

                                    <div class="mt-3">
                                        <c:choose>
                                            <c:when test="${booking.status == 'pending'}">
                                                <span class="badge bg-warning text-dark fs-6">
                                                    <ui:text key="screen.331551b0de4157c9"/>
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'confirmed'}">
                                                <span class="badge bg-primary fs-6">
                                                    <ui:text key="screen.fe00b67b6dd1143f"/>
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'checked_in'}">
                                                <span class="badge bg-info text-dark fs-6">
                                                    <ui:text key="screen.17c05c08a9ac3e5a"/>
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'checked_out'}">
                                                <span class="badge bg-secondary fs-6">
                                                    <ui:text key="screen.cbe2b33735c6335c"/>
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'completed'}">
                                                <span class="badge bg-success fs-6">
                                                    <ui:text key="screen.22a970d2e5b1cc23"/>
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'cancelled'}">
                                                <span class="badge bg-danger fs-6">
                                                    <ui:text key="screen.d353a99eb4556847"/>
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="badge bg-dark fs-6">
                                                    <ui:message text="${booking.status}"/>
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="card shadow-sm h-100">
                                <div class="card-body">
                                    <h5 class="card-title"><ui:text key="screen.1f65bccb17385498"/></h5>

                                    <div class="mt-3">
                                        <c:choose>
                                            <c:when test="${paid}">
                                                <span class="badge bg-success fs-6">
                                                    <ui:text key="screen.ba9054bc0354df6e"/>
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="badge bg-warning text-dark fs-6">
                                                    <ui:text key="screen.6a00df629fbad618"/>
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <p class="small text-muted mt-3 mb-0">
                                        <ui:text key="screen.28d7d16670dc0a79"/>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-header">
                            <strong><ui:text key="screen.e9143587ebf6f078"/></strong>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty payments}">
                                    <div class="table-responsive">
                                        <table class="table table-bordered align-middle mb-0">
                                            <thead class="table-light">
                                                <tr>
                                                    <th><ui:text key="screen.3843971dcfdee508"/></th>
                                                    <th><ui:text key="screen.99c40ab405926cb5"/></th>
                                                    <th><ui:text key="screen.49e96d7cdf58069c"/></th>
                                                    <th><ui:text key="screen.52a0f9b65b278850"/></th>
                                                    <th><ui:text key="screen.920e413c7d411b61"/></th>
                                                    <th><ui:text key="screen.eec26ddd9a408449"/></th>
                                                    <th><ui:text key="screen.cb72ec985b5f3d75"/></th>
                                                </tr>
                                            </thead>

                                            <tbody>
                                                <c:forEach var="payment" items="${payments}">
                                                    <tr>
                                                        <td>${payment.id}</td>
                                                        <td>
                                                            <c:out value="${payment.paymentDate}"/>
                                                        </td>

                                                        <td class="text-nowrap">
                                                <fmt:formatNumber
                                                    value="${payment.amount}"
                                                    maxFractionDigits="0"/>
                                                VND
                                                </td>

                                                <td>
                                                    <c:out value="${payment.paymentMethod}"/>
                                                </td>

                                                <td>
                                                    <ui:message text="${payment.paymentStatus}"/>
                                                </td>

                                                <td>
                                                    <c:out value="${payment.transactionCode}"
                                                           default="-"/>
                                                </td>

                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty payment.discount}">
                                                            <c:out value="${payment.discount.code}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            -
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:when>

                                <c:otherwise>
                                    <span class="text-muted">
                                        <ui:text key="screen.cff1cce696d02556"/>
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

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
                            <strong><ui:text key="screen.1c66fcafeab3b38e"/></strong>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty booking.details}">
                                    <div class="table-responsive">
                                        <table class="table table-bordered align-middle">
                                            <thead class="table-light">
                                                <tr>
                                                    <th><ui:text key="screen.911ea89c43d9dbb8"/></th>
                                                    <th><ui:text key="booking.detail.current.room.type"/></th>
                                                    <th><ui:text key="screen.f49fc7767bcfb17b"/></th>
                                                    <th><ui:text key="screen.5b4ecf4c9808dca1"/></th>
                                                    <th><ui:text key="screen.3c8e0fde6fcc71e0"/></th>
                                                    <th><ui:text key="screen.2de469ee4cd7c680"/></th>
                                                    <th><ui:text key="screen.8d40b90ad50b8351"/></th>
                                                </tr>
                                            </thead>

                                            <tbody>
                                                <c:forEach var="detail"
                                                           items="${booking.details}">
                                                    <tr>
                                                        <td>
                                                            <ui:text key="screen.6c8c6af24c9b021c"/>${detail.roomNumber}
                                                        </td>
                                                        <td class="room-type-name"><c:out value="${roomTypeNames[detail.roomNumber]}" default="—"/></td>
                                                        <td>${detail.checkInDate}</td>
                                                        <td>${detail.checkOutDate}</td>
                                                        <td>${detail.guestCount}</td>

                                                        <td class="text-nowrap">
                                                <fmt:formatNumber
                                                    value="${detail.pricePerNight}"
                                                    maxFractionDigits="0"/>
                                                VND
                                                </td>

                                                <td>
                                                    <c:out value="${empty detail.specialRequest
                                                                    ? '-' : detail.specialRequest}"/>
                                                </td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:when>

                                <c:otherwise>
                                    <div class="alert alert-info mb-0">
                                        <ui:text key="screen.7c754327645dc989"/>
                                    </div>
                                </c:otherwise>
                            </c:choose>
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
                                    <fmt:formatNumber
                                        value="${booking.totalPrice}"
                                        maxFractionDigits="0"/>
                                    VND
                                </div>

                                <div class="col-md-6">
                                    <strong><ui:text key="screen.bf6899307a899450"/></strong>
                                    <br>
                                    <c:out value="${booking.bookingDate}"/>
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
                                        <c:out value="${booking.cancellationDate}"/>
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

                    <div class="card shadow-sm">
                        <div class="card-header">
                            <strong><ui:text key="screen.d275eb2a59741cbf"/></strong>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty nextStatuses}">
                                    <p class="small text-muted mb-3"><ui:text key="booking.detail.status.hint"/></p>
                                    <form action="${pageContext.request.contextPath}/booking-management"
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
                                                    class="btn btn-primary w-100"
                                                    onclick="return confirm('Update this booking status?');">
                                                <ui:text key="screen.3030ddce9921f3f9"/>
                                            </button>
                                        </div>
                                    </form>
                                </c:when>

                                <c:otherwise>
                                    <div class="alert alert-secondary mb-0">
                                        <ui:text key="screen.da87095c74827c40"/>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp"%>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
