<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title data-i18n="screen.95627b899e25e113">${requestScope.i18n['screen.95627b899e25e113']}</title>
        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-modern.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-ui.css" rel="stylesheet">
        <style>
            body {
                background: #f8f5f0;
            }

            .review-page {
                max-width: 850px;
                margin: 0 auto;
            }

            .review-card {
                background: #fff;
                border: 1px solid #eadfD2;
                border-radius: 20px;
                box-shadow: 0 10px 30px rgba(70, 50, 30, .07);
            }

            .review-title {
                font-family: Georgia, serif;
                font-size: 36px;
                font-weight: bold;
                color: #34271f;
            }

            .review-content {
                background: #faf7f2;
                border: 1px solid #eadfd2;
                border-radius: 12px;
                padding: 20px;
                line-height: 1.7;
            }

            .review-status {
                display: inline-block;
                padding: 7px 14px;
                border-radius: 20px;
                font-size: 13px;
                font-weight: 600;
            }

            .status-approved {
                background: #e7f4e9;
                color: #287442;
            }

            .status-pending {
                background: #fff3d6;
                color: #946600;
            }

            .status-rejected {
                background: #fde8e7;
                color: #b42318;
            }

            .review-back {
                background: #795333;
                color: white;
                border: none;
                border-radius: 9px;
                padding: 10px 22px;
            }

            .review-back:hover {
                background: #5f4028;
                color: white;
            }
        </style>
    </head>
    <body>
        <%@include file="/WEB-INF/include/header.jsp" %>
        <main class="container py-5 review-page">
            <a href="${pageContext.request.contextPath}/history" class="text-decoration-none"><ui:text key="screen.75365a2c3de4d111"/></a>
            <div class="review-card mt-4">
                <div class="card-body p-4 p-md-5">
                    <h1 class="review-title mb-3"><c:choose><c:when test="${not empty existingReview}"><ui:text key="screen.113fcf1cbe6996d1"/></c:when><c:otherwise><ui:text key="screen.95627b899e25e113"/></c:otherwise></c:choose></h1>
                    <p class="text-muted mb-4"><ui:text key="screen.8e9d3e1467f7cc3f"/><c:out value="${bookingId}"/> <ui:text key="screen.fc22788d41d69b67"/><c:out value="${roomNumber}"/></p>
                    <c:choose>
                        <c:when test="${not empty existingReview}">
                            <p><strong><ui:text key="screen.59cbcbca704be347"/></strong> <c:out value="${existingReview.star}"/> / 5 ★</p>
                            <p><strong><ui:text key="screen.c266ec8162232fcf"/></strong></p>
                            <div class="review-content mb-3">
                                <c:out value="${existingReview.comment}"
                                       default="No written feedback"/>
                            </div>
                            <p>
                                <strong>Status:</strong>

                                <c:choose>
                                    <c:when test="${existingReview.reviewStatus eq 'approved'}">
                                        <span class="review-status status-approved">Approved</span>
                                    </c:when>

                                    <c:when test="${existingReview.reviewStatus eq 'pending'}">
                                        <span class="review-status status-pending">Pending</span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="review-status status-rejected">Rejected</span>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                            <a href="${pageContext.request.contextPath}/history" class="btn btn-outline-secondary"><ui:text key="screen.4f23f3773dbd070a"/></a>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/review" method="post">
                                <input type="hidden" name="booking-id" value="<c:out value='${bookingId}'/>">
                                <input type="hidden" name="room-number" value="<c:out value='${roomNumber}'/>">
                                <div class="mb-4">
                                    <label for="star" class="form-label fw-semibold"><ui:text key="screen.e02bce4b378f5766"/></label>
                                    <select name="star" class="form-select" required>
                                        <option value="5.0">5.0</option>
                                        <option value="4.5">4.5</option>
                                        <option value="4.0">4.0</option>
                                        <option value="3.5">3.5</option>
                                        <option value="3.0">3.0</option>
                                        <option value="2.5">2.5</option>
                                        <option value="2.0">2.0</option>
                                        <option value="1.5">1.5</option>
                                        <option value="1.0">1.0</option>
                                    </select>
                                </div>
                                <div class="mb-4">
                                    <label for="comment" class="form-label fw-semibold"><ui:text key="screen.61f84f05c17b6984"/></label>
                                    <textarea id="comment" name="comment" maxlength="1000" rows="5" class="form-control" placeholder="${requestScope.i18n['screen.1645e3a7c9399884']}" required data-i18n-placeholder="screen.1645e3a7c9399884"></textarea>
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
    </body>
</html>
