(() => {
    'use strict';
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => new bootstrap.Tooltip(el));
    document.querySelectorAll('form[method="post"]').forEach(form => {
        form.addEventListener('submit', event => {
            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            if (!form.checkValidity()) {
                event.preventDefault();
                form.reportValidity();
                return;
            }
            if (form.dataset.confirm && !window.confirm(window.UC24Translate ? window.UC24Translate(form.dataset.confirm) : form.dataset.confirm)) {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';
            form.querySelectorAll('button[type="submit"]').forEach(button => {
                button.disabled = true;
                button.setAttribute('aria-busy', 'true');
            });
        });
    });
    document.querySelectorAll('.modal[data-auto-open="true"]').forEach(el => new bootstrap.Modal(el).show());
})();
