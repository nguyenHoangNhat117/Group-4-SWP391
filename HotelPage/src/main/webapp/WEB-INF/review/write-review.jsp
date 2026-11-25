<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${requestScope.i18n['screen.95627b899e25e113']}</title>
        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-modern.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-ui.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/review.css" rel="stylesheet">

    </head>
    <body>
        <%@include file="/WEB-INF/include/header.jsp" %>
        <main class="container py-5 review-page">
            
            <div class="review-card mt-4">

                <div class="review-hero">
                    <h1 class="review-title mb-2">
                        <c:choose>
                            <c:when test="${not empty existingReview}">
                                <ui:text key="screen.113fcf1cbe6996d1"/>
                            </c:when>
                            <c:otherwise>
                                <ui:text key="screen.95627b899e25e113"/>
                            </c:otherwise>
                        </c:choose>
                    </h1>

                    <p class="review-hero-subtitle mb-0">
                        <ui:text key="screen.8e9d3e1467f7cc3f"/>
                        <c:out value="${bookingId}"/>
                        <ui:text key="screen.fc22788d41d69b67"/>
                        <c:out value="${roomNumber}"/>
                    </p>
                </div>

                <div class="card-body p-4 p-md-5">


                    <c:choose>
                        <c:when test="${not empty existingReview}">
                            <div class="mb-4">
                                <strong><ui:text key="screen.59cbcbca704be347"/></strong>

                                <div class="review-stars">
                                    <c:forEach begin="1" end="5" var="i">
                                        <c:choose>
                                            <c:when test="${existingReview.star >= i}">
                                                <span class="star-filled">★</span>
                                            </c:when>

                                            <c:when test="${existingReview.star >= i - 0.5}">
                                                <span class="star-half">★</span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="star-empty">★</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>

                                    <span class="review-score">
                                        <c:out value="${existingReview.star}"/> / 5
                                    </span>
                                </div>
                            </div>
                            <p><strong><ui:text key="screen.c266ec8162232fcf"/></strong></p>
                            <div class="review-content mb-3">
                                <c:out value="${existingReview.comment}"
                                       default="No written feedback"/>
                            </div>
                            <p>
                                <strong>Trạng thái:</strong>

                                <c:choose>
                                    <c:when test="${existingReview.reviewStatus eq 'approved'}">
                                        <span class="review-status status-approved">Đã duyệt</span>
                                    </c:when>

                                    <c:when test="${existingReview.reviewStatus eq 'pending'}">
                                        <span class="review-status status-pending">Chờ xử lý</span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="review-status status-rejected">Đã từ chối</span>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                            <a href="${pageContext.request.contextPath}/history" class="btn btn-outline-secondary"><ui:text key="screen.4f23f3773dbd070a"/></a>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/review" method="post">
                                <input type="hidden"
                                       name="csrfToken"
                                       value="<c:out value='${sessionScope.csrfToken}'/>">
                                <input type="hidden" name="booking-id" value="<c:out value='${bookingId}'/>">
                                <input type="hidden" name="room-number" value="<c:out value='${roomNumber}'/>">
                                <div class="rating-field">

                                    <label class="form-label fw-semibold">
                                        <ui:text key="screen.e02bce4b378f5766"/>
                                    </label>

                                    <input type="hidden"
                                           id="star"
                                           name="star"
                                           value=""
                                           >

                                    <div class="star-picker" role="group"
                                         aria-label="Chọn điểm đánh giá">

                                        <div class="star-unit" data-index="1">
                                            <span class="star-visual">★</span>
                                            <button type="button" class="star-hit left"
                                                    data-value="0.5"
                                                    aria-label="0.5 sao"></button>
                                            <button type="button" class="star-hit right"
                                                    data-value="1.0"
                                                    aria-label="1 sao"></button>
                                        </div>

                                        <div class="star-unit" data-index="2">
                                            <span class="star-visual">★</span>
                                            <button type="button" class="star-hit left"
                                                    data-value="1.5"
                                                    aria-label="1.5 sao"></button>
                                            <button type="button" class="star-hit right"
                                                    data-value="2.0"
                                                    aria-label="2 sao"></button>
                                        </div>

                                        <div class="star-unit" data-index="3">
                                            <span class="star-visual">★</span>
                                            <button type="button" class="star-hit left"
                                                    data-value="2.5"
                                                    aria-label="2.5 sao"></button>
                                            <button type="button" class="star-hit right"
                                                    data-value="3.0"
                                                    aria-label="3 sao"></button>
                                        </div>

                                        <div class="star-unit" data-index="4">
                                            <span class="star-visual">★</span>
                                            <button type="button" class="star-hit left"
                                                    data-value="3.5"
                                                    aria-label="3.5 sao"></button>
                                            <button type="button" class="star-hit right"
                                                    data-value="4.0"
                                                    aria-label="4 sao"></button>
                                        </div>

                                        <div class="star-unit" data-index="5">
                                            <span class="star-visual">★</span>
                                            <button type="button" class="star-hit left"
                                                    data-value="4.5"
                                                    aria-label="4.5 sao"></button>
                                            <button type="button" class="star-hit right"
                                                    data-value="5.0"
                                                    aria-label="5 sao"></button>
                                        </div>

                                    </div>

                                    <div id="ratingText" class="rating-text">
                                        Vui lòng chọn số sao đánh giá
                                    </div>

                                </div>
                                <div class="mb-4">
                                    <label for="comment" class="form-label fw-semibold"><ui:text key="screen.61f84f05c17b6984"/></label>
                                    <textarea id="comment" name="comment" maxlength="1000" rows="5" class="form-control" placeholder="${requestScope.i18n['screen.1645e3a7c9399884']}" required></textarea>
                                </div>
                                <button type="submit" class="btn btn-primary"><ui:text key="screen.f6cc12201a362b06"/></button>
                                <a href="${pageContext.request.contextPath}/history" class="btn btn-outline-secondary ms-2"><ui:text key="screen.19766ed6ccb2f4a3"/></a>
                            </form>
                            <p class="small text-muted mt-3 mb-0"><ui:text key="screen.a3f8a83bbcc76b56"/></p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/assets/js/review.js"></script>
    </body>
</html>
