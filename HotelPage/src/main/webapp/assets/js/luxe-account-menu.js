(() => {
    'use strict';
    function init() {
        const header = document.querySelector('[data-luxe-header]');
        if (!header || header.dataset.luxeReady === 'true')
            return;
        header.dataset.luxeReady = 'true';
        header.classList.add('luxe-header-enhanced');
        const details = header.querySelector('[data-luxe-account]');
        const summary = details && details.querySelector('summary');
        const toggle = header.querySelector('[data-luxe-nav-toggle]');
        const navbar = header.querySelector('#navbar');
        if (details && summary) {
            summary.setAttribute('aria-expanded', String(details.open));
            details.addEventListener('toggle', () => {
                summary.setAttribute('aria-expanded', String(details.open));
                if (details.open && toggle) {
                    header.classList.remove('is-nav-open');
                    toggle.setAttribute('aria-expanded', 'false');
                }
            });
            document.addEventListener('click', event => {
                if (details.open && !details.contains(event.target))
                    details.open = false;
            });
            details.addEventListener('focusout', () => {
                setTimeout(() => {
                    if (!details.contains(document.activeElement))
                        details.open = false;
                }, 0);
            });
        }
        if (toggle && navbar) {
            toggle.addEventListener('click', () => {
                const open = header.classList.toggle('is-nav-open');
                toggle.setAttribute('aria-expanded', String(open));
                if (open && details)
                    details.open = false;
            });
            navbar.addEventListener('click', event => {
                if (event.target.closest('a')) {
                    header.classList.remove('is-nav-open');
                    toggle.setAttribute('aria-expanded', 'false');
                }
            });
        }
        document.addEventListener('keydown', event => {
            if (event.key !== 'Escape')
                return;
            if (details && details.open) {
                details.open = false;
                summary.focus();
            } else if (header.classList.contains('is-nav-open')) {
                header.classList.remove('is-nav-open');
                if (toggle) {
                    toggle.setAttribute('aria-expanded', 'false');
                    toggle.focus();
                }
            }
        });
    }
    if (document.readyState === 'loading')
        document.addEventListener('DOMContentLoaded', init, {once: true});
    else
        init();
})();

