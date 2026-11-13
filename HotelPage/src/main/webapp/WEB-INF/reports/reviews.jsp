<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
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
<html lang="${requestScope.uiLanguage}" class="hotel-reports-page">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.d0315fc960e50004">${requestScope.i18n['screen.d0315fc960e50004']}</title>

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

            <%-- Review summary for the currently loaded result set. Manager filters/pages apply. --%>
            <c:set var="approvedCount" value="0" />
            <c:set var="pendingCount" value="0" />
            <c:set var="rejectedCount" value="0" />
            <c:set var="starTotal" value="0" />
            <c:set var="fiveStars" value="0" /><c:set var="fourStars" value="0" /><c:set var="threeStars" value="0" /><c:set var="twoStars" value="0" /><c:set var="oneStar" value="0" />
            <c:forEach var="review" items="${reviews}">
                <c:choose>
                    <c:when test="${review.reviewStatus == 'approved'}">
                        <c:set var="approvedCount" value="${approvedCount + 1}" />
                        <c:set var="starTotal" value="${starTotal + review.star}" />
                        <c:choose><c:when test="${review.star == 5}"><c:set var="fiveStars" value="${fiveStars + 1}"/></c:when><c:when test="${review.star == 4}"><c:set var="fourStars" value="${fourStars + 1}"/></c:when><c:when test="${review.star == 3}"><c:set var="threeStars" value="${threeStars + 1}"/></c:when><c:when test="${review.star == 2}"><c:set var="twoStars" value="${twoStars + 1}"/></c:when><c:when test="${review.star == 1}"><c:set var="oneStar" value="${oneStar + 1}"/></c:when></c:choose>
                    </c:when>
                    <c:when test="${review.reviewStatus == 'pending'}"><c:set var="pendingCount" value="${pendingCount + 1}" /></c:when>
                    <c:when test="${review.reviewStatus == 'rejected'}"><c:set var="rejectedCount" value="${rejectedCount + 1}" /></c:when>
                </c:choose>
            </c:forEach>
            <section class="container my-4" aria-label="${requestScope.i18n['screen.d796c3f47a2c5009']}" data-i18n-aria-label="screen.d796c3f47a2c5009">
                <h2><ui:text key="screen.d796c3f47a2c5009"/></h2>
                <div class="row g-3">
                    <div class="col-md-3"><div class="card p-3"><strong><ui:text key="screen.980777020186b784"/></strong><h3>${fn:length(reviews)}</h3></div></div>
                    <div class="col-md-3"><div class="card p-3"><strong><ui:text key="screen.87b42e40c2a290e0"/></strong><h3>${approvedCount}</h3></div></div>
                    <div class="col-md-3"><div class="card p-3"><strong><ui:text key="screen.0bcdff77493ef760"/></strong><h3>${pendingCount} / ${rejectedCount}</h3></div></div>
                    <div class="col-md-3"><div class="card p-3"><strong><ui:text key="screen.a3ee576b0504de9f"/></strong><h3><c:choose><c:when test="${approvedCount > 0}"><fmt:formatNumber value="${starTotal / approvedCount}" maxFractionDigits="1" minFractionDigits="1"/> / 5</c:when><c:otherwise><ui:text key="screen.e2f79e5b60330bba"/></c:otherwise></c:choose></h3></div></div>
                </div>
                <div class="mt-3">
    <strong><ui:text key="screen.0f6484cf8b5d3961"/></strong>

    5★ ${approvedRatingCounts[5]} ·
    4★ ${approvedRatingCounts[4]} ·
    3★ ${approvedRatingCounts[3]} ·
    2★ ${approvedRatingCounts[2]} ·
    1★ ${approvedRatingCounts[1]}
