<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : counter-payment
    Created on : Nov 5, 2026, 9:48:52 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" import="java.time.format.DateTimeFormatter"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi" class="uc09-page"><head>
        <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
        <c:set var="titleKey" value="${summary.offline ? 'offline.title' : 'counter.online.title'}"/>
        <title><c:out value="${requestScope.i18n[titleKey]}"/></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/offline-booking.css?v=stable-1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/management.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/offline-counter.css?v=rooms-2">
    </head><body class="offline-booking-page management-page counter-payment-page"><div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>
            <main class="ob-page">
                <div class="management-hero">
                    <div>
                        <div class="eyebrow">LUXE ESCAPE</div>
                        <h1><ui:text key="${titleKey}"/> #${summary.bookingId}</h1>
                    </div>
                    <div class="d-flex flex-wrap gap-2">
                        <c:if test="${summary.legacyOnline}">
                            <c:url var="legacyDetail" value="/booking-management">
                                <c:param name="view" value="detail"/>
                                <c:param name="id" value="${summary.bookingId}"/>
                            </c:url>
                            <a class="btn btn-light" href="${fn:escapeXml(legacyDetail)}">Mở chi tiết đơn</a>
                        </c:if>
                        <a class="btn btn-light" href="<c:url value='/offline-booking?view=lookup'/>">Tra đơn khác</a>
                        <a class="btn btn-light" href="${pageContext.request.contextPath}/booking-management">
                            <ui:text key="offline.back"/>
                        </a>
                    </div>
                </div>
                <c:if test="${not empty operationMessage}"><div class="oc-message" role="status"><ui:message text="${operationMessage}"/></div></c:if>
                <c:if test="${param.created eq '1'}"><div class="oc-message"><ui:text key="offline.created"/></div></c:if>
                <div class="oc-overview ob-panel"><div><small><ui:text key="offline.customer"/></small><strong><a class="ob-back" href="${pageContext.request.contextPath}/customers?view=detail&amp;id=${operations.customerId}"><c:out value="${summary.customerName}"/></a></strong></div>
                    <div><small><ui:text key="offline.status"/></small><strong><ui:text key="offline.state.${summary.status}"/></strong></div>
                    <c:if test="${summary.offline}">
                        <div><small><ui:text key="offline.creator"/></small><strong><c:out value="${summary.creator}"/></strong></div>
                    </c:if>
                    <c:if test="${summary.status eq 'pending'}">
                        <div>
                            <small>Giờ hết hạn giữ chỗ</small>
                            <strong><c:choose>
                                    <c:when test="${not empty summary.holdExpiresAt}"><c:out value="${summary.holdExpiresAt}"/></c:when>
                                    <c:when test="${not summary.offline and not empty summary.bookingDate}">
                                        <c:out value="${summary.bookingDate.plusMinutes(30).format(DateTimeFormatter.ofPattern('dd/MM/yyyy HH:mm'))}"/>
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose></strong>
                        </div>
                    </c:if>
                </div>
                <div class="oc-money"><div><small><ui:text key="offline.total"/></small><strong><fmt:formatNumber value="${summary.payable}"/> VND</strong></div>
                    <div><small><ui:text key="offline.paid"/></small><strong><fmt:formatNumber value="${summary.paid}"/> VND</strong></div>
                    <div class="oc-balance"><small><ui:text key="offline.remaining"/></small><strong><fmt:formatNumber value="${summary.remaining}"/> VND</strong></div></div>
                <c:if test="${not empty summary.promotionError}"><div class="oc-message"><c:out value="${summary.promotionError}"/></div></c:if>
                    <section class="ob-panel oc-room-list" aria-labelledby="booked-rooms-heading">
                        <div class="oc-room-header">
                            <div>
                                <h2 id="booked-rooms-heading">Phòng đã đặt</h2>
                                <p class="oc-room-meta">${fn:length(operations.rooms)} phòng · Giờ nhận ${operations.standardCheckIn} · Giờ trả ${operations.standardCheckOut}</p>
                        </div>
                        <c:if test="${summary.offline and not empty operations.actualGuestCount}">
                            <div class="oc-arrival-total">
                                <span>Khách đã đến</span>
                                <strong>${operations.actualGuestCount}/${operations.plannedGuestCount}</strong>
                                <c:if test="${operations.allGuestsArrived}"><span class="oc-arrival-complete">Đã đến đủ</span></c:if>
                                </div>
                        </c:if>
                    </div>
                    <c:if test="${not empty operations.actualCheckInTime or not empty operations.actualCheckoutDate}">
                        <div class="oc-stay-record">
                            <c:if test="${not empty operations.actualCheckInTime}">
                                <span>Đã nhận phòng lúc <strong><c:out value="${operations.actualCheckInTime}"/></strong></span>
                            </c:if>
                            <c:if test="${not empty operations.actualCheckoutDate}">
                                <span>Trả phòng thực tế (giờ Việt Nam) <strong><c:out value="${operations.actualCheckoutDate}"/></strong></span>
                            </c:if>
                        </div>
                    </c:if>
                    <c:set var="canUpdateArrivals" value="${operations.canUpdateArrivals and empty activeQr}"/>
                    <ul class="oc-room-rows list-unstyled">
                        <c:forEach items="${operations.rooms}" var="room">
                            <c:set var="stayRoom" value="${operations.offlineRoomGuests[room.BookingDetailID]}"/>
                            <li class="oc-room-row">
                                <div class="oc-room-row-info">
                                    <div class="oc-room-identity">
                                        <strong><c:choose>
                                                <c:when test="${empty room.RoomNumber}">Chưa xếp phòng</c:when>
                                                <c:otherwise>Phòng #${room.RoomNumber}</c:otherwise>
                                            </c:choose></strong>
                                            <c:if test="${not empty room.RoomTypeName}">
                                            <span><c:out value="${room.RoomTypeName}"/></span>
                                        </c:if>
                                        <c:if test="${room.NeedsCleaning}"><span class="oc-badge"><ui:text key="offline.dirty"/></span></c:if>
                                        </div>
                                        <dl class="oc-room-fact">
                                            <dt>Lịch đặt</dt>
                                            <dd><fmt:formatDate value="${room.CheckInDate}" pattern="dd/MM/yyyy"/> → <fmt:formatDate value="${room.CheckOutDate}" pattern="dd/MM/yyyy"/></dd>
                                    </dl>
                                    <dl class="oc-room-fact">
                                        <dt>Khách theo đơn</dt>
                                        <dd><c:choose>
                                                <c:when test="${summary.offline}">${empty stayRoom ? room.GuestCount : stayRoom.plannedGuests} khách</c:when>
                                                <c:when test="${not empty room.BookedAdults}">
                                                    ${room.BookedAdults} <ui:text key="room.selection.adultUnit"/>
                                                    · ${room.BookedChildren} <ui:text key="room.selection.childUnit"/>
                                                </c:when>
                                                <c:when test="${operations.online}"><ui:text key="room.booking.party.unknown"/></c:when>
                                                <c:otherwise>${room.GuestCount} <ui:text key="offline.guests"/></c:otherwise>
                                            </c:choose></dd>
                                            <c:if test="${summary.offline and stayRoom.legacyChildren gt 0}">
                                            <dd class="oc-room-meta">Gồm ${stayRoom.legacyChildren} trẻ dưới 6 tuổi theo đơn cũ</dd>
                                        </c:if>
                                    </dl>
                                    <c:if test="${summary.offline and (not empty operations.actualCheckInTime or stayRoom.actualGuests ne null or stayRoom.legacyActualChildren ne null)}">
                                        <dl class="oc-room-fact">
                                            <dt>Khách đã đến</dt>
                                            <dd><c:choose>
                                                    <c:when test="${stayRoom.actualGuests ne null}"><strong>${stayRoom.actualGuests} khách</strong></c:when>
                                                    <c:otherwise>Chưa ghi nhận</c:otherwise>
                                                </c:choose></dd>
                                                <c:if test="${stayRoom.legacyActualChildren ne null}">
                                                <dd class="oc-room-meta">${stayRoom.legacyActualChildren} trẻ thực tế đã ghi trước đây</dd>
                                            </c:if>
                                        </dl>
                                    </c:if>
                                </div>
                                <c:if test="${canUpdateArrivals and not operations.missingRoomGuestCounts and stayRoom.canUpdateArrivals}">
                                    <details class="oc-room-update">
                                        <summary><span class="visually-hidden">Phòng #${room.RoomNumber}: </span>Cập nhật khách đã đến</summary>
                                        <form method="post" action="<c:url value='/counter-payment'/>" class="oc-form">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="paymentKey" value="${paymentKey}">
                                            <input type="hidden" name="action" value="update_arrived_guests">
                                            <label>Tổng số khách đã đến phòng #${room.RoomNumber}
                                                <input class="ob-input" type="number" name="arrived-${room.BookingDetailID}"
                                                       min="${stayRoom.actualGuests + 1}" max="${stayRoom.arrivalLimit}" required>
                                            </label>
                                            <label class="oc-check">
                                                <input type="checkbox" name="identityVerified" value="yes" required>
                                                Đã đối chiếu giấy tờ của khách đến thêm
                                            </label>
                                            <button type="submit" class="ob-button ob-primary">Lưu số lượng khách đã đến</button>
                                        </form>
                                    </details>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ul>
                    <c:if test="${canUpdateArrivals and operations.missingRoomGuestCounts}">
                        <details class="oc-room-update oc-room-update-batch">
                            <summary>Cập nhật số khách đã đến theo phòng</summary>
                            <form method="post" action="<c:url value='/counter-payment'/>" class="oc-form">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="paymentKey" value="${paymentKey}">
                                <input type="hidden" name="action" value="update_arrived_guests">
                                <p class="ob-note">Đơn còn thiếu số liệu theo phòng. Nhập số khách đã đến cho những phòng bên dưới.</p>
                                <c:forEach items="${operations.rooms}" var="room">
                                    <c:set var="stayRoom" value="${operations.offlineRoomGuests[room.BookingDetailID]}"/>
                                    <c:if test="${stayRoom.canUpdateArrivals}">
                                        <label>Tổng số khách đã đến phòng #${room.RoomNumber}
                                            <input class="ob-input" type="number" name="arrived-${room.BookingDetailID}"
                                                   min="${empty stayRoom.actualGuests ? 1 : stayRoom.actualGuests}"
                                                   max="${stayRoom.arrivalLimit}" value="${stayRoom.actualGuests}" required>
                                        </label>
                                    </c:if>
                                </c:forEach>
                                <label class="oc-check">
                                    <input type="checkbox" name="identityVerified" value="yes" required>
                                    Đã xác nhận số khách và đối chiếu giấy tờ
                                </label>
                                <button type="submit" class="ob-button ob-primary">Lưu số lượng khách đã đến</button>
                            </form>
                        </details>
                    </c:if>
                    <details class="oc-room-policy">
                        <summary>Chính sách trả sớm</summary>
                        <p>Tiền phòng theo lịch đã chốt được giữ nguyên, không tự giảm hoặc hoàn tiền cho những đêm chưa ở. Nhân viên cần thông báo cho khách trước khi thu tiền.</p>
                    </details>
                </section>
                <c:if test="${not empty activeQr}">
                    <section class="ob-panel">
                        <h2>Đang đối soát QR</h2>
                        <p>Không thu tiền mặt, đổi mã giảm hoặc thao tác booking khi giao dịch chưa kết thúc.</p>
                        <a class="ob-button ob-primary" href="${pageContext.request.contextPath}/counter-payment?bookingId=${summary.bookingId}&amp;view=qr&amp;orderCode=${activeQr.OrderCode}">Mở lại QR / kiểm tra kết quả</a>
                    </section>
                </c:if>
                <c:if test="${empty activeQr}"><div class="oc-grid">
                        <c:if test="${summary.canPay}"><section class="ob-panel"><h2><ui:text key="offline.payment"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="paymentKey" value="${paymentKey}">
                                    <input type="hidden" name="action" value="pay">
                                    <label><ui:text key="offline.method"/><select class="ob-input" name="method"><option value="cash"><ui:text key="offline.cash"/></option><option value="bank_transfer" ${qrConfigured ? '' : 'disabled'}>QR payOS</option></select></label>
                                    <p class="ob-note" data-counter-method-note>Chỉ xác nhận sau khi nhận đủ tiền mặt.</p>
                                    <c:if test="${not qrConfigured}"><p class="ob-note">QR chưa được cấu hình. Liên hệ người phụ trách thanh toán.</p></c:if>
                                    <label><ui:text key="offline.plan"/><select class="ob-input" name="percent">
                                            <c:if test="${summary.firstPayment and summary.depositPercent lt 100}"><option value="${summary.depositPercent}" data-amount="${summary.depositAmount}"><ui:text key="offline.deposit"/> ${summary.depositPercent}%</option></c:if>
                                            <option value="100" selected data-amount="${summary.remaining}"><ui:text key="offline.balance"/></option></select></label><p><ui:text key="offline.dueNow"/>: <strong data-counter-due><fmt:formatNumber value="${summary.remaining}"/> VND</strong></p><p class="ob-note"><ui:text key="offline.paymentNote"/></p><button type="submit" class="ob-button ob-primary"><ui:text key="offline.pay"/></button></form></section></c:if>
                        <c:if test="${summary.canChange}"><section class="ob-panel"><h2><ui:text key="offline.promotion"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="paymentKey" value="${paymentKey}">
                                    <input type="hidden" name="action" value="promotion">
                                    <input class="ob-input" name="promotionCode" maxlength="50" value="${fn:escapeXml(summary.promotionCode)}"><button type="submit" class="ob-button ob-primary"><ui:text key="offline.promotion"/></button></form></section></c:if>
                                <c:if test="${summary.status eq 'confirmed'}">
                            <section class="ob-panel">
                                <h2>Nhận phòng</h2>
                                <c:choose>
                                    <c:when test="${not empty operations.checkInError}">
                                        <p class="ob-note" role="status"><c:out value="${operations.checkInError}"/></p>
                                    </c:when>
                                    <c:when test="${not empty operations.checkIn}">
                                        <form method="post" action="<c:url value='/counter-payment'/>" class="oc-form" data-check-in>
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="paymentKey" value="${paymentKey}">
                                            <input type="hidden" name="action" value="check_in">
                                            <c:set var="hasUnassignedSlot" value="${false}"/>
                                            <fieldset>
                                                <legend class="fs-6">Chọn đủ ${fn:length(operations.checkIn.slots)} phòng</legend>
                                                <c:forEach items="${operations.checkIn.slots}" var="slot" varStatus="line">
                                                    <c:choose>
                                                        <c:when test="${slot.retained}">
                                                            <input type="hidden" name="room-${slot.id}" value="${slot.assigned}" data-assigned-room>
                                                            <p>${line.count}. <c:out value="${slot.typeName}"/> · Giữ phòng #${slot.assigned}</p>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:set var="hasUnassignedSlot" value="${true}"/>
                                                            <label>
                                                                ${line.count}. <c:out value="${slot.typeName}"/>
                                                                <c:if test="${not empty slot.bookedAdults}">
                                                                    · ${slot.bookedAdults} <ui:text key="room.selection.adultUnit"/>
                                                                    · ${slot.bookedChildren} <ui:text key="room.selection.childUnit"/>
                                                                </c:if>
                                                                <select class="ob-input" name="room-${slot.id}" required
                                                                        data-assigned-room data-auto-room="${operations.checkIn.suggested[slot.id]}">
                                                                    <option value="">Chọn phòng</option>
                                                                    <c:forEach items="${slot.options}" var="roomNumber">
                                                                        <option value="${roomNumber}" ${slot.provisional and operations.checkIn.suggested[slot.id] == roomNumber ? 'selected' : ''}>Phòng #${roomNumber}</option>
                                                                    </c:forEach>
                                                                </select>
                                                            </label>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <c:if test="${summary.offline}">
                                                        <c:set var="stayRoom" value="${operations.offlineRoomGuests[slot.id]}"/>
                                                        <label>Số khách thực tế ở phòng ${line.count}, gồm cả trẻ em
                                                            <input class="ob-input" type="number" name="guests-${slot.id}" min="1"
                                                                   max="${stayRoom.capacity}" value="${stayRoom.plannedGuests}" required>
                                                        </label>
                                                        <label class="oc-check">
                                                            <input type="checkbox" name="adultPresent-${slot.id}" value="yes" required>
                                                            Đã đối chiếu giấy tờ và xác nhận có người từ đủ 18 tuổi ở phòng này
                                                        </label>
                                                    </c:if>
                                                </c:forEach>
                                            </fieldset>
                                            <c:if test="${hasUnassignedSlot}">
                                                <button type="button" class="ob-button ob-secondary" data-auto-assign hidden>Tự gán</button>
                                            </c:if>
                                            <p class="ob-note" role="status" data-assignment-message></p>
                                            <c:if test="${not summary.offline}">
                                                <label>Số khách thực tế
                                                    <input class="ob-input" type="number" name="actualGuests" min="1"
                                                           max="${operations.checkIn.capacity lt 1000 ? operations.checkIn.capacity : 1000}"
                                                           value="${operations.checkIn.guests}" required>
                                                </label>
                                            </c:if>
                                            <label class="oc-check">
                                                <input type="checkbox" name="identityVerified" value="yes" required> Đã đối chiếu giấy tờ
                                            </label>
                                            <button class="ob-button ob-primary" type="submit">Xác nhận nhận phòng</button>
                                        </form>
                                    </c:when>
                                </c:choose>
                            </section>
                        </c:if>
                        <c:if test="${summary.offline and summary.status eq 'checked_in'}"><section class="ob-panel"><h2><ui:text key="offline.charge"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="paymentKey" value="${paymentKey}">
                                    <input type="hidden" name="action" value="charge">
                                    <label><ui:text key="offline.description"/><input class="ob-input" name="description" maxlength="500" required></label>
                                    <label><ui:text key="offline.amount"/><input class="ob-input" name="amount" type="number" min="1" max="1000000000" step="1" required></label><button type="submit" class="ob-button ob-primary"><ui:text key="offline.charge"/></button></form></section></c:if>
                        <c:if test="${summary.offline and (summary.status eq 'pending' or summary.status eq 'confirmed')}"><section class="ob-panel"><h2><ui:text key="offline.cancel"/></h2><p class="ob-note"><ui:text key="offline.cancelPolicy"/> ${summary.depositPercent}%.</p><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="paymentKey" value="${paymentKey}">
                                    <input type="hidden" name="action" value="cancel">
                                    <label><ui:text key="offline.reason"/><textarea class="ob-input" name="reason" maxlength="255" required></textarea></label>
                                    <label class="oc-check"><input type="checkbox" required> <ui:text key="offline.cancelConfirm"/></label><button type="submit" class="ob-button ob-secondary"><ui:text key="offline.cancel"/></button></form></section></c:if>
                        <c:if test="${not empty operations.refunds}"><section class="ob-panel"><h2><ui:text key="offline.refund"/></h2><c:forEach items="${operations.refunds}" var="refund"><p><ui:text key="offline.refundAmount"/>: <strong><fmt:formatNumber value="${refund.Amount}"/> VND</strong> · <c:out value="${refund.Status}"/></p>
                                    <c:if test="${refund.Status eq 'pending'}"><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="paymentKey" value="${paymentKey}">
                                            <input type="hidden" name="action" value="refund">
                                            <label><ui:text key="offline.method"/><select class="ob-input" name="method"><option value="cash"><ui:text key="offline.cash"/></option><option value="bank_transfer"><ui:text key="offline.bank"/></option></select></label>
                                            <label><ui:text key="offline.reference"/><input class="ob-input" name="reference" maxlength="100" autocomplete="off"></label><p class="ob-note"><ui:text key="offline.bankNote"/></p><label class="oc-check"><input type="checkbox" required> <ui:text key="offline.refundConfirm"/></label><button type="submit" class="ob-button ob-primary"><ui:text key="offline.refund"/></button></form></c:if></c:forEach></section></c:if>
                                    <c:if test="${not summary.legacyOnline and summary.status eq 'checked_in'
                                                  and (summary.offline or summary.remaining le 0)}"><section class="ob-panel"><h2><ui:text key="offline.check_out"/></h2><p class="ob-note"><ui:text key="offline.checkoutNote"/></p><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="paymentKey" value="${paymentKey}">
                                    <input type="hidden" name="action" value="check_out">
                                    <c:if test="${operations.earlyCheckout}">
                                        <div class="oc-message">Khách trả phòng trước ngày đã đặt. Tổng tiền phòng giữ nguyên theo chính sách; cần thu đủ các khoản phát sinh trước khi trả.</div>
                                        <label class="oc-check"><input type="checkbox" name="earlyCheckoutAccepted" value="yes" required> Đã thông báo và khách đồng ý chính sách trả sớm không tự giảm hoặc hoàn tiền phòng.</label>
                                        </c:if>
                                    <button type="submit" class="ob-button ob-primary"><ui:text key="offline.check_out"/></button></form></section></c:if>
                        <c:if test="${not summary.legacyOnline and summary.status eq 'checked_out'}"><section class="ob-panel"><h2><ui:text key="offline.closing"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="paymentKey" value="${paymentKey}">
                                    <input type="hidden" name="action" value="complete">
                                    <button type="submit" class="ob-button ob-primary"><ui:text key="offline.complete"/></button></form><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="paymentKey" value="${paymentKey}">
                                    <input type="hidden" name="action" value="clean">
                                    <button type="submit" class="ob-button ob-secondary"><ui:text key="offline.clean"/></button></form></section></c:if>
                        <c:if test="${not summary.legacyOnline and summary.status eq 'completed'}"><section class="ob-panel"><h2><ui:text key="offline.clean"/></h2><form method="post" action="${pageContext.request.contextPath}/counter-payment" class="oc-form">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="paymentKey" value="${paymentKey}">
                                    <input type="hidden" name="action" value="clean">
                                    <button type="submit" class="ob-button ob-primary"><ui:text key="offline.clean"/></button></form></section></c:if>
                    </div></c:if>
                <details class="ob-panel oc-history"><summary><ui:text key="offline.history"/></summary>
                    <div class="oc-table"><table><thead><tr><th><ui:text key="offline.action"/></th><th><ui:text key="offline.description"/></th><th><ui:text key="offline.amount"/></th><th><ui:text key="offline.actor"/></th><th>Giờ Việt Nam (UTC+7)</th></tr></thead><tbody>
                                <c:forEach items="${operations.events}" var="event"><tr><td><c:choose>
                                                <c:when test="${event.Action eq 'update_arrived_guests'}">Cập nhật số khách đã đến</c:when>
                                                <c:otherwise><ui:text key="offline.action.${event.Action}"/></c:otherwise>
                                            </c:choose></td><td><c:out value="${event.Note}"/></td><td><fmt:formatNumber value="${event.Amount}"/></td><td><c:out value="${event.Actor}"/></td><td><c:out value="${event.CreatedAtVietnam}"/></td></tr></c:forEach>
                                </tbody></table></div></details>
                    <details class="ob-panel oc-history"><summary><ui:text key="offline.receipts"/></summary><div class="oc-table"><table><thead><tr><th>Mã</th><th><ui:text key="offline.amount"/></th><th><ui:text key="offline.method"/></th><th><ui:text key="offline.reference"/></th><th><ui:text key="offline.status"/></th><th><ui:text key="offline.refunded"/></th></tr></thead><tbody>
                                <c:forEach items="${operations.payments}" var="payment"><tr><td>${payment.PaymentID}</td><td><fmt:formatNumber value="${payment.Amount}"/></td><td><c:out value="${payment.PaymentMethod}"/></td><td><c:out value="${payment.TransactionCode}"/><br><c:out value="${payment.CounterReference}"/></td><td><c:out value="${payment.PaymentStatus}"/></td><td><fmt:formatNumber value="${payment.RefundedAmount}"/></td></tr></c:forEach>
                                </tbody></table></div></details>
                    <a class="ob-button ob-secondary" href="${pageContext.request.contextPath}/offline-booking"><ui:text key="offline.newBooking"/></a>
            </main><%@include file="/WEB-INF/include/footer.jsp"%></div>
        <script defer src="${pageContext.request.contextPath}/assets/js/offline-counter.js?v=check-in-1"></script>
    </body></html>
