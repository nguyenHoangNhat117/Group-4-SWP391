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

    <title data-i18n="screen.0927faa59b0c912b">${requestScope.i18n['screen.0927faa59b0c912b']}</title>

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
                        <ui:text key="screen.80843b921c352858"/> ${requestScope.historyCustomerId}<ui:text key="screen.55b4461e69978451"/>
                    </h2>

                </c:when>

                <c:otherwise>

                    <h2 class="fw-bold text-center mb-4">
                        <ui:text key="screen.b997c2047fd25834"/>
                    </h2>

                </c:otherwise>

            </c:choose>


            <!-- ================================================= -->
            <!-- MESSAGES                                          -->
            <!-- ================================================= -->

            <c:if test="${param.message == 'review-unavailable'}">
                <div class="alert alert-warning text-center"><ui:text key="screen.649d935afa6545db"/></div>
            </c:if>

            <c:if test="${param.message == 'cancel-success'}">

                <div class="alert alert-success text-center">

                    <ui:text key="screen.294dc5d4dd39aedc"/>

                </div>

            </c:if>


            <c:if test="${param.message == 'cannot-cancel'}">

                <div class="alert alert-danger text-center">

                    <ui:text key="screen.bb5b5d49c340039d"/>

                </div>

            </c:if>


            <c:if test="${param.message == 'invalid-booking'}">

                <div class="alert alert-danger text-center">

                    <ui:text key="screen.9843834fdbc59ec7"/>

                </div>

            </c:if>


            <c:if test="${param.message == 'booking-not-found'}">

                <div class="alert alert-danger text-center">

                    <ui:text key="screen.8c3d35a16594da92"/>

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
                                    <th scope="col"><ui:text key="screen.948e03e7054b919e"/></th>
                                    <th scope="col"><ui:text key="screen.011519b48fd7ae0a"/></th>
                                    <th scope="col"><ui:text key="screen.e5ba03fca518b20d"/></th>
                                    <th scope="col"><ui:text key="screen.c9b3c38247f744e1"/></th>
                                    <th scope="col"><ui:text key="screen.ea50259543e17999"/></th>
                                    <th scope="col"><ui:text key="screen.7048f38b33f135df"/></th>
                                    <th scope="col" class="text-end"><ui:text key="screen.ff8059dc6752afdd"/></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="booking" items="${requestScope.bookings}">
                                    <tr>
                                        <td class="fw-semibold">#${booking.id}</td>
                                        <td>${fn:substring(booking.bookingDate, 8, 10)}/${fn:substring(booking.bookingDate, 5, 7)}/${fn:substring(booking.bookingDate, 0, 4)}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty booking.details}">
                                                    ${fn:substring(booking.details[0].checkInDate, 8, 10)}/${fn:substring(booking.details[0].checkInDate, 5, 7)}/${fn:substring(booking.details[0].checkInDate, 0, 4)}
                                                    <span class="text-muted"><ui:text key="screen.663ea1bfffe5038f"/></span>
                                                    ${fn:substring(booking.details[0].checkOutDate, 8, 10)}/${fn:substring(booking.details[0].checkOutDate, 5, 7)}/${fn:substring(booking.details[0].checkOutDate, 0, 4)}
                                                </c:when>
                                                <c:otherwise><span class="text-muted"><ui:text key="screen.67a926f7008e2b0e"/></span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="fw-bold text-success text-nowrap">$${booking.totalPrice}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${booking.status == 'pending'}"><span class="badge bg-warning text-dark"><ui:text key="screen.331551b0de4157c9"/></span></c:when>
                                                <c:when test="${booking.status == 'confirmed'}"><span class="badge bg-primary"><ui:text key="screen.fe00b67b6dd1143f"/></span></c:when>
                                                <c:when test="${booking.status == 'cancelled'}"><span class="badge bg-danger"><ui:text key="screen.d353a99eb4556847"/></span></c:when>
                                                <c:when test="${booking.status == 'checked_in'}"><span class="badge bg-info text-dark"><ui:text key="screen.17c05c08a9ac3e5a"/></span></c:when>
                                                <c:when test="${booking.status == 'checked_out'}"><span class="badge bg-secondary"><ui:text key="screen.cbe2b33735c6335c"/></span></c:when>
                                                <c:when test="${booking.status == 'completed'}"><span class="badge bg-success"><ui:text key="screen.22a970d2e5b1cc23"/></span></c:when>
                                                <c:otherwise><span class="badge bg-secondary"><ui:message text="${booking.status}"/></span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:set var="paidAmount" value="${paidAmountMap[booking.id]}" />
                                            <c:set var="remainingAmount" value="${remainingAmountMap[booking.id]}" />
                                            <c:choose>
                                                <c:when test="${booking.status == 'cancelled' && refundedMap[booking.id]}">
                                                    <span class="badge bg-success"><ui:text key="screen.c797239908d33b97"/></span>
                                                    <div class="small text-muted mt-1"><ui:text key="screen.ace7885e1829b09f"/></div>
                                                </c:when>
                                                <c:when test="${booking.status == 'cancelled'}">
                                                    <span class="badge bg-secondary"><ui:text key="screen.ae4dab10261262ba"/></span>
                                                    <div class="small text-muted mt-1"><ui:text key="screen.0a8c6fcde356d1af"/></div>
                                                </c:when>
                                                <c:when test="${empty paidAmount || paidAmount <= 0}">
                                                    <span class="badge bg-warning text-dark"><ui:text key="screen.4b80e74f3fa47867"/></span>
                                                </c:when>
                                                <c:when test="${remainingAmount <= 0}">
                                                    <span class="badge bg-success"><ui:text key="screen.ba9054bc0354df6e"/></span>
                                                    <div class="small text-muted mt-1"><ui:text key="screen.52d848993f1b098b"/>${paidAmount}</div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-info text-dark"><ui:text key="screen.1390db305695537e"/></span>
                                                    <div class="small text-muted mt-1"><ui:text key="screen.52d848993f1b098b"/>${paidAmount}<br><ui:text key="screen.86e401cbd3861223"/>${remainingAmount}</div>
                                                </c:otherwise>
                                            </c:choose>

                                        </td>
                                        <td class="text-end text-nowrap">
                                            <button class="btn btn-outline-primary btn-sm"
                                                    type="button"
                                                    data-bs-toggle="collapse"
                                                    data-bs-target="#booking-details-${booking.id}"
                                                    aria-expanded="false"
                                                    aria-controls="booking-details-${booking.id}">
                                                <ui:text key="screen.90789c12d0731b44"/>
                                            </button>
                                        </td>
                                    </tr>
                                    <tr class="table-light">
                                        <td colspan="7" class="p-0 border-0">
                                            <div class="collapse" id="booking-details-${booking.id}">
                                                <div class="p-3 p-md-4 border-bottom">
                                                    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                                                        <div>
                                                            <h5 class="mb-2"><ui:text key="screen.8e9d3e1467f7cc3f"/>${booking.id} <ui:text key="screen.45989de49fb7f66d"/></h5>
                                                            <p class="mb-1"><strong><ui:text key="screen.bf6899307a899450"/></strong> ${fn:substring(booking.bookingDate, 8, 10)}/${fn:substring(booking.bookingDate, 5, 7)}/${fn:substring(booking.bookingDate, 0, 4)}</p>
                                                            <c:if test="${not empty booking.specialRequest}">
                                                                <p class="mb-1"><strong><ui:text key="screen.906fb211c5e1ee2e"/></strong> ${booking.specialRequest}</p>
                                                            </c:if>
                                                            <c:if test="${booking.status == 'cancelled'}">
                                                                <p class="mb-1"><strong><ui:text key="screen.873135e90ec6e045"/></strong> ${fn:substring(booking.cancellationDate, 8, 10)}/${fn:substring(booking.cancellationDate, 5, 7)}/${fn:substring(booking.cancellationDate, 0, 4)}</p>
                                                                <c:if test="${not empty booking.cancellationReason}">
                                                                    <p class="mb-1"><strong><ui:text key="screen.821b8e24bc1baa29"/></strong> ${booking.cancellationReason}</p>
                                                                </c:if>
                                                            </c:if>
                                                        </div>
                                                        <div class="text-md-end">
                                                            <div class="text-muted small"><ui:text key="screen.9da7a94f088e6a33"/></div>
                                                            <div class="fw-bold fs-5 text-success">$${booking.totalPrice}</div>
                                                        </div>
                                                    </div>

                                                    <h6 class="fw-bold mb-3"><ui:text key="screen.6816c8a3445d0f73"/></h6>
                                                    <c:forEach var="detail" items="${booking.details}">
                                                        <div class="card mb-2">
                                                            <div class="card-body">
                                                                <div class="row g-3">
                                                                    <div class="col-12 col-md-6">
                                                                        <p class="mb-1"><strong><ui:text key="screen.93853234d7ebd9b2"/></strong>
                                                                            <a href="${pageContext.request.contextPath}/details?roomNumber=${detail.roomNumber}">#${detail.roomNumber}</a>
                                                                        </p>
                                                                        <p class="mb-1"><strong><ui:text key="screen.a44e12f354688caf"/></strong> ${fn:substring(detail.checkInDate, 8, 10)}/${fn:substring(detail.checkInDate, 5, 7)}/${fn:substring(detail.checkInDate, 0, 4)}</p>
                                                                        <p class="mb-1"><strong><ui:text key="screen.865998541ea4eba7"/></strong> ${fn:substring(detail.checkOutDate, 8, 10)}/${fn:substring(detail.checkOutDate, 5, 7)}/${fn:substring(detail.checkOutDate, 0, 4)}</p>
                                                                    </div>
                                                                    <div class="col-12 col-md-6">
                                                                        <p class="mb-1"><strong><ui:text key="screen.fe89253ac2b97036"/></strong> ${detail.guestCount}</p>
                                                                        <p class="mb-1"><strong><ui:text key="screen.5721f9a3c415ad9e"/></strong> $${detail.pricePerNight}</p>
                                                                        <c:if test="${not empty detail.specialRequest}">
                                                                            <p class="mb-0"><strong><ui:text key="screen.d0a5e5a4c95e9e12"/></strong> ${detail.specialRequest}</p>
                                                                        </c:if>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </c:forEach>

                                                    <c:if test="${loggedUser.role == 'customer'}">
                                                        <div class="d-flex justify-content-end flex-wrap gap-2 mt-3">
                            <c:if test="${booking.status != 'cancelled'
                                          && remainingAmountMap[booking.id] > 0}">
                                <form action="${pageContext.request.contextPath}/payment" method="get" class="m-0">
                                    <input type="hidden" name="bookingId" value="${booking.id}">
                                    <button class="btn btn-success btn-sm" type="submit">
                                        <c:choose>
                                            <c:when test="${paidAmountMap[booking.id] > 0}"><ui:text key="screen.4c84ed0cee20c477"/></c:when>
                                            <c:otherwise><ui:text key="screen.42a327fadf7f52c2"/></c:otherwise>
                                        </c:choose>
                                    </button>
                                </form>
                            </c:if>
                            <c:if test="${booking.status == 'pending' || booking.status == 'confirmed'
                                          || booking.status == 'checked_in'}">
                                <c:choose>
                                    <c:when test="${cancellableMap[booking.id]}">
                                        <a href="${pageContext.request.contextPath}/history/cancel-booking?id=${booking.id}"
                                           class="btn btn-danger btn-sm"><ui:text key="screen.dcd0884db34dcece"/></a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="d-inline-block"
                                              title="${requestScope.i18n['screen.b0ebf4bfbb10b71b']}" data-i18n-title="screen.b0ebf4bfbb10b71b">
                                            <button type="button" class="btn btn-outline-danger btn-sm" disabled><ui:text key="screen.dcd0884db34dcece"/></button>
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <c:if test="${booking.status == 'completed' && not empty booking.details}">
                                <a href="${pageContext.request.contextPath}/review?booking-id=${booking.id}&amp;room-number=${booking.details[0].roomNumber}"
                                   class="btn btn-outline-primary btn-sm"><ui:text key="screen.aff0766a5290e117"/></a>
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
                            aria-label="${requestScope.i18n['screen.a7f23a9c7133cfc6']}"
                            class="d-flex
                                   justify-content-center
                                   mt-4" data-i18n-aria-label="screen.a7f23a9c7133cfc6">


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

                                        <ui:text key="screen.a57b08a480b822a0"/>

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

                                        <ui:text key="screen.1ff57a29d7c9d11b"/>

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

                                <ui:text key="screen.d7e44afe879f09b9"/>

                            </c:when>

                            <c:otherwise>

                                <ui:text key="screen.7f7bf65f662348a7"/>

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