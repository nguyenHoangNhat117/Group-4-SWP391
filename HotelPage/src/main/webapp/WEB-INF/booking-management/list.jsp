<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title>Booking Management</title>

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
    </head>

    <body class="luxe-admin-suite luxe-admin-bookings">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main>
                <div class="luxe-admin-banner" aria-label="Booking operations">
                    <span class="luxe-admin-banner-eyebrow">
                        LUXE ESCAPE
                        <span aria-hidden="true">/</span>
                        RESERVATIONS &amp; STAYS
                    </span>

                    <span class="luxe-admin-banner-title">
                        Booking operations
                    </span>

                    <span class="luxe-admin-banner-art" aria-hidden="true"></span>
                </div>

                <div class="container py-5">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                        <div>
                            <h2 class="fw-bold mb-1">Booking Management</h2>
                            <span class="text-muted">
                                Total: ${totalBookings} booking(s)
                            </span>
                        </div>

                        <a class="btn btn-primary"
                           href="${pageContext.request.contextPath}/offline-booking">
                            Create Offline Booking
                        </a>
                    </div>

                    <c:if test="${param.message == 'invalid-data'}">
                        <div class="alert alert-danger">
                            Invalid booking information.
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'booking-not-found'}">
                        <div class="alert alert-danger">
                            Booking not found.
                        </div>
                    </c:if>

                    <c:if test="${not empty dateError}">
                        <div class="alert alert-warning">
                            <c:out value="${dateError}"/>
                        </div>
                    </c:if>

                    <div class="card shadow-sm mb-4">
                        <div class="card-body">
                            <form action="${pageContext.request.contextPath}/booking-management"
                                  method="get"
                                  class="row g-3">

                                <input type="hidden" name="view" value="list">

                                <div class="col-lg-4 col-md-6">
                                    <label for="keyword"
                                           class="form-label fw-semibold">
                                        Search
                                    </label>

                                    <input type="text"
                                           id="keyword"
                                           name="keyword"
                                           class="form-control"
                                           value="${fn:escapeXml(keyword)}"
                                           placeholder="Booking ID, customer, email...">
                                </div>

                                <div class="col-lg-2 col-md-6">
                                    <label for="status"
                                           class="form-label fw-semibold">
                                        Status
                                    </label>

                                    <select id="status"
                                            name="status"
                                            class="form-select">
                                        <option value="">All</option>

                                        <option value="pending"
                                                ${selectedStatus == 'pending' ? 'selected' : ''}>
                                            Pending
                                        </option>

                                        <option value="confirmed"
                                                ${selectedStatus == 'confirmed' ? 'selected' : ''}>
                                            Confirmed
                                        </option>

                                        <option value="checked_in"
                                                ${selectedStatus == 'checked_in' ? 'selected' : ''}>
                                            Checked In
                                        </option>

                                        <option value="checked_out"
                                                ${selectedStatus == 'checked_out' ? 'selected' : ''}>
                                            Checked Out
                                        </option>

                                        <option value="completed"
                                                ${selectedStatus == 'completed' ? 'selected' : ''}>
                                            Completed
                                        </option>

                                        <option value="cancelled"
                                                ${selectedStatus == 'cancelled' ? 'selected' : ''}>
                                            Cancelled
                                        </option>
                                    </select>
                                </div>

                                <div class="col-lg-2 col-md-6">
                                    <label for="from-date"
                                           class="form-label fw-semibold">
                                        From Date
                                    </label>

                                    <input type="date"
                                           id="from-date"
                                           name="from-date"
                                           class="form-control"
                                           value="${fromDate}">
                                </div>

                                <div class="col-lg-2 col-md-6">
                                    <label for="to-date"
                                           class="form-label fw-semibold">
                                        To Date
                                    </label>

                                    <input type="date"
                                           id="to-date"
                                           name="to-date"
                                           class="form-control"
                                           value="${toDate}">
                                </div>

                                <div class="col-lg-2 d-flex align-items-end">
                                    <button type="submit"
                                            class="btn btn-primary w-100">
                                        Search
                                    </button>
                                </div>
                            </form>

                            <c:if test="${not empty keyword
                                          or not empty selectedStatus
                                          or not empty fromDate
                                          or not empty toDate}">
                                  <div class="mt-3">
                                      <a href="${pageContext.request.contextPath}/booking-management"
                                         class="btn btn-outline-secondary btn-sm">
                                          Clear Filters
                                      </a>
                                  </div>
                            </c:if>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${not empty bookings}">
                            <div class="table-responsive">
                                <table class="table table-bordered table-hover align-middle">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>Booking ID</th>
                                            <th>Customer</th>
                                            <th>Booking Date</th>
                                            <th>Room / Stay</th>
                                            <th>Total Price</th>
                                            <th>Status</th>
                                            <th class="text-center">Action</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        <c:forEach var="booking" items="${bookings}">
                                            <tr>
                                                <td>#${booking.id}</td>

                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty booking.customer.firstName
                                                                        or not empty booking.customer.lastName}">
                                                            <c:out value="${booking.customer.lastName}"/>
                                                            <c:out value="${booking.customer.firstName}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            Customer #${booking.customer.customerID}
                                                        </c:otherwise>
                                                    </c:choose>

                                                    <br>

                                                    <small class="text-muted">
                                                        <c:out value="${booking.customer.email}"/>
                                                    </small>
                                                </td>

                                                <td>
                                                    <c:out value="${booking.bookingDate}"/>
                                                </td>

                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty booking.details}">
                                                            <c:forEach var="detail"
                                                                       items="${booking.details}">
                                                                <div class="mb-2">
                                                                    <strong>
                                                                        Room #${detail.roomNumber}
                                                                    </strong>
                                                                    <br>
                                                                    <small class="text-muted">
                                                                        ${detail.checkInDate}
                                                                        →
                                                                        ${detail.checkOutDate}
                                                                    </small>
                                                                    <br>
                                                                    <small>
                                                                        Guests: ${detail.guestCount}
                                                                    </small>
                                                                </div>
                                                            </c:forEach>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted">
                                                                No detail
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                <td class="text-nowrap">
                                        <fmt:formatNumber
                                            value="${booking.totalPrice}"
                                            maxFractionDigits="2"/>
                                        VND
                                        </td>

                                        <td>
                                            <c:choose>
                                                <c:when test="${booking.status == 'pending'}">
                                                    <span class="badge bg-warning text-dark">
                                                        Pending
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'confirmed'}">
                                                    <span class="badge bg-primary">
                                                        Confirmed
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'checked_in'}">
                                                    <span class="badge bg-info text-dark">
                                                        Checked In
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'checked_out'}">
                                                    <span class="badge bg-secondary">
                                                        Checked Out
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'completed'}">
                                                    <span class="badge bg-success">
                                                        Completed
                                                    </span>
                                                </c:when>

                                                <c:when test="${booking.status == 'cancelled'}">
                                                    <span class="badge bg-danger">
                                                        Cancelled
                                                    </span>
                                                </c:when>

                                                <c:otherwise>
                                                    <span class="badge bg-dark">
                                                        <c:out value="${booking.status}"/>
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/booking-management?view=detail&amp;id=${booking.id}"
                                               class="btn btn-outline-primary btn-sm">
                                                Details
                                            </a>
                                        </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <div class="alert alert-info text-center">
                                No bookings found.
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${totalPages > 1}">
                        <c:url var="previousPageUrl" value="/booking-management">
                            <c:param name="view" value="list"/>
                            <c:param name="page" value="${currentPage - 1}"/>
                            <c:param name="keyword" value="${keyword}"/>
                            <c:param name="status" value="${selectedStatus}"/>
                            <c:param name="from-date" value="${fromDate}"/>
                            <c:param name="to-date" value="${toDate}"/>
                        </c:url>

                        <c:url var="nextPageUrl" value="/booking-management">
                            <c:param name="view" value="list"/>
                            <c:param name="page" value="${currentPage + 1}"/>
                            <c:param name="keyword" value="${keyword}"/>
                            <c:param name="status" value="${selectedStatus}"/>
                            <c:param name="from-date" value="${fromDate}"/>
                            <c:param name="to-date" value="${toDate}"/>
                        </c:url>

                        <nav class="mt-4" aria-label="Booking pages">
                            <ul class="pagination justify-content-center flex-wrap">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="${fn:escapeXml(previousPageUrl)}">
                                        Previous
                                    </a>
                                </li>

                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <c:url var="pageUrl" value="/booking-management">
                                        <c:param name="view" value="list"/>
                                        <c:param name="page" value="${i}"/>
                                        <c:param name="keyword" value="${keyword}"/>
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
                                        Next
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
    </body>
</html>