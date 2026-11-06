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

    <title data-i18n="screen.dcd0884db34dcece">${requestScope.i18n['screen.dcd0884db34dcece']}</title>

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

                    <ui:text key="screen.dcd0884db34dcece"/>

                </h2>


               


                <div class="alert ${requestScope.refundEligible ? 'alert-success' : 'alert-info'}">
                    <c:choose>
                        <c:when test="${requestScope.refundEligible}">
                            <ui:text key="screen.146aeb6d6e108f5e"/>
                        </c:when>
                        <c:otherwise>
                            <ui:text key="screen.06e02fec35b2eb5e"/>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="mb-4">

                    <p class="fs-5 mb-2">

                        <strong><ui:text key="screen.534de687fcf21d03"/></strong>

                        ${requestScope.booking.id}

                    </p>


                    <p class="mb-2">

                        <strong><ui:text key="screen.755c8b2a9fb11446"/></strong>

                        <ui:message text="${requestScope.booking.status}"/>

                    </p>


                    <p class="mb-2">

                        <strong><ui:text key="screen.d104a659abae9ebc"/></strong>

                        $${requestScope.booking.totalPrice}

                    </p>

                </div>


                <!-- ========================================= -->
                <!-- ROOM DETAILS                              -->
                <!-- ========================================= -->

                <c:if test="${not empty requestScope.booking.details}">

                    <h5 class="mb-3">
                        <ui:text key="screen.b299518a2a1cdf5b"/>
                    </h5>


                    <c:forEach
                        var="detail"
                        items="${requestScope.booking.details}">


                        <div
                            class="border rounded-3
                                   p-3 mb-3">


                            <p class="mb-1">

                                <strong><ui:text key="screen.93853234d7ebd9b2"/></strong>

                                #${detail.roomNumber}

                            </p>


                            <p class="mb-1">

                                <strong>
                                    <ui:text key="screen.a44e12f354688caf"/>
                                </strong>

                                ${fn:substring(detail.checkInDate, 8, 10)}/${fn:substring(detail.checkInDate, 5, 7)}/${fn:substring(detail.checkInDate, 0, 4)}

                            </p>


                            <p class="mb-1">

                                <strong>
                                    <ui:text key="screen.865998541ea4eba7"/>
                                </strong>

                                ${fn:substring(detail.checkOutDate, 8, 10)}/${fn:substring(detail.checkOutDate, 5, 7)}/${fn:substring(detail.checkOutDate, 0, 4)}

                            </p>


                            <p class="mb-0">

                                <strong>
                                    <ui:text key="screen.1ac580f9fde7374f"/>
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

                        <ui:text key="screen.052aafb8ebd693e9"/>
                        <span class="text-muted">
                            <ui:text key="screen.0059798b7f7023e4"/>
                        </span>

                    </label>


                    <textarea
                        id="reason"
                        name="reason"
                        class="form-control"
                        rows="4"
                        maxlength="500"
                        placeholder="${requestScope.i18n['screen.4e158e9f92517c1a']}" data-i18n-placeholder="screen.4e158e9f92517c1a"></textarea>

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

                        <ui:text key="screen.79442059c04c095a"/>

                    </button>


                    <a
                        href="${pageContext.request.contextPath}/history?id=${sessionScope.customer.customerID}&view=bookings"
                        class="btn btn-secondary
                               btn-lg px-4">

                        <ui:text key="screen.76900f1bfd16c8d4"/>

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