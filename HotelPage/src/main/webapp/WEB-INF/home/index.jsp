<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : index.jsp
    Created on : Jun 14, 2025, 4:08:24 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Luxe Escape Hotel</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/landing-page.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css?v=hero-gold" />
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-home-motion.css?v=lobby-parallax">
    </head>
    <body class="luxe-home ${cookie.animation.value == 'disable' ? 'disable-animation' : ''}">
        <%@include file="/WEB-INF/include/header.jsp" %>
        <main>
            <div class="main-wrapper">
                <section id="hero">
                    <div class="hero-wrapper">
                        <h1 id="hero-heading">
                            <span class="text left ${cookie.animation.value == "enable" ? "hidden" : ""}"><ui:text key="screen.6cc1baff4c4f7a2c"/></span
                            ><span class="text right ${cookie.animation.value == "enable" ? "hidden" : ""}">LUXE ESCAPE</span>
                        </h1>
                        <div id="hero-content" class="text left ${cookie.animation.value == "enable" ? "hidden" : ""}">
                            <div id="hero-text">
                                <p class="mb-0"><ui:text key="screen.d74b92a2c888a6b0"/></p>
                                <p class="mt-0"><ui:text key="screen.57bfbbcaec61ea4e"/></p>
                            </div>
                            <a href="./booking" class="cta-button" id="hero-cta"><ui:text key="screen.b6b3d1164a649595"/></a>
                        </div>
                    </div>
                    <div id="hero-img" class="card">
                        <img
                            src="${pageContext.request.contextPath}/assets/img/lobby.jpg"
                            class="lobby-photo"
                            alt="${requestScope.i18n['screen.43b4ce3c717c67c9']}"
                            data-i18n-alt="screen.43b4ce3c717c67c9"/>
                    </div>
                </section>
                <section id="rooms">
                    <div class="rooms-header">
                        <h2 id="rooms-heading" class="text left"><ui:text key="screen.261fa3456965d5d4"/></h2>
                        <div class="rooms-navigation" hidden>
                            <button type="button" data-room-prev aria-controls="room-container" aria-disabled="true">
                                <span aria-hidden="true">&larr;</span>
                                <ui:text key="screen.a57b08a480b822a0"/>
                            </button>
                            <button type="button" data-room-next aria-controls="room-container">
                                <ui:text key="screen.1ff57a29d7c9d11b"/>
                                <span aria-hidden="true">&rarr;</span>
                            </button>
                        </div>
                    </div>

                    <div id="room-container" class="rooms-carousel" role="region" aria-labelledby="rooms-heading" tabindex="0">
                        <!-- 1. Deluxe City View -->
                        <div class="room-card card">
                            <div class="room-img-wrap">
                                <img src="${pageContext.request.contextPath}/assets/img/roomType/standard-room.jpg"
                                     class="zoom-in-hover" 
                                     alt="Deluxe City View" />
                            </div>
                            <div class="room-description">
                                <p class="room-name">Deluxe City View</p>
                                <p class="room-short-desc">Panoramic skyline views with modern bespoke interiors.</p>
                                <a href="${pageContext.request.contextPath}/booking?type=deluxe-city" class="view-room">
                                    <img src="${pageContext.request.contextPath}/assets/icons/arrow-up-right.svg" 
                                         width="30" height="30" 
                                         alt="${requestScope.i18n['screen.1c6333509debf060']}" 
                                         class="arrow-up-right" 
                                         data-i18n-alt="screen.1c6333509debf060"/>
                                </a>
                            </div>
                        </div>

                        <!-- 2. Premium Ocean View -->
                        <div class="room-card card">
                            <div class="room-img-wrap">
                                <img src="${pageContext.request.contextPath}/assets/img/roomType/junior-1.jpg"
                                     class="zoom-in-hover" 
                                     alt="Premium Ocean View" />
                            </div>
                            <div class="room-description">
                                <p class="room-name">Premium Ocean View</p>
                                <p class="room-short-desc">Private balcony overlooking the tranquil turquoise sea.</p>
                                <a href="${pageContext.request.contextPath}/booking?type=premium-ocean" class="view-room">
                                    <img src="${pageContext.request.contextPath}/assets/icons/arrow-up-right.svg" 
                                         width="30" height="30" 
                                         alt="${requestScope.i18n['screen.1c6333509debf060']}" 
                                         class="arrow-up-right" 
                                         data-i18n-alt="screen.1c6333509debf060"/>
                                </a>
                            </div>
                        </div>

                        <!-- 3. Executive Club Room -->
                        <div class="room-card card">
                            <div class="room-img-wrap">
                                <img src="${pageContext.request.contextPath}/assets/img/roomType/junior-3.jpg"
                                     class="zoom-in-hover" 
                                     alt="Executive Club Room" />
                            </div>
                            <div class="room-description">
                                <p class="room-name">Executive Club Room</p>
                                <p class="room-short-desc">Exclusive lounge access paired with refined workspace.</p>
                                <a href="${pageContext.request.contextPath}/booking?type=executive-club" class="view-room">
                                    <img src="${pageContext.request.contextPath}/assets/icons/arrow-up-right.svg" 
                                         width="30" height="30" 
                                         alt="${requestScope.i18n['screen.1c6333509debf060']}" 
                                         class="arrow-up-right" 
                                         data-i18n-alt="screen.1c6333509debf060"/>
                                </a>
                            </div>
                        </div>

                        <!-- 4. Grand Family Suite -->
                        <div class="room-card card">
                            <div class="room-img-wrap">
                                <img src="${pageContext.request.contextPath}/assets/img/roomType/family-suite-1.jpg"
                                     class="zoom-in-hover" 
                                     alt="Grand Family Suite" />
                            </div>
                            <div class="room-description">
                                <p class="room-name">Grand Family Suite</p>
                                <p class="room-short-desc">Spacious interconnected living area for family comfort.</p>
                                <a href="${pageContext.request.contextPath}/booking?type=grand-family" class="view-room">
                                    <img src="${pageContext.request.contextPath}/assets/icons/arrow-up-right.svg" 
                                         width="30" height="30" 
                                         alt="${requestScope.i18n['screen.1c6333509debf060']}" 
                                         class="arrow-up-right" 
                                         data-i18n-alt="screen.1c6333509debf060"/>
                                </a>
                            </div>
                        </div>

                        <!-- 5. Presidential Suite -->
                        <div class="room-card card">
                            <div class="room-img-wrap">
                                <img src="${pageContext.request.contextPath}/assets/img/roomType/VipProMaxRoom.jpg"
                                     class="zoom-in-hover" 
                                     alt="Presidential Suite" />
                            </div>
                            <div class="room-description">
                                <p class="room-name">Presidential Suite</p>
                                <p class="room-short-desc">Unrivaled penthouse luxury with private butler service.</p>
                                <a href="${pageContext.request.contextPath}/booking?type=presidential" class="view-room">
                                    <img src="${pageContext.request.contextPath}/assets/icons/arrow-up-right.svg" 
                                         width="30" height="30" 
                                         alt="${requestScope.i18n['screen.1c6333509debf060']}" 
                                         class="arrow-up-right" 
                                         data-i18n-alt="screen.1c6333509debf060"/>
                                </a>
                            </div>
                        </div>
                    </div>
                </section>
                <section id="restaurant">
                    <div id="restaurant-leap"></div>
                    <div class="restaurant-grid">
                        <div class="restaurant-wrap card">
                            <div class="restaurant-content">
                                <h2 id="restaurant-heading"><ui:text key="screen.cdf780aa8a6db763"/></h2>
                                <p>
                                    <ui:text key="screen.e52a3a77255e3b5e"/>
                                </p>
                            </div>
                            <a href="./booking" class="cta-button" id="restaurant-cta"
                               ><ui:text key="screen.b6b3d1164a649595"/></a
                            >
                        </div>
                        <div class="img-wrapper card">
                            <img
                                src="assets/img/restaurant-2.jpg"
                                class="img-fluid"
                                alt="${requestScope.i18n['screen.5ce401a9a57995a5']}"
                                data-i18n-alt="screen.5ce401a9a57995a5"/>
                        </div>
                    </div>
                </section>
            </div>
            <section id="call-to-action">
                <h2 class="text left"><ui:text key="screen.d16572613a6246df"/></h2>
                <!-- <img
                    src="assets/img/cta-background.jpg"
                    class="img-fluid"
                    alt="hotel reception"
                  /> -->
                <a href="./booking" class="cta-button text bottom" id="cta-button"
                   ><ui:text key="screen.b6b3d1164a649595"/></a
                >
            </section>
        </main>


        <%@include file="/WEB-INF/include/footer.jsp" %>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script type="module"
                src="${pageContext.request.contextPath}/script/index.js?v=lobby-parallax">
        </script>
    </body>
</html>
