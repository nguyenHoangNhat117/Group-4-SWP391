<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>

    <meta
        http-equiv="Content-Type"
        content="text/html; charset=UTF-8">

    <title data-i18n="screen.a44778c56e36d86e">${requestScope.i18n['screen.a44778c56e36d86e']}</title>

    <link
        href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
        rel="stylesheet">

    <link
        href="<%= request.getContextPath()%>/assets/css/general.css"
        rel="stylesheet">


    <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>

<body class="luxe-admin-suite luxe-admin-customers">

<div class="body-wrapper">

    <%@include file="/WEB-INF/include/header.jsp" %>


    <main>
        <div class="luxe-admin-banner" aria-label="${requestScope.i18n['screen.0e2eadf8f0d866a1']}" data-i18n-aria-label="screen.0e2eadf8f0d866a1">
          <span class="luxe-admin-banner-eyebrow">LUXE ESCAPE <span aria-hidden="true">/</span> <ui:text key="screen.7c431e4de4562299"/></span>
          <span class="luxe-admin-banner-title"><ui:text key="screen.0e2eadf8f0d866a1"/></span>
          <span class="luxe-admin-banner-art" aria-hidden="true"></span>
        </div>


        <div class="container py-5">


            <h2 class="fw-bold text-center mb-4">

                <ui:text key="screen.a44778c56e36d86e"/>

            </h2>


            <!-- ========================================= -->
            <!-- MESSAGES                                  -->
            <!-- ========================================= -->

            <c:if test="${param.message == 'invalid-data'}">

                <div class="alert alert-danger text-center">

                    <ui:text key="screen.5ac5e9b89eeea9ce"/>

                </div>

            </c:if>


            <c:if test="${param.message == 'customer-not-found'}">

                <div class="alert alert-danger text-center">

                    <ui:text key="screen.9fe4ec40d1dfeb7b"/>

                </div>

            </c:if>


            <!-- ========================================= -->
            <!-- SEARCH / FILTER                           -->
            <!-- ========================================= -->

            <form
                action="${pageContext.request.contextPath}/customers"
                method="get"
                class="row g-3 mb-4">


                <input
                    type="hidden"
                    name="view"
                    value="list">


                <!-- Search -->

                <div class="col-md-7">

                    <label
                        for="keyword"
                        class="form-label fw-semibold">

                        <ui:text key="screen.91caf622ea92e81b"/>

                    </label>


                    <input
                        type="text"
                        id="keyword"
                        name="keyword"
                        class="form-control"
                        value="${keyword}"
                        placeholder="${requestScope.i18n['screen.54738f70a52dbbf3']}" data-i18n-placeholder="screen.54738f70a52dbbf3">

                </div>


                <!-- Status -->

                <div class="col-md-3">

                    <label
                        for="status"
                        class="form-label fw-semibold">

                        <ui:text key="screen.dafb01abaf3dd0c7"/>

                    </label>


                    <select
                        id="status"
                        name="status"
                        class="form-select">


                        <option value="" data-i18n="screen.05f567125eb09680">
                            ${requestScope.i18n['screen.05f567125eb09680']}
                        </option>


                        <option
                            value="active"
                            ${selectedStatus == 'active'
                              ? 'selected'
                              : ''} data-i18n="screen.92340695899bd2d8">

                            ${requestScope.i18n['screen.92340695899bd2d8']}

                        </option>


                        <option
                            value="locked"
                            ${selectedStatus == 'locked'
                              ? 'selected'
                              : ''} data-i18n="screen.a424e33d90931d1e">

                            ${requestScope.i18n['screen.a424e33d90931d1e']}

                        </option>


                        <option
                            value="inactive"
                            ${selectedStatus == 'inactive'
                              ? 'selected'
                              : ''} data-i18n="screen.ac7c949f1211b781">

                            ${requestScope.i18n['screen.ac7c949f1211b781']}

                        </option>


                    </select>

                </div>


                <div
                    class="col-md-2
                           d-flex
                           align-items-end">


                    <button
                        type="submit"
                        class="btn btn-primary w-100">

                        <ui:text key="screen.49c266baaaa70981"/>

                    </button>


                </div>


            </form>


            <div
                class="d-flex
                       justify-content-between
                       align-items-center
                       mb-3">


                <span class="text-muted">

                    <ui:text key="screen.18e872be2359d76e"/>
                    ${totalCustomers}
                    <ui:text key="screen.1512f1021c59c4bf"/>

                </span>


                <c:if test="${not empty keyword
                              || not empty selectedStatus}">

                    <a
                        href="${pageContext.request.contextPath}/customers"
                        class="btn btn-outline-secondary btn-sm">

                        <ui:text key="screen.910bda6aed78b23e"/>

                    </a>

                </c:if>


            </div>


            <!-- ========================================= -->
            <!-- CUSTOMER LIST                             -->
            <!-- ========================================= -->

            <c:choose>


                <c:when test="${not empty customers}">


                    <div class="table-responsive">


                        <table
                            class="table
                                   table-hover
                                   table-bordered
                                   align-middle">


                            <thead class="table-dark">


                                <tr>

                                    <th class="text-center">
                                        <ui:text key="screen.3843971dcfdee508"/>
                                    </th>

                                    <th>
                                        <ui:text key="screen.bf3763383aaf4306"/>
                                    </th>

                                    <th>
                                        <ui:text key="screen.969ccbd3cf6300ec"/>
                                    </th>

                                    <th>
                                        <ui:text key="screen.63dceb8800b2dd4a"/>
                                    </th>

                                    <th>
                                        <ui:text key="screen.e3b89e9d33f88e52"/>
                                    </th>

                                    <th>
                                        <ui:text key="screen.920e413c7d411b61"/>
                                    </th>

                                    <th class="text-center">
                                        <ui:text key="screen.ff8059dc6752afdd"/>
                                    </th>

                                </tr>


                            </thead>


                            <tbody>


                                <c:forEach
                                    var="customer"
                                    items="${customers}">


                                    <tr>


                                        <td class="text-center">

                                            ${customer.customerID}

                                        </td>


                                        <td>


                                            <c:choose>

                                                <c:when test="${not empty customer.firstName
                                                                || not empty customer.lastName}">

                                                    ${customer.firstName}
                                                    ${customer.lastName}

                                                </c:when>


                                                <c:otherwise>

                                                    <span class="text-muted">

                                                        <ui:text key="screen.d83b9ff3c07a8390"/>

                                                    </span>

                                                </c:otherwise>

                                            </c:choose>


                                            <c:if test="${not empty customer.user.displayName}">

                                                <br>

                                                <small class="text-muted">

                                                    ${customer.user.displayName}

                                                </small>

                                            </c:if>


                                        </td>


                                        <td>

                                            ${customer.email}

                                        </td>


                                        <td>

                                            <c:choose>

                                                <c:when test="${not empty customer.phone}">

                                                    ${customer.phone}

                                                </c:when>

                                                <c:otherwise>

                                                    -

                                                </c:otherwise>

                                            </c:choose>

                                        </td>


                                        <td>

                                            ${customer.user.username}

                                        </td>


                                        <!-- Account Status -->

                                        <td>


                                            <c:choose>


                                                <c:when test="${customer.user.accountStatus == 'active'}">

                                                    <span
                                                        class="badge bg-success">

                                                        <ui:text key="screen.92340695899bd2d8"/>

                                                    </span>

                                                </c:when>


                                                <c:when test="${customer.user.accountStatus == 'locked'}">

                                                    <span
                                                        class="badge bg-danger">

                                                        <ui:text key="screen.a424e33d90931d1e"/>

                                                    </span>

                                                </c:when>


                                                <c:when test="${customer.user.accountStatus == 'inactive'}">

                                                    <span
                                                        class="badge bg-secondary">

                                                        <ui:text key="screen.ac7c949f1211b781"/>

                                                    </span>

                                                </c:when>


                                            </c:choose>


                                        </td>


                                        <!-- Actions -->

                                        <td class="text-center">


                                            <div
                                                class="d-flex
                                                       flex-wrap
                                                       justify-content-center
                                                       gap-2">


                                                <!-- Details -->

                                                <a
                                                    href="${pageContext.request.contextPath}/customers?view=detail&id=${customer.customerID}"
                                                    class="btn
                                                           btn-outline-primary
                                                           btn-sm">

                                                    <ui:text key="screen.45989de49fb7f66d"/>

                                                </a>


                                                <!-- Booking History -->

                                                <a
                                                    href="${pageContext.request.contextPath}/history?id=${customer.customerID}&view=bookings"
                                                    class="btn
                                                           btn-outline-success
                                                           btn-sm">

                                                    <ui:text key="screen.4e5f81ada70c344e"/>

                                                </a>


                                                <!-- Payment History -->

                                                <a
                                                    href="${pageContext.request.contextPath}/history?id=${customer.customerID}&view=payments"
                                                    class="btn
                                                           btn-outline-secondary
                                                           btn-sm">

                                                    <ui:text key="screen.632e1c5839411a10"/>

                                                </a>


                                            </div>


                                        </td>


                                    </tr>


                                </c:forEach>


                            </tbody>


                        </table>


                    </div>


                    <!-- ================================= -->
                    <!-- PAGINATION                        -->
                    <!-- ================================= -->

                    <c:if test="${totalPages > 1}">


                        <nav
                            aria-label="${requestScope.i18n['screen.26cafe6bad420ca0']}"
                            class="d-flex
                                   justify-content-center
                                   mt-4" data-i18n-aria-label="screen.26cafe6bad420ca0">


                            <ul class="pagination">


                                <li
                                    class="page-item
                                           ${currentPage == 1
                                             ? 'disabled'
                                             : ''}">


                                    <a
                                        class="page-link"
                                        href="${pageContext.request.contextPath}/customers?view=list&page-index=${currentPage - 1}&keyword=${keyword}&status=${selectedStatus}">

                                        <ui:text key="screen.a57b08a480b822a0"/>

                                    </a>


                                </li>


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
                                            href="${pageContext.request.contextPath}/customers?view=list&page-index=${i}&keyword=${keyword}&status=${selectedStatus}">

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
                                        href="${pageContext.request.contextPath}/customers?view=list&page-index=${currentPage + 1}&keyword=${keyword}&status=${selectedStatus}">

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


                        <ui:text key="screen.1538ef0040c700d1"/>

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