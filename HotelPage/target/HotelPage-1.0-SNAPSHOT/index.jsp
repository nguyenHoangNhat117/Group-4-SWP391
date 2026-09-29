<%-- 
    Document   : index.jsp
    Created on : Jun 14, 2025, 4:08:24 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Luxe Escape Hotel</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/landing-page.css" />
    </head>
    <body class="${cookie.animation.value == 'enable' ? '' : 'disable-animation'}">
        <%@include file="/WEB-INF/include/header.jsp" %>
        <form action="./toggle-animation" method="post" class="text-end mt-2 me-2">
            <input type="hidden" name="toggleAnimation" value="${cookie.animation.value == "enable" || cookie.animation.value == null ? "disable" : "enable"}">
            <button class="btn btn-sm btn-primary" type="submit">${cookie.animation.value == "enable" || cookie.animation.value == null ? "Disable" : "Enable"} Animations</button>
        </form>
        <main>
            <div class="main-wrapper">
                <section id="hero">
                    <div class="hero-wrapper">
                        <h1 id="hero-heading">
                            <span class="text left ${cookie.animation.value == "enable" ? "hidden" : ""}">WELCOME TO</span
                            ><span class="text right ${cookie.animation.value == "enable" ? "hidden" : ""}">LUXE ESCAPE</span>
                        </h1>
                        <div id="hero-content" class="text left ${cookie.animation.value == "enable" ? "hidden" : ""}">
                            <div id="hero-text">
                                <p class="mb-0">Welcome to our Cozy Haven, where Comfort Meets</p>
                                <p class="mt-0">Style and Exceptional Service Elevates Every Experience</p>
                            </div>
                            <a href="./booking" class="cta-button" id="hero-cta">Book now</a>
                        </div>
                    </div>
                    <img
                        src="assets/img/lobby.jpg"
                        id="hero-img"
                        class="card ${cookie.animation.value == "enable" ? "hidden" : ""}"
                        alt="hotel lobby"
                        />
                </section>
                <section id="rooms">
                    <h2 id="rooms-heading" class="text left">OUR ROOMS</h2>
                    <div id="room-container">
                        <div class="room-card card">
                            <img
                                src="assets/img/standard-room.jpg"
                                class="zoom-in-hover"
                                alt="standard room"
                                />
                            <div class="room-description">
                                <p class="room-name">Standard Room</p>
                                <p>1 bed | 2 sleeps</p>
                                <a href="./booking" class="view-room"
                                   ><img
                                        src="assets/icons/arrow-up-right.svg"
                                        width="30"
                                        height="30"
                                        alt="show"
                                        class="arrow-up-right"
                                        />
                                </a>
                            </div>
                        </div>
                        <div class="room-card card">
                            <img src="assets/img/junior-1.jpg" alt="junior suite" />
                            <div class="room-description">
                                <p class="room-name">Junior Suite</p>
                                <p>1 bed | 2 sleeps</p>
                                <a href="./booking" class="view-room"
                                   ><img
                                        src="assets/icons/arrow-up-right.svg"
                                        width="30"
                                        height="30"
                                        alt="show"
                                        class="arrow-up-right"
                                        />
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
                                <p>2 double beds | 6 sleeps</p>
                                <a href="./booking" class="view-room"
                                   ><img
                                        src="assets/icons/arrow-up-right.svg"
                                        width="30"
                                        height="30"
                                        alt="show"
                                        class="arrow-up-right"
                                        />
                                </a>
                            </div>
                        </div>
                        <div id="rooms-content" class="card">
                            <p>
                                The hotel offers different room categories that are suitable for
                                both business travelers and vacationers
                            </p>
                            <a href="./booking" class="cta-button" id="rooms-cta">Book now</a>
                        </div>
                    </div>
                </section>
                <section id="restaurant">
                    <div id="restaurant-leap"></div>
                    <div class="restaurant-grid">
                        <div class="restaurant-wrap card">
                            <div class="restaurant-content">
                                <h2 id="restaurant-heading">RESTAURANT AT THE HOTEL</h2>
                                <p>
                                    Our chefs offer both international and local dishes,
                                    prepared only with fresh ingredients, in the cozy atmosphere
                                    of the restaurant, you can enjoy not only delicious food but
                                    also pleasant music and a friendly ambiance.
                                </p>
                            </div>
                            <a href="./booking" class="cta-button" id="restaurant-cta"
                               >Book now</a
                            >
                        </div>
                        <div class="img-wrapper card">
                            <img
                                src="assets/img/restaurant-2.jpg"
                                class="img-fluid"
                                alt="restaurant tabel setup"
                                />
                        </div>
                    </div>
                </section>
            </div>
            <section id="call-to-action">
                <h2 class="text left">ESCAPE TODAY - BOOK YOUR ROOM</h2>
                <!-- <img
                    src="assets/img/cta-background.jpg"
                    class="img-fluid"
                    alt="hotel reception"
                  /> -->
                <a href="./booking" class="cta-button text bottom" id="cta-button"
                   >Book now</a
                >
            </section>
        </main>


        <%@include file="/WEB-INF/include/footer.jsp" %>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="<%= request.getContextPath()%>/script/index.js"></script>
    </body>
</html>

