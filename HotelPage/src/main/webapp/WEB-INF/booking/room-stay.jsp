<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="stay-page" lang="${requestScope.uiLanguage}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${roomType.name}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/general.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/room-checkout.css">
</head>
<body class="body-wrapper">
    <%@ include file="../include/header.jsp" %>
    <main class="stay-content">
      <div class="container stay-shell">
        <a href="<c:url value='/room?page-index=1'/>" class="stay-back"><i class="bi bi-arrow-left" aria-hidden="true"></i> <ui:text key="room.back.results"/></a>
        <div class="stay-heading">
            <div class="stay-brand">Luxe Escape</div>
            <h1><c:out value="${roomType.name}"/></h1>
        </div>
        <div class="row g-4">
            <div class="col-lg-7">
                <img class="stay-photo" src="${pageContext.request.contextPath}/assets/img/<c:out value='${roomType.picture}'/>"
                     alt="<c:out value='${roomType.name}'/>">
                <div class="stay-room-info">
                <p class="stay-description"><c:out value="${roomType.description}"/></p>
                <p class="stay-features"><i class="bi bi-moon-stars" aria-hidden="true"></i> ${roomType.beds} <ui:text key="screen.2e2a3cd1a00809d3"/> ·
                    ${roomType.capacity} <ui:text key="room.guests.per.room"/></p>
                <c:if test="${not empty amenities}">
                    <h2 class="h5"><ui:text key="room.amenities"/></h2>
                    <ul class="stay-amenities">
                        <c:forEach items="${amenities}" var="amenity">
                            <li><i class="bi bi-check2" aria-hidden="true"></i> <c:out value="${amenity.name}"/></li>
                        </c:forEach>
                    </ul>
                </c:if>
                </div>
            </div>
            <div class="col-lg-5">
                <section class="stay-panel stay-summary">
                    <div class="p-3 p-md-4">
                        <p class="stay-rate"><ui:money amount="${roomType.pricePerNight}"/>
                            <small class="text-muted"><ui:text key="screen.f8b08898c2e4a561"/></small></p>
                        <p class="stay-stock">${available} <ui:text key="room.available"/></p>
                        <p class="stay-dates"><i class="bi bi-calendar3" aria-hidden="true"></i> ${checkIn} → ${checkOut} · ${nights} <ui:text key="room.nights"/></p>
                        <c:if test="${param.error == 'quantity' || param.error == 'availability'}">
                            <p class="alert alert-warning" role="alert">
                                <c:choose>
                                    <c:when test="${param.error == 'quantity'}"><ui:text key="room.quantity.error"/></c:when>
                                    <c:otherwise><ui:text key="room.availability.error"/></c:otherwise>
                                </c:choose>
                            </p>
                        </c:if>
                        <c:if test="${available > 0 && (empty sessionScope.loggedUser || sessionScope.loggedUser.role == 'customer')}">
                                <form action="<c:url value='/checkout'/>" method="post" id="room-stay-form"
                                      data-nightly="${roomType.pricePerNight}" data-nights="${nights}" data-capacity="${roomType.capacity}">
                                    <input type="hidden" name="action" value="review">
                                    <input type="hidden" name="roomTypeId" value="${roomType.id}">
                                    <input type="hidden" name="checkIn" value="${checkIn}">
                                    <input type="hidden" name="checkOut" value="${checkOut}">
                                    <div class="row g-3 mb-3">
                                        <div class="col-6">
                                            <label class="form-label" for="quantity"><ui:text key="room.quantity"/></label>
                                            <input class="form-control" id="quantity" name="quantity" type="number"
                                                   min="1" max="${available}" value="1" required>
                                        </div>
                                        <div class="col-6">
                                            <label class="form-label" for="guests"><ui:text key="room.guests.total"/></label>
                                            <input class="form-control" id="guests" name="guests" type="number"
                                                   min="1" max="${roomType.capacity}" value="${empty sessionScope.guestCount
                                                       || sessionScope.guestCount > roomType.capacity ? 1 : sessionScope.guestCount}" required>
                                        </div>
                                    </div>
                                    <p class="stay-estimate d-flex justify-content-between flex-wrap gap-2"><ui:text key="room.estimated.total"/>
                                        <strong id="stay-total"><ui:money amount="${roomType.pricePerNight * nights}"/></strong></p>
                                    <button type="submit" class="btn stay-action w-100"><ui:text key="room.reserve.now"/> <i class="bi bi-arrow-right" aria-hidden="true"></i></button>
                                </form>
                        </c:if>
                    </div>
                </section>
            </div>
        </div>
      </div>
    </main>
    <%@ include file="../include/footer.jsp" %>
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    <script>
        const stayForm = document.getElementById('room-stay-form');
        if (stayForm) {
            const quantity = stayForm.elements.quantity;
            const guests = stayForm.elements.guests;
            const money = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
            function updateQuote() {
                const rooms = quantity.valueAsNumber;
                if (Number.isInteger(rooms) && rooms > 0) {
                    guests.min = rooms;
                    guests.max = Math.min(1000, rooms * Number(stayForm.dataset.capacity));
                    if (guests.valueAsNumber < rooms) {
                        guests.value = rooms;
                    }
                    document.getElementById('stay-total').textContent =
                        money.format(rooms * Number(stayForm.dataset.nightly) * Number(stayForm.dataset.nights));
                }
            }
            quantity.addEventListener('input', updateQuote);
            updateQuote();
        }
    </script>
</body>
</html>
