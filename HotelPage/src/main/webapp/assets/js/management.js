(() => {
    'use strict';
    let pending = null;
    const dialog = document.createElement('dialog');
    dialog.className = 'management-confirm';
    dialog.setAttribute('aria-labelledby', 'management-confirm-title');
    dialog.innerHTML = '<h2 id="management-confirm-title"></h2><p></p><div class="confirm-actions"><button type="button" class="btn btn-outline-secondary" data-cancel></button><button type="button" class="btn btn-danger" data-accept></button></div>';
    document.body.append(dialog);
    const cancel = dialog.querySelector('[data-cancel]');
    const accept = dialog.querySelector('[data-accept]');
    const close = () => { dialog.close(); pending = null; };
    cancel.addEventListener('click', close);
    dialog.addEventListener('cancel', () => { pending = null; });
    accept.addEventListener('click', () => {
        const task = pending;
        close();
        if (task) task();
    });
    window.managementConfirm = (message, task) => {
        dialog.querySelector('h2').textContent = 'Xác nhận thao tác';
        dialog.querySelector('p').textContent = message;
        cancel.textContent = 'Hủy';
        accept.textContent = 'Xác nhận';
        pending = task;
        dialog.showModal();
        cancel.focus();
    };
    document.querySelectorAll('[data-confirm-trigger]').forEach(button => {
        button.addEventListener('click', () => button.form.requestSubmit());
    });
    document.addEventListener('submit', event => {
        const form = event.target;
        if (!form.matches('form[data-confirm]') || form.dataset.confirmed === 'true') return;
        event.preventDefault();
        event.stopImmediatePropagation();
        const submitter = event.submitter;
        let message = form.dataset.confirm;
        if (form.dataset.confirmTarget) {
            message = message.replace('{name}', () => form.dataset.confirmTarget);
        }
        if (form.dataset.roomNumber) message = 'Xóa phòng #' + form.dataset.roomNumber + '?';
        window.managementConfirm(message, () => {
            form.dataset.confirmed = 'true';
            form.requestSubmit(submitter || undefined);
            delete form.dataset.confirmed;
        });
    }, true);
    document.querySelectorAll('[data-page]').forEach(button => {
        button.addEventListener('click', () => {
            const url = new URL(location.href);
            url.searchParams.set('page', button.dataset.page);
            location.assign(url);
        });
    });
    document.querySelectorAll('[data-sort]').forEach(button => {
        const url = new URL(location.href);
        const direction = url.searchParams.get('sort');
        const th = button.closest('th');
        if (th) th.setAttribute('aria-sort', direction === 'asc' ? 'ascending' : direction === 'desc' ? 'descending' : 'none');
        const arrow = document.createElement('span');
        arrow.setAttribute('aria-hidden', 'true');
        arrow.textContent = direction === 'asc' ? '↑' : direction === 'desc' ? '↓' : '↕';
        button.append(arrow);
        button.addEventListener('click', () => {
            url.searchParams.set('sort', direction === 'asc' ? 'desc' : 'asc');
            url.searchParams.delete('page'); url.searchParams.delete('page-index');
            location.assign(url);
        });
    });
})();
