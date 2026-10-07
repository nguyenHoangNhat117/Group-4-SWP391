<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <title>Booking Reports</title>

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

                <div class="container py-4">

                    <h2 class="fw-bold text-center mb-4">
                        Booking Report
                    </h2>


                    <c:choose>

                        <c:when test="${not empty requestScope.bookings}">

                            <div class="d-flex flex-column gap-3">

                                <c:forEach
                                    var="booking"
                                    items="${requestScope.bookings}">

                                    <div
                                        class="d-flex
                                               flex-column
                                               flex-md-row
                                               shadow-sm
                                               rounded-4
                                               p-3
                                               align-items-md-center
                                               justify-content-between
                                               bg-white">

                                        <div class="mb-3 mb-md-0">

                                            <h5 class="fw-semibold text-dark mb-2">

                                                Booking #${booking.id}

                                            </h5>


                                            <p class="mb-1">

                                                <strong>
                                                    Customer ID:
                                                </strong>

                                                ${booking.customer.customerID}

                                            </p>


                                            <p class="mb-1">

                                                <strong>
                                                    Customer:
                                                </strong>

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

                                            </p>


                                            <p class="mb-1">

                                                <strong>
                                                    Booking Date:
                                                </strong>

                                                ${booking.bookingDate}

                                            </p>


                                            <p class="mb-1">

                                                <strong>
                                                    Status:
                                                </strong>

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
                                                            ${booking.status}
                                                        </span>

                                                    </c:otherwise>

                                                </c:choose>

                                            </p>


                                            <c:choose>

                                                <c:when test="${not empty booking.details}">

                                                    <div class="mt-3">

                                                        <strong>
                                                            Room Details:
                                                        </strong>

                                                        <c:forEach
                                                            var="detail"
                                                            items="${booking.details}">

                                                            <div
                                                                class="border
                                                                       rounded-3
                                                                       p-2
                                                                       mt-2">

                                                                <p class="mb-1">

                                                                    <strong>
                                                                        Room:
                                                                    </strong>

                                                                    <a
                                                                        href="${pageContext.request.contextPath}/details?roomNumber=${detail.roomNumber}"
                                                                        class="text-black fw-bold">

                                                                        #${detail.roomNumber}

                                                                    </a>

                                                                </p>


                                                                <p class="mb-1">

                                                                    <strong>
                                                                        Check-in:
                                                                    </strong>

                                                                    ${detail.checkInDate}

                                                                </p>


                                                                <p class="mb-1">

                                                                    <strong>
                                                                        Check-out:
                                                                    </strong>

                                                                    ${detail.checkOutDate}

                                                                </p>


                                                                <p class="mb-1">

                                                                    <strong>
                                                                        Guests:
                                                                    </strong>

                                                                    ${detail.guestCount}

                                                                </p>


                                                                <p class="mb-1">

                                                                    <strong>
                                                                        Price / Night:
                                                                    </strong>

                                                                    $${detail.pricePerNight}

                                                                </p>


                                                                <c:if test="${not empty detail.specialRequest}">

                                                                    <p class="mb-1">

                                                                        <strong>
                                                                            Special Request:
                                                                        </strong>

                                                                        <c:out value="${detail.specialRequest}"/>

                                                                    </p>

                                                                </c:if>

                                                            </div>

                                                        </c:forEach>

                                                    </div>

                                                </c:when>


                                                <c:otherwise>

                                                    <p class="text-muted mt-2 mb-0">

                                                        No room information available.

                                                    </p>

                                                </c:otherwise>

                                            </c:choose>

                                        </div>


                                        <div class="text-md-end mt-3 mt-md-0">

                                            <p class="text-muted mb-1">

                                                Total Price

                                            </p>

                                            <p
                                                class="fw-bold
                                                       fs-5
                                                       text-success
                                                       mb-2">

                                                $${booking.totalPrice}

                                            </p>

                                        </div>

                                    </div>

                                </c:forEach>

                            </div>

                        </c:when>


                        <c:otherwise>

                            <div class="alert alert-info text-center mt-5">

                                There are no bookings yet...

                            </div>

                        </c:otherwise>

                    </c:choose>


                    <c:if test="${requestScope.totalPages > 1}">

                        <nav
                            aria-label="Page navigation"
                            class="d-flex
                                   w-100
                                   justify-content-center
                                   mt-4">

                            <ul class="pagination">


                                <li
                                    class="page-item
                                           ${currentPage == 1
                                           ? 'disabled'
                                           : ''}">

                                    <a
                                        class="page-link"
                                        href="${pageContext.request.contextPath}/reports?view=bookings&page-index=${currentPage - 1}">

                                        Previous

                                    </a>

                                </li>


                                <c:forEach
                                    var="i"
                                    begin="1"
                                    end="${totalPages}">

                                    <li
                                        class="page-item
                                               ${i == currentPage
                                               ? 'active'
                                               : ''}">

                                        <a
                                            class="page-link"
                                            href="${pageContext.request.contextPath}/reports?view=bookings&page-index=${i}">

                                            ${i}

                                        </a>

                                    </li>

                                </c:forEach>


                                <li
                                    class="page-item
                                           ${currentPage == totalPages
                                           ? 'disabled'
                                           : ''}">

                                    <a
                                        class="page-link"
                                        href="${pageContext.request.contextPath}/reports?view=bookings&page-index=${currentPage + 1}">

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