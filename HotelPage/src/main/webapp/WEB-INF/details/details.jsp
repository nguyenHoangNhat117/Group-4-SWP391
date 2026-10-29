<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta charset="UTF-8">
        <title>Room Details</title>

        <!-- Bootstrap & Icons -->
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

        <!-- Custom CSS -->
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/rateYo/2.3.4/jquery.rateyo.min.css">

        <style>
            body {
                padding-bottom: 100px;
                background-color: #f8f9fa;
            }
            .section-title {
                margin-top: 30px;
                margin-bottom: 15px;
                font-weight: bold;
                font-size: 1.4rem;
            }
            .review-box {
                background-color: #ffffff;
                border: 1px solid #dee2e6;
                border-radius: 8px;
                padding: 15px;
                margin-bottom: 15px;
                box-shadow: 0 1px 3px rgba(0,0,0,0.05);
            }
        </style>
            <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
    </head>
    <body>
        <%@include file="../include/header.jsp" %>

        <div class="container mt-4">
            <h2 class="fw-bold">Room ${room.roomNumber} (${room.roomType.name})</h2>

            <div class="d-flex align-items-center text-muted">
                <div class="me-3 d-flex align-items-center">

                    <c:choose>
                        <c:when test="${empty reviews}">
                            <p class="text-muted">No reviews yet for this room.</p>
                        </c:when>  
                        <c:otherwise>
                            <i class="bi bi-star-fill text-warning me-1"></i>
                            ${averageStar} <span class="ms-1">(${reviews.size()} reviews)</span>
                        </c:otherwise>
                    </c:choose>
                </div>  
            </div>

            <!-- Room photo and approved reviews are visible together. -->
            <div class="row g-4 mt-2 align-items-start">
                <div class="col-lg-7">
                    <img src="${pageContext.request.contextPath}/assets/img/${room.roomType.picture}"
                         class="img-fluid w-100 rounded-3 shadow-sm"
                         style="max-height: 500px; object-fit: cover;" alt="Room ${room.roomNumber}"/>
                </div>
                <div class="col-lg-5">
                    <div class="bg-white border rounded-3 p-3 shadow-sm">
                        <h3 class="fs-5 fw-bold mb-3">Customer Reviews
                            <c:if test="${not empty reviews}">
                                <span class="text-muted fw-normal fs-6">(${reviews.size()})</span>
                            </c:if>
                        </h3>
                        <c:choose>
                            <c:when test="${empty reviews}">
                                <p class="text-muted mb-0">No approved reviews yet for this room.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="mb-3 text-warning fw-semibold">
                                    <i class="bi bi-star-fill"></i> ${averageStar} / 5
                                </div>
                                <div style="max-height: 390px; overflow-y: auto;" aria-label="Room customer reviews">
                                    <c:forEach var="review" items="${reviews}">
                                        <div class="review-box mb-2">
                                            <div class="d-flex align-items-center gap-2 mb-2">
                                                <img width="40" height="40" class="rounded-circle object-fit-cover"
                                                     src="${pageContext.request.contextPath}/assets/img/${empty review.booking.customer.avatar ? 'avatar.jpg' : review.booking.customer.avatar}"
                                                     alt="Customer avatar"/>
                                                <strong><c:out value="${review.booking.customer.user.username}"/></strong>
                                                <span class="ms-auto text-warning"><c:out value="${review.star}"/> ★</span>
                                            </div>
                                            <p class="mb-0 text-secondary" style="overflow-wrap: anywhere;"><c:out value="${review.comment}"/></p>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <div class="container mb-4">
                <div class="bg-light p-4 rounded text-center fw-bold">
                    <p>${room.roomType.beds} Bed - ${room.roomType.capacity} Sleeps</p>
                </div>
            </div>

            <div>
                <div class="section-title">Room Amenities</div>
                <div class="row g-3 justify-content-center">
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-wifi me-2"></i>Free high-speed internet</div></div>
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-car-front me-2"></i>Parking</div></div>
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-easel me-2"></i>Meeting event space</div></div>
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-droplet-half me-2"></i>Pool</div></div>
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-cash-coin me-2"></i>Cash machine/ATM</div></div>
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-airplane me-2"></i>Airport shuttle</div></div>
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-barbell me-2"></i>Fitness center</div></div>
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-heart-pulse me-2"></i>Full service spa</div></div>
                    <div class="col-md-3 col-6"><div class="bg-light p-2 rounded text-center"><i class="bi bi-house-heart me-2"></i>Housekeeping service twice daily</div></div>
                </div>
            </div>
        </div>

        <c:if test="${loggedUser.role == 'customer'}">
            <div class="position-fixed bottom-0 start-0 w-100 bg-white border-top shadow text-center py-3" style="z-index: 1050;">
                <div class="fw-bold fs-5 mb-2">Price: ${room.roomType.pricePerNight} USD / Night</div>
                <a href="./booking" class="btn btn-dark text-white">Booking now</a>
            </div>
        </c:if>

        <!-- JS Scripts -->
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
            <script src="<%= request.getContextPath()%>/assets/js/luxe-ui.js" defer></script>
    </body>
</html>