</div>
                <c:if test="${not reportIsAdmin}"><small><ui:text key="screen.4bfc5c616de51c80"/></small></c:if>
            </section>
            <c:choose>
                <c:when test="${reportIsAdmin}">
            <div class="report-heading">
                <div>
                    <p class="report-eyebrow">
                        <ui:text key="screen.b11fef0048e7eff8"/>
                    </p>

                    <h1>
                        <c:choose>
                            <c:when test="${reportIsAdmin}">
                                <ui:text key="screen.47bf4e92fa8a67be"/>
                            </c:when>

                            <c:otherwise>
                                <ui:text key="screen.d515fdd22c1437ff"/>
                            </c:otherwise>
                        </c:choose>
                    </h1>

                    <p class="report-description">
                        <ui:text key="screen.326be3a3dda19299"/>
                    </p>
                </div>

                <c:choose>
                    <c:when test="${reportIsAdmin}">
                        <a class="report-button" href="${ctx}/">
                            <ui:text key="screen.74fcd10c9e772c3e"/>
                        </a>
                    </c:when>

                    <c:otherwise>
                        <a class="report-button" href="${ctx}/dashboard">
                            <ui:text key="screen.a7750c2b09f60f0b"/>
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>

            <%-- Manager report navigation. --%>
            <c:if test="${not reportIsAdmin}">
                <div class="report-tabs" aria-label="${requestScope.i18n['screen.19025f8911e31c54']}" data-i18n-aria-label="screen.19025f8911e31c54">

                    <a href="${ctx}/reports?view=bookings&amp;page-index=1">
                        <ui:text key="screen.4e5f81ada70c344e"/>
                    </a>

                    <a href="${ctx}/reports?view=payments&amp;page-index=1">
                        <ui:text key="screen.632e1c5839411a10"/>
                    </a>

                    <a href="${ctx}/reports?view=reviews"
                       aria-current="page">
                        <ui:text key="screen.84cb7871b741c32e"/>
                    </a>

                    <a href="${ctx}/reports?view=month">
                        <ui:text key="screen.c4b7330bd91e0c97"/>
                    </a>

                </div>
            </c:if>

                </c:when>
                <c:otherwise>
                    <%@ include file="report-list-toolbar.jsp" %>
                </c:otherwise>
            </c:choose>

            <c:choose>

                <%-- A loading failure must not appear as an empty result. --%>
                <c:when test="${not empty reviewLoadError}">

                    <section class="report-panel">

                        <div class="report-panel-heading">
                            <h2><ui:text key="screen.b4aef2f0b115f5bb"/></h2>
                        </div>

                        <div class="report-empty" role="alert">

                            <p>
                                <strong>
                                    <c:out value="${reviewLoadError}" />
                                </strong>
                            </p>

                            <a class="report-button primary"
                               href="<c:out value='${reviewRetryUrl}' />">
                                <ui:text key="screen.d8b8392e2c542950"/>
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
                                    <ui:text key="screen.61346df63f9b47f3"/>
                                </div>
                            </c:when>

                            <c:when test="${param.message == 'rejected'}">
                                <div class="report-note" role="status">
                                    <ui:text key="screen.ad3c9f5b35ae0bed"/>
                                </div>
                            </c:when>

                            <c:when test="${param.message == 'update-failed'}">
                                <div class="report-note" role="alert">
                                    <ui:text key="screen.d08ffcf9874a378b"/>
                                </div>
                            </c:when>

                            <c:when test="${param.message == 'invalid-review'}">
                                <div class="report-note" role="alert">
                                    <ui:text key="screen.1ecdd4240beb3569"/>
                                </div>
                            </c:when>

                        </c:choose>
                    </c:if>



                    <section class="report-panel">

                        <div class="report-panel-heading">
                            <h2><ui:text key="screen.ed9ee489ce7cc90d"/></h2>

                            <span class="report-meta">
                                <c:choose>
                                    <c:when test="${reportIsAdmin}">${fn:length(reviews)} <ui:text key="screen.5464105120381493"/></c:when>
                                    <c:otherwise>${totalRecords} <ui:text key="screen.1a5d1077e5a88835"/> ${fn:length(reviews)} <ui:text key="screen.c36ee348ff2d354a"/></c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <c:choose>

                            <c:when test="${not empty reviews}">

                                <div class="report-table-scroll">

                                    <table class="report-table">

                                        <thead>
                                            <tr>
                                                <th scope="col"><ui:text key="screen.aff0766a5290e117"/></th>
                                                <th scope="col"><ui:text key="screen.948e03e7054b919e"/></th>
                                                <th scope="col"><ui:text key="screen.fe43f0a1373a3d68"/></th>
                                                <th scope="col"><ui:text key="screen.bf3763383aaf4306"/></th>
                                                <th scope="col"><ui:text key="screen.9f29530464f730bd"/></th>
                                                <th scope="col"><ui:text key="screen.44f5e3fbec57be75"/></th>
                                                <th scope="col"><ui:text key="screen.1c205cbb90e19abd"/></th>
                                                <th scope="col"><ui:text key="screen.920e413c7d411b61"/></th>

                                                <c:if test="${reportIsAdmin}">
                                                    <th scope="col"><ui:text key="screen.ff8059dc6752afdd"/></th>
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
                                                                                <ui:text key="screen.6c8c6af24c9b021c"/>${detail.roomNumber}
                                                                            </a>
                                                                        </strong>

                                                                        <span class="report-muted">
                                                                            ${fn:substring(detail.checkInDate, 8, 10)}/${fn:substring(detail.checkInDate, 5, 7)}/${fn:substring(detail.checkInDate, 0, 4)}
                                                                            → ${fn:substring(detail.checkOutDate, 8, 10)}/${fn:substring(detail.checkOutDate, 5, 7)}/${fn:substring(detail.checkOutDate, 0, 4)}
                                                                        </span>

                                                                    </div>

                                                                </c:forEach>

                                                            </c:when>

                                                            <c:otherwise>
                                                                <span class="report-muted">
                                                                    <ui:text key="screen.448d8aa4cbfe963c"/>
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
                                                                <ui:text key="screen.641d305570b93564"/>${r.booking.customer.customerID}
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
                                                        <c:out value="${fn:substring(r.reviewDate, 8, 10)}/${fn:substring(r.reviewDate, 5, 7)}/${fn:substring(r.reviewDate, 0, 4)}" />
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
                                                                            <ui:text key="screen.6007acbe30b2cd98"/>
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
                                                                            <ui:text key="screen.ab604a360777735f"/>
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
                                    <ui:text key="screen.b6b9cb7b06b6fef0"/>
                                </div>
                            </c:otherwise>

                        </c:choose>

                    </section>

                </c:otherwise>

            </c:choose>

            <c:if test="${not reportIsAdmin}">
                <%@ include file="report-list-pagination.jsp" %>
            </c:if>
        </main>

        <%@ include file="/WEB-INF/include/footer.jsp" %>

        <script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>

    </body>
</html>