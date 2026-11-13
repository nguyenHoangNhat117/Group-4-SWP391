<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta charset="UTF-8">
        <title><c:out value="${roomType.name}"/></title>

        <!-- Bootstrap & Icons -->
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css">

        <!-- Custom CSS -->
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
        <link rel="stylesheet" href="<c:url value='/assets/css/search-calendar.css'/>">

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
            .room-spec-card {
                display: flex;
                flex-direction: column;
                justify-content: center;
                gap: .3rem;
                min-height: 6rem;
                height: 100%;
                padding: .85rem .65rem;
                background: #f7f3ee;
                border: 1px solid #ede5dc;
                border-radius: .75rem;
                overflow-wrap: anywhere;
            }
            .room-amenity-card {
                border: 1px solid #ede5dc;
                background: #fcfaf7;
            }
            .room-amenity-icon {
                display: grid;
                place-items: center;
                flex: 0 0 2rem;
                height: 2rem;
                background: #f1e7d9;
                border-radius: .6rem;
                color: #79583c;
            }
            .availability-card {
                background: #ffffff;
                border-radius: 16px;
                box-shadow: 0 8px 25px rgba(0,0,0,0.06);
                border: 1px solid #eee;
            }
            .detail-price,
            .detail-total {
                color: #76583e;
            }
            .detail-quote {
                background: #f8f3ec;
                border: 1px solid #e8dfd2;
            }
            .detail-stock {
                background: #f8f3ec;
                color: #76583e;
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
    <body class="room-details-page">
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>

            <main class="container py-4 mb-5">
                <!-- Back Navigation & Header -->
                <div class="mb-3">
                    <a href="${pageContext.request.contextPath}/room?page-index=1" class="text-decoration-none text-secondary">
                        <i class="bi bi-arrow-left me-1"></i> <ui:text key="screen.6140b1324c02a169"/>
                    </a>
                </div>

                <!-- Review Submission Feedback Messages -->
                <c:if test="${param.error == 'review-pending'}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <ui:text key="screen.92c7aa32e0c960ae"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}" data-i18n-aria-label="screen.7d9eb7acb13e2462"></button>
                    </div>
                </c:if>
                <c:if test="${param.error == 'already-review'}">
                    <div class="alert alert-warning alert-dismissible fade show" role="alert">
                        <ui:text key="screen.82c1f7a7faf691d9"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}" data-i18n-aria-label="screen.7d9eb7acb13e2462"></button>
                    </div>
                </c:if>
                <c:if test="${param.error == 'not-book-yet'}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <ui:text key="screen.e45927062807c3b3"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}" data-i18n-aria-label="screen.7d9eb7acb13e2462"></button>
                    </div>
                </c:if>

                <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
                    <div>
                        <h1 class="fw-bold mb-2"><c:if test="${not empty room}"><ui:text key="screen.911ea89c43d9dbb8"/> ${room.roomNumber} &mdash; </c:if><c:out value="${roomType.name}"/></h1>
                        <div class="d-flex flex-wrap align-items-center text-muted gap-3">
                            <c:if test="${not empty room}">
                            <span>
                                <c:choose>
                                    <c:when test="${empty reviews}">
                                        <i class="bi bi-star text-muted me-1"></i><ui:text key="screen.539fd8609f5b572c"/>
                                    </c:when>
                                    <c:otherwise>
                                        <i class="bi bi-star-fill text-warning me-1"></i>
                                        <strong class="text-dark">${averageStar}</strong> / 5
                                        <span class="ms-1">(${reviews.size()} <ui:text key="screen.8718c7027494240d"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                            <span>&middot;</span>
                            </c:if>
                            <span><i class="bi bi-moon-stars me-1"></i>${roomType.beds} <ui:text key="screen.3ec3cf485f379517"/></span>
                            <span>&middot;</span>
                            <span class="badge rounded-pill detail-stock fs-6"><i class="bi bi-people me-1" aria-hidden="true"></i>${roomType.capacity} <ui:text key="room.guests.per.room"/></span>
                        </div>
                    </div>

                </div>

                <!-- Main Row: Image Gallery + Specifications (Left) & Availability/Booking Widget (Right) -->
                <div class="row g-4 mt-1 align-items-start">
                    <!-- Left Column: Image, Description, Amenities -->
                    <div class="col-lg-7">
                        <img src="${pageContext.request.contextPath}/assets/img/<c:out value='${roomType.picture}'/>"
                             class="img-fluid w-100 rounded-4 shadow-sm mb-4"
                             style="max-height: 480px; object-fit: cover;"
                             alt="<c:out value='${roomType.name}'/>"/>

                        <!-- Room Description -->
                        <div class="bg-white border rounded-4 p-4 shadow-sm mb-4">
                            <h4 class="fw-bold mb-3"><ui:text key="screen.de659431fe98de4e"/></h4>
                            <p class="text-secondary mb-3">
                                <c:out value="${not empty roomType.description ? roomType.description : 'Experience comfort and elegance in our well-appointed room.'}"/>
                            </p>
                            <div class="row g-3 text-center pt-2 border-top">
                                <div class="${empty roomSpecifications ? 'col-sm-4' : 'col-sm-6 col-xl-3'}">
                                    <div class="room-spec-card">
                                        <div class="text-muted small text-uppercase"><ui:text key="screen.bf193eab4a1aafb0"/></div>
                                        <div class="fs-5 fw-bold text-dark"><ui:money amount="${roomType.pricePerNight}"/></div>
                                    </div>
                                </div>
                                <div class="${empty roomSpecifications ? 'col-sm-4' : 'col-sm-6 col-xl-3'}">
                                    <div class="room-spec-card">
                                        <div class="text-muted small text-uppercase"><ui:text key="screen.2e2a3cd1a00809d3"/></div>
                                        <div class="fs-5 fw-bold text-dark">${roomType.beds} <ui:text key="screen.3ec3cf485f379517"/></div>
                                    </div>
                                </div>
                                <div class="${empty roomSpecifications ? 'col-sm-4' : 'col-sm-6 col-xl-3'}">
                                    <div class="room-spec-card">
                                        <div class="text-muted small text-uppercase"><ui:text key="screen.a641d8404ba319e8"/></div>
                                        <div class="fs-5 fw-bold text-dark">${roomType.capacity} <ui:text key="screen.3c8e0fde6fcc71e0"/></div>
                                    </div>
                                </div>
                                <c:if test="${not empty roomSpecifications}">
                                    <div class="col-sm-6 col-xl-3">
                                        <div class="room-spec-card">
                                            <div class="text-muted small text-uppercase"><ui:text key="room.spec.area"/></div>
                                            <div class="fs-5 fw-bold text-dark">${roomSpecifications.areaM2} m²</div>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <!-- Dynamic RoomType Amenities (Normal Flow Step 2) -->
                        <div class="bg-white border rounded-4 p-4 shadow-sm mb-4">
                            <h4 class="fw-bold mb-3"><ui:text key="screen.fa7a5f35ea119b26"/></h4>
                            <c:choose>
                                <c:when test="${not empty amenities}">
                                    <div class="row g-3">
                                        <c:forEach var="amenity" items="${amenities}">
                                            <div class="col-md-6">
                                                <div class="room-amenity-card d-flex align-items-center gap-3 p-3 rounded-3 h-100">
                                                    <span class="room-amenity-icon"><i class="bi bi-stars" aria-hidden="true"></i></span>
                                                    <div>
                                                        <c:set var="amenityNameKey" value="room.amenity.name.${amenity.name}"/>
                                                        <c:set var="amenityDescriptionKey" value="room.amenity.description.${amenity.description}"/>
                                                        <div class="fw-semibold text-dark" data-i18n="${not empty requestScope.i18n[amenityNameKey] ? amenityNameKey : ''}"><c:out value="${requestScope.i18n[amenityNameKey]}" default="${amenity.name}"/></div>
                                                        <c:if test="${not empty amenity.description}">
                                                            <small class="text-muted" data-i18n="${not empty requestScope.i18n[amenityDescriptionKey] ? amenityDescriptionKey : ''}"><c:out value="${requestScope.i18n[amenityDescriptionKey]}" default="${amenity.description}"/></small>
                                                        </c:if>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <p class="text-muted mb-0"><ui:text key="screen.f0b00dafda36d5eb"/></p>
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
                                    <span class="fs-2 fw-bold detail-price"><ui:money amount="${roomType.pricePerNight}"/></span>
                                    <span class="text-muted"><ui:text key="screen.9f93223e3ed44949"/></span>
                                </div>
                            </div>
                            <form action="<c:url value='/details'/>" method="post" id="details-stay-form"
                                  data-endpoint="<c:url value='/details'/>" data-capacity="${roomType.capacity}">
                                <input type="hidden" name="action" value="review">
                                <input type="hidden" name="roomTypeId" value="${roomType.id}">
                                <c:if test="${not empty room}">
                                    <input type="hidden" name="roomNumber" value="${room.roomNumber}">
                                </c:if>
                                <div class="row g-3 mb-3">
                                    <div class="col-12">
                                        <label for="detailBookingDates" class="form-label fw-semibold"><ui:text key="screen.c832b0d0d927dcff"/></label>
                                        <input class="form-control bg-light" id="detailBookingDates" type="text" readonly
                                               value="${dateRange}" data-calendar-url="<c:url value='/room'/>"
                                               aria-describedby="detail-calendar-status">
                                        <input type="hidden" name="checkIn" value="${checkInDate}">
                                        <input type="hidden" name="checkOut" value="${checkOutDate}">
                                        <small id="detail-calendar-status" class="visually-hidden" aria-live="polite"
                                               data-loading="${requestScope.i18n['room.calendar.loading']}"
                                               data-error="${requestScope.i18n['room.details.load.error']}"
                                               data-unavailable="${requestScope.i18n['room.calendar.unavailable']}"></small>
                                    </div>
                                    <div class="col-6">
                                        <label for="detailQuantity" class="form-label"><ui:text key="room.quantity"/></label>
                                        <input class="form-control" id="detailQuantity" type="number" name="quantity"
                                               min="1" max="${maxRooms > 0 ? maxRooms : 1}" value="${quantity}" required
                                               ${maxRooms == 0 ? 'readonly' : ''}
                                               aria-describedby="detailRoomLimit">
                                        <small id="detailRoomLimit" class="text-muted"><ui:text key="room.details.max"/> <span id="detailMaxRooms">${maxRooms}</span></small>
                                    </div>
                                    <div class="col-6">
                                        <label for="detailGuests" class="form-label"><ui:text key="room.guests.total"/></label>
                                        <input class="form-control" id="detailGuests" type="number" name="guests"
                                               min="${quantity}" max="${maxGuests}" value="${guestCount}" required
                                               ${maxRooms == 0 ? 'readonly' : ''}
                                               aria-describedby="detailGuestLimit">
                                        <small id="detailGuestLimit" class="text-muted"><ui:text key="room.details.max"/> <span id="detailMaxGuests">${maxGuests}</span></small>
                                    </div>
                                </div>

                                <div class="detail-quote rounded-3 p-3 mb-3" aria-live="polite" id="detailQuote"
                                     ${stayError == 'room.details.dates.error' ? 'hidden' : ''}>
                                    <div class="d-flex flex-wrap gap-2 justify-content-between fw-semibold text-dark mb-2">
                                        <span><strong id="detailNights">${nights}</strong> <ui:text key="room.nights"/></span>
                                        <span><strong id="detailAvailable">${available}</strong> <ui:text key="room.available"/></span>
                                    </div>
                                    <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center border-top pt-2">
                                        <span class="fw-bold text-dark"><ui:text key="room.estimated.total"/></span>
                                        <span class="fs-4 fw-bold detail-total" id="detailTotal">
                                            <c:choose>
                                                <c:when test="${stayError == 'room.quantity.error'}">&mdash;</c:when>
                                                <c:when test="${stayError == 'room.details.stock.error'}">&mdash;</c:when>
                                                <c:otherwise><ui:money amount="${totalStayPrice}"/></c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </div>
                                <div id="detailStatus" class="mb-3 text-secondary" aria-live="polite">
                                    <c:forEach var="message" items="${['room.details.dates.error', 'room.quantity.error',
                                               'room.details.stock.error', 'room.availability.error', 'room.calendar.loading', 'room.details.load.error']}">
                                        <div class="${message == 'room.calendar.loading' ? '' : 'text-danger'}"
                                             data-message="${message}" ${stayError == message ? '' : 'hidden'}><ui:text key="${message}"/></div>
                                    </c:forEach>
                                </div>

                                <c:choose>
                                    <c:when test="${empty sessionScope.loggedUser || sessionScope.loggedUser.role == 'customer'}">
                                        <button type="submit" id="detailReserve" class="btn btn-luxe btn-lg w-100 rounded-pill py-2"
                                                ${canBook ? '' : 'disabled'}>
                                            <i class="bi bi-calendar-check me-1" aria-hidden="true"></i> <ui:text key="room.reserve.now"/>
                                        </button>
                                    </c:when>
                                    <c:when test="${sessionScope.loggedUser.role == 'staff' && staffRoomAvailable}">
                                        <a href="<c:url value='/booking-management'/>" class="btn btn-luxe btn-lg w-100 rounded-pill py-2">
                                            <i class="bi bi-person-plus me-1" aria-hidden="true"></i> <ui:text key="screen.afe42350b046a3d9"/>
                                        </a>
                                    </c:when>
                                </c:choose>
                            </form>
                        </div>

                        <c:if test="${not empty room}">
                        <!-- Approved Customer Reviews Section (Normal Flow Step 3 & 4, BR-48) -->
                        <div class="bg-white border rounded-4 p-4 shadow-sm" id="review-title">
                            <h4 class="fw-bold mb-3">
                                <ui:text key="screen.e7cb00891c5b4234"/>
                                <c:if test="${not empty reviews}">
                                    <span class="text-muted fw-normal fs-6">(${reviews.size()})</span>
                                </c:if>
                            </h4>
                            <c:choose>
                                <c:when test="${empty reviews}">
                                    <p class="text-muted mb-0"><ui:text key="screen.f53aac7ea48e404f"/></p>
                                </c:when>
                                <c:otherwise>
                                    <div class="mb-3 text-warning fw-semibold">
                                        <i class="bi bi-star-fill"></i> ${averageStar} <ui:text key="screen.18fa6f5773c7b8cf"/>
                                    </div>
                                    <div style="max-height: 390px; overflow-y: auto;" aria-label="${requestScope.i18n['screen.8380c237806a24e2']}" data-i18n-aria-label="screen.8380c237806a24e2">
                                        <c:forEach var="review" items="${reviews}">
                                            <div class="review-box mb-2">
                                                <div class="d-flex align-items-center gap-2 mb-2">
                                                    <img width="40" height="40" class="rounded-circle object-fit-cover"
                                                         src="${pageContext.request.contextPath}/assets/img/${empty review.booking.customer.avatar ? 'avatar.jpg' : review.booking.customer.avatar}"
                                                         alt="${requestScope.i18n['screen.3c7f17938dfc7e9c']}" data-i18n-alt="screen.3c7f17938dfc7e9c"/>
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
                        </c:if>
                    </div>
                </div>
            </main>

            <%@include file="../include/footer.jsp" %>
        </div>

        <!-- JS Scripts -->
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js" defer></script>
        <script src="<c:url value='/assets/js/search-calendar.js'/>" defer></script>
        <script src="${pageContext.request.contextPath}/assets/js/room-details.js" defer></script>
    </body>
</html>
