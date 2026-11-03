<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Room Management & Availability - Luxe Hotel</title>

        <!-- CSS Dependencies -->
        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css">
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>

        <link href="${pageContext.request.contextPath}/assets/css/general.css" rel="stylesheet">

        <c:if test="${sessionScope.loggedUser.role == 'admin'}">
            <link href="${pageContext.request.contextPath}/assets/css/admin-list.css" rel="stylesheet">
        </c:if>

        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />

        <style>
            /* ==== ADMIN STYLES ==== */
            .admin-card {
                border: none;
                border-radius: 1rem;
                box-shadow: 0 4px 20px rgba(0,0,0,0.05);
                background: #fff;
            }
            .table-modern th {
                text-transform: uppercase;
                font-size: 0.8rem;
                letter-spacing: 0.5px;
                color: #6c757d;
                background-color: #f8f9fa;
                border-bottom: 2px solid #e9ecef;
                padding: 15px;
            }
            .table-modern td {
                vertical-align: middle;
                padding: 15px;
                border-bottom: 1px solid #f1f3f5;
            }
            .badge-soft-success {
                background-color: rgba(25, 135, 84, 0.1);
                color: #198754;
            }
            .badge-soft-warning {
                background-color: rgba(255, 193, 7, 0.15);
                color: #b08100;
            }
            .badge-soft-secondary {
                background-color: rgba(108, 117, 125, 0.1);
                color: #6c757d;
            }
            .btn-icon {
                width: 35px;
                height: 35px;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                border-radius: 8px;
                transition: all 0.2s;
            }
            .btn-icon:hover {
                transform: translateY(-2px);
            }

            /* ==== CUSTOMER STYLES ==== */
            .room-card {
                border: none;
                border-radius: 16px;
                overflow: hidden;
                transition: all 0.3s ease;
                box-shadow: 0 5px 15px rgba(0,0,0,0.08);
            }
            .room-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 15px 30px rgba(0,0,0,0.15);
            }
            .room-img-wrap {
                position: relative;
                height: 240px;
                overflow: hidden;
            }
            .room-img-wrap img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                transition: transform 0.5s ease;
            }
            .room-card:hover .room-img-wrap img {
                transform: scale(1.05);
            }
            .room-badge {
                position: absolute;
                top: 15px;
                right: 15px;
                background: rgba(255, 255, 255, 0.9);
                color: #212529;
                padding: 5px 12px;
                border-radius: 30px;
                font-weight: 600;
                font-size: 0.85rem;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            .room-price {
                font-size: 1.4rem;
                font-weight: 700;
                color: #b29575;
            }
            .btn-book {
                background-color: #b29575;
                border: none;
                color: white;
                padding: 10px 24px;
                border-radius: 50px;
                font-weight: 600;
                transition: all 0.3s;
            }
            .btn-book:hover {
                background-color: #9a7f60;
                color: white;
            }
            .search-bar-wrapper {
                background: #fff;
                padding: 20px;
                border-radius: 12px;
                box-shadow: 0 5px 20px rgba(0,0,0,0.05);
                margin-bottom: 30px;
            }

            .room-title-hover {
                transition: color 0.3s ease;
            }
            .room-title-hover:hover {
                color: #b29575 !important;
            }
        </style>
    </head>

    <body class="bg-light">
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>

            <c:choose>
                <%-- ==========================================
                     ADMIN VIEW: BẢNG QUẢN LÝ PHÒNG (DASHBOARD)
                     ========================================== --%>
                <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                    <main class="container py-5">

                        <!-- Header Area -->
                        <div class="d-flex justify-content-between align-items-end mb-4">
                            <div>
                                <p class="text-uppercase fw-bold text-muted mb-1" style="font-size: 0.85rem; letter-spacing: 1px;">Administration</p>
                                <h2 class="fw-bold text-dark mb-0">Room Inventory</h2>
                                <p class="text-secondary mt-2 mb-0">Manage hotel rooms, monitor capacities and operational status.</p>
                            </div>
                            <a class="btn btn-primary rounded-pill px-4 shadow-sm" style="background-color: #b29575; border-color: #b29575;"
                               href="${pageContext.request.contextPath}/room?view=create">
                                <i class="bi bi-plus-lg me-1"></i> Add New Room
                            </a>
                        </div>

                        <!-- Table Card -->
                        <div class="admin-card p-0 mb-4">
                            <div class="table-responsive">
                                <table class="table table-modern mb-0">
                                    <thead>
                                        <tr>
                                            <th scope="col">Room</th>
                                            <th scope="col">Type / Category</th>
                                            <th scope="col">Price per Night</th>
                                            <th scope="col" class="text-center">Beds</th>
                                            <th scope="col" class="text-center">Capacity</th>
                                            <th scope="col">Status</th>
                                            <th scope="col" class="text-end pe-4">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="room" items="${rooms}">
                                            <tr>
                                                <td>
                                                    <span class="fw-bold text-dark fs-5">#${room.roomNumber}</span>
                                                </td>
                                                <td>
                                                    <div class="fw-semibold text-dark">${room.roomType.name}</div>
                                                </td>
                                                <td class="fw-medium text-secondary">
                                                    $${room.roomType.pricePerNight}
                                                </td>
                                                <td class="text-center text-secondary">${room.roomType.beds}</td>
                                                <td class="text-center text-secondary"><i class="bi bi-people-fill me-1"></i>${room.roomType.capacity}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${room.status == 'available'}">
                                                            <span class="badge badge-soft-success px-3 py-2 rounded-pill">Available</span>
                                                        </c:when>
                                                        <c:when test="${room.status == 'maintenance'}">
                                                            <span class="badge badge-soft-warning px-3 py-2 rounded-pill">Maintenance</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-soft-secondary px-3 py-2 rounded-pill">Out of Service</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end pe-4">
                                                    <div class="d-flex justify-content-end gap-2">
                                                        <a href="${pageContext.request.contextPath}/details?roomNumber=${room.roomNumber}" 
                                                           class="btn btn-icon btn-light text-primary" data-bs-toggle="tooltip" title="View Details">
                                                            <i class="bi bi-eye"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/room?view=update&roomNumber=${room.roomNumber}" 
                                                           class="btn btn-icon btn-light text-warning" data-bs-toggle="tooltip" title="Edit Room">
                                                            <i class="bi bi-pencil-square"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/room?view=delete&roomNumber=${room.roomNumber}" 
                                                           class="btn btn-icon btn-light text-danger" data-bs-toggle="tooltip" title="Delete Room">
                                                            <i class="bi bi-trash"></i>
                                                        </a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>

                                        <c:if test="${empty rooms}">
                                            <tr>
                                                <td colspan="7" class="text-center py-5 text-muted">
                                                    <i class="bi bi-inboxes fs-1 d-block mb-3"></i>
                                                    No rooms found. Click "Add New Room" to get started.
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- Pagination Admin -->
                        <c:if test="${totalPages > 1}">
                            <nav aria-label="Room Pagination">
                                <ul class="pagination justify-content-end">
                                    <li class="page-item ${pageIndex <= 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/room?page-index=${pageIndex - 1}">Previous</a>
                                    </li>
                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <li class="page-item ${pageIndex == i ? 'active' : ''}">
                                            <a class="page-link" href="${pageContext.request.contextPath}/room?page-index=${i}">${i}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${pageIndex >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/room?page-index=${pageIndex + 1}">Next</a>
                                    </li>
                                </ul>
                            </nav>
                        </c:if>
                    </main>
                </c:when>

                <%-- ==========================================
                     CUSTOMER / GUEST / STAFF VIEW: DANH SÁCH THẺ PHÒNG (UC06)
                     ========================================== --%>
                <c:otherwise>
                    <main class="container py-5">

                        <!-- Validation Alerts (Exception 4a & 4b) -->
                        <c:if test="${param.error == 'missing-dates' || param.error == 'invalid-dates'}">
                            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                Check-out date must be later than Check-in date, and Check-in date cannot be in the past.
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>

                        <c:if test="${param.error == 'invalid-filter'}">
                            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                Please enter valid numeric filter values (Guest count must be greater than 0, and Minimum price cannot exceed Maximum price).
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>

                        <!-- Search & Refine Filter Bar (Normal Flow Step 2 & Alternative Flow 3a) -->
                        <div class="search-bar-wrapper mx-auto">
                            <form class="row g-3 align-items-end" method="post" action="${pageContext.request.contextPath}/room?action=search">
                                <input type="hidden" name="source" value="room">

                                <div class="col-lg-3 col-md-6">
                                    <label for="floatingBookingDate" class="form-label small fw-bold text-uppercase text-muted mb-1">Check-in &amp; Check-out</label>
                                    <div class="position-relative">
                                        <i class="bi bi-calendar-range position-absolute top-50 translate-middle-y ms-3 text-muted"></i>
                                        <input type="text"
                                               class="form-control bg-light ps-5 ${param.error == 'missing-dates' || param.error == 'invalid-dates' ? 'is-invalid border-danger' : 'border-0'}"
                                               id="floatingBookingDate"
                                               value="${sessionScope.dates}"
                                               placeholder="${sessionScope.dates}"
                                               name="booking-dates" readonly required>
                                    </div>
                                </div>

                                <div class="col-lg-1 col-md-3 col-6">
                                    <label for="guest-count" class="form-label small fw-bold text-uppercase text-muted mb-1">Guests</label>
                                    <input type="number" min="1" max="20"
                                           class="form-control bg-light ${param.error == 'invalid-filter' ? 'is-invalid border-danger' : 'border-0'}"
                                           id="guest-count" name="guest-count"
                                           value="${not empty sessionScope.guestCount ? sessionScope.guestCount : 1}" required>
                                </div>

                                <div class="col-lg-2 col-md-3 col-6">
                                    <label for="room-type-id" class="form-label small fw-bold text-uppercase text-muted mb-1">Room Type</label>
                                    <select class="form-select bg-light border-0" id="room-type-id" name="room-type-id">
                                        <option value="">All Types</option>
                                        <c:forEach var="rt" items="${requestScope.roomTypes}">
                                            <option value="${rt.id}" ${sessionScope.searchRoomTypeId == rt.id ? 'selected' : ''}>
                                                ${rt.name}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="col-lg-2 col-md-4 col-6">
                                    <label class="form-label small fw-bold text-uppercase text-muted mb-1">Price Range ($)</label>
                                    <div class="input-group">
                                        <input type="number" step="0.01" min="0"
                                               class="form-control bg-light ${param.error == 'invalid-filter' ? 'is-invalid border-danger' : 'border-0'}"
                                               name="min-price" placeholder="Min"
                                               value="${sessionScope.searchMinPrice}">
                                        <input type="number" step="0.01" min="0"
                                               class="form-control bg-light ${param.error == 'invalid-filter' ? 'is-invalid border-danger' : 'border-0'}"
                                               name="max-price" placeholder="Max"
                                               value="${sessionScope.searchMaxPrice}">
                                    </div>
                                </div>

                                <div class="col-lg-2 col-md-4 col-6">
                                    <label for="sort-by" class="form-label small fw-bold text-uppercase text-muted mb-1">Sort By</label>
                                    <select class="form-select bg-light border-0" id="sort-by" name="sort-by">
                                        <option value="room_asc" ${sessionScope.searchSortBy == 'room_asc' ? 'selected' : ''}>Room Number</option>
                                        <option value="price_asc" ${sessionScope.searchSortBy == 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
                                        <option value="price_desc" ${sessionScope.searchSortBy == 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
                                        <option value="capacity_desc" ${sessionScope.searchSortBy == 'capacity_desc' ? 'selected' : ''}>Capacity: High to Low</option>
                                    </select>
                                </div>

                                <div class="col-lg-2 col-md-4 col-12">
                                    <button class="btn btn-book w-100 text-nowrap py-2" type="submit">
                                        <i class="bi bi-search me-1"></i> Search
                                    </button>
                                </div>
                            </form>
                        </div>

                        <!-- Room Cards Grid -->
                        <div class="row g-4 mb-5">
                            <c:forEach var="room" items="${rooms}">
                                <div class="col-md-6 col-lg-4">
                                    <div class="card room-card h-100">
                                        <a href="${pageContext.request.contextPath}/details?roomNumber=${room.roomNumber}" class="room-img-wrap d-block text-decoration-none">
                                            <img src="${pageContext.request.contextPath}/assets/img/${room.roomType.picture}" alt="${room.roomType.name}">
                                            <span class="room-badge"><i class="bi bi-door-closed me-1"></i> Room ${room.roomNumber}</span>
                                        </a>

                                        <div class="card-body d-flex flex-column p-4">
                                            <div class="d-flex justify-content-between align-items-start mb-2">
                                                <h5 class="fw-bold text-dark mb-0">
                                                    <a href="${pageContext.request.contextPath}/details?roomNumber=${room.roomNumber}" class="text-dark text-decoration-none">
                                                        ${room.roomType.name}
                                                    </a>
                                                </h5>
                                                <!-- Public Approved Rating Summary (BR-48) -->
                                                <div class="small text-nowrap ms-2">
                                                    <c:choose>
                                                        <c:when test="${reviewCountMap[room.roomNumber] > 0}">
                                                            <i class="bi bi-star-fill text-warning"></i>
                                                            <span class="fw-semibold">${avgStarMap[room.roomNumber]}</span>
                                                            <span class="text-muted">(${reviewCountMap[room.roomNumber]})</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted small">No reviews</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>

                                            <p class="text-muted small mb-4" style="display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;">
                                                ${room.roomType.description}
                                            </p>

                                            <div class="d-flex gap-3 mb-4 mt-auto">
                                                <div class="text-muted small"><i class="bi bi-moon-stars me-1 text-primary"></i> ${room.roomType.beds} Beds</div>
                                                <div class="text-muted small"><i class="bi bi-people me-1 text-primary"></i> Up to ${room.roomType.capacity} Guests</div>
                                            </div>

                                            <div class="d-flex justify-content-between align-items-end border-top pt-3 mt-auto">
                                                <div>
                                                    <span class="d-block text-muted small" style="margin-bottom: -5px;">Starting from</span>
                                                    <span class="room-price">$${room.roomType.pricePerNight}</span>
                                                    <span class="text-muted small">/night</span>
                                                </div>

                                                <div class="d-flex gap-2">
                                                    <!-- Trigger UC07 View Room Details (Alternative Flow 6a) -->
                                                    <a href="${pageContext.request.contextPath}/details?roomNumber=${room.roomNumber}"
                                                       class="btn btn-outline-secondary rounded-pill px-3 py-2 fw-semibold">
                                                        Details
                                                    </a>
                                                    <c:if test="${empty sessionScope.loggedUser || sessionScope.loggedUser.role == 'customer'}">
                                                        <a href="./booking?roomNumber=${room.roomNumber}" class="btn btn-book text-decoration-none">
                                                            Book Now
                                                        </a>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>

                            <!-- Exception 6b: No Available Rooms Found -->
                            <c:if test="${empty rooms}">
                                <div class="col-12 text-center py-5">
                                    <i class="bi bi-calendar-x display-1 text-muted mb-3"></i>
                                    <h4 class="fw-bold text-dark">No Available Rooms Found</h4>
                                    <p class="text-muted">No available rooms found matching your criteria for the selected dates. Please try different dates or adjust your filters.</p>
                                </div>
                            </c:if>
                        </div>

                        <!-- Pagination Customer (BR-17 & Alternative Flow 3a) -->
                        <c:if test="${requestScope.totalPages > 1}">
                            <!-- THÊM class d-flex và justify-content-center TRỰC TIẾP VÀO THẺ NAV -->
                            <nav aria-label="Page navigation" class="mt-5 d-flex justify-content-center w-100">
                                <ul class="pagination pagination-lg mb-0">
                                    <li class="page-item ${pageIndex <= 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="./room?page-index=${pageIndex - 1}&guest-count=${sessionScope.guestCount}&room-type-id=${sessionScope.searchRoomTypeId}&min-price=${sessionScope.searchMinPrice}&max-price=${sessionScope.searchMaxPrice}&sort-by=${sessionScope.searchSortBy}"><i class="bi bi-chevron-left"></i></a>
                                    </li>
                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <li class="page-item ${pageIndex == i ? 'active' : ''}">
                                            <a class="page-link" href="./room?page-index=${i}&guest-count=${sessionScope.guestCount}&room-type-id=${sessionScope.searchRoomTypeId}&min-price=${sessionScope.searchMinPrice}&max-price=${sessionScope.searchMaxPrice}&sort-by=${sessionScope.searchSortBy}">${i}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${pageIndex >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="./room?page-index=${pageIndex + 1}&guest-count=${sessionScope.guestCount}&room-type-id=${sessionScope.searchRoomTypeId}&min-price=${sessionScope.searchMinPrice}&max-price=${sessionScope.searchMaxPrice}&sort-by=${sessionScope.searchSortBy}"><i class="bi bi-chevron-right"></i></a>
                                    </li>
                                </ul>
                            </nav>
                        </c:if>
                    </main>
                </c:otherwise>
            </c:choose>

            <%@include file="../include/footer.jsp" %>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <script>
            // Khởi tạo Tooltip của Bootstrap cho các nút Icon của Admin
            var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
            var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl);
            });

            // Khởi tạo lịch
            const dateInput = document.getElementById('floatingBookingDate');
            if (dateInput) {
                const picker = new Litepicker({
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
        </script>
    </body>
</html>