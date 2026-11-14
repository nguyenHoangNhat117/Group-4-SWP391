/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

/*
 * Home-page reveal effects.
 * Business logic and other pages remain unchanged.
 */
(function () {
    "use strict";

    const hero = document.getElementById("hero");

    if (!hero) {
        return;
    }

    const body = document.body;
    const main = hero.closest("main");

    if (!main) {
        return;
    }

    document.documentElement.classList.add("luxe-home-page");

    const targets = Array.from(
        main.querySelectorAll(".text, .card")
    );

    // Remove legacy reveal states before initializing this implementation.
    targets.forEach(function (element) {
        element.classList.remove("hidden");
    });

    const animationsDisabled =
        body.classList.contains("disable-animation");

    const reducedMotion = window.matchMedia(
        "(prefers-reduced-motion: reduce)"
    ).matches;

    initRoomsCarousel(main, animationsDisabled || reducedMotion);

    if (!animationsDisabled) {
        initLobbyParallax(hero.querySelector("#hero-img"));
    }

    if (animationsDisabled
            || reducedMotion
            || !("IntersectionObserver" in window)) {
        return;
    }

    const observer = new IntersectionObserver(
        function (entries) {
            entries.forEach(function (entry) {

                if (!entry.isIntersecting) {
                    return;
                }

                entry.target.classList.add("is-visible");
                observer.unobserve(entry.target);
            });
        },
        {
            threshold: 0,
            rootMargin: "0px 0px -24px 0px"
        }
    );

    targets.forEach(function (element, index) {
        element.classList.add("home-reveal");

        if (element.classList.contains("text")) {
            element.classList.add("home-reveal--text");
        } else {
            element.classList.add("home-reveal--card");
        }

        element.style.transitionDelay =
            Math.min(index % 3, 2) * 90 + "ms";
    });

    body.classList.add("home-motion-ready");

    // Allow the initial reveal styles to render before observing.
    requestAnimationFrame(function () {
        requestAnimationFrame(function () {
            targets.forEach(function (element) {
                observer.observe(element);
            });
        });
    });
})();

function initLobbyParallax(frame) {
    if (!frame) {
        return;
    }

    const motion = window.matchMedia("(prefers-reduced-motion: no-preference)");
    frame.classList.add("lobby-parallax");
    const travel = 14;
    let updatePending = false;

    function scheduleUpdate() {
        if (updatePending || !motion.matches) {
            return;
        }

        updatePending = true;
        requestAnimationFrame(() => {
            updatePending = false;
            const { top, height } = frame.getBoundingClientRect();
            const progress = (window.innerHeight - top) / (window.innerHeight + height);
            const offset = (2 * Math.max(0, Math.min(1, progress)) - 1) * travel;
            frame.style.setProperty("--lobby-offset", `${offset}px`);
        });
    }

    window.addEventListener("scroll", scheduleUpdate, { passive: true });
    window.addEventListener("resize", scheduleUpdate);
    motion.addEventListener("change", scheduleUpdate);
    scheduleUpdate();
}

function initRoomsCarousel(main, instantScroll) {
    const track = main.querySelector(".rooms-carousel");
    const navigation = main.querySelector(".rooms-navigation");
    if (!track || !navigation) {
        return;
    }

    const previous = navigation.querySelector("[data-room-prev]");
    const next = navigation.querySelector("[data-room-next]");

    function updateButtons() {
        const maxScroll = track.scrollWidth - track.clientWidth;
        previous.setAttribute("aria-disabled", track.scrollLeft <= 1);
        next.setAttribute("aria-disabled", track.scrollLeft >= maxScroll - 1);
    }

    function move(button, direction) {
        if (button.getAttribute("aria-disabled") === "true") {
            return;
        }

        track.scrollBy({
            left: direction * track.clientWidth,
            behavior: instantScroll ? "instant" : "smooth"
        });
    }

    previous.addEventListener("click", () => move(previous, -1));
    next.addEventListener("click", () => move(next, 1));
    track.addEventListener("scroll", updateButtons, { passive: true });
    window.addEventListener("resize", updateButtons);
    updateButtons();
    navigation.hidden = false;
}
