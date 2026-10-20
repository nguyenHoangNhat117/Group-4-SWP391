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
            .badge-soft-success { background-color: rgba(25, 135, 84, 0.1); color: #198754; }
            .badge-soft-warning { background-color: rgba(255, 193, 7, 0.15); color: #b08100; }
            .badge-soft-secondary { background-color: rgba(108, 117, 125, 0.1); color: #6c757d; }
            .btn-icon {
                width: 35px; height: 35px;
                display: inline-flex; align-items: center; justify-content: center;
                border-radius: 8px; transition: all 0.2s;
            }
            .btn-icon:hover { transform: translateY(-2px); }

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
                width: 100%; height: 100%;
                object-fit: cover;
                transition: transform 0.5s ease;
            }
            .room-card:hover .room-img-wrap img {
                transform: scale(1.05);
            }
            .room-badge {
                position: absolute;
                top: 15px; right: 15px;
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
                     CUSTOMER VIEW: DANH SÁCH THẺ PHÒNG
                     ========================================== --%>
                <c:otherwise>
                    <main class="container py-5">
                        
                        <!-- Search Bar Update -->
                        <div class="search-bar-wrapper mx-auto" style="max-width: 800px;">
                            <form class="d-flex align-items-center gap-3" method="post" action="${pageContext.request.contextPath}/room?action=search">
                                <div class="flex-grow-1 position-relative">
                                    <i class="bi bi-calendar-range position-absolute top-50 translate-middle-y ms-3 text-muted"></i>
                                    <input type="text" class="form-control form-control-lg bg-light border-0 ps-5" 
                                           id="floatingBookingDate" placeholder="${sessionScope.dates}" name="booking-dates" readonly required>
                                </div>
                                <button class="btn btn-lg btn-book text-nowrap px-4" type="submit">
                                    <i class="bi bi-search me-1"></i> Update Search
                                </button>
                            </form>
                        </div>

                        <!-- Room Cards Grid -->
                        <div class="row g-4 mb-5">
                            <c:forEach var="room" items="${rooms}">
                                <div class="col-md-6 col-lg-4">
                                    <div class="card room-card h-100">
                                        <div class="room-img-wrap">
                                            <img src="${pageContext.request.contextPath}/assets/img/${room.roomType.picture}" alt="${room.roomType.name}">
                                            <span class="room-badge"><i class="bi bi-door-closed me-1"></i> Room ${room.roomNumber}</span>
                                        </div>
                                        
                                        <div class="card-body d-flex flex-column p-4">
                                            <div class="d-flex justify-content-between align-items-start mb-2">
                                                <h5 class="fw-bold text-dark mb-0">${room.roomType.name}</h5>
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
                                                
                                                <c:if test="${empty sessionScope.loggedUser || sessionScope.loggedUser.role == 'customer'}">
                                                    <a href="./booking?roomNumber=${room.roomNumber}" class="btn btn-book text-decoration-none">
                                                        Book Now
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                            
                            <c:if test="${empty rooms}">
                                <div class="col-12 text-center py-5">
                                    <i class="bi bi-calendar-x display-1 text-muted mb-3"></i>
                                    <h4 class="fw-bold text-dark">No Rooms Available</h4>
                                    <p class="text-muted">Sorry, we couldn't find any rooms for the selected dates. Please try updating your search.</p>
                                </div>
                            </c:if>
                        </div>

                        <!-- Pagination Customer -->
                        <c:if test="${requestScope.totalPages > 1}">
                            <nav aria-label="Page navigation" class="mt-4">
                                <ul class="pagination justify-content-center pagination-lg">
                                    <li class="page-item ${param['page-index'] <= 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="./room?page-index=${param['page-index'] - 1}"><i class="bi bi-chevron-left"></i></a>
                                    </li>
                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <li class="page-item ${param['page-index'] == i ? 'active' : ''}">
                                            <a class="page-link" href="./room?page-index=${i}">${i}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${param['page-index'] >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="./room?page-index=${param['page-index'] + 1}"><i class="bi bi-chevron-right"></i></a>
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
                    tooltipText: { one: 'day', other: 'days' },
                    minDate: new Date()
                });
            }
        </script>
    </body>
</html>