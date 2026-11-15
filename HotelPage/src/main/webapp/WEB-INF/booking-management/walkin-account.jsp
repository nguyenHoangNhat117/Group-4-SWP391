<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<%@page import="util.AuthSupport"%>
<!DOCTYPE html><html lang="${requestScope.uiLanguage}"><head>
        <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
        <title data-i18n="uc24.activation.title"><c:out value="${requestScope.i18n['uc24.activation.title']}"/></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-auth.css">
        <%@include file="/WEB-INF/include/auth-password-toggle.jsp"%>
        <style>.walkin-activation .wrapper{
            min-height:auto
        }
        .walkin-activation .form-box{
            padding-top:2rem;
            padding-bottom:2rem
        }
        .walkin-activation .input-box{
            margin:22px 0
        }
        .walkin-activation .auth-hint{
            color:#fff;
            line-height:1.6;
            font-size:.9rem
        }
        .walkin-activation h1{
            font-size:1.8rem
        }</style>
    </head><body class="page-login walkin-activation">
        <header><span><a href="${pageContext.request.contextPath}/">Luxe Escape</a></span><jsp:include page="/WEB-INF/include/language-switcher.jsp"/></header>
        <main class="wrapper show"><div class="form-box">
                <h1 class="text-center"><ui:text key="uc24.activation.heading"/></h1>
                <c:if test="${not empty error}"><div class="alert alert-danger mt-3" role="alert"><ui:message text="${error}"/></div></c:if>
                <c:choose><c:when test="${activated}">
                        <div class="alert alert-success mt-3" role="status"><ui:text key="uc24.activation.success"/></div>
                        <a class="btn" href="${pageContext.request.contextPath}/login"><ui:text key="uc24.activation.login"/></a>
                    </c:when><c:otherwise>
                        <p class="auth-hint"><ui:text key="uc24.activation.intro"/></p>
                        <form method="post" action="${pageContext.request.contextPath}/walkin-account">
                            <input type="hidden" name="csrfToken" value="<%= AuthSupport.token(session)%>">
                            <div class="input-box"><input id="activationEmail" type="email" name="email" maxlength="100" required autocomplete="email" placeholder=" " value="${fn:escapeXml(not empty enteredEmail ? enteredEmail : challengeEmail)}"><label for="activationEmail"><ui:text key="uc24.activation.email"/></label></div>
                            <button class="btn" name="action" value="send" type="submit"><ui:text key="uc24.activation.send"/></button>
                        </form>
                        <c:if test="${not empty challengeEmail}"><p class="auth-hint mt-3" role="status"><ui:text key="uc24.activation.sent"/></p></c:if>
                        <c:if test="${challengeActive}">
                            <p class="auth-hint"><ui:text key="uc24.activation.verifying"/> <strong data-user-content><c:out value="${challengeEmail}"/></strong>. <ui:text key="uc24.activation.attempts"/> <c:out value="${remainingAttempts}"/>.</p>
                            <form method="post" action="${pageContext.request.contextPath}/walkin-account" autocomplete="on">
                                <input type="hidden" name="csrfToken" value="<%= AuthSupport.token(session)%>">
                                <div class="input-box"><input id="activationOtp" name="otp" inputmode="numeric" pattern="[0-9]{6}" minlength="6" maxlength="6" required autocomplete="one-time-code" placeholder=" "><label for="activationOtp"><ui:text key="uc24.activation.otp"/></label></div>
                                <div class="input-box"><input id="activationUsername" name="username" minlength="3" maxlength="50" required autocomplete="username" placeholder=" " value="${fn:escapeXml(enteredUsername)}"><label for="activationUsername"><ui:text key="uc24.activation.username"/></label></div>
                                <div class="input-box"><input id="password" name="password" type="password" minlength="8" maxlength="128" required autocomplete="new-password" placeholder=" "><label for="password"><ui:text key="uc24.activation.password"/></label></div>
                                <div class="input-box"><input id="confirmPassword" name="confirmPassword" type="password" minlength="8" maxlength="128" required autocomplete="new-password" placeholder=" "><label for="confirmPassword"><ui:text key="uc24.activation.confirm"/></label></div>
                                <p class="auth-hint"><ui:text key="uc24.activation.passwordHint"/></p>
                                <button class="btn" name="action" value="activate" type="submit"><ui:text key="uc24.activation.activate"/></button>
                            </form></c:if>
                        <p class="auth-hint mt-3"><ui:text key="uc24.activation.existing"/> <a href="${pageContext.request.contextPath}/login"><ui:text key="uc24.activation.login"/></a> <ui:text key="uc24.activation.or"/> <a href="${pageContext.request.contextPath}/forgot-password"><ui:text key="uc24.activation.recover"/></a>.</p>
                    </c:otherwise></c:choose>
            </div></main><footer>© 2026 Luxe Escape</footer>
    </body></html>