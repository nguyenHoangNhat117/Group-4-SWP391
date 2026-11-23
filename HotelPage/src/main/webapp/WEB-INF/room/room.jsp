<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi" class="${sessionScope.loggedUser.role == 'admin' ? '' : 'room-selection-page'}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${requestScope.i18n['screen.67a6f4f9750ef17b']}</title>

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
        <link rel="stylesheet" href="<c:url value='/assets/css/room-detail-content.css'/>">

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
            html.room-selection-page {
                scroll-snap-type: none;
                scroll-behavior: auto;
            }
            .room-card {
                border: none;
                border-radius: 16px;
                overflow: hidden;
                transition: box-shadow 0.3s ease;
                box-shadow: 0 5px 15px rgba(0,0,0,0.08);
            }
            .room-card:hover {
                box-shadow: 0 15px 30px rgba(0,0,0,0.15);
            }
            .room-card-specs {
                display: flex;
                flex-wrap: wrap;
                align-items: baseline;
                justify-content: space-between;
                gap: .375rem 1rem;
            }
            .room-card-specs > span {
                white-space: nowrap;
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
            .room-selection-layout {
                display: grid;
                grid-template-columns: minmax(0, 7fr) minmax(0, 3fr);
                gap: 24px;
                align-items: start;
            }
            .room-cart {
                --cart-top: 104px;
                position: sticky;
                top: var(--cart-top);
                max-height: calc(100vh - var(--cart-top) - 24px);
                overflow-y: auto;
                border-radius: 16px;
            }
            @media (max-width: 991.98px) {
                .room-selection-layout {
                    grid-template-columns: minmax(0, 1fr);
                }
                .room-cart {
                    position: static;
                    max-height: none;
                }
            }
            .room-quantity {
                display: inline-flex;
                align-items: center;
                gap: 8px;
            }
            .room-quantity output {
                min-width: 1.5rem;
                text-align: center;
            }
            .room-quantity button {
                width: 34px;
                height: 34px;
                padding: 0;
            }
            .room-cart-warning {
                color: #874500;
                background-color: #fff1dd;
            }
            .room-cart-party {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: .5rem;
                padding: .375rem .5rem .375rem .625rem;
                border-radius: .5rem;
                background: #f7f4ef;
                color: #51473e;
                font-size: .875rem;
            }
            .room-cart-remove {
                flex: none;
                width: 2rem;
                height: 2rem;
                padding: 0;
                line-height: 1;
            }
            html.room-selection-page body.modal-open .lx-top {
                visibility: hidden;
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
        <script type="module" src="<c:url value='/assets/js/room-selection.js?v=6'/>"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/management.css">
    <link rel="stylesheet" href="<c:url value='/assets/css/online-booking-select.css?v=4'/>">
</head>

    <body class="management-page bg-light ${sessionScope.loggedUser.role == 'admin' ? '' : 'disable-animation'}">
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>

            <c:choose>
                <%-- ==========================================
                     ADMIN VIEW: BẢNG QUẢN LÝ PHÒNG (DASHBOARD)
                     ========================================== --%>
                <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                    <main class="container py-5">

                        <!-- Header Area -->
                        <div class="management-hero">
                            <div>
                                <div class="eyebrow">LUXE ESCAPE</div>
                                <h2 class="fw-bold text-dark mb-0"><ui:text key="screen.47ec390c932e0f88"/></h2>
                                <p class="text-secondary mt-2 mb-0"><ui:text key="screen.e669adda2b5a7a71"/></p>
                                <p class="mt-2 mb-0">
                                    <ui:text key="room.admin.capacity.registered"/>
                                    <strong>${registeredRoomCount} / ${roomLimit}</strong>
                                    &middot; <ui:text key="room.admin.capacity.remaining"/> <strong>${remainingRoomSlots}</strong>
                                </p>
                            </div>
                            <div class="d-flex flex-wrap gap-2">
                                <button class="btn btn-outline-light rounded-pill px-4" type="button"
                                        data-bs-toggle="modal" data-bs-target="#room-limit-dialog">
                                    <ui:text key="room.admin.capacity.edit"/>
                                </button>
                                <c:choose>
                                    <c:when test="${remainingRoomSlots gt 0}">
                                        <a class="btn btn-primary rounded-pill px-4 shadow-sm" style="background-color: #b29575; border-color: #b29575;"
                                           href="${pageContext.request.contextPath}/room?view=create">
                                            <i class="bi bi-plus-lg me-1" aria-hidden="true"></i> <ui:text key="screen.3ff00245ea9cde06"/>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-light rounded-pill px-4" type="button" disabled>
                                            <ui:text key="room.admin.capacity.reached"/>
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${param['room-limit-result'] eq 'updated'}">
                                <p class="alert alert-success" role="status"><ui:text key="room.admin.capacity.updated"/></p>
                            </c:when>
                            <c:when test="${param['room-limit-result'] eq 'below-count'}">
                                <p class="alert alert-warning" role="alert"><ui:text key="room.admin.capacity.belowCount"/></p>
                            </c:when>
                            <c:when test="${param['room-limit-result'] eq 'invalid'}">
                                <p class="alert alert-warning" role="alert"><ui:text key="room.admin.capacity.invalid"/></p>
                            </c:when>
                            <c:when test="${param['room-limit-result'] eq 'failed'}">
                                <p class="alert alert-danger" role="alert"><ui:text key="room.admin.capacity.failed"/></p>
                            </c:when>
                        </c:choose>

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

                        <form class="row g-3 align-items-end mb-4 management-filters" method="get"
                              action="${pageContext.request.contextPath}/room">
                            <div class="col-sm-5 col-lg-4">
                                <label for="admin-room-type" class="form-label"><ui:text key="screen.82dea9a01c0033ec"/></label>
                                <select id="admin-room-type" name="room-type-id" class="form-select">
                                    <option value="">${requestScope.i18n['room.admin.allTypes']}</option>
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
                            <input type="hidden" name="sort" value="${priceSort}"><div class="col-sm-4 col-lg-3"><label for="admin-status" class="form-label"><ui:text key="screen.920e413c7d411b61"/></label><select id="admin-status" name="status" class="form-select"><option value="">${requestScope.i18n['management.all']}</option><option value="available" ${adminStatus == 'available' ? 'selected' : ''}>${requestScope.i18n['screen.e674447337e83c13']}</option><option value="maintenance" ${adminStatus == 'maintenance' ? 'selected' : ''}>${requestScope.i18n['screen.17ccfa5b681e3e7b']}</option><option value="out_of_service" ${adminStatus == 'out_of_service' ? 'selected' : ''}>${requestScope.i18n['screen.3cd4b662003a9db7']}</option></select></div><div class="col-sm-auto d-flex gap-2">
                                <button class="btn btn-primary" type="submit"><ui:text key="room.admin.applyFilters"/></button>
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/room">
                                    <ui:text key="room.admin.clearFilters"/>
                                </a>
                            </div>
                        </form>
                        <c:if test="${invalidRoomNumber}">
                            <p class="alert alert-danger" role="alert"><ui:text key="room.admin.invalidNumber"/></p>
                        </c:if>

                        <c:if test="${param.error == 'active-booking' || param.error == 'delete-failed'}">
                            <div class="alert alert-danger" role="alert">
                                <ui:text key="${param.error == 'active-booking' ? 'room.delete.activeBooking' : 'management.deleteFailed'}"/>
                                <c:if test="${param.error == 'active-booking' and not empty param.stayFrom and not empty param.stayTo}">
                                    <span class="d-block"><ui:text key="room.inventory.affectedStay"/> <c:out value="${param.stayFrom}"/> → <c:out value="${param.stayTo}"/></span>
                                </c:if>
                            </div>
                        </c:if>
<!-- Table Card -->
                        <div class="admin-card p-0 mb-4">
                            <div class="table-responsive">
                                <table class="table table-modern mb-0">
                                    <thead>
                                        <tr>
                                            <th scope="col"><ui:text key="screen.911ea89c43d9dbb8"/></th>
                                            <th scope="col"><ui:text key="screen.578ec0c553b6c947"/></th>
                                            <th scope="col"><button type="button" class="management-sort" data-sort><ui:text key="screen.0702805f3bf73124"/></button></th>
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
                                                           class="btn btn-icon btn-light text-primary" data-bs-toggle="tooltip" title="${requestScope.i18n['screen.90789c12d0731b44']}">
                                                            <i class="bi bi-eye"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/room?view=update&roomNumber=${room.roomNumber}"
                                                           class="btn btn-icon btn-light text-warning" data-bs-toggle="tooltip" title="${requestScope.i18n['screen.1e15cc0c5d15cccb']}">
                                                            <i class="bi bi-pencil-square"></i>
                                                        </a>
                                                        <form method="post" action="${pageContext.request.contextPath}/room?action=delete" data-room-number="${room.roomNumber}" data-confirm="${requestScope.i18n['management.deleteRoom']} #${room.roomNumber}?">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="room-number" value="${room.roomNumber}"><button type="submit" class="btn btn-icon btn-light text-danger" aria-label="${requestScope.i18n['screen.34e71958470b5088']}"><i class="bi bi-trash"></i></button></form>
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
                            <nav class="room-admin-pagination d-flex justify-content-center w-100 p-0" aria-label="${requestScope.i18n['screen.d75e66b013af28b8']}">
                                <ul class="pagination justify-content-center flex-wrap w-100 mb-0">
                                    <li class="page-item ${pageIndex <= 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/room?page-index=${pageIndex - 1}&amp;room-type-id=${adminRoomTypeId}&amp;room-number=${adminRoomNumber}&amp;status=${adminStatus}&amp;sort=${priceSort}"><ui:text key="screen.a57b08a480b822a0"/></a>
                                    </li>
                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <li class="page-item ${pageIndex == i ? 'active' : ''}">
                                            <a class="page-link" ${pageIndex == i ? 'aria-current="page"' : ''} href="${pageContext.request.contextPath}/room?page-index=${i}&amp;room-type-id=${adminRoomTypeId}&amp;room-number=${adminRoomNumber}&amp;status=${adminStatus}&amp;sort=${priceSort}">${i}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${pageIndex >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/room?page-index=${pageIndex + 1}&amp;room-type-id=${adminRoomTypeId}&amp;room-number=${adminRoomNumber}&amp;status=${adminStatus}&amp;sort=${priceSort}"><ui:text key="screen.1ff57a29d7c9d11b"/></a>
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
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}"></button>
                            </div>
                        </c:if>

                        <c:if test="${param.error == 'invalid-filter' || param.error == 'invalid-guests'}">
                            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                <ui:text key="${param.error == 'invalid-guests' ? 'room.search.party.invalid' : 'screen.5effc47a5bae9591'}"/>
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}"></button>
                            </div>
                        </c:if>

                        <!-- Search & Refine Filter Bar (Normal Flow Step 2 & Alternative Flow 3a) -->
                        <div class="search-bar-wrapper mx-auto">
                            <form class="row g-3 align-items-end pb-3" method="post" action="${pageContext.request.contextPath}/room?action=search">
                                <input type="hidden" name="source" value="room">

                                <div class="col-xl-4 col-lg-3 col-md-6">
                                    <label for="floatingBookingDate" class="form-label small fw-bold text-uppercase text-muted mb-1"><ui:text key="screen.c832b0d0d927dcff"/></label>
                                    <div class="position-relative">
                                        <i class="bi bi-calendar-range position-absolute top-50 translate-middle-y ms-3 text-muted"></i>
                                        <input type="text"
                                               class="form-control bg-light ps-5 ${param.error == 'missing-dates' || param.error == 'invalid-dates' ? 'is-invalid border-danger' : 'border-0'}"
                                               id="floatingBookingDate"
                                               value="${sessionScope.dates}"
                                               placeholder="${sessionScope.dates}"
                                               name="booking-dates" aria-describedby="search-calendar-status"
                                               data-submit-on-select readonly required>
                                        <small id="search-calendar-status" class="visually-hidden" aria-live="polite"
                                           data-loading="${requestScope.i18n['room.calendar.loading']}"
                                           data-error="${requestScope.i18n['room.calendar.error']}"
                                           data-unavailable="${requestScope.i18n['room.calendar.unavailable']}"
                                           data-changed="${requestScope.i18n['room.calendar.changed']}"></small>
                                    </div>
                                </div>

                                <div class="col-lg-2 col-md-3 col-6">
                                    <label for="selection-adults" class="form-label small fw-bold text-muted mb-1"><ui:text key="room.search.party.adults"/></label>
                                    <input type="number" min="1" step="1" class="form-control bg-light border-0"
                                           id="selection-adults" name="adults" value="${requestScope.searchAdults}" required>
                                </div>

                                <div class="col-lg-2 col-md-3 col-6">
                                    <label for="selection-children" class="form-label small fw-bold text-muted mb-1"><ui:text key="room.search.party.children"/></label>
                                    <input type="number" min="0" step="1" class="form-control bg-light border-0"
                                           id="selection-children" name="children" value="${requestScope.searchChildren}" required>
                                </div>

                                <div class="col-xl-2 col-lg-3 col-md-4 col-12">
                                    <label for="sort-by" class="form-label booking-select-label"><ui:text key="screen.6d2227555fccff99"/></label>
                                    <select class="form-select booking-select" id="sort-by" name="sort-by">
                                        <option value="price_asc" ${requestScope.searchSortBy == 'price_asc' ? 'selected' : ''}>${requestScope.i18n['screen.3044c75602f03114']}</option>
                                        <option value="price_desc" ${requestScope.searchSortBy == 'price_desc' ? 'selected' : ''}>${requestScope.i18n['screen.8abbc1606cfd7f90']}</option>
                                    </select>
                                </div>

                                <div class="col-lg-2 col-md-4 col-12">
                                    <button class="btn btn-book w-100 text-nowrap py-2" type="submit">
                                        <i class="bi bi-search me-1"></i> <ui:text key="room.selection.update"/>
                                    </button>
                                </div>
                            </form>
                        </div>

                        <div id="room-selection" class="room-selection-layout mb-5"
                             data-check-url="${pageContext.request.contextPath}/room?action=check-selection"
                             data-csrf-token="${requestScope.selectionCsrfToken}"
                             data-criteria="${requestScope.searchCriteria}"
                             data-stay="${sessionScope.checkInDate}|${sessionScope.checkOutDate}"
                             data-adults="${requestScope.searchAdults}" data-children="${requestScope.searchChildren}"
                             data-nights="${requestScope.searchNights}" data-price-order="${requestScope.searchSortBy}"
                             data-night-unit="${requestScope.i18n['room.search.party.nights']}"
                             data-room-unit="${requestScope.i18n['room.selection.roomUnit']}"
                             data-per-room="${requestScope.i18n['room.selection.perRoom']}"
                             data-empty="${requestScope.i18n['room.selection.empty']}"
                             data-sold-out="${requestScope.i18n['room.selection.soldOut']}"
                             data-available="${requestScope.i18n['room.available']}"
                             data-adult-unit="${requestScope.i18n['room.selection.adultUnit']}"
                             data-child-unit="${requestScope.i18n['room.selection.childUnit']}"
                             data-remove="${requestScope.i18n['room.selection.remove']}"
                             data-apply-search="${requestScope.i18n['room.selection.applySearch']}"
                             data-stock-changed="${requestScope.i18n['room.selection.stockChanged']}"
                             data-stay-changed="${requestScope.i18n['room.selection.stayChanged']}"
                             data-invalid="${requestScope.i18n['room.selection.invalid']}"
                             data-reload="${requestScope.i18n['room.selection.reload']}"
                             data-failed="${requestScope.i18n['room.selection.failed']}"
                             data-checking="${requestScope.i18n['room.selection.checking']}">
                            <section class="row g-4" data-room-catalog aria-label="${requestScope.i18n['room.selection.catalog']}">
                            <c:forEach var="result" items="${availableTypes}">
                                <c:set var="type" value="${result.type}"/>
                                <c:url var="pictureUrl" value="/assets/img/${empty type.picture ? 'avatar.jpg' : type.picture}"/>
                                <c:url var="previewUrl" value="/details">
                                    <c:param name="action" value="preview"/>
                                    <c:param name="roomTypeId" value="${type.id}"/>
                                </c:url>
                                <div class="col-12 col-md-6" data-room-type="${type.id}" data-available="${result.available}"
                                     data-capacity="${type.capacity}" data-max-children="${result.maxChildren}"
                                     data-nightly="${type.pricePerNight}" data-preview-url="${previewUrl}">
                                    <article class="card room-card h-100">
                                        <button type="button" class="room-img-wrap d-block w-100 p-0 border-0"
                                                data-preview-open aria-haspopup="dialog"
                                                aria-label="<c:out value="${requestScope.i18n['screen.cb4759ed919a639c']} ${type.name}"/>">
                                            <img src="<c:out value='${pictureUrl}'/>" alt="<c:out value='${type.name}'/>">
                                            <span class="room-badge" data-room-badge>
                                                <c:choose>
                                                    <c:when test="${result.available > 0}">${result.available} <ui:text key="room.available"/></c:when>
                                                    <c:otherwise><ui:text key="room.selection.soldOut"/></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </button>
                                        <div class="card-body d-flex flex-column p-3 p-sm-4">
                                            <h5 class="fw-bold">
                                                <button type="button" class="btn btn-link p-0 text-dark text-start text-decoration-none fw-bold"
                                                        data-room-name data-preview-open aria-haspopup="dialog"><c:out value="${type.name}"/></button>
                                            </h5>
                                            <c:if test="${result.reviewCount > 0}">
                                                <div class="small mb-2"><i class="bi bi-star-fill text-warning"></i>
                                                    ${result.rating} <span class="text-muted">(${result.reviewCount})</span>
                                                </div>
                                            </c:if>
                                            <p class="text-muted small"><ui:catalogText group="room.catalog.description" value="${type.description}"/></p>
                                            <div class="room-card-specs text-muted small mt-auto mb-3">
                                                <span><i class="bi bi-moon-stars me-1" aria-hidden="true"></i>${type.beds} <ui:text key="${type.beds == 1 ? 'room.catalog.one.bed' : 'screen.2e2a3cd1a00809d3'}"/></span>
                                                <span><i class="bi bi-people me-1" aria-hidden="true"></i><ui:text key="room.details.max"/>
                                                    ${type.capacity} <ui:text key="room.selection.adultUnit"/>
                                                    + ${result.maxChildren} <ui:text key="room.selection.childUnit"/>
                                                </span>
                                            </div>
                                            <div class="d-flex justify-content-between align-items-center border-top pt-3">
                                                <span><strong class="room-price"><ui:money amount="${type.pricePerNight}"/></strong> <ui:text key="screen.f8b08898c2e4a561"/></span>
                                                <div class="room-quantity" role="group" aria-label="<c:out value='${type.name}'/>">
                                                    <button type="button" class="btn btn-outline-secondary" data-quantity-minus
                                                            aria-label="${requestScope.i18n['room.selection.remove']}" disabled>&minus;</button>
                                                    <output data-quantity>0</output>
                                                    <button type="button" class="btn btn-outline-secondary" data-quantity-plus
                                                            aria-label="${requestScope.i18n['room.selection.add']}" disabled>+</button>
                                                </div>
                                            </div>
                                            <button type="button" class="btn btn-link align-self-start p-0 mt-3"
                                                    data-preview-open aria-haspopup="dialog"><ui:text key="screen.cb4759ed919a639c"/></button>
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
                            </section>

                            <aside class="room-cart card border-0 shadow-sm p-4" aria-labelledby="room-cart-heading">
                                <h2 id="room-cart-heading" class="h5 mb-3" tabindex="-1"><ui:text key="room.selection.cart"/></h2>
                                <ul class="list-unstyled mb-3" data-cart-lines></ul>
                                <div class="small d-flex justify-content-between gap-2 mb-2">
                                    <strong><ui:text key="room.selection.partyTotal"/></strong>
                                    <output data-cart-party aria-live="polite"></output>
                                </div>
                                <div class="border-top pt-3 mb-3 d-flex justify-content-between gap-2">
                                    <strong><ui:text key="room.selection.total"/></strong>
                                    <output data-cart-total aria-live="polite"><ui:money amount="${0}"/></output>
                                </div>
                                <p class="alert room-cart-warning small p-2" data-cart-warning aria-live="polite" hidden></p>
                                <p class="small" data-cart-notice aria-live="polite" hidden></p>
                                <button type="button" class="btn btn-book w-100" data-cart-continue disabled>
                                    <ui:text key="room.selection.continue"/>
                                </button>
                            </aside>
                        </div>
                    </main>
                </c:otherwise>
            </c:choose>

            <%@include file="../include/footer.jsp" %>
        </div>

        <c:if test="${sessionScope.loggedUser.role eq 'admin'}">
            <div class="modal fade" id="room-limit-dialog" tabindex="-1" aria-labelledby="room-limit-title" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <form class="modal-content" method="post" action="<c:url value='/room'/>">
                        <input type="hidden" name="action" value="update-room-limit">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <div class="modal-header">
                            <h2 class="modal-title h5" id="room-limit-title"><ui:text key="room.admin.capacity.edit"/></h2>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"
                                    aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}"></button>
                        </div>
                        <div class="modal-body">
                            <label class="form-label fw-semibold" for="room-limit"><ui:text key="room.admin.capacity.limit"/></label>
                            <input class="form-control" id="room-limit" name="room-limit" type="number"
                                   min="${registeredRoomCount gt 0 ? registeredRoomCount : 1}" max="2147483647"
                                   step="1" value="${roomLimit}" aria-describedby="room-limit-help" required>
                            <div class="form-text" id="room-limit-help">
                                <ui:text key="room.admin.capacity.registered"/> <strong>${registeredRoomCount}</strong>.
                                <ui:text key="room.admin.capacity.belowCount"/>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal"><ui:text key="common.cancel"/></button>
                            <button class="btn btn-primary" type="submit"><ui:text key="common.save"/></button>
                        </div>
                    </form>
                </div>
            </div>
        </c:if>

        <c:if test="${sessionScope.loggedUser.role != 'admin' and not empty availableTypes}">
            <div class="modal fade" id="room-preview" tabindex="-1" aria-labelledby="room-preview-title" aria-hidden="true"
                 data-loading="${requestScope.i18n['room.preview.loading']}"
                 data-failed="${requestScope.i18n['room.preview.failed']}"
                 data-limit="${requestScope.i18n['room.preview.limit']}"
                 data-adult-unit="${requestScope.i18n['room.selection.adultUnit']}"
                 data-child-unit="${requestScope.i18n['room.selection.childUnit']}">
                <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2 id="room-preview-title" class="modal-title h4"></h2>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"
                                    aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}"></button>
                        </div>
                        <div class="modal-body p-4">
                            <p class="mb-0" role="status" data-preview-status></p>
                            <div data-preview-content></div>
                        </div>
                        <div class="modal-footer gap-2">
                            <p class="text-muted small mb-0" data-preview-stock aria-live="polite"></p>
                            <button type="button" class="btn btn-book" data-preview-add disabled><ui:text key="room.preview.choose"/></button>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <script>
            // Khởi tạo Tooltip của Bootstrap cho các nút Icon của Admin
            var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
            var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl);
            });

        </script>
    <script src="${pageContext.request.contextPath}/assets/js/management.js"></script>
</body>
</html>
