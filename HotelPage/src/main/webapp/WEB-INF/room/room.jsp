<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title data-i18n="screen.67a6f4f9750ef17b">${requestScope.i18n['screen.67a6f4f9750ef17b']}</title>

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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/search-calendar.css">
        <script src="${pageContext.request.contextPath}/assets/js/search-calendar.js" defer></script>
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
                                <p class="text-uppercase fw-bold text-muted mb-1" style="font-size: 0.85rem; letter-spacing: 1px;"><ui:text key="screen.4b42c6690ebeb867"/></p>
                                <h2 class="fw-bold text-dark mb-0"><ui:text key="screen.47ec390c932e0f88"/></h2>
                                <p class="text-secondary mt-2 mb-0"><ui:text key="screen.e669adda2b5a7a71"/></p>
                            </div>
                            <a class="btn btn-primary rounded-pill px-4 shadow-sm" style="background-color: #b29575; border-color: #b29575;"
                               href="${pageContext.request.contextPath}/room?view=create">
                                <i class="bi bi-plus-lg me-1"></i> <ui:text key="screen.3ff00245ea9cde06"/>
                            </a>
                        </div>

                        <c:if test="${not empty createdRooms}">
                            <div class="alert alert-success" role="status">
                                <div><ui:text key="room.admin.createdCount"/> <strong>${createdRooms.size()}</strong></div>
                                <details class="mt-2">
                                    <summary><ui:text key="room.admin.viewCreated"/></summary>
                                    <div class="d-flex flex-wrap gap-2 mt-2">
                                        <c:forEach var="createdNumber" items="${createdRooms}">
                                            <a class="btn btn-sm btn-outline-success"
                                               href="${pageContext.request.contextPath}/room?room-number=${createdNumber}">#${createdNumber}</a>
                                        </c:forEach>
                                    </div>
                                </details>
                            </div>
                        </c:if>

                        <form class="row g-3 align-items-end mb-4" method="get"
                              action="${pageContext.request.contextPath}/room">
                            <div class="col-sm-5 col-lg-4">
                                <label for="admin-room-type" class="form-label"><ui:text key="screen.82dea9a01c0033ec"/></label>
                                <select id="admin-room-type" name="room-type-id" class="form-select">
                                    <option value="" data-i18n="room.admin.allTypes">${requestScope.i18n['room.admin.allTypes']}</option>
                                    <c:forEach var="roomType" items="${roomTypes}">
                                        <option value="${roomType.id}" ${roomType.id == adminRoomTypeId ? 'selected' : ''}>
                                            <c:out value="${roomType.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-sm-4 col-lg-3">
                                <label for="admin-room-number" class="form-label"><ui:text key="room.admin.searchNumber"/></label>
                                <input id="admin-room-number" name="room-number" type="text" inputmode="numeric"
                                       class="form-control ${invalidRoomNumber ? 'is-invalid' : ''}"
                                       aria-invalid="${invalidRoomNumber}"
                                       value="<c:out value='${param["room-number"]}'/>">
                            </div>
                            <div class="col-sm-auto d-flex gap-2">
                                <button class="btn btn-primary" type="submit"><ui:text key="room.admin.applyFilters"/></button>
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/room">
                                    <ui:text key="room.admin.clearFilters"/>
                                </a>
                            </div>
                        </form>
                        <c:if test="${invalidRoomNumber}">
                            <p class="alert alert-danger" role="alert"><ui:text key="room.admin.invalidNumber"/></p>
                        </c:if>

                        <!-- Table Card -->
                        <div class="admin-card p-0 mb-4">
                            <div class="table-responsive">
                                <table class="table table-modern mb-0">
                                    <thead>
                                        <tr>
                                            <th scope="col"><ui:text key="screen.911ea89c43d9dbb8"/></th>
                                            <th scope="col"><ui:text key="screen.578ec0c553b6c947"/></th>
                                            <th scope="col"><ui:text key="screen.0702805f3bf73124"/></th>
                                            <th scope="col" class="text-center"><ui:text key="screen.2e2a3cd1a00809d3"/></th>
                                            <th scope="col" class="text-center"><ui:text key="screen.ae65d096550ff4eb"/></th>
                                            <th scope="col"><ui:text key="screen.920e413c7d411b61"/></th>
                                            <th scope="col" class="text-end pe-4"><ui:text key="screen.ff8059dc6752afdd"/></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="room" items="${rooms}">
                                            <tr>
                                                <td>
                                                    <span class="fw-bold text-dark fs-5">#${room.roomNumber}</span>
                                                </td>
                                                <td>
                                                    <div class="fw-semibold text-dark"><c:out value="${room.roomType.name}"/></div>
                                                </td>
                                                <td class="fw-medium text-secondary">
                                                    <ui:money amount="${room.roomType.pricePerNight}"/>
                                                </td>
                                                <td class="text-center text-secondary">${room.roomType.beds}</td>
                                                <td class="text-center text-secondary"><i class="bi bi-people-fill me-1"></i>${room.roomType.capacity}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${room.status == 'available'}">
                                                            <span class="badge badge-soft-success px-3 py-2 rounded-pill"><ui:text key="screen.e674447337e83c13"/></span>
                                                        </c:when>
                                                        <c:when test="${room.status == 'maintenance'}">
                                                            <span class="badge badge-soft-warning px-3 py-2 rounded-pill"><ui:text key="screen.17ccfa5b681e3e7b"/></span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-soft-secondary px-3 py-2 rounded-pill"><ui:text key="screen.3cd4b662003a9db7"/></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end pe-4">
                                                    <div class="d-flex justify-content-end gap-2">
                                                        <a href="${pageContext.request.contextPath}/details?roomNumber=${room.roomNumber}" 
                                                           class="btn btn-icon btn-light text-primary" data-bs-toggle="tooltip" title="${requestScope.i18n['screen.90789c12d0731b44']}" data-i18n-title="screen.90789c12d0731b44">
                                                            <i class="bi bi-eye"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/room?view=update&roomNumber=${room.roomNumber}" 
                                                           class="btn btn-icon btn-light text-warning" data-bs-toggle="tooltip" title="${requestScope.i18n['screen.1e15cc0c5d15cccb']}" data-i18n-title="screen.1e15cc0c5d15cccb">
                                                            <i class="bi bi-pencil-square"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/room?view=delete&roomNumber=${room.roomNumber}" 
                                                           class="btn btn-icon btn-light text-danger" data-bs-toggle="tooltip" title="${requestScope.i18n['screen.34e71958470b5088']}" data-i18n-title="screen.34e71958470b5088">
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
                                                    <c:choose>
                                                        <c:when test="${invalidRoomNumber || not empty adminRoomTypeId || not empty adminRoomNumber}">
                                                            <ui:text key="room.admin.noMatches"/>
                                                        </c:when>
                                                        <c:otherwise><ui:text key="screen.158872d668b1d6dc"/></c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- Pagination Admin -->
                        <c:if test="${totalPages > 1}">
                            <nav aria-label="${requestScope.i18n['screen.d75e66b013af28b8']}" data-i18n-aria-label="screen.d75e66b013af28b8">
                                <ul class="pagination justify-content-end">
                                    <li class="page-item ${pageIndex <= 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/room?page-index=${pageIndex - 1}&amp;room-type-id=${adminRoomTypeId}&amp;room-number=${adminRoomNumber}"><ui:text key="screen.a57b08a480b822a0"/></a>
                                    </li>
                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <li class="page-item ${pageIndex == i ? 'active' : ''}">
                                            <a class="page-link" href="${pageContext.request.contextPath}/room?page-index=${i}&amp;room-type-id=${adminRoomTypeId}&amp;room-number=${adminRoomNumber}">${i}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${pageIndex >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/room?page-index=${pageIndex + 1}&amp;room-type-id=${adminRoomTypeId}&amp;room-number=${adminRoomNumber}"><ui:text key="screen.1ff57a29d7c9d11b"/></a>
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
                                <ui:text key="screen.0fd3e982eaee6db5"/>
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}" data-i18n-aria-label="screen.7d9eb7acb13e2462"></button>
                            </div>
                        </c:if>

                        <c:if test="${param.error == 'invalid-filter'}">
                            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                <ui:text key="screen.5effc47a5bae9591"/>
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}" data-i18n-aria-label="screen.7d9eb7acb13e2462"></button>
                            </div>
                        </c:if>

                        <!-- Search & Refine Filter Bar (Normal Flow Step 2 & Alternative Flow 3a) -->
                        <div class="search-bar-wrapper mx-auto">
                            <form class="row g-3 align-items-end" method="post" action="${pageContext.request.contextPath}/room?action=search">
                                <input type="hidden" name="source" value="room">

                                <div class="col-lg-3 col-md-6">
                                    <label for="floatingBookingDate" class="form-label small fw-bold text-uppercase text-muted mb-1"><ui:text key="screen.c832b0d0d927dcff"/></label>
                                    <div class="position-relative">
                                        <i class="bi bi-calendar-range position-absolute top-50 translate-middle-y ms-3 text-muted"></i>
                                        <input type="text"
                                               class="form-control bg-light ps-5 ${param.error == 'missing-dates' || param.error == 'invalid-dates' ? 'is-invalid border-danger' : 'border-0'}"
                                               id="floatingBookingDate"
                                               value="${sessionScope.dates}"
                                               placeholder="${sessionScope.dates}"
                                               name="booking-dates" readonly required>
                                        <small id="search-calendar-status" class="visually-hidden" aria-live="polite"
                                           data-loading="${requestScope.i18n['room.calendar.loading']}"
                                           data-error="${requestScope.i18n['room.calendar.error']}"
                                           data-unavailable="${requestScope.i18n['room.calendar.unavailable']}"
                                           data-changed="${requestScope.i18n['room.calendar.changed']}"></small>
                                    </div>
                                </div>

                                <div class="col-lg-1 col-md-3 col-6">
                                    <label for="guest-count" class="form-label small fw-bold text-uppercase text-muted mb-1"><ui:text key="screen.3c8e0fde6fcc71e0"/></label>
                                    <input type="number" min="1" max="20"
                                           class="form-control bg-light ${param.error == 'invalid-filter' ? 'is-invalid border-danger' : 'border-0'}"
                                           id="guest-count" name="guest-count"
                                           value="${not empty sessionScope.guestCount ? sessionScope.guestCount : 1}" required>
                                </div>

                                <div class="col-lg-2 col-md-3 col-6">
                                    <label for="room-type-id" class="form-label small fw-bold text-uppercase text-muted mb-1"><ui:text key="screen.82dea9a01c0033ec"/></label>
                                    <select class="form-select bg-light border-0" id="room-type-id" name="room-type-id">
                                        <option value="" data-i18n="screen.bc01325482f36133">${requestScope.i18n['screen.bc01325482f36133']}</option>
                                        <c:forEach var="rt" items="${requestScope.roomTypes}">
                                            <option value="${rt.id}" ${sessionScope.searchRoomTypeId == rt.id ? 'selected' : ''}>
                                                ${rt.name}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="col-lg-2 col-md-4 col-6">
                                    <jsp:include page="/WEB-INF/room/price-band.jsp"/>
                                </div>

                                <div class="col-lg-2 col-md-4 col-6">
                                    <label for="sort-by" class="form-label small fw-bold text-uppercase text-muted mb-1"><ui:text key="screen.6d2227555fccff99"/></label>
                                    <select class="form-select bg-light border-0" id="sort-by" name="sort-by">
                                        <option value="room_asc" ${sessionScope.searchSortBy == 'room_asc' ? 'selected' : ''} data-i18n="screen.82dea9a01c0033ec">${requestScope.i18n['screen.82dea9a01c0033ec']}</option>
                                        <option value="price_asc" ${sessionScope.searchSortBy == 'price_asc' ? 'selected' : ''} data-i18n="screen.3044c75602f03114">${requestScope.i18n['screen.3044c75602f03114']}</option>
                                        <option value="price_desc" ${sessionScope.searchSortBy == 'price_desc' ? 'selected' : ''} data-i18n="screen.8abbc1606cfd7f90">${requestScope.i18n['screen.8abbc1606cfd7f90']}</option>
                                        <option value="capacity_desc" ${sessionScope.searchSortBy == 'capacity_desc' ? 'selected' : ''} data-i18n="screen.a980d50906f2f261">${requestScope.i18n['screen.a980d50906f2f261']}</option>
                                    </select>
                                </div>

                                <div class="col-lg-2 col-md-4 col-12">
                                    <button class="btn btn-book w-100 text-nowrap py-2" type="submit">
                                        <i class="bi bi-search me-1"></i> <ui:text key="screen.49c266baaaa70981"/>
                                    </button>
                                </div>
                            </form>
                        </div>

                        <div class="row g-4 mb-5">
                            <c:forEach var="result" items="${availableTypes}">
                                <c:set var="type" value="${result.type}"/>
                                <c:url var="detailsUrl" value="/details">
                                    <c:param name="roomTypeId" value="${type.id}"/>
                                </c:url>
                                <div class="col-md-6 col-lg-4">
                                    <article class="card room-card h-100">
                                        <a href="${detailsUrl}" class="room-img-wrap d-block text-decoration-none">
                                            <img src="${pageContext.request.contextPath}/assets/img/<c:out value='${type.picture}'/>" alt="<c:out value='${type.name}'/>">
                                            <span class="room-badge">${result.available} <ui:text key="room.available"/></span>
                                        </a>
                                        <div class="card-body d-flex flex-column p-4">
                                            <h5 class="fw-bold"><a class="text-dark text-decoration-none" href="${detailsUrl}"><c:out value="${type.name}"/></a></h5>
                                            <c:if test="${result.reviewCount > 0}">
                                                <div class="small mb-2"><i class="bi bi-star-fill text-warning"></i>
                                                    ${result.rating} <span class="text-muted">(${result.reviewCount})</span>
                                                </div>
                                            </c:if>
                                            <p class="text-muted small"><c:out value="${type.description}"/></p>
                                            <div class="text-muted small mb-3">
                                                ${type.beds} <ui:text key="screen.2e2a3cd1a00809d3"/> ·
                                                ${type.capacity} <ui:text key="screen.3c8e0fde6fcc71e0"/>
                                            </div>
                                            <div class="d-flex justify-content-between align-items-center border-top pt-3 mt-auto">
                                                <span><strong class="room-price"><ui:money amount="${type.pricePerNight}"/></strong> <ui:text key="screen.f8b08898c2e4a561"/></span>
                                                <a href="${detailsUrl}" class="btn btn-book"><ui:text key="screen.cb4759ed919a639c"/></a>
                                            </div>
                                        </div>
                                    </article>
                                </div>
                            </c:forEach>

                            <!-- Exception 6b: No Available Rooms Found -->
                            <c:if test="${empty availableTypes}">
                                <div class="col-12 text-center py-5">
                                    <i class="bi bi-calendar-x display-1 text-muted mb-3"></i>
                                    <h4 class="fw-bold text-dark"><ui:text key="screen.cbe53eabc103e3c8"/></h4>
                                    <p class="text-muted"><ui:text key="screen.2ce4d43e6899b3e3"/></p>
                                </div>
                            </c:if>
                        </div>

                        <!-- Pagination Customer (BR-17 & Alternative Flow 3a) -->
                        <c:if test="${requestScope.totalPages > 1}">
                            <!-- THÊM class d-flex và justify-content-center TRỰC TIẾP VÀO THẺ NAV -->
                            <nav aria-label="${requestScope.i18n['screen.024148d6099b7d63']}" class="mt-5 d-flex justify-content-center w-100" data-i18n-aria-label="screen.024148d6099b7d63">
                                <ul class="pagination pagination-lg mb-0">
                                    <li class="page-item ${pageIndex <= 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="./room?page-index=${pageIndex - 1}"><i class="bi bi-chevron-left"></i></a>
                                    </li>
                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <li class="page-item ${pageIndex == i ? 'active' : ''}">
                                            <a class="page-link" href="./room?page-index=${i}">${i}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${pageIndex >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="./room?page-index=${pageIndex + 1}"><i class="bi bi-chevron-right"></i></a>
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

        </script>
    </body>
</html>
