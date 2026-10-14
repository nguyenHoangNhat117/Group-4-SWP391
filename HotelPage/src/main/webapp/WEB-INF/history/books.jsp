<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>

    <meta
        http-equiv="Content-Type"
        content="text/html; charset=UTF-8">

    <title>Booking History</title>

    <link
        href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
        rel="stylesheet">

    <link
        href="<%= request.getContextPath()%>/assets/css/general.css"
        rel="stylesheet">

</head>

<body>

<div class="body-wrapper">

    <%@include file="/WEB-INF/include/header.jsp" %>

    <main>

        <div class="container py-4">

            <!-- ================================================= -->
            <!-- PAGE TITLE                                        -->
            <!-- ================================================= -->

            <c:choose>

                <c:when test="${loggedUser.role == 'admin'
                                || loggedUser.role == 'staff'}">

                    <h2 class="fw-bold text-center mb-4">
                        Customer
                        (ID: ${requestScope.historyCustomerId})
                        Booking History
                    </h2>

                </c:when>

                <c:otherwise>

                    <h2 class="fw-bold text-center mb-4">
                        Your Booking History
                    </h2>

                </c:otherwise>

            </c:choose>


            <!-- ================================================= -->
            <!-- MESSAGES                                          -->
            <!-- ================================================= -->

            <c:if test="${param.message == 'cancel-success'}">

                <div class="alert alert-success text-center">

                    Booking cancelled successfully.

                </div>

            </c:if>


            <c:if test="${param.message == 'cannot-cancel'}">

                <div class="alert alert-danger text-center">

                    This booking cannot be cancelled.

                    Cancellation is allowed only for eligible
                    pending/confirmed bookings at least
                    24 hours before check-in.

                </div>

            </c:if>


            <c:if test="${param.message == 'invalid-booking'}">

                <div class="alert alert-danger text-center">

                    Invalid booking.

                </div>

            </c:if>


            <c:if test="${param.message == 'booking-not-found'}">

                <div class="alert alert-danger text-center">

                    Booking not found.

                </div>

            </c:if>


            <!-- ================================================= -->
            <!-- BOOKING LIST: compact table with expandable details -->
            <c:choose>
                <c:when test="${not empty requestScope.bookings}">
                    <div class="table-responsive shadow-sm rounded-3">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th scope="col">Booking</th>
                                    <th scope="col">Booking Date</th>
                                    <th scope="col">Stay Dates</th>
                                    <th scope="col">Total</th>
                                    <th scope="col">Booking Status</th>
                                    <th scope="col">Payment</th>
                                    <th scope="col" class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="booking" items="${requestScope.bookings}">
                                    <tr>
                                        <td class="fw-semibold">#${booking.id}</td>
                                        <td>${booking.bookingDate}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty booking.details}">
                                                    ${booking.details[0].checkInDate}
                                                    <span class="text-muted">to</span>
                                                    ${booking.details[0].checkOutDate}
                                                </c:when>
                                                <c:otherwise><span class="text-muted">Not available</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="fw-bold text-success text-nowrap">$${booking.totalPrice}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${booking.status == 'pending'}"><span class="badge bg-warning text-dark">Pending</span></c:when>
                                                <c:when test="${booking.status == 'confirmed'}"><span class="badge bg-primary">Confirmed</span></c:when>
                                                <c:when test="${booking.status == 'cancelled'}"><span class="badge bg-danger">Cancelled</span></c:when>
                                                <c:when test="${booking.status == 'checked_in'}"><span class="badge bg-info text-dark">Checked In</span></c:when>
                                                <c:when test="${booking.status == 'checked_out'}"><span class="badge bg-secondary">Checked Out</span></c:when>
                                                <c:when test="${booking.status == 'completed'}"><span class="badge bg-success">Completed</span></c:when>
                                                <c:otherwise><span class="badge bg-secondary">${booking.status}</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${paidMap[booking.id]}"><span class="badge bg-success">Paid</span></c:when>
                                                <c:otherwise><span class="badge bg-warning text-dark">Unpaid</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end text-nowrap">
                                            <button class="btn btn-outline-primary btn-sm"
                                                    type="button"
                                                    data-bs-toggle="collapse"
                                                    data-bs-target="#booking-details-${booking.id}"
                                                    aria-expanded="false"
                                                    aria-controls="booking-details-${booking.id}">
                                                View Details
                                            </button>
                                        </td>
                                    </tr>
                                    <tr class="table-light">
                                        <td colspan="7" class="p-0 border-0">
                                            <div class="collapse" id="booking-details-${booking.id}">
                                                <div class="p-3 p-md-4 border-bottom">
                                                    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                                                        <div>
                                                            <h5 class="mb-2">Booking #${booking.id} Details</h5>
                                                            <p class="mb-1"><strong>Booking Date:</strong> ${booking.bookingDate}</p>
                                                            <c:if test="${not empty booking.specialRequest}">
                                                                <p class="mb-1"><strong>Special Request:</strong> ${booking.specialRequest}</p>
                                                            </c:if>
                                                            <c:if test="${booking.status == 'cancelled'}">
                                                                <p class="mb-1"><strong>Cancellation Date:</strong> ${booking.cancellationDate}</p>
                                                                <c:if test="${not empty booking.cancellationReason}">
                                                                    <p class="mb-1"><strong>Cancellation Reason:</strong> ${booking.cancellationReason}</p>
                                                                </c:if>
                                                            </c:if>
                                                        </div>
                                                        <div class="text-md-end">
                                                            <div class="text-muted small">Total price</div>
                                                            <div class="fw-bold fs-5 text-success">$${booking.totalPrice}</div>
                                                        </div>
                                                    </div>

                                                    <h6 class="fw-bold mb-3">Room Details</h6>
                                                    <c:forEach var="detail" items="${booking.details}">
                                                        <div class="card mb-2">
                                                            <div class="card-body">
                                                                <div class="row g-3">
                                                                    <div class="col-12 col-md-6">
                                                                        <p class="mb-1"><strong>Room:</strong>
                                                                            <a href="${pageContext.request.contextPath}/details?roomNumber=${detail.roomNumber}">#${detail.roomNumber}</a>
                                                                        </p>
                                                                        <p class="mb-1"><strong>Check-in:</strong> ${detail.checkInDate}</p>
                                                                        <p class="mb-1"><strong>Check-out:</strong> ${detail.checkOutDate}</p>
                                                                    </div>
                                                                    <div class="col-12 col-md-6">
                                                                        <p class="mb-1"><strong>Guest Count:</strong> ${detail.guestCount}</p>
                                                                        <p class="mb-1"><strong>Price Per Night:</strong> $${detail.pricePerNight}</p>
                                                                        <c:if test="${not empty detail.specialRequest}">
                                                                            <p class="mb-0"><strong>Room Request:</strong> ${detail.specialRequest}</p>
                                                                        </c:if>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </c:forEach>

                                                    <c:if test="${loggedUser.role == 'customer'}">
                                                        <div class="d-flex justify-content-end flex-wrap gap-2 mt-3">
                                                            <c:if test="${booking.status == 'pending' && !paidMap[booking.id]}">
                                                                <form action="${pageContext.request.contextPath}/payment" method="get" class="m-0">
                                                                    <input type="hidden" name="bookingId" value="${booking.id}">
                                                                    <button class="btn btn-success btn-sm" type="submit">Pay Now</button>
                                                                </form>
                                                            </c:if>
                                                            <c:if test="${booking.status == 'pending' || booking.status == 'confirmed'}">
                                                                <a href="${pageContext.request.contextPath}/history/cancel-booking?id=${booking.id}"
                                                                   class="btn btn-danger btn-sm">Cancel Booking</a>
                                                            </c:if>
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- ================================================= -->
                    <!-- PAGINATION                                        -->
                    <!-- ================================================= -->

                    <c:if test="${totalPages > 1}">

                        <nav
                            aria-label="Booking history pagination"
                            class="d-flex
                                   justify-content-center
                                   mt-4">


                            <ul class="pagination">


                                <!-- Previous -->

                                <li
                                    class="page-item
                                           ${currentPage == 1
                                             ? 'disabled'
                                             : ''}">

                                    <a
                                        class="page-link"
                                        href="${pageContext.request.contextPath}/history?id=${requestScope.historyCustomerId}&view=bookings&page=${currentPage - 1}">

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
                                            href="${pageContext.request.contextPath}/history?id=${requestScope.historyCustomerId}&view=bookings&page=${i}">

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
                                        href="${pageContext.request.contextPath}/history?id=${requestScope.historyCustomerId}&view=bookings&page=${currentPage + 1}">

                                        Next

                                    </a>

                                </li>


                            </ul>

                        </nav>

                    </c:if>



                </c:when>


                <c:otherwise>

                    <div class="alert alert-info text-center mt-5">

                        <c:choose>

                            <c:when test="${loggedUser.role == 'admin'
                                            || loggedUser.role == 'staff'}">

                                This customer has no bookings.

                            </c:when>

                            <c:otherwise>

                                You have no bookings yet.

                            </c:otherwise>

                        </c:choose>

                    </div>

                </c:otherwise>

            </c:choose>


        </div>

    </main>


    <%@include file="/WEB-INF/include/footer.jsp" %>

</div>


<script
    src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js">
</script>

</body>

</html>