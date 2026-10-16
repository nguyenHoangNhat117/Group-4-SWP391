<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Hotel Rooms</title>

        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/room.css"
              rel="stylesheet">

        <link rel="stylesheet"
              href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css">
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>

        <c:if test="${sessionScope.loggedUser.role == 'admin'}">
            <link href="${pageContext.request.contextPath}/assets/css/admin-list.css"
                  rel="stylesheet">
            <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-rooms.css"
                  rel="stylesheet">
        </c:if>
    
    <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>

    <body class="luxe-admin-suite luxe-admin-rooms luxe-room-management-page">
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>

            <c:choose>
                <%-- ADMIN: BẢNG QUẢN LÝ --%>
                <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                    <main class="admin-list luxe-room-admin">
        <div class="luxe-admin-banner" aria-label="Room inventory">
          <span class="luxe-admin-banner-eyebrow">LUXE ESCAPE <span aria-hidden="true">/</span> ROOMS & AVAILABILITY</span>
          <span class="luxe-admin-banner-title">Room inventory</span>
          <span class="luxe-admin-banner-art" aria-hidden="true"></span>
        </div>

                        <div class="list-heading">
                            <div>
                                <div class="luxe-eyebrow"><span class="luxe-eyebrow-dot"></span> LUXE ESCAPE / ADMINISTRATION</div>
                                <h1>Room Management</h1>
                                <p class="text-muted mb-0">
                                    Manage room types, capacity and operational status.
                                </p>
                            </div>

                            <a class="btn btn-primary"
                               href="${pageContext.request.contextPath}/room?view=create">
                                <span aria-hidden="true" class="luxe-add-symbol">+</span> Add Room
                            </a>
                        </div>

                        <div class="table-panel">
                            <div class="luxe-table-topline"><div><span class="luxe-table-label">ROOM INVENTORY</span><span class="luxe-table-subtitle">Overview of rooms on this page</span></div><span class="luxe-table-accent" aria-hidden="true"></span></div>
                            <div class="table-responsive">
                                <table class="table table-hover align-middle">
                                    <caption class="visually-hidden">
                                        Rooms on the current page
                                    </caption>

                                    <thead>
                                        <tr>
                                            <th scope="col">Room Number</th>
                                            <th scope="col">Room Type</th>
                                            <th scope="col">Price / Night</th>
                                            <th scope="col">Beds</th>
                                            <th scope="col">Capacity</th>
                                            <th scope="col">Status</th>
                                            <th scope="col">Actions</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        <c:forEach var="room" items="${rooms}">
                                            <tr class="luxe-data-row">
                                                <td class="fw-semibold">
                                                    <span class="luxe-room-number">#${room.roomNumber}</span>
                                                </td>

                                                <td>
                                                    <c:out value="${room.roomType.name}"/>
                                                </td>

                                                <td>${room.roomType.pricePerNight} USD</td>
                                                <td>${room.roomType.beds}</td>
                                                <td>${room.roomType.capacity}</td>

                                                <td>
                                                    <c:choose>
                                                        <c:when test="${room.status == 'available'}">
                                                            <span class="badge bg-success">
                                                                Available
                                                            </span>
                                                        </c:when>

                                                        <c:when test="${room.status == 'maintenance'}">
                                                            <span class="badge bg-warning text-dark">
                                                                Maintenance
                                                            </span>
                                                        </c:when>

                                                        <c:otherwise>
                                                            <span class="badge bg-secondary">
                                                                Out of Service
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                <td>
                                                    <div class="row-actions">
                                                        <a class="btn btn-outline-secondary btn-sm"
                                                           href="${pageContext.request.contextPath}/details?roomNumber=${room.roomNumber}"
                                                           aria-label="Details of room ${room.roomNumber}">
                                                            Details
                                                        </a>

                                                        <a class="btn btn-outline-primary btn-sm"
                                                           href="${pageContext.request.contextPath}/room?view=update&roomNumber=${room.roomNumber}"
                                                           aria-label="Edit room ${room.roomNumber}">
                                                            Edit
                                                        </a>

                                                        <a class="btn btn-outline-danger btn-sm"
                                                           href="${pageContext.request.contextPath}/room?view=delete&roomNumber=${room.roomNumber}"
                                                           aria-label="Delete room ${room.roomNumber}">
                                                            Delete
                                                        </a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>

                                        <c:if test="${empty rooms}">
                                            <tr>
                                                <td colspan="7" class="empty-list">
                                                    No rooms found.
                                                    Add a room to get started.
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <c:if test="${totalPages > 1}">
                            <nav class="list-pagination" aria-label="Pagination">
                                <ul class="pagination mb-0">
                                    <li class="page-item ${pageIndex <= 1 ? 'disabled' : ''}">
                                        <c:choose>
                                            <c:when test="${pageIndex > 1}">
                                                <a class="page-link"
                                                   href="${pageContext.request.contextPath}/room?page-index=${pageIndex - 1}">
                                                    Previous
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="page-link">Previous</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </li>

                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <li class="page-item ${pageIndex == i ? 'active' : ''}">
                                            <c:choose>
                                                <c:when test="${pageIndex == i}">
                                                    <span class="page-link" aria-current="page">
                                                        ${i}
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a class="page-link"
                                                       href="${pageContext.request.contextPath}/room?page-index=${i}">
                                                        ${i}
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </li>
                                    </c:forEach>

                                    <li class="page-item ${pageIndex >= totalPages ? 'disabled' : ''}">
                                        <c:choose>
                                            <c:when test="${pageIndex < totalPages}">
                                                <a class="page-link"
                                                   href="${pageContext.request.contextPath}/room?page-index=${pageIndex + 1}">
                                                    Next
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="page-link">Next</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </li>
                                </ul>
                            </nav>
                        </c:if>
                    </main>
                </c:when>

                <%-- KHÁCH: CARD PHÒNG --%>
                <c:otherwise>
                    <main>
                        <div class="search-bar">
                            <form class="ms-auto w-50 d-flex justify-content-end"
                                  method="post"
                                  action="${pageContext.request.contextPath}/room?action=search">
                                <div class="w-50 text-center">
                                    <input type="text"
                                           class="form-control border-3 rounded-4 p-3"
                                           id="floatingBookingDate"
                                           placeholder="${sessionScope.dates}"
                                           name="booking-dates"
                                           readonly required>
                                </div>

                                <button class="update-button" type="submit">
                                    Update Search
                                </button>
                            </form>
                        </div>

                        <div class="hotel-list">
                            <c:forEach var="room" items="${rooms}">
                                <div class="hotel-card w-4">
                                    <a href="./details?roomNumber=${room.roomNumber}">
                                        <img class="img-fluid"
                                             src="${pageContext.request.contextPath}/assets/img/${room.roomType.picture}"
                                             alt="${room.roomType.name}">
                                    </a>

                                    <div class="hotel-details">
                                        <div class="hotel-name">
                                            Room ${room.roomNumber} (${room.roomType.name})
                                        </div>

                                        <div class="hotel-info">
                                            ${room.roomType.description}
                                        </div>

                                        <a href="./details?roomNumber=${room.roomNumber}"
                                           class="details-link">
                                            More details
                                        </a>

                                        <div class="price-rate">
                                            <div class="price">
                                                ${room.roomType.pricePerNight} USD / Night
                                            </div>

                                            <c:if test="${empty sessionScope.loggedUser
                                                          || sessionScope.loggedUser.role == 'customer'}">
                                                <a class="view-button text-decoration-none"
                                                   href="./booking?roomNumber=${room.roomNumber}">
                                                    Book
                                                </a>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </main>

                    <c:if test="${requestScope.totalPages > 1}">
                        <nav aria-label="Page navigation"
                             class="d-flex w-100 justify-content-center">
                            <ul class="pagination">
                                <li class="page-item ${param['page-index'] == 1 ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="./room?page-index=${param['page-index'] - 1}">
                                        Previous
                                    </a>
                                </li>

                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item">
                                        <a class="page-link ${param['page-index'] == i ? 'active' : ''}"
                                           href="./room?page-index=${i}">
                                            ${i}
                                        </a>
                                    </li>
                                </c:forEach>

                                <li class="page-item ${param['page-index'] == totalPages ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="./room?page-index=${param['page-index'] + 1}">
                                        Next
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </c:if>
                </c:otherwise>
            </c:choose>

            <%@include file="../include/footer.jsp" %>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>

        <script>
            const dateInput = document.getElementById('floatingBookingDate');

            // Admin không có ô chọn ngày nên chỉ tạo picker khi ô này tồn tại.
            if (dateInput) {
                const picker = new Litepicker({
                    element: dateInput,
                    singleMode: false,
                    numberOfMonths: 2,
                    numberOfColumns: 2,
                    autoApply: true,
                    format: 'MMM D, YYYY',
                    tooltipText: {
                        one: 'day',
                        other: 'days'
                    },
                    minDate: new Date()
                });
            }
        </script>
    </body>
</html>