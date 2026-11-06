<%-- 
    Document   : offline
    Created on : Nov 5, 2026, 9:48:13 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="vi" class="uc09-page">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>Đặt phòng tại quầy | Luxe Escape</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/offline-booking.css?v=1">
    </head>

    <body class="offline-booking-page">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main class="ob-page">
                <div class="ob-heading">
                    <h1>Đặt phòng tại quầy</h1>

                    <a class="ob-back"
                       href="${pageContext.request.contextPath}/booking-management">
                        ← Danh sách đặt phòng
                    </a>
                </div>

                <ol class="ob-steps" aria-label="Các bước đặt phòng">
                    <li class="${empty quote ? 'is-current' : ''}">
                        <span>01</span> Lưu trú
                    </li>
                    <li>
                        <span>02</span> Khách hàng
                    </li>
                    <li class="${not empty quote ? 'is-current' : ''}">
                        <span>03</span> Xác nhận
                    </li>
                </ol>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger mb-4" role="alert">
                        <c:out value="${error}"/>
                    </div>
                </c:if>

                <c:choose>
                    <%-- Màn xác nhận --%>
                    <c:when test="${not empty quote}">
                        <div class="ob-grid">
                            <section class="ob-panel">
                                <div class="ob-panel-top">
                                    <h2>Xác nhận đặt phòng</h2>
                                    <span class="ob-number">03</span>
                                </div>

                                <div class="ob-review-room">
                                    <div class="ob-room-mark" aria-hidden="true">
                                        <svg viewBox="0 0 32 32">
                                        <path d="M5 24V10m22 14V10M5 20h22M5 14h22v6H5zm4 0V9h6v5m2 0V9h6v5"/>
                                        </svg>
                                    </div>

                                    <div>
                                        <strong>
                                            Phòng ${quote.draft.roomNumber}
                                        </strong>
                                        <span>
                                            ${quote.draft.guests} khách
                                            · ${quote.nights} đêm
                                        </span>
                                    </div>
                                </div>

                                <dl class="ob-review-details">
                                    <div class="ob-full">
                                        <dt>Khách hàng</dt>
                                        <dd>
                                            <c:out value="${quote.customerName}"/>
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Ngày nhận phòng</dt>
                                        <dd>${quote.draft.checkIn}</dd>
                                    </div>

                                    <div>
                                        <dt>Ngày trả phòng</dt>
                                        <dd>${quote.draft.checkOut}</dd>
                                    </div>

                                    <div>
                                        <dt>Đơn giá / đêm</dt>
                                        <dd>
                                            <fmt:formatNumber value="${quote.nightly}"/>
                                            VND
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Khuyến mãi</dt>
                                        <dd>
                                            <c:out value="${empty quote.draft.promotionCode
                                                            ? 'Không áp dụng'
                                                            : quote.draft.promotionCode}"/>
                                        </dd>
                                    </div>

                                    <div class="ob-full">
                                        <dt>Yêu cầu đặc biệt</dt>
                                        <dd>
                                            <c:out value="${empty quote.draft.specialRequest
                                                            ? 'Không có'
                                                            : quote.draft.specialRequest}"/>
                                        </dd>
                                    </div>
                                </dl>

                                <div class="ob-review-note">
                                    <a class="ob-back"
                                       href="${pageContext.request.contextPath}/offline-booking?edit=${quote.draft.requestKey}">
                                        ← Chỉnh sửa thông tin
                                    </a>
                                </div>
                            </section>

                            <aside class="ob-panel ob-summary">
                                <div class="ob-summary-head">
                                    <small>LUXE ESCAPE</small>
                                    <h2>Đặt phòng mới</h2>
                                </div>

                                <div class="ob-summary-body">
                                    <dl>
                                        <div class="ob-summary-row">
                                            <dt>Tiền phòng</dt>
                                            <dd>
                                                <fmt:formatNumber value="${quote.gross}"/>
                                                VND
                                            </dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt>Giảm dự kiến</dt>
                                            <dd>
                                                − <fmt:formatNumber value="${quote.gross - quote.payable}"/>
                                                VND
                                            </dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt>Giữ phòng</dt>
                                            <dd>${holdMinutes} phút</dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt>Cọc tối thiểu</dt>
                                            <dd>${depositPercent}%</dd>
                                        </div>
                                    </dl>

                                    <div class="ob-total">
                                        <span>Tổng dự kiến</span>
                                        <strong class="ob-price">
                                            <fmt:formatNumber value="${quote.payable}"/>
                                            VND
                                        </strong>
                                    </div>

                                    <form action="${pageContext.request.contextPath}/offline-booking"
                                          method="post">

                                        <input type="hidden"
                                               name="csrfToken"
                                               value="${sessionScope.csrfToken}">

                                        <input type="hidden"
                                               name="action"
                                               value="create">

                                        <input type="hidden"
                                               name="requestKey"
                                               value="${quote.draft.requestKey}">

                                        <button class="ob-button ob-primary"
                                                type="submit">
                                            Tạo đặt phòng
                                            <span aria-hidden="true">→</span>
                                        </button>
                                    </form>

                                    <p class="ob-note">
                                        Chưa thu tiền ở bước này.
                                        Khuyến mãi được kiểm tra lại khi thanh toán.
                                    </p>
                                </div>
                            </aside>
                        </div>
                    </c:when>

                    <%-- Màn nhập thông tin --%>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/offline-booking"
                              method="post"
                              id="offlineForm">

                            <input type="hidden"
                                   name="csrfToken"
                                   value="${sessionScope.csrfToken}">

                            <div class="ob-grid">
                                <div class="ob-stack">

                                    <section class="ob-panel">
                                        <div class="ob-panel-top">
                                            <h2>Lưu trú &amp; phòng</h2>
                                            <span class="ob-number">01</span>
                                        </div>

                                        <div class="ob-fields">
                                            <div class="ob-field">
                                                <label for="checkIn">
                                                    Ngày nhận phòng
                                                </label>

                                                <input class="ob-input"
                                                       type="date"
                                                       id="checkIn"
                                                       name="checkIn"
                                                       required
                                                       min="${today}"
                                                       value="${fn:escapeXml(form.checkIn)}">
                                            </div>

                                            <div class="ob-field">
                                                <label for="checkOut">
                                                    Ngày trả phòng
                                                </label>

                                                <input class="ob-input"
                                                       type="date"
                                                       id="checkOut"
                                                       name="checkOut"
                                                       required
                                                       min="${today}"
                                                       value="${fn:escapeXml(form.checkOut)}">
                                            </div>

                                            <div class="ob-search ob-full">
                                                <div class="ob-field">
                                                    <label for="guests">
                                                        Số khách
                                                    </label>

                                                    <input class="ob-input"
                                                           type="number"
                                                           id="guests"
                                                           name="guests"
                                                           min="1"
                                                           max="1000"
                                                           required
                                                           value="${fn:escapeXml(form.guests)}">
                                                </div>

                                                <button class="ob-button ob-secondary"
                                                        type="submit"
                                                        name="action"
                                                        value="search"
                                                        formnovalidate>
                                                    Tìm phòng trống
                                                    <span aria-hidden="true">→</span>
                                                </button>
                                            </div>
                                        </div>

                                        <c:if test="${searched and empty rooms}">
                                            <p class="ob-empty" role="status">
                                                Không có phòng phù hợp.
                                                Thử đổi ngày hoặc số khách.
                                            </p>
                                        </c:if>

                                        <div class="ob-field ob-room-field">
                                            <label for="roomNumber">Chọn phòng</label>

                                            <select class="ob-input"
                                                    id="roomNumber"
                                                    name="roomNumber"
                                                    required>

                                                <option value="">
                                                    — Chọn phòng —
                                                </option>

                                                <c:forEach items="${rooms}" var="room">
                                                    <option value="${room.roomNumber}"
                                                            data-price="${room.roomType.pricePerNight}"
                                                            data-label="${fn:escapeXml(room.roomType.name)}"
                                                            ${form.roomNumber == room.roomNumber
                                                              ? 'selected' : ''}>

                                                        Phòng ${room.roomNumber}
                                                        · <c:out value="${room.roomType.name}"/>
                                                        · ${room.roomType.capacity} khách
                                                        · <fmt:formatNumber value="${room.roomType.pricePerNight}"/>
                                                        VND/đêm
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </section>

                                    <section class="ob-panel">
                                        <div class="ob-panel-top">
                                            <h2>Khách hàng</h2>
                                            <span class="ob-number">02</span>
                                        </div>

                                        <div class="ob-mode"
                                             role="group"
                                             aria-label="Loại khách hàng">

                                            <label>
                                                <input type="radio"
                                                       name="customerMode"
                                                       value="existing"
                                                       ${form.customerMode != 'new'
                                                         ? 'checked' : ''}>
                                                <span>Khách có sẵn</span>
                                            </label>

                                            <label>
                                                <input type="radio"
                                                       name="customerMode"
                                                       value="new"
                                                       ${form.customerMode == 'new'
                                                         ? 'checked' : ''}>
                                                <span>+ Khách mới</span>
                                            </label>
                                        </div>

                                        <div data-customer-pane="existing">
                                            <div class="ob-search">
                                                <div class="ob-field">
                                                    <label for="q">
                                                        Điện thoại, email hoặc username
                                                    </label>

                                                    <input class="ob-input"
                                                           type="search"
                                                           id="q"
                                                           name="q"
                                                           maxlength="100"
                                                           value="${fn:escapeXml(form.q)}"
                                                           placeholder="Tìm khách hàng...">
                                                </div>

                                                <button class="ob-button ob-secondary"
                                                        type="submit"
                                                        name="action"
                                                        value="search"
                                                        formnovalidate>
                                                    Tìm khách
                                                </button>
                                            </div>

                                            <c:if test="${not empty form.q and empty customers}">
                                                <p class="ob-empty" role="status">
                                                    Chưa tìm thấy khách.
                                                    Chọn “Khách mới” để tạo hồ sơ.
                                                </p>
                                            </c:if>

                                            <div class="ob-field" style="margin-top:18px">
                                                <label for="customerId">
                                                    Hồ sơ khách hàng
                                                </label>

                                                <select class="ob-input"
                                                        name="customerId"
                                                        id="customerId">

                                                    <option value="">
                                                        — Chọn khách —
                                                    </option>

                                                    <c:forEach items="${customers}"
                                                               var="customer">
                                                        <option value="${customer.customerID}"
                                                                ${form.customerId == customer.customerID
                                                                  ? 'selected' : ''}
                                                                  ${customer.user.accountStatus != 'active'
                                                                    ? 'disabled' : ''}>

                                                                    <c:out value="${customer.lastName}
                                                                           ${customer.firstName}
                                                                           · ${customer.email}
                                                                           · ${customer.phone}
                                                                           · ${customer.user.accountStatus}"/>
                                                                </option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                            </div>

                                            <div data-customer-pane="new">
                                                <div class="ob-fields">
                                                    <div class="ob-field">
                                                        <label for="lastName">
                                                            Họ và tên đệm
                                                        </label>

                                                        <input class="ob-input"
                                                               id="lastName"
                                                               name="lastName"
                                                               maxlength="50"
                                                               autocomplete="family-name"
                                                               value="${fn:escapeXml(form.lastName)}"
                                                               placeholder="Nguyễn Hoàng">
                                                    </div>

                                                    <div class="ob-field">
                                                        <label for="firstName">Tên</label>

                                                        <input class="ob-input"
                                                               id="firstName"
                                                               name="firstName"
                                                               maxlength="50"
                                                               autocomplete="given-name"
                                                               value="${fn:escapeXml(form.firstName)}"
                                                               placeholder="Nhật">
                                                    </div>

                                                    <div class="ob-field">
                                                        <label for="email">Email</label>

                                                        <input class="ob-input"
                                                               type="email"
                                                               id="email"
                                                               name="email"
                                                               maxlength="100"
                                                               autocomplete="email"
                                                               value="${fn:escapeXml(form.email)}"
                                                               placeholder="khach@example.com">
                                                    </div>

                                                    <div class="ob-field">
                                                        <label for="phone">Điện thoại</label>

                                                        <input class="ob-input"
                                                               type="tel"
                                                               id="phone"
                                                               name="phone"
                                                               maxlength="11"
                                                               inputmode="numeric"
                                                               autocomplete="tel"
                                                               value="${fn:escapeXml(form.phone)}"
                                                               placeholder="0901234567">
                                                    </div>
                                                </div>
                                            </div>
                                        </section>

                                        <section class="ob-panel">
                                            <div class="ob-panel-top">
                                                <h2>Thông tin thêm</h2>
                                                <span class="ob-number">03</span>
                                            </div>

                                            <div class="ob-fields">
                                                <div class="ob-field ob-full">
                                                    <label for="promotionCode">
                                                        Mã khuyến mãi
                                                    </label>

                                                    <input class="ob-input"
                                                           name="promotionCode"
                                                           id="promotionCode"
                                                           maxlength="50"
                                                           value="${fn:escapeXml(form.promotionCode)}"
                                                           placeholder="Nhập mã nếu có">
                                                </div>

                                                <div class="ob-field ob-full">
                                                    <label for="specialRequest">
                                                        Yêu cầu đặc biệt
                                                    </label>

                                                    <textarea class="ob-input"
                                                              name="specialRequest"
                                                              id="specialRequest"
                                                              maxlength="500"
                                                              rows="3"
                                                              placeholder="Giờ đến dự kiến, yêu cầu khác..."><c:out value="${form.specialRequest}"/></textarea>
                                                </div>
                                            </div>
                                        </section>
                                    </div>

                                    <aside class="ob-panel ob-summary"
                                           aria-label="Tóm tắt đặt phòng">

                                        <div class="ob-summary-head">
                                            <small>LUXE ESCAPE</small>
                                            <h2 id="obRoom">Chọn phòng</h2>
                                            <p id="obType" class="ob-note">
                                                Thông tin đặt phòng
                                            </p>
                                        </div>

                                        <div class="ob-summary-body">
                                            <dl>
                                                <div class="ob-summary-row">
                                                    <dt>Lưu trú</dt>
                                                    <dd id="obDates">—</dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt>Số đêm</dt>
                                                    <dd id="obNights">—</dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt>Số khách</dt>
                                                    <dd id="obGuests">—</dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt>Giữ phòng</dt>
                                                    <dd>${holdMinutes} phút</dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt>Cọc tối thiểu</dt>
                                                    <dd>${depositPercent}%</dd>
                                                </div>
                                            </dl>

                                            <div class="ob-total">
                                                <span>Tiền phòng dự kiến</span>
                                                <strong class="ob-price"
                                                        id="obAmount"
                                                        aria-live="polite">
                                                    —
                                                </strong>
                                            </div>

                                            <button type="submit"
                                                    name="action"
                                                    value="preview"
                                                    class="ob-button ob-primary"
                                                    ${empty rooms ? 'disabled' : ''}>
                                                Xem tóm tắt
                                                <span aria-hidden="true">→</span>
                                            </button>

                                            <p class="ob-note">
                                                Khuyến mãi được tính ở bước tiếp theo.
                                            </p>

                                            <a class="ob-cancel"
                                               href="${pageContext.request.contextPath}/booking-management">
                                                Hủy thao tác
                                            </a>
                                        </div>
                                    </aside>
                                </div>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </main>

                <%@include file="/WEB-INF/include/footer.jsp"%>
            </div>

            <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
            <script src="${pageContext.request.contextPath}/assets/js/offline-booking.js?v=1" defer></script>
        </body>
    </html>