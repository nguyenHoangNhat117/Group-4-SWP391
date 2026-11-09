<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <meta http-equiv="Content-Type"
              content="text/html; charset=UTF-8">

        <title data-i18n="screen.d4c7bc94c3af4d4f">${requestScope.i18n['screen.d4c7bc94c3af4d4f']}</title>

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

                <div class="container py-5">

                    <h2 class="mb-4 text-center">
                        <ui:text key="screen.d4c7bc94c3af4d4f"/>
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
                                                <ui:text key="screen.d9c9afd836811e5a"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.948e03e7054b919e"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.fe43f0a1373a3d68"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.6dc00412a976f94a"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.6723294ee4a8f78f"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.52a0f9b65b278850"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.920e413c7d411b61"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.cb72ec985b5f3d75"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.eec26ddd9a408449"/>
                                            </th>

                                            <th>
                                                <ui:text key="screen.10b5440f239de9cb"/>
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

                                                        <ui:message text="${payment.booking.status}"/>

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
                                                                <ui:text key="screen.6c8c6af24c9b021c"/>${detail.roomNumber}
                                                            </strong>

                                                            <br>

                                                            <small>

                                                                ${detail.checkInDate}

                                                                →

                                                                ${detail.checkOutDate}

                                                            </small>

                                                            <br>

                                                            <small>

                                                                <ui:text key="screen.1ac580f9fde7374f"/>
                                                                ${detail.guestCount}

                                                            </small>

                                                        </div>

                                                    </c:forEach>

                                                </td>


                                                <!-- Original Booking Total -->
                                                <td>

                                                    <ui:money amount="${payment.booking.totalPrice}"/>

                                                </td>


                                                <!-- Actual Paid Amount -->
                                                <td>

                                                    <strong
                                                        class="text-success">

                                                        <ui:money amount="${payment.amount}"/>

                                                    </strong>

                                                </td>


                                                <!-- Payment Method -->
                                                <td>

                                                    <c:choose>

                                                        <c:when test="${payment.paymentMethod == 'cash'}">
                                                            <ui:text key="screen.e2da108f63008e44"/>
                                                        </c:when>

                                                        <c:when test="${payment.paymentMethod == 'bank_transfer'}">
                                                            <ui:text key="screen.ee82ae2801cf9097"/>
                                                        </c:when>

                                                        <c:when test="${payment.paymentMethod == 'credit_card'}">
                                                            <ui:text key="screen.9f629f57b7d02936"/>
                                                        </c:when>

                                                        <c:when test="${payment.paymentMethod == 'e_wallet'}">
                                                            <ui:text key="screen.f7bb9a1dcdb515df"/>
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

                                                                <ui:text key="screen.fb81b961af456e5e"/>

                                                            </span>

                                                        </c:when>


                                                        <c:when test="${payment.paymentStatus == 'pending'}">

                                                            <span
                                                                class="badge
                                                                bg-warning
                                                                text-dark">

                                                                <ui:text key="screen.331551b0de4157c9"/>

                                                            </span>

                                                        </c:when>


                                                        <c:when test="${payment.paymentStatus == 'failed'}">

                                                            <span
                                                                class="badge
                                                                bg-danger">

                                                                <ui:text key="screen.031a8f0f659df890"/>

                                                            </span>

                                                        </c:when>


                                                        <c:when test="${payment.paymentStatus == 'refunded'}">

                                                            <span
                                                                class="badge
                                                                bg-secondary">

                                                                <ui:text key="screen.117f6a7cf0372277"/>

                                                            </span>

                                                        </c:when>


                                                        <c:otherwise>

                                                            <ui:message text="${payment.paymentStatus}"/>

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

                                                            <ui:text key="screen.65a4b024888139b6"/>

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
                                    aria-label="${requestScope.i18n['screen.34c35b4b93d2c51e']}"
                                    class="d-flex
                                    w-100
                                    justify-content-center
                                    mt-4" data-i18n-aria-label="screen.34c35b4b93d2c51e">

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

                                                <ui:text key="screen.1ff57a29d7c9d11b"/>

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

                                <ui:text key="screen.68a815f07fea6622"/>

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