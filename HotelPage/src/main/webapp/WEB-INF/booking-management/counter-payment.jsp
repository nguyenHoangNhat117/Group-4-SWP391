<%-- 
    Document   : counter-payment
    Created on : Nov 5, 2026, 9:48:52 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="vi" class="uc09-page">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title>Thu tiền tại quầy | Luxe Escape</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <style>
            html {
                scroll-snap-type: none !important;
                scroll-behavior: auto !important;
            }

            main section {
                scroll-snap-align: none !important;
                scroll-margin-top: 0 !important;
            }
        </style>
    </head>

    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main class="container py-5" style="max-width:850px">
                <h1 class="h2 mb-4">
                    Thu tiền tại quầy · Booking #${summary.bookingId}
                </h1>

                <c:if test="${param.created == '1'}">
                    <div class="alert alert-success">
                        Đã tạo đặt phòng. Chưa ghi nhận thanh toán.
                    </div>
                </c:if>

                <c:if test="${param.paid == '1'}">
                    <div class="alert alert-success">
                        Đã ghi nhận thanh toán thành công.
                    </div>
                </c:if>

                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger">
                        Thao tác chưa hoàn tất: số dư, trạng thái
                        hoặc khuyến mãi có thể đã thay đổi.
                        Kiểm tra thông tin mới bên dưới trước khi thử lại.
                    </div>
                </c:if>

                <section class="card shadow-sm">
                    <div class="card-body p-4">
                        <dl class="row">
                            <dt class="col-sm-5">Khách hàng</dt>
                            <dd class="col-sm-7">
                                <c:out value="${summary.customerName}"/>
                            </dd>

                            <dt class="col-sm-5">Người tạo đặt phòng</dt>
                            <dd class="col-sm-7">
                                <c:out value="${summary.creator}"/>
                            </dd>

                            <dt class="col-sm-5">Trạng thái</dt>
                            <dd class="col-sm-7">
                                <c:out value="${summary.status}"/>
                            </dd>

                            <dt class="col-sm-5">
                                Hạn giữ phòng — giờ Việt Nam
                            </dt>
                            <dd class="col-sm-7">
                                <c:out value="${summary.holdExpiresAt}"/>
                            </dd>

                            <dt class="col-sm-5">Tổng tiền hiện lưu</dt>
                            <dd class="col-sm-7">
                                <fmt:formatNumber value="${summary.total}"/>
                                VND
                            </dd>

                            <dt class="col-sm-5">Mã khuyến mãi đã chọn</dt>
                            <dd class="col-sm-7">
                                <c:out value="${empty summary.promotionCode
                                                ? 'Không áp dụng'
                                                : summary.promotionCode}"/>
                            </dd>

                            <dt class="col-sm-5">Tổng sau khuyến mãi</dt>
                            <dd class="col-sm-7">
                                <fmt:formatNumber value="${summary.payable}"/>
                                VND
                            </dd>

                            <dt class="col-sm-5">Đã thu</dt>
                            <dd class="col-sm-7">
                                <fmt:formatNumber value="${summary.paid}"/>
                                VND
                            </dd>

                            <dt class="col-sm-5">Còn phải thu</dt>
                            <dd class="col-sm-7 fw-bold">
                                <fmt:formatNumber value="${summary.remaining}"/>
                                VND
                            </dd>
                        </dl>

                        <c:if test="${not empty summary.promotionError}">
                            <div class="alert alert-warning">
                                <c:out value="${summary.promotionError}"/>
                                Sửa mã hoặc để trống để bỏ mã,
                                sau đó kiểm tra lại số tiền trước khi thu.
                            </div>
                        </c:if>

                        <c:choose>
                            <c:when test="${summary.canPay}">
                                <form action="${pageContext.request.contextPath}/counter-payment"
                                      method="post">

                                    <input type="hidden"
                                           name="csrfToken"
                                           value="${sessionScope.csrfToken}">

                                    <input type="hidden"
                                           name="paymentKey"
                                           value="${paymentKey}">

                                    <div class="row g-3">
                                        <div class="col-md-6">
                                            <label for="method"
                                                   class="form-label">
                                                Phương thức đã nhận tiền
                                            </label>

                                            <select class="form-select"
                                                    name="method"
                                                    id="method">
                                                <option value="cash">
                                                    Tiền mặt
                                                </option>
                                                <option value="bank_transfer">
                                                    Chuyển khoản đã xác nhận
                                                </option>
                                            </select>
                                        </div>

                                        <div class="col-md-6">
                                            <label for="percent"
                                                   class="form-label">
                                                Khoản thu
                                            </label>

                                            <select class="form-select"
                                                    name="percent"
                                                    id="percent">

                                                <c:if test="${summary.firstPayment
                                                              and summary.depositPercent < 100}">
                                                      <option value="${summary.depositPercent}">
                                                          Đặt cọc
                                                          ${summary.depositPercent}%
                                                          tổng sau khuyến mãi
                                                      </option>
                                                </c:if>

                                                <option value="100" selected>
                                                    Toàn bộ số tiền còn lại
                                                </option>
                                            </select>
                                        </div>
                                    </div>

                                    <p class="text-muted small mt-3">
                                        Chỉ xác nhận sau khi đã thực nhận tiền.
                                        Thanh toán thành công xác nhận
                                        booking pending; không tự check-in.
                                        Khuyến mãi chỉ trừ lượt trong
                                        giao dịch thanh toán đầu tiên.
                                    </p>

                                    <button class="btn btn-primary"
                                            type="submit">
                                        Xác nhận đã nhận tiền
                                    </button>
                                </form>
                            </c:when>

                            <c:otherwise>
                                <p class="alert alert-info">
                                    Không còn khoản phải thu hoặc trạng thái/
                                    khuyến mãi hiện tại chưa cho phép thu tiền.
                                </p>
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${summary.canChange}">
                            <hr>

                            <form action="${pageContext.request.contextPath}/counter-payment"
                                  method="post"
                                  class="mb-3">

                                <input type="hidden"
                                       name="csrfToken"
                                       value="${sessionScope.csrfToken}">

                                <input type="hidden"
                                       name="paymentKey"
                                       value="${paymentKey}">

                                <label for="promotionCode" class="form-label">
                                    Đổi hoặc bỏ mã khuyến mãi
                                    trước thanh toán
                                </label>

                                <div class="input-group">
                                    <input class="form-control"
                                           name="promotionCode"
                                           id="promotionCode"
                                           maxlength="50"
                                           value="<c:out value='${summary.promotionCode}'/>">

                                    <button class="btn btn-outline-secondary"
                                            name="action"
                                            value="promotion">
                                        Tính lại
                                    </button>
                                </div>
                            </form>

                            <form action="${pageContext.request.contextPath}/counter-payment"
                                  method="post">

                                <input type="hidden"
                                       name="csrfToken"
                                       value="${sessionScope.csrfToken}">

                                <input type="hidden"
                                       name="paymentKey"
                                       value="${paymentKey}">

                                <button class="btn btn-outline-danger"
                                        name="action"
                                        value="cancel"
                                        onclick="return confirm('Hủy đặt phòng chưa thanh toán này?')">
                                    Hủy theo yêu cầu khách
                                </button>
                            </form>

                            <p class="small text-muted mt-2">
                                Chờ thanh toán không kéo dài thời hạn
                                giữ phòng. Có thể mở lại màn này
                                từ chi tiết booking.
                            </p>
                        </c:if>
                    </div>
                </section>

                <div class="d-flex gap-2 mt-4">
                    <a class="btn btn-outline-secondary"
                       href="${pageContext.request.contextPath}/booking-management?view=detail&amp;id=${summary.bookingId}">
                        ${summary.canChange
                          ? 'Để thanh toán sau / xem chi tiết'
                          : 'Chi tiết đặt phòng'}
                    </a>

                    <a class="btn btn-outline-primary"
                       href="${pageContext.request.contextPath}/offline-booking">
                        Tạo đặt phòng khác
                    </a>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp"%>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>