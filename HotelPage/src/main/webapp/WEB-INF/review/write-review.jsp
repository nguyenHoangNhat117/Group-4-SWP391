<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Write a Review</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/general.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/luxe-modern.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/luxe-ui.css" rel="stylesheet">
</head>
<body>
    <%@include file="/WEB-INF/include/header.jsp" %>
    <main class="container py-5" style="max-width:760px">
        <a href="${pageContext.request.contextPath}/history" class="text-decoration-none">&larr; Back to Booking History</a>
        <div class="card border-0 shadow-sm rounded-4 mt-4">
            <div class="card-body p-4 p-md-5">
                <h1 class="h3 fw-bold mb-2"><c:choose><c:when test="${not empty existingReview}">Your Review</c:when><c:otherwise>Write a Review</c:otherwise></c:choose></h1>
                <p class="text-muted mb-4">Booking #<c:out value="${bookingId}"/> &middot; Room #<c:out value="${roomNumber}"/></p>
                <c:choose>
                    <c:when test="${not empty existingReview}">
                        <p><strong>Rating:</strong> <c:out value="${existingReview.star}"/> / 5 ★</p>
                        <p><strong>Feedback:</strong></p>
                        <div class="border rounded-3 p-3 mb-3" style="white-space:pre-wrap;word-break:break-word"><c:out value="${existingReview.comment}" default="No written feedback"/></div>
                        <p><strong>Status:</strong> <c:out value="${existingReview.reviewStatus}"/></p>
                        <a href="${pageContext.request.contextPath}/history" class="btn btn-outline-secondary">Back to Booking History</a>
                    </c:when>
                    <c:otherwise>
                <form action="${pageContext.request.contextPath}/review" method="post">
                    <input type="hidden" name="booking-id" value="<c:out value='${bookingId}'/>">
                    <input type="hidden" name="room-number" value="<c:out value='${roomNumber}'/>">
                    <div class="mb-4">
                        <label for="star" class="form-label fw-semibold">Your Rating</label>
                        <select id="star" name="star" class="form-select" required>
                            <option value="" selected disabled>Select a rating</option>
                            <option value="5">★★★★★ — 5 stars</option>
                            <option value="4">★★★★☆ — 4 stars</option>
                            <option value="3">★★★☆☆ — 3 stars</option>
                            <option value="2">★★☆☆☆ — 2 stars</option>
                            <option value="1">★☆☆☆☆ — 1 star</option>
                        </select>
                    </div>
                    <div class="mb-4">
                        <label for="comment" class="form-label fw-semibold">Your Feedback</label>
                        <textarea id="comment" name="comment" maxlength="1000" rows="5" class="form-control" placeholder="Tell us about your stay..." required></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">Submit Review</button>
                    <a href="${pageContext.request.contextPath}/history" class="btn btn-outline-secondary ms-2">Cancel</a>
                </form>
                <p class="small text-muted mt-3 mb-0">Your review will appear publicly after admin approval.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </main>
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
