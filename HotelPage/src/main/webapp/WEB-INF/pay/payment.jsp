<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <meta
        http-equiv="Content-Type"
        content="text/html; charset=UTF-8">

    <title data-i18n="screen.7048f38b33f135df">${requestScope.i18n['screen.7048f38b33f135df']}</title>

    <link
        href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
        rel="stylesheet">

    <link
        href="<%= request.getContextPath()%>/assets/css/general.css"
        rel="stylesheet">

        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
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

                <ui:text key="screen.ed291f171aa86115"/>

            </h2>


            <!-- ========================================= -->
            <!-- ERRORS                                    -->
            <!-- ========================================= -->

            <c:if test="${param.error == 'invalid-discount'}">

                <div
                    class="alert
                           alert-danger
                           text-center">

                    <ui:text key="screen.977c66234640a8b2"/>

                </div>

            </c:if>


            <c:if test="${param.error == 'invalid-method'}">

                <div
                    class="alert
                           alert-danger
                           text-center">

                    <ui:text key="screen.771f01602f932fba"/>

                </div>

            </c:if>


            <c:if test="${param.error == 'payment-failed'}">

                <div
                    class="alert
                           alert-danger
                           text-center">

                    <ui:text key="screen.f0bb321462d05aa0"/>

                </div>

            </c:if>


            <c:if test="${param.error == 'invalid-payment-percent'}">

                <div class="alert alert-danger text-center">
                    <ui:text key="screen.5269fce2b442cd48"/>
                </div>

            </c:if>


            <!-- ========================================= -->
            <!-- BOOKING SUMMARY                           -->
            <!-- ========================================= -->

            <div class="mb-4">

                <h5>
                    <ui:text key="screen.8e9d3e1467f7cc3f"/>${booking.id}
                </h5>


                <p class="mb-1">

                    <strong><ui:text key="screen.755c8b2a9fb11446"/></strong>

                    <ui:message text="${booking.status}"/>

                </p>


                <p class="mb-0">

                    <strong><ui:text key="screen.f981e52aa5f5e667"/></strong>

                    <span
                        class="text-primary
                               fw-bold">

                        $${booking.totalPrice}

                    </span>

                </p>

                <p class="mb-1 mt-2"><strong><ui:text key="screen.a2e1944f944f0e3b"/></strong> $${paidAmount}</p>
                <p class="mb-0"><strong><ui:text key="screen.b7adb0e79057b084"/></strong> <span class="text-danger fw-bold">$${remainingAmount}</span></p>

            </div>


            <!-- ========================================= -->
            <!-- BOOKING DETAILS                           -->
            <!-- ========================================= -->

            <h5 class="mb-3">
                <ui:text key="screen.6816c8a3445d0f73"/>
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
                                <ui:text key="screen.93853234d7ebd9b2"/>
                            </strong>

                            #${detail.roomNumber}

                        </div>


                        <div class="col-md-6">

                            <strong>
                                <ui:text key="screen.1ac580f9fde7374f"/>
                            </strong>

                            ${detail.guestCount}

                        </div>


                        <div class="col-md-6">

                            <strong>
                                <ui:text key="screen.a44e12f354688caf"/>
                            </strong>

                            ${fn:substring(detail.checkInDate, 8, 10)}/${fn:substring(detail.checkInDate, 5, 7)}/${fn:substring(detail.checkInDate, 0, 4)}

                        </div>


                        <div class="col-md-6">

                            <strong>
                                <ui:text key="screen.865998541ea4eba7"/>
                            </strong>

                            ${fn:substring(detail.checkOutDate, 8, 10)}/${fn:substring(detail.checkOutDate, 5, 7)}/${fn:substring(detail.checkOutDate, 0, 4)}

                        </div>


                        <div class="col-md-12">

                            <strong>
                                <ui:text key="screen.89ae1c4ac3ed0966"/>
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


                <c:if test="${firstPayment}">
                    <div class="mb-4">
                        <label for="payment-percent" class="form-label fw-semibold"><ui:text key="screen.8312d6d815743896"/></label>
                        <select id="payment-percent" name="payment-percent" class="form-select form-select-lg" required ${selectedPercent != null ? 'disabled' : ''}>
                            <option value="10" ${selectedPercent == 10 ? 'selected' : ''}>10%</option>
                            <option value="20" ${selectedPercent == 20 ? 'selected' : ''}>20%</option>
                            <option value="100" ${selectedPercent == 100 ? 'selected' : ''}>100%</option>
                        </select>
                        <c:if test="${selectedPercent != null}">
                            <input type="hidden" name="payment-percent" value="${selectedPercent}">
                        </c:if>
                        <div class="form-text">${selectedPercent != null ? 'Payment percentage was selected during booking.' : 'This is the first payment for the booking.'}</div>
                    </div>
                </c:if>

                <c:if test="${!firstPayment}">
                    <div class="alert alert-info">
                        <ui:text key="screen.fbd43a17fae70f8c"/> <strong>$${remainingAmount}</strong>.
                    </div>
                </c:if>

                <!-- Payment Method -->

                <div class="mb-4">

                    <label
                        for="payment-method"
                        class="form-label
                               fw-semibold">

                        <ui:text key="screen.f89a4e17557766ec"/>

                    </label>


                    <select
                        id="payment-method"
                        name="payment-method"
                        class="form-select
                               form-select-lg"
                        required>


                        <option value="" data-i18n="screen.f9b3d69c6c703742">
                            ${requestScope.i18n['screen.f9b3d69c6c703742']}
                        </option>


                        <option value="cash" data-i18n="screen.e2da108f63008e44">
                            ${requestScope.i18n['screen.e2da108f63008e44']}
                        </option>


                        <option value="bank_transfer" data-i18n="screen.ee82ae2801cf9097">
                            ${requestScope.i18n['screen.ee82ae2801cf9097']}
                        </option>


                        <option value="credit_card" data-i18n="screen.9f629f57b7d02936">
                            ${requestScope.i18n['screen.9f629f57b7d02936']}
                        </option>


                        <option value="e_wallet" data-i18n="screen.f7bb9a1dcdb515df">
                            ${requestScope.i18n['screen.f7bb9a1dcdb515df']}
                        </option>


                    </select>

                </div>


                <c:if test="${firstPayment}">

                <!-- Discount -->

                <div class="mb-4">

                    <label
                        for="discount-code"
                        class="form-label
                               fw-semibold">

                        <ui:text key="screen.fd947a841a798fb7"/>
                        <span class="text-muted">
                            <ui:text key="screen.0059798b7f7023e4"/>
                        </span>

                    </label>


                    <input
                        type="text"
                        id="discount-code"
                        name="discount-code"
                        class="form-control
                               form-control-lg"
                        maxlength="50"
                        placeholder="${requestScope.i18n['screen.b7ee37d374ce6a5c']}" data-i18n-placeholder="screen.b7ee37d374ce6a5c">

                </div>


                <!-- Active Promotions -->

                <c:if test="${not empty promotions}">

                    <div
                        class="border
                               rounded-3
                               p-3
                               mb-4">


                        <h6 class="fw-bold mb-3">

                            <ui:text key="screen.6a85de18585c99f5"/>

                        </h6>


                        <c:forEach
                            var="promotion"
                            items="${promotions}">


                            <div class="mb-2">

                                <strong>
                                    ${promotion.code}
                                </strong>

                                -
                                ${promotion.saleOff}<ui:text key="screen.5e0aa53f41a26fe8"/>


                                <c:if test="${promotion.minimumAmount != null}">

                                    <ui:text key="screen.97953ebdb79279c3"/>${promotion.minimumAmount}

                                </c:if>


                                <c:if test="${promotion.maximumDiscount != null}">

                                    <ui:text key="screen.f2f07057ca60ce74"/>${promotion.maximumDiscount}

                                </c:if>


                            </div>

                        </c:forEach>


                    </div>

                </c:if>

                </c:if>


                <div
                    class="alert
                           alert-info">

                    <ui:text key="screen.58cd661919761e50"/>

                </div>


                <button
                    type="submit"
                    class="btn
                           btn-lg
                           w-100
                           text-white"
                    style="background-color: #b29575;">

                    <c:choose><c:when test="${firstPayment}"><ui:text key="screen.8c907a591a8e3612"/></c:when><c:otherwise><ui:text key="screen.cb8be35a87c097ab"/></c:otherwise></c:choose>

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