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

    <title>Cancel Booking</title>

    <link
        href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
        rel="stylesheet">

    <link
        href="<%= request.getContextPath()%>/assets/css/general.css"
        rel="stylesheet">

</head>

<body class="bg-light">

<div class="body-wrapper">

    <%@include file="/WEB-INF/include/header.jsp" %>


    <main
        class="min-vh-50
               d-flex
               align-items-center
               justify-content-center">


        <div
            class="shadow rounded-4
                   bg-white p-5"
            style="max-width: 700px; width: 100%;">


            <form
                action="${pageContext.request.contextPath}/history/cancel-booking"
                method="post">


                <h2 class="mb-4 text-center">

                    Cancel Booking

                </h2>


                <div
                    class="alert alert-warning">

                    Cancelling a booking does not delete
                    its history.

                    The booking will be marked as
                    <strong>cancelled</strong>.

                </div>


                <div class="mb-4">

                    <p class="fs-5 mb-2">

                        <strong>Booking ID:</strong>

                        ${requestScope.booking.id}

                    </p>


                    <p class="mb-2">

                        <strong>Status:</strong>

                        ${requestScope.booking.status}

                    </p>


                    <p class="mb-2">

                        <strong>Total Price:</strong>

                        $${requestScope.booking.totalPrice}

                    </p>

                </div>


                <!-- ========================================= -->
                <!-- ROOM DETAILS                              -->
                <!-- ========================================= -->

                <c:if test="${not empty requestScope.booking.details}">

                    <h5 class="mb-3">
                        Stay Details
                    </h5>


                    <c:forEach
                        var="detail"
                        items="${requestScope.booking.details}">


                        <div
                            class="border rounded-3
                                   p-3 mb-3">


                            <p class="mb-1">

                                <strong>Room:</strong>

                                #${detail.roomNumber}

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


                            <p class="mb-0">

                                <strong>
                                    Guests:
                                </strong>

                                ${detail.guestCount}

                            </p>


                        </div>

                    </c:forEach>

                </c:if>


                <!-- ========================================= -->
                <!-- CANCELLATION REASON                       -->
                <!-- ========================================= -->

                <div class="mb-4">

                    <label
                        for="reason"
                        class="form-label fw-semibold">

                        Cancellation Reason
                        <span class="text-muted">
                            (optional)
                        </span>

                    </label>


                    <textarea
                        id="reason"
                        name="reason"
                        class="form-control"
                        rows="4"
                        maxlength="500"
                        placeholder="Please tell us why you want to cancel..."></textarea>

                </div>


                <input
                    type="hidden"
                    name="id"
                    value="${requestScope.booking.id}">


                <!-- ========================================= -->
                <!-- ACTIONS                                   -->
                <!-- ========================================= -->

                <div
                    class="d-flex
                           justify-content-center
                           gap-3">


                    <button
                        type="submit"
                        class="btn btn-danger
                               btn-lg px-4">

                        Confirm Cancellation

                    </button>


                    <a
                        href="${pageContext.request.contextPath}/history?id=${sessionScope.customer.customerID}&view=bookings"
                        class="btn btn-secondary
                               btn-lg px-4">

                        Back

                    </a>


                </div>


            </form>

        </div>

    </main>


    <%@include file="/WEB-INF/include/footer.jsp" %>

</div>


<script
    src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js">
</script>

</body>

</html>