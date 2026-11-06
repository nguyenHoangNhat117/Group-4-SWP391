<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title>Booking Details</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
    </head>

    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main>
                <div class="container py-5">

                    <%-- UC09: người tạo và điểm vào thu tiền tại quầy --%>
                    <c:if test="${not empty offlineCreator}">
                        <div class="alert alert-info d-flex flex-wrap justify-content-between align-items-center gap-3">
                            <span>
                                Offline booking · Người tạo:
                                <strong>
                                    <c:out value="${offlineCreator}"/>
                                </strong>
                            </span>

                            <a class="btn btn-primary"
                               href="${pageContext.request.contextPath}/counter-payment?bookingId=${booking.id}">
                                Thu tiền tại quầy / xem số dư
                            </a>
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'status-updated'}">
                        <div class="alert alert-success">
                            Booking status updated successfully.
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'invalid-transition'}">
                        <div class="alert alert-danger">
                            Invalid booking status transition.
                        </div>
                    </c:if>

                    <c:if test="${param.message == 'payment-required'}">
                        <div class="alert alert-warning">
                            Payment is required before confirming this booking.
                            For an offline booking, use the counter payment screen.
                        </div>
                    </c:if>

                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                        <div>
                            <h2 class="fw-bold mb-1">
                                Booking #${booking.id}
                            </h2>
                            <span class="text-muted">
                                Created:
                                <c:out value="${booking.bookingDate}"/>
                            </span>
                        </div>

                        <a href="${pageContext.request.contextPath}/booking-management"
                           class="btn btn-outline-secondary">
                            Back to Bookings
                        </a>
                    </div>

                    <div class="row g-4 mb-4">
                        <div class="col-md-6">
                            <div class="card shadow-sm h-100">
                                <div class="card-body">
                                    <h5 class="card-title">Booking Status</h5>

                                    <div class="mt-3">
                                        <c:choose>
                                            <c:when test="${booking.status == 'pending'}">
                                                <span class="badge bg-warning text-dark fs-6">
                                                    Pending
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'confirmed'}">
                                                <span class="badge bg-primary fs-6">
                                                    Confirmed
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'checked_in'}">
                                                <span class="badge bg-info text-dark fs-6">
                                                    Checked In
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'checked_out'}">
                                                <span class="badge bg-secondary fs-6">
                                                    Checked Out
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'completed'}">
                                                <span class="badge bg-success fs-6">
                                                    Completed
                                                </span>
                                            </c:when>

                                            <c:when test="${booking.status == 'cancelled'}">
                                                <span class="badge bg-danger fs-6">
                                                    Cancelled
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="badge bg-dark fs-6">
                                                    <c:out value="${booking.status}"/>
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
                                    <h5 class="card-title">Payment Status</h5>

                                    <div class="mt-3">
                                        <c:choose>
                                            <c:when test="${paid}">
                                                <span class="badge bg-success fs-6">
                                                    Fully Paid
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="badge bg-warning text-dark fs-6">
                                                    Not Fully Paid
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <p class="small text-muted mt-3 mb-0">
                                        A deposit may have been collected.
                                        Check payment records or open the counter
                                        payment screen to see the remaining balance.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-header">
                            <strong>Payment Details</strong>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty payments}">
                                    <div class="table-responsive">
                                        <table class="table table-bordered align-middle mb-0">
                                            <thead class="table-light">
                                                <tr>
                                                    <th>ID</th>
                                                    <th>Date</th>
                                                    <th>Amount</th>
                                                    <th>Method</th>
                                                    <th>Status</th>
                                                    <th>Transaction</th>
                                                    <th>Discount</th>
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
                                                    maxFractionDigits="2"/>
                                                VND
                                                </td>

                                                <td>
                                                    <c:out value="${payment.paymentMethod}"/>
                                                </td>

                                                <td>
                                                    <c:out value="${payment.paymentStatus}"/>
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
                                        No payment record for this booking.
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-header">
                            <strong>Customer Information</strong>
                        </div>

                        <div class="card-body">
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <strong>Customer ID:</strong>
                                    <br>
                                    ${booking.customer.customerID}
                                </div>

                                <div class="col-md-4">
                                    <strong>Name:</strong>
                                    <br>

                                    <c:choose>
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
                                    <strong>Username:</strong>
                                    <br>
                                    <c:out value="${booking.customer.user.username}"/>
                                </div>

                                <div class="col-md-4">
                                    <strong>Email:</strong>
                                    <br>
                                    <c:out value="${booking.customer.email}"/>
                                </div>

                                <div class="col-md-4">
                                    <strong>Phone:</strong>
                                    <br>
                                    <c:out value="${empty booking.customer.phone
                                                    ? '-' : booking.customer.phone}"/>
                                </div>

                                <div class="col-md-4">
                                    <strong>Address:</strong>
                                    <br>
                                    <c:out value="${booking.customer.street}"/>
                                    <c:out value="${booking.customer.city}"/>
                                    <c:out value="${booking.customer.country}"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-header">
                            <strong>Stay Information</strong>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty booking.details}">
                                    <div class="table-responsive">
                                        <table class="table table-bordered align-middle">
                                            <thead class="table-light">
                                                <tr>
                                                    <th>Room</th>
                                                    <th>Check In</th>
                                                    <th>Check Out</th>
                                                    <th>Guests</th>
                                                    <th>Price / Night</th>
                                                    <th>Special Request</th>
                                                </tr>
                                            </thead>

                                            <tbody>
                                                <c:forEach var="detail"
                                                           items="${booking.details}">
                                                    <tr>
                                                        <td>
                                                            Room #${detail.roomNumber}
                                                        </td>
                                                        <td>${detail.checkInDate}</td>
                                                        <td>${detail.checkOutDate}</td>
                                                        <td>${detail.guestCount}</td>

                                                        <td class="text-nowrap">
                                                <fmt:formatNumber
                                                    value="${detail.pricePerNight}"
                                                    maxFractionDigits="2"/>
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
                                        No booking details found.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-header">
                            <strong>Booking Information</strong>
                        </div>

                        <div class="card-body">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <strong>Total Price:</strong>
                                    <br>
                                    <fmt:formatNumber
                                        value="${booking.totalPrice}"
                                        maxFractionDigits="2"/>
                                    VND
                                </div>

                                <div class="col-md-6">
                                    <strong>Booking Date:</strong>
                                    <br>
                                    <c:out value="${booking.bookingDate}"/>
                                </div>

                                <div class="col-12">
                                    <strong>Special Request:</strong>
                                    <br>
                                    <c:out value="${empty booking.specialRequest
                                                    ? '-' : booking.specialRequest}"/>
                                </div>

                                <c:if test="${booking.status == 'cancelled'}">
                                    <div class="col-md-6">
                                        <strong>Cancellation Date:</strong>
                                        <br>
                                        <c:out value="${booking.cancellationDate}"/>
                                    </div>

                                    <div class="col-md-6">
                                        <strong>Cancellation Reason:</strong>
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
                            <strong>Update Booking Status</strong>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty nextStatuses}">
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
                                                New Status
                                            </label>

                                            <select id="status"
                                                    name="status"
                                                    class="form-select"
                                                    required>

                                                <option value="" selected disabled>
                                                    Select new status
                                                </option>

                                                <c:forEach var="status"
                                                           items="${nextStatuses}">
                                                    <option value="${status}">
                                                        <c:choose>
                                                            <c:when test="${status == 'confirmed'}">
                                                                Confirmed
                                                            </c:when>
                                                            <c:when test="${status == 'cancelled'}">
                                                                Cancelled
                                                            </c:when>
                                                            <c:when test="${status == 'checked_in'}">
                                                                Checked In
                                                            </c:when>
                                                            <c:when test="${status == 'checked_out'}">
                                                                Checked Out
                                                            </c:when>
                                                            <c:when test="${status == 'completed'}">
                                                                Completed
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
                                                Update Status
                                            </button>
                                        </div>
                                    </form>
                                </c:when>

                                <c:otherwise>
                                    <div class="alert alert-secondary mb-0">
                                        This booking has no further status transition.
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