<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Booking Management</title>

    <link
        href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css"
        rel="stylesheet">

    <link
        href="${pageContext.request.contextPath}/assets/css/general.css"
        rel="stylesheet">

</head>

<body>

<div class="body-wrapper">

    <%@include file="/WEB-INF/include/header.jsp" %>

    <main>

        <div class="container py-5">

            <div
                class="d-flex
                       justify-content-between
                       align-items-center
                       mb-4">

                <h2 class="fw-bold mb-0">

                    Booking Management

                </h2>

                <span class="text-muted">

                    Total:
                    ${totalBookings}
                    booking(s)

                </span>

            </div>


            <!-- ========================================= -->
            <!-- MESSAGES                                  -->
            <!-- ========================================= -->

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

                    ${dateError}

                </div>

            </c:if>


            <!-- ========================================= -->
            <!-- SEARCH / FILTER                           -->
            <!-- ========================================= -->

            <div class="card shadow-sm mb-4">

                <div class="card-body">

                    <form
                        action="${pageContext.request.contextPath}/booking-management"
                        method="get"
                        class="row g-3">

                        <input
                            type="hidden"
                            name="view"
                            value="list">


                        <!-- Keyword -->

                        <div class="col-lg-4 col-md-6">

                            <label
                                for="keyword"
                                class="form-label fw-semibold">

                                Search

                            </label>

                            <input
                                type="text"
                                id="keyword"
                                name="keyword"
                                class="form-control"
                                value="${keyword}"
                                placeholder="Booking ID, customer, email...">

                        </div>


                        <!-- Status -->

                        <div class="col-lg-2 col-md-6">

                            <label
                                for="status"
                                class="form-label fw-semibold">

                                Status

                            </label>

                            <select
                                id="status"
                                name="status"
                                class="form-select">

                                <option value="">
                                    All
                                </option>

                                <option
                                    value="pending"
                                    ${selectedStatus == 'pending'
                                      ? 'selected'
                                      : ''}>

                                    Pending

                                </option>

                                <option
                                    value="confirmed"
                                    ${selectedStatus == 'confirmed'
                                      ? 'selected'
                                      : ''}>

                                    Confirmed

                                </option>

                                <option
                                    value="checked_in"
                                    ${selectedStatus == 'checked_in'
                                      ? 'selected'
                                      : ''}>

                                    Checked In

                                </option>

                                <option
                                    value="checked_out"
                                    ${selectedStatus == 'checked_out'
                                      ? 'selected'
                                      : ''}>

                                    Checked Out

                                </option>

                                <option
                                    value="completed"
                                    ${selectedStatus == 'completed'
                                      ? 'selected'
                                      : ''}>

                                    Completed

                                </option>

                                <option
                                    value="cancelled"
                                    ${selectedStatus == 'cancelled'
                                      ? 'selected'
                                      : ''}>

                                    Cancelled

                                </option>

                            </select>

                        </div>


                        <!-- From Date -->

                        <div class="col-lg-2 col-md-6">

                            <label
                                for="from-date"
                                class="form-label fw-semibold">

                                From Date

                            </label>

                            <input
                                type="date"
                                id="from-date"
                                name="from-date"
                                class="form-control"
                                value="${fromDate}">

                        </div>


                        <!-- To Date -->

                        <div class="col-lg-2 col-md-6">

                            <label
                                for="to-date"
                                class="form-label fw-semibold">

                                To Date

                            </label>

                            <input
                                type="date"
                                id="to-date"
                                name="to-date"
                                class="form-control"
                                value="${toDate}">

                        </div>


                        <!-- Search button -->

                        <div
                            class="col-lg-2
                                   d-flex
                                   align-items-end">

                            <button
                                type="submit"
                                class="btn btn-primary w-100">

                                Search

                            </button>

                        </div>

                    </form>


                    <c:if test="${not empty keyword
                                  || not empty selectedStatus
                                  || not empty fromDate
                                  || not empty toDate}">

                        <div class="mt-3">

                            <a
                                href="${pageContext.request.contextPath}/booking-management"
                                class="btn btn-outline-secondary btn-sm">

                                Clear Filters

                            </a>

                        </div>

                    </c:if>

                </div>

            </div>


            <!-- ========================================= -->
            <!-- BOOKING TABLE                             -->
            <!-- ========================================= -->

            <c:choose>

                <c:when test="${not empty bookings}">

                    <div class="table-responsive">

                        <table
                            class="table
                                   table-bordered
                                   table-hover
                                   align-middle">

                            <thead class="table-dark">

                                <tr>

                                    <th>
                                        Booking ID
                                    </th>

                                    <th>
                                        Customer
                                    </th>

                                    <th>
                                        Booking Date
                                    </th>

                                    <th>
                                        Room / Stay
                                    </th>

                                    <th>
                                        Total Price
                                    </th>

                                    <th>
                                        Status
                                    </th>

                                    <th class="text-center">
                                        Action
                                    </th>

                                </tr>

                            </thead>


                            <tbody>

                                <c:forEach
                                    var="booking"
                                    items="${bookings}">

                                    <tr>

                                        <!-- Booking ID -->

                                        <td>

                                            #${booking.id}

                                        </td>


                                        <!-- Customer -->

                                        <td>

                                            <c:choose>

                                                <c:when test="${not empty booking.customer.firstName
                                                                || not empty booking.customer.lastName}">

                                                    ${booking.customer.firstName}
                                                    ${booking.customer.lastName}

                                                </c:when>

                                                <c:otherwise>

                                                    Customer
                                                    #${booking.customer.customerID}

                                                </c:otherwise>

                                            </c:choose>

                                            <br>

                                            <small class="text-muted">

                                                ${booking.customer.email}

                                            </small>

                                        </td>


                                        <!-- Booking Date -->

                                        <td>

                                            ${booking.bookingDate}

                                        </td>


                                        <!-- Room / stay -->

                                        <td>

                                            <c:choose>

                                                <c:when test="${not empty booking.details}">

                                                    <c:forEach
                                                        var="detail"
                                                        items="${booking.details}">

                                                        <div class="mb-2">

                                                            <strong>

                                                                Room
                                                                #${detail.roomNumber}

                                                            </strong>

                                                            <br>

                                                            <small class="text-muted">

                                                                ${detail.checkInDate}
                                                                →
                                                                ${detail.checkOutDate}

                                                            </small>

                                                            <br>

                                                            <small>

                                                                Guests:
                                                                ${detail.guestCount}

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


                                        <!-- Price -->

                                        <td>

                                            ${booking.totalPrice}

                                        </td>


                                        <!-- Status -->

                                        <td>

                                            <c:choose>

                                                <c:when test="${booking.status == 'pending'}">

                                                    <span
                                                        class="badge
                                                               bg-warning
                                                               text-dark">

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

                                                        ${booking.status}

                                                    </span>

                                                </c:otherwise>

                                            </c:choose>

                                        </td>


                                        <!-- Action -->

                                        <td class="text-center">

                                            <a
                                                href="${pageContext.request.contextPath}/booking-management?view=detail&id=${booking.id}"
                                                class="btn
                                                       btn-outline-primary
                                                       btn-sm">

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


            <!-- ========================================= -->
            <!-- PAGINATION                                -->
            <!-- ========================================= -->

            <c:if test="${totalPages > 1}">

                <nav class="mt-4">

                    <ul class="pagination justify-content-center">


                        <!-- Previous -->

                        <li
                            class="page-item
                                   ${currentPage == 1
                                     ? 'disabled'
                                     : ''}">

                            <a
                                class="page-link"
                                href="${pageContext.request.contextPath}/booking-management?view=list&page=${currentPage - 1}&keyword=${keyword}&status=${selectedStatus}&from-date=${fromDate}&to-date=${toDate}">

                                Previous

                            </a>

                        </li>


                        <!-- Pages -->

                        <c:forEach
                            var="i"
                            begin="1"
                            end="${totalPages}">

                            <li
                                class="page-item
                                       ${currentPage == i
                                         ? 'active'
                                         : ''}">

                                <a
                                    class="page-link"
                                    href="${pageContext.request.contextPath}/booking-management?view=list&page=${i}&keyword=${keyword}&status=${selectedStatus}&from-date=${fromDate}&to-date=${toDate}">

                                    ${i}

                                </a>

                            </li>

                        </c:forEach>


                        <!-- Next -->

                        <li
                            class="page-item
                                   ${currentPage == totalPages
                                     ? 'disabled'
                                     : ''}">

                            <a
                                class="page-link"
                                href="${pageContext.request.contextPath}/booking-management?view=list&page=${currentPage + 1}&keyword=${keyword}&status=${selectedStatus}&from-date=${fromDate}&to-date=${toDate}">

                                Next

                            </a>

                        </li>

                    </ul>

                </nav>

            </c:if>

        </div>

    </main>

    <%@include file="/WEB-INF/include/footer.jsp" %>

</div>


<script
    src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js">
</script>

</body>

</html>