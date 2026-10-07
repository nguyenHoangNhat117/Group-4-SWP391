<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta http-equiv="Content-Type"
          content="text/html; charset=UTF-8">

    <title>Review Management</title>

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

        <div class="container py-4">

            <h2 class="fw-bold text-center mb-4">
                Review Management
            </h2>


            <!-- ========================================= -->
            <!-- MESSAGES                                  -->
            <!-- ========================================= -->

            <c:if test="${param.message == 'approved'}">

                <div class="alert alert-success text-center">
                    Review approved successfully.
                </div>

            </c:if>


            <c:if test="${param.message == 'rejected'}">

                <div class="alert alert-warning text-center">
                    Review rejected successfully.
                </div>

            </c:if>


            <c:if test="${param.message == 'update-failed'}">

                <div class="alert alert-danger text-center">
                    Review status could not be updated.
                </div>

            </c:if>


            <c:if test="${param.message == 'invalid-review'}">

                <div class="alert alert-danger text-center">
                    Invalid review.
                </div>

            </c:if>


            <!-- ========================================= -->
            <!-- REVIEW TABLE                              -->
            <!-- ========================================= -->

            <c:choose>

                <c:when test="${not empty reviews}">

                    <div class="table-responsive">

                        <table
                            class="table
                                   table-bordered
                                   table-striped
                                   table-hover
                                   align-middle">

                            <thead class="table-dark">

                                <tr>

                                    <th>
                                        Review ID
                                    </th>

                                    <th>
                                        Booking
                                    </th>

                                    <th>
                                        Room
                                    </th>

                                    <th>
                                        Customer
                                    </th>

                                    <th>
                                        Rating
                                    </th>

                                    <th>
                                        Comment
                                    </th>

                                    <th>
                                        Date
                                    </th>

                                    <th>
                                        Status
                                    </th>

                                    <th>
                                        Action
                                    </th>

                                </tr>

                            </thead>


                            <tbody>

                            <c:forEach
                                var="r"
                                items="${reviews}">

                                <tr>

                                    <!-- Review ID -->
                                    <td>
                                        ${r.reviewID}
                                    </td>


                                    <!-- Booking -->
                                    <td>

                                        #${r.booking.id}

                                        <br>

                                        <small class="text-muted">
                                            ${r.booking.status}
                                        </small>

                                    </td>


                                    <!-- Room -->
                                    <td>

                                        <c:choose>

                                            <c:when test="${not empty r.booking.details}">

                                                <c:forEach
                                                    var="detail"
                                                    items="${r.booking.details}">

                                                    <div class="mb-1">

                                                        <a
                                                            href="${pageContext.request.contextPath}/details?roomNumber=${detail.roomNumber}">

                                                            Room
                                                            #${detail.roomNumber}

                                                        </a>

                                                        <br>

                                                        <small class="text-muted">

                                                            ${detail.checkInDate}
                                                            →
                                                            ${detail.checkOutDate}

                                                        </small>

                                                    </div>

                                                </c:forEach>

                                            </c:when>


                                            <c:otherwise>

                                                <span class="text-muted">
                                                    No room information
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>


                                    <!-- Customer -->
                                    <td>

                                        <c:choose>

                                            <c:when test="${not empty r.booking.customer.user.displayName}">

                                                ${r.booking.customer.user.displayName}

                                            </c:when>


                                            <c:when test="${not empty r.booking.customer.firstName
                                                            || not empty r.booking.customer.lastName}">

                                                ${r.booking.customer.firstName}
                                                ${r.booking.customer.lastName}

                                            </c:when>


                                            <c:otherwise>

                                                Customer
                                                #${r.booking.customer.customerID}

                                            </c:otherwise>

                                        </c:choose>


                                        <br>


                                        <small class="text-muted">

                                            ${r.booking.customer.user.username}

                                        </small>

                                    </td>


                                    <!-- Rating -->
                                    <td>

                                        <strong>
                                            ${r.star} ★
                                        </strong>

                                    </td>


                                    <!-- Comment -->
                                    <td>

                                        <c:choose>

                                            <c:when test="${not empty r.comment}">

                                                <c:out value="${r.comment}"/>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="text-muted">
                                                    No comment
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>


                                    <!-- Review Date -->
                                    <td>

                                        ${r.reviewDate}

                                    </td>


                                    <!-- Review Status -->
                                    <td>

                                        <c:choose>

                                            <c:when test="${r.reviewStatus == 'pending'}">

                                                <span
                                                    class="badge
                                                           bg-warning
                                                           text-dark">

                                                    Pending

                                                </span>

                                            </c:when>


                                            <c:when test="${r.reviewStatus == 'approved'}">

                                                <span
                                                    class="badge
                                                           bg-success">

                                                    Approved

                                                </span>

                                            </c:when>


                                            <c:when test="${r.reviewStatus == 'rejected'}">

                                                <span
                                                    class="badge
                                                           bg-danger">

                                                    Rejected

                                                </span>

                                            </c:when>


                                            <c:otherwise>

                                                <span
                                                    class="badge
                                                           bg-secondary">

                                                    ${r.reviewStatus}

                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>


                                    <!-- ================================= -->
                                    <!-- MODERATION ACTIONS                -->
                                    <!-- ================================= -->

                                    <td>

                                        <div
                                            class="d-flex
                                                   flex-column
                                                   gap-2">


                                            <!-- APPROVE -->

                                            <c:if test="${r.reviewStatus != 'approved'}">

                                                <form
                                                    action="${pageContext.request.contextPath}/admin/reviews/moderate"
                                                    method="post">

                                                    <input
                                                        type="hidden"
                                                        name="reviewID"
                                                        value="${r.reviewID}">

                                                    <input
                                                        type="hidden"
                                                        name="action"
                                                        value="approve">

                                                    <button
                                                        type="submit"
                                                        class="btn
                                                               btn-success
                                                               btn-sm
                                                               w-100"
                                                        onclick="return confirm('Approve this review?');">

                                                        Approve

                                                    </button>

                                                </form>

                                            </c:if>


                                            <!-- REJECT -->

                                            <c:if test="${r.reviewStatus != 'rejected'}">

                                                <form
                                                    action="${pageContext.request.contextPath}/admin/reviews/moderate"
                                                    method="post">

                                                    <input
                                                        type="hidden"
                                                        name="reviewID"
                                                        value="${r.reviewID}">

                                                    <input
                                                        type="hidden"
                                                        name="action"
                                                        value="reject">

                                                    <button
                                                        type="submit"
                                                        class="btn
                                                               btn-danger
                                                               btn-sm
                                                               w-100"
                                                        onclick="return confirm('Reject this review?');">

                                                        Reject

                                                    </button>

                                                </form>

                                            </c:if>


                                        </div>

                                    </td>

                                </tr>

                            </c:forEach>

                            </tbody>

                        </table>

                    </div>

                </c:when>


                <c:otherwise>

                    <div
                        class="alert
                               alert-info
                               text-center
                               mt-5">

                        There are no reviews yet.

                    </div>

                </c:otherwise>

            </c:choose>


            <!-- Back to admin area -->
            <div class="mt-4">

                <a
                    href="${pageContext.request.contextPath}/"
                    class="btn btn-outline-secondary">

                    Back

                </a>

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