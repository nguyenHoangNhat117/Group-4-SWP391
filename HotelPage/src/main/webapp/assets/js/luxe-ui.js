/* Luxe Escape - UI effects. Presentation only: never touches forms, data or existing handlers. */
(function () {
  'use strict';
  var d = document;
  function ready(fn) { d.readyState === 'loading' ? d.addEventListener('DOMContentLoaded', fn) : fn(); }

  ready(function () {
    var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var animOff = d.body.classList.contains('disable-animation');

    /* scroll progress bar + header state + back-to-top */
    var bar = d.createElement('div'); bar.className = 'lx-progress'; bar.setAttribute('aria-hidden', 'true');
    var top = d.createElement('button'); top.type = 'button'; top.className = 'lx-top';
    top.setAttribute('aria-label', 'Back to top'); top.innerHTML = '&#8593;';
    d.body.appendChild(bar); d.body.appendChild(top);
    top.addEventListener('click', function () { window.scrollTo({ top: 0, behavior: reduce ? 'auto' : 'smooth' }); });

    var header = d.querySelector('header.luxe-shared-header');
    var ticking = false;
    function onScroll() {
      var y = window.pageYOffset || d.documentElement.scrollTop;
      var h = d.documentElement.scrollHeight - window.innerHeight;
      bar.style.transform = 'scaleX(' + (h > 0 ? Math.min(y / h, 1) : 0) + ')';
      if (header) header.classList.toggle('is-scrolled', y > 8);
      top.classList.toggle('show', y > 600);
      ticking = false;
    }
    window.addEventListener('scroll', function () {
      if (!ticking) { ticking = true; window.requestAnimationFrame(onScroll); }
    }, { passive: true });
    onScroll();

    /* scroll reveal for inner pages. Landing page (#hero) keeps its own observer. */
    if (reduce || animOff || !('IntersectionObserver' in window) || d.getElementById('hero')) return;
    var sel = 'main .card, main .table-responsive, main .review-box, main .promo-card, main .promo-hero, main .glass-card, main form.rounded-4';
    var nodes = Array.prototype.filter.call(d.querySelectorAll(sel), function (el) {
      /* skip nested ones & anything inside a modal so only top-level blocks animate */
      return !el.closest('.modal') && !(el.parentElement && el.parentElement.closest(sel));
    });
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (e.isIntersecting) { e.target.classList.add('lx-in'); io.unobserve(e.target); }
      });
    }, { threshold: 0.08, rootMargin: '0px 0px -40px 0px' });
    nodes.forEach(function (el, i) {
      el.classList.add('lx-pre');
      el.style.transitionDelay = Math.min(i % 6, 5) * 70 + 'ms';
      io.observe(el);
    });
    /* safety net: never leave content hidden */
    setTimeout(function () { nodes.forEach(function (el) { el.classList.add('lx-in'); }); }, 4000);
  });
})();
