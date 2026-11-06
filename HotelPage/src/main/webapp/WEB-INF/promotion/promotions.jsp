<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title data-i18n="screen.0ef7a6fd6e4ace71">${requestScope.i18n['screen.0ef7a6fd6e4ace71']}</title>
    <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
    <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-promotions.css" />
    <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>
<body class="bg-light">
<div class="body-wrapper promotions-page">
    <%@ include file="/WEB-INF/include/header.jsp" %>

    <main class="promo-main">
        <section class="promo-hero container-xl">
            <div class="promo-hero__content">
                <div class="promo-eyebrow"><ui:text key="screen.b1d2a26d2113c301"/></div>
                <h1><ui:text key="screen.3c67986f977d8c2f"/></h1>
                <p>
                    <ui:text key="screen.920e69d8ac0832f5"/>
                </p>
                <div class="promo-hero__highlights">
                    <span><ui:text key="screen.88c34c527c099446"/></span>
                    <span><ui:text key="screen.4b3fb703e4629427"/></span>
                    <span><ui:text key="screen.752908159bd25f22"/></span>
                </div>
            </div>
            <div class="promo-hero__panel">
                <div class="promo-hero__panel-label"><ui:text key="screen.888a8321cc5a7308"/></div>
                <div class="promo-hero__panel-number">
                    <c:choose>
                        <c:when test="${not empty promotions}">
                            <ui:text key="screen.9c8dade5f4ebdb69"/> <strong>${promotions[0].saleOff}%</strong>
                        </c:when>
                        <c:otherwise>
                            <ui:text key="screen.8cf4415e871c0568"/>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="promo-hero__panel-text">
                    <ui:text key="screen.80fd302c907fcee5"/>
                </div>
            </div>
        </section>

        <section class="container-xl promo-section">
            <div class="promo-section__heading">
                <div>
                    <h2><ui:text key="screen.398b456c80e15fc0"/></h2>
                    <p><ui:text key="screen.8365923fef9833d5"/></p>
                </div>
                <c:if test="${not empty promotions}">
                    <div class="promo-count-badge">${promotions.size()} <ui:text key="screen.0b2b676e2070fa7a"/></div>
                </c:if>
            </div>

            <c:choose>
                <c:when test="${empty promotions}">
                    <div class="promo-empty-state">
                        <div class="promo-empty-state__icon">✦</div>
                        <h3><ui:text key="screen.28eb746ed71efa94"/></h3>
                        <p><ui:text key="screen.e550367f8fe37814"/></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="promo-grid">
                        <c:forEach var="promotion" items="${promotions}">
                            <article class="promo-card">
                                <div class="promo-card__topbar">
                                    <span class="promo-code-pill">${promotion.code}</span>
                                    <span class="promo-tag">
                                        <c:choose>
                                            <c:when test="${promotion.saleOff >= 40}"><ui:text key="screen.8088e0569328484c"/></c:when>
                                            <c:when test="${promotion.saleOff >= 20}"><ui:text key="screen.886223648e3eb639"/></c:when>
                                            <c:otherwise><ui:text key="screen.b0749da9d07b2bb8"/></c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>

                                <div class="promo-card__discount-row">
                                    <div>
                                        <div class="promo-card__discount-value">${promotion.saleOff}%</div>
                                        <div class="promo-card__discount-label"><ui:text key="screen.9ef76b81f55c378e"/></div>
                                    </div>
                                    <div class="promo-card__badge">
                                        <span><ui:text key="screen.e674447337e83c13"/></span>
                                        <strong>${promotion.quantity}</strong>
                                    </div>
                                </div>

                                <div class="promo-card__divider"></div>

                                <div class="promo-card__details">
                                    <div class="promo-detail-item">
                                        <span class="promo-detail-label"><ui:text key="screen.9a56d91b557102a1"/></span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty promotion.startDate}">${promotion.startDate}</c:when>
                                                <c:otherwise><ui:text key="screen.3afb2146c8ec5dd2"/></c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>

                                    <div class="promo-detail-item">
                                        <span class="promo-detail-label"><ui:text key="screen.92cd054da773366b"/></span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty promotion.endDate}">${promotion.endDate}</c:when>
                                                <c:otherwise><ui:text key="screen.67f1fb49d6ea5040"/></c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>

                                    <div class="promo-detail-item">
                                        <span class="promo-detail-label"><ui:text key="screen.1f5a4a67ec147476"/></span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty promotion.minimumAmount}">${promotion.minimumAmount} USD</c:when>
                                                <c:otherwise><ui:text key="screen.dc937b59892604f5"/></c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>

                                    <div class="promo-detail-item">
                                        <span class="promo-detail-label"><ui:text key="screen.b97d28b155bd566e"/></span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty promotion.maximumDiscount}">${promotion.maximumDiscount} USD</c:when>
                                                <c:otherwise><ui:text key="screen.b5cfdf2524d479e0"/></c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>
                                </div>

                                <div class="promo-card__footer">
                                    <span class="promo-card__footer-note"><ui:text key="screen.371111bf402bc397"/> <strong>${promotion.code}</strong> <ui:text key="screen.8440bde3ef010ce0"/></span>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <%@ include file="/WEB-INF/include/footer.jsp" %>
</div>
<script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
