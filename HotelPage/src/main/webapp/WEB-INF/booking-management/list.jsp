<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title data-i18n="screen.60bbdeefb7640eb4">${requestScope.i18n['screen.60bbdeefb7640eb4']}</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <c:url var="bookingListStyles" value="/assets/css/booking-management.css">
            <c:param name="v" value="staff-calendar-2"/>
        </c:url>
        <link rel="stylesheet" href="${bookingListStyles}">
    </head>

    <body class="luxe-admin-suite luxe-admin-bookings">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main>
                <div class="luxe-admin-banner" aria-label="${requestScope.i18n['screen.687cac03f6dbbfad']}" data-i18n-aria-label="screen.687cac03f6dbbfad">
                    <span class="luxe-admin-banner-eyebrow">
                        LUXE ESCAPE
                        <span aria-hidden="true">/</span>
                        <ui:text key="screen.fdf811baf35bc1f1"/>
                    </span>

                    <span class="luxe-admin-banner-title">
                        <ui:text key="screen.687cac03f6dbbfad"/>
                    </span>

                    <span class="luxe-admin-banner-art" aria-hidden="true"></span>
                </div>

                <div class="container booking-list-shell py-4 py-lg-5">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                        <div>
                            <h1 class="h2 fw-bold mb-2"><ui:text key="screen.60bbdeefb7640eb4"/></h1>
                            <span class="booking-result-count">
                                <ui:text key="screen.18e872be2359d76e"/> ${totalBookings} <ui:text key="screen.1576870e601b1e12"/>
                            </span>
                        </div>

                    </div>

                    <c:if test="${param.message == 'invalid-data'}">
                        <div class="alert alert-danger">
                            <ui:text key="screen.05b9746d136b68f2"/>
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'booking-not-found'}">
                        <div class="alert alert-danger">
                            <ui:text key="screen.8c3d35a16594da92"/>
                        </div>
                    </c:if>

                    <c:if test="${invalidDates || invalidRoom}">
                        <div class="alert alert-warning" role="alert">
                            <c:if test="${invalidDates}"><p class="mb-0"><ui:text key="booking.list.date.error"/></p></c:if>
                            <c:if test="${invalidRoom}"><p class="mb-0"><ui:text key="booking.list.room.error"/></p></c:if>
                        </div>
                    </c:if>

                    <div class="card booking-filter-card mb-4">
                        <div class="card-body">
                            <form action="${pageContext.request.contextPath}/booking-management"
                                  method="get"
                                  class="booking-filter-grid">

                                <input type="hidden" name="view" value="list">

                                <div class="booking-filter-keyword">
                                    <label for="keyword"
                                           class="form-label fw-semibold">
                                        <ui:text key="screen.49c266baaaa70981"/>
                                    </label>

                                    <input type="text"
                                           id="keyword"
                                           name="keyword"
                                           class="form-control"
                                           value="${fn:escapeXml(keyword)}"
                                           placeholder="${requestScope.i18n['screen.47077267c183fca4']}" data-i18n-placeholder="screen.47077267c183fca4">
                                </div>

                                <div class="booking-filter-room">
                                    <label for="room-number" class="form-label fw-semibold"><ui:text key="booking.list.room"/></label>
                                    <input type="text" inputmode="numeric" pattern="[0-9]+" maxlength="10"
                                           id="room-number" name="room-number" class="form-control ${invalidRoom ? 'is-invalid' : ''}"
                                           value="${fn:escapeXml(roomInput)}">
                                </div>

                                <div class="booking-filter-field">
                                    <label for="status"
                                           class="form-label fw-semibold">
                                        <ui:text key="screen.920e413c7d411b61"/>
                                    </label>

                                    <select id="status"
                                            name="status"
                                            class="form-select">
                                        <option value="" data-i18n="screen.a52ace420f2175d0">${requestScope.i18n['screen.a52ace420f2175d0']}</option>

                                        <option value="pending"
                                                ${selectedStatus == 'pending' ? 'selected' : ''} data-i18n="screen.331551b0de4157c9">
                                            ${requestScope.i18n['screen.331551b0de4157c9']}
                                        </option>

                                        <option value="confirmed"
                                                ${selectedStatus == 'confirmed' ? 'selected' : ''} data-i18n="screen.fe00b67b6dd1143f">
                                            ${requestScope.i18n['screen.fe00b67b6dd1143f']}
                                        </option>

                                        <option value="checked_in"
                                                ${selectedStatus == 'checked_in' ? 'selected' : ''} data-i18n="screen.17c05c08a9ac3e5a">
                                            ${requestScope.i18n['screen.17c05c08a9ac3e5a']}
                                        </option>

                                        <option value="checked_out"
                                                ${selectedStatus == 'checked_out' ? 'selected' : ''} data-i18n="screen.cbe2b33735c6335c">
                                            ${requestScope.i18n['screen.cbe2b33735c6335c']}
                                        </option>

                                        <option value="completed"
                                                ${selectedStatus == 'completed' ? 'selected' : ''} data-i18n="screen.22a970d2e5b1cc23">
                                            ${requestScope.i18n['screen.22a970d2e5b1cc23']}
                                        </option>

                                        <option value="cancelled"
                                                ${selectedStatus == 'cancelled' ? 'selected' : ''} data-i18n="screen.d353a99eb4556847">
                                            ${requestScope.i18n['screen.d353a99eb4556847']}
                                        </option>
                                    </select>
                                </div>

                                <div class="booking-filter-field">
                                    <label for="source" class="form-label fw-semibold"><ui:text key="booking.list.source"/></label>
                                    <select id="source" name="source" class="form-select">
                                        <option value="" data-i18n="screen.a52ace420f2175d0">${requestScope.i18n['screen.a52ace420f2175d0']}</option>
                                        <option value="online" ${selectedSource eq 'online' ? 'selected' : ''}>Online</option>
                                        <option value="offline" ${selectedSource eq 'offline' ? 'selected' : ''}>Offline</option>
                                    </select>
                                </div>

                                <div class="booking-filter-field">
                                    <label for="from-date"
                                           class="form-label fw-semibold">
                                        <ui:text key="booking.list.stay.from"/>
                                    </label>

                                    <div class="search-calendar-field">
                                        <i class="bi bi-calendar3" aria-hidden="true"></i>
                                        <input type="date"
                                               id="from-date"
                                               name="from-date" data-booking-calendar
                                               class="form-control"
                                               value="${fromDate}">
                                    </div>
                                </div>

                                <div class="booking-filter-field">
                                    <label for="to-date"
                                           class="form-label fw-semibold">
                                        <ui:text key="booking.list.stay.to"/>
                                    </label>

                                    <div class="search-calendar-field">
                                        <i class="bi bi-calendar3" aria-hidden="true"></i>
                                        <input type="date"
                                               id="to-date"
                                               name="to-date" data-booking-calendar
                                               class="form-control"
                                               value="${toDate}">
                                    </div>
                                </div>

                                <div class="booking-filter-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <ui:text key="screen.49c266baaaa70981"/>
                                    </button>
                                    <c:if test="${not empty keyword || not empty selectedStatus || not empty selectedSource || not empty roomInput
                                                  || not empty fromDate || not empty toDate || invalidDates}">
                                        <a href="<c:url value='/booking-management'/>" class="btn btn-outline-secondary">
                                            <ui:text key="screen.5776503c1a52f4fa"/>
                                        </a>
                                    </c:if>
                                </div>
                            </form>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${not empty bookings}">
                            <div class="table-responsive booking-results">
                                <table class="table table-hover align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th><ui:text key="screen.a85aca2f532a24ab"/></th>
                                            <th scope="col"><ui:text key="booking.list.source"/></th>
                                            <th><ui:text key="screen.bf3763383aaf4306"/></th>
                                            <th><ui:text key="screen.011519b48fd7ae0a"/></th>
                                            <th><ui:text key="screen.fe43f0a1373a3d68"/></th>
                                            <th><ui:text key="screen.3cb3f0517ef0da8b"/></th>
                                            <th><ui:text key="screen.920e413c7d411b61"/></th>
                                            <th class="text-center"><ui:text key="screen.64cff1319d2fd2cb"/></th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        <c:forEach var="booking" items="${bookings}">
                                            <tr>
                                                <td class="fw-semibold">#${booking.id}</td>
                                                <td><span class="badge rounded-pill ${booking.offline ? 'text-bg-secondary' : 'text-bg-primary'}">${booking.offline ? 'Offline' : 'Online'}</span></td>

                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty booking.customer.firstName
                                                                        or not empty booking.customer.lastName}">
                                                            <c:out value="${booking.customer.lastName}"/>
                                                            <c:out value="${booking.customer.firstName}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <ui:text key="screen.641d305570b93564"/>${booking.customer.customerID}
                                                        </c:otherwise>
                                                    </c:choose>

                                                    <br>

                                                    <small class="text-muted">
                                                        <c:out value="${booking.customer.email}"/>
                                                    </small>
                                                </td>

                                                <td>
                                                    <time datetime="<c:out value='${booking.bookingDate}'/>"><c:out value="${fn:replace(fn:substring(booking.bookingDate, 0, 16), 'T', ' ')}"/></time>
                                                </td>

                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty booking.details}">
                                                            <c:forEach var="detail"
                                                                       items="${booking.details}">
                                                                <div class="booking-stay-row">
                                                                    <strong>
                                                                        <ui:text key="screen.6c8c6af24c9b021c"/>${detail.roomNumber}
                                                                    </strong>
                                                                    <br>
                                                                    <small class="text-muted">
                                                                        ${detail.checkInDate}
                                                                        →
                                                                        ${detail.checkOutDate}
                                                                    </small>
                                                                    <br>
                                                                    <small>
                                                                        <ui:text key="screen.1ac580f9fde7374f"/> ${detail.guestCount}
                                                                    </small>
                                                                </div>
                                                            </c:forEach>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted">
                                                                <ui:text key="screen.7184c1650cd25a38"/>
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                <td class="text-nowrap">
                                        <fmt:formatNumber
                                            value="${booking.totalPrice}"
                                            maxFractionDigits="0"/>
                                        VND
                                        </td>

                                        <td>
                                            <c:choose>
                                                <c:when test="${booking.status == 'pending'}">
                                                    <span class="badge bg-warning text-dark">
                                                        <ui:text key="screen.331551b0de4157c9"/>
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'confirmed'}">
                                                    <span class="badge bg-primary">
                                                        <ui:text key="screen.fe00b67b6dd1143f"/>
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'checked_in'}">
                                                    <span class="badge bg-info text-dark">
                                                        <ui:text key="screen.17c05c08a9ac3e5a"/>
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'checked_out'}">
                                                    <span class="badge bg-secondary">
                                                        <ui:text key="screen.cbe2b33735c6335c"/>
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'completed'}">
                                                    <span class="badge bg-success">
                                                        <ui:text key="screen.22a970d2e5b1cc23"/>
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'cancelled'}">
                                                    <span class="badge bg-danger">
                                                        <ui:text key="screen.d353a99eb4556847"/>
                                                    </span>
                                                </c:when>

                                                <c:otherwise>
                                                    <span class="badge bg-dark">
                                                        <ui:message text="${booking.status}"/>
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/booking-management?view=detail&amp;id=${booking.id}"
                                               class="btn btn-outline-primary btn-sm">
                                                <ui:text key="screen.45989de49fb7f66d"/>
                                            </a>
                                        </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <c:if test="${!invalidDates && !invalidRoom}">
                                <div class="booking-empty text-center" role="status">
                                    <ui:text key="screen.07b38d42ae8d6405"/>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${totalPages > 1}">
                        <c:url var="previousPageUrl" value="/booking-management">
                            <c:param name="view" value="list"/>
                            <c:param name="page" value="${currentPage - 1}"/>
                            <c:param name="keyword" value="${keyword}"/>
                            <c:param name="room-number" value="${roomInput}"/>
                            <c:param name="source" value="${selectedSource}"/>
                            <c:param name="status" value="${selectedStatus}"/>
                            <c:param name="from-date" value="${fromDate}"/>
                            <c:param name="to-date" value="${toDate}"/>
                        </c:url>

                        <c:url var="nextPageUrl" value="/booking-management">
                            <c:param name="view" value="list"/>
                            <c:param name="page" value="${currentPage + 1}"/>
                            <c:param name="keyword" value="${keyword}"/>
                            <c:param name="room-number" value="${roomInput}"/>
                            <c:param name="source" value="${selectedSource}"/>
                            <c:param name="status" value="${selectedStatus}"/>
                            <c:param name="from-date" value="${fromDate}"/>
                            <c:param name="to-date" value="${toDate}"/>
                        </c:url>

                        <nav class="booking-pagination mt-4" aria-label="${requestScope.i18n['screen.15fced987e317bf7']}" data-i18n-aria-label="screen.15fced987e317bf7">
                            <ul class="pagination justify-content-center flex-wrap">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="${fn:escapeXml(previousPageUrl)}">
                                        <ui:text key="screen.a57b08a480b822a0"/>
                                    </a>
                                </li>

                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <c:url var="pageUrl" value="/booking-management">
                                        <c:param name="view" value="list"/>
                                        <c:param name="page" value="${i}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="room-number" value="${roomInput}"/>
                                        <c:param name="source" value="${selectedSource}"/>
                                        <c:param name="status" value="${selectedStatus}"/>
                                        <c:param name="from-date" value="${fromDate}"/>
                                        <c:param name="to-date" value="${toDate}"/>
                                    </c:url>

                                    <li class="page-item ${currentPage == i ? 'active' : ''}">
                                        <a class="page-link"
                                           href="${fn:escapeXml(pageUrl)}">
                                            ${i}
                                        </a>
                                    </li>
                                </c:forEach>

                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="${fn:escapeXml(nextPageUrl)}">
                                        <ui:text key="screen.1ff57a29d7c9d11b"/>
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </c:if>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp"%>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js" defer></script>
        <script src="<c:url value='/assets/js/booking-management-calendar.js?v=staff-calendar-2'/>" defer></script>
    </body>
</html>
