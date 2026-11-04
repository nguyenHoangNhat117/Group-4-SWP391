<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta charset="UTF-8">
        <title>Room ${room.roomNumber} - ${room.roomType.name} | Luxe Escape</title>

        <!-- Bootstrap & Icons -->
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css" />
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>

        <!-- Custom CSS -->
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />

        <style>
            body {
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
            .availability-card {
                background: #ffffff;
                border-radius: 16px;
                box-shadow: 0 8px 25px rgba(0,0,0,0.06);
                border: 1px solid #eee;
            }
            .btn-luxe {
                background-color: #b29575;
                color: #fff;
                border: none;
                font-weight: 600;
                transition: all 0.3s ease;
            }
            .btn-luxe:hover:not(:disabled) {
                background-color: #9a7f60;
                color: #fff;
            }
            .btn-luxe:disabled {
                background-color: #ccc;
                color: #666;
                cursor: not-allowed;
            }
        </style>
    </head>
    <body>
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>

            <main class="container py-4 mb-5">
                <!-- Back Navigation & Header -->
                <div class="mb-3">
                    <a href="${pageContext.request.contextPath}/room?page-index=1" class="text-decoration-none text-secondary">
                        <i class="bi bi-arrow-left me-1"></i> Back to Search Results
                    </a>
                </div>

                <!-- Review Submission Feedback Messages -->
                <c:if test="${param.error == 'review-pending'}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        Your review has been submitted and is awaiting administrator approval.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                <c:if test="${param.error == 'already-review'}">
                    <div class="alert alert-warning alert-dismissible fade show" role="alert">
                        You have already submitted a review for this completed booking.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                <c:if test="${param.error == 'not-book-yet'}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        Only customers with a completed stay in this room can submit a review.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
                    <div>
                        <h2 class="fw-bold mb-1">Room ${room.roomNumber} &mdash; <c:out value="${room.roomType.name}"/></h2>
                        <div class="d-flex align-items-center text-muted gap-3">
                            <span>
                                <c:choose>
                                    <c:when test="${empty reviews}">
                                        <i class="bi bi-star text-muted me-1"></i>No approved reviews yet
                                    </c:when>
                                    <c:otherwise>
                                        <i class="bi bi-star-fill text-warning me-1"></i>
                                        <strong class="text-dark">${averageStar}</strong> / 5
                                        <span class="ms-1">(${reviews.size()} approved reviews)</span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                            <span>&middot;</span>
                            <span><i class="bi bi-moon-stars me-1"></i>${room.roomType.beds} Bed(s)</span>
                            <span>&middot;</span>
                            <span><i class="bi bi-people me-1"></i>Max Capacity: ${room.roomType.capacity} Guest(s)</span>
                        </div>
                    </div>

                    <!-- Real-time Availability Badge (Normal Flow Step 4) -->
                    <div>
                        <c:choose>
                            <c:when test="${isRoomAvailable}">
                                <span class="badge bg-success fs-6 px-3 py-2 rounded-pill">
                                    <i class="bi bi-check-circle me-1"></i> Available for Selected Dates
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-danger fs-6 px-3 py-2 rounded-pill">
                                    <i class="bi bi-x-circle me-1"></i> Unavailable for Selected Dates
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Main Row: Image Gallery + Specifications (Left) & Availability/Booking Widget (Right) -->
                <div class="row g-4 mt-1 align-items-start">
                    <!-- Left Column: Image, Description, Amenities -->
                    <div class="col-lg-7">
                        <img src="${pageContext.request.contextPath}/assets/img/${room.roomType.picture}"
                             class="img-fluid w-100 rounded-4 shadow-sm mb-4"
                             style="max-height: 480px; object-fit: cover;"
                             alt="Room ${room.roomNumber}"/>

                        <!-- Room Description -->
                        <div class="bg-white border rounded-4 p-4 shadow-sm mb-4">
                            <h4 class="fw-bold mb-3">Room Description &amp; Specifications</h4>
                            <p class="text-secondary mb-3">
                                <c:out value="${not empty room.roomType.description ? room.roomType.description : 'Experience comfort and elegance in our well-appointed room.'}"/>
                            </p>
                            <div class="row g-3 text-center pt-2 border-top">
                                <div class="col-sm-4">
                                    <div class="p-3 bg-light rounded-3">
                                        <div class="text-muted small text-uppercase">Price Per Night</div>
                                        <div class="fs-5 fw-bold text-dark">$${room.roomType.pricePerNight}</div>
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="p-3 bg-light rounded-3">
                                        <div class="text-muted small text-uppercase">Beds</div>
                                        <div class="fs-5 fw-bold text-dark">${room.roomType.beds} Bed(s)</div>
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="p-3 bg-light rounded-3">
                                        <div class="text-muted small text-uppercase">Max Capacity</div>
                                        <div class="fs-5 fw-bold text-dark">${room.roomType.capacity} Guests</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Dynamic RoomType Amenities (Normal Flow Step 2) -->
                        <div class="bg-white border rounded-4 p-4 shadow-sm mb-4">
                            <h4 class="fw-bold mb-3">Room Amenities</h4>
                            <c:choose>
                                <c:when test="${not empty amenities}">
                                    <div class="row g-3">
                                        <c:forEach var="amenity" items="${amenities}">
                                            <div class="col-md-6">
                                                <div class="d-flex align-items-start p-3 bg-light rounded-3 h-100">
                                                    <i class="bi bi-check2-circle text-success fs-5 me-2"></i>
                                                    <div>
                                                        <div class="fw-semibold text-dark"><c:out value="${amenity.name}"/></div>
                                                        <c:if test="${not empty amenity.description}">
                                                            <small class="text-muted"><c:out value="${amenity.description}"/></small>
                                                        </c:if>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <p class="text-muted mb-0">Standard hotel amenities are included with this room type.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Right Column: Availability Widget, Price Calculation & Reviews -->
                    <div class="col-lg-5">
                        <!-- Availability & Booking Widget (Normal Flow Step 4, Alternative Flow 4a & 4b, Exception 4c) -->
                        <div class="availability-card p-4 mb-4">
                            <div class="d-flex justify-content-between align-items-baseline border-bottom pb-3 mb-3">
                                <div>
                                    <span class="fs-3 fw-bold" style="color: #b29575;">$${room.roomType.pricePerNight}</span>
                                    <span class="text-muted">/ night</span>
                                </div>
                                <span class="text-muted small">Room #${room.roomNumber}</span>
                            </div>

                            <!-- Date Validation Error (BR-14) -->
                            <c:if test="${not empty dateError}">
                                <div class="alert alert-danger small py-2" role="alert">
                                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                                    <c:out value="${dateError}"/>
                                </div>
                            </c:if>

                            <!-- Exception 4c: Room Unavailable Notice -->
                            <c:if test="${not empty unavailableNotice}">
                                <div class="alert alert-warning small py-2" role="alert">
                                    <i class="bi bi-exclamation-circle-fill me-1"></i>
                                    <c:out value="${unavailableNotice}"/>
                                    <c:if test="${!isCapacitySatisfied}">
                                        <div class="mt-1 fw-semibold">
                                            (Requested guest count of ${guestCount} exceeds room capacity of ${room.roomType.capacity}.)
                                        </div>
                                    </c:if>
                                </div>
                            </c:if>

                            <!-- Alternative Flow 4a: Modify Check-in, Check-out, or Guest Count -->
                            <form action="${pageContext.request.contextPath}/details" method="get" class="mb-3">
                                <input type="hidden" name="roomNumber" value="${room.roomNumber}">

                                <div class="mb-3">
                                    <label for="detailBookingDates" class="form-label small fw-bold text-uppercase text-muted">
                                        Check-in &amp; Check-out Dates
                                    </label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-calendar3"></i></span>
                                        <input type="text"
                                               id="detailBookingDates"
                                               name="booking-dates"
                                               class="form-control ${not empty dateError ? 'is-invalid border-danger' : ''}"
                                               value="<c:out value='${sessionScope.dates}'/>"
                                               readonly required>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="detailGuestCount" class="form-label small fw-bold text-uppercase text-muted">
                                        Number of Guests (Max: ${room.roomType.capacity})
                                    </label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-people-fill"></i></span>
                                        <input type="number"
                                               id="detailGuestCount"
                                               name="guest-count"
                                               min="1"
                                               max="${room.roomType.capacity}" 
                                               class="form-control ${!isCapacitySatisfied ? 'is-invalid border-danger' : ''}"
                                               value="${guestCount > room.roomType.capacity ? room.roomType.capacity : guestCount}"
                                               required>
                                        <button type="submit" class="btn btn-outline-secondary fw-semibold">
                                            Check Availability
                                        </button>
                                    </div>
                                </div>
                            </form>

                            <!-- Calculated Stay Price Breakdown (Normal Flow Step 4 & Alternative Flow 4a) -->
                            <div class="bg-light rounded-3 p-3 mb-3">
                                <div class="d-flex justify-content-between small text-secondary mb-1">
                                    <span>Check-in: <strong>${checkInDate}</strong></span>
                                    <span>Check-out: <strong>${checkOutDate}</strong></span>
                                </div>
                                <div class="d-flex justify-content-between small text-secondary mb-2">
                                    <span>$${room.roomType.pricePerNight} &times; ${nights} night(s)</span>
                                    <span>Guests: ${guestCount}</span>
                                </div>
                                <div class="d-flex justify-content-between align-items-center border-top pt-2">
                                    <span class="fw-bold text-dark">Estimated Total Charge:</span>
                                    <span class="fs-5 fw-bold text-success">$${totalStayPrice}</span>
                                </div>
                            </div>

                            <!-- Alternative Flow 4b & Exception 4c: Action Button -->
                            <c:choose>
                                <c:when test="${empty sessionScope.loggedUser || sessionScope.loggedUser.role == 'customer'}">
                                    <c:choose>
                                        <c:when test="${isRoomAvailable}">
                                            <a href="${pageContext.request.contextPath}/booking?roomNumber=${room.roomNumber}"
                                               class="btn btn-luxe btn-lg w-100 rounded-pill py-2">
                                                <i class="bi bi-calendar-check me-1"></i> Book Now
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <button type="button" class="btn btn-luxe btn-lg w-100 rounded-pill py-2" disabled>
                                                <i class="bi bi-slash-circle me-1"></i> Book Now (Unavailable)
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>

                                <c:when test="${sessionScope.loggedUser.role == 'staff'}">
                                    <c:choose>
                                        <c:when test="${isRoomAvailable}">
                                            <a href="${pageContext.request.contextPath}/booking-management"
                                               class="btn btn-luxe btn-lg w-100 rounded-pill py-2">
                                                <i class="bi bi-person-plus me-1"></i> Create Offline Booking
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <button type="button" class="btn btn-luxe btn-lg w-100 rounded-pill py-2" disabled>
                                                <i class="bi bi-slash-circle me-1"></i> Room Unavailable
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                            </c:choose>
                        </div>

                        <!-- Approved Customer Reviews Section (Normal Flow Step 3 & 4, BR-48) -->
                        <div class="bg-white border rounded-4 p-4 shadow-sm" id="review-title">
                            <h4 class="fw-bold mb-3">
                                Approved Customer Reviews
                                <c:if test="${not empty reviews}">
                                    <span class="text-muted fw-normal fs-6">(${reviews.size()})</span>
                                </c:if>
                            </h4>
                            <c:choose>
                                <c:when test="${empty reviews}">
                                    <p class="text-muted mb-0">No approved reviews yet for this room.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="mb-3 text-warning fw-semibold">
                                        <i class="bi bi-star-fill"></i> ${averageStar} / 5 average rating
                                    </div>
                                    <div style="max-height: 390px; overflow-y: auto;" aria-label="Room customer reviews">
                                        <c:forEach var="review" items="${reviews}">
                                            <div class="review-box mb-2">
                                                <div class="d-flex align-items-center gap-2 mb-2">
                                                    <img width="40" height="40" class="rounded-circle object-fit-cover"
                                                         src="${pageContext.request.contextPath}/assets/img/${empty review.booking.customer.avatar ? 'avatar.jpg' : review.booking.customer.avatar}"
                                                         alt="Customer avatar"/>
                                                    <div>
                                                        <strong><c:out value="${review.booking.customer.user.username}"/></strong>
                                                        <div class="text-muted" style="font-size: 0.75rem;">
                                                            <c:out value="${review.reviewDate}"/>
                                                        </div>
                                                    </div>
                                                    <span class="ms-auto text-warning fw-bold"><c:out value="${review.star}"/> ★</span>
                                                </div>
                                                <p class="mb-0 text-secondary" style="overflow-wrap: anywhere;">
                                                    <c:out value="${review.comment}"/>
                                                </p>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </main>

            <%@include file="../include/footer.jsp" %>
        </div>

        <!-- JS Scripts -->
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script>
            document.addEventListener("DOMContentLoaded", function () {
                const dateInput = document.getElementById("detailBookingDates");
                if (dateInput) {
                    new Litepicker({
                        element: dateInput,
                        singleMode: false,
                        numberOfMonths: 2,
                        numberOfColumns: 2,
                        autoApply: true,
                        format: 'MMM D, YYYY',
                        tooltipText: {one: 'day', other: 'days'},
                        minDate: new Date()
                    });
                }
            });
        </script>
    </body>
</html>