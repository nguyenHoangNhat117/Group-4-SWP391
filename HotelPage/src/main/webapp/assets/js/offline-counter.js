(() => {
    document.querySelectorAll('select[name="percent"]').forEach(select => {
        const update = () => {
            const target = select.form.querySelector('[data-counter-due]');
            const amount = Number(select.selectedOptions[0]?.dataset.amount);
            if (target && Number.isFinite(amount)) {
                target.textContent = new Intl.NumberFormat(document.documentElement.lang === 'vi' ? 'vi-VN' : 'en-US').format(amount) + ' VND';
            }
        };
        select.addEventListener('change', update);
        document.addEventListener('hotel:languagechange', update);
        update();
    });
    document.querySelectorAll('.oc-form').forEach(form => {
        form.addEventListener('submit', event => {
            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';
            form.setAttribute('aria-busy', 'true');
            form.querySelectorAll('button[type="submit"]').forEach(button => { button.disabled = true; });
        });
    });
    window.addEventListener('pageshow', () => {
        document.querySelectorAll('.oc-form').forEach(form => {
            delete form.dataset.submitting;
            form.removeAttribute('aria-busy');
            form.querySelectorAll('button[type="submit"]').forEach(button => { button.disabled = false; });
        });
    });
})();

