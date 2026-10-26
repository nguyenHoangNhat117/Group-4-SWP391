/* Luxe Escape - UI effects. Presentation only: never touches forms, data or existing handlers. */
(function () {
    'use strict';
    var d = document;
    function ready(fn) {
        d.readyState === 'loading' ? d.addEventListener('DOMContentLoaded', fn) : fn();
    }

    ready(function () {
        var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        var animOff = d.body.classList.contains('disable-animation');

        /* scroll progress bar + header state + back-to-top */
        var bar = d.createElement('div');
        bar.className = 'lx-progress';
        bar.setAttribute('aria-hidden', 'true');
        var top = d.createElement('button');
        top.type = 'button';
        top.className = 'lx-top';
        top.setAttribute('aria-label', 'Back to top');
        top.innerHTML = '&#8593;';
        d.body.appendChild(bar);
        d.body.appendChild(top);
        top.addEventListener('click', function () {
            window.scrollTo({top: 0, behavior: reduce ? 'auto' : 'smooth'});
        });

        var header = d.querySelector('header.luxe-shared-header');
        var ticking = false;
        function onScroll() {
            var y = window.pageYOffset || d.documentElement.scrollTop;
            var h = d.documentElement.scrollHeight - window.innerHeight;
            bar.style.transform = 'scaleX(' + (h > 0 ? Math.min(y / h, 1) : 0) + ')';
            if (header)
                header.classList.toggle('is-scrolled', y > 8);
            top.classList.toggle('show', y > 600);
            ticking = false;
        }
        window.addEventListener('scroll', function () {
            if (!ticking) {
                ticking = true;
                window.requestAnimationFrame(onScroll);
            }
        }, {passive: true});
        onScroll();

        /* scroll reveal for inner pages. Landing page (#hero) keeps its own observer. */
        if (reduce || animOff || !('IntersectionObserver' in window) || d.getElementById('hero'))
            return;
        var sel = 'main .card, main .table-responsive, main .review-box, main .promo-card, main .promo-hero, main .glass-card, main form.rounded-4';
        var nodes = Array.prototype.filter.call(d.querySelectorAll(sel), function (el) {
            /* skip nested ones & anything inside a modal so only top-level blocks animate */
            return !el.closest('.modal') && !(el.parentElement && el.parentElement.closest(sel));
        });
        var io = new IntersectionObserver(function (entries) {
            entries.forEach(function (e) {
                if (e.isIntersecting) {
                    e.target.classList.add('lx-in');
                    io.unobserve(e.target);
                }
            });
        }, {threshold: 0.08, rootMargin: '0px 0px -40px 0px'});
        nodes.forEach(function (el, i) {
            el.classList.add('lx-pre');
            el.style.transitionDelay = Math.min(i % 6, 5) * 70 + 'ms';
            io.observe(el);
        });
        /* safety net: never leave content hidden */
        setTimeout(function () {
            nodes.forEach(function (el) {
                el.classList.add('lx-in');
            });
        }, 4000);
    });
})();
/* =========================================================
   Simplified Dashboard and Reports presentation.
   Does not modify data, URLs, form values or business logic.
   ========================================================= */

(function () {
    "use strict";

    function simplifyHotelPresentation() {

        const dashboard = document.getElementById("hotel-dashboard");
        const reports = document.querySelector(".hotel-reports");

        if (!dashboard && !reports) {
            return;
        }

        const roots = [dashboard, reports].filter(Boolean);

        /*
         * Remove decorative arrows from clickable labels only.
         * Date ranges and other non-clickable content are untouched.
         */
        roots.forEach(function (root) {

            root.querySelectorAll("a, button").forEach(function (element) {

                const walker = document.createTreeWalker(
                    element,
                    NodeFilter.SHOW_TEXT
                );

                const textNodes = [];
                let current;

                while ((current = walker.nextNode())) {
                    textNodes.push(current);
                }

                textNodes.forEach(function (textNode) {
                    textNode.nodeValue = textNode.nodeValue.replace(
                        /[↗↖↘↙↑↓←→➜➝➞➟➠➤]/gu,
                        ""
                    );
                });

                element.querySelectorAll("span[aria-hidden='true']")
                    .forEach(function (span) {

                        if (!span.textContent.trim()
                                && span.children.length === 0) {
                            span.remove();
                        }
                    });
            });
        });

        if (!dashboard) {
            return;
        }

        /*
         * Retain essential reporting scope in short headings
         * instead of multiple subtitle lines.
         */
        const fromInput = dashboard.querySelector(
            "input[name='fromDate']"
        );

        const selectedYear = fromInput
            ? fromInput.value.slice(0, 4)
            : "";

        const revenueHeading = dashboard.querySelector(
            ".hd-revenue-panel h2"
        );

        if (revenueHeading) {
            revenueHeading.textContent = selectedYear
                ? "Revenue overview · " + selectedYear + " (VND)"
                : "Revenue overview (VND)";
        }

        const customerLabel = dashboard.querySelector(
            ".hd-kpi-sage .hd-kpi-top > span:first-child"
        );

        if (customerLabel) {
            customerLabel.textContent = "Total customers";
        }

        const roomChart = document.getElementById("hd-room-chart");

        if (roomChart) {
            const panel = roomChart.closest(".hd-panel");
            const heading = panel ? panel.querySelector("h2") : null;

            if (heading) {
                heading.textContent = "Current room status";
            }
        }

        const reviewPanel = dashboard.querySelector(
            ".hd-bottom-grid .hd-panel:nth-child(3)"
        );

        if (reviewPanel) {
            const heading = reviewPanel.querySelector("h2");

            if (heading) {
                heading.textContent = "Guest rating · Overall";
            }
        }

        /*
         * Replace the arrow-only review link with a readable label.
         * Its original destination remains unchanged.
         */
        const reviewLink = dashboard.querySelector(
            ".hd-review-total > a"
        );

        if (reviewLink) {
            reviewLink.textContent = "View reviews";
            reviewLink.setAttribute(
                "aria-label",
                "View review records"
            );
        }

        const monthlyValues = dashboard.querySelector(
            ".hd-data-details summary"
        );

        if (monthlyValues) {
            monthlyValues.textContent = "Monthly values";
        }

        /*
         * Keep the occupancy assumption available on hover
         * after removing its long visible caption.
         */
        const occupancyCard = dashboard.querySelector(".hd-kpi-clay");

        if (occupancyCard) {
            occupancyCard.setAttribute(
                "title",
                "Estimated from stored stay dates, assuming the currently "
                + "available room inventory throughout the selected period."
            );
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener(
            "DOMContentLoaded",
            simplifyHotelPresentation,
            {once: true}
        );
    } else {
        simplifyHotelPresentation();
    }
})();
