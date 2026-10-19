<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Promotions - Luxe Escape</title>
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
                <div class="promo-eyebrow">LUXE ESCAPE OFFERS</div>
                <h1>Curated Promotions for Your Next Stay</h1>
                <p>
                    Discover exclusive savings on premium rooms and memorable getaways.
                    Every offer below is applied based on its real availability and booking conditions.
                </p>
                <div class="promo-hero__highlights">
                    <span>Exclusive rates</span>
                    <span>Flexible booking conditions</span>
                    <span>Luxury stays, better value</span>
                </div>
            </div>
            <div class="promo-hero__panel">
                <div class="promo-hero__panel-label">Featured Savings</div>
                <div class="promo-hero__panel-number">
                    <c:choose>
                        <c:when test="${not empty promotions}">
                            up to <strong>${promotions[0].saleOff}%</strong>
                        </c:when>
                        <c:otherwise>
                            seasonal
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="promo-hero__panel-text">
                    Offers are displayed from live promotion data in the system.
                </div>
            </div>
        </section>

        <section class="container-xl promo-section">
            <div class="promo-section__heading">
                <div>
                    <h2>Current Promotions</h2>
                    <p>Elegant offer cards with all conditions clearly displayed for customers.</p>
                </div>
                <c:if test="${not empty promotions}">
                    <div class="promo-count-badge">${promotions.size()} offers available</div>
                </c:if>
            </div>

            <c:choose>
                <c:when test="${empty promotions}">
                    <div class="promo-empty-state">
                        <div class="promo-empty-state__icon">✦</div>
                        <h3>No active promotions right now</h3>
                        <p>Please check back later for new hotel offers and seasonal deals.</p>
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
                                            <c:when test="${promotion.saleOff >= 40}">Best Deal</c:when>
                                            <c:when test="${promotion.saleOff >= 20}">Popular Offer</c:when>
                                            <c:otherwise>Special Offer</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>

                                <div class="promo-card__discount-row">
                                    <div>
                                        <div class="promo-card__discount-value">${promotion.saleOff}%</div>
                                        <div class="promo-card__discount-label">OFF YOUR BOOKING</div>
                                    </div>
                                    <div class="promo-card__badge">
                                        <span>Available</span>
                                        <strong>${promotion.quantity}</strong>
                                    </div>
                                </div>

                                <div class="promo-card__divider"></div>

                                <div class="promo-card__details">
                                    <div class="promo-detail-item">
                                        <span class="promo-detail-label">Valid from</span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty promotion.startDate}">${promotion.startDate}</c:when>
                                                <c:otherwise>No start limit</c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>

                                    <div class="promo-detail-item">
                                        <span class="promo-detail-label">Valid until</span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty promotion.endDate}">${promotion.endDate}</c:when>
                                                <c:otherwise>No end limit</c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>

                                    <div class="promo-detail-item">
                                        <span class="promo-detail-label">Minimum booking</span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty promotion.minimumAmount}">${promotion.minimumAmount} USD</c:when>
                                                <c:otherwise>None</c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>

                                    <div class="promo-detail-item">
                                        <span class="promo-detail-label">Maximum discount</span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty promotion.maximumDiscount}">${promotion.maximumDiscount} USD</c:when>
                                                <c:otherwise>No maximum</c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>
                                </div>

                                <div class="promo-card__footer">
                                    <span class="promo-card__footer-note">Use code <strong>${promotion.code}</strong> when your booking qualifies.</span>
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
