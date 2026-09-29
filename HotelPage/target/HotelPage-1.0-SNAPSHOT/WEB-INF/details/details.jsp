<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
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

            <div class="mx-auto mt-5" style="width: 70%; height: 500px;">
                <img src="<%= request.getContextPath()%>/assets/img/${room.roomType.picture}" class="w-100 h-100"/>
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

            <div class="mt-5">
                <div class="section-title">Customer Reviews</div>

                <c:if test="${empty reviews}">
                    <p class="text-muted">No reviews yet for this room.</p>
                </c:if>

                <c:forEach var="review" items="${reviews}">
                    <div class="review-box d-flex align-items-center bg-light-subtle">
                        <img width="45" height="45" class="rounded-circle border border-3 me-2 object-fit-cover"  src="<%= request.getContextPath()%>/assets/img/${review.booking.customer.avatar != null ? review.booking.customer.avatar : "avatar.jpg"}" alt="avatar">  
                        <div>
                            <strong>${review.booking.customer.user.username}</strong> 
                            <span class="text-warning">${review.star} ★</span><br>
                            <small class="text-muted">${review.comment}</small>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${loggedUser.role == 'customer'}">
                    <div class="section-title" id="review-title">Leave a Review</div>
                    <c:choose>
                        <c:when test="${param.error == 'not-book-yet'}">
                            <div class="alert alert-danger text-center">
                                You haven't booked or check out this room yet.
                            </div>
                        </c:when>
                        <c:when test="${param.error == 'already-review'}">
                            <div class="alert alert-danger text-center">
                                You have already reviewed for your last stay.
                            </div>
                        </c:when>
                    </c:choose>
                    <form action="review" method="post" class="mb-5">
                        <input type="hidden" name="room-number" value="${room.roomNumber}" />

                        <div class="mb-3">
                            <label class="form-label">Your Rating:</label>
                            <div id="rateYo"></div>
                            <input type="hidden" name="star" id="ratingValue">
                        </div>

                        <div class="mb-3">
                            <label for="comment" class="form-label">Your Comment</label>
                            <textarea name="comment" rows="3" class="form-control" required></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary">Submit Review</button>
                    </form>
                </c:if>
            </div>
        </div>

        <c:if test="${loggedUser.role == 'customer'}">
            <div class="position-fixed bottom-0 start-0 w-100 bg-white border-top shadow text-center py-3" style="z-index: 1050;">
                <div class="fw-bold fs-5 mb-2">Price: ${room.roomType.pricePerNight} USD / Night</div>
                <a href="./booking" class="btn btn-dark text-white">Booking now</a>
            </div>
        </c:if>

        <!-- JS Scripts -->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/rateYo/2.3.4/jquery.rateyo.min.js"></script>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script>
            $(function () {
                const defaultRating = 3.5;
                let roundedRating = Math.round(defaultRating * 2) / 2;

                // Initialize RateYo
                $("#rateYo").rateYo({
                    rating: roundedRating,
                    fullStar: false,
                    starWidth: "30px",
                    precision: 1, // allow half stars
                    step: 0.5
                });

                // Set default hidden input value
                $("#ratingValue").val(roundedRating);

                // Update hidden input on change
                $("#rateYo").on("rateyo.change", function (e, data) {
                    let newRating = Math.round(data.rating * 2) / 2;
                    $("#ratingValue").val(newRating);
                });
            });
        </script>
    </body>
</html>
