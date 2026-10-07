<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Booking Details</title>

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


            <!-- ========================================= -->
            <!-- MESSAGES                                  -->
            <!-- ========================================= -->

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

                    This booking cannot be confirmed
                    because payment has not been completed.

                </div>

            </c:if>


            <!-- ========================================= -->
            <!-- HEADER                                    -->
            <!-- ========================================= -->

            <div
                class="d-flex
                       justify-content-between
                       align-items-center
                       mb-4">

                <div>

                    <h2 class="fw-bold mb-1">

                        Booking #${booking.id}

                    </h2>

                    <span class="text-muted">

                        Created:
                        ${booking.bookingDate}

                    </span>

                </div>

                <a
                    href="${pageContext.request.contextPath}/booking-management"
                    class="btn btn-outline-secondary">

                    Back to Bookings

                </a>

            </div>


            <!-- ========================================= -->
            <!-- STATUS / PAYMENT                          -->
            <!-- ========================================= -->

            <div class="row g-4 mb-4">

                <div class="col-md-6">

                    <div class="card shadow-sm h-100">

                        <div class="card-body">

                            <h5 class="card-title">

                                Booking Status

                            </h5>

                            <div class="mt-3">

                                <c:choose>

                                    <c:when test="${booking.status == 'pending'}">

                                        <span
                                            class="badge
                                                   bg-warning
                                                   text-dark
                                                   fs-6">

                                            Pending

                                        </span>

                                    </c:when>


                                    <c:when test="${booking.status == 'confirmed'}">

                                        <span
                                            class="badge
                                                   bg-primary
                                                   fs-6">

                                            Confirmed

                                        </span>

                                    </c:when>


                                    <c:when test="${booking.status == 'checked_in'}">

                                        <span
                                            class="badge
                                                   bg-info
                                                   text-dark
                                                   fs-6">

                                            Checked In

                                        </span>

                                    </c:when>


                                    <c:when test="${booking.status == 'checked_out'}">

                                        <span
                                            class="badge
                                                   bg-secondary
                                                   fs-6">

                                            Checked Out

                                        </span>

                                    </c:when>


                                    <c:when test="${booking.status == 'completed'}">

                                        <span
                                            class="badge
                                                   bg-success
                                                   fs-6">

                                            Completed

                                        </span>

                                    </c:when>


                                    <c:when test="${booking.status == 'cancelled'}">

                                        <span
                                            class="badge
                                                   bg-danger
                                                   fs-6">

                                            Cancelled

                                        </span>

                                    </c:when>


                                    <c:otherwise>

                                        <span
                                            class="badge
                                                   bg-dark
                                                   fs-6">

                                            ${booking.status}

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

                            <h5 class="card-title">

                                Payment Status

                            </h5>

                            <div class="mt-3">

                                <c:choose>

                                    <c:when test="${paid}">

                                        <span
                                            class="badge
                                                   bg-success
                                                   fs-6">

                                            Paid

                                        </span>

                                    </c:when>

                                    <c:otherwise>

                                        <span
                                            class="badge
                                                   bg-warning
                                                   text-dark
                                                   fs-6">

                                            Not Paid

                                        </span>

                                    </c:otherwise>

                                </c:choose>

                            </div>

                        </div>

                    </div>

                </div>

            </div>


            <!-- ========================================= -->
            <!-- CUSTOMER                                  -->
            <!-- ========================================= -->

            <div class="card shadow-sm mb-4">

                <div class="card-header">

                    <strong>
                        Customer Information
                    </strong>

                </div>

                <div class="card-body">

                    <div class="row g-3">

                        <div class="col-md-4">

                            <strong>
                                Customer ID:
                            </strong>

                            <br>

                            ${booking.customer.customerID}

                        </div>


                        <div class="col-md-4">

                            <strong>
                                Name:
                            </strong>

                            <br>

                            <c:choose>

                                <c:when test="${not empty booking.customer.firstName
                                                || not empty booking.customer.lastName}">

                                    ${booking.customer.firstName}
                                    ${booking.customer.lastName}

                                </c:when>

                                <c:otherwise>

                                    -

                                </c:otherwise>

                            </c:choose>

                        </div>


                        <div class="col-md-4">

                            <strong>
                                Username:
                            </strong>

                            <br>

                            ${booking.customer.user.username}

                        </div>


                        <div class="col-md-4">

                            <strong>
                                Email:
                            </strong>

                            <br>

                            ${booking.customer.email}

                        </div>


                        <div class="col-md-4">

                            <strong>
                                Phone:
                            </strong>

                            <br>

                            <c:choose>

                                <c:when test="${not empty booking.customer.phone}">

                                    ${booking.customer.phone}

                                </c:when>

                                <c:otherwise>

                                    -

                                </c:otherwise>

                            </c:choose>

                        </div>


                        <div class="col-md-4">

                            <strong>
                                Address:
                            </strong>

                            <br>

                            ${booking.customer.street}
                            ${booking.customer.city}
                            ${booking.customer.country}

                        </div>

                    </div>

                </div>

            </div>


            <!-- ========================================= -->
            <!-- BOOKING DETAILS                           -->
            <!-- ========================================= -->

            <div class="card shadow-sm mb-4">

                <div class="card-header">

                    <strong>
                        Stay Information
                    </strong>

                </div>

                <div class="card-body">

                    <c:choose>

                        <c:when test="${not empty booking.details}">

                            <div class="table-responsive">

                                <table
                                    class="table
                                           table-bordered
                                           align-middle">

                                    <thead class="table-light">

                                        <tr>

                                            <th>
                                                Room
                                            </th>

                                            <th>
                                                Check In
                                            </th>

                                            <th>
                                                Check Out
                                            </th>

                                            <th>
                                                Guests
                                            </th>

                                            <th>
                                                Price / Night
                                            </th>

                                            <th>
                                                Special Request
                                            </th>

                                        </tr>

                                    </thead>

                                    <tbody>

                                        <c:forEach
                                            var="detail"
                                            items="${booking.details}">

                                            <tr>

                                                <td>

                                                    Room
                                                    #${detail.roomNumber}

                                                </td>

                                                <td>

                                                    ${detail.checkInDate}

                                                </td>

                                                <td>

                                                    ${detail.checkOutDate}

                                                </td>

                                                <td>

                                                    ${detail.guestCount}

                                                </td>

                                                <td>

                                                    ${detail.pricePerNight}

                                                </td>

                                                <td>

                                                    <c:choose>

                                                        <c:when test="${not empty detail.specialRequest}">

                                                            <c:out value="${detail.specialRequest}"/>

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

                            <div class="alert alert-info">

                                No booking details found.

                            </div>

                        </c:otherwise>

                    </c:choose>

                </div>

            </div>


            <!-- ========================================= -->
            <!-- BOOKING INFORMATION                       -->
            <!-- ========================================= -->

            <div class="card shadow-sm mb-4">

                <div class="card-header">

                    <strong>
                        Booking Information
                    </strong>

                </div>

                <div class="card-body">

                    <div class="row g-3">

                        <div class="col-md-6">

                            <strong>
                                Total Price:
                            </strong>

                            <br>

                            ${booking.totalPrice}

                        </div>


                        <div class="col-md-6">

                            <strong>
                                Booking Date:
                            </strong>

                            <br>

                            ${booking.bookingDate}

                        </div>


                        <div class="col-12">

                            <strong>
                                Special Request:
                            </strong>

                            <br>

                            <c:choose>

                                <c:when test="${not empty booking.specialRequest}">

                                    <c:out value="${booking.specialRequest}"/>

                                </c:when>

                                <c:otherwise>

                                    -

                                </c:otherwise>

                            </c:choose>

                        </div>


                        <c:if test="${booking.status == 'cancelled'}">

                            <div class="col-md-6">

                                <strong>
                                    Cancellation Date:
                                </strong>

                                <br>

                                ${booking.cancellationDate}

                            </div>


                            <div class="col-md-6">

                                <strong>
                                    Cancellation Reason:
                                </strong>

                                <br>

                                <c:choose>

                                    <c:when test="${not empty booking.cancellationReason}">

                                        <c:out value="${booking.cancellationReason}"/>

                                    </c:when>

                                    <c:otherwise>

                                        -

                                    </c:otherwise>

                                </c:choose>

                            </div>

                        </c:if>

                    </div>

                </div>

            </div>


            <!-- ========================================= -->
            <!-- UPDATE STATUS                             -->
            <!-- ========================================= -->

            <div class="card shadow-sm">

                <div class="card-header">

                    <strong>
                        Update Booking Status
                    </strong>

                </div>

                <div class="card-body">

                    <c:choose>

                        <c:when test="${not empty nextStatuses}">

                            <form
                                action="${pageContext.request.contextPath}/booking-management"
                                method="post"
                                class="row g-3 align-items-end">

                                <input
                                    type="hidden"
                                    name="action"
                                    value="update-status">

                                <input
                                    type="hidden"
                                    name="booking-id"
                                    value="${booking.id}">


                                <div class="col-md-8">

                                    <label
                                        for="status"
                                        class="form-label fw-semibold">

                                        New Status

                                    </label>

                                    <select
                                        id="status"
                                        name="status"
                                        class="form-select"
                                        required>

                                        <option
                                            value=""
                                            selected
                                            disabled>

                                            Select new status

                                        </option>

                                        <c:forEach
                                            var="status"
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
                                                        ${status}
                                                    </c:otherwise>

                                                </c:choose>

                                            </option>

                                        </c:forEach>

                                    </select>

                                </div>


                                <div class="col-md-4">

                                    <button
                                        type="submit"
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

    <%@include file="/WEB-INF/include/footer.jsp" %>

</div>


<script
    src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js">
</script>

</body>

</html>