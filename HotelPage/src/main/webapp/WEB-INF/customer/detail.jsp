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

    <title>Customer Details</title>

    <link
        href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
        rel="stylesheet">

    <link
        href="<%= request.getContextPath()%>/assets/css/general.css"
        rel="stylesheet">

    <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>

<body class="bg-light">

<div class="body-wrapper">

    <%@include file="/WEB-INF/include/header.jsp" %>


    <main class="container py-5">


        <!-- ========================================= -->
        <!-- MESSAGES                                  -->
        <!-- ========================================= -->

        <c:if test="${param.message == 'status-updated'}">

            <div class="alert alert-success text-center">

                Account status updated successfully.

            </div>

        </c:if>


        <c:if test="${param.message == 'update-failed'}">

            <div class="alert alert-danger text-center">

                Account status could not be updated.

            </div>

        </c:if>


        <c:if test="${param.message == 'info-updated'}">
            <div class="alert alert-success text-center">
                Customer information updated successfully.
            </div>
        </c:if>

        <c:if test="${param.message == 'info-update-failed'}">
            <div class="alert alert-danger text-center">
                Customer information could not be updated. The email may already be in use.
            </div>
        </c:if>

        <c:if test="${param.message == 'invalid-data'}">
            <div class="alert alert-danger text-center">
                Invalid customer information.
            </div>
        </c:if>

        <div
            class="card
                   border-0
                   shadow
                   rounded-4">


            <div class="card-body p-5">


                <div
                    class="d-flex
                           justify-content-between
                           align-items-center
                           mb-4">


                    <h2 class="mb-0">

                        Customer Details

                    </h2>


                    <a
                        href="${pageContext.request.contextPath}/customers"
                        class="btn btn-outline-secondary">

                        Back

                    </a>


                </div>


                <!-- ================================= -->
                <!-- PROFILE                           -->
                <!-- ================================= -->

                <div class="row g-4">


                    <div class="col-md-6">


                        <p>

                            <strong>
                                Customer ID:
                            </strong>

                            ${customerDetail.customerID}

                        </p>


                        <p>

                            <strong>
                                First Name:
                            </strong>

                            <c:choose>

                                <c:when test="${not empty customerDetail.firstName}">
                                    ${customerDetail.firstName}
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </p>


                        <p>

                            <strong>
                                Last Name:
                            </strong>

                            <c:choose>

                                <c:when test="${not empty customerDetail.lastName}">
                                    ${customerDetail.lastName}
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </p>


                        <p>

                            <strong>
                                Email:
                            </strong>

                            ${customerDetail.email}

                        </p>


                        <p>

                            <strong>
                                Phone:
                            </strong>

                            <c:choose>

                                <c:when test="${not empty customerDetail.phone}">
                                    ${customerDetail.phone}
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </p>


                    </div>


                    <div class="col-md-6">


                        <p>

                            <strong>
                                Username:
                            </strong>

                            ${customerDetail.user.username}

                        </p>


                        <p>

                            <strong>
                                Display Name:
                            </strong>

                            <c:choose>

                                <c:when test="${not empty customerDetail.user.displayName}">

                                    ${customerDetail.user.displayName}

                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </p>


                        <p>

                            <strong>
                                Country:
                            </strong>

                            <c:choose>

                                <c:when test="${not empty customerDetail.country}">
                                    ${customerDetail.country}
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </p>


                        <p>

                            <strong>
                                City:
                            </strong>

                            <c:choose>

                                <c:when test="${not empty customerDetail.city}">
                                    ${customerDetail.city}
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </p>


                        <p>

                            <strong>
                                Street:
                            </strong>

                            <c:choose>

                                <c:when test="${not empty customerDetail.street}">
                                    ${customerDetail.street}
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </p>


                    </div>


                </div>


                <hr class="my-4">


                <h4 class="mb-3">Customer Information Management</h4>

                <form action="${pageContext.request.contextPath}/customers" method="post" class="row g-3 mb-4">
                    <input type="hidden" name="action" value="update-info">
                    <input type="hidden" name="customer-id" value="${customerDetail.customerID}">

                    <div class="col-md-6">
                        <label class="form-label">First Name</label>
                        <input class="form-control" name="first-name" maxlength="50" value="<c:out value='${customerDetail.firstName}'/>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Last Name</label>
                        <input class="form-control" name="last-name" maxlength="50" value="<c:out value='${customerDetail.lastName}'/>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Email</label>
                        <input type="email" class="form-control" name="email" maxlength="100" required value="<c:out value='${customerDetail.email}'/>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Phone</label>
                        <input class="form-control" name="phone" maxlength="20" value="<c:out value='${customerDetail.phone}'/>">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Country</label>
                        <input class="form-control" name="country" maxlength="50" value="<c:out value='${customerDetail.country}'/>">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">City</label>
                        <input class="form-control" name="city" maxlength="50" value="<c:out value='${customerDetail.city}'/>">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Street</label>
                        <input class="form-control" name="street" maxlength="100" value="<c:out value='${customerDetail.street}'/>">
                    </div>
                    <div class="col-12 text-end">
                        <button type="submit" class="btn btn-primary" onclick="return confirm('Update this customer information?');">
                            Update Information
                        </button>
                    </div>
                </form>

                <hr class="my-4">

                <!-- ================================= -->
                <!-- ACCOUNT INFORMATION               -->
                <!-- ================================= -->

                <h4 class="mb-3">

                    Account Information

                </h4>


                <div class="row">


                    <div class="col-md-6">


                        <p>

                            <strong>
                                Role:
                            </strong>

                            ${customerDetail.user.role}

                        </p>


                        <p>

                            <strong>
                                Created At:
                            </strong>

                            ${customerDetail.user.createdAt}

                        </p>


                    </div>


                    <div class="col-md-6">


                        <p>

                            <strong>
                                Current Status:
                            </strong>


                            <c:choose>


                                <c:when test="${customerDetail.user.accountStatus == 'active'}">

                                    <span
                                        class="badge bg-success">

                                        Active

                                    </span>

                                </c:when>


                                <c:when test="${customerDetail.user.accountStatus == 'locked'}">

                                    <span
                                        class="badge bg-danger">

                                        Locked

                                    </span>

                                </c:when>


                                <c:otherwise>

                                    <span
                                        class="badge bg-secondary">

                                        Inactive

                                    </span>

                                </c:otherwise>


                            </c:choose>


                        </p>


                    </div>


                </div>


                <hr class="my-4">


                <!-- ================================= -->
                <!-- CHANGE ACCOUNT STATUS             -->
                <!-- ================================= -->

                <h4 class="mb-3">

                    Account Status Management

                </h4>


                <form
                    action="${pageContext.request.contextPath}/customers"
                    method="post"
                    class="row g-3">


                    <input
                        type="hidden"
                        name="action"
                        value="update-status">


                    <input
                        type="hidden"
                        name="customer-id"
                        value="${customerDetail.customerID}">


                    <div class="col-md-8">


                        <select
                            name="account-status"
                            class="form-select"
                            required>


                            <option
                                value="active"
                                ${customerDetail.user.accountStatus == 'active'
                                  ? 'selected'
                                  : ''}>

                                Active

                            </option>


                            <option
                                value="locked"
                                ${customerDetail.user.accountStatus == 'locked'
                                  ? 'selected'
                                  : ''}>

                                Locked

                            </option>


                            <option
                                value="inactive"
                                ${customerDetail.user.accountStatus == 'inactive'
                                  ? 'selected'
                                  : ''}>

                                Inactive

                            </option>


                        </select>


                    </div>


                    <div class="col-md-4">


                        <button
                            type="submit"
                            class="btn btn-primary w-100"
                            onclick="return confirm('Change this customer account status?');">

                            Update Status

                        </button>


                    </div>


                </form>


                <hr class="my-4">


                <!-- ================================= -->
                <!-- HISTORY                           -->
                <!-- ================================= -->

                <h4 class="mb-3">

                    Customer History

                </h4>


                <div
                    class="d-flex
                           flex-wrap
                           gap-3">


                    <a
                        href="${pageContext.request.contextPath}/history?id=${customerDetail.customerID}&view=bookings"
                        class="btn btn-outline-primary">

                        View Booking History

                    </a>


                    <a
                        href="${pageContext.request.contextPath}/history?id=${customerDetail.customerID}&view=payments"
                        class="btn btn-outline-success">

                        View Payment History

                    </a>


                </div>


            </div>


        </div>


    </main>


    <%@include file="/WEB-INF/include/footer.jsp" %>


</div>


<script
    src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js">
</script>


</body>

</html>