<%--
    Document   : counter-qr
    Created on : Nov 14, 2026, 7:11:14 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>QR · Booking #${qr.BookingID}</title>
        <link rel="stylesheet" href="<c:url value='/assets/css/bootstrap.min.css'/>">
        <link rel="stylesheet" href="<c:url value='/assets/css/general.css'/>">
        <link rel="stylesheet" href="<c:url value='/assets/css/luxe-shared-shell.css'/>">
        <link rel="stylesheet" href="<c:url value='/assets/css/luxe-ui.css'/>">
        <link rel="stylesheet" href="<c:url value='/assets/css/offline-booking.css'/>">
        <link rel="stylesheet" href="<c:url value='/assets/css/offline-counter.css'/>">
    </head>
    <body class="offline-booking-page">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>
            <main class="ob-page">
                <div class="ob-heading"><h1>QR · Booking #${qr.BookingID}</h1></div>
                <section class="ob-panel">
                    <h2>Số tiền cần thu: <fmt:formatNumber value="${qr.Amount}"/> VND</h2>
                    <p>Mã giao dịch: <c:out value="${qr.OrderCode}"/></p>
                    <c:if test="${qr.ActualReceived gt 0}">
                        <p>payOS báo đã nhận: <fmt:formatNumber value="${qr.ActualReceived}"/> VND.
                        Khoản này chỉ được tính vào đã thu của booking sau khi đối soát hợp lệ.</p>
                    </c:if>
                    <c:url var="statusUrl" value="/counter-payment">
                        <c:param name="bookingId" value="${qr.BookingID}"/>
                        <c:param name="view" value="qr-status"/>
                        <c:param name="orderCode" value="${qr.OrderCode}"/>
                    </c:url>
                    <c:url var="backUrl" value="/counter-payment">
                        <c:param name="bookingId" value="${qr.BookingID}"/>
                    </c:url>
                    <div id="counterQr" data-status-url="<c:out value='${statusUrl}'/>"
                         data-back-url="<c:out value='${backUrl}'/>"
                         data-state="<c:out value='${qrAvailable ? qr.State : "unavailable"}'/>">
                        <p id="counterQrState" class="oc-message" role="status" aria-live="polite">
                            Đang kiểm tra trạng thái thanh toán…
                        </p>
                        <c:if test="${qr.Active and not empty qr.CheckoutUrl}">
                            <iframe id="counterQrFrame" title="Thanh toán QR payOS"
                                    src="<c:out value='${qr.CheckoutUrl}'/>"
                                    width="100%" height="650" referrerpolicy="no-referrer"
                                    style="border:1px solid #e6dccf;border-radius:16px"></iframe>
                            <p id="counterQrLink">
                                <a class="ob-back" href="<c:out value='${qr.CheckoutUrl}'/>"
                                   target="_blank" rel="noopener noreferrer">Mở QR trên trang payOS</a>
                            </p>
                        </c:if>
                    </div>
                    <p class="ob-note">Khách quét QR và chuyển đúng số tiền. Chỉ hệ thống xác nhận thanh toán;
                        không xác nhận dựa trên ảnh chụp hoặc thông báo từ trình duyệt.</p>
                    <p class="ob-note">Nếu chuyển thiếu, dư hoặc kết quả chưa rõ, giao dịch được giữ để đối soát;
                        không thu thêm bằng tiền mặt khi chưa xử lý xong.</p>
                    <a class="ob-button ob-secondary" href="<c:out value='${backUrl}'/>">Về hồ sơ booking</a>
                </section>
            </main>
            <%@include file="/WEB-INF/include/footer.jsp"%>
        </div>
        <script defer src="<c:url value='/assets/js/counter-qr.js?v=1'/>"></script>
    </body>
</html>
