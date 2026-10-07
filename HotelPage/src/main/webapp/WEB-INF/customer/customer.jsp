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

    <title>Customer Management</title>

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


            <h2 class="fw-bold text-center mb-4">

                Customer Management

            </h2>


            <!-- ========================================= -->
            <!-- MESSAGES                                  -->
            <!-- ========================================= -->

            <c:if test="${param.message == 'invalid-data'}">

                <div class="alert alert-danger text-center">

                    Invalid customer information.

                </div>

            </c:if>


            <c:if test="${param.message == 'customer-not-found'}">

                <div class="alert alert-danger text-center">

                    Customer not found.

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

                        Search Customer

                    </label>


                    <input
                        type="text"
                        id="keyword"
                        name="keyword"
                        class="form-control"
                        value="${keyword}"
                        placeholder="ID, name, email, phone or username">

                </div>


                <!-- Status -->

                <div class="col-md-3">

                    <label
                        for="status"
                        class="form-label fw-semibold">

                        Account Status

                    </label>


                    <select
                        id="status"
                        name="status"
                        class="form-select">


                        <option value="">
                            All Statuses
                        </option>


                        <option
                            value="active"
                            ${selectedStatus == 'active'
                              ? 'selected'
                              : ''}>

                            Active

                        </option>


                        <option
                            value="locked"
                            ${selectedStatus == 'locked'
                              ? 'selected'
                              : ''}>

                            Locked

                        </option>


                        <option
                            value="inactive"
                            ${selectedStatus == 'inactive'
                              ? 'selected'
                              : ''}>

                            Inactive

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

                        Search

                    </button>


                </div>


            </form>


            <div
                class="d-flex
                       justify-content-between
                       align-items-center
                       mb-3">


                <span class="text-muted">

                    Total:
                    ${totalCustomers}
                    customer(s)

                </span>


                <c:if test="${not empty keyword
                              || not empty selectedStatus}">

                    <a
                        href="${pageContext.request.contextPath}/customers"
                        class="btn btn-outline-secondary btn-sm">

                        Clear Filter

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
                                        ID
                                    </th>

                                    <th>
                                        Customer
                                    </th>

                                    <th>
                                        Email
                                    </th>

                                    <th>
                                        Phone
                                    </th>

                                    <th>
                                        Username
                                    </th>

                                    <th>
                                        Status
                                    </th>

                                    <th class="text-center">
                                        Actions
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

                                                        Not provided

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

                                                        Active

                                                    </span>

                                                </c:when>


                                                <c:when test="${customer.user.accountStatus == 'locked'}">

                                                    <span
                                                        class="badge bg-danger">

                                                        Locked

                                                    </span>

                                                </c:when>


                                                <c:when test="${customer.user.accountStatus == 'inactive'}">

                                                    <span
                                                        class="badge bg-secondary">

                                                        Inactive

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

                                                    Details

                                                </a>


                                                <!-- Booking History -->

                                                <a
                                                    href="${pageContext.request.contextPath}/history?id=${customer.customerID}&view=bookings"
                                                    class="btn
                                                           btn-outline-success
                                                           btn-sm">

                                                    Bookings

                                                </a>


                                                <!-- Payment History -->

                                                <a
                                                    href="${pageContext.request.contextPath}/history?id=${customer.customerID}&view=payments"
                                                    class="btn
                                                           btn-outline-secondary
                                                           btn-sm">

                                                    Payments

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
                            aria-label="Customer pagination"
                            class="d-flex
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
                                        href="${pageContext.request.contextPath}/customers?view=list&page-index=${currentPage - 1}&keyword=${keyword}&status=${selectedStatus}">

                                        Previous

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


                        No customers found.

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