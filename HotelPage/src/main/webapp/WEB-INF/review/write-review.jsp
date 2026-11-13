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
    </head>
    <body>
        <%@include file="/WEB-INF/include/header.jsp" %>
        <main class="container py-5" style="max-width:760px">
            <a href="${pageContext.request.contextPath}/history" class="text-decoration-none"><ui:text key="screen.75365a2c3de4d111"/></a>
            <div class="card border-0 shadow-sm rounded-4 mt-4">
                <div class="card-body p-4 p-md-5">
                    <h1 class="h3 fw-bold mb-2"><c:choose><c:when test="${not empty existingReview}"><ui:text key="screen.113fcf1cbe6996d1"/></c:when><c:otherwise><ui:text key="screen.95627b899e25e113"/></c:otherwise></c:choose></h1>
                    <p class="text-muted mb-4"><ui:text key="screen.8e9d3e1467f7cc3f"/><c:out value="${bookingId}"/> <ui:text key="screen.fc22788d41d69b67"/><c:out value="${roomNumber}"/></p>
                    <c:choose>
                        <c:when test="${not empty existingReview}">
                            <p><strong><ui:text key="screen.59cbcbca704be347"/></strong> <c:out value="${existingReview.star}"/> / 5 ★</p>
                            <p><strong><ui:text key="screen.c266ec8162232fcf"/></strong></p>
                            <div class="border rounded-3 p-3 mb-3" style="white-space:pre-wrap;word-break:break-word"><c:out value="${existingReview.comment}" default="No written feedback"/></div>
                            <p><strong><ui:text key="screen.755c8b2a9fb11446"/></strong> <c:out value="${existingReview.reviewStatus}"/></p>
                            <a href="${pageContext.request.contextPath}/history" class="btn btn-outline-secondary"><ui:text key="screen.4f23f3773dbd070a"/></a>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/review" method="post">
                                <input type="hidden" name="booking-id" value="<c:out value='${bookingId}'/>">
                                <input type="hidden" name="room-number" value="<c:out value='${roomNumber}'/>">
                                <div class="mb-4">
                                    <label for="star" class="form-label fw-semibold"><ui:text key="screen.e02bce4b378f5766"/></label>
                                    <select id="star" name="star" class="form-select" required>
                                        <option value="" selected disabled data-i18n="screen.dfdb45eb3953c304">${requestScope.i18n['screen.dfdb45eb3953c304']}</option>
                                        <option value="5">5 ★</option>
                                        <option value="4.5">4.5 ★</option>
                                        <option value="4">4 ★</option>
                                        <option value="3.5">3.5 ★</option>
                                        <option value="3">3 ★</option>
                                        <option value="2.5">2.5 ★</option>
                                        <option value="2">2 ★</option>
                                        <option value="1.5">1.5 ★</option>
                                        <option value="1">1 ★</option>
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
