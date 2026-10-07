<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

    <head>

        <meta http-equiv="Content-Type"
              content="text/html; charset=UTF-8">

        <title>Payment History</title>

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

                <div class="container py-5">

                    <h2 class="mb-4 text-center">
                        Payment History
                    </h2>


                    <c:choose>

                        <c:when test="${not empty payments}">

                            <div class="table-responsive">

                                <table
                                    class="table
                                    table-bordered
                                    table-hover
                                    align-middle">

                                    <thead class="table-dark">

                                        <tr>

                                            <th>
                                                Payment ID
                                            </th>

                                            <th>
                                                Booking
                                            </th>

                                            <th>
                                                Room / Stay
                                            </th>

                                            <th>
                                                Original Total
                                            </th>

                                            <th>
                                                Paid Amount
                                            </th>

                                            <th>
                                                Method
                                            </th>

                                            <th>
                                                Status
                                            </th>

                                            <th>
                                                Discount
                                            </th>

                                            <th>
                                                Transaction
                                            </th>

                                            <th>
                                                Payment Date
                                            </th>

                                        </tr>

                                    </thead>


                                    <tbody>

                                        <c:forEach
                                            var="payment"
                                            items="${payments}">

                                            <tr>

                                                <!-- Payment ID -->
                                                <td>

                                                    ${payment.id}

                                                </td>


                                                <!-- Booking -->
                                                <td>

                                                    #${payment.booking.id}

                                                    <br>

                                                    <small class="text-muted">

                                                        ${payment.booking.status}

                                                    </small>

                                                </td>


                                                <!-- Room / Stay -->
                                                <td>

                                                    <c:forEach
                                                        var="detail"
                                                        items="${payment.booking.details}">

                                                        <div
                                                            class="mb-2">

                                                            <strong>
                                                                Room
                                                                #${detail.roomNumber}
                                                            </strong>

                                                            <br>

                                                            <small>

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

                                                </td>


                                                <!-- Original Booking Total -->
                                                <td>

                                                    $${payment.booking.totalPrice}

                                                </td>


                                                <!-- Actual Paid Amount -->
                                                <td>

                                                    <strong
                                                        class="text-success">

                                                        $${payment.amount}

                                                    </strong>

                                                </td>


                                                <!-- Payment Method -->
                                                <td>

                                                    <c:choose>

                                                        <c:when test="${payment.paymentMethod == 'cash'}">
                                                            Cash
                                                        </c:when>

                                                        <c:when test="${payment.paymentMethod == 'bank_transfer'}">
                                                            Bank Transfer
                                                        </c:when>

                                                        <c:when test="${payment.paymentMethod == 'credit_card'}">
                                                            Credit Card
                                                        </c:when>

                                                        <c:when test="${payment.paymentMethod == 'e_wallet'}">
                                                            E-Wallet
                                                        </c:when>

                                                        <c:otherwise>

                                                            ${payment.paymentMethod}

                                                        </c:otherwise>

                                                    </c:choose>

                                                </td>


                                                <!-- Payment Status -->
                                                <td>

                                                    <c:choose>

                                                        <c:when test="${payment.paymentStatus == 'paid'}">

                                                            <span
                                                                class="badge
                                                                bg-success">

                                                                Paid

                                                            </span>

                                                        </c:when>


                                                        <c:when test="${payment.paymentStatus == 'pending'}">

                                                            <span
                                                                class="badge
                                                                bg-warning
                                                                text-dark">

                                                                Pending

                                                            </span>

                                                        </c:when>


                                                        <c:when test="${payment.paymentStatus == 'failed'}">

                                                            <span
                                                                class="badge
                                                                bg-danger">

                                                                Failed

                                                            </span>

                                                        </c:when>


                                                        <c:when test="${payment.paymentStatus == 'refunded'}">

                                                            <span
                                                                class="badge
                                                                bg-secondary">

                                                                Refunded

                                                            </span>

                                                        </c:when>


                                                        <c:otherwise>

                                                            ${payment.paymentStatus}

                                                        </c:otherwise>

                                                    </c:choose>

                                                </td>


                                                <!-- Discount -->
                                                <td>

                                                    <c:choose>

                                                        <c:when test="${payment.discount != null}">

                                                            <strong>

                                                                ${payment.discount.code}

                                                            </strong>

                                                            <br>

                                                            ${payment.discount.saleOff}%

                                                        </c:when>


                                                        <c:otherwise>

                                                            No Discount

                                                        </c:otherwise>

                                                    </c:choose>

                                                </td>


                                                <!-- Transaction Code -->
                                                <td>

                                                    <c:choose>

                                                        <c:when test="${not empty payment.transactionCode}">

                                                            ${payment.transactionCode}

                                                        </c:when>

                                                        <c:otherwise>

                                                            -

                                                        </c:otherwise>

                                                    </c:choose>

                                                </td>


                                                <!-- Payment Date -->
                                                <td>

                                                    ${payment.paymentDate}

                                                </td>

                                            </tr>

                                        </c:forEach>

                                    </tbody>

                                </table>

                            </div>


                            <!-- ===================================== -->
                            <!-- PAGINATION                            -->
                            <!-- ===================================== -->

                            <c:if test="${totalPages > 1}">

                                <nav
                                    aria-label="Payment history pagination"
                                    class="d-flex
                                    w-100
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
                                                href="${pageContext.request.contextPath}/history?id=${requestScope.historyCustomerId}&view=payments&page=${currentPage - 1}">

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
                                                    href="${pageContext.request.contextPath}/history?id=${requestScope.historyCustomerId}&view=payments&page=${i}">

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
                                                href="${pageContext.request.contextPath}/history?id=${requestScope.historyCustomerId}&view=payments&page=${currentPage + 1}">

                                                Next

                                            </a>

                                        </li>

                                    </ul>

                                </nav>

                            </c:if>

                        </c:when>


                        <c:otherwise>

                            <div
                                class="alert
                                alert-info
                                text-center">

                                No payment records found.

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