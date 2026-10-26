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