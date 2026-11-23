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

<fmt:setLocale value="vi_VN" scope="page"/>
<!DOCTYPE html>
<html lang="vi" class="uc09-page">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${requestScope.i18n['screen.5a779d440195be98']}</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/offline-booking.css?v=room-picker-7">
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

                <nav class="ob-tabs" aria-label="Đặt phòng tại quầy">
                    <a class="ob-tab"
                       ${lookupView ? '' : 'aria-current="page"'} href="<c:url value='/offline-booking'/>">Tạo đơn tại quầy</a>
                    <a class="ob-tab"
                       ${lookupView ? 'aria-current="page"' : ''} href="<c:url value='/offline-booking?view=lookup'/>">Tra đơn đã đặt</a>
                </nav>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger mb-4" role="alert" data-offline-booking-error>
                        <ui:message text="${error}"/>
                    </div>
                </c:if>



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
                    <form action="${pageContext.request.contextPath}/offline-booking"
                          method="post"
                          id="offlineForm" data-form-version="${offlineBookingFormVersion}" data-searched="${searched}" data-reviewed="${not empty quote}" data-uncertain="${uncertain}" data-quote-age="${quoteRemainingMillis}" data-search-key="${fn:escapeXml(searchedStay)}" data-quote-signature="${fn:escapeXml(quoteSignature)}">

                        <input type="hidden"
                               name="csrfToken"
                               value="${sessionScope.csrfToken}">
                        <input type="hidden" name="submissionToken" value="${submissionToken}">
                        <input type="hidden" name="roomSelection" value="${fn:escapeXml(form.roomSelection)}">
                        <input type="hidden" name="quoteToken" value="${quoteToken}">
                        <p id="obFormVersionError" class="alert alert-danger" role="alert" hidden>
                            Giao diện và máy chủ đặt phòng tại quầy đang khác phiên bản. Cần build và triển khai lại cùng một bộ mã.
                        </p>
                        <p id="obSearchChanged" class="alert alert-warning" hidden>Thông tin lưu trú đã đổi. Tìm lại phòng để tiếp tục.</p>

                        <div class="ob-grid">
                            <div class="ob-stack">

                                <section class="ob-panel">
                                    <div class="ob-panel-top">
                                        <h2>1. Lưu trú &amp; phòng</h2>
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

                                        <p id="obStayNights" class="ob-note ob-full ob-stay-nights" aria-live="polite" hidden></p>

                                        <div class="ob-field">
                                            <label for="adults">Người lớn (từ đủ 18 tuổi)</label>
                                            <div class="ob-stepper">
                                                <button type="button" data-step="-1" aria-controls="adults" aria-label="Giảm: Người lớn (từ đủ 18 tuổi)">−</button>
                                                <input class="ob-input" type="number" id="adults" name="adults"
                                                       min="1" max="1000" required value="${fn:escapeXml(form.adults)}"
                                                       aria-describedby="obAdultHint">
                                                <button type="button" data-step="1" aria-controls="adults" aria-label="Tăng số người lớn">+</button>
                                            </div>
                                            <p id="obAdultHint" class="ob-note">Mỗi phòng cần ít nhất một người lớn đi cùng và lưu trú.</p>
                                        </div>
                                        <div class="ob-field">
                                            <label for="children">Trẻ em (dưới 18 tuổi)</label>
                                            <div class="ob-stepper">
                                                <button type="button" data-step="-1" aria-controls="children" aria-label="Giảm số trẻ em">−</button>
                                                <input class="ob-input" type="number" id="children" name="children"
                                                       min="0" max="999" required value="${fn:escapeXml(form.children)}"
                                                       aria-describedby="obChildHint">
                                                <button type="button" data-step="1" aria-controls="children" aria-label="Tăng số trẻ em">+</button>
                                            </div>
                                            <p id="obChildHint" class="ob-note">Tất cả trẻ em đều tính vào sức chứa của phòng.</p>
                                        </div>
                                        <div class="ob-full">
                                            <button class="ob-button ob-primary" type="submit" name="action"
                                                    value="search" data-search-rooms formnovalidate>
                                                Tìm phòng trống
                                            </button>
                                        </div>
                                    </div>

                                    <c:if test="${searched and availableCapacity lt requiredCapacity}">
                                        <p class="ob-empty" role="status">
                                            Chỉ còn chỗ cho ${availableCapacity}/${requiredCapacity} khách trong kỳ lưu trú đã chọn.
                                        </p>
                                    </c:if>
                                    <c:if test="${searched and availableCapacity ge requiredCapacity}">
                                        <c:if test="${feasibleCapacity lt requiredCapacity}">
                                            <p class="ob-empty" role="status">
                                                Với số người lớn và số phòng có thể chọn, chỉ xếp được ${feasibleCapacity}/${requiredCapacity} khách.
                                            </p>
                                        </c:if>
                                        <fieldset class="ob-room-field" id="obRoomPicker" aria-describedby="obRoomHelp">
                                            <legend>Chọn phòng theo loại</legend>
                                            <p class="ob-note" id="obRoomHelp">Bấm + để thêm phòng và − để giảm. Số phòng tối đa bằng số người lớn đi cùng.</p>
                                            <p id="obRoomProgress" class="ob-room-progress" aria-live="polite">
                                                Đang tải bộ chọn phòng. Tải lại trang nếu các nút vẫn bị khóa.
                                            </p>
                                            <p id="obAdultLimit" class="ob-empty" hidden>Mỗi phòng cần một người lớn. Giảm số phòng đã chọn.</p>
                                            <c:if test="${roomStockChanged}">
                                                <p id="obRoomStockChanged" class="ob-empty" role="status">
                                                    Số phòng trống vừa đổi. Kiểm tra lại số phòng đã chọn.
                                                </p>
                                            </c:if>
                                            <label class="visually-hidden" for="obRoomSearch">Tìm theo tên loại phòng</label>
                                            <input class="ob-input" type="search" id="obRoomSearch" autocomplete="off"
                                                   placeholder="Tìm theo tên loại phòng">
                                            <div class="ob-room-results" id="obRoomResults">
                                                <c:forEach items="${rooms}" var="option">
                                                    <c:set var="room" value="${option.stock}"/>
                                                    <c:set var="quantity" value="${option.maximum eq 0 or empty roomQuantities[room.type.id] ? 0 : roomQuantities[room.type.id]}"/>
                                                    <div class="ob-room-choice ${quantity gt 0 ? 'is-selected' : ''}">
                                                        <div class="ob-room-pick">
                                                            <span class="ob-room-info">
                                                                <strong class="ob-room-name"><c:out value="${room.type.name}"/></strong>
                                                                <span class="ob-room-meta">
                                                                    <span class="ob-room-stock">Còn ${room.available} phòng trong kỳ</span>
                                                                    <span>Tối đa ${room.type.capacity} khách/phòng</span>
                                                                    <c:if test="${room.readyNow ne null}">
                                                                        <span>Có thể nhận hôm nay: ${option.maximum} phòng</span>
                                                                    </c:if>
                                                                </span>
                                                            </span>
                                                            <span class="ob-room-price">
                                                                <strong><ui:money amount="${room.type.pricePerNight}"/></strong>
                                                                <small>/ đêm</small>
                                                            </span>
                                                            <div class="ob-room-quantity ob-field">
                                                                <label class="visually-hidden" for="quantity-${room.type.id}">
                                                                    Số phòng <c:out value="${room.type.name}"/>
                                                                </label>
                                                                <div class="ob-stepper">
                                                                    <button type="button" data-step="-1" data-room-step
                                                                            aria-controls="quantity-${room.type.id}"
                                                                            aria-label="Giảm một phòng ${fn:escapeXml(room.type.name)}" disabled>−</button>
                                                                    <input class="ob-input" type="number" id="quantity-${room.type.id}" name="roomQty-${room.type.id}"
                                                                           data-room-type="${room.type.id}" data-stock="${option.maximum}"
                                                                           data-label="${fn:escapeXml(room.type.name)}"
                                                                           data-capacity="${room.type.capacity}" data-price="${room.type.pricePerNight}"
                                                                           min="0" max="${option.maximum}" value="${quantity}" required
                                                                           ${option.maximum eq 0 ? 'disabled' : ''}>
                                                                    <button type="button" data-step="1" data-room-step
                                                                            aria-controls="quantity-${room.type.id}"
                                                                            aria-label="Thêm một phòng ${fn:escapeXml(room.type.name)}" disabled>+</button>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </div>
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
                                        <h2>2. Khách hàng</h2>
                                    </div>
                                    <p class="ob-note mb-2">Nhập thông tin một người từ đủ 18 tuổi làm đại diện và lưu trú cùng đoàn.</p>
                                    <p class="ob-note">Khách tại quầy chưa có tài khoản có thể xác minh email để kích hoạt và giữ lịch sử đặt phòng:
                                        <a href="${pageContext.request.contextPath}/walkin-account" target="_blank" rel="noopener">Liên kết kích hoạt gửi cho khách</a>.
                                        Khách mở liên kết trên thiết bị của mình, chưa đăng nhập. Không tự tạo tài khoản thay khách.
                                    </p>

                                    <div class="ob-mode"
                                         role="group"
                                         aria-label="${requestScope.i18n['screen.4ac86ab4f546c113']}">

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
                                                       placeholder="${requestScope.i18n['screen.bd9ab9176cf69767']}">
                                            </div>

                                            <button class="ob-button ob-secondary"
                                                    type="submit"
                                                    name="action"
                                                    value="search"
                                                    data-search-customers formnovalidate>
                                                <ui:text key="screen.57805bd2d7e63e4d"/>
                                            </button>
                                        </div>

                                        <p id="obCustomerSearchStatus" class="ob-note" role="status" hidden></p>
                                        <div data-customer-search-results>
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

                                                    <option value="">
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
                                        <h2>3. Thông tin thêm</h2>
                                    </div>

                                    <div class="ob-fields">
                                        <div class="ob-field ob-full">
                                            <label for="promotionCode">
                                                <ui:text key="screen.fd947a841a798fb7"/>
                                            </label>

                                            <select class="ob-input" name="promotionCode" id="promotionCode"
                                                    aria-describedby="obPromotionHint">
                                                <option value="">${requestScope.i18n['offline.noPromotion']}</option>
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
                                                      placeholder="${requestScope.i18n['screen.8ec7552bf0a4f249']}"><c:out value="${form.specialRequest}"/></textarea>
                                        </div>
                                    </div>
                                </section>
                            </div>

                            <aside class="ob-panel ob-summary"
                                   aria-label="${requestScope.i18n['screen.6b00218f389a7460']}">

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
                                            <dt>Khách lưu trú</dt>
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

                                            <p class="ob-note">${quote.draft.adults} người lớn · ${quote.draft.children} trẻ em</p>

                                            <div class="ob-quote-customer">
                                                <span class="ob-customer-mark" aria-hidden="true">KH</span>
                                                <div>
                                                    <span class="ob-customer-label">Khách lưu trú</span>
                                                    <strong><c:out value="${quote.customerName}"/></strong>
                                                </div>
                                            </div>

                                            <dl class="ob-quote-lines">
                                                <c:forEach items="${quote.lines}" var="line">
                                                    <div class="ob-summary-row">
                                                        <dt><c:out value="${line.name}"/> × ${line.quantity}</dt>
                                                        <dd><fmt:formatNumber value="${line.nightly}" maxFractionDigits="2"/> ₫ / phòng / đêm</dd>
                                                    </div>
                                                </c:forEach>

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
                                        <p id="obQuoteCustomerHint" class="ob-note mb-2" hidden></p>
                                        <button type="submit" name="action" value="preview"
                                                class="ob-button ob-secondary" disabled>
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
        <script src="${pageContext.request.contextPath}/assets/js/offline-booking.js?v=room-picker-11" defer></script>
    </body>
</html>
