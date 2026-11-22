
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Yêu cầu rút tiền - Luxe Escape</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/general.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/luxe-modern.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">

    <style>
        body {
            background: #faf7f2;
            color: #32291f;
        }

        .withdrawal-container {
            max-width: 850px;
            margin: 50px auto;
            padding: 20px;
        }

        .withdrawal-title {
            font-family: Georgia, serif;
            font-size: 40px;
            margin-bottom: 20px;
        }

        .withdrawal-card {
            background: #fff;
            border: 1px solid #e7ded1;
            border-radius: 18px;
            padding: 30px;
            margin-bottom: 25px;
        }

        .balance-card {
            background: linear-gradient(
                115deg, #31271f, #64503d
            );
            color: #fff;
            border-radius: 18px;
            padding: 28px;
            margin-bottom: 25px;
        }

        .balance-amount {
            font-family: Georgia, serif;
            font-size: 36px;
            font-weight: 600;
        }

        .withdrawal-btn {
            background: #b49a78;
            border: none;
            color: #fff;
            padding: 11px 22px;
            border-radius: 9px;
        }

        .withdrawal-btn:hover {
            background: #967c5b;
            color: #fff;
        }
    </style>
</head>

<body>

    <jsp:include page="/WEB-INF/include/header.jsp"/>

    <main class="withdrawal-container">

        <h1 class="withdrawal-title">
            Yêu cầu rút tiền
        </h1>

        <p class="text-muted">
            Gửi yêu cầu rút tiền từ ví của bạn.
        </p>

        <c:if test="${param.success == '1'}">
            <div class="alert alert-success">
                Đã gửi yêu cầu rút tiền và đang chờ duyệt.
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger">
                <c:choose>
                    <c:when test="${param.error == 'invalid-amount'}">
                        Vui lòng nhập số tiền rút hợp lệ.
                    </c:when>
                    <c:when test="${param.error == 'invalid-bank'}">
                        Vui lòng kiểm tra thông tin ngân hàng.
                    </c:when>
                    <c:when test="${param.error == 'insufficient-balance'}">
                        Số dư khả dụng không đủ.
                    </c:when>
                    <c:otherwise>
                        Chưa gửi được yêu cầu.
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <div class="balance-card">
            <div class="small text-uppercase mb-2">
                Số dư khả dụng
            </div>

            <div class="balance-amount">
                <fmt:formatNumber
                    value="${availableBalance}"
                    type="number"
                    maxFractionDigits="0"/>
                VND
            </div>

            <div class="mt-3 small">
                Số dư ví:
                <fmt:formatNumber
                    value="${walletBalance}"
                    maxFractionDigits="0"/>
                VND
            </div>

            <div class="small mt-1">
                Đang giữ cho yêu cầu rút tiền:
                <fmt:formatNumber
                    value="${reservedAmount}"
                    maxFractionDigits="0"/>
                VND
            </div>
        </div>

        <section class="withdrawal-card">

            <h3 class="mb-4">
                Thông tin rút tiền
            </h3>

            <form action="${pageContext.request.contextPath}/withdrawal"
                  method="post">

                <input type="hidden"
                       name="csrfToken"
                       value="${sessionScope.withdrawalCsrfToken}">

                <div class="mb-3">
                    <label class="form-label">
                        Số tiền rút (VND)
                    </label>

                    <input type="number"
                           name="amount"
                           class="form-control"
                           min="1"
                           step="1"
                           required>

                    <div class="form-text">
                        Số tiền rút không được vượt quá số dư khả dụng.
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">
                        Tên ngân hàng
                    </label>

                    <input type="text"
                           name="bankName"
                           class="form-control"
                           maxlength="100"
                           required>
                </div>

                <div class="mb-3">
                    <label class="form-label">
                        Số tài khoản
                    </label>

                    <input type="text"
                           name="accountNumber"
                           class="form-control"
                           inputmode="numeric"
                           pattern="[0-9]{6,30}"
                           maxlength="30"
                           required>
                </div>

                <div class="mb-4">
                    <label class="form-label">
                        Chủ tài khoản
                    </label>

                    <input type="text"
                           name="accountHolder"
                           class="form-control"
                           maxlength="150"
                           required>
                </div>

                <div class="alert alert-light border">
                    Yêu cầu rút tiền cần được duyệt trước khi xác nhận chi trả. Vui lòng kiểm tra kỹ thông tin ngân hàng.
                </div>

                <div class="d-flex gap-3 flex-wrap">

                    <button type="submit"
                            class="withdrawal-btn">
                        Gửi yêu cầu rút tiền
                    </button>

                    <a href="${pageContext.request.contextPath}/wallet"
                       class="btn btn-outline-secondary">
                        Về ví
                    </a>

                    <a href="${pageContext.request.contextPath}/my-withdrawals"
                       class="btn btn-outline-secondary">
                        Yêu cầu của tôi
                    </a>
                </div>
            </form>
        </section>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
