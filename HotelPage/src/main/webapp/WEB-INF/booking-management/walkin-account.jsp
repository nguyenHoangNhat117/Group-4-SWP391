<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@page import="util.AuthSupport"%>
<!DOCTYPE html><html lang="vi"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Kích hoạt tài khoản khách tại quầy | Luxe Escape</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-auth.css">
<%@include file="/WEB-INF/include/auth-password-toggle.jsp"%>
<style>.walkin-activation .wrapper{min-height:auto}.walkin-activation .form-box{padding-top:2rem;padding-bottom:2rem}.walkin-activation .input-box{margin:22px 0}.walkin-activation .auth-hint{color:#fff;line-height:1.6;font-size:.9rem}.walkin-activation h1{font-size:1.8rem}</style>
</head><body class="page-login walkin-activation">
<header><span><a href="${pageContext.request.contextPath}/">Luxe Escape</a></span></header>
<main class="wrapper show"><div class="form-box">
<h1 class="text-center">Kích hoạt tài khoản</h1>
<c:if test="${not empty error}"><div class="alert alert-danger mt-3" role="alert"><c:out value="${error}"/></div></c:if>
<c:choose><c:when test="${activated}">
<div class="alert alert-success mt-3" role="status">Đã tạo tài khoản và liên kết hồ sơ tại quầy. Lịch sử đặt phòng được giữ nguyên.</div>
<a class="btn" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
</c:when><c:otherwise>
<p class="auth-hint">Dành cho khách đã đặt tại quầy nhưng chưa có tài khoản website. Xác minh email trên hồ sơ để giữ lại lịch sử đặt phòng.</p>
<form method="post" action="${pageContext.request.contextPath}/walkin-account">
<input type="hidden" name="csrfToken" value="<%= AuthSupport.token(session) %>">
<div class="input-box"><input id="activationEmail" type="email" name="email" maxlength="100" required autocomplete="email" placeholder=" " value="${fn:escapeXml(not empty enteredEmail ? enteredEmail : challengeEmail)}"><label for="activationEmail">Email đã cung cấp tại quầy</label></div>
<button class="btn" name="action" value="send" type="submit">Gửi / gửi lại mã xác minh</button>
</form>
<c:if test="${not empty challengeEmail}"><p class="auth-hint mt-3" role="status">Nếu hồ sơ đủ điều kiện, mã đã được gửi tới email. Mã có hiệu lực 5 phút, tối đa 3 lần gửi form kích hoạt. Không có email trên hồ sơ hoặc email sai: liên hệ lễ tân.</p></c:if>
<c:if test="${challengeActive}">
<p class="auth-hint">Email đang xác minh: <strong><c:out value="${challengeEmail}"/></strong>. Còn <c:out value="${remainingAttempts}"/> lượt.</p>
<form method="post" action="${pageContext.request.contextPath}/walkin-account" autocomplete="on">
<input type="hidden" name="csrfToken" value="<%= AuthSupport.token(session) %>">
<div class="input-box"><input id="activationOtp" name="otp" inputmode="numeric" pattern="[0-9]{6}" minlength="6" maxlength="6" required autocomplete="one-time-code" placeholder=" "><label for="activationOtp">Mã xác minh 6 số</label></div>
<div class="input-box"><input id="activationUsername" name="username" minlength="3" maxlength="50" required autocomplete="username" placeholder=" " value="${fn:escapeXml(enteredUsername)}"><label for="activationUsername">Username mới</label></div>
<div class="input-box"><input id="password" name="password" type="password" minlength="8" maxlength="128" required autocomplete="new-password" placeholder=" "><label for="password">Mật khẩu mới</label></div>
<div class="input-box"><input id="confirmPassword" name="confirmPassword" type="password" minlength="8" maxlength="128" required autocomplete="new-password" placeholder=" "><label for="confirmPassword">Xác nhận mật khẩu</label></div>
<p class="auth-hint">Mật khẩu 8–128 ký tự: có chữ hoa, chữ thường, số và ký tự đặc biệt; không có khoảng trắng.</p>
<button class="btn" name="action" value="activate" type="submit">Xác minh và tạo tài khoản</button>
</form></c:if>
<p class="auth-hint mt-3">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a> hoặc <a href="${pageContext.request.contextPath}/forgot-password">khôi phục mật khẩu</a>.</p>
</c:otherwise></c:choose>
</div></main><footer>© 2026 Luxe Escape</footer>
</body></html>
