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
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-home-motion.css">
    </head>
    <body class="luxe-home ${cookie.animation.value == 'disable' ? 'disable-animation' : ''}">
        <%@include file="/WEB-INF/include/header.jsp" %>
        <c:if test="${sessionScope.loggedUser.role != 'admin'
                      and sessionScope.loggedUser.role != 'staff'
                      and sessionScope.loggedUser.role != 'manager'}">
              <form action="./toggle-animation" method="post" class="text-end mt-2 me-2">
                  <input type="hidden" name="toggleAnimation" value="${cookie.animation.value == "enable" || cookie.animation.value == null ? "disable" : "enable"}">
                  <button class="btn btn-sm btn-primary" type="submit">${cookie.animation.value == "enable" || cookie.animation.value == null ? "Disable" : "Enable"} <ui:text key="screen.7f7c330667649a84"/></button>
              </form>
        </c:if>
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
                    <img
                        src="assets/img/lobby.jpg"
                        id="hero-img"
                        class="card ${cookie.animation.value == "enable" ? "hidden" : ""}"
                        alt="${requestScope.i18n['screen.43b4ce3c717c67c9']}"
                         data-i18n-alt="screen.43b4ce3c717c67c9"/>
                </section>
                <section id="rooms">
                    <h2 id="rooms-heading" class="text left"><ui:text key="screen.261fa3456965d5d4"/></h2>
                    <div id="room-container">
                        <div class="room-card card">
                            <img
                                src="assets/img/standard-room.jpg"
                                class="zoom-in-hover"
                                alt="standard room"
                                />
                            <div class="room-description">
                                <p class="room-name">Standard Room</p>
                                <p><ui:text key="screen.b3cbc3e1b4b63552"/></p>
                                <a href="./booking" class="view-room"
                                   ><img
                                        src="assets/icons/arrow-up-right.svg"
                                        width="30"
                                        height="30"
                                        alt="${requestScope.i18n['screen.1c6333509debf060']}"
                                        class="arrow-up-right"
                                         data-i18n-alt="screen.1c6333509debf060"/>
                                </a>
                            </div>
                        </div>
                        <div class="room-card card">
                            <img src="assets/img/junior-1.jpg" alt="junior suite" />
                            <div class="room-description">
                                <p class="room-name">Junior Suite</p>
                                <p><ui:text key="screen.b3cbc3e1b4b63552"/></p>
                                <a href="./booking" class="view-room"
                                   ><img
                                        src="assets/icons/arrow-up-right.svg"
                                        width="30"
                                        height="30"
                                        alt="${requestScope.i18n['screen.1c6333509debf060']}"
                                        class="arrow-up-right"
                                         data-i18n-alt="screen.1c6333509debf060"/>
                                </a>
                            </div>
                        </div>
                        <div class="room-card card">
                            <img
                                src="assets/img/family-suite-1.jpg"
                                alt="family suite"
                                />
                            <div class="room-description">
                                <p class="room-name">Family Suite</p>
                                <p><ui:text key="screen.cbc967cf9701d94f"/></p>
                                <a href="./booking" class="view-room"
                                   ><img
                                        src="assets/icons/arrow-up-right.svg"
                                        width="30"
                                        height="30"
                                        alt="${requestScope.i18n['screen.1c6333509debf060']}"
                                        class="arrow-up-right"
                                         data-i18n-alt="screen.1c6333509debf060"/>
                                </a>
                            </div>
                        </div>
                        <div id="rooms-content" class="card">
                            <p>
                                <ui:text key="screen.bc54a632e2d20348"/>
                            </p>
                            <a href="./booking" class="cta-button" id="rooms-cta"><ui:text key="screen.b6b3d1164a649595"/></a>
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
                src="${pageContext.request.contextPath}/script/index.js">
        </script>
    </body>
</html>
