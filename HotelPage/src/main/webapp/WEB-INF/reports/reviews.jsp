<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<fmt:setLocale value="vi_VN" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="reportIsAdmin"
       value="${sessionScope.loggedUser.role == 'admin'}" />

<c:choose>
    <c:when test="${reportIsAdmin}">
        <c:url var="reviewRetryUrl" value="/admin/reviews" />
    </c:when>

    <c:otherwise>
        <c:url var="reviewRetryUrl" value="/reports">
            <c:param name="view" value="reviews" />
        </c:url>
    </c:otherwise>
</c:choose>

<!DOCTYPE html>
<html lang="en" class="hotel-reports-page">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>Guest Reviews | Luxe Escape</title>

        <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/assets/css/general.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-modern.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-ui.css">
        <link rel="stylesheet" href="${ctx}/assets/css/hotel-reports.css">
    </head>

    <body class="hotel-reports-body ${cookie.animation.value == 'disable' ? 'disable-animation' : ''}">

        <%@ include file="/WEB-INF/include/header.jsp" %>

        <main class="hotel-reports">

            <div class="report-heading">
                <div>
                    <p class="report-eyebrow">
                        Luxe Escape / Guest feedback
                    </p>

                    <h1>
                        <c:choose>
                            <c:when test="${reportIsAdmin}">
                                Review management
                            </c:when>

                            <c:otherwise>
                                Review report
                            </c:otherwise>
                        </c:choose>
                    </h1>

                    <p class="report-description">
                        Guest ratings, comments and review moderation statuses.
                    </p>
                </div>

                <c:choose>
                    <c:when test="${reportIsAdmin}">
                        <a class="report-button" href="${ctx}/">
                            Back to home ↗
                        </a>
                    </c:when>

                    <c:otherwise>
                        <a class="report-button" href="${ctx}/dashboard">
                            Back to dashboard ↗
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>

            <%-- Manager report navigation. --%>
            <c:if test="${not reportIsAdmin}">
                <div class="report-tabs" aria-label="Report navigation">

                    <a href="${ctx}/reports?view=bookings&amp;page-index=1">
                        Bookings
                    </a>

                    <a href="${ctx}/reports?view=payments&amp;page-index=1">
                        Payments
                    </a>

                    <a href="${ctx}/reports?view=reviews"
                       aria-current="page">
                        Reviews
                    </a>

                    <a href="${ctx}/reports?view=month">
                        Revenue
                    </a>

                </div>
            </c:if>

            <c:choose>

                <%-- A loading failure must not appear as an empty result. --%>
                <c:when test="${not empty reviewLoadError}">

                    <section class="report-panel">

                        <div class="report-panel-heading">
                            <h2>Review data unavailable</h2>
                        </div>

                        <div class="report-empty" role="alert">

                            <p>
                                <strong>
                                    <c:out value="${reviewLoadError}" />
                                </strong>
                            </p>

                            <a class="report-button primary"
                               href="<c:out value='${reviewRetryUrl}' />">
                                Try again
                            </a>

                        </div>

                    </section>

                </c:when>

                <c:otherwise>

                    <%-- Show moderation feedback only after loading succeeds. --%>
                    <c:if test="${reportIsAdmin}">
                        <c:choose>

                            <c:when test="${param.message == 'approved'}">
                                <div class="report-note" role="status">
                                    Review approved successfully.
                                </div>
                            </c:when>

                            <c:when test="${param.message == 'rejected'}">
                                <div class="report-note" role="status">
                                    Review rejected successfully.
                                </div>
                            </c:when>

                            <c:when test="${param.message == 'update-failed'}">
                                <div class="report-note" role="alert">
                                    The review could not be updated.
                                </div>
                            </c:when>

                            <c:when test="${param.message == 'invalid-review'}">
                                <div class="report-note" role="alert">
                                    Invalid review request.
                                </div>
                            </c:when>

                        </c:choose>
                    </c:if>

                    <div class="report-note">
                        This list includes reviews from all periods and
                        moderation statuses. The dashboard average rating
                        includes approved reviews only.
                    </div>

                    <section class="report-panel">

                        <div class="report-panel-heading">
                            <h2>Guest feedback records</h2>

                            <span class="report-meta">
                                ${fn:length(reviews)} reviews
                            </span>
                        </div>

                        <c:choose>

                            <c:when test="${not empty reviews}">

                                <div class="report-table-scroll">

                                    <table class="report-table">

                                        <thead>
                                            <tr>
                                                <th scope="col">Review</th>
                                                <th scope="col">Booking</th>
                                                <th scope="col">Room / Stay</th>
                                                <th scope="col">Customer</th>
                                                <th scope="col">Rating</th>
                                                <th scope="col">Comment</th>
                                                <th scope="col">Review date</th>
                                                <th scope="col">Status</th>

                                                <c:if test="${reportIsAdmin}">
                                                    <th scope="col">Actions</th>
                                                    </c:if>
                                            </tr>
                                        </thead>

                                        <tbody>

                                            <c:forEach var="r" items="${reviews}">

                                                <tr>

                                                    <td>
                                                        <strong>#${r.reviewID}</strong>
                                                    </td>

                                                    <td class="report-nowrap">
                                                        <strong>#${r.booking.id}</strong>

                                                        <span class="report-muted">
                                                            <c:out value="${fn:replace(r.booking.status, '_', ' ')}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <c:choose>

                                                            <c:when test="${not empty r.booking.details}">

                                                                <c:forEach var="detail"
                                                                           items="${r.booking.details}">

                                                                    <div class="report-room">

                                                                        <strong>
                                                                            <a href="${ctx}/details?roomNumber=${detail.roomNumber}">
                                                                                Room #${detail.roomNumber}
                                                                            </a>
                                                                        </strong>

                                                                        <span class="report-muted">
                                                                            ${detail.checkInDate}
                                                                            → ${detail.checkOutDate}
                                                                        </span>

                                                                    </div>

                                                                </c:forEach>

                                                            </c:when>

                                                            <c:otherwise>
                                                                <span class="report-muted">
                                                                    No room information
                                                                </span>
                                                            </c:otherwise>

                                                        </c:choose>
                                                    </td>

                                                    <td>
                                                        <c:choose>

                                                            <c:when test="${not empty r.booking.customer.user.displayName}">
                                                                <c:out value="${r.booking.customer.user.displayName}" />
                                                            </c:when>

                                                            <c:when test="${not empty r.booking.customer.firstName
                                                                            or not empty r.booking.customer.lastName}">
                                                                <c:out value="${r.booking.customer.firstName}" />
                                                                <c:out value="${r.booking.customer.lastName}" />
                                                            </c:when>

                                                            <c:otherwise>
                                                                Customer #${r.booking.customer.customerID}
                                                            </c:otherwise>

                                                        </c:choose>

                                                        <span class="report-muted">
                                                            <c:out value="${r.booking.customer.user.username}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <span class="report-rating">
                                                            <fmt:formatNumber
                                                                value="${r.star}"
                                                                pattern="0.##" />
                                                            ★ / 5
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <div class="report-comment"><c:out value="${empty r.comment ? 'No comment' : r.comment}" /></div>
                                                    </td>

                                                    <td class="report-nowrap">
                                                        <c:out value="${fn:replace(r.reviewDate, 'T', ' ')}" />
                                                    </td>

                                                    <td>
                                                        <span class="report-status <c:out value='${r.reviewStatus}' />">
                                                            <c:out value="${r.reviewStatus}" />
                                                        </span>
                                                    </td>

                                                    <%-- Server-side authorization remains enforced by the filter and servlet. --%>
                                                    <c:if test="${reportIsAdmin}">
                                                        <td>
                                                            <div class="report-actions">

                                                                <c:if test="${r.reviewStatus != 'approved'}">

                                                                    <form action="${ctx}/admin/reviews/moderate"
                                                                          method="post">

                                                                        <input type="hidden"
                                                                               name="reviewID"
                                                                               value="${r.reviewID}">

                                                                        <input type="hidden"
                                                                               name="action"
                                                                               value="approve">

                                                                        <button type="submit"
                                                                                class="report-button approve"
                                                                                onclick="return confirm('Approve this review?');">
                                                                            Approve
                                                                        </button>

                                                                    </form>

                                                                </c:if>

                                                                <c:if test="${r.reviewStatus != 'rejected'}">

                                                                    <form action="${ctx}/admin/reviews/moderate"
                                                                          method="post">

                                                                        <input type="hidden"
                                                                               name="reviewID"
                                                                               value="${r.reviewID}">

                                                                        <input type="hidden"
                                                                               name="action"
                                                                               value="reject">

                                                                        <button type="submit"
                                                                                class="report-button reject"
                                                                                onclick="return confirm('Reject this review?');">
                                                                            Reject
                                                                        </button>

                                                                    </form>

                                                                </c:if>

                                                            </div>
                                                        </td>
                                                    </c:if>

                                                </tr>

                                            </c:forEach>

                                        </tbody>

                                    </table>

                                </div>

                            </c:when>

                            <c:otherwise>
                                <div class="report-empty">
                                    No review records found.
                                </div>
                            </c:otherwise>

                        </c:choose>

                    </section>

                </c:otherwise>

            </c:choose>

        </main>

        <%@ include file="/WEB-INF/include/footer.jsp" %>

        <script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>

    </body>
</html>