<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : offline
    Created on : Nov 5, 2026, 9:48:13 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<fmt:setLocale value="${requestScope.uiLanguage == 'vi' ? 'vi_VN' : 'en_US'}" scope="page"/>
<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}" class="uc09-page">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title data-i18n="screen.5a779d440195be98">${requestScope.i18n['screen.5a779d440195be98']}</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/offline-booking.css?v=room-picker-2">
        <%@ include file="/WEB-INF/include/date-picker.jsp" %>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="<c:url value='/assets/css/booking-management.css?v=staff-calendar-2'/>">
        <link rel="stylesheet" href="<c:url value='/assets/css/management.css'/>">
    </head>

    <body class="offline-booking-page management-page">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main class="ob-page">
                <div class="management-hero">
                    <h1><ui:text key="screen.02c223c42181cf2b"/></h1>

                    <a class="btn btn-light"
                       href="${pageContext.request.contextPath}/booking-management">
                        <ui:text key="screen.c94d50d55899f838"/>
                    </a>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger mb-4" role="alert">
                        <ui:message text="${error}"/>
                    </div>
                </c:if>


                <nav class="d-flex gap-2 flex-wrap mb-3" aria-label="Đặt phòng tại quầy">
                    <a class="ob-button ${lookupView ? 'ob-secondary' : 'ob-primary'}"
                       ${lookupView ? '' : 'aria-current="page"'} href="<c:url value='/offline-booking'/>">Tạo đơn tại quầy</a>
                    <a class="ob-button ${lookupView ? 'ob-primary' : 'ob-secondary'}"
                       ${lookupView ? 'aria-current="page"' : ''} href="<c:url value='/offline-booking?view=lookup'/>">Tra đơn có sẵn</a>
                </nav>

                <c:if test="${lookupView}">
                    <section class="ob-panel">
                        <form class="oc-form" method="post" action="<c:url value='/offline-booking?view=lookup'/>">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="lookup">
                            <label for="bookingQuery">Mã đơn, số điện thoại hoặc tên khách</label>
                            <input class="ob-input" type="search" name="bookingQuery" id="bookingQuery"
                                   maxlength="120" required value="${fn:escapeXml(bookingQuery)}">
                            <button class="ob-button ob-primary mt-2" type="submit">Tra đơn</button>
                        </form>
                        <c:if test="${not empty bookingQuery and empty error}">
                            <c:choose>
                                <c:when test="${empty matchingBookings}">
                                    <p class="ob-note mt-3" role="status">Không tìm thấy đơn phù hợp.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="oc-table table-responsive mt-3">
                                        <table class="table">
                                            <thead><tr>
                                                    <th scope="col">Mã đơn</th><th scope="col">Khách</th>
                                                    <th scope="col">Ngày ở</th><th scope="col">Loại phòng</th>
                                                    <th scope="col">Trạng thái</th><th scope="col">Còn thiếu</th>
                                                </tr></thead>
                                            <tbody>
                                                <c:forEach items="${matchingBookings}" var="match">
                                                    <c:url var="openBooking" value="/counter-payment">
                                                        <c:param name="bookingId" value="${match.id}"/>
                                                    </c:url>
                                                    <tr>
                                                        <th scope="row"><a href="${openBooking}">#${match.id}</a></th>
                                                        <td><c:out value="${match.name}"/></td>
                                                        <td><c:out value="${match.dates}"/></td>
                                                        <td><c:out value="${match.roomTypes}" default="—"/></td>
                                                        <td><ui:text key="offline.state.${match.status}"/></td>
                                                        <td><ui:money amount="${match.remaining}"/></td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <c:if test="${fn:length(matchingBookings) == 30}">
                                        <p class="ob-note">Hiển thị tối đa 30 đơn. Nhập thêm thông tin để thu hẹp kết quả.</p>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </section>
                </c:if>

                <c:if test="${not lookupView}">
                    <p class="ob-note">Khách tại quầy chưa có tài khoản có thể xác minh email để kích hoạt và giữ lịch sử đặt phòng:
                        <a href="${pageContext.request.contextPath}/walkin-account" target="_blank" rel="noopener">Liên kết kích hoạt gửi cho khách</a>.
                        Khách mở liên kết trên thiết bị của mình, chưa đăng nhập. Không tự tạo tài khoản thay khách.
                    </p>
                    <form action="${pageContext.request.contextPath}/offline-booking"
                          method="post"
                          id="offlineForm" data-reviewed="${not empty quote}" data-uncertain="${uncertain}" data-quote-age="${quoteRemainingMillis}" data-search-key="${fn:escapeXml(searchedStay)}" data-quote-signature="${fn:escapeXml(quoteSignature)}">

                        <input type="hidden"
                               name="csrfToken"
                               value="${sessionScope.csrfToken}">
                        <input type="hidden" name="submissionToken" value="${submissionToken}">
                        <input type="hidden" name="quoteToken" value="${quoteToken}">
                        <p id="obSearchChanged" class="alert alert-warning" hidden>Ngày hoặc số khách đã đổi. Tìm lại phòng để tiếp tục.</p>

                        <div class="ob-grid">
                            <div class="ob-stack">

                                <section class="ob-panel">
                                    <div class="ob-panel-top">
                                        <h2><ui:text key="screen.8a25de2296303d74"/></h2>
                                    </div>

                                    <div class="ob-fields">
                                        <div class="ob-field">
                                            <label for="checkIn">
                                                <ui:text key="screen.5d7b2d63203faae8"/>
                                            </label>

                                            <div class="search-calendar-field">
                                                <i class="bi bi-calendar3" aria-hidden="true"></i>
                                                <input class="ob-input" data-booking-calendar
                                                       type="date"
                                                       id="checkIn"
                                                       name="checkIn"
                                                       required
                                                       min="${today}"
                                                       value="${fn:escapeXml(form.checkIn)}">
                                            </div>
                                        </div>

                                        <div class="ob-field">
                                            <label for="checkOut">
                                                <ui:text key="screen.1a51b94f713b1ee9"/>
                                            </label>

                                            <div class="search-calendar-field">
                                                <i class="bi bi-calendar3" aria-hidden="true"></i>
                                                <input class="ob-input" data-booking-calendar data-after-date="checkIn"
                                                       type="date"
                                                       id="checkOut"
                                                       name="checkOut"
                                                       required
                                                       min="${today}"
                                                       value="${fn:escapeXml(form.checkOut)}">
                                            </div>
                                        </div>

                                        <div class="ob-search ob-full">
                                            <div class="ob-field">
                                                <label for="guests">
                                                    <ui:text key="screen.3c8e0fde6fcc71e0"/>
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

                                            <div>
                                                <label for="quantity">Số phòng</label>
                                                <input class="ob-input" type="number" id="quantity" name="quantity"
                                                       min="1" max="1000" required value="${fn:escapeXml(form.quantity)}">
                                            </div>

                                            <button class="ob-button ob-secondary"
                                                    type="submit"
                                                    name="action"
                                                    value="search"
                                                    formnovalidate>
                                                <ui:text key="screen.40081e387cab1d5c"/>
                                                <span aria-hidden="true">→</span>
                                            </button>
                                        </div>
                                    </div>

                                    <c:if test="${searched and empty rooms}">
                                        <p class="ob-empty" role="status">
                                            <ui:text key="screen.1066e476402ece31"/>
                                        </p>
                                    </c:if>

                                    <c:if test="${not empty rooms}">
                                        <fieldset class="ob-room-field" id="obRoomPicker">
                                            <legend>Chọn loại phòng</legend>
                                            <label class="visually-hidden" for="obRoomSearch">Tìm theo tên loại phòng</label>
                                            <input class="ob-input" type="search" id="obRoomSearch" autocomplete="off"
                                                   placeholder="Tìm theo tên loại phòng">
                                            <div class="ob-room-results" id="obRoomResults">
                                                <c:forEach items="${rooms}" var="room">
                                                    <label class="ob-room-choice">
                                                        <input type="radio" name="roomTypeId" required
                                                               value="${room.type.id}" data-price="${room.type.pricePerNight}"
                                                               data-label="${fn:escapeXml(room.type.name)}"
                                                               data-available="${room.available}"
                                                               data-ready="${room.readyNow}"
                                                               ${form.roomTypeId == room.type.id ? 'checked' : ''}>
                                                        <span class="ob-room-info">
                                                            <strong class="ob-room-name"><c:out value="${room.type.name}"/></strong>
                                                            <span class="ob-room-meta">
                                                                <span class="ob-room-stock">Còn ${room.available} phòng</span>
                                                                <span>Tối đa ${room.type.capacity} khách/phòng</span>
                                                                <c:if test="${room.readyNow ne null}">
                                                                    <span>${room.readyNow} phòng sẵn sàng ngay</span>
                                                                </c:if>
                                                            </span>
                                                        </span>
                                                        <span class="ob-room-price">
                                                            <strong><ui:money amount="${room.type.pricePerNight}"/></strong>
                                                            <small>/ đêm</small>
                                                        </span>
                                                    </label>
                                                </c:forEach>
                                            </div>
                                            <p class="ob-note" id="obReadinessWarning" role="status" hidden>
                                                Loại phòng đã chọn chưa có đủ phòng sẵn sàng để nhận ngay. Chọn loại khác hoặc ngày khác.
                                            </p>
                                            <p class="ob-note" id="obNoMatchingRooms" hidden><ui:text key="offline.noMatchingRooms"/></p>
                                            <div class="ob-room-pagination">
                                                <button type="button" class="ob-button ob-secondary" id="obRoomsPrevious" aria-controls="obRoomResults" aria-label="Trang trước"><i class="bi bi-chevron-left" aria-hidden="true"></i></button>
                                                <span id="obRoomRange" role="status"></span>
                                                <button type="button" class="ob-button ob-secondary" id="obRoomsNext" aria-controls="obRoomResults" aria-label="Trang sau"><i class="bi bi-chevron-right" aria-hidden="true"></i></button>
                                            </div>
                                        </fieldset>
                                    </c:if>
                                </section>

                                <section class="ob-panel">
                                    <div class="ob-panel-top">
                                        <h2><ui:text key="screen.bf3763383aaf4306"/></h2>
                                    </div>

                                    <div class="ob-mode"
                                         role="group"
                                         aria-label="${requestScope.i18n['screen.4ac86ab4f546c113']}" data-i18n-aria-label="screen.4ac86ab4f546c113">

                                        <label>
                                            <input type="radio"
                                                   name="customerMode"
                                                   value="existing"
                                                   ${form.customerMode != 'new'
                                                     ? 'checked' : ''}>
                                            <span><ui:text key="screen.b941d2696ce7958d"/></span>
                                        </label>

                                        <label>
                                            <input type="radio"
                                                   name="customerMode"
                                                   value="new"
                                                   ${form.customerMode == 'new'
                                                     ? 'checked' : ''}>
                                            <span><ui:text key="screen.855efa16ef338b26"/></span>
                                        </label>
                                    </div>

                                    <div data-customer-pane="existing">
                                        <div class="ob-search">
                                            <div class="ob-field">
                                                <label for="q">
                                                    <ui:text key="screen.80ac476328a1a6ca"/>
                                                </label>

                                                <input class="ob-input"
                                                       type="search"
                                                       id="q"
                                                       name="q"
                                                       maxlength="100"
                                                       value="${fn:escapeXml(form.q)}"
                                                       placeholder="${requestScope.i18n['screen.bd9ab9176cf69767']}" data-i18n-placeholder="screen.bd9ab9176cf69767">
                                            </div>

                                            <button class="ob-button ob-secondary"
                                                    type="submit"
                                                    name="action"
                                                    value="search"
                                                    formnovalidate>
                                                <ui:text key="screen.57805bd2d7e63e4d"/>
                                            </button>
                                        </div>

                                        <c:if test="${not empty form.q and empty customers}">
                                            <p class="ob-empty" role="status">
                                                <ui:text key="screen.892733be5dbea18d"/>
                                            </p>
                                        </c:if>

                                        <div class="ob-field" style="margin-top:18px">
                                            <label for="customerId">
                                                <ui:text key="screen.71bc48dd14a2be24"/>
                                            </label>

                                            <select class="ob-input"
                                                    name="customerId"
                                                    id="customerId">

                                                <option value="" data-i18n="screen.255c4597f3d79f5a">
                                                    ${requestScope.i18n['screen.255c4597f3d79f5a']}
                                                </option>

                                                <c:forEach items="${customers}"
                                                           var="customer">
                                                    <option value="${customer.customerID}"
                                                            ${form.customerId == customer.customerID
                                                              ? 'selected' : ''}
                                                            >

                                                        <c:out value="${customer.lastName}
                                                               ${customer.firstName}
                                                               · ${customer.email}
                                                               · ${customer.phone}
                                                               "/>
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>

                                    <div data-customer-pane="new">
                                        <div class="ob-fields">
                                            <div class="ob-field">
                                                <label for="lastName">
                                                    <ui:text key="screen.cecbf3ef59fef946"/>
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
                                                <label for="firstName"><ui:text key="screen.96ce6e6193a04726"/></label>

                                                <input class="ob-input"
                                                       id="firstName"
                                                       name="firstName"
                                                       maxlength="50"
                                                       autocomplete="given-name"
                                                       value="${fn:escapeXml(form.firstName)}"
                                                       placeholder="Nhật">
                                            </div>

                                            <div class="ob-field">
                                                <label for="email"><ui:text key="offline.emailOptional"/></label>

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
                                                <label for="phone"><ui:text key="screen.63dceb8800b2dd4a"/></label>

                                                <input class="ob-input"
                                                       type="tel"
                                                       id="phone"
                                                       name="phone"
                                                       maxlength="20"
                                                       inputmode="tel"
                                                       autocomplete="tel"
                                                       value="${fn:escapeXml(form.phone)}"
                                                       placeholder="+84901234567"><p class="ob-note"><ui:text key="offline.phoneHint"/></p>
                                            </div>
                                        </div>
                                    </div>
                                </section>

                                <section class="ob-panel">
                                    <div class="ob-panel-top">
                                        <h2><ui:text key="screen.a42f24143514fb13"/></h2>
                                    </div>

                                    <div class="ob-fields">
                                        <div class="ob-field ob-full">
                                            <label for="promotionCode">
                                                <ui:text key="screen.fd947a841a798fb7"/>
                                            </label>

                                            <select class="ob-input" name="promotionCode" id="promotionCode"
                                                    aria-describedby="obPromotionHint">
                                                <option value="" data-i18n="offline.noPromotion">${requestScope.i18n['offline.noPromotion']}</option>
                                                <c:forEach var="promotion" items="${promotions}">
                                                    <option value="${fn:escapeXml(promotion.code)}" data-minimum="${promotion.minimum}"
                                                            ${form.promotionCode eq promotion.code ? 'selected' : ''}>
                                                        <c:out value="${promotion.code}"/> · <fmt:formatNumber value="${promotion.percent}"/>%
                                                    </option>
                                                </c:forEach>
                                            </select>
                                            <p class="ob-note mb-0" id="obPromotionHint"><ui:text key="offline.promotionEligibility"/></p>
                                        </div>

                                        <div class="ob-field ob-full">
                                            <label for="specialRequest">
                                                <ui:text key="screen.8d40b90ad50b8351"/>
                                            </label>

                                            <textarea class="ob-input"
                                                      name="specialRequest"
                                                      id="specialRequest"
                                                      maxlength="500"
                                                      rows="3"
                                                      placeholder="${requestScope.i18n['screen.8ec7552bf0a4f249']}" data-i18n-placeholder="screen.8ec7552bf0a4f249"><c:out value="${form.specialRequest}"/></textarea>
                                        </div>
                                    </div>
                                </section>
                            </div>

                            <aside class="ob-panel ob-summary"
                                   aria-label="${requestScope.i18n['screen.6b00218f389a7460']}" data-i18n-aria-label="screen.6b00218f389a7460">

                                <div class="ob-summary-head">
                                    <small>LUXE ESCAPE</small>
                                    <h2 id="obRoom"><ui:text key="screen.7c2d837276e934c7"/></h2>
                                    <p id="obType" class="ob-note">
                                        <ui:text key="screen.e3c166be8ed36aba"/>
                                    </p>
                                </div>

                                <div class="ob-summary-body">
                                    <dl>
                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.08fd1de4b04df4a2"/></dt>
                                            <dd id="obDates">—</dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.6a14a27a176f3d9a"/></dt>
                                            <dd id="obNights">—</dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.3c8e0fde6fcc71e0"/></dt>
                                            <dd id="obGuests">—</dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.3622756f73037b11"/></dt>
                                            <dd>${holdMinutes} <ui:text key="screen.90e63d85fa1ab2c8"/></dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.157ed3242bd5a322"/></dt>
                                            <dd>${depositPercent}%</dd>
                                        </div>
                                    </dl>

                                    <div class="ob-total">
                                        <span><ui:text key="screen.544ddc571296b6b9"/></span>
                                        <strong class="ob-price"
                                                id="obAmount"
                                                aria-live="polite">
                                            —
                                        </strong>
                                    </div>

                                    <c:if test="${not empty quote}">
                                        <div id="obReviewedQuote" class="ob-quote" role="status">
                                            <div class="ob-quote-title">
                                                <h3>Chi tiết báo giá</h3>
                                                <span class="ob-quote-badge">Đã kiểm tra giá</span>
                                            </div>

                                            <div class="ob-quote-customer">
                                                <span class="ob-customer-mark" aria-hidden="true">KH</span>
                                                <div>
                                                    <span class="ob-customer-label">Khách lưu trú</span>
                                                    <strong><c:out value="${quote.customerName}"/></strong>
                                                </div>
                                            </div>

                                            <dl class="ob-quote-lines">
                                                <div class="ob-summary-row">
                                                    <dt>Đơn giá / đêm</dt>
                                                    <dd>
                                                        <fmt:formatNumber value="${quote.nightly}"
                                                                          maxFractionDigits="2"/> ₫
                                                    </dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt>Tiền phòng (${quote.draft.quantity} phòng × ${quote.nights} đêm)</dt>
                                                    <dd>
                                                        <fmt:formatNumber value="${quote.gross}"
                                                                          maxFractionDigits="2"/> ₫
                                                    </dd>
                                                </div>

                                                <c:if test="${quote.gross > quote.payable}">
                                                    <div class="ob-summary-row ob-discount-row">
                                                        <dt>Giảm dự kiến</dt>
                                                        <dd>
                                                            −<fmt:formatNumber value="${quote.gross - quote.payable}"
                                                                              maxFractionDigits="2"/> ₫
                                                        </dd>
                                                    </div>
                                                </c:if>
                                            </dl>

                                            <div class="ob-quote-total">
                                                <span>Tổng dự kiến</span>
                                                <strong>
                                                    <fmt:formatNumber value="${quote.payable}"
                                                                      maxFractionDigits="2"/>
                                                    <small>₫</small>
                                                </strong>
                                            </div>

                                            <div class="ob-quote-deposit">
                                                <div>
                                                    <span>Cọc dự kiến</span>
                                                    <small>${depositPercent}% tổng tiền</small>
                                                </div>
                                                <strong>
                                                    <fmt:formatNumber value="${quotedDeposit}"
                                                                      maxFractionDigits="2"/> ₫
                                                </strong>
                                            </div>
                                        </div>
                                        <p id="obQuoteChanged" class="alert alert-warning" hidden>Thông tin đã thay đổi hoặc báo giá hết hạn. Xem và kiểm tra giá lại trước khi tạo.</p>
                                    </c:if>
                                    <c:if test="${not uncertain}">
                                        <button type="submit" name="action" value="preview"
                                                class="ob-button ob-secondary" ${empty rooms ? 'disabled' : ''}>
                                            <c:choose>
                                                <c:when test="${not empty quote}">Cập nhật báo giá</c:when>
                                                <c:otherwise>Xem và kiểm tra giá</c:otherwise>
                                            </c:choose>
                                        </button>
                                        <c:if test="${not empty quote}">
                                            <button type="submit" name="action" value="create"
                                                    class="ob-button ob-primary mt-3" disabled>
                                                Xác nhận tạo đặt phòng
                                            </button>
                                        </c:if>
                                    </c:if>
                                    <p class="ob-note mt-3">
                                        Xem giá chưa giữ phòng. Báo giá có hiệu lực xác nhận trong 5 phút;
                                        hệ thống kiểm tra lại khi tạo. Khuyến mãi vẫn được kiểm tra ở bước thu tiền.
                                        Tạo đơn chưa ghi nhận thanh toán.
                                    </p>

                                    <a class="ob-cancel"
                                       href="${pageContext.request.contextPath}/booking-management">
                                        <ui:text key="screen.a9197554370f5880"/>
                                    </a>
                                </div>
                            </aside>
                        </div>
                    </form>
                    <c:if test="${uncertain}">
                        <section class="ob-panel mt-3" role="status">
                            <p>Chưa rõ yêu cầu trước đã tạo đơn hay chưa. Kiểm tra lịch sử trước, hoặc kiểm tra lại đúng yêu cầu cũ.</p>
                            <form method="post" action="${pageContext.request.contextPath}/offline-booking">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="submissionToken" value="${submissionToken}">
                                <input type="hidden" name="quoteToken" value="${quoteToken}">
                                <button class="ob-button ob-primary" type="submit" name="action" value="retry">Kiểm tra lại yêu cầu cũ</button>
                            </form>
                        </section>
                    </c:if>
                </c:if>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp"%>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/assets/js/offline-booking.js?v=room-type-2" defer></script>
    </body>
</html>
