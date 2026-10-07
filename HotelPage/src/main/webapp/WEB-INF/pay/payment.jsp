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

    <title>Payment</title>

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
        class="d-flex
               justify-content-center
               align-items-center
               py-5"
        style="min-height: 90vh;">


        <div
            class="bg-white
                   shadow
                   rounded-4
                   p-5"
            style="width: 100%; max-width: 720px;">


            <h2 class="text-center mb-4">

                Confirm Your Payment

            </h2>


            <!-- ========================================= -->
            <!-- ERRORS                                    -->
            <!-- ========================================= -->

            <c:if test="${param.error == 'invalid-discount'}">

                <div
                    class="alert
                           alert-danger
                           text-center">

                    Discount code is invalid,
                    expired, inactive, exhausted,
                    or does not satisfy the
                    minimum booking amount.

                </div>

            </c:if>


            <c:if test="${param.error == 'invalid-method'}">

                <div
                    class="alert
                           alert-danger
                           text-center">

                    Please select a valid
                    payment method.

                </div>

            </c:if>


            <c:if test="${param.error == 'payment-failed'}">

                <div
                    class="alert
                           alert-danger
                           text-center">

                    Payment could not be completed.
                    Please try again.

                </div>

            </c:if>


            <!-- ========================================= -->
            <!-- BOOKING SUMMARY                           -->
            <!-- ========================================= -->

            <div class="mb-4">

                <h5>
                    Booking #${booking.id}
                </h5>


                <p class="mb-1">

                    <strong>Status:</strong>

                    ${booking.status}

                </p>


                <p class="mb-0">

                    <strong>Original Total:</strong>

                    <span
                        class="text-primary
                               fw-bold">

                        $${booking.totalPrice}

                    </span>

                </p>

            </div>


            <!-- ========================================= -->
            <!-- BOOKING DETAILS                           -->
            <!-- ========================================= -->

            <h5 class="mb-3">
                Room Details
            </h5>


            <c:forEach
                var="detail"
                items="${booking.details}">


                <div
                    class="border
                           rounded-3
                           p-3
                           mb-3">


                    <div
                        class="row
                               g-2">


                        <div class="col-md-6">

                            <strong>
                                Room:
                            </strong>

                            #${detail.roomNumber}

                        </div>


                        <div class="col-md-6">

                            <strong>
                                Guests:
                            </strong>

                            ${detail.guestCount}

                        </div>


                        <div class="col-md-6">

                            <strong>
                                Check-in:
                            </strong>

                            ${detail.checkInDate}

                        </div>


                        <div class="col-md-6">

                            <strong>
                                Check-out:
                            </strong>

                            ${detail.checkOutDate}

                        </div>


                        <div class="col-md-12">

                            <strong>
                                Price per night:
                            </strong>

                            $${detail.pricePerNight}

                        </div>


                    </div>

                </div>

            </c:forEach>


            <!-- ========================================= -->
            <!-- PAYMENT FORM                              -->
            <!-- ========================================= -->

            <form
                action="${pageContext.request.contextPath}/payment"
                method="post">


                <input
                    type="hidden"
                    name="bookingId"
                    value="${booking.id}">


                <!-- Payment Method -->

                <div class="mb-4">

                    <label
                        for="payment-method"
                        class="form-label
                               fw-semibold">

                        Payment Method

                    </label>


                    <select
                        id="payment-method"
                        name="payment-method"
                        class="form-select
                               form-select-lg"
                        required>


                        <option value="">
                            -- Select payment method --
                        </option>


                        <option value="cash">
                            Cash
                        </option>


                        <option value="bank_transfer">
                            Bank Transfer
                        </option>


                        <option value="credit_card">
                            Credit Card
                        </option>


                        <option value="e_wallet">
                            E-Wallet
                        </option>


                    </select>

                </div>


                <!-- Discount -->

                <div class="mb-4">

                    <label
                        for="discount-code"
                        class="form-label
                               fw-semibold">

                        Promotion Code
                        <span class="text-muted">
                            (optional)
                        </span>

                    </label>


                    <input
                        type="text"
                        id="discount-code"
                        name="discount-code"
                        class="form-control
                               form-control-lg"
                        maxlength="50"
                        placeholder="Enter promotion code">

                </div>


                <!-- Active Promotions -->

                <c:if test="${not empty promotions}">

                    <div
                        class="border
                               rounded-3
                               p-3
                               mb-4">


                        <h6 class="fw-bold mb-3">

                            Available Promotions

                        </h6>


                        <c:forEach
                            var="promotion"
                            items="${promotions}">


                            <div class="mb-2">

                                <strong>
                                    ${promotion.code}
                                </strong>

                                -
                                ${promotion.saleOff}% off


                                <c:if test="${promotion.minimumAmount != null}">

                                    |
                                    Minimum:
                                    $${promotion.minimumAmount}

                                </c:if>


                                <c:if test="${promotion.maximumDiscount != null}">

                                    |
                                    Maximum discount:
                                    $${promotion.maximumDiscount}

                                </c:if>


                            </div>

                        </c:forEach>


                    </div>

                </c:if>


                <div
                    class="alert
                           alert-info">

                    The final payable amount is
                    calculated on the server after
                    validating the promotion code.

                </div>


                <button
                    type="submit"
                    class="btn
                           btn-lg
                           w-100
                           text-white"
                    style="background-color: #b29575;">

                    Confirm Payment

                </button>


            </form>

        </div>

    </main>


    <%@include file="/WEB-INF/include/footer.jsp" %>

</div>


<script
    src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js">
</script>

</body>

</html>